package teamproject.wipeout.networking.server;

import javafx.geometry.Point2D;

import java.io.*;
import java.util.List;

/**
 * {@code GameServerRunner} is a utility which deals with
 * a {@link GameServer} in a child process, which is created by this utility.
 */
public class GameServerRunner {

    protected String serverName;
    protected boolean gameRunning;

    protected Process serverProcess;
    protected BufferedWriter processWriter;
    protected BufferedReader processReader;

    /**
     * Creates a new child process and starts a {@link GameServer} with the given name.
     * Only one {@code GameServer} can be run.
     *
     * @param serverName Name for the {@code GameServer}
     * @throws IOException Thrown when the child process cannot be started.
     */
    // Running a child process: https://www.programmersought.com/article/95206092506/ (source)
    public void startServer(String serverName) throws ServerRunningException, IOException {
        // Only one game server can be running
        if (this.isActive()) {
            throw new ServerRunningException(this.serverName + " - server is already running!");
        }

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String ownClasspath = GameServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String javafxGraphicsClasspath = Point2D.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String classpath = ownClasspath + ":" + javafxGraphicsClasspath;
        String className = GameServer.class.getName();

        List<String> theCommand = List.of(javaBin, "-cp", classpath, className, serverName);

        ProcessBuilder sProcessBuilder = new ProcessBuilder(theCommand);
        sProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process newProcess = sProcessBuilder.start();

        this.serverName = serverName;
        this.gameRunning = false;

        this.serverProcess = newProcess;
        this.processWriter = new BufferedWriter(new OutputStreamWriter(newProcess.getOutputStream()));
        this.processReader = new BufferedReader(new InputStreamReader(newProcess.getInputStream()));
    }

    /**
     * {@code serverName} variable getter
     *
     * @return name of the active {@link GameServer}.<br>
     * Can be {@code null} if no {@code GameServer} is active.
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * {@code gameRunning} variable getter
     *
     * @return {@code true} if the game is running on the server. <br> Otherwise {@code false}.
     */
    public boolean isGameRunning() {
        return this.gameRunning;
    }

    /**
     * {@code isActive} variable getter
     *
     * @return {@code true} if the {@link GameServer} is active. <br> Otherwise {@code false}.
     */
    public boolean isActive() {
        return this.serverProcess != null;
    }

    /**
     * Sends a {@link ProcessMessage} to the {@link GameServer} to start the game
     * and waits for confirmation.
     *
     * @throws IOException Thrown when the {@code ProcessMessage} cannot be sent.
     */
    public void startGame() throws IOException {
        this.processWriter.write(ProcessMessage.START_GAME.rawValue + '\n');
        this.processWriter.flush();

        String confirmation = this.processReader.readLine();
        if (confirmation.equals(ProcessMessage.START_GAME.rawValue + ProcessMessage.CONFIRMATION.rawValue)) {
            this.gameRunning = true;
        }
    }

    /**
     * Sends a {@link ProcessMessage} to the {@link GameServer} to stop the game
     * and waits for confirmation.
     *
     * @throws IOException Thrown when the {@code ProcessMessage} cannot be sent.
     */
    public void stopGame() throws IOException {
        this.processWriter.write(ProcessMessage.STOP_GAME.rawValue + '\n');
        this.processWriter.flush();

        String confirmation = this.processReader.readLine();
        if (confirmation.equals(ProcessMessage.STOP_GAME.rawValue + ProcessMessage.CONFIRMATION.rawValue)) {
            this.gameRunning = false;
        }
    }

    /**
     * Sends a {@link ProcessMessage} to the {@link GameServer} to stop the server.
     * Subsequently, the child process containing the {@code GameServer} is killed.
     *
     * @throws IOException Thrown when there is a problem with stopping the server and its child process.
     */
    public void stopServer() throws IOException {
        this.processWriter.write(ProcessMessage.STOP_SERVER.rawValue + '\n');
        this.processWriter.flush();

        this.gameRunning = false;

        try {
            this.serverProcess.waitFor(); // Busy waiting until the process exits

            this.processWriter.close();
            this.processReader.close();

            this.serverName = null;
            this.serverProcess = null;
            this.processWriter = null;
            this.processReader = null;

        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

}