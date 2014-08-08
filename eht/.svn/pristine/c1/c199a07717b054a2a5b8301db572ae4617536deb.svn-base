package com.eht.system.service;

import org.dom4j.Element;

import com.eht.resource.entity.ClassName;
import com.eht.system.bean.ClientEntity;


public interface DataInitService {
	
	/**
	 * 初始化角色数据
	 */
	public void initRoles();
	
	/**
	 * 初始化资源数据
	 */
	public void initResources();
	
	/**
	 * 初始化角色的专题相关权限
	 */
	public void initResourcesPermission(Element resourceEle, ClassName clazz);
	
	/**
	 * 客户端注册
	 * @param clientType 客户端类型
	 * @return
	 */
	public String registerClient(String clientType);
	
	/**
	 * 删除注册客户端
	 * @param clientID 客户端标识 
	 * @return
	 */
	public boolean deleteClient(String clientId);
	
	/**
	 * 获取客户信息
	 * @param clientId
	 * @return
	 */
	public ClientEntity getClient(String clientId);
}
