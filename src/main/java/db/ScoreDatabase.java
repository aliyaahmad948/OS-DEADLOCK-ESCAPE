package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScoreDatabase {

    // useSSL=false avoids SSL handshake instability on localhost;
    // allowPublicKeyRetrieval=true is REQUIRED for MySQL 8's default
    // caching_sha2_password auth over a non-SSL connection (otherwise the
    // classic "Public Key Retrieval is not allowed" error makes saves fail).
    // Short timeouts mean the app never hangs if MySQL is down.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/deadlock_game"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&connectTimeout=3000&socketTimeout=5000";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "abc123";

    private String lastError;
    private boolean available = true;

    public ScoreDatabase() {
        createTable();
        createModeTable();
        createProgressTable();
    }

    /** True if the last database operation succeeded (MySQL reachable + tables OK). */
    public boolean isAvailable() {
        return available;
    }

    /** Last failure reason (null if connection is healthy). */
    public String getLastError() {
        return lastError;
    }

    /** Opens a throwaway connection to confirm MySQL is reachable right now. */
    public boolean testConnection() {
        try (Connection conn = connect()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection connect() throws SQLException {
        try {
            available = true;
            lastError = null;
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            lastError = e.getMessage();
            available = false;
            throw e;
        }
    }

    private void recordError(SQLException e) {
        lastError = e.getMessage();
        available = false;
        System.out.println("Database Error: " + e.getMessage());
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scores ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "player_name VARCHAR(100) NOT NULL, "
                + "level_number INT NOT NULL, "
                + "score INT NOT NULL, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            recordError(e);
        }
    }

    /**
     * New table for mode-based scores (Phase 16 enhancement).
     * Kept separate from the legacy 'scores' table to avoid breaking existing data.
     */
    private void createModeTable() {
        String sql = "CREATE TABLE IF NOT EXISTS scores_mode ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "player_name VARCHAR(100) NOT NULL, "
                + "game_mode VARCHAR(50) NOT NULL, "
                + "score INT NOT NULL, "
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            recordError(e);
        }
    }

    /**
     * New table for player progress (level unlocks, stars, XP).
     */
    private void createProgressTable() {
        String sql = "CREATE TABLE IF NOT EXISTS level_progress ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "player_name VARCHAR(100) NOT NULL, "
                + "game_mode VARCHAR(50) NOT NULL, "
                + "level_number INT NOT NULL, "
                + "stars INT DEFAULT 0, "
                + "best_score INT DEFAULT 0, "
                + "completed BOOLEAN DEFAULT FALSE, "
                + "UNIQUE KEY idx_progress (player_name, game_mode, level_number)"
                + ")";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            recordError(e);
        }
    }

    public void saveScore(String playerName, int levelNumber, int score) {
        String sql = "INSERT INTO scores (player_name, level_number, score) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setInt(2, levelNumber);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            recordError(e);
        }
    }

    public List<String[]> getLeaderboard(int levelNumber) {
        List<String[]> leaderboard = new ArrayList<>();
        String sql = "SELECT player_name, score, timestamp FROM scores "
                + "WHERE level_number = ? ORDER BY player_name ASC, score DESC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, levelNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String[] row = new String[]{
                        rs.getString("player_name"),
                        String.valueOf(rs.getInt("score")),
                        rs.getString("timestamp")
                };
                leaderboard.add(row);
            }
        } catch (SQLException e) {
            recordError(e);
        }
        return leaderboard;
    }

    public int getHighestScore(int levelNumber) {
        String sql = "SELECT MAX(score) FROM scores WHERE level_number = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, levelNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            recordError(e);
        }
        return 0;
    }

    public String getHighestScorer(int levelNumber) {
        String sql = "SELECT player_name, score FROM scores "
                + "WHERE level_number = ? ORDER BY score DESC LIMIT 1";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, levelNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("player_name") + " - " + rs.getInt("score");
            }
        } catch (SQLException e) {
            recordError(e);
        }
        return "No scores yet";
    }

    // ================= Phase 16: Mode-based methods (new) =================

    public void saveModeScore(String playerName, String gameMode, int score) {
        String sql = "INSERT INTO scores_mode (player_name, game_mode, score) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, gameMode);
            pstmt.setInt(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            recordError(e);
        }
    }

    /**
     * Mode leaderboard: shows each player's BEST score per mode, one row per
     * player. Usernames are compared case-insensitively ("aliya" and "Aliya"
     * are the same player), and only the highest-scoring row of each player is
     * kept (original casing of that row is preserved for display).
     */
    public List<String[]> getModeLeaderboard(String gameMode) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT player_name, score, timestamp FROM scores_mode "
                + "WHERE game_mode = ? ORDER BY score DESC, timestamp ASC";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gameMode);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("player_name"),
                        String.valueOf(rs.getInt("score")),
                        rs.getString("timestamp")
                });
            }
        } catch (SQLException e) {
            recordError(e);
        }
        return dedupeBestPerUser(rows, 15);
    }

    /**
     * Keeps only the BEST score per player (case-insensitive username),
     * preserving the first occurrence's original casing for display.
     * Expects input rows to be pre-sorted by score descending.
     */
    private List<String[]> dedupeBestPerUser(List<String[]> rows, int limit) {
        Map<String, String[]> best = new LinkedHashMap<>();
        for (String[] row : rows) {
            String key = row[0].trim().toLowerCase(Locale.ROOT);
            if (!best.containsKey(key)) {
                best.put(key, row);
            }
        }
        List<String[]> result = new ArrayList<>(best.values());
        if (result.size() > limit) {
            result = new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    /**
     * Saves/updates the player's best stars and score for a level.
     */
    public void saveLevelProgress(String playerName, String gameMode, int levelNumber,
                                  int stars, int score) {
        String sql = "INSERT INTO level_progress (player_name, game_mode, level_number, stars, best_score, completed) "
                + "VALUES (?, ?, ?, ?, ?, TRUE) "
                + "ON DUPLICATE KEY UPDATE stars = GREATEST(stars, VALUES(stars)), "
                + "best_score = GREATEST(best_score, VALUES(best_score)), completed = TRUE";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, gameMode);
            pstmt.setInt(3, levelNumber);
            pstmt.setInt(4, stars);
            pstmt.setInt(5, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            recordError(e);
        }
    }

    /**
     * Loads the player's saved level progress (game_mode, level_number, stars)
     * so stars + level unlocks survive app restarts. Empty list if none yet.
     */
    public List<String[]> loadLevelProgress(String playerName) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT game_mode, level_number, stars FROM level_progress "
                + "WHERE player_name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rows.add(new String[]{
                        rs.getString("game_mode"),
                        String.valueOf(rs.getInt("level_number")),
                        String.valueOf(rs.getInt("stars"))
                });
            }
        } catch (SQLException e) {
            recordError(e);
        }
        return rows;
    }
}