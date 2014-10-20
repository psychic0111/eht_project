<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
	String webpagePath = basePath +"/webpage";
	String uploadifyPath = basePath + "/webpage/front/js/uploadify";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />
<script type="text/javascript">
	//关闭分享、黑名单窗口
	if(!!$("#easyDialogWrapper").attr("id")){
		$("#easyDialogWrapper").remove();
	}

	function toEditSubject(obj){
		url = "${webRoot}/subjectController/front/viewEditSubject.dht?id="+obj;
		AT.load("iframepage",url,function() {});	
	}

	function toMemberManage(obj){
		url = "${webRoot}/subjectController/front/memberManage.dht?id="+obj;
		AT.load("iframepage",url,function() {});	
	}
	
	function toExportSubject(obj){
	var urllink='get:${webRoot}/subjectController/front/treeSuject.dht?subjectId='+obj;
    $.jBox(urllink, {
    title: "导出专题",
    width: 500,
    height: 350,
   buttons: { '导出': 1, '关闭': 0 },
   submit: function (v, h, f) {
            if (v == 1) {
            var zTree = $.fn.zTree.getZTreeObj("treeSubject");
           var nodes= zTree.getCheckedNodes(true);
            var checkCount = nodes.length;
            if(checkCount==0){
            	 MSG.alert("请选择导出的目录");
          		 return false; 
            }else{
            var subjectId="";
            var dirsId="";
              for(var i=0;i<nodes.length;i++){
             	 if(i==0){
              		subjectId=nodes[i].id;
             	 }else{
             	   dirsId=dirsId+nodes[i].id;
             	   if(i!=nodes.length-1){
             	   	   dirsId=dirsId+",";
             	   }
             	 }
              }
               var params = {"subjectId":subjectId,"dirsId":dirsId};
				 AT.post("${webRoot}/subjectController/front/exportSuject.dht",params,function(data){
				               if(data.success){
				                MSG.alert("等待专题导出完毕后才能继续导出");
				               }
						},false);
                 }
            return true; 
            } 
            return true;
        }
	});
	}

	function toLeadingSuject(){
		$("#subject_import").attr("class", "Button3");
		$("#subject_m").attr("class", "Button4");
		$("#subject_p").attr("class", "Button4");
		
		url = "${webRoot}/subjectController/front/leadingSuject.dht";
		AT.load("subject_list",url,function() {});		
	}


</script>
    <div class="right_top">
       <div class="Nav" id="nav_div" style="padding-top:8px;">
			专题管理
			<input style="width:100px;height:23px;margin-left:10px;" id="subject_p" onclick="toSujectList(1)" class="Button4" type="button" value="个人专题" name="subject_p"/>
			<input style="width:100px;height:23px;margin-left:1px;" id="subject_m" onclick="toSujectList(2)" class="Button4" type="button" value="多人专题" name="subject_m"/>
			<input style="width:100px;height:23px;margin-left:1px;" id="subject_import" onclick="toLeadingSuject()" class="Button4" type="button" value="导入专题" name="subject_import"/>        
       </div>
    </div>
    <div class="right_index" id="subject_list">
		<jsp:include page="subjectlist.jsp"></jsp:include>
	</div>
<script type="text/javascript">
$(document).ready(function(){
	var type = '${subjectType}';
	if(type == 1){
		$("#subject_p").attr("class", "Button3");
	}else if(type == 2){
		$("#subject_m").attr("class", "Button3");
	}else{
		$("#subject_import").attr("class", "Button3");
	}
});

function toSujectList(subjectType){
	if(subjectType == 1){
		$("#subject_p").attr("class", "Button3");
		$("#subject_m").attr("class", "Button4");
		$("#subject_import").attr("class", "Button4");
	}else if(subjectType == 2){
		$("#subject_m").attr("class", "Button3");
		$("#subject_p").attr("class", "Button4");
		$("#subject_import").attr("class", "Button4");
	}
	var url = webRoot+"/subjectController/front/subjectList.dht?pageNo=1&pageSize=20&subjectType=" + subjectType;
	AT.load("subject_list", url, function(){});
}

if(actionSchedule!=null){

}else{
	actionSchedule=setInterval(
		function(){
		if(!document.getElementById("schedule_1409022827875")){
			clearInterval(actionSchedule);
			actionSchedule=null;
			return;
		}
		 AT.post("${webRoot}/subjectController/front/sujectSchedule.dht",null,function(data){
				if(data.success){
				   var c='专题正在导出('+data.attributes.cout+'/'+data.attributes.couts+')';
				   $("#"+data.attributes.subjectId + "_schedule").text(c);
				}else{
				   $(".schedulesubjects").text("");
				}
			},true);
	},10000);

}		
</script>
