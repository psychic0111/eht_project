var rightMenu = null;
var zTree_Menu = null;
var rightClickNode = null;//右键节点
var currentSubjectId = null;
var editorheight = null;
var editorWidth=null;
var noteEditor = null;
var currentdirAttachmentURL = webRoot+"/subjectController/front/dirAttaManage.dht";
/*
 * 这个布尔值用于 判断编辑条目保存时 beforeNodeClick 和 onNodeClick 事件重复弹出而设计
 * 当beforeNodeClick弹出提示以后onclickAndBefore=true 否则onclickAndBefore = false;
 * */
var onclickAndBefore = true;

function SelectInfo(){
	this.curMenu = null; 
	this.subjectId = "";//当前节点的subjectid
	this.searchDirIds = ""; //当前节点可用于检索的目录id
	this.dirId = ""; //当前节点的目录id，如果有的话
	this.userId = ""; //创建人Id
	this.isDeleted = 0;
	this.tagId = "";//标签id
	this.newEnable = true;//是否可以新建数据
} 
var selectInfo = new SelectInfo();

var treeSetting = {
	edit: {
		enable: true,
		showRemoveBtn: false,
		showRenameBtn: false,
		drag: {
			autoExpandTrigger: false,
			isCopy: false,
			isMove: false,
			prev: true,
			next: true,
			inner: true,
			minMoveSize: 5,
			borderMax: 10,
			borderMin: -5,
			maxShowNodeNum: 5,
			autoOpenTime: 500
		}
	},
	
	view: {
		showLine: false,
		showTitle: false,
		selectedMulti: true,
		dblClickExpand: false,
		nameIsHTML: true
	},
	data: {
		simpleData: {
			enable: false
		}
	},
	callback: {
		beforeClick: this.beforeNodeClick,  //点击节点前触发事件
		onRightClick: this.nodeOnRightClick,//点击右键触发事件
		beforeRightClick: this.nodeBeforeRightClick,//点击右键前触发事件
		onClick: this.onNodeClick,//点击节点触发事件
		beforeRename:this.zTreeBeforeRename,//改名字前验证触发事件
		onRename: this.nodeOnRename,//改名字后触发事件
		onAsyncSuccess: this.zTreeOnAsyncSuccess,//异步加载后触发事件
		onExpand: this.onExpand//展开事件
	},
	async : {
	    autoParam : ["id", "dataType", "branchId"],
	    contentType : "application/x-www-form-urlencoded",
	    dataFilter : null,
	    dataType : "text",
	    enable : true,
	    type : "post",
	    url : webRoot+"/indexController/front/reloadNode.dht"

	}
}; 
function onNodeClick(e, treeId, node) {
	 if(node.name == "我的标签" || node.name == "专题标签"){
		 return false;
	 }
	 onNodeClickDo(e, treeId, node);
}

//树节点click事件
function onNodeClickDo(e, treeId, node) {
	 
	if(!onclickAndBefore)return;
	var showD = true;

	if(node.dataType == "SUBJECT"){   //专题条目检索
		var subjectId = node.id;
		selectInfo = new SelectInfo();
		selectInfo.curMenu = node;
		selectInfo.subjectId = subjectId; 
		if(showD){
			if(!$("#noteEditor_td").is(":visible")){
				showNotePage();
			}
			showSearchDiv();
		}
	}else if(node.dataType == "DIRECTORY"){   //专题条目检索
		if(!isDocumentFolder(node)){
			if(!$("#noteEditor_td").is(":visible")){
				showNotePage();
			}
			selectInfo = new SelectInfo();
			var dirId = node.id.replace("_deleted", "");  // 回收站中的目录ID都加了 _deleted后缀
			selectInfo.dirId = dirId;
			if(node.branchId != 'RECYCLEP' && node.branchId != 'RECYCLE'){//拼接所有子目录的id
				var nodes = zTree_Menu.getNodesByParam("dataType", "DIRECTORY", node);
				var ids = getSonIds(nodes);
				var strids = node.id;
				if(ids!=null){
					strids = strids + ","+ids.join(",");
				}
				selectInfo.searchDirIds = strids;
				selectInfo.subjectId = node.subjectId;
			}else{
				selectInfo.isDeleted = 1;   // 查询回收站条目
				selectInfo.searchDirIds = dirId;
				selectInfo.dirIds = node.id;
				selectInfo.newEnable = false;
			}
			selectInfo.curMenu = node;
			if(node.name != '文档资料'){
				selectInfo.dirId = dirId;
				showSearchDiv();
			}
		}else{
			//处理文档类
				if($("#noteEditor_td").is(":visible")){
					hideNotePage();
				}
				currentDirId = node.id; 
				dirAttachmentManage(node.subjectId);
		}
	}else if(node.dataType == 'RECYCLEP' || node.dataType == 'RECYCLE'){
		if(!$("#noteEditor_td").is(":visible")){
			showNotePage();
		}
		selectInfo = new SelectInfo();
		selectInfo.curMenu = node;
		selectInfo.dirIds =  "recycle_personal";
		selectInfo.searchDirIds = "recycle_personal";
		selectInfo.newEnable = false;
		var parentNode = node.getParentNode();
		var subjectId = "";				
		if(node.dataType == 'RECYCLEP'){
			subjectId = ""; //个人回收，包括所有个人专题
		}else{
			subjectId = parentNode.id;
		}
		selectInfo.isDeleted = 1;
		selectInfo.subjectId = subjectId;
		showSearchDiv();
	}else if(node.dataType == "TAG"){
		if(!$("#noteEditor_td").is(":visible")){
			showNotePage();
		}
		selectInfo = new SelectInfo();
		selectInfo.curMenu = node;
		selectInfo.newEnable = false;
		//选择专题树下的标签时查询当前标签下的数据
		 if(node.name!='我的标签' || node.name!='专题标签'){
			 selectInfo.tagId = node.id;
		 }else{
			 selectInfo.tagId = "";
		 }
		 selectInfo.isDeleted = 0;
		 if(node.subjectId){
			 selectInfo.subjectId = node.subjectId;
		 }
		 showSearchDiv();
	}else if(node.dataType == "MSG"){   //消息中心
		selectInfo = new SelectInfo();
		selectInfo.curMenu = node;
		var msgType = "";
		var dataId = node.id;
		if(dataId == '-101'){
			msgType = '1';
		}
		if(dataId == '-103'){
			msgType = '2';
		} 
		if(dataId == '-100'){
			var picId = node.tId;
			picId = picId+"_ico";
			$("#"+picId).removeClass("button sys_ico_docu").addClass("msg_ico_open newPriSubjectPic"); 
		} 
		//默认选中未读信息
		if(msgType==''){
			var parentNode = zTree_Menu.getNodeByParam("id", "-102", zTree_Menu.getNodes()[2]);
			zTree_Menu.selectNode(parentNode);
		}
		if($("#noteEditor_td").is(":visible")){
			hideNotePage();
		}
		var url = webRoot+"/messageController/front/messageList.dht?msgType=" + msgType;
		AT.load("iframepage",url,function() {
		});
	}else if(node.dataType == "REMENBERCHILD"){
		selectInfo = new SelectInfo();
		selectInfo.newEnable = false;
		selectInfo.subjectId = node.subjectId;
		selectInfo.curMenu = node;
		selectInfo.userId=node.id;
		if(!$("#noteEditor_td").is(":visible")){
			showNotePage();
		}
		showSearchDiv();
	}else if(node.id == "addDirectory"){
		//添加目录
		var parentNode = node.getParentNode();
		var newNode = {id:"",name:"新目录",icon:imgPath+"/tree/folder.png"};
		newNode = zTree_Menu.addNodes(parentNode, newNode, true);
		zTree_Menu.editName(newNode[0]);
	}else{
		selectInfo = new SelectInfo();
		selectInfo.newEnable = false;
	}
	
}
//个人标签  多人标签 消息标签 点击时候修改样式
function changeCss(node){
	var picId = node.tId;
	picId = picId+"_ico";
	$("#"+picId).removeClass("button").addClass("newPriSubjectPic");
	
}
//销毁已经构建好的树，如果已经构建了的话。
function destoryTree(){
	if (zTree_Menu!=null){
		var nodes2 = zTree_Menu.getNodes();
		for(var i = 0; i < nodes2.length; i ++){
			zTree_Menu.removeNode(nodes2[i]);
		}
		zTree_Menu.destroy();
	}
	$.fn.zTree.destroy("treeMenu");
}


function beforeNodeClick(treeId, node) {
	if (node.level === 0) {
		beforeNodeClickDo(treeId, node);
		return false;
	}
	 beforeNodeClickDo(treeId, node);
}

//点击节点前
function beforeNodeClickDo(treeId, node) { 
	if (node.isParent) {
		if (node.level === 0) {
			var pNode = selectInfo.curMenu;
			while (pNode && pNode.level !==0) {
				pNode = pNode.getParentNode();
			}
			if (pNode !== node) {
				try{
					if(pNode.tId!=null){
						var a = $("#" + pNode.tId + "_a");
						a.removeClass("cur");
					}
				}catch(e){} 
			}
			a = $("#" + node.tId + "_a");
			
			//点击根节点判断效果
			if(node.open){
				zTree_Menu.expandNode(node, false);	
			}else{
				zTree_Menu.expandNode(node, true);	
			}
			changeCss(node);
		} else {
			zTree_Menu.expandNode(node, true, false, false, true);
		}
	}
	return true;
}

function nodeOnRightClick(e, treeId, node){
	//鼠标右键事件
	zTree_Menu.selectNode(node);
	
} 
function nodeBeforeRightClick(treeId, node) {
     nodeBeforeRightClickDo(treeId, node);
	 onclickAndBefore = true;
	 return true;
}

//鼠标注右键点击前, 准备菜单
function nodeBeforeRightClickDo(treeId, node){
	if(node.level == 0){
		return false;
	}
	rightClickNode = node;
	if(selectInfo.curMenu != node){
		//先执行click事件
		beforeNodeClick("treeMenu",node);
		onNodeClick(null,"treeMenu",node);
	}
	var subjectNode = isShareSubject(node);
	// 返回null, 是个人专题
	if(subjectNode == null){
		$("#treeRightMenu").html(rightMenu);
		mouseMenu(treeId, node);
		return true;
	}else{
		var url = webRoot + "/indexController/front/mouseMenu.dht?subjectId=" + subjectNode.id;
		AT.get(url, function(data){
			$("#treeRightMenu").html(data);
			mouseMenu(treeId, node);
		}, false);
	}
	return true;
}

//鼠标右键事件
function mouseMenu(treeId, node){
	var hideObj = null;
	var showId = null;
	if(node.dataType == 'SUBJECT'){   //专题
		hideObj = [$("#treeRightMenu_ul_directory"),$("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycle"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_attachment"),$("#treeRightMenu_ul_MemberManage")];
		showId = "treeRightMenu_ul_subject";
	}else if(node.dataType == 'DIRECTORY'){  //目录
		if(rightClickNode.name != '回收站' && (node.branchId == 'RECYCLEP' || node.branchId == 'RECYCLE')){  //回收站
			hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_directory"), $("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_attachment"),$("#treeRightMenu_ul_MemberManage")];
			showId = "treeRightMenu_ul_recycle";
		}else if(isDocumentFolder(rightClickNode)){
			hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_directory"), $("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_recycle"),$("#treeRightMenu_ul_MemberManage")];
			showId = "treeRightMenu_ul_attachment";
			//文档资料文件夹不能删除,不能重命名
			if(rightClickNode.name == '文档资料'){
				$("#treeRightMenu_delete_attadir").hide();
				$("#treeRightMenu_rename_attadir").hide();
			}
		}else{
			var params = {'id':node.branchId};
			hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycle"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_attachment"),$("#treeRightMenu_ul_MemberManage")];
			showId = "treeRightMenu_ul_directory";
			$("#"+showId + " #treeRightMenu_black_dir").remove();
			AT.post(webRoot+"/subjectController/front/checkSubjectType.dht",params,function(data){
				if(data.obj==2){
					followNodeId=node.tId + "_a";//弹出框定位，根据节点偏移来定位
					$("#treeRightMenu_rename_dir").after('<li id=\"treeRightMenu_black_dir\" onclick=\"blackListdir(\''+node.id+'\',\''+node.branchId+'\')\">黑名单设置</li>');
				}
			});
		}
	}else if(node.dataType == 'TAG'){  //标签
		hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_directory"), $("#treeRightMenu_ul_recycle"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_attachment"),$("#treeRightMenu_ul_MemberManage")];
		showId = "treeRightMenu_ul_tag";
	}else if(node.dataType == 'RECYCLEP' || node.dataType == 'RECYCLE'){
		hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_directory"), $("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycle"), $("#treeRightMenu_ul_attachment"),$("#treeRightMenu_ul_MemberManage")];
		showId = "treeRightMenu_ul_recycleRoot";
	}else if(node.dataType == 'REMENBER'){
		
		hideObj = [$("#treeRightMenu_ul_subject"),$("#treeRightMenu_ul_directory"), $("#treeRightMenu_ul_tag"), $("#treeRightMenu_ul_recycle"), $("#treeRightMenu_ul_recycleRoot"), $("#treeRightMenu_ul_attachment")];
		showId = "treeRightMenu_ul_MemberManage";
	}
	if(hideObj != null && showId != null){
		for(var i=0;i<hideObj.length;i++){
			hideObj[i].hide();
		}
		//判断控件内是否有菜单存在
		if($("#" + showId + " li") != null && $("#" + showId + " li").length > 0){
			$("#" + showId).show();
			if("treeRightMenu_ul_tag"==showId && ("我的标签"==node.name || "专题标签"==node.name)){
				$("#treeRightMenu_rename_tag").hide();
				$("#treeRightMenu_delete_tag").hide();
			}
			if("treeRightMenu_ul_subject"==showId && "默认专题"==node.name){
				$("#treeRightMenu_delete_subject").hide();
			}
			var obj = $("#" + node.tId + "_a");
			var top = obj.offset().top - 15;
			var left = obj.offset().left;
			$("#treeRightMenu").css({position: "absolute",'top':top, 'left':left + 60});
			$("#treeRightMenu").show();
		}
	}
	$("body").bind("mousedown", onBodyClick);
}

function getSonIds(nodes){
	if(nodes!=null){
		var ids = [];
		for(var i = 0; i < nodes.length; i++){
			ids[i + 1] = nodes[i].id.replace("_deleted", "");
		}
		return ids;
	}
	return null;
}

//条目附件管理跳转
function dirAttachmentManage(subjectId){ 
	url = currentdirAttachmentURL+"?dirId=" +currentDirId  + "&subjectId=" + subjectId;
	AT.load("iframepage",url,function() {});
} 

//重命名之前名称 长度验证 --于浩
function zTreeBeforeRename(treeId, treeNode, newName){
	if (newName.length > 30) {
		MSG.alert("节点名称长度不能超过30.");
		zTree_Menu.editName(treeNode);
		return false;
	}
  	if (newName.length == 0) {
		MSG.alert("节点名称不能为空.");
		zTree_Menu.editName(treeNode);
		return false;
	}
  	if(treeNode.id!=""){
		if(newName == "新目录" || newName == "新标签"){
			MSG.alert("节点名称不能为新目录或新标签.");
			zTree_Menu.editName(treeNode);
	  		return false;
	  	}
	}
	return true;
}

//编辑节点名称前
function nodeOnRename(e, treeId, node){
		if(node.id==""){
			if(node.name == "新目录" || node.name == "新标签"){
		  		zTree_Menu.removeNode(node);
		  		return;
		  	}
		}
		var parentId = "", subjectId = "";
		var parentNode = node.getParentNode();
		if(parentNode.dataType == "TAG"){  //标签
			if(parentNode.id == 'tag_personal'){  //个人标签
				// parentId和subjectId
			}else if(parentNode.branchId == "tag_personal"){
				parentId = parentNode.id;
			}else if(node.id!=null&&node.id.indexOf('tag_subject')!= -1){ //专题标签
				subjectId = parentNode.branchId;
			}else{
				subjectId = parentNode.branchId;
				parentId = parentNode.id;
			}
			var dataParam = "id=" + node.id + "&name=" + node.name + "&subjectId=" + subjectId + "&parentId=" + parentId;
			var url = webRoot+"/tagController/front/saveTag.dht";
			AT.post(url, dataParam, function(data){
				if(data.status != 'undefined' && data.status != null && data.status != '' && data.status == 500){
					zTree_Menu.editName(node);
				}else{
					//添加后设置目录节点的ID
					node.id = data.id;
					node.dataType = "TAG";
					node.branchId = data.subjectId;
					node.pId = data.pId;
					zTree_Menu.updateNode(node);
					beforeNodeClick("treeMenu",node);
					onNodeClick(null,"treeMenu",node);
				}
			}, true);
		}else{
			if(parentNode.dataType == "SUBJECT"){  //父节点为专题
				subjectId = parentNode.id;
			} else if(parentNode.dataType == "DIRECTORY"){  //父节点为目录
				subjectId = parentNode.branchId;
				parentId = parentNode.id;
			}
			
			var dataParam = "id=" + node.id + "&dirName=" + node.name + "&subjectId=" + subjectId + "&parentId=" + parentId;
			var url = webRoot+"/directoryController/front/saveDirectory.dht";
			AT.post(url, dataParam, function(data){
				if(data.status != 'undefined' && data.status != null && data.status != '' && data.status == 500){
					zTree_Menu.editName(node);
				}else{
					//添加后设置目录节点的ID
					node.id = data.id;
					node.dataType = "DIRECTORY";
					node.branchId = data.subjectId;
					node.subjectId = data.subjectId;							
					node.pId = data.Pid;
					zTree_Menu.updateNode(node);
					beforeNodeClick("treeMenu",node);
					k(null,"treeMenu",node);
				}
			}, true);
		}
}

//加载成功后
function zTreeOnAsyncSuccess(event, treeId, treeNode, msg){
	if(treeNode!=null && treeNode.dataType && findSubjectNode(treeNode)){
		addDirForOneNode(treeNode);
	}
}
//专题添加跳转
function toAddSubject(subjectType){
	url = webRoot+"/subjectController/front/viewAddSubject.dht?subjectType=" + subjectType;
	AT.load("iframepage",url,function() {});	
	hideRightMenu();
}

function onExpand(event, treeId, treeNode)  {
	if(treeNode.dataType=='REMENBER'&&treeNode.open){
		var nodes = treeNode.children;
		for(var i=0;i<nodes.length;i++){
			var params = {'userId':nodes[i].id,'subjectId':nodes[i].subjectId,'tid':nodes[i].tId};
			AT.post(webRoot+"/noteController/front/showcount.dht",params,function(data){
				$("#diyBtn_"+data.userId+"_"+data.subjectId).remove();
				var aObj = $("#" + data.tId + '_a');
			    var editStr = "<span id='diyBtn_" +data.userId+"_"+data.subjectId+ "' >"+"("+data.total+")"+"</span>";
				aObj.append(editStr);
			},true);
		}
	}
	if(treeNode.dataType=='TAG'&&treeNode.open){
		//if(treeNode.id=='tag_personal'){
		//	return;
		//}
		//if(treeNode.id.indexOf('tag_subject')!=-1&&treeNode.level==2){
		//	return;
		//}
		var nodes = treeNode.children;
		for(var i=0;i<nodes.length;i++){
			var params = {'id':nodes[i].id,'tid':nodes[i].tId};
			AT.post(webRoot+"/tagController/front/showcount.dht",params,function(data){
				$("#diyBtn_"+data.id).remove();
				var aObj = $("#" + data.tId + '_a');
				var editStr = "<span id='diyBtn_" +data.id+ "' >"+"("+data.total+")"+"</span>";
				aObj.append(editStr);
			},true);
		}
		
	}
	return true;
}