package com.cardGame.AssSpade;
import com.google.android.gms.games.multiplayer.Participant;

import java.util.*;

import android.util.Log;

public class AssSpadeHPlayer extends AssSpadePlayer implements UIMessageListener
{
	Object lock = new Object();
	private int g_card=-1;
	
//	public AssSpadeHPlayer()
//	{
//		AssSpadeDrawState.getInstance().registerUIListener(this);
//	}
	
	public AssSpadeHPlayer(String s)
	{
		super(s);
		AssSpadeDrawState.getInstance().registerUIListener(this);
	}
	
	@Override
	public void addCard(int c)
	{
		super.addCard(c);
		AssSpadeDrawState.getInstance().addOpenCard(c);
		Log.i(MODULE,"Added card color = " + c/13 + "val = " + c%13);
	}
	
	@Override
	public void removeCard(int c)
	{
		super.removeCard(c);
		AssSpadeDrawState.getInstance().removeOpenCard(c);
		Log.i(MODULE,"Removed card color = " + c/13 + "val = " + c%13);
	}
	
	public int play()
	{
		/*try
		{
			Thread.sleep(1000);
			if(m_cards.size()>0) 
			{
				Log.i("### ASSSPADE ###","Human played");
				return (int)((Integer)m_cards.remove(0)).intValue();
			}			
		}
		catch(Exception e){}*/
		synchronized(lock)
		{
			try
			{
				lock.wait();
				if(m_cards.size()>0) 
				{
					Log.i("### ASSSPADE ###","Human played");
					//int c = (int)((Integer)m_cards.elementAt(0)).intValue();
					removeCard(g_card);
					AssSpadeDrawState.getInstance().updateOpenCards();
					return g_card;
				}
			}
			catch(Exception e){}
		}
		return -1;
	}
	
	public void cardPlayedFromUI(int card)
	{
		Log.i("### ASSSPADE ###","Message from UI");
		synchronized(lock)
		{
			g_card = card;
			lock.notifyAll();
		}
	}
}