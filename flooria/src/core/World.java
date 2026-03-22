package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.RandomUtils.uniform;

public class World {
    private final int width;
    private final int height;
    private final TETile[][] world;
    private final Random random;
    private final List<Room> rooms;

    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.world = new TETile[width][height];
        this.random = new Random(seed);
        this.rooms = new ArrayList<>();
        initialize();
    }

    private void initialize() {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void createRooms() {
        int roomNum = uniform(this.random, 15, 25);
        for (int i = 0; i < roomNum; i++) {
            Room r = createARoom();
            r.render(this.world);
            rooms.add(r);
        }
        connectRooms();
    }

    public Room createARoom() {
        int w = RandomUtils.uniform(random, 5, 10);
        int h = RandomUtils.uniform(random, 4, 9);
        int x = RandomUtils.uniform(random, 1, this.width - w - 1);
        int y = RandomUtils.uniform(random, 1, this.height - h - 1);
        return new Room(x, y, w, h);
    }

    public void connectRooms() {
        Hallway hw = new Hallway();
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room r1 = rooms.get(i);
            Room r2 = rooms.get(i + 1);

            int x1 = r1.centerX();
            int x2 = r2.centerX();
            int y1 = r1.centerY();
            int y2 = r2.centerY();
            hw.createHallway(x1, y1, x2, y2, world);
        }
    }

    public TETile[][] getWorld() {
        return this.world;
    }
}
