package com.gabrieldev525.bltchat;
import android.content.*;
import android.bluetooth.*;
import android.util.*;

public class PairingRequest extends BroadcastReceiver
{

	public PairingRequest() {
		super();
	}
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		String action = intent.getAction();
		
		if(action.equals("bluetooth.PairingRequest")) {
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.i("pairing device", device.getName());
		}
	}
}
