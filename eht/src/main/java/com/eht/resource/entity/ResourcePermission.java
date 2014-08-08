package com.eht.resource.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="eht_resourcepermission")
public class ResourcePermission implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private String roleId;
	
	private String resourceName;
	
	private String primaryKey;
	
	private int actionIds;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public int getActionIds() {
		return actionIds;
	}

	public void setActionIds(int actionIds) {
		this.actionIds = actionIds;
	}

}
