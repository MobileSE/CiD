package lu.uni.snt.cid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import lu.uni.snt.cid.utils.CommonUtils;

public class FrameworkBase 
{
	public Map<String, Set<String>> class2SuperClasses = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> class2Methods = new HashMap<String, Set<String>>();
	public static Set<String> javaLangClasses = new HashSet<String>();
	
	public Map<String, Set<String>> class2Fields = new HashMap<String, Set<String>>();

	private String lifetimeAPIPath = "apis/android/android-java-lang-classes.txt";
	private String[] possibleGenerics = new String[] {"A", "B", "D", "E", "F", "K", "S", "T", "U", "X", "V", "W"};
	
	public void load(String androidAPIPath)
	{
		javaLangClassLoad();
		if (androidAPIPath.endsWith(".txt"))
		{
			parseTxtFile(androidAPIPath);
		}
		else if (androidAPIPath.endsWith(".xml"))
		{
			parseXmlFile(androidAPIPath);
		}
	}

	public void javaLangClassLoad() {
		List<String> lines = CommonUtils.loadFileToList(lifetimeAPIPath);
		for (String cls: lines) {
			javaLangClasses.add(cls);
		}
	}
	
	public void parseTxtFile(String txtFilePath)
	{
		List<String> lines = CommonUtils.loadFileToList(txtFilePath);
		
		String currentPkgName = "";
		String currentClsName = "";
		
		Map<String, String> genericTypes = new HashMap<String, String>();
		
		String classGenericPattern = ".* class [a-zA-Z]\\w*(<.*?>)";
		String methodGenericPattern = ".*(<.*?>).*";
		Pattern clsGenericPattern = Pattern.compile(classGenericPattern);
		Pattern mGenericPattern = Pattern.compile(methodGenericPattern);
		
		String genericLine = "";
		
		for (String line : lines)
		{
			line = line.trim();
			genericLine = line;
			
			line = removeGenericType(line);
			
			if (line.startsWith("package"))
			{
				currentPkgName = line;
				currentPkgName = currentPkgName.replace("package", "").replace("{", "").trim();
			}
			else if (line.contains(" class "))
			{
				if (!genericTypes.isEmpty()) {
					genericTypes.clear();
				}
				Matcher clsM = clsGenericPattern.matcher(genericLine);
				if (clsM.find()) {
					String genericMatch = clsM.group(1);
					String genericDec = clsM.group(1).substring(1, genericMatch.length() - 1);
					if (genericDec.contains("extends")) {
						String[] decSplits = genericDec.split("extends");
						String[] generics = decSplits[0].split(",");
						for (String gt : generics) {
							genericTypes.put(gt.trim(), decSplits[1].trim());
						}
					} else {
						genericTypes.put(genericDec.trim(), "java.lang.Object");
					}
				}
				line = line.replaceAll(".*class ", "");
				line = currentPkgName + "." + line;
				line = line.replace("{", "").trim();
				Set<String> superClses = new HashSet<String>();
				
				if (line.contains("implements"))
				{
					String[] strs = line.split("implements");
					line = strs[0].trim();
					
					String[] interfaces = strs[1].trim().split(" ");
					for (String interf : interfaces)
					{
						superClses.add(interf);
					}
				}
				
				if (line.contains("extends"))
				{
					String[] strs = line.split("extends");
					line = strs[0].trim();
					
					superClses.add(strs[1].trim());
				}
				
				currentClsName = line;
				
				if (!currentClsName.contains(".test."))
					class2SuperClasses.put(currentClsName, superClses);
			}
			else if (line.contains(" interface "))
			{
				line = line.replaceAll(".*interface ", "");
				line = currentPkgName + "." + line;
				line = line.replace("{", "").trim();
				
				Set<String> superClses = new HashSet<String>();

				if (line.contains("implements"))
				{
					String[] strs = line.split("implements");
					line = strs[0].trim();
					
					String[] interfaces = strs[1].trim().split(" ");
					for (String interf : interfaces)
					{
						superClses.add(interf);
					}
				}
				
				if (line.contains("extends"))
				{
					String[] strs = line.split("extends");
					line = strs[0].trim();
					
					superClses.add(strs[1].trim());
				}
				
				currentClsName = line;
				
				if (!currentClsName.contains(".test."))
					class2SuperClasses.put(currentClsName, superClses);
			}
			else if (line.startsWith("ctor") || line.startsWith("method"))
			{
				StringBuilder sb = new StringBuilder();
				StringBuilder sbparams = new StringBuilder();
				
				Matcher mm = mGenericPattern.matcher(genericLine);
				if (mm.find()) {
					String genericMatch = mm.group(1);
					String genericDec = genericMatch.substring(1, genericMatch.length() - 1);
					if (genericDec.contains("extends")) {
						String[] decSplits = genericDec.split("extends");
						String[] gts = decSplits[0].split(",");
						for(String gt : gts) {
							String genDec = gt.trim();
							if (genDec.equals("?")) {
								genDec = ("\\" + genDec);
							}
							String genDecPattern = "\\b" + genDec + "\\b";
							line = line.replaceAll(genDecPattern, decSplits[1].trim());
						}
					}
				}
				if (!genericTypes.isEmpty()) {
					for (Map.Entry<String, String> entry : genericTypes.entrySet()) {
						String genDec = entry.getKey().trim();
						if (genDec.equals("?")) {
							genDec = ("\\" + genDec);
						}
						String genDecPattern = "\\b" + genDec + "\\b";
						line = line.replaceAll(genDecPattern, entry.getValue().trim());
					}
				}
				line = line.replaceAll("\\.\\.\\.", "[]");
				for (String gt : possibleGenerics) {
					String genDec = gt.trim();
					if (genDec.equals("?")) {
						genDec = ("\\" + genDec);
					}
					String genDecPattern = "\\b" + genDec + "\\b";
					line = line.replaceAll(genDecPattern, "java.lang.Object");
				}

//				String regPattern = "@BoolRes\\s*|@FontRes\\s*|@IntegerRes\\s*|@DimenRes\\s*|@ColorRes\\s*|@HalfFloat\\s*|@TransitionRes\\s*|@AnimatorRes\\s*|@android\\.*?\\s*|@InterpolatorRes\\s*|@Px\\s*|@RawRes\\s*|@AnimRes\\s*|@RequiresPermission\\s*|@ArrayRes\\s*|@XmlRes\\s*|@MenuRes\\s*|@StringRes\\s*|@PluralsRes\\s*|@AnyRes\\s*|@ColorLong\\s*|@IdRes\\s*|@AttrRes\\s*|@StyleRes\\s*|@LayoutRes\\s*|@DrawableRes\\s*|@Deprecated\\s*|@StyleableRes\\s*|@ColorInt\\s*|@NonNull\\s*|@Nullable\\s*|@FloatRange\\(.*?\\)\\s*|@IntRange\\(.*?\\)\\s*|@Size\\(.*?\\)\\s*";
				String regPattern = "@[a-zA-Z].*?\\s+";
				if (line.startsWith("ctor"))
				{
					sb.append("<" + currentClsName + ": void <init>");
					
//					String params = line.substring(line.lastIndexOf('('), line.lastIndexOf(')')+1).replace(" ", "");
					String params = line.substring(line.lastIndexOf('('), line.lastIndexOf(')')+1).replaceAll(regPattern, "");
					String[] paramSplits = params.substring(1, params.length() - 1).split(",");
					sbparams.append("(");
					boolean commaAdd = false;
					for (String param: paramSplits) {
						String neatParam = javaLangAppend(param.trim());
						if (commaAdd) {
							sbparams.append("," + neatParam);
						} else {
							sbparams.append(neatParam);
							commaAdd = true;
						}
					}
					sbparams.append(")");
//					sb.append(params + ">");
					sb.append(sbparams.toString() + ">");
				}
				else if (line.startsWith("method"))
				{

					int firstBracketPos = line.indexOf("(");
					int lastBracketPos = line.lastIndexOf(")");

					if (txtFilePath.contains("android-30.txt") && line.contains("setLineSpacing(float,")) {
						System.out.println("Start:" + line);
					}
					line = line.substring(0, firstBracketPos + 1) + line.substring(firstBracketPos + 1, lastBracketPos).replaceAll(regPattern, "") + line.substring(lastBracketPos);
					if (txtFilePath.contains("android-30.txt") && line.contains("setLineSpacing(float,")) {
						System.out.println("End:" + line);
					}
					sb.append("<" + currentClsName + ": ");
					
					String params = line.substring(line.lastIndexOf('('), line.lastIndexOf(')')+1).replace(" ", "");
					
					line = line.substring(0, line.lastIndexOf('('));
					String[] strs = line.split(" ");
					sbparams.append("(");
					String[] paramSplits = params.substring(1, params.length() - 1).split(",");
					boolean commaAdd = false;
					for (String param: paramSplits) {
						String neatParam = javaLangAppend(param.trim());
						if (commaAdd) {
							sbparams.append("," + neatParam);
						} else {
							sbparams.append(neatParam);
							commaAdd = true;
						}
					}
					sbparams.append(")");
					sb.append(javaLangAppend(strs[strs.length-2]) + " " + strs[strs.length-1]);
//					sb.append(params + ">");
					sb.append(sbparams.toString() + ">");
				}
				
				if (!currentClsName.contains(".test."))
					put(class2Methods, currentClsName, sb.toString());

			} else if (line.contains("field")) {
				StringBuilder sb = new StringBuilder();
				StringBuilder modsb = new StringBuilder();
				String newLine = line + "\n";
				String pattern = "(.*)(;\\n|;[ \\t\\x0B\\f\\r]*\\n|;[ \\t\\x0B\\f\\r]*//.*\\n)";
				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(newLine);
				String commentRemoved = line;
				boolean firstMod = true;
				if (m.find()) {
					commentRemoved = m.group(1);
				}
				int semicolonPos = line.indexOf(";");
//				String commentRemoved = line.substring(0, semicolonPos);
				
				sb.append("<" + currentClsName + ": ");
				if (commentRemoved.contains("=")) {
					String splits[] = commentRemoved.split(" ");
					int splitSize = splits.length;
					// field public static final int STATE_ERROR = 3; // 0x3
					sb.append(javaLangAppend(splits[splitSize - 4]) + " ");
					sb.append(splits[splitSize - 3] + " ");
					sb.append(splits[splitSize - 2] + " ");
					sb.append(splits[splitSize - 1]);
					for (int i = 0; i < splitSize - 4; i++) {
						if (firstMod) {
							modsb.append(splits[i].trim());
							firstMod = false;
						} else {
							modsb.append("," + splits[i].trim());
						}
					}
				} else {
					String splits[] = commentRemoved.split(" ");
					int splitSize = splits.length;
					// field public final int flags;
					sb.append(javaLangAppend(splits[splitSize - 2]) + " ");
					sb.append(splits[splitSize - 1]);
					
					for (int i = 0; i < splitSize - 2; i++) {
						if (firstMod) {
							modsb.append(splits[i].trim());
							firstMod = false;
						} else {
							modsb.append("," + splits[i].trim());
						}
					}
				}
				sb.append(">");
				
				put(class2Fields, currentClsName, sb.toString());
			}
		}
	}
	
	public void parseXmlFile(String xmlFilePath)
	{
		SAXBuilder builder = new SAXBuilder();
		
		try
		{
			Document doc = (Document) builder.build(xmlFilePath);
			Element api = doc.getRootElement();

			List<Element> packageEles = api.getChildren("package");
			for (Element packageEle : packageEles)
			{
				String packageName = packageEle.getAttributeValue("name");
				if (packageName.contains(".test."))
					continue;
				
				List<Element> classEles = packageEle.getChildren("class");
				List<Element> interfaceEles = packageEle.getChildren("interface");
				
				List<Element> workList = new ArrayList<Element>();
				if (classEles != null)
				{
					workList.addAll(classEles);
				}
				if (interfaceEles != null)
				{
					workList.addAll(interfaceEles);
				}
				
				for (Element classEle : workList)
				{
					String className = packageName + "." + classEle.getAttributeValue("name");

					String extendedClass = classEle.getAttributeValue("extends");
					if (! "java.lang.Object".equals(extendedClass))
					{
						if (!className.contains(".test."))
							put(class2SuperClasses, className, classEle.getAttributeValue("extends"));
					}

					for (Element implementsEle : classEle.getChildren("implements"))
					{
						if (!className.contains(".test."))
							put(class2SuperClasses, className, implementsEle.getAttributeValue("name"));
					}
					
					for (Element constructorEle : classEle.getChildren("constructor"))
					{
						StringBuilder sb = new StringBuilder();
						sb.append("<" + className + ": void <init>(");
						
						boolean first = true;
						
						for (Element parameterEle : constructorEle.getChildren("parameter"))
						{
							if (first)
							{
								sb.append(javaLangAppend(removeGenericType(parameterEle.getAttributeValue("type"))));
								first = false;
							}
							else
							{
								sb.append("," + javaLangAppend(removeGenericType(parameterEle.getAttributeValue("type"))));
							}
						}
						
						sb.append(")>");
						
						String ctorStr = sb.toString();
						ctorStr = ctorStr.replaceAll("\\.\\.\\.", "[]");
						for (String gt : possibleGenerics) {
							String genDec = gt.trim();
							if (genDec.equals("?")) {
								genDec = ("\\" + genDec);
							}
							String genDecPattern = "\\b" + genDec + "\\b";
							ctorStr = ctorStr.replaceAll(genDecPattern, "java.lang.Object");
						}

						if (!className.contains(".test."))
							put(class2Methods, className, ctorStr);
					}
					
					for (Element methodEle : classEle.getChildren("method"))
					{
						StringBuilder sb = new StringBuilder();
						sb.append("<" + className + ": ");
						sb.append(javaLangAppend(removeGenericType(methodEle.getAttributeValue("return"))));
						sb.append(" " + methodEle.getAttributeValue("name"));
						sb.append("(");
						
						boolean first = true;
						
						for (Element parameterEle : methodEle.getChildren("parameter"))
						{
							if (first)
							{
								sb.append(javaLangAppend(removeGenericType(parameterEle.getAttributeValue("type"))));
								first = false;
							}
							else
							{
								sb.append("," + javaLangAppend(removeGenericType(parameterEle.getAttributeValue("type"))));
							}
						}
						
						sb.append(")>");
						
						String methodStr = sb.toString();
						methodStr = methodStr.replaceAll("\\.\\.\\.", "[]");
						for (String gt : possibleGenerics) {
							String genDec = gt.trim();
							if (genDec.equals("?")) {
								genDec = ("\\" + genDec);
							}
							String genDecPattern = "\\b" + genDec + "\\b";
							methodStr = methodStr.replaceAll(genDecPattern, "java.lang.Object");
						}
						
						if (!className.contains(".test."))
							put(class2Methods, className, methodStr);
					}
					
					for (Element fieldEle : classEle.getChildren("field")) {
						StringBuilder sb = new StringBuilder();
						sb.append("<" + className + ": ");
						sb.append(fieldEle.getAttributeValue("type") + " ");
						sb.append(fieldEle.getAttributeValue("name"));
						String fieldValue = fieldEle.getAttributeValue("value");
						if (null != fieldValue) {
							sb.append(" = " + fieldValue);
						}
						sb.append(">");
						
						put(class2Fields, className, sb.toString());
					}
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static String removeGenericType(String str)
	{
		while (str.contains(">"))
		{
			int endAngleBracket = str.indexOf('>');
			int startAngleBracket = str.lastIndexOf('<', endAngleBracket);
			
			String strInAngleBracket = str.substring(startAngleBracket, endAngleBracket+1);
			
			str = str.replace(strInAngleBracket, "");
		}
		
		return str;
	}
	
	public void put(Map<String, Set<String>> class2Methods, String cls, String method)
	{
		if (class2Methods.containsKey(cls))
		{
			Set<String> methods = class2Methods.get(cls);
			methods.add(method);
			class2Methods.put(cls, methods);
		}
		else
		{
			Set<String> methods = new HashSet<String>();
			methods.add(method);
			class2Methods.put(cls, methods);
		}
	}

	public static String javaLangTrim(String paramType) {
		String retType = paramType.trim();
		if (paramType.contains("java.lang.")) {
			retType = paramType.trim().replace("java.lang.", "");
		}
		return retType;
	}

	public static String  javaLangAppend(String paramType) {
		String param = paramType.trim();
		String regPattern = "(^[A-Za-z]+)?(.*)";
		if (param.contains(".")) {
			return param;
		} else {
	        Pattern p = Pattern.compile(regPattern);
	        Matcher m = p.matcher(param);
	        if (m.matches() && javaLangClasses.contains(m.group(1))) {
	        	return "java.lang." + param;
	        } else {
	        	return param;
	        }
		}
	}
}
