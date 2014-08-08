package com.eht.group.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="eht_group_user")
public class GroupUser implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private String UserId;
	
	private Long GroupId;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public Long getGroupId() {
		return GroupId;
	}

	public void setGroupId(Long groupId) {
		GroupId = groupId;
	}

}
