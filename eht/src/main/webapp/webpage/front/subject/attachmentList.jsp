<%@page import="com.eht.common.util.AppRequstUtiles"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>  
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %> 
<%
	String basePath = AppRequstUtiles.getAppUrl(); 
%>
<c:set var="webRoot" value="<%=basePath%>" /> 
<script type="text/javascript">
//分页
function doPage(ths,pageNo,pageSize){
	AT.load("datadiv","${webRoot}/subjectController/front/dirAttaManage.dht?subjectId=${subjectId}&dirId=${dirId}&pageNo="+pageNo+"&pageSize="+pageSize+"&ispage=true",function() {});
}
function removeCurrAttachment(obj){
	var submit = function (v, h, f) {
	    if (v == true){
	    	 var params = {"id":$(obj).attr("attaid")};  
	    	 var url = "${webRoot}/noteController/front/removeAttach.dht";
	    	 AT.post(url, params, function(data){
	    	 	var url="${webRoot}/subjectController/front/dirAttaManage.dht?subjectId=${subjectId}&dirId=${dirId}&ispage=true&pageNo=${pageNo}";
	    		AT.get(url,function(data){
	    			$("#datadiv").html(data);
	    		});
	    	 });
	    }
	    return true;
	}; 
	$.jBox.confirm("您确定删除？", "提示", submit, { buttons: { '确定': true, '取消': false} })
	 
	 
}

</script> 
	<input type="hidden" name="sessionId" id="sessionId" value="${sessionId }" />
	<input type="hidden" name="dirId" id="dirId" value="${dirId}" />
	<input type="hidden" id="wdzl_dirId" value="" />
	
	 
	              <table  width="100%" border="0" cellspacing="1" cellpadding="0" style="padding-right:4px;">
					<tr id="trFirst"  class="TD1"> 
						<th  width="20%" align="center">附件名</th>
						<th  width="10%" align="center">所属目录</th>
						<th  width="10%" align="center">所属条目</th>
						<th  width="15%" align="center">更新时间</th>
						<th  width="10%" align="center">上传人</th>
						<th  width="20%" align="center">操作</th>
					</tr> 
					<c:forEach items="${pageResult.rows }" var="atta">
						<tr  class="TD1"> 
							<td align="right">
								<span onclick="downloadByid('${atta.id}')">
									<a href="javascript:;">
										<c:choose>
										    <c:when test="${fn:length(atta.fileName)>20}">
										       <b title="${atta.fileName}">${fn:substring(atta.fileName,0,18) }...</b>
										    </c:when>
										    <c:when test="${fn:length(atta.fileName)<=20}">
										      <b title="${atta.fileName}">${atta.fileName }</b>
										    </c:when> 
										</c:choose>								
									</a>
								</span>&nbsp;
							</td>
							<td align="center">${atta.directoryEntity.dirName }</td>
							<td align="center">
								<c:choose>
									<c:when test="${fn:length(atta.noteEntity.title) >= 8}">
										${fn:substring(atta.noteEntity.title, 0, 8)}...
									</c:when>
									<c:otherwise>
										${atta.noteEntity.title}
									</c:otherwise>
								</c:choose>
								
							</td>
							<td align="center">
								<c:choose>
									<c:when test="${atta.updateTime != null}">
										<fmt:formatDate value="${atta.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
									</c:when>
									<c:otherwise>
										<fmt:formatDate value="${atta.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
									</c:otherwise>
								</c:choose>
							</td>
							<td align="center">${atta.creator.username }</td>
							<td align="center">
								<span onclick="downloadByid('${atta.id}')"><a class="Button2" href="javascript:;">下载</a></span>&nbsp;
								<span onclick="removeCurrAttachment(this)"  attaid="${atta.id}"><a class="Button_other1" href='javascript:;'>删除</a></span>
							</td>
						</tr> 
					</c:forEach>
				</table>
          <!-- Begin pages-->
          <div class="pages">
          		<xd:pager  pagerFunction="doPage" pagerStyle="cursor:pointer"  curPagerTheme="pages_cur" showTextPager="true"></xd:pager>
          </div>
          <!-- End pages-->
