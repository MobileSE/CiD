package lu.uni.snt.cid.toolkits;

import java.util.Set;

import lu.uni.snt.cid.utils.CommonUtils;
import lu.uni.snt.cid.utils.MethodSignature;

public class DumpAPIs_GT_Varargs {

	public static void main(String[] args) 
	{
		Set<String> lines = CommonUtils.loadFile("apis/Official/android/android_api_lifetime.txt");

		StringBuilder gt = new StringBuilder();
		StringBuilder varargs = new StringBuilder();
		
		for (String line : lines)
		{
			int lastAngleBracketPos = line.lastIndexOf('>');
			String methodSig = line.substring(0, lastAngleBracketPos+1);
			
			MethodSignature ms = new MethodSignature(methodSig);
			if (ms.containsGenericType())
			{
				gt.append(methodSig + "\n");
			}
			if (ms.containsVarargs())
			{
				varargs.append(methodSig + "\n");
			}
		}
		
		if (gt.toString().length() != 0) {
			CommonUtils.writeResultToFile("apis/Official/android/android_api_generictype.txt", gt.toString());
		}
		if (varargs.toString().length() != 0) {
			CommonUtils.writeResultToFile("apis/Official/android/android_api_varargs.txt", varargs.toString());
		}
	}

}
