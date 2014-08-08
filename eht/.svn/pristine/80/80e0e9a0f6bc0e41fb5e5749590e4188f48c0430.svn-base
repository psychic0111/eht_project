package com.eht.common.bean;

import java.io.Serializable;

import org.jeecgframework.core.util.JSONHelper;

import com.eht.common.enumeration.ResponseCode;
import com.eht.tag.entity.TagEntity;

/**
 * 操作状态与信息
 * @author chenlong
 *
 */
public class ResponseStatus {
	
	/**
	 * 操作结果信息
	 */
	private ResponseCode response;
	
	/**
	 * 同步时同时返回上传数据的后续同步数据（上传数据和后续数据是针对同一数据）
	 */
	private Object data;
	
	public ResponseStatus(){
		this.response = ResponseCode.NEXT;
	}
	
	public ResponseStatus(ResponseCode response){
		this.response = response;
	}
	
	public ResponseStatus(ResponseCode response, Object data){
		this.response = response;
		this.data = data;
	}
	
	public ResponseCode getResponse() {
		return response;
	}

	public void setResponse(ResponseCode response) {
		this.response = response;
	}

	public int getStatus() {
		return response.getCode();
	}

	public String getMessage() {
		return response.getMessage();
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString(){
		return JSONHelper.bean2json(this);
	}
	
	public static void main(String[] args){
		TagEntity tag = new TagEntity();
		tag.setId("123");
		ResponseStatus res = new ResponseStatus(ResponseCode.NEXT, tag);
		System.out.println(res.toString());
	}
}
