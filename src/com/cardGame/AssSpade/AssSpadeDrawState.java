package com.cardGame.AssSpade;

import java.util.*;

import com.cardGame.DrawState;
import com.cardGame.ScreenListener;
import com.cardGame.GameView;
import com.cardGame.GameData;
import com.cardGame.GameEventListener;
import com.cardGame.DrawPic;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class AssSpadeDrawState extends DrawState implements ScreenListener
{
	private static AssSpadeDrawState m_drawState;
	private Vector m_closedCards;
	private Vector m_openCards;
	private Vector m_tableCards;
	private Vector m_cards;
	private Vector m_dStrings;
	private int NUM_PLAYERS;
	private int GLOBAL_OFFSET = 0;
	private final int NUM_MAXPLAYER = 8,NUM_VERTICALCARDS = 8;	
	private DString dString = new DString();
	
	protected int WIDTH_PIXEL = 0;
	protected int HEIGHT_PIXEL = 0;
	private float OPEN_CARDX, OPEN_CARDY;
	private final float COLORX_FACT = (float) 0.2, COLORY_FACT = (float) 0.2, NUMX_FACT = (float) 0.5, NUMY_FACT = (float) 0.2;
	private GameView m_GameView;
	private float CARD_HEIGHT, CARD_WIDTH;
	private GameEventListener mGameEventListener;
	private Thread cardSlelectionThread;
	
	private float tableSlotX[] = new float[3];
	private float tableSlotY[] = new float[3];
	private float playerSlotX[] = new float[3];
	private float playerSlotY[] = new float[3];
	
	private final static String MODULE = "GAME";
	
	public final static int NONE = -1;
	public final static int SPADE = 0;
	public final static int HEARTS = 1;
	public final static int DIAMOND = 2;
	public final static int CLUB = 3;
	
	private UIMessageListener m_UIMessageListener;
	
	private int mCurTableColor = NONE;
	
	private List<DrawPic> picsList = new ArrayList();
	
	AssSpadeDrawState()
	{		
		
		//computeScreen();
	}
	
	public void initializeVectors()
	{
		m_closedCards = new Vector();
		m_openCards = new Vector();
		m_tableCards = new Vector();
		m_cards = new Vector();
		m_dStrings = new Vector();
		m_dStrings.add(dString);
		mCurTableColor = NONE;
		GLOBAL_OFFSET = 0;
		
	}
	
	public List<DrawPic> getDrawPics()
	{
		return picsList;
	}
	
	public void RegisterGameEventListenerr(GameEventListener g)
    {
    	mGameEventListener = g;
    }
	
	public void registerUIListener(UIMessageListener uIMessageListener)
	{
		m_UIMessageListener = uIMessageListener;
	}
	
	public void setToNull()
	{
		m_drawState = null;
	}
	
	protected void associateWithGView(GameView gv)
	{
		m_GameView = gv;
		m_GameView.setScreenListener(this);
		m_GameView.setDrawState(this);
	}
	
	public static AssSpadeDrawState getInstance()
	{
		if(m_drawState == null)
		{
			m_drawState = new AssSpadeDrawState();
		}
		return m_drawState;
	}
	
	public Vector getCards()
	{
		return m_cards;
	}
	
	public Vector getStrings()
	{
		return m_dStrings;
	}
			
	public int getCurTableColor()
	{
		return mCurTableColor;
	}
	
	
	private boolean isSuitePresent(Vector v,int color)
	{
		for(int i=0;i<v.size();i++)
		{
			Card c = (Card)v.elementAt(i);
			if(c.color == color) return true;
		}
		return false;
	}
	
	public void onScreenTouched(int state, float x, float y)
	{
		
		Log.i(MODULE,"Inside onScreenTouched x = "+x+"y = "+y+"OPEN_CARDX= "+OPEN_CARDX+"OPEN_CARDY = "+OPEN_CARDY);
		
		
		
		switch(state)
		{
		case ACTION_DOWN:
			if(y>OPEN_CARDY)
			{
				Card firstCard = (Card)m_openCards.elementAt(0);
				Card lastCard = (Card)m_openCards.elementAt(m_openCards.size()-1);
				
				if(x<OPEN_CARDX) 
					if(firstCard.startX < WIDTH_PIXEL/2 && (picsList.get(1).isVisible)) 
						GLOBAL_OFFSET++;
					else ;
				else if(x>(WIDTH_PIXEL - OPEN_CARDX) && (picsList.get(2).isVisible)) 
					if(lastCard.startX > WIDTH_PIXEL/2) 
						GLOBAL_OFFSET--; 
					else;
				else
				{
					Card c ;
					int n = (int)((x-OPEN_CARDX)/CARD_WIDTH)-GLOBAL_OFFSET;
					if(n>=0 && n<m_openCards.size())
					{
						c = (Card)m_openCards.elementAt(n);
						if(c.color == mCurTableColor || !isSuitePresent(m_openCards,mCurTableColor))
						{
							m_UIMessageListener.cardPlayedFromUI(c.color*13 + c.number - 2);
//							bCardSelected = true;
//							gSelectedCard = n;
						}
					}
				}
				
				updateOpenCards();
				AssSpadeManager.getInstance().refreshScreen();
			}
			else if( isWitihinRange(x,y,picsList.get(0)) )
			{
				mGameEventListener.handleGameEvent(GameEventListener.GE_EXIT);
			}
//			else if(isWitihinRange(x,y,picsList.get(1)) && picsList.get(1).isVisible)
//				mGameEventListener.handleGameEvent(GameEventListener.GE_RESTART);
			break;
		}
		
	}

	
	protected void setNumPlayers(int n)
	{
		NUM_PLAYERS = n; 
	}
	
	protected void setPlayerCardCnt(int slot,int nCard,String name)
	{
		Card crd = getPlayerCardBySlot(slot);
		crd.numOfCards = nCard;
		crd.Name = name;
	}
	
	protected void addTableCard(int slot, int card)
	{
		Card crd = addTableCardBySlot(slot);
		crd.color = card/13;
		crd.number = card%13 + 2;
		crd.colorX = crd.startX + CARD_WIDTH * COLORX_FACT;
		crd.colorY = crd.startY + CARD_HEIGHT * COLORY_FACT;
		crd.nX = crd.startX + CARD_WIDTH * NUMX_FACT;
		crd.nY = crd.startY + CARD_WIDTH * NUMY_FACT;
		addTableCard(crd);
		mCurTableColor = crd.color;
	}
	
	protected void addOpenCard(int card)
	{
		int offSet;
		if(m_openCards == null)
			offSet = 0;
		else
			offSet = m_openCards.size();
		
		Card c = new Card();
		c.startX = OPEN_CARDX + (float) offSet * CARD_WIDTH ;
		c.startY = OPEN_CARDY;
		c.endX = c.startX + CARD_WIDTH;
		c.endY = c.startY + CARD_HEIGHT;
		c.color = card/13;
		c.number = card%13 + 2;
		c.colorX = c.startX + CARD_WIDTH * COLORX_FACT;
		c.colorY = c.startY + CARD_HEIGHT * COLORY_FACT;
		c.nX = c.startX + CARD_WIDTH * NUMX_FACT;
		c.nY = c.startY + CARD_WIDTH * NUMY_FACT;
		//Log.i(MODULE,"x = " + c.startX + " y = " + c.startY);
		addOpenCard(c);		
	}
	
	protected Vector getTableCards()
	{
		return m_tableCards;
	}
	
	private void updateDirectionsVisible()
	{
		picsList.get(1).isVisible = false;
		picsList.get(2).isVisible = false;
		for(int i =0; i<m_openCards.size();i++)
		{
			Card c = (Card) m_openCards.elementAt(i);
			if(c.visible == false)
			{
				if(c.startX < OPEN_CARDX )
					picsList.get(1).isVisible = true;
				else if(c.endX >WIDTH_PIXEL - OPEN_CARDX)
					picsList.get(2).isVisible = true;
			}
				
		}
	}
	
	protected void removeOpenCard(int card)
	{
		Card c = getCardByVal(m_openCards, card);
		if(c !=null)
		{			
			removeOpenCard(c);			
			updateOpenCards();
		}
	}
	
	protected void flushTable()
	{
		mCurTableColor = NONE;
		if(m_tableCards != null)
		{
			Enumeration e = m_tableCards.elements();
			while(e.hasMoreElements())
			{
				Card c = (Card)e.nextElement();
				m_cards.remove(c);
			}
			m_tableCards.removeAllElements();			
		}
	}
	
	private Card getCardByVal(Vector v, int card)
	{
		if(v != null)
		{
			Enumeration e = v.elements();
			while(e.hasMoreElements())
			{
				Card c = (Card)e.nextElement();
				if(c.color == card/13 && (c.number == (card%13 + 2)))
					return c;
			}
		}
		return null;
	}
	
	public void updateOpenCards()
	{
		if(m_openCards != null)
		{
			//int offSet = m_openCards.size();
			int offSet = 0;
			Enumeration e = m_openCards.elements();
			while(e.hasMoreElements())
			{
				Card c = (Card)e.nextElement();
				c.startX = OPEN_CARDX + ((float) offSet + (float)GLOBAL_OFFSET) * CARD_WIDTH;
				c.startY = OPEN_CARDY;
				c.endX = c.startX + CARD_WIDTH;
				c.endY = c.startY + CARD_HEIGHT;				
				c.colorX = c.startX + CARD_WIDTH * COLORX_FACT;
				c.colorY = c.startY + CARD_HEIGHT * COLORY_FACT;
				c.nX = c.startX + CARD_WIDTH * NUMX_FACT;
				c.nY = c.startY + CARD_WIDTH * NUMY_FACT;
				if(c.startX < OPEN_CARDX || c.endX > (WIDTH_PIXEL - OPEN_CARDX))
					c.visible = false;
				else
					c.visible = true;
				offSet++;				
			}
		}
		updateDirectionsVisible();
		if(picsList.get(1).isVisible && picsList.get(2).isVisible)
			dString.str = "There are cards to your right and left. Its you turn.";
		else if(picsList.get(1).isVisible)
			dString.str = "There are cards to your left. Its you turn.";
		else if(picsList.get(2).isVisible)
		dString.str = "There are cards to your right. Its you turn.";
		else
			dString.str = "Its you turn.";
		if(!isHumanPlaying())
			dString.str = "";
	}
	
	private boolean isHumanPlaying()
	{
		return AssSpadeManager.getInstance().isHumanPlaying();
	}
	
	private Card getPlayerCardBySlot(int slot)
	{
		Card c;
		c = getCardByHandle(m_closedCards, slot);
		if(c == null)
		{
			c = new Card();
			c.handle = slot;
			c.startX = playerSlotX[getSlotX(slot)];
			c.startY = playerSlotY[getSlotY(slot)];
			c.endX = playerSlotX[getSlotX(slot)] + CARD_WIDTH;
			c.endY = playerSlotY[getSlotY(slot)] + CARD_HEIGHT;
			c.numCX = c.startX - CARD_WIDTH * (float) 0.4;
			if(slot == 4)
				c.numCY = c.endY + CARD_HEIGHT * (float) 0.2;
			else
				c.numCY = c.endY + CARD_HEIGHT * (float) 0.4;
			addClosedCard(c);
		}
		return c;
	}
	
	private Card addTableCardBySlot(int slot)
	{
		Card c = new Card();
		c.handle = slot;
		c.startX = tableSlotX[getSlotX(slot)];
		c.startY = tableSlotY[getSlotY(slot)];
		c.endX = tableSlotX[getSlotX(slot)] + CARD_WIDTH;
		c.endY = tableSlotY[getSlotY(slot)] + CARD_HEIGHT;
		return c;
	}
	
	protected void computeScreen()
	{
		Log.i(MODULE,"Inside computeScreen");
		
		CARD_HEIGHT = (HEIGHT_PIXEL/NUM_VERTICALCARDS) * (float)1.2;
		CARD_WIDTH = CARD_HEIGHT/((float)1.25);
		
		playerSlotY[0] = (float) HEIGHT_PIXEL * ((float)0/(float)NUM_VERTICALCARDS);
		playerSlotY[1] = HEIGHT_PIXEL * ((float)3.0/(float)NUM_VERTICALCARDS);
		playerSlotY[2] = HEIGHT_PIXEL * ((float)6.0/(float)NUM_VERTICALCARDS);
		playerSlotX[0] = WIDTH_PIXEL * (float) 0.1;
		playerSlotX[1] = WIDTH_PIXEL * (float) 0.45;
		playerSlotX[2] = WIDTH_PIXEL * (float) 0.8;
		
		tableSlotY[0] = HEIGHT_PIXEL * (float)(1.5/NUM_VERTICALCARDS);
		tableSlotY[1] = HEIGHT_PIXEL * (float)(3.0/NUM_VERTICALCARDS);
		tableSlotY[2] = HEIGHT_PIXEL * (float)(4.5/NUM_VERTICALCARDS);
		tableSlotX[0] = WIDTH_PIXEL * (float) 0.2;
		tableSlotX[1] = WIDTH_PIXEL * (float) 0.45;
		tableSlotX[2] = WIDTH_PIXEL * (float) 0.7;
		
		//OPEN_CARDX = WIDTH_PIXEL * (float) 0.2;
		//OPEN_CARDY = HEIGHT_PIXEL * (float)(6.5/NUM_VERTICALCARDS);
		OPEN_CARDX = CARD_WIDTH * (float) 1.5;
		OPEN_CARDY = HEIGHT_PIXEL * (float)(6.5/NUM_VERTICALCARDS);
		Log.i(MODULE,"HEIGHT_PIXEL = " + HEIGHT_PIXEL + " WIDTH_PIXEL = " + WIDTH_PIXEL);
		Log.i(MODULE,"OPEN_CARDX = " + OPEN_CARDX + " OPEN_CARDY = " + OPEN_CARDY);
		picsList.clear();
		dString.strX = 0;
		dString.strY = OPEN_CARDY + (float)(CARD_HEIGHT * 1.2);
		dString.str = "";
		addDrawPics();
	}
	
	private void addDrawPics()
	{
		DrawPic dp;
		int x1,y1,x2,y2;
		y1 = (int)(HEIGHT_PIXEL/2) - (int)(HEIGHT_PIXEL/20);
		y2 = (int)(HEIGHT_PIXEL/2) + (int)(HEIGHT_PIXEL/20); 
		x1 = (int)(WIDTH_PIXEL/2) - (int)(WIDTH_PIXEL/30);
		x2 = (int)(WIDTH_PIXEL/2) + (int)(WIDTH_PIXEL/30);
		dp = new DrawPic(x1,y1,x2,y2,true);
		picsList.add(dp);	

		
		y1 = (int)OPEN_CARDY;
		y2 = (int)(OPEN_CARDY+CARD_HEIGHT); 
		x1 = 0;
		x2 = (int)CARD_WIDTH;
		dp = new DrawPic(x1,y1,x2,y2,true);
		picsList.add(dp);		
		
		y1 = (int)OPEN_CARDY;
		y2 = (int)(OPEN_CARDY+CARD_HEIGHT); 
		x1 = (int)(WIDTH_PIXEL - CARD_WIDTH);
		x2 = (int)WIDTH_PIXEL;
		dp = new DrawPic(x1,y1,x2,y2,true);
		picsList.add(dp);			
	}	
	
	private boolean isWitihinRange(float x1, float y1, DrawPic dp)
	{
		int x = (int)x1;
		int y = (int)y1;
		if( x>=dp.x1 && x<=dp.x2 && y>=dp.y1 && y<=dp.y2)
			return true;
		return false;
	}
	
	private int getSlotX(int slot)
	{
		if(slot == 1 || slot == 2 || slot == 3)
			return 0;
		if(slot == 4 || slot == 8)
			return 1;
		if(slot == 5 || slot == 6 || slot == 7)
			return 2;
		return -1;
	}
	
	private int getSlotY(int slot)
	{
		if(slot == 3 || slot == 4 || slot == 5)
			return 0;
		if(slot == 2 || slot == 6)
			return 1;
		if(slot == 1 || slot == 7 || slot == 8)
			return 2;
		return -1;
	}
	
	private void addClosedCard(Card crd)
	{
		crd.closed = true;
		m_closedCards.addElement(crd);
		m_cards.addElement(crd);
	}
	
	private void addOpenCard(Card crd)
	{
		//Log.i(MODULE,"inside addOpenCard");
		crd.closed = false;
		crd.numOfCards = -1;
		
		int insertOpenCardIndex = -1;
		int crdVal = crd.color*13 + crd.number;
		for(int i = 0;i<m_openCards.size();i++)
		{
			Card c =(Card) m_openCards.elementAt(i);
			int toCheckVal = c.color*13 + c.number;
			if(toCheckVal > crdVal)
			{
				insertOpenCardIndex = i;
				break;
			}				
		}
		if(insertOpenCardIndex == -1)
			m_openCards.add(crd);
		else
		{
			Card c =(Card) m_openCards.elementAt(insertOpenCardIndex);
			crd.startX = c.startX;
			crd.endX = c.endX;
			m_openCards.insertElementAt(crd,insertOpenCardIndex);
		}
		for(int i = insertOpenCardIndex+1;i<m_openCards.size();i++)
		{
			Card c =(Card) m_openCards.elementAt(i);
			c.startX = c.startX + CARD_WIDTH;
			c.endX = c.endX + CARD_WIDTH;
		}
		m_cards.add(crd);
		updateOpenCards();		
	}
	
	private void removeOpenCard(Card crd)
	{
		m_openCards.remove(crd);
		m_cards.remove(crd);
	}
	
	private void addTableCard(Card crd)
	{
		crd.closed = false;
		crd.numOfCards = -1;
		m_tableCards.addElement(crd);
		m_cards.addElement(crd);
	}
	
	private Card getCardByHandle(Vector cards, int handle)
	{
		if (cards == null)
			return null;
		Enumeration e = cards.elements();
		while(e.hasMoreElements())
		{
			Card c = (Card)e.nextElement();
			if(c.handle == handle)
				return c;
		}
		return null;
	}
}