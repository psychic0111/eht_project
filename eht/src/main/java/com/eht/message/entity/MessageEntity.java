package com.eht.message.entity;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 系统消息提醒
 * @author yuhao
 * @date 2014-04-02 11:50:18
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_message", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
@SuppressWarnings("serial")
public class MessageEntity implements java.io.Serializable {
	/**主键ID*/
	private java.lang.String id;
	/**消息内容*/
	private java.lang.String content;
	/**创建者*/
	private java.lang.String createUser;
	/**创建时间*/
	private java.util.Date createTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	
	private Integer msgType;
	
	private String className;
	
	private String classPk;
	
	private String operate;
	
	private Integer userIsRead;
	
	/** 树节点使用 */
	private String parentId;
  
	private AccountEntity creator;
	
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", updatable=false, insertable=false)
	public AccountEntity getCreator() {
		return creator;
	}

	public void setCreator(AccountEntity creator) {
		this.creator = creator;
	}
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  主键ID
	 */
	@Id
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
	 *方法: 取得java.lang.Object
	 *@return: java.lang.Object  消息内容
	 */
	@Column(name ="CONTENT",length=65535)
	public java.lang.String getContent(){
		return this.content;
	}

	/**
	 *方法: 设置java.lang.Object
	 *@param: java.lang.Object  消息内容
	 */
	public void setContent(java.lang.String content){
		this.content = content;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  创建者
	 */
	@Column(name ="CREATEUSER",nullable=true,length=32)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  创建者
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}
	
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建时间
	 */
	@Column(name ="CREATETIME",nullable=true)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
		if(this.createTime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createTime.getTime(); 
		}
	}

	@Transient
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getMsgType() {
		return msgType;
	}

	public void setMsgType(Integer msgType) {
		this.msgType = msgType;
	}
	@Transient
	public Integer getUserIsRead() {
		return userIsRead;
	}

	public void setUserIsRead(Integer userIsRead) {
		this.userIsRead = userIsRead;
	}
	
	public Long getCreateTimeStamp() {
		return createTimeStamp;
	}

	public void setCreateTimeStamp(Long createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
		if(this.createTimeStamp != null && this.createTime == null){
			this.createTime = new Date(this.createTimeStamp);
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassPk() {
		return classPk;
	}

	public void setClassPk(String classPk) {
		this.classPk = classPk;
	}

	public String getOperate() {
		return operate;
	}

	public void setOperate(String operate) {
		this.operate = operate;
	}
}
