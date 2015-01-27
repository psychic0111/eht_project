<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/webpage/front/include/front_common.jsp" %>  
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>重置密码</title>
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
            <div class="title">密码找回</div>
            <div class="Table">
            <form id="getpwdForm" action="<c:url value="/center/sendEmailPWD.dht"/>"   method="post" >
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">邮箱：</td>
	                  <td>
	                   <input class="InputTxt2" id="email" style="width:40%; height:28px;" type="text" name="email" datatype="e,*5-20"  autocomplete="off"/>
		                    <span class="Validform_checktip">${msg}</span> 
	                  </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td>
	                    <input class="Button1" type="submit" name="button" id="button" value="发送" />
	                   </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><a href="${webRoot}/" class="link1b"  >返回登录</a></td>
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
	 $("#getpwdForm").validate({
 	   rules:{
 		    email:{required:true,email:true,remote:{url:'${webRoot}/center/checkisEmail.dht'}},
		},
		messages:{ 
			email:{remote:'* 此邮箱还没有注册！',required:'* 请输入您的邮箱！',email:'* 邮箱格式不对!'},
		}
	}
	);
}); 
</script>
</body>
</html>
