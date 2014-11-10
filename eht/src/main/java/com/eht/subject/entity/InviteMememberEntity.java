package com.eht.subject.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.eht.role.entity.Role;


/**   
 * @Title: Entity
 * @Description: 邀请成员记录
 * @author yuhao
 * @date 2014-05-14
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_invitememember", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class InviteMememberEntity  implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**
	 * 专题ID
	 * */
	private String subjectid;
	/**
	 * 权限  超级管理员 1  编辑员2 作者3 读者4
	 * */
	private String roleid;
	
	/**
	 * email
	 * */
	private String email;
   
	private Role role;
	
	private String username;
	
	private String createUserId;
	
	private long createTimeStamp;
	
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name ="ID",nullable=false,length=32)
	public java.lang.String getId() {
		return id;
	}

	public void setId(java.lang.String id) {
		this.id = id;
	}
	@Column(name ="subjectid",nullable=false,length=32)
	public String getSubjectid() {
		return subjectid;
	}

	public void setSubjectid(String subjectid) {
		this.subjectid = subjectid;
	}
	
	@Column(name ="roleid",nullable=false,length=32)
	public String getRoleid() {
		return roleid;
	}

	public void setRoleid(String roleid) {
		this.roleid = roleid;
	}

	@Column(name ="email",nullable=false,length=32)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
	@Transient
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "roleid",insertable=false,updatable=false)
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public long getCreateTimeStamp() {
		return createTimeStamp;
	}

	public void setCreateTimeStamp(long createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
	}
	
	
}
