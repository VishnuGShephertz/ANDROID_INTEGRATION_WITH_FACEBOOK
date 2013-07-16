package com.example.facebook.android.integration;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FacebookService {
    	
	private Handler mUIThreadHandler = null;
	private Facebook facebook = new Facebook(Constants.FB_APP_ID);
	public static AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;    
	private Context appContext = null;
    private static FacebookService _instance = null;
    
    public static FacebookService instance(){
    	if(_instance == null){
    		_instance = new FacebookService();
    	}
    	return _instance;
    }
    
    public void setContext(Context context){
    	_instance.appContext = context;
    	_instance.refreshFromContext();
    }
    
    public void signout() throws MalformedURLException, IOException{
    	facebook.logout(appContext);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("display_name", "");
        editor.putString("warp_join_id", "");
        editor.putString("profile_url", "");
        editor.putString("access_token", null);
        editor.putLong("access_expires", 0);
        editor.commit();
    }
    
    private void refreshFromContext(){
    	mPrefs = appContext.getSharedPreferences("MyGamePreferences", android.content.Context.MODE_PRIVATE);
        /*
         * Get existing access_token if any
         */
        
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
            UserContext.accessToken=access_token;
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }   
        if(facebook.isSessionValid()){
        	UserContext.MyDisplayName = mPrefs.getString("display_name", "");
        	UserContext.MyUserName = mPrefs.getString("warp_join_id", "");
        	UserContext.MyPicUrl = mPrefs.getString("profile_url", "");
        }else{
        	UserContext.MyDisplayName = "";
        	UserContext.MyUserName = "";
        	UserContext.MyPicUrl = "";
        }
    }
    
	private FacebookService(){
        mAsyncRunner = new AsyncFacebookRunner(facebook);                
	}
	
	public void authorizeCallback(int requestCode, int resultCode, Intent data)
	{
		facebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	public boolean isFacebookSessionValid(){
		return facebook.isSessionValid();
	}
    
    public void fetchFacebookProfile(final FriendList hostActivity)
    {
    	if(mUIThreadHandler == null){
    		mUIThreadHandler = new Handler();
    	}
    	System.out.println("fetchFacebookProfile");
      
    	facebook.authorize(hostActivity, new
        		String[] {
        		"friends_online_presence"}
        		, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	System.out.println("authorize on complete");
            	if(mPrefs == null){
            		mPrefs = appContext.getSharedPreferences("MyGamePreferences", android.content.Context.MODE_PRIVATE);
            	}
                SharedPreferences.Editor editor = mPrefs.edit();
                UserContext.accessToken= facebook.getAccessToken();
                editor.putString("access_token", facebook.getAccessToken());
                editor.putLong("access_expires", facebook.getAccessExpires());
                editor.commit();
                FacebookService.this.getFacebookProfile(hostActivity);
            }

            @Override
            public void onFacebookError(FacebookError error) {
            	System.out.println("ewqqweweeweewew111111111111111");
            	System.err.println("Facebook onFacebookError");
            	hostActivity.onFbError();
            }

            @Override
            public void onError(DialogError e) {
            	System.out.println("ewqqweweeweewew111111111111122222222222211");
            	System.err.println("Facebook DialogError");
            	hostActivity.onFbError();
            }

            @Override
            public void onCancel() {
            	System.out.println("ccccccccccccccccccc");
            	System.err.println("Facebook onCancel");
            	hostActivity.onFbError();
            }
        });	
    }
    
    public void getFacebookProfile(FriendList callingActivity)
    {
    	System.out.println("euierrewre");
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");    	
    	mAsyncRunner.request("me", params, new FacebookRequestListener(callingActivity));
    	//mAsyncRunner.request("me",  new FacebookRequestListener(callingActivity));
    }
    
    public void getFacebookFriends(FacebookFriendListRequester caller){

    	if(mUIThreadHandler == null){
    		mUIThreadHandler = new Handler();
    	}
        Bundle params = new Bundle();
    	params.putString("method","fql.query");
    	params.putString("query","SELECT name,uid,pic,online_presence FROM user WHERE uid IN ( SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY name" ); 
    	mAsyncRunner.request(params, new FacebookFriendListRequest(caller));
    }
    
    private class FacebookFriendListRequest implements RequestListener{

    	FacebookFriendListRequester callBack;
		public FacebookFriendListRequest(FacebookFriendListRequester caller){
			this.callBack = caller;
		}
		
		@Override
		public void onComplete(String response, Object state) {
			JSONArray jsonArray;
			final ArrayList<JSONObject> onlinefriendInfo = new ArrayList<JSONObject>();
			final ArrayList<JSONObject> idlefriendInfo = new ArrayList<JSONObject>();
			final ArrayList<JSONObject> offlnefriendInfo = new ArrayList<JSONObject>();
			try {
				jsonArray = new JSONArray(response);
				
					for(int i=0;i<jsonArray.length();i++){
						JSONObject friendData = jsonArray.getJSONObject(i);
						String onlineStatus=friendData.getString(Constants.KEYF_PRESENCE).trim();
						   if(onlineStatus.equals(Constants.KEYF_ACTIVE)){
							   onlinefriendInfo.add(friendData);
						   }
						   else if(onlineStatus.endsWith(Constants.KEYF_IDLE)){
							   idlefriendInfo.add(friendData);
						   }
						   else{
							   offlnefriendInfo.add(friendData);
						   }
					}

				System.out.println("dsff");
	            mUIThreadHandler.post(new Runnable() {
	                @Override
	                public void run() {
	                	callBack.onListFetched(onlinefriendInfo, idlefriendInfo,offlnefriendInfo);
	                }
	            });
				
			} catch (JSONException e) {
			callBack.onFbError();
				e.printStackTrace();
			};
		}

		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
	private class FacebookRequestListener implements RequestListener
	{
		FriendList callBack;
		public FacebookRequestListener(FriendList callingActivity){
			this.callBack = callingActivity;
		}
		
		@Override
		public void onComplete(String response, Object state) {
	        JSONObject jsonObject;
	        try {
	            jsonObject = new JSONObject(response);
	            JSONObject picObj = jsonObject.getJSONObject("picture");
	            JSONObject dataObj = picObj.getJSONObject("data");
	            
	            UserContext.MyDisplayName = jsonObject.getString("name");
	            UserContext.MyUserName = jsonObject.getString("id");
	            UserContext.MyPicUrl = dataObj.getString("url");
	            
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("display_name", UserContext.MyDisplayName);
                editor.putString("warp_join_id", UserContext.MyUserName);
                editor.putString("profile_url", UserContext.MyPicUrl);
                editor.commit();
                
	            mUIThreadHandler.post(new Runnable() {
	                @Override
	                public void run() {
	                	callBack.onFacebookProfileRetreived(true);
	                }
	            });

	        } catch (JSONException e) {
	            mUIThreadHandler.post(new Runnable() {
	                @Override
	                public void run() {
	                	callBack.onFacebookProfileRetreived(false);
	                }
	            });
	            e.printStackTrace();
	        }
		}

		public void showFriendsList(){
			
		}
		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public interface FacebookFriendListRequester{
		public void onListFetched(ArrayList<JSONObject> onLinefriends,
				ArrayList<JSONObject> idleLinefriends,ArrayList<JSONObject> offLinefriends) ;
		public void onFbError();
	}
	
	
	
	
}



