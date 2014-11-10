
function subjectMember(){
	var subjectId = rightClickNode.id;
	var dataParam = "subjectId=" + subjectId;
	url = webRoot+"/subjectController/front/viewMembers.dht?" + dataParam;
	AT.load("teamMember",url,function(){
		var obj = $("#" + rightClickNode.tId + "_a");
		var left = obj.offset().left;
		var top = obj.offset().top - ($("#teamMember").height() / 2);
		$("#teamMember").css({position: "absolute",'top': top + 25,'left':left + 148});
		$("#teamMember").show();
		$("body").bind("mousedown", _member_onbodyDown);//绑定鼠标点击隐藏树窗口事件
	});
	hideRightMenu();
}

//鼠标页面点击事件
function _member_onbodyDown(event) {  
	$("#teamMember").fadeOut("fast");
	$("body").unbind("mousedown", _member_onbodyDown);
}

//右键菜单增加目录、子目录
function addChildDir(){
	var subjectId = rightClickNode.id;
	if(rightClickNode.dataType == "DIRECTORY"){
		subjectId = rightClickNode.subjectId;
	}
	var newNode = {id:"",name:"新目录",icon:imgPath+"/tree/folder.png",dataType:"DIRECTORY",subjectId:subjectId};
	newNode = zTree_Menu.addNodes(rightClickNode, newNode);
	hideRightMenu();
	zTree_Menu.editName(newNode[0]);
}

//右键菜单增加标签
function addChildTag(){
	var newNode = {id:"",name:"新标签",icon:imgPath+"/tree/tag_blue.png"};
	newNode = zTree_Menu.addNodes(rightClickNode, newNode);
	hideRightMenu();
	zTree_Menu.editName(newNode[0]);
}

function subjectManage(obj,obj1) {
	 if(isNoteStats()){//判断是否编辑状态
		   var submit = function (v, h, f) {
			if (v == true){ 
				subjectManageDo(obj,obj1); 
			} 
			return true;
		};
		// 自定义按钮
		$.jBox.confirm("您的条目尚未保存，被改动的内容将会丢失.是否确定离开？？", "提示", submit, { buttons: { '是': true, '否': false} });
	 }else{
		 subjectManageDo(obj,obj1);
	 }
}

//专题管理跳转
function subjectManageDo(obj,obj1){
	if($("#noteEditor_td").is(":visible")){
		hideNotePage();
	}
	var url='';
	if(obj1==1){
		url = webRoot+"/subjectController/front/viewAddSubject.dht?pageNo=1&pageSize=20";
	}else{
		 url = webRoot+"/subjectController/front/subjectManage.dht?pageNo=1&pageSize=20";
	}
	if(obj.id==-2){
		url=url+"&subjectType=2";
	}else{
		url=url+"&subjectType=1";
	}
	AT.load("iframepage",url,function() {
	});
	hideRightMenu();
}

//右键菜单删除
function deleteNode(){
	var msg = "";
	var url = "";
	var dataParam = "id=" + rightClickNode.id;
	var subjectId = rightClickNode.subjectId;
	var dataType = rightClickNode.dataType;
	var parentNode = rightClickNode.getParentNode();
	if (rightClickNode.name == '默认专题') {
		MSG.alert('默认专题不能删除');
		hideRightMenu();
		return false;
	}
	
	if(dataType == "SUBJECT"){
		subjectId = rightClickNode.id;
		msg = "确认要删除此专题吗？此操作不可恢复！";
		url = webRoot+"/subjectController/front/deleteSubject.dht?subjectId=" + subjectId;
	}
	
	if(dataType == "DIRECTORY"){ //目录节点
		//回收站中的目录
		if(rightClickNode.name != '回收站' && (rightClickNode.branchId == "RECYCLE" || rightClickNode.branchId == "RECYCLEP")){ //回收站节点
			msg = "确认要彻底删除此目录吗？";
			url = webRoot+"/directoryController/front/truncateDirectory.dht";
		}else{  //普通目录，刷新回收站
			msg = "确认要删除此目录吗？";
			url = webRoot+"/directoryController/front/delDirectory.dht";
		}
	}
	
	if(dataType == "TAG"){ //标签节点
		msg = "确认要删除此标签吗？";
		url = webRoot+"/tagController/front/deleteTag.dht";
	} 
	var submit = function(v, h, f) {
		if (v == true) {
			AT.post(url, dataParam, function(data){
				zTree_Menu.removeNode(rightClickNode);
				rightClickNode = null;
				if(dataType == "SUBJECT"){
					//重新加载个人回收站节点
					if(parentNode && parentNode.id=='-1'){
						var recycle_Node = zTree_Menu.getNodeByParam("id", "recycle_personal", null);
						if(recycle_Node){
							zTree_Menu.reAsyncChildNodes(recycle_Node, "refresh", true);
						}
					}
					if(parentNode &&  parentNode.children.length>0){
						zTree_Menu.selectNode(parentNode.children[0]);
						beforeNodeClick("treeMenu",parentNode.children[0]);
						onNodeClick(null,"treeMenu",parentNode.children[0]);
					}else{
						if(parentNode.id=='-2'){
							parentNode.isParent = true;
						}
						zTree_Menu.selectNode(parentNode);
						beforeNodeClick("treeMenu",parentNode);
						onNodeClick(null,"treeMenu",parentNode);
					}
					
				}
				if(dataType == "DIRECTORY"){ //目录节点
					//parentNode = zTree_Menu.getNodeByParam("id", subjectId, null);
					var rn = zTree_Menu.getNodeByParam("name", '回收站', parentNode);
					// 刷新个人专题回收站节点
					if(rn == null){
						var recycle_Node = zTree_Menu.getNodeByParam("id", "recycle_personal", null);
						recycle_Node.isParent = true;
						zTree_Menu.reAsyncChildNodes(recycle_Node, "refresh", true);
					}else{  // 刷新某多人专题下的回收站节点
						var recycle_Node = zTree_Menu.getNodeByParam("id", "recycle_subject_" + subjectId, null);
						recycle_Node.isParent = true;
						zTree_Menu.reAsyncChildNodes(recycle_Node, "refresh", true);
					}
					zTree_Menu.selectNode(parentNode);
					$("#" + parentNode.tId + "_a").addClass("curSelectedNode");
					beforeNodeClick("treeMenu",parentNode);
					onNodeClick(null,"treeMenu",parentNode);
				}
				if(dataType == "TAG"){
					if(parentNode){
						zTree_Menu.selectNode(parentNode);
						$("#" + parentNode.tId + "_a").addClass("curSelectedNode");
						beforeNodeClick("treeMenu",parentNode);
						onNodeClick(null,"treeMenu",parentNode);
					}
				}
				
			}, true);
			return true;
		}
		return true;
	};
	$.jBox.confirm(msg, "提示", submit, {
		buttons : {
			'确定' : true,
			'取消' : false
		}
	});
	hideRightMenu();
}
//右键菜单重名命
function renameNode(){
    rightClickNode.name=rightClickNode.name;
	zTree_Menu.editName(rightClickNode);
	hideRightMenu();
}

//还原目录
function restoreDirectory(){
	var url = webRoot+"/directoryController/front/restoreDirectory.dht";
	var dataParam = "id=" + rightClickNode.id.replace("_deleted", "");
	var subjectId = rightClickNode.subjectId;
	hideRightMenu();
	AT.post(url, dataParam, function(data){
		var removeDirNodes=data.obj.split(",");
		for(var i = 0; i < removeDirNodes.length; i++){
			var removeNode=	zTree_Menu.getNodeByParam("id", removeDirNodes[i]+"_deleted", null);
			zTree_Menu.removeNode(removeNode);
		}
		var parentNode = zTree_Menu.getNodeByParam("id", subjectId, null);
		zTree_Menu.reAsyncChildNodes(parentNode, "refresh", true);
	}, true);
}
//清空回收站
function deleteChildNodes(){
	var subjectId = rightClickNode.subjectId;
	var dataParam = "id=" + rightClickNode.id + "&subjectId=" + subjectId;
	msg = "确认要清空回收站所有数据吗？";
	url = webRoot+"/directoryController/front/truncateAll.dht";
	if(window.confirm(msg)){
		AT.post(url, dataParam, function(data){
			zTree_Menu.reAsyncChildNodes(rightClickNode, "refresh", true);
			hideRightMenu();
			showSearchDiv();
		}, true);
	}
}
//邀请成员
function treeToMemberManage(obj){
	url = webRoot+"/subjectController/front/memberManage.dht?id="+obj;
	if($("#noteEditor_td").is(":visible")){
		hideNotePage();
	}
	AT.load("iframepage",url,function() {
		hideRightMenu();
	});	
}
