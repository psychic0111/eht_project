
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ page import="com.eht.subject.entity.SubjectEntity"%>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>e划通</title>  
<%@  include file="/webpage/front/include/front_common.jsp" %> 
<style type="text/css">
.ztree li a.level0 {
	width:200px!important;
	margin-bottom:1px!important;
	height:32px!important;
	padding-left:10px!important;
	padding-top:10px!important;
	background:url(${webRoot}/webpage/front/images/bg_menu1a.png) repeat-x!important;
	border:1px #d5d5d5 solid!important;
	text-decoration:none!important;
	color:#333!important;
}
.ztree li a.level0.cur {
	width:200px;
	margin-bottom:1px;
	height:32px;
	padding-left:10px;
	padding-top:10px;
	border:1px #d5d5d5 solid;
	text-decoration:none;
	background:url(${webRoot}/webpage/front/images/bg_menu1b.jpg) repeat-x;
	color: white;
}
.ztree li a.level0 span {
	display: block;
	padding-top:3px;
	font-size:14px;
	font-weight: bold;
	word-spacing: 2px;
}
.ztree li a.level0 span.button {
	float:left;
	margin-left: 10px;
	visibility: visible;
}
.ztree li span.button.switch.level0 {
	display:none;
}

.numb{
    color: red;
    float: right;
    height: 20px;
    margin-right: 10px !important;
    margin-top: -17px !important;
    text-align: center;
    width: 21px;
}
.gear{
	background:url(${imgPath}/gear.png) no-repeat !important;
    color: #FFFFFF;
    float: right;
    height: 24px;
    margin-right: 10px !important;
    margin-top: -17px !important;
    text-align: center;
    width: 24px;
}
.newPriSubjectPic{ 
    border: 0 none;
    cursor: pointer;
    display: inline-block;
    height: 32px;
    line-height: 0;
    margin: -7px 0 0!important;
    outline: medium none;
    vertical-align: middle;
    width: 32px;
    float: left;
    margin-left: 10px;
    visibility: visible;
}
.numa{
	background-color: #5B6368;
    color: #FFFFFF;
    float: right;
    height: 20px;
    margin-right: 10px !important;
    margin-top: -17px !important;
    text-align: center;
    width: 21px;
}

.msg_ico_open{
	background:url(${imgPath}/20140821042246434_easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
	width:32px !important;
	height:32px !important;
	padding-top:5px !important;
}
.msg_ico_close{
	background:url(${imgPath}/20140821042246434_easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
	width:32px !important;
	height:32px !important;
	padding-top:6px !important;
}

.psub_ico_open{
	background:url(${imgPath}/easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
}
.psub_ico_close{
	background:url(${imgPath}/easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
}

.msub_ico_open{
	background:url(${imgPath}/20140821042241790_easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
}
.msub_ico_close{
	background:url(${imgPath}/20140821042241790_easyicon_net_32.png) no-repeat !important;
	margin-right:5px !important; 
}

.noread_ico_docu{
	background:url(${imgPath}/dot.png) no-repeat !important;
}

.sys_ico_docu{
	background:url(${imgPath}/dot.png) no-repeat !important;
}

.user_ico_docu{
	background:url(${imgPath}/dot.png) no-repeat !important;
}
#pageloading_tree{position:absolute; left:0px; top:0px;background:white url('${imgPath}/loading.gif') no-repeat center; width:100%; height:100%;z-index:99998;}
#pageloading_search{position:absolute; left:0px; top:0px;background:white url('${imgPath}/loading.gif') no-repeat center; width:100%; height:100%;z-index:9998;}
#pageloading_edit{position:absolute; left:0px; top:0px;background:white url('${imgPath}/loading.gif') no-repeat center; width:100%; height:100%;z-index:9998;}
</style>
<script type="text/javascript" src="${frontPath}/js/tree.js"></script>
<script type="text/javascript" src="${frontPath}/js/treeRightFunction.js"></script>
<script type="text/javascript" src="${frontPath}/js/notesearch.js"></script>
<script type="text/javascript"> 
window.UEDITOR_HOME_URL = "${frontPath}/js/ueditor/";
window.UEDITOR_IMG_URL = "${webRoot}";
window.DOWNLOAD_URL = "${webRoot}/noteController/front/downloadNodeAttach.dht";
window.imgPath = imgPath;
</script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/ueditor.all.min.js"> </script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/lang/zh-cn/zh-cn.js"></script>
</head>
<body style="width: auto;min-width: 1024px;position:relative;top:0"> 
		<div id="pageloading_tree" style="display:none"></div> 
<!-- Begin header-->
<%@ include file="./include/head.jsp"%>
<!-- End header--> 

<!-- Begin mainer-->
<div class="mainer" id="page_mainer">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="Directory" id="treeMenu_td" style="position:relative;top:0;left:0;padding-top: 0px;">
        <div class="items">
        	<ul id="treeMenu" class="ztree"></ul>
        	<div class="rightMenu" id="treeRightMenu">
        		<ul id="treeRightMenu_ul_subject">
       				<li id="treeRightMenu_add_dir" onclick="addChildDir()">新建目录</li>
       				<li id="treeRightMenu_manage_subject" onclick="toAddSubject()">新建专题</li>
       				<li id="treeRightMenu_delete_subject" onclick="deleteNode()">删除</li>
        		</ul>
        		<ul id="treeRightMenu_ul_directory">
       				<li id="treeRightMenu_add_subdir" onclick="addChildDir()">添加子目录</li>
       				<li id="treeRightMenu_rename_dir" onclick="renameNode()">重命名</li>
       				<li id="treeRightMenu_delete_dir" onclick="deleteNode()">删除</li>
        		</ul>
        		<ul id="treeRightMenu_ul_attachment">
					<li id="treeRightMenu_add_attadir" onclick="addChildDir()">添加子目录</li>
					<li id="treeRightMenu_rename_attadir" onclick="renameNode()">重命名</li>
					<!-- <li id="treeRightMenu_add_attachment" onclick="dirAttachmentManage()">上传文档</li> -->
					<li id="treeRightMenu_delete_attadir" onclick="deleteNode()">删除</li>
				</ul>
        		<ul id="treeRightMenu_ul_tag">
       				<li id="treeRightMenu_add_tag" onclick="addChildTag()">添加标签</li>
       				<li id="treeRightMenu_rename_tag" onclick="renameNode()">修改标签</li>
       				<li id="treeRightMenu_delete_tag" onclick="deleteNode()">删除</li>
        		</ul>
        		<ul id="treeRightMenu_ul_recycle">
       				<li id="treeRightMenu_restore" onclick="restoreDirectory()">还原</li>
       				<li id="treeRightMenu_delete" onclick="deleteNode()">删除</li>
        		</ul>
        		<ul id="treeRightMenu_ul_recycleRoot">
    				<li id="treeRightMenu_deleteAll" onclick="deleteChildNodes()">清空回收站</li>
        		</ul>
        	</div>
        </div>
        
        </td>
      <td class="Fold">
        <a href="#" onclick="hideTreeMenu()"><img id="hideTree_img" src="<%=imgPath %>/button_fold.png" width="8" height="110" /></a>
      </td>
      <td id="iframepage" valign="top" class="mainer_right">
        <!-- Begin mainer_index-->
        <!-- End mainer_index-->
        </td>
    </tr>
  </table>
  <!-- Begin footer-->
<!-- End footer-->
</div>
<!-- End mainer--> 
<jsp:include page="./include/footer.jsp" />
<script type="text/javascript">
//构造左边整棵树 
//selectNode1 需要默认选中的一级节点的序号 0=个人专题 1=多人专题 3=消息中心
//selectNode2Id 需要选中的二级节点的id 如果为空，则默认选中第一个节点
//loadRightPage 值为true，刷新右侧页面
function buildMainMenu(selectFirstNodeIndex,selectNode2Id,loadRightPage){
	showLoading_tree();
	destoryTree();
	//重新构建
	var url = "${webRoot}/indexController/front/treeMenu.dht";
	AT.post(url, null, function(data){
		//初始化树
		$("#treeMenu").html("");
		$.fn.zTree.init($("#treeMenu"), treeSetting, data);
		zTree_Menu = $.fn.zTree.getZTreeObj("treeMenu");
		var nodes = zTree_Menu.getNodes();
		if(nodes.length > 3){
			for(var i = 0; i < nodes.length; i ++){
				//一级节点不能是以下数据类型
				if(nodes[i].dataType == 'DIRECTORY' || nodes[i].dataType == 'TAG'){
					zTree_Menu.removeNode(nodes[i]);
					i--;
				}else{
					alert(zTree_Menu.getNodes()[2].tId);
					//$("#" + zTree_Menu.getNodes()[2].tId + "_a").append('<span id="noReadMsgNum" class="numb">'+ json.totalCount +'</span>');
				}
			}
		}
		/* 计算消息数量 */
		countMessage();
		/* 添加【新建目录】 节点 */
		addDirNode();
		
		var nodes = zTree_Menu.getNodes();
		var curMenu = nodes[selectFirstNodeIndex];
		zTree_Menu.selectNode(curMenu, true);
		//var a = $("#" + curMenu.tId + "_a");
		//a.addClass("cur");
		if(selectNode2Id!=null && selectNode2Id!=''){
			var subjectNode=zTree_Menu.getNodesByParam("id", selectNode2Id, curMenu);
			if(subjectNode.length>0){
				curMenu = subjectNode[0];
			}
		}else{
			//找到第一个dataType=SUBJECT的子节点
			for(var i=0;i<curMenu.children.length;i++){
				if(curMenu.children[i].dataType=='SUBJECT'){
					curMenu = curMenu.children[i];
					break;
				}
			}
		}
		zTree_Menu.selectNode(curMenu);
		if(loadRightPage){
			beforeNodeClick("treeMenu",curMenu);
			onNodeClick(null,"treeMenu",curMenu);
		}
		if(editorheight==null){
			editorheight = document.body.clientHeight- 380;
		}

		setTimeout('hideLoading_tree()',800);
		zTree_Menu.expandNode(zTree_Menu.getNodes()[0], true);	
		zTree_Menu.expandNode(zTree_Menu.getNodes()[1], true);	
		zTree_Menu.expandNode(zTree_Menu.getNodes()[2], true);	
		
		$("#"+zTree_Menu.getNodes()[0].tId+"_a").append('<span id="-1"  class="gear"></span>');
		$("#"+zTree_Menu.getNodes()[1].tId+"_a").append('<span id="-2"  class="gear"></span>');
		$("#"+zTree_Menu.getNodes()[0].tId+"_ico").removeClass("button").addClass("newPriSubjectPic");
		$("#"+zTree_Menu.getNodes()[1].tId+"_ico").removeClass("button").addClass("newPriSubjectPic");
		$("#"+zTree_Menu.getNodes()[0].tId+"_ico").removeClass("button").addClass("newPriSubjectPic");
		$("#"+zTree_Menu.getNodes()[2].tId+"_ico").removeClass("button").addClass("newPriSubjectPic");

		$(".gear").click(function(){
			subjectManage();
		});
		
	});
}
	 
/* 计算消息数量 */
function countMessage(){
	AT.get("${webRoot}/indexController/front/messageCount.dht",function(json){
		//消息中心count、
		
		if(json.totalCount !=0){
			$("#" + zTree_Menu.getNodes()[2].tId + "_a").find(".numb").remove();
			$("#" + zTree_Menu.getNodes()[2].tId + "_a").append('<span id="noReadMsgNum" class="numb">'+ json.totalCount +'</span>');
		}else{
			$("#" + zTree_Menu.getNodes()[2].tId + "_a").find(".numb").remove();
		}
		//未读消息count
		var parentNode = zTree_Menu.getNodeByParam("id", "<%=Constants.MSG_NODEID_NR%>", zTree_Menu.getNodes()[2]);
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_span").remove();
		if(json.totalCount !=0){
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","未读消息("+json.totalCount+")");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">未读消息('+ json.totalCount +')</span>');
		}else{
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","未读消息");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">未读消息</span>');
		}
		//系统消息count
		parentNode = zTree_Menu.getNodeByParam("id", "<%=Constants.MSG_NODEID_SYS%>", zTree_Menu.getNodes()[2]);
		$("#" + parentNode.tId).find("#"+parentNode.tId + "_span").remove();
		if(json.sysMsgCount !=0){
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","系统消息("+json.sysMsgCount+")");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">系统消息('+ json.sysMsgCount +')</span>');
		}else{
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","系统消息");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">系统消息</span>');
		}
		//用户消息count
		parentNode = zTree_Menu.getNodeByParam("id", "<%=Constants.MSG_NODEID_U%>", zTree_Menu.getNodes()[2]);
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_span").remove();
		if(json.userCount !=0){
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","用户消息("+json.userCount+")");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">用户消息('+ json.userCount +')</span>');
		}else{
			$("#" + parentNode.tId).find("#"+parentNode.tId + "_a").attr("title","用户消息");
			$("#"+parentNode.tId + "_a").append('<span id="'+parentNode.tId+'_span">用户消息</span>');
		}
	},false);
	
}
//每个专题下添加“新建目录”节点
function addDirNode(){
	var nodes = zTree_Menu.getNodesByFilter(findSubjectNode);
	for(var i = 0; i < nodes.length; i++){
		addDirForOneNode(nodes[i]);
	}
}
//为某一个节点添加  【新建目录】的节点
function addDirForOneNode(node){
	if(node.getParentNode().id == "<%=Constants.SUBJECT_PID_P%>"){
		var newNode = {id:"addDirectory",name:"新建目录",icon:"${imgPath}/tree/page_add.png"};
		newNode = zTree_Menu.addNodes(node, newNode, true);
	}else{
		// 判断是否有添加目录权限
		var url = "${webRoot}/indexController/front/subjectPermission.dht?subjectId=" + node.id;
		AT.get(url, function(json){
			for(var key in json){
				var sNode = zTree_Menu.getNodeByParam("id", key, null);
				var data = json[key];
				if(data.ADD_DIRECTORY == 'true'){
					var newNode = {id:"addDirectory",name:"新建目录",icon:"${imgPath}/tree/page_add.png"};
					newNode = zTree_Menu.addNodes(sNode, newNode, true);
				}
			}
			
		}, false);
	}
}

//查询专题节点,只取专题节点
function findSubjectNode(node){
	if((node.pId == '<%=Constants.SUBJECT_PID_P%>' || node.pId == '<%=Constants.SUBJECT_PID_M%>') && node.dataType == 'SUBJECT'){
		return true;					
	}else{
		return false;
	}
}


//鼠标页面点击事件
function onBodyClick(event) {
	if (event.target.id != "treeRightMenu" && $(event.target).parents("#treeRightMenu").attr("id") != "treeRightMenu") {
		hideRightMenu();
	}
}

//右键菜单关闭方法
function hideRightMenu(){
	$("#treeRightMenu").hide();
	$("#treeRightMenu_ul_directory").show();
	$("#treeRightMenu_ul_subject").show();
	$("#treeRightMenu_ul_tag").show();
	$("body").unbind("mousedown", onBodyClick);
}

function isShareSubject(node){
	if(node.level == 0 && node.id == '<%=Constants.SUBJECT_PID_P%>'){
		return null;
	}
	
	if(node.level == 0 && node.id == '<%=Constants.SUBJECT_PID_M%>'){
		return node.children[0];
	}
	var subjectNode = null;
	if(node.dataType == 'SUBJECT'){
		subjectNode = node;
	}else if(node.dataType == 'DIRECTORY'){
		//查找专题节点
		var subjectId = node.subjectId;
		subjectNode = zTree_Menu.getNodeByParam("id", subjectId, null);
	}else if(node.dataType == 'RECYCLE'){
		subjectNode = node.getParentNode();
	}else{
		return null;
	}
	
	if(subjectNode != null){
		var rNode = subjectNode.getParentNode();
		
		if(rNode.id == '<%=Constants.SUBJECT_PID_P%>'){
			return null;  //个人专题
		}else{
			return subjectNode; // 多人专题返回专题节点
		}
	}
}

//判断是否是“文档资料”或其子目录
function isDocumentFolder(node){
	if(node.name == '<%=Constants.SUBJECT_DOCUMENT_DIRNAME%>'){
		return true;
	}
	var parentNode = node.getParentNode();
	while(parentNode != null){
		if(parentNode.name == '<%=Constants.SUBJECT_DOCUMENT_DIRNAME%>'){
			return true;
		}
		parentNode = parentNode.getParentNode();
	}
	return false;
}

function hideTreeMenu(){
	$("#treeMenu_td").toggle();
	var rect = $("#treeMenu_td")[0].getBoundingClientRect();;
	var isVisible = !!(rect.bottom - rect.top);
	if(isVisible){
		$("#hideTree_img").attr("src", "${imgPath}/button_fold.png");
	}else{
		$("#hideTree_img").attr("src", "${imgPath}/button_fold2.png");
	}
	editorWidth = $("#notes_new").width()-20; 
	reloadEditor();
}

//打开页面 加载树
$(document).ready(function(){
	rightMenu = $("#treeRightMenu").html();
	buildMainMenu(0,null,true);
});
//专题导出进度任务句柄
var actionSchedule=null;
/* $( document ).tooltip({
	track: true
}); */
</script>
</body>
</html>
