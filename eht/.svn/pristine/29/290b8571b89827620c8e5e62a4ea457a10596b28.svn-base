package com.eht.message.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

/**   
 * @Title: Entity
 * @Description: 系统消息关系对应表
 * @author yuhao
 * @date 2014-04-02 11:52:20
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_message_user", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class MessageUserEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**主键ID*/
	private java.lang.String id;
	/**系统消息ID*/
	private java.lang.String messageId;
	/**用户ID*/
	private java.lang.String userId;
	/**是否已读 0 未读  1 已读*/
	private java.lang.Integer isRead;
	
	private MessageEntity messageEntity;
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  主键ID
	 */
	
	@Id
	@GeneratedValue(generator = "paymentableGenerator")
	@GenericGenerator(name = "paymentableGenerator", strategy = "uuid")
	@Column(name ="ID",nullable=false,length=32)
	public java.lang.String getId(){
		return this.id;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  主键ID
	 */
	public void setId(java.lang.String id){
		this.id = id;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  系统消息ID
	 */
	@Column(name ="MESSAGEID",nullable=true,length=32)
	public java.lang.String getMessageId() {
		return messageId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  系统消息ID
	 */
	public void setMessageId(java.lang.String messageId) {
		this.messageId = messageId;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  用户ID
	 */
	@Column(name ="USERID",nullable=true,length=32)
	public java.lang.String getUserId() {
		return userId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  用户ID
	 */
	public void setUserId(java.lang.String userId) {
		this.userId = userId;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  是否已读
	 */
	@Column(name ="ISREAD",nullable=true,precision=3,scale=0)
	public java.lang.Integer getIsRead() {
		return isRead;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  是否已读
	 */
	public void setIsRead(java.lang.Integer isRead) {
		this.isRead = isRead;
	}
	
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "messageId")
	public MessageEntity getMessageEntity() {
		return messageEntity;
	}

	public void setMessageEntity(MessageEntity messageEntity) {
		this.messageEntity = messageEntity;
	}
	
}
