<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">
function toExportMhtSubject(obj){
	window.location.href= "${webRoot}/subjectController/front/exportSujectmht.dht?id="+obj;
		//AT.load("iframepage",url,iframeHeight);	
}

function toExportWordSubject(obj){
	window.location.href= "${webRoot}/subjectController/front/exportSujectWord.dht?id="+obj;
		//AT.load("iframepage",url,iframeHeight);	
}

$(document).ready(function(){
	var h = document.documentElement.clientHeight - 115;
	$("#reportDiv").height(h);
});

</script>
<div class="right_top">
     <div class="Nav" id="nav_div" style="padding-top:8px;">
     	专题管理
     	<input style="width:100px;height:23px;margin-left:10px;" id="subject_manage" onclick="toExportMhtSubject('${subjectEntity.id}')" class="Button2" type="button" value="导出mht报告" name="subject_manage"/>
     	<input style="width:100px;height:23px;margin-left:10px;" id="subject_word" onclick="toExportWordSubject('${subjectEntity.id}')" class="Button2" type="button" value="导出word报告" name="subject_word"/>
     </div>
</div>
 <div class="right_index" id="reportDiv" style="overflow:auto;"> 
      <!-- Begin Information-->
      <div class="Information">
        <div class="title">${subjectEntity.subjectName}专题报告</div>
        <div class="Table">
          <table width="100%" border="0" cellspacing="0" cellpadding="0">
           ${sb}
          </table>
        </div>
      </div>
      <!-- End Information--> 
 </div>
