package nl.miraclethings.parkeerbeter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

	private ParkeerAPI api;
  
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        TextView t = (TextView)findViewById(R.id.reasonText);

        int reason = R.string.login_needcredentials;
        int r = getIntent().getExtras().getInt("reason");
        if (r > 0) reason = r;
        t.setText(getString(reason));
        
        ((Button)findViewById(R.id.loginButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginActivity.this.login();
			}
		});
        
        api = new ParkeerAPI(getSharedPreferences("ParkeerBeter", MODE_PRIVATE));
        
        ((EditText)findViewById(R.id.loginUsername)).setText(api.getUsername());
        ((EditText)findViewById(R.id.loginPassword)).setText(api.getPassword());
    }

	protected void login() {
		String username = ((EditText)findViewById(R.id.loginUsername)).getText().toString();
		String password = ((EditText)findViewById(R.id.loginPassword)).getText().toString();
		
		api.storeCredentials(username, password);
		
		startActivity(new Intent(this, StartupActivity.class));
		finish();
	}
}


