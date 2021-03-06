<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<!DOCTYPE html>
<html>
 <head>
  <title>模板管理</title>
  <script type="text/javascript" src="${webRoot}/plug-in/jquery/jquery-1.8.3.min.js"></script>
  <script type="text/javascript" src="${frontPath}/js/jquery.ztree.all-3.5.min.js"></script>
  <link rel="stylesheet" href="${cssPath}/zTreeStyle/zTreeStyle.css" type="text/css">
  <t:base type="easyui,tools,DatePicker"></t:base>
  	<SCRIPT type="text/javascript">
		<!--
		var setting = {
			view: {
				dblClickExpand: false
			},
			
			callback: {
				onRightClick: OnRightClick,
				beforeRename: beforeRename
			},

			data: {
				simpleData: {
					enable: true
				}
			}
		};

		var zNodes =[];
		//右键点击菜单模板树节点
		function OnRightClick(event, treeId, treeNode) {
			if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
				zTree.cancelSelectedNode();
				showRMenu("root", event.clientX, event.clientY);
			} else if (treeNode && !treeNode.noR) {
				zTree.selectNode(treeNode);
				showRMenu("node", event.clientX, event.clientY);
			}
		}

		function showRMenu(type, x, y) {
			$("#rMenu ul").show();
			if (type=="root") {
				$("#m_del").hide();
				$("#m_check").hide();
				$("#m_unCheck").hide();
			} else {
				$("#m_del").show();
				$("#m_check").show();
				$("#m_unCheck").show();
			}
			rMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});

			$("body").bind("mousedown", onBodyMouseDown);
		}
		function hideRMenu() {
			if (rMenu) rMenu.css({"visibility": "hidden"});
			$("body").unbind("mousedown", onBodyMouseDown);
		}
		function onBodyMouseDown(event){
			if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
				rMenu.css({"visibility" : "hidden"});
			}
		}
		var addCount = 0;
		function addTreeNode() {
			hideRMenu();
			addCount++;
			var newNode = { name:"增加" + (addCount)};
			if (zTree.getSelectedNodes()[0]) {
				newNode.checked = zTree.getSelectedNodes()[0].checked;
				var pids=zTree.getSelectedNodes()[0].id
				zTree.addNodes(zTree.getSelectedNodes()[0], {id:addCount, pId:pids, name:"增加" + (addCount)});
			} else {
				zTree.addNodes(null, {id:addCount, pId:0, name:"增加" + (addCount)});
			}
		      
		}
		function removeTreeNode() {
			hideRMenu();
			var nodes = zTree.getSelectedNodes();
			if (nodes && nodes.length>0) {
				if (nodes[0].children && nodes[0].children.length > 0) {
						zTree.removeNode(nodes[0]);
				} else {
					zTree.removeNode(nodes[0]);
				}
			}
		}
		function checkTreeNode(checked) {
			var nodes = zTree.getSelectedNodes();
			if (nodes && nodes.length>0) {
				zTree.checkNode(nodes[0], checked, true);
			}
			hideRMenu();
		}
		function resetTree() {
			hideRMenu();
			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
		}

		var zTree, rMenu;
		$(document).ready(function(){
			var k='${templatePage.content}'
				if(k!=''){
					zNodes=eval(k);
					}
			for (var i=0;i<zNodes.length;i++)
			{
			var noded=zNodes[i];
			if(addCount<noded.id){
				addCount=noded.id;
				}
			}
			$.fn.zTree.init($("#treeDemo"), setting, zNodes);
			zTree = $.fn.zTree.getZTreeObj("treeDemo");
			rMenu = $("#rMenu");
		});

		function edit() {
			hideRMenu();
			var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
			nodes = zTree.getSelectedNodes(),
			treeNode = nodes[0];
			if (nodes.length == 0) {
				alert("请先选择一个节点");
				return;
			}
			zTree.editName(treeNode);
		};

		function beforeRename(treeId, treeNode, newName) {
			if (newName.length == 0) {
				alert("节点名称不能为空.");
				var zTree = $.fn.zTree.getZTreeObj("treeDemo");
				setTimeout(function(){zTree.editName(treeNode)}, 10);
				return false;
			}
			if(treeNode.level==0){
                 if(newName=='回收站'||newName=='文档资料'){
                	  alert("根节点名称不能以回收站和文档资料命名");
                	  var zTree = $.fn.zTree.getZTreeObj("treeDemo");
       				setTimeout(function(){zTree.editName(treeNode)}, 10);
      				return false;
                     }
				};
			return true;
		}

		function setTreeJosn(){
			var treeObj = $.fn.zTree.getZTreeObj("treeDemo"); 
			if(treeObj!=null){
				var nodes = treeObj.transformToArray(treeObj.getNodes());
				if(nodes.length==0){
					 alert("请编辑模板内容");
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
			}
		}
		//-->
	</SCRIPT>
  <SCRIPT type="text/javascript">
  function test(data) {
	  alert(data.msg);
	  addOneTab('模板管理','templateController.do?template&clickFunctionId=4028813344dd52720144dd5641840001');
	  closetab('模板添加');
	  closetab('模板编辑');
	}
  </SCRIPT>
  <style type="text/css">
div#rMenu {position:absolute; visibility:hidden; top:0; background-color: #555;text-align: left;padding: 2px;}
div#rMenu ul li{
	margin: 1px 0;
	padding: 0 5px;
	cursor: pointer;
	list-style: none outside none;
	background-color: #DFDFDF;
}
html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, font, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td {
	margin: 0;padding: 0;border: 0;outline: 0;font-weight: inherit;font-style: inherit;font-size: 100%;font-family: inherit;vertical-align: baseline;}
body {color: #2f332a;font: 15px/21px Arial, Helvetica, simsun, sans-serif;background: #f0f6e4 \9;}
h1, h2, h3, h4, h5, h6 {color: #2f332a;font-weight: bold;font-family: Helvetica, Arial, sans-serif;padding-bottom: 5px;}
h1 {font-size: 24px;line-height: 34px;text-align: center;}
h2 {font-size: 14px;line-height: 24px;padding-top: 5px;}
h6 {font-weight: normal;font-size: 12px;letter-spacing: 1px;line-height: 24px;text-align: center;}
a {color:#3C6E31;text-decoration: underline;}
a:hover {background-color:#3C6E31;color:white;}
input.radio {margin: 0 2px 0 8px;}
input.radio.first {margin-left:0;}
input.empty {color: lightgray;}
code {color: #2f332a;}
.highlight_red {color:#A60000;}
.highlight_green {color:#A7F43D;}
li {list-style: circle;font-size: 12px;}
li.title {list-style: none;}
ul.list {margin-left: 17px;}

div.content_wrap {width: 600px;height:380px;}
div.content_wrap div.left{float: left;width: 250px;}
div.content_wrap div.right{float: right;width: 340px;}
div.zTreeDemoBackground {width:250px;height:362px;text-align:left;}

ul.ztree {margin-top: 10px;border: 1px solid #617775;background: #f0f6e4;width:220px;height:360px;overflow-y:scroll;overflow-x:auto;}
ul.log {border: 1px solid #617775;background: #f0f6e4;width:300px;height:170px;overflow: hidden;}
ul.log.small {height:45px;}
ul.log li {color: #666666;list-style: none;padding-left: 10px;}
ul.log li.dark {background-color: #E3E3E3;}

/* ruler */
div.ruler {height:20px; width:220px; background-color:#f0f6e4;border: 1px solid #333; margin-bottom: 5px; cursor: pointer}
div.ruler div.cursor {height:20px; width:30px; background-color:#3C6E31; color:white; text-align: right; padding-right: 5px; cursor: pointer}
	</style>
 </head>
 <body style="overflow-y: hidden" scroll="no">
  <t:formvalid formid="formobj" dialog="false"  layout="table" action="templateController.do?toAdd" btnsub="btn" callback="test" beforeSubmit="setTreeJosn()">
			<input id="id" name="id" type="hidden" value="${templatePage.id }">
			
			<input id="josns" name="josns" type="hidden" >
			<table style="width:700px;" cellpadding="0" cellspacing="1" class="formtable">
				<tr>
					<td align="right" style="width:80px;">
						<label class="Validform_label">
							模板名称:
						</label>
					</td>
					<td class="value" style="width:600px;">
						<input class="inputxt" id="templateName" name="templateName" datatype="*4-20"
							   value="${templatePage.templateName}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							模本分类:
						</label>
					</td>
					<td class="value">
						  	  <c:if test="${templatePage.templateType==0||templatePage.templateType eq null}">系统内置</c:if> 
						  	 <c:if test="${templatePage.templateType==1}">用户自定义</c:if> 
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							内容:
						</label>
					</td>
					<td class="value">
					  <div class="content_wrap">
						<div class="zTreeDemoBackground left">
							<ul id="treeDemo" class="ztree"></ul>
						</div>
						
					</div>
					</td>
					
				</tr>
				<tr height="40">
			<td class="upload" colspan="6"><a href="#" class="easyui-linkbutton" id="btn" iconCls="icon-ok">提交</a> <a href="#" class="easyui-linkbutton" id="btn_reset" iconCls="icon-back">重置</a></td>
		</tr>
			</table>
		</t:formvalid>
		 <div id="rMenu">
				<ul>
					<li id="m_add" onclick="addTreeNode();">增加节点</li>
					<li id="m_del" onclick="removeTreeNode();">删除节点</li>
					<li id="m_del" onclick="edit();">编辑节点</li>
					<li id="m_reset" onclick="resetTree();">恢复zTree</li>
				</ul>			
		</div>
 </body>
 </html>
