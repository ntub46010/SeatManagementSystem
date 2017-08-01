package org.sample.test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Test {
	public static void main(String[] args) {
		/*
		try {
			JSONArray ary = new JSONArray();
			JSONObject obj = new JSONObject("{\"MaxRow\":\"0\",\"MaxColumn\":\"0\"}");
			
			System.out.println(ary.toString());
		}catch(JSONException e) {
			e.printStackTrace();
		}
		*/
		JSONObject parentData = new JSONObject();
		JSONObject childData = new JSONObject();
		JSONArray ary = new JSONArray();

		try {
		    parentData.put("command", "login");
		    parentData.put("uid", "MyUid");

		    childData.put("username", "MyUsername");
		    childData.put("password", "MyPassword");
		    
		    ary.put(childData);
		    parentData.put("params", ary);
		    
		    System.out.println(parentData.toString());

		} catch (JSONException e) {
		    e.printStackTrace();
		}

	}
}
