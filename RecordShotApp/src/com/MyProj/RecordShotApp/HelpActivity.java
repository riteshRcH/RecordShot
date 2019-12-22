package com.MyProj.RecordShotApp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends Activity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ty);
		setTitle("\t\tHelp");
		
		TextView helpContents = (TextView)findViewById(R.id.HelpContents);
		helpContents.setTextColor(Color.WHITE);
		helpContents.setText("\t\tTO CAPTURE\n\n1.Click on Screen Capture \n2. Customize various options such as play sound, filename etc \n3.Press the home button or tap Start Service Button present in the end \n4.Either shake or click the system overlay button to capture \n5.View notifications\n6.Finish\n\n\t\tTO RECORD\n\n1.Click on Screen Recording Button \n2.Customize Options such capture audio, its format, FPS value, shake to start capture etc \n3.Click on start Service Button \n4.Move to screen from where you want to start recording \n5.Touch the system overlay button or shake to start recording \n6.To stop, go into application and click \"Stop Service\" \n7.Finish\n\n\t\tTHANK YOU! ");
	}
}
