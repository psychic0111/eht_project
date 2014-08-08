<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>个人资料</title>
<%@ include file="/webpage/front/include/front_common.jsp" %>

<LINK href="${webpagePath}/front/css/css_login.css" type=text/css rel=stylesheet>
<LINK href="${webpagePath}/front/css/common.css" type=text/css rel=stylesheet>

</head>
<body >
<!-- Begin header-->
<%@include file="/webpage/front/include/head.jsp"%>
<!-- End header--> 
<!-- Begin mainer-->
<div class="mainer"  style="background:url();height:650px">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right"><div class="right_top">
          <div class="Nav"></div>
          <div class="CurTitle" style="padding-left:50px;">个人资料</div>
        </div>
        <!-- Begin mainer_index-->
        <div class="right_index"> 
          <!-- Begin Information-->
          <div class="Information" style="padding:50px; padding-top:20px;">
            <div class="title">填写信息</div>
            <div class="Table">
            <form id="setpwdForm" action="<c:url value="/accountController/front/editUser.dht"/>"  enctype="multipart/form-data" name="setpwdForm" method="post" >
            	<input type="hidden" name="id" value="<%=request.getParameter("id")%>"/> 
	              <table width="100%" border="0" cellspacing="0" cellpadding="0"> 
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
							<label>${SESSION_USER_ATTRIBUTE.userName}</label>&nbsp;&nbsp;&nbsp;<a href="<c:url value="/accountController/front/resetpwd.dht"/>">修改密码</a> &nbsp;&nbsp;&nbsp;<font color="green">${msg}</font>
	               	  </td>
	                </tr>
	                <tr>
	                  <td width="100">邮箱：</td>
	                  <td>
							${SESSION_USER_ATTRIBUTE.email}
	               	  </td>
	                </tr>　　
	                <tr>
	                  <td>手机：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px; " type="text" name="mobile" id="mobile" datatype="m" value="${SESSION_USER_ATTRIBUTE.mobile }" autocomplete="off" />
	                  </td>
	                </tr>
	                <tr>
	                  <td>头像：</td>
	                  <td>
	                      <img src="${webRoot}/${SESSION_USER_ATTRIBUTE.photo}" width="60" height="60" />  
						  <input class="InputTxt2" id="file" value="default.img"  name="file" type="file"  autocomplete="off"/>
	                  </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td>
	                  	<input class="Button1" type="submit" name="submit" id="submit" value="确认"  />
	                  	<input class="Button1" type="button" onclick="backindexpage();" value="返回"  />
	                  	
	                  	</td>
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
	function backindexpage(){
		$("#subject_manage").show();
		window.location.href='${webRoot}/indexController/front/index.dht'
		}
    $().ready(function() {
    	$("#subject_manage").hide();
    	$("#setpwdForm").validate({
   			rules:{
    				mobile:{required:false,isMobile : true},
    				file:{required:false,img_1:true}
    		}
    	}
    	);
    });
</script>
</body>
</html>
