package teamproject.wipeout.networking.client;

import javafx.collections.MapChangeListener;
import org.junit.jupiter.api.*;
import teamproject.wipeout.GameMode;
import teamproject.wipeout.networking.server.GameServer;
import teamproject.wipeout.util.resources.ResourceLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerDiscoveryTest {

    private static final String TEST_SERVER_NAME = "TestServer#123";
    private static final int GENERAL_DELAY = 50;

    private GameServer gameServer;
    private ServerDiscovery serverDiscovery;

    private AtomicReference<String> serverName;
    private AtomicReference<InetSocketAddress> serverAddress;

    @BeforeAll
    void initializeServerDiscovery() {
        ResourceLoader.setTargetClass(ResourceLoader.class);

        this.serverName = new AtomicReference<String>(null);
        this.serverAddress = new AtomicReference<InetSocketAddress>(null);

        try {
            this.gameServer = new GameServer(TEST_SERVER_NAME, GameMode.TIME_MODE, 1_000);

            this.serverDiscovery = new ServerDiscovery();
            this.serverDiscovery.availableServers.addListener((MapChangeListener.Change<? extends String, ? extends InetSocketAddress> change) -> {
                if (change.wasAdded()) {
                    this.serverName.set(change.getKey());
                    this.serverAddress.set(change.getValueAdded());
                } else {
                    this.serverName.set(null);
                    this.serverAddress.set(null);
                }
            });

        } catch (IOException | ReflectiveOperationException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        this.serverName.set(null);
        this.serverAddress.set(null);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.gameServer.stopServer();

        if (this.serverDiscovery.isActive.get()) {
            this.serverDiscovery.stopLookingForServers();
        }
        this.serverDiscovery.availableServers.clear();
        this.serverDiscovery.lastHeardServers.clear();
    }

    @RepeatedTest(5)
    @Timeout(value = 3, unit = TimeUnit.SECONDS) // Longer because of running the server each time
    void testFindingGameServer() {
        try {
            this.gameServer.startClientSearch();

            this.serverDiscovery.startLookingForServers();
            Thread.sleep(ServerDiscovery.REFRESH_DELAY);

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(TEST_SERVER_NAME, this.serverName.get());
        Assertions.assertNotNull(this.serverAddress.get());

        Assertions.assertEquals(
                this.serverDiscovery.availableServers.get(TEST_SERVER_NAME),
                this.serverDiscovery.getAvailableServers().get(TEST_SERVER_NAME)
        );
    }

    @RepeatedTest(5)
    @Timeout(value = 6, unit = TimeUnit.SECONDS) // Longer because of running the server each time
    void testLoosingGameServer() {
        try {
            this.serverDiscovery.startLookingForServers();
            this.gameServer.startClientSearch();

            Thread.sleep(ServerDiscovery.REFRESH_DELAY);

            this.gameServer.stopClientSearch();

            Thread.sleep(ServerDiscovery.REFRESH_DELAY * 4);

            Assertions.assertNull(this.serverName.get());
            Assertions.assertNull(this.serverAddress.get());

            Assertions.assertTrue(this.serverDiscovery.availableServers.isEmpty());
            Assertions.assertTrue(this.serverDiscovery.getAvailableServers().isEmpty());

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testStartLookingForServers() {
        Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                "ServerDiscovery is active despite the fact that it has not started searching.");
        try {
            this.serverDiscovery.startLookingForServers();

            Thread.sleep(ServerDiscoveryTest.GENERAL_DELAY);

            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            this.serverDiscovery.startLookingForServers();

            Thread.sleep(ServerDiscoveryTest.GENERAL_DELAY);

            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testStartStopLookingForServers() {
        try {
            this.serverDiscovery.startLookingForServers();
            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            this.serverDiscovery.startLookingForServers();
            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            this.serverDiscovery.startLookingForServers();
            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            // Duplicate start calls to ensure that the duplicates are ignored

            this.serverDiscovery.stopLookingForServers();
            Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is active despite the fact that it stopped searching.");

            this.serverDiscovery.stopLookingForServers();
            Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is active despite the fact that it stopped searching.");

            this.serverDiscovery.stopLookingForServers();
            Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is active despite the fact that it stopped searching.");

            // Duplicate stop calls to ensure that the duplicates are ignored

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

}