<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<!DOCTYPE html>
<html>
 <head>
  <title>专题信息</title>
  <t:base type="jquery,easyui,tools,DatePicker"></t:base>
 </head>
 <body style="overflow-y: hidden" scroll="no">
  <t:formvalid formid="formobj" dialog="true" usePlugin="password" layout="table" action="subjectController.do?save">
			<input id="id" name="id" type="hidden" value="${subjectPage.id }">
			<table style="width: 600px;" cellpadding="0" cellspacing="1" class="formtable">
				<tr>
					<td align="right">
						<label class="Validform_label">
							专题名称:
						</label>
					</td>
					<td class="value">
						<input class="inputxt" id="subjectname" name="subjectname" 
							   value="${subjectPage.subjectName}">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
				<tr>
					<td align="right">
						<label class="Validform_label">
							专题类型:
						</label>
					</td>
					<td class="value">
					    <select name="subjecttype">
					       <option value="0" <c:if test="${subjectPage.subjectType==0}">selected="selected"</c:if> >个人专题</option>
					       <option value="1" <c:if test="${subjectPage.subjectType==1}">selected="selected"</c:if> >多人专题</option>
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
							   value="${subjectPage.accountCreateUser.username}">
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
							     value="<fmt:formatDate value='${subjectPage.createTime}' type="date" pattern="yyyy-MM-dd"/>">
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
							   value="${subjectPage.accountUpdateUser.username}">
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
							     value="<fmt:formatDate value='${subjectPage.updateTime}' type="date" pattern="yyyy-MM-dd"/>">
						<span class="Validform_checktip"></span>
					</td>
				</tr>
			</table>
		</t:formvalid>
 </body>