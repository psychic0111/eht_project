package com.eht.module.webservice.testservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jeecgframework.core.util.JSONHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.eht.common.bean.ResponseStatus;
import com.eht.note.entity.NoteEntity;
import com.eht.note.service.NoteServiceI;
import com.eht.testbase.TestBaseService;

public class TestDataSynchizeService extends TestBaseService{
	
	/*@Autowired
	private NoteServiceI service;*/
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * 根据ID获取条目
	 */
	@Test
	@Ignore
	public void testGetNoteById() {
		String noteId = "11";  //数据库中存在该记录
		String path = "/note/";
		client = client.path(path + noteId);
		NoteEntity note = client.get(NoteEntity.class);
		Assert.assertEquals("11", note.getId());
		
		noteId = "12";   //数据库中不存在该记录
		client = client.back(true).path(path + noteId);
		Response res = client.get();
		Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), res.getStatus());
	}
	
	/**
	 * 更新条目内容接口	
	 */
	@Test
	@Ignore
	public void testUpdateNoteContent() {
		HttpClient httpClient = new HttpClient();
		PostMethod method = new PostMethod(client.getBaseURI().toString() + "/note_u");
		try {
			method.addParameter("id", "11");
			method.addParameter("content", "33311");
			httpClient.executeMethod(method);
			String result = method.getResponseBodyAsString();
			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 上传文件接口
	 * ID=1、ID=2的文件MD5相同
	 */
	@Test
	@Ignore
	public void testUploadAttachment(){
		HttpClient httpClient = new HttpClient();
		
		/** 从头完整上传文件   */
		String attachmentId = "3";        //附件元数据ID
		String uri = client.getBaseURI().toString() + "/upload/" + attachmentId;
		PostMethod method = new PostMethod(uri);
		File file = new File("e:/搜索引擎需求整理-11111.docx");   //本地文件
		RequestEntity entity = new FileRequestEntity(file, MediaType.APPLICATION_OCTET_STREAM);
		method.setRequestEntity(entity);
		try {
			httpClient.executeMethod(method);
			String str = method.getResponseBodyAsString();
			System.out.println(str);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			method.releaseConnection();
		}
		
		/** 秒传文件   */
		attachmentId = "2";        //附件元数据ID
		uri = client.getBaseURI().toString() + "/upload/" + attachmentId;
		method = new PostMethod(uri);
		file = new File("e:/搜索引擎需求整理-11111.docx");   //本地文件
		entity = new FileRequestEntity(file, MediaType.APPLICATION_OCTET_STREAM);
		method.setRequestEntity(entity);
		try {
			httpClient.executeMethod(method);
			String str = method.getResponseBodyAsString();
			System.out.println(str);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			method.releaseConnection();
		}
		
	}
	
	/**
	 * ID=4数据的transfer大于0，状态为未完成，说明以前上传过该文件
	 */
	@Test
	@Ignore
	public void testContinueUploadAttachment(){
		HttpClient httpClient = new HttpClient();
		/** 续传文件   */
		String attachmentId = "4";        //附件元数据ID
		String uri = client.getBaseURI().toString() + "/upload/" + attachmentId;
		PostMethod method = new PostMethod(uri);
		File file = new File("e:/VMware-workstation_myhack58.rar");   //本地文件
		FileRequestEntity entity = new FileRequestEntity(file, MediaType.APPLICATION_OCTET_STREAM);
		method.setRequestEntity(entity);
		
		try {
			httpClient.executeMethod(method);
			String str = method.getResponseBodyAsString();
			System.out.println("上传已经上传字节：" + str);
			
			//准备续传文件
			ResponseStatus res = JSONHelper.fromJsonToObject(str, ResponseStatus.class);
			long position = 0;//Long.parseLong(res.getStatus());
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(position);
			int cap = (raf.length() - position) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)(raf.length() - position);
			byte[] b = new byte[cap];
			raf.readFully(b);
			InputStream inputStream = new ByteArrayInputStream(b);
			InputStreamRequestEntity insEntity = new InputStreamRequestEntity(inputStream, MediaType.APPLICATION_OCTET_STREAM);
			
			uri = client.getBaseURI().toString() + "/upload/resume/" + attachmentId;
			method = new PostMethod(uri);
			method.setRequestEntity(insEntity);
			httpClient.executeMethod(method);
			String sss = method.getResponseBodyAsString();
			System.out.println("续传文件结果 ：" + sss);
			
			raf.close();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			method.releaseConnection();
		}
	}
}
