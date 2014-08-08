package com.eht.note.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

/**   
 * @Title: Entity
 * @Description: 条目版本
 * @author yuhao
 *
 *
 */
@Entity
@Table(name = "eht_version", schema = "")
@DynamicUpdate(true)
@DynamicInsert(true)
public class NoteVersionEntity implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	/**ID*/
	private java.lang.String id;
	
	/**条目iD*/
	private String noteid;

	/**版本*/
	private Integer version;
	
	/**创建人*/
	private String createuser;
	
	/**内容*/
	private String 	content;
	
	/**创建时间*/
	private java.util.Date createtime;
    
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@Column(name ="ID",nullable=false,length=32)
	public java.lang.String getId() {
		return id;
	}

	public void setId(java.lang.String id) {
		this.id = id;
	}
	
	@Column(name ="noteid",nullable=true,length=32)
	public String getNoteid() {
		return noteid;
	}

	public void setNoteid(String noteid) {
		this.noteid = noteid;
	}
	@Column(name ="version",nullable=true,precision=10,scale=0)
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	@Column(name ="createuser",nullable=true,length=32)
	public String getCreateuser() {
		return createuser;
	}

	public void setCreateuser(String createuser) {
		this.createuser = createuser;
	}
	@Column(name ="content",nullable=true)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Column(name ="createtime",nullable=true)
	public java.util.Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(java.util.Date createtime) {
		this.createtime = createtime;
	}
	
	
}
