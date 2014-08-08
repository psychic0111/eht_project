package com.eht.message.service;

import java.util.List;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.message.entity.MessageEntity;
import com.eht.message.entity.MessageUserEntity;

public interface MessageServiceI extends CommonService{
	/**
	 * 根据ID查询消息
	 * @param id
	 * @return
	 */
	public MessageEntity getMessage(String id);
	/**
	 * 查询消息-用户关系数据
	 * @param userId
	 * @param id
	 * @return
	 */
	public MessageUserEntity getUserMessage(String userId, String messageId);
	
	/**
	 * 查询用户消息
	 * @return
	 */
	public List<MessageEntity> findUserMessages(String userId, Integer msgType, String content, String orderField, String orderType, int pageSize, int page);
	/**
	 * 根据类型获取用户消息数量
	 * @param userId
	 * @param msgType
	 * @param content
	 * @return
	 */
	public long countUserMessages(String userId, Integer msgType, String content);
	/**
	 * 查询用户未读消息
	 * @param  
	 * @return
	 */
	public List<MessageEntity> findNoReadMessageByType(String userId, Integer msgType, String content, String orderField, String orderType, int pageSize, int page);
	/**
	 * 根据类型获取未读消息数量
	 * @param userId
	 * @param msgType
	 * @param content
	 * @return
	 */
	public long countNoReadMessageByType(String userId, Integer msgType, String content);
	/**
	 * 查询用户未读消息数量
	 * @param  
	 * @return
	 */
	public long getNoReadMessageCount(String userId);
	
	/**
	 * 查询用户未读消息数量,按消息类型
	 * @param  
	 * @return
	 */
	public long getNoReadMessageCount(String userId, Integer msgType);
	
	/**
	 * 标记消息已读
	 * @param messageId
	 */
	public void markReadMessage(String userId, String messageId);
	
	/**
	 * 删除消息
	 * @param message
	 */
	public void deleteMessage(MessageEntity message);
	/**
	 * 删除消息
	 * @param messageId
	 */
	public void deleteMessage(String messageId);
	
	/**
	 * 删除消息-用户关系
	 * @param messageId
	 */
	public void deleteUserMessage(String messageId, String userId);
}
