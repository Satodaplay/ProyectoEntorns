package com.example.trivia.dao;

import com.example.trivia.model.Game;
import java.sql.*;
import java.util.UUID;

public class GameDAO {
    private final Connection connection;

    public GameDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID roomId, Timestamp startedAt, Timestamp endedAt) throws SQLException {
        String sql = """
            INSERT INTO games (room_id, started_at, ended_at)
            VALUES (?, ?, ?)
            RETURNING game_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roomId);
            stmt.setTimestamp(2, startedAt);
            if (endedAt != null) stmt.setTimestamp(3, endedAt); else stmt.setNull(3, Types.TIMESTAMP);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("game_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Game getById(UUID gameId) throws SQLException {
        String sql = "SELECT * FROM games WHERE game_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gameId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Game(
                    (UUID) rs.getObject("game_id"),
                    (UUID) rs.getObject("room_id"),
                    rs.getTimestamp("started_at"),
                    rs.getTimestamp("ended_at")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Game game) throws SQLException {
        String sql = """
            UPDATE games
            SET room_id = ?, started_at = ?, ended_at = ?
            WHERE game_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, game.roomId());
            stmt.setTimestamp(2, game.startedAt());
            if (game.endedAt() != null) stmt.setTimestamp(3, game.endedAt()); else stmt.setNull(3, Types.TIMESTAMP);
            stmt.setObject(4, game.gameId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID gameId) throws SQLException {
        String sql = "DELETE FROM games WHERE game_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gameId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}