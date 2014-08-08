<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>设置密码</title>
<%@ include file="/webpage/front/include/front_common.jsp" %>  
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
            <div class="title">设置密码</div>
            <div class="Table">
            <form id="setpwdForm" action="<c:url value="/center/registerSave.dht"/>"  enctype="multipart/form-data" name="setpwdForm" method="post" >
            	<input type="hidden" name="id" value="<%=request.getParameter("id")%>"/>
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">密码：</td>
	                  <td>
	                   <input class="InputTxt2"  style=" width:40%; height:28px; " id="password" name="password" type="password" autocomplete="off" >
	               	  </td>
	                </tr>
	                <tr>
	                  <td>确认密码：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px; " id="passwordConf" name="passwordConf"  recheck="password" type="password" autocomplete="off"  />
	                  </td>
	                </tr>
	                <tr>
	                  <td>手机：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px; " type="text" name="mobile" id="mobile"  autocomplete="off"/>
	                  </td>
	                </tr>
	                <tr>
	                  <td>头像：</td>
	                  <td>
						  <input class="" id="file" name="file" type="file"  autocomplete="off" />
	                  </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><input class="Button1" type="submit" name="button" id="button" value="确认" /></td>
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
	$("#setpwdForm").validate({
 	   rules:{
 		  	password:{required:true,maxlength:20},
			passwordConf:{required:true,maxlength:20,equalTo:'#password'},
			mobile:{required:false,isMobile : true},
			file:{required:false,img_1:true}
		},
		messages:{
			passwordConf:{equalTo:'* 确认密码要和密码一致!'},
		}
	}
	);
});  
</script>
</body>
</html>
