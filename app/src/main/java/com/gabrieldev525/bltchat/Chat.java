package com.gabrieldev525.bltchat;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import com.gabrieldev525.bltchat.ui.*;
import android.graphics.*;
import android.widget.RelativeLayout.*;
import java.util.*;
import android.graphics.drawable.*;
import android.util.*;

public class Chat extends Activity {
	private Utils utils;

	private ActionBar actionBar;

	//views
	private LinearLayout mainLayout;
	private LinearLayout outputLayout;
	private ScrollView outputLayoutScroll;
	private LinearLayout outputLayoutScrollLay;
	private LinearLayout inputLayout;
	private EditText input;
	private Button send;

	//socket of connection
	private BluetoothSocket socket;

	//handler
	protected Handler handler;
	protected static final int STATE_MESSAGE_RECEIVED = 10;
	ConnectedThread conn = null;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.chat);

		utils = new Utils(Chat.this);

		//top bar
		actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayUseLogoEnabled(false);

		//getting the socket connected from MainActivity
		socket = MainActivity.finalSocket;

		actionBar.setTitle(socket.getRemoteDevice().getName());

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

							//close the activity
							if(tempMsg.equals("{#close_connection#}")) {
								closeConn();
							} else {
								//object message
								MessageContent message = new MessageContent(Chat.this);
								message.setMessageText(tempMsg);
								/*ImageView img = new ImageView(Chat.this);

								 Bitmap bmp = BitmapFactory.decodeByteArray(readBuffer, 0, msg.arg1);
								 img.setImageBitmap(bmp);

								 message.addView(img);*/
								message.setType(message.getType("RECEIVE"));

								//time
								Date currentTime = Calendar.getInstance().getTime();
								message.setTimeText(currentTime.getHours() + ":" + currentTime.getMinutes());
								outputLayoutScrollLay.addView(message);

								//outputLayoutScroll.scrollTo(0, outputLayoutScroll.getBottom());
								outputLayoutScroll.post(new Runnable() {
										@Override
										public void run() {
											outputLayoutScroll.fullScroll(View.FOCUS_DOWN);
										}
									});
							}

							break;
					}
					return true;
				}
			});

		//views
		mainLayout = (LinearLayout) findViewById(R.id.chatLinearLayout);

		int inputHeight = utils.getScreenHeight() / 4;

		outputLayout = (LinearLayout) findViewById(R.id.chat_output);
		outputLayout.setLayoutParams(new LinearLayout.LayoutParams(utils.getScreenWidth(), utils.getScreenHeight() - inputHeight));

		outputLayoutScroll = (ScrollView) findViewById(R.id.chatOutputScroll);
		outputLayoutScrollLay = (LinearLayout) findViewById(R.id.chatOutputScrollLayout);

		//input
		inputLayout = (LinearLayout) findViewById(R.id.chatLinearLayoutInput);
		inputLayout.setLayoutParams(new LinearLayout.LayoutParams(utils.getScreenWidth(), inputHeight));

		//field input
		input = (EditText) findViewById(R.id.chat_input);
		input.setLayoutParams(new LinearLayout.LayoutParams(utils.getScreenWidth() - utils.getScreenWidth() / 6 - utils.getScreenWidth() / 20, inputHeight / 3));
		input.setPadding(utils.getScreenWidth() / 70, 0, utils.getScreenWidth() / 70, 0);

		//send button
		send = (Button) findViewById(R.id.sendButton);
		LinearLayout.LayoutParams paramsSend = new LinearLayout.LayoutParams(utils.getScreenWidth() / 6, inputHeight / 4);
		paramsSend.setMargins(utils.getScreenWidth() / 20 - utils.getScreenWidth() / 50, (inputHeight / 3 - inputHeight / 4) / 2, 0, 0);
		send.setLayoutParams(paramsSend);
		send.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					if(!input.getText().toString().trim().isEmpty()) {
						conn.write(input.getText().toString().getBytes());
						//output.setText(output.getText() + "\n" + input.getText().toString());

						//object message
						MessageContent message = new MessageContent(Chat.this);
						message.setMessageText(input.getText().toString());
						message.setType(message.getType("SEND"));

						//time
						Date currentTime = Calendar.getInstance().getTime();
						message.setTimeText(currentTime.getHours() + ":" + currentTime.getMinutes());
						outputLayoutScrollLay.addView(message);

						input.setText(null);

						outputLayoutScroll.post(new Runnable() {
								@Override
								public void run() {
									outputLayoutScroll.fullScroll(View.FOCUS_DOWN);
								}
							});
					}
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

	//create a dialog
	private void comfirmCloseConn() {
		AlertDialog.Builder dlg = new AlertDialog.Builder(Chat.this);
		dlg.setTitle("Parar conexão?");
		dlg.setPositiveButton("sim", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2) {
					conn.write("{#close_connection#}".getBytes());
					closeConn();
				}
			});
		dlg.setNegativeButton("não", null);
		dlg.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
			case event.KEYCODE_BACK:
				comfirmCloseConn();
				break;
		}
		return false;
	}

	//close the connection and activity
	private void closeConn() {
		AlertDialog.Builder dlgCancel = new AlertDialog.Builder(Chat.this);
		dlgCancel.setTitle("Conexão finalizada");
		dlgCancel.setPositiveButton("ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2) {
					conn.cancel();
					finish();
				}
			});
		dlgCancel.setCancelable(false);
		dlgCancel.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
				/*case android.R.id.home:
				 comfirmCloseConn();
				 break;*/

			case R.id.upload_menu:
				Bitmap icon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.ic_launcher);

				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				icon.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
				byte image[] = bytes.toByteArray();
				conn.write(image);
				break;
		}
		return true;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.menu_chat, menu);
		return super.onCreateOptionsMenu(menu);
	}



}
