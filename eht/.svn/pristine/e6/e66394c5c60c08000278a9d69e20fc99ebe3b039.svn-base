package com.eht.template.controller;
import java.util.Date;
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
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import com.eht.common.constant.Constants;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;

/**   
 * @Title: Controller
 * @Description: 模板管理
 * @author yuhao
 * @date 2014-03-20 10:24:04
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/templateController")
public class TemplateController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(TemplateController.class);

	@Autowired
	private TemplateServiceI templateService;
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
	 * 模板管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "template")
	public ModelAndView template(HttpServletRequest request) {
		return new ModelAndView("com/eht/template/templateList");
	}

	/**
	 * 前台根据模板ID 查询树的joson数据
	 * @return
	 */
	@RequestMapping(value = "/front/findtemplate.dht", produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	public String findtemplate(HttpServletRequest request) {
		String id=	request.getParameter("id");
		TemplateEntity	template = systemService.getEntity(TemplateEntity.class, id);
		return template.getContent();
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
	public void datagrid(TemplateEntity template,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		template.setDeleted(Constants.DATA_NOT_DELETED);
		CriteriaQuery cq = new CriteriaQuery(TemplateEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, template, request.getParameterMap());
		this.templateService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除模板管理
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(TemplateEntity template, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		template = systemService.getEntity(TemplateEntity.class, template.getId());
		message = "模板管理删除成功";
		template.setDeleted(Constants.DATA_DELETED);
		templateService.saveOrUpdate(template);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}


	/**
	 * 查看模板管理页面
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(TemplateEntity template, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(template.getId())) {
			template = templateService.getEntity(TemplateEntity.class, template.getId());
			req.setAttribute("templatePage", template);
		}
		return new ModelAndView("com/eht/template/template");
	}
	
	/**
	 * 模板添加
	 * 
	 * @return
	 */
	@RequestMapping(params = "add")
	public ModelAndView add(HttpServletRequest req) {
		if (req.getParameter("id") != null) {
			req.setAttribute("templatePage", templateService.getEntity(TemplateEntity.class, req.getParameter("id")));
		}
		return new ModelAndView("com/eht/template/templateAdd");
	}
	
	/**
	 * 查看模板管理页面
	 * 
	 * @return
	 */
	@RequestMapping(params = "toAdd")
	@ResponseBody
	public AjaxJson toAdd(TemplateEntity template, HttpServletRequest req) {
		AjaxJson j = new AjaxJson();
		if (!StringUtil.isEmpty(template.getId())) {
			TemplateEntity entity = this.systemService.get(TemplateEntity.class, template.getId());
			try {
				message="操作成功";
				template.setContent(req.getParameter("josns"));
				MyBeanUtils.copyBeanNotNull2Bean(template, entity);
				templateService.updateEntitie(entity);
				systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				message="操作失败";
				e.printStackTrace();
			}
		
		}else {
			try {
			message="操作成功";
			template.setTemplateType(Constants.TEMPLATE_SYSTEM_DEFAULT);
			template.setContent(req.getParameter("josns"));
			template.setDeleted(Constants.DATA_NOT_DELETED);
			templateService.save(template);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			message="操作失败";
			e.printStackTrace();
		}
		}
		j.setMsg(message);
		return j;
	}
	
}
