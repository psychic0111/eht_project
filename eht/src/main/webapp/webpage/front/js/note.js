var followNodeId='';
//显示、隐藏评论
function togComment(){
	var src = $("#comment_img").attr("src");
	if(src.indexOf("comments1a.png") != -1){
		$("#comment_img").attr("src",imgPath+ "/comments1b.png");
		 //显示评论
		var id=$("#noteForm_id").val();
		var params = {'noteId':id};
		 AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
			 $("#comments_list").empty();
			 $("#comments_list").append(data);
		});
		 $("#comments_list").show();
	}else{
		$("#comment_img").attr("src", imgPath+ "/comments1a.png");
		 $("#comments_list").hide();
	}
}

//编辑条目按钮事件
function enableEditNote(){
	if($("#note_edit").hasClass("Button4")){
		noteEditor.setContent($("#divhiden").text());
		$("#htmlViewFrame").contents().find('body').html("");
		editNotePageAndButton();
	}else if($("#note_edit").hasClass("Button3")){
		$("#divhiden").text(noteEditor.getContent());
		$("#htmlViewFrame").contents().find('body').html($("#divhiden").text());
		viewNotePageAndButton();
		$("#tagSelectNode").empty();
		$("input[name='noteTagId']").remove();
		for(var i = 0; i < selectTags.length; i++){
			var treeNode = selectTags[i];
			// 标签显示
			var tagLbl = $("<li onclick='selectTagTree()' class='note_tag' id='li_"+ treeNode.id +"'>"+ treeNode.name +"</li>");
			$("#tagSelectNode").prepend(tagLbl);
			
			// 条目form中添加隐藏域
			var tagObj = $("<input type='hidden' name='noteTagId' id='" + treeNode.id + "' value='" + treeNode.id + "'/>");
			$("#noteForm").append(tagObj);
		}
	}
}


//选择附件或者标签时   条目切换成编辑状态
function enableEditNoteT(){
	if(document.getElementById("note_edit").style.display!='none'){
		if($("#note_edit").hasClass("Button4")){
			noteEditor.setContent($("#divhiden").text());
			$("#htmlViewFrame").contents().find('body').html("");
			editNotePageAndButton();
		}
	} 
}

function saveNote(){
	showLoading_edit();
	noteEditor.sync("noteForm");
	$("#divhiden").text(noteEditor.getContent());
	AT.post($("#noteForm").attr("action"),$("#noteForm").serialize(), function(data){
		//刷新当前条目id
		$("#noteForm_id").val(data.id);
		$("#noteForm_createuser").val(data.createUserId);
		//显示&重置附件
		
		$("#htmlViewFrame").contents().find('body').html($("#divhiden").text());
		viewNotePageAndButton();
		searchNotes(selectInfo.isDeleted,true);
		$("#note_new").val("+ 新建条目");
		if($("#addCommentForm").length==0){
			 var params = {'noteId':data.id};
			 //显示评论
			 AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
				 $("#comments_list").empty();
				 $("#comments_list").append(data);
			});
		}
		setTimeout('hideLoading_edit()',100);
	});
} 

function saveNoteQuiet(){
	noteEditor.sync("noteForm");
	$("#divhiden").text(noteEditor.getContent());
	AT.post($("#noteForm").attr("action"),$("#noteForm").serialize(), function(data){
		//刷新当前条目id
		$("#noteForm_id").val(data.id);
		$("#noteForm_createuser").val(data.createUserId);
		//显示&重置附件
		
		if($("#addCommentForm").length==0){
			 var params = {'noteId':data.id};
			 //显示评论
			 AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
				 $("#comments_list").empty();
				 $("#comments_list").append(data);
			});
		}
	});
} 

//添加评论
function addComment(){
	 var noteId=$("#noteId").val();
	AT.postFrm("addCommentForm",function(data){
		if(data.success){  
			MSG.alert(data.msg);
			 var params = {'noteId':noteId};  
    		 AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
    			 $("#comments_list").empty();
    			 $("#comments_list").append(data);
    		});
		}else{
			MSG.alert(data.msg);
		}
	},true);
}
//删除评论
function delComment(obj){
		 var noteId=$("#noteId").val();
		 var paramsdel = {'Id':obj};
		 AT.post(webRoot+"/commentController/front/commentDel.dht",paramsdel,function(data){
			 if(data.success){  
					MSG.alert(data.msg);
					 var params = {'noteId':noteId};  
		    		 AT.post(webRoot+"/commentController/front/findCommentByNote.dht",params,function(data){
		    			 $("#comments_list").empty();
		    			 $("#comments_list").append(data);
		    		});
			}else{
				MSG.alert(data.msg);
			}
		});
}

//删除条目按钮事件
function deleteNote(){
	var submit = function (v, h, f) {
	    if (v == true){ 
	    	var deleted = 0;
	    	if($("#restoreNote_btn").is(":visible")){
	    		deleted = 1;	
	    	}
	    	var url = webRoot+"/noteController/front/deleteNote.dht?id=" + $("#noteForm_id").val() + "&deleted=" + deleted;
	    	AT.post(url,null, function(data){
	    		searchNotes(selectInfo.isDeleted);
	    	});
	    }
	    return true;
	}; 
	$.jBox.confirm("您确定删除？", "提示", submit, { buttons: { '确定': true, '取消': false} })
}

//========================上传附件 start===================================================================
function removeCurrAttachment(obj){
	var submit = function (v, h, f) {
	    if (v == true){
	    	var params = {"id":$(obj).attr("attaid")};  
			var url = webRoot+"/noteController/front/removeAttach.dht";
			AT.post(url, params, function(data){ 
				$(obj).parent().remove();
				//刷新附件前7个附件
				loadAttachment("current");
				loadAttachment();
			}); 
	    }
	    return true;
	}; 
	$.jBox.confirm("您确定删除？", "提示", submit, { buttons: { '确定': true, '取消': false} })
}
//========================上传附件 end===================================================================

function noteblacklist(){
	var note_id =$("#noteForm_id").val(); 
	if(note_id!=null&&note_id==''){
		MSG.alert("请选择条目!");
		return false;
	}
	 var params = {'nodeId':note_id,"subjectid":$('#noteForm_subjectId').val()};
	 AT.post(webRoot+"/noteController/front/blackListNote.dht",params,function(data){
		 easyDialog.open({
				container : {
					header : '<img src='+imgPath+'/mail_blue.png  height="23"  >',
					content : data
				},
				follow : 'note_blacklist',
				followX : 0,
				followY : 34
			}); 
	});
}

function addBlackListNote(userId,nodeId,obj){
	var params = {'userId':userId,'nodeId':nodeId};
    if(confirm("确定将此用户"+obj.value+"?")){
		 AT.post(webRoot+"/noteController/front/addblackListNote.dht",params,function(data){
				if(data.success){
					 MSG.alert(data.msg);
					 var k=obj.value;
					 if(k=='移除黑名单'){
						 obj.value='加入黑名单';
						 }else{
							 obj.value='移除黑名单';
							 }
				}else{
					 MSG.alert(data.msg);
				}	
		});
    }
}
//分页
function doPageblackList(ths,pageNo,pageSize){
	var historyNoteId=$("#historyNoteId").val();
	var params = {'nodeId':historyNoteId,'pageNo':pageNo};  
	
	 AT.post("${webRoot}/noteController/front/historyNote.dht",params,function(data){
		 easyDialog.open({
				container : {
					header : '<img src="${imgPath}/note_ico.png">',
					content : data
				},
				follow : 'note_blacklist',
				followX : 0,
				followY : 34
			}); 
	});
}

//设置目录黑名单
function blackListdir(obj,obj2){
	var params = {'subjectId':obj2,'directoryId':obj};
	 AT.post(webRoot+"/directoryController/front/blackListDirectory.dht",params,function(data){
		 easyDialog.open({
				container : {
					header : '<img src="${imgPath}/mail_blue.png" height="23"  >',
					content : data
				},
				follow : followNodeId,
				followX : 30,
				followY : 0
			}); 
	});
	hideRightMenu();
	}

//分页
function doPageblackDirectoryList(ths,pageNo,pageSize){
	 AT.post(webRoot+"/directoryController/front/blackListDirectory.dht",$("#blackListDirectoryForm").serialize(),function(data){
		 easyDialog.open({
				container : {
					header : '<img src="${imgPath}/mail_blue.png"  height="23"  >',
					content : data
				},
				follow : followNodeId,
				followX : 30,
				followY : 0
			}); 
	});
}

function addBlackListDirectory(userId,directoryId,obj){
	var params = {'userId':userId,'directoryId':directoryId};
	
    if(confirm("确定将此用户"+obj.value+"?")){
	 AT.post(webRoot+"/directoryController/front/addblackListDirectory.dht",params,function(data){
			if(data.success){
				 MSG.alert(data.msg);
				 var k=obj.value;
				 if(k=='移除黑名单'){
					 obj.value='加入黑名单';
					 }else{
						 obj.value='移除黑名单';
						 }
			}else{
				 MSG.alert(data.msg);
			}	
	});
    }
}
//设置目录黑名单结束