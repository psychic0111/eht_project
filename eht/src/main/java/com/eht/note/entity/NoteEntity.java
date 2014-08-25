package com.eht.note.entity;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.common.bean.BaseSubjectModel;
import com.eht.common.util.HtmlParser;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.user.entity.AccountEntity;

/**
 * @Title: Entity
 * @Description: 条目信息
 * @author yuhao
 * @date 2014-03-31 10:13:10
 * @version V1.0
 */
@Entity
@Table(name = "eht_note", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
@XmlRootElement(name = "note")
public class NoteEntity extends BaseSubjectModel implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/** ID */
	private java.lang.String id;
	/** 标题 */
	private java.lang.String title;
	/** 内容 */
	private java.lang.String content;
	/** 所属目录 */
	private java.lang.String dirId;
	/** 所属专题 */
	private java.lang.String subjectId;
	/** 版本号 */
	private java.lang.Integer version;
	/** MD5 */
	private java.lang.String md5;
	/** 删除标识 */
	private java.lang.Integer deleted;
	/** 创建时间 */
	private java.util.Date createTime;
	/** 创建人 */
	private java.lang.String createUser;
	/** 修改人 */
	private java.lang.String updateUser;
	/** 修改时间 */
	private java.util.Date updateTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	/** 标签 */
	private String tagId;
	/** 父标签 */
	private String parentTag;
	/** 根标签 */
	private String rootTag;

	private String oldId;

	/** 专题 */
	private SubjectEntity subjectEntity;

	private DirectoryEntity directoryEntity;

	private List<AttachmentEntity> attachmentEntitylist;

	private AccountEntity accountCreateUser;

	private AccountEntity accountUpdateUser;

	private String sujectType;
	
	private String summary;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	
	/** 标题 */
	private java.lang.String titleMht;
	/** 内容 */
	private java.lang.String contentMht;
	
	private String operation;
	
	private String className;
	
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
	
	@JsonIgnore
	// getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", insertable=false, updatable=false)
	public AccountEntity getAccountCreateUser() {
		return accountCreateUser;
	}

	public void setAccountCreateUser(AccountEntity accountCreateUser) {
		this.accountCreateUser = accountCreateUser;
	}

	@JsonIgnore
	// getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updateUser", insertable=false, updatable=false)
	public AccountEntity getAccountUpdateUser() {
		return accountUpdateUser;
	}

	public void setAccountUpdateUser(AccountEntity accountUpdateUser) {
		this.accountUpdateUser = accountUpdateUser;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String ID
	 */

	@Id
	public java.lang.String getId() {
		return this.id;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String ID
	 */
	public void setId(java.lang.String id) {
		this.id = id;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String 标题
	 */
	public java.lang.String getTitle() {
		return this.title;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String 标题
	 */
	public void setTitle(java.lang.String title) {
		this.title = title;
	}

	/**
	 * 方法: 取得java.lang.Object
	 * 
	 * @return: java.lang.Object 内容
	 */
	public java.lang.String getContent() {
		return this.content;
	}

	/**
	 * 方法: 设置java.lang.Object
	 * 
	 * @param: java.lang.Object 内容
	 */
	public void setContent(java.lang.String content) {
		this.content = content;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String 所属目录
	 */
	public java.lang.String getDirId() {
		return dirId;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String 所属目录
	 */
	public void setDirId(java.lang.String dirId) {
		this.dirId = dirId;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String 所属专题
	 */
	public java.lang.String getSubjectId() {
		return subjectId;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String 所属专题
	 */
	public void setSubjectId(java.lang.String subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * 方法: 取得java.lang.Integer
	 * 
	 * @return: java.lang.Integer 版本号
	 */
	@ClientJsonIgnore
	public java.lang.Integer getVersion() {
		return this.version;
	}

	/**
	 * 方法: 设置java.lang.Integer
	 * 
	 * @param: java.lang.Integer 版本号
	 */
	public void setVersion(java.lang.Integer version) {
		this.version = version;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String MD5
	 */
	public java.lang.String getMd5() {
		return this.md5;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String MD5
	 */
	public void setMd5(java.lang.String md5) {
		this.md5 = md5;
	}

	/**
	 * 方法: 取得java.lang.Integer
	 * 
	 * @return: java.lang.Integer 删除标识
	 */
	@Column(columnDefinition="int default 0")
	@ClientJsonIgnore
	public java.lang.Integer getDeleted() {
		return this.deleted;
	}

	/**
	 * 方法: 设置java.lang.Integer
	 * 
	 * @param: java.lang.Integer 删除标识
	 */
	public void setDeleted(java.lang.Integer deleted) {
		this.deleted = deleted;
	}

	/**
	 * 方法: 取得java.util.Date
	 * 
	 * @return: java.util.Date 创建时间
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.util.Date getCreateTime() {
		return createTime;
	}

	/**
	 * 方法: 设置java.util.Date
	 * 
	 * @param: java.util.Date 创建时间
	 */
	public void setCreateTime(java.util.Date createTime) {
		this.createTime = createTime;
		if(this.createTime != null && this.createTimeStamp == null){
			this.createTimeStamp = this.createTime.getTime(); 
		}
	}

	/**
	 * 方法: 取得java.lang.Integer
	 * 
	 * @return: java.lang.Integer 创建人
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 * 方法: 设置java.lang.Integer
	 * 
	 * @param: java.lang.Integer 创建人
	 */
	public void setCreateUser(java.lang.String createUser) {
		this.createUser = createUser;
	}

	/**
	 * 方法: 取得java.lang.String
	 * 
	 * @return: java.lang.String 修改人
	 */
	public java.lang.String getUpdateUser() {
		return updateUser;
	}

	/**
	 * 方法: 设置java.lang.String
	 * 
	 * @param: java.lang.String 修改人
	 */
	public void setUpdateUser(java.lang.String updateUser) {
		this.updateUser = updateUser;
	}

	/**
	 * 方法: 取得java.util.Date
	 * 
	 * @return: java.util.Date 修改时间
	 */
	@ClientJsonIgnore
	public java.util.Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * 方法: 设置java.util.Date
	 * 
	 * @param: java.util.Date 修改时间
	 */
	public void setUpdateTime(java.util.Date updateTime) {
		this.updateTime = updateTime;
		if(this.updateTime != null && this.updateTimeStamp == null){
			this.updateTimeStamp = this.updateTime.getTime();
		}
	}

	@JsonIgnore
	// getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subjectId", insertable=false, updatable=false)
	public SubjectEntity getSubjectEntity() {
		return subjectEntity;
	}

	public void setSubjectEntity(SubjectEntity subjectEntity) {
		this.subjectEntity = subjectEntity;
	}

	@JsonIgnore
	// getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dirId", insertable=false, updatable=false)
	public DirectoryEntity getDirectoryEntity() {
		return directoryEntity;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	@ClientJsonIgnore
	public String getParentTag() {
		return parentTag;
	}

	public void setParentTag(String parentTag) {
		this.parentTag = parentTag;
	}

	@ClientJsonIgnore
	public String getRootTag() {
		return rootTag;
	}

	public void setRootTag(String rootTag) {
		this.rootTag = rootTag;
	}

	public void setDirectoryEntity(DirectoryEntity directoryEntity) {
		this.directoryEntity = directoryEntity;
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
	public List<AttachmentEntity> getAttachmentEntitylist() {
		return attachmentEntitylist;
	}

	public void setAttachmentEntitylist(List<AttachmentEntity> attachmentEntitylist) {
		this.attachmentEntitylist = attachmentEntitylist;
	}

	@Transient
	@ClientJsonIgnore
	public String getSujectType() {
		return sujectType;
	}

	public void setSujectType(String sujectType) {
		this.sujectType = sujectType;
	}

	@Transient
	@ClientJsonIgnore
	public String getSummary() {
		if(content!=null){
			HtmlParser parser = new HtmlParser(content);
			return parser.subStrAsText(50, "...");
			
		}
		return content;
		
	}

	public void setSummary(String summary) {
		this.summary = summary;
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

	@Override
	public String findOwnSubjectId() {
		return this.subjectId;
	}
    
	@Transient
	public java.lang.String getContentMht() {
		return contentMht;
	}

	public void setContentMht(java.lang.String contentMht) {
		this.contentMht = contentMht;
	}
	
	@Transient
	public java.lang.String getTitleMht() {
		return titleMht;
	}

	public void setTitleMht(java.lang.String titleMht) {
		this.titleMht = titleMht;
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
	
}
