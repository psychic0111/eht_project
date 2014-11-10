

var url = webRoot+ "/noteController/front/getGroupsEmail.dht";

//分享条目 
function clickLi(currTrIndex){
         var prevTrIndex = $("#prevTrIndex").val();    
         if (currTrIndex > -1){    
                $("#li_" + currTrIndex).addClass("over");    
         }    
         $("#li_" + prevTrIndex).removeClass("over");    
         $("#prevTrIndex").val(currTrIndex); 
   } 
   $(document).click(function(){  
         $("#flttishi").hide();  
	     $("#emailMsgDiv").hide();
});  
function sendMsg(msg){
		   if(msg=="true"){
			   window.setTimeout(function () { $.jBox.tip('发送成功!', 'success'); }, 2000);
		   }		 
		   if(msg=="false"){
			   window.setTimeout(function () { $.jBox.tip('发送失败!。', 'success'); }, 2000);
		   }
}  
function shareNote() {
	var note_id = $("#noteForm_id").val();
	if (note_id != null && note_id == '') {
		MSG.alert("请选择条目!");
		return false;
	}
	var htm = '<table><tr><td style="border-right:1px solid rgb(213,213,213)"><div style="height:300px;">'
			+ '<form target="emailIframe"  action="'+webRoot+'/noteController/front/sendShareEmail.dht" method="post" onsubmit="return checkEmailShareForm();">'
			+ '<input type="hidden" value="'
			+ note_id
			+ '" name="noteid" />'
			+ '<input  type="text" id="emailField" class="InputTxt3"  value=""  style=" width:70%; "  title="请输入单个邮件地址,按【回车键】或【添加】按钮添加至【邮件发送预备区】！"  autocomplete="off"/>'
			+ '<input class="Button4" type="button" id="addEmailMessage" value="添加" title="将文本框中的邮件添加至【邮件发送预备区】！" />'
			+ '<table><tr><td valign="top"><div style="height:225px;overflow:auto;"   title="【邮件发送预备区】">'
			+ '<span  id="tempdiv" ></span>'
			+ '</div><div style="width:360px;align:center;margin-top:5px"><input title="发送【邮件发送预备区】的邮件。" class="Button4" type="submit"  value="发送" /></div></td></tr></table>' 
			+ '</form>' + '</div></td><td valign="top"><span>'
			+'<input  type="text" id="searchEmail" name="searchEmail" class="InputTxt1" style="color: rgb(160, 160, 160);width:78%" title="根据邮箱或用户名查询跟您有关联的人(即跟您有共同专题的人的邮件)！" /></span>'
			+'<div id="groupEmails" style="width:146px;height:265px;align:center;margin-top:5px;overflow:auto;"></div></td></tr></table>'
			+ '<iframe style="display:none" name="emailIframe" src="'
			+ frontPath + '/note/emailStatus.jsp"></iframe>';

	var params = {
		'searchField' :""
	};
	//初始化右侧邮件列表
	getShareEmailToList(url, params);
	easyDialog.open({
		container : {
			header : '<img src="' + imgPath + '/email_go.png" height="20"/>',
			content : htm
		},
		lock : true,
		follow : 'note_share',
		followX : -80,
		followY : 34
	});

	//绑定右侧邮件列表查询窗口
	$("#searchEmail").keyup(
					function(evt) {
						if (evt.keyCode == 38 || evt.keyCode == 40
								|| evt.keyCode == 37 || evt.keyCode == 39) {
							return;
						} 
						var params = {
							'searchField' : $("#searchEmail").val()
						};
						getShareEmailToList(url, params); 
					}).keydown(function(evt) {
				if (evt.keyCode == 13) {
					return false;
				}
			});
	
	//绑定输入窗口事件
	$("#emailField").keyup(
					function(evt) {
						if (evt.keyCode == 38 || evt.keyCode == 40
								|| evt.keyCode == 37 || evt.keyCode == 39) {
							return;
						}
						//回车添加当前输入框的值
						if (evt.keyCode == 13) {
							addEmailToDiv();
						}
						var params = {
							'searchField' : $("#emailField").val()
						};
						getShareEmail(url, params);
						//绑定鼠标移动效果 
						if ($("#flttishi").html() != '') {
							$("#flttishi").show();
							var height = $(this).height();//按钮的高度
							var top = $(this).offset().top;//按钮的位置高度
							var left = $(this).offset().left;//按钮的位置左边距离
							//设置div的top left
							$("#emailMsgDiv").css("left", left);
							$("#emailMsgDiv").css("top", height + top + 3);
							$("#emailMsgDiv").show();
						}
						bindMouseOverLi();
					}).keydown(function(evt) {
				if (evt.keyCode == 13) {
					return false;
				}
			});
	//绑定‘添加’按钮
	$("#addEmailMessage").bind("click",function(){
		addEmailToDiv();
	});
} 
//添加Email地址到email队列
function addEmailToDiv(){
	if (checkEmail($("#emailField"))) {
		if(addEmailAreaHandle()){
			$("#emailField").val("");
		}
	}
}
function cancelEmail(o) {
	$(o).parent().remove();
}
//jquery验证邮箱
function checkEmail(email) {
	if ($(email).val() == null || $(email).val() == "") {
		$.jBox.tip('您输入的邮箱地址不能为空！'); 
		return false;
	}
	if (!$(email).val().match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/)) {
		$.jBox.tip('您输入的邮箱地址格式不正确'); 
		$(email).focus();
		return false;
	} 
	return true;
}
//输入提示下拉框的提示邮件地址鼠标点击事件
function bindMouseOverLi() {
	$("#prevTrIndex").val("-1");//默认-1     
	$("#flttishi li")
			.mouseover(function() {//鼠标滑过    
				$(this).addClass("over");
			})
			.mouseout(function() { //鼠标滑出    
				$(this).removeClass("over");
			})
			.each(function(i) { //初始化 id 和 index 属性    
				$(this).attr("id", "li_" + i).attr("index", i);
			}).click(function() { //鼠标单击（添加当前选择的目标）
						$("#emailMsgDiv").hide();
						//添加当前选择的邮件至发送预备区
						if(!addEmailArea(this)){
							$("#emailField").val("");
						};
					});
		clickLi(0);
} 


//添加当前选择的邮件至发送预备区【手动录入】
function addEmailAreaHandle(){
	var email = $("#emailField").val();
	//检查邮件区是否已经存在
	var t = $("#tempdiv").find("input[name='shareEmails'][value='"+email+"']").val(); 
	if(t!=null){
		$.jBox.tip('这个邮件地址已添加,您不能重复添加！'); 
		return false;
	} 
	var addtemp = "<span><br><input type='hidden' name='shareEmails' value='"
			+ email
			+ "' > "
			+ email
			+ " <img src='"+imgPath+"/34aL_046.png' onclick='cancelEmail(this)' > </span>";
	$("#tempdiv").prepend(addtemp);
	return true;
}
//添加当前选择的邮件至发送预备区【   联想提示 点击事件 】
function addEmailArea(obj){
	var cutUserName = $(obj).find("input[name='shareName']").val();
	var email = $(obj).find("input[name='shareEmails']").val();
	
	//检查邮件区是否已经存在
	var t = $("#tempdiv").find("input[name='shareEmails'][value='"+email+"']").val(); 
	if(t!=null){
		$.jBox.tip('这个邮件地址已添加,您不能重复添加！'); 
		return false;
	}
	if(cutUserName!=null&&cutUserName.length>16){
		cutUserName =  cutUserName.substring(0,15)+"...";
	}
	var addtemp = "<span title='["+$(obj).find("input[name='shareName']").val()+"]"+email+"'><br>"
			+"<input type='hidden' name='shareEmails' value='"+ email+ "' />"
			+"["+cutUserName+"]<font color='#DADADA'>"+ email+ "</font>"
			+"<img src='"+imgPath+"/34aL_046.png' onclick='cancelEmail(this)' > </span>";
	$("#tempdiv").prepend(addtemp);
}
//获取共享邮件
function getShareEmail(url, params) {
	$.ajax({
				type : "POST",
				url : url,
				data : params,
				dataType : "json",
				success : function(result) {
					//新元素重新绑定
					if (result.data == null || result.data.length <= 0) {
						$("#flttishi").hide();
						$("#flttishi").html("");
						;
						$("#emailMsgDiv").hide();
					} else {
						//var jsonData = eval(result);//接收到的数据转化为JQuery对象，由JQuery为我们处理  
						var str = "";
						var groupTemp = "";
						$.each(result.data,function(index, objVal) {//遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。
							 var userName = "";
			  	        	 if(objVal.name>16){
			  	        		userName = objVal.name.substring(0,15)+"...";
			  	        	 }else{
			  	        		userName = objVal.name;
			  	        	 }
							//联想提示
							var temp = "<li><input type='hidden' name='shareEmails' value='"
									+ objVal.email
									+ "'> <input type='hidden' name='shareName' value='"
									+ userName
									+ "'> "
									+ "["
									+ userName
									+ "]"
									+ objVal.email + "</li>";
							str += temp; 
						});
						$("#flttishi").html(str);
						bindMouseOverLi();
					}
				}
			});
}


//获取共享邮件
function getShareEmailToList(url, params) {
	$.ajax({
				type : "POST",
				url : url,
				data : params,
				dataType : "json",
				success : function(result) {
					//新元素重新绑定
					if (result.data == null || result.data.length <= 0) {
						$("#groupEmails").html(""); 
					} else {
						var groupTemp = "";
						$.each(result.data,function(index, objVal) {//遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。 
							 var userName = "";
			  	        	 if(objVal.name>16){
			  	        		userName = objVal.name.substring(0,15)+"...";
			  	        	 }else{
			  	        		userName = objVal.name;
			  	        	 }	
							//邮件列表
								groupTemp += "<span title='["+objVal.name+"]"+objVal.email+"'><br><a href='javascript:;'><input type='hidden' name='shareEmails' value='"+objVal.email+"'>"
										 +"<input type='hidden' name='shareName' value='"+objVal.name+"'>"+userName+ "</a></span>";
							});
						$("#groupEmails").html(groupTemp);
						//绑定鼠标点击邮箱添加到邮件发送区
						$("#groupEmails span").click(function(){
							addEmailArea(this);
						});
					}
				}
			});
}
function clickLi(currTrIndex){
    var prevTrIndex = $("#prevTrIndex").val();    
    if (currTrIndex > -1){    
           $("#li_" + currTrIndex).addClass("over");    
    }    
    $("#li_" + prevTrIndex).removeClass("over");    
    $("#prevTrIndex").val(currTrIndex); 
} 

//表单提交前验证
function checkEmailShareForm(){
	
	if ($("#tempdiv").html() == null || $("#tempdiv").html() == "") {
		$.jBox.tip('请添加邮件！'); 
		return false;
	}
	if ($("#emailField").val() == null || $("#emailField").val() == "") {
		$.jBox.tip("请稍后...！", 'loading');
		return true;
	}
	if (!$("#emailField").val().match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/)) {
		$.jBox.tip('请填写正确邮箱地址！'); 
		$("#emailField").focus();
		return false;
	}else{
		if(addEmailAreaHandle()){
			$("#emailField").val("");
		}
		/*var addtemp = "<span><br><input type='hidden' name='shareEmails' value='"
			+ $("#emailField").val()
			+ "' > "
			+ $("#emailField").val()
			+ " <img src='"+imgPath+"/34aL_046.png' onclick='cancelEmail(this)' > </span>";
			$("#tempdiv").prepend(addtemp);*/
	}
	$.jBox.tip("请稍后...！", 'loading');
	return true;
} 
//===========================条目共享   end=========================