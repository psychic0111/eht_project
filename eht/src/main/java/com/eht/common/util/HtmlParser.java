package com.eht.common.util;

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

	static  public String repleceHtmlImg(String content,String replece){
	Document doc = Jsoup.parseBodyFragment(content);
	Elements imgs = doc.select("img");
	for(int i=0;i<imgs.size();i++){
	String url=imgs.get(i).attr("src");
	url=url.replaceAll(replece, "");
	imgs.get(i).attr("src", url);
	}
		return doc.html();
	}
	
	public static void main(String[] args) {

		String content =
				"123" +
				"<div>" +
				"	陈光标纽约街头送钱" +
				"	<img imgId=\"img1\" src=\"/71761403848376295.jpg\">" +
				"	<img imgId=\"img1\" src=\"/71761403848376295.jpg\">" +
				"</div>" +
				"456";
		
		Document doc = Jsoup.parseBodyFragment(content);
		Elements imgs = doc.select("img");
		for(int i=0;i<imgs.size();i++){
		String url=imgs.get(i).attr("src");
		imgs.get(i).attr("src", "11");
		}
		System.out.println(doc.html());
		//System.out.println(doc.text());


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