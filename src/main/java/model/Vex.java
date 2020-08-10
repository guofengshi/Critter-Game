package model;

/**
 * A representation of a "vector" in the world, containing its distance from some orign,
 * the contents on that coordinate, its direction, as well as its color in a dijkstra's graph
 */
public class Vex {
    int color = 0; // 0:White 1:Gray 2:Black
    int dist = -2; // -2 is infinity, -1 is the distance to root, everything else increments normally
    int direction = -1; //Direction that the "vector" is facing once it reaches into another grid
    Hex hex;

    public Vex(int c, int d, Hex h, int dir){
        color = c;
        dist = d;
        hex = h;
        direction = dir;
    }

    public Vex(Hex h){
        hex = h;
    }

    public Vex(int d){
        dist = d;
    }
}
