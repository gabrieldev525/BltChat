package com.gabrieldev525.bltchat;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

public class Chat extends Activity 
{
	private EditText output;
	private EditText input;
	private Button send;
	
	private BluetoothSocket socket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.chat);
		
		Intent i = getIntent();
		//socket = (BluetoothSocket) i.getSerializableExtra("socket");
		Object ob = i.getBundleExtra("socket").getSerializable("socket");
		socket = (BluetoothSocket) ob;
		
		Toast.makeText(Chat.this, socket.getRemoteDevice().getName(), Toast.LENGTH_LONG).show();
		
		output = (EditText) findViewById(R.id.chat_output);
		input = (EditText) findViewById(R.id.chat_input);

		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					// TODO: Implement this method
				}
			});

			
		
		super.onCreate(savedInstanceState);
	}
	
}
