package ol.world.blocks.pressure;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Nullable;
import mindustry.core.GameState;
import mindustry.entities.TargetPriority;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import ol.content.OlFx;
import ol.content.blocks.OlDistribution;
import ol.world.blocks.RegionAble;

import static mindustry.Vars.state;
import static mindustry.Vars.world;

public class PressurePipe extends PressureBlock implements PressureReplaceable, RegionAble {
    public final ObjectMap<String, TextureRegion> cache = new ObjectMap<>();
    public @Nullable Block junctionReplacement = OlDistribution.pressureJunction;

    /**draw connections?*/
    public boolean mapDraw = true;

    int timer = timers++;

    public void drawT(int x, int y, int rotation) {
        TextureRegion pressureIcon = Core.atlas.find("ol-pressure-icon");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        if(rotation == 1 || rotation == 3) {
            Draw.rect(pressureIcon, dx, dy + ds, -90);
            Draw.rect(pressureIcon, dx, dy - ds, 90);
        }

        if(rotation == 0 || rotation == 2) {
            Draw.rect(pressureIcon, dx + ds, dy, 180);
            Draw.rect(pressureIcon, dx - ds, dy, 0);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawT(x, y, rotation);
    }

    public PressurePipe(String name) {
        super(name);

        conveyorPlacement = underBullets = rotate = update = solid = true;
        drawArrow = false;
        group = BlockGroup.power;
        priority = TargetPriority.transport;
    }

    @Override
    public boolean canReplace(Block other) {
        boolean valid = true;
        if(other instanceof PressurePipe pipe) {
            valid = pipe.tier == tier || pipe.tier == -1 || tier == -1;
        }

        return canBeReplaced(other) && valid;
    }

    @Override
    public String name() {
        return name;
    }

    public class PressurePipeBuild extends PressureBlockBuild {
        @Override
        public void updateTile() {
            super.updateTile();

            if(pressure > maxPressure && canExplode) {
                float random = Mathf.random(-3, 3);

                if(timer(PressurePipe.this.timer, Mathf.random(35, 65))) {
                    OlFx.pressureDamage.at(x + random/2, y + random/2, this.totalProgress() * random, Layer.blockUnder);
                }
            }
        }

        public boolean avalible(Building b) {
            return b instanceof PressureAble && inNet(b, false);
        }

        @Override
        public PressurePipeBuild self() {
            return this;
        }

        /**pipes name based on connections <name>-[L][R][T][B]

        example: if connected to all sides when loaded 1111
                if connected only right when loaded 0100
                ...
                if connected only right and left when loaded 1100...*/

        @Override
        public void draw() {
            if(mapDraw) {
                int tx = tileX();
                int ty = tileY();

                Building left   = world.build(tx - 1, ty);
                Building right  = world.build(tx + 1, ty);
                Building bottom = world.build(tx, ty - 1);
                Building top    = world.build(tx, ty + 1);

                boolean bLeft   = avalible(left)   || left    instanceof PressureJunction.PressureJunctionBuild;
                boolean bRight  = avalible(right)  || right   instanceof PressureJunction.PressureJunctionBuild;
                boolean bTop    = avalible(top)    || top     instanceof PressureJunction.PressureJunctionBuild;
                boolean bBottom = avalible(bottom) || bottom  instanceof PressureJunction.PressureJunctionBuild;

                int l = bLeft   ? 1 : 0;
                int r = bRight  ? 1 : 0;
                int t = bTop    ? 1 : 0;
                int b = bBottom ? 1 : 0;

                String sprite = "-" + l + "" + r + "" + t + "" + b;

                if(cache.get(sprite) == null) {
                    cache.put(sprite, loadRegion(sprite));
                }

                if(pressure > maxPressure && canExplode) {
                    if(state.is(GameState.State.paused)) {
                        Draw.rect(cache.get(sprite), this.x, this.y);
                    } else {
                        Draw.rect(cache.get(sprite), this.x, this.y, Mathf.random(-3, 3));
                    }
                } else {
                    Draw.rect(cache.get(sprite), this.x, this.y);
                }
            } else {
                super.draw();
            }

            this.drawTeamTop();
        }
    }
}