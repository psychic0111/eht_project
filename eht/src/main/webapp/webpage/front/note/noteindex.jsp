<%@page import="com.eht.common.constant.Constants"%>
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

<link rel="stylesheet" href="${cssPath}/zTreeStyle/tagTree.css" type="text/css"/>

   <div class="right_index" id="right_index_mid" style="margin-bottom:1px;width:99%;">
	<!-- Begin notes--> 
    <div class="notes" style="width:100%;height:100%;">
      <!-- Begin function-->
      <div id="topFuncDiv" class="function" style="height:70px;width:95%;position:relative">
        <div class="search" style="width:97%;">
          <input type="hidden" name="subjectId" id="note_subjectId" value="${subjectId }"/>
          <input type="hidden" name="dirId" id="note_dirId" value="${dirId }"/>
          <input type="hidden" name="selectDirId" id="note_selectdirId" value="${selectDirId }"/>
          <input type="hidden" name="deleted" id="note_deleted" value="${deleted }"/>
          <input type="hidden" name="topNodeId" id="note_topNodeId" value="${topNodeId }"/>
          <input type="hidden" name="newEnable" id="note_newEnable" value="${newEnable }"/>
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td class="search_input"><input class="InputTxt1" style="width:85%;line-height:26px;" type="text" name="searchInput" id="searchNoteField" value="${searchInput }"/></td>
              <td class="search_btn"><input class="Button1"  onclick="searchNotesclick()" type="button" name="note_search" id="note_search" value="搜索" /></td>
            </tr>
          </table>
        </div>
        <div style="width:93%;">
	        <div class="others" style="float:left;margin-top:4px;height:30px;width:70%">
				<input type="hidden" name="tagId" id="note_tagId" value="${tagId }"/>
				<input type="hidden" name="parentTag" id="note_parentTag" value="${parentTag }"/>
				<input type="hidden" name="rootTag" id="note_rootTag" value="${rootTag }"/>
				<input id="tagSelect" style="float:left;height:25px;width:66%" type="text" onclick="showTagTree()" readonly value="" class="InputTxt_tag"/>
				<img alt="" src="${imgPath}/delete.png" style="cursor:pointer;float:left;margin-top:7px;" onclick='$("#tagSelect").val("");$("#note_tagId").val("");searchNotesclick();' width="20" height="20" title="清空过滤标签">
				<!-- <input id="tagQuery_btn" style="float:right;width:98px;" onclick="showTagTree()" class="Button5" type="button" value="标签过滤" name="tagQuery_btn"/> -->
			</div>
	        <div class="others" style="float:right;margin-top:5px;width:30%">
		         <style> 
					.box{border:1px solid 
					#C0C0C0;width:85px;height:23px;clip:rect(0px,181px,18px,0px);overflow:hidden;} 
					.box2{border:1px solid 
					#F4F4F4;width:85px;height:23px;clip:rect(0px,179px,16px,0px);overflow:hidden;} 
					select{position:relative;left:-2px;top:-2px;font-size:12px;width:86px;height:25px;line-height:14px;bo 
					rder:0px;color:#909993;} 
				</style> 
				<div class=box>
					<div class=box2>
				          <select hidefocus name="orderField" id="noteOrderField" onchange="searchNotesclick()">
				            <option value="createTime" <c:if test="${orderField == 'createTime'}">selected="selected"</c:if>>时间排序 </option>
				            <option value="title" <c:if test="${orderField == 'title'}">selected="selected"</c:if>>标题排序 </option>
				         </select>
			         </div>
			    </div> 
	        </div>
		</div>
        <div class="clear"></div>
      </div>
      <!-- End function--> 
      <!-- Begin notes_list-->
      <div id="noteList_div" style="position:relative;top:0;left:0;padding-top: 0px;">
			<div id="pageloading_search" style="display:block"></div>
      		<div class="notes_list" id="noteListIframe" style="width:93%;padding-left:10px;padding-top:10px;padding-right:5px;height:100%"></div>
      		<script type="text/javascript">$("#pageloading_search").show();</script>
      </div>
      <!-- End notes_list--> 
    </div>
    <!-- End notes--> 
    
    <div class="clear"></div>
   </div>   
    <div id="tagContent" class="menuContent" style="background:#FFFFFF;font-size:13px;border:1px solid #3C85BA;display:none; position: absolute;z-index:2003">
		<ul id="tagTree" class="tag_tree" style="margin-top:0; width:186px;"></ul>
	</div>
	<div id="divhiden" style="display:none;"></div>
<script type="text/javascript">
//滚动浮动
var funcDiv_top = $("#topFuncDiv").offset().top;
var funcDiv_left = $("#topFuncDiv").offset().left;
var topFuncDivWidth = $("#topFuncDiv").width();
window.onscroll = function(){
    var t = document.documentElement.scrollTop || document.body.scrollTop; 
    var top_div = $("#topFuncDiv");
    if(t >= funcDiv_top) {
        top_div.addClass("topDiv");
        $("#topFuncDiv").width(topFuncDivWidth);
    } else {
        top_div.removeClass("topDiv");
    }
};

var contentHeight = (document.documentElement.clientHeight - 72);
document.getElementById("right_index_mid").style.height = contentHeight + "px";
document.getElementById("noteList_div").style.height = (contentHeight - 100) + "px";
</script>
	