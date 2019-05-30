package com.gabrieldev525.bltchat;

import android.app.*;

public class Utils 
{
	private int screenWidth, screenHeight;
	
	//context
	private Activity ctx;
	
	public Utils(Activity ctx) {
		this.ctx = ctx;
		
		//screen size
		screenWidth = ctx.getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = ctx.getWindowManager().getDefaultDisplay().getHeight();
		
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getScreenHeight() {
		return screenHeight;
	}
}
