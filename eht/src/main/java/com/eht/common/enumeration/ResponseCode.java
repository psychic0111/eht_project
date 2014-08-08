package com.eht.common.enumeration;


public enum ResponseCode {
	
	NEXT(200){
		@Override
		public String getMessage() {
			return "数据处理完成，客户端上传下一条记录!";
		}
	},
	
	SUCCESS(300){
		@Override
		public String getMessage() {
			return "数据处理完成，同步结束!";
		}
	},
	
	PART_SYNCH_FINISHED(301){
		@Override
		public String getMessage() {
			return "此阶段数据同步已完成!";
		}
	},
	
	ADD(201){
		@Override
		public String getMessage() {
			return "数据处理完成，客户端需下载该数据更新!";
		}
	},
	
	UPDATE(202){
		@Override
		public String getMessage() {
			return "数据处理完成，客户端需下载该数据更新!";
		}
	},
	
	DELETE(203){
		@Override
		public String getMessage() {
			return "数据处理完成，该数据已被删除!";
		}
	},
	
	DOWNLOAD(204){
		@Override
		public String getMessage() {
			return "数据处理完成，该数据已被删除!";
		}
	},
	
	REQUEST(205){
		@Override
		public String getMessage() {
			return "数据处理完成，该数据已被删除!";
		}
	},
	
	NOT_COMPLETED(299){
		@Override
		public String getMessage() {
			return "接受和处理、但处理未完成!";
		}
	},
	SERVER_ERROR(500){
		@Override
		public String getMessage() {
			return "服务器处理发生异常!";
		}
	}, 
	AUTHORIZE_FAILED(403){
		@Override
		public String getMessage() {
			return "用户验证失败!";
		}
	}, 
	DATA_NOT_FOUND(404){
		@Override
		public String getMessage() {
			return "数据未找到!";
		}
	};
	
	private int code;
	
	private ResponseCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
        return code;
    }
	
	public String getMessage(){
		return "同步操作完成！";
	}

}
