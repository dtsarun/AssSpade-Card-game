package com.cardGame;

public class DrawPic
{
	public boolean isVisible = false;
	public int x1 = 0;
	public int y1 = 0;
	public int x2 = 0;
	public int y2 = 0;	
	
	public DrawPic(int a1,int b1,int a2,int b2,boolean v)
	{
		x1 = a1;
		x2 = a2;
		y1 = b1;
		y2 = b2;
		isVisible = v;
	}
}