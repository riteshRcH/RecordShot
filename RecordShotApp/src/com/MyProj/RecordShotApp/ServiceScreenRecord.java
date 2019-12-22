package com.MyProj.RecordShotApp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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

import com.MyProj.RecordShotApp.ServiceScreenShot.Screenshot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class ServiceScreenRecord extends Service
{
	private static final int PORT = 42380;
	private static String SD_CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	private static String SCREEN_VID_RECORD_PATH = SD_CARD_PATH + "/RecordShot/ScreenVideos";
	private static String RECORDED_AUDIO_PATH = SD_CARD_PATH + "RecordShot/ScreenVideos";
	String capturedAudioName = "";
	static String capturedAudioExtension = ".mp3";
	MediaRecorder audioRecorder;
	static int FPS = 20;
	public static boolean RECORDING = false;
	long count=0;
	private int stopRecordServiceNotificationID = 2;
	private int vidCnt = 0;
	String vidName = "Vid" + vidCnt;
	
	public static boolean SHAKED = false;
	
	private SensorManager sensorManager;
	private ShakeEventListener sensorListener;
	
	static BgRecord asyncTask;
	
	ImageButton screenRecordBtn;
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	class BgRecord extends AsyncTask<Integer, Integer, Long>
	{
		protected void onPreExecute()
		{
			if(!isNativeRunning())
			{
				Toast.makeText(getBaseContext(), "Native Service Not Found", Toast.LENGTH_SHORT).show();
				this.cancel(true);
			}else
				Toast.makeText(getBaseContext(), "Screen Recording Started! To stop go into App > Back Button > Exit \n\n OR Resume App > Stop Service!", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected Long doInBackground(Integer... fps)
		{
			int fpsValue = fps[0];
			if(ScreenRecordActivity.RECORD_AUDIO)
			{
				audioRecorder = new MediaRecorder();
				audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				audioRecorder.setOutputFile(RECORDED_AUDIO_PATH+"/"+capturedAudioName);
				audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				try
				{
					audioRecorder.prepare();
				}catch(IOException e)
				{
					Toast.makeText(getBaseContext(), "Error during recording audio", Toast.LENGTH_SHORT).show();
				}
				audioRecorder.start();
			}
			try
			{
				while(true)
				{
					//Thread.sleep(200);
					Thread.sleep(1000/fpsValue);
					takeScreenshot();
					count++;
				}
			}catch(InterruptedException e)
			{
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		protected void onCancelled()
		{
		stopSelf();
		if(isNativeRunning())
			Toast.makeText(getBaseContext(), "Done Recording!", Toast.LENGTH_SHORT).show();
		dispOverlayBtn();
		SHAKED=false;
			if(ScreenRecordActivity.RECORD_AUDIO)
			{
				audioRecorder.stop();
				audioRecorder.release();
			}
		}
		protected void onPostExecute(Long result)
		{
			Toast.makeText(getBaseContext(), "Completed Recording!", Toast.LENGTH_SHORT).show();
			dispOverlayBtn();
			SHAKED=false;
			if(ScreenRecordActivity.RECORD_AUDIO)
			{
				audioRecorder.stop();
				audioRecorder.release();
			}
		}
	}
	private void handleFilesNFolders()
	{
		FileWriter fw;
		FileOutputStream fos;
		File screenVidLog = new File(SCREEN_VID_RECORD_PATH+"/ScreenVidLog.txt");
		try
		{
			if(screenVidLog.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(screenVidLog));
				vidCnt = Integer.parseInt(br.readLine());
				fw = new FileWriter(screenVidLog);
				fw.write(new String());
				fw.write(Integer.toString(++vidCnt));
				fw.write(System.getProperty("line.separator", "\n"));
				for(int i=1;i<=vidCnt;i++)
				{
					fw.write("Vid"+i);
					fw.write(System.getProperty("line.separator", "\n"));
					fw.flush();
				}
				vidName = "Vid"+(vidCnt);
				fw.close();
				br.close();
			}else
			{
				fos = new FileOutputStream(screenVidLog);
				fos.close();
				
				fw=new FileWriter(screenVidLog);
				BufferedWriter bw = new BufferedWriter(fw);
				vidCnt = vidCnt + 1;
				bw.write(Integer.toString(vidCnt));
				bw.newLine();
				bw.flush();
				vidName = "Vid" + (vidCnt);
				bw.write(vidName);
				bw.newLine();
				bw.flush();
				bw.close();
				fw.close();
			}
		}catch (FileNotFoundException e)
		{
			Toast.makeText(getBaseContext(), "Video Log File missing, creating it....", Toast.LENGTH_SHORT).show();
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void onCreate()
	{
		super.onCreate();
		
		/*Intent i = new Intent(this, CloseScreenRecordService.class);
        i.putExtra("stopRecordServiceNotificationID", stopRecordServiceNotificationID);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, i, 0);
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification(R.drawable.stop_screen_capture_service_icon, "Stop Service", System.currentTimeMillis());
        notif.setLatestEventInfo(this, "Stop Recording Service", "Stop the Screen Record Service", pendingIntent);
        nm.notify(stopRecordServiceNotificationID, notif);*/
		
		sensorListener = new ShakeEventListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(ScreenRecordActivity.SHAKE_2_START_REC)
        	sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        
        sensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener()
        {
			public void onShake()
			{
				if(ScreenRecordActivity.SHAKE_2_START_REC)
				{
					if(SHAKED)
					{
						if(isNativeRunning())
							Toast.makeText(getBaseContext(), "You have already shaked and started the recording!", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(getBaseContext(), "Native Service Not Found!", Toast.LENGTH_SHORT).show();
					}
					else
					{
						SHAKED=true;
						handleFilesNFolders();
						
						((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenRecordBtn);
						if(ScreenRecordActivity.RECORD_AUDIO)
	        			{
	        				capturedAudioName="CapturedAudio"+vidCnt+capturedAudioExtension;
	        				RECORDED_AUDIO_PATH = SCREEN_VID_RECORD_PATH + "/" + vidName;
	        				new File(RECORDED_AUDIO_PATH).mkdirs();
	        			}
	        			asyncTask = new BgRecord();
	        			asyncTask.execute(FPS);
					}
				}
			}
		});
		
		screenRecordBtn = new ImageButton(this);
        screenRecordBtn.setImageResource(R.drawable.screen_recording_start);
        screenRecordBtn.setOnTouchListener(new View.OnTouchListener()
        {
			public boolean onTouch(View v, MotionEvent event)
			{
				float touchedAtX = event.getX();
        		float touchedAtY = event.getY();
        		
        		Rect btnSize = new Rect();
        		screenRecordBtn.getDrawingRect(btnSize);
        		
        		if(btnSize.contains((int)touchedAtX, (int)touchedAtY))
        		{
        			handleFilesNFolders();
        			
        			
        			((WindowManager) getSystemService(WINDOW_SERVICE)).removeViewImmediate(screenRecordBtn);
        			if(ScreenRecordActivity.RECORD_AUDIO)
        			{
        				capturedAudioName="CapturedAudio"+vidCnt+capturedAudioExtension;
        				RECORDED_AUDIO_PATH = SCREEN_VID_RECORD_PATH + "/" + vidName;
        				new File(RECORDED_AUDIO_PATH).mkdirs();
        			}
        			asyncTask = new BgRecord();
        			asyncTask.execute(FPS);
        			/*try {
        				for(int  i=0;i<150;i++)
        				{
        					Thread.sleep(200);
        					String file = takeScreenshot();
    						count++;
        				}
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
        			dispOverlayBtn();*/
        		}
        		return false;
			}
		});
        dispOverlayBtn();
	}
	void dispOverlayBtn()
    {
    	WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(screenRecordBtn, params);
    }
	public void onDestroy()
    {
        super.onDestroy();
        if(screenRecordBtn != null)
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(screenRecordBtn);
            screenRecordBtn = null;
        }
        if(ScreenRecordActivity.SHAKE_2_START_REC)
        	sensorManager.unregisterListener(sensorListener);
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
  			bmp.compress(CompressFormat.JPEG, 85, fos);
  	}
  //Takes screenshot and saves to a file.
  	public String takeScreenshot() throws IOException
  	{
  		// make sure the path to save screens exists
  		File screenShotPath = new File(SCREEN_VID_RECORD_PATH+"/"+vidName);
  		screenShotPath.mkdirs();
  			
  		// construct screenshot file name
  		StringBuffer sb = new StringBuffer();
  		sb.append(screenShotPath.getAbsolutePath() + "/");
  		sb.append("image");
  		if(count>=1 && count<=9)
  			sb.append("000"+Long.toString(count));
  		else if(count>=10 && count<=99)
  			sb.append("00"+Long.toString(count));
  		else if(count>=100 && count<=999)
  			sb.append("0" + Long.toString(count));
  		else if(count>=1000 && count<=9999)
  			sb.append(Long.toString(count));
  		sb.append(".jpg");
  		
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
}