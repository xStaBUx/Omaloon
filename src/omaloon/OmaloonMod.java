package omaloon;

import arc.*;
import arc.scene.actions.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.TechTree.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.ui.*;
import omaloon.ui.dialogs.*;
import omaloon.ui.fragments.*;
import omaloon.utils.*;
import omaloon.world.blocks.environment.*;

import static arc.Core.app;

public class OmaloonMod extends Mod{
    public static EditorListener editorListener;
    public static SafeClearer safeClearer;

    public static ShapedEnvPlacerFragment shapedEnvPlacerFragment;
    public static CliffFragment cliffFragment;

    public static OlInputDialog olInputDialog;
    public static OlGameDataDialog olGameDataDialog;
    public static OlGameDialog olGameDialog;
    public static OlEndDialog olEndDialog;

    /**
     * Buffer radius increase to take splashRadius into account, increase if necessary.
     */
    public static float shieldBuffer = 40f;

    public OmaloonMod(){
        super();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            StartSplash.build(Vars.ui.menuGroup);
            StartSplash.show();
            if(!Vars.mobile){
                shapedEnvPlacerFragment.build(Vars.ui.hudGroup);
                cliffFragment.build(Vars.ui.hudGroup);
                OlBinding.load();
            }
            Vars.maps.all().removeAll(map -> {
                if(map.mod == null || !map.mod.name.equals("omaloon")){
                    return false;
                }
                Mods.LoadedMod otherMod = Vars.mods.getMod("test-utils");
                return otherMod == null || !otherMod.enabled();
            });


        });

        if(!Vars.headless){
            editorListener = new EditorListener();
        }
        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            OlIcons.load();
            OlSettings.load();
            EventHints.addHints();
            CustomShapePropProcess.instance = new CustomShapePropProcess();
            Vars.asyncCore.processes.add(CustomShapePropProcess.instance);
            safeClearer = new SafeClearer();
        });

        Events.on(EventType.FileTreeInitEvent.class, e ->
            app.post(OlShaders::load)
        );

        Events.on(EventType.MusicRegisterEvent.class, e ->
            OlMusics.load()
        );

        Events.on(EventType.DisposeEvent.class, e ->
            OlShaders.dispose()
        );

        Log.info("Loaded OmaloonMod constructor.");
    }

    @Override
    public void init(){
        super.init();
        IconLoader.loadIcons();
        if(Vars.headless) return;
        shapedEnvPlacerFragment = new ShapedEnvPlacerFragment();
        cliffFragment = new CliffFragment();
        olInputDialog = new OlInputDialog();
        olGameDataDialog = new OlGameDataDialog();
        olGameDialog = new OlGameDialog();
        olEndDialog = new OlEndDialog();
        Events.on(EventType.SectorCaptureEvent.class, e -> {
            if(e.sector.preset == OlSectorPresets.deadValley) olEndDialog.show(Core.scene, Actions.sequence(
                Actions.fadeOut(0),
                Actions.fadeIn(1)
            ));
        });
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        OlSounds.load();
        OlItems.load();
        OlLiquids.load();
        OlStatusEffects.load();
        OlUnitTypes.load();
        OlBlocks.load();
        OlWeathers.load();
        OlPlanets.load();
        OlSectorPresets.load();
        OlSchematics.load();
        OlTechTree.load();
    }

    public static void resetSaves(Planet planet){
        planet.sectors.each(sector -> {
            if(!sector.hasSave()) return;
            sector.save.delete();
            sector.save = null;
        });
    }

    public static void resetTree(TechNode root){
        root.reset();
        root.content.clearUnlock();
        root.children.each(OmaloonMod::resetTree);
    }
}
