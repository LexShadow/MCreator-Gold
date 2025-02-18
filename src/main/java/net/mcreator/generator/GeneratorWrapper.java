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

package net.mcreator.generator;

import net.mcreator.element.GeneratableElement;
import net.mcreator.element.NamespacedGeneratableElement;
import net.mcreator.element.RecipeType;
import net.mcreator.element.parts.Procedure;
import net.mcreator.generator.mapping.NameMapper;
import net.mcreator.util.FilenameUtilsPatched;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import net.mcreator.workspace.elements.VariableElement;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "ClassCanBeRecord" }) public class GeneratorWrapper {

	private final Generator generator;

	public GeneratorWrapper(Generator generator) {
		this.generator = generator;
	}

	public String getGeneratorBuildFileVersion() {
		return generator.getGeneratorBuildFileVersion();
	}

	public int getStartIDFor(String baseType) {
		return generator.getStartIDFor(baseType);
	}

	public String map(String rawName, String mappingMap) {
		return new NameMapper(generator.getWorkspace(), mappingMap).getMapping(rawName);
	}

	public String map(String rawName, String mappingMap, int mappingTable) {
		return new NameMapper(generator.getWorkspace(), mappingMap).getMapping(rawName, mappingTable);
	}

	public VariableElement getVariableElementByName(String elementName) {
		return generator.getWorkspace().getVariableElementByName(elementName);
	}

	public List<Procedure> procedureNamesToObjects(String proceduresString) {
		return Arrays.stream(proceduresString.split(",")).map(e -> {
			if (e.equals("null"))
				return null;
			return new Procedure(e);
		}).collect(Collectors.toList());
	}

	public RecipeType getRecipeElementType(String elementName) {
		try {
			return generator.getWorkspace().getModElementByName(getElementPlainName(elementName)).getType()
					.getRecipeType();
		} catch (Exception e) {
			generator.getLogger().warn("Failed to determine recipe type for: " + elementName, e);
			return RecipeType.NONE;
		}
	}

	public String getRegistryNameFromFullName(String elementName) {
		try {
			return generator.getWorkspace().getModElementByName(getElementPlainName(elementName)).getRegistryName();
		} catch (Exception e) {
			generator.getLogger().warn("Failed to determine recipe type for: " + elementName, e);
			return NameMapper.UNKNOWN_ELEMENT;
		}
	}

	public boolean isBlock(String elementName) {
		String ext = getElementExtension(elementName);
		if (ext.equals("helmet") || ext.equals("body") || ext.equals("legs") || ext.equals("boots") || ext.equals(
				"bucket"))
			return false;

		return this.isRecipeTypeBlockOrBucket(elementName);
	}

	public boolean isRecipeTypeBlockOrBucket(String elementName) {
		RecipeType recipeType = this.getRecipeElementType(elementName);
		return recipeType == RecipeType.BLOCK || recipeType == RecipeType.BUCKET;
	}

	public String getElementPlainName(String elementName) {
		return elementName.replace("CUSTOM:", "").replace(".block", "").replace(".helmet", "").replace(".body", "")
				.replace(".legs", "").replace(".boots", "").replace(".bucket", "");
	}

	public String getElementExtension(String elementName) {
		if (elementName.contains(".")) {
			return FilenameUtilsPatched.getExtension(elementName);
		}
		return elementName;
	}

	public String getRegistryNameForModElement(String modElement) {
		ModElement element = generator.getWorkspace().getModElementByName(modElement);
		if (element != null)
			return element.getRegistryName();

		generator.LOG.warn("Failed to determine registry name for: " + modElement);
		return NameMapper.UNKNOWN_ELEMENT;
	}

	public String getResourceLocationForModElement(String modElement) {
		ModElement element = generator.getWorkspace().getModElementByName(modElement);
		if (element != null) {
			return getResourceLocationForModElement(element);
		}

		generator.LOG.warn("Failed to determine resource location for mod element: " + modElement);
		return generator.getWorkspaceSettings().getModID() + ":" + NameMapper.UNKNOWN_ELEMENT;
	}

	public String getResourceLocationForModElement(ModElement element) {
		// check if we are dealing with namespaced element
		if (NamespacedGeneratableElement.class.isAssignableFrom(element.getType().getModElementStorageClass())) {
			GeneratableElement namespacedgeneratableemenet = element.getGeneratableElement();
			if (namespacedgeneratableemenet instanceof NamespacedGeneratableElement) {
				return ((NamespacedGeneratableElement) namespacedgeneratableemenet).getResourceLocation();
			}
		}

		// otherwise we use a normal registry name
		return generator.getWorkspaceSettings().getModID() + ":" + element.getRegistryName();
	}

	public Workspace getWorkspace() {
		return generator.getWorkspace();
	}

}
