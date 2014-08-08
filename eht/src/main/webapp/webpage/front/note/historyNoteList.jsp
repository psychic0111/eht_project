<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>
<div style="width:540px;height:auto;">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="">
    <tbody style="">
    <tr style="">
      <td valign="top" style="height:321px;">
    <div> 
	<form name="historyNoteListForm" id="historyNoteListForm" method="post" > 
		<input type="hidden" value="${nodeId}" name="nodeId" id="historyNoteId">
          <!-- End function--> 
          <div>
              <table width="100%" border="0" cellspacing="1" cellpadding="0" style="table-layout:fixed;">
                	<tbody>
                	 <c:forEach items="${pageResult.rows}" var="noteversion">
                	<tr class="TD1">
                		<td width="90%">
                			<input type="hidden" value="3" name="id">
                  			<span class="Font2"><strong>内容：</strong></span>
                  			<font size="2" style="cursor:pointer;">
	                  				<strong title="${noteversion.content}"><c:out value="${noteversion.content}" escapeXml="true"></c:out></strong><br>
                  			</font>
                    		<span class="Font1">${noteversion.createtime}</span>
                    	</td>
                    	<td width="20%" align="center">
	                  		<span class="others">
	                    		<input type="button" value="还原" id="3" name="del_btn" onclick="shapeNote('${noteversion.id}')" class="Button_other1">
	                   		</span>
	              		</td>
                	</tr>
                	 </c:forEach>
              </tbody>
              </table>
          </div>
          <!-- Begin pages-->
          <div class="pages" style="width:525px;">
          	<xd:pager  pagerFunction="doPageHistoryNoteList" pagerStyle="cursor:pointer"  curPagerTheme="pages_cur" showTextPager="true"></xd:pager>
          	<!-- <a href="#">下一页</a> <a href="#" class="pages_cur">1</a> <a href="#">2</a> <a href="#">3</a> <a href="#">下一页</a> --> 
          </div>
          <!-- End pages--> 
		</form>
	</div></td>
    </tr>
  </tbody>
  </table>
</div>