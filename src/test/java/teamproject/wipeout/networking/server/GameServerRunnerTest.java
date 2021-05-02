package teamproject.wipeout.networking.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import teamproject.wipeout.GameMode;

import java.io.IOException;

class GameServerRunnerTest {

    private static final String SERVER_NAME = "TestServer#101";
    private static final String OTHER_SERVER_NAME = "OtherServer#0";

    private static final long DEFAULT_GAME_DURATION = 1_000;
    private static final GameMode DEFAULT_GAME_MODE = GameMode.TIME_MODE;
    private static final int CATCHUP_TIME = 10;

    private GameServerRunner runner;

    @BeforeEach
    void setUp() {
        this.runner = new GameServerRunner();
    }

    @AfterEach
    void tearDown() throws IOException {
        this.runner.stopServer();
        this.runner = null;
    }

    @RepeatedTest(5)
    void testStartingServer() {
        Assertions.assertNull(this.runner.getServerName());
        Assertions.assertFalse(this.runner.isServerActive());

        try {
            this.runner.startServer(SERVER_NAME, DEFAULT_GAME_MODE, DEFAULT_GAME_DURATION);

            Assertions.assertEquals(SERVER_NAME, this.runner.getServerName());
            Assertions.assertTrue(this.runner.isServerActive());

            this.runner.startServer(OTHER_SERVER_NAME, DEFAULT_GAME_MODE, DEFAULT_GAME_DURATION);
            Assertions.assertNotEquals(OTHER_SERVER_NAME, this.runner.getServerName());

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }

    }

    @RepeatedTest(5)
    void testStartingGame() {
        try {
            this.runner.startServer(SERVER_NAME, DEFAULT_GAME_MODE, DEFAULT_GAME_DURATION);
            Assertions.assertTrue(this.runner.isServerActive());

            this.runner.startGame();

            Assertions.assertTrue(this.runner.isGameRunning());

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    void testStoppingAndStartingNewServer() {
        try {
            this.runner.startServer(SERVER_NAME, DEFAULT_GAME_MODE, DEFAULT_GAME_DURATION);

            Thread.sleep(CATCHUP_TIME); // so that server has time to start up in the child process
            Assertions.assertTrue(this.runner.isServerActive());

            this.runner.stopServer();
            Assertions.assertFalse(this.runner.isServerActive());

            this.runner.startServer(OTHER_SERVER_NAME, DEFAULT_GAME_MODE, DEFAULT_GAME_DURATION);
            Assertions.assertEquals(OTHER_SERVER_NAME, this.runner.getServerName());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }
}