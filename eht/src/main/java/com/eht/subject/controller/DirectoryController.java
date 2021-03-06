package com.eht.subject.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.eht.common.bean.ResponseStatus;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.ResponseCode;
import com.eht.common.page.PageResult;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.service.GroupService;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.NoteServiceI;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Controller
 * @Description: 目录信息
 * @author yuhao
 * @date 2014-03-21 15:00:05
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/directoryController")
public class DirectoryController extends BaseController {

	@Autowired
	private NoteServiceI noteServiceIMpl;
	@Autowired
	private DirectoryServiceI directoryService;
	
	@Autowired
	private SystemService systemService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private GroupService groupService;
	
	
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * 目录黑名单列表
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/blackListDirectory.dht")
	public ModelAndView blackListDirectory(HttpServletRequest request, @ModelAttribute PageResult pageResult){
		ModelMap mmap = new ModelMap();
		ModelAndView mv = new ModelAndView("front/subject/blackListDirectory");
		String subjectid =request.getParameter("subjectId");
		String directory =request.getParameter("directoryId");
		List<RoleUser>  list=	roleService.findDirtUsers(subjectid,directory,pageResult);
		for (RoleUser roleUser : list) {
			roleUser.setBlackList(groupService.checkDirectoryUser(roleUser.getUserId(), directory));
		}
		mmap.put("subjectId", subjectid);
		mmap.put("directoryId", directory);
		mmap.put("pageResult", pageResult);
		mv.addAllObjects(mmap);
		return mv;
	}
	
	
	/**
	 * 目录成员加入黑名单(如果成员是黑名单那么点击移除)
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/addblackListDirectory.dht")
	@ResponseBody
	public AjaxJson addblackListDirectory(HttpServletRequest request){
		AjaxJson j= new  AjaxJson(); 
		String userId =request.getParameter("userId");
		String directoryId =request.getParameter("directoryId");
		try {
			boolean  c=groupService.checkDirectoryUser(userId, directoryId);
			if(c){
				directoryService.removeUser4lacklist(userId, directoryId);
			}else{
				directoryService.blacklistedUser(userId, directoryId);
			}
			}catch (Exception e) {
				e.printStackTrace();
				j.setMsg("操作失败");
				j.setSuccess(false);
				return j;
			}
		return j;
	}
	
	
	@RequestMapping(value="/front/saveDirectory.dht", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String saveDirectory(DirectoryEntity dir, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(directoryService.nameExists(dir)){
			ResponseStatus res = new ResponseStatus(ResponseCode.SERVER_ERROR);
			return JSONHelper.bean2json(res);
		}
		if(dir.getId() == null || "".equals(dir.getId())){
			dir.setId(UUIDGenerator.uuid());
			dir.setCreateTime(new Date());
			dir.setCreateUser(user.getId());
			dir.setDeleted(0);
			directoryService.addDirectory(dir);
		}else{
			DirectoryEntity updir = systemService.getEntity(DirectoryEntity.class, dir.getId());
			updir.setUpdateTime(new Date());
			updir.setUpdateUser(user.getId());
			updir.setDirName(dir.getDirName());
			directoryService.saveOrUpdate(updir);
//			dir.setUpdateTime(new Date());
//			dir.setUpdateUser(user.getId());
			//directoryService.updateDirectory(dir);
		}
		return JSONHelper.bean2json(dir);
	}
	
	//删除目录至回收站
	@RequestMapping(value="/front/delDirectory.dht")
	public @ResponseBody String deleteDirectory(String id, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		DirectoryEntity dir = directoryService.getDirectory(id);
		dir.setUpdateTime(new Date());
		dir.setUpdateUser(user.getId());
		directoryService.markDelDirectory(dir);
		return "true";
	}
	
	/**
	 * 回收站中删除目录--彻底删除
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/front/truncateDirectory.dht")
	public @ResponseBody String truncateDirectory(String id, HttpServletRequest request){
		directoryService.deleteDirectory(id.replace("_deleted", ""));
		return "true";
	}
	
	/**
	 * 清空回收站
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/front/truncateAll.dht")
	public @ResponseBody String truncateAll(String subjectId, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		List<DirectoryEntity> dirList = null;
		List<NoteEntity> noteList = null;
		// 专题ID空，查询个人回收站数据
		if(StringUtil.isEmpty(subjectId)){
			//找到个人已删除的目录
			dirList = directoryService.findDeletedDirs(user.getId(), Constants.SUBJECT_TYPE_P);
			if(dirList != null && !dirList.isEmpty()){
				for(DirectoryEntity dir : dirList){
					directoryService.deleteDirectory(dir.getId());
				}
			}
			//找到个人专题已经删除的节点
			noteList = noteServiceIMpl.findDeletedNotes(user.getId(), Constants.SUBJECT_TYPE_P);
			if(noteList != null && !noteList.isEmpty()){
				for(NoteEntity note : noteList){
					noteServiceIMpl.deleteNote(note);
				}
			}
		}else{

			//找到指定专题已删除的目录
			dirList = directoryService.findDeletedDirs(user.getId(), subjectId);
			if(dirList != null && !dirList.isEmpty()){
				for(DirectoryEntity dir : dirList){
					directoryService.deleteDirectory(dir.getId());
				}
			}
			try {
				int subid = Integer.parseInt(subjectId);
				//找到指定专题下已经删除的节点
				noteList = noteServiceIMpl.findDeletedNotes(user.getId(), subid);
				if(noteList != null && !noteList.isEmpty()){
					for(NoteEntity note : noteList){
						noteServiceIMpl.deleteNote(note);
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return "true";
	}
	
	/**
	 * 回收站中还原目录
	 * @param id
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/front/restoreDirectory.dht")
	public @ResponseBody AjaxJson restoreDirectory(String id, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		DirectoryEntity dir = directoryService.getDirectory(id);
		dir.setUpdateTime(new Date());
		dir.setUpdateUser(user.getId());
		List<String> dirIdList = new ArrayList<String>();
		AjaxJson j= new  AjaxJson();
		directoryService.restoreDirectory(dir, dirIdList,true);
		StringBuffer sb=new StringBuffer("");
		for (int i = 0; i < dirIdList.size(); i++) {
			if(i!=0){
				sb.append(",");
			}
			sb.append(dirIdList.get(i));
		}
		j.setObj(sb.toString());
		return j;
	}
	
	@RequestMapping(value="/front/blacklist.dht", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String blacklist(String dirId, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		String str = "";
		if(!StringUtil.isEmpty(dirId)){
			String[] dirIds = dirId.split(",");
			for(String id : dirIds){
				if(!StringUtil.isEmpty(dirId)){
					continue;
				}
				boolean result = directoryService.inDirBlackList(user.getId(), id);
				if(result){
					str += "id,";
				}
			}
			if(str.length() > 0){
				str = str.substring(0, str.length() - 1);
			}
		}
		return str;
	}
	
	/**
	 * 目录信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "directory")
	public ModelAndView directory(HttpServletRequest request) {
		return new ModelAndView("com/eht/subject/directoryList");
	}

	/**
	 * easyui AJAX请求数据
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */
	@RequestMapping(params = "datagrid")
	public void datagrid(DirectoryEntity directory,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(DirectoryEntity.class, dataGrid);
		
		//查询条件组装器
		directory.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, directory, request.getParameterMap());
		this.directoryService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 *后台删除目录信息
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(DirectoryEntity directory, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		directory = systemService.getEntity(DirectoryEntity.class, directory.getId());
		directory.setDeleted(Constants.DATA_DELETED);
		directoryService.saveOrUpdate(directory);
		message = "目录信息删除成功";
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}

	/**
	 * 目录信息列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(DirectoryEntity directory, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(directory.getId())) {
			directory = directoryService.getEntity(DirectoryEntity.class, directory.getId());
			req.setAttribute("directoryPage", directory);
		}
		return new ModelAndView("com/eht/subject/directory");
	}
}
