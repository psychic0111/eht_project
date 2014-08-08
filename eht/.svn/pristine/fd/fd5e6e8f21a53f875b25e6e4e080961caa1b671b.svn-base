package com.eht.system.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 树结构数据的数据传输对象
 * 
 * @author chenlong
 * 
 */
public class TreeData {
	/** 父节点ID */
	private String pId;
	/** 节点ID */
	private String id;
	/** 节点名称 */
	private String name;
	
	/** 是否为父节点 */
	private String isParent;
	/** 是否展开节点 */
	private String open;
	/** 节点图标 */
	private String icon;
	/** 节点打开图标 */
	private String iconOpen;
	/** 节点关闭图标 */
	private String iconClose;
	/** 节点图标样式 */
	private String iconSkin;
	/** 节点url打开方式 _blank _self或其它 */
	private String target;
	/** 节点数据类型 */
	private String dataType;
	/** 节点数据所属分支标识 这里主要设为所属专题ID */
	private String branchId;
	
	private String subjectId;
	
	//子节点集合
	private List<TreeData> children;
	
	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TreeData> getChildren() {
		return children;
	}

	public void setChildren(List<TreeData> children) {
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static TreeData toTreeData(Object obj) {
		return (TreeData) obj;
	}

	public static List<TreeData> toTreeDataList(Object obj) {
		List list = (List) obj;
		List<TreeData> treeDataList = new ArrayList<TreeData>(list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			treeDataList.add((TreeData) object);
		}
		return treeDataList;
	}

	public String getIsParent() {
		return isParent;
	}

	public void setIsParent(String isParent) {
		this.isParent = isParent;
	}

	public String getOpen() {
		// 初始化只打开根节点
		if(this.pId != null && !"".equals(this.pId)){
			return open;
		}else{
			return "false";
		}
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIconOpen() {
		return iconOpen;
	}

	public void setIconOpen(String iconOpen) {
		this.iconOpen = iconOpen;
	}

	public String getIconClose() {
		return iconClose;
	}

	public void setIconClose(String iconClose) {
		this.iconClose = iconClose;
	}

	public String getIconSkin() {
		return iconSkin;
	}

	public void setIconSkin(String iconSkin) {
		this.iconSkin = iconSkin;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

}
