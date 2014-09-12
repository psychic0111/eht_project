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
	 * 专题ID
	 */
	private String groupId;
	
	private AccountEntity accountEntity;
	
	private SubjectEntity subjectEntity;
	
	private Role role;
	
	private boolean blackList;
	
	private long noteCount;
	
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
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "groupId",insertable=false,updatable=false)
	public SubjectEntity getSubjectEntity() {
		return subjectEntity;
	}
	
	public void setSubjectEntity(SubjectEntity subjectEntity) {
		this.subjectEntity = subjectEntity;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "roleId",insertable=false,updatable=false)
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
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

	@Transient
	public long getNoteCount() {
		return noteCount;
	}

	public void setNoteCount(long noteCount) {
		this.noteCount = noteCount;
	}
	
}
