/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.modgui;

import net.mcreator.blockly.BlocklyCompileNote;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.data.Dependency;
import net.mcreator.blockly.data.ExternalBlockLoader;
import net.mcreator.blockly.data.ToolboxBlock;
import net.mcreator.blockly.java.BlocklyToJava;
import net.mcreator.element.GeneratableElement;
import net.mcreator.element.ModElementType;
import net.mcreator.element.parts.Particle;
import net.mcreator.element.parts.TabEntry;
import net.mcreator.element.types.GUI;
import net.mcreator.element.types.LivingEntity;
import net.mcreator.generator.blockly.BlocklyBlockCodeGenerator;
import net.mcreator.generator.blockly.ProceduralBlockCodeGenerator;
import net.mcreator.generator.template.TemplateGeneratorException;
import net.mcreator.minecraft.DataListEntry;
import net.mcreator.minecraft.ElementUtil;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.MCreatorApplication;
import net.mcreator.ui.blockly.BlocklyEditorToolbar;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.ui.blockly.BlocklyPanel;
import net.mcreator.ui.blockly.CompileNotesPanel;
import net.mcreator.ui.component.JColor;
import net.mcreator.ui.component.JEmptyBox;
import net.mcreator.ui.component.SearchableComboBox;
import net.mcreator.ui.component.util.ComboBoxUtil;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.dialogs.TextureImportDialogs;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.laf.renderer.ModelComboBoxRenderer;
import net.mcreator.ui.laf.renderer.WTextureComboBoxRenderer;
import net.mcreator.ui.minecraft.*;
import net.mcreator.ui.procedure.ProcedureSelector;
import net.mcreator.ui.validation.AggregatedValidationResult;
import net.mcreator.ui.validation.Validator;
import net.mcreator.ui.validation.component.VComboBox;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.ui.validation.validators.TextFieldValidator;
import net.mcreator.util.ListUtils;
import net.mcreator.util.StringUtils;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.elements.VariableTypeLoader;
import net.mcreator.workspace.resources.Model;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LivingEntityGUI extends ModElementGUI<LivingEntity> {

	private ProcedureSelector onStruckByLightning;
	private ProcedureSelector whenMobFalls;
	private ProcedureSelector whenMobDies;
	private ProcedureSelector whenMobIsHurt;
	private ProcedureSelector onRightClickedOn;
	private ProcedureSelector whenThisMobKillsAnother;
	private ProcedureSelector onMobTickUpdate;
	private ProcedureSelector onPlayerCollidesWith;
	private ProcedureSelector onInitialSpawn;

	private ProcedureSelector particleCondition;
	private ProcedureSelector spawningCondition;

	private final SoundSelector livingSound = new SoundSelector(mcreator);
	private final SoundSelector hurtSound = new SoundSelector(mcreator);
	private final SoundSelector deathSound = new SoundSelector(mcreator);
	private final SoundSelector stepSound = new SoundSelector(mcreator);

	private final VTextField mobName = new VTextField();

	private final JSpinner attackStrength = new JSpinner(new SpinnerNumberModel(3, 0, 10000, 1));
	private final JSpinner movementSpeed = new JSpinner(new SpinnerNumberModel(0.3, 0, 50, 0.1));
	private final JSpinner armorBaseValue = new JSpinner(new SpinnerNumberModel(0.0, 0, 100, 0.1));
	private final JSpinner health = new JSpinner(new SpinnerNumberModel(10, 0, 1024, 1));
	private final JSpinner knockbackResistance = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 0.1));
	private final JSpinner attackKnockback = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 0.1));

	private final JSpinner trackingRange = new JSpinner(new SpinnerNumberModel(64, 0, 10000, 1));

	private final JSpinner spawningProbability = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
	private final JSpinner minNumberOfMobsPerGroup = new JSpinner(new SpinnerNumberModel(4, 1, 1000, 1));
	private final JSpinner maxNumberOfMobsPerGroup = new JSpinner(new SpinnerNumberModel(4, 1, 1000, 1));

	private final JSpinner modelWidth = new JSpinner(new SpinnerNumberModel(0.6, 0, 1024, 0.1));
	private final JSpinner modelHeight = new JSpinner(new SpinnerNumberModel(1.8, 0, 1024, 0.1));
	private final JSpinner mountedYOffset = new JSpinner(new SpinnerNumberModel(0, -1024, 1024, 0.1));
	private final JSpinner modelShadowSize = new JSpinner(new SpinnerNumberModel(0.5, 0, 20, 0.1));
	private final JCheckBox disableCollisions = new JCheckBox("Disable collision box");

	private final JSpinner xpAmount = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));

	private final JCheckBox hasAI = L10N.checkbox("elementgui.living_entity.has_ai");
	private final JCheckBox isBoss = new JCheckBox();

	private final JCheckBox immuneToFire = L10N.checkbox("elementgui.living_entity.immune_fire");
	private final JCheckBox immuneToArrows = L10N.checkbox("elementgui.living_entity.immune_arrows");
	private final JCheckBox immuneToFallDamage = L10N.checkbox("elementgui.living_entity.immune_fall_damage");
	private final JCheckBox immuneToCactus = L10N.checkbox("elementgui.living_entity.immune_cactus");
	private final JCheckBox immuneToDrowning = L10N.checkbox("elementgui.living_entity.immune_drowning");
	private final JCheckBox immuneToLightning = L10N.checkbox("elementgui.living_entity.immune_lightning");
	private final JCheckBox immuneToPotions = L10N.checkbox("elementgui.living_entity.immune_potions");
	private final JCheckBox immuneToPlayer = L10N.checkbox("elementgui.living_entity.immune_player");
	private final JCheckBox immuneToExplosion = L10N.checkbox("elementgui.living_entity.immune_explosions");
	private final JCheckBox immuneToTrident = L10N.checkbox("elementgui.living_entity.immune_trident");
	private final JCheckBox immuneToAnvil = L10N.checkbox("elementgui.living_entity.immune_anvil");
	private final JCheckBox immuneToWither = L10N.checkbox("elementgui.living_entity.immune_wither");
	private final JCheckBox immuneToDragonBreath = L10N.checkbox("elementgui.living_entity.immune_dragon_breath");

	private final JCheckBox spawnParticles = L10N.checkbox("elementgui.living_entity.spawn_particles_around");

	private final JCheckBox waterMob = L10N.checkbox("elementgui.living_entity.is_water_mob");
	private final JCheckBox flyingMob = L10N.checkbox("elementgui.living_entity.is_flying_mob");

	private final JCheckBox hasSpawnEgg = new JCheckBox();
	private final DataListComboBox creativeTab = new DataListComboBox(mcreator);

	private final JComboBox<String> mobSpawningType = new JComboBox<>(
			ElementUtil.getDataListAsStringArray("mobspawntypes"));

	private MCItemHolder mobDrop;
	private MCItemHolder equipmentMainHand;
	private MCItemHolder equipmentHelmet;
	private MCItemHolder equipmentBody;
	private MCItemHolder equipmentLeggings;
	private MCItemHolder equipmentBoots;
	private MCItemHolder equipmentOffHand;

	private final JComboBox<String> guiBoundTo = new JComboBox<>();
	private final JSpinner inventorySize = new JSpinner(new SpinnerNumberModel(9, 0, 256, 1));
	private final JSpinner inventoryStackSize = new JSpinner(new SpinnerNumberModel(64, 1, 1024, 1));

	private MCItemHolder rangedAttackItem;

	private final JComboBox<String> rangedItemType = new JComboBox<>();

	private final JTextField mobLabel = new JTextField();

	private final JCheckBox spawnInDungeons = L10N.checkbox("elementgui.living_entity.spawn_dungeons");
	private final JColor spawnEggBaseColor = new JColor(mcreator);
	private final JColor spawnEggDotColor = new JColor(mcreator);

	private static final Model biped = new Model.BuiltInModel("Biped");
	private static final Model chicken = new Model.BuiltInModel("Chicken");
	private static final Model cow = new Model.BuiltInModel("Cow");
	private static final Model creeper = new Model.BuiltInModel("Creeper");
	private static final Model ghast = new Model.BuiltInModel("Ghast");
	private static final Model pig = new Model.BuiltInModel("Pig");
	private static final Model slime = new Model.BuiltInModel("Slime");
	private static final Model spider = new Model.BuiltInModel("Spider");
	private static final Model villager = new Model.BuiltInModel("Villager");
	private static final Model silverfish = new Model.BuiltInModel("Silverfish");
	public static final Model[] builtinmobmodels = new Model[] { biped, chicken, cow, creeper, ghast, pig, slime,
			spider, villager, silverfish };
	private final JComboBox<Model> mobModel = new JComboBox<>(builtinmobmodels);

	private final VComboBox<String> mobModelTexture = new SearchableComboBox<>();
	private final VComboBox<String> mobModelGlowTexture = new SearchableComboBox<>();

	//mob bases
	private final JComboBox<String> aiBase = new JComboBox<>(
			Stream.of("(none)", "Creeper", "Skeleton", "Enderman", "Blaze", "Slime", "Witch", "Zombie", "MagmaCube",
					"Pig", "Villager", "Wolf", "Cow", "Bat", "Chicken", "Ocelot", "Squid", "Horse", "Spider",
					"IronGolem").sorted().toArray(String[]::new));

	private final JComboBox<String> mobBehaviourType = new JComboBox<>(new String[] { "Mob", "Creature" });
	private final JComboBox<String> mobCreatureType = new JComboBox<>(
			new String[] { "UNDEFINED", "UNDEAD", "ARTHROPOD", "ILLAGER", "WATER" });
	private final JComboBox<String> bossBarColor = new JComboBox<>(
			new String[] { "PINK", "BLUE", "RED", "GREEN", "YELLOW", "PURPLE", "WHITE" });
	private final JComboBox<String> bossBarType = new JComboBox<>(
			new String[] { "PROGRESS", "NOTCHED_6", "NOTCHED_10", "NOTCHED_12", "NOTCHED_20" });

	private final DataListComboBox particleToSpawn = new DataListComboBox(mcreator);
	private final JComboBox<String> particleSpawningShape = new JComboBox<>(
			new String[] { "Spread", "Top", "Tube", "Plane" });
	private final JSpinner particleSpawningRadious = new JSpinner(new SpinnerNumberModel(0.5, 0, 2, 0.1f));
	private final JSpinner particleAmount = new JSpinner(new SpinnerNumberModel(4, 0, 1000, 1));

	private final JCheckBox ridable = L10N.checkbox("elementgui.living_entity.is_rideable");

	private final JCheckBox canControlForward = L10N.checkbox("elementgui.living_entity.control_forward");
	private final JCheckBox canControlStrafe = L10N.checkbox("elementgui.living_entity.control_strafe");

	private final JCheckBox breedable = L10N.checkbox("elementgui.living_entity.is_breedable");

	private final JCheckBox tameable = L10N.checkbox("elementgui.living_entity.is_tameable");

	private final JCheckBox ranged = L10N.checkbox("elementgui.living_entity.is_ranged");

	private MCItemListField breedTriggerItems;

	private final JCheckBox spawnThisMob = new JCheckBox();
	private final JCheckBox doesDespawnWhenIdle = new JCheckBox();

	private BiomeListField restrictionBiomes;

	private BlocklyPanel blocklyPanel;
	private final CompileNotesPanel compileNotesPanel = new CompileNotesPanel();
	private boolean hasErrors = false;
	private Map<String, ToolboxBlock> externalBlocks;

	private boolean disableMobModelCheckBoxListener = false;

	public LivingEntityGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
		super(mcreator, modElement, editingMode);
		this.initGUI();
		super.finalizeGUI();
	}

	private void setDefaultAISet() {
		blocklyPanel.setXML("<xml xmlns=\"https://developers.google.com/blockly/xml\">"
				+ "<block type=\"aitasks_container\" deletable=\"false\" x=\"40\" y=\"40\"><next>"
				+ "<block type=\"attack_on_collide\"><field name=\"speed\">1.2</field><field name=\"longmemory\">FALSE</field><next>"
				+ "<block type=\"wander\"><field name=\"speed\">1</field><next>"
				+ "<block type=\"attack_action\"><field name=\"callhelp\">FALSE</field><next>"
				+ "<block type=\"look_around\"><next><block type=\"swim_in_water\"/></next>"
				+ "</block></next></block></next></block></next></block></next></block></xml>");
	}

	private void regenerateAITasks() {
		BlocklyBlockCodeGenerator blocklyBlockCodeGenerator = new BlocklyBlockCodeGenerator(externalBlocks,
				mcreator.getGeneratorStats().getGeneratorAITasks());

		BlocklyToJava blocklyToJava;
		try {
			blocklyToJava = new BlocklyToJava(mcreator.getWorkspace(), "aitasks_container", blocklyPanel.getXML(), null,
					new ProceduralBlockCodeGenerator(blocklyBlockCodeGenerator));
		} catch (TemplateGeneratorException e) {
			return;
		}

		List<BlocklyCompileNote> compileNotesArrayList = blocklyToJava.getCompileNotes();

		SwingUtilities.invokeLater(() -> {
			compileNotesPanel.updateCompileNotes(compileNotesArrayList);
			hasErrors = false;
			for (BlocklyCompileNote note : compileNotesArrayList) {
				if (note.type() == BlocklyCompileNote.Type.ERROR) {
					hasErrors = true;
					break;
				}
			}
		});
	}

	@Override protected void initGUI() {
		onStruckByLightning = new ProcedureSelector(this.withEntry("entity/when_struck_by_lightning"), mcreator,
				L10N.t("elementgui.living_entity.event_struck_by_lightning"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity"));
		whenMobFalls = new ProcedureSelector(this.withEntry("entity/when_falls"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_falls"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity"));
		whenMobDies = new ProcedureSelector(this.withEntry("entity/when_dies"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_dies"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity"));
		whenMobIsHurt = new ProcedureSelector(this.withEntry("entity/when_hurt"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_is_hurt"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity"));
		onRightClickedOn = new ProcedureSelector(this.withEntry("entity/when_right_clicked"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_right_clicked"),
				VariableTypeLoader.BuiltInTypes.ACTIONRESULTTYPE, Dependency.fromString(
				"x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity/itemstack:itemstack")).makeReturnValueOptional();
		whenThisMobKillsAnother = new ProcedureSelector(this.withEntry("entity/when_kills_another"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_kills_another"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity"));
		onMobTickUpdate = new ProcedureSelector(this.withEntry("entity/on_tick_update"), mcreator,
				L10N.t("elementgui.living_entity.event_mob_tick_update"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity"));
		onPlayerCollidesWith = new ProcedureSelector(this.withEntry("entity/when_player_collides"), mcreator,
				L10N.t("elementgui.living_entity.event_player_collides_with"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity/sourceentity:entity"));
		onInitialSpawn = new ProcedureSelector(this.withEntry("entity/on_initial_spawn"), mcreator,
				L10N.t("elementgui.living_entity.event_initial_spawn"),
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity"));

		particleCondition = new ProcedureSelector(this.withEntry("entity/particle_condition"), mcreator,
				L10N.t("elementgui.living_entity.condition_particle_spawn"), ProcedureSelector.Side.CLIENT, true,
				VariableTypeLoader.BuiltInTypes.LOGIC,
				Dependency.fromString("x:number/y:number/z:number/world:world/entity:entity")).makeInline();
		spawningCondition = new ProcedureSelector(this.withEntry("entity/condition_natural_spawning"), mcreator,
				L10N.t("elementgui.living_entity.condition_natural_spawn"), VariableTypeLoader.BuiltInTypes.LOGIC,
				Dependency.fromString("x:number/y:number/z:number/world:world")).setDefaultName(
				L10N.t("condition.common.use_vanilla")).makeInline();

		restrictionBiomes = new BiomeListField(mcreator);
		breedTriggerItems = new MCItemListField(mcreator, ElementUtil::loadBlocksAndItems);

		mobModelTexture.setRenderer(new WTextureComboBoxRenderer.OtherTextures(mcreator.getWorkspace()));
		mobModelGlowTexture.setRenderer(new WTextureComboBoxRenderer.OtherTextures(mcreator.getWorkspace()));

		guiBoundTo.addActionListener(e -> {
			if (!isEditingMode()) {
				String selected = (String) guiBoundTo.getSelectedItem();
				if (selected != null) {
					ModElement element = mcreator.getWorkspace().getModElementByName(selected);
					if (element != null) {
						GeneratableElement generatableElement = element.getGeneratableElement();
						if (generatableElement instanceof GUI) {
							inventorySize.setValue(((GUI) generatableElement).getMaxSlotID() + 1);
						}
					}
				}
			}
		});

		spawnInDungeons.setOpaque(false);
		mobModelTexture.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");
		mobModelGlowTexture.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXXXXXXXX");

		mobDrop = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentMainHand = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentHelmet = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentBody = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentLeggings = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentBoots = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		equipmentOffHand = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);
		rangedAttackItem = new MCItemHolder(mcreator, ElementUtil::loadBlocksAndItems);

		JPanel pane1 = new JPanel(new BorderLayout(0, 0));
		JPanel pane2 = new JPanel(new BorderLayout(0, 0));
		JPanel pane3 = new JPanel(new BorderLayout(0, 0));
		JPanel pane4 = new JPanel(new BorderLayout(0, 0));
		JPanel pane6 = new JPanel(new BorderLayout(0, 0));
		JPanel pane5 = new JPanel(new BorderLayout(0, 0));
		JPanel pane7 = new JPanel(new BorderLayout(0, 0));

		JPanel subpane1 = new JPanel(new GridLayout(11, 2, 0, 2));

		immuneToFire.setOpaque(false);
		immuneToArrows.setOpaque(false);
		immuneToFallDamage.setOpaque(false);
		immuneToCactus.setOpaque(false);
		immuneToDrowning.setOpaque(false);
		immuneToLightning.setOpaque(false);
		immuneToPotions.setOpaque(false);
		immuneToPlayer.setOpaque(false);
		immuneToExplosion.setOpaque(false);
		immuneToTrident.setOpaque(false);
		immuneToAnvil.setOpaque(false);
		immuneToDragonBreath.setOpaque(false);
		immuneToWither.setOpaque(false);

		subpane1.setOpaque(false);

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/behaviour"),
				L10N.label("elementgui.living_entity.behaviour")));
		subpane1.add(mobBehaviourType);

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/drop"),
				L10N.label("elementgui.living_entity.mob_drop")));
		subpane1.add(PanelUtils.totalCenterInPanel(mobDrop));

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/creature_type"),
				L10N.label("elementgui.living_entity.creature_type")));
		subpane1.add(mobCreatureType);

		subpane1.add(L10N.label("elementgui.living_entity.health_xp_amount"));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, 0, 0,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/health"), health),
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/xp_amount"), xpAmount)));

		subpane1.add(L10N.label("elementgui.living_entity.movement_speed_tracking_range"));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, 0, 0,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/movement_speed"), movementSpeed),
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/tracking_range"), trackingRange)));

		subpane1.add(L10N.label("elementgui.living_entity.attack_strenght_armor_value"));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, 0, 0,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/attack_strength"), attackStrength),
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/armor_base_value"), armorBaseValue)));

		subpane1.add(L10N.label("elementgui.living_entity.knockback"));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, 0, 0,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/attack_knockback"), attackKnockback),
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/knockback_resistance"), knockbackResistance)));

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/equipment"),
				L10N.label("elementgui.living_entity.equipment")));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, PanelUtils.totalCenterInPanel(
				PanelUtils.join(FlowLayout.LEFT, 2, 0, equipmentMainHand, equipmentOffHand, equipmentHelmet,
						equipmentBody, equipmentLeggings, equipmentBoots))));

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/ridable"),
				L10N.label("elementgui.living_entity.ridable")));
		subpane1.add(PanelUtils.join(FlowLayout.LEFT, 0, 0, ridable, canControlForward, canControlStrafe));

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/water_entity"),
				L10N.label("elementgui.living_entity.water_mob")));
		subpane1.add(waterMob);

		subpane1.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/flying_entity"),
				L10N.label("elementgui.living_entity.flying_mob")));
		subpane1.add(flyingMob);

		hasAI.setOpaque(false);
		isBoss.setOpaque(false);
		waterMob.setOpaque(false);
		flyingMob.setOpaque(false);
		hasSpawnEgg.setOpaque(false);
		disableCollisions.setOpaque(false);

		livingSound.setText("");
		hurtSound.setText("entity.generic.hurt");
		deathSound.setText("entity.generic.death");

		JPanel subpanel2 = new JPanel(new GridLayout(1, 2, 0, 2));
		subpanel2.setOpaque(false);

		subpanel2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/immunity"),
				L10N.label("elementgui.living_entity.is_immune_to")));
		subpanel2.add(
				PanelUtils.gridElements(4, 4, 0, 0, immuneToFire, immuneToArrows, immuneToFallDamage, immuneToCactus,
						immuneToDrowning, immuneToLightning, immuneToPotions, immuneToPlayer, immuneToExplosion,
						immuneToAnvil, immuneToTrident, immuneToDragonBreath, immuneToWither));

		pane1.add("Center", PanelUtils.totalCenterInPanel(PanelUtils.northAndCenterElement(subpane1, subpanel2)));

		JPanel spo2 = new JPanel(new GridLayout(12, 2, 0, 2));

		spo2.setOpaque(false);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/name"),
				L10N.label("elementgui.living_entity.name")));
		spo2.add(mobName);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/model"),
				L10N.label("elementgui.living_entity.entity_model")));
		spo2.add(mobModel);

		JButton importmobtexture = new JButton(UIRES.get("18px.add"));
		importmobtexture.setToolTipText(L10N.t("elementgui.living_entity.entity_model_import"));
		importmobtexture.setOpaque(false);
		importmobtexture.addActionListener(e -> {
			TextureImportDialogs.importOtherTextures(mcreator);
			mobModelTexture.removeAllItems();
			mobModelTexture.addItem("");
			mcreator.getFolderManager().getOtherTexturesList().forEach(el -> mobModelTexture.addItem(el.getName()));
			mobModelGlowTexture.removeAllItems();
			mobModelGlowTexture.addItem("");
			mcreator.getFolderManager().getOtherTexturesList().forEach(el -> mobModelGlowTexture.addItem(el.getName()));
		});

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/texture"),
				L10N.label("elementgui.living_entity.texture")));
		spo2.add(PanelUtils.centerAndEastElement(mobModelTexture, importmobtexture));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/glow_texture"),
				L10N.label("elementgui.living_entity.glow_texture")));
		spo2.add(mobModelGlowTexture);

		ComponentUtils.deriveFont(mobModelTexture, 16);
		ComponentUtils.deriveFont(mobModelGlowTexture, 16);
		ComponentUtils.deriveFont(aiBase, 16);
		ComponentUtils.deriveFont(mobModel, 16);
		ComponentUtils.deriveFont(rangedItemType, 16);

		mobModel.setRenderer(new ModelComboBoxRenderer());

		spawnEggBaseColor.setOpaque(false);
		spawnEggDotColor.setOpaque(false);

		modelWidth.setPreferredSize(new Dimension(85, 32));
		mountedYOffset.setPreferredSize(new Dimension(85, 32));
		modelHeight.setPreferredSize(new Dimension(85, 32));
		modelShadowSize.setPreferredSize(new Dimension(85, 32));

		armorBaseValue.setPreferredSize(new Dimension(250, 32));
		movementSpeed.setPreferredSize(new Dimension(250, 32));
		trackingRange.setPreferredSize(new Dimension(250, 32));
		attackStrength.setPreferredSize(new Dimension(250, 32));
		attackKnockback.setPreferredSize(new Dimension(250, 32));
		knockbackResistance.setPreferredSize(new Dimension(250, 32));
		health.setPreferredSize(new Dimension(250, 32));
		xpAmount.setPreferredSize(new Dimension(250, 32));

		mobModel.addActionListener(e -> {
			if (disableMobModelCheckBoxListener)
				return;

			if (biped.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.6);
				modelHeight.setValue(1.8);
			} else if (chicken.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.4);
				modelHeight.setValue(0.7);
			} else if (cow.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.9);
				modelHeight.setValue(1.4);
			} else if (creeper.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.6);
				modelHeight.setValue(1.7);
			} else if (ghast.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(1.0);
				modelHeight.setValue(1.0);
			} else if (pig.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.9);
				modelHeight.setValue(0.9);
			} else if (slime.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(1.0);
				modelHeight.setValue(1.0);
			} else if (spider.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(1.4);
				modelHeight.setValue(0.9);
			} else if (villager.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.6);
				modelHeight.setValue(1.95);
			} else if (silverfish.equals(mobModel.getSelectedItem())) {
				modelWidth.setValue(0.4);
				modelHeight.setValue(0.3);
			}
		});

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/bounding_box"),
				L10N.label("elementgui.living_entity.bounding_box")));
		spo2.add(PanelUtils.join(FlowLayout.LEFT, modelWidth, modelHeight, new JEmptyBox(7, 7), modelShadowSize,
				new JEmptyBox(7, 7), mountedYOffset, new JEmptyBox(7, 7), disableCollisions));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_egg_options"),
				L10N.label("elementgui.living_entity.spawn_egg_options")));
		spo2.add(PanelUtils.join(hasSpawnEgg, spawnEggBaseColor, spawnEggDotColor, creativeTab));

		bossBarColor.setEnabled(false);
		bossBarType.setEnabled(false);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/boss_entity"),
				L10N.label("elementgui.living_entity.mob_boss")));
		spo2.add(PanelUtils.join(isBoss, bossBarColor, bossBarType));

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/label"),
				L10N.label("elementgui.living_entity.label")));
		spo2.add(mobLabel);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/sound"),
				L10N.label("elementgui.living_entity.sound")));
		spo2.add(livingSound);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/step_sound"),
				L10N.label("elementgui.living_entity.step_sound")));
		spo2.add(stepSound);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/hurt_sound"),
				L10N.label("elementgui.living_entity.hurt_sound")));
		spo2.add(hurtSound);

		spo2.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/death_sound"),
				L10N.label("elementgui.living_entity.death_sound")));
		spo2.add(deathSound);

		ComponentUtils.deriveFont(mobLabel, 16);

		pane2.setOpaque(false);

		pane2.add("Center", PanelUtils.totalCenterInPanel(spo2));

		JPanel aitop = new JPanel(new GridLayout(2, 2, 10, 10));
		aitop.setOpaque(false);
		aitop.add(PanelUtils.join(FlowLayout.LEFT,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/enable_ai"), hasAI)));

		aitop.add(PanelUtils.join(FlowLayout.LEFT, new JEmptyBox(20, 5),
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/base"),
						L10N.label("elementgui.living_entity.mob_base")), aiBase));

		aitop.add(PanelUtils.join(FlowLayout.LEFT,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/breedable"), breedable), breedTriggerItems,
				tameable));

		breedTriggerItems.setPreferredSize(new Dimension(300, 32));
		aiBase.setPreferredSize(new Dimension(250, 32));

		aitop.add(PanelUtils.join(FlowLayout.LEFT,
				HelpUtils.wrapWithHelpButton(this.withEntry("entity/do_ranged_attacks"), ranged),
				L10N.label("elementgui.living_entity.do_ranged_attacks"), rangedItemType, rangedAttackItem));

		rangedAttackItem.setEnabled(false);

		rangedItemType.addActionListener(
				e -> rangedAttackItem.setEnabled("Default item".equals(rangedItemType.getSelectedItem())));

		ridable.setOpaque(false);
		canControlStrafe.setOpaque(false);
		canControlForward.setOpaque(false);

		JPanel aitopwrp = PanelUtils.maxMargin(aitop, 2, true, true, true, true);
		aitopwrp.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder((Color) UIManager.get("MCreatorLAF.BRIGHT_COLOR"), 1),
				L10N.t("elementgui.living_entity.ai_parameters"), 0, 0, getFont().deriveFont(12.0f),
				(Color) UIManager.get("MCreatorLAF.BRIGHT_COLOR")));

		JPanel aipan = new JPanel(new BorderLayout(0, 5));
		aipan.setOpaque(false);

		externalBlocks = BlocklyLoader.INSTANCE.getAITaskBlockLoader().getDefinedBlocks();

		blocklyPanel = new BlocklyPanel(mcreator);
		blocklyPanel.addTaskToRunAfterLoaded(() -> {
			BlocklyLoader.INSTANCE.getAITaskBlockLoader()
					.loadBlocksAndCategoriesInPanel(blocklyPanel, ExternalBlockLoader.ToolboxType.EMPTY);
			blocklyPanel.getJSBridge()
					.setJavaScriptEventListener(() -> new Thread(LivingEntityGUI.this::regenerateAITasks).start());
			if (!isEditingMode()) {
				setDefaultAISet();
			}
		});

		aipan.add("North", aitopwrp);

		JPanel bpb = new JPanel(new GridLayout());
		bpb.setOpaque(false);
		bpb.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder((Color) UIManager.get("MCreatorLAF.BRIGHT_COLOR"), 1),
				L10N.t("elementgui.living_entity.ai_tasks"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
				getFont(), Color.white));
		BlocklyEditorToolbar blocklyEditorToolbar = new BlocklyEditorToolbar(mcreator, BlocklyEditorType.AI_TASK, blocklyPanel);
		blocklyEditorToolbar.setTemplateLibButtonWidth(156);
		bpb.add(PanelUtils.northAndCenterElement(blocklyEditorToolbar, blocklyPanel));
		aipan.add("Center", bpb);
		aipan.add("South", compileNotesPanel);

		blocklyPanel.setPreferredSize(new Dimension(150, 150));

		pane3.add("Center", PanelUtils.maxMargin(aipan, 10, true, true, true, true));

		breedable.setOpaque(false);
		tameable.setOpaque(false);
		ranged.setOpaque(false);

		hasAI.setSelected(true);

		breedable.addActionListener(actionEvent -> {
			if (breedable.isSelected()) {
				hasAI.setSelected(true);
				hasAI.setEnabled(false);
				this.breedTriggerItems.setEnabled(true);
				this.tameable.setEnabled(true);
			} else {
				hasAI.setEnabled(true);
				this.breedTriggerItems.setEnabled(false);
				this.tameable.setEnabled(false);
			}
		});

		isBoss.addActionListener(e -> {
			bossBarColor.setEnabled(isBoss.isSelected());
			bossBarType.setEnabled(isBoss.isSelected());
		});

		pane3.setOpaque(false);

		JPanel events = new JPanel(new GridLayout(3, 4, 8, 8));
		events.add(onStruckByLightning);
		events.add(whenMobFalls);
		events.add(whenMobDies);
		events.add(whenMobIsHurt);
		events.add(onRightClickedOn);
		events.add(whenThisMobKillsAnother);
		events.add(onMobTickUpdate);
		events.add(onPlayerCollidesWith);
		events.add(onInitialSpawn);
		events.setOpaque(false);
		pane4.add("Center", PanelUtils.totalCenterInPanel(events));

		JPanel particles = new JPanel(new BorderLayout());
		particles.setOpaque(false);

		spawnParticles.setOpaque(false);

		JPanel options = new JPanel(new GridLayout(5, 2, 0, 2));

		options.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gen_particles"), spawnParticles));
		options.add(new JLabel());

		options.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gen_type"),
				L10N.label("elementgui.living_entity.particle_type")));
		options.add(particleToSpawn);

		options.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gen_shape"),
				L10N.label("elementgui.living_entity.particle_shape")));
		options.add(particleSpawningShape);

		options.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gen_spawn_radius"),
				L10N.label("elementgui.living_entity.particle_spawn_radius")));
		options.add(particleSpawningRadious);

		options.add(HelpUtils.wrapWithHelpButton(this.withEntry("particle/gen_average_amount"),
				L10N.label("elementgui.living_entity.particle_average_amount")));
		options.add(particleAmount);

		options.setOpaque(false);

		isBoss.setOpaque(false);

		particles.add("West", PanelUtils.join(PanelUtils.northAndCenterElement(options, particleCondition, 5, 5)));
		pane6.add("Center", PanelUtils.totalCenterInPanel(particles));

		pane4.setOpaque(false);

		JPanel selp = new JPanel(new GridLayout(8, 2, 30, 2));

		ComponentUtils.deriveFont(mobName, 16);

		spawnThisMob.setSelected(true);
		doesDespawnWhenIdle.setSelected(true);

		spawnThisMob.setOpaque(false);
		doesDespawnWhenIdle.setOpaque(false);

		hasSpawnEgg.setSelected(true);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/enable_spawning"),
				L10N.label("elementgui.living_entity.enable_mob_spawning")));
		selp.add(spawnThisMob);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/despawn_idle"),
				L10N.label("elementgui.living_entity.despawn_idle")));
		selp.add(doesDespawnWhenIdle);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_weight"),
				L10N.label("elementgui.living_entity.spawn_weight")));
		selp.add(spawningProbability);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_type"),
				L10N.label("elementgui.living_entity.spawn_type")));
		selp.add(mobSpawningType);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_group_size"),
				L10N.label("elementgui.living_entity.min_spawn_group_size")));
		selp.add(minNumberOfMobsPerGroup);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_group_size"),
				L10N.label("elementgui.living_entity.max_spawn_group_size")));
		selp.add(maxNumberOfMobsPerGroup);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("common/restrict_to_biomes"),
				L10N.label("elementgui.living_entity.restrict_to_biomes")));
		selp.add(restrictionBiomes);

		selp.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/spawn_in_dungeons"),
				L10N.label("elementgui.living_entity.does_spawn_in_dungeons")));
		selp.add(spawnInDungeons);

		selp.setOpaque(false);

		JComponent selpcont = PanelUtils.northAndCenterElement(selp,
				PanelUtils.gridElements(1, 2, 5, 5, L10N.label("elementgui.living_entity.spawn_general_condition"),
						PanelUtils.westAndCenterElement(new JEmptyBox(12, 5), spawningCondition)), 5, 5);

		pane5.add("Center", PanelUtils.totalCenterInPanel(selpcont));

		pane5.setOpaque(false);

		JPanel props = new JPanel(new GridLayout(3, 2, 35, 2));
		props.setOpaque(false);

		props.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/bind_gui"),
				L10N.label("elementgui.living_entity.bind_to_gui")));
		props.add(guiBoundTo);

		props.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/inventory_size"),
				L10N.label("elementgui.living_entity.inventory_size")));
		props.add(inventorySize);

		props.add(HelpUtils.wrapWithHelpButton(this.withEntry("entity/inventory_stack_size"),
				L10N.label("elementgui.common.max_stack_size")));
		props.add(inventoryStackSize);

		pane7.add(PanelUtils.totalCenterInPanel(props));
		pane7.setOpaque(false);
		pane7.setOpaque(false);

		mobModelTexture.setValidator(() -> {
			if (mobModelTexture.getSelectedItem() == null || mobModelTexture.getSelectedItem().equals(""))
				return new Validator.ValidationResult(Validator.ValidationResultType.ERROR,
						L10N.t("elementgui.living_entity.error_entity_model_needs_texture"));
			return Validator.ValidationResult.PASSED;
		});

		mobName.setValidator(
				new TextFieldValidator(mobName, L10N.t("elementgui.living_entity.error_entity_needs_name")));
		mobName.enableRealtimeValidation();

		pane1.setOpaque(false);
		pane6.setOpaque(false);

		addPage(L10N.t("elementgui.living_entity.page_visual_and_sound"), pane2);
		addPage(L10N.t("elementgui.living_entity.page_behaviour"), pane1);
		addPage(L10N.t("elementgui.living_entity.page_particles"), pane6);
		addPage(L10N.t("elementgui.common.page_inventory"), pane7);
		addPage(L10N.t("elementgui.common.page_triggers"), pane4);
		addPage(L10N.t("elementgui.living_entity.page_ai_and_goals"), pane3);
		addPage(L10N.t("elementgui.living_entity.page_spawning"), pane5);

		if (!isEditingMode()) {
			String readableNameFromModElement = StringUtils.machineToReadableName(modElement.getName());
			mobName.setText(readableNameFromModElement);
		}
	}

	@Override public void reloadDataLists() {
		disableMobModelCheckBoxListener = true;

		super.reloadDataLists();
		onStruckByLightning.refreshListKeepSelected();
		whenMobFalls.refreshListKeepSelected();
		whenMobDies.refreshListKeepSelected();
		whenMobIsHurt.refreshListKeepSelected();
		onRightClickedOn.refreshListKeepSelected();
		whenThisMobKillsAnother.refreshListKeepSelected();
		onMobTickUpdate.refreshListKeepSelected();
		onPlayerCollidesWith.refreshListKeepSelected();
		onInitialSpawn.refreshListKeepSelected();

		particleCondition.refreshListKeepSelected();
		spawningCondition.refreshListKeepSelected();

		ComboBoxUtil.updateComboBoxContents(mobModelTexture, ListUtils.merge(Collections.singleton(""),
				mcreator.getFolderManager().getOtherTexturesList().stream().map(File::getName)
						.collect(Collectors.toList())), "");

		ComboBoxUtil.updateComboBoxContents(mobModelGlowTexture, ListUtils.merge(Collections.singleton(""),
				mcreator.getFolderManager().getOtherTexturesList().stream().map(File::getName)
						.collect(Collectors.toList())), "");

		ComboBoxUtil.updateComboBoxContents(mobModel, ListUtils.merge(Arrays.asList(builtinmobmodels),
				Model.getModels(mcreator.getWorkspace()).stream()
						.filter(el -> el.getType() == Model.Type.JAVA || el.getType() == Model.Type.MCREATOR)
						.collect(Collectors.toList())));

		ComboBoxUtil.updateComboBoxContents(creativeTab, ElementUtil.loadAllTabs(mcreator.getWorkspace()),
				new DataListEntry.Dummy("MISC"));

		ComboBoxUtil.updateComboBoxContents(rangedItemType, ListUtils.merge(Collections.singleton("Default item"),
				mcreator.getWorkspace().getModElements().stream()
						.filter(var -> var.getType() == ModElementType.RANGEDITEM).map(ModElement::getName)
						.collect(Collectors.toList())), "Default item");

		ComboBoxUtil.updateComboBoxContents(guiBoundTo, ListUtils.merge(Collections.singleton("<NONE>"),
				mcreator.getWorkspace().getModElements().stream().filter(var -> var.getType() == ModElementType.GUI)
						.map(ModElement::getName).collect(Collectors.toList())), "<NONE>");

		ComboBoxUtil.updateComboBoxContents(particleToSpawn, ElementUtil.loadAllParticles(mcreator.getWorkspace()));

		disableMobModelCheckBoxListener = false;
	}

	@Override protected AggregatedValidationResult validatePage(int page) {
		if (page == 0) {
			return new AggregatedValidationResult(mobModelTexture, mobName);
		} else if (page == 5) {
			if (hasErrors)
				return new AggregatedValidationResult.MULTIFAIL(compileNotesPanel.getCompileNotes().stream()
						.map(compileNote -> "Living entity AI builder: " + compileNote.message())
						.collect(Collectors.toList()));
		} else if (page == 6) {
			if ((int) minNumberOfMobsPerGroup.getValue() > (int) maxNumberOfMobsPerGroup.getValue()) {
				return new AggregatedValidationResult.FAIL("Minimal mob group size can't be bigger than maximal size");
			}
		}
		return new AggregatedValidationResult.PASS();
	}

	@Override public void openInEditingMode(LivingEntity livingEntity) {
		disableMobModelCheckBoxListener = true;
		mobName.setText(livingEntity.mobName);
		mobModelTexture.setSelectedItem(livingEntity.mobModelTexture);
		mobModelGlowTexture.setSelectedItem(livingEntity.mobModelGlowTexture);
		mobSpawningType.setSelectedItem(livingEntity.mobSpawningType);
		rangedItemType.setSelectedItem(livingEntity.rangedItemType);
		spawnEggBaseColor.setColor(livingEntity.spawnEggBaseColor);
		spawnEggDotColor.setColor(livingEntity.spawnEggDotColor);
		mobLabel.setText(livingEntity.mobLabel);
		onStruckByLightning.setSelectedProcedure(livingEntity.onStruckByLightning);
		whenMobFalls.setSelectedProcedure(livingEntity.whenMobFalls);
		whenMobDies.setSelectedProcedure(livingEntity.whenMobDies);
		whenMobIsHurt.setSelectedProcedure(livingEntity.whenMobIsHurt);
		onRightClickedOn.setSelectedProcedure(livingEntity.onRightClickedOn);
		whenThisMobKillsAnother.setSelectedProcedure(livingEntity.whenThisMobKillsAnother);
		onMobTickUpdate.setSelectedProcedure(livingEntity.onMobTickUpdate);
		onPlayerCollidesWith.setSelectedProcedure(livingEntity.onPlayerCollidesWith);
		onInitialSpawn.setSelectedProcedure(livingEntity.onInitialSpawn);
		mobBehaviourType.setSelectedItem(livingEntity.mobBehaviourType);
		mobCreatureType.setSelectedItem(livingEntity.mobCreatureType);
		attackStrength.setValue(livingEntity.attackStrength);
		attackKnockback.setValue(livingEntity.attackKnockback);
		knockbackResistance.setValue(livingEntity.knockbackResistance);
		movementSpeed.setValue(livingEntity.movementSpeed);
		mobDrop.setBlock(livingEntity.mobDrop);
		equipmentMainHand.setBlock(livingEntity.equipmentMainHand);
		equipmentHelmet.setBlock(livingEntity.equipmentHelmet);
		equipmentBody.setBlock(livingEntity.equipmentBody);
		equipmentLeggings.setBlock(livingEntity.equipmentLeggings);
		equipmentBoots.setBlock(livingEntity.equipmentBoots);
		health.setValue(livingEntity.health);
		trackingRange.setValue(livingEntity.trackingRange);
		immuneToFire.setSelected(livingEntity.immuneToFire);
		immuneToArrows.setSelected(livingEntity.immuneToArrows);
		immuneToFallDamage.setSelected(livingEntity.immuneToFallDamage);
		immuneToCactus.setSelected(livingEntity.immuneToCactus);
		immuneToDrowning.setSelected(livingEntity.immuneToDrowning);
		immuneToLightning.setSelected(livingEntity.immuneToLightning);
		immuneToPotions.setSelected(livingEntity.immuneToPotions);
		immuneToPlayer.setSelected(livingEntity.immuneToPlayer);
		immuneToExplosion.setSelected(livingEntity.immuneToExplosion);
		immuneToTrident.setSelected(livingEntity.immuneToTrident);
		immuneToAnvil.setSelected(livingEntity.immuneToAnvil);
		immuneToWither.setSelected(livingEntity.immuneToWither);
		immuneToDragonBreath.setSelected(livingEntity.immuneToDragonBreath);
		xpAmount.setValue(livingEntity.xpAmount);
		livingSound.setSound(livingEntity.livingSound);
		hurtSound.setSound(livingEntity.hurtSound);
		deathSound.setSound(livingEntity.deathSound);
		stepSound.setSound(livingEntity.stepSound);
		hasAI.setSelected(livingEntity.hasAI);
		isBoss.setSelected(livingEntity.isBoss);
		hasSpawnEgg.setSelected(livingEntity.hasSpawnEgg);
		disableCollisions.setSelected(livingEntity.disableCollisions);
		aiBase.setSelectedItem(livingEntity.aiBase);
		spawningProbability.setValue(livingEntity.spawningProbability);
		minNumberOfMobsPerGroup.setValue(livingEntity.minNumberOfMobsPerGroup);
		maxNumberOfMobsPerGroup.setValue(livingEntity.maxNumberOfMobsPerGroup);
		spawnInDungeons.setSelected(livingEntity.spawnInDungeons);
		restrictionBiomes.setListElements(livingEntity.restrictionBiomes);
		spawnParticles.setSelected(livingEntity.spawnParticles);
		particleToSpawn.setSelectedItem(livingEntity.particleToSpawn);
		particleSpawningShape.setSelectedItem(livingEntity.particleSpawningShape);
		particleCondition.setSelectedProcedure(livingEntity.particleCondition);
		spawningCondition.setSelectedProcedure(livingEntity.spawningCondition);
		particleSpawningRadious.setValue(livingEntity.particleSpawningRadious);
		particleAmount.setValue(livingEntity.particleAmount);
		breedTriggerItems.setListElements(livingEntity.breedTriggerItems);
		bossBarColor.setSelectedItem(livingEntity.bossBarColor);
		bossBarType.setSelectedItem(livingEntity.bossBarType);
		equipmentOffHand.setBlock(livingEntity.equipmentOffHand);
		ridable.setSelected(livingEntity.ridable);
		canControlStrafe.setSelected(livingEntity.canControlStrafe);
		canControlForward.setSelected(livingEntity.canControlForward);
		breedable.setSelected(livingEntity.breedable);
		tameable.setSelected(livingEntity.tameable);
		ranged.setSelected(livingEntity.ranged);
		rangedAttackItem.setBlock(livingEntity.rangedAttackItem);
		spawnThisMob.setSelected(livingEntity.spawnThisMob);
		doesDespawnWhenIdle.setSelected(livingEntity.doesDespawnWhenIdle);
		modelWidth.setValue(livingEntity.modelWidth);
		modelHeight.setValue(livingEntity.modelHeight);
		mountedYOffset.setValue(livingEntity.mountedYOffset);
		modelShadowSize.setValue(livingEntity.modelShadowSize);
		armorBaseValue.setValue(livingEntity.armorBaseValue);
		waterMob.setSelected(livingEntity.waterMob);
		flyingMob.setSelected(livingEntity.flyingMob);
		guiBoundTo.setSelectedItem(livingEntity.guiBoundTo);
		inventorySize.setValue(livingEntity.inventorySize);
		inventoryStackSize.setValue(livingEntity.inventoryStackSize);

		if (livingEntity.creativeTab != null)
			creativeTab.setSelectedItem(livingEntity.creativeTab);

		Model model = livingEntity.getEntityModel();
		if (model != null && model.getType() != null && model.getReadableName() != null)
			mobModel.setSelectedItem(model);

		blocklyPanel.setXMLDataOnly(livingEntity.aixml);
		blocklyPanel.addTaskToRunAfterLoaded(() -> {
			blocklyPanel.clearWorkspace();
			blocklyPanel.setXML(livingEntity.aixml);
			regenerateAITasks();
		});

		if (breedable.isSelected()) {
			hasAI.setSelected(true);
			hasAI.setEnabled(false);
			this.breedTriggerItems.setEnabled(true);
			this.tameable.setEnabled(true);
		} else {
			hasAI.setEnabled(true);
			this.breedTriggerItems.setEnabled(false);
			this.tameable.setEnabled(false);
		}

		bossBarColor.setEnabled(isBoss.isSelected());
		bossBarType.setEnabled(isBoss.isSelected());

		rangedAttackItem.setEnabled("Default item".equals(rangedItemType.getSelectedItem()));

		disableMobModelCheckBoxListener = false;
	}

	@Override public LivingEntity getElementFromGUI() {
		LivingEntity livingEntity = new LivingEntity(modElement);
		livingEntity.mobName = mobName.getText();
		livingEntity.mobLabel = mobLabel.getText();
		livingEntity.mobModelTexture = mobModelTexture.getSelectedItem();
		livingEntity.mobModelGlowTexture = mobModelGlowTexture.getSelectedItem();
		livingEntity.spawnEggBaseColor = spawnEggBaseColor.getColor();
		livingEntity.spawnEggDotColor = spawnEggDotColor.getColor();
		livingEntity.hasSpawnEgg = hasSpawnEgg.isSelected();
		livingEntity.disableCollisions = disableCollisions.isSelected();
		livingEntity.isBoss = isBoss.isSelected();
		livingEntity.bossBarColor = (String) bossBarColor.getSelectedItem();
		livingEntity.bossBarType = (String) bossBarType.getSelectedItem();
		livingEntity.equipmentMainHand = equipmentMainHand.getBlock();
		livingEntity.equipmentOffHand = equipmentOffHand.getBlock();
		livingEntity.equipmentHelmet = equipmentHelmet.getBlock();
		livingEntity.equipmentBody = equipmentBody.getBlock();
		livingEntity.equipmentLeggings = equipmentLeggings.getBlock();
		livingEntity.equipmentBoots = equipmentBoots.getBlock();
		livingEntity.mobBehaviourType = (String) mobBehaviourType.getSelectedItem();
		livingEntity.mobCreatureType = (String) mobCreatureType.getSelectedItem();
		livingEntity.attackStrength = (int) attackStrength.getValue();
		livingEntity.attackKnockback = (double) attackKnockback.getValue();
		livingEntity.knockbackResistance = (double) knockbackResistance.getValue();
		livingEntity.movementSpeed = (double) movementSpeed.getValue();
		livingEntity.health = (int) health.getValue();
		livingEntity.trackingRange = (int) trackingRange.getValue();
		livingEntity.immuneToFire = immuneToFire.isSelected();
		livingEntity.immuneToArrows = immuneToArrows.isSelected();
		livingEntity.immuneToFallDamage = immuneToFallDamage.isSelected();
		livingEntity.immuneToCactus = immuneToCactus.isSelected();
		livingEntity.immuneToDrowning = immuneToDrowning.isSelected();
		livingEntity.immuneToLightning = immuneToLightning.isSelected();
		livingEntity.immuneToPotions = immuneToPotions.isSelected();
		livingEntity.immuneToPlayer = immuneToPlayer.isSelected();
		livingEntity.immuneToExplosion = immuneToExplosion.isSelected();
		livingEntity.immuneToTrident = immuneToTrident.isSelected();
		livingEntity.immuneToAnvil = immuneToAnvil.isSelected();
		livingEntity.immuneToWither = immuneToWither.isSelected();
		livingEntity.immuneToDragonBreath = immuneToDragonBreath.isSelected();
		livingEntity.xpAmount = (int) xpAmount.getValue();
		livingEntity.ridable = ridable.isSelected();
		livingEntity.canControlForward = canControlForward.isSelected();
		livingEntity.canControlStrafe = canControlStrafe.isSelected();
		livingEntity.mobDrop = mobDrop.getBlock();
		livingEntity.livingSound = livingSound.getSound();
		livingEntity.hurtSound = hurtSound.getSound();
		livingEntity.deathSound = deathSound.getSound();
		livingEntity.stepSound = stepSound.getSound();
		livingEntity.spawnParticles = spawnParticles.isSelected();
		livingEntity.particleToSpawn = new Particle(mcreator.getWorkspace(), particleToSpawn.getSelectedItem());
		livingEntity.particleSpawningShape = (String) particleSpawningShape.getSelectedItem();
		livingEntity.particleSpawningRadious = (double) particleSpawningRadious.getValue();
		livingEntity.particleAmount = (int) particleAmount.getValue();
		livingEntity.particleCondition = particleCondition.getSelectedProcedure();
		livingEntity.spawningCondition = spawningCondition.getSelectedProcedure();
		livingEntity.onStruckByLightning = onStruckByLightning.getSelectedProcedure();
		livingEntity.whenMobFalls = whenMobFalls.getSelectedProcedure();
		livingEntity.whenMobDies = whenMobDies.getSelectedProcedure();
		livingEntity.whenMobIsHurt = whenMobIsHurt.getSelectedProcedure();
		livingEntity.onRightClickedOn = onRightClickedOn.getSelectedProcedure();
		livingEntity.whenThisMobKillsAnother = whenThisMobKillsAnother.getSelectedProcedure();
		livingEntity.onMobTickUpdate = onMobTickUpdate.getSelectedProcedure();
		livingEntity.onPlayerCollidesWith = onPlayerCollidesWith.getSelectedProcedure();
		livingEntity.onInitialSpawn = onInitialSpawn.getSelectedProcedure();
		livingEntity.hasAI = hasAI.isSelected();
		livingEntity.aiBase = (String) aiBase.getSelectedItem();
		livingEntity.aixml = blocklyPanel.getXML();
		livingEntity.breedable = breedable.isSelected();
		livingEntity.tameable = tameable.isSelected();
		livingEntity.breedTriggerItems = breedTriggerItems.getListElements();
		livingEntity.ranged = ranged.isSelected();
		livingEntity.rangedAttackItem = rangedAttackItem.getBlock();
		livingEntity.spawnThisMob = spawnThisMob.isSelected();
		livingEntity.doesDespawnWhenIdle = doesDespawnWhenIdle.isSelected();
		livingEntity.spawningProbability = (int) spawningProbability.getValue();
		livingEntity.mobSpawningType = (String) mobSpawningType.getSelectedItem();
		livingEntity.rangedItemType = (String) rangedItemType.getSelectedItem();
		livingEntity.minNumberOfMobsPerGroup = (int) minNumberOfMobsPerGroup.getValue();
		livingEntity.maxNumberOfMobsPerGroup = (int) maxNumberOfMobsPerGroup.getValue();
		livingEntity.restrictionBiomes = restrictionBiomes.getListElements();
		livingEntity.spawnInDungeons = spawnInDungeons.isSelected();
		livingEntity.modelWidth = (double) modelWidth.getValue();
		livingEntity.modelHeight = (double) modelHeight.getValue();
		livingEntity.mountedYOffset = (double) mountedYOffset.getValue();
		livingEntity.modelShadowSize = (double) modelShadowSize.getValue();
		livingEntity.armorBaseValue = (double) armorBaseValue.getValue();
		livingEntity.mobModelName = ((Model) Objects.requireNonNull(mobModel.getSelectedItem())).getReadableName();
		livingEntity.waterMob = waterMob.isSelected();
		livingEntity.flyingMob = flyingMob.isSelected();
		livingEntity.creativeTab = new TabEntry(mcreator.getWorkspace(), creativeTab.getSelectedItem());
		livingEntity.inventorySize = (int) inventorySize.getValue();
		livingEntity.inventoryStackSize = (int) inventoryStackSize.getValue();
		livingEntity.guiBoundTo = (String) guiBoundTo.getSelectedItem();
		return livingEntity;
	}

	@Override public @Nullable URI contextURL() throws URISyntaxException {
		return new URI(MCreatorApplication.SERVER_DOMAIN + "/wiki/how-make-mob");
	}

}