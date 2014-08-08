<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
  <t:datagrid name="directoryLists" title="目录信息" actionUrl="subjectController?directoryDatagrid&subjectid=${subjectid}" idField="id" fit="true"  fitColumns="true"  queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="目录名称" field="dirname" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="directoryController.do?del&id={id}" />
   <t:dgConfOpt exp="status#eq#0" url="directoryController.do?save&id={id}&status=1" message="是否禁用?" title="禁用"></t:dgConfOpt>
   <t:dgConfOpt exp="status#eq#1" url="directoryController.do?save&id={id}&status=0" message="是否启用?" title="启用"></t:dgConfOpt>
   <t:dgToolBar title="查看" icon="icon-search" url="directoryController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
