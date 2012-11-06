package nl.miraclethings.parkeerbeter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;


public class SelectZoneActivity extends MapActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		Intent i = getIntent();
		GeoPoint center = (GeoPoint)i.getSerializableExtra("point");
		
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    if (center != null) {
	    	MapController c = mapView.getController();
	    	c.setCenter(center);
	    }
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}

