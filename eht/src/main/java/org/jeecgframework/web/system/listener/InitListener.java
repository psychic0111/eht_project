package org.jeecgframework.web.system.listener;

import javax.servlet.ServletContextEvent;

import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.web.system.service.MenuInitService;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.eht.common.util.AppContextUtils;
import com.eht.system.service.DataInitService;
import com.eht.webservice.util.DataSynchizeUtil;


/**
 * 系统初始化监听器,在系统启动时运行,进行一些初始化工作
 * @author laien
 *
 */
public class InitListener  implements javax.servlet.ServletContextListener {

	
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}
	
	public void contextInitialized(ServletContextEvent event) {
		AppContextUtils.setServletContext(event.getServletContext());
		
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());
		SystemService systemService = (SystemService) webApplicationContext.getBean("systemService");
		MenuInitService menuInitService = (MenuInitService) webApplicationContext.getBean("menuInitService");
		
		/**
		 * 第一部分：对数据字典进行缓存
		 */
		systemService.initAllTypeGroups();
		
		
		
		/**
		 * 第二部分：自动加载新增菜单和菜单操作权限
		 * 说明：只会添加，不会删除（添加在代码层配置，但是在数据库层未配置的）
		 */
		if("true".equals(ResourceUtil.getConfigByName("auto.scan.menu.flag").toLowerCase())){
			menuInitService.initMenu();
		}
		
		/**
		 * 初始数据
		 */
		DataInitService dataInitService = (DataInitService) AppContextUtils.getBean("dataInitService");
		dataInitService.initRoles();
		dataInitService.initResources();
		
		/**
		 * 初始化同步配置
		 */
		DataSynchizeUtil.readSynConfig();
	}

}
