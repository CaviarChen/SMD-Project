package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;

import java.util.ArrayList;
import java.util.Objects;

public class SimplifyPath implements Pipeline.Step<ArrayList<Position>, MapRecorder>{

    @Override
    public ArrayList<Position> execute(ArrayList<Position> input, MapRecorder mapRecorder) {

        for (int i=0; i<input.size()-2; i++) {
            if (input.get(i)==null) continue;

            for (int j=i+2; j<input.size(); j++) {
                Position pos1 = input.get(i);
                Position pos2 = input.get(j-1);
                Position pos3 = input.get(j);

                if (pos2==null || pos3==null) break;

                int minX = Math.round(Math.min(pos1.x, pos3.x));
                int maxX = Math.round(Math.max(pos1.x, pos3.x));
                int minY = Math.round(Math.min(pos1.y, pos3.y));
                int maxY = Math.round(Math.max(pos1.y, pos3.y));

                // skip if pos2 is not in the rectangle of pos1 and pos3
                if (!(pos2.x <= maxX && pos2.x>=minX && pos2.y <= maxY && pos2.y>=minY)) continue;


                boolean flag = true;

                // if pos1 or pos3 is LavaTrap, then allow to go through LavaTrap
                boolean noLavaTrap = true;
                if (mapRecorder.mapTiles[Math.round(pos1.x)][Math.round(pos1.y)] instanceof LavaTrap ||
                        mapRecorder.mapTiles[Math.round(pos3.x)][Math.round(pos3.y)] instanceof LavaTrap)
                    noLavaTrap = false;

                for (int x=minX; x<=maxX; x++) {
                    for (int y=minY; y<=maxY; y++) {
                        MapTile current = mapRecorder.mapTiles[x][y];

                        // break the loop if there is Wall or LavaTrap in the rectangle
                        if (current.isType(MapTile.Type.WALL) || (current instanceof LavaTrap && noLavaTrap)) {
                            flag = false;
                            break;
                        }

                    }
                    if (!flag) break;
                }

                if (flag) {
                    // clean, remove pos2
                    input.set(j-1, null);
                }


            }
        }

        // clean up
        input.removeIf(Objects::isNull);

        return input;
    }
}
