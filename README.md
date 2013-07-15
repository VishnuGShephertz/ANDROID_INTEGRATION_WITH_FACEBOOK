ANDROID_INTEGRATION_WITH_FACEBOOK
=================================
# About application

1. This application shows how can we integrate our android application with FaceBook API.
2. This application simply shows how can we get facebook all facebook friends with their online presence.

# Running Sample

1. First off all download Face-book SDk from [here ] (https://github.com/facebook/facebook-android-sdk).
2. Create an application [here ] (https://developers.facebook.com/apps/) to get an Facebook APP ID for your 
   application.
3. You need to create the key hash value of your signature and your android debugkeystore (for the develop stage).
4. For this First download OpenSSL from [here](http://code.google.com/p/openssl-for-windows/downloads/list) and extract to a folder 
   (in my case, c:\openssl) and follow these steps.

```
A. To create key hash values, you need to navigate to your JAVA jdk folder, where the keytool.exe is. (In my case, in windows 
    is: C:\Program Files (x86)\Java\jdk1.6.0_16\bin)
B. This also requires path of your debug.keystore (In my case, in windows is: C:\Users\MyUsername\.android).
C. Now open your command prompt and navigate to the jdk bin folder and run following command.
   keytool -exportcert -alias androiddebugkey -keystore "<debug.keystore path>" > C:\openssl\bin\debug.txt
D. Navigate to "C:\openssl\bin\" using command prompt and run following commands.
   openssl sha1 -binary debug.txt > debug_sha.txt
   openssl base64 -in debug_sha.txt > debug_base64.txt
E. The debug_base64.txt contains the key hash value,
F. Copy this key hash value to your Facebook Application that you made in step 2.
   (Edit Settings -> Native Android App -> Key Hashes:) and also enable Facebook Login and save it.
G. Now your application is authenticated with Face-book.
```

5.&nbsp; Download the project from [here] (https://github.com/VishnuGShephertz/ANDROID_INTEGRATION_WITH_FACEBOOK/archive/master.zip) and import it in the eclipse.<br/>
6.&nbsp; Import Face-Book SDK project in you eclipse and make it as a library project.<br/>
7.&nbsp; Add this library project ion your sample android application.<br/>
8.&nbsp; Open Constants.java file and change FB_APP_ID with your fb APP ID.<br/>
9.&nbsp; Build your android application and install on your android device.<br/>
