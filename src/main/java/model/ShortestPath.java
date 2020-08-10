package model;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A representation of the shortest path from a point to a food location.
 */
public class ShortestPath {

    /**
     * The comparator for the priority Queue of vectors.
     * Vectors are sorted by their distance/magnitude.
     */
    private Comparator<Vex> comparator = new Comparator<Vex>() {
        @Override
        public int compare(Vex o1, Vex o2) {
            if(o1.dist > o2.dist){
                return 1;
            }else if(o1.dist < o2.dist){
                return -1;
            }
            return 0;
        }
    };

    private PriorityQueue<Vex> food = new PriorityQueue<>(comparator);

    World w;
    Vex[][] vexMap;
    PriorityQueue<Vex> frontier = new PriorityQueue<>(comparator);

    /**
     * Constructor. Initializes the vector map and performs a BFS
     * @param l the location of the "orign"
     * @param d the direction of the "orign"
     * @param w the world that the critter (orign) inhabits
     */
    public ShortestPath(Location l, int d, World w){
        this.w = w;
        vexMap = new Vex[w.H.length][w.H[0].length];

        for(int i = 0; i < vexMap.length; i++){
            for(int j = 0; j < vexMap[0].length; j++){
                vexMap[i][j] = new Vex(w.H[i][j]);
            }
        }
        vexMap[l.getCol()][l.getRow()] = new Vex(1, -1, w.H[l.getCol()][l.getRow()], d);
        frontier.add(vexMap[l.getCol()][l.getRow()]);

        while(!frontier.isEmpty()){
            Vex cur = frontier.poll();
            sense(cur, cur.direction);
            cur.color = 2; //Becomes black
        }
    }

    /**
     * Looks at the current vectors immediate vicinity and processes them
     * @param cur the current vector
     * @param dir the direction of the current vector
     */
    private void sense(Vex cur, int dir){
        Location newLoc = cur.hex.getLoc().addDir(dir);
        //Check ief it exists already
        updateOrAdd(cur, newLoc, dir, 1);

        newLoc = cur.hex.getLoc().addDir(Math.floorMod(dir+1, 6));
        updateOrAdd(cur, newLoc, Math.floorMod(dir+1, 6), 2);

        newLoc = cur.hex.getLoc().addDir(Math.floorMod(dir-1, 6));
        updateOrAdd(cur, newLoc, Math.floorMod(dir-1, 6), 2);



        newLoc = cur.hex.getLoc().addDir(Math.floorMod(dir+2, 6));
        updateOrAdd(cur, newLoc, Math.floorMod(dir+2, 6), 3);

        newLoc = cur.hex.getLoc().addDir(Math.floorMod(dir-2, 6));
        updateOrAdd(cur, newLoc, Math.floorMod(dir-2, 6), 3);


        newLoc = cur.hex.getLoc().addDir(Math.floorMod(dir+3, 6));
        updateOrAdd(cur, newLoc, Math.floorMod(dir-2, 6), 4);
        //dir +-1,
        //dir +-2,
        //dir +3
    }

    /**
     * Either add a white node as a gray node, update a gray node, or do nothing if it's outside
     * @param cur the current vector
     * @param newLoc the new location adjacent to the current vector
     * @param dir the direction of the current vector
     * @param add the increment
     */
    private void updateOrAdd(Vex cur, Location newLoc, int dir, int add){
        if(cur.dist+add<=Constants.MAX_SMELL_DISTANCE && withinBounds(newLoc)
        && !vexMap[newLoc.getCol()][newLoc.getRow()].hex.hasRock() && !vexMap[newLoc.getCol()][newLoc.getRow()].hex.hasCritter()){
            if (vexMap[newLoc.getCol()][newLoc.getRow()].dist == -2 ) {
                vexMap[newLoc.getCol()][newLoc.getRow()].dist = cur.dist + add;
                vexMap[newLoc.getCol()][newLoc.getRow()].direction = dir;
                frontier.add(vexMap[newLoc.getCol()][newLoc.getRow()]);
                vexMap[newLoc.getCol()][newLoc.getRow()].color = 1; // Becomes gray
                if (w.H[newLoc.getCol()][newLoc.getRow()].hasFood()) food.add(vexMap[newLoc.getCol()][newLoc.getRow()]);
            } else {
                if (vexMap[newLoc.getCol()][newLoc.getRow()].dist > cur.dist + add) {
                    vexMap[newLoc.getCol()][newLoc.getRow()].dist = cur.dist + add;
                    vexMap[newLoc.getCol()][newLoc.getRow()].direction = dir;
                }
            }
        }
    }

    /**
     * Checks if a vector is within the bounds of the map
     * @param l the location
     * @return whether the location is within the bounds of the vector/world map
     */
    private boolean withinBounds(Location l){
        return l.getRow() >= 0 && l.getRow() < vexMap[0].length && l.getCol() >= 0
                && l.getCol() < vexMap.length;
    }

    /**
     * Recursively calls itself, looking for a smaller dist than itself until it's adjacent to the critter0
     * @param v the current vector
     * @return a relative direction in which the critter should turn in order to reach the closest available food
     */
    private int getDirection(Vex v){
        int dir = v.direction;
        Vex min= new Vex(11);
        for(int i = 0; i < 6; i++){
            Location nextLoc = v.hex.getLoc().addDir(Math.floorMod(dir + i, 6));
            // The following statement must be true at least once for every vector save the critter itself
            // Note: dist = -2 means another critter or a rock
            if(withinBounds(nextLoc)) {
                if (vexMap[nextLoc.getCol()][nextLoc.getRow()].dist >=0
                        && vexMap[nextLoc.getCol()][nextLoc.getRow()].dist < v.dist) {
                    min = (min.dist > vexMap[nextLoc.getCol()][nextLoc.getRow()].dist)?vexMap[nextLoc.getCol()][nextLoc.getRow()]:min;
                } else if (vexMap[nextLoc.getCol()][nextLoc.getRow()].dist == -1) {
                   //System.out.println(v.direction + ", " + vexMap[nextLoc.getCol()][nextLoc.getRow()].direction);
                    return Math.floorMod(v.direction-vexMap[nextLoc.getCol()][nextLoc.getRow()].direction, 6);
                }
            }

        }
        return getDirection(min);
    }

    /**
     * gets the location of the closest food
     * @return the location of the closest food
     */
    public Location getFoodLoc(){
        return (food.isEmpty())?new Location(-1, -1):food.peek().hex.getLoc();
    }

    /**
     * Computes the values to be returned.
     * @return distance to food * 1000 + relative direction to turn
     */
    public int computeValue(){
        return food.isEmpty()?1000000:(food.peek().dist)*1000 + getDirection(food.peek());
    }



}
