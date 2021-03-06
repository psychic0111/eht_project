package com.eht.log.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;

/**   
 * @Title: Entity
 * @Description: 同步日志
 * @author zhangdaihao
 * @date 2014-03-21 14:49:54
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_synchlog", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class SynchLogEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**数据类型*/
	private java.lang.String className;
	/**数据主键*/
	private java.lang.String classPK;
	/**	日志影响用户（需同步此操作的用户） */
	private String targetUser;
	/**数据操作*/
	private java.lang.String action;
	/** 数据操作值：ADD 1, DELETE -1, UPDATE 0 */
	private java.lang.Integer operateType;
	/**操作人*/
	private java.lang.String operateUser;
	/**数据操作发生时间*/
	private long operateTime;
	/**同步操作时间*/
	private long synchTime;
	/**客户端类型:*/
	private java.lang.String clientType;
	/**客户端标识*/
	private String clientId;
	/**操作结果*/
	private Integer operateResult;
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  ID
	 */
	
	@Id
	@Column(name ="ID",nullable=false,length=32)
	@JsonIgnore
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

	public java.lang.String getClassName() {
		return className;
	}

	public void setClassName(java.lang.String className) {
		this.className = className;
	}

	public java.lang.String getClassPK() {
		return classPK;
	}

	public void setClassPK(java.lang.String classPK) {
		this.classPK = classPK;
	}
	
	@Column(updatable = false)
	public java.lang.String getAction() {
		return action;
	}

	public void setAction(java.lang.String action) {
		this.action = action;
		if(action != null){
			if(action.equals(DataSynchAction.ADD.toString())){
				this.operateType = 1;
			}else if(action.equals(DataSynchAction.DELETE.toString())){
				this.operateType = -1;
			}else if(action.equals(DataSynchAction.UPDATE.toString())){
				this.operateType = 0;
			}
		}
	}
	
	@JsonIgnore
	public java.lang.Integer getOperateType() {
		return operateType;
	}

	public void setOperateType(java.lang.Integer operateType) {
		this.operateType = operateType;
	}

	public java.lang.String getOperateUser() {
		return operateUser;
	}

	public void setOperateUser(java.lang.String operateUser) {
		this.operateUser = operateUser;
	}

	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}

	public java.lang.String getClientType() {
		return clientType;
	}

	public void setClientType(java.lang.String clientType) {
		this.clientType = clientType;
	}
	@JsonIgnore
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	@JsonIgnore
	public Integer getOperateResult() {
		return operateResult;
	}

	public void setOperateResult(Integer operateResult) {
		this.operateResult = operateResult;
	}
	
	@JsonIgnore
	public String getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(String targetUser) {
		this.targetUser = targetUser;
	}
	
	@ClientJsonIgnore
	public long getSynchTime() {
		return synchTime;
	}

	public void setSynchTime(long synchTime) {
		this.synchTime = synchTime;
	}
	
}
