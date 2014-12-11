<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<style type="text/css" >
#pinglun, #autoTalkText, #autoUserTipsPosition{
			font-size:normal;
			font-family:Tahoma, Arial;
			line-height:normal;
			
		}
	/*
	 * user auto tips css
	 */
	.recipients-tips{ font-family:Tahoma, Arial;position:absolute; background:#f7f7f2; z-index:2147483647; padding:2px; border:1px solid #6b9228; display:none;}
	.recipients-tips li a{display:block; padding:2px 5px; color:#333; cursor:pointer; font-family:Tahoma, Arial;}
	.recipients-tips li a em{font-weight:700; color:#000; font-family:Tahoma, Arial;}
	.autoSelected{background:#b8c796; font-family:Tahoma, Arial;}
	
	/*****/
	.textarea{ width:300px; height:50px; padding:3px; font-family:Tahoma, Arial;}
</style>
<script type="text/javascript">
	// 绑定表情
	var context='${imgPath}/biaoqing/';
	$('#face').SinaEmotion($('#pinglun'),context);
	$().ready(function() {
		 $("#addCommentForm").validate({
				rules:{
					comment_content:{required:true,maxlength:200}
				}
			}
		);
		
	});
	$(".commentcontent").each(function(index){
   		  var inputText = $(this).text();
   		  $(this).html(AnalyticEmotion(inputText,context));
	});
</script>
<form action="${webRoot}/commentController/front/commentAdd.dht" method="post" id="addCommentForm" name="addCommentForm"> 
<input type="hidden" name="noteId" id="noteId" value="${noteId}">
<div style="margin-bottom:1px;padding-right:10px;width:100%;">
   <div style="margin-top:-4px;width:80%;" class="left">
     	<font color="#999999">选择最近@的人或直接输入加空格</font>
     	<textarea style="width:100%;height:50px"  name="comment_content" cols="" rows="" id="pinglun"></textarea>
   </div>
   <div class="left"  style="margin-top:15px;margin-left:5px;width:17%;">
   		<input type="button" value="评论" class="Button5" style="height:53px;" onclick="addComment()">
   		<input type="button" value="表情" id="face" style="height:53px;" class="Button4">
   </div>
          <div class="clear"></div>
</div>
</form>
<table width="97%" border="0" cellspacing="0" cellpadding="0" style="border:0px;background-color:#f3f3f3;">
<c:forEach items="${commentEntityList}" var="commentEntity">
     <tr>
       <td style="border:0px;" width="35" align="center">
       	<img onerror="loadDefaultPhoto(this)" src="${webRoot}/${commentEntity.accountCreateUser.photo}" width="35" height="34" />
       </td>
       <td style="border:0px;" style="word-break:break-all;word-wrap:break-word">
       		<span class="Font2"><strong>${commentEntity.accountCreateUser.username}</strong></span><br />
       		<div class="commentcontent" style="font-size:13px;">${commentEntity.content}</div>
         	<span class="Font1"> <fmt:formatDate value ="${commentEntity.createTime}" pattern="yyyy-MM-dd HH:mm" /></span>
       </td>
       <td style="border:0px;" width="80" align="center">
	       <span class="others">
	         <input class="Button_other1" type="button" name="button2" id="button2" value="删除" onclick="delComment('${commentEntity.id}')" />
	       </span>
       </td>
     </tr>
</c:forEach> 
</table>
<script type="text/javascript">
$.ajax({
    type:"POST", 
    url:'${webRoot}/noteController/front/getGroupsEmailSuject.dht',
    data:{'subjectid':$("#noteForm_subjectId").val()},
    dataType:"json",
    success:function(result){
       
	var friendsData=[];
	 $.each(result.data, function(index, objVal) { 
		 friendsData[index]={'user':objVal.name,'name':objVal.name};
      });
	 userAutoTips({id:'pinglun'},friendsData);
    }

});
</script>
