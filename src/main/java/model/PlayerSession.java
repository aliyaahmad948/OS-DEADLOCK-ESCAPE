package model;

public class PlayerSession {

    private static PlayerSession instance;
    private String playerName;

    private PlayerSession() {}

    public static PlayerSession getInstance() {
        if (instance == null) {
            instance = new PlayerSession();
        }
        return instance;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
