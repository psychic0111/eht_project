<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@page import="com.eht.common.constant.*"%>
<%@ taglib prefix="t" uri="/easyui-tags"%>
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
	String webpagePath = basePath +"/webpage";
	String uploadifyPath = basePath + "/webpage/front/js/uploadify";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />
<c:set var="webpagePath" value="<%=webpagePath%>" />
<c:set var="uploadifyPath" value="<%=uploadifyPath%>" />
<LINK href="${cssPath}/css.css" type=text/css rel=stylesheet>
<LINK href="${cssPath}/common.css" type=text/css rel=stylesheet>
<link rel="stylesheet" href="${cssPath}/zTreeStyle/zTreeStyle.css" type="text/css">
<LINK href="${cssPath}/easydialog.css" type=text/css rel=stylesheet>
<link id="skin" rel="stylesheet" href="${frontPath}/js/jBox/Skins2/Blue/jbox.css">
<script type="text/javascript" src="${webRoot}/plug-in/jquery/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/jquery.form.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/jquery.metadata.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/jquery.validate.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/jquery.validate.expand.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/messages_zh.js"></script>
<script type="text/javascript" src="${frontPath}/js/plugins/additional-methods.min.js"></script>
<script type="text/javascript" src="${webRoot}/plug-in/My97DatePicker/WdatePicker.js"></script>
<script type="text/javascript" src="${frontPath}/js/jquery.ztree.all-3.5.js"></script>
<script type="text/javascript" src="${frontPath}/js/easydialog.js"></script>
<script type="text/javascript" src="${frontPath}/js/userAutoTips.js"></script>
<script type="text/javascript" src="${frontPath}/js/json2.js"></script>
<script type="text/javascript" src="${frontPath}/js/jBox/jquery.jBox.src.js"></script>

<script type="text/javascript"  >
var webRoot = "${webRoot}";
var imgPath = "${imgPath}";
var frontPath = "${frontPath}";
var uploadifyPath = "${uploadifyPath}";
var sessionuserid = '${SESSION_USER_ATTRIBUTE.id}';
	/**返回页面的pageNo,没有pageNo域时，返回1*/
	var pageNo=function(){
		cpage=$("input[name=pageNo]").val();
		if(cpage){
			return cpage;
		}
		return 1;
	}

	/**
	 * 进行ajax请求的类
	 * 
	 * @return
	 */
	var AT = new AjaxTool();
	function AjaxTool() {
		/**
		 * get方式提交数据
		 */
		this.get = function(url, func, async) {
			if (url.indexOf("?") > 0) {
				url += "&_rad=" + new Date().getTime();
			} else {
				url += "?_rad=" + new Date().getTime();
			}
			
			var options = {
				async : async,
				cache : false,
				url : url,
				type : "get",
				success : function(data, textStatus) {
					func(data);
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					//MSG.alert("请求出错！");
				}
			};
			$.ajax(options);
		}

		this.load = function(divID,url,func) {
			if (url.indexOf("?") > 0) {
				url += "&_rad=" + new Date().getTime();
			} else {
				url += "?_rad=" + new Date().getTime();
			}
			//$("#progressBar").show();
			var options = {
				async : true,
				cache : false,
				url : url,
				type : "get",
				success : function(data, textStatus) {
					$("#"+divID).html(data);
					func();
					//$("#progressBar").hide();
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					//$("#progressBar").hide();
					//MSG.alert("请求出错！");
				}
			};
			$.ajax(options);
		}

		/**
		 * post方式提交
		 * 
		 * @return
		 */
		this.post = function(url, dataParam, func, sync) {
			if(sync == null  || sync == 'undefined'){
				sync = true;
			}
			if (url.indexOf("?") > 0) {
				url += "&_rad=" + new Date().getTime();
			} else {
				url += "?_rad=" + new Date().getTime();
			}
			$("#progressBar").show();
			var options = {
				async : sync,
				cache : false,
				data : dataParam,
				url : url,
				type : "POST",
				success : function(data, textStatus) {
					func(data);
					$("#progressBar").hide();
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					//$("#progressBar").hide();
					//MSG.alert("请求出错！");
				}
			};
			$.ajax(options);
		}
		
		/**
		 * ajax验证
		 * 
		 * 
		 */
		this.check = function(url, dataParam, message, func, sync) {
			if(sync == null || sync == '' || sync == 'undefined'){
				sync = true;
			}
			if (url.indexOf("?") > 0) {
				url += "&_rad=" + new Date().getTime();
			} else {
				url += "?_rad=" + new Date().getTime();
			}
			$("#progressBar").show();
			var options = {
				async : sync,
				cache : false,
				data : dataParam,
				url : url,
				type : "POST",
				success : function(data, textStatus) {
					if($.trim(data) == 'true' || $.trim(data) == ''){
						func(data);
					}else{
						if(message != null && message != ''){
							MSG.alert(message);
						}else{
							MSG.alert(data);
						}
					}
					$("#progressBar").hide();
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {
					//$("#progressBar").hide();
					//MSG.alert("请求出错！");
				}
			};
			$.ajax(options);
		}
		
		/**
		 * 提交表单
		 * 
		 * @return
		 */
		this.postFrm = function(formId, func,isValid) {
			var $form = $("#"+formId);
			var valid=isValid?$form.valid():true;
			if (valid) {
				$("#progressBar").show();
				var param = $form.serializeArray();
				var url=$form.attr("action");
				var options = {
						async : true,
						cache : false,
						data : param,
						url : url,
						type : "POST",
						success : function(data, textStatus) {
							func(data);
							$("#progressBar").hide();
						},
						error : function(XMLHttpRequest, textStatus, errorThrown) {
							//$("#progressBar").hide();
							//MSG.alert("请求出错！");
						}
				};
				$.ajax(options);
			}
			return false;
		}
	}
	
	
	/**消息处理*/
	var MSG = new Message();
	function Message() {
		this.alert=function(msg,title,timeout,options){
		if(title == null  || title == 'undefined'){
			title='提示信息';
		}
			$.jBox.messager(msg, title,timeout,options);
			//xddialog.alert(msg,title,width,OKFuncName);
		};
		this.confirm=function(message,title,okfunc,cancelfuncUser){
			//xddialog.confirm(message,title,okfunc,cancelfuncUser);
		}
	}

</script> 
