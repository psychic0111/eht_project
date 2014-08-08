package com.eht.common.util;

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Chang Fei
 */
@SuppressWarnings("unchecked")
public class AppContextUtils {

	/** ServletContext */
	private static ServletContext appContext;

	/** sprig WebApplicationContext 容器 */
	private static WebApplicationContext springAppContext;
	/**
	 * 项目文件路径
	 */
	private static String realPath=null;
	/**
	 * web路径
	 */
	private static String contextPath = null;
	
	/** 设置 ServletConttext */
	public static void setServletContext(ServletContext context) {
		appContext = context;
		contextPath = context.getContextPath();
		realPath = context.getRealPath("/");
	}

	/** 获取servletContext */
	public static ServletContext getServletContext() {
		return appContext;
	}

	/** 获取Spring WebApplicationContext 容器 */
	public static WebApplicationContext getSpringAppContext() {
		if(springAppContext==null){
			springAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		}
		return springAppContext;
	}

	public static String getRealPath() {
		return realPath;
	}

	/**
	 * @param <T>
	 *            返回值
	 * @param idOrBeanName
	 *            spring container中bean的id或者bean的name.
	 * @return
	 */
	public static <T> T getBean(String idOrBeanName) {
		if (getSpringAppContext().containsBean(idOrBeanName)) {
			return (T) getSpringAppContext().getBean(idOrBeanName);
		} else {
			return null;
		}
	}

	public static String getContextPath() {
		return contextPath;
	}

}
