package com.eht.common.page;

import java.util.List;

/**
 * 数据分页模型
 * @author Chang Fei
 *
 */
public class PageResult {
	/*** 总的记录 */
	private Long total;
	/*** 数据*/
	private List<?> rows;
	/**当前页*/
    private int pageNo=1;
    /**每页的记录数*/
    private int pageSize=20;
    /** 分页的URL*/
    private String url;
    /**总页数*/
    private int pageCount;
    
    public PageResult(){}
    
    public PageResult(Long total,List<?> list){
		this.total= total;
		this.rows = list;
	}
    
    /**数据总数*/
	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<?> getRows() {
		return rows;
	}

	public  void setRows(List<?> rows) {
		this.rows = rows;
	}
	
	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageCount() {
		pageCount = (int) (total/pageSize);
		int mod = (int) (total%pageSize);
		if(mod>0){
			pageCount = pageCount+1;
		}
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
