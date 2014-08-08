<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%> 
<%@ page import="java.util.*" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<%
	String uploadifyPath = basePath + "/webpage/front/js/uploadify";
	String sessionId = pageContext.getSession().getId();
%>

<c:set var="uploadifyPath" value="<%=uploadifyPath%>" />
<c:set var="sessionId" value="<%=sessionId%>" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>专题管理</title>
<!-- 附件上传 -->
<link rel="stylesheet" type="text/css" href="<%=frontPath %>/js/uploadify3/uploadify.css"/>
<script type="text/javascript" src="<%=frontPath %>/js/uploadify3/jquery.uploadify.js"></script>
<!-- 附件上传 -->
<script type="text/javascript"> 
window.UEDITOR_HOME_URL = "${frontPath}/js/ueditor/";
window.UEDITOR_IMG_URL = "${webRoot}";
//========================上传附件 start======================= 

	 var downloadPath = "${webRoot}/noteController/front/downloadNodeAttach.dht";
	 var upLoadPath ="${webRoot}/noteController/front/uploadNodeAttach.dht";
	 var initPath ="${webRoot}/subjectController/front/initDirAtta.dht";
	 var checkPath ="${webRoot}/subjectController/front/checkPathAtta.dht";
	 var basePath = "${uploadifyPath}";
	 $(document).ready(function() {
			$(function() {
				$("#attachment").uploadify({
					height        : 20,
					swf           : '<%=frontPath %>/js/uploadify3/uploadify.swf',
					width         : 50,
					buttonText    : '上传文件',
					uploader      : upLoadPath,
					queueSizeLimit: 10,
					formData	  :{
										'jsessionid':'<%=sessionId%>',
										'dirId':'${dirId}',
										'noteid':''
									},
					onSelect:function(file){
						var disableType=new Array();
						disableType["exe"]=true;
						disableType["com"]=true;
						disableType["bat"]=true;
						disableType["sh"]=true;
						var fileName=file.name;
						fileName=fileName.toLocaleLowerCase();
						var terms=fileName.split("\.");
						var type="";
						if(fileName.indexOf(".")>-1){
							var terms=fileName.split("\.");
							type=terms[terms.length-1];
						}
						type=$.trim(type);
						if(disableType[type]){
							alert("您不能上传后缀为.exe .com .bat .sh的文件！");
							$("#attachment").uploadify("cancel",file.id);			
						}
					},
					onUploadComplete:function(file){
						$("#attachment").uploadify("cancel",file.id);	
						var url="${webRoot}/subjectController/front/dirAttaManage.dht?subjectId=${subjectId}&dirId=${dirId}&ispage=true";
						AT.get(url,function(data){
							$("#datadiv").html(data);
						});
					}
				});
			});
		});
	 
	/**上传完之后刷新列表页面**/
	function refresh_wdzl(){
		var url="${webRoot}/subjectController/front/dirAttaManage.dht?subjectId=${subjectId}&dirId=${dirId}&ispage=true";
		AT.get(url,function(data){
			$("#datadiv").html(data);
		});
	}
//========================上传附件 end=======================  
</script>
</head>
<body>

    <div class="right_top">
       <div class="Nav" id="nav_div" style="position:absolute;">
			<div id="attachment"></div>     
       </div>
    </div>
    <div class="right_index" >
		<div class="Subjects">
          <div class="comments">
            <div id="datadiv"class="comments_list">
			<jsp:include page="attachmentList.jsp"/>
			</div>
			</div>
		</div>
	</div>

</body>
</html>
