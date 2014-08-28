<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">
$().ready(function() {
	 $('input[id^=textarea]').bind("keyup",function(event){
		 var obj=$(this);
		if(obj.val()!=''){
		 if(event.keyCode==13){
			 if(!obj.val().match(/^\w+([\.\-]\w+)*\@\w+([\.\-]\w+)*\.\w+$/)){
				 MSG.alert("邮箱格式不正确");
				 return;
				 }
			 hideInvitememberMenu();
			 var div = $("<div style='float:left'></div>");
			 div.append($("<input type='hidden' name='"+obj.attr("id")+"' value='"+obj.val()+"'><span class='inner-tag-name'>"+obj.val()+"</span>"));
			 div.append($("<input type='hidden' name='type' value='"+ $("#textareatype").val()+"'>"));
			  div.append($("<a >"+$("#textareatype option:selected").text()+"</a>"));
			 div.append($("<a onclick='delsb(this);'  title='删除' href='#'>×</a>"));
			 $("."+obj.attr("id")).append(div);
			 obj.val('');
			 return;
			}
       	
		 AT.post("${webRoot}/subjectController/front/subjectTemember.dht","email="+obj.val()+"&textarea="+obj.attr("id"),function(data){
		 	 $("#invitememberAuto").find('ul').remove();
		 	 if($(data).find('li').length>0){
		 		$("#invitememberAuto").append(data);
		 		if ((navigator.userAgent.indexOf('MSIE') >= 0) && (navigator.userAgent.indexOf('Opera') < 0)){
		 		$("#invitememberAuto").css({position: "absolute",'top':155 ,'left':148.25});
		 		}
   				else {
   				$("#invitememberAuto").css({position: "absolute",'top':140 ,'left':148.25});
   				}
			 		$("#invitememberAuto").show();
			 	 }else{
			 		$("#invitememberAuto").hide();
				 	 }
		 });
		}else{
			 $("#invitememberAuto").find('ul').remove();
			 $("#invitememberAuto").hide();
			}
		});
});

function delsb(obj){
	 $(obj).parent().remove();
}

function hideInvitememberMenu(){
	$("#invitememberAuto").hide();
}

function addemail(value,id){
	var obj=$("."+id);
	var div = $("<div style='float:left'></div>");
	 div.append($("<input type='hidden' name='"+id+"' value='"+value+"'><span class='inner-tag-name'>"+value+"</span>"));
	  div.append($("<input type='hidden' name='type' value='"+ $("#textareatype").val()+"'>"));
	  div.append($("<a >"+$("#textareatype option:selected").text()+"</a>"));
	 div.append($("<a onclick='delsb(this);'  title='删除' href='#'>×</a>"));
	 obj.append(div);
	 $("#"+id).val('');
	 $("#invitememberAuto").hide();
	 $("#invitememberAuto").find('ul').remove();
}
</script>
<div class="mainer" id="page_mainer">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right">
       <div class="right_index"> 
	 <form action="${webRoot}/subjectController/front/addInvitemember.dht" method="post" id="addInvitemember" name="addInvitemember" onsubmit="return false;" > 
		<input type="hidden" name="id" value="${id}">
          <!-- Begin Information-->
          <div class="Information">
            <div class="title">邀请成员(回车键录入邮箱)</div>
            <div class="Table">
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td><div class="Table">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                          <td style="height:28px">
                  		<select id="textareatype" style="width:20%;height:28px;vertical-align:middle;">
			                  <option value="1">超级管理员</option>
			                  <option value="2">编辑</option>
			                  <option value="3">作者</option>
			                  <option value="4">读者</option>
                 		 </select>
                 		 <input class="InputTxt2" style="width:60%;height:28px;vertical-align:middle;line-height:28px;"  autocomplete="off" id="textarea1"  type="text"/></td>
                        </tr>
                        <tr>
                          <td class="textarea1"></td>
                        </tr>
                      </table>
                    </div></td>
                </tr>
              </table>
            </div>
          </div>
          </form>
          <!-- End Information--> 
        </div>
	<div class="rightMenu"  id="invitememberAuto">
    </div>
	</td>
    </tr>
  </table>
</div>


	
