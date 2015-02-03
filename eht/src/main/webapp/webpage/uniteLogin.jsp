<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<body > 
<!-- Begin mainer-->
<div class="mainer">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td valign="top" class="mainer_right">
        <!-- Begin mainer_index-->
        <div class="right_index"> 
          <!-- Begin Information-->
          <div class="Information">
            <div class="Table" id="bind_user">
            <form id="bind_loginForm" name="bind_loginForm" action="/center/login.dht" method="post">
					<!-- 第三方id （id）-->
		     		<input type="hidden" name="openId" value="${openId}"/>
		       		<!-- 第三方user(账号) -->
		       		<input type="hidden" name="openUser" value="${openUser}"/>
		       		<!-- 第三方类型（qq,sina.....) -->
		       		<input type="hidden" name="type" value="${type}"/>
	              <table width="100%" border="0" cellspacing="0" cellpadding="0">
	                <tr>
	                  <td width="80">用户名：</td>
	                  <td>
	                    <input class="InputTxt2" id="username"  style="width:150px; height:28px;" type="text" name="username" />
	                    </td>
	                </tr> 
	                <tr>
	                  <td width="80">密码：</td>
	                  <td>
	                    <input class="InputTxt2" id="password"  style="width:150px; height:28px;" type="password" name="password" />
	                    </td>
	                </tr> 
	                <tr>
	                  <td>&nbsp;</td>
	                  <td><input class="Button1" type="submit" name="button" id="button" value="绑定" /></td>
	                </tr>
	              </table>
              </form>
            </div>
            
          </div>
          <!-- End Information--> 
          
        </div>
        <!-- End mainer_index-->
        </td>
    </tr>
  </table>
</div>
<!-- End mainer--> 

<script type="text/javascript">  
	$().ready(function() {
		$("#bind_loginForm").validate({
	 	   rules:{
				username:{required:true,maxlength:20},
				password:{required:true,maxlength:20}
			},
			messages:{
				username:{required:'*账号不能为空！'},
				password:{required:'*密码不能为空！'}
			}
		});

	}); 
	function getVerifiCode(){
        document.getElementById("verifi_code").src ="${webRoot}/getVerifiCode.dht?r=" + new Date().getTime();
    }   
</script>
</body>
</html>
