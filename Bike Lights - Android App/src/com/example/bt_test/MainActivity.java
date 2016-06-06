package com.example.bt_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener {
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;

	public ProgressDialog progress;

	ConnectedThread connectedThread;
	KeepAlive keepAlive;

	ArrayAdapter<String> listAdapter;
	ArrayAdapter<String> fileListAdapter;
	ListView listView;
	ListView fileListView;
	TextView tvPD;
	Button bRefresh;
	BluetoothAdapter btAdapter;
	Set<BluetoothDevice> devicesArray;
	ArrayList<String> pairedDevices;
	ArrayList<BluetoothDevice> devices;
	IntentFilter filter;
	BroadcastReceiver receiver;
	String readMessage;
	String[] fileNames;
	Boolean receiverRegistered = false;

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			switch(msg.what){
			case SUCCESS_CONNECT:
				connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
				dismissLoading();
				String s = "r";
				connectedThread.write(s.getBytes());
				connectedThread.start();
				Log.d("None", "Connected");
				keepAlive = new KeepAlive(connectedThread);
				keepAlive.start();
				showLoading("Loading", "Getting file names...");
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[])msg.obj;
				readMessage = new String(readBuf, 0, msg.arg1);
				Log.d("None", readMessage);
				dismissLoading();
				fileNames = readMessage.split("\n");
				fileListView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				bRefresh.setVisibility(View.GONE);
				tvPD.setVisibility(View.GONE);

				for (int i = 0; i < fileNames.length; i ++)
				{
					fileListAdapter.add(fileNames[i].substring(0, fileNames[i].length() - 5));
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		if(btAdapter == null){
			Toast.makeText(getApplicationContext(), "No BT detected", 0).show();
			finish();
		} else {
			if(!btAdapter.isEnabled())
			{
				turnOnBT();
			}
			getPairedDevices();
			startDiscovery();
		}
	}

	private void startDiscovery() {
		ArrayAdapter<String> tempAdapter = (ArrayAdapter<String>) listView.getAdapter();
		tempAdapter.clear();
		btAdapter.cancelDiscovery();
		btAdapter.startDiscovery();
	}

	private void turnOnBT() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);	
	}

	private void getPairedDevices() {
		devicesArray = btAdapter.getBondedDevices();
		if(devicesArray.size()>0){
			for(BluetoothDevice device:devicesArray){
				pairedDevices.add(device.getName());
			}
		}
	}

	private void init() {
		listView = (ListView)findViewById(R.id.listView);
		fileListView = (ListView)findViewById(R.id.fileListView);
		bRefresh = (Button)findViewById(R.id.bRefresh);
		bRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startDiscovery();
			}
		});
		tvPD = (TextView)findViewById(R.id.tvPD);
		listView.setOnItemClickListener(this);
		fileListView.setOnItemClickListener(this);
		//listAdapter = new ArrayAdapter<String>(this, R.layout.list_row, R.id.text1);
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,0);
		//fileListAdapter = new ArrayAdapter<String>(this, R.layout.list_row, R.id.text1);
		fileListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,0);
		listView.setAdapter(listAdapter);
		fileListView.setAdapter(fileListAdapter);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = new ArrayList<String>();
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		devices = new ArrayList<BluetoothDevice>();
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();

				if(BluetoothDevice.ACTION_FOUND.equals(action)){
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					devices.add(device);
					String s = "";
					for(int a = 0; a < pairedDevices.size(); a++){
						if(device.getName().equals(pairedDevices.get(a))){
							//append
							s = "(PAIRED)";
							break;
						}
					}
					if(device.getName().contains("Bike Lights")){
						listAdapter.add(device.getName()+" "+s);
					}
				}

				else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
					//if(listAdapter.isEmpty()) listAdapter.add("Searching...");
				}
				else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
					//if(listAdapter.isEmpty()) listAdapter.add("No bike lights found!");
				}
				else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
					if(btAdapter.getState() == btAdapter.STATE_OFF){
						turnOnBT();
					}
				}
			}
		};
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
		receiverRegistered = true;
	}

	protected void onPause(){
		super.onPause();
		if(receiverRegistered){
			unregisterReceiver(receiver);
			receiverRegistered = false;
		}
	}

	public void onBackPressed(){
		super.onBackPressed();
		if(connectedThread != null)
		{
			connectedThread.setRunning(false);
			keepAlive.setRunning(false);
			connectedThread.cancel();
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_CANCELED){
			Toast.makeText(getApplicationContext(), "BT must be enabled to continue", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (parent.getId() == R.id.listView)
		{
			if(btAdapter.isDiscovering()){
				btAdapter.cancelDiscovery();
			}
			if(listAdapter.getItem(position).contains("(PAIRED)")){
				showLoading("Connecting", "Please wait...");
				BluetoothDevice selectedDevice = devices.get(position);
				ConnectThread connect = new ConnectThread(selectedDevice);
				connect.start();
			}
			else{
				Toast.makeText(getApplicationContext(), "Device is not paired - please pair in phone Settings", 0).show();
			}
		} 
		else if (parent.getId() == R.id.fileListView)
		{
			String s = String.valueOf(position);
			connectedThread.write(s.getBytes());
		}
	}

	private void showLoading(String title, String message) {
		progress = new ProgressDialog(this);
		progress.setTitle(title);
		progress.setMessage(message);
		progress.show();
	}

	private void dismissLoading() {
		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			btAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				unregisterReceiver(receiver);
				receiverRegistered = false;
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
					dismissLoading();
					Log.e("Fail", "Could not connect");
				} catch (IOException closeException) { }
				Log.e("Fail Exception", "Could not connect");
				return;
			}

			// Do work to manage the connection (in a separate thread)
			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private volatile boolean running = true;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { 
				Log.e("Error", "Exception in ConnectedThread", e);
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024];;  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (running) {
				try {
					//Log.d("Listening", "....");
					if(mmInStream.available() > 0){
						Thread.sleep(1000);
						// Read from the InputStream 
						bytes = mmInStream.read(buffer);
						mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
					}
				} catch (Exception e) {
					Log.e("Error", "Exception in receive", e);
					break;
				}
			}
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}

	public class KeepAlive extends Thread{
		private ConnectedThread connectedThread;
		private final String TAG = "KeepAlive";
		private volatile boolean running = true;

		public KeepAlive(ConnectedThread connectedThread) {
			this.connectedThread = connectedThread;
			running = true;
		}

		public synchronized void run() {
			Log.d(TAG,"KeepAlive Thread starting");
			while(running) {

				connectedThread.write("!".getBytes());

				try {
					wait(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG,"KeepAlive Thread closing");
		}

		public void setRunning(boolean running) {
			this.running = running;
		}
	}
}
