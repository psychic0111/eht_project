package com.eht.mail.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.SendMailUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import com.eht.mail.entity.SendMailEntity;
import com.eht.mail.service.SendMailServiceI;
import com.eht.user.entity.AccountEntity;


/**   
 * @Title: Controller
 * @Description: 群发邮件管理
 * @author zhangdaihao
 * @date 2014-04-08 14:55:13
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/sendMailController")
public class SendMailController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SendMailController.class);

	@Autowired
	private SendMailServiceI sendMailService;
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
	 * 群发邮件管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "sendMail")
	public ModelAndView sendMail(HttpServletRequest request) {
		return new ModelAndView("com/eht/mail/sendMailList");
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
	public void datagrid(SendMailEntity sendMail,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(SendMailEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, sendMail, request.getParameterMap());
		this.sendMailService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除群发邮件管理
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(SendMailEntity sendMail, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		sendMail = systemService.getEntity(SendMailEntity.class, sendMail.getId());
		message = "群发邮件管理删除成功";
		sendMailService.delete(sendMail);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		
		j.setMsg(message);
		return j;
	}

	
	
	@RequestMapping(params = "accountList")
	public void accountList(AccountEntity account,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		account.setDeleted(0);
		CriteriaQuery cq = new CriteriaQuery(AccountEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, account, request.getParameterMap());
		this.systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}
	
	/**
	 * 群发邮件选择用户跳转页面
	 * 
	 * @return
	 */
	@RequestMapping(params = "users")
	public ModelAndView users() {
		return new ModelAndView("com/eht/mail/accountList");
	}
	
	/**
	 * 添加群发邮件管理
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "save")
	@ResponseBody
	public AjaxJson save(SendMailEntity sendMail, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
			try {
			message = "群发邮件管理添加成功";
			sendMailService.save(sendMail);
			String[] email =request.getParameter("email").split(",");
			SendMailUtil.sendCommonMail(email, sendMail.getTitle(), sendMail.getBody());
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				e.printStackTrace();
				message = "群发邮件管理添加失败";
			}
		j.setMsg(message);
		return j;
	}

	/**
	 * 群发邮件管理列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(SendMailEntity sendMail, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(sendMail.getId())) {
			sendMail = sendMailService.getEntity(SendMailEntity.class, sendMail.getId());
			req.setAttribute("sendMailPage", sendMail);
		}
		return new ModelAndView("com/eht/mail/sendMail");
	}
	
	@RequestMapping(params = "show")
	public ModelAndView show(SendMailEntity sendMail, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(sendMail.getId())) {
			sendMail = sendMailService.getEntity(SendMailEntity.class, sendMail.getId());
			req.setAttribute("sendMailPage", sendMail);
		}
		return new ModelAndView("com/eht/mail/showMail");
	}
}
