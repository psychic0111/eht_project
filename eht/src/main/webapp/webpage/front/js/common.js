function sumlocation(path){
	window.location='${pageContext.request.contextPath}'+path;
	window.location=path;
} 
function ajaxCollbackDiv(path,content){ 
	$.ajax({ 
		async: false, 
		type:"POST",  
		url:path,   
		success:function(str){
			$("#"+content).html(str); 
	    } 
	});
}

function logout(){
	$.ligerDialog.confirm("您确定要退出?", function (yes){
	    if(yes){
			var path='${pageContext.request.contextPath}';
			var domain = '<%=request.getServerName()%>';
			var username=$.cookie("pn");
			var pwd=$.cookie("pw");
			if(path===""){
				path="/";
			}
			$.cookie("pn","",{expires:0,domain:domain,path:path});
			$.cookie("pw","",{expires:0,domain:domain,path:path});
			window.location='${pageContext.request.contextPath}/user/logout.do'; 
			//window.location.href=href;
			    }
	}); 
}
function ajaxFormCollbackDiv(path,formid,tempid){
		$.ajax({
			async: false, 
		    cache: true,
		    type: "POST",
		    url:path,
		    data:$('#'+formid).serialize(),// 你的formid
		    async: false,
		    error: function(request) {
		        alert("请求失败");
		    },
		    success: function(data) {
		        $("#"+tempid).html(data);
		    }
		});
}