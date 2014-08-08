<%@page import="com.eht.common.constant.ActionName"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>
<%@ taglib prefix="xd" uri="http://www.xd-tech.com.cn/" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
	String uploadifyPath = basePath + "/webpage/front/js/uploadify";
	String sessionId = pageContext.getSession().getId();
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />
<c:set var="uploadifyPath" value="<%=uploadifyPath%>" />
<c:set var="sessionId" value="<%=sessionId%>" />
<style>
<!--
li.over {background-color: #bcd4ec;}   
-->
</style> 
<script type="text/javascript"> 
window.UEDITOR_HOME_URL = "${frontPath}/js/ueditor/";
window.UEDITOR_IMG_URL = "";
window.DOWNLOAD_URL = "${webRoot}/noteController/front/downloadNodeAttach.dht";
</script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/ueditor.all.min.js"> </script>
<script type="text/javascript" charset="utf-8" src="${frontPath}/js/ueditor/lang/zh-cn/zh-cn.js"></script>

<!-- 附件上传 -->
<link rel="stylesheet" type="text/css" href="<%=frontPath %>/js/uploadify3/uploadify.css"/>
<script type="text/javascript" src="<%=frontPath %>/js/uploadify3/jquery.uploadify.js"></script>
<script type="text/javascript" src="<%=frontPath %>/js/uploadify3/uploadify_api.js" charset="utf-8"></script>
<!-- 附件上传 -->
<script type="text/javascript" src="<%=frontPath %>/js/uploadfile.js" charset="utf-8"></script>


<script type="text/javascript" src="${frontPath}/js/note.js"></script>
<script type="text/javascript" src="${frontPath}/js/tag.js"></script>
<script type="text/javascript" src="${frontPath}/js/noteshare.js"></script>
<!-- Begin function-->
	<form id="noteForm" name="noteForm" action="${webRoot}/noteController/front/saveNote.dht" method="post" onsubmit="return false;">
		<input type="hidden" id="noteForm_id" name="id" value="${id}"/>
		<input type="hidden" id="noteForm_subjectId" name="subjectId" value="${subjectId}"/>
		<input type="hidden" id="noteForm_dirId" name="dirId" value="${dirId}"/>
		<input type="hidden" id="noteForm_tagId" name="tagId" value="${tagId}"/>
		<input type="hidden" id="noteForm_version" name="version" value="${version}"/> 
		<input type="hidden" id="sessionId" name="sessionId" value="${sessionId}"/> 
		<input type="hidden" id="noteForm_createuser" value="${createUserId }"/> 
      <div class="function" id="_more">
        <div class="left1">
          <input class="Button2" style="width:82px;display:none;" onclick="addNewNote()" type="button" name="note_new" id="note_new" value="+ 新建条目" />
          <input class="Button1" type="button" name="saveNote_btn" id="saveNote_btn" onclick="saveNote()" value="保存" style="display:none;"/>
          <input class="Button4" type="button" name="note_edit" id="note_edit" onclick="enableEditNote()" value="编辑条目" style="display:none;"/>
          <input style="display:none;" class="Button2" type="button" name="restoreNote_btn" id="restoreNote_btn" onclick="restoreNote()" value="还原" />
          <input class="Button4" type="button" name="note_share" id="note_share" onclick="shareNote()" value="分享" style="display:none;"/>
          <input class="Button4" type="button" name="note_blacklist" id="note_blacklist" onclick="noteblacklist()" value="黑名单"  style="display:none;"/>
        </div>
        <div class="others">
          <input class="Button3" type="button" onclick="deleteNote()" name="deleteNote_btn" id="deleteNote_btn" value="删除" />
          <!-- <input class="Button5" type="button" name="historyNote_btn" id="historyNote_btn" onclick="historyNote()" value="历史版本" /> -->
        </div>
        <div class="clear"></div>
      </div>
      <!-- End function--> 
      <div id="noteContentDiv">
      <!-- Begin new_edit-->
      <div class="new_edit" id="new_edit">
        <div class="title">
          <div class="left" style="margin-top:-5px;width:60%;">
          	<input id="noteTitleField" class="InputTxt3" type="text" value="${title}" name="title" style=" width:100%;line-height:29px;" maxlength="200">
          </div>
           <div class="clear"></div>
        </div> 
        <div class="title">
          <div class="left" style="font-size:14px;">位置：<span id="noteSubjectName"></span><span id="noteDirName"></span> </div>
          <div class="clear"></div>
        </div>
        <div class="Edit">
          <div style="font-size:12px;text-decoration:none;position:relative; color: rgb(0, 99, 220);"padding-right: 5px>
		       <div class="Edit_others" style="padding: 7px">
			        标签：<span>
			       	  <img src="${imgPath}/97162.gif.png" id="selectTag" onclick="selectTagTree()"  style="cursor:pointer;width:18px" title="添加标签"  />
			          <span id="tagSelectNode" ></span>
			       </span>
		        </div>
			    <div id="tagSelectContent" hidefocus="true" class="menuContent" style="background:#FFFFFF;font-size:13px;border: 1px solid rgb(190, 190, 190);display:none; position: absolute;z-index:1001">
					<ul id="tagSelectTree"  hidefocus="true" class="tag_tree" style="margin-top:0; width:186px;z-index:1002"></ul>
						<div class="rightMenu" id="tagSelectTreeRightMenu">
				        		<ul id="tagSelectTreeRightMenu_ul_tag">
				       				<li id="tagSelectTreeRightMenu_add_tag" onclick="tagaddChildTag()">添加标签</li>
				       				<li id="tagSelectTreeRightMenu_rename_tag" onclick="tagRenameNode()">修改标签</li>
				       				<li id="tagSelectTreeRightMenu_delete_tag" onclick="tagDeleteNode()">删除</li>
				        		</ul>
		        	   </div> 
				</div>
          </div>
        </div>
         <div class="title1" style="padding-bottom: 1px;padding: 7px">
         <div id="attTemp" style="display:none"><div id="attachment" style="line-height:16px;height:20px;"></div></div>
   	  	   <div class="left" style="font-size:12px;color: rgb(0, 99, 220);padding-right: 15px">
	   	  	   	附件：
	   	  	    <span><img src="${imgPath}/97162.gif.png" id="selectAta" onclick="showuploadwindow()"  style="cursor:pointer;width:18px" title="添加附件"  /></span>
   	  	   </div>
   	  	   <div id="attachmentListDiv"></div>
   	  	   <div class="clear"></div>
        </div>
        <div class="Edit_input">
          <script id="note_editor" name="content" type="text/plain" style="width:100%;height:700px;display:none;"></script>
        </div>
        <div id="parentHtmlViewDiv" style="width: 100%;overflow-y: scroll;overflow-x: auto; height:440px;bottom: 0;box-sizing: border-box;">
        	<div id="htmlViewDiv" style="box-sizing: border-box;-webkit-box-sizing:border-box;-moz-box-sizing:border-box;padding-bottom: 0px;padding-top:0px;background-color: white;">
    		 	<iframe id="htmlViewFrame"  frameborder="0" border="0" style="overflow-y: auto;border:0;outline:0;border-bottom:0px;border-top:0px;border-right:0px;border-left:0px; frameborder:0; outline-style:none;outline-color:invert;outline-width:0px; min-width: 100%;" >
   				</iframe>
    		</div>
    		<!-- Begin comments-->
		       <div class="comments" id="comments_div" style="height:40px;display:none;">
		        <div class="top" style="cursor:pointer;" ><img src="${imgPath}/comments1a.png" id="comment_img" onclick="togComment()" height="35px"/></div>
		        <div class="comments_list" style="display:none;" id="comments_list"></div>
		      </div>
		    <!-- End comments--> 
        </div>
      </div>
        
      <!-- End new_edit--> 
     
      </div>
    <!-- End notes_new-->
    <div class="clear"></div>
	</form>
	   
<div id="emailMsgDiv" style="font-size:14px;color: rgb(68, 68, 68);display:none;z-index:10001;background:#FFFFFF;border:1px solid rgb(213, 213, 213);width: 300px;position: absolute;top: 0px;left: 0px;">
    <ul style="display:none;" class="floattishi" id="flttishi" ></ul>
	<input type="hidden" name="prevTrIndex" id="prevTrIndex" value="-1" />    
</div>
<script>
function reloadEditor(){
	var hidden = false;
	var edui1=$(".edui-editor");
	if(edui1.length>0 && edui1.is(":visible")){
		
	}else{
		hidden = true;
	}
	if(noteEditor!=null){
		noteEditor.destroy();
	}
	noteEditor = UE.getEditor('note_editor', {initialFrameWidth:editorWidth, initialFrameHeight:editorheight,autoHeightEnabled:false});
	
	if(hidden){
		noteEditor.addListener("ready", function(){
			noteEditor.hide();
		});
	}
}

$(document).ready(function() {
	showLoading_edit(); 
	if(UE.browser.ie){
		editorheight = editorheight -10;
	}
	editorWidth = $("#notes_new").width()-20; 
	noteEditor = UE.getEditor('note_editor', {initialFrameWidth:editorWidth, initialFrameHeight:editorheight,autoHeightEnabled:false});
	noteEditor.addListener("ready", function(){
		noteEditor.hide();
	});
	//$("#htmlViewDiv").height(editorheight);
	//$("#notes_new").height($("#right_index").height()+100);
	var downloadPath = webRoot+"/noteController/front/downloadNodeAttach.dht";
	var upLoadPath =webRoot+"/noteController/front/uploadNodeAttach.dht";
	var basePath = uploadifyPath;
	var multiUpload=new MultiUpload("attachmentListDiv","filename",downloadPath,upLoadPath,'<%=frontPath%>',$('#sessionId').val());//附件上传
  	  if($('#noteForm_id').val()==null||$('#noteForm_id').val()==''){
      	$('#attachment').hide();
  	 }

  	hideLoading_edit();
});

</script>