/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link EXMARaLDAImporter}. 
 * @author Florian Zipser
 *
 */
public class EXMARaLDAImporterProperties extends PepperModuleProperties 
{
	public static final String PREFIX="exmaralda.importer.";
	
	public static final String PROP_TOKEN_TIER="salt.token";
	public static final String PROP_TOKENSEP="salt.tokenSeperator";
	public static final String PROP_TIERMERGE="salt.tierMerge";
	public static final String PROP_LAYERS_SMALL="salt.layers";
	public static final String PROP_LAYERS_BIG="salt.Layers";
	public static final String PROP_URI_ANNOTATION="salt.URIAnnotation";
	public static final String PROP_SALT_SEMANTICS_POS="saltSemantics.POS";
	public static final String PROP_SALT_SEMANTICS_LEMMA="saltSemantics.LEMMA";
	public static final String PROP_SALT_SEMANTICS_WORD="saltSemantics.WORD";
	public static final String PROP_CLEAN_MODEL= "cleanModel";
	
	public EXMARaLDAImporterProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_TOKEN_TIER, String.class, "With this property you can mark the Tier object which shall be interpreted as the one containing the tokenization and the primary text.", true));
		this.addProperty(new PepperModuleProperty<String>(PROP_TOKENSEP, String.class, "With this property you can set a single sign or a sequence of signs which shall be used between the concatenation of event values representing the primary data.",false));
		this.addProperty(new PepperModuleProperty<String>(PROP_TIERMERGE, String.class, "In EXMARaLDA there is just one value per each Event object. When mapping those data to Salt, this restriction results, that a SSpan or SToken object will be created for each Event object. To avoid this, you can mark a sequence of sets of tiers to map them to a single SSpan or SToken object in Salt.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LAYERS_SMALL, String.class, "With this property you can use the layer mechanism of Salt. This means, with this property, you can map all SToken or SSpan object caused by Event objects to an SLayer object. You can also group SToken or SSpan coming from Event objects contained in several Tier objects to the same SLayer object.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LAYERS_BIG, String.class, "With this property you can use the layer mechanism of Salt. This means, with this property, you can map all SToken or SSpan object caused by Event objects to an SLayer object. You can also group SToken or SSpan coming from Event objects contained in several Tier objects to the same SLayer object.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_URI_ANNOTATION, String.class, "With the property salt.URIAnnotation, you can mark a Tier object containing annotations, which are references in URI notation and are not simple String values. When mapping the data to Salt the resulting SAnnotation.sValue objects will be typed as URI values, so that each interpreting tool can interpret them as references.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_POS, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_LEMMA, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SALT_SEMANTICS_WORD, String.class, "You can influence the creation of objects in Salt to have a more semantic typing when mapping data to Salt. Here we provide three properties which can be used for a closer definition or typing of SAnnotation, SToken or SSpan objects conform to ISOCat1. This can be important in the case of a further processing with Pepper. Some modules exist, which only can deal with semantical enriched data for example they need a special kind of annotation like part-of-speech for their processing.", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CLEAN_MODEL, Boolean.class, "....", false, false));
	}
	
	public String getTokenTier()
	{
		return((String)this.getProperty(PROP_TOKEN_TIER).getValue());
	}
	public String getTokenSeparator()
	{
		return((String)this.getProperty(PROP_TOKENSEP).getValue());
	}
	public String getTierMerge()
	{
		return((String)this.getProperty(PROP_TIERMERGE).getValue());
	}
	public String getLayers()
	{
		String retval= (String)this.getProperty(PROP_LAYERS_SMALL).getValue();
		if (retval== null)
			retval= (String)this.getProperty(PROP_LAYERS_BIG).getValue();
		return((String)this.getProperty(PROP_LAYERS_SMALL).getValue());
	}
	public String getURIAnnotation()
	{
		return((String)this.getProperty(PROP_URI_ANNOTATION).getValue());
	}
	public String getPOS()
	{
		return((String)this.getProperty(PROP_SALT_SEMANTICS_POS).getValue());
	}
	public String getLemma()
	{
		return((String)this.getProperty(PROP_SALT_SEMANTICS_LEMMA).getValue());
	}
	public String getWord()
	{
		return((String)this.getProperty(PROP_SALT_SEMANTICS_WORD).getValue());
	}
	public Boolean getCleanModel()
	{
		return((Boolean)this.getProperty(PROP_CLEAN_MODEL).getValue());
	}
}