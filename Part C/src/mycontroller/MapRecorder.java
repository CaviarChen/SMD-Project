package mycontroller;

import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.World;

import java.util.*;

/**
 * Class that responsible for recording the map for MyAIController
 */
public class MapRecorder {

    // status enum for tile
    public enum TileStatus {
        UNREACHABLE, SEARCHED, UNSEARCHED
    }

    public static final int LAVA_FOUND     = 0b001;
    public static final int NEXT_KEY_FOUND = 0b010;

    public static final int RADAR_RADIUS = 4;


    public TileStatus[][] mapStatus;
    public MapTile[][] mapTiles;

    // Coordinate for special tile
    public Coordinate[] keysCoord;
    public HashSet<Coordinate> finishCoords = new HashSet<>();
    public HashSet<Coordinate> healthCoords = new HashSet<>();

    public int width = World.MAP_WIDTH, height = World.MAP_HEIGHT;


    /**
     * Constructor
     *
     * @param mapHashMap map get from CarController.getMap()
     * @param keySize    the number of key in this map
     */
    public MapRecorder(HashMap<Coordinate, MapTile> mapHashMap, int keySize) {

        mapStatus = new TileStatus[width][height];
        mapTiles = new MapTile[width][height];
        keysCoord = new Coordinate[keySize - 1];

        Coordinate startCoord = null;

        // add all tiles into mapTiles
        for (Map.Entry<Coordinate, MapTile> entry : mapHashMap.entrySet()) {
            int x = entry.getKey().x;
            int y = entry.getKey().y;
            mapTiles[x][y] = entry.getValue();
            if (mapTiles[x][y].getType() == MapTile.Type.START) {
                startCoord = entry.getKey();
            } else if (mapTiles[x][y].getType() == MapTile.Type.FINISH) {
                finishCoords.add(entry.getKey());
            }
        }

        // use DFS to find out all reachable tiles in this map
        findReachableDFS(startCoord.x, startCoord.y);

        // mark everything unreachable if not reached by DFS
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mapStatus[i][j] == null) {
                    mapStatus[i][j] = TileStatus.UNREACHABLE;
                }
            }
        }

        // mark finish tiles as searched
        for (Coordinate coord : finishCoords) {
            mapStatus[coord.x][coord.y] = TileStatus.SEARCHED;
        }

    }

    /**
     * Add a view of the car to the map.
     *
     * @param view       from CarController.getView()
     * @param currentKey current number of key
     * @return a status that indicates special tiles found (0 or a sum of flags responding to type of tiles found).
     * Including LAVA_FOUND and NEXT_KEY_FOUND
     */
    public int addCarView(HashMap<Coordinate, MapTile> view, int currentKey) {

        int tilesFoundFlag = 0;

        for (Map.Entry<Coordinate, MapTile> entry : view.entrySet()) {
            int tileX = entry.getKey().x;
            int tileY = entry.getKey().y;

            if (inRange(tileX, tileY)) {
                if (mapStatus[tileX][tileY] == TileStatus.UNSEARCHED) {
                    // mark as searched
                    mapStatus[tileX][tileY] = TileStatus.SEARCHED;
                    mapTiles[tileX][tileY] = entry.getValue();


                    if (mapTiles[tileX][tileY] instanceof LavaTrap) {
                        tilesFoundFlag |= LAVA_FOUND;
                        LavaTrap trap = (LavaTrap) mapTiles[tileX][tileY];
                        if (trap.getKey() > 0) {
                            if (trap.getKey() == currentKey - 1)
                                tilesFoundFlag |= NEXT_KEY_FOUND;
                            if (Arrays.asList(keysCoord).get(trap.getKey() - 1) == null) {
                                keysCoord[trap.getKey() - 1] = new Coordinate(tileX, tileY);
                            }
                        }
                    } else if (mapTiles[tileX][tileY] instanceof HealthTrap) {
                        healthCoords.add(new Coordinate(tileX, tileY));
                    }
                }
            }
        }

        return tilesFoundFlag;
    }

    /**
     * Use DFS to find out all reachable position
     *
     * @param x starting position
     * @param y starting position
     */
    private void findReachableDFS(int x, int y) {
        if (!inRange(x, y)) {
            return;
        }

        if (mapStatus[x][y] != null) {
            return;
        }

        if (mapTiles[x][y].getType() == MapTile.Type.WALL) {
            mapStatus[x][y] = TileStatus.UNREACHABLE;
            return;
        }

        mapStatus[x][y] = TileStatus.UNSEARCHED;

        findReachableDFS(x + 1, y);
        findReachableDFS(x - 1, y);
        findReachableDFS(x, y + 1);
        findReachableDFS(x, y - 1);
    }

    /**
     * @param x a position
     * @param y a position
     * @return true if the given position is in the map range
     */
    public boolean inRange(int x, int y) {
        return !(x < 0 || x >= width || y < 0 || y >= height);
    }

    /**
     * Calculate a list of coordinates to that the car can explore
     * to discover all cells on the map
     *
     * @return a list of coordinates that can uncover all cells when visited
     * Coordinates are returned in no order.
     */
    public HashSet<Coordinate> coordinatesToExplore() {
        // HashSet to record all coordinates that is yet to discover
        HashSet<Coordinate> coordinatesPending = new HashSet<>();

        // List of coordinates to visit
        HashSet<Coordinate> queue = new HashSet<>();

        // Add all unsearched cells
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (mapStatus[i][j] == TileStatus.UNSEARCHED) {
                    coordinatesPending.add(new Coordinate(i, j));
                }
            }
        }

        while (!coordinatesPending.isEmpty()) {
            // Randomly pick a cell from those to be visited
            Coordinate i = coordinatesPending.iterator().next();

            // Record the furthest
            Coordinate nonLavaCoordinate = null,
                    lavaCoordinate = null,
                    exploreCoordinate = null;

            // Check diagonally from 3 cells bottom right to the candidate cell
            // to the candidate cell itself
            for (int j = RADAR_RADIUS; j >= 0; j--) {
                // check if the coordinate is valid and reachable
                if (inRange(i.x + j, i.y + j) &&
                        mapStatus[i.x + j][i.y + j] != TileStatus.UNREACHABLE) {
                    Coordinate c = new Coordinate(i.x + j, i.y + j);

                    // Record the first lava cell and non-lava cell seen.
                    if (mapTiles[i.x + j][i.y + j] instanceof LavaTrap &&
                            lavaCoordinate == null)
                        lavaCoordinate = c;
                    else {
                        nonLavaCoordinate = c;
                        // Break the loop if non-lava cell is found as
                        // non-lava cell is always preferred.
                        break;
                    }
                }
            }

            // If such a cell is available, we prefer the
            // non-lava cell first, then the lava cell if non-lava cell
            // is not found
            exploreCoordinate = nonLavaCoordinate != null ? nonLavaCoordinate : lavaCoordinate;
            if (exploreCoordinate == null) continue;

            // add the coordinate to the set of coordinates to explore
            queue.add(exploreCoordinate);

            // Remove all candidate cells that can be reached
            // from the radar at that cell.
            for (int x = exploreCoordinate.x - RADAR_RADIUS;
                 x <= exploreCoordinate.x + RADAR_RADIUS; x++)
                for (int y = exploreCoordinate.y - RADAR_RADIUS;
                     y <= exploreCoordinate.y + RADAR_RADIUS; y++) {
                    coordinatesPending.remove(new Coordinate(x, y));
                }
        }

        return queue;
    }

}
