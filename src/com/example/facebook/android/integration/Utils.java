package com.example.facebook.android.integration;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.ImageView;

public class Utils {

	public static boolean isAuthenticated() {
		return (FacebookService.instance().isFacebookSessionValid() && UserContext.MyUserName
				.length() > 0);
	}

	public static void loadImageFromUrl(final ImageView view, final String url) {
		final Handler callerThreadHandler = new Handler();
		new Thread() {

			@Override
			public void run() {

				final Bitmap bitmap = loadBitmap(url);

				callerThreadHandler.post(new Runnable() {

					@Override
					public void run() {

						if (bitmap != null) {

							view.setImageBitmap(bitmap);

						}

					}

				});

			}

		}.start();

	}

	public static Bitmap loadBitmap(String url) {

		Bitmap bitmap = null;

		try {

			InputStream in = new java.net.URL(url).openStream();
			bitmap = BitmapFactory.decodeStream(in);
			in.close();

		}

		catch (Exception e) {

		}

		return bitmap;

	}

	/*
	 * This method is used to check availability of network connection in
	 * android device uses CONNECTIVITY_SERVICE of android device to get desired
	 * network internet connection
	 * 
	 * @return status of availability of internet connection in true or false
	 * manner
	 */
	public static boolean haveNetworkConnection(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo[] netInfo = cm.getAllNetworkInfo();
			for (NetworkInfo ni : netInfo) {
				if (ni.getTypeName().equalsIgnoreCase("WIFI"))
					if (ni.isConnected())
						haveConnectedWifi = true;
				if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
					if (ni.isConnected())
						haveConnectedMobile = true;
			}

		} catch (Exception e) {

		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}
