package com.eht.common.webinterceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 调试拦截器
 * 
 * @author Chang Fei
 */
@Component
public class DebugInterceptor extends HandlerInterceptorAdapter {

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		System.out.println("path: http://" + request.getLocalAddr() + ":" + request.getLocalPort() + request.getRequestURI());
		System.out.println("parameters: " + request.getQueryString());
		System.out.println("action: " + handler.getClass().getSimpleName());
		System.out.println("request encoding: " + request.getCharacterEncoding());
		return super.preHandle(request, response, handler);
	}

	/**
	 * 进入view前执行
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if (modelAndView != null) {
			System.out.println("view: " + modelAndView.getViewName());
		}
		System.out.println("response encoding: " + response.getCharacterEncoding());
		super.postHandle(request, response, handler, modelAndView);
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}
}

