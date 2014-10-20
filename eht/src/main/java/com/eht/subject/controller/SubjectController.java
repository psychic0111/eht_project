package com.eht.subject.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.eht.common.constant.Constants;
import com.eht.common.page.PageResult;
import com.eht.common.util.AppRequstUtiles;
import com.eht.common.util.SubjectToMht;
import com.eht.common.util.UUIDGenerator;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.Info;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.entity.SubjectMht;
import com.eht.subject.entity.SujectSchedule;
import com.eht.subject.entity.ZipEntity;
import com.eht.subject.service.InviteMememberServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

/**
 * @Title: Controller
 * @Description: 专题信息
 * @author yuhao
 * @date 2014-03-21 14:49:53
 * @version V1.0
 * 
 */
@Controller
@RequestMapping("/subjectController")
public class SubjectController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SubjectController.class);

	@Autowired
	private SubjectServiceI subjectService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private NoteServiceI noteService;

	@Autowired
	private TemplateServiceI templateService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private AttachmentServiceI attachmentService;
	
	@Autowired
	private InviteMememberServiceI inviteMememberService;
	
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 前台添加专题 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/front/subjectManage.dht")
	public ModelAndView viewSubjectManage(int subjectType, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(subjectType <= 0){
			subjectType = 1;
		}
		List<SubjectEntity> subjectList = subjectService.findUsersSubjectByType(user.getId(), subjectType);
		ModelAndView mv = new ModelAndView("front/subject/subjectmanage");
		
		mv.addObject("subjectList", subjectList);
		mv.addObject("subjectType", subjectType);
		return mv;
	}
	
	/**
	 * 前台添加专题 专题列表查询
	 * 
	 * @return
	 */
	@RequestMapping("/front/subjectList.dht")
	public ModelAndView subjectList(int subjectType, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(subjectType <= 0){
			subjectType = 1;
		}
		List<SubjectEntity> subjectList = subjectService.findUsersSubjectByType(user.getId(), subjectType);
		ModelAndView mv = new ModelAndView("front/subject/subjectlist");
		
		mv.addObject("subjectList", subjectList);
		mv.addObject("subjectType", subjectType);
		return mv;
	}

	/**
	 * 前台目录上传 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/front/dirAttaManage.dht")
	public ModelAndView viewDirAttaManage(String subjectId, String dirId, String ispage, HttpServletRequest request, @ModelAttribute PageResult pageResult) {
		if (pageResult == null) {
			pageResult = new PageResult();
			pageResult.setPageSize(10);
		}
		// pageResult.setPageSize(Constants.PER_PAGE_COUNT);
		if (pageResult != null && pageResult.getPageSize() != 10) {
			pageResult.setPageSize(10);
		}
		List<AttachmentEntity> attList = attachmentService.findAttachmentsByDir(subjectId, dirId, pageResult.getPageNo(), pageResult.getPageSize());
		pageResult.setTotal(attachmentService.findAttachmentsByDirCount(subjectId, dirId));
		pageResult.setRows(attList);

		String viewUrl = "front/subject/attachment";
		ModelMap mmp = new ModelMap();
		// 如果是分页请求
		if (ispage != null && ispage.equals("true")) {
			viewUrl = "front/subject/attachmentList";
		}
		mmp.put("pageResult", pageResult);
		mmp.put("dirId", dirId);
		mmp.put("subjectId", subjectId);
		mmp.put("pageNo", pageResult.getPageNo() == 0 ? "1" : (pageResult.getPageNo() + ""));

		ModelAndView mv = new ModelAndView(viewUrl);
		mv.addAllObjects(mmp);
		return mv;
	}

	/**
	 * 单个附件初始化相关数据
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/front/initDirAtta.dht", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String initDirAtta(String attid, HttpServletRequest request) {
		String json = "";
		AttachmentEntity att = attachmentService.getAttachment(attid);
		// 拿目录名（directoryName）和条目名（noteName）
		if (att != null) {
			String noteTitle = att.getNoteEntity() == null ? "" : att.getNoteEntity().getTitle();
			String userName = att.getCreator() == null ? "" : att.getCreator().getUsername();
			String dirName = att.getDirectoryEntity() == null ? "" : att.getDirectoryEntity().getDirName();
			json += "{\"id\":\"" + att.getId() + "\"" + ",\"noteIdCN\":\"" + noteTitle + "\"" + ",\"createUserCN\":\"" + userName + "\"" + ",\"directoryIdCN\":\"" + dirName + "\"" + ",\"updateTime\":\"" + att.getUpdateTime() + "\"" + ",\"fileName\":\"" + att.getFileName() + "\"" + "}";
		}
		return json;
	}

	/**
	 * 前台专题成员管理
	 * 
	 * @return
	 */
	@RequestMapping("/front/memberManage.dht")
	public ModelAndView memberManage(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/subject/membermanage");
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, request.getParameter("id"));
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		List<RoleUser> list = roleService.findSubjectUsers(subjectEntity.getId());
		mv.addObject("isAdmin", false);
		for (RoleUser roleUser : list) {
			if (user.getId().equals(roleUser.getUserId())) {
				mv.addObject("user", roleUser);
			}
		}
		List<InviteMememberEntity>  inviteMememberList =inviteMememberService.findInviteMemember(subjectEntity.getId());
		mv.addObject("subjectEntity", subjectEntity);
		mv.addObject("list", list);
		mv.addObject("inviteMememberList", inviteMememberList);
		return mv;
	}

	@RequestMapping("/front/viewMembers.dht")
	public ModelAndView viewMembers(String subjectId, HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/subject/viewmembers");
		SubjectEntity subjectEntity = subjectService.getSubject(subjectId);
		List<RoleUser> list = roleService.findSubjectUsers(subjectId);
		for(RoleUser ru : list){
			long count = noteService.countNotesBySubjectUser(subjectId, ru.getUserId());
			ru.setNoteCount(count);
		}
		
		mv.addObject("subjectEntity", subjectEntity);
		mv.addObject("list", list);
		return mv;
	}
	
	/**
	 * 前台添加专题 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/front/viewAddSubject.dht")
	public ModelAndView viewAddSubject(int subjectType, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		List<TemplateEntity> templateList = templateService.findUserTemplates(user.getId(), Constants.TEMPLATE_SUBJECT_CLASSIFY);
		ModelAndView mv = new ModelAndView("front/subject/addsubject");
		if(subjectType <= 0){
			subjectType = 1;
		}
		mv.addObject("subjectType", subjectType);
		mv.addObject("templateList", templateList);
		return mv;
	}

	/**
	 * 前台删除专题角色
	 * 
	 * @return
	 */
	@RequestMapping("/front/delSubjectRole.dht")
	@ResponseBody
	public AjaxJson delSubjectRole(HttpServletRequest request) {
		String[] dels = request.getParameterValues("ids");
		String subjectid = request.getParameter("subjectid");
		AjaxJson j = new AjaxJson();
		try {
			subjectService.delInviteMember(dels, subjectid);
		} catch (Exception e) {
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;
	}

	/**
	 * 前台修改专题角色
	 * 
	 * @return
	 */
	@RequestMapping("/front/updateSubjectRole.dht")
	@ResponseBody
	public AjaxJson updateSubjectRole(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String[] ids = request.getParameterValues("ids");
		String type = request.getParameter("type");
		try {
			subjectService.updateInviteMemberRole(ids, type);
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;
	}

	/**
	 * 前台添加专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/addSubject.dht")
	@ResponseBody
	public AjaxJson addSubject(SubjectEntity subject, String templateId, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		subject.setCreateUser(user.getId());
		subject.setCreateTime(new Date());
		subject.setId(UUIDGenerator.uuid());
		AjaxJson j = new AjaxJson();
		try {
			subjectService.addSubject(subject);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("subjectId", subject.getId());
			map.put("subjectType", subject.getSubjectType());
			j.setAttributes(map);
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;

	}
	
	/**
	 * 专题导出状态
	 * 
	 * @return
	 */
	@RequestMapping("/front/sujectSchedule.dht")
	@ResponseBody
	public AjaxJson sujectSchedule(HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		Info  info =SujectSchedule.getSchedule(user.getId());
		AjaxJson j = new AjaxJson();
		if(info!=null){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("subjectId", info.getSubjectId());
			map.put("cout", info.getCout());
			map.put("couts", info.getCouts());
			j.setSuccess(true);
			j.setAttributes(map);
		}else{
			j.setSuccess(false);
		}
		return j;
	}

	/**
	 * 前台编辑专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/viewEditSubject.dht")
	public ModelAndView viewEditSubject(SubjectEntity subject, String templateId, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		List<TemplateEntity> templateList = templateService.findUserTemplates(user.getId(), Constants.TEMPLATE_SUBJECT_CLASSIFY);
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, subject.getId());
		ModelAndView mv = new ModelAndView("front/subject/editsubject");
		mv.addObject("templateList", templateList);
		mv.addObject("subject", subjectEntity);
		return mv;
	}

	/**
	 * 前台编辑专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/editSubject.dht")
	@ResponseBody
	public AjaxJson editSubject(SubjectEntity subject, String templateId, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, subject.getId());
		Integer type=subjectEntity.getSubjectType();
		subjectEntity.setDescription(subject.getDescription());
		if(subject.getSubjectName()!=null){
			subjectEntity.setSubjectName(subject.getSubjectName());
		}
		subjectEntity.setSubjectType(subject.getSubjectType());
		subjectEntity.setUpdateUser(user.getId());
		subjectEntity.setUpdateTime(new Date());
		try {
			if(type!=subjectEntity.getSubjectType()){
				subjectService.updateSubject(subjectEntity,true);
			}else{
				subjectService.updateSubject(subjectEntity);
			}
		Map <String, Object>map=new HashMap<String, Object>();
		map.put("subjectId", subject.getId());
		map.put("subjectType", subject.getSubjectType());
		j.setAttributes(map);
		} catch (Exception e) {
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;
	}

	/**
	 * 前台判断专题是多人还是个人专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/checkSubjectType.dht")
	@ResponseBody
	public AjaxJson checkSubjectType(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, request.getParameter("id"));
		j.setObj(subjectEntity.getSubjectType());
		return j;
	}

	/**
	 * 前台专题验证
	 * 
	 * @return
	 */
	@RequestMapping("/front/checkSubjectName.dht")
	public @ResponseBody
	String checkSubjectName(SubjectEntity subject) {
		int type = subjectService.existsSubjectName(subject.getSubjectName(), subject.getId());
		String result = "true";
		if (type != -1) {
			result = "false";
		}
		return result;
	}

	/**
	 * 前台专题邀请成员验证
	 * 
	 * @return
	 */
	@RequestMapping("/front/checkInvitemember.dht")
	public @ResponseBody
	String checkInvitemember(HttpServletRequest request) {
		String result = "true";
		String type = request.getParameter("type");
		String textarea = request.getParameter("textarea" + type);
		if (textarea.equals("")) {
			return result;
		}
		String textareas[] = textarea.split(",");
		for (int i = 0; i < textareas.length; i++) {
			if (!checkEmail(textareas[i])) {
				return "false";
			}
		}
		return result;
	}

	/**
	 * 前台邀请成员页面跳转
	 * 
	 * @return
	 */
	@RequestMapping("/front/viewInvitemember.dht")
	public ModelAndView viewInvitemember(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/subject/invitemember");
		mv.addObject("id", request.getParameter("id"));
		return mv;
	}

	/**
	 * 前台邀请成员页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/addInvitemember.dht", produces = { "text/html;charset=UTF-8" })
	public @ResponseBody
	String addInvitemember(HttpServletRequest request) {
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, request.getParameter("id"));
		String textarea1[] = request.getParameterValues("textarea1");
		String types[] = request.getParameterValues("type");
		StringBuffer sb = new StringBuffer("");
		try {
			subjectService.inviteMemember(textarea1, types, request, subjectEntity);
		} catch (Exception e) {
			sb.append("邀请失败");
		}
		return sb.toString();
	}

	
	/**
	 * 
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/sendInvitemember.dht", produces = { "text/html;charset=UTF-8" })
	public @ResponseBody String sendInvitemember(HttpServletRequest request){ 
		StringBuffer sb = new StringBuffer("");
		try {
			subjectService.inviteMemember(request.getParameter("id"), request);
		} catch (Exception e) {
			sb.append("发送失败");
		}
		return sb.toString();
	}
	
	/**
	 * 前台删除成员邀请
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/delInvitemember.dht", produces = { "text/html;charset=UTF-8" })
	public @ResponseBody String delInvitemember(HttpServletRequest request){ 
		StringBuffer sb = new StringBuffer("");
		try {
			InviteMememberEntity inviteMememberEntity=	subjectService.get(InviteMememberEntity.class, request.getParameter("id"));
			subjectService.delete(inviteMememberEntity);
		} catch (Exception e) {
			sb.append("删除失败");
		}
		return sb.toString();
	}
	
	/**
	 * 前台接受邀请成员
	 * 
	 * @return
	 */
	@RequestMapping("/center/acceptInvitemember")
	public ModelAndView acceptInvitemember(HttpServletRequest request, HttpSession session) {
		ModelAndView mv = new ModelAndView("user/register_status");
		InviteMememberEntity inviteMememberEntity = subjectService.get(InviteMememberEntity.class, request.getParameter("id"));
		if (inviteMememberEntity != null) {
			AccountEntity accountEntity = accountService.findUserByEmail(inviteMememberEntity.getEmail());
			if (accountEntity == null) {
				mv.addObject("msg", "请先注册");
				mv.addObject("linkname", "注册");
				mv.addObject("linkpath", "webpage/register.jsp?id=" + inviteMememberEntity.getId()+"&email="+inviteMememberEntity.getEmail());

			} else {
				try {
					subjectService.acceptInviteMember(inviteMememberEntity, accountEntity);
					mv.addObject("msg", "邀请成功");
				} catch (Exception e) {
					e.printStackTrace();
					mv.addObject("msg", "邀请失败");
				}
				mv.addObject("linkname", "登录");
				mv.addObject("linkpath", "webpage/login.jsp");
				return mv;
			}
		} else {
			mv.addObject("msg", "此链接过期");
		}
		return mv;
	}

	/**
	 * 前台导出专题
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/treeSuject.dht")
	public ModelAndView treeSuject(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("front/subject/subjectTree");
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		mv.addObject("tree", subjectService.treeSubject(request.getParameter("subjectId"),user.getId(),false));
		return mv;
	}
	
	@RequestMapping(value = "/front/treeDataEdit.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String treeDataEdit(HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		String json=subjectService.treeSubject(request.getParameter("subjectId"),user.getId(),true);
		return json;
	}
	
	/**
	 * 前台导出专题
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/exportSuject.dht")
	@ResponseBody
	public AjaxJson exportSuject(HttpServletRequest request, HttpServletResponse response) {
		final AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		final String subjectId=request.getParameter("subjectId");
		final String dirs[] =request.getParameter("dirsId").split(",");
		final String path = request.getSession().getServletContext().getRealPath("/");
		final  String basePath = AppRequstUtiles.getAppUrl(request);
		Info info=new Info();
		info.setCouts(dirs.length);
		info.setSubjectId(subjectId);
		info.setUserId(user.getId());
		AjaxJson ajaxJson=new AjaxJson();
		boolean b=SujectSchedule.putSchedule(user.getId(), info);
		if(b){
			ajaxJson.setSuccess(false);
			Thread thread = new Thread() {
				public void run() {
					try {
						subjectService.exportSuject(subjectId, path,basePath, user,dirs);
					} catch (Exception e) {
						
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}else{
			ajaxJson.setSuccess(true);
		}
		
		return ajaxJson;
	}
	
	/**
	 * 前台下载专题
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/downzip.dht")
	public ModelAndView downzip(HttpServletRequest request, HttpServletResponse response) {
		ZipEntity  zipEntity=subjectService.getEntity(ZipEntity.class, request.getParameter("id"));
		File file=new File(zipEntity.getPath());
		if(!file.exists()){
			try {
				response.setContentType("text/html;charset=UTF-8");
				response.getWriter().write("文件已不存在！");
			} catch (IOException e) {
			}
			return null;
		}
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=export.zip");
	    OutputStream  outputStream=null;
	    InputStream inStream=null;
		byte[] b = new byte[1024];
	    int len;
		try {
			outputStream=response.getOutputStream();
			inStream = new FileInputStream(file);// 文件的存放路径
			 while ((len = inStream.read(b)) > 0){
				 outputStream.write(b, 0, len);
			 }
			 outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(inStream!=null){
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	/**
	 * 前台导出报告
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/exportSujectmht.dht")
	public ModelAndView exportSujectmht(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/octet-stream;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=export.mht");
		Writer out = null;
		try {
			out = response.getWriter();
			AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
			String basePath = AppRequstUtiles.getAppUrl(request)+"/noteController/front/downloadNodeAttach.dht?id=";
			SubjectMht  mht=subjectService.SubjectforMht(request.getParameter("id"),user);
			SubjectToMht.subjectToMht(mht, request);
			Map map=new HashMap<String, Object>();
			map.put("subject", mht.getSubjectEntity());
			map.put("subjectNoteslist", mht.getSubjectNoteslist());
			map.put("subjectdirlist", mht.getSortList());
			map.put("subjectImglist", mht.getList());
			map.put("basePath", basePath);
			try {
			SubjectToMht.subjectToMht("wordtempalte/word.ftl", map,out);
			} catch (Exception e) {
				e.printStackTrace();
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {

				}
			}
		}
		return null;
	}

	/**
	 * 前台导入专题页面
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/leadingSuject.dht")
	public ModelAndView leadingSuject(HttpServletRequest request) {
		return new ModelAndView("front/subject/leadinginsubject");
	}

	/**
	 * 删除专题
	 * 
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/front/deleteSubject.dht")
	public @ResponseBody
	String deleteSubject(String subjectId, HttpServletRequest request) {
		//SubjectEntity subject = subjectService.getSubject(subjectId);
		subjectService.deleteSubject(subjectId);
		/*// 删除专题下目录
		List<DirectoryEntity> dirList = directoryService.findDirsBySubject(subjectId);
		for (DirectoryEntity dir : dirList) {
			directoryService.deleteDirectory(dir.getId());
		}

		// 删除专题下条目
		List<NoteEntity> noteList = noteService.findNotesBySubject(subjectId);
		for (NoteEntity note : noteList) {
			noteService.markDelNote(note);
		}*/
		return "true";
	}

	/**
	 * 导入专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/leadinginSuject.dht")
	public ModelAndView leadinginSuject(HttpServletRequest request) {
		try {
			subjectService.leadinginSuject(request);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("msg", "导入失败");
			return new ModelAndView("front/subject/leadingSujectResult");
		}
		request.setAttribute("msg", "导入成功");
		return new ModelAndView("front/subject/leadingSujectResult");
	}

	/**
	 * 前台展现专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/showSubject.dht")
	public ModelAndView showSubject(HttpServletRequest request) {
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, request.getParameter("id"));
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		StringBuffer sb = new StringBuffer();
		subjectService.showCatalogueSubject(subjectEntity, user, sb);
		request.setAttribute("subjectEntity", subjectEntity);
		request.setAttribute("sb", sb);
		return new ModelAndView("front/subject/showSubject");
	}

	/**
	 * 前台专题邀请邮箱模糊匹配
	 * 
	 * @return
	 */
	@RequestMapping("/front/subjectTemember.dht")
	public ModelAndView subjectTemember(HttpServletRequest req) {
		List<AccountEntity> list = noteService.getShareEmail(req.getParameter("email"));
		req.setAttribute("list", list);
		req.setAttribute("textarea", req.getParameter("textarea"));
		return new ModelAndView("front/subject/autoTemember");
	}

	/**
	 * 专题信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "subject")
	public ModelAndView subject(HttpServletRequest request) {
		return new ModelAndView("com/eht/subject/subjectList");
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
	public void datagrid(SubjectEntity subject, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(SubjectEntity.class, dataGrid);
		// 查询条件组装器
		subject.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, subject, request.getParameterMap());
		this.subjectService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 方法描述: 查看目录列表 作 者：于浩
	 * 
	 * @param request
	 * @param subjectid
	 * @return 返回类型： ModelAndView
	 */
	@RequestMapping(params = "directoryList")
	public ModelAndView directoryList(HttpServletRequest request, String subjectid) {
		request.setAttribute("subjectid", subjectid);
		return new ModelAndView("com/eht/subject/subjectdirectoryList");
	}

	/**
	 * 查看目录列表 easyui AJAX请求数据
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */
	@RequestMapping(params = "directoryDatagrid")
	public void datagrid(DirectoryEntity directory, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(DirectoryEntity.class, dataGrid);
		String departid = oConvertUtils.getString(request.getParameter("subjectid"));
		if (!StringUtil.isEmpty(departid)) {
			DetachedCriteria dc = cq.getDetachedCriteria();
			DetachedCriteria dcDepart = dc.createCriteria("subjectEntity");
			dcDepart.add(Restrictions.eq("id", departid));
		}
		// 查询条件组装器
		directory.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, directory);
		systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 条目信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "noteList")
	public ModelAndView note(HttpServletRequest request, String subjectid) {
		request.setAttribute("subjectid", subjectid);
		return new ModelAndView("com/eht/subject/noteList");
	}

	/**
	 * 条目信息列表 页面跳转 easyui AJAX请求数据
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "noteDatagrid")
	public void datagrid(NoteEntity note, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(NoteEntity.class, dataGrid);
		// 查询条件组装器
		String departid = oConvertUtils.getString(request.getParameter("subjectid"));
		if (!StringUtil.isEmpty(departid)) {
			DetachedCriteria dc = cq.getDetachedCriteria();
			DetachedCriteria dcDepart = dc.createCriteria("subjectEntity");
			dcDepart.add(Restrictions.eq("id", departid));
		}
		note.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, note, request.getParameterMap());
		this.noteService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除专题信息
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(SubjectEntity subject, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		subject = subjectService.getSubject(subject.getId());
		message = "专题信息删除成功";
		subject.setDeleted(Constants.DATA_DELETED);
		subjectService.saveOrUpdate(subject);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}

	/**
	 * 启用禁用专题信息
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "save")
	@ResponseBody
	public AjaxJson save(SubjectEntity subject, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		if (StringUtil.isNotEmpty(subject.getId())) {
			message = "专题信息更新成功";
			SubjectEntity t = subjectService.get(SubjectEntity.class, subject.getId());
			try {
				MyBeanUtils.copyBeanNotNull2Bean(subject, t);
				subjectService.saveOrUpdate(t);
				systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				e.printStackTrace();
				message = "专题信息更新失败";
			}
		} else {
			message = "专题信息更新失败";

		}
		j.setMsg(message);
		return j;
	}

	/**
	 * 专题信息列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(SubjectEntity subject, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(subject.getId())) {
			subject = subjectService.getEntity(SubjectEntity.class, subject.getId());
			req.setAttribute("subjectPage", subject);
		}
		return new ModelAndView("com/eht/subject/subject");
	}

	/**
	 * 验证邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		boolean flag = false;
		try {
			Pattern regex = Pattern.compile("[\\w[.-]]+@[\\w[.-]]+\\.[\\w]+");
			Matcher matcher = regex.matcher(email);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

}
