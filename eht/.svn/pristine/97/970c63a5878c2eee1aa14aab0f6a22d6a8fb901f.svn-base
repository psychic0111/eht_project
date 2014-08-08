package com.eht.resource.service.impl;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.service.ResourceActionService;

@Service("resourceActionService")
@Transactional
public class ResourceActionServiceImpl extends CommonServiceImpl implements ResourceActionService {

	@Override
	public void addResourceAction(ResourceAction ac) {
		save(ac);
	}

	@Override
	public void addResource(ClassName c) {
		save(c);
	}

	@Override
	public ClassName findResourceByName(String className) {
		ClassName c = findUniqueByProperty(ClassName.class, "className", className);
		return c;
	}

	@Override
	public ResourceAction findResourceAction(String resourceName, String action) {
		DetachedCriteria dc = DetachedCriteria.forClass(ResourceAction.class);
		dc.add(Restrictions.eq("resourceName", resourceName)).add(Restrictions.eq("action", action));
		List<ResourceAction> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<ResourceAction> findActionsByName(String resourceName) {
		DetachedCriteria dc = DetachedCriteria.forClass(ResourceAction.class);
		dc.add(Restrictions.eq("resourceName", resourceName));
		List<ResourceAction> list = findByDetached(dc);
		return list;
	}

}
