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
/* 上传附件不换行 */
#attTemp1 div{display:inline;border:1px double red}


#tagSelectContent ul li a span:nth-child(2){	
			width:200px;
			height:12px;
			overflow:hidden;
			white-space:nowrap;
			text-overflow:ellipsis;
			text-overflow: ellipsis;/* IE/Safari */
			-ms-text-overflow: ellipsis;
			-o-text-overflow: ellipsis;/* Opera */
			-moz-binding: url("ellipsis.xml#ellipsis");/*FireFox*/
} 

.note_tag{
	cursor:pointer;
	background-color: #fff7b5;
	border-radius: 10px;
    box-shadow: 0 0 2px rgba(0, 0, 0, 0.25);
    color: #c3c3c3;
    display: block;
    float: left;
    margin: 0 6px 6px 0;
    /* max-width: 100px; */
    padding: 1px 12px;
    transition: all 100ms ease 0s;
}

.note_comment{
	cursor:pointer;
	background-color: #dcdcdc;
    color: #555;
    display: block;
    float: right;
    margin: 0 6px 6px 0;
    padding: 1px 12px;
    transition: all 100ms ease 0s;
}

-->
</style> 

	<!-- Begin function-->
	<form id="noteForm" name="noteForm" action="${webRoot}/noteController/front/saveNote.dht" method="post" onsubmit="return false;">
		<input type="hidden" id="noteForm_id" name="id" value="${id}"/>
		<input type="hidden" id="noteForm_subjectId" name="subjectId" value="${subjectId}"/>
		<input type="hidden" id="noteForm_dirId" name="dirId" value="${dirId}"/>
		<%-- <input type="hidden" id="noteForm_tagId" name="noteTagId" value="${tagId}"/> --%>
		<input type="hidden" id="noteForm_version" name="version" value="${version}"/> 
		<input type="hidden" id="sessionId" name="sessionId" value="${sessionId}"/> 
		<input type="hidden" id="noteForm_createuser" value="${createUserId }"/> 
      <div class="function" id="_more">
        <div class="left1">
          <input class="Button2" type="button" name="note_new" id="note_new"  onclick="addNewNote()" value="+ 新建条目" style="width:82px;display:none;"/>
          <input class="Button1" type="button" name="saveNote_btn" id="saveNote_btn" onclick="saveNote()" value="保存" style="display:none;"/>
          <input class="Button4" type="button" name="note_edit" id="note_edit" onclick="enableEditNote()" value="编辑条目" style="display:none;"/>
          <input class="Button2" type="button" name="restoreNote_btn" id="restoreNote_btn" onclick="restoreNote()" value="还原" style="display:none;"/>
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
      <div id="noteContentDiv" style="height:100%;">
      <!-- Begin new_edit-->
      <div class="new_edit" id="new_edit" style="height:100%;">
        <div class="title">
          <div class="left" style="margin-top:-5px;width:60%;">
          	<input id="noteTitleField" class="InputTxt3" type="text" value="${title}" name="title" style=" width:100%;line-height:29px;" maxlength="200">
          </div>
          <div class="note_comment"><a href="#" onclick="toComment()">评论</a></div>
           <div class="clear"></div>
        </div> 
        <div class="title">
          <div class="left" style="font-size:14px;">位置：
          <img src="${imgPath}/97162.gif.png" id="selectDir" onclick="selectDirTree()"  style="cursor:pointer;width:18px;display:none;" title="修改位置" />
          <span id="noteSubjectName"></span>
          </div>
          <div id="dirSelectContent" hidefocus="true" class="menuContent" style="background:#FFFFFF;font-size:13px;border: 1px solid rgb(190, 190, 190);display:none; position: absolute;z-index:1501">
					<i onclick="hideDirMenu()" style="float:right;margin-top:5px;margin-right:5px;background-image:url('${imgPath}/34aL_046.png');width:16px;height:16px;cursor:pointer;"></i>
					<ul id="dirSelectTree"  hidefocus="true" class="tag_tree" style="margin-top:0; width:186px;z-index:1002">
					</ul>
			</div>
          <div class="clear"></div>
        </div>
        <div class="Edit">
          <div style="font-size:12px;text-decoration:none;position:relative;">
		       <div class="Edit_others" style="padding:7px 7px 7px 10px">
			        标签：<span>
			       	  <img src="${imgPath}/97162.gif.png" id="selectTag" onclick="enableEditNoteT();selectTagTree()"  style="cursor:pointer;width:18px" title="添加标签"  />
			          
			          <ul id="tagSelectNode" style="display:inline-block;vertical-align:middle;height:20px;">
			          	
			          </ul>
			       </span>
			       <div id="tagSelectNode_div" style="display:none;border:1px solid #8F83BF;top:40px;left:71px;position:absolute;border-radius:3px;box-shadow:0px 0px 2px rgba(0, 0, 0, 0.25);background:#E1E4EF;z-index:100;">
						<span style="float:right;margin-right:5px;cursor:pointer;" onclick="closeTagDiv()">
							<i style="font:inherit;color:#2866C3;">关闭</i>
						</span>
						<ul id="tagSelectNode_hidden" style="display:inline-block;vertical-align:middle;padding:15px 15px 0 5px;">
						</ul>
				   </div>
		        </div>
			    <div id="tagSelectContent" hidefocus="true" class="menuContent" style="background:#FFFFFF;font-size:13px;border: 1px solid rgb(190, 190, 190);display:none; position: absolute;z-index:1501">
					<i onclick="hideTagMenu()" style="float:right;margin-top:5px;margin-right:5px;background-image:url('${imgPath}/34aL_046.png');width:16px;height:16px;cursor:pointer;"></i>
					<ul id="tagSelectTree" hidefocus="true" class="tag_tree" style="margin-top:0; width:186px;z-index:1002">
					</ul>
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
         <div class="title1" style="padding-bottom: 1px;padding:7px 7px 7px 10px">
         <div id="attTemp" style="position: relative;z-index:99">
         		附件：<div onclick="enableEditNoteT()" id="attachment" style="line-height: 16px; height: 0px; display: block;" title="添加附件"></div>
   	  	   		<div id="attachmentListDiv" style="position: absolute;top:0px;left:65px"></div>
   	  	   		<div id="attaMore" style="position: absolute;top:0px;left:89%;color: rgb(153, 153, 153);cursor: pointer;display: none;float: right;font-size: 12px;line-height: 20px;text-align: right;width: 40px;"  onclick="showButtonMore(this)">更多</div>
   		 </div>
   	  	   <%-- <div class="left" style="font-size:12px;color: rgb(0, 99, 220);padding-right: 15px">
	   	  	   
	   	  	    <span><img src="${imgPath}/97162.gif.png" id="selectAta" onclick="showuploadwindow()"  style="cursor:pointer;width:18px" title="添加附件"  /></span>
   	  	   </div> --%>
   	  	   <div class="clear"></div>
        </div>
        <div id="attMoreDIV" class="Edit_input" style="display: block;padding-left:10px"></div>
        <div class="Edit_input">
          <script id="note_editor" name="content" type="text/plain" style="width:100%;height:700px;display:none;"></script>
        </div>
        <div id="parentHtmlViewDiv" style="height:100%;border-top:1px solid #d9d9d9;border-left:1px solid #d9d9d9;border-right:1px solid #d9d9d9;border-bottom:1px solid #d9d9d9;overflow:auto;">
        	<div id="htmlViewDiv">
    		 	<iframe id="htmlViewFrame" scrolling="no" frameborder="0" style="border:none;outline:0; frameborder:0; outline-style:none;outline-color:invert;outline-width:0px; min-width: 100%;height:100%;" >
   				</iframe>
    		</div>
    		<!-- Begin comments-->
	        <div class="comments" id="comments_div" style="width:90%;margin-left:50px;">
	        	<%-- <div class="top" style="cursor:pointer;" ><img src="${imgPath}/comments1a.png" id="comment_img" onclick="togComment()" height="35px"/></div> --%>
	        	<div class="comments_list" id="comments_list">
	        	</div>
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
<div id="divhiden" style="display:none;"></div>
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
	if(editorheight==null){
		editorheight = document.body.clientHeight;
	}
	var frameHeight = editorheight - 278;
	editorheight = frameHeight - $("#edui1_toolbarbox").outerHeight() - 54;
	if(UE.browser.ie){
		editorheight = editorheight -10;
	}
	editorWidth = $("#notes_new").width() - 4; 
	noteEditor = UE.getEditor('note_editor', {initialFrameWidth:editorWidth, initialFrameHeight:editorheight,autoHeightEnabled:false});
	noteEditor.addListener("ready", function(){
		noteEditor.hide();
	});
	$("#parentHtmlViewDiv").height(frameHeight);
	//$("#htmlViewFrame").height(editorheight);
	//$("#notes_new").height($("#right_index").height()+100);
	var downloadPath = webRoot+"/noteController/front/downloadNodeAttach.dht";
	var upLoadPath =webRoot+"/noteController/front/uploadNodeAttach.dht";
	var basePath = uploadifyPath;
	var upfileButton = " <a href='javascript:void(0);;' style='border:1px solid #fff;z-index:0'>添加附件 </a>";
	var multiUpload=new MultiUpload("attachmentListDiv","filename",downloadPath,upLoadPath,'<%=frontPath%>',$('#sessionId').val(),upfileButton);//附件上传
	if($('#noteForm_id').val()==null||$('#noteForm_id').val()==''){
      	$('#attachment').hide();
      	$("#selectTag").hide();
  	 }
  	hideLoading_edit();
});

function toComment(){
	if($("#note_edit").hasClass("Button3")){
		enableEditNote();
	}
	$("#pinglun").focus();
}
</script>