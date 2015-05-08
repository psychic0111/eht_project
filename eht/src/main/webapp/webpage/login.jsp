<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ include file="/webpage/front/include/front_common.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta property="wb:webmaster" content="d35f84c34aa93c58" />
<meta property="qc:admins" content="2447516343105264136136367" />

<!-- QQ第三方登录JS导入 -->
<script type="text/javascript" src="http://qzonestyle.gtimg.cn/qzone/openapi/qc_loader.js" data-appid="101190840" data-redirecturi="http://idpaper.las.ac.cn" charset="utf-8"></script>
<!-- Sina第三方登录JS导入 -->
<script  type="text/javascript" src="http://tjs.sjs.sinajs.cn/open/api/js/wb.js?appkey=398741386"  charset="utf-8"></script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/login.js"></script>
<script type="text/javascript">
if(top != window){
	top.location.href = window.location.href;
}

</script>

<title>DPaper</title>
</head>
<body>
<!-- Begin header-->
<div class="header">
  <div class="left1"><img src="${imgPath}/logo.png"  height="30" style="margin-top:6px;"/></div>
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
            <form id="loginForm" name="loginForm" action="<%=AppRequstUtiles.getAppUrl() %>/center/login.dht" method="post">
	               <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="100">用户名：</td>
	                  <td>
	                  	<input class="InputTxt2"  style=" width:40%; height:28px;line-height:28px; " type="text" name="username" id="username" autocomplete="off"/><c:if test="${sendmail eq '1'}"><input class="Button1" type="button" onclick="repeat('${username}');" value="账号未激活,重发邮件？" style="display:inline" /></c:if>
                  	  </td>
	                </tr>
	                <tr>
	                  <td>密码：</td>
	                  <td>
	                  	<input class="InputTxt2" style=" width:40%; height:28px;line-height:28px; " type="password" name="password" id="textfield2" autocomplete="off"/>
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
	                  	 <span id="wb_connect_btn"></span>
	                  	 <span id="qq_login_btn"></span>
           			  </td>
	                </tr>
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><a href="${webpagePath}/register.jsp" class="link1b">立即注册e划通账户</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="${webpagePath}/user/retrievepassword.jsp" class="link1b">忘记密码？</a></td>
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
  <div id="test_div">
  </div>
</div>
<!-- End mainer--> 
<!-- Begin footer-->
<div class="footer">
  <div class="left">&copy; Copyright <a href="#" class="link3">idpaper.las.ac.cn</a></div>
  <div class="right">Powered by CNOOC-VS</div>
  <div class="clear"></div>
</div>
<!-- End footer-->

<script type="text/javascript" charset="utf-8"> 
function repeat(obj){
   var params = {'username':obj};
   AT.post("${webRoot}/center/repeatajax.dht",params,function(data){
				if(data.success){
				MSG.alert("请查看邮件并激活账号！");
				}else{
				MSG.alert("邮件发送失败！");
				}
			},true);
}
$().ready(function() {
	$("#username").focus();
	$("#loginForm").validate({
 	   rules:{
			username:{required:true,chrnum:true,maxlength:200},
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

var toLogin = true;
if('${logout}' == 'true'){
	 toLogin = false;
}
//document.getElementByIdx_x("qq_login_btn").innerHTML = document.getElementByIdx_x("qq_login_btn").getAttribute("_origText");
var cbLoginFun = function(o, oOpts){
	if(QC.Login.check()){
		self.window.location = "<%=AppRequstUtiles.getAppUrl()%>";
	}
};
QC.Login(
	{btnId:"qq_login_btn"} //插入按钮的节点id
	//cbLoginFun
);
//login("123", "uuuui", "QQ");
if(QC.Login.check()){//如果已登录
	QC.Login.getMe(function(openId, accessToken){
	   //alert(["当前登录用户的", "openId为："+openId, "accessToken为："+accessToken].join("n"));
		if(toLogin){
	   		login(openId, accessToken, '<%=Constants.OPEN_LOGIN_QQ%>');
		}else{
	  		toLogin = true;
	  	}
	});
	//这里可以调用自己的保存接口
	//...
}else{
	//jBox.tip("QQ登录未成功！", "info", {timeout:3000, top:"1px"});
}
//login("222", "uuul", 'QQ');
//新浪登录
 WB2.anyWhere(function(W){
	W.widget.connectButton({
	 id: "wb_connect_btn",
	 callback : {
	  login:function(o){
	  //登录成功之后执行
	  	if(toLogin){
	  		login(o.idstr, o.name, '<%=Constants.OPEN_LOGIN_SINA%>');
	  	}else{
	  		toLogin = true;
	  	}
	  	<%-- var url = "${webRoot}/center/openLogin.dht";
	  	var params = {"openId": o.idstr, "type": "<%=Constants.OPEN_LOGIN_SINA%>", "openUser":o.name};
	  	AT.post(url, params, function(data){
	  		alert(123);
	  	}); --%>
	  },
	  logout:function(){
	   //退出之后执行
	  }
	 }
	});
});
</script>
</body>
</html>

