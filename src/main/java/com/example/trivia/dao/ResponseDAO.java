package com.example.trivia.dao;

import com.example.trivia.model.Response;
import java.sql.*;
import java.util.UUID;

public class ResponseDAO {
    private final Connection connection;

    public ResponseDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID questionId, UUID playerId, Timestamp submittedAt, String textReply, UUID optionId, Boolean isCorrect) throws SQLException {
        String sql = """
            INSERT INTO responses (question_id, player_id, submitted_at, text_reply, option_id, is_correct)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING response_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, questionId);
            stmt.setObject(2, playerId);
            stmt.setTimestamp(3, submittedAt);
            if (textReply != null) stmt.setString(4, textReply); else stmt.setNull(4, Types.VARCHAR);
            if (optionId != null) stmt.setObject(5, optionId); else stmt.setNull(5, Types.OTHER);
            if (isCorrect != null) stmt.setBoolean(6, isCorrect); else stmt.setNull(6, Types.BOOLEAN);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("response_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Response getById(UUID responseId) throws SQLException {
        String sql = "SELECT * FROM responses WHERE response_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, responseId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Response(
                    (UUID) rs.getObject("response_id"),
                    (UUID) rs.getObject("question_id"),
                    (UUID) rs.getObject("player_id"),
                    rs.getTimestamp("submitted_at"),
                    rs.getString("text_reply"),
                    (UUID) rs.getObject("option_id"),
                    rs.getObject("is_correct") != null ? rs.getBoolean("is_correct") : null
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Response response) throws SQLException {
        String sql = """
            UPDATE responses
            SET question_id = ?, player_id = ?, submitted_at = ?, text_reply = ?, option_id = ?, is_correct = ?
            WHERE response_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, response.questionId());
            stmt.setObject(2, response.playerId());
            stmt.setTimestamp(3, response.submittedAt());
            if (response.textReply() != null) stmt.setString(4, response.textReply()); else stmt.setNull(4, Types.VARCHAR);
            if (response.optionId() != null) stmt.setObject(5, response.optionId()); else stmt.setNull(5, Types.OTHER);
            if (response.isCorrect() != null) stmt.setBoolean(6, response.isCorrect()); else stmt.setNull(6, Types.BOOLEAN);
            stmt.setObject(7, response.responseId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID responseId) throws SQLException {
        String sql = "DELETE FROM responses WHERE response_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, responseId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}