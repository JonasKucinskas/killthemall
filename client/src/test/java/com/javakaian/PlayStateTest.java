
package com.javakaian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public class PlayStateTest {

    private PlayState mockPlayState;
    private Player mockPlayer;
    private OClient mockClient;
    private StateController mockStateController; 
    static {
        GdxNativesLoader.load();
    }
    @Before
    public void setUp() {
        mockStateController = mock(StateController.class);
        mockPlayState = mock(PlayState.class);
        mockClient = mock(OClient.class);

        mockPlayState.myclient = mockClient;
        mockPlayState.players = new ArrayList<>(); 
        mockPlayState.enemies = new ArrayList<>();
        mockPlayState.bullets = new ArrayList<>();

        mockPlayer = mock(Player.class);
        when(mockPlayer.getId()).thenReturn(0);

        mockPlayState.player = mockPlayer;
        when(mockPlayState.getSc()).thenReturn(mockStateController);

        Gdx.input = mock(Input.class);
        when(Gdx.input.isKeyPressed(anyInt())).thenReturn(false);
    }

    @Test
    public void testLoginReceived() {
        LoginMessage loginMessage = new LoginMessage();
        loginMessage.setX(100);
        loginMessage.setY(200);
        loginMessage.setId(1);

        doCallRealMethod().when(mockPlayState).loginReceieved(any(LoginMessage.class));

        mockPlayState.loginReceieved(loginMessage);

        assertNotNull(mockPlayState.player);
        assertEquals(100, mockPlayState.player.getPosition().x, 0.01);
        assertEquals(200, mockPlayState.player.getPosition().y, 0.01);
        assertEquals(1, mockPlayState.player.getId());
    }

    @Test 
    public void testPlayerDiedReceived() 
    { 
        PlayerDied playerDied = mock(PlayerDied.class);
        when(playerDied.getId()).thenReturn(0);

        doCallRealMethod().when(mockPlayState).playerDiedReceived(any(PlayerDied.class)); 
        mockPlayState.playerDiedReceived(playerDied);
        verify(mockClient).sendTCP(any(LogoutMessage.class)); 
        verify(mockClient).close(); 
        verify(mockStateController).setState(StateEnum.GAME_OVER_STATE); 
    }
    @Test
    public void testGwmReceived() {
        GameWorldMessage mockGameWorldMessage = mock(GameWorldMessage.class);

        when(mockGameWorldMessage.getEnemies()).thenReturn(new float[]{1.1f});
        when(mockGameWorldMessage.getBullets()).thenReturn(new float[]{1.1f});
        when(mockGameWorldMessage.getPlayers()).thenReturn(new float[]{1.1f});
        
        doCallRealMethod().when(mockPlayState).gwmReceived(any(GameWorldMessage.class));

        mockPlayState.gwmReceived(mockGameWorldMessage);

    }

    @Test
    public void testProcessInputs() {
        doCallRealMethod().when(mockPlayState).processInputs();

        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);

        mockPlayState.processInputs();

        verify(mockClient).sendUDP(any(PositionMessage.class));
    }

    @Test
    public void testShoot() {
        doCallRealMethod().when(mockPlayState).shoot();

        AimLine mockAimLine = mock(AimLine.class);
        mockPlayState.aimLine = mockAimLine; 

        when(mockAimLine.getAngle()).thenReturn(1.1f);
        mockPlayState.shoot();
        verify(mockClient).sendUDP(any(ShootMessage.class));
    }
}
