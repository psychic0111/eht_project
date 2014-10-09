<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<link rel="stylesheet" href="${cssPath}/zTreeStyle/tagTree.css" type="text/css">
<style type="text/css">
</style>
<script type="text/javascript">
var zNodestemplate =[];
var zTreeTemplate, rMenuTemplate;
var addCount = 0;

var settingtemplate = {
		view: {
			dblClickExpand: false
		},
		callback: {
			onRightClick: OnRightClickTemplate,
			beforeRename: beforeRename
		},
		data: {
			simpleData: {
				enable: true
			}
		}
	};

$().ready(function() {
document.getElementById("subjectName").focus();
	$("#addSubjectForm").validate({
			rules:{
				subjectName:{required:true,minlength:2,maxlength:40},
				description:{maxlength:200}
			},
			messages:{
				subjectName:{remote:'专题名称已被使用'}
			}
		}
	);
	rMenuTemplate = $("#rMenuTemplate");
	$('#templateSelectContent').hide();
});

function OnRightClickTemplate(event, treeId, treeNode) {
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		zTreeTemplate.cancelSelectedNode();
		showRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		zTreeTemplate.selectNode(treeNode);
		showRMenu("node", event.clientX, event.clientY);
	}
}
function showRMenu(type, x, y) {
	
	$("#rMenuTemplate ul").show();
	if (type=="root") {
		$("#m_del").hide();
		$("#m_check").hide();
		$("#m_unCheck").hide();
	} else {
		$("#m_del").show();
		$("#m_check").show();
		$("#m_unCheck").show();
	}
	rMenuTemplate.css({"top":y+"px", "left":x+"px", position: "absolute"});
	rMenuTemplate.show();
	$("body").bind("mousedown", onBodyMouseDown);
}

function beforeRename(treeId, treeNode, newName) {
	if (newName.length == 0) {
		MSG.alert("节点名称不能为空.");
		var zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
		setTimeout(function(){zTreeTemplate.editName(treeNode)}, 10);
		return false;
	}
	if(treeNode.level==0){
         if(newName=='回收站'||newName=='文档资料'){
        	  MSG.alert("根节点名称不能以回收站和文档资料命名");
        	  var zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
				setTimeout(function(){zTreeTemplate.editName(treeNode)}, 10);
				return false;
             }
		};
	return true;
}

function hideRMenu() {
	if (rMenuTemplate) {
	rMenuTemplate.hide();
	}
	$("body").unbind("mousedown", onBodyMouseDown);
}

function addSubject(){
	if($('#templateId').val()!=''){
		var treeObj = $.fn.zTree.getZTreeObj("treeDemo");
		var nodes = treeObj.transformToArray(treeObj.getNodes());
		if(nodes.length==0){
			 MSG.alert("请编辑模板内容");
			 return false;
		}
		var arr = new Array();
		for (var i=0;i<nodes.length;i++)
		{
			var noded=nodes[i];
			arr[i]={id:noded.id, pId:noded.pId, name:noded.name};
		}
		var jsonText = JSON.stringify(arr);  
		$("#josns").val(jsonText);
	}else{
		$("#josns").val('');

		}
		if($('#subjectName').val()=='个人专题'||$('#subjectName').val()=='多人专题'||$('#subjectName').val()=='消息中心'){
		MSG.alert('专题名称不能以个人专题,多人专题,消息中心命名', '提示信息');
		return false;
		}
	AT.postFrm("addSubjectForm",function(data){
		if(data.success){  
		MSG.alert(data.msg, '提示信息');
			if(data.attributes.subjectType==1){
				buildMainMenu(0,data.attributes.subjectId,false);
			}else{
				buildMainMenu(1,data.attributes.subjectId,false);
			}
				AT.load("iframepage","${webRoot}/subjectController/front/subjectManage.dht?subjectType=" + data.attributes.subjectType,function() {});
			}else{
				MSG.alert(data.msg);
			}
	},true);
}

function onBodyMouseDown(event){
	if (!(event.target.id == "rMenuTemplate" || $(event.target).parents("#rMenuTemplate").length>0)) {
		rMenuTemplate.hide();
		
	}
}
function hideTemplateMenu(){
$('#templateSelectContent').hide();
}
$('#templateId').change(function(){
	if($(this).val()!=''){
		var params = {'id':$(this).val()};
		AT.post("${webRoot}/templateController/front/findtemplate.dht",params,function(data){
			addCount=0;
			if(data!=null&&data.length>0){
					for (var i=0;i<data.length;i++)
					{
						var noded=data[i];
						if(addCount<noded.id){
							addCount=noded.id;
						}
					}
					zNodestemplate=data;
					$.fn.zTree.init($("#treeDemo"), settingtemplate, data);
					zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
					$('#templateSelectContent').css({
						left : "515px",
					    top : "255px"
					}).show();
			 }else{
				 MSG.alert("当前模板数据出现问题");
			 	 $('#templateId').val("");
			     zNodestemplate=[];
			     $.fn.zTree.init($("#treeDemo"), settingtemplate, []);
			     zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
			     $('#templateSelectContent').hide();
			 }
		});
	}else{
		zNodestemplate=[];
		$.fn.zTree.init($("#treeDemo"), settingtemplate, []);
		zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
		$('#templateSelectContent').hide();
	}
	
});

function edit() {
	hideRMenu();
	var zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo"),
	nodes = zTreeTemplate.getSelectedNodes(),
	treeNode = nodes[0];
	if (nodes.length == 0) {
		MSG.alert("请先选择一个节点");
		return;
	}
	zTreeTemplate.editName(treeNode);
};

function beforeRename(treeId, treeNode, newName) {
	if (newName.length == 0) {
		MSG.alert("节点名称不能为空.");
		var zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
		setTimeout(function(){zTreeTemplate.editName(treeNode)}, 10);
		return false;
	}
	if(treeNode.level==0){
         if(newName=='回收站'||newName=='文档资料'){
        	  MSG.alert("根节点名称不能以回收站和文档资料命名");
        	  var zTreeTemplate = $.fn.zTree.getZTreeObj("treeDemo");
				setTimeout(function(){zTreeTemplate.editName(treeNode)}, 10);
				return false;
             }
		};
	return true;
}

function addTreeNode() {
	hideRMenu();
	addCount++;
	var newNode = { name:"增加" + (addCount)};
	if (zTreeTemplate.getSelectedNodes()[0]) {
		newNode.checked = zTreeTemplate.getSelectedNodes()[0].checked;
		var pids=zTreeTemplate.getSelectedNodes()[0].id
		zTreeTemplate.addNodes(zTreeTemplate.getSelectedNodes()[0], {id:addCount, pId:pids, name:"增加" + (addCount)});
	} else {
		zTreeTemplate.addNodes(null, {id:addCount, pId:0, name:"增加" + (addCount)});
	}
      
}
function removeTreeNode() {
	hideRMenu();
	var nodes = zTreeTemplate.getSelectedNodes();
	if (nodes && nodes.length>0) {
		if (nodes[0].children && nodes[0].children.length > 0) {
				zTreeTemplate.removeNode(nodes[0]);
		} else {
			zTreeTemplate.removeNode(nodes[0]);
		}
	}
}
function checkTreeNode(checked) {
	var nodes = zTreeTemplate.getSelectedNodes();
	if (nodes && nodes.length>0) {
		zTreeTemplate.checkNode(nodes[0], checked, true);
	}
	hideRMenu();
}
function resetTree() {
	hideRMenu();
	$.fn.zTree.init($("#treeDemo"), settingtemplate, zNodestemplate);
}

</script>
<body >
 <div class="right_top">
     <div class="Nav" id="nav_div">添加专题</div>
 </div>
    <div class="right_index" >
	<form action="${webRoot}/subjectController/front/addSubject.dht" method="post" id="addSubjectForm" name="addSubjectForm" onsubmit="return false;"> 
	<input id="josns" name="josns" type="hidden" >
		<!-- Begin Information-->
		<div class="Information">
		  <div class="title">专题信息</div>
		  <div class="Table">
		    <table width="100%" border="0" cellspacing="0" cellpadding="0">
		      <tr>
		        <td width="100">专题名称：</td>
		        <td>
		         <span class="tags">
		           <input name="subjectName" id="subjectName" class="InputTxt2" style="width:70%;height:28px;line-height:28px;" type="text"/>
		          </span>
		         </td>
		       </tr>
		       <tr>
		         <td>专题类型：</td>
		         <td>
		         	<input type="radio" name="subjectType" id="subjectType_p" value="1" <c:if test="${subjectType ==1}"> checked="checked"  </c:if>/>
		           	个人
		           <input type="radio" name="subjectType" id="subjectType_m" value="2" <c:if test="${subjectType == 2}"> checked="checked"  </c:if> />
		          	 多人
				</td>
		       </tr>
		       <tr>
		         <td>专题模板：</td>
		         <td>
		         	<select id="templateId" name="templateId" style="height:28px;float:left">
		        		<option value="">请选择模板... </option>
		         		<c:forEach items="${templateList}" var="template">
		         			<option value="${template.id }">${template.templateName }</option>
		         		</c:forEach>
		          </select>
		          <div id="templateSelectContent" hidefocus="true" class="menuContent" style="background:#FFFFFF;font-size:13px;border: 1px solid rgb(190, 190, 190);display:none; position: absolute;z-index:1501">
					<i onclick="hideTemplateMenu()" style="float:right;margin-top:5px;margin-right:5px;background-image:url('${imgPath}/34aL_046.png');width:16px;height:16px;cursor:pointer;"></i>
					<ul id="treeDemo"  hidefocus="true" class="tag_tree" style="margin-top:0; width:186px;z-index:1002">
					</ul>
					</div>
		          <!-- 
		          <div class="content_wrap">
		          	<div class="zTreeDemoBackground left">
						<ul id="treeDemo" class="tag_tree" style="margin-top: 0px;border: 1px solid #617775;background: #f0f6e4;width:220px;height:200px;overflow-y:scroll;overflow-x:auto;z-index:1003;"></ul>
					</div>
				</div>
				-->
		        </td>
		      </tr>
		      <tr>
		        <td>专题介绍：</td>
		        <td>
		        	<textarea  name="description" cols="70" rows="5"></textarea>
		        </td>
		      </tr>
		    </table>
		    <div class="submit">
		      <input class="Button1" type="button" name="sub_btn" id="sub_btn" value="创建" onclick="addSubject()"/>
		      <input class="Button2" type="button" name="sub_btn" id="-${subjectType}" value="返回" onclick="subjectManage(this)" style="margin-left:10px;"/>
		    </div>
		  </div>
		</div>
	<!-- End Information--> 
	</form>
	<div  id="rMenuTemplate" class="rightMenu" style="z-index:13000">
        		<ul id="treeRightMenu_ul_subject">
       				 <li id="m_add" onclick="addTreeNode();">增加节点</li>
					<li id="m_del" onclick="removeTreeNode();">删除节点</li>
					<li id="m_edit" onclick="edit();">编辑节点</li>
					<li id="m_reset" onclick="resetTree();">恢复模板</li>
        		</ul>
     </div>
</div>