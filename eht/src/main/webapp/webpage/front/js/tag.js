//树对象
var tag_zTree_Menu = null;
//右键点击的节点
var tagRightClickNode = null;
//右键之前节点名 
var beforeNodeName = null;
//============================================编辑区 【标签】===============================================================
//标签树初始化
var noteContent_Tag_TreeSetting = {
	data : {
		simpleData : {
			enable : true
		}
	},
	check:{
		enable: true,
		chkboxType: { "Y": "", "N": "" }
	},
	view : {
		dblClickExpand : false,
		nameIsHTML : false
	},
	callback : {
		beforeClick: beforeTagClick,
		onClick : current_noteContent_Tag,
		beforeRightClick : this.tagSelectNodeBeforeRightClick,
		onRename : this.tagSelectNodeOnRename,
		onRightClick : this.tagnodeOnRightClick,
		onCheck : checkTagNode
	}
};

function checkTagNode(event, treeId, treeNode){
	var selected = !isSelected(treeId, treeNode); //返回的是点击后节点的check状态，所以非
	if(treeNode.children != null && treeNode.children.length > 0){
		//点击根节点判断效果
		/*if(treeNode.open){
			zTree_Menu.expandNode(treeNode, false);	
		}else{
			zTree_Menu.expandNode(treeNode, true);	
		}*/
	}
	if(treeNode.name != '添加标签'){
		if(selected){
			//取消选中当前tag
			tag_zTree_Menu.cancelSelectedNode(treeNode);
			$("#li_" + treeNode.id).remove();
			$("#" + treeNode.id).remove();
			/*for(var i= 0; i< selectTags.length; i++){
				if(selectTags[i].id == treeNode.id){
					selectTags.splice(i, 1);
					break;
				}
			}*/
			//return false;
		}else{
			tag_zTree_Menu.selectNode(treeNode, true);
			// 标签显示
			var displayName = "<font color='#aa33ff'>" + treeNode.name + "</font>";
			var parentNode = treeNode.getParentNode();
			while(parentNode != null && parentNode != ''){
				displayName = parentNode.name + " > " + displayName;
				parentNode = parentNode.getParentNode();
			}
			
			var tagLbl = $("<li onclick='selectTagTree()' class='note_tag' id='li_"+ treeNode.id +"'>"+ displayName +"</li>");
			$("#tagSelectNode").prepend(tagLbl);
			
			// 条目form中添加隐藏域
			var tagObj = $("<input type='hidden' name='noteTagId' id='" + treeNode.id + "' value='" + treeNode.id + "'/>");
			$("#noteForm").append(tagObj);
			
			//selectTags.push(treeNode);
		}
		return false;
	}else{
		return true;
	}
}

function tagnodeOnRightClick(){}

//判断节点是否为选中状态
function isSelected(treeId, treeNode){
	/*if(!!$("#" + treeNode.tId + "_a").attr("class")){
		if($("#" + treeNode.tId + "_a").hasClass("curSelectedNode")){
			return true;
		}
	}*/
	return treeNode.checked;
}

function beforeTagClick(treeId, treeNode, clickFlag){
	return true;
}
//追加选择当前tag标签
function current_noteContent_Tag(event, treeId, treeNode, clickFlag) {
	if(treeNode.name != '添加标签'){
		/*tag_zTree_Menu.cancelSelectedNode();
		setSelectedNodes();
		var selected = isSelected(treeId, treeNode);
		if(selected){
			// 标签显示
			if(!$("#li_"+ treeNode.id).attr("id")){
				var tagLbl = $("<li onclick='selectTagTree()' class='note_tag' id='li_"+ treeNode.id +"'>"+ treeNode.name +"</li>");
				$("#tagSelectNode").append(tagLbl);
			}
			// 条目form中添加隐藏域
			if(!$("#" + treeNode.id).attr("id")){
				var tagObj = $("<input type='hidden' name='noteTagId' id='" + treeNode.id + "' value='" + treeNode.id + "'/>");
				$("#noteForm").append(tagObj);
			}
		}*/
	}else{
		var newNode = {
				id : "",
				name : "新标签",
				icon : imgPath + "/tree/tag_blue.png"
			};
			newNode = tag_zTree_Menu.addNodes(null, newNode);
			tagSelectHideRightMenu();
			tag_zTree_Menu.editName(newNode[0]);
			return false;
	}
}

//隐藏标签树功能
function hideTagMenu() {
	$("#tagSelectContent").fadeOut("fast");
	$("body").unbind("mousedown", onBodyTagDown);
}
//鼠标页面点击事件
function onBodyTagDown(event) {
	if (isShowConfirm(event)) {
		return;
	}
	if (event.target.id != "tagSelectContent"
			&& event.target.id != "tagSelectTreeRightMenu"
				&& $(event.target).parents("#tagSelectContent").attr("id") == null) {
		hideTagMenu();
	}
}

//显示标签树
function selectTagTree() {
	var params = {
		"subjectid" : $('#noteForm_subjectId').val(),
		"noteId" : $('#noteForm_id').val()
	};
	var url = webRoot + "/noteController/front/treeDataEdit.dht";
	AT.post(url, params,function(data) {
		$.fn.zTree.init($("#tagSelectTree"),noteContent_Tag_TreeSetting, data);
		tag_zTree_Menu = $.fn.zTree.getZTreeObj("tagSelectTree");
		var tid = tag_zTree_Menu.getNodes()[0].tId;
		$("#" + tid + "_check").remove();
		tag_zTree_Menu.expandAll(true);
	});
	$("#tagSelectTree").html("");
	$("#tagSelectContent").css({
		left : "0px",
		top : "42px"
	}).slideDown("fast");
	$("body").bind("mousedown", onBodyTagDown);
}

//设置节点选中状态
function setSelectedNodes(){
	var nodes = tag_zTree_Menu.transformToArray(tag_zTree_Menu.getNodes());
	var selectedTags = $("#tagSelectNode > li");
	for(var i = 0; i < nodes.length; i++){
		for(var k = 0; k < selectedTags.length; k++){
			var tagId = $(selectedTags[k]).attr("id").substring(3);
			if(nodes[i].id == tagId){
				tag_zTree_Menu.selectNode(nodes[i], true);
				break;
			}
		}
	}
}

//-------------------------------------------------------------
//鼠标右键之前初始化
function tagSelectMouseMenu(treeId, node) {
	var showId = "tagSelectTreeRightMenu_ul_tag";
	//判断控件内是否有菜单存在
	if ($("#" + showId + " li") != null && $("#" + showId + " li").length > 0) {
		$("#" + showId).show();
		var obj = $("#" + node.tId + "_a");
		var top = obj.offset().top;
		var left = obj.offset().left;
		$("#tagSelectTreeRightMenu").css({
			position : "absolute",
			'top' :top - 200,
			'left' :left - 560
		});
		$("#tagSelectTreeRightMenu").show();
	}
	$("body").bind("mousedown", tagSelectOnBodyClick);
}

//鼠标注右键点击前, 准备菜单
function tagSelectNodeBeforeRightClick(treeId, node,event) {
	if(node.name == '添加标签'){
		return false;
	}
	tagRightClickNode = node; 
	tagSelectMouseMenu(treeId, node); 
	return true;
}
//判断当前元素是否是confirm
function isShowConfirm(event) {
	var classname = $(event.target).attr("class");
	if(typeof(classname) == 'undefined'){
		classname = '';
	}
	var bool = (classname.indexOf("jbox") >-1);
	return bool;
}
//右键菜单关闭方法（）
function tagSelectHideRightMenu() {
	$("#tagSelectTreeRightMenu").hide();
	//tagRightClickNode = null; 
	$("body").unbind("mousedown", tagSelectOnBodyClick);
}

//鼠标页面点击事件
function tagSelectOnBodyClick(event) {
	if (event.target.id != "tagSelectTreeRightMenu_add_tag"
			&& event.target.id != "tagSelectTreeRightMenu_rename_tag"
			&& event.target.id != "tagSelectTreeRightMenu_delete_tag") {
		tagSelectHideRightMenu();
	}
} 

//右键菜单增加标签
function tagaddChildTag() {
	var newNode = {
		id : "",
		name : "新标签",
		icon : imgPath + "/tree/tag_blue.png"
	};
	newNode = tag_zTree_Menu.addNodes(tagRightClickNode, newNode);
	tagSelectHideRightMenu();
	tag_zTree_Menu.editName(newNode[0]);
}
//右键菜单重名命
function tagRenameNode() { 
	beforeNodeName = tagRightClickNode.name;
	tag_zTree_Menu.editName(tagRightClickNode);
	tagSelectHideRightMenu();
}

//右键菜单删除
function tagDeleteNode() {
	var url = "";
	var dataParam = "id=" + tagRightClickNode.id;
	var dataType = tagRightClickNode.dataType;
	if (dataType == "TAG") { //标签节点
		url = webRoot + "/tagController/front/deleteTag.dht";
	} 
	var submit = function(v, h, f) {
		if (v == true) {
			if(!!$("#li_" + tagRightClickNode.id)){
				$("#li_" + tagRightClickNode.id).remove();
				$("#" + tagRightClickNode.id).remove();
			} 
			AT.post(url, dataParam, function(data) {
				if (data == true) {
					tag_zTree_Menu.removeNode(tagRightClickNode);
					//获取专题树对象
					zTree_Menu = $.fn.zTree.getZTreeObj("treeMenu");
					//根据id拿到标签节点对象
					var pNode = zTree_Menu.getNodeByParam("id", tagRightClickNode.id, null);
					//删除左边专题树上的对应标签
					zTree_Menu.removeNode(pNode);
				}
			}, true);
		}
		return true;
	};
	$.jBox.confirm("确认要删除此标签吗？", "提示", submit, {
		buttons : {
			'确定' : true,
			'取消' : false
		}
	});
}
//编辑节点名称前
function tagSelectNodeOnRename(e, treeId, node) { 
	var nodeName = node;
	var parentNode = node.getParentNode();
	if(node.name!=null){
		nodeName = node.name.replace(/(^\s*)|(\s*$)/g, "");
	}
	//判断是否是新标签
	if (nodeName.length==0||nodeName=="新标签") {
		tag_zTree_Menu.removeNode(node);
		return false;
	}
	//判断重命名---------------第一层----------------
	var treeObj = $.fn.zTree.getZTreeObj("tagSelectTree");
	var node1 = treeObj.getNodesByFilter(function (nodea) { return (nodea.level == 0)&&(nodea.name==nodeName)});
	var nodeMore =  null;
	//如果不是第一层
	if(parentNode!=null){
		 nodeMore = treeObj.getNodesByParam("name", nodeName, node.getParentNode());
	}
	//第一层||第二层以下N层
	if(node1.length>1||(nodeMore!=null&&nodeMore.length>1)){
		if(beforeNodeName!=null){
			tagRightClickNode.name=beforeNodeName;
			tag_zTree_Menu.editName(tagRightClickNode);
			beforeNodeName = null;
		}else{
			tag_zTree_Menu.removeNode(node);
		}
		$.jBox.error('同级节点不能重名！', 'jBox'); 
		return false;
	} 
	var parentNode = node.getParentNode();
	var subjectId = "";
	var parentId = "";
	if(parentNode!=null){ 
			parentId = parentNode.id; 
	}
	//个人标签
	if($("#note_topNodeId").val()== "-2"){
		subjectId = $("#note_subjectId").val();
	} 
	var dataParam = "id=" + node.id + "&name=" + node.name + "&subjectId="
			+ subjectId + "&parentId=" + parentId;
	var url = webRoot + "/tagController/front/saveTag.dht";
	AT.post(url, dataParam, function(data) {
		if (data.status != 'undefined' && data.status != null
				&& data.status != '' && data.status == 500) { 
			tag_zTree_Menu.editName(node);
		} else {
			//添加后设置目录节点的ID
			node.id = data.id;
			node.dataType = "TAG";
			node.branchId = data.subjectId;
			node.pId = data.pId;
			//获取专题树对象
			zTree_Menu = $.fn.zTree.getZTreeObj("treeMenu");
			//根据当前标签id从专题树对象里拿到标签对象
			var treeNode = zTree_Menu.getNodeByParam("id", data.id, null);
			//如果专题树不存在此id标签则需要新建标签
			if(treeNode==null){
				//新建标签标签对象的父id
				var innerPid = "tag_personal";//默认个人总标签节点
				if($("#note_topNodeId").val()=="-2"){
					innerPid = "tag_subject_" + $("#note_subjectId").val();//如果是多人
				}
				//如果是在叶节点标签创建的 则拿到这个标签的id作为父节点id
				if(tagRightClickNode!=null){
					innerPid =  tagRightClickNode!=null?tagRightClickNode.id:innerPid;
				}
				treeNode = {
						id : data.id,
						dataType : "TAG",
						branchId: data.subjectId,
						name : node.name,
						icon : imgPath + "/tree/tag_blue.png"
					}; 
				//拿父节点
				var pNode = zTree_Menu.getNodeByParam("id", innerPid, null);
				//在pNode节点下插入新标签
				zTree_Menu.addNodes(pNode, treeNode);
			}else{
				treeNode.name = node.name;
				zTree_Menu.updateNode(treeNode); 
			}
			tag_zTree_Menu.updateNode(node);
			
			tag_zTree_Menu.cancelSelectedNode();
			setSelectedNodes();
		}
	}, true);
}

//============编辑区 标签选择 END=================================================================================>>>>>>> .r935
//树对象
var dir_zTree_Menu = null;
//标签树初始化
var noteContent_Dir_TreeSetting = {
	data : {
		simpleData : {
			enable : true
		}
	},
	view : {
		dblClickExpand : false,
		nameIsHTML : false
	},
	callback : {
		onClick : current_noteContent_Dir
	}
};

//
function current_noteContent_Dir(event, treeId, treeNode, clickFlag) {
	$("#noteSubjectName").text(recurDirParentName(treeNode,""));
	if(treeNode.dataType=='SUBJECT'){
		$("#noteForm_dirId").val("");
	}else{
		$("#noteForm_dirId").val(treeNode.id);
	}
	hideDirMenu();
}

function recurDirParentName(node,subjectName){
	if(node!= null&&node.dataType!='SUBJECT'){
		subjectName =node.name + "/"+subjectName;
		return recurDirParentName(node.getParentNode(),subjectName);
	}else{
		if(node.branchId=='-1'){
			subjectName ='个人专题'+ "/"+node.name + "/"+subjectName;
		}else{
			subjectName ='多人专题'+ "/"+node.name + "/"+subjectName;
		}
		
		return subjectName;
	}
}
//隐藏标签树功能
function hideDirMenu() {
	$("#dirSelectContent").fadeOut("fast");
	$("body").unbind("mousedown", onBodyDirDown);
}
//鼠标页面点击事件
function onBodyDirDown(event) {
	if (isShowConfirm(event)) {
		return;
	}
	if (event.target.id != "dirSelectContent"
			&& event.target.id != "dirSelectTreeRightMenu"
				&& $(event.target).parents("#dirSelectContent").attr("id") == null) {
		hideDirMenu();
	}
}

function selectDirTree(){
	var params = {
			"subjectId" : $('#note_subjectId').val()
		};
		var url = webRoot + "/subjectController/front/treeDataEdit.dht";
		AT.post(url, params,function(data) {
			$.fn.zTree.init($("#dirSelectTree"),noteContent_Dir_TreeSetting, data);
			dir_zTree_Menu = $.fn.zTree.getZTreeObj("dirSelectTree");
			dir_zTree_Menu.expandAll(true);
		});
		$("#dirSelectTree").html("");
		$("#dirSelectContent").css({
			left : "0px",
			top : "112px"
		}).slideDown("fast");
		$("body").bind("mousedown", onBodyDirDown);
}
