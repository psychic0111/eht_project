package com.eht.user.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.util.ContextHolderUtils;
import org.jeecgframework.core.util.DataUtils;
import org.jeecgframework.core.util.FileUtils;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.web.system.manager.ClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.eht.common.annotation.RecordOperate;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.enumeration.DataType;
import com.eht.common.util.UUIDGenerator;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.SubjectServiceI;
import com.eht.user.entity.AccountEntity;
import com.eht.user.entity.GadUserEntity;
import com.eht.user.service.AccountServiceI;

@Service("accountService")
@Transactional
public class AccountServiceImpl extends CommonServiceImpl implements AccountServiceI {

	@Autowired
	private SubjectServiceI subjectService;
	
	@Override
	public List<AccountEntity> findAccountsByIds(String[] ids) {
		String query = "from AccountEntity n where id=-1 ";
		if(ids!=null&&ids.length>0){
			for(String id:ids){
				query += " or n.id = '"+id+"' "; 
			}
		}
		List<AccountEntity> list = findByQueryString(query);
		return list;
	}

	@Override
	public AccountEntity findUserByAccount(String account) {
		return findUniqueByProperty(AccountEntity.class, "userName", account);
	}
	
	@Override
	public GadUserEntity findUserByGad(String account) {
		return findUniqueByProperty(GadUserEntity.class, "openid", account);
	}

	@Override
	public AccountEntity findUserByEmail(String email) {
		return findUniqueByProperty(AccountEntity.class, "email", email);
	}

	@Override
	public void delEmailSessionByEmal(String email) {
		this.commonDao.executeHql("delete from SendEmailSession ses where email='"+email+"'");
	}
	@Override
	public List<AccountEntity> findUserByEmail(String email, int size) {
		DetachedCriteria dc = DetachedCriteria.forClass(AccountEntity.class);
		dc.add(Restrictions.like("email", email,MatchMode.ANYWHERE));
		List<AccountEntity> list= pageList(dc, 0,size);
		return list;
	}  
	@Override 
	public List<String> uploadFile(HttpServletRequest request,AccountEntity user){
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		List<String> list=new ArrayList<String>();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile mf = entity.getValue();// 获取上传文件对象
			String fileName = mf.getOriginalFilename();// 获取文件名
			if(fileName==null||fileName.equals("")){ 
				if(user.getPhoto()==null||user.getPhoto().equals("")){
					String defaultPath = "/" + uppath + "/user.png";
					list.add(defaultPath);
				}
			}else{
				String extend = FileUtils.getExtend(fileName);// 获取文件扩展名
				String path = uppath + "/";// 文件保存在硬盘的相对路径
				String realPath = multipartRequest.getSession().getServletContext().getRealPath("/") + "/" + path;
				File file = new File(realPath);
				if (!file.exists()) {
					file.mkdirs();// 创建根目录
				}
				String noextfilename = UUIDGenerator.uuid();//自定义文件名称
			    String myfilename=noextfilename+"."+extend;
			    String savePath = realPath + myfilename;// 文件保存全路径
			    File savefile = new File(savePath);
			    try {
					FileCopyUtils.copy(mf.getBytes(), savefile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			    String xdpath= path + myfilename;
			    list.add(xdpath);
			}
		}
		return list;
	}
	
	@Override
	public AccountEntity getUser4Session(){
		return getUser4Session(null);
	}
	
	@Override
	public AccountEntity getUser4Session(String sessionId){
		AccountEntity user = null;
		Object o = null;
		if(sessionId == null){
			o = ContextHolderUtils.getSession().getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		}else{
			o = ClientManager.getInstance().getSession(sessionId) == null ? null : ClientManager.getInstance().getSession(sessionId).getAttribute(Constants.SESSION_USER_ATTRIBUTE);
		}
		if (o != null) {
			user = (AccountEntity) o;
		}else{
			try {
				SecurityContext context = SecurityContextHolder.getContext();
				//UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) context.getAuthentication();
				RememberMeAuthenticationToken authentication = (RememberMeAuthenticationToken) context.getAuthentication();
				user = (AccountEntity) authentication.getPrincipal();
			} catch (Exception e) {
				return null;
			}
		}
		return user;
	}

	@Override
	@RecordOperate(dataClass=DataType.USER, action=DataSynchAction.UPDATE, keyIndex=0, keyMethod="getId")
	public void updateAccount(AccountEntity user) {
		updateEntitie(user);
	}

	@Override
	public AccountEntity getUser(String userId) {
		AccountEntity user = get(AccountEntity.class, userId);
		return user;
	}

	public void activeUser(AccountEntity account,String sessionId) {
		super.updateEntitie(account);
		ClientManager.getInstance().getSession(sessionId).setAttribute(Constants.SESSION_USER_ATTRIBUTE, account);
		
		SubjectEntity subject = new SubjectEntity();
		subject.setCreateUser(account.getId());
		subject.setCreateTime(new Date());
		subject.setId(account.getId() + "_S");
		subject.setDescription("");
		subject.setSubjectType(1);
		subject.setStatus(0);
		subject.setDeleted(0);
		subject.setSubjectName("默认专题");	
		List<SubjectEntity> list = subjectService.findSubjectByParam(subject.getSubjectName(), account.getId(), subject.getSubjectType());
		if(list == null || list.isEmpty()){
			subjectService.addSubject(subject, account.getId());
		}
	}
	
}