package com.eht.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jeecgframework.core.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import sun.misc.BASE64Encoder;

import com.eht.subject.entity.MhtImg;

public class HtmlParser {

	private final String CONTENT;

	private Parser parser;

	private Stack<TagNode> nodeStack;

	private int subLength;

	private int textLength = 0;

	private int pos = 0;

	private PrototypicalNodeFactory factory;

	public HtmlParser(String content) {
		CONTENT = content;

		parser = Parser.createParser(content, "UTF-8");

		factory = new PrototypicalNodeFactory();

		factory.registerTag(new StrongTag());

		parser.setNodeFactory(factory);

		nodeStack = new Stack<TagNode>();

	}

	private void recusive(NodeIterator iterator) throws ParserException {

		while (iterator.hasMoreNodes()) {

			Node node = iterator.nextNode();

			if (node instanceof TagNode) {
				TagNode tagNode = (TagNode) node;

				if (tagNode.getTagName().equalsIgnoreCase("img")) {
					continue;
				}
				Tag tag = tagNode.getEndTag();
				if (tag != null) {
					nodeStack.push(tagNode);
				}
			} else if (node instanceof TextNode) {

				if (node.getText().trim().length() == 0) {

					continue;
				}

				String nodeText = node.getText();

				int tLen = nodeText.length();

				if ((textLength < subLength)
						&& ((textLength + tLen) > subLength)) {

					pos = node.getStartPosition() + subLength - textLength;

					textLength = subLength;

					return;
				} else {

					textLength += tLen;

					pos = node.getEndPosition();

				}
			}
			if (node.getChildren() == null) {

				continue;
			}

			recusive(node.getChildren().elements());

			if (subLength <= textLength) {

				return;
			}
		}

	}

	public String subStrAsText(int length, String end) {
		try {
			String html = parser.parse(null).asString();
			if (length >= html.length() || length <= 0) {
				return html;
			}
			return html.substring(0, length - 1) + end;
		} catch (ParserException e) {
			e.printStackTrace();
			return "";
		}
	}

	public String subString(int length, String end) {
		if (length >= CONTENT.length() || length <= 0) {

			return CONTENT;

		}
		subLength = length;

		try {
			// NodeList list = parser.parse(new NotFilter(new
			// TagNameFilter("IMG")));
			recusive(parser.elements());
		} catch (ParserException e) {
			e.printStackTrace();
		}


		int size = nodeStack.size();

		StringBuffer buffer = new StringBuffer();

		buffer.append(CONTENT.substring(0, pos));

		while (size > 0) {
			TagNode node = nodeStack.pop();

			size--;
			if (node.getEndTag().getEndPosition() <= pos
					|| node.getTagBegin() >= pos) {

				continue;
			}
			buffer.append("</");
			buffer.append(node.getTagName());
			buffer.append(">");
		}
		buffer.append(end);
		return buffer.toString();

	}

	public String parseNoteContent(String attaId, String imgUrl)
			throws ParserException {
		NodeList list = parser.parse(null);
		replaceImageUrl(attaId, imgUrl, list);

		return list.toHtml();
	}

	public String parseNoteContentByFileName(String fileName, String imgUrl)
			throws ParserException {
		NodeList list = parser.parse(null);
		replaceImageUrlByFileName(fileName, imgUrl, list);

		return list.toHtml();
	}

	/**
	 * 替换图片src
	 * 
	 * @param attaId
	 * @param imgUrl
	 * @param list
	 */
	public void replaceImageUrl(String attaId, String imgUrl, NodeList list) {
		for (int i = 0; i < list.size(); i++) {
			Node node = list.elementAt(i);
			if (node instanceof TagNode
					&& ((TagNode) node).getTagName().equalsIgnoreCase("img")) {
				TagNode tag = (TagNode) node;
				String imgId = tag.getAttribute("imgId");
				if (!StringUtil.isEmpty(imgId) && imgId.equals(attaId)) {
					tag.setAttribute("src", imgUrl);
					break;
				}
			} else if (node.getChildren() != null) {
				replaceImageUrl(attaId, imgUrl, node.getChildren());
			}

		}
	}

	/**
	 * 根据图片名称替换图片src
	 * 
	 * @param attaId
	 * @param imgUrl
	 * @param list
	 */
	public void replaceImageUrlByFileName(String fileName, String imgUrl,
			NodeList list) {
		for (int i = 0; i < list.size(); i++) {
			Node node = list.elementAt(i);
			if (node instanceof TagNode
					&& ((TagNode) node).getTagName().equalsIgnoreCase("img")) {
				TagNode tag = (TagNode) node;
				String src = tag.getAttribute("src");
				String name = src.substring(src.lastIndexOf("/") + 1);
				if (!StringUtil.isEmpty(name) && name.equals(fileName)) {
					tag.setAttribute("src", imgUrl);
					break;
				}
			} else if (node.getChildren() != null) {
				replaceImageUrlByFileName(fileName, imgUrl, node.getChildren());
			}

		}
	}

	static public String repleceHtmlImg(String content, String replace, String replaceTo) {
		Document doc = Jsoup.parseBodyFragment(content);
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("src");
			url = url.replaceAll(replace, replaceTo);
			imgs.get(i).attr("r_src", url);
			imgs.get(i).attr("src", url);
		}
		return doc.html();
	}
	
	/**
	 * 把html文件中的img路径替换成编辑器中可显示路径
	 * @param content
	 * @param replace
	 * @param replaceTo
	 * @return
	 */
	static public String replaceHtmlImg(String content, String concat) {
		Document doc = Jsoup.parseBodyFragment(content);
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("src");
			url = concat + url;
			imgs.get(i).attr("src", url);
		}
		return doc.html();
	}
	
	/**
	 * 把html文件中的img路径替换成编辑器中可显示路径
	 * @param content
	 * @param replace
	 * @param replaceTo
	 * @return
	 * @throws IOException 
	 */
	static public String replaceHtmlImg(File htmlFile, String concat) throws IOException {
		Document doc = Jsoup.parse(htmlFile, "UTF-8");
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("src");
			url = concat + url;
			imgs.get(i).attr("src", url);
		}
		return doc.html();
	}
	
	/**
	 * 把客户端上传的html文件中的img路径替换成编辑器中可显示路径
	 * @param content
	 * @param replace
	 * @param replaceTo
	 * @return
	 */
	static public String replaceClientHtmlImg(String content, String concat) {
		Document doc = Jsoup.parseBodyFragment(content, "UTF-8");
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("r_src");
			url = concat + url;
			imgs.get(i).attr("src", url);
		}
		return doc.html();
	}
	
	/**
	 * 把客户端上传的html文件中的img路径替换成编辑器中可显示路径
	 * @param htmlFile
	 * @param replace
	 * @param replaceTo
	 * @return
	 * @throws IOException 
	 */
	static public String replaceClientHtmlImg(File htmlFile, String concat) throws IOException {
		Document doc = Jsoup.parse(htmlFile, "UTF-8");
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("r_src");
			url = concat + url;
			imgs.get(i).attr("src", url);
		}
		return doc.html();
	}
	
	static public String repleceHtmlImg(String content, List <MhtImg> list,String webpath) {
		Document doc = Jsoup.parseBodyFragment(content);
		Elements imgs = doc.select("img");
		for (int i = 0; i < imgs.size(); i++) {
			String url = imgs.get(i).attr("src");
			if(url==null||url.equals("")){
				continue;
			}
			if(url.startsWith("http://")){
				imgs.get(i).attr("src", "3D\""+url);
				
			}else{
				String base64=null;
				String path=url.replaceAll("../../", "");
				try {
					 base64= GetImageStr(webpath+path);
				} catch (IOException e) {
					continue;
				}
				String kzm=path.substring(path.lastIndexOf(".")+1);
				String uuid=UUIDGenerator.uuid()+"."+kzm;
				imgs.get(i).attr("src", "3D\"export.files/"+uuid);
				MhtImg mht=new MhtImg();
				mht.setUuid(uuid);
				mht.setBase64(base64);
				mht.setKzm(kzm);
				list.add(mht);
			}
		}
		Elements as = doc.select("a");
		for (int i = 0; i < as.size(); i++) {
			String url = as.get(i).attr("href");
			if(url.startsWith("http")){
				as.get(i).attr("href", "3D\""+url);
			}
		}
		return doc.select("body").get(0).html().replaceAll("\"3D&quot;", "3D\"");
	}
	
	public static String GetImageStr(String imgFile) throws IOException  
    {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理  
      
        InputStream in = null;  
        byte[] data = null;  
        //读取图片字节数组  
        try   
        {  
            in = new FileInputStream(imgFile);          
            data = new byte[in.available()];  
            in.read(data);  
            
        }   
        catch (IOException e)   
        {  
            throw new IOException();
        }finally{
        	if(in!=null){
        		in.close();
        	}
        } 
        //对字节数组Base64编码  
        BASE64Encoder encoder = new BASE64Encoder();  
        return encoder.encode(data);//返回Base64编码过的字节数组字符串  
    }  
	
	
	public static void main(String[] args) {

		String content ="<html> <head></head> <body s='11'>"+
				"123" +
				"<div>" +
				"	陈光标纽约街头送钱" +
				"<img title=\"Chrysanthemum.jpg\" src=\"../../notes/ebd4ea5593e048a280bb1bc1d789e2c6/e774aa5ae70e4ba994ae90afa9f17d94/files/img/74171407307085658.jpg\">"+
				"	<IMG imgId=\"img1\" src=\"71761403848376295.jpg\">" +
				"	<img  src=\"http://www.baidu.com/img/bdlogo.png\">" +
				"</div>" +
				"456"+" </body></html>";
		Document doc =Jsoup.parse(content);
		//Document doc = Jsoup.parseBodyFragment(content);
		Elements imgs = doc.select("img");
		for(int i=0;i<imgs.size();i++){
		String url=imgs.get(i).attr("src");
			
		System.out.println(url);
		imgs.get(i).attr("src", "3D\"http://www.baidu.com/img/bdlogo.png");
		}
		System.out.println(doc.select("body").get(0).html());
		System.out.println(doc.select("body").get(0).html().replaceAll("\"3D&quot;", "3D\""));
		//System.out.println(doc.text());

//String l="../../notes/ebd4ea5593e048a280bb1bc1d789e2c6/e774aa5ae70e4ba994ae90afa9f17d94/files/img/74171407307085658.jpg";

//System.out.println(l.lastIndexOf("."));
//;
//		HtmlParser hu = new HtmlParser(content);
//		String str;
//		try {
//			str = hu.parseNoteContent("img1", "/eht/aaa.jpg");
//			System.out.println(str);
//		} catch (ParserException e) {
//			e.printStackTrace();
//		}

	}
}

class StrongTag extends CompositeTag {

	private static final long serialVersionUID = 1L;

	private static final String[] mIds = new String[] { "STRONG" };

	private static final String[] mEndTagEnders = new String[] { "BODY", "HTML" };

	public String[] getIds() {

		return mIds;

	}

	public String[] getEndTagEnders()

	{
		return (mEndTagEnders);

	}
}