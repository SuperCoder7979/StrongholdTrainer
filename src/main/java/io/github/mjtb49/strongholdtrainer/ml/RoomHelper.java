package io.github.mjtb49.strongholdtrainer.ml;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomHelper {

    static final Map<Direction, int[]> DIR_TO_VECTOR = new HashMap<Direction, int[]>() {{
        put(Direction.EAST,  new int[] {1,0,0,0});
        put(Direction.WEST,  new int[] {0,1,0,0});
        put(Direction.NORTH, new int[] {0,0,1,0});
        put(Direction.SOUTH, new int[] {0,0,0,1});
    }};


    static final Map<String, int[]> ROOM_TO_VECTOR = new HashMap<String, int[]>() {{
        put("Corridor"       ,  new int[] {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put("PrisonHall"     ,  new int[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put("LeftTurn"       ,  new int[] {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put("RightTurn"      ,  new int[] {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put("SquareRoom"     ,  new int[] {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        put("Stairs"         ,  new int[] {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0});
        put("SpiralStaircase",  new int[] {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0});
        put("FiveWayCrossing",  new int[] {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0});
        put("ChestCorridor"  ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0});
        put("Library"        ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0});
        put("PortalRoom"     ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0});
        put("SmallCorridor"  ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0});
        put("Start"          ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0});
        put("None"           ,  new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
    }};


    //for _ in range(5 - len(children)):
    //        children.append('None')
    //
    //    lists = [depth]
    //    lists += room_to_vector[last] + room_to_vector[current]
    //    for child in children:
    //        lists += room_to_vector[child]
    //    lists += dir_to_vector[direction]
    //    data.write(str(lists) + '\n')
    //    labels.write(str([label]) + '\n')
    protected static INDArray getMLInputFromRoom(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {
        //TODO looks like start is null here sometimes
        Map<StructurePiece, StructurePiece> parents = ((StrongholdTreeAccessor) start).getParents();

        List<StructurePiece> children = ((StrongholdTreeAccessor) start).getTree().get(piece);
        StructurePiece parent = ((StrongholdTreeAccessor) start).getParents().get(piece);
        Direction direction = piece.getFacing();

        int depth = 0;
        StructurePiece currentPiece = piece;
        while (parents.get(currentPiece) != null) {
            depth++;
            currentPiece = parents.get(currentPiece);
        }

        int[][] data = new int[][] {{depth}};
        data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(parent));
        data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(piece));
        for (int i = 0; i < 5; i++) {
            if (i < children.size())
                data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(children.get(i)));
            else
                data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(null));
        }
        data[0] = ArrayUtils.addAll(data[0], DIR_TO_VECTOR.get(direction));
        return Nd4j.createFromArray(data);
    }

    //for _ in range(5 - len(children)):
    //        children.append('None')
    //
    //    lists = [depth]
    //    lists += room_to_vector[last] + room_to_vector[current]
    //    for child in children:
    //        lists += room_to_vector[child]
    //    data.write(str(lists) + '\n')
    //    labels.write(str([label]) + '\n')
    protected static INDArray getMLInputFromRoomNoDir(StrongholdGenerator.Start start, StrongholdGenerator.Piece piece) {

        Map<StructurePiece, StructurePiece> parents = ((StrongholdTreeAccessor) start).getParents();

        List<StructurePiece> children = ((StrongholdTreeAccessor) start).getTree().get(piece);
        StructurePiece parent = ((StrongholdTreeAccessor) start).getParents().get(piece);

        int depth = 0;
        StructurePiece currentPiece = piece;
        while (parents.get(currentPiece) != null) {
            depth++;
            currentPiece = parents.get(currentPiece);
        }

        int[][] data = new int[][] {{depth}};
        data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(parent));
        data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(piece));
        for (int i = 0; i < 5; i++) {
            if (i < children.size())
                data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(children.get(i)));
            else
                data[0] = ArrayUtils.addAll(data[0], getArrayFromPiece(null));
        }
        return Nd4j.createFromArray(data);
    }

    static private int[] getArrayFromPiece(StructurePiece piece) {
        if (piece == null)
            return ROOM_TO_VECTOR.get("None");
        else
            return ROOM_TO_VECTOR.get(piece.getClass().getSimpleName());
    }
}
