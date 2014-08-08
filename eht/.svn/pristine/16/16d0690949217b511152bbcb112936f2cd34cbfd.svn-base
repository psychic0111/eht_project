/*
 * 多文件上传
 * elId : dom元素ID
 * inputFileName : 设置保存文件名的inputName　
 * inputPathName : 设置保存文件路径的inputName　
 * relativePath  : 相对于uploadfiles目录的路径
 * basePath : 基本路径
 */
function MultiUploadAttachment(elId,inputFileName,downloadPath,uploadPath,basePath,initPath,sessionId,completeFunc){
	var mupload=this;
	var filecode;
	var btnId="btn"+(new Date()).getTime();
	var container=$("#"+elId),
		//uploadBtn=$("<div><a  class='Button4' href='javascript:$(\'#"+btnId+"\').uploadifySettings('scriptData',{'studentId':${student.userId}});$('#"+btnId+"').uploadifyUpload()' id='"+btnId+"'>添加附件</a></div>"),
		uploadBtn=$("<div><a  class='Button4' href='javascript:;' id='"+btnId+"'>添加附件</a></div>"),
	    fileList=$("<div id='currAttachment1'></div>");
		container.empty();
		container.append(uploadBtn);
		container.append(fileList);
		var config=null;
		if(completeFunc){
			config={"onComplete":completeFunc};
		}
		bindUploadBtn(btnId,{
			basePath:basePath,
			uploadPath:uploadPath,
			multi:true,
			sessionId:sessionId,
			maxCount:100,
			maxSize:1024*1024*1024*1024,
			callback:function(result){
				if(result.success){
					mupload.addFile(result.filename,result.filecode,downloadPath,initPath);
				}else{
					alert(result.msg);
				}
		}
	},config);
	this.addFile=function(filename,filecode,downloadPath,initPath){ 
			var url = currentdirAttachmentURL+"?dirId=" + currentDirId + "&subjectId=" + currentSubjectId+"&ispage=true";
			AT.load("datadiv",url,iframeHeight);
	}
}

function downloadByid(id){
	 var url =window.DOWNLOAD_URL+"?id="+id;
	 window.open (url);
}