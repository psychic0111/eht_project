package com.eht.user.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jeecgframework.core.common.service.CommonService;

import com.eht.user.entity.AccountEntity;
import com.eht.user.entity.GadUserEntity;

public interface AccountServiceI extends CommonService{
	/**
	 * 查找用户id
	 * @param account
	 * @return
	 */
	public GadUserEntity findUserByGad(String account);
	/**
	 * 
	 * @param ids
	 * @return
	 */
	public List<AccountEntity> findAccountsByIds(String[] ids);
	/**
	 * 根据帐号查询用户
	 * @param account
	 * @return
	 */
	public AccountEntity findUserByAccount(String username);
	
	/**
	 * 更新用户
	 * @param user
	 */
	public void updateAccount(AccountEntity user);
	
	/**
	 * 根据邮箱查询用户
	 * @param account
	 * @return
	 */
	public AccountEntity findUserByEmail(String email);
	/**
	 * 删除邮箱找回密码临时状态
	 * @param account
	 * @return
	 */
	public void delEmailSessionByEmal(String email);
	
	/**
	 * 根据邮箱模糊匹配用户
	 * @param account
	 * @return
	 */
	public List<AccountEntity> findUserByEmail(String email,int size);

	/**
	 * 上传
	 * @param account
	 * @return
	 */
	public List<String> uploadFile(HttpServletRequest request,AccountEntity user);

	/**
	 *  获取用户对象,sessionId可传null
	 * @return
	 */
	public AccountEntity getUser4Session(String sessionId);
	
	/**
	 *  查询用户    
	 * @return
	 */
	public AccountEntity getUser(String userId);
	
	/**
	 * 获取用户
	 * @return
	 */
	public AccountEntity getUser4Session();
	
	/**
	 * 激活用户
	 */
	public void activeUser(AccountEntity account,String sessionId);
}
