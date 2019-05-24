package com.gabrieldev525.bltchat;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;

public class Chat extends Activity {
	private EditText output;
	private EditText input;
	private Button send;

	//socket of connection
	private BluetoothSocket socket;

	//handler
	protected Handler handler;
	protected static final int STATE_MESSAGE_RECEIVED = 10;
	ConnectedThread conn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.chat);

		//getting the socket connected from MainActivity
		socket = MainActivity.finalSocket;
		//init the comunication thread
		conn = new ConnectedThread(socket);
		conn.start();
		
		
		//handler
		handler = new Handler(new Handler.Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					switch(msg.what) {
						case STATE_MESSAGE_RECEIVED:
							byte[] readBuffer= (byte[]) msg.obj;
							String tempMsg= new String(readBuffer, 0, msg.arg1);

							output.setText(output.getText() + "\n" + tempMsg);
							break;
					}
					return true;
				}
			});

		output = (EditText) findViewById(R.id.chat_output);
		input = (EditText) findViewById(R.id.chat_input);

		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					conn.write(input.getText().toString().getBytes());
					output.setText(output.getText() + "\n" + input.getText().toString());
					input.setText(null);
				}
			});



		super.onCreate(savedInstanceState);
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream input;
		private final OutputStream output;

		public ConnectedThread(BluetoothSocket socket) {
			this.mSocket = socket;

			InputStream tempInput = null;
			OutputStream tempOutput = null;

			try {
				tempInput = mSocket.getInputStream();
				tempOutput = mSocket.getOutputStream();
			} catch(IOException e) {}

			input = tempInput;
			output = tempOutput;

		}

		@Override
		public void run() {
			byte buffer[] = new byte[1024];
			int bytes;

			while(true) {
				try {
					bytes = input.read(buffer);
					handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();


				} catch(IOException e) {
					break;
				}
			}
		}

		public void write(byte[] bytes) {
			try {
				output.write(bytes);
			} catch(IOException e) {}
		}

		public void cancel() {
			try {
				mSocket.close();
			} catch(IOException e) {}
		}
	}
}
