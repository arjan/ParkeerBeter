package nl.miraclethings.parkeerbeter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.SharedPreferences;
import android.util.Log;

public class ParkeerAPI {
	private static final String BASE_URL = "https://sas.smsparking.nl/";
	private SharedPreferences pref;

	public ParkeerAPI(SharedPreferences pref) {
		this.pref = pref;
	}
	
	public String getUsername() {
		return pref.getString("username", "");
	}

	public String getPassword() {
		return pref.getString("password", "");
	}

	public String getCookie() {
		return pref.getString("cookie", "");
	}

	public String getCookieName() {
		return pref.getString("cookie_name", "");
	}
	
	public void storeCredentials(String username, String password) {
		pref.edit()
			.putString("username", username)
			.putString("password", password)
			.commit();
	}
	
	public void storeCookie(String name, String value) {
		pref.edit()
			.putString("cookie_name", name)
			.putString("cookie", value)
			.commit();
	}

	public int checkLogin()
	{
		if (getUsername().length() == 0) {
			Log.v("ParkeerAPI", "No credentials");
			return R.string.login_needcredentials;
		}
 
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();		
		params.add(new BasicNameValuePair("username", getUsername()));
		params.add(new BasicNameValuePair("password", getPassword()));
		params.add(new BasicNameValuePair("lang", "nl_NL"));
		
		HttpPost request = new HttpPost(BASE_URL + "logmein.pl");
		
        request.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        request.getParams().setParameter("http.protocol.handle-redirects",false);

		try {
			request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Log.e("ParkeerAPI", e.getMessage());
			return R.string.login_unexpected;
		}
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;

		try {
			response = client.execute(request);
		} catch (Exception e) {
			Log.e("ParkeerAPI", e.getMessage());
			return R.string.login_unexpected;
		}
  
		if (response.getStatusLine().getStatusCode() != 302) {
			Log.e("ParkeerAPI", "Login failure, HTTP " + response.getStatusLine().getStatusCode());
			return R.string.login_fail;
		}
		
		// All OK, store the cookie for later requests.
		List<Cookie> cookies = client.getCookieStore().getCookies();
		storeCookie(cookies.get(0).getName(), cookies.get(0).getValue());
		
		return 0;
	}
	
	private Document getRequest(String urlpart) throws ParkeerAPIException {
		return getRequest(urlpart, false);
	}
	
	private Document getRequest(String urlpart, boolean retry) throws ParkeerAPIException {
		Document doc;
		try {
			doc = Jsoup.connect(BASE_URL + urlpart)
				.cookie(getCookieName(), getCookie())
				.get();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ParkeerAPIException("I/O error");
		}
		if (!retry && doc.toString().contains("onload=\"sessionend")) {
			// renew
			if (checkLogin() != 0) {
				throw new ParkeerAPIException("Authorization failed");
			}
			return getRequest(urlpart, true);
		}
		
		return doc;
	}
	
	public List<ParkeerAPIEntry> getCurrentParkings() throws ParkeerAPIException
	{
		List<ParkeerAPIEntry> results = new ArrayList<ParkeerAPIEntry>();
		
		Document doc = getRequest("myparkings.pl");
		
		Elements emptyCheck = doc.select("#main tr h2");
		if (emptyCheck.size() == 1) {
			Log.v("ParkeerAPI", "Niets actief");
			return results;
		}
		
		Elements trs = doc.select("#main tr");
		Log.v("ParkeerAPI", "Nr of TRs: " + trs.size());
		
		Elements datas = doc.select("#main tr h1.acpark");
		Log.v("ParkeerAPI", "Nr of H1s: " + datas.size());
		
		for (int i=0; i<datas.size(); i+= 7) {
			ParkeerAPIEntry entry = new ParkeerAPIEntry(
					datas.get(i+0).text(),
					datas.get(i+1).text(),
					datas.get(i+2).text(),
					datas.get(i+3).text(),
					datas.get(i+4).text(),
					datas.get(i+5).text(),
					datas.get(i+6).text()
					);
			results.add(entry);
		}
		
		Log.v("ParkeerAPI", "Nr of entries: " + results.size());
		return results;
	}
}
