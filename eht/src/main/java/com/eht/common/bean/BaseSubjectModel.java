package com.eht.common.bean;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import com.eht.note.service.NoteServiceI;
import com.eht.subject.service.DirectoryServiceI;

public abstract class BaseSubjectModel implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Autowired
	protected NoteServiceI noteService;
	
	@Autowired
	protected DirectoryServiceI directoryService;
	
	public abstract String findOwnSubjectId();
}
