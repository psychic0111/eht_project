<%@page import="com.eht.common.util.AppRequstUtiles"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>登陆页面</title>
<%
	String basePath = AppRequstUtiles.getAppUrl();
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
<script type="text/javascript" src="${pageContext.request.contextPath}/plug-in/jquery/jquery-1.8.3.min.js"></script>

<script type="text/javascript"  >
$.ajax({
	
	    url:'${pageContext.request.contextPath}/noteController/front/historyNote.dht',
	    data:{'nodeId':1},
	    
    success:function(result){
        alert(result)
    }
}); 



		
</script> 
</head>
<body>

	<c:if test="${param.authentication_error=='true' }">
		<font color="red">用户名或密码错误</font><br/>
		
	</c:if>
	<form id="loginForm" name="loginForm"
		action="<c:url value="/login.do"/>" method="post">
		<p>
			<label>用户名: <input type='text' name='userName'
				value="marissa" /></label>
		</p>
		<p>
			<label>密码: <input type='text' name='password' value="koala" /></label>
		</p>

		<p>
			<input name="login" value="Login" type="submit" />
		</p>
	</form>
</body>
</html>