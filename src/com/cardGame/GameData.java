package com.cardGame;

import com.cardGame.AssSpade.AssSpadePlayer;

import android.util.Log;

import java.util.*;

public class GameData
{
	public final static int ASSSPADE = 0;
	
	public int game;
	
	public static GameData m_GameData = null;
	
	private static final String MODULE = "GameData";
	
	private Vector m_players = new Vector();
	
	public static GameData getInstance()
	{
		if(m_GameData == null)
		{
			m_GameData = new GameData();
		}
		return m_GameData;
	}
	
	public void addPlayer(AssSpadePlayer player)
	{
		m_players.addElement(player);
    	Log.i(MODULE, "inside addPlayer : size = " + m_players.size());
	}
	
	public Vector getPlayers()
	{
		return m_players;
	}
	
	public int numOfPlayers()
	{
		return m_players.size();
	}
	
	public void ClearPlayers()
	{
		 m_players = new Vector();
	}
}