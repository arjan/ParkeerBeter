package nl.miraclethings.parkeerbeter.map;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;

public class ParkeerGeoPoint implements Serializable {

	private int latitudeE6;
	private int longitudeE6;

	public ParkeerGeoPoint(int latitudeE6, int longitudeE6) {
		this.latitudeE6 = latitudeE6;
		this.longitudeE6 =longitudeE6;
	}

	private static final long serialVersionUID = 1L;

	public GeoPoint getPoint() {
		return new GeoPoint(latitudeE6, longitudeE6);
	}
}
