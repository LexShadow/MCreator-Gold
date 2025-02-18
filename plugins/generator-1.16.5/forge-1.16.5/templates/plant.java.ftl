<#--
 # MCreator (https://mcreator.net/)
 # Copyright (C) 2020 Pylo and contributors
 # 
 # This program is free software: you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 # 
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 # 
 # You should have received a copy of the GNU General Public License
 # along with this program.  If not, see <https://www.gnu.org/licenses/>.
 # 
 # Additional permission for code generator templates (*.ftl files)
 # 
 # As a special exception, you may create a larger work that contains part or 
 # all of the MCreator code generator templates (*.ftl files) and distribute 
 # that work under terms of your choice, so long as that work isn't itself a 
 # template for code generation. Alternatively, if you modify or redistribute 
 # the template itself, you may (at your option) remove this special exception, 
 # which will cause the template and the resulting code generator output files 
 # to be licensed under the GNU General Public License without this special 
 # exception.
-->

<#-- @formatter:off -->
<#include "boundingboxes.java.ftl">
<#include "procedures.java.ftl">
<#include "mcitems.ftl">

package ${package}.block;

import net.minecraft.block.material.Material;
import net.minecraft.util.SoundEvent;

@${JavaModName}Elements.ModElement.Tag public class ${name}Block extends ${JavaModName}Elements.ModElement {

	@ObjectHolder("${modid}:${registryname}")
	public static final Block block = null;

	<#if data.hasTileEntity>
	@ObjectHolder("${modid}:${registryname}")
	public static final TileEntityType<CustomTileEntity> tileEntityType = null;
	</#if>

	public ${name}Block(${JavaModName}Elements instance) {
		super(instance, ${data.getModElement().getSortID()});

		<#if data.hasTileEntity>
		FMLJavaModLoadingContext.get().getModEventBus().register(new TileEntityRegisterHandler());
		</#if>
		<#if data.tintType != "No tint">
			FMLJavaModLoadingContext.get().getModEventBus().register(new BlockColorRegisterHandler());
			<#if data.isItemTinted>
			FMLJavaModLoadingContext.get().getModEventBus().register(new ItemColorRegisterHandler());
			</#if>
		</#if>

		<#if (data.spawnWorldTypes?size > 0)>
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().register(new FeatureRegisterHandler());
		</#if>
	}

	@Override public void initElements() {
		elements.blocks.add(() -> new BlockCustomFlower());
		elements.items.add(() -> new <#if data.plantType == "double">Tall</#if>BlockItem(block, new Item.Properties().group(${data.creativeTab})).setRegistryName(block.getRegistryName()));
	}

	<#if data.hasTileEntity>
	private static class TileEntityRegisterHandler {

		@SubscribeEvent public void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> event) {
			event.getRegistry().register(TileEntityType.Builder.create(CustomTileEntity::new, block).build(null).setRegistryName("${registryname}"));
		}

	}
	</#if>

	@Override @OnlyIn(Dist.CLIENT) public void clientLoad(FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(block, RenderType.getCutout());
	}

	<#if data.tintType != "No tint">
	private static class BlockColorRegisterHandler {
		@OnlyIn(Dist.CLIENT) @SubscribeEvent public void blockColorLoad(ColorHandlerEvent.Block event) {
			event.getBlockColors().register((bs, world, pos, index) -> {
				return world != null && pos != null ?
				<#if data.tintType == "Grass">
					BiomeColors.getGrassColor(world, pos) : GrassColors.get(0.5D, 1.0D);
				<#elseif data.tintType == "Foliage">
					BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefault();
				<#elseif data.tintType == "Water">
					BiomeColors.getWaterColor(world, pos) : -1;
				<#elseif data.tintType == "Sky">
					Minecraft.getInstance().world.getBiome(pos).getSkyColor() : 8562943;
				<#elseif data.tintType == "Fog">
					Minecraft.getInstance().world.getBiome(pos).getFogColor() : 12638463;
				<#else>
					Minecraft.getInstance().world.getBiome(pos).getWaterFogColor() : 329011;
				</#if>
			}, block);
		}
	}

		<#if data.isItemTinted>
		private static class ItemColorRegisterHandler {
			@OnlyIn(Dist.CLIENT) @SubscribeEvent public void itemColorLoad(ColorHandlerEvent.Item event) {
				event.getItemColors().register((stack, index) -> {
					<#if data.tintType == "Grass">
						return GrassColors.get(0.5D, 1.0D);
					<#elseif data.tintType == "Foliage">
						return FoliageColors.getDefault();
					<#elseif data.tintType == "Water">
						return 3694022;
					<#elseif data.tintType == "Sky">
						return 8562943;
					<#elseif data.tintType == "Fog">
						return 12638463;
					<#else>
						return 329011;
					</#if>
				}, block);
			}
		}
		</#if>
	</#if>

	<#if (data.spawnWorldTypes?size > 0)>
	private static Feature<BlockClusterFeatureConfig> feature = null;
	private static ConfiguredFeature<?, ?> configuredFeature = null;

	private static class FeatureRegisterHandler {

		@SubscribeEvent public void registerFeature(RegistryEvent.Register<Feature<?>> event) {
			<#if data.plantType == "normal">
				<#if data.staticPlantGenerationType == "Flower">
				feature = new DefaultFlowersFeature(BlockClusterFeatureConfig.field_236587_a_) {
					@Override public BlockState getFlowerToPlace(Random random, BlockPos bp, BlockClusterFeatureConfig fc) {
						return block.getDefaultState();
					}
				<#else>
				feature = new RandomPatchFeature(BlockClusterFeatureConfig.field_236587_a_) {
				</#if>

					@Override public boolean generate(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, BlockClusterFeatureConfig config) {
						RegistryKey<World> dimensionType = world.getWorld().getDimensionKey();
						boolean dimensionCriteria = false;

						<#list data.spawnWorldTypes as worldType>
							<#if worldType=="Surface">
								if(dimensionType == World.OVERWORLD)
									dimensionCriteria = true;
							<#elseif worldType=="Nether">
								if(dimensionType == World.THE_NETHER)
									dimensionCriteria = true;
							<#elseif worldType=="End">
								if(dimensionType == World.THE_END)
									dimensionCriteria = true;
							<#else>
								if(dimensionType == RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
										new ResourceLocation("${generator.getResourceLocationForModElement(worldType.toString().replace("CUSTOM:", ""))}")))
									dimensionCriteria = true;
							</#if>
						</#list>

						if(!dimensionCriteria)
							return false;

						<#if hasProcedure(data.generateCondition)>
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						if (!<@procedureOBJToConditionCode data.generateCondition/>)
							return false;
						</#if>

						return super.generate(world, generator, random, pos, config);
					}
				};
			<#elseif data.plantType == "growapable">
				feature = new Feature<BlockClusterFeatureConfig>(BlockClusterFeatureConfig.field_236587_a_) {
					@Override public boolean generate(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, BlockClusterFeatureConfig config) {
						RegistryKey<World> dimensionType = world.getWorld().getDimensionKey();
						boolean dimensionCriteria = false;

						<#list data.spawnWorldTypes as worldType>
							<#if worldType=="Surface">
								if(dimensionType == World.OVERWORLD)
									dimensionCriteria = true;
							<#elseif worldType=="Nether">
								if(dimensionType == World.THE_NETHER)
									dimensionCriteria = true;
							<#elseif worldType=="End">
								if(dimensionType == World.THE_END)
									dimensionCriteria = true;
							<#else>
								if(dimensionType == RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
										new ResourceLocation("${generator.getResourceLocationForModElement(worldType.toString().replace("CUSTOM:", ""))}")))
									dimensionCriteria = true;
							</#if>
						</#list>

						if(!dimensionCriteria)
							return false;

						<#if hasProcedure(data.generateCondition)>
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						if (!<@procedureOBJToConditionCode data.generateCondition/>)
							return false;
						</#if>

						int generated = 0;
						for(int j = 0; j < ${data.frequencyOnChunks}; ++j) {
							BlockPos blockpos = pos.add(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
							if (world.isAirBlock(blockpos)) {
								BlockPos blockpos1 = blockpos.down();
								int k = 1 + random.nextInt(random.nextInt(${data.growapableMaxHeight}) + 1);
								k = Math.min(${data.growapableMaxHeight}, k);
								for(int l = 0; l < k; ++l) {
									if (block.getDefaultState().isValidPosition(world, blockpos)) {
										world.setBlockState(blockpos.up(l), block.getDefaultState(), 2);
										generated++;
									}
								}
							}
						}
						return generated > 0;
					}
				};
			<#elseif data.plantType == "double">
				feature = new RandomPatchFeature(BlockClusterFeatureConfig.field_236587_a_) {
					@Override public boolean generate(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, BlockClusterFeatureConfig config) {
						RegistryKey<World> dimensionType = world.getWorld().getDimensionKey();
						boolean dimensionCriteria = false;

			        	<#list data.spawnWorldTypes as worldType>
							<#if worldType=="Surface">
			    		        if(dimensionType == World.OVERWORLD)
									dimensionCriteria = true;
							<#elseif worldType=="Nether">
			    				if(dimensionType == World.THE_NETHER)
									dimensionCriteria = true;
							<#elseif worldType=="End">
			    				if(dimensionType == World.THE_END)
									dimensionCriteria = true;
							<#else>
			    				if(dimensionType == RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
										new ResourceLocation("${generator.getResourceLocationForModElement(worldType.toString().replace("CUSTOM:", ""))}")))
									dimensionCriteria = true;
							</#if>
						</#list>

						if(!dimensionCriteria)
							return false;

			    		<#if hasProcedure(data.generateCondition)>
			    		    int x = pos.getX();
			    			int y = pos.getY();
			    			int z = pos.getZ();
			    			if (!<@procedureOBJToConditionCode data.generateCondition/>)
								return false;
						</#if>

						return super.generate(world, generator, random, pos, config);
					}
				};
			</#if>

			configuredFeature = feature
					.withConfiguration((new BlockClusterFeatureConfig.Builder(new SimpleBlockStateProvider(block.getDefaultState()),
											new <#if data.plantType == "double">DoublePlant<#else>Simple</#if>BlockPlacer())).tries(${data.patchSize})
											<#if data.plantType == "double" && data.doublePlantGenerationType == "Flower">.func_227317_b_()</#if>.build()
			                          )
					<#if (data.plantType == "normal" && data.staticPlantGenerationType == "Grass") || (data.plantType == "double" && data.doublePlantGenerationType == "Grass")>
					.withPlacement(Placement.COUNT_NOISE.configure(new NoiseDependant(-0.8, 0, ${data.frequencyOnChunks})))
					<#else>
						<#if data.plantType == "normal" || data.plantType == "double">
						.withPlacement(Features.Placements.VEGETATION_PLACEMENT).withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT).func_242731_b(${data.frequencyOnChunks})
						<#else>
						.withPlacement(Features.Placements.PATCH_PLACEMENT).func_242731_b(${data.frequencyOnChunks})
						</#if>
					</#if>;

			event.getRegistry().register(feature.setRegistryName("${registryname}"));
			Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation("${modid}:${registryname}"), configuredFeature);
		}

	}

	@SubscribeEvent public void addFeatureToBiomes(BiomeLoadingEvent event) {
		<#if data.restrictionBiomes?has_content>
				boolean biomeCriteria = false;
			<#list data.restrictionBiomes as restrictionBiome>
				<#if restrictionBiome.canProperlyMap()>
					if (new ResourceLocation("${restrictionBiome}").equals(event.getName()))
						biomeCriteria = true;
				</#if>
			</#list>
				if (!biomeCriteria)
					return;
		</#if>

		event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)
				.add(() -> configuredFeature);
	}
	</#if>

	public static class BlockCustomFlower extends <#if data.plantType == "normal">Flower<#elseif data.plantType == "growapable">SugarCane<#elseif data.plantType == "double">DoublePlant</#if>Block {

		public BlockCustomFlower() {
			super(<#if data.plantType == "normal">
				${data.suspiciousStewEffect?starts_with("CUSTOM:")?then("Effects.SATURATION", generator.map(data.suspiciousStewEffect, "effects"))}, ${data.suspiciousStewDuration},</#if>
					<#if generator.map(data.colorOnMap, "mapcolors") != "DEFAULT">
					Block.Properties.create(Material.PLANTS, MaterialColor.${generator.map(data.colorOnMap, "mapcolors")})
					<#else>
					Block.Properties.create(Material.PLANTS)
					</#if>
					<#if data.plantType == "growapable" || data.forceTicking>
					.tickRandomly()
					</#if>
					.doesNotBlockMovement()
					<#if data.isCustomSoundType>
						.sound(new ForgeSoundType(1.0f, 1.0f, () -> new SoundEvent(new ResourceLocation("${data.breakSound}")),
						() -> new SoundEvent(new ResourceLocation("${data.stepSound}")),
						() -> new SoundEvent(new ResourceLocation("${data.placeSound}")),
						() -> new SoundEvent(new ResourceLocation("${data.hitSound}")),
						() -> new SoundEvent(new ResourceLocation("${data.fallSound}"))))
					<#else>
						.sound(SoundType.${data.soundOnStep})
					</#if>
					<#if data.unbreakable>
					.hardnessAndResistance(-1, 3600000)
					<#else>
					.hardnessAndResistance(${data.hardness}f, ${data.resistance}f)
					</#if>
					<#if data.emissiveRendering>
					.setNeedsPostProcessing((bs, br, bp) -> true).setEmmisiveRendering((bs, br, bp) -> true)
					</#if>
					<#if data.speedFactor != 1.0>
					.speedFactor(${data.speedFactor}f)
					</#if>
					<#if data.jumpFactor != 1.0>
					.jumpFactor(${data.jumpFactor}f)
					</#if>
					.setLightLevel(s -> ${data.luminance})
			);
			setRegistryName("${registryname}");
		}

		<#if data.plantType == "normal">
			<#if data.suspiciousStewEffect?starts_with("CUSTOM:")>
			@Override public Effect getStewEffect() {
				return ${generator.map(data.suspiciousStewEffect, "effects")};
			}
			</#if>

			<#if (data.suspiciousStewDuration > 0)>
			@Override public int getStewEffectDuration() {
				return ${data.suspiciousStewDuration};
			}
			</#if>
		</#if>

		<#if data.customBoundingBox && data.boundingBoxes??>
		@Override public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
			<#if data.isBoundingBoxEmpty()>
				return VoxelShapes.empty();
			<#else>
				<#if !data.disableOffset> Vector3d offset = state.getOffset(world, pos); </#if>
				<@makeBoundingBox data.positiveBoundingBoxes() data.negativeBoundingBoxes() data.disableOffset "north"/>
			</#if>
		}
		</#if>

        <#if data.isReplaceable>
        @Override public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
			return useContext.getItem().getItem() != this.asItem();
		}
        </#if>

		<#if data.flammability != 0>
		@Override public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
			return ${data.flammability};
		}
		</#if>

		<#if generator.map(data.aiPathNodeType, "pathnodetypes") != "DEFAULT">
		@Override public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos, MobEntity entity) {
			return PathNodeType.${generator.map(data.aiPathNodeType, "pathnodetypes")};
		}
		</#if>

		<#if data.offsetType != "XZ">
		@Override public Block.OffsetType getOffsetType() {
			return Block.OffsetType.${data.offsetType};
		}
		</#if>

		<#if data.specialInfo?has_content>
		@Override @OnlyIn(Dist.CLIENT) public void addInformation(ItemStack itemstack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
			super.addInformation(itemstack, world, list, flag);
			<#list data.specialInfo as entry>
			list.add(new StringTextComponent("${JavaConventions.escapeStringForJava(entry)}"));
			</#list>
		}
		</#if>

		<#if data.fireSpreadSpeed != 0>
		@Override public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
			return ${data.fireSpreadSpeed};
		}
		</#if>

		<#if data.creativePickItem?? && !data.creativePickItem.isEmpty()>
		@Override public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        	return ${mappedMCItemToItemStackCode(data.creativePickItem, 1)};
    	}
        </#if>

        <#if !data.useLootTableForDrops>
		    <#if data.dropAmount != 1 && !(data.customDrop?? && !data.customDrop.isEmpty())>
		    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			    <#if data.plantType == "double">
                if(state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER)
                    return Collections.emptyList();
                </#if>

                List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			    if(!dropsOriginal.isEmpty())
				    return dropsOriginal;
			    return Collections.singletonList(new ItemStack(this, ${data.dropAmount}));
		    }
		    <#elseif data.customDrop?? && !data.customDrop.isEmpty()>
		    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			    <#if data.plantType == "double">
                if(state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER)
                    return Collections.emptyList();
                </#if>

                List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			    if(!dropsOriginal.isEmpty())
				    return dropsOriginal;
			    return Collections.singletonList(${mappedMCItemToItemStackCode(data.customDrop, data.dropAmount)});
		    }
		    <#else>
		    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
			    <#if data.plantType == "double">
                if(state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.LOWER)
                    return Collections.emptyList();
                </#if>

                List<ItemStack> dropsOriginal = super.getDrops(state, builder);
			    if(!dropsOriginal.isEmpty())
				    return dropsOriginal;
			    return Collections.singletonList(new ItemStack(this, 1));
		    }
            </#if>
        </#if>

		<#if (data.canBePlacedOn?size > 0) || hasProcedure(data.placingCondition)>
			<#if data.plantType != "growapable">
			@Override public boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
				<#if hasProcedure(data.placingCondition)>
					boolean additionalCondition = true;
					if (worldIn instanceof IWorld) {
						IWorld world = (IWorld) worldIn;
						int x = pos.getX();
						int y = pos.getY() + 1;
						int z = pos.getZ();
						BlockState blockstate = world.getBlockState(pos.up());
						additionalCondition = <@procedureOBJToConditionCode data.placingCondition/>;
					}
				</#if>

				Block ground = state.getBlock();
				return
				<#if (data.canBePlacedOn?size > 0)>(
					<#list data.canBePlacedOn as canBePlacedOn>
						ground == ${mappedBlockToBlock(canBePlacedOn)}
						<#if canBePlacedOn?has_next>||</#if>
					</#list>)
				</#if>
				<#if (data.canBePlacedOn?size > 0) && hasProcedure(data.placingCondition)> && </#if>
				<#if hasProcedure(data.placingCondition)> additionalCondition </#if>;
			}
			</#if>

			@Override public boolean isValidPosition(BlockState blockstate, IWorldReader worldIn, BlockPos pos) {
				BlockPos blockpos = pos.down();
				BlockState groundState = worldIn.getBlockState(blockpos);
				Block ground = groundState.getBlock();

				<#if data.plantType = "normal">
					return this.isValidGround(groundState, worldIn, blockpos)
				<#elseif data.plantType == "growapable">
					<#if hasProcedure(data.placingCondition)>
					boolean additionalCondition = true;
					if (worldIn instanceof IWorld) {
						IWorld world = (IWorld) worldIn;
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						additionalCondition = <@procedureOBJToConditionCode data.placingCondition/>;
					}
					</#if>

					return ground == this ||
					<#if (data.canBePlacedOn?size > 0)>(
						<#list data.canBePlacedOn as canBePlacedOn>
						ground == ${mappedBlockToBlock(canBePlacedOn)}
						<#if canBePlacedOn?has_next>||</#if>
					</#list>)</#if>
					<#if (data.canBePlacedOn?size > 0) && hasProcedure(data.placingCondition)> && </#if>
					<#if hasProcedure(data.placingCondition)> additionalCondition </#if>
				<#else>
					if (blockstate.get(HALF) == DoubleBlockHalf.UPPER)
						return groundState.isIn(this) && groundState.get(HALF) == DoubleBlockHalf.LOWER;
					else
						return this.isValidGround(groundState, worldIn, blockpos)
				</#if>;
			}
		</#if>

		@Override public PlantType getPlantType(IBlockReader world, BlockPos pos) {
			return PlantType.${generator.map(data.growapableSpawnType, "planttypes")};
		}

        <#if hasProcedure(data.onBlockAdded)>
		@Override public void onBlockAdded(BlockState blockstate, World world, BlockPos pos, BlockState oldState, boolean moving) {
			super.onBlockAdded(blockstate, world, pos, oldState, moving);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onBlockAdded/>
		}
        </#if>

        <#if hasProcedure(data.onTickUpdate) || data.plantType == "growapable">
		@Override public void tick(BlockState blockstate, ServerWorld world, BlockPos pos, Random random) {
			<#if hasProcedure(data.onTickUpdate)>
                int x = pos.getX();
			    int y = pos.getY();
			    int z = pos.getZ();
                <@procedureOBJToCode data.onTickUpdate/>
            </#if>

            <#if data.plantType == "growapable">
			if (!blockstate.isValidPosition(world, pos)) {
			   world.destroyBlock(pos, true);
			} else if (world.isAirBlock(pos.up())) {
			   int i = 1;
			   for(;world.getBlockState(pos.down(i)).getBlock() == this; ++i);
			   if (i < ${data.growapableMaxHeight}) {
			      int j = blockstate.get(AGE);
			      if (j == 15) {
			         world.setBlockState(pos.up(), getDefaultState());
			         world.setBlockState(pos, blockstate.with(AGE, 0), 4);
			      } else {
			         world.setBlockState(pos, blockstate.with(AGE, j + 1), 4);
			      }
			   }
			}
            </#if>
		}
        </#if>

        <#if hasProcedure(data.onRandomUpdateEvent)>
		@OnlyIn(Dist.CLIENT) @Override
		public void animateTick(BlockState blockstate, World world, BlockPos pos, Random random) {
			super.animateTick(blockstate, world, pos, random);
			PlayerEntity entity = Minecraft.getInstance().player;
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onRandomUpdateEvent/>
		}
        </#if>

        <#if hasProcedure(data.onNeighbourBlockChanges)>
		@Override
		public void neighborChanged(BlockState blockstate, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
			super.neighborChanged(blockstate, world, pos, neighborBlock, fromPos, isMoving);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onNeighbourBlockChanges/>
		}
        </#if>

        <#if hasProcedure(data.onEntityCollides)>
		@Override public void onEntityCollision(BlockState blockstate, World world, BlockPos pos, Entity entity) {
			super.onEntityCollision(blockstate, world, pos, entity);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onEntityCollides/>
		}
        </#if>

        <#if hasProcedure(data.onDestroyedByPlayer)>
		@Override
		public boolean removedByPlayer(BlockState blockstate, World world, BlockPos pos, PlayerEntity entity,
				boolean willHarvest, FluidState fluid) {
			boolean retval = super.removedByPlayer(blockstate, world, pos, entity, willHarvest, fluid);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onDestroyedByPlayer/>
			return retval;
		}
        </#if>

        <#if hasProcedure(data.onDestroyedByExplosion)>
		@Override public void onExplosionDestroy(World world, BlockPos pos, Explosion e) {
			super.onExplosionDestroy(world, pos, e);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onDestroyedByExplosion/>
		}
        </#if>

        <#if hasProcedure(data.onStartToDestroy)>
		@Override public void onBlockClicked(BlockState blockstate, World world, BlockPos pos, PlayerEntity entity) {
			super.onBlockClicked(blockstate, world, pos, entity);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onStartToDestroy/>
		}
        </#if>

        <#if hasProcedure(data.onBlockPlacedBy)>
		@Override
		public void onBlockPlacedBy(World world, BlockPos pos, BlockState blockstate, LivingEntity entity, ItemStack itemstack) {
			super.onBlockPlacedBy(world, pos, blockstate, entity, itemstack);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			<@procedureOBJToCode data.onBlockPlacedBy/>
		}
        </#if>

        <#if hasProcedure(data.onRightClicked)>
		@Override public ActionResultType onBlockActivated(BlockState blockstate, World world, BlockPos pos, PlayerEntity entity, Hand hand, BlockRayTraceResult hit) {
			super.onBlockActivated(blockstate, world, pos, entity, hand, hit);
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			double hitX = hit.getHitVec().x;
			double hitY = hit.getHitVec().y;
			double hitZ = hit.getHitVec().z;
			Direction direction = hit.getFace();
			<#if hasReturnValueOf(data.onRightClicked, "actionresulttype")>
				return <@procedureOBJToActionResultTypeCode data.onRightClicked/>;
			<#else>
				<@procedureOBJToCode data.onRightClicked/>
				return ActionResultType.SUCCESS;
			</#if>
		}
        </#if>

		<#if data.hasTileEntity>
		@Override public boolean hasTileEntity(BlockState state) {
			return true;
		}

		@Override public TileEntity createTileEntity(BlockState state, IBlockReader world) {
			return new CustomTileEntity();
		}

		@Override
		public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
			super.eventReceived(state, world, pos, eventID, eventParam);
			TileEntity tileentity = world.getTileEntity(pos);
			return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
		}
		</#if>

	}

	<#if data.hasTileEntity>
	private static class CustomTileEntity extends TileEntity {

		public CustomTileEntity() {
			super(tileEntityType);
		}

		@Override public SUpdateTileEntityPacket getUpdatePacket() {
			return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
		}

		@Override public CompoundNBT getUpdateTag() {
			return this.write(new CompoundNBT());
		}

		@Override public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
			this.read(this.getBlockState(), pkt.getNbtCompound());
		}

	}
	</#if>

}
<#-- @formatter:on -->