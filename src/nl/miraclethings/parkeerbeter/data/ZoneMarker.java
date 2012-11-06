package nl.miraclethings.parkeerbeter.data;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;


public class ZoneMarker extends Object implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String zonecode;
	public String name;
	public GeoPoint point;
	
	public ZoneMarker(String zonecode, String name, double latitude, double longitude) {
		this.zonecode = zonecode;
		this.name = name;
		this.point = new GeoPoint((int)(latitude*1e6), (int)(longitude));
	}
	
}
