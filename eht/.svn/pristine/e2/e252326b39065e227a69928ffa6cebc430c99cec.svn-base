<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<!DOCTYPE html>
<html>
 <head>
  <title>群发邮件管理</title>
  <t:base type="jquery,easyui,tools,DatePicker"></t:base>
 </head>
 <body style="overflow-y: hidden" scroll="no">
  <t:formvalid formid="formobj" dialog="true" usePlugin="password" layout="table" action="sendMailController.do?save">
			<input id="id" name="id" type="hidden" value="${sendMailPage.id }">
			<table style="width:700px;"  cellpadding="0" cellspacing="1" class="formtable">
			    <tr>
					<td align="right" style="width:80px;">
						<label class="Validform_label">
							标题:
						</label>
					</td>
					<td class="value" style="width:600px;">
					<input class="inputxt" id="title" name="title" datatype="*5-200"
							   value="${sendMailPage.title}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				
				<tr>
					<td align="right" >
						<label class="Validform_label">
							收件人:
						</label>
					</td>
					<td class="value" >
						<input class="inputxt" id="email"  name="email"  type="hidden"  >
						<textarea cols="110" id="username" name="accept" rows="5"  datatype="*" readonly="readonly">${sendMailPage.accept}</textarea></br>
						<a href="#" class="easyui-linkbutton" plain="true" icon="icon-search" onClick="choose_4028813345441280014544236ddd000d()">选择</a>
						<a href="#" class="easyui-linkbutton" plain="true" icon="icon-redo" onClick="clearAll_4028813345441280014544236ddd000d();">清空</a>
						<script type="text/javascript">
						var windowapi = frameElement.api, W = windowapi.opener;
						function choose_4028813345441280014544236ddd000d(){
							if(typeof(windowapi) == 'undefined')
							{$.dialog({content: 'url:sendMailController.do?users',zIndex: 2100,title: '用户列表',lock : true,width :400,height :350,left :'85%',top :'65%',opacity : 0.4,button : [ {name : '确认',callback : clickcallback_4028813345441280014544236ddd000d,focus : true}, {name : '取消',callback : function() {}} ]});}
						else{$.dialog({content: 'url:sendMailController.do?users',zIndex: 2100,title: '用户列表',lock : true,parent:windowapi,width :400,height :350,left :'85%',top :'65%',opacity : 0.4,button : [ {name : '确认',callback : clickcallback_4028813345441280014544236ddd000d,focus : true}, {name : '取消',callback : function() {}} ]});}}
						function clearAll_4028813345441280014544236ddd000d(){
							if($('#username').length>=1){
								$('#username').val('');$('#username').blur();
								}
							if($("input[name='username']").length>=1){
							$("input[name='username']").val('');
							$("input[name='username']").blur();
							}
							$('#email').val("");
							}
						function clickcallback_4028813345441280014544236ddd000d(){
							iframe = this.iframe.contentWindow;var username=iframe.getusersListSelections('username');	
							if($('#username').length>=1){
								$('#username').val(username);$('#username').blur();
								}
							if($("input[name='username']").length>=1){$("input[name='username']").val(username);
							$("input[name='username']").blur();}
							var id =iframe.getusersListSelections('email');
							if (id!== undefined &&id!=""){$('#email').val(id);}}
						</script>
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							正文:
						</label>
					</td>
					<td class="value">
					   <textarea cols="110" id="body" name="body"" rows="10"  datatype="*0-2000">${sendMailPage.body}</textarea></br>
						<span class="Validform_checktip"></span>
					</td>
				</tr>
			</table>
		</t:formvalid>
 </body>