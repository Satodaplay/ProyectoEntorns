package com.example.trivia.dao;

import com.example.trivia.model.Round;
import java.sql.*;
import java.util.UUID;

public class RoundDAO {
    private final Connection connection;

    public RoundDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID gameId, int number, Timestamp startedAt, Timestamp endedAt) throws SQLException {
        String sql = """
            INSERT INTO rounds (game_id, number, started_at, ended_at)
            VALUES (?, ?, ?, ?)
            RETURNING round_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, gameId);
            stmt.setInt(2, number);
            stmt.setTimestamp(3, startedAt);
            if (endedAt != null) stmt.setTimestamp(4, endedAt); else stmt.setNull(4, Types.TIMESTAMP);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("round_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Round getById(UUID roundId) throws SQLException {
        String sql = "SELECT * FROM rounds WHERE round_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roundId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Round(
                    (UUID) rs.getObject("round_id"),
                    (UUID) rs.getObject("game_id"),
                    rs.getInt("number"),
                    rs.getTimestamp("started_at"),
                    rs.getTimestamp("ended_at")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Round round) throws SQLException {
        String sql = """
            UPDATE rounds
            SET game_id = ?, number = ?, started_at = ?, ended_at = ?
            WHERE round_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, round.gameId());
            stmt.setInt(2, round.number());
            stmt.setTimestamp(3, round.startedAt());
            if (round.endedAt() != null) stmt.setTimestamp(4, round.endedAt()); else stmt.setNull(4, Types.TIMESTAMP);
            stmt.setObject(5, round.roundId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID roundId) throws SQLException {
        String sql = "DELETE FROM rounds WHERE round_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roundId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}