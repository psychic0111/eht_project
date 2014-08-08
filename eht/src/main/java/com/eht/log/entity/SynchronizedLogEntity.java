package com.eht.log.entity;

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
 * @Description: 同步完成的日志记录
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
	
	private String classPk;
	
	private String action;
	
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

	public long getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(long operateTime) {
		this.operateTime = operateTime;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(String targetUser) {
		this.targetUser = targetUser;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassPk() {
		return classPk;
	}

	public void setClassPk(String classPk) {
		this.classPk = classPk;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
