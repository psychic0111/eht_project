<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div id="main_subject_list" class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="subjectList" title="专题信息" actionUrl="subjectController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="专题名称" field="subjectName" query="true"></t:dgCol>
   <t:dgCol title="专题类型" field="subjectType" replace="个人专题_0,多人专题_1" query="true"></t:dgCol>
   <t:dgCol title="状态" field="status" replace="启用_0,禁用_1" query="true" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="subjectController.do?del&id={id}" />
    <t:dgConfOpt exp="status#eq#0" url="subjectController.do?save&id={id}&status=1" message="是否禁用?" title="禁用"></t:dgConfOpt>
   <t:dgConfOpt exp="status#eq#1" url="subjectController.do?save&id={id}&status=0" message="是否启用?" title="启用"></t:dgConfOpt>
     <t:dgFunOpt funname="queryDirectory(id)" title="查看目录"></t:dgFunOpt>
     <t:dgFunOpt funname="queryNote(id)" title="查看条目"></t:dgFunOpt>
   <t:dgToolBar title="查看" icon="icon-search" url="subjectController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>
 
 <div data-options="region:'east',
	title:'列表',
	collapsed:true,
	split:true,
	border:false,
	onExpand : function(){
		li_east = 1;
	},
	onCollapse : function() {
	    li_east = 0;
	}"
	style="width: 400px; overflow: hidden;">
<div class="easyui-panel" style="padding: 1px;" fit="true" border="false" id="subjectListpanel"></div>
</div>

<script type="text/javascript">
<!--
$(function() {
	var li_east = 0;
});

function queryDirectory(id){
	if(li_east == 0){
	   $('#main_subject_list').layout('expand','east'); 
	}
	$('#subjectListpanel').panel("refresh", "subjectController.do?directoryList&subjectid=" + id);
}

function queryNote(id){
	if(li_east == 0){
	   $('#main_subject_list').layout('expand','east'); 
	}
	$('#subjectListpanel').panel("refresh", "subjectController.do?noteList&subjectid=" + id);
}
//-->
</script>