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
			 div.append($("<a onclick='delsb(this);'  title='删除' href='#'>×</a>"));
			 $("."+obj.attr("id")).append(div);
			 obj.val('');
			 return;
			}
       	
		 AT.post("${webRoot}/subjectController/front/subjectTemember.dht","email="+obj.val()+"&textarea="+obj.attr("id"),function(data){
		 	 $("#invitememberAuto").find('ul').remove();
		 	 if($(data).find('li').length>0){
		 		$("#invitememberAuto").append(data);
		 		 var top = obj.offset().top+30;
			 		var left = obj.offset().left;
			 		$("#invitememberAuto").css({position: "absolute",'top':top ,'left':left});
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

function addTemember(){

for(var c=1;c<5;c++){
	var mail=$("#textarea"+c).val();
	if(mail==''){
	continue;
	}
	if(!mail.match(/^\w+([\.\-]\w+)*\@\w+([\.\-]\w+)*\.\w+$/)){
	if(c==1){
	 MSG.alert("超级管理员邮箱格式不正确");
	}
	if(c==2){
	 MSG.alert("编辑成员邮箱格式不正确");
	}
	if(c==3){
	 MSG.alert("作者邮箱格式不正确");
	}
	if(c==4){
	 MSG.alert("读者邮箱格式不正确");
	}
		return;		 
	}
	addemail(mail,"textarea"+c);
}

	AT.postFrm("addInvitemember",function(data){
		if(data==''){
				 MSG.alert('操作成功');
			}else{
                MSG.alert(data);
			}
			AT.load("iframepage","${webRoot}/subjectController/front/subjectManage.dht",function() {});	
	},true);
}
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
	 div.append($("<a onclick='delsb(this);'  title='删除' href='#'>×</a>"));
	 obj.append(div);
	 $("#"+id).val('');
	 $("#invitememberAuto").hide();
	 $("#invitememberAuto").find('ul').remove();
}
</script>
<body >
	<div class="right_top">
	    <div class="Nav" id="nav_div">邀请成员</div>
	</div>
	 <div class="right_index"> 
	 <form action="${webRoot}/subjectController/front/addInvitemember.dht" method="post" id="addInvitemember" name="addInvitemember"> 
		<input type="hidden" name="id" value="${id}">
          <!-- Begin Information-->
          <div class="Information">
            <div class="title">邀请成员(回车键录入邮箱)</div>
            <div class="Table">
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td width="100">超级管理员：</td>
                  <td><div class="Table">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td class="textarea1"></td>
                        </tr>
                        <tr>
                          <td><input class="InputTxt2" style="width:100%; height:28px;"  id="textarea1"  type="text"/></td>
                        </tr>
                      </table>
                    </div></td>
                </tr>
                <tr>
                  <td>编辑：</td>
                  <td><div class="Table">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td class="textarea2"></td>
                        </tr>
                        <tr>
                          <td>
                          <input  class="InputTxt2" style="width:100%; height:28px" id="textarea2"  type="text"/>
                          </td>
                        </tr>
                      </table>
                    </div></td>
                </tr>
                <tr>
                  <td>作者：</td>
                  <td><div class="Table">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td class="textarea3"></td>
                        </tr>
                        <tr>
                          <td>
							<input  class="InputTxt2" style="width:100%; height:28px" id="textarea3"  type="text"/>
						</td>
                        </tr>
                      </table>
                    </div></td>
                </tr>
                <tr>
                  <td>读者：</td>
                  <td><div class="Table">
                      <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td class="textarea4"></td>
                        </tr>
                        <tr>
                          <td> 
                          <input  class="InputTxt2" style="width:100%; height:28px" id="textarea4"  type="text"/>
                          </td>
                        </tr>
                      </table>
                    </div></td>
                </tr>
              </table>
              <div class="submit">
                <input class="Button1" type="button" name="button" id="button" value="发送邀请" onclick="addTemember();"/>
              </div>
            </div>
          </div>
          </form>
          <!-- End Information--> 
        </div>
	<div class="rightMenu"  id="invitememberAuto">
    </div>
