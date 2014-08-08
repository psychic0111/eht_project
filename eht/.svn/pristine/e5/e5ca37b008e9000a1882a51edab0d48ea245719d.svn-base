<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%-- <%
	String uid = null;
 	String logintype = null;
	Object obj=request.getSession().getAttribute("3uinfo");
	if(obj!=null){
		String[] terms=obj.toString().split("\t");
		uid=terms[0].trim();
		logintype=terms[1].trim();
		request.getSession().removeValue("3uinfo");
	}
%> --%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>注册</title>
<%@ include file="/webpage/front/include/front_common.jsp"%> 
<%-- <c:set var="uid" value="<%=uid%>" />
<c:set var="logintype" value="<%=logintype%>" /> --%>
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
<div class="mainer">
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
            <div class="title">已经注册过E划通（绑定和E划通账户）</div>
            <div class="Table">
       		
            <form id="loginForm" name="loginForm" action="<c:url value="/center/login.dht"/>" method="post">
					<!-- 第三方id （id）-->
		     		<input type="hidden" name="bind_openid" value="${uid}"/>
		       		<!-- 第三方user(账号) -->
		       		<input type="hidden" name="bind_openuser" value="test123"/>
		       		<!-- 第三方类型（qq,sina.....) -->
		       		<input type="hidden" name="bind_opentype" value="${logintype}"/>
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
	                    <input class="InputTxt2" id="username"  style=" width:40%; height:28px; " type="text" name="username" />
	                    </td>
	                </tr> 
	                <tr>
	                  <td width="100">密码：</td>
	                  <td>
	                    <input class="InputTxt2" id="password"  style=" width:40%; height:28px; " type="password" name="password" />
	                    </td>
	                </tr> 
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><input class="Button1" type="submit" name="button" id="button" value="绑定" /></td>
	                </tr>
	              </table>
              </form>
            </div>
            <div class="title">还没有注册过E划通（创建E划通账户）</div>
            <div class="Table">
            <form id="regForm" name="regForm" action="<c:url value="/center/reg.dht"/>" method="post">
				<!-- 第三方id （id）-->
	     		<input type="hidden" name="reg_openid" value="${uid}"/>
	       		<!-- 第三方user(账号) -->
	       		<input type="hidden" name="reg_openuser" value="test123"/>
	       		<!-- 第三方类型（qq,sina.....) -->
	       		<input type="hidden" name="reg_opentype" value="${logintype}"/>
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
	                    <input class="InputTxt2" id="username"  style=" width:40%; height:28px; " type="text" name="username" />
	                    </td>
	                </tr>
	                <tr>
	                  <td>邮箱：</td>
	                  <td><input class="InputTxt2" id="email"  style=" width:40%; height:28px; " type="text"   name="email"  />
	                    </td>
	                </tr>
	                <tr>
	                  <td>验证码：</td>
	                  <td>
	                    <input type="text" class="InputTxt2"  id="code" name="code"    style="width:100px; height:28px;" />
	                    <a href="javascript:getVerifiCode()" class="link3">看不清换一个</a>
	                    <img id="verifi_code" onclick="getVerifiCode();" class="img_link" src="${webRoot}/getVerifiCode.dht" title="看不清换一个" align="absmiddle" />
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
	$().ready(function() {
		$("#loginForm").validate({
	 	   rules:{
				username:{required:true,maxlength:20},
				password:{required:true,maxlength:20}
			},
			messages:{
				username:{required:'*账号不能为空！'},
				password:{required:'*密码不能为空！'}
			}
		});

		$("#regForm").validate({
	 	   rules:{
				username:{required:true,maxlength:20,remote:{url:'${webRoot}/center/checkUser.dht'}},
			    email:{required:true,email:true,remote:{url:'${webRoot}/center/checkEmail.dht'}},
				code:{required:true,remote:{url:'${webRoot}/center/checkCode.dht'}}
			},
			messages:{
				username:{remote:'* 账号已存在！',required:'* 请输入您的账号！'},
				email:{remote:'* 邮箱已存在！',required:'* 请输入您的邮箱！',email:'* 邮箱格式不对!'},
				code:{remote:'* 验证码不正确！',required:"* 请输入验证码！"}
			}
		});
	}); 
	function getVerifiCode(){
        document.getElementById("verifi_code").src ="${webRoot}/getVerifiCode.dht?r=" + new Date().getTime();
        }   
</script>
</body>
</html>
