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
					 content:{required:true,maxlength:200}
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
<div style="margin-bottom: 1px;padding-right: 10px; ">
          <div style="margin-top:-4px;width:75%;" class="left">
          	<textarea style="width:100%;height:50px"  name="content" cols="" rows="" id="pinglun"></textarea>
          	选择最近@的人或直接输入加空格
    	   </div>
          <div  class="right"  style="margin-top:-4px;">
          <input type="button" value="表情" id="face" class="Button4">
          <input type="button" value="评论" class="Button4" onclick="addComment()">
          </div>
          <div class="clear"></div>
</div>
</form>
<table width="100%" border="0" cellspacing="1" cellpadding="0">
<c:forEach items="${commentEntityList}" var="commentEntity">
            <tr class="TD1">
              <td width="35" align="center"><img src="${webRoot}/${commentEntity.accountCreateUser.photo}" width="35" height="34" /></td>
              <td style="word-break:break-all;word-wrap:break-word">
              <span class="Font2"><strong>${commentEntity.accountCreateUser.username}</strong></span><br />
              <div class="commentcontent">${commentEntity.content}</div>
                <span class="Font1"> <fmt:formatDate value ="${commentEntity.createTime}" pattern="yyyy-MM-dd HH:mm" /></span></td>
              <td width="80" align="center"><span class="others">
                <input class="Button_other1" type="button" name="button2" id="button2" value="删除" onclick="delComment('${commentEntity.id}')" />
                </span></td>
            </tr>
</c:forEach> 
</table>
<script type="text/javascript">
$.ajax({
    type:"POST", 
    url:'${webRoot}/noteController/front/getGroupsEmailAll.dht',
    data:{},
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
