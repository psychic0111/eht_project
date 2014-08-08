package com.eht.common;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;

public class UserMessageSource extends ResourceBundleMessageSource{
	public UserMessageSource() {
        setBasename("com.eht.auth.messages");
    }

    //~ Methods ========================================================================================================

    public static MessageSourceAccessor getAccessor() {
        return new MessageSourceAccessor(new UserMessageSource());
    }
}
