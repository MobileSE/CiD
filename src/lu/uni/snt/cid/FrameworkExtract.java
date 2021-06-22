package lu.uni.snt.cid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.cid.utils.CommonUtils;

public class FrameworkExtract {
	public Map<String, Set<String>> class2SuperClasses = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> class2Methods = new HashMap<String, Set<String>>();
	
	public Map<String, Set<String>> field2Class = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> field2SuperClasses = new HashMap<String, Set<String>>();
	
	public void load(String androidAPIPath)
	{
		if (androidAPIPath.contains("field")) {
			parseTxtFile4Field(androidAPIPath);
		} else {
			parseTxtFile4Method(androidAPIPath);
		}
	}
	
	public void parseTxtFile4Method(String txtFilePath)
	{
		List<String> lines = CommonUtils.loadFileToList(txtFilePath);
		
		for (String line : lines) {
			String[] splits = line.split(">:<");
			String method = splits[0] + ">";
			String clsSupers = splits[2].substring(0, splits[2].length() - 1);
			String[] clsSuperSplits = clsSupers.split(":");
			String clsName = clsSuperSplits[0];
			if (clsSupers.contains(":")) {
				String[] superNames = clsSuperSplits[1].split(",");
				for (String superName : superNames) {
					CommonUtils.put(class2SuperClasses, clsName, superName);
				}
			}
			CommonUtils.put(class2Methods, clsName, method);
		}
	}
	
	public void parseTxtFile4Field(String txtFilePath) {
		List<String> lines = CommonUtils.loadFileToList(txtFilePath);
		
		for (String line : lines) {
			String[] splits = line.split(">:<");
			String field = splits[0] + ">";
			String clsSupers = splits[3].substring(0, splits[3].length() - 1);
			String[] clsSuperSplits = clsSupers.split(":");
			String clsName = clsSuperSplits[0];
			if (clsSupers.contains(":")) {
				String[] superNames = clsSuperSplits[1].split(",");
				for (String superName : superNames) {
					CommonUtils.put(field2SuperClasses, field, superName);
				}
			}
			CommonUtils.put(field2Class, clsName, field);
		}
	}
}
