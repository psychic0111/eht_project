package com.eht.template.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.template.entity.TemplateEntity;
import com.eht.template.service.TemplateServiceI;

@Service("templateService")
@Transactional
public class TemplateServiceImpl extends CommonServiceImpl implements TemplateServiceI {

	@SuppressWarnings("unchecked")
	@Override
	public List<TemplateEntity> findTemplatesByExample(TemplateEntity template) {
		return (List<TemplateEntity>)findByExample(TemplateEntity.class.getName(), template);
	}

	@Override
	public List<TemplateEntity> findUserTemplates(String userId, Integer classify) {
		/** 用户自己创建的与内置的都应包括在内 */
		DetachedCriteria dc = DetachedCriteria.forClass(TemplateEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.or(Restrictions.eq("createUser", userId), Restrictions.eq("templateType", Constants.TEMPLATE_SYSTEM_DEFAULT)));
		List<TemplateEntity> list = findByDetached(dc);
		return list;
	}

	@Override
	@RecordOperate(dataClass=DataType.TEMPLATE, action=DataSynchAction.UPDATE, keyIndex=0, keyMethod="getId")
	public void updateTemplate(TemplateEntity template) {
		updateEntitie(template);
	}

	@Override
	@RecordOperate(dataClass=DataType.TEMPLATE, action=DataSynchAction.ADD, keyIndex=0, keyMethod="getId")
	public String addTemplate(TemplateEntity template) {
		save(template);
		return template.getId();
	}

	@Override
	@RecordOperate(dataClass=DataType.TEMPLATE, action=DataSynchAction.DELETE, keyIndex=0, keyMethod="getId")
	public void deleteTemplate(TemplateEntity template) {
		delete(template);
	}

	@Override
	public void deleteTemplate(String templateId) {
		TemplateEntity template = getTemplate(templateId);
		deleteTemplate(template);
	}

	@Override
	public TemplateEntity getTemplate(String templateId) {
		return get(TemplateEntity.class, templateId);
	}

	@Override
	public long findUserTemplatesCout(String userId) {
		DetachedCriteria dc = DetachedCriteria.forClass(TemplateEntity.class);
		dc.add(Restrictions.eq("deleted", Constants.DATA_NOT_DELETED));
		dc.add(Restrictions.eq("createUser", userId));
		dc.add(Restrictions.eq("templateType", Constants.TEMPLATE_USER_DEFINED));
		Long total = (Long) dc.getExecutableCriteria(getSession()).setProjection(Projections.rowCount()).uniqueResult();
		return total;
	}
	
}