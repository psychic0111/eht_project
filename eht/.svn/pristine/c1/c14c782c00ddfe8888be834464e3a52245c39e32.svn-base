<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<div class="easyui-layout" fit="true">
  <div region="center" style="padding:1px;">
  <t:datagrid name="accountList" title="用户信息" actionUrl="accountController.do?datagrid" idField="id" fit="true" queryMode="group">
   <t:dgCol title="编号" field="id" hidden="false"  ></t:dgCol>
   <t:dgCol title="用户名" field="username"  query="true" ></t:dgCol>
   <t:dgCol title="邮箱" field="email"  query="true"></t:dgCol>
   <t:dgCol title="状态" field="status" replace="启用_0,禁用_1" ></t:dgCol>
   <t:dgCol title="创建时间" field="createtime" formatter="yyyy-MM-dd hh:mm:ss" ></t:dgCol>
   <t:dgCol title="修改时间" field="updatetime" formatter="yyyy-MM-dd hh:mm:ss" ></t:dgCol>
   <t:dgCol title="操作" field="opt" width="100"></t:dgCol>
   <t:dgDelOpt title="删除" url="accountController.do?del&id={id}" />
   <t:dgConfOpt exp="status#eq#0" url="accountController.do?save&id={id}&status=1" message="是否禁用?" title="禁用"></t:dgConfOpt>
   <t:dgConfOpt exp="status#eq#1" url="accountController.do?save&id={id}&status=0" message="是否启用?" title="启用"></t:dgConfOpt>
   <t:dgToolBar title="查看" icon="icon-search" url="accountController.do?show" funname="detail"></t:dgToolBar>
  </t:datagrid>
  </div>
 </div>