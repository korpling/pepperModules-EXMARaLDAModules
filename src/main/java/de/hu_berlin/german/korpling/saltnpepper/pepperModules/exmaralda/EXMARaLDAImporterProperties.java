/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

/**
 * Defines the properties to be used for the {@link EXMARaLDAImporter}.
 * 
 * @author Florian Zipser
 *
 */
public class EXMARaLDAImporterProperties extends PepperModuleProperties {
	private static final Logger logger = LoggerFactory.getLogger(EXMARaLDAImporter.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 3941331601854796510L;

	public static final String PREFIX = "exmaralda.importer.";

	@Deprecated
	public static final String PROP_TOKEN_TIER = "salt.token";
	public static final String PROP_TOKENSEP = "salt.tokenSeparator";
	public static final String PROP_TIERMERGE = "salt.tierMerge";
	public static final String PROP_LAYERS_SMALL = "salt.layers";
	public static final String PROP_LAYERS_BIG = "salt.Layers";
	public static final String PROP_URI_ANNOTATION = "salt.URIAnnotation";
	public static final String PROP_SALT_SEMANTICS_POS = "saltSemantics.POS";
	public static final String PROP_SALT_SEMANTICS_LEMMA = "saltSemantics.LEMMA";
	public static final String PROP_SALT_SEMANTICS_WORD = "saltSemantics.WORD";
	public static final String PROP_CLEAN_MODEL = "cleanModel";

	public EXMARaLDAImporterProperties() {
		this.addProperty(new PepperModuleProperty(PROP_TOKEN_TIER, String.class, "With this property you can mark the Tier object which shall be interpreted as the one containing the tokenization and the primary text.", true));
		this.addProperty(new PepperModuleProperty<String>(PROP_TOKENSEP, String.class, "With this property you can set a single sign or a sequence of signs which shall be used between the concatenation of event values representing the primary data.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_TIERMERGE, String.class, "In EXMARaLDA there is just one value per each Event object. When mapping those data to Salt, this restriction results, that a SSpan or SToken object will be created for each Event object. To avoid this, you can mark a sequence of sets of tiers to map them to a single SSpan or SToken object in Salt.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LAYERS_SMALL, String.class, "With this property you can use the layer mechanism of Salt. This means, with this property, you can map all SToken or SSpan object caused by Event objects to an SLayer object. You can also group SToken or SSpan coming from Event objects contained in several Tier objects to the same SLayer object.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LAYERS_BIG, String.class, "With this property you can use the layer mechanism of Salt. This means, with this property, you can map all SToken or SSpan object caused by Event objects to an SLayer object. You can also group SToken or SSpan coming from Event objects contained in several Tier objects to the same SLayer object.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_URI_ANNOTATION, String.class, "With the property salt.URIAnnotation, you can mark a Tier object containing annotations, which are references in URI notation and are not simple String values. When mapping the data to Salt the resulting SAnnotation.sValue objects will be typed as URI values, so that each interpreting tool can interpret them as references.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_POS, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_LEMMA, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_WORD, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CLEAN_MODEL, Boolean.class, "....", false, false));
	}

	@Deprecated
	public Set<String> getTokenTiers() {
		String prop = ((String) this.getProperty(PROP_TOKEN_TIER).getValue());
		Set<String> tokenTiers = new LinkedHashSet<String>();
		if ((prop != null) && (!prop.isEmpty())) {
			if (prop.startsWith("{")) {
				prop = prop.replace("{", "").replace("}", "");
				String[] splitted = prop.split(",");
				for (String s : splitted) {
					tokenTiers.add(s.trim());
				}
			} else {
				tokenTiers.add(prop.trim());
			}
		}
		return (tokenTiers);
	}

	public String getTokenSeparator() {
		return ((String) this.getProperty(PROP_TOKENSEP).getValue());
	}

	public String getTierMerge() {
		return ((String) this.getProperty(PROP_TIERMERGE).getValue());
	}

	public String getURIAnnotation() {
		return ((String) this.getProperty(PROP_URI_ANNOTATION).getValue());
	}

	public String getPOS() {
		return ((String) this.getProperty(PROP_SALT_SEMANTICS_POS).getValue());
	}

	public String getLemma() {
		return ((String) this.getProperty(PROP_SALT_SEMANTICS_LEMMA).getValue());
	}

	public String getWord() {
		return ((String) this.getProperty(PROP_SALT_SEMANTICS_WORD).getValue());
	}

	public Boolean getCleanModel() {
		return ((Boolean) this.getProperty(PROP_CLEAN_MODEL).getValue());
	}

	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String TIER_NAME_DESC = "(_|-|[A-Z]|[a-z]|[0-9])+";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String SIMPLE_TIER_LIST_DESC = "\\{" + TIER_NAME_DESC + "(,\\s?" + TIER_NAME_DESC + ")*" + "\\}";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String LAYER_NAME_DESC = "(_|-|[A-Z]|[a-z]|[0-9])+";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String SIMPLE_LAYER_DESC = "\\{" + LAYER_NAME_DESC + SIMPLE_TIER_LIST_DESC + "\\}";

	/**
	 * Checks the given properties, if the necessary ones are given and creates
	 * the data-structures being needed to store the properties. Throws an
	 * exception, if the needed properties are not there.
	 * 
	 * @return Relates the name of the tiers to the layers, to which they shall
	 *         be append.
	 */
	public Map<String, SLayer> getTier2SLayers() {
		Hashtable<String, SLayer> tierNames2SLayers = new Hashtable<String, SLayer>();

		// tiers to SLayer-objects
		String tier2SLayerStr = (String) this.getProperty(PROP_LAYERS_SMALL).getValue();
		if (tier2SLayerStr == null) {
			tier2SLayerStr = (String) this.getProperty(PROP_LAYERS_BIG).getValue();
		}

		if ((tier2SLayerStr != null) && (!tier2SLayerStr.trim().isEmpty())) {
			// if a tier to layer mapping is given
			// check if number of closing brackets is identical to number of
			// opening brackets
			char[] tier2SLayerChar = tier2SLayerStr.toCharArray();
			int numberOfOpeningBrackets = 0;
			int numberOfClosingBrackets = 0;
			for (int i = 0; i < tier2SLayerChar.length; i++) {
				if (tier2SLayerChar.equals('{'))
					numberOfOpeningBrackets++;
				else if (tier2SLayerChar.equals('}'))
					numberOfClosingBrackets++;
			}
			if (numberOfClosingBrackets != numberOfOpeningBrackets) {
				throw new PepperModuleException("Cannot import the given data, because property file contains a corrupt value for property '" + EXMARaLDAImporterProperties.PROP_LAYERS_BIG + "'. Please check the breckets you used.");
			}
			tier2SLayerStr = tier2SLayerStr.replace(" ", "");
			Pattern pattern = Pattern.compile(SIMPLE_LAYER_DESC, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(tier2SLayerStr);
			while (matcher.find()) {
				// find all simple layer descriptions
				String[] tierNames = null;
				String tierNameList = null;
				Pattern pattern1 = Pattern.compile(SIMPLE_TIER_LIST_DESC, Pattern.CASE_INSENSITIVE);
				Matcher matcher1 = pattern1.matcher(matcher.group());
				while (matcher1.find()) {
					// find all tier lists
					tierNameList = matcher1.group();
					tierNames = tierNameList.replace("}", "").replace("{", "").split(",");
				}
				String sLayerName = matcher.group().replace(tierNameList, "").replace("}", "").replace("{", "");
				SLayer sLayer = SaltFactory.eINSTANCE.createSLayer();
				sLayer.setSName(sLayerName);
				for (String tierName : tierNames) {
					// put all tiernames in table to map them to the layer
					tierNames2SLayers.put(tierName, sLayer);
				}
			}

			if (tierNames2SLayers.size() == 0) {
				logger.warn("[EXMARaLDAImporter] It seems as if there is a syntax failure in the given special-param file in property '" + EXMARaLDAImporterProperties.PROP_LAYERS_BIG + "'. A value is given, but the layers to named could not have been extracted.");
			}
		}
		return (tierNames2SLayers);
	}
}
