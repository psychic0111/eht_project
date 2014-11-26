package com.eht.system.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.util.FilePathUtil;
import com.eht.common.util.FileToolkit;
import com.eht.common.util.UUIDGenerator;
import com.eht.common.util.XmlUtil;
import com.eht.resource.entity.ClassName;
import com.eht.resource.entity.ResourceAction;
import com.eht.resource.entity.ResourcePermission;
import com.eht.resource.service.ResourceActionService;
import com.eht.resource.service.ResourcePermissionService;
import com.eht.role.entity.Role;
import com.eht.role.service.RoleService;
import com.eht.system.bean.ClientEntity;
import com.eht.system.service.DataInitService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

@Service("dataInitService")
@Transactional
public class DataInitServiceImpl extends CommonServiceImpl implements DataInitService {

	@Autowired
	private RoleService roleService;

	@Autowired
	private ResourceActionService resourceActionService;
	
	@Autowired
	private ResourcePermissionService resourcePermissionService;
	
	/**
	 * 初始化角色数据
	 */
	public void initRoles() {
		XStream xst = new XStream(new Dom4JDriver());
		StringBuilder sb = new StringBuilder(FilePathUtil.getClassPath());
		sb.append(File.separator).append("data");
		sb.append(File.separator).append("init_role.xml");
		File file = new File(sb.toString());

		Object obj = xst.fromXML(file);
		if (obj != null) {
			List<?> list = (List<?>) obj;
			for (int i = 0; i < list.size(); i++) {
				Role role = (Role) list.get(i);
				Role r = roleService.getRole(role.getId());
				if(r == null){
					roleService.addRole(role);
				}
			}
		}

	}
	
	/**
	 * 初始化资源和权限
	 */
	public void initResources() {
		String folder = FilePathUtil.getClassPath() + File.separator + "resource-actions";
		String[] filePaths = FileToolkit.listFilebySuffix(folder, ".xml");
		for (String path : filePaths) {
			Document doc = XmlUtil.readXmlFile(folder + File.separator + path);
			Element element = doc.getRootElement();
			parseModuleResouce(element);  //解析模块权限
			
			parseEntityResouce(element);  //解析保存实体权限
		}
	}
	
	/**
	 * 初始模块资源
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private void parseModuleResouce(Element root){
		List<Element> list = (List<Element>) XmlUtil.getElementsByNodeName(root, "module-resource");
		for (Element ele : list) {
			String className = XmlUtil.getUniqueElement(ele, "module-name").getTextTrim();
			ClassName cn = resourceActionService.findResourceByName(className);
			// 如果数据库还没有此资源,则添加到数据库
			if (cn == null) {
				cn = new ClassName();
				cn.setClassName(className);
				resourceActionService.addResource(cn);
			}
			parseResourceActions(ele, cn);
		}
	}
	
	/**
	 * 初始实体资源
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private void parseEntityResouce(Element root){
		List<Element> list = (List<Element>) XmlUtil.getElementsByNodeName(root, "entity-resource");
		for (Element ele : list) {
			String className = XmlUtil.getUniqueElement(ele, "entity-name").getTextTrim();
			ClassName cn = resourceActionService.findResourceByName(className);
			// 如果数据库还没有此资源,则添加到数据库
			if (cn == null) {
				cn = new ClassName();
				cn.setClassName(className);
				resourceActionService.addResource(cn);
			}
			parseResourceActions(ele, cn);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private List<ResourceAction> parseResourceActions(Element resourceEle, ClassName cn){
		Element e = XmlUtil.getUniqueElement(XmlUtil.getUniqueElement(resourceEle, "permissions"), "supports");
		List<Element> acList = (List<Element>) XmlUtil.getElementsByNodeName(e, "actionkey");
		List<ResourceAction> list = new ArrayList<ResourceAction>();
		for (int i = 0; i < acList.size(); i++) {
			Element acEle = acList.get(i);
			String action = acEle.getTextTrim();
			ResourceAction ra = resourceActionService.findResourceAction(cn.getClassName(), action);
			// 如果数据库中还没有此资源的此操作,添加到数据库中
			if (ra == null) {
				double value = Math.pow(2, i);
				ra = new ResourceAction();
				ra.setResourceName(cn.getClassName());
				ra.setAction(action);
				ra.setBitwiseValue((int) value);
				resourceActionService.addResourceAction(ra);
				list.add(ra);
			}
		}
		//系统中如果已经有该资源的授权，则不进行这部分数据初始化
		List<ResourcePermission> rpList = resourcePermissionService.findResourcePermission(cn.getClassName(), cn.getClassNameId().toString());
		if(rpList == null || rpList.isEmpty()){
			initResourcesPermission(resourceEle, cn);
		}
		return list;
	}

	/**
	 * 初始化模块资源权限，没有实体的，因为实体是针对具体数据授权
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initResourcesPermission(Element resourceEle, ClassName clazz) {
		List<?> roleEleList = XmlUtil.getElementsByNodeName(XmlUtil.getUniqueElement(resourceEle, "permissions"), "roledefaults");
		String resourceName = clazz.getClassName();
		for(Object o : roleEleList){
			Element e = (Element) o;
			String roleName = e.attributeValue("roleName");
			
			Role role = roleService.findRoleByName(roleName);
			List<Element> acList = (List<Element>) XmlUtil.getElementsByNodeName(e, "actionkey");
			for(Element acEle : acList){
				String action = acEle.getTextTrim();
					ResourceAction ra = resourceActionService.findResourceAction(resourceName, action);
					resourcePermissionService.grantPermissions(resourceName, clazz.getClassNameId().toString(), role.getId(), ra.getBitwiseValue());
			}
		}
		
	}

	@Override
	public String registerClient(String clientId, String clientType) {
		ClientEntity client = new ClientEntity();
		client.setClientType(clientType);
		client.setClientId(clientId);
		save(client);
		return client.getClientId();
	}

	@Override
	public boolean deleteClient(String clientId) {
		ClientEntity client = get(ClientEntity.class, clientId);
		delete(client);
		return true;
	}

	@Override
	public ClientEntity getClient(String clientId) {
		return get(ClientEntity.class, clientId);
	}
}
