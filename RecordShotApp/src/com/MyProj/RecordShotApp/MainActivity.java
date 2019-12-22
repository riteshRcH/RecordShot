package com.MyProj.RecordShotApp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stericson.RootTools.*;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle("\t\t\t\t\tRecordShot !!");
        ((View)getWindow().findViewById(android.R.id.title)).setBackgroundColor(Color.rgb(0, 59, 250));
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        installNativeService();
        
        Button screenShotBtn = (Button)findViewById(R.id.screenshotBtn);
        screenShotBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera_pic_screenshot_btn, 0, 0, 0);
        screenShotBtn.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
				startActivity(new Intent("com.MyProj.RecordShotApp.ScreenShot"));
			}
		});
        
        Button screenRecordBtn = (Button)findViewById(R.id.screenRecordBtn);
        screenRecordBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.camcorder_screen_record_btn, 0);
        screenRecordBtn.setOnClickListener(new View.OnClickListener()
        {	
			public void onClick(View v)
			{
				startActivity(new Intent("com.MyProj.RecordShotApp.ScreenRecordActivity"));
			}
		});
        
        Button exitBtn = (Button)findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() 
        {	
			public void onClick(View v)
			{
				finish();
				stopService(new Intent(getBaseContext(), ServiceScreenShot.class));
				if(ServiceScreenRecord.asyncTask!=null)
					ServiceScreenRecord.asyncTask.cancel(true);
				stopService(new Intent(getBaseContext(), ServiceScreenRecord.class));
				NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				nm.cancelAll();
			}
		});
        Button helpBtn = (Button)findViewById(R.id.helpBtn);
        helpBtn.setTextColor(Color.BLUE);
        helpBtn.setOnClickListener(new View.OnClickListener()
        {	
			public void onClick(View v)
			{
				startActivity(new Intent("android.intent.action.HelpActivity"));				
			}
		});
        exitBtn.setTextColor(Color.RED);
        screenRecordBtn.setTextColor(Color.MAGENTA);
        screenShotBtn.setTextColor(Color.BLUE);
    }
    public void installNativeService()
    {
    	if(RootTools.isRootAvailable())
        {
        	if(isNativeRunning())
        	{
        		Toast.makeText(getBaseContext(), "Native Service already running!", Toast.LENGTH_SHORT).show();
        	}else
        	{
        		try
        		{
        			InputStream in = getAssets().open("asl-native");
        			FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/asl-native");
        			
        			byte buffer[] = new byte[1024];
        			while((in.read(buffer))>0)
        				fos.write(buffer);
        			in.close();
        			in=null;
        			fos.flush();
        			fos.close();fos=null;
        			
					RootTools.sendShell(new String[]{"cp "+Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/asl-native data/local/", "system/bin/chmod 0777 data/local/asl-native", "/data/local/asl-native /data/local/asl-native.log"}, 100, 5000);
				}catch (IOException e)
				{
					e.printStackTrace();
				}catch (RootToolsException e)
				{
					e.printStackTrace();
				}catch (TimeoutException e)
				{
					e.printStackTrace();
				}
    			Toast.makeText(getBaseContext(), "Your phone is rooted! :) .. Installed the native service!", Toast.LENGTH_LONG ).show();
        	}
        }else
        	Toast.makeText(getBaseContext(), "Your phone is not rooted so you need to install the service from the computer!", Toast.LENGTH_SHORT).show();
    }
    public boolean isNativeRunning()
	{
		Socket sock=null;
		boolean available=false;
		try
		{
			sock = new Socket();
			sock.connect(new InetSocketAddress(InetAddress.getLocalHost(), 42380), 10);	// short timeout
		}catch(Exception e)
		{
			return false;
		}
		
		if(sock.isConnected())
			available=true;
		else
			available=false;
		
		try
		{
			sock.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return available;
	}
}