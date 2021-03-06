package com.eht.log.aop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecgframework.core.util.ContextHolderUtils;
import org.jeecgframework.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.constant.SynchConstants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.ReflectionUtils;
import com.eht.common.util.UUIDGenerator;
import com.eht.log.entity.SynchLogEntity;
import com.eht.log.service.SynchLogServiceI;
import com.eht.message.entity.MessageEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.note.entity.NoteTag;
import com.eht.note.service.NoteServiceI;
import com.eht.role.entity.RoleUser;
import com.eht.role.service.RoleService;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.DirectoryServiceI;
import com.eht.subject.service.SubjectServiceI;
import com.eht.tag.entity.TagEntity;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;
import com.eht.webservice.util.SynchDataCache;

@Aspect
@Component
public class OperateLogInterceptor {

	@Autowired
	private AccountServiceI accountService;

	@Autowired
	private SubjectServiceI subjectService;

	@Autowired
	private SynchLogServiceI synchLogService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private DirectoryServiceI directoryService;

	@Autowired
	private NoteServiceI noteService;
	
	@Autowired
	private MessageServiceI messageService;

	@Pointcut("execution(* com.eht.webservice.service.impl.DataSynchizeServiceImpl.*(..))")
	public void pointCut() {
	}

	/*@Before("@annotation(sc)")
	public void addResponseHeader(JoinPoint jp, SynchControl sc) {
		Object[] args = jp.getArgs();
		HttpServletResponse response = null;
		for(Object obj : args){
			if(obj != null && obj instanceof HttpServletResponse){
				response = (HttpServletResponse) obj;
				break;
			}
		}
		if(response != null){
			response.addHeader(SynchConstants.HEADER_DATATYPE, sc.dataType());
			response.addHeader(SynchConstants.HEADER_ACTION, sc.action());
			response.addHeader(SynchConstants.HEADER_NEXT_ACTION, sc.nextAction());
			response.addHeader(SynchConstants.HEADER_NEXT_DATATYPE, sc.nextDataType());
		}
	}*/
	
	@AfterReturning("@annotation(rp)")
	public void recordLog(JoinPoint jp, RecordOperate rp) {
		// 获取方法参数
		Object[] args = jp.getArgs();
		Object paramEntity = args[0]; // 增删改实体参数都放在第一个
		AccountEntity user = accountService.getUser4Session();
		if(user == null){
			String sessionId = String.valueOf(ContextHolderUtils.getRequest().getAttribute("jsessionid"));
			user = accountService.getUser4Session(sessionId);
		}
		int length = rp.dataClass().length;
		for (int i = 0; i < length; i++) {
			SynchLogEntity log = new SynchLogEntity();
			log.setId(UUIDGenerator.uuid());
			log.setClassName(rp.dataClass()[i].toString());
			log.setAction(rp.action()[i].toString());
			if(log.getClassName().equals(DataType.SUBJECTUSER.toString())){
				if(args[0].getClass().getName().equals(RoleUser.class.getName())){
					RoleUser ru = (RoleUser) args[0];
					log.setOperateUser(ru.getUserId());
				}else{
					log.setOperateUser(args[1].toString());
				}
				
			}else{
				log.setOperateUser(user.getId());
			}
			log.setOperateResult(SynchConstants.LOG_NOT_SYNCHRONIZED);
			
			// 数据操作发生的时间
			if (StringUtil.isEmpty(rp.timeStamp()[i])) {
				log.setOperateTime(System.currentTimeMillis());
			} else {
				Object dateObj = ReflectionUtils.invokeGetterMethod(paramEntity, rp.timeStamp()[i]);
				if (dateObj != null) {
					Date date = (Date) dateObj;
					log.setOperateTime(date.getTime());
				}else{
					log.setOperateTime(System.currentTimeMillis());
				}
			}
			// 同步时间
			log.setSynchTime(System.currentTimeMillis());

			int index = rp.keyIndex()[i];
			String methodName = rp.keyMethod()[i];
			String primaryKey = null;
			if (StringUtil.isEmpty(methodName)) {
				primaryKey = args[index].toString();
			} else {
				 Object pk= ReflectionUtils.invokeMethod(paramEntity, methodName, null, null);
				 //yuhao 修改  
				 if(pk instanceof java.lang.String){
					 primaryKey = (String) pk;
				 }else{
					 primaryKey =  pk+"";
				 }
				
			}
			log.setClassPK(primaryKey);
			
			// 根据注解设置影响用户
			if(rp.targetUser()[i] != -1){
				log.setTargetUser(args[rp.targetUser()[i]].toString());
				synchLogService.saveSynchLog(log);
			}else{ 
				//是否需要查询操作影响用户，多人专题下的数据变更
				Map<String, String> map = isOwnShareSubject(paramEntity);
				if(map != null){
					//查询多人专题下所有成员
					List<String> userIdList = getTargetUsers(map.get("subjectId"), map.get("directoryId"), map.get("noteId"));
					if(log.getClassName().equals(DataType.SUBJECTUSER.toString()) && log.getAction().equals(DataSynchAction.ADD.toString())){
						String newMemberId = args[1].toString();
						userIdList.add(newMemberId);  // 包括刚刚加入的成员
					}
					
					for(String uid : userIdList){
						SynchLogEntity newLog = new SynchLogEntity();
						try {
							BeanUtils.copyProperties(newLog, log);
							newLog.setId(UUIDGenerator.uuid());
							newLog.setTargetUser(uid);
						} catch (Exception e) {
							e.printStackTrace();
						}
						synchLogService.saveSynchLog(newLog);
						
						// 条目操作发送系统消息给其他成员
						if(!uid.equals(user.getId())){
							if(log.getClassName().equals(DataType.NOTE.toString())){
								MessageEntity msg = new MessageEntity();
								msg.setId(log.getId());
								
								NoteEntity note = (NoteEntity) paramEntity;
								String content = msgContent(log.getAction(), user.getUserName(), note.getTitle());
								msg.setContent(content);
								msg.setClassName(SynchDataCache.getDataClass(log.getClassName()).getName());
								msg.setClassPk(log.getClassPK());
								msg.setOperate(log.getAction());
								
								Date date = new Date();
								msg.setCreateTime(date);
								msg.setCreateTimeStamp(date.getTime());
								msg.setCreateUser(user.getId());
								msg.setMsgType(Constants.MSG_SYSTEM_TYPE);
								msg.setUserIsRead(Constants.NOT_READ_OBJECT);
								messageService.saveMessages(msg, uid);
							}
						}
					}
				}else{
					// 只影响操作者本身
					log.setTargetUser(user.getId());
					synchLogService.saveSynchLog(log);
				}
			}
			
		}
	}
	
	/**
	 * 是否为多人专题下数据
	 * @param paramEntity
	 * @return map：subjectId  directoryId noteId
	 */
	private Map<String, String> isOwnShareSubject(Object paramEntity){
		String subjectId = null;
		String directoryId = null;
		String noteId = null;
		if (paramEntity.getClass().getName().equals(String.class.getName())) {
			subjectId = paramEntity.toString();
		}
		// 专题数据
		if (paramEntity.getClass().getName().equals(SubjectEntity.class.getName())) {
			SubjectEntity sub = (SubjectEntity) paramEntity;
			subjectId = sub.getId();
		}
		// 标签数据
		if (paramEntity.getClass().getName().equals(TagEntity.class.getName())) {
			TagEntity tag = (TagEntity) paramEntity;
			subjectId = tag.getSubjectId();
		}
		//目录
		if (paramEntity.getClass().getName().equals(DirectoryEntity.class.getName())) {
			DirectoryEntity dir = (DirectoryEntity) paramEntity;
			directoryId = dir.getId();
			subjectId = dir.getSubjectId();
		}
		//条目
		if (paramEntity.getClass().getName().equals(NoteEntity.class.getName())) {
			NoteEntity note = (NoteEntity) paramEntity;
			noteId = note.getId();
			subjectId = note.getSubjectId();
		}
		//条目标签关系
		if (paramEntity.getClass().getName().equals(NoteTag.class.getName())) {
			NoteTag noteTag = (NoteTag) paramEntity;
			NoteEntity note = noteService.getNote(noteTag.getNoteId());
			subjectId = note.getSubjectId();
		}
		//专题成员关系
		if (paramEntity.getClass().getName().equals(RoleUser.class.getName())) {
			RoleUser ru = (RoleUser) paramEntity;
			subjectId = ru.getGroupId();
		}
		//附件
		if (paramEntity.getClass().getName().equals(AttachmentEntity.class.getName())) {
			AttachmentEntity atta = (AttachmentEntity) paramEntity;
			String nid = atta.getNoteId();
			if (!StringUtil.isEmpty(nid)) {
				NoteEntity note = noteService.getNote(nid);
				noteId = note.getId();
				subjectId = note.getSubjectId();
			} else {
				String dirId = atta.getDirectoryId();
				if (!StringUtil.isEmpty(dirId)) {
					DirectoryEntity dir = directoryService.getDirectory(dirId);
					subjectId = dir.getSubjectId();
				}
			}
		}
		if(!StringUtil.isEmpty(subjectId)){
			SubjectEntity subject = subjectService.getSubject(subjectId);
			if (subject.getSubjectType() == Constants.SUBJECT_TYPE_M) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("subjectId", subjectId);
				map.put("directoryId", directoryId);
				map.put("noteId", noteId);
				return map;
			}
		}
		return null;
	}
	
	public List<String> getTargetUsers(String subjectId, String directoryId, String noteId) {
		List<String> userIdList = new ArrayList<String>();

		List<RoleUser> userList = roleService.findSubjectUsers(subjectId);
		for (RoleUser ru : userList) {
			if (directoryId != null) {
				if (!directoryService.inDirBlackList(ru.getUserId(), directoryId)) {
					userIdList.add(ru.getUserId());
				}
			}

			if (noteId != null) {
				if (!noteService.inNoteBlackList(ru.getUserId(), noteId)) {
					userIdList.add(ru.getUserId());
				}
			}
			if(!userIdList.contains(ru.getUserId())){
				userIdList.add(ru.getUserId());
			}
		}
		return userIdList;
	}
	
	private String msgContent(String action, String userName, String title){
		String operate = "新增";
		if(action.equals(DataSynchAction.UPDATE.toString())){
			operate = "修改";
		}
		if(action.equals(DataSynchAction.DELETE.toString())){
			operate = "删除";
		}
		String content = userName + operate + "条目: " + title;
		return content;
	}
}