<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ page import="weibo4j.*,weibo4j.http.*,weibo4j.org.json.*"%>

<%
	String context=request.getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + context;
	String uid=null;//用户ID
	String code=request.getParameter("code");//微博返回的客户端
	System.out.println("========================================================="+code);
	Oauth oauth = new Oauth();
	oauth.authorize("code", "", "");
	weibo4j.http.AccessToken token = oauth.getAccessTokenByCode(code);
	Account am = new Account();
	am.client.setToken(token.getAccessToken());
	JSONObject uidObject = am.getUid();
	uid=uidObject.getString("uid");
	String logintype="sina"; 
	request.getSession().setAttribute("3uinfo", uid+"\t"+logintype);
	response.sendRedirect(basePath+"/center/openLogin.dht");
%>

