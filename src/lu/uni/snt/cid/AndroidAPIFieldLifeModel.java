package lu.uni.snt.cid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.cid.api.APILife;
import lu.uni.snt.cid.utils.CommonUtils;
import lu.uni.snt.cid.utils.MethodSignature;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class AndroidAPIFieldLifeModel implements Serializable
{	
	private static final long serialVersionUID = 1785987027002129118L;
	
	public Map<String, Set<String>> class2SuperClasses = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> class2Methods = new HashMap<String, Set<String>>();
	public Map<String, APILife> method2APILifes = new HashMap<String, APILife>();
	
	//For such APIs that contain generic types or contain varargs.
	public Map<String, Set<String>> compactSig2Methods = new HashMap<String, Set<String>>();
	
	//compactSig2Methods = compactSig2Methods_gt U compactSig2Methods_varargs
	public Map<String, Set<String>> compactSig2Methods_gt = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> compactSig2Methods_varargs = new HashMap<String, Set<String>>();
	
	public Map<String, String> method2inheritedAPIs = new HashMap<String, String>();
	
	Map<String, Set<String[]>> deviceMethods  = new HashMap<String, Set<String[]>>();
	Map<String, Set<String[]>> deviceFields = new HashMap<String, Set<String[]>>();
	
	public Map<String, APILife> field2FieldLifes = new HashMap<String, APILife>();
	public Map<String, String> field2inheritedFields = new HashMap<String, String>();
	
	public boolean apiLevel1Exists;
	
	public String[] csvMethodHeaders = null;
	public String[] csvFieldHeaders = null;
	
	private static AndroidAPIFieldLifeModel instance = null;
	private static String modelPath = "apis/android/android_api_model.txt";
	private static String deviceModelPath = "devices/device_specific_method_field.txt";
	private static String cidModelPath = "apis/android_cid_model.txt";
	
	private String lifetimeAPIPath = "apis/android/android_api_lifetime.txt";
	private String genericAPIPath = "apis/android/android_api_generictype.txt";
	private String varargsAPIPath = "apis/android/android_api_varargs.txt";
	private String androidAPIsDirPath = "apis/android/android-apis-refinement";
	
	private String androidFieldsDirPath = "apis/android/android-fields-refinement";
	private String androidLifetimeFieldPath = "apis/android/android_field_lifetime.txt";
	
	private String deviceAPIPath = "apis/android/android-devices/device_specific_methods.csv";
	private String deviceFieldPath = "apis/android/android-devices/device_specific_fields.csv";
	
	public static AndroidAPIFieldLifeModel getInstance()
	{
		if (null == instance)
		{
			File model = new File(cidModelPath);
			if (model.exists())
			{
				try
				{
					FileInputStream fis = new FileInputStream(cidModelPath);
					ObjectInputStream ois = new ObjectInputStream(fis);
					
					instance = (AndroidAPIFieldLifeModel) ois.readObject();
					ois.close();
					fis.close();
				}
				catch (IOException | ClassNotFoundException ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				instance = new AndroidAPIFieldLifeModel();
				instance.serialize();
			}
			
			Set<String> genericAPIs = CommonUtils.loadFile(instance.genericAPIPath);
			for (String genericAPI : genericAPIs)
			{
				String compactSig = new MethodSignature(genericAPI).getCompactSignature();
				CommonUtils.put(instance.compactSig2Methods, compactSig, genericAPI);
				CommonUtils.put(instance.compactSig2Methods_gt, compactSig, genericAPI);
			}
			
			Set<String> varargsAPIs = CommonUtils.loadFile(instance.varargsAPIPath);
			for (String varargsAPI : varargsAPIs)
			{
				String compactSig = new MethodSignature(varargsAPI).getCompactSignature();
				CommonUtils.put(instance.compactSig2Methods, compactSig, varargsAPI);
				CommonUtils.put(instance.compactSig2Methods_varargs, compactSig, varargsAPI);
			}
			
			//CommonUtils.put(instance.compactSig2Methods, instance.compactSig2Methods_gt);
			//CommonUtils.put(instance.compactSig2Methods, instance.compactSig2Methods_varargs);
		}

		return instance;
	}
	
	private AndroidAPIFieldLifeModel()
	{
//		for (File file : androidAPIsDir.listFiles())
//		{
//			FrameworkBase fb = new FrameworkBase();
//			
//			fb.load(file.getAbsolutePath());
//			
//			CommonUtils.put(class2SuperClasses, fb.class2SuperClasses);
//			CommonUtils.put(class2Methods, fb.class2Methods);
//		}
		apiLevel1Exists = false;
		Map<String, Set<String[]>> deviceM = CommonUtils.csvDeviceReader(deviceAPIPath);
		Map<String, Set<String[]>> deviceF = CommonUtils.csvDeviceReader(deviceFieldPath);
		
		List<String[]> methodHeader = new ArrayList<String[]>(deviceM.get("headers"));
		List<String[]> fieldHeader = new ArrayList<String[]>(deviceF.get("headers"));
		csvMethodHeaders = new String[methodHeader.get(0).length];
		int idx = 0;
		for (String val : methodHeader.get(0)) {
			csvMethodHeaders[idx++] = val; 
		}
		idx = 0;
		csvFieldHeaders = new String[fieldHeader.get(0).length];
		for (String val : fieldHeader.get(0)) {
			csvFieldHeaders[idx++] = val;
		}
		
		CommonUtils.devicePut(deviceMethods, deviceM);
		CommonUtils.devicePut(deviceFields, deviceF);
		
		File androidAPIsDir = new File(androidAPIsDirPath);
		for (File file : androidAPIsDir.listFiles()) {
			FrameworkExtract fe = new FrameworkExtract();
			fe.load(file.getAbsolutePath());
			if (file.getAbsolutePath().contains("android-1.txt")) {
				apiLevel1Exists = true;
			}
			CommonUtils.put(class2SuperClasses, fe.class2SuperClasses);
			CommonUtils.put(class2Methods, fe.class2Methods);
		}
		
		Set<String> lines = CommonUtils.loadFile(lifetimeAPIPath);
		for (String line : lines)
		{
			APILife apiLife = new APILife(line);
			method2APILifes.put(apiLife.getSignature(), apiLife);
		}
		
//		File androidFieldsDir = new File(androidFieldsDirPath);
		
//		for (File file : androidFieldsDir.listFiles()) {
////			FrameworkExtract fe = new FrameworkExtract();
//			FrameworkBase fb = new FrameworkBase();
//			fb.load(file.getAbsolutePath());
////			CommonUtils.put(field2SuperClasses, fb.field2SuperClasses);
////			CommonUtils.put(field2Class, fb.field2Class);
//		}
		
		Set<String> fields = CommonUtils.loadFile(androidLifetimeFieldPath);
		for (String field : fields)
		{
			APILife apiLife = new APILife(field);
			field2FieldLifes.put(apiLife.getSignature(), apiLife);
		}
	}
	
	public boolean containsGenericType(String methodSig)
	{
		methodSig = methodSig.replace("$", ".");
		
		MethodSignature ms = new MethodSignature(methodSig);
		String compactSig = ms.getCompactSignature();
		
		if (compactSig2Methods_gt.containsKey(compactSig))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean containsVarargs(String methodSig)
	{
		methodSig = methodSig.replace("$", ".");
		
		MethodSignature ms = new MethodSignature(methodSig);
		String compactSig = ms.getCompactSignature();
		
		if (compactSig2Methods_varargs.containsKey(compactSig))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isDeviceMethod(String method) {
		boolean deviceMethod = false;
		if (deviceMethods.containsKey(method)) {
			deviceMethod = true;
		}
		return deviceMethod;
	}
	
	public boolean isDeviceField(String field) {
		boolean deviceField = false;
		if (deviceFields.containsKey(field)) {
			deviceField = true;
		}
		return deviceField;
	}
	
	public Set<String[]> deviceSpecificMethod(String method) {
		if (deviceMethods.containsKey(method)) {
			return deviceMethods.get(method);
		}
		return null;
	}
	
	public Set<String[]> deviceSpecificField(String field) {
		if (deviceFields.containsKey(field)) {
			return deviceFields.get(field);
		}
		return null;
	}
	
	public APILife getFieldDirectLifeTime(String field) {
		if (field2FieldLifes.containsKey(field)) {
			return field2FieldLifes.get(field);
		} else {
			return null;
		}
	}
	
	/**
	 * Extension
	 * Generic Type
	 * Varargs
	 * 
	 * @param methodSig
	 * @return
	 */
	public boolean isAndroidAPI(String methodSig)
	{	
		methodSig = methodSig.replace("$", ".");
		
		if (method2APILifes.containsKey(methodSig))
		{
			return true;
		}
		else 
		{	
			String compatMethodSig = new MethodSignature(methodSig).getCompactSignature();
			
			if (compactSig2Methods_gt.containsKey(compatMethodSig))
			{
				if (Config.DEBUG)
					System.out.println("[DEBUG] Generic Programming:" + methodSig + " is an Android API with generic type");
				
				return true;
			}
			else if (compactSig2Methods_varargs.containsKey(compatMethodSig))
			{
				if (Config.DEBUG)
					System.out.println("[DEBUG] Varargs:" + methodSig + " is an Android API with varargs");
				
				return true;
			}
		}


		return false;
	}
	
	public Set<String> getInheritedAPIs(String methodSig) {
		Set<String> inheritedMethods = new HashSet<String>();
		try
		{
			SootMethod sootMethod = Scene.v().getMethod(methodSig);

			SootClass sootClass = sootMethod.getDeclaringClass();

			List<SootClass> workList = new LinkedList<SootClass>();
			if (sootClass.hasSuperclass())
			{
				workList.add(sootClass.getSuperclass());
			}
			for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
			{
				workList.add(iter.next());
			}

			while (! workList.isEmpty())
			{
				sootClass = workList.remove(0);

				String newMethodSig = methodSig.replace("<" + sootMethod.getDeclaringClass().getName() + ":", "<" + sootClass.getName() + ":");

				if (isAndroidAPI(newMethodSig))
				{
					inheritedMethods.add(newMethodSig.replace("$", "."));
				}
//				else
//				{
					if (sootClass.hasSuperclass() && !sootClass.getSuperclass().getName().equals("java.lang.Object"))
					{
						workList.add(sootClass.getSuperclass());
					}
					for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
					{
						workList.add(iter.next());
					}
//				}
			}
		}
		catch (Exception ex)
		{
			//TODO:
		}

		return inheritedMethods;
	}

	public boolean isInheritedAndroidAPI(String methodSig)
	{
		try
		{
			SootMethod sootMethod = Scene.v().getMethod(methodSig);
			
			SootClass sootClass = sootMethod.getDeclaringClass();
			
			List<SootClass> workList = new LinkedList<SootClass>();
			if (sootClass.hasSuperclass())
			{
				workList.add(sootClass.getSuperclass());
			}
			for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
			{
				workList.add(iter.next());
			}
			
			while (! workList.isEmpty())
			{
				sootClass = workList.remove(0);
				
				String newMethodSig = methodSig.replace("<" + sootMethod.getDeclaringClass().getName() + ":", "<" + sootClass.getName() + ":");
				
				if (isAndroidAPI(newMethodSig))
				{
					method2inheritedAPIs.put(methodSig, newMethodSig);
					return true;
				}
				else
				{
					if (sootClass.hasSuperclass())
					{
						workList.add(sootClass.getSuperclass());
					}
					for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
					{
						workList.add(iter.next());
					}
				}
			}
		}
		catch (Exception ex)
		{
			//TODO:
		}
		
		return false;
	}
	
	public void serialize()
	{
		try 
		{
			FileOutputStream fos = new FileOutputStream(cidModelPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
	        
			System.out.printf("The API Life Model is Serialized into file device modle: devices/device_specific_method_field.txt");
	    }
		catch(IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	public APILife getAPIDirectLifeTime(String methodSignature) {
		if (method2APILifes.containsKey(methodSignature)) {
			return method2APILifes.get(methodSignature);
		} else {
			return null;
		}
	}
	
	public void retrieveSuperClassAPILifes(String methodSig, Set<APILife> lifes) {
		if (method2APILifes.containsKey(methodSig)) {
			lifes.add(method2APILifes.get(methodSig));
			MethodSignature sig = new MethodSignature(methodSig);
			String cls = sig.getCls();
			if (class2SuperClasses.containsKey(cls)) {
				for (String superCls : class2SuperClasses.get(cls)) {
					String newMethodSig = methodSig.replace(cls + ":", superCls + ":");
					retrieveSuperClassAPILifes(newMethodSig, lifes);
				}
			}
		}
	}
	
	
	/**
	 * Extension
	 * Generic Type
	 * Varargs
	 * 
	 * @param methodSig
	 * @return
	 */
	public boolean isAndroidField(String field)
	{	
		field = field.replace("$", ".");
		
		if (field2FieldLifes.containsKey(field))
		{
			return true;
		}
		return false;
	}
	
	public boolean isInheritedAndroidField(String field)
	{
		try
		{
			String currClsName = CommonUtils.getClassName(field);
			
			SootClass sootClass = Scene.v().getSootClass(currClsName);
			
			List<SootClass> workList = new LinkedList<SootClass>();
			if (sootClass.hasSuperclass())
			{
				workList.add(sootClass.getSuperclass());
			}
			for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
			{
				workList.add(iter.next());
			}
			
			while (! workList.isEmpty())
			{
				sootClass = workList.remove(0);
				int semicolonPos = field.indexOf(":");
				
				String newFieldSig = "<" + sootClass.getName() + field.substring(semicolonPos);
				
				if (isAndroidField(newFieldSig))
				{
					field2inheritedFields.put(field, newFieldSig);
					return true;
				}
				else
				{
					if (sootClass.hasSuperclass())
					{
						workList.add(sootClass.getSuperclass());
					}
					for (Iterator<SootClass> iter = sootClass.getInterfaces().snapshotIterator(); iter.hasNext(); )
					{
						workList.add(iter.next());
					}
				}
			}
		}
		catch (Exception ex)
		{
			//TODO:
		}
		
		return false;
	}
	
	/**
	 * To also check the lifetime of its super classes
	 * 
	 * @param methodSignature
	 * @return
	 */
	public APILife getLifetime(String methodSignature)
	{
		methodSignature = methodSignature.replace("$", ".");
		
		APILife apiLife = new APILife(methodSignature, -1, -1);
		
		MethodSignature sig = new MethodSignature(methodSignature);
		
		if (! method2APILifes.containsKey(methodSignature))
		{
			Set<String> methods = compactSig2Methods.get(sig.getCompactSignature());
			if (null != methods)
			{
				for (String methodSig : methods)
				{
					MethodSignature ms = new MethodSignature(methodSig);
					if (ms.containsGenericType())
					{
						if (Config.DEBUG)
							System.out.println("[DEBUG]: GT Found, " + methodSig + "-->" + methodSignature);
						//refine(apiLife, methodSig);
					}
					else
					{
						if (Config.DEBUG)
							System.out.println("[DEBUG]: Varargs Found, " + methodSig + "-->" + methodSignature);
					}

					if (ms.containsGenericReturnType() ||
						ms.getReturnType().equals(sig.getReturnType()))
					{
						refine(apiLife, methodSig);
					}
					
					// To be more precise: ms.containsGenericType() && ms.containsVarargs()
				}
			}
		}
		else
		{
			refine(apiLife, methodSignature);
		}
		
		return apiLife;
	}
	
	public APILife refine(APILife current, String methodSignature)
	{
		if (method2APILifes.containsKey(methodSignature))
		{
			APILife target = method2APILifes.get(methodSignature);
			
			if (current.getMinAPILevel() == -1 || current.getMinAPILevel() > target.getMinAPILevel())
			{
				current.setMinAPILevel(target.getMinAPILevel());
			}
			if (current.getMinAPILevel() == -1 || current.getMaxAPILevel() < target.getMaxAPILevel())
			{
				current.setMaxAPILevel(target.getMaxAPILevel());
			}
			current.setAPILevels(target.getAPILevels());
		}
		
		
		MethodSignature sig = new MethodSignature(methodSignature);
		String cls = sig.getCls();
		if (class2SuperClasses.containsKey(cls))
		{
			for (String superCls : class2SuperClasses.get(cls))
			{
				current = refine(current, cls, superCls, methodSignature);
			}
		}
		
		return current;
	}
	
	
	public APILife refine(APILife current, String currentCls, String superCls, String methodSignature)
	{
		String newMethodSig = methodSignature.replace(currentCls + ":", superCls + ":");
		
		if (method2APILifes.containsKey(newMethodSig))
		{
			if (Config.DEBUG)
				System.out.println("[DEBUG] SuperClass:" + current.getSignature() + "-->" + newMethodSig);
			
			APILife target = method2APILifes.get(newMethodSig);
			
			if (current.getMinAPILevel() == -1 || current.getMinAPILevel() > target.getMinAPILevel())
			{
				current.setMinAPILevel(target.getMinAPILevel());
			}
			if (current.getMinAPILevel() == -1 || current.getMaxAPILevel() < target.getMaxAPILevel())
			{
				current.setMaxAPILevel(target.getMaxAPILevel());
			}
			current.setAPILevels(target.getAPILevels());
		}
	
		if (class2SuperClasses.containsKey(superCls))
		{
			for (String superSuperCls : class2SuperClasses.get(superCls))
			{
				current = refine(current, currentCls, superSuperCls, newMethodSig);
			}
		}
		
		return current;
	}
}
