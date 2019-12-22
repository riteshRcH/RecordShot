package com.MyProj.RecordShotApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class ScreenShot extends Activity
{
	final public static int IMG_FORMAT_DIALOG = 0;
	public static boolean PNG_FORMAT = true;
	public static boolean JPG_FORMAT = false;
	public static boolean SHAKE2CAPTURE = false;
	public static boolean APPEND_TIMESTAMP = true;
	public static boolean DELAY_ENABLED = false;
	public static int TIME_DELAY = 5;
	public static boolean FILE_NAME_SPECIFIED = false;
	public static boolean PLAY_SOUND_ENABLED = true;
	public static boolean GAME_MODE_ENABLED = false;
	static String txtInFileNameField = "";
	
	boolean VIEWING_GALLERY = false;
	
	EditText GetDelayEntry, GetDefaultFileNameEntry;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screenshot);
		setTitle("\t\t\t\tScreen Capture !!");
		((View)getWindow().findViewById(android.R.id.title)).setBackgroundColor(Color.rgb(0, 59, 250));
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		Button screenShotImgFormatBtn = (Button)findViewById(R.id.ScreenshotImgFormatBtn);
		screenShotImgFormatBtn.setTextColor(Color.BLUE);
		screenShotImgFormatBtn.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				showDialog(IMG_FORMAT_DIALOG);
			}
		});
		
		Button PreviousShotsViewBtn = (Button)findViewById(R.id.PreviousShotsViewBtn);
		PreviousShotsViewBtn.setTextColor(Color.BLUE);
		PreviousShotsViewBtn.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
				startActivity(i);
				VIEWING_GALLERY=true;
			}
		});
		
		final CheckBox shake2CaptureChkBox = (CheckBox)findViewById(R.id.shake2CaptureChkBox);
		shake2CaptureChkBox.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				//Scope1:
				{
					if(((CheckBox)shake2CaptureChkBox).isChecked())
						SHAKE2CAPTURE=true;
					else
						SHAKE2CAPTURE=false;
				}
				//Scope2:
				{
				if(SHAKE2CAPTURE)
					Toast.makeText(getBaseContext(), "Shake 2 Capture Activated", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getBaseContext(), "Shake 2 Capture De-Activated", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		final CheckBox delaySetChkBox = (CheckBox)findViewById(R.id.delaySetChkBox);
		delaySetChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
				{
					DELAY_ENABLED = true;
					GetDelayEntry.setEnabled(true);
					TIME_DELAY=5;
				}else
				{
					DELAY_ENABLED = false;
					GetDelayEntry.setEnabled(false);
				}
				
				if(DELAY_ENABLED)
				{
					Toast.makeText(getBaseContext(), "Delay ENabled! Please specify a delay value in seconds!", Toast.LENGTH_SHORT).show();
				}else
				{
					TIME_DELAY=0;
					Toast.makeText(getBaseContext(), "Delay DISabled, immediate capture ENabled! ", Toast.LENGTH_SHORT).show();
				}		
			}
		});
		
		final CheckBox playSoundOnCaptureChkBox = (CheckBox)findViewById(R.id.playSoundOnCaptureChkBox);
		playSoundOnCaptureChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
					PLAY_SOUND_ENABLED=true;
				else
					PLAY_SOUND_ENABLED=false;
				
				if(PLAY_SOUND_ENABLED)
					Toast.makeText(getBaseContext(), "Capture Sound Play ENabled", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getBaseContext(), "Capture Sound Play DISabled", Toast.LENGTH_SHORT).show();
				
			}
		});
		
		GetDefaultFileNameEntry = (EditText)findViewById(R.id.GetDefaultFileNameEntry);
		
		GetDelayEntry = (EditText)findViewById(R.id.GetDelayEntry);
		
		final CheckBox appendTimeStampChkBox = (CheckBox)findViewById(R.id.appendTimeStampChkBox);
		appendTimeStampChkBox.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				//Scope1:
				{
					if(((CheckBox)appendTimeStampChkBox).isChecked())
						APPEND_TIMESTAMP=true;
					else
						APPEND_TIMESTAMP=false;
				}
				//Scope2:
				{
					if(APPEND_TIMESTAMP)
						Toast.makeText(getBaseContext(), "TimeStamp appending ENabled", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(getBaseContext(), "TimeStamp appending DISabled", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		final CheckBox gameCaptureModeChkBox = (CheckBox)findViewById(R.id.gameCaptureModeChkBox);
		gameCaptureModeChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
					GAME_MODE_ENABLED = true;
				else
					GAME_MODE_ENABLED=false;
				
				if(GAME_MODE_ENABLED)
				{
					Toast.makeText(getBaseContext(), "Game Capture Mode ENabled; No notifications would be displayed", Toast.LENGTH_SHORT).show();
					PLAY_SOUND_ENABLED=false;
				}
				else
				{
					Toast.makeText(getBaseContext(), "Game Capture Mode DISabled", Toast.LENGTH_SHORT).show();
					PLAY_SOUND_ENABLED=true;
				}
					
			}
		});
		
		Button StartScreenCaptureServiceBtn = (Button)findViewById(R.id.StartScreenCaptureServiceBtn);
		StartScreenCaptureServiceBtn.setTextColor(Color.BLUE);
		StartScreenCaptureServiceBtn.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				moveTaskToBack(true);
				
				/*String ToastText = "";
				if(GetDefaultFileNameEntry.getText().equals(null) || GetDefaultFileNameEntry.getText().length()==0 || GetDefaultFileNameEntry.getText().equals(""))
				{
					ToastText += "No fileName specified; default will be used: Screenshot";
					FILE_NAME_SPECIFIED = false;
				}
				else
				{
					ToastText += "FileName in use: " + GetDefaultFileNameEntry.getText();
					txtInFileNameField = GetDefaultFileNameEntry.getText().toString();
					FILE_NAME_SPECIFIED = true;
				}
				Toast.makeText(getBaseContext(), ToastText, Toast.LENGTH_SHORT).show();*/
				
				startService(new Intent("android.intent.action.ServiceScreenShot"));
			}
		});
	}
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case IMG_FORMAT_DIALOG:
				return new AlertDialog.Builder(this)
				.setTitle("Choose Image Format for ScreenShot")
				.setSingleChoiceItems(R.array.ScreenShotImgFrmt, 0, new DialogInterface.OnClickListener()
				{		
					public void onClick(DialogInterface dialog, int which)
					{
						if(which == 0)
						{
							PNG_FORMAT=true;
							JPG_FORMAT=false;
						}else
						{
							PNG_FORMAT=false;
							JPG_FORMAT=true;
						}
					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{	
					public void onClick(DialogInterface dialog, int which)
					{
						int selectedFrmt = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
						if(selectedFrmt==0)
						{
							PNG_FORMAT = true;
							JPG_FORMAT = false;
						}else
						{
							PNG_FORMAT = false;
							JPG_FORMAT = true;
						}
						if(PNG_FORMAT)
							Toast.makeText(getBaseContext(), "PNG Chosen", Toast.LENGTH_SHORT).show();
						if(JPG_FORMAT)
							Toast.makeText(getBaseContext(), "JPG Chosen", Toast.LENGTH_SHORT).show();
					}
				})
				.create();
		}
		return null;
	}
	protected void onPause()
	{
		super.onPause();
		
		if(!VIEWING_GALLERY)
		{
			String ToastText = "";
			if(GetDefaultFileNameEntry.getText().equals(null) || GetDefaultFileNameEntry.getText().length()==0 || GetDefaultFileNameEntry.getText().equals(""))
			{
				ToastText += "No fileName specified; default will be used: Screenshot";
				FILE_NAME_SPECIFIED = false;
			}else
			{
				ToastText += "FileName in use: " + GetDefaultFileNameEntry.getText();
				txtInFileNameField = GetDefaultFileNameEntry.getText().toString();
				FILE_NAME_SPECIFIED = true;
			}
		
			if(DELAY_ENABLED)
			{
				if(GetDelayEntry.getText().equals("") || GetDelayEntry.getText().equals(null) || GetDelayEntry.getText().length()==0)
					ToastText += "\n\nDelay ENabled of " + TIME_DELAY + " seconds";
				else
				{
					TIME_DELAY = Integer.parseInt(GetDelayEntry.getText().toString());
					ToastText += "\n\nDelay ENabled of " + TIME_DELAY + " seconds";
				}
			}else
				ToastText += "\n\nDelay DISabled! Immediate Capture ENabled!";
		
			if(SHAKE2CAPTURE)
				ToastText += "\nShake detection started!";
		
			Toast.makeText(getBaseContext(), ToastText, Toast.LENGTH_SHORT).show();
		
			startService(new Intent(this, ServiceScreenShot.class));
		}
	}
}
