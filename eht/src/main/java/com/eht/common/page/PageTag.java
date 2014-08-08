package com.eht.common.page;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 分页标签
 * 
 * @author Chang Fei
 */
public class PageTag extends TagSupport {
	private static final long serialVersionUID = 1L;
	/** 是否显示数字分页 */
	private boolean showNumerPager = true;
	/** 是否显示文本分页 */
	private boolean showTextPager = true;
	/** 是否显示分页*/
	private boolean showSizeCombox = false;
	/** 数字列表页偏移量 */
	private int offset = 2;
	/** 内嵌样式 */
	private String pagerStyle = "";
	/** 当前页的内嵌样式 */
	private String curPageStyle = "";
	/** 类样式 */
	private String pagerTheme = "";
	/** 当前页样式 */
	private String curPagerTheme = "";
	/** 分页使用的html标签，默认使用A标签 */
	private String htmlTag = "a";
	/** 分页函数 */
	private String pagerFunction = "_pageFrmsubmit";

	public int doStartTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		PageResult pageResult = (PageResult) request.getAttribute("pageResult");
		StringBuilder sb = new StringBuilder();
		if (pageResult != null) {
			int pageCount = pageResult.getPageCount();
			int pageNo = pageResult.getPageNo();
			int pageSize = pageResult.getPageSize();
			int minNo = pageNo - offset;
			int maxNo = pageNo + offset;
			minNo = minNo < 1 ? 1 : minNo;
			maxNo = maxNo > pageCount ? pageCount : maxNo;
			if (pageCount == 0) {
				sb.append("<a>共0条记录</a>");
				sb.append("<input type='hidden' name='pageNo'  id='_pageNo' value='" + pageNo + "'/>");
				sb.append("<input type='hidden' name='pageSize' value='" + pageSize + "'/>");
			} else {
				if (pageCount > 1) {
					if (showTextPager) {
						if (pageNo == 1) {
							// 当前页为第一页
							if (pageCount == pageNo) {
								sb.append(createPager("下一页", (pageNo + 1),pageSize));
							}
						} else {
							sb.append(createPager("首页", 1,pageSize));
							sb.append(createPager("上一页", pageNo - 1,pageSize));
						}
					}
					if (showNumerPager) {
						for (int i = minNo; i <= maxNo; i++) {
							sb.append(createPager(i + "", i ,pageSize,i == pageNo));
						}
					}
					if (showTextPager) {
						if (pageNo != pageCount) {
							sb.append(createPager("下一页", pageNo + 1,pageSize));
							sb.append(createPager("尾页", pageCount,pageSize));
						}
						// sb.append("<a>共" + pageCount +
						// "页</a>&nbsp;&nbsp;");

					}
				}else{
					sb.append("<a>共1页</a>");
				}
				// sb.append("<a>当前第"+pageResult.getPageNo()+"页</a>&nbsp;&nbsp;");
				sb.append("<input type='hidden' name='pageNo'  id='_pageNo' value='" + pageNo + "'/>");
				sb.append("<input type='hidden' name='pageSize' value='" + pageSize + "'/>");
			}
			try {
				pageContext.getOut().write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return SKIP_BODY;
	}

	/**
	 * 创建分页tigger.
	 * 
	 * @param pagerText
	 * @param action
	 * @return
	 */
	private String createPager(String pagerText, int pageNo,int pageSize) {
		return createPager(pagerText, pageNo,pageSize, false);
	}

	/**
	 * 创建分页tigger.
	 * 
	 * @param pagerText
	 * @param action
	 * @param isCurrentPage
	 * @return
	 */
	private String createPager(String pagerText, int pageNo, int pageSize, boolean isCurrentPage) {
		StringBuilder pager = new StringBuilder();
		pager.append("<");
		pager.append(htmlTag);
		pager.append(' ').append("href='javascript:;'").append(' ').append("pageIndex='").append(pageNo).append("' pageSize='").append(pageSize).append('\'');
		if (pagerStyle.length() > 0) {
			pager.append(" style='" + pagerStyle + "'");
		}
		if (pagerTheme.length() > 0) {
			pager.append(" class='" + pagerTheme + "'");
		} else if (isCurrentPage && curPagerTheme.length() > 0) {
			pager.append(" class='" + curPagerTheme + "'");
		}
		pager.append(" onclick='" + pagerFunction + "(this," + pageNo + ","+pageSize+");return false;'");
		pager.append(" >");
		if (pagerText != null && pagerText.length() > 0) {
			pager.append(pagerText);
		}
		pager.append("</" + htmlTag + ">");

		return pager.toString();
	}

	public boolean isShowNumerPager() {
		return showNumerPager;
	}

	public void setShowNumerPager(boolean showNumerPager) {
		this.showNumerPager = showNumerPager;
	}

	public boolean isShowTextPager() {
		return showTextPager;
	}

	public void setShowTextPager(boolean showTextPager) {
		this.showTextPager = showTextPager;
	}
	
	public boolean isShowSizeCombox() {
		return showSizeCombox;
	}

	public void setShowSizeCombox(boolean showSizeCombox) {
		this.showSizeCombox = showSizeCombox;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getPagerStyle() {
		return pagerStyle;
	}

	public void setPagerStyle(String pageStyle) {
		this.pagerStyle = pageStyle;
	}

	public String getCurPageStyle() {
		return curPageStyle;
	}

	public void setCurPageStyle(String curPageStyle) {
		this.curPageStyle = curPageStyle;
	}

	public String getPagerTheme() {
		return pagerTheme;
	}

	public void setPagerTheme(String pagerTheme) {
		this.pagerTheme = pagerTheme;
	}

	public String getCurPagerTheme() {
		return curPagerTheme;
	}

	public void setCurPagerTheme(String curPagerTheme) {
		this.curPagerTheme = curPagerTheme;
	}

	public String getHtmlTag() {
		return htmlTag;
	}

	public void setHtmlTag(String htmlTag) {
		this.htmlTag = htmlTag;
	}

	public String getPagerFunction() {
		return pagerFunction;
	}

	public void setPagerFunction(String pagerFunction) {
		this.pagerFunction = pagerFunction;
	}
	
}
