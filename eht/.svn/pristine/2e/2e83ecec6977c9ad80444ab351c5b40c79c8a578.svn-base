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
		var index = scripts[i - 1].src.lastIndexOf("uploadify_one.js");
	    if (index > -1) {
	        basePath = scripts[i - 1].src.substring(0, index);
	        break;
	    }
	}
	var uploadBtn=$("#"+btnId);
	uploadBtn.parent().append("<div id='"+uploadQueueId+"'></div>");
	var defaultCfg={
		'uploader'  : basePath+'uploadify.swf',
		'script'    : basePath+'uploadfile.jsp',
		'cancelImg' : basePath+'cancel.png',
		'wmode'     : 'transparent',
		'removeCompleted':true,
		'auto'      : true,
		'width'     : uploadBtn.outerWidth(),
		'height'    : uploadBtn.outerHeight(),
		'sizeLimit' : 20*1024*1024,
		'hideButton': true,
		'queueID' : uploadQueueId,
		'onQueueFull':function(event,queueSizeLimit){
			alert("已经超过上传文件数限制(最大上传文件数"+queueSizeLimit+"个)");
			return false;
		},
		'onSelect':function(event,itemId,fileObj){
			var t_num=new Array();
			t_num=document.getElementsByName('filename');
			if(t_num.length>=1){
				alert('已经超过上传文件数限制(最大上传文件数 1 个)');
				return false;
			}
			if(fileObj.size>20*1024*1024){
				alert("已经超过上传文件大小限制(最大上传文件大小 "+(options.sizeLimit)/(1024*1024)+" MB)");
				return false;
			}
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
 * 单文件上传 
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 */
function MultiUpload(elId,inputFileName,inputPathName,relativePath ){
	var bflag=true;
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<a href='javascript:;' id='"+btnId+"' style='border:1px solid #fff;'>&nbsp;添加附件</a>"),
	    fileList=$("<div></div>");
	container.empty();
	container.append(uploadBtn);
	//container.append("<span style='color:#ff0000;'>&nbsp;上传单个附件；附件大小限制为：20M </span>");
	container.append(fileList);
	bindUploadBtn(btnId,{
		multi:false,
		maxCount:1,
		maxSize:20*1024*1024,
		callback:function(result){
			if(result.success){
				mupload.addFile(result.filename,result.filepath);
			}else{
				alert(result.msg);
			}
		}
	});
	this.addFile=function(filename,filepath){
		if(bflag){
			var fileItem=$("<span style='display: inline-block;margin-right:20px;margin-top:5px;'></span>");
			fileItem.append(
				"<input type='hidden' name='"+inputFileName+"' value='"+filename+"' />"+
				"<input type='hidden' name='"+inputPathName+"' value='"+filepath+"' />"+
				"<a href='"+relativePath+filepath+"' target='_blank' style='color: #1F8919;'>"+filename+"</a>&nbsp;"
			);
			var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
				bflag=true;
				fileItem.remove();
			});
			fileItem.append(delBtn);
			fileList.append(fileItem);
		}
		bflag=false;
	}
}



/*
 * 单文件上传HTML 
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 */
function MultiUploadHTML(elId,inputFileName,inputPathName,relativePath ){
	var bflag=true;
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<a href='javascript:;' id='"+btnId+"' style='border:1px solid #fff;'>&nbsp;添加附件123</a>"),
	    fileList=$("<div></div>");
	container.empty();
	container.append(uploadBtn);
	//container.append("<span style='color:#ff0000;'>&nbsp;上传单个附件[.html;.htm]；附件大小限制为：20M </span>");
	container.append(fileList);
	bindUploadBtn(btnId,{
		multi:false,
		maxCount:1,
		fileExt:'*.html;*.htm;',
		fileDesc:'*.*',
		maxSize:20*1024*1024,
		callback:function(result){
			if(result.success){
				mupload.addFile(result.filename,result.filepath);
			}else{
				alert(result.msg);
			}
		}
	});
	this.addFile=function(filename,filepath){
		if(bflag){
			var fileItem=$("<span style='display: inline-block;margin-right:20px;margin-top:5px;'></span>");
			fileItem.append(
				"<input type='hidden' name='"+inputFileName+"' value='"+filename+"' />"+
				"<input type='hidden' name='"+inputPathName+"' value='"+filepath+"' />"+
				"<a href='"+relativePath+filepath+"' target='_blank' style='color: #1F8919;'>"+filename+"</a>&nbsp;"
			);
			var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
				bflag=true;
				fileItem.remove();
			});
			fileItem.append(delBtn);
			fileList.append(fileItem);
		}
		bflag=false;
	}
}

/*
 * 单文件上传 Excel
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 */
function MultiUploadExcel(elId,inputFileName,inputPathName,relativePath ){
	var bflag=true;
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<a href='javascript:;' id='"+btnId+"' style='border:1px solid #fff;'>&nbsp;添加附件</a>"),
	    fileList=$("<div></div>");
	container.empty();
	container.append(uploadBtn);
	container.append("<span style='color:#ff0000;'>&nbsp; 支持标准格式Excel2003数据导入上传");
	container.append(fileList);
	bindUploadBtn(btnId,{
		multi:false,
		maxCount:1,
		fileExt:'*.xls;',
		fileDesc:'*.*',
		maxSize:20*1024*1024,
		callback:function(result){
			if(result.success){
				mupload.addFile(result.filename,result.filepath);
			}else{
				alert(result.msg);
			}
		}
	});
	this.addFile=function(filename,filepath){
		if(bflag){
			var fileItem=$("<span style='display: inline-block;margin-right:20px;margin-top:5px;'></span>");
			fileItem.append(
				"<input type='hidden' name='"+inputFileName+"' value='"+filename+"' />"+
				"<input type='hidden' name='"+inputPathName+"' value='"+filepath+"' />"+
				"<a href='"+relativePath+filepath+"' target='_blank' style='color: #1F8919;'>"+filename+"</a>&nbsp;"
			);
			var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
				bflag=true;
				fileItem.remove();
			});
			fileItem.append(delBtn);
			fileList.append(fileItem);
		}
		bflag=false;
	}
}

/*
 * 单文件上传 Excel 支持Excel2003/2007
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 */
function MultiUploadExcels(elId,inputFileName,inputPathName,relativePath ){
	var bflag=true;
	var mupload=this;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
	    uploadBtn=$("<a href='javascript:;' id='"+btnId+"' style='border:1px solid #fff;'>&nbsp;添加附件</a>"),
	    fileList=$("<div></div>");
	container.empty();
	container.append(uploadBtn);
	container.append("<span style='color:#ff0000;'>&nbsp; 支持标准格式Excel数据导入上传");
	container.append(fileList);
	bindUploadBtn(btnId,{
		multi:false,
		maxCount:1,
		fileExt:'*.xls;*.xlsx;',
		fileDesc:'*.*',
		maxSize:20*1024*1024,
		callback:function(result){
			if(result.success){
				mupload.addFile(result.filename,result.filepath);
			}else{
				alert(result.msg);
			}
		}
	});
	this.addFile=function(filename,filepath){
		if(bflag){
			var fileItem=$("<span style='display: inline-block;margin-right:20px;margin-top:5px;'></span>");
			fileItem.append(
				"<input type='hidden' name='"+inputFileName+"' value='"+filename+"' />"+
				"<input type='hidden' name='"+inputPathName+"' value='"+filepath+"' />"+
				"<a href='"+relativePath+filepath+"' target='_blank' style='color: #1F8919;'>"+filename+"</a>&nbsp;"
			);
			var delBtn=$("<a href='javascript:;'>删除</a>").click(function(){
				bflag=true;
				fileItem.remove();
			});
			fileItem.append(delBtn);
			fileList.append(fileItem);
		}
		bflag=false;
	}
}