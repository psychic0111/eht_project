<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="noteLists" title="条目信息" actionUrl="subjectController?noteDatagrid&subjectid=${subjectid}" idField="id" fit="true" queryMode="group">
     <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="标题" field="title" query="true"></t:dgCol>
   <t:dgCol title="内容"  field="content" ></t:dgCol>
   <t:dgCol title="所属目录" field="directoryEntity_dirName"></t:dgCol>
   <t:dgCol title="所属专题" field="subjectEntity_subjectName" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="noteController.do?del&id={id}" />
   <t:dgToolBar title="查看" icon="icon-search" url="noteController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>