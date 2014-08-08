package com.eht.template.entity;

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
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 模板管理
 * @author zhangdaihao
 * @date 2014-03-20 10:24:05
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_template")
@DynamicUpdate(true)
@DynamicInsert(true)
public class TemplateEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**模板名称*/
	private java.lang.String templateName;
	/**模板类型 0 系统内置 1 用户自定义*/
	private java.lang.Integer templateType;
	/**模本分类 1 专题模板  2 条目模板*/
	private java.lang.Integer classify;
	/**删除标记*/
	private java.lang.Integer deleted;
	/**创建者*/
	private java.lang.String createUser;
	/**创建时间*/
	private java.util.Date createTime;
	/**修改者*/
	private java.lang.String updateUser;
	/**修改时间*/
	private java.util.Date updateTime;
	
	private String content;
	
	private AccountEntity accountCreateUser;
	
	private AccountEntity accountUpdateUser;
	
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser")
	public AccountEntity getAccountCreateUser() {
		return accountCreateUser;
	}

	public void setAccountCreateUser(AccountEntity accountCreateUser) {
		this.accountCreateUser = accountCreateUser;
	}
   
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updateUser")
	public AccountEntity getAccountUpdateUser() {
		return accountUpdateUser;
	}
	
	public void setAccountUpdateUser(AccountEntity accountUpdateUser) {
		this.accountUpdateUser = accountUpdateUser;
	}
	
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  ID
	 */
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name ="id",nullable=false,length=32)
	public java.lang.String getId() {
		return id;
	}
	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  ID
	 */
	public void setId(java.lang.String id) {
		this.id = id;
	}
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  模板名称
	 */
	@Column(name ="templatename",nullable=true,length=40)
	public java.lang.String getTemplateName() {
		return templateName;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  模板名称
	 */
	public void setTemplateName(java.lang.String templateName) {
		this.templateName = templateName;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  模板类型
	 */
	@Column(name ="templatetype",nullable=true,precision=3,scale=0)
	public java.lang.Integer getTemplateType() {
		return templateType;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  模板类型
	 */
	public void setTemplateType(java.lang.Integer templateType) {
		this.templateType = templateType;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  模本分类
	 */
	@Column(name ="classify",nullable=true,precision=3,scale=0)
	public java.lang.Integer getClassify() {
		return classify;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  模本分类
	 */
	public void setClassify(java.lang.Integer classify) {
		this.classify = classify;
	}

	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标记
	 */
	@Column(name ="deleted",nullable=true,precision=3,scale=0)
	public java.lang.Integer getDeleted(){
		return this.deleted;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  删除标记
	 */
	public void setDeleted(java.lang.Integer deleted){
		this.deleted = deleted;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  创建者
	 */
	@Column(name ="createuser",nullable=true,length=32)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  创建者
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建时间
	 */
	@Column(name ="createtime",nullable=true)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  修改者
	 */
	@Column(name ="updateuser",nullable=true,length=32)
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
	@Column(name ="updatetime",nullable=true)
	public java.util.Date getUpdateTime() {
		return updateTime;
	}
	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  修改时间
	 */
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
	}
    
	@Column(name ="content",nullable=true,length=2000)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
