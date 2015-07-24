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
import com.cardGame.DrawPic;
import java.util.List;

import android.util.Log;

import java.util.*;

public class GameView extends View
{
	private Paint greenPaint, blackPaint;
	private DrawState m_drawState = null;
	private ScreenListener m_screenListener = null;
	private final int CWIDTH = 73;
	private final int CHEIGHT = 98;
	
	private final static String MODULE = "GameView:";
	
	public GameView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		greenPaint = new Paint();
		greenPaint.setColor(0xFF00FF00);
		blackPaint = new Paint();
		blackPaint.setColor(0xFF88FF88);
		blackPaint.setFakeBoldText(true);
	}
	
	private Rect getRect(int number,int color)
	{
		int x1 = (number-1)%13;
		int x2 = (number == 13)?number:number%13;
		int y1 = (color+3)%4;
		int y2 = (color == 0)?4:(color)%4;
		return(new Rect((int)x1*CWIDTH,(int)y1*CHEIGHT,(int)x2*CWIDTH,(int)y2*CHEIGHT) );
	}
	
	@Override  
    protected void onDraw(Canvas canvas)  
    {  
		super.onDraw(canvas);
		canvas.drawARGB(0xFF,0,0,0);
		
		Log.i(MODULE,"inside onDraw");
		
		if(m_drawState != null)
		{			
			synchronized(m_drawState)
			{
				
				Resources res = getResources();
				Bitmap cards = BitmapFactory.decodeResource(res, R.drawable.cards);
				Bitmap closedCard = BitmapFactory.decodeResource(res, R.drawable.closedcard);
				Bitmap left = BitmapFactory.decodeResource(res, R.drawable.left);
				Bitmap right = BitmapFactory.decodeResource(res, R.drawable.right);
				Bitmap close = BitmapFactory.decodeResource(res, R.drawable.close);				
				
				Vector v = m_drawState.getCards();				
				
				List<DrawPic> drawPics = m_drawState.getDrawPics();
				
				List<Bitmap> bmps = new ArrayList();
				bmps.add(close);				
				bmps.add(left);
				bmps.add(right);
				
				for(int i =0;i<drawPics.size();i++)
				{
					DrawPic dp = drawPics.get(i);
					if(dp.isVisible)
					{
						canvas.drawBitmap((Bitmap)bmps.get(i),new Rect(0,0,200,200),new Rect((int)dp.x1,(int)dp.y1,(int)dp.x2,(int)dp.y2),null);
					}
				}
				//Log.i(MODULE,"Size = " + v.size());
				if(v.size() > 0)
				{
					Enumeration e = v.elements();
					while(e.hasMoreElements())
					{
						DrawState.Card card = (DrawState.Card)e.nextElement();
						if(card.visible)
						if(card.closed == true)
						{
							//Log.i(MODULE,"CLOSED x = " + card.startX + " y = " + card.startY);
							canvas.drawBitmap(closedCard,new Rect(0,0,150,210),new Rect((int)card.startX, (int)card.startY, (int)card.endX, (int)card.endY),null);
							canvas.drawText("" + card.Name.substring(0, Math.min(card.Name.length(), 16)) + "(" + card.numOfCards + ")", card.numCX, card.numCY, blackPaint);
						}
						else
						{
							int n = card.number;
							int c = card.color;
							//Log.i(MODULE,"OPEN x = " + card.startX + " y = " + card.startY);
							canvas.drawBitmap(cards,getRect(card.number,card.color),new Rect((int)card.startX, (int)card.startY, (int)card.endX, (int)card.endY),null);
							//canvas.drawText("" + card.color, card.colorX, card.colorY, blackPaint);
							//canvas.drawText("" + card.number, card.nX, card.nY, blackPaint);
							
						}					
					}
				}
				
				v =  m_drawState.getStrings();
				if(v.size() > 0)
				{
					Enumeration e = v.elements();
					while(e.hasMoreElements())
					{
						DrawState.DString dstr = (DrawState.DString)e.nextElement();
						canvas.drawText("" + dstr.str, dstr.strX, dstr.strY, blackPaint);	
						
					}
				}				
			}
		}
			
    }
	
	@Override        
	public boolean onTouchEvent(MotionEvent event)
	{
		Log.i(MODULE,"OnTouchEvent --- hope this comes");
		int action = event.getAction();
		if(m_screenListener != null)
		{	
			if(action == MotionEvent.ACTION_DOWN)
			{
				m_screenListener.onScreenTouched(m_screenListener.ACTION_DOWN, event.getX(), event.getY());
			}
		
			if(action == MotionEvent.ACTION_MOVE)
			{
				m_screenListener.onScreenTouched(m_screenListener.ACTION_MOVE, event.getX(), event.getY());
			}
			
			if(action == MotionEvent.ACTION_UP)
			{
				m_screenListener.onScreenTouched(m_screenListener.ACTION_UP, event.getX(), event.getY());
			}
		}
		return true;
	}
	
	public void setScreenListener(ScreenListener sc)
	{
		m_screenListener = sc;
	}
	
	public void setDrawState(DrawState ds)
	{
		m_drawState = ds;
	}
}