<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>

<div style="width:100%;height:auto;">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="">
    <tbody style="">
    <tr style="">
      <td valign="top" style="height: 321px;">
    <div> 
	<form name="blackListNoteForm" id="blackListNoteForm" method="post" > 
		<input type="hidden" value="${nodeId}" name="nodeId" >
		<input type="hidden" value="${subjectid}" name="subjectid" >
          <div>
              <table width="100%" border="0" cellspacing="1" cellpadding="0" style="table-layout:fixed;">
                	<tbody>
                	 <c:forEach items="${pageResult.rows}" var="RoleUser">
                	<tr class="TD1">
                		<td width="80%">
                  			<span class="Font2"><strong>用户：</strong></span>
                  			<font size="2" style="cursor:pointer;">
	                  				<strong title="${RoleUser.accountEntity.username}"><c:out value="${RoleUser.accountEntity.username}" escapeXml="true"></c:out></strong><br>
                  			</font>
                    		<span class="Font1"></span>
                    	</td>
                    	<td width="20%" align="center">
	                  		<span class="others">
	                  			<c:if test="${ RoleUser.accountEntity.id != SESSION_USER_ATTRIBUTE.id}">
	                  		     <c:if test="${RoleUser.blackList eq true}">
	                  		     <input type="button" value="移除黑名单" id="3" name="del_btn" onclick="addBlackListNote('${RoleUser.accountEntity.id}','${nodeId}',this)" class="Button_other1">
	                  		     </c:if>
	                  		    <c:if test="${RoleUser.blackList eq false}">
	                  		    <input type="button" value="加入黑名单" id="3" name="del_btn" onclick="addBlackListNote('${RoleUser.accountEntity.id}','${nodeId}',this)" class="Button_other1">
	                  		     </c:if>
	                  		     </c:if>
	                   		</span>
	              		</td>
                	</tr>
                	 </c:forEach>
              </tbody>
              </table>
          </div>
          <!-- Begin pages-->
          <div class="pages">
          	<xd:pager  pagerFunction="doPageblackList" pagerStyle="cursor:pointer"  curPagerTheme="pages_cur" showTextPager="true"></xd:pager>
          	<!-- <a href="#">下一页</a> <a href="#" class="pages_cur">1</a> <a href="#">2</a> <a href="#">3</a> <a href="#">下一页</a> --> 
          </div>
          <!-- End pages--> 
		</form>
	</div></td>
    </tr>
  </tbody>
  </table>
</div>