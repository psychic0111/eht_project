var switchNote = true;

//显示条目搜索的div
function showSearchDiv(){ 
	var topNode = findTopParentNode(selectInfo.curMenu);
	AT.load("iframepage",webRoot + "/noteController/front/noteIndex.dht?subjectId=" + selectInfo.subjectId+"&dirId="+selectInfo.searchDirIds+"&selectDirId="+selectInfo.dirId
			+"&tagId=" + selectInfo.tagId +"&deleted="+selectInfo.isDeleted+"&topNodeId="+topNode.id+"&newEnable="+selectInfo.newEnable+"&userId="+selectInfo.userId,function(){
		//showLoading_edit();
		//按钮权限判断
		buttonStatus(selectInfo.curMenu);
		//检索条目
		searchNotes(selectInfo.isDeleted);
	});

}

//判断权限， 按钮是否可用
function buttonStatus(node,noteId){
	if(selectInfo.newEnable ==false){
		makeBtnDisappear('note_new');
	return;
   }else{
	   if($("#restoreNote_btn").is(":hidden")){
		}else{
			makeBtnDisappear('note_new');
			 return;
		}
	  
   }
	
	var subjectNode = isShareSubject(node);
	if(subjectNode != null && subjectNode != -1){
		var url = webRoot+"/indexController/front/subjectPermission.dht?subjectId=" + subjectNode.id+"&noteId="+noteId;
		AT.get(url, function(data){
			data = data[subjectNode.id];
				if(data.ADD_NOTE == 'true'){
					makeBtnAppear('note_new');
				}else{
					makeBtnDisappear('note_new');
				}
		}, false);
	}else if(subjectNode == -1){
		var n = zTree_Menu.getNodeByParam("name", "默认专题", null);
		zTree_Menu.selectNode(n);
		beforeNodeClick("treeMenu",n);
		onNodeClick(null,"treeMenu",n);
		
		//editNotePageAndButton();
		$("#note_edit").hide();
		$("#note_blacklist").hide();
		$("#noteTitleField").val("");
		
		$("input[name='noteTagId']").remove();
		$("#tagSelectNode").empty();
		
		$("#noteForm_createuser").val("");
		$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
		UE.getEditor("note_editor").setContent("");
		UE.getEditor("note_editor").sync("noteForm");
		//新建条目
		$("#currAttachment1").html("");
		$('#tagSelectNode').html("");
		$("#note_edit").val("编辑条目");
		$("#note_share").hide();
		
		clearAttaMore();
		$("#attaMore").hide();
		
		$('#attachment').show();
		$("#selectTag").show();
		
		$("#divhiden").text('');
		$("#htmlViewFrame").height(0);
		$("#htmlViewFrame").contents().find('body').html('');
		setHtmlDivHeight();
		$("#note_new").show();
	}else{
		$("#note_new").show();
	}
}




function showSearchDivDo(){}


function findTopParentNode(node){
	var pNode = node;
	while (pNode && pNode.level !==0) {
		pNode = pNode.getParentNode();
	}
	return pNode;
}

//条目搜索,根据检索词、标签、时间排序还是标题排序、个人专题还是多人专题、专题id、目录id这条条件检索条目
//deleted =true 搜索回收站的条目，否则搜索正在使用中的条目
//unloadfirst true,不加载第一条条目
function searchNotes(deleted,unloadfirst,undir){
	showLoading_search(); 
	clearAttaMore();
	if(deleted){
		deleted = 1;
	}else{
		deleted = 0;
	}
	var input = $("#searchNoteField").val();//检索词
	var orderField = $("#noteOrderField").val();//排序字段
	var subjectId = $("#note_subjectId").val();//专题id
	var dirId = $("#note_dirId").val();//目录id
	var tagId = $("#note_tagId").val();//标签id
	var topNodeId = $("#note_topNodeId").val();//顶级节点主键
	var userId = $("#note_userId").val();//顶级节点主键
	if(!input){
		input = '';
	}
	input = encodeURIComponent(input);
	if(!orderField){
		orderField = '';
	}
	
	if(!subjectId){
		subjectId = '';
	}
	if(!dirId){
		dirId = '';
	}
	if(undir&&dirId!='recycle_personal'){
		dirId = '';
	}
	if(!tagId){
		tagId = '';
	}
	var frmId = "noteListIframe";
	var	url = webRoot + "/noteController/front/noteList.dht?pageNo=1&pageSize=20&subjectId=" + subjectId + "&dirId=" + dirId + "&searchInput=" + input + "&orderField=" + orderField + "&tagId=" + tagId 
			+ "&deleted=" + deleted+"&topNodeId="+topNodeId+"&userId="+userId;
	AT.load(frmId,url,function(){
			if(!unloadfirst){
				if(switchNote){
					viewNote($("#firstNodeId").val(),subjectId);
				}
			}
		setTimeout('hideLoading_search()',100);
	});
}


function searchNotesclick() {
		 searchNotesclickDo();
}

//页面点击按钮搜索 和下拉列表搜索
function searchNotesclickDo(){
	searchNotes(selectInfo.isDeleted,true,true);
}

//显示第一条条目
function showFirstView(){
	
}

var tagTreeSetting = {
		view : {
			dblClickExpand : false,
			nameIsHTML: false
		},
		data : {
			simpleData : {
				enable : true
			}
		},
		callback : { 
			onClick : onTagClick
		}
	}; 

function onTagClick() {
		 onTagClickDo();
}

//点击标签事件
function onTagClickDo(){
	var zTree = $.fn.zTree.getZTreeObj("tagTree");
	var nodes = zTree.getSelectedNodes();
	var v = nodes[0].name;
	var tagObj = $("#tagSelect");
	tagObj.attr("value", v);
	var tagIds = [];
	//找到所有叶标签
	var leafNodes = [];
	if(!nodes[0].isParent){
		tagIds[0] = nodes[0].id;
	}else{
		tagIds[0] = nodes[0].id;
		leafNodes = zTree.getNodesByParam("isParent", false, nodes[0]);
		for (var i=0;i<leafNodes.length; i++) {
			tagIds[i+1] = leafNodes[i].id;
		}
	}
	$("#note_tagId").val(tagIds.join(","));
	searchNotes();
	hideM();
}
	
//标签过滤树
function buildTagTree() {
	var params = {
		"subjectid" : $('#note_subjectId').val()
	};
	var url = webRoot+"/noteController/front/treeData.dht";
	AT.post(url, params, function(data) {
		$.fn.zTree.init($("#tagTree"), tagTreeSetting, data);
	});
}
//显示标签树
function showTagTree() {
	buildTagTree();//初始化树
	var tagSelect = $("#tagSelect");//获取触发弹出树元素id
	var tagOffset = tagSelect.offset();
	$("#tagContent").css({left:tagOffset.left + "px", top:tagOffset.top + tagSelect.outerHeight() + 2 + "px"}).slideDown("fast");
	$("body").bind("mousedown", _index_onbodyDown);//绑定鼠标点击隐藏树窗口事件
}
//鼠标页面点击事件
function _index_onbodyDown(event) {  
	if (event.target!=null&&(event.target.id.indexOf("tagTree")==-1&&event.target.id != "tagContent")) { 
		hideM();//隐藏过滤表签树
	}
}
//隐藏标签树功能
function hideM() {
	$("#tagContent").fadeOut("fast");
	$("body").unbind("mousedown", _index_onbodyDown);
}
//在弹出区域不需要隐藏树
$("#tagContent").mouseover(function(){
	$("body").unbind("mousedown", _index_onbodyDown);
}).mouseout(function(){
	$("body").bind("mousedown", _index_onbodyDown);
});


function viewNoteclick(id,subjectId) {
	 if(isNoteStats()){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				viewNoteclickDo(id,subjectId); 
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		viewNoteclickDo(id,subjectId);
	 }
}

//查看一条note 页面点击
function viewNoteclickDo(id,subjectId){
	viewNote(id,subjectId);
}

//查看一条note
function viewNote(id,subjectId){
	showLoading_edit();
	clearAttaMore();
	// 关闭分享、黑名单窗口
	if(!!$("#easyDialogWrapper").attr("id")){
		$("#easyDialogWrapper").remove();
	}
	AT.post(webRoot+"/noteController/front/loadNote.dht?id=" + id+"&subjectId=" + subjectId, null,function(data) {
		if (data['id'] == null || data['id'] == ''){
				$("#note_new").show();
				$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));	
				setTimeout('hideLoading_edit()',100);
				return;
		}else{
			var note = data['note'];
			var act = data['action'];
			var attaList = data['attaList'];//附件
			$("#noteForm_id").val(note.id);
			$("#noteTitleField").val(note.title);
			$("#noteForm_version").val(data['version']);
			$("#noteForm_subjectId").val(note.subjectId);
			$("#noteForm_dirId").val(note.dirId);
			$("#noteForm_createuser").val(note.createUserId);
			viewNotePageAndButton();
			//是否显示【更多】
			var isMore = data['isMore'];
			if(isMore=="true"){
				$("#attaMore").show();
			}else{
				$("#attaMore").hide();
			}
			var node = zTree_Menu.getSelectedNodes()[0];
			if(node.branchId == 'RECYCLE' || node.dataType == 'RECYCLE' || node.branchId == 'RECYCLEP' || node.dataType == 'RECYCLEP'){ //回收站下
				$("#note_share").hide();
				$("#note_new").hide();
				$('#saveNote_btn').hide();
				$("#note_edit").hide();
				$("#restoreNote_btn").show();
				$("#historyNote_btn").hide();
				$('#attachment').hide();
				$('#comments_div').hide();
			}else{
				$("#restoreNote_btn").hide();
				if (data['type'] == '1') {
					$("#note_blacklist").hide();
					$("#note_edit").show();
					$("#deleteNote_btn").show();
					$("#note_share").show();
					$('#attachment').show();
					$("#selectTag").show();
				} else {
					$("#note_share").hide();
					// 如果是创建人 则有共享条目功能
					if (note.createUserId == sessionuserid) {
						$("#note_blacklist").show();
					} else {
						$("#note_blacklist").hide();
					}
					makeBtnAppear('note_edit');
					makeBtnAppear('deleteNote_btn');
					if (act.UPDATE_NOTE == 'true') {
						$("#note_edit").show();
						$('#attachment').show();
						$("#selectTag").show();
					} else {
						$("#note_edit").hide();
					}
					if (act.DELETE_NOTE == 'true') {
						$("#deleteNote_btn").show();
					} else {
						$("#deleteNote_btn").hide();
					}
					
				}
				//加载评论
				var id=$("#noteForm_id").val();
				var params = {'noteId':id};
				AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
					 $("#comments_list").empty();
					 $("#comments_list").append(data);
				});
			}
			$("#divhiden").text(note.content);
			$("#htmlViewFrame").height(0);
			$("#htmlViewFrame").contents().find('body').html(note.content);
			setHtmlDivHeight();
			$("#readpng_" + id).attr("src", imgPath+"/read.png");
			$("#note_status_" + id + " span").text("已读");
			var residential = data['residential'];// 条目位置
			$("#noteSubjectName").text(residential);
			noteAttachment(attaList); 
			currentNoteCss(id);
		}
			
	},true);
	//标签控制----------数据获取-隐藏选择功能----------
	spellTag(id); 
	$("#comments_list").empty();
	$("#comment_img").attr("src", imgPath+ "/comments1a.png");
	setTimeout('hideLoading_edit()',100);
	switchNote = false; // 点击节点不切换条目内容页
}

function currentNoteCss(noteId){
	var div_title = $("#contentListUl .title");
	$(div_title).each(function(i, item){
		if($(item).attr("id") == 'title_' + noteId){
			$(item).addClass("cur_title");
		}else{
			$(item).removeClass("cur_title");
		}
	});
	
	var div_content = $("#contentListUl .contents");
	$(div_content).each(function(i, item){
		if($(item).attr("id") == 'content_' + noteId){
			$(item).addClass("cur_content");
		}else{
			$(item).removeClass("cur_content");
		}
	});
	
}

//附件【更多】按钮
function showButtonMore(o){
	if($(o).html()=="更多"){
		$("#attMoreDIV").fadeIn(1000);
		loadAttachment("all");
		$(o).html("收起");
	}else{
		$("#attMoreDIV").fadeOut(100);
		$(o).html("更多");
	}
}
 
//查看一条note
function loadAttachment(searchtype){
	if(searchtype==null){
		searchtype = "all";
	}
	var id = $("#noteForm_id").val();
	AT.post(webRoot+"/noteController/front/loadAttachment.dht?id=" + id + "&searchtype="+searchtype, null,function(data) {
				var attaList = data['attaList'];//附件
				var isMore = data['isMore'];
				if(isMore=="true"){
					$("#attaMore").show();
				}else{ 
					$("#attaMore").hide();
				}
				noteAttachment(attaList,searchtype);  
	},false); 
}

//获取当前note的附件（searchType:current前7条     searchType：all 所有）
function noteAttachment(attaList,searchType){
	var temp = $("#currAttachment1");
	if(searchType!=null&&searchType=="all"){
		temp = $("#attMoreDIV");
	}
	temp.html("");
	var text = "";
	var isShow = "";
	if($("#saveNote_btn").css("display")!="none"){
		isShow= "";
	};
	if(attaList){
		$.each(attaList, function(i, attachment) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。
			var attaStr = attachment.fileName!=null&&attachment.fileName.length>5?attachment.fileName.substring(0,4)+'...':attachment.fileName;
			text+=
				'<span style="display: inline-block;margin-right:10px;" title="'+attachment.fileName+'">'+
				'<input type="hidden" value="'+attachment.id+'" name="filename">'+
				"<span onclick='downloadByid(\""+attachment.id+"\")'><a href='javascript:;' style='color: #1F8919;'>"+attaStr+"</a>&nbsp;</span>"+
				'<span onclick="removeCurrAttachment(this)" attaid="'+attachment.id+'">'+
				'<img style="width:12px;display:'+isShow+'" src="'+window.imgPath +'/34aL_046.png" >'+
				'</span>'+
		 		'</span>';
            }); 
	}
	temp.html(text);
}

//拼接标签位置
function spellTag(noteId){
	var url = webRoot+"/noteController/front/loadNoteTags.dht?noteId=" + noteId;
	$("#tagSelectNode").empty();
	$("input[name='noteTagId']").remove();
	AT.get(url, function(data){
		$("#tagSelectNode_hidden").empty();
		var maxWidth = $("#tagSelectNode").parents("div .Edit_others").width() - 220;
		for(var i = 0; i < data.length; i ++){
			var node = data[i];
			var displayName = "<font color='#aa33ff'>" + node.name + "</font>";
			var parentNode = node.tagEntity;
			while(parentNode != null && parentNode != ''){
				displayName = parentNode.name + " > " + displayName;
				parentNode = parentNode.tagEntity;
			}
			var text = "<li onclick='selectTagTree()' class='note_tag' id='li_" + node.id + "'>"+displayName+"</li>";
			var w = $("#tagSelectNode").width();
			if(w < maxWidth){
				$("#tagSelectNode").append($(text));
				$("#tagSelectNode_hidden").append($("<li class='note_tag'>"+displayName+"</span>"));
			}else{
				if(!$("#tag_more").attr("id")){
					$("#tagSelectNode").append($("<li onclick='toggleTagMore()' class='tag_more' id='tag_more'>显示所有</li>"));
				}
				$("#tagSelectNode_hidden").append($("<li id='hid_"+ node.id +"' class='note_tag'>"+displayName+"</span>"));
			}
			
			// 条目form中添加隐藏域
			var tagObj = $("<input type='hidden' name='noteTagId' id='" + node.id + "' value='" + node.id + "'/>");
			$("#noteForm").append(tagObj);
		}
	}, true);
}

//在新建条目前，处理内容页信息
function befornewNote(){
	//设置 专题主键、目录主键
	$("#noteForm_subjectId").val($("#note_subjectId").val());
	$("#noteForm_dirId").val($("#note_selectdirId").val());
	//设置位置
	$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
}
function clearAttaMore(){
	//初始化附件【更多】按钮 
	$("#attaMore").html("更多");
	$("#attMoreDIV").html("");
	$("#attMoreDIV").hide();
}

//条目只读时候页面显示和按钮状态
function viewNotePageAndButton(){
	//标题不可编辑
	$("#noteTitleField").attr("disabled", "disabled");
	$("#selectDir").hide();
	// 提交按钮不可点
	$("#saveNote_btn").hide();
	
	$("#note_edit").attr("class", "Button4");//编辑条目按钮可以使用
	$("#note_edit").val("编辑条目");

	
	//隐藏标签功能
	//$("#selectTag").hide();
	//隐藏附件功能
	//$("#selectAta").hide();
	//隐藏附件删除按钮
	//$("div#new_edit [src$='34aL_046.png']").hide();
	//隐藏上传附件功能
	//$("#attachment").hide();  
	//没有条目不显示分享按钮
	if($("#noteForm_id").val()==""){
		$("#note_share").hide();
		$("#deleteNote_btn").hide();
		$("#note_edit").hide();
		$("#comments_div").hide();
	}else{
		//多人专题的条目不分享
		if($("#note_topNodeId").val()=='-1'){
			$("#note_share").show();
		}else{
			$("#note_share").hide();
		}
		$("#note_edit").show();
		$("#deleteNote_btn").show();
		//显示评论
		$("#comments_div").show();
	}
	
	//如果当前选中节点不属于任何专题，或者是回收站、标签节点，则不可以新建条目
	$("#note_new").val("+ 新建条目");
	if($("#note_newEnable").val()=="false"){
		$("#note_new").hide();
	}else{
		$("#note_new").show();
	}
	
	//黑名单
	if($("#note_topNodeId").val()!='-1'){
		if ($("#noteForm_createuser").val() == sessionuserid) {
			$("#note_blacklist").show();
		} else {
			$("#note_blacklist").hide();
		}
	}else{
		$("#note_blacklist").hide();
	}
	
	try{
		// 内容不可编辑
		noteEditor.hide();
		$("#htmlViewDiv").show();
		$("#parentHtmlViewDiv").show();
		window.setTimeout('setHtmlDivHeight();', 400);
	}catch(e){
	}
}

function setHtmlDivHeight(){
	var iframe = document.getElementById("htmlViewFrame");
	var idoc = iframe.contentWindow && iframe.contentWindow.document || iframe.contentDocument; 
	var height = Math.max(idoc.body.clientHeight, idoc.documentElement.clientHeight);
	var width = Math.max(idoc.body.clientWidth, idoc.documentElement.clientWidth);
	if(UE.browser.ie){
		$("#htmlViewFrame").height(idoc.documentElement.scrollHeight);
		$("#htmlViewFrame").width(idoc.documentElement.scrollWidth);
		$("#htmlViewDiv").height(height);
		$("#htmlViewDiv").width(width);
	}else{
	   $("#htmlViewFrame").height(idoc.documentElement.scrollHeight);
	   $("#htmlViewFrame").width(idoc.documentElement.scrollWidth);
	   $("#htmlViewDiv").height(height);
	   $("#htmlViewDiv").width(width);
	}
}

//条目编辑时候页面显示和按钮状态
function editNotePageAndButton(){
	$("#noteTitleField").removeAttr("disabled");//标题可编辑
	//内容可编辑
	$("#note_editor").show();
	noteEditor.show();
	$("#htmlViewDiv").hide();
	$("#parentHtmlViewDiv").hide();
	
	//展现保存按钮
	$("#saveNote_btn").show();
	//条目ID有值时，删除按钮才有用
	if($("#noteForm_id").val() != null && $("#noteForm_id").val() != ''){
		$("#deleteNote_btn").show();
	}else{
		$("#deleteNote_btn").hide();
	}
	//$("#selectTag").show();
	//$("#selectAta").show();
	//显示删除按钮
	//$("div#new_edit [src$='34aL_046.png']").show();
	//$("#attachment").show();
	if($("#topNodeId").val()=='-1'){
		$("#note_share").show();
	}else{
		$("#note_share").hide();
	}
	
	//编辑按钮
	$("#note_edit").attr("class", "Button3");
	$("#note_edit").val("返回阅读");
	$("#note_edit").show();
	
	$("#comments_div").hide();
}
//显示附件删除按钮（）
function attaDeletebuttonShow(){
	
}

//显示附件删除按钮（隐藏）
function attaDeletebuttonHiden(){
	
}

function recurParentName(node,subjectName){
	if(node.id != '-1'&&node.id != '-2'){
		subjectName =node.name + "/"+subjectName;
		return recurParentName(node.getParentNode(),subjectName);
	}else{
		subjectName =node.name + "/"+subjectName;
		return subjectName;
	}
}

function makeBtnDisappear(btnId){
	$("#"+btnId).hide();
	$("#"+btnId).attr("id",btnId+"_hidden");
}

function makeBtnAppear(btnId){
	$("#"+btnId+"_hidden").attr("id",btnId);
	$("#"+btnId).show();
}

function addNewNote() {
	 if(isNoteStats() && $("#note_new").val() != "- 撤消条目"){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				addNewNotedo();
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		 addNewNotedo();
	 }
}

function addNewNotedo() {
	$("#selectDir").hide();
	if ($("#note_new").val() != "- 撤消条目") {
		var uuid = Math.uuid().replace(/\-/g,"");
		noteEditor.setContent("");
		editNotePageAndButton();
		$("#divhiden").text('');//比对文本隐藏域设置为空
		$("#note_edit").hide();
		$("#noteTitleField").val("新建条目");
		//设置 专题主键、目录主键
		$("#noteForm_subjectId").val($("#note_subjectId").val());
		$("#noteForm_dirId").val($("#note_selectdirId").val());
		$("#noteForm_id").val(uuid);
		$("#noteForm_version").val(1);
		
		//$("#noteForm_tagId").val("");
		$("input[name='noteTagId']").remove();
		$("#tagSelectNode").empty();
		
		$("#noteForm_createuser").val("");
		$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
		UE.getEditor("note_editor").setContent("");
		UE.getEditor("note_editor").sync("noteForm");
		//新建条目
		/*$('#attachment').hide();*/
		$("#currAttachment1").html("");
		$('#tagSelectNode').html("");
		$("#note_new").val("- 撤消条目");
		$("#note_edit").val("编辑条目");
		$("#note_share").hide();
		
		clearAttaMore();
		$("#attaMore").hide();
		
		$('#attachment').show();
		$("#selectTag").show();
	} else {
		$("#note_new").val("+ 新建条目");
		$("#noteTitleField").val("");
		
		//$("#noteForm_tagId").val("");
		$("input[name='noteTagId']").remove();
		$("#tagSelectNode").empty();
		
		$("#noteForm_id").val("");
		noteEditor.setContent("");
		noteEditor.sync("noteForm");
		$("#htmlViewFrame").contents().find('body').html("");
		viewNotePageAndButton();
		$("#note_edit").val("编辑条目");
		$("#note_edit").hide();
		
	}

}

//条目回收，同时回收目录
function restoreNote(id){
	if(!id){
		id = $("#noteForm_id").val();
	}
	var url = webRoot+"/noteController/front/restoreNote.dht?id=" + id;
	AT.post(url, null, function(data){
	if(data.dirId!=''){
			var removeDirNodes=data.dirId.split(",");
			for(var i = 0; i < removeDirNodes.length; i++){
				var removeNode=	zTree_Menu.getNodeByParam("id", removeDirNodes[i]+"_deleted", null);
				zTree_Menu.removeNode(removeNode);
			}
  			var parentNode = zTree_Menu.getNodeByParam("id", data.subjectId, null);
			zTree_Menu.reAsyncChildNodes(parentNode, "refresh", true);
  		}
		searchNotes(1);
	});
}

//判断条目是否是编辑状态
function isNoteStats(){
	var edui1=$(".edui-editor");
	if(edui1.length>0){
		if(edui1.is(":visible")){
		   var uecontent=noteEditor.getContent();
		   if(uecontent!=$("#divhiden").text()){
				return true;
		   }
		}
	}
	return false;
}

//----------------logging tree----------
function showLoading_tree(){
	if(document.getElementById("pageloading_tree").style.display=='none'){
		$("#pageloading_tree").show(); 
	}
}
function hideLoading_tree(){
	if(document.getElementById("pageloading_tree").style.display!='none'){
		$("#pageloading_tree").hide(); 
	}
}
 
//----------------logging search----------
function showLoading_search(){
	$("#pageloading_search").show(); 
}
function hideLoading_search(){
	$("#pageloading_search").hide(); 
}

//----------------logging edit----------
function showLoading_edit(){
	$("#pageloading_edit").show(); 
}
function hideLoading_edit(){
	$("#pageloading_edit").hide(); 
}


//获取条目存储目录
function getNoteImgPath(){
	var noteImgPath="notes/"+$("#noteForm_subjectId").val()+"/"+$("#noteForm_id").val()+"/files/img";
	return noteImgPath;
}

