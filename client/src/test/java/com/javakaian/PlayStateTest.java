package com.javakaian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.javakaian.network.OClient;
import com.javakaian.network.messages.GameWorldMessage;
import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PlayerDied;
import com.javakaian.network.messages.PositionMessage;
import com.javakaian.network.messages.ShootMessage;
import com.javakaian.shooter.shapes.AimLine;
import com.javakaian.shooter.shapes.Bullet;
import com.javakaian.shooter.shapes.Enemy;
import com.javakaian.shooter.shapes.Player;
import com.javakaian.shooter.utils.OMessageParser;
import com.javakaian.states.PlayState;
import com.javakaian.states.State.StateEnum;
import com.javakaian.states.StateController;

public class PlayStateTest {

    private PlayState playState;
    private StateController mockStateController;
    private OClient mockClient;
    private ShapeRenderer mockShapeRenderer;
    private SpriteBatch mockSpriteBatch;
    private Camera mockCamera;

    @Before
    public void setUp() {

        mockStateController = mock(StateController.class);
        mockClient = mock(OClient.class);
        mockShapeRenderer = mock(ShapeRenderer.class);
        mockSpriteBatch = mock(SpriteBatch.class);
        mockCamera = mock(OrthographicCamera.class);

        playState = new PlayState(mockStateController);
        playState.myclient = mockClient;
        playState.sr = mockShapeRenderer;
        playState.sb = mockSpriteBatch;
        playState.camera = (OrthographicCamera) mockCamera;

        Gdx.graphics = mock(Graphics.class);
        Gdx.input = mock(Input.class);

        when(Gdx.input.isKeyPressed(anyInt())).thenReturn(false);
        when(Gdx.graphics.getDeltaTime()).thenReturn(1 / 60f);
        
        playState.players = new ArrayList<>();
        playState.enemies = new ArrayList<>();
        playState.bullets = new ArrayList<>();
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
    }

    @Test
    public void testLogoutReceived() {
        Player player = new Player(100, 200, 50);
        player.setId(1);
        playState.players.add(player);
        LogoutMessage logoutMessage = new LogoutMessage();
        logoutMessage.setId(1);

        playState.logoutReceieved(logoutMessage);

        assertTrue(playState.players.isEmpty());
    }

    @Test
    public void testPlayerDiedReceived() {
        Player player = new Player(100, 200, 50);
        player.setId(1);
        playState.player = player;
        PlayerDied playerDied = new PlayerDied();
        playerDied.setId(1);

        playState.playerDiedReceived(playerDied);

        verify(mockClient).sendTCP(any(LogoutMessage.class));
        verify(mockClient).close();
        verify(mockStateController).setState(StateEnum.GAME_OVER_STATE);
    }

    @Test
    public void testGwmReceived() {
        GameWorldMessage gameWorldMessage = new GameWorldMessage();

        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 200, 10));
        List<Bullet> bullets = new ArrayList<>();
        bullets.add(new Bullet(100, 200, 10));
        List<Player> players = new ArrayList<>();
        players.add(new Player(100, 200, 50));

        when(OMessageParser.getEnemiesFromGWM(gameWorldMessage)).thenReturn(enemies);
        when(OMessageParser.getBulletsFromGWM(gameWorldMessage)).thenReturn(bullets);
        when(OMessageParser.getPlayersFromGWM(gameWorldMessage)).thenReturn(players);

        playState.gwmReceived(gameWorldMessage);

        assertEquals(enemies, playState.enemies);
        assertEquals(bullets, playState.bullets);
        assertEquals(players, playState.players);
    }

    @Test
    public void testProcessInputs() {
        PositionMessage positionMessage = new PositionMessage();
        positionMessage.setId(1);

        when(Gdx.input.isKeyPressed(Keys.W)).thenReturn(true);

        playState.processInputs();

        verify(mockClient).sendUDP(any(PositionMessage.class));
    }


    @Test
    public void testShoot() {
        Player player = new Player(100, 200, 50);
        playState.player = player;
        AimLine aimLine = new AimLine(new Vector2(0, 0), new Vector2(1, 0));
        playState.aimLine = aimLine;

        playState.shoot();

        verify(mockClient).sendUDP(any(ShootMessage.class));
    }
}
