<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
 <link rel="stylesheet" type="text/css" href="${cssPath}/style.css" /> 
<!-- Begin header-->
<div class="header" style="width:100%;">
  <div class="left1"><a href="${webRoot}/indexController/front/index.dht"> <img  src="<%=imgPath %>/logo.png"  height="40" /></a></div>
  
 <div class="left1">
 	
  </div> 
  <div class="right1">
		<div id="dd" class="wrapper-dropdown-5" tabindex="1" >
		    <input class="Button1" type="button" name="sendmessage_btn" id="sendmessage_btn" style="margin-top:5px;" value="发送消息" onclick="sendmessages()"/>
  	        <img  src="<%=imgPath %>/Sline.png"  height="40" onclick="window.location.href='${webRoot}/accountController/front/viewEditUser.dht'" />
  		    <img src="${webRoot}/${SESSION_USER_ATTRIBUTE.photo}" width="22" height="22" onclick="window.location.href='${webRoot}/accountController/front/viewEditUser.dht'" onerror="loadDefaultPhoto(this)"/> 
  	        <a href="${webRoot}/accountController/front/viewEditUser.dht" title='${SESSION_USER_ATTRIBUTE.userName}'>
  	        	 <c:if test='${fn:length(SESSION_USER_ATTRIBUTE.userName)>20}'>${fn:substring(SESSION_USER_ATTRIBUTE.userName,0,19)}...</c:if>
  	        	 <c:if test='${fn:length(SESSION_USER_ATTRIBUTE.userName)<=20}'>${SESSION_USER_ATTRIBUTE.userName}</c:if>
  	        	 <img src="<%=imgPath %>/Sline.png"  height="40" onclick="window.location.href='${webRoot}/accountController/front/viewEditUser.dht'" /></a>
  	        <a href="${webRoot}/center/logout.dht">注销</a>
		</div>
  </div>
  <div class="clear"></div>
</div>
<!-- End header-->  
