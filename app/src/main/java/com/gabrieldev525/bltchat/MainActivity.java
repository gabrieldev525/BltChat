package com.gabrieldev525.bltchat;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.util.*;
import java.io.*;
import android.view.ActionMode.*;

public class MainActivity extends Activity {
	private final String mUUID = "b2dce34f-d7e8-43bb-9c63-29b50f10c267";
	private final String CONN_NAME = "chat_bluetooth";

	//result on activate bluetooth
	private int REQUEST_ENABLE_BT;

	private ListView pairedDevicesList;
	private ListView discoveredDevicesList;
	private Button createServer;
	private Button sendMessage;

	//adapter
	ArrayAdapter<String> adapterDiscoveryList;

	//list
	Set<BluetoothDevice> pairedDevices;
	ArrayList<String> devicesPaired;
	ArrayList<BluetoothDevice> listDevicesPaired = new ArrayList<BluetoothDevice>();
	ArrayList<String> devicesDiscovery = new ArrayList<String>();

	BluetoothAdapter bluetoothAdapter;

	private BroadcastReceiver receiver;
	
	public static BluetoothSocket finalSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


		//lists init
		pairedDevicesList = (ListView) findViewById(R.id.devices_paired_listview);
		pairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
					final ClientThread client = new ClientThread(listDevicesPaired.get(p3));
					client.start();
				}
			});
		discoveredDevicesList = (ListView) findViewById(R.id.devices_discovered_listview);

		//bluetooth system
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter == null) { //device dont support bluetooth

		} else {
			if(!bluetoothAdapter.isEnabled()) { //bluetooth isn't enabled
				Intent enableBluetoothIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);

				if(REQUEST_ENABLE_BT == RESULT_CANCELED) { //user cancel
					Toast.makeText(MainActivity.this, "you need active bluetooth to use this app", Toast.LENGTH_LONG).show();
				} else if(REQUEST_ENABLE_BT == RESULT_OK) {
					connectBluetooth();
				}
			} else {
				connectBluetooth();
			}
		}

		//button
		createServer = (Button) findViewById(R.id.createServer);
		createServer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					//start server
					final ServerThread server = new ServerThread();
					server.start();
				}
			});
    }

	//server
	private class ServerThread extends Thread {
		private final BluetoothServerSocket serverSocket;
		private UUID uuid;

		AlertDialog dialog;

		public ServerThread() {
			//alert
			AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
			dlg.setTitle("Esperando conexão com outro dispositivo...");
			dlg.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface p1, int p2) {
						cancel();
						p1.dismiss();
					}
				});
			dlg.setCancelable(false);

			dialog = dlg.create();
			dialog.show();
			
			BluetoothServerSocket tmp = null;

			uuid = UUID.fromString(mUUID);

			try {
				tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(CONN_NAME, uuid);
			} catch(IOException e) {}

			serverSocket = tmp;
		}

		@Override
		public void run() {
			BluetoothSocket socket = null;
			while(true) {
				try {
					socket = serverSocket.accept();
				} catch(IOException e) { break; }

				if(socket != null) { //connected
					cancel();

					//start activity if connected
					Intent i = new Intent(MainActivity.this, Chat.class);
					
					finalSocket = socket;
					startActivity(i);
					
					dialog.dismiss();
					break;
				}
			}

		}

		public void cancel() {
			try {
				serverSocket.close();
			} catch(IOException e) {}
		}
	}

	//client
	private class ClientThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice mdevice;

		private UUID uuid;

		AlertDialog dialog;

		public ClientThread(BluetoothDevice device) {
			//alert
			AlertDialog.Builder dlg1 = new AlertDialog.Builder(MainActivity.this);
			dlg1.setTitle("Tentando conexão com outro dispositivo...");
			dlg1.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						cancel();
						dialog.dismiss();
					}
				});
			dlg1.setCancelable(false);
			
			dialog = dlg1.create();
			dialog.show();

			mdevice = device;
			BluetoothSocket tmp = null;

			uuid = UUID.fromString(mUUID);

			try {
				tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
			} catch(IOException e) {}

			socket = tmp;
		}

		@Override
		public void run() {
			bluetoothAdapter.cancelDiscovery();

			try {
				socket.connect();
			} catch(IOException e) {
				cancel();

				return;
			}

			//start activity if connected
			Intent i = new Intent(MainActivity.this, Chat.class);
			
			dialog.dismiss();
			
			finalSocket = socket;
			startActivity(i);

			//manage conection
		}

		public void cancel() {
			try {
				socket.close();
			} catch(IOException e) {}
		}
	}

	

	private void connectBluetooth() {
		//get the list of all devices paired
		pairedDevices = bluetoothAdapter.getBondedDevices();

		if(pairedDevices.size() > 0) {
			devicesPaired = new ArrayList<String>();
			for(BluetoothDevice d : pairedDevices) {
				devicesPaired.add(d.getName() + "\n" + d.getAddress());
				listDevicesPaired.add(d);
			}

			//list all paired devices
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, devicesPaired);
			pairedDevicesList.setAdapter(adapter);
		}

		//make the bluetooth visible
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
		startActivity(discoverableIntent);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch(item.getItemId()) {
			case R.id.search_menu:
				//receiver
				receiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						String action = intent.getAction();

						//when discovery a device
						if(BluetoothDevice.ACTION_FOUND.equals(action)) {
							BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							devicesDiscovery.add(device.getName() + "\n" + device.getAddress());

							Toast.makeText(MainActivity.this, "discovered\n" + device.getName(), Toast.LENGTH_LONG).show();

							//list all discovered devices
							adapterDiscoveryList = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, devicesDiscovery);
							discoveredDevicesList.setAdapter(adapterDiscoveryList);
						}
					}

				};

				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
				registerReceiver(receiver, filter);

				break;
		}

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.devices_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


}
