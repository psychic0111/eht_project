<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + path;
	String frontPath = basePath + "/webpage/front";
	String cssPath = basePath + "/webpage/front/css";
	String imgPath = basePath + "/webpage/front/images";
	String webpagePath = basePath +"/webpage";
%>
<c:set var="webRoot" value="<%=basePath%>" />
<c:set var="frontPath" value="<%=frontPath%>" />
<c:set var="cssPath" value="<%=cssPath%>" />
<c:set var="imgPath" value="<%=imgPath%>" />
<c:set var="webpagePath" value="<%=webpagePath%>" />
<LINK href="${cssPath}/css.css" type=text/css rel=stylesheet>
<LINK href="${cssPath}/common.css" type=text/css rel=stylesheet>

<script type="text/javascript" language="javascript">
//分页
function doPage(ths,pageNo,pageSize){
	AT.load("iframepage","${webRoot}/messageController/front/messageList.dht?pageNo=${pageNo}&pageSize=${pageSize}&msgType=" + msgType,function() {});
}
//排序方式
function orderMsg(){
	var param = $("#messageListForm").serialize();
	AT.load("iframepage","${webRoot}/messageController/front/messageList.dht?" + param, function() {});
}
//标记已读
function markMessage(){
	var param = $("#messageListForm").serialize();
	AT.load("iframepage","${webRoot}/messageController/front/messageMark.dht?" + param, function() {});
	var nodes = zTree_Menu.getNodesByFilter(findMsgNode);
	for(var i = 0; i < nodes.length; i++){
		nodes[i].name = nodes[i].name.substring(0,4); 
		zTree_Menu.updateNode(nodes[i]);
	}
	$("#noReadMsgNum").empty().append(0);
}
//删除消息
function deleteMessage(obj){
	var submit = function(v, h, f) {
		if (v == true) {
			var id = $(obj).attr("id");
			var param = $("#messageListForm").serialize() + "&messageId=" + id;
			var options = {
					async : true,
					cache : false,
					url : "${webRoot}/messageController/front/messageDel.dht?" + param,
					type : "get",
					success : function(data, textStatus) {
						$("#iframepage").html(data);
						countMessage();
					},
					error : function(XMLHttpRequest, textStatus, errorThrown) {
					}
				};
			$.ajax(options);
		}
		return true;
	};
	$.jBox.confirm("确认要删除吗？", "提示", submit, {buttons : {'确定' : true,'取消' : false}});
}
//搜索消息
function searchMessage(){
	orderMsg();
}
//查询消息中心节点 
function findMsgNode(node){
	if(node.dataType == "MSG"){
		return true;
	}else{
		return false;
	}
}

$("#message_list font").each(function(index){
	  var inputText = $(this).html();
	  $(this).html(AnalyticEmotion(inputText,context));
	});
	
function sendmessages(obj){
	var urllink='get:${webRoot}/messageController/front/sendMessag.dht';
    $.jBox(urllink, {
    title: "发送消息",
    width: 500,
    height: 350,
   buttons: { '发送': 1, '关闭': 0 },
   submit: function (v, h, f) {
            if (v == 1) {
              AT.postFrm("addsendMessageForm",function(data){
					if(data.success){  
						MSG.alert('发送成功');
					}
				},true);
            return true; 
            } 
            return true;
        }
	});
	}	

</script>

	<div class="right_top">
          <div class="Nav">消息中心</div>
    </div>
    <div class="right_index"> 
	<form action="${webRoot}/messageController/front/messageList.dht" method="post" id="messageListForm" name="messageListForm"> 
		<input type="hidden" name="msgType" value="${msgType }">
		<div class="function">
            <div class="search">
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td class="search_input"><input value="${content}" class="InputTxt1" style=" width:100%; " type="text" name="content" id="content" /></td>
                  <td class="search_btn"><input class="Button1" type="button" name="search_btn" id="search_btn" value="搜索" onclick="searchMessage()"/></td>
				  <td class="search_btn"><input class="Button1" type="button" name="sendmessage_btn" id="sendmessage_btn" value="发送消息" onclick="sendmessages()"/></td>                  
                </tr>
              </table>
            </div>
            <div class="others">
              <input class="Button4" type="button" name="mark_btn" id="mark_btn" onclick="markMessage()" value="全标记为已读" />
              <select name="orderField" style="height:28px;" onchange="orderMsg()">
                <option value="createTime" <c:if test="${orderField == 'createTime' }"> selected="selected"</c:if>>按日期排序 </option>
                <option value="createUser" <c:if test="${orderField == 'createUser' }"> selected="selected"</c:if>>按发送人排序 </option>
              </select>
              <input type="radio" name="orderType" id="radio" value="ASC" onclick="orderMsg()" <c:if test="${orderType == 'ASC' }">checked="checked"</c:if>/>
             	 升序
              <input type="radio" name="orderType" id="radio" value="DESC" <c:if test='${orderType != "ASC"}'>checked="checked"</c:if>  onclick="orderMsg()"/>
             	 降序 
            </div>
            <div class="clear"></div>
          </div>
          <!-- End function--> 
          <!-- Begin comments-->
          <div class="comments">
            <div class="comments_list">
              <table id="message_list" width="100%" border="0" cellspacing="1" cellpadding="0">
                <c:forEach items="${pageResult.rows}" var="msg">
                	<tr class="TD1">
                		<td width="35" align="center">
                			<img src="${webRoot}/${msg.creator.photo}" width="35" height="34" />
                		</td>
                		<td >
                			<input type="hidden" name="id" value="${msg.id }">
                  			<span class="Font2"><strong>${msg.creator.userName}：</strong></span>
                  			<font size="2">
	                  			<c:if test="${msg.userIsRead == 0}"> 
	                  				<strong>${msg.content}</strong><br />
	                  			</c:if>
	                  			<c:if test="${msg.userIsRead == 1}"> 
	                  				${msg.content}<br />
	                  			</c:if>
                  			</font>
                    		<span class="Font1">${msg.createTime}</span>
                    	</td>
                    	<td width="80" align="center">
	                  		<span class="others">
	                    		<input class="Button_other1" onclick="deleteMessage(this)" type="button" name="del_btn" id="${msg.id }" value="删除" />
	                   		</span>
	              		</td>
                	</tr>
                </c:forEach>
              </table>
            </div>
          </div>
          <!-- End comments--> 
          <!-- Begin pages-->
          <div class="pages">
          	<xd:pager  pagerFunction="doPage" pagerStyle="cursor:pointer"  curPagerTheme="pages_cur" showTextPager="true"></xd:pager>
          </div>
          <!-- End pages--> 
		</form>
	</div>
