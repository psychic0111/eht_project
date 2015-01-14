package com.eht.user.entity;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.eht.auth.bean.User;
import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.enumeration.DataType;

/**   
 * @Title: Entity
 * @Description: 用户信息
 * @author zhangdaihao
 * @date 2014-03-18 11:47:52
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_user", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class AccountEntity extends User implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**主键*/
	private java.lang.String id;
	/**用户名*/
	private java.lang.String userName;
	/**密码*/
	private java.lang.String password;
	/**邮箱*/
	private java.lang.String email;
	/**状态 1 正常 0 禁用 */
	private java.lang.Integer status;
	/**手机号码*/
	private java.lang.String mobile;
	/**删除标记*/
	private java.lang.Integer deleted;
	/**创建者*/
	private java.lang.String createuser;
	/**创建时间*/
	private java.util.Date createtime;
	/**修改者*/
	private java.lang.String updateuser;
	/**修改时间*/
	private java.util.Date updatetime;
  
	private java.lang.String photo;
	
	/** 用户正在使用的客户端ID */
	private String clientId;

	private String operation;
	
	private String className = DataType.USER.toString();
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  主键
	 */
	@Id
	public java.lang.String getId() {
		return id;
	}
	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  ID
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  主键
	 */
	public void setId(java.lang.String id){
		this.id = id;
	}
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  用户名
	 */
	public java.lang.String getUserName(){
		return this.userName;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  用户名
	 */
	public void setUserName(java.lang.String userName){
		this.userName = userName;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  密码
	 */
	@ClientJsonIgnore
	public java.lang.String getPassword(){
		return this.password;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  密码
	 */
	public void setPassword(java.lang.String password){
		this.password = password;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  邮箱
	 */
	public java.lang.String getEmail(){
		return this.email;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  邮箱
	 */
	public void setEmail(java.lang.String email){
		this.email = email;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  状态
	 */
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
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  手机号码
	 */
	public java.lang.String getMobile(){
		return this.mobile;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  手机号码
	 */
	public void setMobile(java.lang.String mobile){
		this.mobile = mobile;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标记
	 */
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
	@ClientJsonIgnore
	public java.lang.String getCreateuser(){
		return this.createuser;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  创建者
	 */
	public void setCreateuser(java.lang.String createuser){
		this.createuser = createuser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建时间
	 */
	@ClientJsonIgnore
	public java.util.Date getCreatetime(){
		return this.createtime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建时间
	 */
	public void setCreatetime(java.util.Date createtime){
		this.createtime = createtime;
		if(this.createtime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createtime.getTime(); 
		}
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  修改者
	 */
	@ClientJsonIgnore
	public java.lang.String getUpdateuser(){
		return this.updateuser;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  修改者
	 */
	public void setUpdateuser(java.lang.String updateuser){
		this.updateuser = updateuser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  修改时间
	 */
	@ClientJsonIgnore
	public java.util.Date getUpdatetime(){
		return this.updatetime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  修改时间
	 */
	public void setUpdatetime(java.util.Date updatetime){
		this.updatetime = updatetime;
		if(this.updatetime != null && this.updateTimeStamp == null){
			this.updateTimeStamp = this.updatetime.getTime();
		}
	}
	
	@Column(name="path")
	public java.lang.String getPhoto() {
		return photo;
	}
	public void setPhoto(java.lang.String photo) {
		this.photo = photo;
	}
	@Transient
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
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
	
	@Transient
	public Long getCreateTimeStamp() {
		return createTimeStamp;
	}
	public void setCreateTimeStamp(Long createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
		if(this.createTimeStamp != null && this.createtime == null){
			this.createtime = new Date(this.createTimeStamp);
		}
	}
	
	@Transient
	public Long getUpdateTimeStamp() {
		return updateTimeStamp;
	}
	public void setUpdateTimeStamp(Long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
		if(this.updateTimeStamp != null && this.updatetime == null){
			this.updatetime = new Date(this.updateTimeStamp);
		}
	}
	
	@Transient
	public java.lang.String getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(java.lang.String createUserId) {
		this.createUserId = createUserId;
		this.createuser = createUserId;
	}
	
	@Transient
	public java.lang.String getUpdateUserId() {
		return updateUserId;
	}
	public void setUpdateUserId(java.lang.String updateUserId) {
		this.updateUserId = updateUserId;
		this.updateuser = updateUserId;
	}

	
}
