<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@include file="/context/mytags.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>登录e划通</title>
<LINK href="${webpagePath}/front/css/css.css" type=text/css rel=stylesheet>
<LINK href="${webpagePath}/front/css/common.css" type=text/css rel=stylesheet>
<script type="text/javascript" src="${webRoot}/plug-in/jquery/jquery-1.8.3.js"></script>
</head>
<body >
<!-- Begin header-->
<div class="header">
  <div class="left1"><img src="${webpagePath}/front/images/logo.png"  height="40" /></div>
  <div class="right1"> <img src="${webpagePath}/front/images/Sline.png"  height="40" /><%-- <img src="${webpagePath}/front/images/temp.jpg" width="22" height="22" /> <a href="#"> ${user.username}!<img src="${webpagePath}/front/images/Sline.png"  height="40" /></a> --%></div>
  <div class="clear"></div>
</div>
<!-- End header--> 
<!-- Begin mainer-->
<div class="mainer_login">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right"><div class="right_top">
          <div class="Nav"></div>
          <div class="CurTitle" style="padding-left:50px;"></div>
        </div>
        <!-- Begin mainer_index-->
        <div class="right_index" > 
          <!-- Begin Information-->
          <div class="Information" style="padding:50px; padding-top:20px;text-align:center">
          	 <c:choose>
          	 	<c:when test="${msg!=null}">
						       ${msg}   	 	
          	 	</c:when>
          	 	<c:otherwise>
           			 请进入邮箱激活账号 ,  
          	 	</c:otherwise>
          	 </c:choose>

          	 
          	 <c:choose>
          	 	<c:when test="${linkpath!=null}">
				    <p><span class="showbox" style="color:blue;font-size:15px"></span>秒之后将自动跳转到${linkname}页面。</p>
           	        <p><a href="${webRoot}/${linkpath}" style="color: blue; text-decoration:none;" >跳转到${linkname}</a></p>   	 	
          	 	</c:when>
          	 	<c:otherwise>
          	 	</c:otherwise>
          	 </c:choose>
          	  <p><a href="#" onclick="window.close();">关闭此页</a></p>
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


<script type="text/javascript">
    //设置超时时间为10秒钟
    <c:if test="${linkpath!=null}">
     var timeout = 10;
     show();
     function show() {
         var showbox = $(".showbox");
         showbox.html(timeout);
         timeout--;
         if (timeout == 0) {
             window.opener = null;
             window.location.href = "${webRoot}/${linkpath}";

         }
         else {
             setTimeout("show()", 1000);
         }
     }
     </c:if>
</script>
</body>
</html>

