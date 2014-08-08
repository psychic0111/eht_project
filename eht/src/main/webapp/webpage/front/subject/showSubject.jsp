<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">
function toExportWordSubject(obj){
	window.location.href= "${webRoot}/subjectController/front/exportSujectWord.dht?id="+obj;
		//AT.load("iframepage",url,iframeHeight);	
	}
</script>
<div class="right_top">
     <div class="Nav" id="nav_div" style="padding-top:8px;">
     	专题管理
     	<input style="width:100px;height:23px;margin-left:10px;" id="subject_manage" onclick="toExportWordSubject('${subjectEntity.id}')" class="Button2" type="button" value="导出报告" name="subject_manage"/>
     </div>
</div>
 <div class="right_index"> 
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
