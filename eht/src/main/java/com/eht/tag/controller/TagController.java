package com.eht.tag.controller;

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
import org.jeecgframework.core.util.JSONHelper;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import com.eht.common.constant.Constants;
import com.eht.common.util.UUIDGenerator;
import com.eht.tag.entity.TagEntity;
import com.eht.tag.service.TagServiceI;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Controller
 * @Description: 标签管理
 * @author zhangdaihao
 * @date 2014-04-01 16:22:12
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/tagController")
public class TagController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(TagController.class);

	@Autowired
	private TagServiceI tagService;
	
	@Autowired
	private SystemService systemService;
	
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@RequestMapping(value="/front/saveTag", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String saveTag(TagEntity tag, HttpServletRequest request){
		AccountEntity user = (AccountEntity) request.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		if(tag.getId() == null || "".equals(tag.getId())){
			tag.setId(UUIDGenerator.uuid());
			tag.setCreateTime(new Date());
			tag.setCreateUser(user.getId());
			tag.setDeleted(Constants.DATA_NOT_DELETED);
			//如果是多人专题则包含了 tag_subject_
			int k = tag.getParentId()==null?-1:tag.getParentId().indexOf("tag_subject_");
			if(tag.getSubjectId().equals("")||tag.getSubjectId()==null){
				tag.setSubjectId(null);
			}
			if(k>=0){
				tag.setParentId(null);
			}
			tagService.addTag(tag);
		}else{
			TagEntity tagOld = tagService.get(TagEntity.class, tag.getId());
			tagOld.setName(tag.getName());
			tagOld.setUpdateTime(new Date());
			tagOld.setUpdateUser(user.getId());
			tagService.updateTag(tagOld);
		}
		return JSONHelper.bean2json(tag);
	}
	
	@RequestMapping(value="/front/deleteTag", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String deleteTag(String id, HttpServletRequest request){
		try{
			TagEntity tagOld = tagService.get(TagEntity.class, id);
			tagService.deleteTag(tagOld);
			return "true";
		}catch(Exception e){
			e.printStackTrace();
		}
		return "false";
	}
	
	/**
	 * 标签管理列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "tag")
	public ModelAndView tag(HttpServletRequest request) {
		return new ModelAndView("com/eht/tag/tagList");
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
	public void datagrid(TagEntity tag,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(TagEntity.class, dataGrid);
		//查询条件组装器
		tag.setDeleted(Constants.DATA_NOT_DELETED);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, tag, request.getParameterMap());
		this.tagService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除标签管理
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(TagEntity tag, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		tag = systemService.getEntity(TagEntity.class, tag.getId());
		tag.setDeleted(Constants.DATA_DELETED);
		message = "标签管理删除成功";
		tagService.saveOrUpdate(tag);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		
		j.setMsg(message);
		return j;
	}


	/**
	 * 标签管理列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(TagEntity tag, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(tag.getId())) {
			tag = tagService.getEntity(TagEntity.class, tag.getId());
			req.setAttribute("tagPage", tag);
		}
		return new ModelAndView("com/eht/tag/tag");
	}
	
	/**
	 * 标签所包含条目数量
	 * 
	 * @return
	 */
	@RequestMapping(value="/front/showcount.dht", produces={"application/json;charset=UTF-8"})
	public @ResponseBody String  showcount(HttpServletRequest req) {
		String id=req.getParameter("id");
		return tagService.findCoutNoteforTags(id)+"";
	}
	
}
