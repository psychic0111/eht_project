package com.eht.resource.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="eht_classname")
public class ClassName implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long classNameId;
	
	/**
	 * 资源名称
	 */
	private String className;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="classnameid")
	public Long getClassNameId() {
		return classNameId;
	}

	public void setClassNameId(Long classNameId) {
		this.classNameId = classNameId;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}	
