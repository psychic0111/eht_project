package com.eht.note.entity;

import java.io.InputStream;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.eht.common.annotation.ClientJsonIgnore;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.user.entity.AccountEntity;

/**   
 * @Title: Entity
 * @Description: 条目附件
 * @author zhangdaihao
 * @date 2014-03-31 11:02:09
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_attachment", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class AttachmentEntity implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	/**ID*/
	private java.lang.String id;
	/**文件名*/
	private java.lang.String fileName;
	/**文件后缀*/
	private java.lang.String suffix;
	/**文件类型 1.条目、目录附件 2.条目内容图片 3.条目内容css 4.条目内容js */
	private java.lang.Integer fileType;
	/**所属条目*/
	private java.lang.String noteId;
	/**文件路径：不包括文件名,以‘/’结尾 */
	private java.lang.String filePath;
	/**MD5*/
	private java.lang.String md5;
	/**已传输*/
	private java.lang.Long tranSfer;
	/**状态: 0 未完成  1上传完成*/
	private java.lang.Integer status;
	/**删除标记*/
	private java.lang.Integer deleted;
	/**创建者*/
	private java.lang.String createUser;
	/**创建时间*/
	private java.util.Date createTime;
	
	/**updateuser*/
	private java.lang.String updateUser;
	/**修改时间*/
	private java.util.Date updateTime;
	
	/**创建时间毫秒*/
	private Long createTimeStamp;
	/**修改时间毫秒*/
	private Long updateTimeStamp;
	
	/**所属目录*/
	private String directoryId;
	/**
	 * 文件数据
	 */
	private InputStream inputStream;
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  ID
	 */
	private NoteEntity noteEntity;
	
	private DirectoryEntity directoryEntity;
	
	private AccountEntity creator;
	
	/**创建人,接口使用*/
	private java.lang.String createUserId;
	/**修改者,接口使用*/
	private java.lang.String updateUserId;
	private  String tempFilePath;
	
	
	private java.lang.String fileNameMht;
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
	 *@return: java.lang.String  文件名
	 */
	public java.lang.String getFileName() {
		return fileName;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  文件名
	 */
	public void setFileName(java.lang.String fileName) {
		this.fileName = fileName;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  文件类型
	 */
	public java.lang.String getSuffix(){
		return this.suffix;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  文件类型
	 */
	public void setSuffix(java.lang.String suffix){
		this.suffix = suffix;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  所属条目
	 */
	public java.lang.String getNoteId() {
		return noteId;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  所属条目
	 */
	public void setNoteId(java.lang.String noteId) {
		this.noteId = noteId;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  文件路径
	 */
	@ClientJsonIgnore
	public java.lang.String getFilePath() {
		return filePath;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  文件路径
	 */
	public void setFilePath(java.lang.String filePath) {
		this.filePath = filePath;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  MD5
	 */
	public java.lang.String getMd5(){
		return this.md5;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  MD5
	 */
	public void setMd5(java.lang.String md5){
		this.md5 = md5;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  已传输
	 */
	@ClientJsonIgnore
	public java.lang.Long getTranSfer() {
		return tranSfer;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  已传输
	 */
	public void setTranSfer(java.lang.Long tranSfer) {
		this.tranSfer = tranSfer;
	}
	
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  状态
	 */
	@ClientJsonIgnore
	public java.lang.Integer getStatus(){
		return this.status;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  状态
	 */
	public void setStatus(java.lang.Integer status){
		this.status = status;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  删除标记
	 */
	@ClientJsonIgnore
	@Column(columnDefinition="int default 0")
	public java.lang.Integer getDeleted(){
		return this.deleted;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  删除标记
	 */
	public void setDeleted(java.lang.Integer deleted){
		this.deleted = deleted;
	}
	/**
	 *方法: 取得java.lang.Integer
	 *@return: java.lang.Integer  创建者
	 */
	@ClientJsonIgnore
	@Column(updatable=false)
	public java.lang.String getCreateUser() {
		return createUser;
	}

	/**
	 *方法: 设置java.lang.Integer
	 *@param: java.lang.Integer  创建者
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
	
	@Transient
	@ClientJsonIgnore
	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  updateuser
	 */
	@ClientJsonIgnore
	public java.lang.String getUpdateUser() {
		return updateUser;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  updateuser
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
	@JoinColumn(name = "noteId", updatable=false, insertable=false)
	public NoteEntity getNoteEntity() {
		return noteEntity;
	}

	public void setNoteEntity(NoteEntity noteEntity) {
		this.noteEntity = noteEntity;
	}
	
	@Column(name ="directoryId",nullable=true,length=32)
	public String getDirectoryId() {
		return directoryId;
	}

	public void setDirectoryId(String directoryId) {
		this.directoryId = directoryId;
	}

	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "directoryId", updatable=false, insertable=false)
	public DirectoryEntity getDirectoryEntity() {
		return directoryEntity;
	}
	
	public void setDirectoryEntity(DirectoryEntity directoryEntity) {
		this.directoryEntity = directoryEntity;
	}

	@JsonIgnore    //getList查询转换为列表时处理json转换异常
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "createUser", updatable=false, insertable=false)
	public AccountEntity getCreator() {
		return creator;
	}

	public void setCreator(AccountEntity creator) {
		this.creator = creator;
	}

	public java.lang.Integer getFileType() {
		return fileType;
	}

	public void setFileType(java.lang.Integer fileType) {
		this.fileType = fileType;
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
	public String getTempFilePath() {
		return tempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		this.tempFilePath = tempFilePath;
	}

	@Transient
	public java.lang.String getFileNameMht() {
		return fileNameMht;
	}

	public void setFileNameMht(java.lang.String fileNameMht) {
		this.fileNameMht = fileNameMht;
	}
		
}
