package com.MyProj.RecordShotApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

public class ScreenRecordActivity extends Activity
{	
	public final int SET_CAPTURED_AUDIO_EXTENSION_DIALOG = 1;
	
	public static boolean SHAKE_2_START_REC = false;
	public static boolean RECORD_AUDIO = false;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_record);
		setTitle("\t\t\t\tScreen Record !!");
		((View)getWindow().findViewById(android.R.id.title)).setBackgroundColor(Color.rgb(0, 59, 250));
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		final Button StopScreenRecordServiceBtn = (Button)findViewById(R.id.StopScreenRecordServiceBtn);
		final Button StartScreenRecordServiceBtn = (Button)findViewById(R.id.StartScreenRecordServiceBtn);
		final CheckBox shake2StartRecordChkBox = (CheckBox) findViewById(R.id.shake2StartRecordChkBox);
		final CheckBox recordAudioChkBox = (CheckBox)findViewById(R.id.recordAudioChkBox);
		final Spinner setFpsSpinner = (Spinner)findViewById(R.id.setFpsSpinner);
		final Button setCapturedAudioExtensionBtn = (Button)findViewById(R.id.setCapturedAudioExtensionBtn);
		
		StartScreenRecordServiceBtn.setTextColor(Color.BLUE);
		StopScreenRecordServiceBtn.setTextColor(Color.MAGENTA);
		setCapturedAudioExtensionBtn.setTextColor(Color.BLUE);
		
		final String fpsValues[]={"5", "10", "15", "20"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, fpsValues);
		setFpsSpinner.setAdapter(adapter);
		setFpsSpinner.setSelection(3, true);
		setFpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3)
			{
				int indexSelected = arg0.getSelectedItemPosition();
				ServiceScreenRecord.FPS=Integer.parseInt(fpsValues[indexSelected]);
				Toast.makeText(getBaseContext(), "FPS Value has been set to: "+ServiceScreenRecord.FPS, Toast.LENGTH_SHORT).show();
			}
			public void onNothingSelected(AdapterView<?> arg0)
			{
									
			}
		});
		
		StartScreenRecordServiceBtn.setOnClickListener(new View.OnClickListener()
		{	
			public void onClick(View v)
			{
				moveTaskToBack(true);
				startService(new Intent(getBaseContext(), ServiceScreenRecord.class));
				StartScreenRecordServiceBtn.setEnabled(false);
				StopScreenRecordServiceBtn.setEnabled(true);
				shake2StartRecordChkBox.setEnabled(false);
				setFpsSpinner.setEnabled(false);
				recordAudioChkBox.setEnabled(false);
			}
		});
		
		StopScreenRecordServiceBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(ServiceScreenRecord.asyncTask!=null)
					ServiceScreenRecord.asyncTask.cancel(true);
				stopService(new Intent(getBaseContext(), ServiceScreenRecord.class));
				StopScreenRecordServiceBtn.setEnabled(false);
				StartScreenRecordServiceBtn.setEnabled(true);
				shake2StartRecordChkBox.setEnabled(true);
				setFpsSpinner.setEnabled(true);
				recordAudioChkBox.setEnabled(true);
			}
		});
		
		setCapturedAudioExtensionBtn.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showDialog(SET_CAPTURED_AUDIO_EXTENSION_DIALOG);				
			}
		});
		
		shake2StartRecordChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
				{
					SHAKE_2_START_REC=true;
					Toast.makeText(getBaseContext(), "You can now shake your phone to start recording!", Toast.LENGTH_SHORT).show();
				}
				else
					SHAKE_2_START_REC=false;
			}
		});
		recordAudioChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{	
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
					RECORD_AUDIO=true;
				else
					RECORD_AUDIO=false;
				
				if(RECORD_AUDIO)
					Toast.makeText(getBaseContext(), "Microphone ENabled! Your voice will also be recorded!", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getBaseContext(), "Microphone is DISabled", Toast.LENGTH_SHORT).show();
			}
		});
	}
	public Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case SET_CAPTURED_AUDIO_EXTENSION_DIALOG:
				return new AlertDialog.Builder(this)
				.setTitle("Choose File Format for Captured Audio")
				.setSingleChoiceItems(R.array.CapturedAudioFrmt, 0, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						if(which==0)
							ServiceScreenRecord.capturedAudioExtension=".mp3";
						else
							ServiceScreenRecord.capturedAudioExtension=".3gp";
					}
				})
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						int selectedFrmt = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
						if(selectedFrmt==0)
						{
							ServiceScreenRecord.capturedAudioExtension=".mp3";
						}else
						{
							ServiceScreenRecord.capturedAudioExtension=".3gp";
						}
						if(ServiceScreenRecord.capturedAudioExtension.equals(".mp3"))
							Toast.makeText(getBaseContext(), "MP4 Chosen", Toast.LENGTH_SHORT).show();
						if(ServiceScreenRecord.capturedAudioExtension.equals(".3gp"))
							Toast.makeText(getBaseContext(), "3GP Chosen", Toast.LENGTH_SHORT).show();
					}
				})
				.create();
		}
		return null;
	}
}
