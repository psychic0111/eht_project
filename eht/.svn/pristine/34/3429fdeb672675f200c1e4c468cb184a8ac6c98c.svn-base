<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>

  <t:datagrid name="templateList" title="模板管理" actionUrl="templateController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="模板名称" field="templatename"  query="true" ></t:dgCol>
   <t:dgCol title="模本类型" field="templateType" query="true" replace="系统内置_0,用户自定义_1"></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="templateController.do?del&id={id}" />
   <t:dgToolBar title="查看" icon="icon-search" url="templateController.do?addorupdate" height="500" width="800" funname="detail"></t:dgToolBar>
   <t:dgToolBar title="模板添加" icon="icon-add" url="#"  height="500" width="800" onclick="addbytabs()"></t:dgToolBar>
   <t:dgToolBar title="模板编辑" icon="icon-edit" url="#"  height="500" width="800" onclick="updatebytabs()"></t:dgToolBar>
  </t:datagrid>
  
 <SCRIPT type="text/javascript">
	function addbytabs() {
		addOneTab("模板添加", "templateController.do?add&isIframe");
	}

	function updatebytabs() {
		var rowsData = $('#templateList').datagrid('getSelections');
		if (!rowsData || rowsData.length==0) {
		tip('请选择编辑项目');
		return;
		}
		if (rowsData.length>1) {
		tip('请选择一条记录再编辑');
		return;
		}
		var  url= 'templateController.do?add&isIframe';
		url += '&id='+rowsData[0].id;
		addOneTab("模板编辑", url);
		} 
</SCRIPT>