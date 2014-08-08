<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="sendMailList" title="群发邮件管理" actionUrl="sendMailController.do?datagrid" idField="id" fit="true">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="标题" field="title" width="100" ></t:dgCol>
   <t:dgCol title="正文" field="body" width="100" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="sendMailController.do?del&id={id}" />
   <t:dgToolBar title="录入" icon="icon-add" url="sendMailController.do?addorupdate" funname="add"></t:dgToolBar>
   <t:dgToolBar title="查看" icon="icon-search" url="sendMailController.do?show" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>