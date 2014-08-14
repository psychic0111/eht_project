package com.eht.subject.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 目录信息
 * @author zhangdaihao
 * @date 2014-03-21 15:00:05
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_directory", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class DirectoryEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**目录名称*/
	private java.lang.String dirName;
	/**所属专题*/
	private java.lang.String subjectId;
	/**上级目录ID*/
	private java.lang.String parentId;
	/**删除标识*/
	private java.lang.Integer deleted;
	/**创建者*/
	private java.lang.String createUser;
	/**创建时间*/
	private java.util.Date createTime;
	/**修改者*/
	private java.lang.String updateUser;
	/**修改时间*/
	private java.util.Date updateTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	private SubjectEntity subjectEntity;
	/**树父ID*/
	private String pId;
	
	private List<NoteEntity> noteEntitylist;
	
	private List<AttachmentEntity> attachmentEntitylist;
	
	private AccountEntity accountCreateUser;
	
	private AccountEntity accountUpdateUser;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	/**目录名称生成mht用*/
	private java.lang.String dirNameTitle;
	
	@Transient
	public java.lang.String getCreateUserId() {
		return createUser;
	}

	public void setCreateUserId(java.lang.String createUserId) {
		this.createUserId = createUserId;
		this.createUser = createUserId;
	}
	
	@Transient
	public java.lang.String getUpdateUserId() {
		return updateUser;
	}

	public void setUpdateUserId(java.lang.String updateUserId) {
		this.updateUserId = updateUserId;
		this.updateUser = updateUserId;
	}
	
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", insertable=false, updatable=false)
	public AccountEntity getAccountCreateUser() {
		return accountCreateUser;
	}

	public void setAccountCreateUser(AccountEntity accountCreateUser) {
		this.accountCreateUser = accountCreateUser;
	}
   
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updateUser", insertable=false, updatable=false)
	public AccountEntity getAccountUpdateUser() {
		return accountUpdateUser;
	}

	public void setAccountUpdateUser(AccountEntity accountUpdateUser) {
		this.accountUpdateUser = accountUpdateUser;
	}
	
	

	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  ID
	 */
	private String oldId;

	@Id
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
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  目录名称
	 */
	public java.lang.String getDirName() {
		return dirName;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  目录名称
	 */
	public void setDirName(java.lang.String dirName) {
		this.dirName = dirName;
	}

	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  所属专题
	 */
	public java.lang.String getSubjectId(){
		return this.subjectId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  所属专题
	 */
	public void setSubjectId(java.lang.String subjectId){
		this.subjectId = subjectId;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  上级目录ID
	 */
	public java.lang.String getParentId(){
		return this.parentId;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  上级目录ID
	 */
	public void setParentId(java.lang.String parentId){
		this.parentId = parentId;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标识
	 */
	@Column(columnDefinition="int default 0")
	@ClientJsonIgnore
	public java.lang.Integer getDeleted(){
		return this.deleted;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  删除标识
	 */
	public void setDeleted(java.lang.Integer deleted){
		this.deleted = deleted;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  创建者
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  创建者
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  创建时间
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
		if(this.createTime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createTime.getTime(); 
		}
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  修改者
	 */
	public java.lang.String getUpdateUser() {
		return updateUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  修改者
	 */
	public void setUpdateUser(java.lang.String updateUser) {
		this.updateUser = updateUser;
	}
	/**
	 *方法: 取得java.util.Date
	 *@return: java.util.Date  修改时间
	 */
	@ClientJsonIgnore
	public java.util.Date getUpdateTime() {
		return updateTime;
	}

	/**
	 *方法: 设置java.util.Date
	 *@param: java.util.Date  修改时间
	 */
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
		if(this.updateTime != null && this.updateTimeStamp == null){
			this.updateTimeStamp = this.updateTime.getTime();
		}
	}
    
	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subjectId", insertable=false, updatable=false)
	public SubjectEntity getSubjectEntity() {
		return subjectEntity;
	}

	public void setSubjectEntity(SubjectEntity subjectEntity) {
		this.subjectEntity = subjectEntity;
	}
	
	@Transient
	@ClientJsonIgnore
	public String getPId() {
		if(parentId == null || parentId.equals("")){
			return this.subjectId;
		}else{
			return this.parentId;
		}
	}

	public void setPId(String pId) {
		this.pId = pId;
	}
	
	@Transient
	@ClientJsonIgnore
	public String getOldId() {
		return oldId;
	}

	public void setOldId(String oldId) {
		this.oldId = oldId;
	}
	
	@Transient
	@ClientJsonIgnore
	public List<NoteEntity> getNoteEntitylist() {
		return noteEntitylist;
	}

	public void setNoteEntitylist(List<NoteEntity> noteEntitylist) {
		this.noteEntitylist = noteEntitylist;
	}
	
	@Transient
	@ClientJsonIgnore
	public List<AttachmentEntity> getAttachmentEntitylist() {
		return attachmentEntitylist;
	}

	public void setAttachmentEntitylist(List<AttachmentEntity> attachmentEntitylist) {
		this.attachmentEntitylist = attachmentEntitylist;
	}

	public Long getCreateTimeStamp() {
		return createTimeStamp;
	}

	public void setCreateTimeStamp(Long createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
		if(this.createTimeStamp != null && this.createTime == null){
			this.createTime = new Date(this.createTimeStamp);
		}
	}

	public Long getUpdateTimeStamp() {
		return updateTimeStamp;
	}

	public void setUpdateTimeStamp(Long updateTimeStamp) {
		this.updateTimeStamp = updateTimeStamp;
		if(this.updateTimeStamp != null && this.updateTime == null){
			this.updateTime = new Date(this.updateTimeStamp);
		}
	}
    
	@Transient
	public java.lang.String getDirNameTitle() {
		return dirNameTitle;
	}

	public void setDirNameTitle(java.lang.String dirNameTitle) {
		this.dirNameTitle = dirNameTitle;
	}
	
	
}
