<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>
<ul id="invitememberAuto_ul_tag" style="width:200px;">
<c:forEach items="${list}" var="sub">
<li onclick="addemail('${sub.email}','${textarea}');">${sub.email}</li>
</c:forEach> 
 </ul>
