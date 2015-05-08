package com.eht.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.eht.common.constant.Constants;
import com.eht.note.entity.AttachmentEntity;
import com.eht.note.entity.NoteEntity;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.subject.entity.MhtImg;
import com.eht.subject.entity.SubjectEntity;
import com.eht.subject.entity.SubjectMht;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class SubjectToMht {

	
	
	public static String subjectToMht(String templatePath, Map<String, Object> map,Writer out)throws Exception{
	  Template template = null;
	  Configuration freeMarkerConfig = null;
	  String htmlText= null;
	    try {
	      freeMarkerConfig = new Configuration();
	      freeMarkerConfig.setDirectoryForTemplateLoading(new File(getFilePath()));
	      freeMarkerConfig.setOutputEncoding("UTF-8");
		  template = freeMarkerConfig.getTemplate(getFileName(templatePath),new Locale("Zh_cn"), "UTF-8");
		   //htmlText = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
		   
		  File file = new File("E:\\test2.mht");    //要写入的文件
          BufferedWriter writer = null;
             if (!file.exists())
                  file.createNewFile();

           writer = new BufferedWriter(new FileWriter (file));
            //  writer.write(htmlText);
           
           //System.out.println(template.getEncoding());
          //System.out.println(freeMarkerConfig.getOutputEncoding());
		 template.process(map, out);
		 writer.close();
	    } catch (Exception e) {
	      throw new Exception();
	    }
		return htmlText;
   }
	
	public static void subjectToMht(SubjectMht subjectMht, HttpServletRequest request) {
		SubjectEntity subjectEntity=  subjectMht.getSubjectEntity();
		subjectEntity.setSubjectNameTitle(StringUtil.encode2HtmlUnicode(subjectEntity.getSubjectNameTitle()));//转码
		
		String webpath = request.getSession().getServletContext().getRealPath("/");
		List<NoteEntity> list = subjectMht.getSubjectNoteslist();
		for (NoteEntity noteEntity : list) {
			String content = noteEntity.getContent();
			if(!StringUtil.isValidateString(content)){
				content = "";
			}
			if(!StringUtil.isValidateString(noteEntity.getPlaintext())){
				noteEntity.setPlaintext("");
			}
			List<MhtImg> listImg = new ArrayList<MhtImg>();
			String contentMht=HtmlParser.repleceHtmlImg(content, listImg, webpath);
			noteEntity.setContentMht(StringUtil.encode2HtmlUnicode(contentMht));
			subjectMht.addMhtImg(listImg);
			
			noteEntity.setTitleMht(StringUtil.encode2HtmlUnicode(noteEntity.getTitle()));
			List<AttachmentEntity> attachmentlist=noteEntity.getAttachmentEntitylist();
			if(attachmentlist!=null){
				for (AttachmentEntity attachmentEntity : attachmentlist) {
					attachmentEntity.setFileNameMht(StringUtil.encode2HtmlUnicode(attachmentEntity.getFileName()));
				}
			}
		}
		List<DirectoryEntity> listk = subjectMht.getSortList();
		for (DirectoryEntity directoryEntity : listk) {
			directoryEntity.setDirNameTitle(StringUtil.encode2HtmlUnicode(directoryEntity.getDirNameTitle()));
			List<AttachmentEntity>  attrList=directoryEntity.getAttachmentEntitylist();
			if(attrList!=null){
				for (AttachmentEntity attachmentEntity : attrList) {
					attachmentEntity.setFileNameMht(StringUtil.encode2HtmlUnicode(attachmentEntity.getFileName()));
				}
			}
			List<NoteEntity> dirnotelist=directoryEntity.getNoteEntitylist();
			if(dirnotelist!=null){
			for (NoteEntity noteEntity : dirnotelist) {
				String content = noteEntity.getContent();
				
				if(!StringUtil.isValidateString(content)){
					content = "";
				}
				if(!StringUtil.isValidateString(noteEntity.getPlaintext())){
					noteEntity.setPlaintext("");
				}
				List<MhtImg> listImg = new ArrayList<MhtImg>();
				String contentMht=HtmlParser.repleceHtmlImg(content, listImg, webpath);
				noteEntity.setContentMht(StringUtil.encode2HtmlUnicode(contentMht));
				subjectMht.addMhtImg(listImg);
				
				noteEntity.setTitleMht(StringUtil.encode2HtmlUnicode(noteEntity.getTitle()));
				
				List<AttachmentEntity> attachmentlist=noteEntity.getAttachmentEntitylist();
				if(attachmentlist!=null){
					for (AttachmentEntity attachmentEntity : attachmentlist) {
						attachmentEntity.setFileNameMht(StringUtil.encode2HtmlUnicode(attachmentEntity.getFileName()));
					}
				}
			}
			}
		}

	}
	
	private static boolean isLeafDir(List<DirectoryEntity> dirList, DirectoryEntity directory){
		for(DirectoryEntity dir : dirList){
			if(dir.getParentId() != null && !"".equals(dir.getParentId())){
				if(dir.getParentId().equals(directory.getId())){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 找出根目录
	 * @param dirList
	 * @return
	 */
	public static List<DirectoryEntity> findRootDirectory(List<DirectoryEntity> dirList){
		List<DirectoryEntity> rootList = new ArrayList<DirectoryEntity>();
		for(DirectoryEntity dir : dirList){
			if(dir.getDirName().equals(Constants.SUBJECT_DOCUMENT_DIRNAME)){
				dir.setMark(Constants.SUBJECT_DOCUMENT_DIRNAME);
			}
			if (dir.getParentId() == null || dir.getParentId().equals("")) {
				rootList.add(dir);
			}
		}
		return rootList;
	}
	
	/**
	 * 构建树形目录
	 * @param dirList
	 * @param parentDir
	 */
	public static void parseDataStrut(List<DirectoryEntity> dirList, DirectoryEntity parentDir){
		List<DirectoryEntity> children = new ArrayList<DirectoryEntity>();
		for(DirectoryEntity dir : dirList){
			if (dir.getParentId() != null && dir.getParentId().equals(parentDir.getId())) {
				if (!isLeafDir(dirList, dir)) {
					parseDataStrut(dirList, dir);
				}
				if(parentDir.getMark() != null && parentDir.getMark().equals(Constants.SUBJECT_DOCUMENT_DIRNAME)){
					dir.setMark(Constants.SUBJECT_DOCUMENT_DIRNAME);
				}
				children.add(dir);
			}
		}
		parentDir.setChildDirlist(children);
	}
	
	private static String getFileName(String path) {
		path = path.replace("\\", "/");
		return path.substring(path.lastIndexOf("/") + 1);
	}
	//获取mht 模板目录
	private static String getFilePath() {
  		String path = getAppPath(SubjectToMht.class);
  		path = path + File.separator +"wordtempalte"+File.separator;
		path = path.replace("\\", "/");
		return path;
	}
	public static String getAppPath(Class cls) {
		// 检查用户传入的参数是否为空
		if (cls == null)
			throw new java.lang.IllegalArgumentException("参数不能为空！");
		ClassLoader loader = cls.getClassLoader();
		// 获得类的全名，包括包名
		String clsName = cls.getName() + ".class";
		// 获得传入参数所在的包
		Package pack = cls.getPackage();
		String path = "";
		// 如果不是匿名包，将包名转化为路径
		if (pack != null) {
			String packName = pack.getName();
			// 此处简单判定是否是Java基础类库，防止用户传入JDK内置的类库
			if (packName.startsWith("java.") || packName.startsWith("javax."))
				throw new java.lang.IllegalArgumentException("不要传送系统类！");
			// 在类的名称中，去掉包名的部分，获得类的文件名
			clsName = clsName.substring(packName.length() + 1);
			// 判定包名是否是简单包名，如果是，则直接将包名转换为路径，
			if (packName.indexOf(".") < 0)
				path = packName + "/";
			else {// 否则按照包名的组成部分，将包名转换为路径
				int start = 0, end = 0;
				end = packName.indexOf(".");
				while (end != -1) {
					path = path + packName.substring(start, end) + "/";
					start = end + 1;
					end = packName.indexOf(".", start);
				}
				path = path + packName.substring(start) + "/";
			}
		}
		// 调用ClassLoader的getResource方法，传入包含路径信息的类文件名
		java.net.URL url = loader.getResource(path + clsName);
		// 从URL对象中获取路径信息
		String realPath = url.getPath();
		// 去掉路径信息中的协议名"file:"
		int pos = realPath.indexOf("file:");
		if (pos > -1)
			realPath = realPath.substring(pos + 5);
		// 去掉路径信息最后包含类文件信息的部分，得到类所在的路径
		pos = realPath.indexOf(path + clsName);
		realPath = realPath.substring(0, pos - 1);
		// 如果类文件被打包到JAR等文件中时，去掉对应的JAR等打包文件名
		if (realPath.endsWith("!"))
			realPath = realPath.substring(0, realPath.lastIndexOf("/"));
		/*------------------------------------------------------------ 
		 ClassLoader的getResource方法使用了utf-8对路径信息进行了编码，当路径 
		  中存在中文和空格时，他会对这些字符进行转换，这样，得到的往往不是我们想要 
		  的真实路径，在此，调用了URLDecoder的decode方法进行解码，以便得到原始的 
		  中文及空格路径 
		-------------------------------------------------------------*/
		try {
			realPath = java.net.URLDecoder.decode(realPath, "utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return realPath;
	}
	public static void main(String[] args) {
//		Map map=new HashMap<String, Object>();
//		SubjectEntity sub=new SubjectEntity();
//		sub.setSubjectName("于浩");
//		map.put("subject", sub);
//		List list=new ArrayList();
//		NoteEntity note1=	new NoteEntity();
//		note1.setTitle("新建文件夹1");
//		note1.setContent("YU242424444");
//		list.add(note1);
//		NoteEntity note2=	new NoteEntity();
//		note2.setTitle("新建文件夹2");
//		note2.setContent("YU242424444");
//		list.add(note2);
//		NoteEntity note3=	new NoteEntity();
//		note3.setTitle("新建文件夹3");
//		note3.setContent("YU242424444");
//		list.add(note3);
//		map.put("rootnotelist", list);
//		//List imgbaselist=new ArrayList();
//		//map.put("imgbaselist", imgbaselist);
//		try {
//		//String k=	SubjectToMht.subjectToMht("wordtempalte/word.ftl", map);
//		System.out.println(k);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
