package com.eht.subject.entity;

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
import com.eht.common.constant.Constants;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 专题信息
 * @author zhangdaihao
 * @date 2014-03-21 14:49:54
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_subject")
@DynamicUpdate(true)
@DynamicInsert(true)
public class SubjectEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**专题名称*/
	private java.lang.String subjectName;
	/**专题类型  1 个人专题  2 多人专题*/
	private java.lang.Integer subjectType;
	/**状态*/
	private java.lang.Integer status;
	/**删除标识*/
	private java.lang.Integer deleted;
	/**创建人*/
	private java.lang.String createUser;
	/**创建时间*/
	private java.util.Date createTime;
	/**修改者*/
	private java.lang.String updateUser;
	/**修改时间*/
	private java.util.Date updateTime;
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	/**描述*/
	private String description;
	/**父ID，构建树菜单时使用*/
	private String parentId;
	
	/**树的josn串*/
	private String josns;
	
	/**模板ID*/
	private String templateId;
	
	private List<DirectoryEntity>  directoryList;
	
	private AccountEntity accountCreateUser;
	
	private AccountEntity accountUpdateUser;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	/**专题名称生成mht用*/
	private java.lang.String subjectNameTitle;
	
	private String operation;
	
	private String className;
	
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
	
	/**oldId
	 * 导出需要保存oldid
	 * 
	 * */
	private String oldId;
	
	@Id
	public java.lang.String getId(){
		return this.id;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  ID
	 */
	public void setId(java.lang.String id){
		this.id = id;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  专题名称
	 */
	public java.lang.String getSubjectName() {
		return subjectName;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  专题名称
	 */
	public void setSubjectName(java.lang.String subjectName) {
		this.subjectName = subjectName;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  专题类型
	 */
	public java.lang.Integer getSubjectType() {
		return subjectType;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  专题类型
	 */

	public void setSubjectType(java.lang.Integer subjectType) {
		this.subjectType = subjectType;
	}
	
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  状态
	 */
	@ClientJsonIgnore
	public java.lang.Integer getStatus(){
		return this.status;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  状态
	 */
	public void setStatus(java.lang.Integer status){
		this.status = status;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标识
	 */
	@Column(columnDefinition="int default 0")
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
	 *@return: java.lang.String  创建人
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  创建人
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建时间
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
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
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  修改者
	 */
	@ClientJsonIgnore
	public java.lang.String getUpdateUser() {
		return updateUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  修改者
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Transient
	@ClientJsonIgnore
	public String getParentId() {
		if(this.subjectType == Constants.SUBJECT_TYPE_P){
			return "-1";
		}else{
			return "-2";
		}
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	@Transient
	@ClientJsonIgnore
	public String getOldId() {
		return oldId;
	}

	public void setOldId(String oldId) {
		this.oldId = oldId;
	}
	
	@Transient
	@ClientJsonIgnore
	public List<DirectoryEntity> getDirectoryList() {
		return directoryList;
	}

	public void setDirectoryList(List<DirectoryEntity> directoryList) {
		this.directoryList = directoryList;
	}

	@Transient
	@ClientJsonIgnore
	public String getJosns() {
		return josns;
	}

	public void setJosns(String josns) {
		this.josns = josns;
	}

	@Transient
	@ClientJsonIgnore
	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
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

    @Transient
	public java.lang.String getSubjectNameTitle() {
		return subjectNameTitle;
	}

	public void setSubjectNameTitle(java.lang.String subjectNameTitle) {
		this.subjectNameTitle = subjectNameTitle;
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
