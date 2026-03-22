package core;


import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

import java.util.Random;

import static core.Game.avatar;


public class Encounter {
    public static final int W = Main.WIDTH;
    public static final int H = Main.HEIGHT;

    public static void startEncounter() {
        TETile[][] g = new TETile[W][H];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                g[x][y] = Tileset.FLOOR;
            }
        }
        int aX = 0;
        int aY = 0;
        g[aX][aY] = avatar;

        Random random = new Random();
        int num = RandomUtils.uniform(random, 2, 6);
        int coin = num;
        for (int i = 0; i < num; i++){
            int putX = RandomUtils.uniform(random, 1, 40);
            int putY = RandomUtils.uniform(random, 1, 20);
            g[putX][putY] = Tileset.WATER;
        }

        TERenderer te1 = new TERenderer();
        te1.initialize(W, H);
        te1.renderFrame(g);


        while (coin != 0) {
            if (StdDraw.hasNextKeyTyped()) {
                char move = Character.toUpperCase(StdDraw.nextKeyTyped());
                int newX = aX;
                int newY = aY;

                if (move == 'W') {
                    newY++;
                } else if (move == 'S') {
                    newY--;
                } else if (move == 'A') {
                    newX--;
                } else if (move == 'D'){
                    newX++;
                }

                if (newX >= 0 && newX < W && newY >= 0 && newY < H) {
                    if (g[newX][newY] == Tileset.WATER){
                        coin --;
                        g[newX][newY] = Tileset.FLOOR;
                    }

                    g[aX][aY] = Tileset.FLOOR;
                    aX = newX;
                    aY = newY;
                    g[aX][aY] = avatar;
                    te1.renderFrame(g);
                }
            }
        }
    }
}
