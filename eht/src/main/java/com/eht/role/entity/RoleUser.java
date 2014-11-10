package com.eht.role.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataType;
import com.eht.subject.entity.SubjectEntity;
import com.eht.user.entity.AccountEntity;

@Entity
@Table(name="eht_user_role")
public class RoleUser implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	/**
	 * 用户ID
	 */
	private String userId;
	/**
	 * 角色ID
	 */
	private String roleId;
	
	/**
	 * 角色英文名
	 * 返回客户端
	 */
	private String roleName;
	
	/**
	 * 专题ID
	 */
	private String subjectId;
	
	private AccountEntity accountEntity;
	
	private SubjectEntity subjectEntity;
	
	private Role role;
	
	private boolean blackList;
	
	private long noteCount;
	
	private String operation;
	
	private String className = DataType.SUBJECTUSER.toString();
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	@Id
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Column(name ="roleId",nullable=true)
	public String getRoleId() {
		return roleId;
	}
	
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	
	@Column(name ="groupId",nullable=true)
	public String getSubjectId() {
		return subjectId;
	}
	
	public void setSubjectId(String groupId) {
		this.subjectId = groupId;
	}
	
	@ClientJsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "groupId",insertable=false,updatable=false)
	public SubjectEntity getSubjectEntity() {
		return subjectEntity;
	}
	
	public void setSubjectEntity(SubjectEntity subjectEntity) {
		this.subjectEntity = subjectEntity;
	}
	
	@ClientJsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "roleId",insertable=false,updatable=false)
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	@ClientJsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId",insertable=false,updatable=false)
	public AccountEntity getAccountEntity() {
		return accountEntity;
	}
	
	public void setAccountEntity(AccountEntity accountEntity) {
		this.accountEntity = accountEntity;
	}

	@Transient
	public boolean isBlackList() {
		return blackList;
	}

	public void setBlackList(boolean blackList) {
		this.blackList = blackList;
	}

	@ClientJsonIgnore
	@Transient
	public long getNoteCount() {
		return noteCount;
	}

	public void setNoteCount(long noteCount) {
		this.noteCount = noteCount;
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
	public String getRoleName() {
		return getRole().getRoleName();
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public Long getCreateTimeStamp() {
		return createTimeStamp;
	}

	public void setCreateTimeStamp(Long createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
	}

	public Long getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(Long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
	}

	public java.lang.String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(java.lang.String createUserId) {
		this.createUserId = createUserId;
	}

	public java.lang.String getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(java.lang.String updateUserId) {
		this.updateUserId = updateUserId;
	}

}
