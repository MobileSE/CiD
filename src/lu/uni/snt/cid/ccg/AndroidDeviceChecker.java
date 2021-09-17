package lu.uni.snt.cid.ccg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lu.uni.snt.cid.AndroidAPIFieldLifeModel;
import lu.uni.snt.cid.Config;
import lu.uni.snt.cid.utils.CommonUtils;
import lu.uni.snt.cid.utils.SootUtils;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class AndroidDeviceChecker// extends BodyTransformer 
{
	/*
	public static void scan(String apkPath, String androidJars)
	{
		G.reset();
		
		String[] args =
        {
			"-process-dir", apkPath,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:false",
			"-src-prec", "apk"
        };
			
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_android_jars(androidJars);
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.AndroidSDKVersionChecker", new AndroidSDKVersionChecker()));
		soot.Main.main(args);
		
		G.reset();
		
		DeviceConditionalCallGraph.expandConstructors();
	}*/
	
	//@Override
	//protected void internalTransform(Body b, String phaseName, Map<String, String> options)
	public static void scan(Body b)
	{
		if (! b.getMethod().getDeclaringClass().getName().startsWith("android.support"))
		{
//			if (b.toString().contains(Config.FIELD_VERSION_SDK_INT))
//			{
//				Config.containsSDKVersionChecker = true;
//			}
			
			ExceptionalUnitGraph graph = new ExceptionalUnitGraph(b);

			for (Unit unit : graph.getHeads())
			{
				traverse(b, graph, unit, new HashSet<Value>(), new HashSet<String>(), new HashSet<Unit>());
			}
		}
	}
	
	static void traverse(Body b, ExceptionalUnitGraph graph, Unit unit, Set<Value> deviceValues, Set<String> conditions, Set<Unit> visitedUnits)
	{
		if (visitedUnits.contains(unit))
		{
			return;
		}
		else
		{
			visitedUnits.add(unit);
		}
		
		List<Unit> succUnits = null;
		Stmt stmt = (Stmt) unit;
		boolean sdkChecker = false;
		
		while(true)
		{
			if (stmt instanceof AssignStmt)
			{
				AssignStmt assignStmt = (AssignStmt) stmt;
				Value leftOp = assignStmt.getLeftOp();
				
				if (CommonUtils.containDeviceCheck(stmt.toString()))
				{
					List<Unit> currSuccUnits = graph.getSuccsOf(stmt);
					for (Unit curr : currSuccUnits) {
						if (curr instanceof AssignStmt) {
							if (curr.toString().contains("<java.lang.String: int compareTo(java.lang.String)>") || 
									curr.toString().contains("<java.lang.String: boolean equals(java.lang.Object)>")) {
								AssignStmt currAssign = (AssignStmt) curr;
								Value currLeft = currAssign.getLeftOp();
								deviceValues.add(currLeft);
							}
						}
					}
				}
				else
				{
					//Remove killed references
					if (deviceValues.contains(leftOp))
					{
						deviceValues.remove(leftOp);
					}
				}

				String leftVar = CommonUtils.getVariable(leftOp.toString());

				Value rightOp = assignStmt.getRightOp();
				String rightVar = CommonUtils.getVariable(rightOp.toString());

				if (stmt.hasTag("LineNumberTag")) {
					LineNumberTag tag = (LineNumberTag) stmt.getTag("LineNumberTag");
					int lineNumber = tag.getLineNumber();

					if (!leftVar.isEmpty()) {
						Edge edge = DeviceConditionalCallGraph.getEdge(b.getMethod().getSignature(), leftVar.replace("$", ".") + "-" + lineNumber);
						edge.conditions.add(conditions.toString());
						DeviceConditionalCallGraph.addEdge(edge);
					}

					if (!rightVar.isEmpty()) {
						Edge edge = DeviceConditionalCallGraph.getEdge(b.getMethod().getSignature(), rightVar.replace("$", ".") + "-" + lineNumber);
						edge.conditions.add(conditions.toString());
						DeviceConditionalCallGraph.addEdge(edge);
					}
				}

			}

			if (stmt.containsInvokeExpr())
			{
					Edge edge = DeviceConditionalCallGraph.getEdge(b.getMethod().getSignature(), stmt.getInvokeExpr().getMethod().getSignature().replace("$", "."));
					edge.conditions.add(conditions.toString());

					DeviceConditionalCallGraph.addEdge(edge);
					String currMethodSig = stmt.getInvokeExpr().getMethod().getSignature();
					if (AndroidAPIFieldLifeModel.getInstance().isInheritedAndroidAPI(currMethodSig)) {
						String superMethodSig = AndroidAPIFieldLifeModel.getInstance().method2inheritedAPIs.get(currMethodSig);
						superMethodSig = superMethodSig.replace("$", ".");
						Edge superClassEdge = DeviceConditionalCallGraph.getEdge(b.getMethod().getSignature(), superMethodSig);
						superClassEdge.conditions.add(conditions.toString());
	
						DeviceConditionalCallGraph.addEdge(superClassEdge);
					}
					
					if (stmt.getInvokeExpr() instanceof InterfaceInvokeExpr)
					{
						SootMethod sootMethod = stmt.getInvokeExpr().getMethod();
						
						if (!sootMethod.getDeclaration().toString().contains("private"))
						{
							//If the method is declared as private, then it cannot be extended by the sub-classes.
	//						continue;
	//					}
	//					} else {
						
							SootClass sootClass = sootMethod.getDeclaringClass();
							Set<SootClass> subClasses = SootUtils.getAllSubClasses(sootClass);
							
							for (SootClass subClass : subClasses)
							{
								Edge e = DeviceConditionalCallGraph.getEdge(edge.srcSig, edge.tgtSig.replace(sootClass.getName() + ":", subClass.getName() + ":"));
								e.conditions.addAll(edge.conditions);
	
								DeviceConditionalCallGraph.addEdge(e);
							}
						}
					}
			}
			
			if (stmt instanceof IfStmt)
			{
				IfStmt ifStmt = (IfStmt) stmt;

				for (ValueBox vb : ifStmt.getCondition().getUseBoxes())
				{
					if (deviceValues.contains(vb.getValue()))
					{
						sdkChecker = true;
						break;
					}
				}
			}
			
			succUnits = graph.getSuccsOf(stmt);
			if (succUnits.size() == 1)
			{
				stmt = (Stmt) succUnits.get(0);
				
				if (stmt instanceof ReturnStmt)
				{
					return;
				}
				
				if (visitedUnits.contains(stmt))
				{
					return;
				}
				else
				{
					visitedUnits.add(stmt);
				}
			}
			else if (succUnits.size() == 0)
			{
				//It's a return statement
				return;
			}
			else
			{
				break;
			}
		}
		
		if (sdkChecker)
		{
			if (stmt instanceof IfStmt)
			{
				IfStmt ifStmt = (IfStmt) stmt;
				Stmt targetStmt = ifStmt.getTarget();
				
				Set<String> positiveConditions = cloneSet(conditions);
				positiveConditions.add(ifStmt.getCondition().toString());
				traverse(b, graph, targetStmt, deviceValues, positiveConditions, visitedUnits);
				
				succUnits.remove(targetStmt);
				Set<String> negativeConditions = cloneSet(conditions);
				negativeConditions.add("-" + ifStmt.getCondition().toString());
				for (Unit u : succUnits)
				{
					if (!visitedUnits.contains(u)) {
						traverse(b, graph, u, deviceValues, negativeConditions, visitedUnits);
					}
				}
			}
			else
			{
				// For example, return statement
				return;
			}
			
		}
		else
		{
			for (Unit u : succUnits)
			{
				if (!visitedUnits.contains(u)) {
					traverse(b, graph, u, deviceValues, conditions, visitedUnits);
				}
			}
		}
	}
	
	public static Set<String> cloneSet(Set<String> src)
	{
		Set<String> tgt = new HashSet<String>();
		for (String str : src)
		{
			tgt.add(str);
		}
		
		return tgt;
	}
}
