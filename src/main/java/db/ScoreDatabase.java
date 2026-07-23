package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreDatabase {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/deadlock_game";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "abc123";

    public ScoreDatabase() {
        createTable();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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
            System.out.println("Database Error: " + e.getMessage());
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
            System.out.println("Database Error: " + e.getMessage());
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
            System.out.println("Database Error: " + e.getMessage());
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
            System.out.println("Database Error: " + e.getMessage());
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
            System.out.println("Database Error: " + e.getMessage());
        }
        return "No scores yet";
    }
}
