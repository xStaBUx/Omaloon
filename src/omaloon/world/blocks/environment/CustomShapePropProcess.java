package omaloon.world.blocks.environment;

import arc.struct.*;
import mindustry.async.*;
import mindustry.world.*;
import omaloon.world.interfaces.*;

import static mindustry.Vars.*;

public class CustomShapePropProcess implements AsyncProcess {
    //TODO interfaces
    private final IntSeq multiProps = new IntSeq();

    public void onWorldLoad(){
        multiProps.clear();
        for(Tile tile : world.tiles){
            Block block = tile.block();
            if(block instanceof MultiPropI prop){
                prop.initTile(tile);
                // TODO tile.index() does not exist
//                multiProps.add(tile.index());
            }
        }
    }

    public void update(){
        if(multiProps.size == 0) return;

        for(int i = 0; i < multiProps.size; i++){
            int index = multiProps.get(i);
            Tile tile = world.tile(index);
            Block block = tile.block();
            if(block instanceof CustomShapePropI prop){
                prop.updateTile(tile);
            }
        }
    }

    public void onRemoveBlock(Tile tile, Block block){
        if(block instanceof SubMultiPropI slave){
            slave.parent().slaveRemoved(tile);
        }
    }
}
