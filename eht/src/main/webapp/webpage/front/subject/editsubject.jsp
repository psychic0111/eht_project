<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">
$().ready(function() {
	$("#editSubjectForm").validate({
			rules:{
				subjectName:{required:true,minlength:2,maxlength:40,remote:{url:'${webRoot}/subjectController/front/checkSubjectName.dht?id=${subject.id}'}}
	,description:{maxlength:200}
	       },
			messages:{
				subjectName:{remote:'专题名称已被使用'}
			}
		}
	);
	
});

function editSubject(){
if($('#subjectName').val()=='个人专题'||$('#subjectName').val()=='多人专题'||$('#subjectName').val()=='消息中心'){
		MSG.alert('专题名称不能以个人专题,多人专题,消息中心命名', '提示信息');
		return false;
		}
	AT.postFrm("editSubjectForm",function(data){
		if(data.success){
				MSG.alert(data.msg);
				if(data.attributes.subjectType==1){
					buildMainMenu(0,data.attributes.subjectId,false);
				}else{
					buildMainMenu(1,data.attributes.subjectId,false);
				}
				AT.load("iframepage","${webRoot}/subjectController/front/subjectManage.dht?subjectType=" + data.attributes.subjectType,function() {});	
			}else{
				MSG.alert(data.msg);
				}
	},true);
}

function toShowSubject(obj){
	url = "${webRoot}/subjectController/front/showSubject.dht?id="+obj;
	AT.load("iframepage",url,function() {});	
}
</script>
<body >
 <div class="right_top">
    <div class="Nav" id="nav_div">修改专题</div>
 </div>
    <div class="right_index" >
	<form action="${webRoot}/subjectController/front/editSubject.dht" method="post" id="editSubjectForm" name=editSubjectForm onsubmit="return false;"> 
		<input type="hidden" name="id" value="${subject.id}">
		<!-- Begin Information-->
		<div class="Information">
		  <div class="title">专题信息 
		  	<input style="width:100px;margin-left:10px;height:25px;" id="subject_report" onclick="toShowSubject('${subject.id}')" class="Button1" type="button" value="专题报告" name="subject_report"/>
		  </div>
		  <div class="Table">
		    <table width="100%" border="0" cellspacing="0" cellpadding="0">
		      <tr>
		        <td width="100">专题名称：</td>
		        <td>
		         <span class="tags">
		            <c:choose>
		            <c:when test="${subject.subjectName eq '默认专题'}">
		               ${subject.subjectName}
		            </c:when>
		            <c:otherwise>
		            <input name="subjectName" id="subjectName" class="InputTxt2" style=" width:70%; height:28px;line-height:28px;" value="${subject.subjectName}" type="text"/>
		            </c:otherwise>
		            </c:choose>
		            
		          </span>
		         </td>
		       </tr>
		       <tr>
		         <td>专题类型：</td>
		         <td>
		          <c:choose>
		            <c:when test="${subject.subjectName eq '默认专题'}">
		            	 	个人<input type="hidden" name="subjectType" value="1">
		            </c:when>
		            <c:otherwise>
		             <c:if test="${subject.subjectType eq 1}"> 
		         	<input type="radio" name="subjectType" id="subjectType_p" value="1" <c:if test="${subject.subjectType ==1}"> checked="checked"  </c:if> />
		           	个人
		          </c:if> 
		          <c:if test="${subject.subjectType eq 1}">   	
		           <input type="radio"   name="subjectType" id="subjectType_m" value="2"  <c:if test="${subject.subjectType==2}"> checked="checked"  </c:if>  />
		          	 多人
		          </c:if>
		          <c:if test="${subject.subjectType eq 2}">
		                                        多人<input type="hidden" name="subjectType" value="2">
		          </c:if>
		            </c:otherwise>
		            </c:choose>
				</td>
		       </tr>
		      <tr>
		        <td>专题介绍：</td>
		        <td>
		        	<textarea cols="70" rows="5" name="description" >${subject.description}
		        	</textarea>
		        </td>
		      </tr>
		    </table>
		    <div class="submit">
		      <input class="Button1" type="button" name="sub_btn" id="sub_btn" value="保存" onclick="editSubject()"/>
		      <input class="Button2" type="button" name="sub_btn" id="-${subject.subjectType}" value="返回" onclick="subjectManage(this)" style="margin-left:10px;"/>
		    </div>
		  </div>
		</div>
	<!-- End Information--> 
	</form>
</div>