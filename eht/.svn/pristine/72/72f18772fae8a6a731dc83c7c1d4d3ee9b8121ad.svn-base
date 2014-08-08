<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="directoryList" title="目录信息" actionUrl="directoryController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="目录名称" field="dirName"  query="true"></t:dgCol>
   <t:dgCol title="专题名称" field="subjectEntity_subjectName" query="true"></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="directoryController.do?del&id={id}" />
   <t:dgConfOpt exp="status#eq#0" url="directoryController.do?save&id={id}&status=1" message="是否禁用?" title="禁用"></t:dgConfOpt>
   <t:dgConfOpt exp="status#eq#1" url="directoryController.do?save&id={id}&status=0" message="是否启用?" title="启用"></t:dgConfOpt>
   <t:dgToolBar title="查看" icon="icon-search" url="directoryController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>