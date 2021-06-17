package lu.uni.snt.cid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.cid.api.APILife;
import lu.uni.snt.cid.utils.CommonUtils;
import soot.Scene;
import soot.SootClass;

public class AndroidFieldLifeModel implements Serializable {
	public static final long serialVersionUID = 1785987027002129119L;
	private static AndroidFieldLifeModel instance = null;
	private static String modelPath = "apis/Official/android/android_field_model.txt";
	
	private String lifetimeFieldPath = "apis/Official/android/android_field_lifetime.txt";
	private String androidFieldsDirPath = "apis/Official/android/android-fields-refinement";
	
	public Map<String, Set<String>> field2SuperClasses = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> field2Class = new HashMap<String, Set<String>>();
	public Map<String, APILife> field2FieldLifes = new HashMap<String, APILife>();
	
	public Map<String, String> field2inheritedFields = new HashMap<String, String>();
	
	public static AndroidFieldLifeModel getInstance()
	{
		if (null == instance)
		{
			File model = new File(modelPath);
			if (model.exists())
			{
				try
				{
					FileInputStream fis = new FileInputStream(modelPath);
					ObjectInputStream ois = new ObjectInputStream(fis);
					
					instance = (AndroidFieldLifeModel) ois.readObject();
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
				instance = new AndroidFieldLifeModel();
				instance.serialize();
			}
			
			//CommonUtils.put(instance.compactSig2Methods, instance.compactSig2Methods_gt);
			//CommonUtils.put(instance.compactSig2Methods, instance.compactSig2Methods_varargs);
		}

		return instance;
	}
	
	private AndroidFieldLifeModel()
	{
		File androidAPIsDir = new File(androidFieldsDirPath);
		
		for (File file : androidAPIsDir.listFiles()) {
			FrameworkExtract fe = new FrameworkExtract();
			fe.load(file.getAbsolutePath());
			CommonUtils.put(field2SuperClasses, fe.field2SuperClasses);
			CommonUtils.put(field2Class, fe.field2Class);
		}
		
		Set<String> lines = CommonUtils.loadFile(lifetimeFieldPath);
		for (String line : lines)
		{
			APILife apiLife = new APILife(line);
			field2FieldLifes.put(apiLife.getSignature(), apiLife);
		}
	}
	
	
	public void serialize()
	{
		try 
		{
			FileOutputStream fos = new FileOutputStream(modelPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
	        
			System.out.printf("The API Life Model is Serialized into file res/android_field_model.txt");
	    }
		catch(IOException ex) 
		{
			ex.printStackTrace();
		}
	}

	public void retrieveSuperClassFieldLifes(String field, Set<APILife> lifes) {
		if (field2FieldLifes.containsKey(field)) {
			lifes.add(field2FieldLifes.get(field));
			String cls = CommonUtils.getClassName(field);
			if (field2SuperClasses.containsKey(cls)) {
				for (String superCls : field2SuperClasses.get(cls)) {
					String newFieldSig = CommonUtils.clsNameReplace(field, superCls);
					retrieveSuperClassFieldLifes(newFieldSig, lifes);
				}
			}
		}
	}
	
	public APILife getDirectLifeTime(String field) {
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
}
