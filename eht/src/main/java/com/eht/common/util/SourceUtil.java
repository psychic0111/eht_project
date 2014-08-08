package com.eht.common.util;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.jeecgframework.core.util.ApplicationContextUtil;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 该帮助类，可以从国际化资源文件中读取错误信息
 * 
 * @author Administrator
 * 
 */
public class SourceUtil {
	/**
	 * 从国际化资源中获得对应方言的提示信息,目前用于错误信息提示的国际化
	 * @param code 资源编号
	 * @return
	 */
	public static String getMessageSource(String code) {
		// 获得方言
		LocaleResolver localeResolver = (LocaleResolver) ApplicationContextUtil.getContext().getBean("localeResolver");
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		if (localeResolver != null && request != null) {
			Locale locale = localeResolver.resolveLocale(request);
			//得到国际化资源
			ReloadableResourceBundleMessageSource source = (ReloadableResourceBundleMessageSource) ApplicationContextUtil
					.getContext().getBean("messageSource");
			String errorMessage = source.getMessage(code, null, locale);
			return StringUtil.isValidateString(errorMessage) ? errorMessage
					: "";
		}
		return "";
	}
}
