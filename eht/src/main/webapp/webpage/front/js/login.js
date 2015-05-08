
function login(openId, openUser, openType){
	var url = webRoot + "/center/openLogin.dht";
	var params = {"openId": openId, "type": openType, "openUser": openUser};
	AT.post(url, params, function(data){
		if($("#bind_loginForm", data).attr('action') != 'undefined' && typeof($("#bind_loginForm", data).attr('action')) != 'undefined'){
			var html1 = "<div style='padding:10px;'><input type='radio' checked='checked' id='radio1' name='bindOpen' value='1'/>绑定我的Dpaper帐号<br/><input type='radio' id='radio2' name='bindOpen' value='0' style='margin-top:10px;'/>还没有Dpaper帐号，先去注册一个</div>";
			var content = {
				    state1: {
				        content: html1,
				        buttons: { '确定': 1, '取消': 0 },
				        buttonsFocus: 0,
				        submit: function (v, h, f) {
				            if (v == 0) {
				                return true; // close the window
				            }else {
				            	var bindOpen = $("input[name='bindOpen']:checked").val();
				            	if(bindOpen == 1){
				            			
				            		$.jBox.nextState();
				            	}else{
				            		window.location = webRoot + "/webpage/register.jsp?openId=" + openId + "&type=" + openType + "&openUser=" + openUser;
				            	}
				            }
				            return false;
				        }
				    },
				    state2: {
				        content: data,
				        buttons: {},
				        buttonsFocus: 0
				    }
			};
			var title = '欢迎使用' + openType + '登录, 请绑定您的DPaper帐号';
			$.jBox(content);
		}else{
			window.location = webRoot + "/indexController/front/index.dht";
		}
		//$("#test_div").html(data);
	});
}