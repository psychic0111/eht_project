package com.eht.message.controller;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
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
import org.springframework.web.servlet.view.RedirectView;
import com.eht.common.constant.Constants;
import com.eht.common.page.PageResult;
import com.eht.common.util.UUIDGenerator;
import com.eht.message.entity.MessageEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.subject.entity.SubjectEntity;
import com.eht.user.entity.AccountEntity;


/**   
 * @Title: Controller
 * @Description: 系统消息提醒
 * @author yuhao
 * @date 2014-04-02 11:50:18
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/messageController")
public class MessageController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(MessageController.class);

	@Autowired
	private MessageServiceI messageService;
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
	 * 系统消息提醒列表[前台]页面跳转、查询
	 * 
	 * @return
	 */
	@RequestMapping(value="/front/messageList")
	public ModelAndView messageList(String msgType, String content, String orderField, String orderType, HttpServletRequest request, @ModelAttribute PageResult pageResult) {
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			
			ModelMap mmap = new ModelMap();
			if(pageResult == null){
				pageResult = new PageResult();
			}
			
			List<MessageEntity> msgList = null;
			Integer type = null;   //消息类型
			if(msgType != null && !msgType.equals("")){
				type = Integer.valueOf(msgType);
			}
			
			//默认时间排序
			if(orderField == null || "".equals(orderField)){
				orderField = "createTime";
			}
			//默认降序排序
			if(orderType == null || "".equals(orderType)){
				orderType = "DESC";
			}
			long total = 0;
			if(type != null){
				msgList = messageService.findUserMessages(user.getId(), type, content, orderField, orderType, pageResult.getPageSize(), pageResult.getPageNo());
				total = messageService.countUserMessages(user.getId(), type, content);
				pageResult.setTotal(total);
			}else{
				msgList = messageService.findNoReadMessageByType(user.getId(), null, content, orderField, orderType, pageResult.getPageSize(), pageResult.getPageNo());
				total = messageService.countNoReadMessageByType(user.getId(), type, content);
				pageResult.setTotal(total);
			}
			pageResult.setRows(msgList);
			mmap.put("pageResult", pageResult);
			mmap.put("orderType", orderType);
			mmap.put("msgType", msgType);
			mmap.put("content", content);
			mmap.put("orderField", orderField);
			ModelAndView mv = new ModelAndView("front/message/message");
			mv.addAllObjects(mmap);
			return mv;
		}
		return new ModelAndView(new RedirectView("/center/login.dht", true));
	}
	
	/**
	 * 标记消息已读
	 * @param id
	 * @param msgType
	 * @param content
	 * @param orderField
	 * @param orderType
	 * @param request
	 * @param pageResult
	 * @return
	 */
	@RequestMapping(value="/front/messageMark.dht")
	public ModelAndView messageMark(String[] id, String msgType, String content, String orderField, String orderType, HttpServletRequest request, @ModelAttribute PageResult pageResult) {
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			if(id != null && id.length > 0){
				for(String mid : id){
					if(mid != null && !mid.equals("")){
						messageService.markReadMessage(user.getId(), mid);
					}
				}
			}
			return messageList(msgType, content, orderField, orderType, request, pageResult);
		}
		return new ModelAndView(new RedirectView("/center/login.dht", true));
	}
	
	
	/**
	 * 标记消息已读
	 * @param id
	 * @param msgType
	 * @param content
	 * @param orderField
	 * @param orderType
	 * @param request
	 * @param pageResult
	 * @return
	 */
	@RequestMapping(value="/front/sendMessag.dht")
	public ModelAndView messageMark() {
		ModelAndView mv = new ModelAndView("front/message/commentList");
		return mv;
	}
	
	/**
	 * 前台添加专题
	 * 
	 * @return
	 */
	@RequestMapping("/front/sendMessagDo.dht")
	@ResponseBody
	public AjaxJson sendMessagDo(HttpServletRequest request) {
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		AjaxJson j = new AjaxJson();
		try {
			String content=request.getParameter("content");
			Pattern p = Pattern.compile("@[^@&&[^\\s]]+[\\s]");
	        Matcher m = p.matcher(content);  
	        List<String> list=new ArrayList<String>();
	        while(m.find()) {  
	        	list.add(m.group());
	        }  
	        messageService.saveMessages(content, list, user.getId());
		} catch (Exception e) {
			e.printStackTrace();
			j.setMsg("操作失败");
			j.setSuccess(false);
			return j;
		}
		return j;

	}
	/**
	 * 前台删除系统消息
	 * 
	 * @return
	 */
	@RequestMapping(value = "/front/messageDel")
	public ModelAndView deleteMessage(String messageId, String msgType, String content, String orderField, String orderType, HttpServletRequest request, @ModelAttribute PageResult pageResult) {
		Object obj = request.getSession(false).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(obj != null){
			AccountEntity user = (AccountEntity) obj;
			messageService.deleteUserMessage(messageId, user.getId());
		
			return messageList(msgType, content, orderField, orderType, request, pageResult);
		}
		return new ModelAndView(new RedirectView("/center/login.dht", true));
	}
	
	/**
	 * 系统消息提醒列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "message")
	public ModelAndView message(HttpServletRequest request) {
		return new ModelAndView("com/eht/message/messageList");
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
	public void datagrid(MessageEntity message,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(MessageEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, message, request.getParameterMap());
		this.messageService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 后台删除系统消息提醒
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(MessageEntity messages, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		messages = systemService.getEntity(MessageEntity.class, messages.getId());
		messageService.deleteMessage(messages);
		message = "系统消息提醒删除成功";
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}
	

	/**
	 * 系统消息提醒列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(MessageEntity message, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(message.getId())) {
			message = messageService.getEntity(MessageEntity.class, message.getId());
			req.setAttribute("messagePage", message);
		}
		return new ModelAndView("com/eht/message/message");
	}
}
