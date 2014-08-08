/**
 * 
 */
package com.eht.resource.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;

/**
 * @author chenlong
 *
 */
public interface ResourceActionService extends CommonService {
	
	/**
	 * 保存资源和操作到数据库
	 * @param ac
	 */
	public void addResourceAction(ResourceAction ac);
	
	/**
	 * 保存资源到数据库
	 * @param c
	 */
	public void addResource(ClassName c);
	
	/**
	 * 根据资源名查询资源
	 * @param className
	 * @return
	 */
	public ClassName findResourceByName(String className);
	
	/**
	 * 根据资源名和操作名查询ResourceAction
	 * @param resourceName
	 * @param action
	 * @return
	 */
	public ResourceAction findResourceAction(String resourceName, String action);
	
	/**
	 * 根据资源名查询该资源的所有Action
	 * @param resourceName
	 * @return
	 */
	public List<ResourceAction> findActionsByName(String resourceName);
}
