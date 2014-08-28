package com.eht.comment.service.impl;

import java.util.List;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eht.comment.entity.CommentEntity;
import com.eht.comment.service.CommentServiceI;
import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.message.entity.MessageEntity;
import com.eht.message.entity.MessageUserEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

@Service("commentService")
@Transactional
public class CommentServiceImpl extends CommonServiceImpl implements CommentServiceI {

	@Autowired
	private AccountServiceI accountServiceI;
	@Autowired
	private  MessageServiceI MessageServiceI;
	@Override
	@RecordOperate(dataClass=DataType.COMMENT, action=DataSynchAction.ADD, keyMethod="getId", timeStamp="createTime")
	public String addComment(CommentEntity comment) {
		save(comment);
		MessageEntity  m=new MessageEntity();
		 m.setContent(comment.getContent());
		 m.setCreateTime(comment.getCreateTime());
		 m.setCreateUser(comment.getCreateUser());
		 m.setMsgType(Constants.MSG_USER_TYPE);
		 m.setUserIsRead(Constants.NOT_READ_OBJECT);
		 MessageServiceI.save(m);
		 List<String> list=comment.getAccout();
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
					 MessageServiceI.save(k);
				 }
				}
		 }
		return comment.getId();
	}

	@Override
	@RecordOperate(dataClass=DataType.COMMENT, action=DataSynchAction.DELETE, keyMethod="getId")
	public boolean deleteComment(CommentEntity comment) {
		delete(comment);
		return true;
	}

	@Override
	public boolean deleteComment(String commentId) {
		CommentEntity comment = getComment(commentId);
		deleteComment(comment);
		return true;
	}

	public boolean deleteComments(String noteId){
		super.commonDao.executeHql("delete from CommentEntity where noteId = '"+noteId+"'");
		return true;
	}
	
	@Override
	public CommentEntity getComment(String commentId) {
		return get(CommentEntity.class, commentId);
	}

	@Override
	public List<CommentEntity> findCommentByNote(String noteId) {
		List<CommentEntity> list = findHql("from CommentEntity c where c.noteId=? order by createTime desc ",new Object[]{noteId});
		return list;
	}

	@Override
	public void updateComment(CommentEntity comment) {
		updateEntitie(comment);
	}
	
}