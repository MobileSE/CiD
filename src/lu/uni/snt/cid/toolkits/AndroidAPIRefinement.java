package lu.uni.snt.cid.toolkits;

import java.io.File;
import java.util.Set;

import lu.uni.snt.cid.FrameworkBase;
import lu.uni.snt.cid.utils.CommonUtils;
import lu.uni.snt.cid.utils.MethodSignature;
import lu.uni.snt.cid.utils.PathUtils;

public class AndroidAPIRefinement 
{
	public static void main(String[] args)
	{
		File androidAPIsDir = new File("apis/Official/android/android-apis/");
		for (File file : androidAPIsDir.listFiles())
		{
			if (! (file.getName().endsWith(".txt") || file.getName().endsWith(".xml")))
			{
				continue;
			}
			
			FrameworkBase fb = new FrameworkBase();
			
			fb.load(file.getAbsolutePath());
			
			StringBuilder sb = new StringBuilder();
			StringBuilder fieldsb = new StringBuilder();
			
			for (String cls : fb.class2SuperClasses.keySet())
			{
				if (null == fb.class2SuperClasses.get(cls))
				{
					System.out.println(cls);
				}
			}
			
			for (String cls : fb.class2Methods.keySet())
			{
				Set<String> methods = fb.class2Methods.get(cls);
				if (null != methods)
				{
					for (String method : methods)
					{
						method = new MethodSignature(method).getSignatureWithoutGPItems();
						sb.append(method + "\n");
					}
				}
				else
				{
					System.out.println(cls);
				}

			}
			
			for (String cls : fb.class2Fields.keySet()) {
				Set<String> fields = fb.class2Fields.get(cls);
				if (null != fields) {
					for (String field : fields) {
						fieldsb.append(field + "\n");
					}
				}
			}
			
			CommonUtils.writeResultToFile("apis/Official/android/android-apis-refinement/" + PathUtils.getFileNameWithoutExtension(file.getAbsolutePath()) + ".txt", sb.toString());
			CommonUtils.writeResultToFile("apis/Official/android/android-fields-refinement/" + PathUtils.getFileNameWithoutExtension(file.getAbsolutePath()) + ".txt", fieldsb.toString());
		}
	}
}
