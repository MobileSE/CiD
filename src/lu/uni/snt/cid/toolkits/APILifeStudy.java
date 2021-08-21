package lu.uni.snt.cid.toolkits;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.cid.utils.CommonUtils;

public class APILifeStudy 
{
	public static final int LATEST_API_LEVEL = 30;
	static Map<String, APILife> apiLifeMap = new HashMap<String, APILife>();
	
	static Map<String, APILife> fieldLifeMap = new HashMap<String, APILife>();
	
	
	public static void main(String[] args) 
	{
		for (int level = 1; level <= LATEST_API_LEVEL; level++)
		{
			if (level == 20)
			{
				continue;
			}

			File androidLevel = new File("apis/android/android-apis-refinement/android-" + level + ".txt");
			if (!androidLevel.exists()) {
				System.out.println("Non exists:" + "apis/android/android-apis-refinement/android-" + level + ".txt");
				continue;
			}
			Set<String> methods = CommonUtils.loadFile("apis/android/android-apis-refinement/android-" + level + ".txt");
//			Set<String> methods = CommonUtils.loadContentFromFile("apis/Official/android/android-apis-refinement/android-" + level + ".txt");
			
			Set<String> fields = CommonUtils.loadFile("apis/android/android-fields-refinement/android-" + level + ".txt");
//			Set<String> fields = CommonUtils.loadContentFromFile("apis/Official/android/android-fields-refinement/android-" + level + ".txt");
			
			for (String method : methods)
			{
				if (apiLifeMap.containsKey(method))
				{
					APILife apiLife = apiLifeMap.get(method);
					apiLife.levelInterval += (level + ",");
					if (apiLife.minAPILevel > level)
					{
						apiLife.minAPILevel = level;
					}
					if (apiLife.maxAPILevel < level)
					{
						apiLife.maxAPILevel = level;
					}
					
					apiLifeMap.put(method, apiLife);
				}
				else
				{
					APILife apiLife = new APILife();
					apiLife.signature = method;
					apiLife.levelInterval += (level + ",");
					
					if (apiLife.minAPILevel > level)
					{
						apiLife.minAPILevel = level;
					}
					if (apiLife.maxAPILevel < level)
					{
						apiLife.maxAPILevel = level;
					}
					
					apiLifeMap.put(method, apiLife);
				}
			}
			
			for (String field : fields)
			{
				if (fieldLifeMap.containsKey(field))
				{
					APILife apiLife = fieldLifeMap.get(field);
					apiLife.levelInterval += (level + ",");
					if (apiLife.minAPILevel > level)
					{
						apiLife.minAPILevel = level;
					}
					if (apiLife.maxAPILevel < level)
					{
						apiLife.maxAPILevel = level;
					}
					
					fieldLifeMap.put(field, apiLife);
				}
				else
				{
					APILife apiLife = new APILife();
					apiLife.signature = field;
					apiLife.levelInterval += (level + ",");
					
					if (apiLife.minAPILevel > level)
					{
						apiLife.minAPILevel = level;
					}
					if (apiLife.maxAPILevel < level)
					{
						apiLife.maxAPILevel = level;
					}
					
					fieldLifeMap.put(field, apiLife);
				}
			}
		}
		
		StringBuilder output = new StringBuilder();
		
		for (String key : apiLifeMap.keySet())
		{
			output.append(apiLifeMap.get(key) + "\n");
		}
		
		CommonUtils.writeResultToFile("apis/android/android_api_lifetime.txt", output.toString());
		
		StringBuilder fieldOut = new StringBuilder();
		
		for (String key : fieldLifeMap.keySet()) {
			fieldOut.append(fieldLifeMap.get(key) + "\n");
		}
		
		CommonUtils.writeResultToFile("apis/android/android_field_lifetime.txt", fieldOut.toString());
	}

	static class APILife
	{
		String signature = "";
		int minAPILevel = Integer.MAX_VALUE;
		int maxAPILevel = Integer.MIN_VALUE;
		String levelInterval = "";
		boolean revert = false;
		
		@Override
		public String toString()
		{
			if (levelInterval.endsWith(",")) {
				return signature + ":[" + levelInterval.substring(0, levelInterval.length() - 1) + "]:" + revert;
			} else {
				return signature + ":[" + levelInterval + "]:" + revert;
			}
		}
	}
}
