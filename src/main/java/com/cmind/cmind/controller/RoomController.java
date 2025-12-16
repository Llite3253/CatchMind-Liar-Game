package com.cmind.cmind.controller;

import com.cmind.cmind.dto.Room;
import com.cmind.cmind.service.RoomService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cmind/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ModelAndView roomPage() {
        return new ModelAndView("room");
    }

    @GetMapping("/list")
    public List<Room> getRooms() {
        return roomService.getRooms();
    }

    @PostMapping
    public Room createRoom(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return roomService.createRoom(name);
    }
}