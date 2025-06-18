package com.example.trivia.dao;

import com.example.trivia.model.Player;
import java.sql.*;
import java.util.UUID;

public class PlayerDAO {
    private final Connection connection;

    public PlayerDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID roomId, String username, boolean isHost, UUID teamId) throws SQLException {
        String sql = """
            INSERT INTO players (room_id, username, is_host, team_id)
            VALUES (?, ?, ?, ?)
            RETURNING player_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roomId);
            stmt.setString(2, username);
            stmt.setBoolean(3, isHost);
            if (teamId != null) stmt.setObject(4, teamId); else stmt.setNull(4, Types.OTHER);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("player_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Player getById(UUID playerId) throws SQLException {
        String sql = "SELECT * FROM players WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Player(
                    (UUID) rs.getObject("player_id"),
                    (UUID) rs.getObject("room_id"),
                    rs.getString("username"),
                    rs.getBoolean("is_host"),
                    (UUID) rs.getObject("team_id")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Player player) throws SQLException {
        String sql = """
            UPDATE players
            SET room_id = ?, username = ?, is_host = ?, team_id = ?
            WHERE player_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, player.roomId());
            stmt.setString(2, player.username());
            stmt.setBoolean(3, player.isHost());
            if (player.teamId() != null) stmt.setObject(4, player.teamId()); else stmt.setNull(4, Types.OTHER);
            stmt.setObject(5, player.playerId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID playerId) throws SQLException {
        String sql = "DELETE FROM players WHERE player_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}