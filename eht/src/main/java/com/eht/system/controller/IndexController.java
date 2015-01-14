package com.eht.system.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.eht.common.constant.Constants;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.JsonUtil;
import com.eht.common.util.TreeUtils;
import com.eht.message.service.MessageServiceI;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.TreeData;
import com.eht.system.service.TreeMenuService;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

/**   
 * @Title: Controller
 * @Description: 专题信息
 * @author zhangdaihao
 * @date 2014-03-21 14:49:53
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/indexController")
public class IndexController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(IndexController.class);

	@Autowired
	private MessageServiceI messageService;
	
	@Autowired
	private SubjectServiceI subjectService;
	
	@Autowired
	private AccountServiceI accountService;
	
	@Autowired
	private TreeMenuService treeMenuService;
	
	@Autowired
	private ResourcePermissionService resourcePermissionService;
	
	private String message;
	
	@Autowired
	private RoleService roleService;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 左侧菜单树基本结构数据
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/treeMenu.dht", produces = {"application/json;charset=UTF-8"})
	public @ResponseBody String treeData(HttpServletRequest request) {
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE); 
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
		
			List<TreeData> dataList = new ArrayList<TreeData>();
			
			try {
				//个人专题菜单部分
				dataList.addAll(treeMenuService.buildPersonalSubject(user.getId()));
				
				//多人专题菜单部分
				dataList.addAll(treeMenuService.buildSharedSubject(user.getId()));
				
				//消息中心菜单部分
				dataList.addAll(treeMenuService.buildMessageCenter(user.getId()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return JSONHelper.collection2json(TreeUtils.buildTreeData(dataList));
		} 
		return "";
	}
	
	/**
	 * 右键菜单
	 * @param subjectId
	 * @param primaryKey
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/front/mouseMenu.dht")
	public ModelAndView mouseMenu(String subjectId, String primaryKey, HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/include/mousemenu");
		mv.addObject("subjectId", subjectId);
		mv.addObject("primaryKey", primaryKey);
		return mv;
	}
	/**
	 *	权限查询 
	 * @param subjectId
	 * @param noteId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/front/subjectPermission.dht", produces = {"application/json;charset=UTF-8"})
	public @ResponseBody String subjectPermission(String subjectId,String noteId, HttpServletRequest request) {
		AccountEntity user = accountService.getUser4Session();
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Map<String, String> actionMap = resourcePermissionService.findSubjectPermissionsByUser(user.getId(), subjectId,noteId);
		map.put(subjectId, actionMap);
		return JsonUtil.map2json(map);
	}
	
	/**
	 * 刷新节点
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/reloadNode.dht", produces = {"application/json;charset=UTF-8"})
	public @ResponseBody String reloadNode(String id, String dataType, HttpServletRequest request) {
		//RECYCLEP RECYCLE
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		List<TreeData> dataList = new ArrayList<TreeData>();
		AccountEntity user = (AccountEntity) obj;
		if(obj != null){
			if(dataType.equals("REMENBER")){
			List<RoleUser> roleUserList=roleService.findSubjectUsers(id.split("_")[2]);
			for (RoleUser roleUser : roleUserList) {
				TreeData remenberchild = new TreeData();
				remenberchild.setDataType("REMENBERCHILD");
				remenberchild.setId(roleUser.getUserId());
				remenberchild.setName(roleUser.getAccountEntity().getUserName());
				remenberchild.setBranchId(id.split("_")[2]);
				remenberchild.setpId(id);
				remenberchild.setSubjectId(id.split("_")[2]);
				remenberchild.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/remenberchild.png");
				dataList.add(remenberchild);
			}
			}else{
			if(!StringUtil.isEmpty(id) && !id.equals(Constants.SUBJECT_PID_P) && !id.equals(Constants.SUBJECT_PID_M)){
				// 刷新个人回收站
				if(dataType.equals("RECYCLEP")){
					dataList.addAll(treeMenuService.loadRecycleNode(user.getId(), Constants.SUBJECT_TYPE_P, "RECYCLEP", id));
				}else if(dataType.equals("RECYCLE")){ //刷新多人回收站
					dataList.addAll(treeMenuService.loadRecycleNode(user.getId(), Constants.SUBJECT_TYPE_M, "RECYCLE", id));
				}else{
					SubjectEntity subject = subjectService.getSubject(id);
					dataList.addAll(treeMenuService.loadSubjectNode(user.getId(), subject, true));
				}
			}else{ 
				//刷新整个个人专题部分
				treeMenuService.buildPersonalSubject(user.getId());
			}
			}
			return JSONHelper.collection2json(TreeUtils.buildTreeData(dataList));
		}
		
		return "";
	}
	
	@RequestMapping(value = "/front/index.dht")
	public ModelAndView index(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/index");
		return mv;
	}
	
	@RequestMapping(value = "/front/messageCount.dht", produces = {"application/json;charset=UTF-8"})
	public @ResponseBody String messageCount(HttpServletRequest request) {
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			long sysMsgCount = messageService.getNoReadMessageCount(user.getId(), Constants.MSG_SYSTEM_TYPE);
			long userCount = messageService.getNoReadMessageCount(user.getId(), Constants.MSG_USER_TYPE);
			long totalCount = sysMsgCount + userCount;
			
			Map<String,String> map = new HashMap<String,String>();
			map.put("sysMsgCount", String.valueOf(sysMsgCount));
			map.put("userCount", String.valueOf(userCount));
			map.put("totalCount", String.valueOf(totalCount));
			return JSONHelper.map2json(map);
		}
		return "";
	}
}
