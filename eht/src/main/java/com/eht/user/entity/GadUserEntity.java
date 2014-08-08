package com.eht.user.entity;


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

/**   
 * @Title: Entity
 * @Description: 第三方用户信息
 * @author zenghui
 * @date 2014-06-18  
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_user_gad", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class GadUserEntity extends User implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**主键*/
	private java.lang.String id;
	/**本地账号id   eht_user.id */
	private java.lang.String uid; 
	/**第三方账号id*/
	private java.lang.String openid;
	/**第三方账号user*/
	private java.lang.String openUser;  
	/**第三方类型（qq，sina...） */
	private java.lang.String openType;  
	/**创建时间*/
	private java.util.Date createtime; 
 
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  主键
	 */
	
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name ="ID",nullable=false,length=32)
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
	public java.lang.String getUid() {
		return uid;
	}
	public void setUid(java.lang.String uid) {
		this.uid = uid;
	}
	public java.lang.String getOpenid() {
		return openid;
	}
	public void setOpenid(java.lang.String openid) {
		this.openid = openid;
	}
	public java.lang.String getOpenUser() {
		return openUser;
	}
	public void setOpenUser(java.lang.String openUser) {
		this.openUser = openUser;
	}
	public java.lang.String getOpenType() {
		return openType;
	}
	public void setOpenType(java.lang.String openType) {
		this.openType = openType;
	}
	public java.util.Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(java.util.Date createtime) {
		this.createtime = createtime;
	}
	
}
