package com.eht.tag.entity;

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
import org.jeecgframework.core.util.StringUtil;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.bean.BaseSubjectModel;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 标签管理
 * @author zhangdaihao
 * @date 2014-04-01 16:22:12
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_tag", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
@SuppressWarnings("serial")
public class TagEntity extends BaseSubjectModel implements java.io.Serializable {
	/**id*/
	private java.lang.String id;
	/**name*/
	private java.lang.String name;
	/**parentid*/
	private java.lang.String parentId;
	/**createuser*/
	private java.lang.String createUser;
	/**createtime*/
	private java.util.Date createTime;
	/**专题ID*/
	private java.lang.String subjectId;
	/**修改者*/
	private java.lang.String updateUser;
	/**修改时间*/
	private java.util.Date updateTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	/**树父ID*/
	private String pId;
	
	private TagEntity tagEntity;
	
	private String oldId;
	
	private AccountEntity accountCreateUser;
	
	private AccountEntity accountUpdateUser;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	@Transient
	public java.lang.String getCreateUserId() {
		return createUser;
	}

	public void setCreateUserId(java.lang.String createUserId) {
		this.createUserId = createUserId;
		this.createUser = createUserId;
	}
	
	@Transient
	public java.lang.String getUpdateUserId() {
		return updateUser;
	}

	public void setUpdateUserId(java.lang.String updateUserId) {
		this.updateUserId = updateUserId;
		this.updateUser = updateUserId;
	}
	
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", insertable=false, updatable=false)
	public AccountEntity getAccountCreateUser() {
		return accountCreateUser;
	}

	public void setAccountCreateUser(AccountEntity accountCreateUser) {
		this.accountCreateUser = accountCreateUser;
	}
   
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updateUser", insertable=false, updatable=false)
	public AccountEntity getAccountUpdateUser() {
		return accountUpdateUser;
	}

	public void setAccountUpdateUser(AccountEntity accountUpdateUser) {
		this.accountUpdateUser = accountUpdateUser;
	}
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  id
	 */
	/**删除标识*/
	private java.lang.Integer deleted;
	
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
	 *@return: java.lang.String  name
	 */
	public java.lang.String getName(){
		return this.name;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  name
	 */
	public void setName(java.lang.String name){
		this.name = name;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  parentid
	 */
	public java.lang.String getParentId() {
		return parentId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  parentid
	 */
	public void setParentId(java.lang.String parentId) {
		this.parentId = parentId;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  createuser
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.lang.String getCreateUser() {
		return createUser;
	}
	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  createuser
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  createtime
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  createtime
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
		if(this.createTime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createTime.getTime(); 
		}
	}
   
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentId", insertable=false, updatable=false)
	@ClientJsonIgnore
	public TagEntity getTagEntity() {
		return tagEntity;
	}

	public void setTagEntity(TagEntity tagEntity) {
		this.tagEntity = tagEntity;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标识
	 */
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
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  修改者
	 */
	public java.lang.String getUpdateUser() {
		return updateUser;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  修改者
	 */
	public void setUpdateUser(java.lang.String updateUser) {
		this.updateUser = updateUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  修改时间
	 */
	@ClientJsonIgnore
	public java.util.Date getUpdateTime() {
		return updateTime;
	}
	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  修改时间
	 */
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
		if(this.updateTime != null && this.updateTimeStamp == null){
			this.updateTimeStamp = this.updateTime.getTime();
		}
	}

	public java.lang.String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(java.lang.String subjectId) {
		this.subjectId = subjectId;
	}
	
	@Transient
	@ClientJsonIgnore
	public String getPId() {
		if(StringUtil.isEmpty(parentId)){
			if(StringUtil.isEmpty(subjectId)){
				return "tag_personal";
			}else{
				return "tag_subject_" + subjectId;
			}
		}else{
			return this.parentId;
		}
	}

	public void setPId(String pId) {
		this.pId = pId;
	}
	@Transient
	@ClientJsonIgnore
	public String getOldId() {
		return oldId;
	}

	public void setOldId(String oldId) {
		this.oldId = oldId;
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

	public Long getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(Long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
		if(this.updateTimeStamp != null && this.updateTime == null){
			this.updateTime = new Date(this.updateTimeStamp);
		}
	}

	@Override
	public String findOwnSubjectId() {
		return this.subjectId;
	}
}
