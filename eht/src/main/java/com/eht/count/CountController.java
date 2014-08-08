package com.eht.count;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.model.json.Highchart;
import org.jeecgframework.web.system.service.SystemService;

import com.eht.user.service.AccountServiceI;

/**   
 * @Title: Controller
 * @Description: 内容统计
 * @author yuhao
 * @date 2014-03-18 11:47:52
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/countController")
public class CountController extends BaseController {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CountController.class);
	
	

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
	 * 用户信息列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "count")
	public ModelAndView account(HttpServletRequest request) {
		return new ModelAndView("/com/eht/count/countList");
	}

	/**
	 * 报表数据生成
	 * 
	 * @return
	 */
	@RequestMapping(params = "report")
	@ResponseBody
	public List<Highchart> report(HttpServletRequest request,String reportType, HttpServletResponse response) {
		List lt = new ArrayList();
		List<Highchart> list = new ArrayList<Highchart>();
		Highchart hc = new Highchart();
		hc = new Highchart();
		hc.setName("内容统计分析");
		hc.setType(reportType);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date start=null;
		Date end=null;
		try {
			 start=format.parse(request.getParameter("start"));
			 end=format.parse(request.getParameter("end"));
		} catch (ParseException e) {
		
		}
		long subjectcount=(Long) systemService.findHql("select count(*) from SubjectEntity s where s.createTime>=? and s.createTime<=? ",new Object []{start,end}).get(0);
		long directorycount=(Long) systemService.findHql("select count(*) from DirectoryEntity d where d.createTime>=? and d.createTime<=? ",new Object []{start,end}).get(0);
		long notecount=(Long) systemService.findHql("select count(*) from NoteEntity n where n.createTime>=? and n.createTime<=? ",new Object []{start,end}).get(0);
		long commentcount=(Long) systemService.findHql("select count(*) from CommentEntity c where c.createTime>=? and c.createTime<=? ",new Object []{start,end}).get(0);
		long count=subjectcount+directorycount+notecount+commentcount;//总数
		
		Map<String, Object> mapsubject= new HashMap<String, Object>();
		mapsubject.put("name", "专题数量");
		mapsubject.put("y", subjectcount);
		Double  percentage = 0.0;
		if (count != 0) {
			percentage = new Double(subjectcount)/count;
		}
		mapsubject.put("percentage", percentage*100);
		
		Map<String, Object> mapdirectory= new HashMap<String, Object>();
		mapdirectory.put("name", "目录数量");
		mapdirectory.put("y", directorycount);
		percentage = 0.0;
		if (count != 0) {
			percentage = new Double(directorycount)/count;
		}
		mapdirectory.put("percentage", percentage*100);
		
		Map<String, Object> mapnote= new HashMap<String, Object>();
		mapnote.put("name", "条目数量");
		mapnote.put("y", notecount);
		percentage = 0.0;
		if (count != 0) {
			percentage = new Double(notecount)/count;
		}
		mapnote.put("percentage", percentage*100);
		
		
		Map<String, Object> mapcomment= new HashMap<String, Object>();
		mapcomment.put("name", "专题数量");
		mapcomment.put("y", commentcount);
		percentage = 0.0;
		if (count != 0) {
			percentage = new Double(commentcount)/count;
		}
		mapcomment.put("percentage", percentage*100);
		
		lt.add(mapcomment);
		lt.add(mapnote);
		lt.add(mapdirectory);
        lt.add(mapsubject);
		hc.setData(lt);
		list.add(hc);
		return list;
	}
	
	/**
	 * 报表打印
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(params = "export")
	public void export(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		String type = request.getParameter("type");
		String svg = request.getParameter("svg");
		String filename = request.getParameter("filename");

		filename = filename == null ? "chart" : filename;
		ServletOutputStream out = response.getOutputStream();
		try {
			if (null != type && null != svg) {
				svg = svg.replaceAll(":rect", "rect");
				String ext = "";
				Transcoder t = null;
				if (type.equals("image/png")) {
					ext = "png";
					t = new PNGTranscoder();
				} else if (type.equals("image/jpeg")) {
					ext = "jpg";
					t = new JPEGTranscoder();
				} else if (type.equals("application/pdf")) {
					ext = "pdf";
					t = (Transcoder) new PDFTranscoder();
				} else if (type.equals("image/svg+xml"))
					ext = "svg";
				response.addHeader("Content-Disposition",
						"attachment; filename=" + new String(filename.getBytes("GBK"),"ISO-8859-1") + "." + ext);
				response.addHeader("Content-Type", type);

				if (null != t) {
					TranscoderInput input = new TranscoderInput(
							new StringReader(svg));
					TranscoderOutput output = new TranscoderOutput(out);

					try {
						t.transcode(input, output);
					} catch (TranscoderException e) {
						out
								.print("Problem transcoding stream. See the web logs for more details.");
						e.printStackTrace();
					}
				} else if (ext.equals("svg")) {
					// out.print(svg);
					OutputStreamWriter writer = new OutputStreamWriter(out,
							"UTF-8");
					writer.append(svg);
					writer.close();
				} else
					out.print("Invalid type: " + type);
			} else {
				response.addHeader("Content-Type", "text/html");
				out
						.println("Usage:\n\tParameter [svg]: The DOM Element to be converted."
								+ "\n\tParameter [type]: The destination MIME type for the elment to be transcoded.");
			}
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
	
}
