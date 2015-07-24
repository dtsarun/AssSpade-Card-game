package com.cardGame;

import android.graphics.Paint;

import java.util.*;

public abstract class DrawState 
{
	abstract public Vector getCards();
	
	abstract public Vector getStrings();
	
	abstract public List<DrawPic> getDrawPics();
	
	public static class Card
	{
		public int handle = 0;
		public float startX = (float) 0.0;
		public float startY = (float) 0.0;
		public float endX = (float) 0.0;
		public float endY = (float) 0.0;
		public boolean closed = false;
		public boolean visible = true;
		public int color = 0;
		public int number = 0;
		public float colorX = (float) 0.0;
		public float colorY = (float) 0.0;
		public float nX = (float) 0.0;
		public float nY = (float) 0.0;
		public int numOfCards = 0;
		public float numCX = (float) 0.0;
		public float numCY = (float) 0.0;
		public String Name;
	}
	
	public static class DString
	{
		public float strX;
		public float strY;
		public String str;		
	}
}