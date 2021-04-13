package teamproject.wipeout.networking.client;

import javafx.collections.MapChangeListener;
import org.junit.jupiter.api.*;
import teamproject.wipeout.networking.server.GameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerDiscoveryTest {

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
                this.serverName.set(change.getKey());
                this.serverAddress.set(change.getValueAdded());
            });

        } catch (UnknownHostException e) {
            fail(e.getMessage());
        }
    }

    @BeforeEach
    void setUp() throws IOException, ClassNotFoundException {
        this.serverName = new AtomicReference<String>(null);
        this.serverAddress = new AtomicReference<InetSocketAddress>(null);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (this.serverDiscovery.isActive.get()) {
            this.serverDiscovery.stopLookingForServers();
        }
        this.serverDiscovery.availableServers.clear();
    }

    @RepeatedTest(5)
    @Timeout(value = 3, unit = TimeUnit.SECONDS) // Longer because of running the server each time
    void testFindingGameServer() throws IOException {
        String testServerName = "TestServer#123";
        GameServer gameServer = null;
        try {
            gameServer = new GameServer(testServerName);
            gameServer.startClientSearch();

            this.serverDiscovery.startLookingForServers();
            Thread.sleep(505);

            gameServer.stopServer();

            Thread.sleep(50);

        } catch (IOException | InterruptedException | ReflectiveOperationException exception) {
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
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    void testStartLookingForServers() {
        Assertions.assertFalse(this.serverDiscovery.getIsActive(),
                "ServerDiscovery is active despite the fact that it has not started searching.");
        try {
            this.serverDiscovery.startLookingForServers();

            Thread.sleep(50);

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