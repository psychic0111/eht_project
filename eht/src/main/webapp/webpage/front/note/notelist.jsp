<%@page import="com.eht.common.util.AppRequstUtiles"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>
<%
	String basePath = AppRequstUtiles.getAppUrl();
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />

<link rel="stylesheet" href="${cssPath}/jquery.sinaEmotion.css" type="text/css">

<div id="scrollbar1" style="height:100%;overflow:auto;width:320px">
   <div class="scrollbar"><div class="track"><div class="thumb"><div class="end"></div></div></div></div>
   	<div id="content_viewport" class="viewport">
   	<div id="content_overview" class="overview" style="width:99%;">
   		<!-- 记录noteList第一个目录id（用来初始化第一个目录详细内容区） -->
   		<c:set var="first" value="true"></c:set>
   		<c:set var="firstNoteId" value=""/>
		<ul id="contentListUl" style="width:100%;">
			<jsp:include page="noteeach.jsp"></jsp:include>
		</ul>
		<input type="hidden" value="${firstNoteId }" id="firstNodeId" />
		<input type="hidden" name="pageNo" id="note_pageNo" value="1"/>
	</div>
	</div>
</div>
<script type="text/javascript">
$(document).ready(function(){
	var nScrollHight = 0; //滚动距离总长(注意不是滚动条的长度)
	var nScrollTop = 0;   //滚动到的当前位置
	var nDivHight = $("#scrollbar1").height();
	
	$("#scrollbar1").scroll(function(){
		nScrollHight = $(this)[0].scrollHeight;
		nScrollTop = $(this)[0].scrollTop;
		if(nScrollTop + nDivHight >= nScrollHight)
			loadMoreNotes();
	});
});

/* //滚动条在Y轴上的滚动距离
function getScrollTop(){
	var scrollTop = 0, bodyScrollTop = 0, documentScrollTop = 0;
	if(document.body){
		bodyScrollTop = document.body.scrollTop;
	}
	if(document.documentElement){
		documentScrollTop = document.documentElement.scrollTop;
	}
	scrollTop = (bodyScrollTop - documentScrollTop > 0) ? bodyScrollTop : documentScrollTop;
	return scrollTop;
}

//文档的总高度
function getScrollHeight(){
	var scrollHeight = 0, bodyScrollHeight = 0, documentScrollHeight = 0;
	if(document.body){
		bodyScrollHeight = document.body.scrollHeight;
	}
	if(document.documentElement){
		documentScrollHeight = document.documentElement.scrollHeight;
	}
	scrollHeight = (bodyScrollHeight - documentScrollHeight > 0) ? bodyScrollHeight : documentScrollHeight;
	return scrollHeight;
}

//浏览器视口的高度
function getWindowHeight(){
	var windowHeight = 0;
	if(document.compatMode == "CSS1Compat"){
		windowHeight = document.documentElement.clientHeight;
	}else{
		windowHeight = document.body.clientHeight;
	}
	return windowHeight;
}

window.onscroll = function(){
　　if(getScrollTop() + getWindowHeight() == getScrollHeight()){
　　　　alert("you are in the bottom!");
　　}
};
*/
function loadMoreNotes(undir){
	var deleted = $("#deleted").val();
	var input = $("#searchNoteField").val();//检索词
	var orderField = $("#noteOrderField").val();//排序字段
	var subjectId = $("#note_subjectId").val();//专题id
	var dirId = $("#note_dirId").val();//目录id
	var tagId = $("#note_tagId").val();//标签id
	if(!input){
		input = '';
	}
	input = encodeURIComponent(input);
	if(!orderField){
		orderField = '';
	}
	
	if(!subjectId){
		subjectId = '';
	}
	if(!dirId){
		dirId = '';
	}
	if(undir&&dirId!='recycle_personal'){
		dirId = '';
	}
	if(!tagId){
		tagId = '';
	}
	var pn = $("#note_pageNo").val();
	if(pn == ''){
		pn = 1;
	}
	var	url = webRoot + "/noteController/front/noteListMore.dht?pageNo=" + pn + "&subjectId=" + subjectId + "&dirId=" + dirId + "&searchInput=" + input + "&orderField=" + orderField + "&tagId=" + tagId 
			+ "&deleted=" + deleted;
	AT.post(url, null, function(data){
		var c = $("#contentListUl");
		c.append(data);
	});
	$("#note_pageNo").val(parseInt(pn) + 1);
}
</script>