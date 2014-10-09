<%@page import="com.eht.common.constant.Constants"%>
<%@page import="com.eht.common.constant.ActionName"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="UTF-8"%>
<%@include file="/context/mytags.jsp"%>
<script type="text/javascript">
//全选按钮
	$("#checkbox2").click(function(){
	  	$(".checkusrs").attr("checked",this.checked);
	});
	$(".checkusrs").click(function(){
	  	var tep= $(".checkusrs");
	 	 $("#checkbox2").attr("checked",tep.length==tep.filter(':checked').length);
	});

$().ready(function() {
	 $('input[id^=textarea]').bind("keyup",function(event){
		 var obj=$(this);
		if(obj.val()!=''){
		 AT.post("${webRoot}/subjectController/front/subjectTemember.dht","email="+obj.val()+"&textarea="+obj.attr("id"),function(data){
		 	 $("#invitememberAuto").find('ul').remove();
		 	 if($(data).find('li').length>0){
		 		$("#invitememberAuto").append(data);
		 			 var top = obj.offset().top+30;
			 		var left = obj.offset().left;
					$("#invitememberAuto").css({position: "absolute",'top':top ,'left':left});
			 		$("#invitememberAuto").show();
			 	 }else{
			 		$("#invitememberAuto").hide();
				 	 }
		 });
		}else{
			 $("#invitememberAuto").find('ul').remove();
			 $("#invitememberAuto").hide();
			}
		});
});

	function delInvitemember(){
         if($("[name=ids]:checked").length==0){
             MSG.alert("请选择用户");
             return false;
             }
         if(confirm("确定删除专题成员?")){
		 AT.post("${webRoot}/subjectController/front/delSubjectRole.dht",$("[name=ids]:checked").serialize()+"&subjectid=${subjectEntity.id}",function(data){
				if(data.success){
					 MSG.alert(data.msg);
					AT.load("iframepage","${webRoot}/subjectController/front/memberManage.dht?id=${subjectEntity.id}",function() {});	
				}else{
					 MSG.alert(data.msg);
				}	
		});
         }
	}
	function invitememberRole(o){
		obj=$(o);
		var top = obj.offset().top;
		var left = obj.offset().left;
		$("#invitememberMenu").css({position: "absolute",'top':top + 10,'left':left + 10});
		$("#invitememberMenu").show();
		}

	//右键菜单关闭方法ids=0&ids=1&ids=3&ids=4
	function hideInvitememberMenu(){ 
		$("#invitememberMenu").hide();
		
	}
	//-------------------鼠标离开关闭角色选择窗口--------------start---------
	//在弹出区域不需要隐藏树
	$("#invitememberMenu").mouseover(function() {
		$("body").unbind("mousedown", onBodyTagDown);
	}).mouseout(function() {
		$("body").bind("mousedown", onBodyTagDown);
	});
	//鼠标页面点击事件
	function onBodyTagDown(event) { 
		if (event.target.id != "invitememberMenu"
				&& event.target.id != "invitememberMenu") {
			$("#invitememberMenu").hide();
		}
	}
	//-------------------鼠标离开关闭角色选择窗口---------------end--------
	
	
	
	
	function updateInvitememberRole(obj){
		if($("[name=ids]:checked").length==0){
            MSG.alert('请选择用户!');
            hideInvitememberMenu();
            return false;
            }
		if(confirm("确定修改专题成员角色?")){
		 AT.post("${webRoot}/subjectController/front/updateSubjectRole.dht",$("[name=ids]:checked").serialize()+'&type='+obj,function(data){
				if(data.success){
					 MSG.alert(data.msg);
					AT.load("iframepage","${webRoot}/subjectController/front/memberManage.dht?id=${subjectEntity.id}",function() {});	
				}else{
					 MSG.alert(data.msg);
				}	
		});
		}
		hideInvitememberMenu();
		}
		
function viewInvitemember(){
              var mail=$("#textarea1").val();
					if(mail!=''){
					if(!mail.match(/^\w+([\.\-]\w+)*\@\w+([\.\-]\w+)*\.\w+$/)){
					 MSG.alert("邮箱格式不正确");
					  return false;
					}
					}else{
					MSG.alert("请填写邮箱");
					}
				
					AT.postFrm("addInvitemember",function(data){
						if(data==''){
								 MSG.alert('操作成功');
							}
							AT.load("iframepage","${webRoot}/subjectController/front/memberManage.dht?id=${subjectEntity.id}",function() {});
					},true);
            return true; 
	}
	
	function addemail(value,id){
	 $("#textarea1").val(value);
	 $("#invitememberAuto").hide();
	 $("#invitememberAuto").find('ul').remove();
}

function sendInvitemember(obj){
					 AT.post("${webRoot}/subjectController/front/sendInvitemember.dht","id="+obj,function(data){
						if(data==''){
								 MSG.alert('操作成功');
							}
							AT.load("iframepage","${webRoot}/subjectController/front/memberManage.dht?id=${subjectEntity.id}",function() {});
					},true);
            return true; 
	}
function delInvitemember(obj){
	 AT.post("${webRoot}/subjectController/front/delInvitemember.dht","id="+obj,function(data){
						if(data==''){
								 MSG.alert('操作成功');
							}
							AT.load("iframepage","${webRoot}/subjectController/front/memberManage.dht?id=${subjectEntity.id}",function() {});
					},true);
            return true; 
	}
	
</script>
		
 		<div class="right_top mainer_right">
          <div class="Nav">
          	<a href="#" style="color:#6969F5" id="-2" onclick="subjectManage(this)">专题管理</a> &gt; ${subjectEntity.subjectName} &gt; 成员管理
          </div>
        </div>
        <!-- Begin mainer_index-->
         <div class="right_index">
          <!-- Begin function-->
		<xd:hasPermission subjectId="${subjectEntity.id }" action="<%=ActionName.ASSIGN_MEMBER %>" resource="<%=Constants.SUBJECT_MODULE_NAME %>">          
          <div class="function">
         	 <div style="float:left;">
         	   <form action="${webRoot}/subjectController/front/addInvitemember.dht" method="post" id="addInvitemember" name="addInvitemember" onsubmit="return false;" > 
               <input type="hidden" name="id" value="${subjectEntity.id }">
              <input onfocus='javascript:this.value=""' class="InputTxt2" value="输入被邀请成员的邮箱地址" style="width:200px;height:27px;float:left;padding-left:12px;margin-right:10px;color:graytext;" name="textarea1" autocomplete="off" id="textarea1"  type="text"/>
         	  <select id="textareatype" style="width:100px;float:left;height:28px;margin-right:10px;" name="type">
         	  				  <option value="4">读者</option>
         	  				  <option value="3">作者</option>
         	  				  <option value="2">编辑</option>
			                  <option value="1">管理员</option>
              </select>
         	  <input style="width:100px;float:left;padding-left:12px"  class="Button2" type="button" name="inviteMem_button" id="inviteMem_button"  onclick="viewInvitemember();" value="邀请新成员" />
              </form>          	
			</div>
            <div class="others">
              <input class="Button3" type="button" name="button4" id="button3" onclick="delInvitemember();" value="删除成员" />
              <input class="Button4" type="button" name="button4" id="button4" onclick="invitememberRole(this);" value="更改角色" />
              <div class="rightMenu" id="invitememberMenu">
        		<ul id="treeRightMenu_ul_tag">
        			<xd:hasPermission subjectId="${subjectEntity.id }" action="<%=ActionName.DELETE_SUBJECT %>" resource="<%=Constants.SUBJECT_MODULE_NAME %>">
       					<li  onclick="updateInvitememberRole('1')">超级管理员</li>
       				</xd:hasPermission>
       				<li  onclick="updateInvitememberRole('2')">编辑</li>
       				<li  onclick="updateInvitememberRole('3')">作者</li>
       				<li  onclick="updateInvitememberRole('4')">读者</li>
       				<!-- <li  onclick="hideInvitememberMenu_close(); ">关闭</li>  -->
        		</ul>
        	</div>
            </div>
            <div class="clear"></div>
          </div>
         </xd:hasPermission>
          <!-- End function--> 
          <!-- Begin Data-->
          <div class="Data">
            <div class="Data_list">
              <table width="100%" border="0" cellspacing="1" cellpadding="0">
                <tr>
                  <td align="center" class="tdTitle" style="width:20%">用户</td>
                  <td align="center" class="tdTitle" style="width:10%">角色</td>
                  <td align="center" class="tdTitle" style="width:10%">状态</td>
                  <td align="center" class="tdTitle" style="width:10%">
                  	<input type="checkbox"  id="checkbox2" />
                  </td>
                </tr>
                <c:forEach items="${list}" var="roleUser">
                <tr class="TD3">
                  <td align="center">${roleUser.accountEntity.username}</td>
                  <td align="center">${roleUser.role.description}</td>
                   <td align="center">已激活</td>
                    <td align="center">
                  <c:choose>
                  <c:when test="${roleUser.accountEntity.id eq user.userId}">
                  </c:when>
                   <c:when test="${user.role.roleName eq 'ADMIN' && (roleUser.role.roleName eq 'ADMIN' || roleUser.role.roleName eq 'OWNER')}">
                  </c:when>
                  <c:when test="${user.role.roleName ne 'ADMIN' && user.role.roleName ne 'OWNER'}">
                  </c:when>
                  <c:otherwise><input type="checkbox" name="ids" class="checkusrs" value="${roleUser.id}" /></c:otherwise>
                  </c:choose>
                  </td>
                </tr>
                </c:forEach>
                 <c:forEach items="${inviteMememberList}" var="inviteMemember">
                <tr class="TD3">
                  <td align="center">
                   <c:choose>
                  <c:when test="${empty  inviteMemember.username}">
                  		${inviteMemember.email}
                  </c:when>
                  <c:otherwise>
                        ${inviteMemember.username}
                  </c:otherwise>
                  </c:choose>
                  </td>
                  <td align="center">${inviteMemember.role.description}</td>
                  <td align="center">未激活</td>
                  <td align="left">
                  	<span style="width:100%;">
                  		<input class="Button2" type="button" onclick="sendInvitemember('${inviteMemember.id}');" value="再次发送" />
                    	<input class="Button3" type="button" onclick="delInvitemember('${inviteMemember.id}');" value="取消邀请" />
                    </span>
                  </td>
                </tr>
                </c:forEach>
              </table>
            </div>
          </div>
          <!-- End Data--> 
        </div>
        <div class="rightMenu"  id="invitememberAuto">
        </div>