//显示条目搜索的div
function showSearchDiv(){ 
	var topNode = findTopParentNode(selectInfo.curMenu);
	AT.load("iframepage",webRoot + "/noteController/front/noteIndex.dht?subjectId=" + selectInfo.subjectId+"&dirId="+selectInfo.searchDirIds+"&selectDirId="+selectInfo.dirId
			+"&tagId=" + selectInfo.tagId +"&deleted="+selectInfo.isDeleted+"&topNodeId="+topNode.id+"&newEnable="+selectInfo.newEnable,function(){
		showLoading_edit();
		//按钮权限判断
		buttonStatus(selectInfo.curMenu);
		//检索条目
		searchNotes(selectInfo.isDeleted);
	});

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
function searchNotes(deleted,unloadfirst){
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
	if(!tagId){
		tagId = '';
	}
	var frmId = "noteListIframe";
	var	url = webRoot + "/noteController/front/noteList.dht?pageNo=1&pageSize=20&subjectId=" + subjectId + "&dirId=" + dirId + "&searchInput=" + input + "&orderField=" + orderField + "&tagId=" + tagId 
			+ "&deleted=" + deleted+"&topNodeId="+topNodeId;
	AT.load(frmId,url,function(){
		if($("#firstNodeId").val()==null||$("#firstNodeId").val()==''){
			viewNote($("#firstNodeId").val(),true);
			viewNotePageAndButton();
			$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
		}else{
			if(!unloadfirst){
				try{
					viewNotePageAndButton();
				}catch(e){}
				$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
				viewNote($("#firstNodeId").val(),true);
			}
			
		}
		setTimeout('hideLoading_search()',100);
	});
}


function searchNotesclick() {
	 if(isNoteStats()){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				searchNotesclickDo(); 
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		 searchNotesclickDo();
	 }
}

//页面点击按钮搜索 和下拉列表搜索
function searchNotesclickDo(){
	searchNotes();
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
	 if(isNoteStats()){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				onTagClickDo(); 
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		 onTagClickDo();
	 }
}

//点击标签事件
function onTagClickDo(){
	var zTree = $.fn.zTree.getZTreeObj("tagTree");
	var nodes = zTree.getSelectedNodes();
	var v = nodes[0].name;
	var tagObj = $("#tagSelect");
	tagObj.attr("value", v);
	
	//找到所有叶标签
	var leafNodes = [];
	if(!nodes[0].isParent){
		leafNodes[0] = nodes[0];
	}else{
		leafNodes = zTree.getNodesByParam("isParent", false, nodes[0]);
	}
	var tagIds = [];
	for (var i=0, l=leafNodes.length; i<l; i++) {
		tagIds[i] = leafNodes[i].id;
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


function viewNoteclick(id) {
	 if(isNoteStats()){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				viewNoteclickDo(id); 
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		 viewNoteclickDo(id);
	 }
}

//查看一条note 页面点击
function viewNoteclickDo(id){
	viewNote(id);
}

//查看一条note
function viewNote(id,ishiden){
	showLoading_edit();
	clearAttaMore();
	if (id == null || id == ''){
		$("#note_edit").hide();
		$("#note_share").hide();
		$("#note_blacklist").hide();
		
		//清空内容页的数据
		try{
			$("#noteForm_id").val("");
			$("#noteTitleField").val("");
			$("#noteForm_version").val("");
			$("#noteForm_subjectId").val("");
			$("#noteForm_dirId").val("");
			$("#noteForm_tagId").val("");
			$("#htmlViewFrame").contents().find('body').html("");
			$("#divhiden").text("");
			noteEditor.setContent("");
		}catch(e){
		}
		setTimeout('hideLoading_edit()',100);
		return;
	}
	AT.post(webRoot+"/noteController/front/loadNote.dht?id=" + id, null,function(data) {
				var note = data['note'];
				var act = data['action'];
				var attaList = data['attaList'];//附件
				
				$("#noteForm_id").val(note.id);
				$("#noteTitleField").val(note.title);
				$("#noteForm_version").val(data['version']);
				$("#noteForm_subjectId").val(note.subjectId);
				$("#noteForm_dirId").val(note.dirId);
				$("#noteForm_tagId").val(note.tagId);
				$("#noteForm_createuser").val(note.createUserId);
				viewNotePageAndButton();

				//是否显示【更多】
				var isMore = data['isMore'];
				if(isMore=="true"){
					$("#attaMore").show();
				}else{
					$("#attaMore").hide();
				}
				if (data['type'] == '1') {
					$("#note_blacklist").hide();
					$("#note_edit").show();
					$("#deleteNote_btn").show();
					$("#note_share").show();
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
					} else {
						$("#note_edit").hide();
					}
					if (act.DELETE_NOTE == 'true') {
						$("#deleteNote_btn").show();
					} else {
						$("#deleteNote_btn").hide();
					}
				}
        
				$("#divhiden").text(note.content);
				$("#htmlViewFrame").contents().find('body').html(note.content);
				setHtmlDivHeight();
			$("#readpng_" + id).attr("src", imgPath+"/read.png");
			$("#note_status_" + id + " span").text("已读");
			
			//标签控制----------数据获取-隐藏选择功能----------
			spellTag(note.subjectId,note.tagId); 
			
			var residential = data['residential'];// 条目位置
			$("#noteSubjectName").text(residential);
			noteAttachment(attaList); 
	    	var node = zTree_Menu.getSelectedNodes()[0];
			if(node.branchId == 'RECYCLE' || node.dataType == 'RECYCLE' || node.branchId == 'RECYCLEP' || node.dataType == 'RECYCLEP'){ //回收站下
				$('#saveNote_btn').hide();
				$("#note_edit").hide();
				$("#restoreNote_btn").show();
				$("#historyNote_btn").hide();
				$('#attachment').hide();
				$('#comments_div').hide();
			}else{
			    $("#comments_div").show();
			}
			
	},false);
	 $("#comments_list").empty();
	$("#comment_img").attr("src", imgPath+ "/comments1a.png");
	setTimeout('hideLoading_edit()',100);
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
	var isShow = "none";
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
function spellTag(nodeid,tagId){
	var params = {"subjectid":nodeid,"tagId":tagId};  
	var url = webRoot + "/noteController/front/getTreePath.dht";
	AT.post(url, params, function(data){
		var ht = Array();
		var text = "";
		$.each(data, function(i, objVal) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
			ht.unshift(objVal);
        }); 
		$.each(ht, function(i, node) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
			if(node){
				text+="-><span style='border: 2px solid rgb(213, 213, 213);'>"+node.name+"</span>" 
			}
        }); 
    	$("#tagSelectNode").html(text);
	}); 
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
	
	// 提交按钮不可点
	$("#saveNote_btn").hide();
	
	$("#note_edit").attr("class", "Button4");//编辑条目按钮可以使用
	$("#note_edit").val("编辑条目");

	
	//隐藏标签功能
	$("#selectTag").hide();
	//隐藏附件功能
	$("#selectAta").hide();
	//隐藏附件删除按钮
	$("div#new_edit [src$='34aL_046.png']").hide();
	//隐藏上传附件功能
	$("#attachment").hide();  
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
		setHtmlDivHeight();
	}catch(e){
	}
}

function setHtmlDivHeight(){
	var iframe = document.getElementById("htmlViewFrame");
	var idoc = iframe.contentWindow && iframe.contentWindow.document || iframe.contentDocument; 
	var height = Math.max(idoc.body.scrollHeight, idoc.documentElement.scrollHeight);
	//if(UE.browser.ie){
		//$("#htmlViewFrame").height(height+550);
		//$("#htmlViewDiv").height(height+570);
	//}else{
	   //$("#htmlViewFrame").height(height+50);
		//$("#htmlViewDiv").height(height+70);
	//}
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
	$("#selectTag").show();
	$("#selectAta").show();
	//显示删除按钮
	$("div#new_edit [src$='34aL_046.png']").show();
	$("#attachment").show();
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

//判断权限， 按钮是否可用
function buttonStatus(node,noteId){
	var subjectNode = isShareSubject(node);
	if(subjectNode != null){
		var url = webRoot+"/indexController/front/subjectPermission.dht?subjectId=" + subjectNode.id+"&noteId="+noteId;
		AT.get(url, function(data){
			data = data[subjectNode.id];
			if(data.ADD_NOTE == 'true'){
				makeBtnAppear('note_new');
			}else{
				makeBtnDisappear('note_new');
			}
			if(data.UPDATE_NOTE == 'true'){
				makeBtnAppear('note_edit');
			}else{
				makeBtnDisappear('note_edit');
			}
			if(data.DELETE_NOTE == 'true'){
				makeBtnAppear('deleteNote_btn');
			}else{
				makeBtnDisappear('deleteNote_btn'); 
			}
		}, false);
	}else{
		if(node!=null&& (node.name=="多人专题" || node.name=="个人专题" )){
			makeBtnDisappear('note_new');
			makeBtnDisappear('note_edit');
			makeBtnDisappear('deleteNote_btn');
			
		}
	}
	
}


function addNewNote() {
	 if(isNoteStats()){//判断是否编辑状态
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
	if ($("#note_new").val() != "- 撤消条目") {
		var url = webRoot+"/noteController/front/noteUUid.dht";
		AT.get(url,function(data){
			if(data.success){
				noteEditor.setContent("");
				editNotePageAndButton();
				$("#divhiden").text('');//比对文本隐藏域设置为空
				$("#note_edit").hide();
				$("#noteTitleField").val("新建条目");
				//设置 专题主键、目录主键
				$("#noteForm_subjectId").val($("#note_subjectId").val());
				$("#noteForm_dirId").val($("#note_selectdirId").val());
				$("#noteForm_id").val(data.obj);
				$("#noteForm_version").val(1);
				$("#noteForm_tagId").val("");
				$("#noteForm_createuser").val("");
				$("#noteSubjectName").text(recurParentName(selectInfo.curMenu,""));
				UE.getEditor("note_editor").setContent("");
				UE.getEditor("note_editor").sync("noteForm");
				//新建条目
				$('#attachment').hide();
				$("#currAttachment1").html("");
				$('#tagSelectNode').html("");
				$("#note_new").val("- 撤消条目");
				$("#note_edit").val("编辑条目");
				$("#note_share").hide();
				
				clearAttaMore();
				$("#attaMore").hide();
			}
		},false);
	} else {
		$("#note_new").val("+ 新建条目");
		$("#noteTitleField").val("");
		$("#noteForm_tagId").val("");
		$("#noteForm_id").val("");
		noteEditor.setContent("");
		noteEditor.sync("noteForm");
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
			   //alert(uecontent);
			   //alert($("#divhiden").text());
			   //var issure=confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？");
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

