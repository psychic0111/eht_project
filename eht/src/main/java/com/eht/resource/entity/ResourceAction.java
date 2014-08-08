package com.eht.resource.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="eht_resourceaction")
public class ResourceAction implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	/**
	 * 资源名称
	 */
	private String resourceName;
	/**
	 * 操作,例：ADD DELETE
	 */
	private String action;
	/**
	 * 权限值: 0 1 2 4
	 */
	private int bitwiseValue;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getBitwiseValue() {
		return bitwiseValue;
	}

	public void setBitwiseValue(int bitwiseValue) {
		this.bitwiseValue = bitwiseValue;
	}
	
}
