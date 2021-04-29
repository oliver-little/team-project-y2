package teamproject.wipeout.networking.data;

import teamproject.wipeout.GameMode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@code InitContainer} class represents a data container needed to initialize a game client before the gameplay.
 * <br>
 * {@code InitContainer} implements {@link Serializable}.
 */
public class InitContainer implements Serializable {

    private GameMode gameMode;
    private long gameModeValue;

    private Integer clientID;
    private Integer farmID;
    private String clientSpriteSheet;

    /**
     * Default initializer for a {@link InitContainer}.
     *
     * @param gameMode          Game mode
     * @param gameModeValue     Game mode value
     * @param clientID          Client ID
     * @param farmID            Picked farm ID
     * @param clientSpriteSheet Picked client's sprite sheet
     */
    public InitContainer(GameMode gameMode, long gameModeValue, Integer clientID, Integer farmID, String clientSpriteSheet) {
        this.gameMode = gameMode;
        this.gameModeValue = gameModeValue;

        this.clientID = clientID;
        this.farmID = farmID;
        this.clientSpriteSheet = clientSpriteSheet;
    }

    /**
     * {@code gameModeValue} getter
     *
     * @return Game mode value
     */
    public long getGameModeValue() {
        return this.gameModeValue;
    }

    /**
     * {@code clientID} getter
     *
     * @return Client ID
     */
    public GameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * {@code clientID} getter
     *
     * @return Client ID
     */
    public int getClientID() {
        return this.clientID;
    }

    /**
     * {@code farmID} getter
     *
     * @return Farm ID
     */
    public int getFarmID() {
        return this.farmID;
    }

    /**
     * {@code clientSpriteSheet} getter
     *
     * @return Client's sprite sheet name
     */
    public String getClientSpriteSheet() {
        return clientSpriteSheet;
    }


    // Methods writeObject(), readObject() and readObjectNoData() are implemented
    // to make PlayerState serializable despite it containing non-serializable properties (Point2D)

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(this.gameModeValue);
        out.writeUTF(this.gameMode.toString());

        out.writeInt(this.clientID);
        out.writeInt(this.farmID);
        if (this.clientSpriteSheet == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(this.clientSpriteSheet);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        this.gameModeValue = in.readLong();
        this.gameMode = GameMode.fromName(in.readUTF());

        this.clientID = in.readInt();
        this.farmID = in.readInt();
        if (in.readBoolean()) {
            this.clientSpriteSheet = in.readUTF();
        } else {
            this.clientSpriteSheet = null;
        }
    }

    private void readObjectNoData() throws IOException {
        throw new IOException("InitContainer is corrupted");
    }

    // Customized equals() and hashCode() methods implemented

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        InitContainer that = (InitContainer) o;
        return this.clientID.equals(that.clientID) && this.farmID.equals(that.farmID);
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(this.clientID / this.farmID).hashCode();
    }

}