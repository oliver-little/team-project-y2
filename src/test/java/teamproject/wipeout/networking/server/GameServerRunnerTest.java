package teamproject.wipeout.networking.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.io.IOException;

class GameServerRunnerTest {

    private static final String SERVER_NAME = "TestServer#101";
    private static final String OTHER_SERVER_NAME = "OtherServer#0";

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
        Assertions.assertFalse(this.runner.isActive());

        boolean exceptionThrown = false;

        try {
            this.runner.startServer(SERVER_NAME);

            Assertions.assertEquals(SERVER_NAME, this.runner.getServerName());
            Assertions.assertTrue(this.runner.isActive());

            this.runner.startServer(OTHER_SERVER_NAME);
            Assertions.assertNotEquals(OTHER_SERVER_NAME, this.runner.getServerName());

        } catch (ServerRunningException runningException) {
            exceptionThrown = true;
        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertTrue(exceptionThrown);
    }

    @RepeatedTest(5)
    void testStartingAndStoppingGame() {
        try {
            this.runner.startServer(SERVER_NAME);
            Assertions.assertTrue(this.runner.isActive());

            this.runner.startGame();

            Assertions.assertTrue(this.runner.isGameRunning());

            this.runner.stopGame();

            Assertions.assertFalse(this.runner.isGameRunning());

        } catch (IOException | ServerRunningException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    void testStoppingAndStartingNewServer() {
        try {
            this.runner.startServer(SERVER_NAME);
            Assertions.assertTrue(this.runner.isActive());

            this.runner.stopServer();
            Assertions.assertFalse(this.runner.isActive());

            this.runner.startServer(OTHER_SERVER_NAME);
            Assertions.assertEquals(OTHER_SERVER_NAME, this.runner.getServerName());

        } catch (IOException | ServerRunningException exception) {
            Assertions.fail(exception.getMessage());
        }
    }
}