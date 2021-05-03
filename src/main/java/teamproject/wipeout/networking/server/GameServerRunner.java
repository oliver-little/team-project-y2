package teamproject.wipeout.networking.server;

import com.google.gson.Gson;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import teamproject.wipeout.game.UI.GameMode;

import java.io.*;
import java.util.List;

/**
 * {@code GameServerRunner} is a utility which deals with
 * a {@link GameServer} in a child process that is created by this utility.
 */
public class GameServerRunner {

    private String serverName;
    private short serverPort;
    private boolean gameRunning;

    private Process serverProcess;
    private BufferedWriter processWriter;
    private BufferedReader processReader;

    /**
     * Creates a new child process and starts a {@link GameServer} with the given name.
     * Only one {@code GameServer} can be run.
     *
     * @param serverName    Name for the {@code GameServer}
     * @param gameMode      Chosen game mode of type {@link GameMode}
     * @param gameModeValue Chosen {@code long} value for the game mode
     * @throws IOException Thrown when the child process cannot be started.
     */
    // Running a child process: https://www.programmersought.com/article/95206092506/ (template that was used)
    public short startServer(String serverName, GameMode gameMode, long gameModeValue) throws IOException {
        // Only one game server can be running
        if (this.serverProcess != null) {
            return this.serverPort;
        }

        String javaHome = System.getProperty("java.home").replace("%20", " ");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        String ownClasspath = GameServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String javafxGraphicsClasspath = Point2D.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String javafxBeansClasspath = DoubleProperty.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String gsonClasspath = Gson.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String classpath = ownClasspath + this.getClasspathSeparator() +
                javafxGraphicsClasspath + this.getClasspathSeparator() +
                javafxBeansClasspath + this.getClasspathSeparator() +
                gsonClasspath;
        classpath = classpath.replace("%20", " ");

        String className = GameServer.class.getName();

        List<String> theCommand = List.of(
                javaBin, "-cp", classpath, className,
                serverName, gameMode.toString(), Long.toString(gameModeValue)
        );

        ProcessBuilder sProcessBuilder = new ProcessBuilder(theCommand);
        sProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        this.serverName = serverName;
        this.gameRunning = false;

        this.serverProcess = sProcessBuilder.start();
        this.processWriter = new BufferedWriter(new OutputStreamWriter(this.serverProcess.getOutputStream()));
        this.processReader = new BufferedReader(new InputStreamReader(this.serverProcess.getInputStream()));

        String serverPortString = this.processReader.readLine();
        this.serverPort = Short.parseShort(serverPortString);
        return this.serverPort;
    }

    /**
     * {@code serverName} getter
     *
     * @return Name of the {@link GameServer}. Can be {@code null} if no {@code GameServer} is active.
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * {@code gameRunning} getter
     *
     * @return {@code true} if the game is running on the server. <br> Otherwise {@code false}.
     */
    public boolean isGameRunning() {
        return this.gameRunning;
    }

    /**
     * @return {@code true} if the {@link GameServer} is up and running. <br> Otherwise {@code false}.
     */
    public boolean isServerActive() {
        if (this.processWriter == null) {
            return false;
        }

        try {
            this.processWriter.write(ProcessMessage.CONFIRMATION.rawValue + '\n');
            this.processWriter.flush();

            String confirmation = this.processReader.readLine();
            if (confirmation != null && confirmation.equals(ProcessMessage.CONFIRMATION.rawValue)) {
                return true;
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
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


    /**
     * Method which distinguishes which OS is the app running on (Windows or UNIX-based)
     * and returns suitable classpath separator for the particular OS.
     *
     * @return {@code char} classpath separator suitable for the current OS
     */
    protected char getClasspathSeparator() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        return isWindows ? ';' : ':';
    }

}
