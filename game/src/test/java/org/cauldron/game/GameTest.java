package org.cauldron.game;

import org.cauldron.audio.AudioManager;
import org.cauldron.database.DBHandler;
import org.cauldron.database.Database;
import org.cauldron.entity.Entity;
import org.cauldron.scenes.OfflineScene;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.cauldron.game.Game.SceneType.*;

public class GameTest {
    static Window window = new Window();
    static Game game;


    @BeforeClass
    public static void gameSetup() {
        AudioManager.init();
        AudioManager.setListenerData();
        window.init();
        GL.createCapabilities();
        game.init(window.getWindow());
    }

    @Test
    public void testInput() {
        ArrayList<Integer> keyPressedHistory = new ArrayList<>();
        ArrayList<Integer> target = new ArrayList<>();
        double longestMouseHeldTime = 0;
        double currentMouseHeldTime = 0;
        //Test instruction
        System.out.println("Type 'cauldron' on the window then press 'escape' to end");

        target.add(GLFW_KEY_C);
        target.add(GLFW_KEY_A);
        target.add(GLFW_KEY_U);
        target.add(GLFW_KEY_L);
        target.add(GLFW_KEY_D);
        target.add(GLFW_KEY_R);
        target.add(GLFW_KEY_O);
        target.add(GLFW_KEY_N);

        while (!glfwWindowShouldClose(window.getWindow())) {
            Input.cycle();
            currentMouseHeldTime = Input.mouseHeldTime;

            //System.out.println(Input.keysPressed.size());
            if (currentMouseHeldTime > longestMouseHeldTime) {
                longestMouseHeldTime = currentMouseHeldTime;
            }
            if (Input.keysPressed.contains(GLFW_KEY_ESCAPE)) {
                break;
            }

            for (int key : Input.keysPressed) {
                if (keyPressedHistory.contains(key))
                    continue;
                keyPressedHistory.add(key);
            }
        }
        for (int i = 0; i < keyPressedHistory.size(); i++) {
            System.out.print(keyPressedHistory.get(i) + " ");
        }

        assertTrue(Input.keysPressed.size() == 1);

        assertEquals(target.size(), keyPressedHistory.size());
        assertEquals(target.get(0), keyPressedHistory.get(0));
        assertEquals(target.get(1), keyPressedHistory.get(1));
        assertEquals(target.get(2), keyPressedHistory.get(2));
        assertEquals(target.get(3), keyPressedHistory.get(3));
        assertEquals(target.get(4), keyPressedHistory.get(4));
        assertEquals(target.get(5), keyPressedHistory.get(5));
        assertEquals(target.get(6), keyPressedHistory.get(6));
        assertEquals(target.get(7), keyPressedHistory.get(7));

        assertTrue(longestMouseHeldTime > 1);
    }

    @Test
    public void testImplementAllEntities () {
        boolean isGenCrate = false;
        boolean isGenGround = false;
        boolean isGenParticle = false;
        boolean isGenProjectile = false;
        boolean isGenTank = false;
        boolean isGenTurret = false;

        final int MAX_ENTITIES = 1600;
        Entity[] entities = new Entity[MAX_ENTITIES];
        OfflineScene offlineScene;

        double startTime = System.currentTimeMillis();
        double timePast = startTime;
        while (!glfwWindowShouldClose(window.getWindow())) {
            game.cycle();
            glfwSwapBuffers(window.getWindow());
            if (game.currentScene.getType() == OFFLINE) {
                offlineScene = (OfflineScene) game.currentScene;
                entities = offlineScene.entityHandler.getEntities();

                timePast = (System.currentTimeMillis() - startTime) / 1000f;
                for (int i = 0; i < MAX_ENTITIES; i++) {
                    if (entities[i] == null)
                        break;
                    switch (entities[i].type) {
                        case TANK:
                            isGenTank = true;
                            break;
                        case CRATE:
                            isGenCrate = true;
                            break;
                        case GROUND:
                            isGenGround = true;
                            break;
                        case TURRET:
                            isGenTurret = true;
                            break;
                        case PARTICLE:
                            isGenParticle = true;
                            break;
                        case PROJECTILE:
                            isGenProjectile = true;
                            break;
                        default:
                            break;
                    }
                }
            }

            if (isGenCrate && isGenGround && isGenParticle && isGenProjectile && isGenTank && isGenTurret)
                break;
        }

        assertTrue(timePast >= 10);
        assertTrue(isGenCrate);
        assertTrue(isGenGround);
        assertTrue(isGenParticle);
        assertTrue(isGenProjectile);
        assertTrue(isGenTank);
        assertTrue(isGenTurret);
    }

    @Test
    public void testGameScore (){
        OfflineScene offlineScene;
        int userScore = 0;
        int aiScore = 0;
        double testStartTime = System.currentTimeMillis();
        double pastTestTime;
        while (!glfwWindowShouldClose(window.getWindow())) {
            game.cycle();
            glfwSwapBuffers(window.getWindow());
            pastTestTime = System.currentTimeMillis();
            if (game.currentScene.getType() == OFFLINE) {
                offlineScene = (OfflineScene) game.currentScene;
                userScore = offlineScene.scores[0];
                aiScore = offlineScene.scores[1];
                if (userScore >= 1 && aiScore >= 1) {
                    break;
                }
                if ((pastTestTime - testStartTime) / 1000f >= 90) {
                    break;
                }
            }

            System.out.println("Time past: " + (pastTestTime - testStartTime) / 1000f);
        }
        assertTrue(userScore >= 1);
        assertTrue(aiScore >= 1);
    }

    @Test
    public void testDB () {
        DBHandler dbHandler = new DBHandler();
        //Test register
        String userID = "testID";
        String password = "testpassword";
        String email = "test@test.com";

        Database db = new Database(userID, email, password);

        dbHandler.save(db);

        Database result = dbHandler.get(db.getUserID());
        assertTrue(result.toString().equals(dbHandler.get(db.getUserID()).toString()));

        //Test verify
        assertTrue(dbHandler.verify(db));

        //Test update
        Database newDB = new Database(db.getUserID(), db.getEmail(), db.getPassword());
        System.out.println("New database");
        System.out.println(newDB.toString());

        newDB.setEmail("test1@test.com");
        assertTrue(dbHandler.update(newDB));
        result = dbHandler.get(newDB.getUserID());
        System.out.println(result.toString());
        assertTrue(result.toString().equals(dbHandler.get(newDB.getUserID()).toString()));

        newDB.setPassword("testPassword1");
        dbHandler.update(newDB);
        result = dbHandler.get(newDB.getUserID());
        System.out.println(result.toString());
        assertTrue(result.toString().equals(dbHandler.get(newDB.getUserID()).toString()));

        //Test email validation
        newDB.setEmail("emailwithflaw");
        assertFalse(dbHandler.update(newDB));

        newDB.setEmail("test@test.com");
        assertTrue(dbHandler.update(newDB));

        //Test delete
        dbHandler.delete(db);
        assertTrue(!dbHandler.isExist(newDB.getUserID()));
    }
}
