<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>注册</title>
<%@ include file="/webpage/front/include/front_common.jsp"%>  

</head>
<body >

<!-- Begin header-->
<div class="header">
  <div class="left1"><img src="${webpagePath}/front/images/logo.png"  height="40" /></div>
  <div class="right1"></div>
  <div class="clear"></div>
</div>
<!-- End header--> 
<!-- Begin mainer-->
<div class="mainer"  style="background:url();height:650px">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right"><div class="right_top">
          <div class="Nav"></div>
          <div class="CurTitle" style="padding-left:50px;"></div>
        </div>
        <!-- Begin mainer_index-->
        <div class="right_index"> 
          <!-- Begin Information-->
          <div class="Information" style="padding:50px; padding-top:20px;">
            <div class="title">填写注册信息</div>
            <div class="Table">
            <form id="regForm" name="regForm" action="<c:url value="/center/reg.dht"/>" method="post">
            <input type="hidden" name="id" value="<%=request.getParameter("id")%>"/>
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
	                    <input class="InputTxt2" id="userName"  style=" width:40%; height:28px; "  type="text" name="userName" autocomplete="off" />
	                    
	                    </td>
	                </tr>
	                <tr>
	                  <td>邮箱：</td>
	                  <td>
	                  <%
						String email=request.getParameter("email");
                         if(email!=null){
                         %>
                          <input class="InputTxt2" id="email"  style=" width:40%; height:28px; " type="text" onblur="repeat(this);" value="<%=request.getParameter("email")%>"  name="email" autocomplete="off"  />
	                 
                      <% }else{
                      
                      %>
                       <input class="InputTxt2" id="email"  style=" width:40%; height:28px; " type="text" onblur="repeat(this);"   name="email" autocomplete="off"  />
                     <% 
                      }
                      %>
	                    </td>
	                </tr>
	                <tr>
	                  <td width="100">密码：</td>
	                  <td>
	                   <input class="InputTxt2"  style=" width:40%; height:28px; " id="password" name="password" type="password" autocomplete="off" />
	               	  </td>
	                </tr>
	                <tr>
	                  <td>确认密码：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px; " id="passwordConf" name="passwordConf"  recheck="password" type="password" autocomplete="off"  />
	                  </td>
	                </tr>
	                <tr>
	                  <td>验证码：</td>
	                  <td>
	                    <input type="text" class="InputTxt2"  id="code" name="code"    style="width:100px; height:28px;" autocomplete="off" />
	                    <a href="javascript:getVerifiCode()" class="link3">看不清换一个</a>
	                    <img id="verifi_code" onclick="getVerifiCode();" class="img_link" src="${webRoot}/getVerifiCode.dht" title="看不清换一个" align="absmiddle" />
	               </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><input class="Button1" type="submit" name="button" id="button" value="确定" /></td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><a href="${webRoot}/" class="link1b">返回登录</a></td>
	                </tr>
	              </table>
              </form>
            </div>
          </div>
          <!-- End Information--> 
        </div>
        <!-- End mainer_index--></td>
    </tr>
  </table>
</div>
<!-- End mainer--> 
<!-- Begin footer-->
<div class="footer">
  <div class="left">&copy; Copyright <a href="#" class="link3">website.com</a></div>
  <div class="right">Powered by CNOOC-VS</div>
  <div class="clear"></div>
</div>
<!-- End footer-->

<script type="text/javascript">  

   function repeat(obj){
	   var name= obj.value;
	   if(name==''){
	   $("#by").remove();
	   }else{
  		 var params = {'username':name};  
    		AT.post("${webRoot}/center/checkUserStats.dht",params,function(data){
    		if(data=='false'){
    		$("#by").remove();
    		var link='${webRoot}/center/repeat.dht?username='+escape(name);
    		var by='<input id="by" class="Button1" type="button" onclick="window.location.href=\''+link+'\'" value="账号未激活,重发邮件" style="display:inline" />';
    		$(obj).after(by); 
    		}else{
    		$("#by").remove();
    		}
			},false);
   
   }
  		
   //window.location.href='${webRoot}/center/repeat.dht?username='+escape(obj);
	}
	
	$().ready(function() {
		$("#regForm").validate({
	 	   rules:{
				userName:{required:true,maxlength:200,remote:{url:'${webRoot}/center/checkUser.dht'}},
			    email:{required:true,email:true,remote:{url:'${webRoot}/center/checkEmail.dht'}},
				code:{required:true,remote:{url:'${webRoot}/center/checkCode.dht'}},
				password:{required:true,maxlength:20,minlength:6},
				passwordConf:{required:true,maxlength:20,minlength:6,equalTo:'#password'}
			},
			messages:{
				userName:{remote:'* 账号已存在！',required:'* 请输入您的账号！'},
				email:{remote:'* 邮箱已存在！',required:'* 请输入您的邮箱！',email:'* 邮箱格式不对!'},
				passwordConf:{equalTo:'* 确认密码要和密码一致!'},
				code:{remote:'* 验证码不正确！',required:"* 请输入验证码！"}
			}
		}
		);
	}); 
	function getVerifiCode(){
        document.getElementById("verifi_code").src ="${webRoot}/getVerifiCode.dht?r=" + new Date().getTime();
        }   
</script>
</body>
</html>
