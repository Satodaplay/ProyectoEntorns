package com.example.trivia.dao;

import com.example.trivia.model.Question;
import java.sql.*;
import java.util.UUID;

public class QuestionDAO {
    private final Connection connection;

    public QuestionDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID roundId, String type, String text, String mediaUrl) throws SQLException {
        String sql = """
            INSERT INTO questions (round_id, type, text, media_url)
            VALUES (?, ?, ?, ?)
            RETURNING question_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roundId);
            stmt.setString(2, type);
            stmt.setString(3, text);
            if (mediaUrl != null) stmt.setString(4, mediaUrl); else stmt.setNull(4, Types.VARCHAR);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("question_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Question getById(UUID questionId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE question_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Question(
                    (UUID) rs.getObject("question_id"),
                    (UUID) rs.getObject("round_id"),
                    rs.getString("type"),
                    rs.getString("text"),
                    rs.getString("media_url")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Question question) throws SQLException {
        String sql = """
            UPDATE questions
            SET round_id = ?, type = ?, text = ?, media_url = ?
            WHERE question_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, question.roundId());
            stmt.setString(2, question.type());
            stmt.setString(3, question.text());
            if (question.mediaUrl() != null) stmt.setString(4, question.mediaUrl()); else stmt.setNull(4, Types.VARCHAR);
            stmt.setObject(5, question.questionId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, questionId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}