package com.cardGame;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cardGame.AssSpade.AssSpadeManager;
import com.cardGame.AssSpade.AssSpadeHPlayer;
import com.cardGame.AssSpade.AssSpadeCPlayer;
import com.cardGame.AssSpade.AssSpadePlayer;
import com.cardGame.AssSpade.AssSpadeRPlayer;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

public class Game extends BaseGameActivity implements RealTimeMessageReceivedListener,
RoomStatusUpdateListener, RoomUpdateListener, MainViewInterface, Broadcaster, OnInvitationReceivedListener, View.OnClickListener, GameEventListener
{
	private final int HOME = 0, GAME = 1, BC = 2;	
    public static int WIDTH_PIXEL;
	public static int HEIGHT_PIXEL;
	
	final static int RC_SELECT_PLAYERS = 10001;
	final static int RC_WAITING_ROOM = 10002;
	final static int RC_INVITATION_INBOX = 10003;
	final static String TAG = "AssSpade";
	
	private boolean bStartedRoom = false;
	private Object noOfCompsLock = new Object();
	int gBroadcastComps = -1;
	private final int BROADCAST_COM = 100;
	private ArrayList<String> ComNames = new ArrayList();
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;
    
    private MainView m_MainView;

    // My participant ID in the currently active game
    String mMyId = null;
    
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;
    
	GameData gameData = GameData.getInstance();
   
    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    int mCurScreen = -1;

    
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
		
        super.onCreate(savedInstanceState);
        
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		WIDTH_PIXEL = dm.widthPixels;
		HEIGHT_PIXEL = dm.heightPixels;
		ComNames.add("Bach");
    	ComNames.add("Mozart");
    	ComNames.add("Beethoven");
    	ComNames.add("Chopin");
    	ComNames.add("Schubert");
        //setView(HOME);
		showMain();
        findViewById(R.id.button_accept_popup_invitation).setOnClickListener(this);
    }
	
	private void showMain()
	{
		setContentView(R.layout.mainview);
        m_MainView = (MainView)findViewById(R.id.MainView);        
        m_MainView.setMainViewListener(this);
        m_MainView.computeScreen(WIDTH_PIXEL,HEIGHT_PIXEL);
        m_MainView.setFocusable(true);
        m_MainView.setFocusableInTouchMode(true);         
        switchToScreen(screen_main);
        
	}	
	
    
    private GameData getGameData()
    {
    	//return (GameData)AssSpadeManager.getInstance();
    	gameData.game = GameData.ASSSPADE;
    	return gameData;
    }
    
    /**
     * Called by the base class (BaseGameActivity) when sign-in has failed. For
     * example, because the user hasn't authenticated yet. We react to this by
     * showing the sign-in button.
     */
    @Override
    public void onSignInFailed() {
        Log.d(TAG, "Sign-in failed.");
        showMain();
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in succeeded. We
     * react by going to our main screen.
     */
    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "Sign-in succeeded.");

       // switchToMainScreen();
    }

    public void onClick(int b) {
        switch (b) {
            case button_single_player:
              	startAssSpadeGameSingle();
                break;
            case button_sign_in:
                beginUserInitiatedSignIn();
                break;
            case button_sign_out:
                // user wants to sign out
                signOut();
                switchToScreen(R.id.screen_sign_in);
                break;
            
            case button_quick_game:
                // user wants to play against a random opponent right now
               // startQuickGame();
            	 // show list of invitable players
            	if(!isSignedIn())
            		beginUserInitiatedSignIn();
            	bStartedRoom = true;
            	Intent intent;
            	 Log.d(TAG, "### before invitation");
            	 switchToScreen(screen_wait);
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), m_MainView.numOfrPlayers, m_MainView.numOfrPlayers,false);
                
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case invitation:
        		bStartedRoom = false;
        		switchToScreen(screen_wait);
            	intent = Games.Invitations.getInvitationInboxIntent(getApiClient());       
            	
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case close:
            	if(mCurScreen == screen_main)
            		finish();
            	else
            		switchToScreen(screen_main);
            	break;
            case question:
            	switchToScreen(screen_question);
            	break;           	
            
        }
    }
    
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
        case R.id.button_accept_popup_invitation:
	        // user wants to accept the invitation shown on the invitation popup
	        // (the one we got through the OnInvitationReceivedListener).
	        acceptInviteToRoom(mIncomingInvitationId);
	        mIncomingInvitationId = null;
	        break;
        }
    }
    
    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(screen_wait);
        keepScreenOn();       
        Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        MIN_OPPONENTS = MAX_OPPONENTS = m_MainView.numOfrPlayers-1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(screen_wait);
        keepScreenOn();
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
    }
    
 // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        if(mCurScreen == screen_game)
        {
	        showMain();
	        leaveRoom();
        }
        // stop trying to keep the screen on
        stopKeepingScreenOn();

      //  switchToScreen(screen_wait);
        super.onStop();
    }
    
   
    // Activity just got to the foreground. We switch to the wait screen because we will now
    // go through the sign-in flow (remember that, yes, every time the Activity comes back to the
    // foreground we go through the sign-in flow -- but if the user is already authenticated,
    // this flow simply succeeds and is imperceptible).
    @Override
    public void onStart() {
      //  switchToScreen(screen_wait);
    	showMain();
        super.onStart();
        
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(getApiClient(), this, mRoomId);
            mRoomId = null;
            switchToScreen(screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
    	Log.d(TAG, "showWaitingRoom");
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }
    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
    // is connected yet).
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    // Called when we've successfully left the room (this happens a result of voluntarily leaving
    // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // Called when we get disconnected from the room. We return to the main screen.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        Toast.makeText(getApplicationContext(), 
   			 "Disconnected from room", Toast.LENGTH_LONG).show();
       showGameError();
    }
    
    public void ToastMessage(String s)
    {
    	 Log.d(TAG, "### ToastMessage ");
    	 Toast.makeText(getApplicationContext(), 
       			 s, Toast.LENGTH_LONG).show();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        showAlert(getString(R.string.game_problem));
        showMain();
    }

    // Called when room has been created
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    // Called when room is fully connected.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }
    
    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        switchToScreen(screen_main); // This will show the invitation popup
    }

    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            switchToScreen(screen_main); // This will hide the invitation popup
        }
    }


    // We treat most of the room update callbacks in the same way: we update our list of
    // participants and update the display. In a real game we would also have to check if that
    // change requires some action like removing the corresponding player avatar from the screen,
    // etc.
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {
    }

    @Override
    public void onP2PConnected(String participant) {
    }

    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
    	Toast.makeText(getApplicationContext(), 
   			 "Somebody left the room.If the person had cards left or hosted computer's play, game might not work properly", Toast.LENGTH_LONG).show();
    	 showMain();
         leaveRoom();
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateRoom(room);
    }

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
    }

    // Called when we receive a real-time message from the network.
    
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
    	Log.d(TAG, " ### onRealTimeMessageReceived 1");
        byte[] buf = rtm.getMessageData();
        if(buf[0] == (byte)BROADCAST_COM)
        	handleComsCount((int)buf[1]);
        else
        {
	        String sender = rtm.getSenderParticipantId();
	        AssSpadeManager.getInstance().decodeMessage(buf,sender);
        }
    }
    
    public void broadcastMessage(byte[] msg)
    {
    	
    	for (Participant p : mParticipants) 
    	{
            if (p.getParticipantId().equals(mMyId))
                continue;            
            Log.d(TAG, " ### broadcastMessage 1");
                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, msg,
                        mRoomId, p.getParticipantId());
            
        }
    }
   
    
    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
    	 Log.d(TAG, "### switching to screen " + screenId);
        m_MainView.setScreen(screenId);
        mCurScreen = screenId;
        
     // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == screen_main );
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
        
        
        m_MainView.invalidate();
        
       

       }

    void switchToMainScreen() {
        switchToScreen(screen_main);
    }

    
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }
    
    
    @Override
    public void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        switch (requestCode) {
        case RC_SELECT_PLAYERS:
            // we got the result from the "select players" UI -- ready to create the room
        	Log.d(TAG, "### before handleselect");
            handleSelectPlayersResult(responseCode, intent);
            if(responseCode == Activity.RESULT_OK)
            	switchToScreen(screen_wait);
            break;
        case RC_INVITATION_INBOX:
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(responseCode, intent);
            if(responseCode == Activity.RESULT_OK)
            	switchToScreen(screen_wait);
            break;
        case RC_WAITING_ROOM:
            // we got the result from the "waiting room" UI.
            if (responseCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                switchToScreen(screen_game);
                startGameMulti();
                
            } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
                switchToScreen(screen_main);
            } else if (responseCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
                switchToScreen(screen_main);
            }
            break;
    }
     
    }
    
    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }       
        keepScreenOn();       
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

   
    private void startGameMulti()
    {
    	//sort the partcicpants list do that all users have same order
    	Collections.sort(mParticipants, new Comparator<Participant>()
		{
			@Override
			public int compare(Participant p1, Participant p2)
			{
				return p1.getParticipantId().compareTo(p2.getParticipantId());
			}
		});
    	
    	int cnt = 0;
    	int myIndex = -1;
    	
    	gameData.ClearPlayers();
    	
    	for (Participant p : mParticipants) 
    	{
	        if (p.getParticipantId().equals(mMyId))
	        {
	        	myIndex = cnt;
	        	(gameData).addPlayer((AssSpadePlayer)new AssSpadeHPlayer("You"));
	        }
	        else
	        	(gameData).addPlayer((AssSpadePlayer)new AssSpadeRPlayer(p.getDisplayName()));
	        cnt++;
    	}    
    	
    	 Log.d(TAG, " ### Going to call startAssSpadeGameMulti 1");
    	
    	
    	
    	if(bStartedRoom)
    	{
	    	byte buf[] = new byte[2];
	    	buf[0] = (byte)BROADCAST_COM;
	    	buf[1] = (byte)m_MainView.numOfaPlayers;
	    	broadcastMessage(buf);
	    	gBroadcastComps = m_MainView.numOfaPlayers;
	    	Log.d(TAG, " ### Going to call startAssSpadeGameMulti 2");
	    	for(int i = 0;i< gBroadcastComps;i++)					
				(gameData).addPlayer((AssSpadePlayer)new AssSpadeCPlayer(cnt + gBroadcastComps,ComNames.get(i)));				
	    	startAssSpadeGameMulti();
    	}    	

    }
    
    private void handleComsCount(int x)
    {
    	gBroadcastComps = x;
    	Log.d(TAG, " ### Going to call startAssSpadeGameMulti 4");	
    	for(int i = 0;i< gBroadcastComps;i++)			
			(gameData).addPlayer((AssSpadePlayer)new AssSpadeRPlayer(ComNames.get(i)));    			
    	Log.d(TAG, " ### Going to call startAssSpadeGameMulti 5");
    	startAssSpadeGameMulti();
//    	synchronized(noOfCompsLock)
//		{
//    		noOfCompsLock.notifyAll();
//		}
    }    
 
    
    private void startAssSpadeGameMulti()
    {
    	Log.d(TAG, "### startAssSpadeGameMulti 1 ###");
   		setContentView(R.layout.play);
   		Log.d(TAG, "### startAssSpadeGameMulti 2 ###");
   		AssSpadeManager.getInstance().RegisterBroadcaster(this);
   		Log.d(TAG, "### startAssSpadeGameMulti 3 ###");
   		AssSpadeManager.getInstance().RegisterGameEventListenerr(this);
   		Log.d(TAG, "### startAssSpadeGameMulti 4 ###");
//   		if(bStartedRoom)
//    	{
//   			Log.d(TAG, " ### Going to call startAssSpadeGameMulti 21");
//   			try{
//   				Thread.sleep(10000);
//   			}catch(Exception e){}
//	    	byte buf[] = new byte[2];
//	    	buf[0] = (byte)BROADCAST_COM;
//	    	buf[1] = (byte)m_MainView.numOfqPlayers;
//	    	broadcastMessage(buf);
//	    	gBroadcastComps = m_MainView.numOfqPlayers;
//	    	Log.d(TAG, " ### Going to call startAssSpadeGameMulti 22");
//    	}    	
   		AssSpadeManager.getInstance().start(WIDTH_PIXEL, HEIGHT_PIXEL, (GameView) findViewById(R.id.gameView),
   			gameData.getPlayers().elementAt(0) instanceof AssSpadeHPlayer ? AssSpadeManager.MULTISERVER : AssSpadeManager.MULTICLIENT);
   		Log.d(TAG, "### startAssSpadeGameMulti 5 ###");
    }
    
    public void handleGameEvent(int msg)
    {    	    	    	
         showMain();
         leaveRoom();
    }
    
    private void startAssSpadeGameSingle()
    {
    	gameData.ClearPlayers();
    	AssSpadeManager.getInstance().RegisterGameEventListenerr(this);
    	gameData.addPlayer((AssSpadePlayer)new AssSpadeHPlayer("You"));
    	for(int i=0;i<m_MainView.numOfqPlayers;i++)
    		gameData.addPlayer((AssSpadePlayer)new AssSpadeCPlayer(m_MainView.numOfqPlayers+1,ComNames.get(i)));
    	
    	setContentView(R.layout.play);
    	
    	AssSpadeManager.getInstance().start(WIDTH_PIXEL, HEIGHT_PIXEL, (GameView) findViewById(R.id.gameView), AssSpadeManager.SINGLE);
    }
    
    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
