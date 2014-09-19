package com.eht.note.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.FileUtils;
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.SendMailUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.eht.common.constant.Constants;
import com.eht.common.page.PageResult;
import com.eht.common.util.AppContextUtils;
import com.eht.common.util.FilePathUtil;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.JsonUtil;
import com.eht.common.util.MD5FileUtil;
import com.eht.common.util.TreeUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.group.service.GroupService;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteTag;
import com.eht.note.service.AttachmentServiceI;
import com.eht.note.service.NoteServiceI;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.TreeData;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;
import com.eht.template.entity.TemplateEntity;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

/**
 * @Title: Controller
 * @Description: 条目信息
 * @author yuhao
 * @date 2014-03-31 10:13:10
 * @version V1.0
 */
@Controller
@RequestMapping("/noteController")
public class NoteController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(NoteController.class);
	
	@Autowired
	private NoteServiceI noteService;

	@Autowired
	private AttachmentServiceI attachmentService;

	@Autowired
	private ResourcePermissionService resourcePermissionService;

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private TagServiceI tagServiceI;

	@Autowired
	private SubjectServiceI subjectService;

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
	 * 根据条目id删除条目附件
	 * 
	 * @return
	 * @return
	 */
	@RequestMapping(value = "/front/removeAttach.dht")
	public @ResponseBody String removeAttach(String id) {
		String json = "";
		if (id != null && !id.isEmpty()) {
			AttachmentEntity attachment = attachmentService.getAttachment(id);
			if (attachment != null) {
				String filePath = attachment.getFilePath() + File.separator + attachment.getFileName();
				File file = new File(filePath);
				if (file.exists()) {
					file.delete();
				}
				file = null;
				System.gc();
				attachmentService.markDelAttachment(attachment);
			}
		}
		return json;
	}

	/**
	 * 根据条目id获取条目附件
	 * 
	 * @return
	 * @return
	 */
	@RequestMapping(value = "/front/getNodeAttach.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String getNodeAttach(String noteid) {
		String json = "[";
		List<AttachmentEntity> dataList = new ArrayList<AttachmentEntity>();
		if (noteid != null && !noteid.isEmpty()) {
			dataList = attachmentService.findAttachmentByNote(noteid, Constants.FILE_TYPE_NORMAL,"all");
			// 取当前专题判断是否多人
		}
		if (dataList.size() > 0) {
			for (int i = 0; i < dataList.size(); i++) {
				String id = dataList.get(i).getId();
				String fileName = dataList.get(i).getFileName();
				json += "{\"fileName\":\"" + fileName + "\",\"id\":\"" + id + "\"}";
				if ((dataList.size() > i + 1))
					json += ",";
			}
		}
		json += "]";
		return json;
	}

	/**
	 * 条目上传附件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/uploadNodeAttach.dht")
	public void uploadNodeAttach(HttpServletRequest request, String[] noteTagId, HttpServletResponse response) throws Exception {
		String json = "";
		// 存放路径
		String noteid = request.getParameter("noteid"); 
		//处理session
		AccountEntity user = accountService.getUser4Session(request.getParameter("jsessionid"));
		request.setAttribute("jsessionid", request.getParameter("jsessionid"));
		
		 request.getSession(false).setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);
		
		
		// 来自
		String dirId = request.getParameter("dirId");
		String subjectId = request.getParameter("subjectId");
		NoteEntity nodeEntity = null;
		
		//如果没有node数据则新插入一条
		nodeEntity = noteService.getNote(noteid);
		if(nodeEntity==null){
			nodeEntity = new NoteEntity();
			nodeEntity.setId(noteid);
			nodeEntity.setContent("");
			nodeEntity.setSubjectId(subjectId);
			this.saveNote(nodeEntity, noteTagId, request);
		}
		
		if (noteid != null && !noteid.isEmpty()) {
			nodeEntity = noteService.getNote(noteid);
			if (nodeEntity != null) {
				dirId = nodeEntity.getDirId();
			}
		} else {
			noteid = null;
		}
		String realPath = FilePathUtil.getFileUploadPath(nodeEntity, dirId);
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
				MultipartFile mf = entity.getValue();// 获取上传文件对象
				String fileName = mf.getOriginalFilename();// 获取文件名
				String extend = FileUtils.getExtend(fileName);// 获取文件扩展名
				String fileZipName = FileUtils.getFilePrefix(fileName)+".zip";//压缩后名
				// 判断类型
				if (".exe.com.bat.sh".indexOf(extend) >= 0) {
					json = "{success:false,msg:'您不能上传后缀为.exe .com .bat .sh的文件！'}";
				} else {
					List l = attachmentService.findAttachmentByFileName(fileName,noteid);
					if (l.size() > 0) {
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");// 设置日期格式
						df.format(new Date());
						fileName = FileUtils.getFilePrefix(fileName);
						fileName += "_重复文件_" + df.format(new Date()).toString() + "_."+ extend;
					}
					File file = new File(realPath);
					String savePath = realPath + "\\" + fileZipName;// 文件保存全路径
					File savefile = new File(savePath);

					// 判断是否已存在
					if (!file.exists()) {
						file.mkdirs();// 创建根目录
					}
					// FileCopyUtils.copy(mf.getBytes(), savefile);
					//FileToolkit.copyFileFromStream(mf.getInputStream(), savefile, true);
					FileToolkit.copyFileFromStreamToZIP(mf.getInputStream(), savefile, true,fileName);
					// 根据md5查询是否
					// String md5 = MD5FileUtil.getFileMD5String(savefile);
					String md5 = null;
					AttachmentEntity newAttach = new AttachmentEntity();

					newAttach.setId(UUID.randomUUID().toString().replace("-", ""));
					newAttach.setFileName(fileName);
					newAttach.setSuffix(extend);
					newAttach.setNoteId(noteid);
					newAttach.setDirectoryId(dirId);
					newAttach.setFilePath(realPath);
					newAttach.setMd5(md5);
					newAttach.setTranSfer(mf.getSize());
					newAttach.setStatus(Constants.FILE_TRANS_COMPLETED);
					newAttach.setDeleted(Constants.DATA_NOT_DELETED);
					newAttach.setCreateUser(user.getId());
					newAttach.setCreateTime(new Date());

					attachmentService.addAttachment(newAttach);
					json = "{success:true,filename:'" + newAttach.getFileName() + "',filecode:'" + newAttach.getId() + "'}";
				}
				try {
					mf.getInputStream().close();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			json = "{success:false,msg:'上传失败！'}";
			e.printStackTrace();
		}
		response.getWriter().write(json);
	}

	/**
	 * 条目下载附件
	 * 
	 * @return
	 */

	@RequestMapping(value = "/front/downloadNodeAttach.dht")
	public void downloadNodeAttach(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		String attachmentId = request.getParameter("id");
		AttachmentEntity attachment = attachmentService.getAttachment(attachmentId);
		String fileName =  FileUtils.getFilePrefix(attachment.getFileName())+".zip";
		try {
			File file = new File(attachment.getFilePath() + File.separator + fileName);
			FileInputStream fis = new FileInputStream(file);
			int length = fis.available();
			byte[] b = new byte[length];
			fis.read(b);

			response.setHeader("Content-Disposition", "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");
			response.addHeader("Content-Length", "" + length);
			OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
			outputStream.write(b);
			outputStream.flush();
			outputStream.close();
			fis.close();
		} catch (Exception e) {
			response.getWriter().write("您请求的 《" + java.net.URLEncoder.encode(fileName, "UTF-8") + "》 文件已不存在！");
		}
	}

	/**
	 * 选择专题标签
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/treeData.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody
	String treeData(String subjectid, String parentid) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		// 取当前专题判断是否多人
		SubjectEntity subject = subjectService.getSubject(subjectid);
		AccountEntity user = accountService.getUser4Session();
		// 默认个人专题
		int subjectType = Constants.SUBJECT_TYPE_P;
		if (subject != null) {
			subjectType = subject.getSubjectType();
		}
		List<TagEntity> list = tagServiceI.findTagByisGroup(subjectType, user.getId(), subjectid);
		// 个人专题菜单部分
		dataList = tagServiceI.buildTagTreeJson(null, list);
		return JSONHelper.collection2json(TreeUtils.buildTreeData(dataList));
	}

	/**
	 * 编辑标签
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/treeDataEdit.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String treeDataEdit(String subjectid, String parentid, String noteId) {
		List<TreeData> dataList = new ArrayList<TreeData>();
		// 取当前专题判断是否多人
		SubjectEntity subject = subjectService.getSubject(subjectid);
		AccountEntity user = accountService.getUser4Session();
		if (subject != null) {
			List<TagEntity> list = tagServiceI.findTagByisGroup(subject.getSubjectType(), user.getId(), subjectid);
			
			List<String> tagList = tagServiceI.findTagIdsByNote(noteId);
			
			dataList = tagServiceI.buildTagTreeJson(null, list, tagList);
		}

		List<TreeData> treeDataList = TreeUtils.buildTreeData(dataList);
		TreeData tagP = new TreeData();
		tagP.setDataType("TAG");
		tagP.setId("tag_personal");
		tagP.setName("添加标签");
		tagP.setpId(Constants.SUBJECT_PID_P);
		tagP.setIcon(AppContextUtils.getContextPath() + "/webpage/front/images/tree/page_add.png");
		treeDataList.add(0, tagP);

		return JSONHelper.collection2json(treeDataList);
	}

	/**
	 * 获取标签
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/getTag.dht")
	public void getTag(HttpServletResponse response, String subjectid, String parentid) {
		// 取当前专题判断是否多人
		SubjectEntity subject = subjectService.getSubject(subjectid);

		AccountEntity user = accountService.getUser4Session();
		List<TagEntity> list = tagServiceI.findTagByisGroup(subject.getSubjectType(), user.getId(), subjectid);

		StringBuffer str = new StringBuffer("{\"data\":[");
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				TagEntity o = list.get(i);
				str.append("{\"id\":\"" + o.getId() + "\",\"name\":\"" + o.getName() + "\"}");
				if ((i + 1 < list.size()))
					str.append(",");
			}
		}
		str.append("]}");
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(str.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取专题所有共享用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/getGroupsEmailAll.dht")
	public void getGroupsEmailAll(HttpServletResponse response) {
		List<AccountEntity> accountList = noteService.getShareEmail();
		StringBuffer str = new StringBuffer("{\"data\":[");
		if (accountList != null && accountList.size() > 0) {
			for (int i = 0; i < accountList.size(); i++) {
				AccountEntity o = accountList.get(i);
				str.append("{\"name\":\"" + o.getUsername() + "\",\"email\":\"" + o.getEmail() + "\"}");
				if ((i + 1 < accountList.size()))
					str.append(",");
			}
		}
		str.append("]}");
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(str.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取专题共享用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/getGroupsEmail.dht")
	public void getGroupEmail(HttpServletResponse response, String searchField) {
		List<AccountEntity> accountList = noteService.getShareEmail(searchField);
		StringBuffer str = new StringBuffer("{\"data\":[");
		if (accountList != null && accountList.size() > 0) {
			for (int i = 0; i < accountList.size(); i++) {
				AccountEntity o = accountList.get(i);
				str.append("{\"name\":\"" + o.getUsername() + "\",\"email\":\"" + o.getEmail() + "\"}");
				if ((i + 1 < accountList.size()))
					str.append(",");
			}
		}
		str.append("]}");
		System.out.println(str);
		try {
			response.getWriter().write(str.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取专题共享用户(带分页)
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/getGroupsEmailPage.dht")
	public void getGroupEmailPage(HttpServletResponse response, String searchField, String start) {
		int m = -1;
		try {
			m = Integer.parseInt(start);
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
		StringBuffer str = new StringBuffer("{emailObj:[");
		if (m != -1) {
			List<AccountEntity> accountList = noteService.getShareEmailbyPage(searchField, m, 10);
			if (accountList != null && accountList.size() > 0) {
				for (int i = 0; i < accountList.size(); i++) {
					AccountEntity o = accountList.get(i);
					str.append("{name:\"" + o.getUsername() + "\",email:\"" + o.getEmail() + "\"}");
					if ((i + 1 < accountList.size()))
						str.append(",");
				}
			}
			str.append("]},{endPage:\"" + (m + 10) + "\"}");
			try {
				response.getWriter().write(str.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取专题共享用户
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/sendShareEmail.dht")
	public ModelAndView sendShareEmail(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("front/note/emailStatus");
		ModelMap mmp = new ModelMap();
		String msg = "";
		String noteid = request.getParameter("noteid");
		String[] shareEmails = request.getParameterValues("shareEmails");
		NoteEntity note = noteService.getNote(noteid);
		try {
			msg = "发送成功请查看邮件";
			List<String> listEmail = Arrays.asList(shareEmails);
			if (!listEmail.isEmpty()) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("subject", note.getTitle());
				map.put("content", note.getContent());
				map.put("link", "");
				SendMailUtil.sendFtlMail(listEmail, "E划通  " + note.getTitle(), "mailtemplate/testmail.ftl", map);
			}
			systemService.addLog(msg, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
			msg = "true";
		} catch (Exception e) {
			e.printStackTrace();
			msg = "false";
		}
		mmp.put("msg", msg);
		mv.addAllObjects(mmp);
		return mv;
	}

	/**
	 * 前台首页跳转
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/noteIndex.dht")
	public ModelAndView noteIndex(String searchInput, String subjectId, String dirId, String selectDirId, String tagId, String deleted, String topNodeId, boolean newEnable,String userId) {
		ModelAndView mv = new ModelAndView("front/note/noteindex");
		mv.addObject("subjectId", subjectId);
		mv.addObject("dirId", dirId);
		mv.addObject("selectDirId", selectDirId);
		mv.addObject("tagId", tagId);
		mv.addObject("deleted", deleted);
		mv.addObject("topNodeId", topNodeId);
		mv.addObject("newEnable", newEnable);
		mv.addObject("userId", userId);
		return mv;
	}

	/**
	 * 前台首页条目查询
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/noteList.dht")
	public ModelAndView noteList(String searchInput, String subjectId, String dirId, String tagId, String orderField, int deleted, String topNodeId, HttpServletRequest request) {
		AccountEntity user = accountService.getUser4Session();
		if (StringUtil.isEmpty(orderField)) {
			orderField = "createTime";
		}
		List<NoteEntity> noteList = null;
		if (Constants.SUBJECT_PID_M.equals(topNodeId)) {
			// 多人专题
			if (deleted == Constants.DATA_DELETED) {
				noteList = noteService.findNotesInRecycleByParams(user.getId(), subjectId, dirId, searchInput, tagId, orderField, Constants.SUBJECT_TYPE_M);
			} else {
				noteList = noteService.findMNotesByParams(subjectId, dirId, searchInput, tagId, orderField, user.getId(),request.getParameter("userId"));
			}
		} else {
			// 个人专题
			if (deleted == Constants.DATA_DELETED) {
				noteList = noteService.findNotesInRecycleByParams(user.getId(), subjectId, dirId, searchInput, tagId, orderField, Constants.SUBJECT_TYPE_P);
			} else {
				noteList = noteService.findNotesByParams(user.getId(), subjectId, dirId, searchInput, tagId, orderField);
			}
		}

		ModelAndView mv = new ModelAndView("front/note/notelist");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("subjectId", subjectId);
		map.put("noteList", noteList);
		map.put("searchInput", searchInput);
		map.put("dirId", dirId);
		map.put("tagId", tagId);
		map.put("orderField", orderField);
		map.put("firstNodeId", noteList != null && noteList.size() > 0 ? noteList.get(0).getId() : null);

		mv.addAllObjects(map);
		return mv;
	}

	@RequestMapping(value = "/front/saveNote.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String saveNote(NoteEntity note, String[] noteTagId, HttpServletRequest request) throws IOException {
		NoteEntity oldNote = noteService.getNote(note.getId());
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		noteService.saveNoteHtml(note);
		if (oldNote == null) {
			note.setDeleted(Constants.DATA_NOT_DELETED);
			note.setVersion(1);
			note.setCreateUser(user.getId());
			note.setCreateTime(new Date());
			if (StringUtil.isEmpty(note.getDirId())) {
				note.setDirectoryEntity(null);
			}
			note.setDirId(StringUtil.isEmpty(note.getDirId()) ? null : note.getDirId());
			if (note.getContent() != null) {
				note.setMd5(MD5FileUtil.getMD5String(note.getContent()));
			}
			noteService.addNote(note);
			
			//保存条目标签关系
			if(noteTagId != null && noteTagId.length > 0){
				tagServiceI.saveNoteTags(note.getId(), noteTagId);
			}
			return JsonUtil.bean2json(note);
		} else {
			oldNote.setTitle(note.getTitle());

			String newMd5 = MD5FileUtil.getMD5String(note.getContent());
			if (!oldNote.getMd5().equals(newMd5)) {
				oldNote.setContent(note.getContent());
			}
			oldNote.setDirId(StringUtil.isEmpty(note.getDirId()) ? null : note.getDirId());
			oldNote.setParentTag(note.getParentTag());
			oldNote.setRootTag(note.getRootTag());
			oldNote.setSubjectId(StringUtil.isEmpty(note.getSubjectId()) ? null : note.getSubjectId());
			oldNote.setTagId(StringUtil.isEmpty(note.getTagId()) ? null : note.getTagId());
			oldNote.setUpdateUser(user.getId());
			oldNote.setUpdateTime(new Date());
			noteService.updateNote(oldNote, true);
			
			//保存条目标签关系
			//原标签集合
			List<String> tagList = tagServiceI.findTagIdsByNote(note.getId());
			if(tagList != null && !tagList.isEmpty()){
				if(noteTagId != null && noteTagId.length > 0){
					//新标签集合
					List<String> newList = new ArrayList<String>(noteTagId.length);
					for(String tagId : noteTagId){
						newList.add(tagId);
					}
					
					//需删除的标签
					for(String tagId : tagList){
						if(!newList.contains(tagId)){
							NoteTag noteTag = tagServiceI.findNoteTag(note.getId(), tagId);
							tagServiceI.deleteNoteTag(noteTag);
						}
					}
					
					//需添加的标签
					for(String tagId : newList){
						if(!tagList.contains(tagId)){
							NoteTag noteTag = new NoteTag();
							noteTag.setId(UUIDGenerator.uuid());
							noteTag.setNoteId(note.getId());
							noteTag.setTagId(tagId);
							tagServiceI.saveNoteTag(noteTag);
						}
					}
				}else{
					for(String tagId : tagList){
						NoteTag noteTag = tagServiceI.findNoteTag(note.getId(), tagId);
						tagServiceI.deleteNoteTag(noteTag);
					}
				}
			}else{
				if(noteTagId != null && noteTagId.length > 0){
					for(String tagId : noteTagId){
						NoteTag noteTag = new NoteTag();
						noteTag.setId(UUIDGenerator.uuid());
						noteTag.setNoteId(note.getId());
						noteTag.setTagId(tagId);
						tagServiceI.saveNoteTag(noteTag);
					}
				}
			}
			//tagServiceI.saveNoteTags(note.getId(), noteTagId);
			return JsonUtil.bean2json(oldNote);
		}

	}

	/**
	 * 删除条目
	 * 
	 * @param id
	 *            条目ID
	 * @param deleted
	 *            是否真删除：1 直接删除 0 标记删除
	 * @param request
	 * @return
	 */
	@RequestMapping("/front/deleteNote.dht")
	public @ResponseBody
	String deleteNote(String id, int deleted, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		NoteEntity note = noteService.getNote(id);
		if (deleted == 1) {
			tagServiceI.deleteNoteTagByNoteId(id);
			noteService.deleteNote(note);
		} else {
			note.setUpdateUser(user.getId());
			note.setUpdateTime(new Date());
			noteService.markDelNote(note);
		}
		return null;
	}

	/**
	 * 获取条目已读、未读，是否有附件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/noteStatus.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody
	String noteStatus(String id, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		boolean result = noteService.noteIsRead(id, user.getId());
		int count = attachmentService.countAttachmentByNote(id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("isRead", String.valueOf(result));
		map.put("attachmentCount", String.valueOf(count));

		return JSONHelper.map2json(map);

	}

	/**
	 * 根据ID查询条目并返回
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/front/loadNote.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String loadNote(String id, HttpServletRequest request) {
		AccountEntity user = accountService.getUser4Session();
		NoteEntity note = noteService.getNote(id);
		if (!noteService.noteIsRead(id, user.getId())) {
			noteService.noteRead(id, user.getId());
		}
		List<AttachmentEntity> attaList = attachmentService.findAttachmentByNote(note.getId(), Constants.FILE_TYPE_NORMAL,"current");
		note.setAttachmentEntitylist(attaList);

		String residential = noteService.getNoteResidential(note.getDirectoryEntity(), note.getSubjectEntity()); // 还是个人专题
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> actionMap = resourcePermissionService.findSubjectPermissionsByUser(user.getId(), note.getSubjectId(), note.getId());
		//是否有更多
		String isMore = "false";
		if(attaList.size()>7){
			isMore="true";
			attaList.remove(attaList.size()-1);
		}
		
		map.put("type", note.getSubjectEntity().getSubjectType() + "");
		map.put("note", note);
		map.put("version", note.getVersion());
		map.put("action", actionMap);
		map.put("attaList", attaList);
		map.put("residential", residential);
		map.put("isMore", isMore);
		return JsonUtil.map2json(map);

	}

	@RequestMapping(value = "/front/loadNoteTags.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String loadNoteTags(String noteId) {
		List<TagEntity> tagList = tagServiceI.findTagByNote(noteId);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		if(tagList != null && !tagList.isEmpty()){
			for(TagEntity tag : tagList){
				
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", tag.getId());
				map.put("name", tag.getName());
				map.put("subjectId", tag.getSubjectId());
				if(!StringUtil.isEmptyOrBlank(tag.getParentId())){
					map.put("tagEntity", tagServiceI.getTag(tag.getParentId()));
				}else{
					map.put("tagEntity", null);
				}
				list.add(map);
			}
		}
		// 不转成map，转json有问题
		System.out.println(JsonUtil.list2json(list));
		return JsonUtil.list2json(list);
	}
	
	/**
	 * 根据ID查询条目并返回
	 * searchtype  （all所有，current前7条
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/front/loadAttachment.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String loadAttachment(String id,String searchtype, HttpServletRequest request) {
		NoteEntity note = noteService.getNote(id); 
		Map<String, Object> map = new HashMap<String, Object>();
		if(note!=null){
			List<AttachmentEntity> attaList = attachmentService.findAttachmentByNote(note.getId(), Constants.FILE_TYPE_NORMAL,searchtype);
			note.setAttachmentEntitylist(attaList);
			//是否有更多
			String isMore = "false";
			//查询附件，默认前七条。
			if(attaList.size()>7&&!searchtype.equals("all")){
				isMore="true";
				attaList.remove(attaList.size()-1);
			}
			//显示附件  【更多】按钮。
			if(searchtype!=null&&searchtype.equals("all")&&attaList.size()>0){
				isMore="true";
			}
			map.put("attaList", attaList);
			map.put("isMore", isMore);
			}
			return JsonUtil.map2json(map);

	}

	@RequestMapping(value = "/front/restoreNote.dht", produces = { "application/json;charset=UTF-8" })
	public @ResponseBody String restoreNote(String id, HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		NoteEntity note = noteService.getNote(id);
		note.setUpdateUser(user.getId());
		note.setUpdateTime(new Date());
		List<String> list = new ArrayList<String>();
		noteService.restoreNote(note, list);
		StringBuffer str = new StringBuffer("");
		StringBuffer dirId = new StringBuffer("");
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				dirId.append(",");
			}
			dirId.append(list.get(i));
		}
		str.append("{\"subjectId\":\"" + note.getSubjectId() + "\",\"dirId\":\"" + dirId.toString() + "\"}");
		return str.toString();
	}

	/**
	 * 条目黑名单列表
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/blackListNote.dht")
	public ModelAndView blackListNote(HttpServletRequest request, @ModelAttribute PageResult pageResult) {
		ModelMap mmap = new ModelMap();
		ModelAndView mv = new ModelAndView("front/note/blackListNote");
		String subjectid = request.getParameter("subjectid");
		String nodeId = request.getParameter("nodeId");
		SubjectEntity subjectEntity = subjectService.get(SubjectEntity.class, subjectid);
		List<RoleUser> list = roleService.findSubjectUsers(subjectEntity.getId(), pageResult);
		for (RoleUser roleUser : list) {
			roleUser.setBlackList(groupService.checkNoteUser(roleUser.getUserId(), nodeId));
		}
		mmap.put("subjectid", subjectid);
		mmap.put("nodeId", nodeId);
		mmap.put("pageResult", pageResult);
		mv.addAllObjects(mmap);
		return mv;
	}

	/**
	 * 条目成员加入黑名单(如果成员是黑名单那么点击移除)
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/addblackListNote.dht")
	@ResponseBody
	public AjaxJson addblackListNote(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String userId = request.getParameter("userId");
		String nodeId = request.getParameter("nodeId");
		try {
			boolean c = groupService.checkNoteUser(userId, nodeId);
			if (c) {
				noteService.removeUser4blacklist(userId, nodeId);
			} else {
				noteService.blacklistedUser(userId, nodeId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;
	}

	/**
	 * 根据ID查询条目历史版本
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/historyNote.dht")
	public ModelAndView historyNote(HttpServletRequest request, @ModelAttribute PageResult pageResult) {

		ModelMap mmap = new ModelMap();
		noteService.getHistoryNote(request.getParameter("nodeId"), pageResult);
		ModelAndView mv = new ModelAndView("front/note/historyNoteList");
		mmap.put("pageResult", pageResult);
		mmap.put("nodeId", request.getParameter("nodeId"));
		mv.addAllObjects(mmap);
		return mv;

	}

	/**
	 * 返回一个条目的UUid
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/noteUUid.dht")
	@ResponseBody
	public AjaxJson noteUUid(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try {
			j.setObj(UUIDGenerator.uuid());
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;
	}

	/**
	 * 根据ID还原条目历史版本
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/shapeNote.dht")
	@ResponseBody
	public AjaxJson shapeNote(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		String cotent = null;
		try {
			AccountEntity user = (AccountEntity) request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
			cotent = noteService.shapeNote(request.getParameter("id"), user.getId());
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		j.setObj(cotent);
		return j;
	}

	/**
	 * 条目信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "note")
	public ModelAndView note(HttpServletRequest request) {

		return new ModelAndView("com/eht/note/noteList");
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
	public void datagrid(NoteEntity note, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(NoteEntity.class, dataGrid);
		// 查询条件组装器
		note.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, note, request.getParameterMap());
		this.noteService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除条目信息
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(NoteEntity note, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		message = "条目信息删除成功";
		note = systemService.getEntity(TemplateEntity.class, note.getId());
		tagServiceI.deleteNoteTagByNoteId(note.getId());
		noteService.delete(note);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}

	/**
	 * 条目信息列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(NoteEntity note, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(note.getId())) {
			note = noteService.getEntity(NoteEntity.class, note.getId());
			req.setAttribute("notePage", note);
		}
		return new ModelAndView("com/eht/note/note");
	}

	/**
	 * 专题成员条目数量
	 * 
	 * @return
	 */
	@RequestMapping(value="/front/showcount.dht", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String  showcount(HttpServletRequest req) {
		long c=tagServiceI.findCoutNoteforRemenber(req.getParameter("subjectId"), req.getParameter("userId"));
		String json = "{\"userId\":\"" + req.getParameter("userId") + "\"" + ",\"subjectId\":\"" + req.getParameter("subjectId") + "\"" + ",\"total\":\"" + c + "\""+ ",\"tId\":\"" + req.getParameter("tid") + "\"" + "}";
		return json;
	}
	
}
