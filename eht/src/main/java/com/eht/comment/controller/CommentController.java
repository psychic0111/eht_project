package com.eht.comment.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import com.eht.comment.entity.CommentEntity;
import com.eht.comment.service.CommentServiceI;
import com.eht.common.constant.Constants;
import com.eht.common.util.UUIDGenerator;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;


/**   
 * @Title: Controller
 * @Description: 评论管理
 * @author  yuhao
 * @date 2014-04-01 13:58:52
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/commentController")
public class CommentController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CommentController.class);

	@Autowired
	private CommentServiceI commentService;
	@Autowired
	private SystemService systemService;
	
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 前台条目评论列表
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/findCommentByNote.dht")
	public ModelAndView findCommentByNote(HttpServletRequest request){
		ModelMap mmap = new ModelMap();
		ModelAndView mv = new ModelAndView("front/comment/commentList");
		List<CommentEntity> commentEntityList=	commentService.findCommentByNote(request.getParameter("noteId"));
		mmap.put("commentEntityList", commentEntityList);
		mmap.put("noteId", request.getParameter("noteId"));
		mv.addAllObjects(mmap);
		return mv;
	}
	
	/**
	 * 前台条目评论添加
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/commentAdd.dht")
	@ResponseBody
	public AjaxJson commentAdd(HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		AjaxJson j = new AjaxJson();
		String content = request.getParameter("comment_content");
		Pattern p = Pattern.compile("@[^@&&[^\\s]]+[\\s]");
		Matcher m = p.matcher(content);  
		List<String> list=new ArrayList<String>();
		while(m.find()) {  
			list.add(m.group());
		    
		}  
		CommentEntity comment =new CommentEntity();
		comment.setId(UUIDGenerator.uuid());
		comment.setAccout(list);
		comment.setCreateUser(user.getId());
		comment.setCreateTime(new Date());
		content = content.replace("\\r\\n", "<br/>");
		comment.setContent(content);
		comment.setDeleted(Constants.DATA_NOT_DELETED);
		comment.setNoteId(request.getParameter("noteId"));
		try {
			commentService.addComment(comment);
		} catch (Exception e) {
			j.setMsg("操作失败");
			j.setSuccess(false);
		}
	
		return j;
	}
	/**
	 * 前台条目评论删除
	 * @param id
	 * @return
	 */
	@RequestMapping("/front/commentDel.dht")
	@ResponseBody
	public AjaxJson commentDel(HttpServletRequest request){
		AjaxJson j = new AjaxJson();
		try {
			CommentEntity commentEntity=commentService.getComment(request.getParameter("Id"));
			commentService.deleteComment(commentEntity);
		} catch (Exception e) {
			j.setMsg("操作失败");
			j.setSuccess(false);
		}
		return j;
	}
	

	/**
	 * 评论管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "comment")
	public ModelAndView comment(HttpServletRequest request) {
		return new ModelAndView("com/eht/comment/commentList");
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
	public void datagrid(CommentEntity comment,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(CommentEntity.class, dataGrid);
		//查询条件组装器
		comment.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, comment, request.getParameterMap());
		this.commentService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除评论管理
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(CommentEntity comment, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		comment = systemService.getEntity(CommentEntity.class, comment.getId());
		comment.setDeleted(Constants.DATA_DELETED);
		message = "评论管理删除成功";
		commentService.saveOrUpdate(comment);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}



	/**
	 * 评论管理列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(CommentEntity comment, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(comment.getId())) {
			comment = commentService.getEntity(CommentEntity.class, comment.getId());
			req.setAttribute("commentPage", comment);
		}
		return new ModelAndView("com/eht/comment/comment");
	}
}
