package com.example.bluetoothapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothapp.Chat.onChatActivityListener;
import com.example.bluetoothapp.DiscoverDevice.OnDiscoverDeviceListener;

public class MainActivity extends FragmentActivity implements
		OnItemClickListener, OnDiscoverDeviceListener, onChatActivityListener {

	BluetoothAdapter mBluetoothAdapter;
	static final int REQUEST_ENABLE_BT = 1;

	static final int CONNECTED = 2;

	Button discover;

	ListView listView;

	Set<BluetoothDevice> devicesList;

	ArrayList<PairedDevice> pairedDevices;

	ArrayAdapter<PairedDevice> mArrayAdapter;

	IntentFilter filter;
	BroadcastReceiver mBluetoothReceiver;

	ProgressBar progressBar;
	private static final int MESSAGE_READ = 5;

	private int Pair_Request = 6;
	ConnectThread connectThread;
	ConnectedThread connectedThread;

	TextView chatTextView;
	TextView inputTextView;


	Handler mHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			Log.i("CONNECT", "ENTROU AQUI");

			if (msg.what == (CONNECTED)) {

				Log.d(CONNECTIVITY_SERVICE, "Entrei no connected");

				Toast.makeText(getApplicationContext(), "Handler",
						Toast.LENGTH_SHORT).show();

				String s = "Connected, welcome!";

				connectedThread.write(s.getBytes());

				return true;
			} else if (msg.what == MESSAGE_READ) {

				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				// for (int i = 0; i < readBuf.length; i++) {
				// // chatTextView.append(" "+Integer.toHexString(0xFF,
				// readBuf[i]));
				// }

				// chatTextView.append(readMessage+" ");
				chatTextView.append(bytes2String(readBuf, msg.arg1) + "");

				chatTextView.append("\n");

				return true;

			}
			return false;

		};
	});

	public static String bytes2String(byte[] b, int count) {
		StringBuilder ret = new StringBuilder();
		// String str ="";
		for (int i = 0; i < count; i++) {
			String myInt = Integer.toHexString((int) (b[i] & 0xFF));
			// String myInt = Integer.toString((int)(b[i] & 0xFF));
			ret.append(myInt + " ");
		}
		return ret.toString();
	}

	Handler chatHandler;

	private String discoverDeviceTag = "DISCOVER_DEVICE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		chatHandler = new Handler();

		menuAdapter();

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

			}

		}

	}

	private void menuAdapter() {
		if (findViewById(R.id.fragment_container) != null) {

			Fragment discoverDevice = new DiscoverDevice();

			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.fragment_container, discoverDevice,
							discoverDeviceTag).commit();

			// wait for the listener in discover device to initiate the
			// procedure
		}

	}

	private void discoverDevicesInit() {

		discover = (Button) findViewById(R.id.btn_Discover);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		mArrayAdapter = new ArrayAdapter<PairedDevice>(this,
				android.R.layout.simple_list_item_checked, 0);
		listView.setAdapter(mArrayAdapter);

		pairedDevices = new ArrayList<PairedDevice>();

	}

	private void registBroadcastReceivers() {
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

		mBluetoothReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					if (devicesList.size() > 0)
						for (PairedDevice pDevice : pairedDevices) {
							if (pDevice.getDevice().getAddress()
									.equals(device.getAddress())) {

								mArrayAdapter
										.add(new PairedDevice(device, true));
								return;
							}

						}

					mArrayAdapter.add(new PairedDevice(device, false));

				} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
						.equals(action)) {

					progressBar.setVisibility(0);

				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
						.equals(action)) {

					progressBar.setVisibility(4);

				} else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

					if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
						Intent enableBtIntent = new Intent(
								BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(enableBtIntent,
								REQUEST_ENABLE_BT);

					}

				}

			}
		};

		registerReceiver(mBluetoothReceiver, filter); // Don't forget to
		// unregister during
		// onDestroy

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver(mBluetoothReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mBluetoothReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mBluetoothReceiver, filter);

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void startDiscovery() {
		mArrayAdapter.clear();
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothAdapter.startDiscovery();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Without bluetooth, it will not work.",
					Toast.LENGTH_SHORT).show();

		} else if (requestCode == Pair_Request && resultCode == RESULT_OK) {

			Toast.makeText(this, "Paring to device", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.e(TEXT_SERVICES_MANAGER_SERVICE, "ON DETROY MAIN ACTIVITY");

		unregisterReceiver(mBluetoothReceiver);
		if (connectedThread != null)
			connectedThread.cancel();
		if (connectThread != null)
			connectThread.cancel();

	}

	public void onClickBtnDiscover(View v) {

		startDiscovery();

	}
	
	public void onCLickSendButton(View v) {

		connectedThread.write(inputTextView.getText().toString().getBytes());
		inputTextView.setText("");

	}

	public void getPairedDevices() {

		devicesList = mBluetoothAdapter.getBondedDevices();
		if (devicesList.size() > 0)
			for (BluetoothDevice device : devicesList) {
				pairedDevices.add(new PairedDevice(device, true));

			}

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int deviceIndex,
			long arg3) {

		if (mBluetoothAdapter.isDiscovering()) {

			mBluetoothAdapter.cancelDiscovery();

		}
		unregisterReceiver(mBluetoothReceiver);

		Fragment chatFragment = new Chat();

		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();

		fragmentTransaction.replace(R.id.fragment_container, chatFragment);
		fragmentTransaction.addToBackStack(null);

		fragmentTransaction.commit();

		if (mArrayAdapter.getItem(deviceIndex).getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
			Log.e(STORAGE_SERVICE, "PAIRED");

			Toast.makeText(this, "Conneting to paired device.",
					Toast.LENGTH_SHORT).show();

			String address = "00:11:11:28:09:45";
			// "XX:XX:XX:XX:XX:XX";
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

			// connectThread = new ConnectThread(mArrayAdapter
			// .getItem(deviceIndex).getDevice());
			connectThread = new ConnectThread(device);

			connectThread.start();

		} else {

			Log.e(STORAGE_SERVICE, "NOT PAIRED");

			Toast.makeText(
					this,
					"Device not pared. You should pair before trying to connect.",
					Toast.LENGTH_SHORT).show();

			connectThread = new ConnectThread(mArrayAdapter
					.getItem(deviceIndex).getDevice());

			connectThread.run();
		}
	}

	// GOOGLE DEVELOPERS
	private class ConnectThread extends Thread {

		private final UUID MY_UUID = UUID
				.fromString("00001101-0000-1000-8000-00805F9B34FB");
		// UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
		// //Standard SerialPortService ID

		// private final UUID MY_UUID = UUID
		// .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"); // USE FOR
		// CONNECTING
		// PEER
		// DEVICES

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// BLUETOOTH ADDRESS: 11:11:280945

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection

			Log.e(STORAGE_SERVICE, "RUN CONNECT THREAD");

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();

				// Log.e(STORAGE_SERVICE,
				// "SOCKET CONNECTED: "+mmSocket.isConnected());

			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)
			manageConnectedSocket(mmSocket);
			Log.d(CONNECTIVITY_SERVICE, "Enviar msg");

			connectedThread = new ConnectedThread(mmSocket);
			connectedThread.start();

			mHandler.obtainMessage(CONNECTED, mmSocket).sendToTarget();
			Log.d(CONNECTIVITY_SERVICE, "Enviei msg");

		}

		private void manageConnectedSocket(BluetoothSocket mmSocket) {
			// TODO Auto-generated method stub
			// DO SOMETHING
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

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
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {

					buffer = new byte[mmInStream.available()];
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
							.sendToTarget();

				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void onDiscoverDeviceActivityCreated() {
		discoverDevicesInit();
		registBroadcastReceivers();
		getPairedDevices();

	}

	@Override
	public void onChatActivityCreated() {
		chatTextView = (TextView) findViewById(R.id.txtView_Chat);
		chatTextView.setMovementMethod(new ScrollingMovementMethod());
		inputTextView = (TextView) findViewById(R.id.edit_text_out);

	}

}
