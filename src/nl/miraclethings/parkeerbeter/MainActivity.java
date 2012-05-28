package nl.miraclethings.parkeerbeter;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends Activity {

	private ParkeerAPI api;
	private ProgressDialog pd;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        api = new ParkeerAPI(getSharedPreferences("ParkeerBeter", MODE_PRIVATE));

        reloadCurrentParkings();
        
    	((EditText)findViewById(R.id.main_zonecode)).setText(getPreferences(MODE_PRIVATE).getString("zonecode", ""));
    	((EditText)findViewById(R.id.main_kenteken)).setText(getPreferences(MODE_PRIVATE).getString("kenteken", ""));
        
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
    }
    
    
    public void stopParking(ParkeerAPIEntry entry) {
        this.pd = ProgressDialog.show(this, 
        		getString(R.string.startup_busy), 
        		getString(R.string.main_stopping),  
        		true, false);
        new StopParkingTask().execute(entry);
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
        }
    
    }

    class StopParkingTask extends AsyncTask<ParkeerAPIEntry, Void, String>
    {

		@Override
		protected String doInBackground(ParkeerAPIEntry... params) {
			ParkeerAPIEntry entry = params[0];
			
			try {
				return MainActivity.this.api.stop(entry);
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
}
