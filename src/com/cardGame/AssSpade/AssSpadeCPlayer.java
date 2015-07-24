package com.cardGame.AssSpade;

import android.util.Log;
import java.util.ArrayList;
import java.util.Enumeration;

public class AssSpadeCPlayer extends AssSpadePlayer
{
	int playerCards[][];
	int tableCards[];
	int displacedCards[];
	int exposedCards[];
	
	private final int NONE = -1;
	private final int SPADE = 0;
	private final int HEART = 1;
	private final int DIAMOND = 2;
	private final int CLUBS = 3;
	
	private int m_RoundFirstPlayer;
	private int m_tableColor;
	
	
	private final int NEXT_PLAYER_NO_CARD_FACT1 = -5;
	private final int NEXT_PLAYER_NO_CARD_EXPOSED = -100;
	private final int NEXT_PLAYER_NO_CARD_NOT_EXPOSED = -200;
	private final int SOME_PLAYER_NO_CARD_TRAP_FACT = 20;
	private final int SOME_PLAYER_NO_CARD_FACT1 = -2;
	private final int NORMAL_PLAY_FACT = 3;
	private final int LAST_TURN_FACT_LARGE = 3;
	private final int LAST_TURN_FACT_SMALL = 5;
	

	private final int NEXT_PLAYER_NO_CARD = 0;
	private final int SOME_PLAYER_NO_CARD = 1;
	private final int EVERY_PLAYER_HAS_CARD	= 2;	
	private final int LAST_PLAYER_OF_ROUND = 3;
	
	private class CardPointPair
	{
		public int card;
		public int pnt;
		CardPointPair(int c,int p)
		{
			card = c;
			pnt = p;
		}
		
	}
	
	public AssSpadeCPlayer(int npl, String s)
	{
		super(npl,s);		
		playerCards = new int[numOfPlayers][4];
		tableCards = new int[4];
		displacedCards = new int[4];
		exposedCards = new int[4];
		m_RoundFirstPlayer = m_tableColor = NONE;		
		for(int i=0;i<numOfPlayers;i++)
		{
			for(int j=0;j<4;j++)
			{
				playerCards[i][j] = 0x1fff;
				tableCards[j] = 0;
				displacedCards[j]=0;
			}			
		}
		
	}
	
	@Override
	public void setPlayerIndex(int x)
	{
		super.setPlayerIndex(x);
		for(int i=0;i<4;i++)
		playerCards[thisPlayer][i]=0;
	}
	
	@Override
	public void addCard(int c)
	{
		super.addCard(c);
		AssSpadeDrawState.getInstance().setPlayerCardCnt(getSlot(),numOfCards(),pName);
		Log.i(MODULE,"Slot = " + getSlot() + "value = " + numOfCards());
		addCardData(thisPlayer,c);
	}
	
	@Override
	public void removeCard(int c)
	{
		super.removeCard(c);
		AssSpadeDrawState.getInstance().setPlayerCardCnt(getSlot(),numOfCards(),pName);
		Log.i(MODULE,"Slot = " + getSlot() + "value = " + numOfCards());
	}
	
	public void notifyCardPlayed(int pl,int c)
	{
		Log.i("### ASSSPADE ###","card is " +c);
		removeCardData(c);
		if((tableCards[0] | tableCards[1] | tableCards[2] | tableCards[3]) == 0 )
		{
			m_RoundFirstPlayer = pl;
			m_tableColor = c/13;
		}
		
		if(c/13 != m_tableColor)
			playerCards[pl][m_tableColor] = 0;
		tableCards[c/13] |= 0x1 << c%13;
	}
	
	public void flushTable()
	{
		m_RoundFirstPlayer = NONE;
		m_tableColor = NONE;
		for(int i=0;i<4;i++)
		{
			displacedCards[i] |= tableCards[i];		
			Log.i("### ASSSPADE ###","table cards " + tableCards[i] + " , " + i);
			tableCards[i] = 0;
		}
	}
	
	public void flushTable(int pl)
	{
		m_RoundFirstPlayer = NONE;
		m_tableColor = NONE;
		for(int i=0;i<4;i++)
		{
			playerCards[pl][i] |= tableCards[i];
			exposedCards[i] |= tableCards[i];
			tableCards[i] = 0;
		}
	}
	
	public int play()
	{
		Log.i("### ASSSPADE ###","computer play");
		int retCard = -1;
		
		CardPointPair bestCrdPP = null;
		if(m_tableColor == NONE)
		{			
			CardPointPair cpp;
			for(int i=0;i<4;i++)
			{
				
				cpp = getBestCardForSuite(i);
				if(bestCrdPP == null || cpp.pnt > bestCrdPP.pnt)
					bestCrdPP = cpp;
			}
			retCard = bestCrdPP.card;
		}
		else if(playerCards[thisPlayer][m_tableColor] != 0)
		{			
			bestCrdPP = getBestCardForSuite(m_tableColor);
			retCard = bestCrdPP.card;
		}
		else
		{			
			retCard = getBestCutCard();			
		}
		
		removeCard(retCard);
		return retCard;
	}
	
	private CardPointPair getBestCardForSuite(int color)
	{
		int i;
		boolean flag = false;
		ArrayList<boolean[]> plCards = new ArrayList();
		ArrayList<Integer> noCardsList = new ArrayList();
		
		for(i=thisPlayer;i<numOfPlayers;i++)
		{
			if(m_RoundFirstPlayer == i && i != thisPlayer)
			{
				flag = true;
				break;
			}
			boolean[] crds = null;
			if((crds = getCardArray_pl_suite(i,color)) != null)
			{
				plCards.add(crds);
				if(playerCards[i][color] == 0)
					noCardsList.add(plCards.indexOf(crds));
			}
		}
		if(!flag)
		{
			for(i=0;i<thisPlayer;i++)
			{
				if(m_RoundFirstPlayer == i)
					break;
				//same as above
				boolean[] crds = null;
				if((crds = getCardArray_pl_suite(i,color)) != null)
				{
					plCards.add(crds);
					if(playerCards[i][color] == 0)
						noCardsList.add(plCards.indexOf(crds));
				}
			}
		}
		
		int bestCardPnt = -10000, bestCard = -1,crdPnt;
		
		boolean[] thisPlayercrds = plCards.get(0);
		
		boolean[] ff = getCardArray_pl_suite(thisPlayer, color);		
		
		Enumeration e = m_cards.elements();
		while(e.hasMoreElements())
		{
			Integer c = (Integer)e.nextElement();			
		}		
		
		if(plCards.size() > 1)
		{			
			if(noCardsList.size() > 0)
			{
				for(i=0;i<13-numberOfBits(displacedCards[color]);i++)
				{
					if(thisPlayercrds[i])
					{
						crdPnt = solveForStrategy(plCards,i
													,(noCardsList.get(0) == 1) ? NEXT_PLAYER_NO_CARD : SOME_PLAYER_NO_CARD,color);
						if(crdPnt > bestCardPnt)
						{
							bestCardPnt = crdPnt;
							bestCard = i;
						}
					}
				}
				
			}
			else
			{
				for(i=0;i<13-numberOfBits(displacedCards[color]);i++)
				{
					if(thisPlayercrds[i])
					{
						crdPnt = solveForStrategy(plCards,i
													,EVERY_PLAYER_HAS_CARD,color);
						if(crdPnt > bestCardPnt)
						{
							bestCardPnt = crdPnt;
							bestCard = i;
						}
					}
				}
			}
		}
		else
		{
			for(i=0;i<13-numberOfBits(displacedCards[color]);i++)
			{
				if(thisPlayercrds[i])
				{
					crdPnt = solveForStrategy(plCards,i
												,LAST_PLAYER_OF_ROUND,color);
					if(crdPnt > bestCardPnt)
					{
						bestCardPnt = crdPnt;
						bestCard = i;
					}					
				}
			}
		}
		for(i=bestCard+1;i<13;i++)
		{
			if(thisPlayercrds[i])
				bestCard = i;
			else
				break;
		}
		CardPointPair retcpp = new CardPointPair(normalizedToOriginal(bestCard,color) + color*13,bestCardPnt);
		return retcpp;
	}
	
	private boolean nextPlayerHasCardOnlyGreater(ArrayList<boolean[]> plCards,int crd)
	{
		boolean[] nextPlayercrds = plCards.get(1);
		for(int i=0; i< crd; i++)
		if(nextPlayercrds[i]) return false;
		return true;
	}
	
	private int solveForStrategy(ArrayList<boolean[]> plCards, int crd, int strategy, int color)
	{
		int ret=-1000;
		
		switch(strategy)
		{
		case NEXT_PLAYER_NO_CARD:
			
			if(normalizedToOriginal(crd,color) > tableMaxCard(color))
			{
				ret = 0;
				ret += crd*NEXT_PLAYER_NO_CARD_FACT1;
				ret += exposedCard(normalizedToOriginal(crd,color),color) ? NEXT_PLAYER_NO_CARD_EXPOSED : NEXT_PLAYER_NO_CARD_NOT_EXPOSED;
			}
			else
			{
				
				ret = -crd*NEXT_PLAYER_NO_CARD_FACT1;
			}
			break;
		case SOME_PLAYER_NO_CARD:
			if(nextPlayerHasCardOnlyGreater(plCards,crd))
			{
				ret = 0;
				ret += crd*SOME_PLAYER_NO_CARD_TRAP_FACT;
			}
			else
			{
				if(normalizedToOriginal(crd,color) > tableMaxCard(color))
				{
					ret = 0;
					ret += crd*SOME_PLAYER_NO_CARD_FACT1;
				}
				else
					ret = -crd*SOME_PLAYER_NO_CARD_FACT1;
			}
			break;
		case EVERY_PLAYER_HAS_CARD:
			
			if(normalizedToOriginal(crd,color) > tableMaxCard(color))
			{
				
				float ideal;
				ideal = ((float)(13 - numberOfBits(displacedCards[color]) - numberOfBits(playerCards[thisPlayer][color]) ) )/((float)(numOfPlayers - numOfCompletedPlayers() - 1));
				if(ideal > 2)
				{
					ret = crd*NORMAL_PLAY_FACT;
				}
				else
				{
					
					float normalizedIdeal = (float)((ideal)*(13 - numberOfBits(displacedCards[color])  - numberOfBits(playerCards[thisPlayer][color]) ));
					ret = 0;
					ret += Math.abs(normalizedIdeal - normalizedToOriginal(crd,color)) * NORMAL_PLAY_FACT;
				}
			}
			else
				ret = crd*NORMAL_PLAY_FACT;			
				
			break;
		case LAST_PLAYER_OF_ROUND:
			if(normalizedToOriginal(crd,color) > tableMaxCard(color))
			{
				ret = crd * LAST_TURN_FACT_LARGE;
			}
			else
				ret = crd * LAST_TURN_FACT_SMALL;
			
			break;
		}
		return ret;
	}
	
	private int numOfCompletedPlayers()
	{
		int cnt = 0;
		for(int i=0; i<numOfPlayers;i++)
		{
			if((playerCards[i][SPADE] | playerCards[i][HEART] | playerCards[i][DIAMOND] | playerCards[i][CLUBS]) == 0)
				cnt++;
		}
		return cnt;
	}
	
	private int tableMaxCard(int color)
	{
		int r = -1;
		for(int i = 0;i < 13;i++)
			if((tableCards[color] & (0x1 << i)) != 0)
				r = i;
		return r;
	}
	
	private boolean exposedCard(int crd,int color)
	{
		return ( (exposedCards[color] & (0x1 << crd)) != 0 );
	}
	
	int normalizedToOriginal(int crd,int color)
	{
		int r = crd;
		int i = 0;
		while(i<=r)
		{
			if((displacedCards[color] & (0x1 << i)) != 0)
				r++;
			i++;
		}
		return r;
	}
	
	private int getBestCutCard()
	{
		int bstColor=NONE,bstCard=NONE;
		for(int i=0;i<4;i++)
		{
			for(int j=0;j<13;j++)
			{
				if( ((playerCards[thisPlayer][i] >> j) & 0x1) != 0 )
					if(bstCard < j)
					{
						bstColor = i;
						bstCard = j;
					}
			}
		}
		return (bstColor*13 + bstCard);
	}
	
	private int numberOfBits(int x)
	{
		int r = 0;
		for(int i = 0;i<32;i++)
		{
			if((x & 0x1) == 1)
				r++;
			x = x >> 1;
		}
		return r;
	}
	
	private boolean[] getCardArray_pl_suite(int player,int color)
	{
		if((playerCards[player][0] | playerCards[player][1] | playerCards[player][2] | playerCards[player][3])==0)
			return null;
		boolean[] cards = new boolean[13];
		int dcrds = 0;
		for(int i=0;i<13;i++)
		{
			//boolean val = (((0x1 << i) & playerCards[player][color]) != 0);
			boolean val = ((0x1 & (playerCards[player][color] >> i)) != 0);
			if( ((0x1 << i) & displacedCards[color]) == 0 ) 
				cards[dcrds++] = val;			
		}
		return cards;
	}
	
	private void addCardData(int pl, int c)
	{
		removeCardData(c);
		playerCards[pl][c/13] |= 0x1 << c%13;
	}
	
	private void removeCardData(int c)
	{
		for(int i=0; i<numOfPlayers; i++)
		{
			playerCards[i][c/13] &= ~(0x1 << c%13);
		}
	}
	
	
}