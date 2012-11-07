package nl.miraclethings.parkeerbeter.map;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.List;

import nl.miraclethings.parkeerbeter.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class SelectZoneActivity extends MapActivity {
	private static final String TAG = "SelectZone";
	private MarkersOverlay mOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		Intent i = getIntent();
		GeoPoint center = ((ParkeerGeoPoint)i.getSerializableExtra("point")).getPoint();
		Log.v(TAG, "center=" + center.toString());
		
		List<ZoneMarker> markers = (List<ZoneMarker>)i.getSerializableExtra("markers");
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    MapController c = mapView.getController();
	    c.setZoom(19);
	    
	    if (center != null) {
	    	c.setCenter(center);
	    }
	    
	    if (markers == null)
	    	markers = new ArrayList<ZoneMarker>();
	    
	    Log.v(TAG, "Markers: " + markers.size());
	    mOverlay = new MarkersOverlay(this, center, getResources().getDrawable(R.drawable.marker), markers);
	    mapView.getOverlays().add(mOverlay);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void zoneSelected(ZoneMarker zoneMarker) {
		 Intent returnIntent = new Intent();
		 returnIntent.putExtra("result",(Serializable)zoneMarker);
		 setResult(RESULT_OK, returnIntent);
		 finish();
	}
}

