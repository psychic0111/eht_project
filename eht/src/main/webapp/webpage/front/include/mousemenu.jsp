<%@page import="com.eht.subject.entity.SubjectEntity"%>
<%@page import="com.eht.common.constant.ActionName"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>

<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
	String webpagePath = basePath +"/webpage";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />
<c:set var="webpagePath" value="<%=webpagePath%>" />

 <link rel="stylesheet" type="text/css" href="${cssPath}/style.css" /> 
<!-- Begin header-->
<ul id="treeRightMenu_ul_subject">
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ADD_DIRECTORY %>">
		<li id="treeRightMenu_add_dir" onclick="addChildDir()">添加目录</li>
	</xd:hasPermission>
	<li id="treeRightMenu_manage_subject" onclick="subjectManage()">新建专题</li>
	<%-- <xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.DELETE_SUBJECT %>">
		<li id="treeRightMenu_manage_subject" onclick="subjectMember()">团队成员</li>
	</xd:hasPermission> --%>
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.DELETE_SUBJECT %>">
		<li id="treeRightMenu_manage_subject" onclick="deleteNode()">删除</li>
	</xd:hasPermission>
	<!-- <li id="treeRightMenu_close1" onclick="hideRightMenu()">关闭</li> -->
</ul>
<ul id="treeRightMenu_ul_directory">
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ADD_DIRECTORY %>">
		<li id="treeRightMenu_add_subdir" onclick="addChildDir()">添加子目录</li>
		<li id="treeRightMenu_rename_dir" onclick="renameNode()">重命名</li>
		<li id="treeRightMenu_delete_dir" onclick="deleteNode()">删除</li>
	</xd:hasPermission>
</ul>
<ul id="treeRightMenu_ul_attachment">
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ADD_DIRECTORY %>">
		<li id="treeRightMenu_add_attadir" onclick="addChildDir()">添加子目录</li>
		<li id="treeRightMenu_rename_attadir" onclick="renameNode()">重命名</li>
		<!-- <li id="treeRightMenu_add_attachment" onclick="dirAttachmentManage()">上传文档</li> -->
		<li id="treeRightMenu_delete_attadir" onclick="deleteNode()">删除</li>
	</xd:hasPermission>
</ul>
<ul id="treeRightMenu_ul_tag">
	<li id="treeRightMenu_add_tag" onclick="addChildTag()">添加标签</li>
	<li id="treeRightMenu_rename_tag" onclick="renameNode()">修改标签</li>
	<li id="treeRightMenu_delete_tag" onclick="deleteNode()">删除</li>
</ul>
<ul id="treeRightMenu_ul_recycle">
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ADD_DIRECTORY %>">
		<li id="treeRightMenu_restore" onclick="restoreDirectory()">还原</li>
		<li id="treeRightMenu_delete" onclick="deleteNode()">删除</li>
	</xd:hasPermission>
</ul>
<ul id="treeRightMenu_ul_recycleRoot">
	<xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ADD_DIRECTORY %>">
		<li id="treeRightMenu_deleteAll" onclick="deleteChildNodes()">清空回收站</li>
	</xd:hasPermission>
</ul>
<ul id="treeRightMenu_ul_MemberManage">
     <xd:hasPermission  resource="SubjectManage" subjectId="${subjectId}" action="<%=ActionName.ASSIGN_MEMBER %>">
      	<li id="treeRightMenu_MemberManage" onclick="treeToMemberManage('${subjectId}')">成员管理</li>
     </xd:hasPermission>
</ul>

<!-- End header-->  
