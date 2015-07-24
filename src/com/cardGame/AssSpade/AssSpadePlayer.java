package com.cardGame.AssSpade;

import java.util.*;
import com.google.android.gms.games.multiplayer.Participant;

import android.util.Log;

public abstract class AssSpadePlayer
{
	private boolean m_focus;
	protected Vector m_cards = new Vector(); 
	private int slot;
	protected int numOfPlayers = 0;
	protected int thisPlayer = -1;	
	
	String pName ;
	
	protected static final String MODULE = "GAME";
	
//	public Participant mParticipant;
	
	
	AssSpadePlayer(String name)
	{
//		mParticipant = p;
		pName = name;
	}
	
	AssSpadePlayer(int npl, String name)
	{
		numOfPlayers = npl;
		pName = name;
	}
	
	public String getName()
	{
		return pName;
	}
	
	public void setSlot(int s)
	{
		slot = s;
	}
	
	public void setPlayerIndex(int x)
	{
		thisPlayer = x;
	}
	
	public int getPlayerIndex()
	{
		return thisPlayer;
	}
	
	public abstract int play();
	
	public void setFocus()
	{
		m_focus = true;
	}
	
	public void takeFocus()
	{
		m_focus = false;
	}
	
	public boolean getFocus()
	{
		return m_focus;
	}
	
	public void addCard(int card)
	{
		m_cards.addElement(new Integer(card));
	}
	
	public void clearCards()
	{
		m_cards.clear();
	}
	
	public void removeCard(int card)
	{
		Integer i = null;
		Enumeration e = m_cards.elements();
		while(e.hasMoreElements())
		{
			Integer c = (Integer)e.nextElement();
			if(c.intValue() == card)
				i = c;
		}		
		m_cards.removeElement(i);
		Log.i(MODULE,"removeCard i is " + i + " size now is " + m_cards.size());
	}
	
	public boolean hasCard(int card)
	{
		Enumeration e = m_cards.elements();
		while(e.hasMoreElements())
		{
			int val = (int)((Integer)e.nextElement()).intValue();
			if(val == card)
				return true;
		}
		return false;
	}
	
	public int getSlot()
	{
		return slot;
	}
	
	public int numOfCards()
	{
		return m_cards.size();
	}
	
	public Vector getCards()
	{
		return m_cards;
	}
	
	public void move(int c)
	{
		
	}
	
}