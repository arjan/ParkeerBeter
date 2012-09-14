package nl.miraclethings.parkeerbeter;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

public class RecentPreferenceList {
	
	private static final String TAG = "RecentPreferenceList";
	private SharedPreferences preferences;
	private String key;
	private List<String> values;
	
	static int max = 5;

	public RecentPreferenceList(SharedPreferences preferences, String key) {
		this.preferences = preferences;
		this.key = key;
		this.values = new ArrayList<String>();
		
		// load
		String[] r = preferences.getString(key, "").split(",");
		for (int i=0; i<r.length; i++) {
			String v = r[i];
			if (v.length() > 0)
				this.values.add(v);
		}
	}
	
	public void save() {
		String imploded = "";
		for (String v : this.values) {
			imploded += v + ",";
		}
		imploded = imploded.substring(0, imploded.length()-1);
		preferences.edit().putString(this.key, imploded).commit();
	}
	
	public void add(String value) {
		value = value.trim();
		if (value.length() == 0) return;
		
		for (int i=0; i<this.values.size(); i++) {
			if (this.values.get(i).toLowerCase().equals(value.toLowerCase())) {
				this.values.remove(i);
				i--;
			}
		}
		this.values.add(0, value);
		while (this.values.size() > max) 
			this.values.remove(this.values.size()-1);
	}
	
	public String mostRecent() {
		return this.values.size() > 0 ? this.values.get(0) : "";
	}
	
	public String[] all() {
		String[] r = new String[this.values.size()];
		return this.values.toArray(r);
	}
}
