package com.javakaian;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.javakaian.network.OServer;
import com.javakaian.network.messages.GameWorldMessage;
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

        serverWorld.players = new ArrayList<>(); 
        serverWorld.enemies = new ArrayList<>(); 
        serverWorld.bullets = new ArrayList<>();
    }

    @Test
    public void testUpdate() {
        float deltaTime = 0f;

        
        Player player = new Player(100, 100, 50, 0);
        Enemy enemy = new Enemy(100, 100, 50);
        Bullet bulletFriendly = new Bullet(300, 300, 10, 0, 0);
        Bullet bulletEnemy = new Bullet(100, 100, 10, 0, 1);

        serverWorld.players.add(player); 
        serverWorld.enemies.add(enemy); 
        serverWorld.bullets.add(bulletFriendly); 

        serverWorld.update(deltaTime);

        assertTrue(enemy.isVisible());
        assertTrue(bulletFriendly.isVisible());

        bulletFriendly.setPosition(new Vector2(100, 100));

        serverWorld.update(deltaTime);

        assertFalse(enemy.isVisible());
        assertFalse(bulletFriendly.isVisible());   

        for(int i = 1; i < 15; i++){
            serverWorld.bullets.add(new Bullet(100, 100, 10, 0, i));
        }

        serverWorld.update(deltaTime);
        assertFalse(player.isAlive());
        
        verify(mockServer, times(3)).parseMessage(); 
        verify(mockServer, times(3)).sendToAllUDP(any(GameWorldMessage.class)); 
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
        logoutMessage.setId(0);

        serverWorld.logoutReceived(logoutMessage);

        assertFalse(serverWorld.players.isEmpty());

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

        serverWorld.deltaTime = 0.1f; 

        serverWorld.playerMovedReceived(positionMessage);

        assertEquals(120, player.getPosition().x, 0.01);
        assertEquals(200, player.getPosition().y, 0.01);

        //fail:
        positionMessage = new PositionMessage();
        positionMessage.setId(0);
        positionMessage.setDirection(PositionMessage.DIRECTION.RIGHT);
        serverWorld.deltaTime = 0.1f; 

        serverWorld.playerMovedReceived(positionMessage);

        //left
        positionMessage = new PositionMessage();
        positionMessage.setId(1);
        positionMessage.setDirection(PositionMessage.DIRECTION.LEFT);

        serverWorld.deltaTime = 0.1f; 

        serverWorld.playerMovedReceived(positionMessage);

        assertEquals(100.0, player.getPosition().x, 0.01);
        assertEquals(200, player.getPosition().y, 0.01);


        //up
        positionMessage = new PositionMessage();
        positionMessage.setId(1);
        positionMessage.setDirection(PositionMessage.DIRECTION.UP);

        serverWorld.deltaTime = 0.1f; 

        serverWorld.playerMovedReceived(positionMessage);

        assertEquals(100.0, player.getPosition().x, 0.01);
        assertEquals(180.0, player.getPosition().y, 0.01);

        //down
        positionMessage = new PositionMessage();
        positionMessage.setId(1);
        positionMessage.setDirection(PositionMessage.DIRECTION.DOWN);

        serverWorld.deltaTime = 0.1f; 

        serverWorld.playerMovedReceived(positionMessage);

        assertEquals(100.0, player.getPosition().x, 0.01);
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


        shootMessage.setId(0);
        shootMessage.setAngleDeg(90);
        serverWorld.shootMessageReceived(shootMessage);
        assertEquals(1, serverWorld.bullets.size());
    }

    @Test
    public void testSpawnRandomEnemy() {
        // Case 1: Spawn enemies when enemyTime >= 0.4 and enemies.size() <= 15
        serverWorld.enemyTime = 0.5f;
        serverWorld.enemies.clear();
        serverWorld.spawnRandomEnemy();
        assertFalse(serverWorld.enemies.isEmpty());
        assertTrue(serverWorld.enemies.size() == 1);

        // Case 2: Spawn enemy when enemyTime < 0.4 (ensuring the condition fails)
        serverWorld.enemyTime = 0.3f;
        serverWorld.spawnRandomEnemy();
        assertTrue(serverWorld.enemies.size() == 1);

        // Case 3: Spawn enough enemies to trigger the % 5 condition
        for (int i = 0; i < 4; i++) {
            serverWorld.enemyTime = 0.5f;
            serverWorld.spawnRandomEnemy();
        }
        assertTrue(serverWorld.enemies.size() == 5);  // Should log because 5 is a multiple of 5

        // Case 4: Add many enemies to exceed 15 and make sure spawning stops
        for (int i = 0; i < 11; i++) {
            Enemy e = new Enemy(new SecureRandom().nextInt(1000), new SecureRandom().nextInt(1000), 10);
            serverWorld.enemies.add(e);
        }
        serverWorld.spawnRandomEnemy();
        assertTrue(serverWorld.enemies.size() == 16);

        // Case 5: Try adding one more enemy when the limit is reached
        serverWorld.enemyTime = 0.5f;
        serverWorld.spawnRandomEnemy();
        assertTrue(serverWorld.enemies.size() == 16); // No more enemies should be added
    }

    @Test
    public void testCheckCollision() {
        Player player = new Player(100, 100, 50, 0);
        Bullet bulletEnemy = new Bullet(100, 100, 10, 0, 2);
        Bullet bulletFriendly = new Bullet(100, 100, 10, 0, 0);
        Enemy enemy = new Enemy(100, 100, 10);

        player.setBoundRect(new Rectangle(100, 100, 10, 10));
        bulletFriendly.setBoundRect(new Rectangle(100, 100, 10, 10));
        bulletEnemy.setBoundRect(new Rectangle(100, 100, 10, 10));
        enemy.setBoundRect(new Rectangle(100, 100, 10, 10));

        serverWorld.players.add(player);

        //player got hit
        serverWorld.bullets.add(bulletEnemy);
        assertTrue(bulletEnemy.isVisible());
        serverWorld.checkCollision();
        assertTrue(player.getHealth() == 90);
        assertFalse(bulletEnemy.isVisible());
        serverWorld.bullets.remove(bulletEnemy);

        //healed
        serverWorld.bullets.add(bulletFriendly);
        serverWorld.enemies.add(enemy);
        serverWorld.checkCollision();
        assertTrue(player.getHealth() == 100);//healed to 100
        assertFalse(enemy.isVisible());
        serverWorld.bullets.remove(bulletFriendly);
        serverWorld.enemies.remove(enemy);

        for(int i = 1; i < 11; i++){
            serverWorld.bullets.add(new Bullet(100, 100, 10, 0, i));
        }
        //died
        serverWorld.checkCollision();
        assertFalse(player.isAlive());
    }

    @Test
    public void testCheckPlayerTakingDamage() {
        
        Player player = new Player(100, 100, 50, 1);
        Bullet bullet = new Bullet(100, 100, 10, 0, 2); 

        int initialHp = player.getHealth();

        player.setBoundRect(new Rectangle(100, 100, 10, 10));
        bullet.setBoundRect(new Rectangle(100, 100, 10, 10));

        serverWorld.players.add(player);
        serverWorld.bullets.add(bullet);

        serverWorld.checkCollision();

        assertTrue(initialHp > player.getHealth());
    }
}

