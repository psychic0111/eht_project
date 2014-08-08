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
 function bindUploadBtn(btnId,options){
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
	alert(btnId+"join");
	var uploadBtn=$("#"+btnId); 
	uploadBtn.parent().append("<div id='"+uploadQueueId+"'></div>");
	var defaultCfg={

            'swf'            : basePath+'/uploadify.swf',  
            'uploader'       : options.uploadPath,//后台处理的请求  
            'queueID'        : btnId,//与下面的id对应  
            'queueSizeLimit' : 5,  
            'auto'           : true,  
            'fileTypeDesc'   : 'rar文件或zip文件',  
            'fileTypeExts'   : '*.rar;*.zip;', //控制可上传文件的扩展名，启用本项时需同时声明fileDesc  
            'multi'          : true,  
            'buttonText'     : '上传附件'  ,
			
			
		/*'uploader'  : basePath+'/uploadify.swf',
		'script'    : '/upload/addAttachment.do',
		'cancelImg' : basePath+'/img/uploadify-cancel.png',
		'wmode'     : 'transparent',
		'removeCompleted':true,
		'auto'      : true,
		'width'     : uploadBtn.outerWidth(),
		'height'    : uploadBtn.outerHeight(),
		'sizeLimit' : 1*1024*1024,
		'hideButton': true,
		'queueID'   : uploadQueueId,*/
		'onQueueFull':function(event,queueSizeLimit){
			alert("已经超过上传文件数限制(最大上传文件数"+queueSizeLimit+"个)");
			return false;
		},
		'onComplete':function(event,itemId,fileObj,response,data){
			if(typeof(response)=='string'){
				//去两端空格
				response=response.replace(/^\s+|\s+$/g,"");
				if(/^\{.*\}$/.test(response)){
					response=eval("("+response+")");
				}
			}
			options.callback(response);
		}
	};
	if(options.maxCount){
		options.queueSizeLimit=options.maxCount;
		delete options.maxCount;
	}
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
/*
 * 多文件上传
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 * basePath : 基本路径
 */
function MultiUpload(elId,inputFileName,downloadPath,uploadPath,basePath ){
	var mupload=this;
	var filecode;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<div><a href='javascript:;'id='"+btnId+"' style='border:1px solid #fff;z-index:0'>添加附件</a></div>"),
	    fileList=$("<div></div>");
		container.empty();
		container.append(uploadBtn);
		container.append("<span style='color:#ff0000;'>&nbsp;一次可选择5个文件上传，每个文件限制20M之内</span>");
		container.append(fileList);
		
		bindUploadBtn(btnId,{
			basePath:basePath,
			uploadPath:uploadPath,
			multi:true,
			maxCount:5,
			maxSize:2000*1024*1024,
			callback:function(result){
				if(result.success){
					mupload.addFile(result.filename,result.filecode,downloadPath);
				}else{
					alert(result.msg);
				}
		}
	});
	this.addFile=function(filename,filecode,downloadPath){ 
		var fileItem=$("<span style='display: inline-block;margin-right:20px;'></span>");
		fileItem.append(
			"<input type='hidden' name='"+inputFileName+"' value='"+filecode+":"+filename+"' />"+
			"<a href='"+downloadPath+"?filecode="+filecode+"&filename="+filename+"' target='_blank'>"+filename+"</a>&nbsp;"
		);
		var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
			fileItem.remove();
		});
		fileItem.append(delBtn);
		fileList.append(fileItem);
	}
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
	container.append(uploadBtn);
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
		var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
			fileItem.remove();
		});
		fileItem.append(delBtn);
		fileList.append(fileItem);
	}
}