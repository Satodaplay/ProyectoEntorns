package com.example.trivia.dao;

import com.example.trivia.model.Settings;
import java.sql.*;
import java.util.UUID;

public class SettingsDAO {
    private final Connection connection;

    public SettingsDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(int rounds, int timePerRound, int questionsPerRound, String difficulty, int maxPlayersPerTeam) throws SQLException {
        String sql = """
            INSERT INTO settings (rounds, time_per_round, questions_per_round, difficulty, max_players_per_team)
            VALUES (?, ?, ?, ?, ?)
            RETURNING settings_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rounds);
            stmt.setInt(2, timePerRound);
            stmt.setInt(3, questionsPerRound);
            stmt.setString(4, difficulty);
            stmt.setInt(5, maxPlayersPerTeam);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("settings_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Settings getById(UUID settingsId) throws SQLException {
        String sql = "SELECT * FROM settings WHERE settings_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, settingsId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Settings(
                    (UUID) rs.getObject("settings_id"),
                    rs.getInt("rounds"),
                    rs.getInt("time_per_round"),
                    rs.getInt("questions_per_round"),
                    rs.getString("difficulty"),
                    rs.getInt("max_players_per_team")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Settings settings) throws SQLException {
        String sql = """
            UPDATE settings
            SET rounds = ?, time_per_round = ?, questions_per_round = ?, difficulty = ?, max_players_per_team = ?
            WHERE settings_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, settings.rounds());
            stmt.setInt(2, settings.timePerRound());
            stmt.setInt(3, settings.questionsPerRound());
            stmt.setString(4, settings.difficulty());
            stmt.setInt(5, settings.maxPlayersPerTeam());
            stmt.setObject(6, settings.settingsId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID settingsId) throws SQLException {
        String sql = "DELETE FROM settings WHERE settings_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, settingsId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}