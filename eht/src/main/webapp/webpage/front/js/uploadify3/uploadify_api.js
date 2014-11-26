/**
 * 绑定上传按钮
 * btnId: 自定义按钮的ID
 * options json对象，可选配置参数如下：
 * fileExt: string，设置上传文件类型，如'*.jpg;*.gif;*.png;'
 * fileDesc: string，对文件类型的描述，当fileExt属性设置了之后必须设置这个属性
 * multi: boolean，设置是否支持多文件上传，默认为false
 * maxCount: integer，设置一次最多可上传的文件数，默认为999，这个属性必须要求multi为true才有效 
 * maxSize: integer，设置单个上传文件的最大byte值，单位为byte，默认50*1024*1024
 * callback: function，文件上传完成后调用此函数，传入服务端返回的数据，如果返回的数据可转换成json格式，将转换json对象
 * 服务务端返回的json格式应当有如下内容：
 * {success:true,filename:'',filepath:'',msg:''}
 */
 function bindUploadBtn(btnId,options,config){
	var time=(new Date()).getTime(),uploadQueueId="upload_queue_"+time,
	fileInputId="file_input_"+time;
	//配置上传插件选项
	options=options||{};
	var basePath="";
	var scripts = document.getElementsByTagName("script");
	for (var i = scripts.length; i > 0; i--) {
		var index = scripts[i - 1].src.lastIndexOf("uploadify_api.js");
	    if (index > -1) {
	        basePath = scripts[i - 1].src.substring(0, index);
	        break;
	    }
	}
	basePath = options.basePath;
	var uploadBtn=$("#"+btnId);
	uploadBtn.parent().append("<div id='"+uploadQueueId+"'></div>");
	$("#"+uploadQueueId).css('background-color','#ffffff');
	var defaultCfg={ 
		//'method'    : 'GET',
		'uploader'  : basePath+'/uploadify.swf',
		'script'    : options.uploadPath,
		'scriptData':{'noteid':$('#noteForm_id').val(),'jsessionid':options.sessionId,'userId':options.userId},
		'cancelImg' : basePath+'/cancel.png',
		'wmode'     : 'transparent',
		'removeCompleted':true,
		'auto'      : true,
		'width'     : uploadBtn.outerWidth(),
		'height'    : uploadBtn.outerHeight(),
		'sizeLimit' : 100*1024*1024*1024,
		'hideButton': true,
		'queueID'   : uploadQueueId,
		'buttonText': '测试',
		//'checkScript':"checkScript.dht",
		'onSelect': function(event, queueID, fileObj) {
			if(fileObj.size > options.maxSize){
                alert('当前选择的文件超过了设置的大小，请重新选择文件！');
                try{
                	document.getElementById(jQuery(event.target).attr('id') + 'Uploader').cancelFileUpload(fileObj.name,true,true,false);
                }catch(e){}
				$('#'+fileInputId).uploadifyCancel(event.data.queueID);
            }
			if(".exe.com.bat.sh".indexOf(fileObj.type)>=0){
				try{
					document.getElementById(jQuery(event.target).attr('id') + 'Uploader').cancelFileUpload(fileObj.name,true,true,false);
				}catch(e){}
				alert('您不能上传后缀为.exe .com .bat .sh的文件！');
				$('#'+fileInputId).uploadifyCancel(event.data.queueID);
			}
			
			var noteForm_id = "";
			var dirId = "";
			var subjectId ="";
			if($('#noteForm_id')!=null&&$('#noteForm_id').val()!=null){
				noteForm_id = $('#noteForm_id').val();
			}
			if($('#dirId')!=null&&$('#dirId').val()!=null){
				dirId = $('#dirId').val();
			}
			if($('#noteForm_subjectId')!=null&&$('#noteForm_subjectId').val()!=null){
				subjectId = $('#noteForm_subjectId').val();
			}
            $('#'+fileInputId).uploadifySettings("scriptData",{"noteid":noteForm_id,"dirId":dirId,"jsessionid":options.sessionId,"subjectId":subjectId}); //动态更新配(执行此处时可获得值)
        },
		'onQueueFull':function(event,queueSizeLimit){//超出文件个数
			alert("已经超过上传文件数限制(最大上传文件数"+queueSizeLimit+"个)");
			return false;
		},
		'onComplete':function(event,itemId,fileObj,response,data){//上传完成之后触发
			if(config&&config.onComplete){
				try{
					config.onComplete(event,itemId,fileObj,response,data)
				}catch(e){
				}
			}else{
				try{
					if(typeof(response)=='string'){
						//去两端空格+
						response=response.replace(/^\s+|\s+$/g,"");
						if(/^\{.*\}$/.test(response)){
							response=eval("("+response+")");
						}
					}
					options.callback(response);
				}catch(e){
				}
			}
		},
		'onCancel':function(event,ID,fileObj,data){
			//alert("push");
			return true;
		},
		'onError' : function (event,itemId,fileObj, errorObj) {
		   if (errorObj.type === "File Size"){
			    alert('超过文件上传大小限制！');
			    try{
			    	alert("itemId=="+itemId);
			    	for(var i in fileObj){
			    		alert(i+"----"+fileObj[i]);
			    	}
			    	$("#"+fileInputId).uploadifyCancel(fileObj);
			    }catch(e){alert(e)}
		   } 
		}
	};
	//设置最大上传个数
	if(options.maxCount){
		options.queueSizeLimit=options.maxCount;
		delete options.maxCount;
	}
	//设置上传单个文件大小单位byte
	if(options.maxSize){
		options.sizeLimit=options.maxSize;
		delete options.maxSize;
	}
	//创建上传插件对象 
	options=$.extend(defaultCfg,options);
	var uploadWrap=$("<div class='upload_btn_wrap'></div>").insertBefore(uploadBtn);
	uploadWrap.append(uploadBtn).append("<input type='file' id='"+fileInputId+"' />");
	$("#"+fileInputId).uploadify(options);
	
	//把文件框移除到表单外面，防止jquery form plugin自动把表单改成上传数据类型造成服务端不能解析
	var getParentForm=function(el){
		var parent=el.parentNode;
		if(parent.nodeName=="BODY"){
			parent=null;
		}else if(parent.nodeName!="FORM"){
			parent=getParentForm(parent);
		} 
		return parent;
	} 
	var fileInput=document.getElementById(fileInputId);
	var parentForm=getParentForm(fileInput);
	if(parentForm!=null){
		parentForm.parentNode.insertBefore(fileInput,parentForm);
	}
}

function uploadBtnClick(btnId){
	$("#"+btnId).uploadifyUpload();
}
/*
 * 多文件上传
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 * basePath : 基本路径
 * upfileButton: 上传按钮
 */
function MultiUpload(elId,inputFileName,downloadPath,uploadPath,basePath,sessionId,userId, upfileButton){
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	if(upfileButton==null){
		upfileButton = " <a href='javascript:void(0);' style='border:1px solid #fff;z-index:0'>添加附件 </a>";
	}
	var container=$("#"+elId),
		uploadBtn=$(upfileButton);
		uploadBtn.attr("id",btnId);
	    fileList=$("<div id='currAttachment1' style='padding-left:1px'></div>");
		container.empty();
		$("#attachment").append(uploadBtn);
		container.append(fileList);
		nBindUploadBtn(btnId,{
			basePath:basePath,
			uploadPath:uploadPath,
			multi:true,
			sessionId:sessionId,
			userId:userId,
			maxCount:100,
			maxSize:300*1024*1024,
			callback:function(result){
				if(result.success){
					mupload.addFile(result.filename,result.filecode,downloadPath);
				}else{
					if(result.msg!=null){
						alert(result.msg+"result.msg");
					}
				}
		}
	});
	this.addFile=function(filename,filecode,downloadPath){
		var fileItem=$("<span style='display: inline-block;margin-right:20px;'></span>");
		var attaStr = fileName!=null&&fileName.length>8?fileName.substring(0,7)+'...':fileName;
		fileItem.append(
			"<input type='hidden' name='"+inputFileName+"' value='"+filecode+"' />"+
			"<span onclick='downloadByid(\""+filecode+"\")'  title='"+fileName+"'><a href='javascript:;' style='color: #1F8919;'>"+attaStr+"</a>&nbsp;</span>"
		);
		var delBtn=$("<span onclick='removeCurrAttachment(this)'   attaid='"+filecode+"'><img style='display:inline;' src='"+imgPath+"/34aL_046.png' ></span>").click(function(){
			fileItem.remove();
		});
		fileItem.append(delBtn);
		fileList.prepend(fileItem);
	}
} 

// 上传条目附件
function nBindUploadBtn(btnId,options,config){
	$("#"+btnId).uploadify({
		auto		  : true,
		height        : 18,
		swf           : options.basePath+'/js/uploadify3/uploadify.swf',
		width         : 18,
		buttonClassOut: "outButtonDiv",
		buttonText    : '',
		buttonClass   : "upload-sa",
		buttonImage	  : options.basePath+'/js/uploadify3/button_1.png',
		uploader      : options.uploadPath,
		queueSizeLimit: 10,
		onSelect:function(file){
			enableEditNoteT();
			var disableType=new Array();
			disableType["exe"]=true;
			disableType["com"]=true;
			disableType["bat"]=true;
			disableType["sh"]=true;
			var fileName=file.name;
			fileName=fileName.toLocaleLowerCase();
			var terms=fileName.split("\.");
			var type="";
			if(fileName.indexOf(".")>-1){
				var terms=fileName.split("\.");
				type=terms[terms.length-1];
			}
			type=$.trim(type);
			if(disableType[type]){
				MSG.alert("您不能上传后缀为.exe .com .bat .sh的文件！");
				$("#"+btnId).uploadify("cancel",file.id);
				return ;
			}
			var sessionId=options.sessionId;
			var userId = options.userId;
			var noteid=$('#noteForm_id').val();
			var dirId=$('#noteForm_dirId').val();
			//var formData={"noteid":noteid,"jsessionid":sessionId};
			var url=options.uploadPath+"?noteid="+noteid+"&userId=" +userId+ "&jsessionid="+sessionId+"&dirId="+dirId;
			//显示进度条，值过小会被UEDITOR挡住
			$("#attTemp").css("z-index", "9999");
			$("#"+btnId).uploadify("settings","uploader",url,true);
		},
		onUploadComplete:function(file){
			//var noteid=$('#noteForm_id').val();
		},
		onQueueComplete:function(){
			loadAttachment("current");
			loadAttachment("all");
			
			//隐藏进度条，值过大会在遮住UEDITOR的上传图片弹出框
			$("#attTemp").css("z-index", "99");
		}
		
	});
}

function downloadByid(id){
	 var url =window.DOWNLOAD_URL+"?id="+id;
	 window.open (url);
}


/*
 * 多文件上传
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 */
function MultiUploadIMG(elId,inputFileName,inputPathName,relativePath ){
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<a href='javascript:;' id='"+btnId+"' style='border:1px solid #fff;'>添加附件</a>"),
	    fileList=$("<div></div>");
	container.empty();
	$("#attachment").append(uploadBtn);
	container.append("<span style='color:#ff0000;'>&nbsp;一次可选择20个文件上传，每个文件限制20M之内,支持格式[.jpg;.jpeg;.gif;.png;.bmp]</span>");
	container.append(fileList);
	bindUploadBtn(btnId,{
		multi:true,
		maxCount:20,
		fileExt:'*.jpg;*.jpeg;*.gif;*.png;*.bmp',
		fileDesc:'*.*',
		maxSize:2000*1024*1024,
		callback:function(result){
			if(result.success){
				mupload.addFile(result.filename,result.filepath);
			}else{
				alert(result.msg);
			}
		}
	});
	this.addFile=function(filename,filepath){
		var fileItem=$("<span style='display: inline-block;margin-right:20px;'></span>");
		fileItem.append(
			"<input type='hidden' name='"+inputFileName+"' value='"+filename+"' />"+
			"<input type='hidden' name='"+inputPathName+"' value='"+filepath+"' />"+
			"<a href='"+relativePath+filepath+"' target='_blank' style='color: #1F8919;'>"+filename+"</a>&nbsp;"
		);
		var delBtn=$("<img src='"+imgPath+"/34aL_046.png' >").click(function(){
			fileItem.remove();
		});
		fileItem.append(delBtn);
		fileList.append(fileItem);
	}
}