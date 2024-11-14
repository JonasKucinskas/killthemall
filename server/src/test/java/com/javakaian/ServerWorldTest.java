package com.javakaian;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryonet.Connection;
import com.javakaian.network.OServer;
import com.javakaian.network.messages.LoginMessage;
import com.javakaian.network.messages.LogoutMessage;
import com.javakaian.network.messages.PositionMessage;
import com.javakaian.network.messages.ShootMessage;
import com.javakaian.shooter.LoginController;
import com.javakaian.shooter.ServerWorld;
import com.javakaian.shooter.shapes.Bullet;
import com.javakaian.shooter.shapes.Enemy;
import com.javakaian.shooter.shapes.Player;

public class ServerWorldTest {

    private ServerWorld serverWorld;
    private OServer mockServer;
    private LoginController mockLoginController;

    @Before
    public void setUp() {
        mockServer = mock(OServer.class);
        mockLoginController = mock(LoginController.class);
        serverWorld = new ServerWorld();
        serverWorld.oServer = mockServer;
        serverWorld.loginController = mockLoginController;
    }

    @Test
    public void testLoginReceived() {
        Connection mockConnection = mock(Connection.class);
        LoginMessage loginMessage = new LoginMessage();
        loginMessage.setX(100);
        loginMessage.setY(200);

        when(mockLoginController.getUserID()).thenReturn(1);

        serverWorld.loginReceived(mockConnection, loginMessage);

        assertEquals(1, serverWorld.players.size());
        Player player = serverWorld.players.get(0);
        assertEquals(100, player.getPosition().x, 0.01);
        assertEquals(200, player.getPosition().y, 0.01);
        assertEquals(1, player.getId());

        ArgumentCaptor<LoginMessage> captor = ArgumentCaptor.forClass(LoginMessage.class);
        verify(mockServer).sendToUDP(eq(mockConnection.getID()), captor.capture());
        assertEquals(1, captor.getValue().getId());
    }

    @Test
    public void testLogoutReceived() {
        Player player = new Player(100, 200, 50, 1);
        serverWorld.players.add(player);
        LogoutMessage logoutMessage = new LogoutMessage();
        logoutMessage.setId(1);

        serverWorld.logoutReceived(logoutMessage);

        assertTrue(serverWorld.players.isEmpty());
        verify(mockLoginController).putUserIDBack(1);
    }

    @Test
    public void testPlayerMovedReceived() {
        Player player = new Player(100, 200, 50, 1);
        serverWorld.players.add(player);
        PositionMessage positionMessage = new PositionMessage();
        positionMessage.setId(1);
        positionMessage.setDirection(PositionMessage.DIRECTION.RIGHT);

        serverWorld.deltaTime = 0.1f; // For consistent movement

        serverWorld.playerMovedReceived(positionMessage);

        assertEquals(120, player.getPosition().x, 0.01);
        assertEquals(200, player.getPosition().y, 0.01);
    }

    @Test
    public void testShootMessageReceived() {
        Player player = new Player(100, 200, 50, 1);
        serverWorld.players.add(player);
        ShootMessage shootMessage = new ShootMessage();
        shootMessage.setId(1);
        shootMessage.setAngleDeg(90);

        serverWorld.shootMessageReceived(shootMessage);

        assertEquals(1, serverWorld.bullets.size());
        Bullet bullet = serverWorld.bullets.get(0);
        assertEquals(125, bullet.getPosition().x, 0.01);
        assertEquals(225, bullet.getPosition().y, 0.01);
        assertEquals(1, bullet.getId());
    }

    @Test
    public void testSpawnRandomEnemy() {
        serverWorld.enemyTime = 0.5f;
        serverWorld.enemies.clear();

        serverWorld.spawnRandomEnemy();

        assertFalse(serverWorld.enemies.isEmpty());
        assertTrue(serverWorld.enemies.size() <= 15);

        for (Enemy e : serverWorld.enemies) {
            assertTrue(e.getX() >= 0 && e.getX() <= 1000);
            assertTrue(e.getY() >= 0 && e.getY() <= 1000);
        }
    }

    @Test
    public void testCheckCollision() {
        Player player = new Player(100, 100, 50, 1);
        Bullet bullet = new Bullet(100, 100, 10, 0, 2); // Different player ID
        Enemy enemy = new Enemy(100, 100, 10);

        player.setBoundRect(new Rectangle(100, 100, 10, 10));
        bullet.setBoundRect(new Rectangle(100, 100, 10, 10));
        enemy.setBoundRect(new Rectangle(100, 100, 10, 10));

        serverWorld.players.add(player);
        serverWorld.bullets.add(bullet);
        serverWorld.enemies.add(enemy);

        serverWorld.checkCollision();

        assertFalse(bullet.isVisible());
        assertFalse(enemy.isVisible());

    }

    @Test
    public void testCheckPlayerTakingDamage() {
        
        Player player = new Player(100, 100, 50, 1);
        Bullet bullet = new Bullet(100, 100, 10, 0, 2); // Different player ID

        int initialHp = player.getHealth();

        player.setBoundRect(new Rectangle(100, 100, 10, 10));
        bullet.setBoundRect(new Rectangle(100, 100, 10, 10));

        serverWorld.players.add(player);
        serverWorld.bullets.add(bullet);

        serverWorld.checkCollision();

        assertTrue(initialHp > player.getHealth());
    }

}

