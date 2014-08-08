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

			data: {
				simpleData: {
					enable: true
				}
			}
		};

		var zNodes =[];

		
	
		var addCount = 0;
		

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

		//-->
	</SCRIPT>
  <SCRIPT type="text/javascript">

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
 <body style="overflow-x: hidden" scroll="no">
  <t:formvalid formid="formobj" dialog="true" usePlugin="password" layout="table" action="templateController.do?save">
			<input id="id" name="id" type="hidden" value="${templatePage.id }">
			<table style="width:700px;" cellpadding="0" cellspacing="1" class="formtable">
				<tr>
					<td align="right" style="width:80px;">
						<label class="Validform_label">
							模板名称:
						</label>
					</td>
					<td class="value" style="width:600px;">
						<input class="inputxt" id="templatename" name="templatename" datatype="*4-20"
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
					     <select id="classify" name="classify">
					   <option value="0" <c:if test="${templatePage.classify==0}">selected="selected"</c:if> >系统内置</option>
					   <option value="1" <c:if test="${templatePage.classify==1}">selected="selected"</c:if> >用户自定义</option>
					   </select>
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							创建者:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="createuser" name="createuser" ignore="ignore"
							   value="${templatePage.accountCreateUser.username}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							创建时间:
						</label>
					</td>
					<td class="value">
						<input class="Wdate" onClick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})"  style="width: 150px" id="createtime" name="createtime" ignore="ignore"
							     value="<fmt:formatDate value='${templatePage.createTime}' type="date" pattern="yyyy-MM-dd"/>">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							修改者:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="updateuser" name="updateuser" ignore="ignore"
							   value="${templatePage.accountUpdateUser.username}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							修改时间:
						</label>
					</td>
					<td class="value">
						<input class="Wdate" onClick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})"  style="width: 150px" id="updatetime" name="updatetime" ignore="ignore"
							     value="<fmt:formatDate value='${templatePage.updateTime}' type="date" pattern="yyyy-MM-dd"/>">
						<span class="Validform_checktip"></span>
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
			</table>
		</t:formvalid>
 </body>