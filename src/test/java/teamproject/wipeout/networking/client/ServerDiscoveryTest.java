package teamproject.wipeout.networking.client;

import javafx.collections.MapChangeListener;
import org.junit.jupiter.api.*;
import teamproject.wipeout.networking.server.GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerDiscoveryTest {

    private static final int GENERAL_DELAY = 50;

    private ServerDiscovery serverDiscovery;
    private AtomicReference<String> serverName;
    private AtomicReference<InetSocketAddress> serverAddress;

    @BeforeAll
    void initializeServerDiscovery() {
        this.serverName = new AtomicReference<String>(null);
        this.serverAddress = new AtomicReference<InetSocketAddress>(null);

        try {
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

        } catch (UnknownHostException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        this.serverName.set(null);
        this.serverAddress.set(null);
    }

    @AfterEach
    void tearDown() {
        if (this.serverDiscovery.isActive.get()) {
            this.serverDiscovery.stopLookingForServers();
        }
        this.serverDiscovery.availableServers.clear();
        this.serverDiscovery.lastHeardServers.clear();
    }

    @RepeatedTest(5)
    @Timeout(value = 3, unit = TimeUnit.SECONDS) // Longer because of running the server each time
    void testFindingGameServer() {
        String testServerName = "TestServer#123";
        try {
            GameServer gameServer = new GameServer(testServerName);
            gameServer.startClientSearch();

            this.serverDiscovery.startLookingForServers();
            Thread.sleep(ServerDiscovery.REFRESH_DELAY);

            gameServer.stopServer();

            Thread.sleep(GENERAL_DELAY);

        } catch (IOException | ReflectiveOperationException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertEquals(testServerName, this.serverName.get());
        Assertions.assertNotNull(this.serverAddress.get());

        Assertions.assertEquals(
                this.serverDiscovery.availableServers.get(testServerName),
                this.serverDiscovery.getAvailableServers().get(testServerName)
        );
    }

    @RepeatedTest(5)
    @Timeout(value = 6, unit = TimeUnit.SECONDS) // Longer because of running the server each time
    void testLoosingGameServer() {
        String testServerName = "TestServer#456";
        try {
            GameServer gameServer = new GameServer(testServerName);
            gameServer.startClientSearch();

            this.serverDiscovery.startLookingForServers();
            Thread.sleep(ServerDiscovery.REFRESH_DELAY);

            gameServer.stopClientSearch();

            Thread.sleep(ServerDiscovery.REFRESH_DELAY * 2);

            gameServer.stopServer();

            Thread.sleep(GENERAL_DELAY);

        } catch (IOException | ReflectiveOperationException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }

        Assertions.assertNull(this.serverName.get());
        Assertions.assertNull(this.serverAddress.get());

        Assertions.assertTrue(this.serverDiscovery.availableServers.isEmpty());
        Assertions.assertTrue(this.serverDiscovery.getAvailableServers().isEmpty());
    }

    @RepeatedTest(5)
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testStartLookingForServers() {
        Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                "ServerDiscovery is active despite the fact that it has not started searching.");
        try {
            this.serverDiscovery.startLookingForServers();

            Thread.sleep(GENERAL_DELAY);

            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            this.serverDiscovery.startLookingForServers();

            Thread.sleep(GENERAL_DELAY);

            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

        } catch (IOException | InterruptedException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

    @RepeatedTest(5)
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    void testStopLookingForServers() {
        try {
            this.serverDiscovery.startLookingForServers();
            Assertions.assertTrue(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is not active despite the fact that it started searching.");

            this.serverDiscovery.stopLookingForServers();
            Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                    "ServerDiscovery is active despite the fact that it stopped searching.");

        } catch (IOException exception) {
            Assertions.fail(exception.getMessage());
        }
    }

}