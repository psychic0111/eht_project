package com.eht.user.controller;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.common.model.json.ValidForm;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.Md5Utils;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import com.eht.common.constant.Constants;
import com.eht.user.entity.AccountEntity;
import com.eht.user.service.AccountServiceI;

/**   
 * @Title: Controller
 * @Description: 用户信息
 * @author yuhao
 * @date 2014-03-18 11:47:52
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/accountController")
public class AccountController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(AccountController.class);
	@Autowired
	private AccountServiceI accountService;
	@Autowired
	private JdbcTokenRepositoryImpl tokenRepository;
	@Autowired
	private SystemService systemService;
	private String message;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	/**
	 * 后台用户信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "account")
	public ModelAndView account(HttpServletRequest request) {
		return new ModelAndView("com/eht/user/accountList");
	}

	/**
	 * easyui AJAX请求数据
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(AccountEntity account,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		account.setDeleted(Constants.DATA_NOT_DELETED);
		CriteriaQuery cq = new CriteriaQuery(AccountEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, account, request.getParameterMap());
		this.accountService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除用户信息
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(AccountEntity account, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		account = systemService.getEntity(AccountEntity.class, account.getId());
		account.setDeleted(Constants.DATA_DELETED);
		systemService.saveOrUpdate(account);
		message = "用户信息删除成功";
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}

	/**
	 * 用户信息查看页面
	 * 
	 * @return
	 */
	@RequestMapping(params = "show")
	public ModelAndView show(AccountEntity account, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(account.getId())) {
			account = accountService.getEntity(AccountEntity.class, account.getId());
			req.setAttribute("accountPage", account);
		}
		return new ModelAndView("com/eht/user/account");
	}
	
	/**
	 * 检查验证码
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "checkCode")
	@ResponseBody
	public ValidForm checkCode(HttpServletRequest request,HttpServletResponse response) {
		ValidForm v = new ValidForm();
		String codesession=oConvertUtils.getString((String) request.getSession().getAttribute("VerifiCode"));
		String code=oConvertUtils.getString(request.getParameter("param"));
		if(codesession.equals("")){
			v.setInfo("请重新刷新验证码");
			v.setStatus("n");
		}else{
			if(code.equals("")||!code.equals(codesession)){
				v.setInfo("验证码不正确");
				v.setStatus("n");
			}
		}
		return v;
	}

	/**修改密码
	 * 
	 */
	@RequestMapping("/front/resetpwd.dht")
    public ModelAndView resetpwd(){
		 return new ModelAndView("/user/resetpwd");
    }   
	@RequestMapping("/front/resetpwdDo.dht")
    public ModelAndView resetpwdDo(HttpSession session,HttpServletRequest request){
		
		 String pwd = request.getParameter("password");
		 String url = "/user/edituserinfo";
		 ModelMap mmp = new ModelMap();
		 if(pwd!=null&&!pwd.equals("")){
			 try {
				 AccountEntity user  =  accountService.getUser4Session(); 
				 user.setPassword(Md5Utils.makeMD5(pwd));
				 accountService.updateAccount(user);
				 // 修改密码后，移除用户token
				 tokenRepository.removeUserTokens(user.getUsername());
				 mmp.put("msg", "修改成功！");
			} catch (Exception e) {
				url = "/user/resetpwd";
				mmp.put("msg", "修改失败，请重新再试或请联系联系管理员！");
				e.printStackTrace();
			}
		 }
		 ModelAndView mv =  new ModelAndView(url);
		 mv.addAllObjects(mmp);
		 return mv;
    }
	@RequestMapping("/front/checkpwd.dht")
    public void getpwd(HttpSession session,HttpServletRequest request,HttpServletResponse response){
		 String bool = "false";
		 String oldpwd = request.getParameter("oldpassword");
		 AccountEntity user = accountService.getUser4Session();
		 if(oldpwd!=null&&Md5Utils.makeMD5(oldpwd).equals(user.getPassword())){
			 bool = "true";
		 } 
		 try {
			response.getWriter().print(bool);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	/**修改个人基本信息
	 * 
	 */
	@RequestMapping("/front/viewEditUser.dht")
     public ModelAndView viewEditUser(HttpServletRequest request){
        String msg = request.getParameter("msg");
		 ModelMap mmp = new ModelMap();
		 if(msg!=null&&!msg.equals("")){
			 mmp.put("msg", msg);
		 }
		 ModelAndView mv = new ModelAndView("/user/edituserinfo");
		 mv.addAllObjects(mmp);
		 return mv;
     } 
	@RequestMapping("/front/editUser.dht")
     public ModelAndView editUser(HttpSession session,AccountEntity account,HttpServletRequest request){
		 List<String> l=null;
		 AccountEntity user  =  accountService.getUser4Session();
		 l=	accountService.uploadFile(request,user); 
		 if(user!=null){
				if(l!=null&&l.size()>0){
					user.setPhoto(l.get(0));
				}
			 user.setMobile(account.getMobile());
			 accountService.updateAccount(user);
		 }
		 return new ModelAndView(new RedirectView("viewEditUser.dht")); 
     }
	
	/**
	 * 添加用户信息
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "save")
	@ResponseBody
	public AjaxJson save(AccountEntity account, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		if (StringUtil.isNotEmpty(account.getId())) {
			message = "用户信息更新成功";
			AccountEntity t = accountService.get(AccountEntity.class, account.getId());
			try {
				t.setStatus(account.getStatus());
				accountService.saveOrUpdate(t);
				systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
			} catch (Exception e) {
				e.printStackTrace();
				message = "用户信息更新失败";
			}
		} 
		j.setMsg(message);
		return j;
	}
	
	
}
