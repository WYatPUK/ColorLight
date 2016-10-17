package com.main.colorlight;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, OnSeekBarChangeListener {
	private final static String TAG = "DeviceScanActivity";// DeviceScanActivity.class.getSimpleName();
	private final String ACTION_NAME_RSSI = "MCU_RSSI"; // 其他文件广播的定义必须一致
	private final String ACTION_CONNECT = "MCU_CONNECT"; // 其他文件广播的定义必须一致

	static TextView Text_Recv;
	static String Str_Recv;

	static String ReciveStr;
	static ScrollView scrollView;
	static Handler mHandler = new Handler();
	static boolean ifDisplayInHexStringOnOff = true;
	static boolean ifDisplayTimeOnOff = true;
	static TextView textview_recive_send_info;
	static int Totol_Send_bytes = 0;
	static int Totol_recv_bytes = 0;
	static int Totol_recv_bytes_temp = 0;
	static String SendString = "NSame:ffffffof9";
	
	static SeekBar SeekBarG;
	static SeekBar SeekBarR;
	static SeekBar SeekBarB;
	static TextView ColorShow;
	
	final int LED_None = 0;
	final int LED_SetOnce = 1;
	final int LED_Breath = 2;
	final int LED_Breath_Not_First = 3;
	final int LED_SetAll = 4;
	final int LED_Alarming = 5;
	final int LED_Alarming_Not_First = 6;
	final int LED_Pushing = 7;
	String PushList[] = {"ff0000","00ff00","0000ff","ffff00","ff00ff","00ffff","fffffff"};
	int LEDstate = LED_None;
	String AlarmingColor = "00ff00";
	boolean If_Alarm_On = false;
	int Alarm_Time = 1; //s
	String LEDColor;
	Thread LEDthread;

	// 根据rssi 值计算距离， 只是参考作用， 不准确
	static final int rssibufferSize = 10;
	int[] rssibuffer = new int[rssibufferSize];
	int rssibufferIndex = 0;
	boolean rssiUsedFalg = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other);
		getActionBar().setTitle("Series");

		registerBoradcastReceiver();

		this.findViewById(R.id.button_breath).setOnClickListener(this);
		this.findViewById(R.id.button_Alarm).setOnClickListener(this);
		this.findViewById(R.id.button_Push).setOnClickListener(this);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String mac_addr = bundle.getString("mac_addr");
		String char_uuid = bundle.getString("char_uuid");


		//Text_Recv = (TextView) findViewById(R.id.device_address);
		//Text_Recv.setGravity(Gravity.CLIP_VERTICAL | Gravity.CLIP_HORIZONTAL);
		ReciveStr = "";
		//Text_Recv.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		SeekBarG = (SeekBar) findViewById(R.id.seekbarG);
		SeekBarR = (SeekBar) findViewById(R.id.seekbarR);
		SeekBarB = (SeekBar) findViewById(R.id.seekbarB);
		SeekBarG.setOnSeekBarChangeListener(this);
		SeekBarR.setOnSeekBarChangeListener(this);
		SeekBarB.setOnSeekBarChangeListener(this);
		ColorShow = (TextView) findViewById(R.id.colorShow);

		Totol_Send_bytes = 0;
		Totol_recv_bytes = 0;
		Totol_recv_bytes_temp = 0;
		//update_display_send_recv_info(Totol_Send_bytes, Totol_recv_bytes);

		ifDisplayInHexStringOnOff = true;
		ifDisplayTimeOnOff = true;


		
		
		
		
		LEDthread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				int t = 0;
				int Alarm_Count = 0;
				double T = 100;
				double Alarming_T = 30;
				boolean Alarm_Lasting = false;
				int Push_Count = 0;
				int Push_Num = 0;
				int Pushing_T = 20;
				while (true)
				{
					switch(LEDstate)
					{
					case LED_None:
						break;
					case LED_SetOnce:
						if (LEDColor.length() ==6) {
							//ColorShow.setText(LEDColor);
							DeviceScanActivity.writeChar6("Unit:" + LEDColor);
							LEDstate = LED_None;
						}
						else {
							//wrong
							Log.e("LED Thread", "Color not of length 6");
							LEDstate = LED_None;
						}
						break;
					case LED_SetAll:
						if (LEDColor.length() ==6) {
							//ColorShow.setText(LEDColor);
							DeviceScanActivity.writeChar6("NSame:" + LEDColor + "of9");
							LEDstate = LED_None;
						}
						else {
							//wrong
							Log.e("LED Thread", "Color not of length 6");
							LEDstate = LED_None;
						}
						break;
					case LED_Breath:
						t = 0;
						LEDstate = LED_Breath_Not_First;
					case LED_Breath_Not_First:
						if (LEDColor.length() == 6) {
							char G,R,B;
							G = (char) ((Math.abs(1.0-2.0*t/T) * (Wy_char_to_int(LEDColor.charAt(0))*16 + Wy_char_to_int(LEDColor.charAt(1)))));
							R = (char) ((Math.abs(1.0-2.0*t/T) * (Wy_char_to_int(LEDColor.charAt(2))*16 + Wy_char_to_int(LEDColor.charAt(3)))));
							B = (char) ((Math.abs(1.0-2.0*t/T) * (Wy_char_to_int(LEDColor.charAt(4))*16 + Wy_char_to_int(LEDColor.charAt(5)))));
							String BreathString = char_to_string(G) + char_to_string(R) + char_to_string(B);
							//ColorShow.setText(BreathString);
							DeviceScanActivity.writeChar6("NSame:" + BreathString + "of9");
						}
						else {
							//wrong
							Log.e("LED Thread", "Color not of length 6");
							LEDstate = LED_None;
						}
						break;
					case LED_Alarming:
						t = 0;
						LEDstate = LED_Alarming_Not_First;
					case LED_Alarming_Not_First:
						if (AlarmingColor.length() == 6) {
							char G,R,B;
							G = (char) ((Math.abs(1.0-2.0*t/Alarming_T) * (Wy_char_to_int(AlarmingColor.charAt(0))*16 + Wy_char_to_int(AlarmingColor.charAt(1)))));
							R = (char) ((Math.abs(1.0-2.0*t/Alarming_T) * (Wy_char_to_int(AlarmingColor.charAt(2))*16 + Wy_char_to_int(AlarmingColor.charAt(3)))));
							B = (char) ((Math.abs(1.0-2.0*t/Alarming_T) * (Wy_char_to_int(AlarmingColor.charAt(4))*16 + Wy_char_to_int(AlarmingColor.charAt(5)))));
							String BreathString = char_to_string(G) + char_to_string(R) + char_to_string(B);
							//ColorShow.setText(BreathString);
							DeviceScanActivity.writeChar6("NSame:" + BreathString + "of9");
						}
						else {
							//wrong
							Log.e("LED Thread", "AlarmingColor not of length 6");
							LEDstate = LED_None;
						}
						break;
					case LED_Pushing:
						Push_Count++;
						if (Push_Count == Pushing_T) {
							Push_Count = 0;
							DeviceScanActivity.writeChar6("PushN:" + PushList[Push_Num] + "of9");
							Push_Num++;
						}
						if (Push_Num == PushList.length) Push_Num = 0;
						break;
					}
					try {
						Thread.sleep(30); //the highest speed tried
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (If_Alarm_On) {
						if (Alarm_Lasting) {
							if (Alarm_Count == Alarm_Time * 20) {
								//Alarm On
								LEDstate = LED_Alarming;
							}
							Alarm_Count++;
						} else {
							Alarm_Lasting = true;
							Alarm_Count = 0;
						}
					} else {
						Alarm_Lasting = false;
					}
					t++;
					if (If_Alarm_On && t%Alarming_T == 0) {
						t=0;
					}
					else if (t%T == 0) t = 0;
				}
			}
		});
		LEDthread.start();
	}

	// 接收 rssi 的广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(ACTION_NAME_RSSI)) {
				int rssi = intent.getIntExtra("RSSI", 0);

				// 以下这些参数我 amomcu 自己设置的， 不太具有参考意义，
				// 实际上我的本意就是根据rssi的信号前度计算以下距离，
				// 以便达到定位目的， 但这个方法并不准 ---amomcu---------20150411

				int rssi_avg = 0;
				int distance_cm_min = 10; // 距离cm -30dbm
				int distance_cm_max_near = 1500; // 距离cm -90dbm
				int distance_cm_max_middle = 5000; // 距离cm -90dbm
				int distance_cm_max_far = 10000; // 距离cm -90dbm
				int near = -72;
				int middle = -80;
				int far = -88;
				double distance = 0.0f;

				if (true) {
					rssibuffer[rssibufferIndex] = rssi;
					rssibufferIndex++;

					if (rssibufferIndex == rssibufferSize)
						rssiUsedFalg = true;

					rssibufferIndex = rssibufferIndex % rssibufferSize;

					if (rssiUsedFalg == true) {
						int rssi_sum = 0;
						for (int i = 0; i < rssibufferSize; i++) {
							rssi_sum += rssibuffer[i];
						}

						rssi_avg = rssi_sum / rssibufferSize;

						if (-rssi_avg < 35)
							rssi_avg = -35;

						if (-rssi_avg < -near) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-near - 35))
									* distance_cm_max_near;
						} else if (-rssi_avg < -middle) {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-middle - 35))
									* distance_cm_max_middle;
						} else {
							distance = distance_cm_min
									+ ((-rssi_avg - 35) / (double) (-far - 35))
									* distance_cm_max_far;
						}
					}
				}

				getActionBar().setTitle(
						"RSSI:" + rssi_avg + "dbm" + "," + "距离:"
								+ (int) distance + "cm");
			} else if (action.equals(ACTION_CONNECT)) {
				int status = intent.getIntExtra("CONNECT_STATUC", 0);
				if (status == 0) {
					getActionBar().setTitle("已断开连接");
					finish();
				} else {
					getActionBar().setTitle("已连接");
				}
			}
		}
	};

	// 注册广播
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction(ACTION_NAME_RSSI);
		myIntentFilter.addAction(ACTION_CONNECT);
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.button_breath:
			ClearLEDMode();
			LEDColor = ColorShow.getText().toString();
			LEDstate = LED_Breath;
			break;
		case R.id.button_Push:
			ClearLEDMode();
			
			break;
		case R.id.button_Alarm:
			ClearLEDMode();
			LEDColor = "00ff00";
			Alarm_Time = 5;
			If_Alarm_On = true;
			break;
		}
	}

	public static synchronized void char6_display(String str, byte[] data, String uuid) {
		Log.i(TAG, "char6_display str = " + str);
		if (uuid.equals(DeviceScanActivity.UUID_CHAR6)) // MCU 的串口透传
		{
			if (ifDisplayTimeOnOff == true) {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"HH:mm:ss ");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String TimeStr = formatter.format(curDate);

				String DisplayStr = "[" + TimeStr + "] " + str;
				// Text_Recv.append(DisplayStr + "\r\n");
				Str_Recv = DisplayStr + "\r\n";
			} else {
				Str_Recv = str;
				// Text_Recv.setText(str);
			}

		} else {
			//should not reach here
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		ClearLEDMode();
		String A;
		A = ColorShow.getText().toString();
		String OG = A.substring(0, 2);
		String OR = A.substring(2, 4);
		String OB = A.substring(4, 6);
		if (seekBar == SeekBarG) {
			char c = (char) (progress * 2.52); 
			if(progress == 100) c = 0xff;
			OG = char_to_string(c);
		}
		else if (seekBar == SeekBarR) {
			char c = (char) (progress * 2.52); 
			if(progress == 100) c = 0xff;
			OR = char_to_string(c);
		}
		else if (seekBar == SeekBarB) {
			char c = (char) (progress * 2.52); 
			if(progress == 100) c = 0xff;
			OB = char_to_string(c);
		}
		ColorShow.setText(OG + OR + OB);
		LEDColor = OG + OR + OB;
		LEDstate = LED_SetAll;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	public static String char_to_string (char r){
		String a = "";
		String b = "%";
		a += b.replace('%', Wy_int_to_hex((r>>4) & 0x0f));
		a += b.replace('%', Wy_int_to_hex(r & 0x0f));
		return a;
	}
	public static char Wy_int_to_hex (int x){
		if (x>=0 && x<10){
			return (char) ('0' + x);
		}
		else if (x>9 && x<16){
			return (char) ('a' + x - 10);
		}
		else return (char) 0;
	}
	public static int Wy_char_to_int(char x){
		if (x-'0' >=0 && x-'0'<10){
			return x-'0';
		}
		else if (x-'a' >= 0 && x-'a'<6){
			return x-'a'+10;
		}
		else return 0;
	}
	
	public void ClearLEDMode()
	{
		LEDstate = LED_None;
		If_Alarm_On = false;
	}
}