package com.eht.mail.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;

import com.eht.mail.service.SendMailServiceI;

@Service("sendMailService")
@Transactional
public class SendMailServiceImpl extends CommonServiceImpl implements SendMailServiceI {
	
}