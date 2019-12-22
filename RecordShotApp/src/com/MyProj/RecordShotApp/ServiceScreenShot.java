package com.MyProj.RecordShotApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.UUID;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class ServiceScreenShot extends Service
{
	//Port number used to communicate with native process.
	private static final int PORT = 42380;
	private static String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static String SCREENSHOT_PATH = SD_CARD_PATH + "/RecordShot/ScreenShots";
	private boolean SHAKE_N_DELAYED_COMBO = false;
	private int stopServiceNotificationID = 1;
	
	private SensorManager sensorManager;
	private ShakeEventListener sensorListener;
	
	ImageButton screenCaptureBtn;
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class DelayedBgCapture extends AsyncTask<Long, Integer, Long>
    {
    	protected void onPreExecute()
    	{
    		Toast.makeText(getBaseContext(), "Your screenshot would be taken in "+ScreenShot.TIME_DELAY+" seconds!", Toast.LENGTH_SHORT).show();
    	}
    	protected Long doInBackground(Long... delay)
    	{
    		long WAIT_FOR_SECS = delay[0];
    		try
    		{
				Thread.sleep(WAIT_FOR_SECS*1000);
				
			}catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    		
			return null;
    	}
    	protected void onPostExecute(Long result)
    	{
    		//Toast.makeText(getBaseContext(), "Cpatured !!!!!!!!", Toast.LENGTH_SHORT).show();
    		try
			{	
				if(isNativeRunning())
				{	
					String file = takeScreenshot();
					if(file == null)
					{
						vibrateForError();
						Toast.makeText(getBaseContext(), "ScreenShot error", Toast.LENGTH_SHORT).show();
						
						playSound(getAssets().openFd("camera_error_sound.mp3"));
					}
					else
					{
						vibrateForSuccessfulOperation();
						Toast.makeText(getBaseContext(), "Screenshot taken successfully!! Saved at "+ file, Toast.LENGTH_LONG).show();
						
						playSound(getAssets().openFd("camera_capture_sound.mp3"));
					}
				}else
				{
					Toast.makeText(getBaseContext(), "Native service not found", Toast.LENGTH_SHORT).show();
					vibrateForError();
					
					playSound(getAssets().openFd("camera_error_sound.mp3"));
				}
			}catch (IOException e)
			{
				e.printStackTrace();
			}
    		dispOverlayBtn();
    		SHAKE_N_DELAYED_COMBO=false;
    	}
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        Log.i("Service", "Service Created");
        
        Intent i = new Intent(this, CloseScreenCaptureService.class);
        i.putExtra("stopServiceNotificationID", stopServiceNotificationID);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, 0);
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification(R.drawable.stop_screen_capture_service_icon, "Stop Service", System.currentTimeMillis());
        notif.setLatestEventInfo(this, "Stop Service", "Stop the Background Screen Capture Service!", pendingIntent);
        nm.notify(stopServiceNotificationID, notif);
        
        sensorListener = new ShakeEventListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(ScreenShot.SHAKE2CAPTURE)
        	sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

        sensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener()
        {
          public void onShake()
          {
        	  if(ScreenShot.SHAKE2CAPTURE)
        	  {
        		  if(SHAKE_N_DELAYED_COMBO)
        		  {
        			  if(!ScreenShot.GAME_MODE_ENABLED)
        			  Toast.makeText(getBaseContext(), "You have already shaked; wait for the delay to get completed!", Toast.LENGTH_SHORT).show();
        		  }else
        		  {
        			  if(ScreenShot.DELAY_ENABLED)
          			{
        				  SHAKE_N_DELAYED_COMBO = true;
          				((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenCaptureBtn);
          				
          				new DelayedBgCapture().execute((long)ScreenShot.TIME_DELAY);
          				
          			}else
          			{
          				((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenCaptureBtn);
          				//remove Button so that it isnt visible at time of screenshot
          				try
          				{	
          					if(isNativeRunning())
          					{	
          						String file = takeScreenshot();
          						if(file == null)
          						{
          							vibrateForError();
          							if(!ScreenShot.GAME_MODE_ENABLED)
          								Toast.makeText(getBaseContext(), "ScreenShot error", Toast.LENGTH_SHORT).show();
          							
          							playSound(getAssets().openFd("camera_error_sound.mp3"));
          						}
          						else
          						{
          							vibrateForSuccessfulOperation();
          							if(!ScreenShot.GAME_MODE_ENABLED)
          								Toast.makeText(getBaseContext(), "Screenshot taken successfully!! Saved at "+ file, Toast.LENGTH_LONG).show();
          							
          							playSound(getAssets().openFd("camera_capture_sound.mp3"));
          						}
          					}else
          					{
          						Toast.makeText(getBaseContext(), "Native service not found", Toast.LENGTH_SHORT).show();
          						vibrateForError();
          						
          						playSound(getAssets().openFd("camera_error_sound.mp3"));
          					}
  						}catch (IOException e)
  						{
  							e.printStackTrace();
  						}
          				dispOverlayBtn();
          			}
        		  }
        		  
        	  }
          }
        });
        
        screenCaptureBtn = new ImageButton(this);
        screenCaptureBtn.setImageResource(R.drawable.pic_4_screen_capture);
        screenCaptureBtn.setBackgroundColor(Color.TRANSPARENT);
        screenCaptureBtn.setOnTouchListener(new OnTouchListener()
        {
        	public boolean onTouch(View v, MotionEvent event)
        	{
        		float touchedAtX = event.getX();
        		float touchedAtY = event.getY();
        		
        		Rect btnSize = new Rect();
        		screenCaptureBtn.getDrawingRect(btnSize);
        		
        		if(btnSize.contains((int)touchedAtX, (int)touchedAtY))
        		{
        			if(ScreenShot.DELAY_ENABLED)
        			{	
        				((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenCaptureBtn);
        				
        				new DelayedBgCapture().execute((long)ScreenShot.TIME_DELAY);
        				
        			}else
        			{
        				((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenCaptureBtn);
        				//remove Button so that it isnt visible at time of screenshot
        				try
        				{	
        					if(isNativeRunning())
        					{	
        						String file = takeScreenshot();
        						if(file == null)
        						{
        							vibrateForError();
        							if(!ScreenShot.GAME_MODE_ENABLED)
        								Toast.makeText(getBaseContext(), "ScreenShot error", Toast.LENGTH_SHORT).show();
        							
        							playSound(getAssets().openFd("camera_error_sound.mp3"));
        						}
        						else
        						{
        							vibrateForSuccessfulOperation();
        							if(!ScreenShot.GAME_MODE_ENABLED)
        								Toast.makeText(getBaseContext(), "Screenshot taken successfully!! Saved at "+ file, Toast.LENGTH_LONG).show();
        							
        							playSound(getAssets().openFd("camera_capture_sound.mp3"));
        						}
        					}else
        					{
        						Toast.makeText(getBaseContext(), "Native service not found", Toast.LENGTH_SHORT).show();
        						vibrateForError();
        						
        						playSound(getAssets().openFd("camera_error_sound.mp3"));
        					}
						}catch (IOException e)
						{
							e.printStackTrace();
						}
        				dispOverlayBtn();
        			}
        		}
        		return false;
        	}
        });
        dispOverlayBtn();
    }
    void vibrateForError()
    {
    	((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{0, 200, 200, 200, 200}, -1);
    }
    void vibrateForSuccessfulOperation()
    {
    	((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
    }
    void playSound(AssetFileDescriptor afd)throws IOException
    {
    	if(ScreenShot.PLAY_SOUND_ENABLED)
    	{
    		MediaPlayer mp = new MediaPlayer();
    		mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
    		mp.prepare();
    		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
    		{	
    			public void onPrepared(MediaPlayer mp)
    			{
    				mp.start();
    		}
    		});
    	}
    }
    void dispOverlayBtn()
    {
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(screenCaptureBtn, params);
    }
    
    //Checks whether the internal native application is running
    public boolean isNativeRunning()
	{
		Socket sock=null;
		boolean available=false;
		try
		{
			sock = new Socket();
			sock.connect(new InetSocketAddress(InetAddress.getLocalHost(), PORT), 10);	// short timeout
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
    //Internal class describing a screenshot.
    class Screenshot
    {
		public Buffer pixels;
		public int width;
		public int height;
		public int bpp;
		
		public boolean isValid()
		{
			if (pixels == null || pixels.capacity() == 0 || pixels.limit() == 0)
				return false;
			if (width <= 0 || height <= 0)
				return false;
			return true;
		}
	}
    //Determines whether the phone's screen is rotated
	private int getScreenRotation() 
	{
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display disp = wm.getDefaultDisplay();
		
		// check whether we operate under Android 2.2 or later
		try {
				Class<?> displayClass = disp.getClass();
				Method getRotation = displayClass.getMethod("getRotation");
				int rot = ((Integer)getRotation.invoke(disp)).intValue();
			
				switch (rot)
				{
					case Surface.ROTATION_0:	return 0;
					case Surface.ROTATION_90:	return 90;
					case Surface.ROTATION_180:	return 180;
					case Surface.ROTATION_270:	return 270;
					default:					return 0;
				}
			}catch (NoSuchMethodException e)
			{
				// no getRotation() method
				int orientation = disp.getOrientation();

				// Sometimes you may get undefined orientation Value is 0
				// simple logic solves the problem compare the screen
				// X,Y Co-ordinates and determine the Orientation in such cases
				if(orientation==Configuration.ORIENTATION_UNDEFINED)
				{
					Configuration config = getResources().getConfiguration();
					orientation = config.orientation;

					if(orientation==Configuration.ORIENTATION_UNDEFINED)
					{
						//if height and widht of screen are equal then it is square orientation
						if(disp.getWidth()==disp.getHeight())
							orientation = Configuration.ORIENTATION_SQUARE;
						else
						{
							//if widht is less than height than it is portrait
						if(disp.getWidth() < disp.getHeight())
							orientation = Configuration.ORIENTATION_PORTRAIT;
						else
							orientation = Configuration.ORIENTATION_LANDSCAPE;
						// if it is not any of the above it will defineitly be landscape
						}
					}
				}
			
				return orientation == 1 ? 0 : 90; // 1 for portrait, 2 for landscape
			}catch (Exception e)
			{
				return 0; // some default
			}
	}
	//Communicates with the native service and retrieves a screenshot from it as a 2D array of bytes.
	private Screenshot retreiveRawScreenshot() throws Exception 
	{
		SocketChannel socket = null;
		try
		{
			// connect to native application			
			//We use SocketChannel,because is more convenience and fast
			socket = SocketChannel.open(new InetSocketAddress("localhost", PORT));
			socket.configureBlocking(false);
			
			//Send Command to take screenshot
			ByteBuffer cmdBuffer = ByteBuffer.wrap("SCREEN".getBytes("ASCII"));
			socket.write(cmdBuffer);
		
			//build a buffer to save the info of screenshot
			//3 parts,width height bpp
			byte[] info = new byte[3 + 3 + 2 + 2];//3 bytes width + 1 byte space + 3 bytes height + 1 byte space + 2 bytes bpp
			ByteBuffer infoBuffer = ByteBuffer.wrap(info);
			
			//we must make sure all the data have been read
			while(infoBuffer.position() != infoBuffer.limit())
				socket.read(infoBuffer);
			
			//we must read one more byte,because after this byte,we will read the image byte
			socket.read(ByteBuffer.wrap(new byte[1]));
			
			//set the position to zero that we can read it.
			infoBuffer.position(0);
			
			StringBuffer sb = new StringBuffer();
			for(int i = 0;i < (3 + 3 + 2 + 2);i++)
				sb.append((char)infoBuffer.get());
			
			String[] screenData = sb.toString().split(" ");
			if (screenData.length >= 3)
			{
				Screenshot ss 	= new Screenshot();
				ss.width 		= Integer.parseInt(screenData[0]);
				ss.height 		= Integer.parseInt(screenData[1]);
				ss.bpp = Integer.parseInt(screenData[2]);
				// Retrieve the screenshot via a faster method
				ByteBuffer bytes = ByteBuffer.allocate (ss.width * ss.height * ss.bpp / 8);
				
				while(bytes.position() != bytes.limit())
				{
					//in the cycle,we must make sure all the image data have been read,maybe sometime the socket will delay a bit time and return some invalid bytes.
					socket.read(bytes);				// reading all at once for speed
				}
				
				bytes.position(0);					// reset position to the beginning of ByteBuffer
				ss.pixels = bytes;
				
				return ss;
			}
		}catch (Exception e)
		{
			throw new Exception(e);
		}
		finally
		{
			socket.close();
		}
		return null;
	}
	//Saves given array of bytes into image file in the PNG format.
	private void writeImageFile(Screenshot ss, String file)
	{
		if (ss == null || !ss.isValid())
			throw new IllegalArgumentException();
		if (file == null || file.length() == 0)
			throw new IllegalArgumentException();
		
		// resolve screenshot's BPP to actual bitmap pixel format
		Bitmap.Config pf;
		switch (ss.bpp)
		{
			case 16:
				pf = Config.RGB_565;
				break;
			case 32:
				pf = Config.ARGB_8888;
				break;
			default:
				pf = Config.RGB_565;
				break;
		}

		// create appropriate bitmap and fill it wit data
		Bitmap bmp = Bitmap.createBitmap(ss.width, ss.height, pf);
		bmp.copyPixelsFromBuffer(ss.pixels);
		
		// handle the screen rotation
		int rot = getScreenRotation();
		if (rot != 0)
		{
			Matrix matrix = new Matrix();
			matrix.postRotate(-rot);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}

		// save it in PNG format
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new InvalidParameterException();
		}
		if(ScreenShot.PNG_FORMAT)
			bmp.compress(CompressFormat.PNG, 100, fos);
		else if(ScreenShot.JPG_FORMAT)
			bmp.compress(CompressFormat.JPEG, 100, fos);
		else
			bmp.compress(CompressFormat.PNG, 100, fos);
	}
	
	//Takes screenshot and saves to a file.
	public String takeScreenshot() throws IOException
	{
		// make sure the path to save screens exists
		File screenShotPath = new File(SCREENSHOT_PATH);
		screenShotPath.mkdirs();
			
		// construct screenshot file name
		StringBuffer sb = new StringBuffer();
		sb.append(screenShotPath.getAbsolutePath() + "/");
		if(ScreenShot.FILE_NAME_SPECIFIED)
			sb.append(ScreenShot.txtInFileNameField);
		else
			sb.append("ScreenShot");
		
		if(ScreenShot.APPEND_TIMESTAMP)
			sb.append(new java.util.Date().toString().replace(' ', '_').replace(':', '_'));
		else
			sb.append(Math.abs(UUID.randomUUID().hashCode()));	// hash code of UUID should be quite random yet short
		
		if(ScreenShot.JPG_FORMAT)
			sb.append(".jpeg");
		else if(ScreenShot.PNG_FORMAT)
			sb.append(".png");
		else
			sb.append(".png");
		
		String file = sb.toString();

		// fetch the screen and save it
		Screenshot ss = null;
		try {
			ss = retreiveRawScreenshot();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		writeImageFile(ss, file);

		return file;
	}

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(screenCaptureBtn != null)
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(screenCaptureBtn);
            screenCaptureBtn = null;
        }
        if(ScreenShot.SHAKE2CAPTURE)
        	sensorManager.unregisterListener(sensorListener);
    }    
}
