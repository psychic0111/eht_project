package com.eht.common.webinterceptor;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jeecgframework.core.util.ContextHolderUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.web.system.manager.ClientManager;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.eht.common.constant.Constants;
import com.eht.common.util.AppRequstUtiles;
import com.eht.common.util.sessionProc.MySessionContext;


/**
 * 权限拦截器
 * 
 * @author  chenlong
 * 
 */
public class UserInterceptor implements HandlerInterceptor {
	 
	private static final Logger logger = Logger.getLogger(UserInterceptor.class);
	
	private SystemService systemService;
	private List<String> excludeUrls;

	public List<String> getExcludeUrls() {
		return excludeUrls;
	}

	public void setExcludeUrls(List<String> excludeUrls) {
		this.excludeUrls = excludeUrls;
	}

	public SystemService getSystemService() {
		return systemService;
	}

	@Autowired
	public void setSystemService(SystemService systemService) {
		this.systemService = systemService;
	}

	/**
	 * 在controller后拦截
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) throws Exception {
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView modelAndView) throws Exception {

	}

	/**
	 * 在controller前拦截
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		String requestPath = ResourceUtil.getRequestPath(request);// 用户访问的资源地址
		
		if(requestPath.startsWith("noteController/front/downloadNodeAttach.dht")){
			Cookie cookies[] = request.getCookies();
			if (cookies != null ) {
				for (int i = 0; i < cookies.length; i++) {
					Cookie cookie = cookies[i];
					if("username".equals(cookie.getName())){
						if(cookie.getValue()!=null){
							return true;
						}
					}
				}
			}
		}
		//上传的时候不拦截
		if(requestPath.startsWith("noteController/front/uploadNodeAttach.dht")){
			return true;
		}
		if (isInexcludeUrlList(requestPath)) {
			return true;
		} else {
			if(hasMenuAuth(request)){
				HttpSession session = request.getSession();
				String projectName = AppRequstUtiles.getContextPath();
			    
			    if(request.getParameter("jsessionid")!=null){
			    	session = ClientManager.getInstance().getSession(request.getParameter("jsessionid"));
			    }
				 // 从session 里面获取用户名的信息  
			    //System.out.println("sessionID======================================" + request.getParameter("jsessionid"));
				Object obj = session.getAttribute(Constants.SESSION_USER_ATTRIBUTE);
				// 判断如果没有取到用户信息，就跳转到登陆页面，提示用户进行登陆  
				if (obj == null || "".equals(obj.toString())) {  
					response.sendRedirect(projectName+"/"); 
					return false;
				}else{
					return true;
				}
			}
		}
		return true;
	}
	
	/**
	 * 判断url是否在排除列表中
	 * 只拦截.do的请求
	 * @param url
	 * @return
	 */
	private boolean isInexcludeUrlList(String url){
		return excludeUrls.contains(url);
	}
	
	private boolean hasMenuAuth(HttpServletRequest request){
		return true;
	}
	/**
	 * 转发
	 * 
	 * @param user
	 * @param req
	 * @return
	 */
	@RequestMapping(params = "forword")
	public ModelAndView forword(HttpServletRequest request) {
		return new ModelAndView(new RedirectView("loginController.do?login"));
	}

	private void forward(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("webpage/login/timeout.jsp").forward(request, response);
	}

}
