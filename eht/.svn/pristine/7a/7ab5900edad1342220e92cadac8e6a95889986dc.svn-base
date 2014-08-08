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

	function viewInvitemember(){
		AT.load("iframepage","${webRoot}/subjectController/front/viewInvitemember.dht?id=${subjectEntity.id}",function() {});	
	}

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
</script>
		
 		<div class="right_top mainer_right">
          <div class="Nav">专题${subjectEntity.subjectName}&gt; <a href="#">成员管理</a></div>
        </div>
        <!-- Begin mainer_index-->
        
        <div class="right_index">
          <!-- Begin function-->
		<xd:hasPermission subjectId="${subjectEntity.id }" action="<%=ActionName.ASSIGN_MEMBER %>" resource="<%=Constants.SUBJECT_MODULE_NAME %>">          
          <div class="function">
            <div class="others">
              <input class="Button2" type="button" name="button" id="button" value="邀请新成员" onclick="viewInvitemember();" />
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
       				<li  onclick="hideInvitememberMenu()">关闭</li>
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
                  <td align="center" class="tdTitle">
                  	<input type="checkbox"  id="checkbox2" />
                  </td>
                  <td align="center" class="tdTitle">用户</td>
                  <td align="center" class="tdTitle">角色</td>
                </tr>
                <c:forEach items="${list}" var="roleUser">
                <tr class="TD3">
                  <td align="center">
                  <c:choose>
                  <c:when test="${roleUser.accountEntity.id eq user.userId}">
                  </c:when>
                   <c:when test="${user.role.roleName eq 'ADMIN' && (roleUser.role.roleName eq 'ADMIN' || roleUser.role.roleName eq 'OWNER')}">
                  </c:when>
                  <c:when test="${user.role.roleName ne 'ADMIN' && user.role.roleName ne 'OWNER'}">
                  </c:when>
                  <c:otherwise><input type="checkbox" name="ids" class="checkusrs" value="${roleUser.userId}" /></c:otherwise>
                  </c:choose>
                  </td>
                  <td align="center">${roleUser.accountEntity.username}</td>
                  <td align="center">${roleUser.role.description}</td>
                </tr>
                </c:forEach>
              </table>
            </div>
          </div>
          <!-- End Data--> 
        </div>