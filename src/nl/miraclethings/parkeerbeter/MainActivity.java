package nl.miraclethings.parkeerbeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ParkeerAPI api;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        api = new ParkeerAPI(getSharedPreferences("ParkeerBeter", MODE_PRIVATE));
        
        try {
			api.getCurrentParkings();
		} catch (ParkeerAPIException e) {
			logout(R.string.login_fail);
		}
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
}


