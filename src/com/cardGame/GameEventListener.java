package com.cardGame;

public interface GameEventListener
{
	public final static int GE_NONE = 0;	
	public final static int GE_EXIT = 1;	
	public final static int GE_RESTART = 2;
	public final static int GE_REMOTERESTART = 3;	
	
	public void handleGameEvent(int msg);
	public void ToastMessage(String s);
}