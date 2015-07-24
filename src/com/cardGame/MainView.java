package com.cardGame;

import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.view.View;
import android.content.res.Resources;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Paint.Style;
import android.os.Parcelable;
import android.os.Bundle;

import android.util.Log;

import java.util.*;

public class MainView extends View
{	
	private int mCurrentScreen = 0;
	private final int sx = 0;
	private final int sy = 1;
	private final int ex = 2;
	private final int ey = 3;
	public int numOfqPlayers = 3;
	public int numOfaPlayers = 0;
	public int numOfrPlayers = 3;
	
	private float bigButton1[] = new float[4];
	private float bigButton2[] = new float[4];
	private float bigButton3[] = new float[4];
	private float questionButton[] = new float[4];
	private float closeButton[] = new float[4];
	
	private float arrowUp1[] = new float[4];
	private float arrowDown1[] = new float[4];
	private float arrowUp2[] = new float[4];
	private float arrowDown2[] = new float[4];
	private float arrowUp3[] = new float[4];
	private float arrowDown3[] = new float[4];
	
	private float num1[] = new float[4];
	private float num2[] = new float[4];
	private float num3[] = new float[4];
	
	private float x_unit,y_unit;

	private MainViewInterface mMainViewInterface;
	
	public MainView(Context context, AttributeSet attrs)
	{
		super(context, attrs);		
	}
	
	@Override        
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			if(x>bigButton1[sx] && x<bigButton1[ex] && mCurrentScreen == MainViewInterface.screen_main)
			{
				if(y>bigButton1[sy] && y<bigButton1[ey])
					mMainViewInterface.onClick(MainViewInterface.button_single_player);
				if(y>bigButton2[sy] && y<bigButton2[ey])
					mMainViewInterface.onClick(MainViewInterface.button_quick_game);
				if(y>bigButton3[sy] && y<bigButton3[ey])
					mMainViewInterface.onClick(MainViewInterface.invitation);	
			}
			if(x>arrowUp1[sx] && x<arrowUp1[ex] && mCurrentScreen == MainViewInterface.screen_main)
			{
				if(y>arrowUp1[sy] && y<arrowUp1[ey])
					decqPlayers();
				if(y>arrowUp2[sy] && y<arrowUp2[ey])
					decaPlayers();					
				if(y>arrowDown1[sy] && y<arrowDown1[ey])
					incqPlayers();
				if(y>arrowDown2[sy] && y<arrowDown2[ey])
					incaPlayers();					
			}
			
			if(x>arrowUp3[sx] && x<arrowUp3[ex] && mCurrentScreen == MainViewInterface.screen_main)
			{
				if(y>arrowUp3[sy] && y<arrowUp3[ey])
					decrPlayers();
				if(y>arrowDown3[sy] && y<arrowDown3[ey])
					incrPlayers();
			}
			
			if(y>closeButton[sy] && y<closeButton[ey])
			{
				if(x>closeButton[sx] && x<closeButton[ex] && (mCurrentScreen == MainViewInterface.screen_main || mCurrentScreen == MainViewInterface.screen_question) )
					mMainViewInterface.onClick(MainViewInterface.close);
				if(x>questionButton[sx] && x<questionButton[ex] && mCurrentScreen == MainViewInterface.screen_main)
					mMainViewInterface.onClick(MainViewInterface.question);
			}
			
			invalidate();
		}
		
		return true;
	}
	
	public void setMainViewListener(MainViewInterface m)
	{
		mMainViewInterface = m;
	}
	
	

    private void incqPlayers()
    {
    	numOfqPlayers = (numOfqPlayers <5) ? numOfqPlayers+1:numOfqPlayers;
    }
    
    private void decqPlayers()
    {
    	numOfqPlayers = (numOfqPlayers >2) ? numOfqPlayers-1:numOfqPlayers;
    }
    
    private void incaPlayers()
    {
    	if(numOfaPlayers<5)
    	{
    		if((numOfaPlayers + numOfrPlayers)<5)
    			numOfaPlayers++;
    		else if(numOfrPlayers>0)
    		{
    			numOfrPlayers--;
    			numOfaPlayers++;
    		}
    	}
    }
    
    private void decaPlayers()
    {
    	if(numOfaPlayers>0)
    	{
    		if((numOfaPlayers + numOfrPlayers)>2)
    			numOfaPlayers--;
    		else if(numOfrPlayers<5)
    		{
    			numOfrPlayers++;
    			numOfaPlayers--;
    		}
    	}
    }
    
    private void incrPlayers()
    {    	
    	if(numOfrPlayers<5)
    	{
    		if((numOfrPlayers + numOfaPlayers)<5)
    			numOfrPlayers++;
    		else if(numOfaPlayers>0)
    		{
    			numOfaPlayers--;
    			numOfrPlayers++;
    		}
    	}
    }
    
    private void decrPlayers()
    {
    	if(numOfrPlayers>0)
    	{
    		if((numOfrPlayers + numOfaPlayers)>2)
    			numOfrPlayers--;
    		else if(numOfaPlayers<5)
    		{
    			numOfaPlayers++;
    			numOfrPlayers--;
    		}
    	}
    }
    
	@Override  
    protected void onDraw(Canvas canvas)  
    {  
		super.onDraw(canvas);
		canvas.drawARGB(0xFF,0,0,0);
    
		Resources res = getResources();
		
		Paint greenPaint = new Paint();
		greenPaint.setColor(0xFF88FF88);
		greenPaint.setFakeBoldText(true);
		
		if(mCurrentScreen == MainViewInterface.screen_main)
		{
			Bitmap singleplayer = BitmapFactory.decodeResource(res,R.drawable.singleplayer);
			Bitmap multiplayer = BitmapFactory.decodeResource(res,R.drawable.multiplayer);
			Bitmap joingame = BitmapFactory.decodeResource(res,R.drawable.joingame);
			Bitmap question = BitmapFactory.decodeResource(res,R.drawable.question);
			Bitmap close = BitmapFactory.decodeResource(res,R.drawable.close);
			
			canvas.drawBitmap(singleplayer,new Rect(0,0,280,100),new Rect((int)bigButton1[sx], (int)bigButton1[sy], (int)bigButton1[ex], (int)bigButton1[ey]),null);
			canvas.drawBitmap(multiplayer,new Rect(0,0,280,100),new Rect((int)bigButton2[sx], (int)bigButton2[sy], (int)bigButton2[ex], (int)bigButton2[ey]),null);
			canvas.drawBitmap(joingame,new Rect(0,0,280,100),new Rect((int)bigButton3[sx], (int)bigButton3[sy], (int)bigButton3[ex], (int)bigButton3[ey]),null);
			canvas.drawBitmap(question,new Rect(0,0,200,200),new Rect((int)questionButton[sx], (int)questionButton[sy], (int)questionButton[ex], (int)questionButton[ey]),null);
			canvas.drawBitmap(close,new Rect(0,0,280,200),new Rect((int)closeButton[sx], (int)closeButton[sy], (int)closeButton[ex], (int)closeButton[ey]),null);
			
			Bitmap upBmp = BitmapFactory.decodeResource(res,R.drawable.up);
			Bitmap downBmp = BitmapFactory.decodeResource(res,R.drawable.down);
			
			canvas.drawBitmap(upBmp,new Rect(0,0,64,64),new Rect((int)arrowUp1[sx], (int)arrowUp1[sy], (int)arrowUp1[ex], (int)arrowUp1[ey]),null);
			canvas.drawBitmap(upBmp,new Rect(0,0,64,64),new Rect((int)arrowUp2[sx], (int)arrowUp2[sy], (int)arrowUp2[ex], (int)arrowUp2[ey]),null);
			canvas.drawBitmap(upBmp,new Rect(0,0,64,64),new Rect((int)arrowUp3[sx], (int)arrowUp3[sy], (int)arrowUp3[ex], (int)arrowUp3[ey]),null);
			canvas.drawBitmap(downBmp,new Rect(0,0,64,64),new Rect((int)arrowDown1[sx], (int)arrowDown1[sy], (int)arrowDown1[ex], (int)arrowDown1[ey]),null);
			canvas.drawBitmap(downBmp,new Rect(0,0,64,64),new Rect((int)arrowDown2[sx], (int)arrowDown2[sy], (int)arrowDown2[ex], (int)arrowDown2[ey]),null);
			canvas.drawBitmap(downBmp,new Rect(0,0,64,64),new Rect((int)arrowDown3[sx], (int)arrowDown3[sy], (int)arrowDown3[ex], (int)arrowDown3[ey]),null);
			
			canvas.drawText(""+numOfqPlayers,(int)(num1[sx]+num1[ex])/2,(int)(num1[sy]+num1[ey])/2,greenPaint);
			canvas.drawText(""+numOfaPlayers,(int)(num2[sx]+num2[ex])/2,(int)(num2[sy]+num2[ey])/2,greenPaint);
			canvas.drawText(""+numOfrPlayers,(int)(num3[sx]+num3[ex])/2,(int)(num3[sy]+num3[ey])/2,greenPaint);
			
			canvas.drawText("com",(int)(arrowUp1[sx] + (int)(x_unit/2)),(int)(y_unit*1.5),greenPaint);
			canvas.drawText("human",(int)(arrowUp3[sx] + (int)(x_unit/2)),(int)(y_unit*1.5),greenPaint);
		}
		else if(mCurrentScreen == MainViewInterface.screen_wait)
		{
			canvas.drawText("Please hold on...",(int)(num1[sx]+num1[ex])/2,(int)(num1[sy]+num1[ey])/2,greenPaint);
		}
		else if(mCurrentScreen == MainViewInterface.screen_question)
		{
			Bitmap instruction = BitmapFactory.decodeResource(res,R.drawable.instruction);
			Bitmap close = BitmapFactory.decodeResource(res,R.drawable.close);
			
			canvas.drawBitmap(instruction,new Rect(0,0,1920,1200),new Rect((int)0, (int)0, (int)(x_unit*20), (int)(y_unit*8)	),null);
			canvas.drawBitmap(close,new Rect(0,0,280,200),new Rect((int)closeButton[sx], (int)closeButton[sy], (int)closeButton[ex], (int)closeButton[ey]),null);
		}
		
		
    }
	
	public void computeScreen(int WIDTH_PIXEL, int HEIGHT_PIXEL)
	{
		
		x_unit = WIDTH_PIXEL/20;
		y_unit = HEIGHT_PIXEL/8;
		
		bigButton1[sx] = (float)x_unit*3;
		bigButton1[sy] = (float)y_unit*2;
		bigButton1[ex] = (float)x_unit*9;
		bigButton1[ey] = (float)y_unit*3;
		
		bigButton2[sx] = (float)x_unit*3;
		bigButton2[sy] = (float)y_unit*4;
		bigButton2[ex] = (float)x_unit*9;
		bigButton2[ey] = (float)y_unit*5;
		
		bigButton3[sx] = (float)x_unit*3;
		bigButton3[sy] = (float)y_unit*6;
		bigButton3[ex] = (float)x_unit*9;
		bigButton3[ey] = (float)y_unit*7;
		
		questionButton[sx] = (float)x_unit*15;
		questionButton[sy] = (float)y_unit*7;
		questionButton[ex] = (float)x_unit*16;
		questionButton[ey] = (float)y_unit*(float)7.5;
		
		closeButton[sx] = (float)x_unit*17;
		closeButton[sy] = (float)y_unit*7;
		closeButton[ex] = (float)x_unit*18;
		closeButton[ey] = (float)y_unit*(float)7.5;
		
		arrowUp1[sx] = (float)x_unit*12;
		arrowUp1[sy] = (float)y_unit*(float)1.5;
		arrowUp1[ex] = (float)x_unit*14;
		arrowUp1[ey] = (float)y_unit*2;
		
		arrowDown1[sx] = (float)x_unit*12;
		arrowDown1[sy] = (float)y_unit*3;
		arrowDown1[ex] = (float)x_unit*14;
		arrowDown1[ey] = (float)y_unit*(float)3.5;
		
		arrowUp2[sx] = (float)x_unit*12;
		arrowUp2[sy] = (float)y_unit*(float)3.5;
		arrowUp2[ex] = (float)x_unit*14;
		arrowUp2[ey] = (float)y_unit*4;
		
		arrowDown2[sx] = (float)x_unit*12;
		arrowDown2[sy] = (float)y_unit*5;
		arrowDown2[ex] = (float)x_unit*14;
		arrowDown2[ey] = (float)y_unit*(float)5.5;
		
		arrowUp3[sx] = (float)x_unit*16;
		arrowUp3[sy] = (float)y_unit*(float)3.5;
		arrowUp3[ex] = (float)x_unit*18;
		arrowUp3[ey] = (float)y_unit*4;
		
		arrowDown3[sx] = (float)x_unit*16;
		arrowDown3[sy] = (float)y_unit*5;
		arrowDown3[ex] = (float)x_unit*18;
		arrowDown3[ey] = (float)y_unit*(float)5.5;
		
		num1[sx] = (float)x_unit*12;
		num1[sy] = (float)y_unit*2;
		num1[ex] = (float)x_unit*14;
		num1[ey] = (float)y_unit*3;
		
		num2[sx] = (float)x_unit*12;
		num2[sy] = (float)y_unit*4;
		num2[ex] = (float)x_unit*14;
		num2[ey] = (float)y_unit*5;
		
		num3[sx] = (float)x_unit*16;
		num3[sy] = (float)y_unit*4;
		num3[ex] = (float)x_unit*18;
		num3[ey] = (float)y_unit*5;
	}
	
	public void setScreen(int screenId)
	{
		mCurrentScreen = screenId;		
	}
}