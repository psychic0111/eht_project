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
	view : {
		dblClickExpand : false,
		nameIsHTML : false
	},
	callback : {
		onClick : current_noteContent_Tag,
		beforeRightClick : this.tagSelectNodeBeforeRightClick,
		onRename : this.tagSelectNodeOnRename,
		onRightClick : this.tagnodeOnRightClick
	}
};
function tagnodeOnRightClick(){}
//选择当前tag标签
function current_noteContent_Tag(event, treeId, treeNode) {
	var temp = new Array();
	if(treeNode!=null&&treeNode.id=="tag_personal"){
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
	//如果节点下有子节点 则展开
	if (treeNode.children != null && treeNode.children.length != 0) {
		tag_zTree_Menu.expandNode(treeNode, true, true, true);
	} else {
		$("#noteForm_tagId").val(treeNode.id);
		do {
			temp.unshift(treeNode);
			if(treeNode!=null){
				treeNode = treeNode.getParentNode();
			}
		} while (treeNode != null && treeNode.isParent);

		var test = '';
		$.each(temp, function(i, node) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
			test += "-><span style='border: 2px solid rgb(213, 213, 213);'>"
					+ node.name + "</span>";
		});
		$("#tagSelectNode").html(test);
		hideTagMenu();
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
			&& event.target.id != "tagSelectTreeRightMenu") {
		hideTagMenu();
	}
}
//在弹出区域不需要隐藏树
$("#tagSelectContent").mouseover(function() {
	$("body").unbind("mousedown", onBodyTagDown);
}).mouseout(function() {
	$("body").bind("mousedown", onBodyTagDown);
});
//显示标签树
function selectTagTree() {
	var params = {
		"subjectid" : $('#noteForm_subjectId').val()
	};
	var url = webRoot + "/noteController/front/treeDataEdit.dht";
	AT.post(url, params,function(data) {
		$.fn.zTree.init($("#tagSelectTree"),noteContent_Tag_TreeSetting, data);tag_zTree_Menu = $.fn.zTree.getZTreeObj("tagSelectTree");
	});
	$("#tagSelectTree").html("");
	$("#tagSelectContent").css({
		left : "0px",
		top : "42px"
	}).slideDown("fast");
	$("body").bind("mousedown", onBodyTagDown);
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
	tagRightClickNode = node; 
	tagSelectMouseMenu(treeId, node); 
	return true;
}
//判断当前元素是否是confirm
function isShowConfirm(event) {
	var classname = $(event.target).attr("class");
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
			if(tagRightClickNode.id==$("#noteForm_tagId").val()){
				$("#tagSelectNode").html("");
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
		}
	}, true);
}

//============编辑区 标签选择 END=================================================================================>>>>>>> .r935
