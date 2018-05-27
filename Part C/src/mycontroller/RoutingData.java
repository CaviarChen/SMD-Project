/**
 * Group 44
 */

package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for storing all routing data
 */
public class RoutingData {
    // Coordinates of all targets
    public HashSet<Coordinate> targets = new HashSet<>();
    // current path to the target with the lowest cost
    public ArrayList<Position> path = new ArrayList<>();
    // record all pair of rush points (a special kind of target)
    public HashMap<Coordinate, Coordinate> targetPairs = new HashMap<>();
}

