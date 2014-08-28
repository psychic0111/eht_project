package com.eht.message.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eht.common.constant.Constants;
import com.eht.message.entity.MessageEntity;
import com.eht.message.entity.MessageUserEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

@Service("messageService")
@Transactional
public class MessageServiceImpl extends CommonServiceImpl implements MessageServiceI {
	
	@Autowired
	private AccountServiceI accountServiceI;

	@Override
	public List<MessageEntity> findUserMessages(String userId, Integer msgType, String content, String orderField, String orderType, int pageSize, int page) {
		List<MessageUserEntity> list = findByProperty(MessageUserEntity.class, "userId", userId);
		if(list != null && !list.isEmpty()){
			List<String> msgIds = new ArrayList<String>();
			Map<String, Integer> map = new HashMap<String, Integer>();
			for(int i=0;i<list.size();i++){
				MessageUserEntity mu = list.get(i);
				msgIds.add(mu.getMessageId());
				//保存消息已读状态
				map.put(mu.getMessageId(), mu.getIsRead());
			}
			DetachedCriteria dc = DetachedCriteria.forClass(MessageEntity.class);
			dc.add(Restrictions.in("id", msgIds));
			if(msgType != null){
				dc.add(Restrictions.eq("msgType", msgType));
			}
			if(content != null && !"".equals(content)){
				dc.add(Restrictions.like("content", content, MatchMode.ANYWHERE));
			}
			
			//排序
			if(orderField != null && !"".equals(orderField)){
				if(orderType != null && !"".equals(orderType)){
					if(orderType.equals("DESC")){
						dc.addOrder(Order.desc(orderField));
					}else{
						dc.addOrder(Order.asc(orderField));
					}
				}
			}
			
			//分页查询
			int firstRow = (page - 1) * pageSize;
			List<MessageEntity> msgList = pageList(dc, firstRow, pageSize);
			//设置是否已读
			for(MessageEntity me : msgList){
				me.setUserIsRead(map.get(me.getId()));
			}
			return msgList;
		}
		return null;
	}
	
	@Override
	public long countUserMessages(String userId, Integer msgType, String content) {
		DetachedCriteria dc = DetachedCriteria.forClass(MessageUserEntity.class);
		dc.add(Restrictions.eq("userId", userId));
		
		List<MessageUserEntity> list = findByDetached(dc);
		long count = 0L;
		if(list != null && !list.isEmpty()){
			String[] msgIds = new String[list.size()];
			for(int i=0;i<list.size();i++){
				MessageUserEntity mu = list.get(i);
				msgIds[i] = mu.getMessageId();
			}
			
			dc = DetachedCriteria.forClass(MessageEntity.class);
			dc.add(Restrictions.in("id", msgIds));
			if(msgType != null){
				dc.add(Restrictions.eq("msgType", msgType));
			}
			if(content != null && !"".equals(content)){
				dc.add(Restrictions.like("content", content, MatchMode.ANYWHERE));
			}
		
			count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		}
		return count;
	}
	
	@Override
	public List<MessageEntity> findNoReadMessageByType(String userId, Integer msgType, String content, String orderField, String orderType, int pageSize, int page) {
		DetachedCriteria dc = DetachedCriteria.forClass(MessageUserEntity.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("isRead", Constants.NOT_READ_OBJECT));
		
		List<MessageUserEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			String[] msgIds = new String[list.size()];
			for(int i=0;i<list.size();i++){
				MessageUserEntity mu = list.get(i);
				msgIds[i] = mu.getMessageId();
			}
			
			dc = DetachedCriteria.forClass(MessageEntity.class);
			dc.add(Restrictions.in("id", msgIds));
			if(msgType != null){
				dc.add(Restrictions.eq("msgType", msgType));
			}
			if(content != null && !"".equals(content)){
				dc.add(Restrictions.like("content", content, MatchMode.ANYWHERE));
			}
			
			//排序
			if(orderField != null && !"".equals(orderField)){
				if(orderType != null && !"".equals(orderType)){
					if(orderType.equals("DESC")){
						dc.addOrder(Order.desc(orderField));
					}else{
						dc.addOrder(Order.asc(orderField));
					}
				}
			}
			
			//分页查询
			int firstRow = (page - 1) * pageSize;
			List<MessageEntity> msgList = pageList(dc, firstRow, pageSize);
			
			//设置为未读
			for(MessageEntity me : msgList){
				me.setUserIsRead(Constants.NOT_READ_OBJECT);
			}
			return msgList;
		}
		return null;
	}
	
	@Override
	public long countNoReadMessageByType(String userId, Integer msgType, String content) {
		DetachedCriteria dc = DetachedCriteria.forClass(MessageUserEntity.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("isRead", Constants.NOT_READ_OBJECT));
		
		List<MessageUserEntity> list = findByDetached(dc);
		long count = 0;
		if(list != null && !list.isEmpty()){
			String[] msgIds = new String[list.size()];
			for(int i=0;i<list.size();i++){
				MessageUserEntity mu = list.get(i);
				msgIds[i] = mu.getMessageId();
			}
			
			dc = DetachedCriteria.forClass(MessageEntity.class);
			dc.add(Restrictions.in("id", msgIds));
			if(msgType != null){
				dc.add(Restrictions.eq("msgType", msgType));
			}
			if(content != null && !"".equals(content)){
				dc.add(Restrictions.like("content", content, MatchMode.ANYWHERE));
			}
		
			count = oConvertUtils.getInt((dc.getExecutableCriteria(getSession())
				.setProjection(Projections.rowCount())).uniqueResult(), 0);
		}
		return count;
	}
	
	
	@Override
	public long getNoReadMessageCount(String userId) {
		String sql = "select count(*) cnt from eht_message_user where userId='" + userId + "' and isread=0";
		long count = getCountForJdbc(sql);
		return count;
	}

	@Override
	public long getNoReadMessageCount(String userId, Integer msgType) {
		String sql = "select count(*) cnt from eht_message_user where userId='" + userId + "' and isread=0 and messageId in(select id from eht_message m where m.msgType="+msgType+")";
		long count = getCountForJdbc(sql);
		return count;
	}

	@Override
	public void markReadMessage(String userId, String messageId) {
		DetachedCriteria dc = DetachedCriteria.forClass(MessageUserEntity.class);
		dc.add(Restrictions.eq("userId", userId));
		dc.add(Restrictions.eq("messageId", messageId));
		List<MessageUserEntity> list = findByDetached(dc);
		if(list != null && !list.isEmpty()){
			MessageUserEntity mue = list.get(0);
			mue.setIsRead(Constants.READED_OBJECT);
			updateEntitie(mue);
		}
	}
	
	@Override
	public void deleteMessage(MessageEntity message) {
		delete(message);
	}
	
	@Override
	public void deleteMessage(String messageId) {
		MessageEntity message = getMessage(messageId);
		deleteMessage(message);
	}

	@Override
	public MessageEntity getMessage(String id) {
		return get(MessageEntity.class, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessageUserEntity getUserMessage(String userId, String messageId) {
		MessageUserEntity mu = new MessageUserEntity();
		mu.setMessageId(messageId);
		mu.setUserId(userId);
		List<MessageUserEntity> list = findByExample(MessageUserEntity.class.getName(), mu);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	//@RecordOperate(dataClass="MESSAGE", action=SynchConstants.DATA_OPERATE_DELETE)
	public void deleteUserMessage(String messageId, String userId) {
		MessageUserEntity mu = getUserMessage(userId, messageId);
		delete(mu);
	}

	@Override
	public void saveMessages(String content, List<String> username,String userId) throws Exception{
		MessageEntity  m=new MessageEntity();
		 m.setContent(content);
		 m.setCreateTime(new Date());
		 m.setCreateUser(userId);
		 m.setMsgType(Constants.MSG_USER_TYPE);
		 m.setUserIsRead(Constants.NOT_READ_OBJECT);
		 save(m);
		 List<String> list=username;
		 if(list!=null){
			 for (String string : list) {
				 if(string.indexOf("@")!=-1){
					 string=string.substring(1).trim();
				 }
				 AccountEntity a=	 accountServiceI.findUserByAccount(string);
				 if(a!=null){
					 MessageUserEntity k=new MessageUserEntity();
					 k.setIsRead(Constants.NOT_READ_OBJECT);
					 k.setMessageId(m.getId());
					 k.setUserId(a.getId());
					 save(k);
				 }
				}
		 }
		
	}

}