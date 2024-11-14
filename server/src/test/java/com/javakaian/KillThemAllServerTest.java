package com.javakaian;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.javakaian.shooter.KillThemAllServer;
import com.javakaian.shooter.ServerWorld;

public class KillThemAllServerTest {

    private KillThemAllServer killThemAllServer;
    private ServerWorld mockServerWorld;
    private Graphics mockGraphics;
    private Logger mockLogger;

    @Before
    public void setUp() {
        mockServerWorld = mock(ServerWorld.class);
        mockGraphics = mock(Graphics.class);
        mockLogger = mock(Logger.class);

        Gdx.graphics = mockGraphics;
        when(Gdx.graphics.getDeltaTime()).thenReturn(0.016f); 

        killThemAllServer = new KillThemAllServer();
        killThemAllServer.serverWorld = mockServerWorld;
        killThemAllServer.logger = mockLogger;
    }

    @Test
    public void testCreate() {
        killThemAllServer.create();

        verify(mockLogger).debug("Server is up");
    }

    @Test
    public void testRender() {
        killThemAllServer.render();

        verify(mockServerWorld).update(0.016f);
        assertEquals(0.016f, killThemAllServer.time, 0.001);
        assertEquals(1, killThemAllServer.updateCounter);
    }

    @Test
    public void testRenderResetsTime() {
        killThemAllServer.time = 1.0f;
        killThemAllServer.render();

        assertEquals(0, killThemAllServer.time, 0.001);
        assertEquals(0, killThemAllServer.updateCounter);
    }
}
