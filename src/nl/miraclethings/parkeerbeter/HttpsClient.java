package nl.miraclethings.parkeerbeter;

import android.content.Context;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HttpsClient extends DefaultHttpClient {

	final Context context;

	public HttpsClient(Context context) {
		this.context = context;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		registry.register(new Scheme("https", newSslSocketFactory(), 443));
		return new SingleClientConnManager(getParams(), registry);
	}

	private SSLSocketFactory newSslSocketFactory() {
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(
					R.raw.httpskeystore);
			try {
				trusted.load(in, "secret".toCharArray());
			} finally {
				in.close();
			}
			SSLSocketFactory factory = new SSLSocketFactory(trusted);
			factory.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return factory;

		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
