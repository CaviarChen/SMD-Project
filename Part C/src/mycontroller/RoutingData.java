package mycontroller;

import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RoutingData {
    public HashSet<Coordinate> targets = new HashSet<>();
    public ArrayList<Position> path = new ArrayList<>();
    public HashMap<Coordinate, Coordinate> targetPairs = new HashMap<>();
}

