package com.example.trivia.controller;

import com.example.trivia.model.Room;
import com.example.trivia.model.Settings;
import com.example.trivia.model.Player;
import com.example.trivia.model.Team;
import com.example.trivia.repository.RoomRepository;
import com.example.trivia.repository.PlayerRepository;
import com.example.trivia.repository.TeamRepository;
import com.example.trivia.repository.SettingsRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomRepository roomRepo;
    private final PlayerRepository playerRepo;
    private final TeamRepository teamRepo;
    private final SettingsRepository settingsRepo;

    public RoomController(
            RoomRepository roomRepo,
            PlayerRepository playerRepo,
            TeamRepository teamRepo,
            SettingsRepository settingsRepo) {
        this.roomRepo = roomRepo;
        this.playerRepo = playerRepo;
        this.teamRepo = teamRepo;
        this.settingsRepo = settingsRepo;
    }

    /** R01: Crear sala + settings por defecto */
    @PostMapping
    public ResponseEntity<Room> createRoom() {
        // 1) Creamos primero los settings
        Settings settings = new Settings();
        settings.setRounds(10);
        settings.setTimePerRound(60);
        settings.setQuestionsPerRound(5);
        settings.setDifficulty("easy");
        settings.setMaxPlayersPerTeam(5);
        settingsRepo.save(settings);

        // 2) Creamos la room apuntando a dichos settings
        Room room = new Room();
        room.setRoomId(UUID.randomUUID().toString());
        room.setCreatedAt(Instant.now());
        room.setSettingsId(settings.getSettingsId());
        roomRepo.save(room);

        URI location = URI.create("/rooms/" + room.getRoomId());
        return ResponseEntity.created(location).body(room);
    }

    /** R02: Obtener sala */
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        return roomRepo.findById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** R02–R03: Unirse a sala */
    @PostMapping("/{roomId}/players")
    public ResponseEntity<Player> joinRoom(
            @PathVariable String roomId,
            @RequestBody Map<String, Object> body,
            HttpSession session
    ) {
        Optional<Room> opt = roomRepo.findById(roomId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Player player = new Player();
        player.setPlayerId(UUID.randomUUID().toString());
        player.setRoomId(roomId);
        player.setUsername((String) body.get("username"));
        // primer jugador es host
        player.setHost(playerRepo.findByRoomId(roomId).isEmpty());
        playerRepo.save(player);

        session.setAttribute(roomId, player);
        URI location = URI.create("/rooms/" + roomId + "/players/" + player.getPlayerId());
        return ResponseEntity.created(location).body(player);
    }

    /** R02: Listar jugadores */
    @GetMapping("/{roomId}/players")
    public ResponseEntity<List<Player>> getRoomPlayers(@PathVariable String roomId) {
        if (roomRepo.findById(roomId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(playerRepo.findByRoomId(roomId));
    }

    /** R04: Obtener settings */
    @GetMapping("/{roomId}/settings")
    public ResponseEntity<Settings> getSettings(@PathVariable String roomId) {
        return roomRepo.findById(roomId)
                .flatMap(r -> settingsRepo.findById(r.getSettingsId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** R04: Actualizar settings (solo host) */
    @PutMapping("/{roomId}/settings")
    public ResponseEntity<Settings> updateSettings(
            @PathVariable String roomId,
            @RequestBody Settings newSettings,
            HttpSession session
    ) {
        Optional<Room> or = roomRepo.findById(roomId);
        if (or.isEmpty()) return ResponseEntity.notFound().build();

        Player current = (Player) session.getAttribute(roomId);
        if (current == null || !current.isHost()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Guardamos cambios
        settingsRepo.save(newSettings);
        return ResponseEntity.ok(newSettings);
    }

    // ... (el resto de endpoints sobre teams, assignaciones, juegos, etc. queda igual)
}
