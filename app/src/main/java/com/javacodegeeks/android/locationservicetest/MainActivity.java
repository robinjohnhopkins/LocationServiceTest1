package com.javacodegeeks.android.locationservicetest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
	  private TextView latitude;
	  private TextView longitude;
	  private TextView choice;
	  private CheckBox fineAcc;
	  private Button choose;
	  private TextView provText;
	  private LocationManager locationManager;
	  private String provider;
	  private MyLocationListener mylistener;
	  private Criteria criteria;
	  private PostClass postclass;
	  
	/** Called when the activity is first created. */

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_main);
		  latitude = (TextView) findViewById(R.id.lat);
		  longitude = (TextView) findViewById(R.id.lon);
		  provText = (TextView) findViewById(R.id.prov);
		  choice = (TextView) findViewById(R.id.choice);
		  fineAcc = (CheckBox) findViewById(R.id.fineAccuracy);
		  choose = (Button) findViewById(R.id.chooseRadio);
          postclass = null;
		  // Get the location manager
		  locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		  // Define the criteria how to select the location provider
		  criteria = new Criteria();
		  criteria.setAccuracy(Criteria.ACCURACY_COARSE);	//default
		  
		  // user defines the criteria
		  choose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(fineAcc.isChecked()){
					  criteria.setAccuracy(Criteria.ACCURACY_FINE);
					  choice.setText("fine accuracy selected");
				 }else {
					 criteria.setAccuracy(Criteria.ACCURACY_COARSE);
					 choice.setText("coarse accuracy selected");
				 }
				 postclass = new PostClass();
                 postclass.execute();
			}
		  });
		  criteria.setCostAllowed(false); 
		  // get the best provider depending on the criteria
		  provider = locationManager.getBestProvider(criteria, false);
	    
		  // the last known location of this provider
		  Location location = locationManager.getLastKnownLocation(provider);

		  mylistener = new MyLocationListener();
	
		  if (location != null) {
			  mylistener.onLocationChanged(location);
		  } else {
			  // leads to the settings because there is no last known location
			  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			  startActivity(intent);
		  }
		  // location updates: at least 1 meter and 200millsecs change
		  locationManager.requestLocationUpdates(provider, 200, 1, mylistener);
	  }

	  private class MyLocationListener implements LocationListener {
	
		  @Override
		  public void onLocationChanged(Location location) {
			// Initialize the location fields
			  latitude.setText("Latitude: "+String.valueOf(location.getLatitude()));
			  longitude.setText("Longitude: "+String.valueOf(location.getLongitude()));
			  provText.setText(provider + " provider has been selected.");
			  
			  Toast.makeText(MainActivity.this,  "Location changed!",
				        Toast.LENGTH_SHORT).show();
		  }
	
		  @Override
		  public void onStatusChanged(String provider, int status, Bundle extras) {
			  Toast.makeText(MainActivity.this, provider + "'s status changed to "+status +"!",
				        Toast.LENGTH_SHORT).show();
		  }
	
		  @Override
		  public void onProviderEnabled(String provider) {
			  Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
		        Toast.LENGTH_SHORT).show();
	
		  }
	
		  @Override
		  public void onProviderDisabled(String provider) {
			  Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
		        Toast.LENGTH_SHORT).show();
		  }
	  }

	private class PostClass extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {

				final TextView outputView = (TextView) findViewById(R.id.choice);
				URL url = new URL("https://4b4f0026.ngrok.io/postposition");

				HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
				//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
				String urlParameters = "lat=0.123456&lon=52.12345";
				connection.setRequestMethod("POST");
				connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
				connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
				connection.setDoOutput(true);
				DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
				dStream.writeBytes(urlParameters);
				dStream.flush();
				dStream.close();
				int responseCode = connection.getResponseCode();
				final StringBuilder output = new StringBuilder("Request URL " + url);
				output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
				output.append(System.getProperty("line.separator")  + "Response Code " + responseCode);
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = "";
				StringBuilder responseOutput = new StringBuilder();
				while((line = br.readLine()) != null ) {
					responseOutput.append(line);
				}
				br.close();

				output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

				MainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						outputView.setText(output);;

					}
				});
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
