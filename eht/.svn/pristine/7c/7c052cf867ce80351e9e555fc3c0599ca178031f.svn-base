package com.eht.template.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.template.entity.TemplateEntity;

public interface TemplateServiceI extends CommonService{
	
	/**
	 * 查询模板
	 */
	public TemplateEntity getTemplate(String templateId);
	/**
	 * 添加模板
	 * @param template
	 * @return
	 */
	public String addTemplate(TemplateEntity template);
	/**
	 * 根据模板类型查询模板
	 * @return
	 */
	public List<TemplateEntity> findTemplatesByExample(TemplateEntity template);
	
	/**
	 * 用户可用模板
	 * @return
	 */
	public List<TemplateEntity> findUserTemplates(String userId, Integer classify);
	
	/**
	 * 更新模板
	 * @param template
	 */
	public void updateTemplate(TemplateEntity template);
	
	/**
	 * 删除模板
	 * @param template
	 */
	public void deleteTemplate(TemplateEntity template);
	
	/**
	 * 删除模板
	 * @param template
	 */
	public void deleteTemplate(String templateId);
	
	/**
	 * 查出用户自定义数量
	 * @return
	 */
	public long findUserTemplatesCout(String userId);
}
