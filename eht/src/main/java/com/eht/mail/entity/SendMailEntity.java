package com.eht.mail.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.SequenceGenerator;

/**   
 * @Title: Entity
 * @Description: 群发邮件管理
 * @author zhangdaihao
 * @date 2014-04-08 14:55:13
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_send_mail", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
@SuppressWarnings("serial")
public class SendMailEntity implements java.io.Serializable {
	/**ID*/
	private java.lang.String id;
	/**收件人*/
	private java.lang.String accept;
	/**正文*/
	private java.lang.String body;
	/**创建者*/
	private java.lang.String createuser;
	/**创建人*/
	private java.util.Date createtime;
	/**修改人*/
	private java.lang.String updateuser;
	/**修改时间*/
	private java.util.Date updatetime;
	
	/**标题*/
	private java.lang.String title;
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  ID
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
	 *@param: java.lang.String  ID
	 */
	public void setId(java.lang.String id){
		this.id = id;
	}
	/**
	 *方法: 取得java.lang.Object
	 *@return: java.lang.Object  收件人
	 */
	@Column(name ="ACCEPT",nullable=true)
	public java.lang.String getAccept(){
		return this.accept;
	}

	/**
	 *方法: 设置java.lang.Object
	 *@param: java.lang.Object  收件人
	 */
	public void setAccept(java.lang.String accept){
		this.accept = accept;
	}
	/**
	 *方法: 取得java.lang.Object
	 *@return: java.lang.Object  正文
	 */
	@Column(name ="BODY",nullable=true)
	public java.lang.String getBody(){
		return this.body;
	}

	/**
	 *方法: 设置java.lang.Object
	 *@param: java.lang.Object  正文
	 */
	public void setBody(java.lang.String body){
		this.body = body;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  创建者
	 */
	@Column(name ="CREATEUSER",nullable=true,length=32)
	public java.lang.String getCreateuser(){
		return this.createuser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  创建者
	 */
	public void setCreateuser(java.lang.String createuser){
		this.createuser = createuser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建人
	 */
	@Column(name ="CREATETIME",nullable=true)
	public java.util.Date getCreatetime(){
		return this.createtime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建人
	 */
	public void setCreatetime(java.util.Date createtime){
		this.createtime = createtime;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  修改人
	 */
	@Column(name ="UPDATEUSER",nullable=true,length=32)
	public java.lang.String getUpdateuser(){
		return this.updateuser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  修改人
	 */
	public void setUpdateuser(java.lang.String updateuser){
		this.updateuser = updateuser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  修改时间
	 */
	@Column(name ="UPDATETIME",nullable=true)
	public java.util.Date getUpdatetime(){
		return this.updatetime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  修改时间
	 */
	public void setUpdatetime(java.util.Date updatetime){
		this.updatetime = updatetime;
	}

	public java.lang.String getTitle() {
		return title;
	}
	@Column(name ="TITLE",nullable=true,length=200)
	public void setTitle(java.lang.String title) {
		this.title = title;
	}
	
}
