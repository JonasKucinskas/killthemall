package com.javakaian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.javakaian.network.OClient;
import com.javakaian.network.messages.GameWorldMessage;
import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PlayerDied;
import com.javakaian.network.messages.PositionMessage;
import com.javakaian.network.messages.ShootMessage;
import com.javakaian.shooter.shapes.AimLine;
import com.javakaian.shooter.shapes.Player;
import com.javakaian.states.PlayState;
import com.javakaian.states.State.StateEnum;
import com.javakaian.states.StateController;

public class ConnectionTests {

    private PlayState playState;
    private ServerWorld server;
    private Player player;
    private OClient client;
    private StateController stateController;
    
    static {
        GdxNativesLoader.load();
    }

    @Before
    public void setUp() {
        stateController = new StateController();
        client = new OClient("localhost", new PlayState(stateController)); 
        player = new Player(100, 200, 50);
        player.setId(1);
        playState = new PlayState(stateController);
        playState.myclient = client;
        playState.players = new ArrayList<>();
        playState.enemies = new ArrayList<>();
        playState.bullets = new ArrayList<>();
        playState.player = player;

        Gdx.input = new Input() {
            @Override
            public boolean isKeyPressed(int key) {
                return false;
            }
            
        };

        playState.camera = new OrthographicCamera();
        playState.camera.setToOrtho(false, 800, 600);
    }

    @Test
    public void testLoginReceived() {
        LoginMessage loginMessage = new LoginMessage();
        loginMessage.setX(100);
        loginMessage.setY(200);
        loginMessage.setId(1);

        playState.loginReceieved(loginMessage);

        assertNotNull(playState.player);
        assertEquals(100, playState.player.getPosition().x, 0.01);
        assertEquals(200, playState.player.getPosition().y, 0.01);
        assertEquals(1, playState.player.getId());
        assertTrue(playState.players.contains(playState.player));
        assertTrue(server.players.contains(playState.player));
    }

    @Test
    public void testPlayerDiedReceived() {
        PlayerDied playerDied = new PlayerDied();
        playerDied.setId(player.getId());

        playState.playerDiedReceived(playerDied);

        assertTrue(playState.players.isEmpty());
        assertTrue(client.isClosed()); 
        assertEquals(StateEnum.GAME_OVER_STATE, stateController.getCurrentState().getStateEnum());
        assertTrue(server.players.isEmpty());
    }

    @Test
    public void testGwmReceived() {
        GameWorldMessage gameWorldMessage = new GameWorldMessage();

        playState.gwmReceived(gameWorldMessage);

        assertTrue(playState.enemies.isEmpty());
        assertTrue(playState.bullets.isEmpty());
        assertTrue(playState.players.isEmpty());
    }

    @Test
    public void testProcessInputs() {
        Gdx.input = new Input() {
            @Override
            public boolean isKeyPressed(int key) {
                return key == Input.Keys.W;
            }
            
        };

        playState.processInputs();
        assertEquals(new Vector2(100, 100), players.stream().filter(p -> p.getId()).getPosition());
    }

    @Test
    public void testShoot() {
        AimLine aimLine = new AimLine(new Vector2(0, 0), new Vector2(1, 0));
        playState.aimLine = aimLine;
        playState.shoot();
        assertEquals(1, serverWorld.bullets.size());
    }
}
