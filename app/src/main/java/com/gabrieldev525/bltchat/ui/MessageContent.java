package com.gabrieldev525.bltchat.ui;

import android.app.*;
import android.widget.*;
import android.graphics.*;
import com.gabrieldev525.bltchat.*;

public class MessageContent extends LinearLayout 
{
	//context
	private Activity ctx;
	private Utils utils;
	
	private String types[] = {"SEND", "RECEIVE"};
	private int type = 0;
	
	//size
	private int contentWidth, contentHeight;
	
	//views
	private TextView messageText;
	private TextView timeText;
	
	//params
	LinearLayout.LayoutParams params;
	LinearLayout.LayoutParams timeTextParams;
	
	public MessageContent(Activity ctx) {
		super(ctx);
		
		this.ctx = ctx;
		utils = new Utils(ctx);
		
		//size
		contentWidth = utils.getScreenWidth() / 2 - utils.getScreenWidth() / 14;
		
		measure(0, 0);
		
		//views
		params = new LayoutParams(contentWidth, LayoutParams.WRAP_CONTENT);
		params.setMargins(utils.getScreenWidth() / 30, utils.getScreenHeight() / 80, 0, 0);
		setLayoutParams(params);
		
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundDrawable(getResources().getDrawable(R.drawable.message));
		setPadding(utils.getScreenWidth() / 40, utils.getScreenHeight() / 120, utils.getScreenWidth() / 40, utils.getScreenHeight() / 120);
		
		//content
		messageText = new TextView(ctx);
		messageText.measure(0, 0);
		messageText.setTextColor(Color.BLACK);
		addView(messageText);
		
		//time of message
		timeText = new TextView(ctx);
		
		timeText.measure(0, 0);
		
		timeTextParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		timeTextParams.setMargins(/*contentWidth - timeText.getMeasuredWidth()*/0, utils.getScreenHeight() / 90, 0, 0);
		
		timeText.setLayoutParams(timeTextParams);
		addView(timeText);
	}
	
	/**
	  * setMessageText()
	  *
	  * @params text (String) - the content of message
	  */
	public void setMessageText(String text) {
		messageText.setText(text);
	}
	
	public void setTimeText(String text) {
		timeText.setText(text);
		
		//position on right bottom
		//timeTextParams.setMargins(/*contentWidth - timeText.getMeasuredWidth()*/0, utils.getScreenHeight() / 60, 0, 0);
		//timeText.setLayoutParams(timeTextParams);
		
		//removeView(timeText);
		//addView(timeText);
	}
	

	//getter and setter
	public void setType(int type) {
		if(type > types.length - 1) {
			type = 0;
		} else {
			this.type = type;
		}
		
		//send
		if(type == getType("SEND")) {
			params.setMargins(utils.getScreenWidth() - contentWidth - utils.getScreenWidth() / 30, utils.getScreenHeight() / 80, 0, 0);
			setLayoutParams(params);
		} 
	}

	public int getCurrentType() {
		return type;
	}
	
	/**
	  * getType() - return the index of the type of a message
	  *
	  * @param type(String) - the type in upper case
	  *
	  * @return (int) - the index of type. case don't exists return -1
	  */
	public int getType(String type) {
		for(int a = 0; a < types.length; a++) {
			if(types[a].equals(type))
				return a;
		}
		
		return -1;
	}
}
