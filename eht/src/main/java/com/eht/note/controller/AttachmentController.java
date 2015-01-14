package com.eht.note.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.common.UploadFile;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.MyClassLoader;
import org.jeecgframework.core.util.ReflectHelper;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.service.AttachmentServiceI;

/**   
 * @Title: Controller
 * @Description: 条目附件
 * @author yuhao
 * @date 2014-03-31 11:02:09
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/attachmentController")
public class AttachmentController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(AttachmentController.class);

	@Autowired
	private AttachmentServiceI attachmentService;
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
	 * 条目附件列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "attachment")
	public ModelAndView attachment(HttpServletRequest request) {
		return new ModelAndView("com/eht/note/attachmentList");
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
	public void datagrid(AttachmentEntity attachment,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(AttachmentEntity.class, dataGrid);
		//查询条件组装器
		attachment.setDeleted(0);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, attachment, request.getParameterMap());
		this.attachmentService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除条目附件
	 * 
	 * @return
	 */
	@RequestMapping(params = "del")
	@ResponseBody
	public AjaxJson del(AttachmentEntity attachment, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		attachment = systemService.getEntity(AttachmentEntity.class, attachment.getId());
		attachment.setUpdateTime(new Date());
		attachment.setUpdateUser(ResourceUtil.getSessionUserName().getUserName());
		message = "条目附件删除成功";
		attachment.setDeleted(1);
		attachmentService.saveOrUpdate(attachment);
		systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		return j;
	}

	
	/**
	 * 条目附件列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(AttachmentEntity attachment, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(attachment.getId())) {
			attachment = attachmentService.getEntity(AttachmentEntity.class, attachment.getId());
			req.setAttribute("attachmentPage", attachment);
		}
		return new ModelAndView("com/eht/note/attachment");
	}
	
	/**
	 * 附件下载
	 * 
	 * @return
	 */
	@RequestMapping(params = "viewFile")
	public ModelAndView viewFile(HttpServletRequest request, HttpServletResponse response) {
		AttachmentEntity attachment = attachmentService.getEntity(AttachmentEntity.class,request.getParameter("id"));
		response.setContentType("UTF-8");
		response.setCharacterEncoding("UTF-8");
		InputStream bis = null;
		BufferedOutputStream bos = null;
		String ctxPath = request.getSession().getServletContext().getRealPath("/");
		String downLoadPath = "";
		long fileLength = 0;
		if (attachment.getFilePath()!= null) {
			downLoadPath = ctxPath + attachment.getFilePath();
			fileLength = new File(downLoadPath).length();
			try {
				bis = new BufferedInputStream(new FileInputStream(downLoadPath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} 
		try {
				if (attachment.getSuffix().equals("text")) {
					response.setContentType("text/plain;");
				} else if (attachment.getSuffix().equals("doc")) {
					response.setContentType("application/msword;");
				} else if (attachment.getSuffix().equals("xls")) {
					response.setContentType("application/ms-excel;");
				} else if (attachment.getSuffix().equals("pdf")) {
					response.setContentType("application/pdf;");
				} else if (attachment.getSuffix().equals("jpg") || attachment.getSuffix().equals("jpeg")) {
					response.setContentType("image/jpeg;");
				} else {
					response.setContentType("application/x-msdownload;");
				}
				response.setHeader("Content-disposition", "attachment; filename=" + new String((attachment.getFileName() + "." + attachment.getSuffix()).getBytes("GBK"), "ISO8859-1"));
				response.setHeader("Content-Length", String.valueOf(fileLength));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
