<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />

<link rel="stylesheet" href="${cssPath}/jquery.sinaEmotion.css" type="text/css">
<link rel="stylesheet" href="${cssPath}/tinyscrollbar.css" type="text/css" media="screen"/>
<script type="text/javascript" src="${frontPath}/js/plugins/jquery.tinyscrollbar.js"></script>

<div id="scrollbar1" style="height:100%;">
   <div class="scrollbar"><div class="track"><div class="thumb"><div class="end"></div></div></div></div>
   	<div class="viewport">
   	<div class="overview" style="width:99%;">
   		
   		<!-- 记录noteList第一个目录id（用来初始化第一个目录详细内容区） -->
   		<c:set var="first" value="true"></c:set>
   		<c:set var="firstNoteId" value=""/>
		<ul id="contentListUl" style="width:100%;">
			<c:forEach items="${noteList }" var="note">
			
				<c:if test="${first==true }">
					<c:set var="firstNoteId" value="${note.id}"/>
					<c:set var="first" value="false"></c:set>
				</c:if>
				<li style="width:100%;">
				<div class="title" id="title_${note.id}" style="font-size:15px;">
					<a href="#" onclick="viewNoteclick('${note.id}','${note.subjectId}')">
						<img src="${imgPath}/note2.png" style="max-height:16px;"/>
						<c:choose>
    						<c:when test="${fn:length(note.title) > 15}">
    							<c:out value="${fn:substring(note.title, 0, 15)}......" />
     						</c:when>
   						  	<c:otherwise>
    					 		${note.title }
    					 	</c:otherwise>
    					</c:choose>
					</a>
					<span onclick="restoreNote('${note.id}')" class="recycle_png" style="float:right;cursor:pointer;display:none;">
						<img src="${imgPath}/arrow_redo.png" title="恢复条目"/>
					</span>
					<input type="hidden" name="id" value="${note.id }"/>
				</div>
				<div class="contents" id="content_${note.id}" style="word-wrap:break-word;">
					<a href="#" onclick="viewNoteclick('${note.id}','${note.subjectId}')">
						<div class="txts">
							${note.summary}
							<br/>
							<div class="Font1">
								<span class="createUser" style="font-weight:normal;color:cadetblue;font-size:12px;">${note.accountCreateUser.username }</span>
								&nbsp;&nbsp;
								- &nbsp;&nbsp;<c:if test="${note.updateTime != null}">
									<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${note.updateTime }" type="both"/>
								</c:if>
								<c:if test="${note.updateTime == null}">
									<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${note.createTime }" type="both"/>
								</c:if>
							</div>
						</div>
					</a>
					<div class="state" id="note_status_${note.id}">
					</div>
				</div>
				</li>
			</c:forEach>
		</ul>
		<input type="hidden" value="${firstNoteId }" id="firstNodeId" />
	</div>
	</div>
</div>
<script type="text/javascript">
function initScrollBar(){
	var height = $("#noteList_div").height() - 10;//document.documentElement.clientHeight;//document.body.clientHeight;
	$("#scrollbar1").height(height);
}
$(document).ready(function(){
	var height = $("#noteList_div").height() - 10;//document.documentElement.clientHeight;//document.body.clientHeight;
	$("#scrollbar1").height(height);
	var b = $('#scrollbar1').tinyscrollbar({wheelSpeed:300,size:height});
	
	if($("#note_deleted").val()==1){
		$("span.recycle_png").show();
	}
	
	$("#contentListUl").find("div.state").each(function(index,obj){
		var noteid = obj.id.replace("note_status_","");
		AT.get(webRoot+"/noteController/front/noteStatus.dht?id="+noteid,function(json){
			if(json.isRead == 'true'){
				$("#"+obj.id).append("<img id=\"readpng_"+obj.id+"\" src=\""+imgPath+"/read.png\" /> <span>已读</span> ");
			}else{
				$("#"+obj.id).append("<img id=\"readpng_"+obj.id+"\" src=\""+imgPath+"/readno.png\" /> <span>未读</span> ");
			}
			if(parseInt(json.attachmentCount) > 0){
				$("#"+obj.id).append("<img src=\""+imgPath+"/attachment.png\" />附件");
			}
		},false);
	});
});

//窗口大小改变事件
var rId;
$(window).wresize(function(){
	//window.clearTimeout(rId);
	//rId = window.setTimeout('initScrollBar();', 1000);
});
</script>