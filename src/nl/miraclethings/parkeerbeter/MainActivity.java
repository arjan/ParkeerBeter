package nl.miraclethings.parkeerbeter;

import java.io.Serializable;
import java.util.List;

import nl.miraclethings.parkeerbeter.api.ParkeerAPI;
import nl.miraclethings.parkeerbeter.api.ParkeerAPIEntry;
import nl.miraclethings.parkeerbeter.api.ParkeerAPIEntryAdapter;
import nl.miraclethings.parkeerbeter.api.ParkeerAPIException;
import nl.miraclethings.parkeerbeter.map.LocationService;
import nl.miraclethings.parkeerbeter.map.ParkeerGeoPoint;
import nl.miraclethings.parkeerbeter.map.SelectZoneActivity;
import nl.miraclethings.parkeerbeter.map.ZoneMarker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class MainActivity extends Activity {

    private static final int REQ_ZONECODE = 0x34;
	private ParkeerAPI api;
    private ProgressDialog pd;
    private boolean needReloadAfterStopCancel = false;
        
    private RecentPreferenceList recentZonecodes;
    private RecentPreferenceList recentKentekens;
	private LocationService mLocationService;
	private ParkeerGeoPoint mLocation;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        Log.v("ParkeerAPI", "MainActivity starting");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        api = new ParkeerAPI(getSharedPreferences("ParkeerBeter", MODE_PRIVATE));

        reloadCurrentParkings();
        
        recentZonecodes = new RecentPreferenceList(getPreferences(MODE_PRIVATE), "zonecode");
        recentKentekens = new RecentPreferenceList(getPreferences(MODE_PRIVATE), "kenteken");
        
        EditText zonecode = ((EditText)findViewById(R.id.main_zonecode));
        zonecode.setText(recentZonecodes.mostRecent());
        zonecode.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				MainActivity.this.selectZonecode();
				return true;
			}
		});
        
        EditText kenteken = ((EditText)findViewById(R.id.main_kenteken));
        kenteken.setText(recentKentekens.mostRecent());
        kenteken.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				MainActivity.this.selectKenteken();
				return true;
			}
		});

        ((Button)findViewById(R.id.kenteken_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				MainActivity.this.selectKenteken();
			}});
        ((Button)findViewById(R.id.zonecode_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new GetZonesTask().execute();
			}});
        
        mLocationService = new LocationService(this);
        
        mLocation = new ParkeerGeoPoint(52373056, 4892222);
    }
    
    @Override
    protected void onStop() {
    	mLocationService.onStop();
    	super.onStop();
    }
    
    protected void showMap(List<ZoneMarker> markers) {
    	Intent i = new Intent(getApplicationContext(), SelectZoneActivity.class);
    	i.putExtra("point", (Serializable)mLocation);
    	i.putExtra("markers", (Serializable)markers);
    	
    	startActivityForResult(i, REQ_ZONECODE);
    } 
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQ_ZONECODE && resultCode == RESULT_OK) {
    		ZoneMarker zone = (ZoneMarker)data.getSerializableExtra("result");
    		Log.v("map", "Yes: " + zone.zonecode);
    		((EditText)findViewById(R.id.main_zonecode)).setText(zone.zonecode);
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    protected void selectKenteken() {
    	final CharSequence[] items = recentKentekens.all();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.kies_kenteken));
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        ((EditText)findViewById(R.id.main_kenteken)).setText(items[item]);
    	        dialog.dismiss();
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }

    protected void selectZonecode() {
    	final CharSequence[] items = recentZonecodes.all();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.kies_zonecode));
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        ((EditText)findViewById(R.id.main_zonecode)).setText(items[item]);
    	        dialog.dismiss();
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }

	public void reloadClick(View b) {
        reloadCurrentParkings();
    }

    private void reloadCurrentParkings() {
        findViewById(R.id.main_loading).setVisibility(View.VISIBLE);
        findViewById(R.id.main_current_parkings).setVisibility(View.INVISIBLE);
        new LoadCurrentParkingsTask().execute(api);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        
        case R.id.main_menu_logout:
            logout(R.string.login_needcredentials);
            return true;
                
        case R.id.main_menu_reload:
            reloadCurrentParkings();
            return true;
                
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
    private void logout(int reason) {
        Intent intent;
        api.storeCredentials(api.getUsername(), "");
        api.storeCookie("", "");
        finish();
        intent = new Intent("nl.miraclethings.parkeerbeter.LOGIN");
        intent.putExtra("reason", reason);
        startActivity(intent);
        finish();
    }
    
    
    public void stopParking(ParkeerAPIEntry entry) {
        
        final ParkeerAPIEntry myEntry = entry;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        
        builder.setMessage("Parkeeractie stoppen voor " + entry.kenteken + ", " + entry.locatie + "?")
            .setCancelable(false)
            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                           
                        MainActivity.this.pd = ProgressDialog.show(MainActivity.this, 
                                                                   getString(R.string.startup_busy), 
                                                                   getString(R.string.main_stopping),  
                                                                   true, false);
                        new StopParkingTask().execute(myEntry);
                    }
                })
            .setNegativeButton("Nee", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (MainActivity.this.needReloadAfterStopCancel) 
                            MainActivity.this.reloadCurrentParkings();
                    }
                });
               
        AlertDialog alert = builder.create();
        alert.show();
    }
    

    public void startParking(View button) {
        String zonecode = ((EditText)findViewById(R.id.main_zonecode)).getText().toString();
        String kenteken = ((EditText)findViewById(R.id.main_kenteken)).getText().toString();
        
        this.pd = ProgressDialog.show(this, 
                                      getString(R.string.startup_busy), 
                                      getString(R.string.main_starting),  
                                      true, false);
        new StartParkingTask(zonecode, kenteken).execute(); 
    }
    
    class LoadCurrentParkingsTask extends AsyncTask<ParkeerAPI, Void, List<ParkeerAPIEntry>>
    {

        @Override
            protected List<ParkeerAPIEntry> doInBackground(ParkeerAPI... params) {
            ParkeerAPI api = params[0];
                        
            try {
                return api.getCurrentParkings();
            } catch (ParkeerAPIException e) {
                MainActivity.this.logout(R.string.login_unexpected);
                return null;
            }
        }
                
        protected void onPostExecute(List<ParkeerAPIEntry> results) {
            MainActivity.this.findViewById(R.id.main_loading).setVisibility(View.INVISIBLE);
            MainActivity.this.findViewById(R.id.main_current_parkings).setVisibility(View.VISIBLE);
            ListView v = (ListView)MainActivity.this.findViewById(R.id.main_current_parkings);
            ParkeerAPIEntry d[] = new ParkeerAPIEntry[results.size()];
            v.setAdapter(new ParkeerAPIEntryAdapter(MainActivity.this, results.toArray(d)));
                
            for (ParkeerAPIEntry e : results) {
                MainActivity.this.makeNotification(e);
            }
        }
    
    }

    class StopParkingTask extends AsyncTask<ParkeerAPIEntry, Void, String>
    {

        @Override
            protected String doInBackground(ParkeerAPIEntry... params) {
            ParkeerAPIEntry entry = params[0];
                        
            try {
                String result = MainActivity.this.api.stop(entry);
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(entry.id, 1);
                return result;
                                
            } catch (ParkeerAPIException e) {
                MainActivity.this.logout(R.string.login_unexpected);
                return null;
            }
        }   
                
        protected void onPostExecute(String message) {
            MainActivity.this.pd.dismiss();
            MainActivity.this.reloadCurrentParkings();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                
            builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    class StartParkingTask extends AsyncTask<Void, Void, String>
    {
        private String zonecode;
        private String kenteken;
                

        public StartParkingTask(String zonecode, String kenteken) {
            this.zonecode = zonecode;
            this.kenteken = kenteken;
        }
         
        @Override
            protected String doInBackground(Void... params) {
            try {
                return MainActivity.this.api.start(zonecode, kenteken);
            } catch (ParkeerAPIException e) {
                MainActivity.this.logout(R.string.login_unexpected);
                return null;
            }
        }

        protected void onPostExecute(String message) {
            MainActivity.this.pd.dismiss();
            MainActivity.this.reloadCurrentParkings();

            // Save as prefs when parked OK
        	recentZonecodes.add(zonecode);
        	recentZonecodes.save();
        	recentKentekens.add(kenteken);
        	recentKentekens.save();
            
            MainActivity.this.getPreferences(MODE_PRIVATE)
                .edit()
                .putString("zonecode", zonecode)
                .putString("kenteken", kenteken)
                .commit();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                
            builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    } 
  
    private void makeNotification(ParkeerAPIEntry e) {
        // Make a notification for this parkeer actie 
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent intent = getPackageManager().getLaunchIntentForPackage("nl.miraclethings.parkeerbeter");
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
            
        PendingIntent i = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String txt = "Parkeeractie actief: " + e.kenteken;
        Notification n = new Notification(R.drawable.ic_status, txt, System.currentTimeMillis());

        n.setLatestEventInfo(this, e.kenteken + " - " + e.locatie, 
                             "Geparkeerd vanaf " + e.gestart, i); 
        n.defaults = Notification.DEFAULT_LIGHTS;
                
        nm.notify(e.id, 1, n);
                
    }

	public void setLocation(Location location) {
		if (location == null) return;
		Log.v("Main", "Got location " + location.toString());
		mLocation = new ParkeerGeoPoint((int)(location.getLatitude()*1e6), (int)(location.getLongitude()*1e6));
	}
	
	
    class GetZonesTask extends AsyncTask<Void, Void, List<ZoneMarker>>
    {
    	@Override
    	protected void onPreExecute() {
            MainActivity.this.pd = ProgressDialog.show(
            		MainActivity.this, 
                    getString(R.string.startup_busy), 
                    getString(R.string.kaart_laden),  
                    true, false);
    	}
    	
    	@Override
    	protected List<ZoneMarker> doInBackground(Void... params) {
    		return api.queryLocations(MainActivity.this, mLocation.getPoint());
    	}
    	
    	@Override
    	protected void onPostExecute(List<ZoneMarker> result) {
    		if (result == null) return;
    		
    		MainActivity.this.pd.dismiss();
    		MainActivity.this.showMap(result);
    	}
    }	
}
