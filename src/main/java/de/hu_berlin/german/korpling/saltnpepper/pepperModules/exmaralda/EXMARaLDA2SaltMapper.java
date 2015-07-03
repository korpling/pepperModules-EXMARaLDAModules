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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.CommonTimeLine;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Speaker;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TIER_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.UDInformation;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleDataException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.exceptions.SaltModuleException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SAudioDSRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SAudioDataSource;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSequentialRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimeline;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimelineRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotatableElement;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SWordAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SaltSemanticsFactory;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * This class maps data coming from the EXMARaLDA (EXB) model to a Salt model.
 * There are some properties to influence the mapping: salt.Layers=
 * {layerName1{tierName1, tierName2,...}}, {layerName2{tierName3,
 * tierName4,...}} With this property you can map some tiers of EXMARaLDA to one
 * SLAyer object. As you can see, the value of this is a list of pairs
 * consisting of the one layerName (name of the SLayer-object) and a list of
 * tiers (Tier-objects). All the events of the tier objects, mapped to an SNode
 * will be added the SLayer-object.
 * 
 * @author Florian Zipser
 * 
 */
public class EXMARaLDA2SaltMapper extends PepperMapperImpl implements PepperMapper {
	private static final Logger logger = LoggerFactory.getLogger(EXMARaLDAImporter.class);
	// -------------------- basic transcription
	private BasicTranscription basicTranscription = null;

	public void setBasicTranscription(BasicTranscription basicTranscription) {
		this.basicTranscription = basicTranscription;
	}

	public BasicTranscription getBasicTranscription() {
		return basicTranscription;
	}

	private ResourceSet resourceSet = null;

	public void setResourceSet(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	/**
	 * casts {@link PepperModulePropertiesImpl} to
	 * {@link EXMARaLDAImporterProperties}
	 **/
	public EXMARaLDAImporterProperties getProps() {
		return ((EXMARaLDAImporterProperties) this.getProperties());
	}

	/**
	 * Relates the name of the tiers to the layers, to which they shall be
	 * append.
	 **/
	private Map<String, SLayer> tierNames2SLayers = null;
	
	/**
	 * Maps a token to the speaker it was created for.
	 */
	private final Map<SToken, Speaker> token2Speaker = new HashMap<>();

	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (this.getSDocument().getSDocumentGraph() == null)
			this.getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());

		if (getBasicTranscription() == null) {
			// load resource
			Resource resource = getResourceSet().createResource(getResourceURI());
			if (resource == null)
				throw new PepperModuleDataException(this, "Cannot load the exmaralda file: " + getResourceURI() + ", becuase the resource is null.");
			try {
				resource.load(null);
			} catch (IOException e) {
				throw new PepperModuleDataException(this, "Cannot load the exmaralda file: " + getResourceURI() + ".", e);
			}

			BasicTranscription basicTranscription = null;
			basicTranscription = (BasicTranscription) resource.getContents().get(0);
			setBasicTranscription(basicTranscription);
		}

		addProgress(0.5);
		//TODO: creates some useful indexes to speed up computing
		//createIndexes();
		this.getSDocument().getSDocumentGraph().setSId(this.getSDocument().getSId());
		tierNames2SLayers = getProps().getTier2SLayers();
		if (tierNames2SLayers.size() > 0) {
			for (SLayer sLayer : tierNames2SLayers.values()) {
				getSDocument().getSDocumentGraph().getSLayers().add(sLayer);
			}
		}
		this.setBasicTranscription(basicTranscription);
		// compute collection of tiers which belong together
		this.computeTierCollection();
		// mapping MetaInformation
		if (basicTranscription.getMetaInformation() != null) {
			this.mapMetaInformation2SDocument(basicTranscription, getSDocument());
		}
		// mapping the speakers object
		for (Speaker speaker : basicTranscription.getSpeakertable()) {
			// map all speaker objects
			this.mapSpeaker2SMetaAnnotation(speaker, getSDocument());
		}

		// mapping the timeline
		STimeline sTimeline = SaltFactory.eINSTANCE.createSTimeline();
		getSDocument().getSDocumentGraph().setSTimeline(sTimeline);
		this.mapCommonTimeLine2STimeine(basicTranscription.getCommonTimeLine(), sTimeline);

		Set<String> tokenTiers = getProps().getTokenTiers();
		Map<Tier, List<Tier>> allTextSlots = new LinkedHashMap<Tier, List<Tier>>();
		for (List<Tier> slot : this.tierCollection) {
			Tier eTextTier = null;
			for (Tier tier : slot) {
				// search for textual source
				if (tokenTiers.size() > 0 && tokenTiers.contains(tier.getCategory().trim())) {
					eTextTier = tier;
					break;
				} else if (tier.getType().getName().trim().equalsIgnoreCase(TIER_TYPE.T.toString())) {
					eTextTier = tier;
					break;
				}
			} // end for each tier in slot

			if (eTextTier != null) {
				allTextSlots.put(eTextTier, slot);
			}

		} // end for each slot of tierCollection
		if (allTextSlots.size() == 0) {
			throw new PepperModuleDataException(this, "Cannot convert given exmaralda file '" + this.getResourceURI() + "', because no textual source layer was found.");
		}
		if ("true".equalsIgnoreCase(getProps().getCleanModel().toString())) {
			// run clean model
			cleanModel(basicTranscription, allTextSlots.keySet());
		}

		// map each text slot
		for (Map.Entry<Tier, List<Tier>> entry : allTextSlots.entrySet()) {
			Tier eTextTier = entry.getKey();
			List<Tier> textSlot = entry.getValue();

			STextualDS sTextDS = SaltFactory.eINSTANCE.createSTextualDS();
			logger.debug("[EXMARaLDAImporter] create primary data for tier '{}'.", eTextTier.getCategory());
			sTextDS.setSName(eTextTier.getCategory());
			getSDocument().getSDocumentGraph().addSNode(sTextDS);
			this.mapTier2STextualDS(eTextTier, sTextDS, textSlot);
		}

		// remove all text-slots as processed
		this.tierCollection.removeAll(allTextSlots.values());
		// map other tiers
		for (List<Tier> slot : this.tierCollection) {
			this.mapTiers2SNodes(slot);
		}
		setProgress(1.0);
		return (DOCUMENT_STATUS.COMPLETED);
	}

	/**
	 * Checks a given EXMARalDA model given by the {@link BasicTranscription}
	 * object if it is valid to be mapped to a Salt model. Some problems
	 * occuring while mapping will be solved in this step. Here is a list of
	 * problems and solutions if exist:
	 * <ul>
	 * <li>Given two or more {@link TLI} objects having no corresponding
	 * {@link Event} object on token tier: Create artificial {@link Event}
	 * objects, having empty values.</li>
	 * </ul>
	 * 
	 * @param basicTranscription
	 *            the model to be checked
	 * @param tokenTiers
	 *            a list of tiers representing the token layer
	 */
	public void cleanModel(BasicTranscription basicTranscription, Collection<Tier> tokenTiers) {
		for (Tier tokenTier : tokenTiers) {
			if (tokenTier.getEvents().size() < basicTranscription.getCommonTimeLine().getTLIs().size() - 1) {
				// if size of events is smaller, than size of tlis -1, events
				// are missing.
				int insertPos = 0;
				for (TLI tli : basicTranscription.getCommonTimeLine().getTLIs()) {
					// find missing token-event
					int tliPos = basicTranscription.getCommonTimeLine().getTLIs().indexOf(tli);
					if (tliPos != basicTranscription.getCommonTimeLine().getTLIs().size() - 1) {
						// only create Event, if tli is not the last one (for
						// the last one an Event is not necessary)
						boolean hasEvent = false;
						for (Event event : tli.getStartingEvents()) {
							if (tokenTier.equals(event.getTier())) {
								hasEvent = true;
								break;
							}
						}
						if (!hasEvent) {
							// missing event found
							Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
							event.setValue("");
							event.setStart(tli);
							int endTliPos = basicTranscription.getCommonTimeLine().getTLIs().indexOf(tli) + 1;
							event.setEnd(basicTranscription.getCommonTimeLine().getTLIs().get(endTliPos));
							tokenTier.getEvents().add(insertPos, event);
						}
						insertPos++;
					}
				}
			}
		}
	}

	/**
	 * Maps all metaInformation objects to SMetaAnnotation objects and adds them
	 * to the given SDocument object. Also UDInformation-objects will be mapped.
	 * If a {@link BasicTranscription} object contains the not empty attribute
	 * referencedFile, a {@link SAudioDataSource} will be created containing the
	 * given {@link URI}.
	 * 
	 * @param basicTranscription
	 * @param sDoc
	 */
	private void mapMetaInformation2SDocument(BasicTranscription basicTranscription, SDocument sDoc) {
		if ((basicTranscription.getMetaInformation().getProjectName() != null) && (!basicTranscription.getMetaInformation().getProjectName().isEmpty())) {
			// project name
			sDoc.setSName(basicTranscription.getMetaInformation().getProjectName());
		}
		if ((basicTranscription.getMetaInformation().getTranscriptionName() != null) && (!basicTranscription.getMetaInformation().getTranscriptionName().isEmpty())) {
			// transcription name
			SMetaAnnotation sMetaAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getTranscriptionName());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}

		if (basicTranscription.getMetaInformation().getReferencedFile() != null) {
			// map referencedFile to SAudioDataSource
			URI audioURI = URI.createURI(basicTranscription.getMetaInformation().getReferencedFile());
			File audioFile = null;
			if (audioURI != null) {
				if (("file".equals(audioURI.scheme())) && (!audioURI.scheme().isEmpty())) {
					audioFile = new File(audioURI.toFileString());
				} else {
					audioFile = new File(audioURI.toString());
				}
			}
			if ((audioFile == null) || (!audioFile.exists())) {
				logger.warn("[EXMARaLDAImporter] The file refered in exmaralda model '" + audioURI + "' does not exist and cannot be mapped to a salt model. It will be ignored.");
			} else {
				SAudioDataSource sAudioDS = SaltFactory.eINSTANCE.createSAudioDataSource();
				sAudioDS.setSAudioReference(audioURI);
				sDoc.getSDocumentGraph().addSNode(sAudioDS);
			}
		}
		if ((basicTranscription.getMetaInformation().getComment() != null) && (!basicTranscription.getMetaInformation().getComment().isEmpty())) {
			// comment
			SMetaAnnotation sMetaAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_COMMENT);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getComment());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}
		if ((basicTranscription.getMetaInformation().getTranscriptionConvention() != null) && (!basicTranscription.getMetaInformation().getTranscriptionConvention().isEmpty())) {
			// project transcription convention
			SMetaAnnotation sMetaAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION);
			sMetaAnno.setSValue(basicTranscription.getMetaInformation().getTranscriptionConvention());
			sDoc.addSMetaAnnotation(sMetaAnno);
		}
		if (basicTranscription.getMetaInformation().getUdMetaInformations() != null) {
			// map ud-informations
			this.mapUDInformations2SMetaAnnotatableElement(basicTranscription.getMetaInformation().getUdMetaInformations(), sDoc);
		}
	}

	/**
	 * Maps informatios of Speaker to SMetaAnnotations and adds them to the
	 * given SDocument object.
	 * 
	 * @param speaker
	 *            speaker object to map
	 * @param sDocument
	 *            object to add SMetaAnnotation objects
	 */
	private void mapSpeaker2SMetaAnnotation(Speaker speaker, SDocument sDocument) {
		if (sDocument == null)
			throw new PepperModuleDataException(this, "Exception in method 'mapSpeaker2SMetaAnnotation()'. The given SDocument-object is null. Exception occurs in file '" + this.getResourceURI() + "'.");
		if ((speaker != null) && (speaker.getUdSpeakerInformations() != null)) {
			// map abbreviation
			if ((speaker.getAbbreviation() != null) && (!speaker.getAbbreviation().isEmpty())) {
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "abbreviation", speaker.getAbbreviation().toString());
			}
			// map sex
			if (speaker.getSex() != null) {
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "sex", speaker.getSex().toString());
			}
			// language used
			if ((speaker.getLanguageUsed() != null) && (speaker.getLanguageUsed().size() > 0)) {
				StringBuilder langUsedStr = null;
				for (String langUsed : speaker.getLanguageUsed()) {
					if (langUsedStr == null) {
						langUsedStr = new StringBuilder();
						langUsedStr.append(langUsed);
					} else
						langUsedStr.append(", " + langUsed);
				}
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "languages-used", langUsedStr.toString());
			}
			// map l1
			if ((speaker.getL1() != null) && (speaker.getL1().size() > 0)) {
				StringBuilder l1Str = null;
				for (String l1 : speaker.getL1()) {
					if (l1Str == null) {
						l1Str = new StringBuilder();
						l1Str.append(l1);
					} else
						l1Str.append(", " + l1);
				}
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "l1", l1Str.toString());
			}
			// map l2
			if ((speaker.getL2() != null) && (speaker.getL2().size() > 0)) {
				StringBuilder l2Str = null;
				for (String l2 : speaker.getL2()) {
					if (l2Str == null) {
						l2Str = new StringBuilder();
						l2Str.append(l2);
					} else
						l2Str.append(", " + l2);
				}

				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "l2", l2Str.toString());
			}
			// map comment
			if ((speaker.getComment() != null) && (!speaker.getComment().isEmpty())) {
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sDocument.createSMetaAnnotation(namespace, "comment", speaker.getComment());
			}
			// map ud-informations
			for (UDInformation udInfo : speaker.getUdSpeakerInformations()) {
				SMetaAnnotation sMetaAnno = null;
				sMetaAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
				String namespace = (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
				sMetaAnno.setSNS(namespace);
				sMetaAnno.setSName(udInfo.getAttributeName());
				sMetaAnno.setSValue(udInfo.getValue());
				if (sDocument.getSMetaAnnotation(sMetaAnno.getQName()) == null) {
					// only create the meta-annotation, if it does not still
					// exists
					sDocument.addSMetaAnnotation(sMetaAnno);
				}
			}
		}
	}

	/**
	 * Mapps a list of UDInformation objects to a list of SMetaAnnotation
	 * objects and adds them to the given sOwner.
	 * 
	 * @param udInformations
	 * @param sOwner
	 */
	private void mapUDInformations2SMetaAnnotatableElement(List<UDInformation> udInformations, SMetaAnnotatableElement sOwner) {
		SMetaAnnotation sMetaAnno = null;
		for (UDInformation udInfo : udInformations) {
			sMetaAnno = SaltFactory.eINSTANCE.createSMetaAnnotation();
			sMetaAnno.setSName(udInfo.getAttributeName());
			sMetaAnno.setSValue(udInfo.getValue());
			sOwner.addSMetaAnnotation(sMetaAnno);
		}
	}

	/**
	 * Stores tiers, which belongs together if mode is merged.
	 */
	private List<List<Tier>> tierCollection = null;

	/**
	 * Compute which tiers belong together and stores them in tierCollection.
	 * Therefore the property {@link EXMARaLDAImporterProperties#getTierMerge()}
	 * is used.
	 */
	public void computeTierCollection() {
		this.tierCollection = new ArrayList<List<Tier>>();
		if ((this.getProps().getTierMerge() != null) && (!this.getProps().getTierMerge().isEmpty())) {
			String[] slotStrings = this.getProps().getTierMerge().split("}");
			for (String slotString : slotStrings) {
				slotString = slotString.replace("{", "");
				if (!slotString.isEmpty()) {
					String[] tierCategories = slotString.split(",");
					// create new slot for slottedt tiers
					List<Tier> slot = new ArrayList<Tier>();
					this.tierCollection.add(slot);
					for (String tierCat : tierCategories) {
						tierCat = tierCat.trim();
						// searching for tier
						for (Tier tier : this.getBasicTranscription().getTiers()) {
							if (tier.getCategory() == null)
								throw new PepperModuleDataException(this, "Cannot convert given exmaralda file '" + this.getResourceURI() + "', because there is a <tier> element ('id=\"" + tier.getId() + "\"') without a @category attribute.");
							;
							if (tier.getCategory().equalsIgnoreCase(tierCat))
								slot.add(tier);
						}
					}
				}
			}
			for (Tier tier1 : this.getBasicTranscription().getTiers()) {
				// create new slots for all tiers, which does not belong to
				// mentioned slots.
				boolean found = false;
				for (List<Tier> slot : this.tierCollection) {
					for (Tier tier2 : slot) {
						if (tier1.equals(tier2)) {
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
				if (!found) {
					// create new slot and add tier
					List<Tier> slot = new ArrayList<Tier>();
					slot.add(tier1);
					this.tierCollection.add(slot);
				}
			}
		} else {
			// create slot for every tier
			for (Tier tier : this.getBasicTranscription().getTiers()) {
				List<Tier> slot = new ArrayList<Tier>();
				slot.add(tier);
				this.tierCollection.add(slot);
			}
		}
	}

	private void mapTiers2SNodes(List<Tier> slot) {
		for (Tier tier : slot) {
			logger.debug("[EXMARaLDAImporter] mapping tier '{}'. ", tier.getCategory());
			SLayer sLayer = null;
			if ((this.tierNames2SLayers != null)) {
				// if current tier shall be added to a layer
				sLayer = this.tierNames2SLayers.get(tier.getCategory());
			}

			for (Event eEvent : tier.getEvents()) {
				SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
				getSDocument().getSDocumentGraph().addSNode(sSpan);
				this.mapEvent2SNode(tier, eEvent, sSpan);

				if (sLayer != null) {
					// if current tier shall be added to a layer, than add sSpan
					// to SLayer
					sSpan.getSLayers().add(sLayer);
				}

				// creating semanticalAnnotation for token
				mapSStructuredNode2SemanticAnnotation(tier, sSpan);

				this.mapUDInformations2SMetaAnnotatableElement(eEvent.getUdInformations(), sSpan);

				Integer startPos = getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getStart());
				if (startPos < 0) {
					if (getBasicTranscription().getCommonTimeLine().getTLIs().contains(eEvent.getStart())) {
						logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue() + "' of tier '" + tier.getCategory() + "' because its start value reffering to timeline is less than 0.");
					} else
						logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue() + "' of tier '" + tier.getCategory() + "' because this event is not connected to the timeline.");
					break;
				}
				Integer endPos = getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getEnd());
				if (endPos < 0) {
					if (getBasicTranscription().getCommonTimeLine().getTLIs().contains(eEvent.getEnd())) {
						logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue() + "' of tier " + tier.getCategory() + "' because its end value reffering to timeline is less than 0.");
					} else
						logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue() + "' of tier " + tier.getCategory() + "' because this event is not connected to the timeline.");
					break;
				}
				SDataSourceSequence sequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
				sequence.setSStart(startPos);
				sequence.setSEnd(endPos);
				sequence.setSSequentialDS(getSDocument().getSDocumentGraph().getSTimeline());

				// TODO: use an index to get overlapped token
				List<SToken> sTokens  = getAdjacentSTokens(sequence);
				
				// filter out tokens that do not belong to the right STextualDS
				ListIterator<SToken> itTokens = sTokens.listIterator();
				while(itTokens.hasNext()) {
					SToken tok = itTokens.next();
					Speaker tokSpeaker = token2Speaker.get(tok);
					if(tokSpeaker != eEvent.getTier().getSpeaker()) {
						itTokens.remove();
					}
				}
				
				if (sTokens.isEmpty()) {
					if (!getProps().getCleanModel()) {
						throw new PepperModuleDataException(this, "There are no matching tokens found on token-tier " + "for current tier: '" + tier.getCategory() + "' in event starting at '" + eEvent.getStart() + "' and ending at '" + eEvent.getEnd() + "' having the value '" + eEvent.getValue() + "'. Exception occurs in file '" + this.getResourceURI() + "'. You can try to set the property \"cleanModel\" to \"true\".");
					} else {
						throw new PepperModuleDataException(this, "There are no matching tokens found on token-tier " + "for current tier: '" + tier.getCategory() + "' in event starting at '" + eEvent.getStart() + "' and ending at '" + eEvent.getEnd() + "' having the value '" + eEvent.getValue() + "'. Exception occurs in file '" + this.getResourceURI() + "'. Unfortunatly property '" + EXMARaLDAImporterProperties.PROP_CLEAN_MODEL + "' did not helped here. ");
					}
				}
				
				for (SToken sToken : sTokens) {
					SSpanningRelation spanRel = SaltFactory.eINSTANCE.createSSpanningRelation();
					spanRel.setSSpan(sSpan);
					spanRel.setSToken(sToken);
					this.getSDocument().getSDocumentGraph().addSRelation(spanRel);
				}

				// medium and url to SAnnotation
				this.mapMediumURL2SSNode(eEvent, sSpan);
			}
		}
	}

	/**
	 * Returns a list of tokens, which are not exactly in the passed sequence.
	 * That means, each token starting or ending inside the range is returned.
	 * This method should help in case of a token tier was not minimal. For
	 * instance imagine a tier containing A, B and C.
	 * <table border= "1">
	 * <tr>
	 * <td>t1</td>
	 * <td>t2</td>
	 * <td>t3</td>
	 * <td>t4</td>
	 * <td>t5</td>
	 * </tr>
	 * <tr>
	 * <td></td>
	 * <td colspan="3">token</td>
	 * <td/>
	 * </tr>
	 * <tr>
	 * <td colspan="2">A</td>
	 * <td>B</td>
	 * <td colspan="2">C</td>
	 * </tr>
	 * </table>
	 * 
	 * @param sequence
	 * @return
	 */
	public List<SToken> getAdjacentSTokens(SDataSourceSequence sequence) {
		List<SToken> sTokens = new ArrayList<SToken>();
		EList<? extends SSequentialRelation> sSeqRels = null;
		if (sequence.getSSequentialDS() instanceof STextualDS)
			sSeqRels = getSDocument().getSDocumentGraph().getSTextualRelations();
		else if (sequence.getSSequentialDS() instanceof STimeline)
			sSeqRels = getSDocument().getSDocumentGraph().getSTimelineRelations();
		else
			throw new SaltModuleException("Cannot compute overlaped nodes, because the given dataSource is not supported by this method.");

		for (SSequentialRelation rel : sSeqRels) {
			// walk through all textual relations
			if (sequence.getSSequentialDS().equals(rel.getSTarget())) {
				if ((rel.getSStart() <= sequence.getSStart()) && (rel.getSEnd() > sequence.getSStart())) {
					if (rel.getSSource() instanceof SToken) {
						sTokens.add((SToken) rel.getSSource());
					}
				} else if ((rel.getSStart() >= sequence.getSStart()) && (rel.getSStart() < sequence.getSEnd())) {
					if (rel.getSSource() instanceof SToken) {
						sTokens.add((SToken) rel.getSSource());
					}
				}
			}
		}
		return (sTokens);
	}

	/**
	 * Maps the medium and url of an event and maps it to SAnnotations of an
	 * SNode.
	 * 
	 * @param event
	 * @param sNode
	 */
	private void mapMediumURL2SSNode(Event event, SNode sNode) {
		// mapMedium to SAnnotation
		if (event.getMedium() != null) {
			sNode.createSAnnotation(null, EXBNameIdentifier.KW_EXB_EVENT_MEDIUM, event.getMedium().toString());
		}
		// mapURL to SAnnotation
		if (event.getUrl() != null) {
			sNode.createSAnnotation(null, EXBNameIdentifier.KW_EXB_EVENT_URL, event.getUrl().toString());
		}
	}

	/**
	 * Maps the common timeline to STimeline.
	 * 
	 * @param eTimeLine
	 * @param sTimeLine
	 */
	public void mapCommonTimeLine2STimeine(CommonTimeLine eTimeLine, STimeline sTimeLine) {
		for (TLI tli : eTimeLine.getTLIs()) {
			if (tli.getTime() == null) {
				sTimeLine.addSPointOfTime("");
			} else {
				sTimeLine.addSPointOfTime(tli.getTime());
			}
		}
	}

	/**
	 * Creates the relation between token and timeline and textualDS.
	 * 
	 * @param eCTimeline
	 * @param eEvent
	 * @param sToken
	 * @param sTime
	 */
	public void mapEvent2SToken(Event eEvent, CommonTimeLine eCTimeline, SToken sToken, STimeline sTime) {
		// create relation to timeline
		// search position of tli in CommonTimeLine
		TLI startTli = eEvent.getStart();
		TLI endTli = eEvent.getEnd();
		Integer startPos = null;
		Integer endPos = null;
		int i = 0;
		for (TLI tli : eCTimeline.getTLIs()) {
			if (tli.equals(startTli))
				startPos = i;
			if (tli.equals(endTli))
				endPos = i;
			if (startPos != null && endPos != null)
				break;
			i++;
		}
		STimelineRelation sTimeRel = SaltFactory.eINSTANCE.createSTimelineRelation();
		sTimeRel.setSTimeline(sTime);
		sTimeRel.setSToken(sToken);
		sTimeRel.setSStart(startPos);
		sTimeRel.setSEnd(endPos);
		this.getSDocument().getSDocumentGraph().addSRelation(sTimeRel);
	}

	/**
	 * Maps a tier to STextualDS, creates SToken objects and relates them by
	 * STextualRelation to STextualDS.
	 * 
	 * @param sText
	 * @param eTextTier
	 */
	public void mapTier2STextualDS(Tier eTextTier, STextualDS sText, List<Tier> textSlot) {
		StringBuilder text = new StringBuilder();
		int start = 0;
		int end = 0;
		for (Event event : eTextTier.getEvents()) {
			start = text.length();
			String eventValue = event.getValue();
			if (eventValue != null) {
				text.append(event.getValue());
			}
			end = text.length();
			String sep = this.getTokenSepearator();
			if ((eventValue != null) && (sep != null)) {
				text.append(sep);
			}
			// creating and adding token
			SToken sToken = SaltFactory.eINSTANCE.createSToken();
			getSDocument().getSDocumentGraph().addSNode(sToken);
			if (this.tierNames2SLayers != null) {
				// add sToken to layer if required
				SLayer sLayer = this.tierNames2SLayers.get(eTextTier.getCategory());
				if (sLayer != null) {
					sToken.getSLayers().add(sLayer);
				}
			}
			token2Speaker.put(sToken, event.getTier().getSpeaker());
			// creating annotation for token
			this.mapUDInformations2SMetaAnnotatableElement(event.getUdInformations(), sToken);
			// creating semanticalAnnotation for token
			mapSStructuredNode2SemanticAnnotation(eTextTier, sToken);
			// medium and url to SAnnotation
			this.mapMediumURL2SSNode(event, sToken);

			// creating textual relation
			STextualRelation sTextRel = SaltFactory.eINSTANCE.createSTextualRelation();
			sTextRel.setSTextualDS(sText);
			sTextRel.setSToken(sToken);
			sTextRel.setSStart(start);
			sTextRel.setSEnd(end);
			getSDocument().getSDocumentGraph().addSRelation(sTextRel);

			if ((getSDocument().getSDocumentGraph().getSAudioDataSources() != null) && (getSDocument().getSDocumentGraph().getSAudioDataSources().size() > 0) && ((event.getStart().getTime() != null) || (event.getEnd().getTime() != null))) {
				// start: creating SAudioDSRelation
				try {
					Double audioStart = null;
					if (event.getStart().getTime() != null)
						audioStart = Double.valueOf(event.getStart().getTime());
					Double audioEnd = null;
					if (event.getEnd().getTime() != null)
						audioEnd = Double.valueOf(event.getEnd().getTime());

					SAudioDSRelation sAudioDSRelation = SaltFactory.eINSTANCE.createSAudioDSRelation();
					sAudioDSRelation.setSToken(sToken);
					sAudioDSRelation.setSAudioDS(getSDocument().getSDocumentGraph().getSAudioDataSources().get(0));
					sAudioDSRelation.setSStart(audioStart);
					sAudioDSRelation.setSEnd(audioEnd);
					getSDocument().getSDocumentGraph().addSRelation(sAudioDSRelation);
				} catch (NumberFormatException e) {
					logger.warn("[EXMARaLDAImporter] Cannot map time attribute of timeline to SStart or SEnd, because value '" + event.getStart().getTime() + "' is not mappable to a double value.");
				}
			}

			// creating timelineRel
			this.mapEvent2SToken(event, this.getBasicTranscription().getCommonTimeLine(), sToken, getSDocument().getSDocumentGraph().getSTimeline());
		}
		sText.setSText(text.toString());

		for (Tier tier : textSlot) {
			if (!tier.equals(eTextTier)) {
				// if tier is annotation tier
				for (Event event : tier.getEvents()) {
					Integer startPos = getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getStart());
					Integer endPos = getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getEnd());

					SDataSourceSequence sequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
					sequence.setSStart(startPos);
					sequence.setSEnd(endPos);
					sequence.setSSequentialDS(getSDocument().getSDocumentGraph().getSTimeline());

					List<SToken> sTokens = getSDocument().getSDocumentGraph().getSTokensBySequence(sequence);

					if (sTokens != null) {
						for (SToken sToken : sTokens) {
							// create for every token an annotation for the
							// tiers
							this.mapEvent2SNode(tier, event, sToken);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a new annotation with the given event on sNode.
	 * 
	 * @param eEvent
	 * @param sNode
	 */
	public void mapEvent2SNode(Tier tier, Event eEvent, SNode sNode) {
		String posTier = this.getProps().getPOS();
		String lemmaTier = this.getProps().getLemma();
		String preUriTiers = this.getProps().getURIAnnotation();
		List<String> uriTiers = null;
		if ((preUriTiers != null) && (!preUriTiers.isEmpty())) {
			uriTiers = new ArrayList<String>();
			for (String uriTier : preUriTiers.trim().split(","))
				uriTiers.add(uriTier);
		}

		if (tier.getCategory().equalsIgnoreCase(posTier)) {
			// this tier has special annotations: pos annotations
			SAnnotation sAnno = SaltSemanticsFactory.eINSTANCE.createSPOSAnnotation();
			sAnno.setSValue(eEvent.getValue());
			sNode.addSAnnotation(sAnno);
		} else if (tier.getCategory().equalsIgnoreCase(lemmaTier)) {
			// this tier has special annotations: lemma annotations
			SAnnotation sAnno = SaltSemanticsFactory.eINSTANCE.createSLemmaAnnotation();
			sAnno.setSValue(eEvent.getValue());
			sNode.addSAnnotation(sAnno);
		} else if ((uriTiers != null) && (uriTiers.contains(tier.getCategory()))) {
			String pathName = this.getResourceURI().toFileString().replace(this.getResourceURI().lastSegment(), eEvent.getValue());
			File file = new File(pathName);
			if (!file.exists()) {
				logger.warn("[EXMARaLDAImporter] Cannot add the uri-annotation '" + eEvent.getValue() + "' of tier '" + tier.getCategory() + "', because the file '" + pathName + "' does not exist.");
			} else {
				URI corpusFilePath = URI.createFileURI(file.getAbsolutePath());
				sNode.createSAnnotation(null, tier.getCategory(), corpusFilePath.toFileString());
			}
		} else {
			String namespace = null;
			if (eEvent.getTier().getSpeaker() != null) {
				namespace = eEvent.getTier().getSpeaker().getAbbreviation();
			}
			sNode.createSAnnotation(namespace, tier.getCategory(), eEvent.getValue());
		}
		if ((eEvent.getUdInformations() != null) && (eEvent.getUdInformations().size() > 0)) {
			this.mapUDInformations2SMetaAnnotatableElement(eEvent.getUdInformations(), sNode);
		}

	}

	/**
	 * Creates additional semantic annotations to given node if necessary. E.g.
	 * marking the given node as Word.
	 * 
	 * @param tier
	 * @param sStructuredNode
	 */
	private void mapSStructuredNode2SemanticAnnotation(Tier tier, SStructuredNode sStructuredNode) {
		// check if tier is word tier
		if (tier.getCategory().equalsIgnoreCase(this.getSWordTier())) {
			SWordAnnotation sWordAnno = SaltSemanticsFactory.eINSTANCE.createSWordAnnotation();
			sStructuredNode.addSAnnotation(sWordAnno);
		}
	}

	private String getTokenSepearator() {
		String retVal = null;
		if ((this.getProps().getTokenSeparator() != null) && (!this.getProps().getTokenSeparator().isEmpty())) {
			String preSep = this.getProps().getTokenSeparator();

			if (preSep.length() > 2) {
				// seperatorString has to be larger than 2, because of the form
				// " "
				preSep = preSep.replace("\"", "");
				retVal = preSep;
			}
		}
		return (retVal);
	}

	/**
	 * Returns the category name of tier, which has to be annoted additional as
	 * Word.
	 * 
	 * @return
	 */
	private String getSWordTier() {
		String retVal = null;
		if ((this.getProps().getWord() != null) && (!this.getProps().getWord().isEmpty())) {
			String wordTier = this.getProps().getWord();

			if (wordTier.length() > 2) {
				// wordTier has to be larger than 2, because of the form " "
				wordTier = wordTier.replace("\"", "");
				retVal = wordTier;
			}
		}
		return (retVal);
	}
}
