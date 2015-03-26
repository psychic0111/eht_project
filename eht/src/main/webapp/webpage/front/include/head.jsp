<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
 <link rel="stylesheet" type="text/css" href="${cssPath}/style.css" />
<!-- Begin header-->
<div class="header" style="width:100%;">
  <div class="left1"><a href="${webRoot}/indexController/front/index.dht"> <img  src="<%=imgPath %>/logo.png"  height="30" style="margin-top:6px;"/></a></div>
  
 <div class="left1">
 	
  </div> 
  <div class="right1">
		<div id="dd" class="wrapper-dropdown-5" tabindex="1" >
		    	<span onclick="sendmessages()" class="Button1" style="height:18px;padding:5px 5px;display:inline-block;">
		    		<img alt="" style="margin:0;" width="24" height="24" src="<%=imgPath %>/20140821042246434_easyicon_net_32.png">
		    		发送消息
		    	</span>
		    	<!-- <input class="Button1" type="button" name="sendmessage_btn" id="sendmessage_btn" style="margin-top:5px;" value="发送消息" onclick="sendmessages()"/> -->
  	        <img  src="<%=imgPath %>/Sline.png"  height="40" />
  		    <img src="${webRoot}/${SESSION_USER_ATTRIBUTE.photo}" width="22" height="22" onclick="window.location.href='${webRoot}/accountController/front/viewEditUser.dht'" onerror="loadDefaultPhoto(this)"/> 
  	        <a href="${webRoot}/accountController/front/viewEditUser.dht" title='${SESSION_USER_ATTRIBUTE.userName}'>
  	        	 <c:if test='${fn:length(SESSION_USER_ATTRIBUTE.userName)>20}'>${fn:substring(SESSION_USER_ATTRIBUTE.userName,0,19)}...</c:if>
  	        	 <c:if test='${fn:length(SESSION_USER_ATTRIBUTE.userName)<=20}'>${SESSION_USER_ATTRIBUTE.userName}</c:if>
  	        </a>
  	        <img id="qqimg" src="" width="0" height="0"/><span id="qqname"></span>
  	        <img src="<%=imgPath %>/Sline.png" height="40"/>
  	        <a href="#" onclick="logout();">注销</a>
		</div>
  </div>
  <div class="clear"></div>
</div>
<!-- QQ第三方登录JS导入 -->
<script type="text/javascript" src="http://qzonestyle.gtimg.cn/qzone/openapi/qc_loader.js" data-appid="101190840" data-redirecturi="http://idpaper.las.ac.cn" charset="utf-8"></script>
<!-- Sina第三方登录JS导入 -->
<script  type="text/javascript" src="http://tjs.sjs.sinajs.cn/open/api/js/wb.js?appkey=398741386&debug=true"  charset="utf-8"></script>
<script type="text/javascript">
<!--

if(QC.Login.check()){
	QC.api("get_user_info")
    .success(function(s){//成功回调  
     	//alert("获取用户信息成功！当前用户昵称为："+s.data.nickname);
    	//$("#qqimg").attr("src", "<%=imgPath %>/Connect_logo_1.png");
    	$("#qqimg").attr("src", s.data.figureurl_qq_1);
    	$("#qqimg").attr("width", 16);
    	$("#qqimg").attr("height", 17);
    	var nickname = s.data.nickname;
    	$("#qqname").text(nickname);
    })  
    .error(function(f){//失败回调  
    	alert("获取用户QQ信息失败！");  
    })  
    .complete(function(c){//完成请求回调  
    	//alert(c.stringifyData());
     //通过QQ OpenAPI提供的各种方法提取数据  
    });
}
function sendmessages(obj){
	var urllink='get:${webRoot}/messageController/front/sendMessag.dht';
    $.jBox(urllink, {
	    title: "发送消息",
	    width: 500,
	    height: 350,
	   	buttons: { '发送': 1, '关闭':0},
	   	submit: function (v, h, f) {
	            if (v == 1) {
	              	AT.postFrm("addsendMessageForm",function(data){
						if(data.success){ 
						   document.getElementById("addsendMessageForm").reset(); 
							MSG.alert('发送成功');
						}
					},true);
					return false;
	            } else{
	            	
	            }
	            return true;
        }
	});
    $("#jbox-content").css('overflow', 'hidden');
}	

function logout(){
	if(QC.Login.check()){
		QC.Login.signOut();
		//window.location = "${webRoot}/center/logout.dht";
	}
	
	if(WB2.checkLogin()){
		WB2.logout(function() {
			//window.location = "${webRoot}/center/logout.dht";
		});
	}
	window.location = "${webRoot}/center/logout.dht";
}
//-->
</script>
<!-- End header-->  
