package com.MyProj.RecordShotApp;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

public class CloseScreenRecordService extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ty);
		
		NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(getIntent().getExtras().getInt("stopRecordServiceNotificationID"));
		ServiceScreenRecord.RECORDING=false;
		//stopService(new Intent(this, ServiceScreenRecord.class));
		finish();
	}
}
