<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<!DOCTYPE html>
<html>
 <head>
  <title>系统消息关系对应表</title>
  <t:base type="jquery,easyui,tools,DatePicker"></t:base>
 </head>
 <body style="overflow-y: hidden" scroll="no">
  <t:formvalid formid="formobj" dialog="true" usePlugin="password" layout="table" action="messageUserController.do?save">
			<input id="id" name="id" type="hidden" value="${messageUserPage.id }">
			<table style="width: 600px;" cellpadding="0" cellspacing="1" class="formtable">
				<tr>
					<td align="right">
						<label class="Validform_label">
							系统消息ID:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="messageid" name="messageid" ignore="ignore"
							   value="${messageUserPage.messageid}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							用户ID:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="userid" name="userid" ignore="ignore"
							   value="${messageUserPage.userid}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							是否已读:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="isread" name="isread" ignore="ignore"
							   value="${messageUserPage.isread}" datatype="n">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
			</table>
		</t:formvalid>
 </body>