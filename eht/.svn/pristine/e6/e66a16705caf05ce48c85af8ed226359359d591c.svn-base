<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ include file="/webpage/front/include/front_common.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>专题管理</title>
<script type="text/javascript">

	function toEditSubject(obj){
		url = "${webRoot}/subjectController/front/viewEditSubject.dht?id="+obj;
		AT.load("iframepage",url,function() {});	
	}

	function toMemberManage(obj){
		url = "${webRoot}/subjectController/front/memberManage.dht?id="+obj;
		AT.load("iframepage",url,function() {});	
	}
	function toExportSubject(obj){
	window.location.href= "${webRoot}/subjectController/front/exportSuject.dht?id="+obj;
		//AT.load("iframepage",url,iframeHeight);	
	}

	function toLeadingSuject(){
		url = "${webRoot}/subjectController/front/leadingSuject.dht";
		AT.load("iframepage",url,function() {});		
		}

</script>
</head>
<body>
    <div class="right_top">
       <div class="Nav" id="nav_div" style="padding-top:8px;">
			专题列表
			<input style="width:100px;height:23px;margin-left:10px;" id="subject_import" onclick="toLeadingSuject()" class="Button2" type="button" value="导入专题" name="subject_import"/>        
       </div>
    </div>
         <div class="right_index" >
	<!-- Begin Subjects-->
	<div class="Subjects">
		<ul>
			<c:forEach items="${subjectList}" var="sub">
			<li>
				<div class="img" style="cursor:pointer;">
					<img src="${imgPath}/temp6.jpg" width="100%" <xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>"> onclick="toEditSubject('${sub.id}')" </xd:hasPermission> />
				</div>
				<div class="title" style="cursor:pointer;" title="${sub.subjectName }"  <xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>"> onclick="toEditSubject('${sub.id}')" </xd:hasPermission>   >
				<c:choose>
					<c:when test="${fn:length(sub.subjectName) > 10}">
						<c:out value="${fn:substring(sub.subjectName, 0, 10)}......" />
					</c:when>
					<c:otherwise>
						<c:out value="${sub.subjectName}" />
					</c:otherwise>
				</c:choose>
				</div>
				<div class="others" style="height:25px">
					<xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>">
					<input class="Button4" type="button" name="button" id="button" style="width:80px;"
						value="导出专题"  onclick="toExportSubject('${sub.id}')"/> 
					<c:if test="${sub.subjectType eq 2}">
						<input class="Button1" type="button" style="width:80px;"  name="button" id="button" value="成员管理" onclick="toMemberManage('${sub.id}')" />
					</c:if>
					</xd:hasPermission>	
				</div>
			</li>
			</c:forEach>
			<li class="Add" onclick="toAddSubject()">
				<div class="Add_type" style="cursor:pointer;">
					<table width="100%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="${imgPath}/add.png" width="15" height="15" /></td>
						</tr>
					</table>
				</div>
			</li>
		</ul>
	</div>
	<!-- End Subjects-->
</div>

</body>
</html>
