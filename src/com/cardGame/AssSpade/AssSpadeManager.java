package com.cardGame.AssSpade;

import android.util.Log;

import com.cardGame.GameData;
import com.cardGame.R;
import com.cardGame.GameView;
import com.cardGame.Broadcaster;
import com.cardGame.DrawState;
import com.cardGame.GameEventListener;

import java.util.*;

import android.widget.Toast;

public class AssSpadeManager implements GameEventListener
{
	
	static AssSpadeManager m_AssSpadeManager;
	public static final int TOTAL_NUM_CARDS = 52;
	public static final int CARDS_PER_COLOR = 13;
	public static final int SINGLE = 0;
	public static final int MULTISERVER = 1;
	public static final int MULTICLIENT = 2;
		
	private final int FIRSTPLAYER = 0;
	private final int DISTRIBUTED_CARDS = 1;
	private final int PLAYED_CARD = 2;	
	private final int RESTART_GAME = 3;
	private int NUM_OF_PLAYERS;
	private int NUM_OF_PLAYERS_FINISHED = 0;
	private GameView m_GameView;
	private int manType = SINGLE;
	private Broadcaster mBroadcaster;
	private GameEventListener mGameEventListener;
	private int m_highestSlot;
	private int m_highestCard;
	private int HEIGHT_PIXEL;
	private int WIDTH_PIXEL;
	private Thread gThread;
	private int gLostPlayer;
	private int gLostTimes;
	
	Object lockDistributed = new Object();	
	boolean cardsDistributed = false;
	private boolean bPlayersInitializedForRestartGame = false;
	private Vector m_players = new Vector();
	private AssSpadeHPlayer gAssSpadeHPlayer;
	private AssSpadeDrawState m_assDrawState;
	private static final String MODULE = "GAME";
	private int mGameEvent = 0;
	
	private AssSpadePlayer m_RoundFirstPlayer;
	private AssSpadePlayer m_RoundBigPlayer;
	
	public static AssSpadeManager getInstance()
	{
		if(m_AssSpadeManager == null)
			m_AssSpadeManager = new AssSpadeManager();
		return m_AssSpadeManager;
	}
	
	private void initiateAssSpadeDrawState()
	{		
		cardsDistributed = false;
		m_assDrawState.initializeVectors();
		m_assDrawState.WIDTH_PIXEL = WIDTH_PIXEL;
		m_assDrawState.HEIGHT_PIXEL = HEIGHT_PIXEL;
		Log.i(MODULE,"HEIGHT_PIXEL = " + HEIGHT_PIXEL + " WIDTH_PIXEL = " + WIDTH_PIXEL);
		m_assDrawState.computeScreen();
		m_assDrawState.associateWithGView(m_GameView);
		m_assDrawState.RegisterGameEventListenerr(this);
		bPlayersInitializedForRestartGame = true;
//		m_assDrawState.setRefreshVisible(false);
	}
	
	 /** Called when the activity is first created. */
    public void start(int WIDTH_PIXEL, int HEIGHT_PIXEL, GameView gm, int server)
    {
        Log.i(MODULE,"AssSpadeManager Oncreate");
        		
		manType = server;
		m_GameView = gm;
		m_GameView.setFocusable(true);
		m_GameView.setFocusableInTouchMode(true);
		
		
		this.HEIGHT_PIXEL = HEIGHT_PIXEL;
		this.WIDTH_PIXEL = WIDTH_PIXEL;
		m_assDrawState = AssSpadeDrawState.getInstance();
		initiateAssSpadeDrawState();
        //setContentView(m_GameView);
		gLostPlayer = 0;
		gLostTimes = 0;
        startGame();
    }
    
    public void RegisterBroadcaster(Broadcaster b)
    {
    	mBroadcaster = b;
    }
    
    public void RegisterGameEventListenerr(GameEventListener g)
    {
    	mGameEventListener = g;
    }

    
    public int numOfPlayers()
    {
    	return m_players.size();
    }    
    
    public void decodeMessage(byte[] buf,String sender)
    {
    	int msgType = (int) buf[0];
    	
    	switch(msgType)
    	{
    	case DISTRIBUTED_CARDS:
    		while(bPlayersInitializedForRestartGame == false);
    		bPlayersInitializedForRestartGame = false;
    		int nop = numOfPlayers();
    		int j = 1;
    		int cnt = 0;
    		for(int i=0; i<nop; i++)
    		{
    			AssSpadePlayer pl = (AssSpadePlayer)m_players.elementAt(i);
    			cnt = (int)buf[j++];
    			while(cnt>0)
    			{    			
    				cnt--;
    				pl.addCard((int)buf[j++]);    				
    			}
    		}   
    		
    		synchronized(lockDistributed)
    		{
    			cardsDistributed = true;
    			lockDistributed.notifyAll();
    		}
    		break;
    	case PLAYED_CARD:
//    		(getPlayerFromPid(sender)).move((int)buf[1]);
    		AssSpadePlayer p = (AssSpadePlayer)m_players.elementAt((int)buf[1]);
    		if(p instanceof AssSpadeRPlayer)
    			p.move((int)buf[2]);
    		break;
    	case RESTART_GAME:
    		handleGameEvent(GE_REMOTERESTART);
    		break;
    	}
    }    
    
    private int[] getPredefinedSlot(int n)
    {
    	switch(n)
    	{
    	case 2:
    		return new int[]{8,4};
    	case 3:
    		return new int[]{8,3,5};
    	case 4:
    		return new int[]{8,2,4,6};
    	case 5:
    		return new int[]{8,2,3,5,6};
    	case 6:
    		return new int[]{8,2,3,4,5,6};
    	}
    	return null;
    }
    
    private void encodeAndSend(int msgType,int data,int index)
    {
    	//byte[] buf = new byte[numOfPlayers()+52+1];
    	switch(msgType)
    	{
    	case DISTRIBUTED_CARDS:
    	{
    		byte[] buf = new byte[numOfPlayers()+52+1];
    		Vector vc;
    		int cnt = 0;
    		buf[cnt++]=(byte)DISTRIBUTED_CARDS;
    		for(int i=0;i<numOfPlayers();i++)
    		{
    			AssSpadePlayer pl = (AssSpadePlayer)m_players.elementAt(i);
    			vc = pl.getCards();
    			for(int j=buf[cnt++]=(byte)vc.size();j>0;j--)
    			{
    				buf[cnt++]=(byte)((Integer)vc.elementAt(vc.size()-j)).intValue();
    			}    			
    		}
    		mBroadcaster.broadcastMessage(buf);
    		break;
    	}
    	case PLAYED_CARD:
    	{
    		byte[] buf = new byte[3];
    		buf[0]=(byte)PLAYED_CARD;
    		buf[1]=(byte)index;
    		buf[2]=(byte)data;
    		mBroadcaster.broadcastMessage(buf);
    		break;
    	}
    	case RESTART_GAME:
    		byte[] buf = new byte[1];
    		buf[0] = (byte)RESTART_GAME;    
    		mBroadcaster.broadcastMessage(buf);
    		break;
    	}
    }
    
    private AssSpadeHPlayer getHPlayer()
    {
    	Enumeration e = m_players.elements();
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer pl = (AssSpadePlayer)e.nextElement();
    		if(pl instanceof AssSpadeHPlayer)
    			return (AssSpadeHPlayer)pl;
    	}
    	return null;
    }
    
    private void notifyCPlayers(AssSpadePlayer pl,int c)
    {
    	int plno = m_players.indexOf(pl);
    	
    	Enumeration e = m_players.elements();
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer p1 = (AssSpadePlayer)e.nextElement();
    		if(p1 instanceof AssSpadeCPlayer)
    		{
    			AssSpadeCPlayer p = (AssSpadeCPlayer)p1;
    			p.notifyCardPlayed(plno, c);
    		}
    	}    	
    }
    
    private void flushTable()
    {    	
    	Enumeration e = m_players.elements();
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer p1 = (AssSpadePlayer)e.nextElement();
    		if(p1 instanceof AssSpadeCPlayer)
    		{
    			AssSpadeCPlayer p = (AssSpadeCPlayer)p1;
    			p.flushTable();
    		}
    	}   	
    	
    }
    
    private void flushTable(AssSpadePlayer pl)
    {    	
    	int plno = m_players.indexOf(pl);
    	Enumeration e = m_players.elements();
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer p1 = (AssSpadePlayer)e.nextElement();
    		if(p1 instanceof AssSpadeCPlayer)
    		{
    			AssSpadeCPlayer p = (AssSpadeCPlayer)p1;
    			p.flushTable(plno);
    		}
    	}   	    	
    }
    
    private void startGame()
    {
    	
    	gAssSpadeHPlayer = getHPlayer();
    	m_players = GameData.getInstance().getPlayers();
    	mGameEvent = GE_NONE;
		
        NUM_OF_PLAYERS = m_players.size();
        for(int i=0;i<NUM_OF_PLAYERS;i++)
    	{
    		AssSpadePlayer pl = (AssSpadePlayer)m_players.elementAt(i);
    		pl.setPlayerIndex(i);
    	}
        int slots[] = getPredefinedSlot(NUM_OF_PLAYERS);
        AssSpadePlayer pl = (AssSpadePlayer)getHPlayer();
        
        for(int i=0;i<NUM_OF_PLAYERS;i++)
        {
        	pl.setSlot(slots[i]);
        	pl = getNextPlayer(pl,false);        	
        }
    	gThread = new Thread ( new Runnable()
    	{
    		public void run()
    		{
    			Log.i(MODULE,"### startGame 1");
    			synchronized(lockDistributed)
    			{
	    			if(manType != MULTICLIENT)
	    				distributeCards();
	    			else if(cardsDistributed != true)
	    			{	
	    				try
	    				{
	    					lockDistributed.wait();
	    				}catch(Exception e){}
	    			}
    			}
    			
    			Log.i(MODULE,"### startGame 2");    			
    			
    			m_assDrawState.updateOpenCards();
    			
    			AssSpadePlayer curPlayer = startPlayer();
    			curPlayer.setFocus();
    			
    			while(gameNotOver() || m_assDrawState.getCurTableColor() != AssSpadeDrawState.NONE)
    			{    				   			
    				int card;    							
    				
    				m_assDrawState.updateOpenCards();
    				
    				refreshScreen();
    				
    				card = curPlayer.play();
    				
    				refreshScreen();
    				
    				if(manType!=SINGLE && !(curPlayer instanceof AssSpadeRPlayer))
    					encodeAndSend(PLAYED_CARD,card,curPlayer.getPlayerIndex());
    				
    				
    				notifyCPlayers(curPlayer,card);
    				//refreshScreen();			
    				//card = 1;
    				curPlayer.takeFocus();
    				
    				AssSpadePlayer tmpPlr = curPlayer;
    				curPlayer = getNextFocusPlayer(curPlayer, card);
    				m_assDrawState.updateOpenCards();
    				//curPlayer = getNextFocusPlayer(curPlayer,card);
    				curPlayer.setFocus();
    				if(getNextPlayer(curPlayer,true) == curPlayer)
    					NUM_OF_PLAYERS_FINISHED = m_players.size() - 1;
    			}
    			

    			NUM_OF_PLAYERS_FINISHED = 0;
    			
    			refreshScreen();
    			
    			curPlayer.takeFocus();
    			
    			AssSpadePlayer p = getLoserPlayer();
    			
    			int lp = m_players.indexOf(p);
    			
    			if(gLostPlayer == lp)
    			{
    				if(gLostTimes<4)
    					gLostTimes++;
    			}
    			else
    			{
    				gLostPlayer = lp;
    				gLostTimes = 1;
    			}  	
    			
    			ToastMessage(""+p.getName() + " lost this round. "+p.getName()+" will have atleast "+gLostTimes+ " Ace in the next round");
    			//mBroadCaster.ToastMessage(""+p.getName() + " lost this round. "+p.getName()+" will have "+gLostTimes+ " Ace in the next round");
    			//mBroadCaster.ToastMessage("aa");
    			Log.i(MODULE,"### startGame Restarting game");
    			handleGameEvent(GE_RESTART);

    		}
    	});
    	gThread.start();
    }
    
    public boolean isHumanPlaying()
    {
    	return getHPlayer().getFocus();
    }
    
    public void ToastMessage(String s)
    {
    	m_GameView.post(new ToastMessage(s));    	
    }
    
    private class ToastMessage implements Runnable
    {
    	String ts;
    	
    	ToastMessage(String s)
    	{
    		ts = s;
    	}
    	
    	public void run()
    	{
    		mGameEventListener.ToastMessage(ts);
    	}
    }
    
    private AssSpadePlayer getLoserPlayer()
    {
    	AssSpadePlayer p = null;
    	for(int i=0; i< m_players.size();i++)
    	{
    		p = (AssSpadePlayer)m_players.elementAt(i);
    		if(p.numOfCards() >0 )
    			return p;
    	}
    	return p;    	
    }
    
    private void restartGame()
    {
    	int comcnt = 0;
    	Enumeration e = m_players.elements();
    	ArrayList<String> ComNames = new ArrayList();
    	ComNames.add("Bach");
    	ComNames.add("Mozart");
    	ComNames.add("Beethoven");
    	ComNames.add("Chopin");
    	ComNames.add("Schubert");
    			
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer p = (AssSpadePlayer)e.nextElement();
    		if(p instanceof AssSpadeCPlayer)
    			comcnt++;
    		else
    			p.clearCards();
    	}
    	
    	
    	if(comcnt>0)
    	for(int i = NUM_OF_PLAYERS - 1;i>=0;i--)
    	{
    		AssSpadePlayer p = (AssSpadePlayer)m_players.elementAt(i);
    		if(p instanceof AssSpadeCPlayer)
    			m_players.remove(p);
    	}
    	
    	
    	for(int i = 0;i < comcnt; i++)
    	m_players.add(new AssSpadeCPlayer(NUM_OF_PLAYERS,ComNames.get(i)));
    	
    	initiateAssSpadeDrawState();
    	
    	startGame();
    }
    
    public void handleGameEvent(int msg)
    {
    	mGameEvent = msg;		
		if(mGameEvent == GE_EXIT)
		{
//					if(gThread.isAlive())
//						gThread.stop();
			m_assDrawState.setToNull();
			m_assDrawState = AssSpadeDrawState.getInstance();
			initiateAssSpadeDrawState();
			mGameEventListener.handleGameEvent(GE_EXIT);
		}
		if(mGameEvent == GE_RESTART)
		{
			if(manType == MULTISERVER)
				encodeAndSend(RESTART_GAME,0,0);
			restartGame();
		}
		if(mGameEvent == GE_REMOTERESTART)
		{
			restartGame();
		}
    }
    
    public void refreshScreen()
    {
    	Log.i(MODULE,"Refreshing screen");
    	m_GameView.post(new Runnable()
		{
			public void run()
			{
				m_GameView.invalidate();
			}
		});    	
    }
    
    private void goToSleep(int s)
    {
    	try
		{
			Thread.sleep(s*1000);			
		}
		catch(Exception e){}
    }
    
    private boolean gameNotOver()
    {
    	return ((NUM_OF_PLAYERS - NUM_OF_PLAYERS_FINISHED) == 1)? false : true ;
    }
    
    private int[] getRandomizedCards(int pl, int noOfAces)
    {
    	int crds[] = new int[TOTAL_NUM_CARDS];
    	for(int i = 0;i< TOTAL_NUM_CARDS; i++)
    		crds[i] = i;
    	Random randomGenerator = new Random();
    	for(int i = 0; i < 1000; i++)
    	{
    		int randomInt1 = randomGenerator.nextInt(TOTAL_NUM_CARDS);
    		int randomInt2 = randomGenerator.nextInt(TOTAL_NUM_CARDS);
    		//Log.i(MODULE,"Inside getRandomizedCards : randomInt1 = " + randomInt1 + " randomInt2" + randomInt2);
    		int tmp = crds[randomInt1];
    		crds[randomInt1] = crds[randomInt2];
    		crds[randomInt2] = tmp;
    	}
    	
    	for(int i = 0; i < noOfAces; i++)
    	{
    		int n1 = pl + i*NUM_OF_PLAYERS;
    		int n2=0;
    		for(int j =0;j<TOTAL_NUM_CARDS;j++)
    			if(crds[j] == 12 + i*13)
    			{
    				n2 = j;
    				break;
    			}
    		int tmp = crds[n1];
    		crds[n1] = crds[n2];
    		crds[n2] = tmp;
    	}
    	return crds;
    }
    
    private void distributeCards()
    {
    	AssSpadePlayer curPlayer;    	
    	Log.i(MODULE,"Inside distributeCards : size = " + m_players.size());
    	for(int i=0;i<m_players.size();i++)
    	{
    		AssSpadePlayer p = (AssSpadePlayer)m_players.elementAt(i);
    		p.clearCards();
    	}
    	curPlayer = (AssSpadePlayer)m_players.elementAt(FIRSTPLAYER);
    	int randomizedCards[] = getRandomizedCards(gLostPlayer,gLostTimes);
    	for(int i=0;i<TOTAL_NUM_CARDS;i++)
    	{
    		curPlayer.addCard(randomizedCards[i]);
    		curPlayer = getNextPlayer(curPlayer,false);
    	}
    	Log.i(MODULE,"Distributed cards");
    	if(manType == MULTISERVER)
    		encodeAndSend(DISTRIBUTED_CARDS,0,0);    	
    }
    
    private AssSpadePlayer getPlayerBySlot(int slot)
    {
    	for(int i=0;i<m_players.size();i++)
    		if( ((AssSpadePlayer)m_players.elementAt(i)).getSlot() == slot) return ((AssSpadePlayer)m_players.elementAt(i));
    	return null;
    }
    
    private AssSpadePlayer getNextFocusPlayer(AssSpadePlayer player, int card)
    {
    	AssSpadePlayer pl;
		if(m_assDrawState.getCurTableColor() == AssSpadeDrawState.NONE)
		{
			Log.i(MODULE,"First Player in the round");
			m_assDrawState.addTableCard(player.getSlot(),card);
			refreshScreen();
			m_RoundFirstPlayer = player;    
			m_highestSlot = player.getSlot();
			m_highestCard = card;
			
			pl =  getNextPlayer(player,true);    		
		}
		else if(m_assDrawState.getCurTableColor() == card/13)
		{    			
			Log.i(MODULE,"Same suite is played");
			m_assDrawState.addTableCard(player.getSlot(),card);
			refreshScreen();
			pl = getNextPlayer(player,true);
			if(card > m_highestCard)
			{
				m_highestSlot = player.getSlot();
				m_highestCard = card;
			}
			
			if( pl == m_RoundFirstPlayer)
			{    	
				flushTable();
				goToSleep(3);
				m_assDrawState.flushTable();    				
				pl = getPlayerBySlot(m_highestSlot);
			}    			
		}
		else
		{
			Log.i(MODULE,"different suite is played");
			m_assDrawState.addTableCard(player.getSlot(),card);
			refreshScreen();
			Vector tc = m_assDrawState.getTableCards();
			pl = getPlayerBySlot(m_highestSlot);
			for(int i=0;i<tc.size();i++)
			{
				pl.addCard( ((DrawState.Card)tc.elementAt(i)).color*13 + ((DrawState.Card)tc.elementAt(i)).number-2 );
			}
			flushTable(pl);
			goToSleep(3);
			m_assDrawState.flushTable();
			
		}
    	
    	return pl;
    }
    
    private AssSpadePlayer getNextPlayer(AssSpadePlayer player,boolean takeIntoAccountCards)
    {
    	AssSpadePlayer pl =  (AssSpadePlayer)m_players.elementAt((m_players.indexOf(player) + 1)%m_players.size());  
    	while(pl.numOfCards() == 0 && takeIntoAccountCards)
    		pl = (AssSpadePlayer)m_players.elementAt((m_players.indexOf(pl) + 1)%m_players.size()); 
    	return pl;
    }
    
    private AssSpadePlayer startPlayer()
    {
    	Enumeration e = m_players.elements();
    	while(e.hasMoreElements())
    	{
    		AssSpadePlayer pl = (AssSpadePlayer)e.nextElement();
    		if(pl.hasCard(12))
    			return pl;
    	}
    	return null;
    }
}