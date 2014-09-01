package com.eht.common.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import com.eht.common.constant.Constants;
import com.eht.subject.entity.DirectoryEntity;
import com.eht.system.bean.TreeData;

public class TreeUtils {
	
	/**
	 * 把对象列表转换成TreeData列表并且将每个TreeData的parent以及children设置上，返回的就是一个真正的树列表
	 * @param dictList 原始对象列表 
	 * @param idField 原始对象中将要转换成TreeData对象的id 属性的属性名
	 * @param textField 原始对象中将要转换TreeData对像的 text 属性的属性名
	 * @param parentField 原始对象中将要转换TreeData对像的 parentId 属性的属性名
	 * @return TreeData 对象列表
	 */
	@SuppressWarnings("rawtypes")
	public static List<TreeData> getTreeDataList(List dictList, String idField, String textField, String parentField){
		List<TreeData> treeDataList = null;
		if(dictList!=null &&!dictList.isEmpty()){
			treeDataList = transformObjectList2TreeDataList(dictList, idField, textField, parentField, null);
			// 构造所有树节点的Map
			Map<String,TreeData> allTreeDataMap = getTreeDataMap(treeDataList);
			// 孩子找父亲
			claimTreeDataChildren(treeDataList,allTreeDataMap);
			// 挑出祖宗
			keepTreeDataGrandfather(treeDataList);
		}
		return treeDataList;
	}

	public static List<TreeData> buildTreeData(List<TreeData> treeDataList){
		if(treeDataList!=null && !treeDataList.isEmpty()){
			// 构造所有树节点的Map
			Map<String,TreeData> allTreeDataMap = getTreeDataMap(treeDataList);
			// 孩子找父亲
			claimTreeDataChildren(treeDataList,allTreeDataMap);
			// 挑出祖宗
			keepTreeDataGrandfather(treeDataList);
		}
		return treeDataList;
	}
	
    //拿到当前节点到更节点的所有节点
	public static List<TreeData> getTreePath(List<TreeData> treeDataList,String noteid){
		List<TreeData> rtList = new ArrayList<TreeData>();
		if(treeDataList!=null && !treeDataList.isEmpty()){
			// 构造所有树节点的Map
			Map<String,TreeData> allTreeDataMap = getTreeDataMap(treeDataList);
			// 孩子找父亲
			claimTreeDataChildren(treeDataList,allTreeDataMap);
			if(treeDataList!=null && !treeDataList.isEmpty()&&allTreeDataMap!=null && !allTreeDataMap.isEmpty()){ 
				TreeData currentNode = allTreeDataMap.get(noteid);
				do{
					rtList.add(currentNode);
					if(currentNode!=null){
						currentNode = allTreeDataMap.get(currentNode.getpId());
					}
				}while(currentNode!=null);
			} 
		}
		return rtList;
	}
	
	/**
	 * 从原始对象映射转换成TreeData对象列表
	 * @param objectList
	 * @param idPro
	 * @param textPro
	 * @param parentField
	 * @param otherMap 其它自定义字段赋值
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<TreeData> transformObjectList2TreeDataList(List objectList,String idPro, String textPro, String parentField, Map<String, String> otherMap) {
		List<TreeData> treeDataList = new ArrayList<TreeData>(objectList.size());
		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			TreeData treeData = new TreeData();
			Object object = (Object) iterator.next();
			Object idValueObj = ReflectionUtils.getFieldValue(object, idPro);
			String idValue = idValueObj==null? null : idValueObj.toString();
			Object textValueObj = ReflectionUtils.getFieldValue(object, textPro);
			String textValue = textValueObj==null? null : textValueObj.toString();
			Object parentValueObj = ReflectionUtils.invokeGetterMethod(object, parentField);
			String parentValue = parentValueObj==null? null : parentValueObj.toString(); 
			
			if(idValue!=null && textValue!=null) {//原对象对应的 id 和 text 对应的属性的值不为空才是有效的值
				ReflectionUtils.setFieldValue(treeData, "id", idValue+"");
				ReflectionUtils.setFieldValue(treeData, "name", textValue);
			}
			if(StringUtils.isNotBlank(parentValue)) {//只有原对象对应的parentId
				ReflectionUtils.setFieldValue(treeData, "pId", parentValue+"");
			}
			
			Object subjectIdObj = null;
			try {
				subjectIdObj = ReflectionUtils.getFieldValue(object, "subjectId");
			} catch (Exception e) {
			}
			Object deletedObj = null;
			try {
				deletedObj = ReflectionUtils.getFieldValue(object, "deleted");
			} catch (Exception e) {
			}
			
			String subjectId = subjectIdObj == null ? null : subjectIdObj.toString();
			treeData.setSubjectId(subjectId);
			
			int deleted = deletedObj == null ? Constants.DATA_NOT_DELETED : Integer.parseInt(deletedObj.toString());
			if(deleted == Constants.DATA_DELETED){
				treeData.setId(treeData.getId() + "_deleted");
			}
			
			if(otherMap != null){
				Set<Entry<String, String>> set = otherMap.entrySet();
				Iterator<Entry<String, String>> it = set.iterator();
				while(it.hasNext()){
					Entry<String, String> entry = it.next();
					ReflectionUtils.setFieldValue(treeData, entry.getKey(), entry.getValue());
				}
			}
			treeDataList.add(treeData);
		}
		
		return treeDataList;
	}
	
	private static Map<String, TreeData> getTreeDataMap(List<TreeData> treeDataList){
		Map<String, TreeData> treeDataMap = new HashMap<String, TreeData>();
		for(TreeData treeData : treeDataList){
			if(StringUtils.isNotBlank(treeData.getId()) && StringUtils.isNotBlank(treeData.getName())){
				treeDataMap.put(treeData.getId(), treeData);
			}
		}
		return treeDataMap;
	}
	
	/**
	 * 以TreeData模型，孩子找父亲
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void claimTreeDataChildren (List<TreeData> treeDataList,Map<String,TreeData> allTreeDataMap){
		if(treeDataList!=null && !treeDataList.isEmpty()){
			int size = treeDataList.size();
			for(int i=size-1;i>=0;i--){
				TreeData tree = treeDataList.get(i);
				if(hasTreeDataParent(tree)){
					TreeData parentTreeData = allTreeDataMap.get(tree.getpId());
					if(parentTreeData!=null){
						List children = parentTreeData.getChildren();
						if(children==null){
							children = new ArrayList<TreeData>();
							parentTreeData.setChildren(children);
						}
						children.add(tree);
					} else {//如果没有找到父亲节点就把parentId置成 null
						tree.setpId(null);
					}
				}
			}
		}
	}
	
	/**
	 * 以TreeData模型，挑出祖宗
	 * @param treeDataList
	 * @return
	 */
	private static void keepTreeDataGrandfather(List<TreeData> treeDataList){
		if(treeDataList!=null && !treeDataList.isEmpty()){
			int size = treeDataList.size();
			for (int i=size-1;i>=0;i--) {
				TreeData treeData = treeDataList.get(i);
				if(hasTreeDataParent(treeData)){
					treeDataList.remove(i);
				}else{
					treeData.setpId(null);
				}
			}
		}
	}

	public static boolean hasTreeDataParent(TreeData treeData){
		if(treeData.getpId() != null && !treeData.getpId().equals("") && !treeData.getpId().equals("0")){
			return true;
		}
		return false;
	}
	/**
	 * 获得树的低级节点列表，为了支持分级加载（IE6下数据过多展现速度慢），将每个顶级节点的孩子数据都
	 * 设置为null
	 * @param treeDataList
	 * @return
	 */
	public static List<TreeData> getTopLevel(List<TreeData> srcTreeDataList) {
		List<TreeData> destTreeDataList = new ArrayList<TreeData>(srcTreeDataList.size());
		for (Iterator<TreeData> iterator = srcTreeDataList.iterator(); iterator.hasNext();) {
			TreeData treeData = (TreeData) iterator.next();
			List<TreeData> children = treeData.getChildren();
			try {
				TreeData treeDataTemp = (TreeData)BeanUtils.cloneBean(treeData);
				//如果本身有孩子节点的话就给一个空的list保证UI组件展示的时候是一个“文件夹图标”
				treeDataTemp.setChildren(children==null? null:new ArrayList<TreeData>());
				destTreeDataList.add(treeDataTemp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return destTreeDataList;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		
		List<TreeData> srcTreeDataList = new ArrayList<TreeData>(); 
		TreeData e2 = new TreeData();
		e2.setId("2");
		srcTreeDataList.add(e2);
		TreeData[] array = srcTreeDataList.toArray(new TreeData[srcTreeDataList.size()]);
		TreeData[] clone = new TreeData[array.length];
		System.arraycopy(array, 0, clone, 0, array.length);
		System.out.println("clone length "+clone.length+clone[0]);
		
		List<TreeData> destTreeDataList = new ArrayList<TreeData>(Arrays.asList(clone));
//		destTreeDataList.add(e1);
		System.out.println("src:size"+srcTreeDataList);
		System.out.println("dest:size"+destTreeDataList);
		
		System.out.println("dest:size"+destTreeDataList);
		System.out.println("src:size"+srcTreeDataList);
//		destTreeDataList.remove(0);
		TreeData treeData = destTreeDataList.get(0);
		treeData.setId(1+"");
		System.out.println("dest:size"+destTreeDataList.get(0).getId());
		System.out.println("src:size"+srcTreeDataList.get(0).getId());
		
		TreeData cloneBean = (TreeData)BeanUtils.cloneBean(e2);
		cloneBean.setId("3");
		System.out.println(e2.getId());
		System.out.println(cloneBean.getId());
		
		destTreeDataList = (List<TreeData>)BeanUtils.cloneBean(srcTreeDataList);
		System.out.println("dest:size"+destTreeDataList);
		System.out.println("src:size"+srcTreeDataList);
		TreeData td2 = destTreeDataList.get(0);
		td2.setId("x");
		System.out.println("dest:size"+destTreeDataList);
		System.out.println("src:size"+srcTreeDataList);
		
		
		
	}
	/**
	 * 获得某个顶级分支的孩子节点数据
	 * @param treeDataList
	 * @param searchId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<TreeData> getBranchChildren(List<TreeData> treeDataList, String searchId) {
		List<TreeData> childrenTreeDataList = new ArrayList<TreeData>();
		if(StringUtils.isNotBlank(searchId))
			for (Iterator iterator = treeDataList.iterator(); iterator.hasNext();) {
				TreeData treeData = (TreeData) iterator.next();
				String id = treeData.getId();//以及是TreeData对象认为他的id是非 blank的。
				if(id.equals(searchId)) {
					childrenTreeDataList = treeData.getChildren();
				}
			}
		return childrenTreeDataList;
	}
}
