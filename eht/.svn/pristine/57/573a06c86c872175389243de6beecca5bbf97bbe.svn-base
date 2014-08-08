<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="attachmentList" title="条目附件" actionUrl="attachmentController.do?datagrid" idField="id" fit="true">
   <t:dgCol title="编号" field="id" hidden="false"></t:dgCol>
   <t:dgCol title="文件名" field="filename" ></t:dgCol>
   <t:dgCol title="文件类型" field="suffix" ></t:dgCol>
    <t:dgCol title="所属条目" field="noteEntity_title" ></t:dgCol>
   <t:dgCol title="已传输" field="transfer" ></t:dgCol>
   <t:dgCol title="状态" field="status" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="attachmentController.do?del&id={id}" />
   <t:dgDefOpt url="attachmentController.do?viewFile&id={id}" title="下载"></t:dgDefOpt>
   <t:dgToolBar title="查看" icon="icon-search" url="attachmentController.do?addorupdate" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div> 