package com.eht.comment.entity;


import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.enumeration.DataType;
import com.eht.note.entity.NoteEntity;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 评论管理
 * @author zhangdaihao
 * @date 2014-04-01 13:58:52
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_comment", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
@SuppressWarnings("serial")
public class CommentEntity implements java.io.Serializable {
	/**id*/
	private java.lang.String id;
	/**条目表_id*/
	private java.lang.String noteId;
	/**content*/
	private java.lang.String content;
	/**删除标识*/
	private java.lang.Integer deleted;
	/**创建者*/
	private java.lang.String createUser;
	/**创建人*/
	private java.util.Date createTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	
	private NoteEntity noteEntity;
	
	private AccountEntity accountCreateUser;
	
	private AccountEntity accountUpdateUser;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	
	private String operation;
	
	private String className = DataType.COMMENT.toString();
	
	@Transient
	public java.lang.String getCreateUserId() {
		return createUser;
	}

	public void setCreateUserId(java.lang.String createUserId) {
		this.createUserId = createUserId;
		this.createUser = createUserId;
	}
	
	//用户缓存
	private List<String> accout;
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", insertable=false, updatable=false)
	public AccountEntity getAccountCreateUser() {
		return accountCreateUser;
	}

	public void setAccountCreateUser(AccountEntity accountCreateUser) {
		this.accountCreateUser = accountCreateUser;
	}
   
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  id
	 */
	@Id
	public java.lang.String getId(){
		return this.id;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  id
	 */
	public void setId(java.lang.String id){
		this.id = id;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  条目表_id
	 */
	public java.lang.String getNoteId() {
		return noteId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  条目表_id
	 */
	public void setNoteId(java.lang.String noteId) {
		this.noteId = noteId;
	}
	/**
	 *方法: 取得java.lang.Object
	 *@return: java.lang.Object  content
	 */
	public java.lang.String getContent(){
		return this.content;
	}

	/**
	 *方法: 设置java.lang.Object
	 *@param: java.lang.Object  content
	 */
	public void setContent(java.lang.String content){
		this.content = content;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标识
	 */
	@ClientJsonIgnore
	public java.lang.Integer getDeleted(){
		return this.deleted;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  删除标识
	 */
	public void setDeleted(java.lang.Integer deleted){
		this.deleted = deleted;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  创建者
	 */
	@Column(updatable=false)
	@ClientJsonIgnore
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
	 *@return: java.util.Date  创建人
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建人
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
		if(this.createTime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createTime.getTime(); 
		}
	}
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "noteId", insertable=false, updatable=false)
	public NoteEntity getNoteEntity() {
		return noteEntity;
	}

	public void setNoteEntity(NoteEntity noteEntity) {
		this.noteEntity = noteEntity;
	}
   
	@Transient
	@ClientJsonIgnore
	public List<String> getAccout() {
		return accout;
	}

	public void setAccout(List<String> accout) {
		this.accout = accout;
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

	@Transient
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	@Transient
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
