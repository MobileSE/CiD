package lu.uni.snt.cid.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lu.uni.snt.cid.AndroidAPILifeModel;
import lu.uni.snt.cid.AndroidFieldLifeModel;
import lu.uni.snt.cid.Config;
import lu.uni.snt.cid.ccg.AndroidSDKVersionChecker;
import lu.uni.snt.cid.utils.CommonUtils;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

public class Mining4UTransformer extends SceneTransformer
{
	public Set<String> accessedAndroidAPIs = new HashSet<String>();
	public Map<String, Set<String>> api2callers = new HashMap<String, Set<String>>();
	
	public Map<String, Set<String>> api2supers = new HashMap<String, Set<String>>();
	
	public Set<String> superMethods = new HashSet<String>();
	
	public Set<String> accessedFields = new HashSet<String>();

	private void extract(Body b)
	{
		String callerMethodSig = b.getMethod().getSignature().replace("$", ".");
		
		if (b.toString().contains(Config.FIELD_VERSION_SDK_INT) || b.toString().contains(Config.FIELD_VERSION_SDK))
		{
			Config.containsSDKVersionChecker = true;
			
			if (b.toString().contains(Config.FIELD_VERSION_SDK))
			{
				if (Config.DEBUG)
					System.out.println("[DEBUG] SDK Field (deprecated in API level 4) is still used!");
			}
		}
		
		PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> unitIter = units.snapshotIterator(); unitIter.hasNext(); )
		{
			Stmt stmt = (Stmt) unitIter.next();
			
			if (stmt.containsInvokeExpr())
			{
				SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
				String methodSig = sootMethod.getSignature();
				
				if (AndroidAPILifeModel.getInstance().isAndroidAPI(methodSig))
				{
					methodSig = methodSig.replace("$", ".");
					
					accessedAndroidAPIs.add(methodSig);
					CommonUtils.put(api2callers, methodSig, callerMethodSig);
				}
//				else if (AndroidAPILifeModel.getInstance().isInheritedAndroidAPI(methodSig))
//				{
//					methodSig = AndroidAPILifeModel.getInstance().method2inheritedAPIs.get(methodSig);
//					
//					methodSig = methodSig.replace("$", ".");
//					
//					accessedAndroidAPIs.add(methodSig);
//					CommonUtils.put(api2callers, methodSig, callerMethodSig);
//				}
				
				String maySuperSig = sootMethod.getSignature();
				String currSig = sootMethod.getSignature();
				if (AndroidAPILifeModel.getInstance().isInheritedAndroidAPI(maySuperSig)) {
					String currAPI = currSig.replace("$", ".");
					Set<String> inheritedMethods = AndroidAPILifeModel.getInstance().getInheritedAPIs(currSig);
					if (api2supers.containsKey(currAPI)) {
						api2supers.get(currAPI).addAll(inheritedMethods);
					} else {
						api2supers.put(currAPI, inheritedMethods);
					}
				}
			} else if (stmt instanceof AssignStmt) {
				AssignStmt assignStmt = (AssignStmt) stmt;
				String leftOp = assignStmt.getLeftOp().toString();
				String rightOp = assignStmt.getRightOp().toString();
				String leftVar = CommonUtils.getVariable(leftOp);
				String rightVar = CommonUtils.getVariable(rightOp);
				if (stmt.hasTag("LineNumberTag")) {
					LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
					int lineNumber = tag.getLineNumber();
					if (!leftVar.isEmpty()) {
						String currField = leftVar;
						if (AndroidFieldLifeModel.getInstance().isAndroidField(currField)) {
							accessedFields.add(currField.replace("$", ".") + "-" + lineNumber);
						}
					}
					if (!rightVar.isEmpty()) {
						String currField = rightVar;
						if (AndroidFieldLifeModel.getInstance().isAndroidField(currField)) {
							accessedFields.add(currField.replace("$", ".") + "-" + lineNumber);
						}
					}
				}
			}
		}
	}
	
	protected void internalBodyTransform(Body b) 
	{
		extract(b);
		AndroidSDKVersionChecker.scan(b);
	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) 
	{
		Chain<SootClass> sootClasses = Scene.v().getApplicationClasses();
		for (Iterator<SootClass> iter = sootClasses.snapshotIterator(); iter.hasNext(); )
		{
			SootClass sc = iter.next();
			
			if (sc.getName().startsWith("android.support."))
			{
				continue;
			}
			
			List<SootMethod> methods = sc.getMethods();
			for (int i = 0; i < methods.size(); i++)
			{
				SootMethod sm = methods.get(i);
				Body body = null;
				try
				{
					body = sm.retrieveActiveBody();
				}
				catch (Exception ex)
				{
					if (Config.DEBUG)
						System.out.println("[DEBUG] No body for method " + sm.getSignature());
				}
				
				if (null != body)
					internalBodyTransform(body);
			}
		}
		
	}
}
