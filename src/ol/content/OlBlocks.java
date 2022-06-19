package ol.content;

import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.*;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import ol.graphics.OlPal;
import ol.graphics.OlShaders;
import ol.type.bullets.OlBulletType;
import ol.world.blocks.crafting.OlCrafter;
import ol.world.blocks.crafting.OlMultiCrafter;
import ol.world.blocks.crafting.recipe.Recipe;
import ol.world.blocks.defense.JoinWall;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.production.GenericCrafter;
//import ol.world.blocks.production.olBurstDrill;
import ol.world.blocks.defense.OlWall;
import ol.world.blocks.power.OlPanel;
import ol.world.draw.DrawCentryfuge;

import static mindustry.type.ItemStack.with;

public class OlBlocks{

	public static Block
	//Environment
	gravel, gravelDalanite, dalanite, deepDalanite,
	//Ores
	oreOmalite,
	//Defence
	omaliteAlloyWall, omaliteAlloyWallLarge, testJoinWall,
	//Distribution
	//Drills
	//Power
	hyperReceiver,
	//Production
	multiFactory, fuser, lowTemperatureSmelter,
	//Turrets
	blueNight, zone;
	//Storages
	//Units

	public static void load() {
		//region Environment
		gravel = new Floor("gravel"){{
			itemDrop = Items.sand;
			playerUnmineable = true;
			variants = 3;
		}};
		gravelDalanite = new Floor("gravel-dalanite"){{
			itemDrop = Items.sand;
			playerUnmineable = true;
			speedMultiplier = 0.8f;
			variants = 3;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanite;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		dalanite = new Floor("flor-dalanite"){{
			speedMultiplier = 0.5f;
			variants = 0;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanite;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		deepDalanite = new Floor("deep-dalanite"){{
			speedMultiplier = 0.3f;
			variants = 0;
			status = OlStatusEffects.slime;
			statusDuration = 6f;
			supportsOverlay = true;
			drownTime = 210f;
			albedo = 0.9f;
			isLiquid = true;
			liquidDrop = OlLiquids.dalanite;
			liquidMultiplier = 1.5f;
			cacheLayer = OlShaders.dalaniteLayer;
		}};
		//endregion
		//region Defence
		omaliteAlloyWall = new OlWall("omalite-alloy-wall") {{
			requirements(Category.defense, ItemStack.with(OlItems.omaliteAlloy, 5, Items.titanium, 2));
			size = 1;
			statusDuration = 140f;
			health = 1420;
			insulated = true;
			status = StatusEffects.freezing;
			flashColor = OlPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.003f;
		}};
		omaliteAlloyWallLarge = new OlWall("omalite-alloy-wall-large") {{
			requirements(Category.defense, ItemStack.with(OlItems.omaliteAlloy, 24, Items.titanium, 10));
			size = 2;
			statusDuration = 140f;
			health = 1840 * size * size;
			insulated = true;
			status = StatusEffects.freezing;
			flashColor = OlPal.OLDarkBlue;
			dynamicEffect = Fx.freezing;
			dynamicEffectChance = 0.004f;
		}};

		testJoinWall = new JoinWall("test-joint"){{
			buildVisibility = BuildVisibility.sandboxOnly;
			category = Category.defense;
			health = 999999999;
			size = 1;
		}};
		//endregion
		//region Production
		multiFactory = new OlMultiCrafter("multi-factory"){{
			requirements(Category.crafting, ItemStack.with(OlItems.grumon, 12, Items.titanium, 11, Items.silicon, 5));
			size = 2;
			consumesPower = true;
			consPower = consumePower(4);
			craftEffect = Fx.none;
			itemCapacity = 20;
			liquidCapacity = 20;
			recipes(
			Recipe.with()
					.produceTime(1.4f * Time.toSeconds)
					.consume(ItemStack.with(OlItems.grumon, 2, Items.silicon, 1), null)
					.output(new ItemStack(OlItems.magneticCombination, 1), null),
			Recipe.with()
					.produceTime(1.8f * Time.toSeconds)
					.consume(ItemStack.with(OlItems.omalite, 1, OlItems.grumon, 1), LiquidStack.with(OlLiquids.dalanite, 12 / 60f))
					.output(new ItemStack(OlItems.zarini, 2), new LiquidStack(Liquids.water, 11 / 60f)),
			Recipe.with()
					.produceTime(2.3f * Time.toSeconds)
					.consume(ItemStack.with(OlItems.omalite, 2, Items.tungsten, 1), null)
					.output(new ItemStack(OlItems.valkron, 1), null)
			);
			health = 300;
		}};
		fuser = new GenericCrafter("fuser") {{
			requirements(Category.crafting, with(Items.surgeAlloy, 20, OlItems.omalite, 50, Items.titanium, 80, Items.thorium, 65));
			craftTime = 185f;
			size = 3;
			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawLiquidTile(OlLiquids.liquidOmalite){{
						drawLiquidLight = true;
					}},
					new DrawRegion("-rotator"){{
						spinSprite = true;
						rotateSpeed = 1f;
					}},
					new DrawDefault(),
					new DrawRegion("-top"));
			itemCapacity = 35;
			liquidCapacity = 45;
			hasPower = hasLiquids = hasItems = true;
			consumeLiquid(Liquids.water, 22f / 60f);
			consumeItems(new ItemStack(OlItems.omalite, 2));
			outputLiquid = new LiquidStack(OlLiquids.liquidOmalite,  22f / 60f);
			consumePower(2.4f);
		}};

		lowTemperatureSmelter = new OlCrafter("low-temperature-smelter") {{
			size = 4;
			health = 540;
			requirements(Category.crafting, ItemStack.with(OlItems.omalite, 80, Items.thorium, 80, Items.titanium, 100));
			craftTime = 270f;
			craftEffect = Fx.shieldBreak;
			updateEffectChance = 0.08f;
			ambientSound = Sounds.pulse;
			ambientSoundVolume = 0.1f;
			accelerationSpeed = 0.0003f;
			decelerationSpeed = 0.006125f;
			powerProduction = 22f;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawCentryfuge() {{
				    plasma1 = Items.titanium.color;
				    plasma2 = OlPal.OLDarkBlue;
			    }}
			);
			onCraft = tile -> {
				Tmp.v1.setToRandomDirection().setLength(27f / 3.4f);
				Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
				Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
			};
			consumePower(7);
			consumeItems(with(Items.titanium, 4, OlItems.omalite, 2));
			consumeLiquid(OlLiquids.liquidOmalite, 0.18f);
			outputItems = with(OlItems.omaliteAlloy, 5);
			itemCapacity = 30;
		}};

		//endregion
		//region Turrets
		blueNight = new PowerTurret("blue-night") {{
			requirements(Category.turret, with(Items.copper, 20, Items.lead, 50, Items.graphite, 20, OlItems.omaliteAlloy, 25, Items.silicon, 15));
			size = 3;
			range = 275f;
			recoil = 1f;
			health = 1980;
			inaccuracy = 1f;
			rotateSpeed = 3f;
			shootCone = 0.1f;
			shootSound = OlSounds.olShot;
			ammoUseEffect = Fx.none;
			heatColor = OlPal.OLDarkBlue;
			targetAir = false;
			shootEffect = OlFx.blueShot;
			shootY = 10;
			drawer = new DrawTurret("intensified-");
			shootType = new OlBulletType(9f, 240f) {{
				shrinkX = 0;
				sprite = "ol-sphere";
				shrinkY = 0;
				lifetime = 29f;
				status = StatusEffects.freezing;
				statusDuration = 120f;
				despawnEffect = hitEffect = new ExplosionEffect() {{
					waveColor = smokeColor = sparkColor = OlPal.OLBlue;
					waveStroke = 4f;
					waveRad = 16f;
					waveLife = 15f;
					sparks = 5;
					sparkRad = 16f;
					sparkLen = 5f;
					sparkStroke = 4f;
				}};
				frontColor = OlPal.OLBlue;
				backColor = OlPal.OLBlue;
				width = height = 13f;
				collidesTiles = true;
				trailColor = OlPal.OLBlue;
				trailWidth = 5f;
				trailLength = 9;
				trailEffect = Fx.railTrail;
				chargeEffect = OlFx.blueSphere;
				splashDamage = 90f;
				splashDamageRadius = 24f;
				homingPower = 0.4f;
				homingRange = 275f;
				drag = 0.008f;
			}};
			shoot.firstShotDelay = 55f;
			moveWhileCharging = false;
			chargeSound = OlSounds.olCharge;
			reload = 120f;
			liquidCapacity = 40;
			consumePower(2f);
			consumeLiquid(OlLiquids.liquidOmalite, 44.2f / 60f);
			smokeEffect = Fx.none;
			squareSprite = false;
		}};

		/*zone = new ItemTurret("zone"){{
			requirements(Category.turret, with(Items.copper, 25, Items.lead, 40, Items.graphite, 22, OlItems.omaliteAlloy, 21, Items.silicon, 10));
			size = 3;
			health = 1880;
			rotateSpeed = 8f;
			range = 225f;
			targetAir = true;
			recoil = 3f;
			inaccuracy = 2f;
			shootCone = 1f;
			shootSound = olSounds.zoneShot;
			reload = 120f;
			itemCapacity = 32;
			squareSprite = false;
			drawer = new DrawTurret("intensified-");
			consumeCoolant(42f / 60f);
			ammo(
					OlItems.omaliteAlloy
			);
		}};*/
		//endregion
		//region Ores
		oreOmalite = new OreBlock("omalite-ore"){{
			oreDefault = true;
			variants = 3;
			oreThreshold = 0.95F;
			oreScale = 20.380953F;
			itemDrop = OlItems.omalite;
			localizedName = itemDrop.localizedName;
			mapColor.set(itemDrop.color);
			useColor = true;
		}};
		//endregion
		//region Power
		hyperReceiver = new OlPanel("hyper-receiver"){{
			requirements(Category.power, with(Items.titanium, 200, Items.surgeAlloy, 110, OlItems.omaliteAlloy, 40));
			size = 4;
			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawLiquidTile(),
				new DrawDefault(),
			    new DrawRegion("-top")
			);
			hasLiquids = true;
			ambientSound = Sounds.none;
			powerProduction = 3.2f;
			liquidCapacity = 56;
			consumeLiquid(OlLiquids.liquidOmalite, 12f / 920f);
		}};
		/*powerTower = new olPowerNode("power-tower"){{
        }};*/

//endregion
//region Drills
			/*explosiveDrill = new olBurstDrill("explosive-drill"){{
				requirements(Category.production, with(Items.copper, 85, Items.silicon, 80, Items.titanium, 70, Items.thorium, 95));
				drillTime = 60f * 12f;
				size = 4;
				hasPower = true;
				tier = 6;
				drillEffect = Fx.explosion;
				shake = 4f;
				itemCapacity = 40;
				//can't mine thorium for balance reasons, needs better drill
				researchCostMultiplier = 0.5f;
				consumes.power(160f / 60f);
				consumes.items(new ItemStack(Items.blastCompound, 1));
				consumes.liquid(Liquids.water, 0.2f);
			}};*/
//endregion
   }
}