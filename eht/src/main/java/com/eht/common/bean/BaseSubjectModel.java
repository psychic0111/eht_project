package com.eht.common.bean;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import com.eht.note.service.NoteServiceI;

public abstract class BaseSubjectModel implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Autowired
	protected NoteServiceI noteService;
	
	public abstract String findOwnSubjectId();
}
