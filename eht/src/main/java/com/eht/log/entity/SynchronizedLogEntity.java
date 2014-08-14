package com.eht.log.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**   
 * @Title: Entity
 * @Description: 本次同步中的日志记录
 * @author zhangdaihao
 * @date 2014-03-21 14:49:54
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_usedlog", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class SynchronizedLogEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**ID*/
	private int id;
	/**同步操作时间*/
	private long operateTime;
	/**客户端标识*/
	private String clientId;
	/**用户ID*/
	private String targetUser;
	/**同步日志ID*/
	private String logId;
	
	private String className;
	
	private String classPK;
	
	private String action;
	/**同步状态*/
	private int status;
	
	
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  ID
	 */
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	public int getId(){
		return this.id;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  ID
	 */
	public void setId(int id){
		this.id = id;
	}
	@Column(updatable=false)
	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}
	@Column(updatable=false)
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	@Column(updatable=false)
	public String getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(String targetUser) {
		this.targetUser = targetUser;
	}
	@Column(updatable=false)
	public String getLogId() {
		return logId;
	}
	@Column(updatable=false)
	public void setLogId(String logId) {
		this.logId = logId;
	}
	@Column(updatable=false)
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	@Column(updatable=false)
	public String getClassPK() {
		return classPK;
	}

	public void setClassPK(String classPK) {
		this.classPK = classPK;
	}
	@Column(updatable=false)
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
