package lu.uni.snt.cid.api;

import java.io.Serializable;

public class APILife implements Serializable
{
	private static final long serialVersionUID = 2736868985941458468L;
	
	String signature = "";
	String apilevels = "";
	int minAPILevel = Integer.MAX_VALUE;
	int maxAPILevel = Integer.MIN_VALUE;
	
	public APILife() {}
	
	public APILife(String signature, int min, int max) 
	{
		this.signature = signature;
		this.minAPILevel = min;
		this.maxAPILevel = max;
	}
	
	public APILife(String apiLifeTxt)
	{
//		this.signature = apiLifeTxt.substring(0, apiLifeTxt.lastIndexOf(':'));
//
//		String levelStr = apiLifeTxt.substring(apiLifeTxt.lastIndexOf('[') + 1, apiLifeTxt.lastIndexOf(']'));
//		this.minAPILevel = Integer.parseInt(levelStr.split(",")[0]);
//		this.maxAPILevel = Integer.parseInt(levelStr.split(",")[1]);
		this.signature = apiLifeTxt.substring(0, apiLifeTxt.indexOf(":["));
		String totalAPIs = apiLifeTxt.substring(apiLifeTxt.indexOf(":[") + 2, apiLifeTxt.lastIndexOf(']'));
		String[] apiSplits = totalAPIs.split(",");
		this.minAPILevel = Integer.parseInt(apiSplits[0]);
		this.maxAPILevel = Integer.parseInt(apiSplits[apiSplits.length - 1]);
		this.apilevels = totalAPIs;
	}

	@Override
	public String toString()
	{
//		return signature + ":[" + minAPILevel + "," + maxAPILevel + "]";
		return signature + ":[" + this.apilevels + "]";
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getMinAPILevel() {
		return minAPILevel;
	}

	public void setMinAPILevel(int minAPILevel) {
		this.minAPILevel = minAPILevel;
	}

	public int getMaxAPILevel() {
		return maxAPILevel;
	}

	public void setMaxAPILevel(int maxAPILevel) {
		this.maxAPILevel = maxAPILevel;
	}

	public String getAPILevels() {
		return this.apilevels;
	}

	public void setAPILevels(String apiLevels) {
		this.apilevels = apiLevels;
	}

	public int[] getAPILevelsInInt() {
		String[] splits = this.apilevels.split(",");
		int[] apis = new int[splits.length];
		int idx = 0;
		for (String api: splits) {
			apis[idx] = Integer.parseInt(api);
			idx += 1;
		}

		return apis;
	}
}
