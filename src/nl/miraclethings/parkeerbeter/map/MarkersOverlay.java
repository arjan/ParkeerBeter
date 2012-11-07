package nl.miraclethings.parkeerbeter.map;

import java.util.List;

import nl.miraclethings.parkeerbeter.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MarkersOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {

	private List<ZoneMarker> mMarkers;
	private Context mContext;
	private GeoPoint mCurrentPosition;
	private Object item;

	public MarkersOverlay(Context context, GeoPoint center, Drawable drawable, List<ZoneMarker> markers) {
		super(boundCenterBottom(drawable));
		mCurrentPosition = center;
		mMarkers = markers;
		mContext = context;
		Log.v("XXX", "Yes, " + mMarkers.size());
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		if (i == 0) {
			OverlayItem item = new OverlayItem(mCurrentPosition, "xx", "xx");
			Log.v("X", "lat: " + mCurrentPosition.getLatitudeE6());
			Log.v("X", "lon: " + mCurrentPosition.getLongitudeE6());
			item.setMarker(boundCenterBottom(mContext.getResources().getDrawable(R.drawable.center)));
			return item;
		}
		ZoneMarker m = mMarkers.get(i-1);
		return new OverlayItem(m.getPoint(), m.zonecode, m.name);
	}

	@Override
	public int size() {
		return mMarkers.size()+1;
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// TODO Auto-generated method stub
		return super.draw(canvas, mapView, false, when);
	}
	
	@Override
	protected boolean onTap(final int index) {
		if (index == 0) return false;
		ZoneMarker m = mMarkers.get(index-1);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(m.zonecode);
		dialog.setMessage(m.name);
		dialog.setPositiveButton(R.string.kies, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				((SelectZoneActivity)mContext).zoneSelected(mMarkers.get(index-1));
			}});
		dialog.show();
		return true;
	}
	
}
