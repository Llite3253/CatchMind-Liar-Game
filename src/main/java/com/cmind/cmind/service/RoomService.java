package com.cmind.cmind.service;

import com.cmind.cmind.dto.Room;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    private final List<Room> rooms = new ArrayList<>();
    private int nextId = 1;

    public List<Room> getRooms() {
        return rooms;
    }

    public Room createRoom(String name) {
        Room room = new Room(nextId++, name, 0);
        rooms.add(room);
        return room;
    }

    public void increaseCount(String roomId) {
        rooms.stream()
                .filter(r -> r.getId() == Integer.parseInt(roomId))
                .findFirst()
                .ifPresent(r -> r.setCount(r.getCount() + 1));
    }

    public void decreaseCount(String roomId) {
        rooms.stream()
                .filter(r -> r.getId() == Integer.parseInt(roomId))
                .findFirst()
                .ifPresent(r -> r.setCount(Math.max(0, r.getCount() - 1)));
    }

    public void removeRoomIfEmpty(String roomId) {
        rooms.removeIf(r -> r.getId() == Integer.parseInt(roomId) && r.getCount() == 0);
    }

}