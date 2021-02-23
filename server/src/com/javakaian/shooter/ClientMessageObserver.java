package com.javakaian.shooter;

import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PositionMessage;
import com.javakaian.network.messages.ShootMessage;

public interface ClientMessageObserver {

	/**
	 * This should be received with playerID and bullet direction
	 * 
	 * @param pp
	 */
	public void shootMessageReceived(ShootMessage pp);

	/**
	 * PlayerID, and location should be received.
	 */
	public void loginReceived(LoginMessage m);

	/**
	 * PlayerID should be received.
	 */
	public void logoutReceived(LogoutMessage m);

	/**
	 * PlayerID and direction should be received.
	 * 
	 * @param move
	 */
	public void playerMovedReceived(PositionMessage move);

}
