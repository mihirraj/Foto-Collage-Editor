package com.wisesharksoftware.promo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.AsyncTask;

class RequestTask extends AsyncTask<String, String, String> {
	Response response;
	public RequestTask(Response response){
		this.response = response;
	}
	
	@Override
	protected String doInBackground(String... params) {
		String response = null;
		try {
			if (isInternetReachable()) {
				DefaultHttpClient hc = new DefaultHttpClient();
				ResponseHandler<String> res = new BasicResponseHandler();
				HttpPost postMethod = new HttpPost(params[0]);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);
				nameValuePairs
						.add(new BasicNameValuePair("package", params[1]));
				postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = hc.execute(postMethod, res);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
		
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (response != null){
			response.onResponse(result);
		}
	}

	public interface Response{
		public void onResponse(String result);
	}
	
	public boolean isInternetReachable() {
		InetAddress ia;
		boolean reachable = false;
		try {
			ia = InetAddress.getByName( "wisesharksoftware.com");
			reachable = ia.isReachable(1000);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return reachable;
	}
}