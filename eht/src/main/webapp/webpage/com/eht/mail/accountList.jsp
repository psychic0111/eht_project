<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>

<!DOCTYPE html >
<html>
<head>
<title>角色集合</title>
<t:base type="jquery,easyui,tools"></t:base>
</head>
<body style="overflow-y: hidden" scroll="no">
  <t:datagrid name="usersList" title="用户信息" actionUrl="sendMailController.do?accountList" idField="id" checkbox="true" showRefresh="false">
   <t:dgCol title="编号" field="id" hidden="false"  ></t:dgCol>
   <t:dgCol title="用户名" field="username"  ></t:dgCol>
   <t:dgCol title="邮箱" field="email" ></t:dgCol>
  </t:datagrid>
 </body>
</html>