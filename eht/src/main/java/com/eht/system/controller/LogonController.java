package com.eht.system.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.ws.security.util.UUIDGenerator;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.Md5Utils;
import org.jeecgframework.core.util.SendMailUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.web.system.manager.ClientManager;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import com.eht.common.constant.Constants;
import com.eht.common.enumeration.DataSynchAction;
import com.eht.common.util.AppRequstUtiles;
import com.eht.common.util.StringUtil;
import com.eht.message.entity.MessageEntity;
import com.eht.message.service.MessageServiceI;
import com.eht.subject.entity.InviteMememberEntity;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.service.SubjectServiceI;
import com.eht.system.bean.SendEmailSession;
import com.eht.user.entity.AccountEntity;
import com.eht.user.entity.GadUserEntity;
import com.eht.user.service.AccountServiceI;

/**   
 * @Title: 
 * @Description: 用户登陆信息
 * @author ZengHui
 * @date 2014-03-18 11:47:52
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/center")
public class LogonController extends BaseController {
	@Autowired
	private SubjectServiceI subjectService;
	
	@Autowired
	private MessageServiceI messageService;
	
	/**
	 * 登陆
	 * @return
	 */
	@RequestMapping(value="/login.dht",method=RequestMethod.POST)
	public ModelAndView account(HttpServletRequest request,HttpSession session,HttpServletResponse response) {
		ModelMap mmp = new ModelMap();
		ModelAndView mv = null;
		
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		AccountEntity u = null;
		//username = StringEscapeUtils.escapeJavaScript(StringEscapeUtils.escapeHtml(username));
		password = Md5Utils.makeMD5(request.getParameter("password"));
		
		if(username!=null&&password!=null){
			try {
				u = accountService.findUserByAccount(username);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(u!=null&&u.getStatus()!=null&&u.getStatus()==Constants.ACTIVATE){
			mv = new ModelAndView("/login");
			mmp.put("username", username);
			mmp.put("password", request.getParameter("password"));
			mmp.put("sendmail", "1");
			mmp.put("message", "账号未激活！");
			mv.addAllObjects(mmp); 
			return mv;
		}
		if(u!=null&&u.getPassword()!=null&&u.getPassword().equals(password) && u.getStatus() == Constants.ENABLED && u.getDeleted() == Constants.DATA_NOT_DELETED){
			session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, u);
			ClientManager.getInstance().addSession(session.getId(), session);
			//绑定账号信息
			String openid = request.getParameter("openId"),
			openuser = request.getParameter("openUser"),
			opentype = request.getParameter("type"),
			uid = u.getId();
			saveGadUser(openid,openuser,opentype,uid);
			String host = request.getServerName();
			Cookie cookie = new Cookie("username", username);
			cookie.setPath("/");  
			cookie.setMaxAge(30*60);
			cookie.setDomain(host); 
			response.addCookie(cookie);
			mv = new ModelAndView(new RedirectView("/indexController/front/index.dht", true));
		}else{
			mmp.put("message", "账号或密码错误！");
			mmp.put("username", username);
			mmp.put("password", request.getParameter("password"));
			mv = new ModelAndView("login");
		}
		mv.addAllObjects(mmp); 
		
		return mv;
	}
	
	/**
	 * 邮件重发
	 * 
	 * @return
	 */
	@RequestMapping("/repeat.dht")
	public ModelAndView repeat( HttpServletRequest request) {
		String linkname = null;
		String linkpath = null;
		try {
			msg = "请查看邮件并激活账号！";
			String type=request.getParameter("type");
			AccountEntity account = null;
			if(type!=null&&type.equals("1")){
				account =accountService.findUserByAccount(request.getParameter("username"));
			}else{
				account =accountService.findUserByEmail(request.getParameter("username"));
			}
			String path = AppRequstUtiles.getAppUrl() + "/";
			SendMailUtil.sendCommonMail(account.getEmail(), "注册E划通", "<a href=\""+path+"center/register.dht?id="+account.getId()+"\">点击激活帐号</a><br/>");
		  } catch (Exception e) {
	     	e.printStackTrace();
	    } 
		return linkLoginMessage(msg,null,linkpath,linkname,null);
	}
	
	/**
	 * 
	 * 邮件重发ajax
	 * @return
	 */
	@RequestMapping("/repeatajax.dht")
	@ResponseBody
	public AjaxJson sujectSchedule(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try {
			 AccountEntity	account =accountService.findUserByAccount(request.getParameter("username"));
			String path = AppRequstUtiles.getAppUrl() + "/";
			SendMailUtil.sendCommonMail(account.getEmail(), "注册E划通", "<a href=\""+path+"center/register.dht?id="+account.getId()+"\">点击激活帐号</a><br/>");
			j.setSuccess(true);
		  } catch (Exception e) {
			  j.setSuccess(false);
	    } 
		return j;
	}
	
	
	/**
	 * 第三方登陆验证
	 * @return
	 */
	@RequestMapping("/openLogin.dht") 
	public ModelAndView openLogin(HttpServletRequest request,HttpSession session) { 
		ModelMap mmp = new ModelMap();
		ModelAndView mv = null;

		String openId = request.getParameter("openId");
		String openUser = request.getParameter("openUser");
		String logintype = request.getParameter("type");
		
		GadUserEntity gad =  accountService.findUserByGad(logintype, openId);
		//跳转到账号绑定页面
		mv = new ModelAndView("uniteLogin");
		if(gad != null && gad.getOpenType().equals(logintype)){
			AccountEntity user = accountService.getUser(gad.getUid());
			if(user!=null){
				if(user.getPassword()==null){
					//跳转到账号激活提示页面。
					mv = new ModelAndView("/user/register_status");
					mmp.put("msg", "此账户还没有激活，请查收邮件激活注册！");
				}else{
					session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);
					ClientManager.getInstance().addSession(session.getId(), session);
					//跳转到用户主页面
					mv = new ModelAndView(new RedirectView("/indexController/front/index.dht", true));
					if(logintype.equals(Constants.OPEN_LOGIN_QQ)){
						mv = new ModelAndView("qqresult");
					}
				}
			}
		}
		mmp.put("openId", openId);
		mmp.put("openUser", openUser);
		mmp.put("type", logintype);
		mv.addAllObjects(mmp); 
		return mv;
	}
	
	
	/**
	 * 登录页面
	 * @return
	 */
	@RequestMapping("/viewLogin.dht")
	public ModelAndView viewLogin() {
		ModelAndView mv = new ModelAndView("login"); 
		return mv;
	}
	
	/**
	 * 登出
	 * @return
	 */
	@RequestMapping("/logout.dht")
	public ModelAndView loginout(HttpSession session) {
		ModelMap mmp = new ModelMap();
		ModelAndView mv = null; 
		session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, null);
		mmp.put("logout", "true");
		mv = new ModelAndView("login");
		mv.addAllObjects(mmp); 
		return mv;
	}

	/**
	 * 注册用户信息
	 * 
	 * @return
	 */
	@RequestMapping("/reg.dht")
	public ModelAndView save(AccountEntity account, HttpServletRequest request) {
		String linkname = null;
		String linkpath = null;
		String viewUrl = null;
		try {
			msg = "注册成功!";
			String openid = request.getParameter("openId");
			String openuser = request.getParameter("openUser");
			String opentype = request.getParameter("type");
			
			account.setId(com.eht.common.util.UUIDGenerator.uuid());
			account.setStatus(Constants.ACTIVATE);
			account.setDeleted(Constants.DATA_NOT_DELETED);
			account.setCreatetime(new Date());
			account.setUpdatetime(new Date());
			account.setPassword(Md5Utils.makeMD5(account.getPassword()));
			
			if(request.getParameter("id") != null && !request.getParameter("id").equals("")){
				InviteMememberEntity inviteMememberEntity=subjectService.get(InviteMememberEntity.class,  request.getParameter("id"));
				if(inviteMememberEntity!=null){
					subjectService.acceptInviteMember(inviteMememberEntity,account);
					saveRegUserInfo(account);
				}else if(StringUtil.isValidateString(openid)){//第三方登录绑定帐号注册，直接激活
					viewUrl = "front/index";
					saveRegUserInfo(account);
					HttpSession session = request.getSession();
					session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, account);
					ClientManager.getInstance().addSession(session.getId(), session);
				}else{
					accountService.save(account);
					String path = AppRequstUtiles.getAppUrl();
					SendMailUtil.sendCommonMail(account.getEmail(), "注册E划通", "<a href=\""+path+"/center/register.dht?id="+account.getId()+"\">点击激活帐号</a><br/>");
					msg = "注册成功！请查看邮件并激活账号！";
				}
			}
			//绑定账号信息
			String uid = account.getId();
			saveGadUser(openid,openuser,opentype,uid);
		  } catch (Exception e) {
	     	e.printStackTrace();
	     	msg = "注册失败！";
	     	linkpath = "webpage/register.jsp";
	     	linkname = "注册";
	    } 
		return linkLoginMessage(msg,null,linkpath,linkname,viewUrl);
	}
	
	private void saveRegUserInfo(AccountEntity account){
		MessageEntity msg = new MessageEntity();
		msg.setClassName(AccountEntity.class.getName());
		msg.setClassPk(account.getId());
		msg.setContent("请完善自己的帐号信息。");
		Date date = new Date();
		msg.setCreateTime(date);
		msg.setCreateTimeStamp(date.getTime());
		msg.setCreateUser(null);
		msg.setId(com.eht.common.util.UUIDGenerator.uuid());
		msg.setUserIsRead(Constants.NOT_READ_OBJECT);
		msg.setOperate(DataSynchAction.UPDATE.toString());
		msg.setMsgType(Constants.MSG_SYSTEM_TYPE);
		messageService.saveMessages(msg, account.getId());
		
		account.setStatus(Constants.ENABLED);
		accountService.save(account);
		
		subjectService.updateEntitie(account);
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
	
	private void saveGadUser(String openid,String openuser,String opentype,String uid){
		//添加绑定注册信息
		if(openid!=null&&!openid.isEmpty()){
			 GadUserEntity gadUser = new GadUserEntity();
			 gadUser.setOpenid(openid);
			 gadUser.setUid(uid);
			 gadUser.setOpenUser(openuser);
			 gadUser.setOpenType(opentype);
			 gadUser.setCreatetime(new Date());
			 subjectService.save(gadUser);
		}
	}

	/**
	 * 通过邮箱找回密码
	 * 
	 * @return
	 */
	@RequestMapping(value="/sendEmailPWD.dht", method=RequestMethod.POST)
	public ModelAndView sendEmailPWD(HttpServletRequest request) {
		String linkname = null;
		String linkpath = null;
		try {
			String email = request.getParameter("email");
			String btnValue = request.getParameter("button");
			if (StringUtil.SQLInjection(email) || StringUtil.SQLInjection(btnValue)) {  
				msg = "含有非法字符！";
		     	linkpath = "webpage/user/retrievepassword.jsp";
		     	linkname = "密码找回"; 
		    }else{
		
				String code = Md5Utils.makeMD5(email)+UUIDGenerator.getUUID().toString();
				SendEmailSession ses = new SendEmailSession();
				ses.setEmail(email);
				ses.setCode(code);
				msg = "发送成功请查看邮件重置密码";
				 
				accountService.save(ses);
				String path = AppRequstUtiles.getAppUrl() + "/";
				SendMailUtil.sendCommonMail(ses.getEmail(), "E划通 密码找回", "<a href=\""+path+"center/reqEmailPWD.dht?code="+ses.getCode()+"\">点击修改您的密码</a><br/>");
				systemService.addLog(msg, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		    }
		  } catch (Exception e) {
	     	e.printStackTrace();
	     	msg = "发送失败！";
	     	linkpath = "/user/retrievepassword";
	     	linkname = "密码找回";
	    }
		return linkLoginMessage(msg,null,linkpath,linkname,null);
	}
	
	

	/**
	 * 通过邮箱找回密码
	 * 
	 * @return
	 */
	@RequestMapping("/reqEmailPWD.dht")
	public ModelAndView reqEmailPWD(HttpServletRequest request) {
		ModelMap mmp = new ModelMap();
		String code = request.getParameter("code"); 
		SendEmailSession ses =  systemService.findUniqueByProperty(SendEmailSession.class, "code", code);
		String url = "/user/reqemailpwd";
		String msg = "";
		if(ses!=null){
			mmp.put("email", ses.getEmail()); 
		}else{
			msg = "链接已过期！"; 
			url = "/user/register_status";
		}
		return linkLoginMessage(msg,mmp,null,null,url);
	}

	/**
	 * 通过邮箱设置密码
	 * 
	 * @return
	 */
	@RequestMapping("/setEmailPWD.dht")
	public ModelAndView setEmailPWD(HttpServletRequest request,HttpSession session) {
		ModelAndView mv = null;
		String email = request.getParameter("email"); 
		String password = request.getParameter("password");
		String msg = "";
		try {
			AccountEntity account =  systemService.findUniqueByProperty(AccountEntity.class, "email", email);
			if(account!=null){
				account.setPassword(Md5Utils.makeMD5(password));
			}
			accountService.updateEntitie(account);
			accountService.delEmailSessionByEmal(email);
			//注册session 并跳转到首页
			mv = linkIndex(session,account);
		    msg = "修改成功！";
		} catch (Exception e) {
		    msg = "修改失败！";
		    linkLoginMessage(msg,null,null,null,null);
		    e.printStackTrace();
		}
		return mv;
	} 
	
	/**
	 * 验证用户名
	 * 
	 * @return
	 */
	@RequestMapping("/checkUser.dht")
	public void checkUser(HttpServletRequest request,HttpServletResponse response) {
		String bool = "false";
		String username = request.getParameter("userName");
		AccountEntity u = accountService.findUserByAccount(username);
		if(u==null){
			  bool = "true";
		} 
		try {
			response.getWriter().print(bool);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 验证账户是否激活
	 * 
	 * @return
	 */
	@RequestMapping("/checkUserStats.dht")
	public @ResponseBody String checkUserStats(HttpServletRequest request,HttpServletResponse response) {
		String bool = "true";
		String username = request.getParameter("username");
		AccountEntity u = accountService.findUserByEmail(username);
		if(u!=null&&u.getStatus()!=null&&u.getStatus()==Constants.ACTIVATE){
			  bool = "false";
		}
		return bool;
	}

	/**
	 * 验证注册邮箱
	 * 
	 * @return
	 */
	@RequestMapping("/checkEmail.dht")
	public void checkEmail(HttpServletRequest request,HttpServletResponse response) {
		String bool = "false";
		String email = request.getParameter("email");
		AccountEntity u = accountService.findUserByEmail(email);
		if(u==null){
			  bool = "true";
		}
		try {
			response.getWriter().print(bool);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 验证注册邮箱是否不存在
	 * 
	 * @return
	 */
	@RequestMapping("/checkisEmail.dht")
	public void checkisEmail(HttpServletRequest request,HttpServletResponse response) {
		String bool = "true";
		String email = request.getParameter("email");
		AccountEntity u = accountService.findUserByEmail(email);
		if(u==null){
			  bool = "false";
		}
		try {
			response.getWriter().print(bool);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 跳转注册页面
	 * 
	 * @return
	 */
	@RequestMapping("/register.dht") 
	public ModelAndView register(HttpServletRequest request) {
		AccountEntity account = accountService.getEntity(AccountEntity.class,request.getParameter("id"));
		if(account.getStatus()!=null&&account.getStatus()!=Constants.ACTIVATE){
			String msg="此账号已经完成注册";
			return linkLoginMessage(msg,null,null,null,null);
		}else{
			// 点击激活邮件直接跳转到首页
			request.setAttribute("accountPage", account);
			//return new ModelAndView("/user/accountRegister");
			MessageEntity msg = new MessageEntity();
			msg.setClassName(AccountEntity.class.getName());
			msg.setClassPk(account.getId());
			msg.setContent("请完善自己的帐号信息。");
			Date date = new Date();
			msg.setCreateTime(date);
			msg.setCreateTimeStamp(date.getTime());
			msg.setCreateUser(null);
			msg.setId(com.eht.common.util.UUIDGenerator.uuid());
			msg.setUserIsRead(Constants.NOT_READ_OBJECT);
			msg.setOperate(DataSynchAction.UPDATE.toString());
			msg.setMsgType(Constants.MSG_SYSTEM_TYPE);
			messageService.saveMessages(msg, account.getId());
			
			account.setStatus(Constants.ENABLED);
			subjectService.updateEntitie(account);
			
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
			return new ModelAndView(new RedirectView("/webpage/login.jsp", true));
		}
	}
	
	/**
	 * 注册页面添加信息
	 * 
	 * @return
	 */
	@RequestMapping("/registerSave.dht") 
	public ModelAndView registerSave(HttpServletRequest request,HttpSession session) {
		 ModelAndView mv = null;
		try{
				String id = request.getParameter("id");
				AccountEntity account = accountService.getEntity(AccountEntity.class,id);
				List<String> l=null;
				String msg = "";
				try {
					 l = accountService.uploadFile(request, account);
					 msg = "恭喜注册成功";
				} catch (Exception e) {
					 msg = "图片上传失败";
					request.setAttribute("id", id);
				}
				if(l!=null&&l.size()>0){
					account.setPhoto(l.get(0));
				}
				//注册session 并跳转到首页
				account.setStatus(Constants.ENABLED);
				mv = linkIndex(session,account);
		}catch(Exception e){
			mv = linkLoginMessage(msg,null,null,null,null); 
		}
		return  mv;
	}
	//跳到登陆页面并提示 message信息
	/**
	 * 
	 * @param message 提示信息
	 * @param mmp 数据容器
	 * @param linkpath 跳转路径
	 * @param linkname 跳转名称
	 * @param viewUrl  返回视图
	 * @return
	 */
	private ModelAndView linkLoginMessage(String message,ModelMap mmp,String linkpath,String linkname,String viewUrl){
		mmp = mmp==null?new ModelMap():mmp;
		ModelAndView mv = new ModelAndView(viewUrl==null?"user/register_status":viewUrl);
     	mmp.put("linkpath", linkpath==null?"":linkpath);
     	mmp.put("linkname", linkname==null?"登陆":linkname);
     	mmp.put("msg", message);
     	mv.addAllObjects(mmp);
		return mv;
	}
	
	/**
	 * 
	 * @param session
	 * @param account
	 * @return
	 */
	private ModelAndView linkIndex(HttpSession session,AccountEntity account){
		session.setAttribute(Constants.SESSION_USER_ATTRIBUTE, user);
		ClientManager.getInstance().addSession(session.getId(), session);
		accountService.activeUser(account,session.getId());
		//跳转到用户主页面
		return new ModelAndView(new RedirectView("/indexController/front/index.dht", true));	
	}
	/**
	 * 检查验证码
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping("/checkCode.dht") 
	public void checkCode(HttpServletRequest request,HttpServletResponse response) {
		String bool = "true";
		String codesession=oConvertUtils.getString((String) request.getSession().getAttribute("VerifiCode"));
		String code=oConvertUtils.getString(request.getParameter("code"));
		if(code.equals("")||!code.equals(codesession)){
			bool = "false";
		}
		try {
			response.getWriter().print(bool);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(LogonController.class); 
	@Autowired
	private AccountServiceI accountService;  
	@Autowired
	private SystemService systemService;

	private String msg;
	
	private AccountEntity user;
	public AccountEntity getUser() {
		return user;
	}
	public void setUser(AccountEntity user) {
		this.user = user;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	 
}
