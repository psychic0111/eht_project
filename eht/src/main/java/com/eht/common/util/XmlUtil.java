package com.eht.common.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlUtil {
	public static boolean readStringXml(String xml) {
        try {
        	DocumentHelper.parseText(xml); // 将字符串转为XML
        } catch (Exception e) {
        	return false; 
        }
        return true;
    }
	
	public static Document readXmlFile(String path) {
		SAXReader reader = new SAXReader();
        Document document = null;
        try {
            File file = new File(path);
            document = reader.read(file);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return document; 
    }
	
	public static Element getElementByAttr(Element Element, String nodeName, String attributeName, String attributeValue) {
        Iterator<?> it = Element.elementIterator(nodeName);
        while(it.hasNext()){
        	Element ele = (Element) it.next();
        	String id = ele.attributeValue(attributeName);
        	if(id.equals(attributeValue)){
        		return ele;
        	}
        }
        return null; 
    }
	
	public static Element getUniqueElement(Element Element, String nodeName) {
        Iterator<?> it = Element.elementIterator(nodeName);
        return (org.dom4j.Element) it.next(); 
    }
	
	public static List<?> getElementsByNodeName(Element Element, String nodeName) {
        List<?> list = Element.elements(nodeName);
        return list; 
    }
	
	public static void main(String[] args){
		String str = "<p style=\"text-align:center;margin-top:0px;margin-bottom:0px;\" class=\"p0\"><strong><span style=\"font-family:&#39;宋体&#39;,&#39;simsun&#39;;font-size:12px\">芜湖市大学生创业园管理办法</span></strong></p>";
		System.out.println(str.replaceAll("<[a-zA-Z]+(\\s+[a-zA-Z]+\\s*=\\s*(\"([^\"]*)\"|'([^']*)'))*\\s*>", "").replaceAll("[<]{1}[/]{1}[a-zA-Z]+[>]{1}", ""));
	}

}
