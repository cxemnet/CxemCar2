package com.cxem_car2;

import com.cxem_car2.R;
import com.cxem_car2.cBluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.cxem_car2.MjpegInputStream;
import com.cxem_car2.MjpegView;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ActivityButtons extends Activity {
    private static final String TAG = "CxemCAR2";

    private cBluetooth bl = null;
    private MjpegView mv;
	private Button btn_forward, btn_backward, btn_left, btn_right;
	private Button btn_cam_up, btn_cam_down, btn_cam_left, btn_cam_right;
    
    private int motorLeft = 0;
    private int motorRight = 0;
    private String address;			// MAC-address from settings (MAC-адрес устройства из настроек)
    private String CameraURL, CameraControlURL;
    private int pwmBtnMotorLeft;
    private int pwmBtnMotorRight;
    private String commandLeft;		// command symbol for left motor from settings (символ команды левого двигателя из настроек)
    private String commandRight;	// command symbol for right motor from settings (символ команды правого двигателя из настроек)
    private String commandHorn;		// command symbol for optional command from settings (for example - horn) (символ команды для доп. канала (звуковой сигнал) из настроек)
    private boolean show_Debug;		// show debug information (from settings) (отображение отладочной информации (из настроек))
    private boolean BT_is_connect;	// bluetooh is connected (переменная для хранения информации подключен ли Bluetooth)
    private String cmdSend;
     
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		address = (String) getResources().getText(R.string.default_MAC);
		pwmBtnMotorLeft = Integer.parseInt((String) getResources().getText(R.string.default_pwmBtnMotorLeft));
		pwmBtnMotorRight = Integer.parseInt((String) getResources().getText(R.string.default_pwmBtnMotorRight));
        commandLeft = (String) getResources().getText(R.string.default_commandLeft);
        commandRight = (String) getResources().getText(R.string.default_commandRight);
        commandHorn = (String) getResources().getText(R.string.default_commandHorn);
        CameraURL = (String) getResources().getText(R.string.default_camURL);
        CameraControlURL = (String) getResources().getText(R.string.default_camControlURL);
		
		loadPref();
		
		//String CameraURL = "http://iris.not.iac.es/axis-cgi/mjpg/video.cgi?resolution=320x240";		// Public MJPEG Camera for test's
        
        bl = new cBluetooth(this, mHandler);
        bl.checkBTState();
      
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    
        mv = new MjpegView(this);
        View stolenView = mv;

        setContentView(R.layout.activity_buttons);
        View view =(findViewById(R.id.Vid));
        ((ViewGroup) view).addView(stolenView);
		
        btn_forward = (Button) findViewById(R.id.moveForward);
        btn_backward = (Button) findViewById(R.id.moveBackward);
        btn_left = (Button) findViewById(R.id.moveLeft);
        btn_right = (Button) findViewById(R.id.moveRight);
        
        btn_cam_up = (Button) findViewById(R.id.moveCamUp);
        btn_cam_down = (Button) findViewById(R.id.moveCamDown);
        btn_cam_left = (Button) findViewById(R.id.moveCamLeft);
        btn_cam_right = (Button) findViewById(R.id.moveCamRight);
               
		btn_forward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	motorLeft = pwmBtnMotorLeft;
		        	motorRight = pwmBtnMotorRight;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        }
				return false;
			    }
			});
	    
		btn_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	motorLeft = -pwmBtnMotorLeft;
		        	motorRight = pwmBtnMotorRight;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        }
				return false;
		    }
		});
		
		btn_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	motorLeft = pwmBtnMotorLeft;
		        	motorRight = -pwmBtnMotorRight;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        }
				return false;
		    }
		});
		
		btn_backward.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE) {
		        	motorLeft = -pwmBtnMotorLeft;
		        	motorRight = -pwmBtnMotorRight;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	motorLeft = 0;
		        	motorRight = 0;
		        	cmdSend = String.valueOf(commandLeft+motorLeft+"\r"+commandRight+motorRight+"\r");
		        	ShowTextDebug(cmdSend);
		        	if(BT_is_connect) bl.sendData(cmdSend);
		        }
				return false;
		    }
		});
		
		btn_cam_up.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	Log.d(TAG, "Camp Up");
		        	String URL = CameraControlURL + "0";
		        	new WebPageTask().execute(URL);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	Log.d(TAG, "Camp Down");
		        	String URL = CameraControlURL + "1";
		        	new WebPageTask().execute(URL);
		        }
				return false;
			}
		});
		
		btn_cam_down.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	String URL = CameraControlURL + "2";
		        	new WebPageTask().execute(URL);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	String URL = CameraControlURL + "3";
		        	new WebPageTask().execute(URL);
		        }
				return false;
			}
		});	
		
		btn_cam_left.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	String URL = CameraControlURL + "6";
		        	new WebPageTask().execute(URL);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	String URL = CameraControlURL + "7";
		        	new WebPageTask().execute(URL);
		        }
				return false;
			}
		});
		
		btn_cam_right.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_DOWN) {
		        	String URL = CameraControlURL + "4";
		        	new WebPageTask().execute(URL);
		        } else if (event.getAction() == MotionEvent.ACTION_UP) {
		        	String URL = CameraControlURL + "5";
		        	new WebPageTask().execute(URL);
		        }
				return false;
			}
		});      
        
        mHandler.postDelayed(sRunnable, 600000);

        new DoRead().execute(CameraURL);
    }
    
    private static class MyHandler extends Handler {
        private final WeakReference<ActivityButtons> mActivity;
     
        public MyHandler(ActivityButtons activity) {
          mActivity = new WeakReference<ActivityButtons>(activity);
        }
     
        @Override
        public void handleMessage(Message msg) {
        	ActivityButtons activity = mActivity.get();
        	if (activity != null) {
        		switch (msg.what) {
	            case cBluetooth.BL_NOT_AVAILABLE:
	               	Log.d(cBluetooth.TAG, "Bluetooth is not available. Exit");
	            	Toast.makeText(activity.getBaseContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
	                activity.finish();
	                break;
	            case cBluetooth.BL_INCORRECT_ADDRESS:
	            	Log.d(cBluetooth.TAG, "Incorrect MAC address");
	            	Toast.makeText(activity.getBaseContext(), "Incorrect Bluetooth address", Toast.LENGTH_SHORT).show();
	                break;
	            case cBluetooth.BL_REQUEST_ENABLE:   
	            	Log.d(cBluetooth.TAG, "Request Bluetooth Enable");
	            	BluetoothAdapter.getDefaultAdapter();
	            	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            	activity.startActivityForResult(enableBtIntent, 1);
	                break;
	            case cBluetooth.BL_SOCKET_FAILED:
	            	Toast.makeText(activity.getBaseContext(), "Socket failed", Toast.LENGTH_SHORT).show();
	            	//activity.finish();
	                break;
	          	}
          	}
        }
	}
     
	private final MyHandler mHandler = new MyHandler(this);
	
	private final static Runnable sRunnable = new Runnable() {
		public void run() { }
	};
	
	private void ShowTextDebug(String txtDebug){
		TextView textCmdSend = (TextView) findViewById(R.id.textViewCmdSend);
		if(show_Debug){
	        textCmdSend.setText(String.valueOf("Send:" + txtDebug));
        }
        else{
        	textCmdSend.setText("");
        }
	}
	
	private void loadPref(){
    	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);  
    	address = mySharedPreferences.getString("pref_MAC_address", address);			// First time we load the default value (Первый раз загружаем дефолтное значение)
    	pwmBtnMotorLeft = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorLeft", String.valueOf(pwmBtnMotorLeft)));
    	pwmBtnMotorRight = Integer.parseInt(mySharedPreferences.getString("pref_pwmBtnMotorRight", String.valueOf(pwmBtnMotorRight)));
    	commandLeft = mySharedPreferences.getString("pref_commandLeft", commandLeft);
    	commandRight = mySharedPreferences.getString("pref_commandRight", commandRight);
    	commandHorn = mySharedPreferences.getString("pref_commandHorn", commandHorn);
    	show_Debug = mySharedPreferences.getBoolean("pref_Debug", false);
    	CameraURL = mySharedPreferences.getString("pref_camURL", CameraURL);
    	CameraControlURL = mySharedPreferences.getString("pref_camControlURL", CameraControlURL);
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	BT_is_connect = bl.BT_Connect(address, false);
    }

	@Override
    public void onPause() {
        super.onPause();
        mv.stopPlayback();
        bl.BT_onPause();
    }
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 loadPref();
	 }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();     
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if(res.getStatusLine().getStatusCode()==401){
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
                //Error connecting to camera
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            mv.setDisplayMode(MjpegView.SIZE_STANDARD);   //SIZE_BEST_FIT
            mv.showFps(true);
        }
    }
    
    private class WebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
          String response = "";
          for (String url : urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
              HttpResponse execute = client.execute(httpGet);
              InputStream content = execute.getEntity().getContent();

              BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
              String s = "";
              while ((s = buffer.readLine()) != null) {
                response += s;
              }

            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          return response;
        }

        @Override
        protected void onPostExecute(String result) {
          //textView.setText(result);
        }
      }
   
}