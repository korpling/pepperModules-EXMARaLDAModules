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

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.CommonTimeLine;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Speaker;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.UDInformation;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.MAPPING_RESULT;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperModuleProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SAudioDSRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SAudioDataSource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimeline;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimelineRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SWordAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SaltSemanticsFactory;

/**
 * This class maps data coming from the EXMARaLDA (EXB) model to a Salt model.
 * There are some properties to influence the mapping:
 * salt.Layers= {layerName1{tierName1, tierName2,...}}, {layerName2{tierName3, tierName4,...}} 
 * With this property you can map some tiers of EXMARaLDA to one SLAyer object. As you can see, the value 
 * of this is a list of pairs consisting of the one layerName (name of the SLayer-object) and a list of
 * tiers (Tier-objects). All the events of the tier objects, mapped to an SNode will be added the 
 * SLayer-object. 
 * 
 * @author Florian Zipser
 *
 */
public class EXMARaLDA2SaltMapper extends PepperMapperImpl implements PepperMapper
{
// -------------------- basic transcription	
	private BasicTranscription basicTranscription= null;
	
	public void setBasicTranscription(BasicTranscription basicTranscription) {
		this.basicTranscription = basicTranscription;
	}

	public BasicTranscription getBasicTranscription() {
		return basicTranscription;
	}

	/** casts {@link PepperModuleProperties} to {@link EXMARaLDAImporterProperties} **/
	public EXMARaLDAImporterProperties getProps() {
		return((EXMARaLDAImporterProperties)this.getProperties());
	}
	
	private static final String DEFAULT_PRIMTEXT_TYPENAME = "t";
	
	/**
	 * Relates the name of the tiers to the layers, to which they shall be append.
	 */
	private Hashtable<String, SLayer> tierNames2SLayers= null;
	
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String TIER_NAME_DESC="(_|-|[A-Z]|[a-z]|[0-9])+";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String SIMPLE_TIER_LIST_DESC= "\\{"+TIER_NAME_DESC+"(,\\s?"+TIER_NAME_DESC+")*"+"\\}";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String LAYER_NAME_DESC="(_|-|[A-Z]|[a-z]|[0-9])+";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String SIMPLE_LAYER_DESC= "\\{"+LAYER_NAME_DESC+SIMPLE_TIER_LIST_DESC+"\\}";
	/**
	 * String for regex for for tier to layer mapping
	 */
	private static final String LAYER_DESC= SIMPLE_LAYER_DESC+"(,"+SIMPLE_LAYER_DESC+")*";
	
	/**
	 * Checks the given properties, if the necessary ones are given and creates
	 * the data-structures being needed to store the properties.
	 * Throws an exception, if the needed properties are not there.
	 */
	private void checkProperties()
	{
		String tokLayer= this.getProps().getTokenTier();
		
		if(tokLayer != null)
		{
			// check that no empty token layer was given
			if(tokLayer.trim().length() == 0)
			{
				this.getLogService().log(LogService.LOG_WARNING, "\"" 
						+ EXMARaLDAImporterProperties.PROP_TOKEN_TIER + "\" property is empty");
			} 
		}
		
		{//tiers to SLayer-objects
			String tier2SLayerStr=null;
			tier2SLayerStr= this.getProps().getLayers();
			if (	(tier2SLayerStr!= null) &&
					(!tier2SLayerStr.trim().isEmpty()))
			{//if a tier to layer mapping is given
				{//check if number of closing brackets is identical to number of opening brackets
					char[] tier2SLayerChar= tier2SLayerStr.toCharArray();
					int numberOfOpeningBrackets= 0;
					int numberOfClosingBrackets= 0;
					for (int i= 0; i< tier2SLayerChar.length; i++)
					{
						if (tier2SLayerChar.equals('{'))
							numberOfOpeningBrackets++;
						else if (tier2SLayerChar.equals('}'))
							numberOfClosingBrackets++;
					}
					if (numberOfClosingBrackets!= numberOfOpeningBrackets)
						throw new EXMARaLDAImporterException("Cannot import the given data, because property file contains a corrupt value for property '"+EXMARaLDAImporterProperties.PROP_LAYERS_BIG+"'. Please check the breckets you used.");
				}//check if number of closing brackets is identical to number of opening brackets
				this.tierNames2SLayers= new Hashtable<String, SLayer>();
				tier2SLayerStr= tier2SLayerStr.replace(" ", "");
				Pattern pattern= Pattern.compile(SIMPLE_LAYER_DESC, Pattern.CASE_INSENSITIVE);
				Matcher matcher= pattern.matcher(tier2SLayerStr);
				while (matcher.find())
				{//find all simple layer descriptions
					String[] tierNames= null;
					String tierNameList= null;
					Pattern pattern1= Pattern.compile(SIMPLE_TIER_LIST_DESC, Pattern.CASE_INSENSITIVE);
					Matcher matcher1= pattern1.matcher(matcher.group());
					while (matcher1.find())
					{//find all tier lists
						tierNameList= matcher1.group();
						tierNames= tierNameList.replace("}", "").replace("{", "").split(",");
					}//find all tier lists
					String sLayerName= matcher.group().replace(tierNameList, "").replace("}", "").replace("{", "");
					SLayer sLayer = SaltFactory.eINSTANCE.createSLayer();
					sLayer.setSName(sLayerName);
					this.getSDocument().getSDocumentGraph().getSLayers().add(sLayer);
					for (String tierName: tierNames)
					{//put all tiernames in table to map them to the layer
						this.tierNames2SLayers.put(tierName, sLayer);
					}//put all tiernames in table to map them to the layer
				}//if a tier to layer mapping is given
				
				if (this.tierNames2SLayers.size()== 0)
				{
					if (this.getLogService()!= null)
						this.getLogService().log(LogService.LOG_WARNING, "It seems as if there is a syntax failure in the given special-param file in property '"+EXMARaLDAImporterProperties.PROP_LAYERS_BIG+"'. A value is given, but the layers to named could not have been extracted.");
				}
			}//find all simple layer descriptions
		}//tiers to SLayer-objects
	}
	
	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public MAPPING_RESULT mapSDocument() {
		if (this.getSDocument().getSDocumentGraph()== null)
			this.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		this.getSDocument().getSDocumentGraph().setSId(this.getSDocument().getSId());
		this.checkProperties();
		this.mapDocument(this.getSDocument(), this.getBasicTranscription());
		return(MAPPING_RESULT.FINISHED);
	}
	
	/**
	 * Checks a given EXMARalDA model given by the {@link BasicTranscription} object if it is valid to be mapped to a Salt
	 * model. Some problems occuring while mapping will be solved in this step.
	 * Here is a list of problems and solutions if exist:
	 * <ul>
	 * 	<li>Given two or more {@link TLI} objects having no corresponding {@link Event} object on token tier: Create artificial {@link Event} objects, having empty values.</li>
	 * </ul> 
	 * @param basicTranscription the model to be checked
	 * @param tokenTiers a list of tiers representing the token layer
	 */
	public void cleanModel(BasicTranscription basicTranscription, Collection<Tier> tokenTiers)
	{
		for (Tier tokenTier: tokenTiers)
		{
			if (tokenTier.getEvents().size() < basicTranscription.getCommonTimeLine().getTLIs().size()-1)
			{//if size of events is smaller, than size of tlis -1, events are missing.
				int insertPos= 0;
				for (TLI tli: basicTranscription.getCommonTimeLine().getTLIs())
				{//find missing token-event
					int tliPos= basicTranscription.getCommonTimeLine().getTLIs().indexOf(tli); 
					if (tliPos!= basicTranscription.getCommonTimeLine().getTLIs().size()-1)
					{//only create Event, if tli is not the last one (for the last one an Event is not necessary)	
						boolean hasEvent= false;
						for (Event event: tli.getStartingEvents())
						{
							if (tokenTier.equals(event.getTier()))
							{
								hasEvent= true;
								break;
							}
						}
						if (!hasEvent)
						{//missing event found
							Event event= ExmaraldaBasicFactory.eINSTANCE.createEvent();
							event.setValue("");
							event.setStart(tli);
							int endTliPos= basicTranscription.getCommonTimeLine().getTLIs().indexOf(tli)+1;
							event.setEnd(basicTranscription.getCommonTimeLine().getTLIs().get(endTliPos));
							tokenTier.getEvents().add(insertPos, event);
						}//missing event found
						insertPos++;
					}//only create Event, if tli is not the last one (for the last one an Event is not necessary)
				}//find missing token-event
			}//if size of events is smaller, than size of tlis -1, events are missing.
		}
	}
	
	public void mapDocument(SDocument sDoc, BasicTranscription basicTranscription)
	{
		if (	(basicTranscription!= null)&&
				(sDoc!= null))
		{
			this.setBasicTranscription(basicTranscription);
			this.setSDocument(sDoc);
			if (sDoc.getSDocumentGraph()== null)
				sDoc.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
			{//compute collection of tiers which belong together
				this.computeTierCollection();
			}//compute collection of tiers which belong together
			{//mapping MetaInformation
				this.mapMetaInformation2SDocument(basicTranscription, sDoc);
			}//mapping MetaInformation
			{//mapping the speakers object
				for (Speaker speaker: basicTranscription.getSpeakertable())
				{//map all speaker objects
					this.mapSpeaker2SMetaAnnotation(speaker, sDoc);
				}//map all speaker objects
			}//mapping the speakers object
			{//mapping the timeline
				STimeline sTimeline= SaltFactory.eINSTANCE.createSTimeline();
				sDoc.getSDocumentGraph().setSTimeline(sTimeline);
				this.mapCommonTimeLine2STimeine(basicTranscription.getCommonTimeLine(), sTimeline);
			}//mapping the timeline	
			{//mapping text and tokens
			
				Set<String> tokenTiers = new LinkedHashSet<String>();
				if(this.getProps().getTokenTier()!=null)
				{
					String rawTokenText = this.getProps().getTokenTier();
					if(rawTokenText.startsWith("{"))
					{
						rawTokenText = rawTokenText.replace("{", "").replace("}", "");
						String[] splitted = rawTokenText.split(",");
						for(String s : splitted)
						{
							tokenTiers.add(s.trim());
						}
					}
					else
					{
						tokenTiers.add(rawTokenText.trim());
					}
				}
			
				Map<Tier, EList<Tier>> allTextSlots = new LinkedHashMap<Tier, EList<Tier>>();
				
				for (EList<Tier> slot: this.tierCollection)
				{
					Tier eTextTier= null;
					
					for (Tier tier: slot)
					{//search for textual source
						if (tokenTiers.size() > 0 &&
							tokenTiers.contains(tier.getCategory().trim())
						)						
						{
							eTextTier= tier;
							break;
						}
						else if(tier.getType().getName().trim().equalsIgnoreCase(DEFAULT_PRIMTEXT_TYPENAME))
						{
							eTextTier = tier;
							break;
						}
					} // end for each tier in slot
					
					if (eTextTier!= null)
					{
						allTextSlots.put(eTextTier, slot);
					}
					
				} // end for each slot of tierCollection
				if (allTextSlots.size() == 0)
					throw new EXMARaLDAImporterException("Cannot convert given exmaralda file '"+this.getResourceURI()+"', because no textual source layer was found.");
				
				if("true".equalsIgnoreCase(getProps().getCleanModel().toString()))
				{
					//run clean model
					cleanModel(basicTranscription, allTextSlots.keySet());
				}
				
				// map each text slot
				for(Map.Entry<Tier, EList<Tier>> entry : allTextSlots.entrySet())
				{
					Tier eTextTier = entry.getKey();
					EList<Tier> textSlot = entry.getValue();
					
					STextualDS sTextDS= SaltFactory.eINSTANCE.createSTextualDS();
					sTextDS.setSName(eTextTier.getCategory());
					sDoc.getSDocumentGraph().addSNode(sTextDS);
					this.mapTier2STextualDS(eTextTier, sTextDS, textSlot);	
				}
				
				//remove all text-slots as processed
				this.tierCollection.removeAll(allTextSlots.values());
				
			}//mapping text and tokens
			{// map other tiers
				for (EList<Tier> slot: this.tierCollection)
				{
					this.mapTiers2SNodes(slot);
				}
			}// map other tiers
		}
	}
	
	/**
	 * Maps all metaInformation objects to SMetaAnnotation objects and adds them to the given 
	 * SDocument object. Also UDInformation-objects will be mapped.
	 * If a {@link BasicTranscription} object contains the not empty attribute referencedFile, a {@link SAudioDataSource} will
	 * be created containing the given {@link URI}.
	 * @param basicTranscription
	 * @param sDoc
	 */
	private void mapMetaInformation2SDocument(BasicTranscription basicTranscription, SDocument sDoc)
	{
		if (	(basicTranscription.getMetaInformation().getProjectName()!= null) &&
				(!basicTranscription.getMetaInformation().getProjectName().isEmpty()))
		{//project name
			sDoc.setSName(basicTranscription.getMetaInformation().getProjectName());
		}
		if (	(basicTranscription.getMetaInformation().getTranscriptionName()!= null) &&
				(!basicTranscription.getMetaInformation().getTranscriptionName().isEmpty()))
		{//transcription name
			SMetaAnnotation sMetaAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getTranscriptionName());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}
		
		if (basicTranscription.getMetaInformation().getReferencedFile()!= null)
		{//map referencedFile to SAudioDataSource
			File audioFile= new File(basicTranscription.getMetaInformation().getReferencedFile().getFile());
			if (!audioFile.exists())
			{
				if (this.getLogService()!= null)
					this.getLogService().log(LogService.LOG_WARNING, "The file refered in exmaralda model '"+audioFile.getAbsolutePath()+"' does not exist and cannot be mapped to a salt model. It will be ignored.");
			}
			else
			{
				SAudioDataSource sAudioDS= SaltFactory.eINSTANCE.createSAudioDataSource();
				sAudioDS.setSAudioReference(URI.createFileURI(audioFile.getAbsolutePath()));
				sDoc.getSDocumentGraph().addSNode(sAudioDS);
			}
		}//map referencedFile to SAudioDataSource
		if (	(basicTranscription.getMetaInformation().getComment()!= null) &&
				(!basicTranscription.getMetaInformation().getComment().isEmpty()))
		{//comment
			SMetaAnnotation sMetaAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_COMMENT);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getComment());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}
		if (	(basicTranscription.getMetaInformation().getTranscriptionConvention()!= null) &&
				(!basicTranscription.getMetaInformation().getTranscriptionConvention().isEmpty()))
		{//project transcription convention
			SMetaAnnotation sMetaAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getTranscriptionConvention());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}
		if (basicTranscription.getMetaInformation().getUdMetaInformations()!= null)
		{//map ud-informations
			this.mapUDInformations2SMetaAnnotatableElement(basicTranscription.getMetaInformation().getUdMetaInformations(), sDoc);
		}//map ud-informations
	}
	
	/**
	 * Maps informatios of Speaker to SMetaAnnotations and adds them to the given SDocument object.
	 * @param speaker speaker object to map
	 * @param sDocument object to add SMetaAnnotation objects
	 */
	private void mapSpeaker2SMetaAnnotation(Speaker speaker, SDocument sDocument)
	{
		if (sDocument== null)
			throw new EXMARaLDAImporterException("Exception in method 'mapSpeaker2SMetaAnnotation()'. The given SDocument-object is null. Exception occurs in file '"+this.getResourceURI()+"'.");
		if (	(speaker!= null) &&
				(speaker.getUdSpeakerInformations()!= null))
		{
			{//map abbreviation
				if (	(speaker.getAbbreviation()!= null) &&
						(!speaker.getAbbreviation().isEmpty()))
				{
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "abbreviation", speaker.getAbbreviation().toString());
				}
			}//map abbreviation
			{//map sex
				if (speaker.getSex()!= null)
				{
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "sex", speaker.getSex().toString());
				}
			}//map sex
			{//language used
				if (	(speaker.getLanguageUsed()!= null) &&
						(speaker.getLanguageUsed().size()> 0))
				{
					StringBuilder langUsedStr= null;
					for (String langUsed: speaker.getLanguageUsed())
					{	
						if (langUsedStr== null)
						{
							langUsedStr= new StringBuilder();
							langUsedStr.append(langUsed);
						}
						else
							langUsedStr.append(", "+ langUsed);
					}
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "languages-used", langUsedStr.toString());
				}
			}//language used
			{//map l1
				if (	(speaker.getL1()!= null) &&
						(speaker.getL1().size()> 0))
				{
					StringBuilder l1Str= null;
					for (String l1: speaker.getL1())
					{	
						if (l1Str== null)
						{
							l1Str= new StringBuilder();
							l1Str.append(l1);
						}
						else
							l1Str.append(", "+ l1);
					}
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "l1", l1Str.toString());
				}
			}//map l1
			{//map l2
				if (	(speaker.getL2()!= null) &&
						(speaker.getL2().size()> 0))
				{
					StringBuilder l2Str= null;
					for (String l2: speaker.getL2())
					{	
						if (l2Str== null)
						{	
							l2Str= new StringBuilder();
							l2Str.append(l2);
						}	
						else
							l2Str.append(", "+ l2);
					}

					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "l2", l2Str.toString());
				}
			}//map l2
			{//map comment
				if (	(speaker.getComment()!= null) &&
						(!speaker.getComment().isEmpty()))
				{
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sDocument.createSMetaAnnotation(namespace, "comment", speaker.getComment());
				}
			}//map comment
			{//map ud-informations
				for (UDInformation udInfo: speaker.getUdSpeakerInformations())
				{
					SMetaAnnotation sMetaAnno= null;
					sMetaAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
					String namespace= (speaker.getAbbreviation()!= null) ? speaker.getAbbreviation(): speaker.getId();
					sMetaAnno.setSNS(namespace);
					sMetaAnno.setSName(udInfo.getAttributeName());
					sMetaAnno.setSValue(udInfo.getValue());
					if (sDocument.getSMetaAnnotation(sMetaAnno.getQName())== null)
					{//only create the meta-annotation, if it does not still exists
						sDocument.addSMetaAnnotation(sMetaAnno);
					}//only create the meta-annotation, if it does not still exists
				}
			}//map ud-informations
		}
	}
	
	/**
	 * Mapps a list of UDInformation objects to a list of SMetaAnnotation objects and adds them to the 
	 * given sOwner.
	 * @param udInformations
	 * @param sOwner
	 */
	private void mapUDInformations2SMetaAnnotatableElement(	EList<UDInformation> udInformations, 
															SMetaAnnotatableElement sOwner)
	{
		SMetaAnnotation sMetaAnno= null;
		for (UDInformation udInfo: udInformations)
		{
			sMetaAnno= SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(udInfo.getAttributeName());
			sMetaAnno.setSValue(udInfo.getValue());
			sOwner.addSMetaAnnotation(sMetaAnno);
		}
	}
	
	/**
	 * Stores tiers, which belongs together if mode is merged.
	 */
	private EList<EList<Tier>> tierCollection= null;
	/**
	 * Compute which tiers belong together and stores it in tierCollection.
	 */
	public void computeTierCollection()
	{
		this.tierCollection= new BasicEList<EList<Tier>>();
		if (	(this.getProps().getTierMerge()!= null) &&
				(!this.getProps().getTierMerge().isEmpty()))
		{
			String[] slotStrings= this.getProps().getTierMerge().split("}");
			for (String slotString:slotStrings)
			{
				slotString = slotString.replace("{", "");
				if (!slotString.isEmpty())
				{	
					String[] tierCategories= slotString.split(",");
					//create new slot for slottedt tiers
					EList<Tier> slot= new BasicEList<Tier>();
					this.tierCollection.add(slot);
					for (String tierCat: tierCategories)
					{
						tierCat= tierCat.trim();
						//searching for tier
						for (Tier tier: this.getBasicTranscription().getTiers())
						{
							if (tier.getCategory()== null)
								throw new EXMARaLDAImporterException("Cannot convert given exmaralda file '"+this.getResourceURI()+"', because there is a <tier> element ('id=\""+tier.getId()+"\"') without a @category attribute.");;
							if (tier.getCategory().equalsIgnoreCase(tierCat))
								slot.add(tier);
						}	
					}
				}
			}	
			for (Tier tier1: this.getBasicTranscription().getTiers())
			{//create new slots for all tiers, which does not belong to mentioned slots.
				boolean found= false;
				for (EList<Tier> slot: this.tierCollection)
				{
					for (Tier tier2: slot)
					{
						if (tier1.equals(tier2))
						{
							found= true;
							break;
						}
					}
					if (found)
						break;
				}
				if (!found)
				{//create new slot and add tier
					EList<Tier> slot= new BasicEList<Tier>();
					slot.add(tier1);
					this.tierCollection.add(slot);
				}
			}	
		}	
		else
		{//create slot for every tier
			for (Tier tier: this.getBasicTranscription().getTiers())
			{
				EList<Tier> slot= new BasicEList<Tier>();
				slot.add(tier);
				this.tierCollection.add(slot);
			}	
		}
	}
	
	
	private void mapTiers2SNodes(EList<Tier> slot)
	{			
		for (Tier tier: slot)
		{
			SLayer sLayer= null;
			if (	(this.tierNames2SLayers!= null))
			{//if current tier shall be added to a layer
				sLayer= this.tierNames2SLayers.get(tier.getCategory());
			}//if current tier shall be added to a layer
			
			int eventCtr= 0;
			for (Event eEvent: tier.getEvents())
			{
				eventCtr++;
				SSpan sSpan= SaltFactory.eINSTANCE.createSSpan();
				this.sDocument.getSDocumentGraph().addSNode(sSpan);
				this.mapEvent2SNode(tier, eEvent, sSpan);
				
				if (sLayer!= null)
				{//if current tier shall be added to a layer, than add sSpan to SLayer
					sSpan.getSLayers().add(sLayer);
				}//if current tier shall be added to a layer, than add sSpan to SLayer
				
				//creating semanticalAnnotation for token
				mapSStructuredNode2SemanticAnnotation(tier, sSpan);	
				
				this.mapUDInformations2SMetaAnnotatableElement(eEvent.getUdInformations(), sSpan);
				
				Integer startPos= getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getStart());
				Integer endPos= getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getEnd());
				SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
				sequence.setSStart(startPos);
				sequence.setSEnd(endPos);
				sequence.setSSequentialDS(getSDocument().getSDocumentGraph().getSTimeline());
				EList<SToken>  sTokens=this.sDocument.getSDocumentGraph().getSTokensBySequence(sequence);
				
				if (sTokens== null)
				{	
					throw new EXMARaLDAImporterException(
							"There are no matching tokens found on token-tier "
							+ "for current tier: '"+ tier.getCategory() 
							+"' in event starting at '"+eEvent.getStart()+"' and ending at '"+eEvent.getEnd()+"' having the value '"
							+ eEvent.getValue()+"'. Exception occurs in file '"
							+this.getResourceURI()
							+"'. You can try to set the property \"cleanModel\" to \"true\".");
				}
				for (SToken sToken: sTokens)
				{
					SSpanningRelation spanRel= SaltFactory.eINSTANCE.createSSpanningRelation();
					spanRel.setSSpan(sSpan);
					spanRel.setSToken(sToken);
					this.getSDocument().getSDocumentGraph().addSRelation(spanRel);
				}
				
				//medium and url to SAnnotation
				this.mapMediumURL2SSNode(eEvent, sSpan);
			}
		}
	}
		
	/**
	 * Maps the medium and url of an event and maps it to SAnnotations of an SNode.
	 * @param event
	 * @param sNode
	 */
	private void mapMediumURL2SSNode(Event event, SNode sNode)
	{//medium and url to SAnnotation
		SAnnotation sAnno= null;
		{//mapMedium to SAnnotation
			if (event.getMedium()!= null)
			{	
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				sAnno.setSName(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM);
				sAnno.setSValue(event.getMedium());
				sNode.addSAnnotation(sAnno);
			}
		}//mapMedium to SAnnotation
		{//mapURL to SAnnotation
			if (event.getUrl()!= null)
			{
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				sAnno.setSName(EXBNameIdentifier.KW_EXB_EVENT_URL);
				sAnno.setSValue(event.getUrl());
				sNode.addSAnnotation(sAnno);
			}
		}//mapURL to SAnnotation
	}
	
	/**
	 * Maps the common timeline to STimeline.
	 * @param eTimeLine
	 * @param sTimeLine
	 */
	public void mapCommonTimeLine2STimeine(CommonTimeLine eTimeLine, STimeline sTimeLine)
	{
		for (TLI tli: eTimeLine.getTLIs())
		{
			if (tli.getTime()== null)
				sTimeLine.addSPointOfTime("");
			else sTimeLine.addSPointOfTime(tli.getTime());
		}	
	}
	
	/**
	 * Creates the relation between token and timeline and textualDS.
	 * @param eCTimeline
	 * @param eEvent
	 * @param sToken
	 * @param sTime
	 */
	public void mapEvent2SToken(	Event eEvent, 
									CommonTimeLine eCTimeline,
									SToken sToken, 
									STimeline sTime)
	{
		{//create relation to timeline
			
			//search position of tli in CommonTimeLine 
			TLI startTli= eEvent.getStart();
			TLI endTli= eEvent.getEnd();
			Integer startPos= null;
			Integer endPos= null;
			int i= 0;
			for (TLI tli: eCTimeline.getTLIs())
			{
				if (tli.equals(startTli))
					startPos= i;
				if (tli.equals(endTli))
					endPos= i;
				if (startPos!= null && endPos!= null)
					break;
				i++;
			}	
			STimelineRelation sTimeRel= SaltFactory.eINSTANCE.createSTimelineRelation();
			sTimeRel.setSTimeline(sTime);
			sTimeRel.setSToken(sToken);
			sTimeRel.setSStart(startPos);
			sTimeRel.setSEnd(endPos);
			this.getSDocument().getSDocumentGraph().addSRelation(sTimeRel);
		}
	}
	
	/**
	 * Maps a tier to STextualDS, creates SToken objects and relates them by STextualRelation 
	 * to STextualDS.
	 * @param sText
	 * @param eTextTier
	 */
	public void mapTier2STextualDS(Tier eTextTier, STextualDS sText, EList<Tier> textSlot)
	{
		StringBuilder text= new StringBuilder();
		int start= 0;
		int end= 0; 
		for (Event event: eTextTier.getEvents())
		{
			start= text.length();
			String eventValue= event.getValue();
			if (eventValue!= null)
				text.append(event.getValue());
			end= text.length();			
			String sep= this.getTokenSepearator();
			if (	(eventValue!= null)&&
					(sep!= null))
				text.append(sep);
			//creating and adding token
			SToken sToken= SaltFactory.eINSTANCE.createSToken();
			this.sDocument.getSDocumentGraph().addSNode(sToken);
			if (this.tierNames2SLayers!= null)
			{//add sToken to layer if required
				SLayer sLayer= this.tierNames2SLayers.get(eTextTier.getCategory());
				if (sLayer!= null)
				{
					sLayer.getSNodes().add(sToken);
				}
			}//add sToken to layer if required
			//creating annotation for token
			if (event.getUdInformations()!= null);
				this.mapUDInformations2SMetaAnnotatableElement(event.getUdInformations(), sToken);
			//creating semanticalAnnotation for token
			mapSStructuredNode2SemanticAnnotation(eTextTier, sToken);	
			//medium and url to SAnnotation
			this.mapMediumURL2SSNode(event, sToken);
			
			//creating textual relation
			STextualRelation sTextRel= SaltFactory.eINSTANCE.createSTextualRelation();
			sTextRel.setSTextualDS(sText);
			sTextRel.setSToken(sToken);
			sTextRel.setSStart(start);
			sTextRel.setSEnd(end);
			sDocument.getSDocumentGraph().addSRelation(sTextRel);
			
			if (	(sDocument.getSDocumentGraph().getSAudioDataSources()!= null)&&
					(sDocument.getSDocumentGraph().getSAudioDataSources().size()> 0)&&
					(	(event.getStart().getTime()!= null)||
						(event.getEnd().getTime()!= null)))
			{//start: creating SAudioDSRelation
				try {
					Double audioStart= null;
					if (event.getStart().getTime()!= null) 
						audioStart= Double.valueOf(event.getStart().getTime());
					Double audioEnd= null;
					if (event.getEnd().getTime()!= null)
						audioEnd= Double.valueOf(event.getEnd().getTime());
					
					SAudioDSRelation sAudioDSRelation= SaltFactory.eINSTANCE.createSAudioDSRelation();
					sAudioDSRelation.setSToken(sToken);
					sAudioDSRelation.setSAudioDS(sDocument.getSDocumentGraph().getSAudioDataSources().get(0));
					sAudioDSRelation.setSStart(audioStart);
					sAudioDSRelation.setSEnd(audioEnd);
					sDocument.getSDocumentGraph().addSRelation(sAudioDSRelation);
				} catch (NumberFormatException e) {
					if (this.getLogService()!= null)
						this.getLogService().log(LogService.LOG_WARNING, "Cannot map time attribute of timeline to SStart or SEnd, because value '"+event.getStart().getTime()+"' is not mappable to a double value.");
				}
			}//end: creating SAudioDSRelation
			
			//creating timelineRel
			this.mapEvent2SToken(event, this.getBasicTranscription().getCommonTimeLine(), sToken, this.sDocument.getSDocumentGraph().getSTimeline());
		}	
		sText.setSText(text.toString());
		for (Tier tier: textSlot)
		{
			if (!tier.equals(eTextTier))
			{//if tier is annotation tier
				for (Event event: tier.getEvents())
				{
					Integer startPos= getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getStart());
					Integer endPos= getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getEnd());
					
					SDataSourceSequence sequence= SaltFactory.eINSTANCE.createSDataSourceSequence();
					sequence.setSStart(startPos);
					sequence.setSEnd(endPos);
					sequence.setSSequentialDS(getSDocument().getSDocumentGraph().getSTimeline());
					EList<SToken>  sTokens=this.sDocument.getSDocumentGraph().getSTokensBySequence(sequence);
					
					if (sTokens!= null)
					{	
						for (SToken sToken: sTokens)
						{//create for every token an annotation for the tiers
							this.mapEvent2SNode(tier, event, sToken);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Creates a new annotation with the given event on sNode.
	 * @param eEvent
	 * @param sNode
	 */
	public void mapEvent2SNode(Tier tier, Event eEvent, SNode sNode)
	{
		SAnnotation sAnno= null;
		String posTier= this.getProps().getPOS();
		String lemmaTier= this.getProps().getLemma();
		String preUriTiers= this.getProps().getURIAnnotation();
		EList<String> uriTiers= null;
		if (	(preUriTiers!= null) && 
				(!preUriTiers.isEmpty()))
		{
			uriTiers= new BasicEList<String>();
			for (String uriTier: preUriTiers.trim().split(","))
				uriTiers.add(uriTier); 
		}
		
		if (tier.getCategory().equalsIgnoreCase(posTier))
		{//this tier has special annotations: pos annotations	
			sAnno= SaltSemanticsFactory.eINSTANCE.createSPOSAnnotation();
			sAnno.setSValue(eEvent.getValue());
		}
		else if (tier.getCategory().equalsIgnoreCase(lemmaTier))
		{//this tier has special annotations: lemma annotations	
			sAnno= SaltSemanticsFactory.eINSTANCE.createSLemmaAnnotation();
			sAnno.setSValue(eEvent.getValue());
		}
		else if (	(uriTiers!= null)&&
					(uriTiers.contains(tier.getCategory())))
		{
			String pathName= this.getResourceURI().toFileString().replace(this.getResourceURI().lastSegment(),eEvent.getValue());
			File file= new File(pathName);
			if (!file.exists())
				this.getLogService().log(LogService.LOG_WARNING, "Cannot add the uri-annotation '"+eEvent.getValue()+"' of tier '"+tier.getCategory()+"', because the file '"+pathName+"' does not exist.");
			else
			{	
				URI corpusFilePath=  URI.createFileURI(file.getAbsolutePath());
				sAnno= SaltFactory.eINSTANCE.createSAnnotation();
				sAnno.setSName(tier.getCategory());
				sAnno.setSValue(corpusFilePath);
			}
		}
		else
		{	
			sAnno= SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(tier.getCategory());
			sAnno.setSValue(eEvent.getValue());
		}
		if (	(eEvent.getUdInformations() != null) &&
				(eEvent.getUdInformations().size() > 0))
			this.mapUDInformations2SMetaAnnotatableElement(eEvent.getUdInformations(), sNode);
		sNode.addSAnnotation(sAnno);
	}
	
	/**
	 * Creates additional semantic annotations to given node if necessary. E.g. marking the
	 * given node as Word.
	 * @param tier
	 * @param sStructuredNode
	 */
	private void mapSStructuredNode2SemanticAnnotation(Tier tier, SStructuredNode sStructuredNode)
	{
		//check if tier is word tier
		if (tier.getCategory().equalsIgnoreCase(this.getSWordTier()))
		{
			SWordAnnotation sWordAnno= SaltSemanticsFactory.eINSTANCE.createSWordAnnotation();
			sStructuredNode.addSAnnotation(sWordAnno);
		}
	}
	
	private String getTokenSepearator()
	{
		String retVal= null;
		if (	(this.getProps().getTokenSeparator()!= null) &&
				(!this.getProps().getTokenSeparator().isEmpty()))
		{
			String preSep= this.getProps().getTokenSeparator();
			
			if (preSep.length() > 2)
			{//seperatorString has to be larger than 2, because of the form " "
				preSep= preSep.replace("\"", "");
				retVal= preSep;
			}	
		}	
		return(retVal);
	}
	
	/**
	 * Returns the category name of tier, which has to be annoted additional as Word.
	 * @return
	 */
	private String getSWordTier()
	{
		String retVal= null;
		if (	(this.getProps().getWord()!= null) &&
				(!this.getProps().getWord().isEmpty()))
		{
			String wordTier= this.getProps().getWord();
			
			if (wordTier.length() > 2)
			{//wordTier has to be larger than 2, because of the form " "
				wordTier= wordTier.replace("\"", "");
				retVal= wordTier;
			}	
		}	
		return(retVal);
	}
}
