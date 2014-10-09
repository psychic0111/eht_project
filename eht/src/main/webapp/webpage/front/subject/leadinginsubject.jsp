<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">

jQuery.validator.addMethod("zip", function(value, element) {
	var tel = /\.zip$/;
	return this.optional(element) || (tel.test(value));
	}, "文件只能上传zip格式"); 

$().ready(function() {
	$("#addSubjectZipForm").validate({
			rules:{
			    subjectName:{required:true,minlength:2,maxlength:40,remote:{url:'${webRoot}/subjectController/front/checkSubjectName.dht'}},
				file:{required:true,zip:true}
			},
			messages:{
				file:{required:'请选择上传文件'},
				subjectName:{remote:'专题名称已被使用'}
			}
		}
	);
	
});

</script>
<body >

	<form action="${webRoot}/subjectController/front/leadinginSuject.dht"  enctype="multipart/form-data"  method="post" id="addSubjectZipForm" name="addSubjectZipForm" target="leaddinginsubject"> 
		<!-- Begin Information-->
		<div class="Information">
		  <div class="title">导入专题</div>
		  <div class="Table">
		    <table width="100%" border="0" cellspacing="0" cellpadding="0">
		     <tr>
		        <td width="100">专题名称：</td>
		        <td>
		         <span class="tags">
		           <input name="subjectName" id="subjectName" class="InputTxt2" style="width:80%;height:28px;line-height:28px;" type="text"/>
		          </span>
		         </td>
		       </tr>
		      <tr>
		        <td width="100">选择文件：</td>
		        <td>
		         <span class="tags">
		           <input name="file" id="file"  class="InputTxt2" style=" width:80%; height:28px; " type="file"/>
		          </span>
		         </td>
		       </tr>
		    </table>
		    <div class="submit">
		      <input class="Button1" type="submit" name="sub_btn" id="sub_btn" value="导入" />
		    </div>
		  </div>
		</div>
	<!-- End Information--> 
	</form>
	<iframe name="leaddinginsubject" style="display:none;">
	
	</iframe>
