package com.MyProj.RecordShotApp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

public class CloseScreenCaptureService extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(getIntent().getExtras().getInt("stopServiceNotificationID"));
		
		stopService(new Intent(this, ServiceScreenShot.class));
		finish();
	}
}
