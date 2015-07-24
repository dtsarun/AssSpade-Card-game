package com.cardGame.AssSpade;
import com.google.android.gms.games.multiplayer.Participant;
import android.util.Log;

public class AssSpadeRPlayer extends AssSpadePlayer
{
	private final static int QUEUE_SIZE = 8;
	private int[] queue = new int[QUEUE_SIZE];
	private Object lockCardPlayed = new Object();
	public AssSpadeRPlayer(String s)
	{	
		super(s);
		for(int i=0;i<QUEUE_SIZE;i++)
			queue[i]=-1;
	}
	
	@Override
	public void addCard(int c)
	{
		super.addCard(c);
		AssSpadeDrawState.getInstance().setPlayerCardCnt(getSlot(),numOfCards(),pName);
		Log.i(MODULE,"Slot = " + getSlot() + "value = " + numOfCards());
	}
	
	@Override
	public void removeCard(int c)
	{
		super.removeCard(c);
		AssSpadeDrawState.getInstance().setPlayerCardCnt(getSlot(),numOfCards(),pName);
		Log.i(MODULE,"Slot = " + getSlot() + "value = " + numOfCards());
	}
	
	public int play()
	{
		int retVal = -1;
		synchronized(lockCardPlayed)
		{
			try
			{
				if(queue[0] == -1)
					lockCardPlayed.wait();
				retVal = popFromQueue();				
			}
			catch(Exception e){}
		}
		removeCard(retVal);
		return retVal;
	}
	
	private int popFromQueue()
	{
		int ret = queue[0];
		for(int i=0;i<QUEUE_SIZE-1;i++)
			queue[i] = queue[i+1];
		return ret;
	}
	
	private void pushToQueue(int x)
	{
		for(int i=0;i<QUEUE_SIZE;i++)
			if(queue[i] == -1)
			{
				queue[i] = x;
				break;
			}
	}
	
	@Override
	public void move(int c)
	{
		synchronized(lockCardPlayed)
		{			
			pushToQueue(c);
			lockCardPlayed.notifyAll();
		}
	}
	
}