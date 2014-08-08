//树对象
var tag_zTree_Menu = null;
//右键点击的节点
var tagRightClickNode = null;
 var editorWidth = $("#notes_new").width() - 2;
 var editorHeight = $("#right_index").height() - $("#topFuncDiv").height() - 30;
 window.onscroll = function(){
	    var t = document.documentElement.scrollTop || document.body.scrollTop;
	    MSG.alert(t);
 }
$(document).ready(function() {  
	 var downloadPath = webRootUR+"/noteController/front/downloadNodeAttach.dht";
	 var upLoadPath =webRootUR+"/noteController/front/uploadNodeAttach.dht";
	 var basePath = uploadifyPath;
	 var multiUpload=new MultiUpload("attachmentListDiv","filename",downloadPath,upLoadPath,basePath,$('#sessionId').val());//附件上传
     if($('#noteForm_id').val()==null||$('#noteForm_id').val()==''){
         $('#attachment').hide();
     }
});
//========================上传附件 start===================================================================
function removeCurrAttachment(obj){
	var submit = function (v, h, f) {
	    if (v == true){
	    	var params = {"id":$(obj).attr("attaid")};  
			var url = webRootUR+"/noteController/front/removeAttach.dht";
			AT.post(url, params, function(data){ 
				$(obj).parent().remove();
			});
			jBox.tip("删除成功!", 'info');
	    }
	    return true;
	}; 
	$.jBox.confirm("您确定删除？", "提示", submit, { buttons: { '确定': true, '取消': false} })
}
//========================上传附件 end===================================================================
	var editorWidth = $("#notes_new").width()-30;
	if(editorWidth>840){
		editorWidth = 800;
	}
	var editorHeight = $("#right_index").height() - $("#topFuncDiv").height() - 205;
	var noteEditor = UE.getEditor("note_editor", {initialFrameWidth:editorWidth, initialFrameHeight:editorHeight});

	$("#edui1").css("width","");
	$("#edui1_iframeholder").css("width","");
	function saveNote(){
		noteEditor.sync("noteForm");
		if($("#noteTitleField").val().length>200){
              MSG.alert('标题长度最多200');
              return;
			}
		AT.post($("#noteForm").attr("action"),$("#noteForm").serialize(), function(data){
			//刷新当前条目id
			$("#noteForm_id").val(data.id);
			
			//显示&重置附件
			flushAttachment_show();
  	    	
			disableEditNote();
			searchNotes();
			$("#note_new").removeAttr("disabled");
			$("#note_new").val("+ 新建条目");
			$("#note_edit").removeAttr("disabled");
		});
	} 
//====================重置附件=================================================================================
		function flushAttachment_show(){
  	    	$('#attachment').show();
  	    	//$('#currAttachment1').html('');
		}
		function flushAttachment_hiden(){
  	    	$('#attachment').hide();
  	    	$('#currAttachment1').html('');
  	    	$('#tagSelectNode').html("");
		}
		function flushTag_empty(){ 
  	    	$('#tagSelectNode').html("");
		}
//============================================编辑区 【标签】===============================================================
		//标签树初始化
		var noteContent_Tag_TreeSetting = {
			data : {
				simpleData : {
					enable : true
				}
			},
			view: {
				dblClickExpand: false,
				nameIsHTML: false
			},
			callback : {  
				onClick : current_noteContent_Tag,
				beforeRightClick: this.tagSelectNodeBeforeRightClick,
				onRename: this.tagSelectNodeOnRename,
				onRightClick: this.nodeOnRightClick
			}/* ,
			async : {
			    autoParam : ["id", "dataType", "branchId"],
			    contentType : "application/x-www-form-urlencoded",
			    dataFilter : null,
			    dataType : "text",
			    enable : true,
			    type : "post",
			    url : webRootUR+"/indexController/front/reloadNode.dht"

			} */
		};
		//选择当前tag标签
	    function  current_noteContent_Tag(event, treeId, treeNode){
	    	var temp = new Array(); 
	    	$("#noteForm_tagId").val(treeNode.id);
	    	//如果节点下有子节点 则展开
	    	if(treeNode.children!=null&&treeNode.children.length!=0){
	    		tag_zTree_Menu.expandNode(treeNode, true, true, true);
	    	}else{
	    		do{
	    			temp.unshift(treeNode);
	    			treeNode = treeNode.getParentNode();
	    		} while(treeNode!=null&&treeNode.isParent);
	    		
	    		var test = '';
	    		$.each(temp, function(i, node) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
	    			test+="-><span style='border: 2px solid rgb(213, 213, 213);'>"+node.name+"</span>";
	    		}); 
	    		$("#tagSelectNode").html(test);
	    		hideTagMenu();
	    	}
		}
	    
		//隐藏标签树功能
		function hideTagMenu() {
			$("#tagSelectContent").fadeOut("fast");
			$("body").unbind("mousedown", onBodyTagDown);
		}
		//鼠标页面点击事件
		function onBodyTagDown(event){
			if(!isShowConfirm(event)){
				return false;
			}
			if (event.target.id != "tagSelectContent"&&event.target.id!="tagSelectTreeRightMenu") { 
				hideTagMenu();
			}
		}
		//在弹出区域不需要隐藏树
		$("#tagSelectContent").mouseover(function(){
			$("body").unbind("mousedown", onBodyTagDown); 
		}).mouseout(function(){
			$("body").bind("mousedown", onBodyTagDown);
		});
		//显示标签树
		function selectTagTree() {
			var params = {"subjectid":$('#noteForm_subjectId').val()};  
			var url = webRootUR+"/noteController/front/treeData.dht";
			AT.post(url, params, function(data){
				$.fn.zTree.init($("#tagSelectTree"), noteContent_Tag_TreeSetting, data);
				tag_zTree_Menu = $.fn.zTree.getZTreeObj("tagSelectTree");
			});
			var tagSelect = $("#tagSelectNode");
			var tagOffset = tagSelect.offset();
			$("#tagSelectTree").html("");
			$("#tagSelectContent").css({left:"0px", top:"122px"}).slideDown("fast");
			$("body").bind("mousedown", onBodyTagDown);
		}
		
		//-------------------------------------------------------------
		//鼠标右键之前初始化
		function tagSelectMouseMenu(treeId, node){
			var hideObj = null;
			var showId =  "tagSelectTreeRightMenu_ul_tag";
			//判断控件内是否有菜单存在
			if($("#" + showId + " li") != null && $("#" + showId + " li").length > 0){
				$("#" + showId).show();
				var obj = $("#" + node.tId + "_a");
				var top = obj.offset().top;
				var left = obj.offset().left;
				$("#tagSelectTreeRightMenu").css({position: "absolute",'top':top - 200,'left':left - 560});
				$("#tagSelectTreeRightMenu").show();
			}
			$("body").bind("mousedown", tagSelectOnBodyClick);
		}
		
		//鼠标注右键点击前, 准备菜单
		function tagSelectNodeBeforeRightClick(treeId, node){
			tagRightClickNode = node; 
			tagSelectMouseMenu(treeId, node);
			return true;
		}
		//判断当前元素是否是confirm
		function isShowConfirm(event){
			var bool = ($(event.target).attr("class")!=null&&$(event.target).attr("class").indexOf("jbox")>0)||($(event.target).attr("style")!=null&&$(event.target).attr("style").indexOf("margin:10px;min-height:30px;height:auto;padding-left:40px;text-align:left")>0)||($(event.target).attr("css")==null&&$(event.target).attr("style")==null)
			if((bool)){
				return true;
			}
		 return false; 
		}
		//右键菜单关闭方法（）
		function tagSelectHideRightMenu(){
			$("#tagSelectTreeRightMenu").hide(); 
			//tagRightClickNode = null; 
			$("body").unbind("mousedown", tagSelectOnBodyClick);
		}
		
		//鼠标页面点击事件
		function tagSelectOnBodyClick(event) {
			if (event.target.id != "tagSelectTreeRightMenu_add_tag" && event.target.id != "tagSelectTreeRightMenu_rename_tag" && event.target.id != "tagSelectTreeRightMenu_delete_tag" ) {
				tagSelectHideRightMenu();
			}
		}
		
		//鼠标右键事件
		function tagSelectNodeOnRightClick(e, treeId, node){
			tag_zTree_Menu.selectNode(node);
		}
		

		//右键菜单增加标签
		function tagaddChildTag(){
			var newNode = {id:"",name:"新标签",icon:imgPath+"/tree/tag_blue.png"};
			newNode = tag_zTree_Menu.addNodes(tagRightClickNode, newNode);
			tagSelectHideRightMenu();
			tag_zTree_Menu.editName(newNode[0]);
		}
		//右键菜单重名命
		function tagRenameNode(){
			tagRightClickNode.name=tagRightClickNode.title;
			tag_zTree_Menu.editName(tagRightClickNode);
			tagSelectHideRightMenu();
		}  

		//右键菜单删除
		function tagDeleteNode(){
			var msg = "";
			var url = "";
			var dataParam = "id=" + tagRightClickNode.id;
			var dataType = tagRightClickNode.dataType;
			if(dataType == "TAG"){ //标签节点
				url = webRootUR+"/tagController/front/deleteTag.dht";
			}  
			var submit = function (v, h, f) {
			    if (v == true){
					AT.post(url, dataParam, function(data){
						if(data==true){
					    	tag_zTree_Menu.removeNode(tagRightClickNode); 
						}
					}, true);
			    }
			    return true;
			}; 
			$.jBox.confirm("确认要删除此标签吗？", "提示", submit, { buttons: { '确定': true, '取消': false} });
		}
		//编辑节点名称前
		function tagSelectNodeOnRename(e, treeId, node){
			var parentNode = node.getParentNode();
			var subjectId = "";
			var parentId = "";
			if(parentNode.dataType == "TAG"){  //标签
				if(parentNode.id == 'tag_personal'){  //个人标签
					// parentId和subjectId
				}else if(parentNode.branchId == "tag_personal"){
					parentId = parentNode.id;
				}else if(node.id!=null&&node.id.indexOf('tag_subject')!= -1){ //专题标签
					subjectId = parentNode.branchId;
				}else{
					subjectId = parentNode.branchId;
					parentId = parentNode.id;
				}
				var dataParam = "id=" + node.id + "&name=" + node.name + "&subjectId=" + subjectId + "&parentId=" + parentId;
				var url = webRootUR+"/tagController/front/saveTag.dht";
				AT.post(url, dataParam, function(data){
					if(data.status != 'undefined' && data.status != null && data.status != '' && data.status == 500){
						tag_zTree_Menu.editName(node);
					}else{
						//添加后设置目录节点的ID
						node.id = data.id;
						node.dataType = "TAG";
						node.branchId = data.subjectId;
						node.pId = data.pId;
						tag_zTree_Menu.updateNode(node); 
					}
				}, true);
			}
		}
//============编辑区 标签选择 END=================================================================================
//============过滤标签选择 START ================================================================================
		//标签过滤树
		function buildTagTree(){ 
				var params = {"subjectid":$('#noteForm_subjectId').val()};  
				var url = webRootUR+"/noteController/front/treeData.dht";
				AT.post(url, params, function(data){
					$.fn.zTree.init($("#tagTree"), tagTreeSetting, data);
				}); 
		}
		//显示标签树
		function showTagTree() {
			buildTagTree();
			var tagSelect = $("#tagSelect");
			var tagOffset = tagSelect.offset();
			$("#tagContent").css({left:tagOffset.left + "px", top:tagOffset.top + tagSelect.outerHeight() + 2 + "px"}).slideDown("fast");
			$("body").bind("mousedown", _index_onbodyDown);
		}
		//鼠标页面点击事件
		function _index_onbodyDown(event) {
			if (event.target.id != "tagContent") { 
				hideM();
			}
		}
		//隐藏标签树功能
		function hideM() {
			$("#tagContent").fadeOut("fast");
			$("body").unbind("mousedown", _index_onbodyDown);
		}
		//在弹出区域不需要隐藏树
		$("#tagContent").mouseover(function(){
			$("body").unbind("mousedown", _index_onbodyDown);
		}).mouseout(function(){
			$("body").bind("mousedown", _index_onbodyDown);
		});
		// 点击条目查看条目
      	function viewNote(id,ishiden){
      		//buildTagTree();
      		 var params = {'noteId':id};  
    		 AT.post(webRootUR+"/commentController/front/findCommentByNote.dht",params,function(data){
    			 $("#comments_list").empty();
    			 $("#comments_list").append(data);
    		});
     		if(id==null||id=='')return;
      		AT.post(webRootUR+"/noteController/front/loadNote.dht?id=" + id,null, function(data){
      			var note = data['note'];
      			var act = data['action'];
      			var attaList = data['attaList'];
      			var residential = data['residential'];

      		//条目位置
				$("#noteSubjectName").text(residential);
				
      			if(data['type']=='1'){
       				$("#note_blacklist").hide();
       				$("#note_edit").show();
       				$("#deleteNote_btn").show();
       				$("#note_share").show();
              	}else{
              		//如果是创建人 则有共享条目功能
           			if(note.createUserId == '${SESSION_USER_ATTRIBUTE.id}'){
           				$("#note_share").show();
               			$("#note_blacklist").show();
           			}else{
           				$("#note_share").hide();
           				$("#note_blacklist").hide();
           			}
           			if(act.UPDATE_NOTE == 'true'){
           				$("#note_edit").show();
           			}else{
           				$("#note_edit").hide();
           			}
           			if(act.DELETE_NOTE == 'true'){
           				$("#deleteNote_btn").show();
           			}else{
           				$("#deleteNote_btn").hide();
           			}
                }
                
      			$("#noteForm_id").val(note.id);
      			$("#noteTitleField").val(note.title);
      			$("#noteForm_version").val(note.version);
      			$("#noteForm_subjectId").val(note.subjectId);
      			$("#noteForm_dirId").val(note.dirId);
      			$("#noteForm_tagId").val(note.tagId);
      			if(ishiden){
      				$("#divhiden").text(note.content);
      				try{
 						UE.getEditor("note_editor").setContent(note.content);
      					UE.getEditor("note_editor").sync("noteForm");
 						}catch(err){
 						}
      			
      			}else{
      				UE.getEditor("note_editor").setContent(note.content);
      				UE.getEditor("note_editor").sync("noteForm");
      			}
      			
      			$("#readpng_" + id).attr("src", imgPath+"/read.png");
      			$("#note_status_" + id + " span").text("已读");
      			
      			//标签控制----------数据获取-隐藏选择功能----------
      			spellTag(note.subjectId,note.tagId); 
      			//附加控制----------数据获取-隐藏添加附件功能---------------
      			try{
	  				disableEditNote();
	  			}catch(err){
				}
      			
      			noteAttachment(attaList); 
      	    	$("#note_edit").removeAttr("disabled");
      	    	var node = zTree_Menu.getSelectedNodes()[0];
      			if(node.branchId == 'RECYCLE' || node.dataType == 'RECYCLE' || node.branchId == 'RECYCLEP' || node.dataType == 'RECYCLEP'){ //回收站下
      				$('#saveNote_btn').hide();
      				$("#note_edit").hide();
      				$("#restoreNote_btn").show();
      				$("#historyNote_btn").hide();
      				$('#attachment').hide();
      				$('#comments_div').hide();
      			}else{
      			    $("#comments_div").show();
      			}
      			$("#deleteNote_btn").removeAttr("disabled");
			},false);
    	}
//============过滤标签选择 END =================================================================================
   var r ;
   function ajaxData(result){
	   r = result;   
   }
   function showTag(obj){
		var note_id =$("#noteForm_id").val(); 
		if(note_id!=null&&note_id==''){
			MSG.alert("请选择条目!");
			return false;
		}
		var htm = '<div style="width:235px;height:310px;">';
		
		var params = {'parentid':$(obj).attr("tagid")==null?null:$(obj).attr("tagid"),"subjectid":$('#noteForm_subjectId').val()};   
		var url = webRootUR+"/noteController/front/getTag.dht";  
		
		myAjax(url,params,false);
		
		if(r!=null){
			$.each(r.data, function(i, objVal) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
			    var temp ='<span><input tagid="'+objVal.id+'" pid="'+($(obj).attr("tagid")!=null?$(obj).attr("tagid"):"")+'" onclick="selectedTag(this)" class="Button4" type="button" value="'+objVal.name+'"/></span>';
			    htm +=temp;
	        });  
		}

	     var myHearder = $(obj).html();
	     htm +="</div>";
	     htm +='<input type="input" value="" name="tagName" /><input class="Button1" type="button" value="添加"/>';
		 //打开标签选择窗口
	     easyDialog.open({
			 container : { 
			 	 header:myHearder,
			 	 content :htm
			 },
			 follow : obj, 
			 followY : 14 
		  }); 
	}
	
	function myAjax(url,params,async){
		 $.ajax({
	            type:"POST", 
	            url:url,
	            async:async,
	            data:params,
	            dataType:"json",
	            success:function(result){ 
	            	ajaxData(result);
	            }
	        }); 
	}
	//selectTag 当前选择的标签，  currentTags 当前标签的父标签
	function selectedTag(selectTag){ 
		//添加当前标签
		if($(selectTag).attr("pid")==null||$(selectTag).attr("pid")==''){
			$("#tagtemp").html("");
		}else{
		}
			$("#tagtemp").find("a[tagid='"+$(selectTag).attr("pid")+"']").nextAll().remove(); 
		$("#tagtemp").append('<a  href="javascript:void(0)" onclick="showTag(this)" tagid="'+$(selectTag).attr("tagid")+'"> > '+$(selectTag).val()+'</a>');
		easyDialog.close();
	}
		
    //=============================选择标签    start==================================
    //=============================条目分享    start================================== 
   function clickLi(currTrIndex){
         var prevTrIndex = $("#prevTrIndex").val();    
         if (currTrIndex > -1){    
                $("#li_" + currTrIndex).addClass("over");    
         }    
         $("#li_" + prevTrIndex).removeClass("over");    
         $("#prevTrIndex").val(currTrIndex); 
   } 
   $(document).click(function(){  
         $("#flttishi").hide();  
	     $("#emailMsgDiv").hide();
    }); 
   function sendMsg(msg){
		   if(msg=="true"){
			   msg = "<font color='green'>发送成功!</font>";
		   }		 
		   if(msg=="false"){
			   msg = "<font color='red'>发送失败!</font>";
		   }
		   $("#emailmsg").html(msg);
   } 
	//分享条目 
	function shareNote(){ 
		var note_id =$("#noteForm_id").val(); 
		if(note_id!=null&&note_id==''){
			MSG.alert("请选择条目!");
			return false;
		}
		var htm = 
				 '<div style="width:550px;height:300px;">'+
						'<form target="emailIframe"  action='+webRootUR+'/noteController/front/sendShareEmail.dht" method="post">'+
								'<input type="hidden" value="'+note_id+'" name="noteid" />'+
								'<input type="text" id="emailField" class="InputTxt3"  value=""  style=" width:65%; "/>'+
							 	'<input class="Button4" type="submit"  value="发送" />'+
							 	'<span id="emailmsg"></span>'+
							 	'<table width=100% ><tr><td width="80%" valign="top"><div style="height:295px;overflow:auto;">'+
							 		'<span  id="tempdiv"></span>'+
						 		'</div></td>'+
			     				'<td  valign="top"><div style="width:100%;height:295px;overflow:auto;"></div></td></tr>'+
		 				'</form>'+
	 			 '</div>'+
		 		'<iframe style="display:none" name="emailIframe" src="${frontPath}/note/emailStatus.jsp"></iframe>';
			easyDialog.open({
				container : {
					header : '<img src='+imgPath+'/email_go.png" height="20"/>',
					content : htm
				},
				lock : true,
				follow : 'saveNote_btn',
				followX : 0,
				followY : 34
			}); 
		//绑定输入窗口事件
        $("#emailField").keyup(function(evt){ 
            if(evt.keyCode==38||evt.keyCode==40||evt.keyCode==37||evt.keyCode==39){ return;}  
            //回车添加当前输入框的值
            if(evt.keyCode==13){
            	if(checkEmail($("#emailField"),$("#emailmsg"))){
	           	var addtemp = "<span><input type='hidden' name='shareEmails' value='"+$("#emailField").val()+"' > "+$("#emailField").val()+" <img src='${imgPath}/34aL_046.png' onclick='cancelEmail(this)' > </span></br>";
	          		$("#tempdiv").prepend(addtemp);
	          		$("#emailField").val("");
            	}
            }
            //var dataParam = "searchField="+$("#emailField").val();

          	 var url = webRootUR+"/noteController/front/getGroupsEmail.dht"; 
             var params = {'searchField':$("#emailField").val()}; 
             getShareEmail(url,params);
             //绑定鼠标移动效果 
   	        if($("#flttishi").html()!=''){
                $("#flttishi").show();  
			     var height = $(this).height();//按钮的高度
			     var top = $(this).offset().top;//按钮的位置高度
			     var left = $(this).offset().left;//按钮的位置左边距离
			     //设置div的top left
			     $("#emailMsgDiv").css("left",left);
			     $("#emailMsgDiv").css("top",height+top+3); 
			     $("#emailMsgDiv").show();  
   	        }
            bindMouseOverLi();     
        }).keydown(function(evt){
        	 if(evt.keyCode==13){
        		 return false;
        	 }
        }); 
	} 
	function cancelEmail(o){  
		 $(o).parent().remove();
	}
	//jquery验证邮箱
    function checkEmail(email,msg){
       if($(email).val()==null||$(email).val()==""){
    	    msg.html("<font color='red'>不能为空！</font>"); 
		    return false;
	   }
	   if(!$(email).val().match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/)){
		    $(msg).html("<font color='red'>格式不正确！</font>");
		    $(email).focus();
		    return false;
	   }
	   msg.html(""); 
	   return true;
	}
	//绑定鼠标效果事件
	function bindMouseOverLi(){
        $("#prevTrIndex").val("-1");//默认-1     
        $("#flttishi li").mouseover(function(){//鼠标滑过    
                $(this).addClass("over");  
            }).mouseout(function(){ //鼠标滑出    
                $(this).removeClass("over");})    
            .each(function(i){ //初始化 id 和 index 属性    
                $(this).attr("id", "li_" + i).attr("index", i);})    
            .click(function(){ //鼠标单击（添加当前选择的目标）
            	$("#emailMsgDiv").hide(); 
           		var addtemp = "<span><input type='hidden' name='shareEmails' value='"+$(this).find("input[name='shareEmails']").val()+"' > ["+$(this).find("input[name='shareName']").val()+"]<font color='#DADADA'>"+$(this).find("input[name='shareEmails']").val()+"</font> <img src='${imgPath}/34aL_046.png' onclick='cancelEmail(this)' > </span>";
           		$("#tempdiv").prepend(addtemp);
               	$("#emailField").val("");	
            });  
       clickLi(0); 
	}
	//获取共享邮件
    function getShareEmail(url,params){
        $.ajax({
            type:"POST", 
            url:url,
            data:params,
            dataType:"json",
            success:function(result){
                //新元素重新绑定
	            if(result.data==null||result.data.length<=0){   
	                 $("#flttishi").hide();  
	                 $("#flttishi").html("");;  
	    		     $("#emailMsgDiv").hide();
        		}else{
        			 //var jsonData = eval(result);//接收到的数据转化为JQuery对象，由JQuery为我们处理  
        			 var str ="";
        			 $.each(result.data, function(index, objVal) { //遍历对象数组，index是数组的索引号，objVal是遍历的一个对象。  
        			    var temp ="<li><input type='hidden' name='shareEmails' value='"+objVal.email+"'> <input type='hidden' name='shareName' value='"+objVal.name+"'> "+"["+objVal.name+"]"+objVal.email+"</li>";
        			    str +=temp;
                     });
                    $("#flttishi").html(str);
	                bindMouseOverLi();
        		}
            }
        }); 
    }
	
//===========================条目共享   end=========================
	//删除条目按钮事件
	function deleteNote(){

		var submit = function (v, h, f) {
		    if (v == true){
				var deleted = 0;
				if($("#restoreNote_btn").is(":visible")){
					deleted = 1;	
				}
				var url = webRootUR+"/noteController/front/deleteNote.dht?id=" + $("#noteForm_id").val() + "&deleted=" + deleted;
				AT.post(url,null, function(data){
					$("#noteForm_id").val("");
					$("#noteTitleField").val("");
					noteEditor.setContent("");
					noteEditor.sync("noteForm");
					//重置附件
					flushAttachment_hiden();
					//重置标签
					flushTag_empty();
					searchNotes(deleted);
				});
				disableEditNote();
		    }
		    return true;
		}; 
		$.jBox.confirm("您确定删除？", "提示", submit, { buttons: { '确定': true, '取消': false} })
		
	}
	//编辑条目按钮事件
	function enableEditNote(){
		if($("#note_edit").hasClass("Button4")){
			$('#saveNote_btn').show();
			// 标题输入框
			if(typeof($("#noteTitleField").attr("disabled")) != "undefined"){
				$("#noteTitleField").removeAttr("disabled");
			}
			// 内容编辑器
			noteEditor.setEnabled();
			
			// 提交按钮
			if(typeof($("#saveNote_btn").attr("disabled")) != "undefined"){
				$("#saveNote_btn").removeAttr("disabled");
			}
			//条目ID有值时，删除按钮才有用
			if($("#noteForm_id").val() != null && $("#noteForm_id").val() != ''){
				$("#deleteNote_btn").removeAttr("disabled");
			}else{
				$("#deleteNote_btn").attr("disabled", "disabled");
			}
			
			$("#note_edit").attr("class", "Button3");
			$("#note_edit").val("返回阅读");
			$("#edui1_toolbarboxouter").children(".edui-editor-toolbarboxinner").show();
			//noteEditor.setHeight(editorHeight - $("#edui1_toolbarboxouter").height());
			//$("#comments_div").hide();//隐藏评论

  			//标签控制显示选择功能----------
  			$("#selectTag").show();
  			//附加控制显示添加附件功能---------------
  			$("#attachment").show();
		}else if($("#note_edit").hasClass("Button3")){
			$('#saveNote_btn').hide();
			disableEditNote();
		}
	}
	//设置条目只读状态
	function disableEditNote(){
		// 标题输入框
		$("#noteTitleField").attr("disabled", "disabled");
		
		// 内容编辑器
		noteEditor.setDisabled();
		
		// 提交按钮
		$("#saveNote_btn").attr("disabled", "disabled");
		//条目ID有值时，删除按钮才有用
		if($("#noteForm_id").val() != null && $("#noteForm_id").val() != ''){
			$("#deleteNote_btn").removeAttr("disabled");
		}else{
			$("#deleteNote_btn").attr("disabled", "disabled");
		}
		$("#note_edit").attr("class", "Button4");
		$("#note_edit").val("编辑条目");
		$("#edui1_toolbarboxouter").children(".edui-editor-toolbarboxinner").hide();
		//noteEditor.setHeight(editorHeight);
		//显示评论
		//$("#comments_div").show();
		//隐藏标签功能
		$("#selectTag").hide();
		//隐藏上传附件功能
		$("#attachment").hide();
	}
	function historyNote(){
		var note_id =$("#noteForm_id").val(); 
		if(note_id!=null&&note_id==''){
			MSG.alert("请选择条目!");
			return false;
		}
		 var params = {'nodeId':note_id};  
		 AT.post(webRootUR+"/noteController/front/historyNote.dht",params,function(data){
			 easyDialog.open({
					container : {
						header : '<img src='+imgPath+'/note_ico.png" height="20"/>',
						content : data
					},
					follow : 'saveNote_btn',
					followX : 0,
					followY : 34
				}); 
		});
        
	}

	 function noteblacklist(){
		var note_id =$("#noteForm_id").val(); 
		if(note_id!=null&&note_id==''){
			MSG.alert("请选择条目!");
			return false;
		}
		 var params = {'nodeId':note_id,"subjectid":$('#noteForm_subjectId').val()};
		 AT.post(webRootUR+"/noteController/front/blackListNote.dht",params,function(data){
			 easyDialog.open({
					container : {
						header : '<img src='+imgPath+'/mail_blue.png"  height="23"  >',
						content : data
					},
					follow : 'saveNote_btn',
					followX : 0,
					followY : 34
				}); 
		});
	}
	noteEditor.addListener("ready", function(){
		// 默认为个人专题第一个专题
		if($("#noteForm_subjectId").val() == ""){
			var subjectId = curMenu.children[0].id;
			$("#noteForm_subjectId").val(subjectId);
			//noteEditor.setContent($("#noteContentField").val());
		}
		//设置条目只读状态
		disableEditNote();
		noteEditor.setContent($("#divhiden").text());
	});
	//分页
	function doPageHistoryNoteList(ths,pageNo,pageSize){
		 AT.post(webRootUR+"/noteController/front/blackListNote.dht",$("#blackListNoteForm").serialize(),function(data){
			 easyDialog.open({
					container : {
						header : '<img src='+imgPath+'/mail_blue.png"  height="23"  >',
						content : data
					},
					follow : 'saveNote_btn',
					followX : 0,
					followY : 34
				}); 
		});
	}

	//分页
	function doPageblackList(ths,pageNo,pageSize){
		var historyNoteId=$("#historyNoteId").val();
		var params = {'nodeId':historyNoteId,'pageNo':pageNo};  
		
		 AT.post(webRootUR+"/noteController/front/historyNote.dht",params,function(data){
			 easyDialog.open({
					container : {
						header : '<img src='+imgPath+'/note_ico.png">',
						content : data
					},
					follow : 'saveNote_btn',
					followX : 0,
					followY : 34
				}); 
		});
	}
	  
	function shapeNote(obj){
		var params = {'id':obj};
		
        if(confirm("确定还原条目?")){
		 AT.post(webRootUR+"/noteController/front/shapeNote.dht",params,function(data){
				if(data.success){
					 MSG.alert(data.msg);
					     if(data.obj==null){
					    	 noteEditor.setContent("");
						 }else{
							 noteEditor.setContent(data.obj);
							 }
					 
				}else{
					 MSG.alert(data.msg);
				}	
		});
        }
	}
	
	function addBlackListNote(userId,nodeId,obj){
		var params = {'userId':userId,'nodeId':nodeId};
		
	    if(confirm("确定将此用户"+obj.value+"?")){
		 AT.post(webRootUR+"/noteController/front/addblackListNote.dht",params,function(data){
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
	//添加评论
	function addComment(){
		 var noteId=$("#noteId").val();
		AT.postFrm("addCommentForm",function(data){
			if(data.success){  
				MSG.alert(data.msg);
				 var params = {'noteId':noteId};  
	    		 AT.post(webRootUR+"/commentController/front/findCommentByNote.dht",params,function(data){
	    			 $("#comments_list").empty();
	    			 $("#comments_list").append(data);
	    		});
			}else{
				MSG.alert(data.msg);
			}
		},true);
	}

	function delComment(obj){
			 var noteId=$("#noteId").val();
			 var paramsdel = {'Id':obj};
			 AT.post(webRootUR+"/commentController/front/commentDel.dht",paramsdel,function(data){
				 if(data.success){  
						MSG.alert(data.msg);
						 var params = {'noteId':noteId};  
			    		 AT.post(webRootUR+"/commentController/front/findCommentByNote.dht",params,function(data){
			    			 $("#comments_list").empty();
			    			 $("#comments_list").append(data);
			    		});
				}else{
					MSG.alert(data.msg);
				}
			});
		}