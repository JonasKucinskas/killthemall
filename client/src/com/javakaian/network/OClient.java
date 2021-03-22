package com.javakaian.network;

import java.io.IOException;
import java.net.InetAddress;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Listener.ThreadedListener;
import com.javakaian.network.messages.GameWorldMessage;
import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PlayerDied;
import com.javakaian.shooter.NetworkEvents;

public class OClient {

	private Client client;
	private NetworkEvents game;

	private String ip;

	public OClient(String ip, NetworkEvents game) {

		this.game = game;
		this.ip = ip;
		client = new Client();
		client.start();

		ONetwork.register(client);

		client.addListener(new ThreadedListener(new Listener() {

			@Override
			public void received(Connection connection, Object object) {

				Gdx.app.postRunnable(new Runnable() {
					public void run() {

						if (object instanceof LoginMessage) {

							LoginMessage newPlayer = (LoginMessage) object;
							addNew(newPlayer.x, newPlayer.y, newPlayer.id);

						} else if (object instanceof LogoutMessage) {

							LogoutMessage pp = (LogoutMessage) object;
							removePlayer(pp);
						} else if (object instanceof GameWorldMessage) {

							GameWorldMessage gwm = (GameWorldMessage) object;
							gwmReceived(gwm);
						} else if (object instanceof PlayerDied) {

							PlayerDied m = (PlayerDied) object;
							playerDied(m);
						}

					}
				});

			}

		}));

		try {
			System.out.println("Attempting to connect args[0]: " + ip);
			client.connect(5000, InetAddress.getByName(ip), 1234, 1235);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void playerDied(PlayerDied m) {
		game.playerDied(m.id);
	}

	public void gwmReceived(GameWorldMessage gwm) {
		game.gwmReceived(gwm);
	}

	public void removePlayer(LogoutMessage pp) {
		game.removePlayer(pp.id);
	}

	public void addNew(float x, float y, int id) {
		game.addNewPlayer(x, y, id);
	}

	public Client getClient() {
		return client;
	}

}