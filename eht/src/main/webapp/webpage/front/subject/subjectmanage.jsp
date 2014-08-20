<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ include file="/webpage/front/include/front_common.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<script type="text/javascript">
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
		url = "${webRoot}/subjectController/front/leadingSuject.dht";
		AT.load("iframepage",url,function() {});		
		}
		
if(actionSchedule){

}else{
 actionSchedule=setInterval(
 function(){
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
    <div class="right_top">
       <div class="Nav" id="nav_div" style="padding-top:8px;">
			专题列表
			<input style="width:100px;height:23px;margin-left:10px;" id="subject_import" onclick="toLeadingSuject()" class="Button2" type="button" value="导入专题" name="subject_import"/>        
       </div>
    </div>
         <div class="right_index" >
	<!-- Begin Subjects-->
	<div class="Subjects">
		<ul>
			<c:forEach items="${subjectList}" var="sub">
			<li>
				<div class="img" style="cursor:pointer;">
					<img src="${imgPath}/temp6.jpg" width="100%" <xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>"> onclick="toEditSubject('${sub.id}')" </xd:hasPermission> />
				</div>
				<div class="title" style="cursor:pointer;" title="${sub.subjectName }"  <xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>"> onclick="toEditSubject('${sub.id}')" </xd:hasPermission>   >
				<c:choose>
					<c:when test="${fn:length(sub.subjectName) > 10}">
						<c:out value="${fn:substring(sub.subjectName, 0, 10)}......" />
					</c:when>
					<c:otherwise>
						<c:out value="${sub.subjectName}" />
					</c:otherwise>
				</c:choose>
				</div>
				<div id="${sub.id}_schedule" style="height:5px;text-align:center" class="schedulesubjects" > 
				
				</div>
				<div class="others" style="height:22px">
					<xd:hasPermission  resource="SubjectManage" subjectId="${sub.id}" action="<%=ActionName.ASSIGN_MEMBER %>">
					<input class="Button4 scheduleExportSubject" type="button" name="button"  style="width:80px;"
						value="导出专题"  onclick="toExportSubject('${sub.id}')"/> 
					<c:if test="${sub.subjectType eq 2}">
						<input class="Button1" type="button" style="width:80px;"  name="button" id="button" value="成员管理" onclick="toMemberManage('${sub.id}')" />
					</c:if>
					</xd:hasPermission>	
				</div>
			</li>
			</c:forEach>
			<li class="Add" onclick="toAddSubject()">
				<div class="Add_type" style="cursor:pointer;">
					<table width="100%" border="0" cellspacing="0" cellpadding="0" >
						<tr>
							<td><img src="${imgPath}/add.png" width="15" height="15" /></td>
						</tr>
					</table>
				</div>
			</li>
		</ul>
	</div>
	<!-- End Subjects-->
</div>

