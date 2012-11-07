package nl.miraclethings.parkeerbeter.map;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;


public class ZoneMarker extends Object implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String zonecode;
	public String name;
	public GeoPoint point;

	private double longitude;
	private double latitude;
	
	public ZoneMarker(String zonecode, String name, double latitude, double longitude) {
		this.zonecode = zonecode;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public GeoPoint getPoint() {
		return new GeoPoint((int)(this.latitude*1e6), (int)(this.longitude*1e6));
	}
	
}
