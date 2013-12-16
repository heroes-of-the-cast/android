package com.timvdalen.hotc.apihandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.timvdalen.hotc.apihandler.error.APIException;
import com.timvdalen.hotc.apihandler.error.CharExistsException;
import com.timvdalen.hotc.apihandler.error.LoginException;
import com.timvdalen.hotc.apihandler.error.ReservedNameException;
import com.timvdalen.hotc.apihandler.error.UserExistsException;
import com.timvdalen.hotc.apihandler.error.UserNotFoundException;
import com.timvdalen.hotc.apihandler.returntypes.APIReturnError;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Manages API calls
 * 
 */
public class APIHandler{
	private static final String API_BASEURL = "http://timvdalen.webfactional.com/hotc/api/";
	
	public static Boolean userexists(String username, Context context) throws NoNetworkException, IOException, APIException{
		String ret = execute(Method.GET, "user/" + username + "/exists", context);
		return (new Gson()).fromJson(ret, Boolean.class);
	}

	private static String execute(Method method, String endpoint, Context context) throws NoNetworkException, IOException, APIException{
		InputStream is = null;

		String ret;

		//Check for a network connection
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo != null && networkInfo.isConnected()){
			//Make the actual request
			try{
				URL url = new URL(API_BASEURL + endpoint);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod(method.toString());
				conn.setDoInput(true);

				conn.connect();
				int response = conn.getResponseCode();
				//TODO: Why not use this to figure out if there's an error?
				is = conn.getInputStream();

				ret = readIs(is);
			}finally{
				if(is != null){
					is.close();
				}
			}

		}else{
			throw new NoNetworkException();
		}
		
		//Check for errors
		//This is weird, using Exceptions for the main flow, I should really do this differently
		try{
			Gson gson = new Gson();
			APIReturnError error = gson.fromJson(ret, APIReturnError.class);
			throw getAPIError(error);
		}catch(JsonSyntaxException e){
			
		}

		return ret;
	}

	private static String readIs(InputStream stream) throws IOException{
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		StringBuilder total = new StringBuilder();
		String line;
		while((line = r.readLine()) != null){
			total.append(line);
		}
		return total.toString();
	}

	private static APIException getAPIError(APIReturnError e){
		switch(e.error.code){
		case 1:
			return new UserExistsException(e.error.message);
		case 11:
			return new ReservedNameException(e.error.message);
		case 12:
			return new UserNotFoundException(e.error.message);
		case 13:
			return new LoginException(e.error.message);
		case 2:
			return new CharExistsException(e.error.message);
		default:
			return null;
		}
	}
	
	private enum Method{
		GET, POST
	}
}
