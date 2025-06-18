package com.example.trivia.dao;

import com.example.trivia.model.Room;
import java.sql.*;
import java.util.UUID;

public class RoomDAO {
    private final Connection connection;

    public RoomDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public UUID insert(String slug, UUID settingsId) throws SQLException {
        String sql = """
            INSERT INTO rooms (slug, settings_id)
            VALUES (?, ?)
            RETURNING room_id;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, slug);
            stmt.setObject(2, settingsId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("room_id");
            } else {
                throw new SQLException("Insert failed, no ID returned.");
            }
        }
    }

    // READ
    public Room getById(UUID roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roomId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Room(
                    (UUID) rs.getObject("room_id"),
                    rs.getString("slug"),
                    rs.getTimestamp("created_at"),
                    (UUID) rs.getObject("settings_id")
                );
            } else {
                return null;
            }
        }
    }

    // UPDATE
    public boolean update(Room room) throws SQLException {
        String sql = """
            UPDATE rooms
            SET slug = ?, settings_id = ?
            WHERE room_id = ?;
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, room.slug());
            stmt.setObject(2, room.settingsId());
            stmt.setObject(3, room.roomId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // DELETE
    public boolean delete(UUID roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, roomId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }
}