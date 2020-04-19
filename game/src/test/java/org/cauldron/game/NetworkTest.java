package org.cauldron.game;

import org.cauldron.audio.AudioManager;
import org.cauldron.network.Client;
import org.cauldron.network.Message;
import org.cauldron.scenes.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;

import static org.cauldron.game.Game.SceneType.*;
import static org.junit.jupiter.api.Assertions.*;

public class NetworkTest {
    static Window window = new Window();
    static Game game;
    static Window window1 = new Window();
    static Window window2 = new Window();
    static Game user1;
    static Game user2;
    final static int LIMIT = 10;

    @BeforeClass
    public static void gameSetup(){
        AudioManager.init();
        AudioManager.setListenerData();
        window.init();
        GL.createCapabilities();
        game.init(window.getWindow());
    }

    @AfterClass
    public static void gameCleanUp(){
        game.cleanup();
    }

    @Test
    public void testCommunication(){
        ArrayList<Message> received = new ArrayList<>();
        double startTime = System.currentTimeMillis();
        double timePast = startTime;

        boolean isClientStart = false;
        boolean isClientEnd = false;

        int numMsgSent = 0;
        final int MAX_SENT_MSG = 5;
        Client client;

        OnlineScene onlineScene = new OnlineScene(game.getGuiLayer(), true);

        client = onlineScene.getClient();
        if(client.address == null)
            client.address = "localhost";
        client.setName("");
        onlineScene.setClient(client);
        isClientStart = client.startClient(onlineScene);
        Message testMsg = new Message("debug");
        testMsg.stringData = "This is first test Message from client";

        while(true){
            timePast = (System.currentTimeMillis() - startTime) / 1000f;
            if(timePast >= LIMIT){
                break;
            }
            if(numMsgSent < MAX_SENT_MSG) {
                System.out.println(client.accepted);
                if(received.size() >= numMsgSent && client.accepted == true){
                    switch (numMsgSent){
                        case 0:
                            testMsg.stringData = "This is first test message from client";
                            break;
                        case 1:
                            testMsg.stringData = "This is second test message from client";
                            break;
                        case 2:
                            testMsg.stringData = "This is third test message from client";
                            break;
                        case 3:
                            testMsg.stringData = "This is fourth test message from client";
                            break;
                        case 4:
                            testMsg.stringData = "This is fifth test message from client";
                            break;
                        default:
                            testMsg.stringData = "This is the last test message from client";
                            break;
                    }
                    client.sendMessage(testMsg);
                    numMsgSent++;
                }

            }
            received = client.testMessages;
        }
        System.out.println("Sent messages logs");

        for(Message msg : received){
            System.out.println(msg.toString());
        }

        client.started = false;
        isClientEnd = client.closeClient();


        assertTrue(isClientStart);
        assertTrue(isClientEnd);
        assertEquals(5, received.size());
    }
}
