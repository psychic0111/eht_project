package com.eht.note.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**   
 * @Title: Entity
 * @Description: 系统消息关系对应表
 * @author yuhao
 * @date 2014-04-02 11:52:20
 * @version V1.0   
 *
 */
@Entity
@Table(name = "eht_note_user", schema = "")
public class NoteUserEntity implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/**主键ID*/
	private java.lang.Integer id;
	/**系统消息ID*/
	private java.lang.String noteId;
	/**用户ID*/
	private java.lang.String userId;
	
	/**
	 *方法: 取得java.lang.String
	 *@return: java.lang.String  主键ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public java.lang.Integer getId(){
		return this.id;
	}

	/**
	 *方法: 设置java.lang.String
	 *@param: java.lang.String  主键ID
	 */
	public void setId(java.lang.Integer id){
		this.id = id;
	}

	public java.lang.String getNoteId() {
		return noteId;
	}

	public void setNoteId(java.lang.String noteId) {
		this.noteId = noteId;
	}

	public java.lang.String getUserId() {
		return userId;
	}

	public void setUserId(java.lang.String userId) {
		this.userId = userId;
	}
	
}
