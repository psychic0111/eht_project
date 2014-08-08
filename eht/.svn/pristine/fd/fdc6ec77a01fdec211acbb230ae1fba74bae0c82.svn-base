<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="personList" title="员工信息" actionUrl="personController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="用户名" field="name" query="true"></t:dgCol>
   <t:dgCol title="年龄" field="age" ></t:dgCol>
   <t:dgCol title="工资" field="salary" ></t:dgCol>
   <t:dgCol title="创建时间" field="createdt" formatter="yyyy-MM-dd hh:mm:ss" query="true" queryMode="group"></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="personController.do?del&id={id}" />
   <t:dgToolBar title="录入" icon="icon-add" url="personController.do?addorupdate" funname="add"></t:dgToolBar>
   <t:dgToolBar title="编辑" icon="icon-edit" url="personController.do?addorupdate" funname="update"></t:dgToolBar>
   <t:dgToolBar title="查看" icon="icon-search" url="personController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>
<script>
$(document).ready(function(){
	$("input[name='createdt_begin']").attr("class","easyui-datebox");
	$("input[name='createdt_end']").attr("class","easyui-datebox");
});
</script>