package nl.miraclethings.parkeerbeter.map;

import nl.miraclethings.parkeerbeter.MainActivity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationService {
	private MainActivity mContext;
	private LocationManager mLocationManager;
	private LocationListener mListener;

	public LocationService(final MainActivity context) {
		mContext = context;
		
		mLocationManager =
		        (LocationManager) context.getSystemService(MainActivity.LOCATION_SERVICE);

		mListener = new LocationListener() {
		    @Override
		    public void onLocationChanged(Location location) {
		        // A new location update is received.  Do something useful with it.  In this case,
		        // we're sending the update to a handler which then updates the UI with the new
		        // location.
		    	context.setLocation(location);
		    }

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		
		context.setLocation(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

//		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//		        30000,          // 10-second interval.
//		        50,             // 10 meters.
//		        mListener);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		        10000,          // 10-second interval.
		        50,             // 10 meters.
		        mListener);
			
	}
	
	public void onStop() {
		mLocationManager.removeUpdates(mListener);
	}
}
