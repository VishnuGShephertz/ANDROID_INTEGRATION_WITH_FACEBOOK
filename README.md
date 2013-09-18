Android_Integration_With_Facebook
=================================
# About application

1. This application shows how can we integrate our android application with Face-Book API.
2. This sample shows how can we get our Face-Book friends with their online presence.

# Running Sample

1. First off all download Face-book SDk from [here ] (https://github.com/facebook/facebook-android-sdk).
2. Create a Face-Book application [here ] (https://developers.facebook.com/apps/) to get an Facebook APP ID for your 
   application.
3. You need to create a key hash of your signature from android debug.keystore (for the develop stage).
4. For this First download OpenSSL from [here](http://code.google.com/p/openssl-for-windows/downloads/list) and extract to a folder 
   (in my case, c:\openssl) and follow these steps.

```
A. To create key hash, you need to navigate to your JAVA jdk folder, where the keytool.exe is. (in my case, in windows 
    is: C:\Program Files (x86)\Java\jdk1.6.0_16\bin)
B. This also requires path of your debug.keystore (in my case, in windows is: C:\Users\MyUsername\.android).
C. Now open your command prompt and navigate to the jdk bin folder and run following command.
   keytool -exportcert -alias androiddebugkey -keystore "<debug.keystore path>" > C:\openssl\bin\debug.txt
D. Navigate to "C:\openssl\bin\" using command prompt and run following commands.
   openssl sha1 -binary debug.txt 
   debug_sha.txt
   openssl base64 -in debug_sha.txt 
   debug_base64.txt
E. debug_base64.txt contains the key hash.
F. Copy this key hash to your Facebook Application that you made in step 2.
   (Edit Settings -> Native Android App -> Key Hashes:) and also enable Facebook login and save it.
G. Now your application is authenticated with Face-Book.
```

5.&nbsp; Download the project from [here] (https://github.com/VishnuGShephertz/ANDROID_INTEGRATION_WITH_FACEBOOK/archive/master.zip) and import it in the eclipse.<br/>
6.&nbsp; Import Face-Book SDK project in you eclipse and make it library project.<br/>
7.&nbsp; Add this library project into your sample android application.<br/>
8.&nbsp; Open Constants.java file and change FB_APP_ID variable value with your FB APP ID.<br/>
9.&nbsp; Build your android application and install on your android device.<br/>

# Design Details:

__Authorization With Face-Book:__ To use Face-book API in your android application you have to authorize application.
 as sample I have authorized my application in FacebookService.java file. In this method you have to pass three parameters :
 1. Your host Activity on which you have to get callback from Facebook API.</br>
 2. All Facebook API permissions required for your application in form of String array.(in this sample I request 
     for friends_online_presence).
 3. This is called only first time after installation  with Face-Book.
 4. Once your application is authorize , you can use face-Book API directly.
 

``` 
   public void fetchFacebookProfile(final FriendList hostActivity)
    {
       if(mUIThreadHandler == null){
    		mUIThreadHandler = new Handler();
    	}
    	facebook.authorize(hostActivity, new
        		String[] {
        		"friends_online_presence"}
        		, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
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
            	hostActivity.onFbError();
            }

            @Override
            public void onError(DialogError e) {
            	hostActivity.onFbError();
            }

            @Override
            public void onCancel() {
            	hostActivity.onFbError();
            }
        });	
    }
```

__Authorization callback :__ After authorization step you have to authorize callback as an acknowledgement in onActivityResult method of your host Activity.
This is done in FriendList.java file.

```
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
		if (!UserContext.authorized) {
			FacebookService.instance().authorizeCallback(requestCode,
					resultCode, data);
			UserContext.authorized = true;
		}
	}
```

__Get Face-Book Profile:__ You can get your Facebook profile after getting authorization (in onComplete() describe above).
This is done in FacebookService.java file.

```
   public void getFacebookProfile(FriendList callingActivity)
    {
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");       
        mAsyncRunner.request("me", params, new FacebookRequestListener(callingActivity));
    }
    
```

__Get Face-Book Friends:__ You can get your Facebook friends by writting your own query or can refer following code ,
written in FacebookService.java file.

```
  public void getFacebookFriends(FacebookFriendListRequester caller){
       if(mUIThreadHandler == null){
    		mUIThreadHandler = new Handler();
    	}
        Bundle params = new Bundle();
    	params.putString("method","fql.query");
    	params.putString("query","SELECT name,uid,pic,online_presence FROM user WHERE uid IN 
    	( SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY name" ); 
    	mAsyncRunner.request(params, new FacebookFriendListRequest(caller));
    }
    
```

