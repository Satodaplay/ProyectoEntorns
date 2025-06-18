package com.example.trivia.dao;

import com.example.trivia.model.Team;
import java.sql.*;
import java.util.UUID;

public class TeamDAO {
    private final Connection connection;

    public TeamDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(UUID roomId, String name) throws SQLException {
        String sql = """
            INSERT INTO teams (room_id, name)
            VALUES (?, ?)
            RETURNING team_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roomId);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("team_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Team getById(UUID teamId) throws SQLException {
        String sql = "SELECT * FROM teams WHERE team_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Team(
                    (UUID) rs.getObject("team_id"),
                    (UUID) rs.getObject("room_id"),
                    rs.getString("name")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Team team) throws SQLException {
        String sql = """
            UPDATE teams
            SET room_id = ?, name = ?
            WHERE team_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, team.roomId());
            stmt.setString(2, team.name());
            stmt.setObject(3, team.teamId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID teamId) throws SQLException {
        String sql = "DELETE FROM teams WHERE team_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, teamId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}