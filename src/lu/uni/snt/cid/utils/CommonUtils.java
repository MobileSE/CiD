package lu.uni.snt.cid.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.opencsv.CSVReader;

public class CommonUtils 
{
	
	public static List<String> deviceCheckers = new ArrayList<String>(Arrays.asList(
			"<android.os.Build: java.lang.String MANUFACTURER>",
	"<android.os.Build: java.lang.String PRODUCT>",
	"<android.os.Build: java.lang.String TAGS>",
	"<android.os.Build: java.lang.String MODEL>",
	"<android.os.Build: java.lang.String DEVICE>",
	"<android.os.Build: java.lang.String BRAND>",
	"<android.os.Build: java.lang.String FINGERPRINT>"));
	
	public static boolean isStringEmpty(String str)
	{
		boolean isEmpty = false;
		
		if (null == str || str.isEmpty())
		{
			isEmpty = true;
		}
		
		return isEmpty;
	}
	
	
	public static String getFileName(String path)
	{
		String fileName = path;
		if (fileName.contains("/"))
		{
			fileName = fileName.substring(fileName.lastIndexOf('/')+1);
		}
		
		return fileName;
	}
	
	public static void writeResultToFile(String path, String content)
	{
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
		    out.print(content);
		    out.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public static void appendResultToFile(String filePath, String content) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath, true));
			bw.write(content);
			bw.newLine();
			bw.write("===============================================================");
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
				bw.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
	}
	
	public static int totalValue(Map<String, Integer> map)
	{
		int total = 0;
		
		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			total += entry.getValue();
		}
		
		return total;
	}
	
	public static Set<String> loadFile(String filePath)
	{
		Set<String> lines = new HashSet<String>();
		boolean isField = false;
		if (filePath.contains("fields")) {
			isField = true;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				String trimLine = line;
				if (isField) {
					if (line.contains("=")) {
						int eqPos = line.indexOf("=");
						trimLine = line.substring(0, eqPos).trim() + ">";
					}
				}
				lines.add(trimLine);
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return lines;
	}
	
	public static Set<String> loadContentFromFile(String filePath)
	{
		Set<String> lines = new HashSet<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				String[] splits = line.split(">:<");
				String method = splits[0] + ">";
				lines.add(method);
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return lines;
	}
	
	public static List<String> loadFileToList(String filePath)
	{
		List<String> lines = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return lines;
	}
	
	public static List<String> loadFileToList(String filePath, String prefix)
	{
		if ("NULL".equals(prefix))
		{
			return loadFileToList(filePath);
		}
		
		List<String> lines = new ArrayList<String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			while ((line = br.readLine()) != null)
			{
				if (null != prefix && line.startsWith(prefix))
				{
					lines.add(line.replace(prefix, ""));
				}
			}
			
			br.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return lines;
	}
	
	public static Set<String> cloneSet(Set<String> src)
	{
		Set<String> dest = new HashSet<String>();
		
		for (String str : src)
		{
			dest.add(str);
		}
		
		return dest;
	}
	
	public static TreeMap<String,Integer> sort(Map<String, Integer> map)
	{
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		sorted_map.putAll(map);
		
		return sorted_map;
	}
	
	static class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	public static double computeSimilarity(int identical, int similar, int _new, int deleted)
	{
		int total1 = identical + similar + deleted;
		int total2 = identical + similar + _new; 
		
		int total = total1 < total2 ? total1 : total2;
		
		double rate = (double) identical / (double) total;
		
		return rate;
	}
	
	public static String getVariable(String varRef) {
		if (varRef.contains(":") && varRef.contains("<") &&
				varRef.contains(">") && !varRef.contains("(") && !varRef.contains(")")) {
			int startBracket = varRef.indexOf("<");
			int endBracket = varRef.lastIndexOf(">");
			if (startBracket < endBracket) {
				return varRef.substring(startBracket, endBracket + 1);
			}
		}
		return "";
	}
	
	public static <T> void put(Map<String, Set<T>> map1, String key, T value)
	{
		if (map1.containsKey(key))
		{
			Set<T> values = map1.get(key);
			values.add(value);
			map1.put(key, values);
		}
		else
		{
			Set<T> values = new HashSet<T>();
			values.add(value);
			map1.put(key, values);
		}
	}
	
	public static void put(Map<String, Set<String>> dest, Map<String, Set<String>> src)
	{
		for (Map.Entry<String, Set<String>> entry : src.entrySet())
		{
			String cls = entry.getKey();
			Set<String> set2 = entry.getValue();
			
			if (dest.containsKey(cls))
			{
				Set<String> set1 = dest.get(cls);
				set1.addAll(set2);
				dest.put(cls, set1);
			}
			else
			{
				Set<String> set1 = new HashSet<String>();
				set1.addAll(set2);
				dest.put(cls, set1);
			}
		}
	}

	public static void devicePut(Map<String, Set<String[]>> dest, Map<String, Set<String[]>> src)
	{
		for (Map.Entry<String, Set<String[]>> entry : src.entrySet())
		{
			String methodField = entry.getKey();
			Set<String[]> set2 = entry.getValue();
			
			if (dest.containsKey(methodField))
			{
				Set<String[]> set1 = dest.get(methodField);
				set1.addAll(set2);
				dest.put(methodField, set1);
			}
			else
			{
				Set<String[]> set1 = new HashSet<String[]>();
				set1.addAll(set2);
				dest.put(methodField, set1);
			}
		}
	}

	public static String getClassName(String methodOrField) {
		int semicolonPos = methodOrField.indexOf(":");
		return methodOrField.substring(1, semicolonPos);
	}
	
	public static String clsNameReplace(String origin, String clsName) {
		int semicolonPos = origin.indexOf(":");
		return "<" + clsName + origin.substring(semicolonPos);
	}
	
	public static boolean isDeviceSpecific(String[] record) {
		boolean isSpecific = false;
		for (String val : record) {
			if (val.equals("0")) {
				isSpecific = true;
				break;
			}
		}
		return isSpecific;
	}

	public static List<Map<String, Set<String[]>>> csvDeviceReader(String csvPath) {
		List<Map<String, Set<String[]>>> deviceMethodField = new ArrayList<Map<String, Set<String[]>>>();
		Map<String, Set<String[]>> deviceMethods = new HashMap<String, Set<String[]>>();
		Map<String, Set<String[]>> deviceFields = new HashMap<String, Set<String[]>>();
		int idx = 0;
		try {
			try (CSVReader csvReader = new CSVReader(new FileReader(csvPath));) {
			    String[] values = null;
			    while ((values = csvReader.readNext()) != null) {
			    	if (idx == 0) {
			    		Set<String[]> lines = new HashSet<String[]>();
		    			lines.add(values);
			    		deviceMethods.put("headers", lines);
			    		deviceFields.put("headers", lines);
			    		idx += 1;
			    	} else {
			    		idx += 1;
			    	}
			    	if (isDeviceSpecific(values)) {
			    		String mf = values[1];
			    		if (mf.contains("(") && mf.contains(")")) {
				    		if (deviceMethods.containsKey(mf)) {
				    			deviceMethods.get(mf).add(values);
				    		} else {
				    			Set<String[]> lines = new HashSet<String[]>();
				    			lines.add(values);
				    			deviceMethods.put(mf, lines);
				    		}
			    		} else {
				    		if (deviceFields.containsKey(mf)) {
				    			deviceFields.get(mf).add(values);
				    		} else {
				    			Set<String[]> lines = new HashSet<String[]>();
				    			lines.add(values);
				    			deviceFields.put(mf, lines);
				    		}
			    		}
			    	}
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		deviceMethodField.add(deviceMethods);
		deviceMethodField.add(deviceFields);
				
		return deviceMethodField;
	}

	public static boolean containDeviceCheck(String content) {
		boolean containCheck = false;
		for (String checker : deviceCheckers) {
			if (content.contains(checker)) {
				containCheck = true;
				break;
			}
		}
		
		return containCheck;
	}
}
