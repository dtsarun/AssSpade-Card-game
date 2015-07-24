package com.cardGame;

public interface ScreenListener
{
	public static final int ACTION_DOWN = 0;
	public static final int ACTION_MOVE = 1;
	public static final int ACTION_UP = 2;
	
	public void onScreenTouched(int w, float x, float y);
}
