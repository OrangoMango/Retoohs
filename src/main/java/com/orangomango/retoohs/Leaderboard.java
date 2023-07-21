package com.orangomango.retoohs;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

public class Leaderboard{
	private String location;
	private JSONObject json;

	public Leaderboard(String loc){
		this.location = loc;
	}

	public void load(){
		try {
			URL url = new URL(this.location+"?mode=load");
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			reader.lines().skip(5).forEach(builder::append);
			reader.close();
			this.json = new JSONObject(builder.toString());
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

	public void addEntry(String user, int value){
		try {
			URL url = new URL(this.location+"?mode=save"+String.format("&user=%s&value=%d", user, value));
			url.openStream();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		load();
	}

	public List<Map.Entry<String, Integer>> getEntries(){
		if (this.json == null){
			return null;
		}
		Map<String, Integer> lead = new HashMap<>();
		for (Object o : this.json.getJSONArray("data")){
			JSONObject j = (JSONObject)o;
			lead.put(j.getString("user"), j.getInt("value"));
		}
		List<Map.Entry<String, Integer>> output = new ArrayList<>(lead.entrySet());
		return output;
	}
}