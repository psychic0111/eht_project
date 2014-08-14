<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<%@ include file="/webpage/front/include/front_common.jsp" %>
<title>登录e划通</title> 
</head>
<body >
<!-- Begin header-->
<div class="header">
  <div class="left1"><img src="${imgPath}/logo.png"  height="40" /></div>
  <div class="right1"></div>
  <div class="clear"></div>
</div>
<!-- End header--> 
<!-- Begin mainer-->
<div class="mainer_login">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right"><div class="right_top">
          <div class="Nav"></div>
          <!-- <div class="CurTitle" style="padding-left:50px;">登录e划通</div> -->
        </div>
        <!-- Begin mainer_index-->
        <div class="right_index"> 
          <!-- Begin Information-->
          <div class="Information" style="padding:50px; padding-top:20px;">
            <div class="title">填写登录信息</div>
            <div class="Table"> 
            <form id="loginForm" name="loginForm" action="<c:url value="/center/login.dht"/>" method="post">
	               <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
	                  	<input class="InputTxt2"  style=" width:40%; height:28px;line-height:28px; " type="text" name="username" id="username" autocomplete="off" value="${username}"/><c:if test="${sendmail eq '1'}"><input class="Button1" type="button" onclick="repeat('${username}');" value="账号未激活,重发邮件" style="display:inline" /></c:if>
                  	  </td>
	                </tr>
	                <tr>
	                  <td>密码：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px;line-height:28px; " type="password" name="password" id="textfield2" autocomplete="off" value="${password}" />
                  	  </td>
	                </tr>
	                <tr>
	                  <td>验证码：</td>
	                  <td>
	                  	<input type="text" class="InputTxt2"  id="code" name="code"   style="width:100px; height:28px;line-height:28px;"  autocomplete="off" />
	                    <img id="verifi_code" onclick="getVerifiCode();" class="img_link" src="${webRoot}/getVerifiCode.dht" title="看不清换一个" align="absmiddle" />
	                    <a href="javascript:getVerifiCode()" class="link3">看不清换一个</a>
	                   </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td>
	                    <input class="Button1" type="submit" name="button" id="button" value="登录" />&nbsp;&nbsp;&nbsp;<font color='red'>${message}</font>
	                   </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td>
	                  	     第三方登录：
	                  	 <a href="https://api.weibo.com/oauth2/authorize?client_id=4257296204&response_type=code&redirect_uri=http://127.0.0.1:81/eht/webpage/login/sinalogin.jsp"><img src="${imgPath}/sina.jpg" /></a>
	                  	 <a href="https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=801445396&redirect_uri=${webRoot}/gadUser/qqUniteLogin.dht" class="link5"> <img src="${imgPath}/qq.jpg" /></a>
					     <img src="${imgPath}/others.jpg" />
           			  </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><a href="${webpagePath}/register.jsp" class="link1b">立即注册e划通帐户</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="${webpagePath}/user/retrievepassword.jsp" class="link1b">忘记密码？</a></td>
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
   window.location.href='${webRoot}/center/repeat.dht?username='+escape(obj);
}
$().ready(function() {
	$("#username").focus();
	$("#loginForm").validate({
 	   rules:{
			username:{required:true,maxlength:200},
			password:{required:true,maxlength:20},
			code:{required:true,remote:{url:'${webRoot}/center/checkCode.dht'}
			}
		},
		messages:{
			username:{required:'*账号不能为空！'},
			password:{required:'*密码不能为空！'},
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

