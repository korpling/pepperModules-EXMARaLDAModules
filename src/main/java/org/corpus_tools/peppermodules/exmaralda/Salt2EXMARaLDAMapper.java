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
package org.corpus_tools.peppermodules.exmaralda;

import com.google.common.base.Splitter;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.CommonTimeLine;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.EVENT_MEDIUM;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.MetaInformation;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.SPEAKER_SEX;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Speaker;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TIER_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.UDInformation;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SMedialDS;
import org.corpus_tools.salt.common.SMedialRelation;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Salt2EXMARaLDAMapper extends PepperMapperImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(EXMARaLDAExporter.class);

	// -------------------- basic transcription
	public void setBasicTranscription(BasicTranscription basicTranscription) {
		this.basicTranscription = basicTranscription;
	}

	public BasicTranscription getBasicTranscription() {
		return basicTranscription;
	}

	private BasicTranscription basicTranscription = null;
	// -------------------- basic transcription
	// -------------------- start: helping structures
	private final Map<String, TLI> tLI2PointOfTimeMap = new HashMap<>();

	// -------------------- end: helping structures
	@Override
	public DOCUMENT_STATUS mapSCorpus() {
		if (getResourceURI() != null) {
			File resourceFile = new File(getResourceURI().toFileString());
			resourceFile.mkdirs();
		}
		return (DOCUMENT_STATUS.COMPLETED);
	}

	/**
	 * {@inheritDoc PepperMapper#setDocument(SDocument)}
	 *
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getDocument().getDocumentGraph() == null) {
			getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		}
		if (getResourceURI() != null) {
			File resourceFile = new File(getResourceURI().toFileString());
			resourceFile.getParentFile().mkdirs();
		}
		this.setBasicTranscription(ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription());
	
		// mapping for MetaInformation
		MetaInformation metaInformation = ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
		basicTranscription.setMetaInformation(metaInformation);
		this.mapSDocument2MetaInfo(getDocument(), metaInformation);
		this.mapMediaSourceToReferencedFile(getDocument().getDocumentGraph(), metaInformation);
		
		Map<String, Speaker> speakerById = this.mapSDocument2SpeakerMeta(getDocument(), basicTranscription.getSpeakertable());
		// creating timeline
		if (this.getDocument().getDocumentGraph().getTimeline() == null) {
			// if no timeline is included, create one SDocumentDataEnricher
			getDocument().getDocumentGraph().createTimeline();
		}
		CommonTimeLine cTimeLine = ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();
		basicTranscription.setCommonTimeLine(cTimeLine);
		this.map2CommonTimeLine(getDocument().getDocumentGraph().getTimeline(), cTimeLine);
		
		// creating token tier
		List<STextualDS> texts = this.getDocument().getDocumentGraph().getTextualDSs();
		if(texts != null && !texts.isEmpty()) {
			int textIdx = 0;
			for(STextualDS text : texts) {
				Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
				basicTranscription.getTiers().add(tokenTier);
				
				DataSourceSequence<Integer> seq = new DataSourceSequence<>(text, text.getStart(), text.getEnd());
				String name = text.getName();
				if(name == null || name.isEmpty() || name.startsWith("sText")) {
					if(textIdx > 0) {
						name = TIER_NAME_TOKEN + textIdx;
					} else {
						name = TIER_NAME_TOKEN;
					}
				}
				SFeature featSpeaker = text.getFeature(SaltUtil.createQName(EXBNameIdentifier.EXB_NS, EXBNameIdentifier.EXB_SPEAKER));
				if(featSpeaker != null && featSpeaker.getValue_STEXT() != null) {
					Speaker speaker = speakerById.get(featSpeaker.getValue_STEXT());
					if(speaker != null) {
						tokenTier.setSpeaker(speaker);
					}
				}
				List<SToken> tokensOfText = getDocument().getDocumentGraph().getSortedTokenByText(text.getGraph().getTokensBySequence(seq));
				this.mapSToken2Tier(tokensOfText, tokenTier, name);
				
				textIdx++;
			}
		} else {
			Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
			basicTranscription.getTiers().add(tokenTier);
			this.mapSToken2Tier(getDocument().getDocumentGraph().getTokens(), tokenTier, TIER_NAME_TOKEN);
		}
		// map all SStructuredNodes to tiers

		List<SStructuredNode> structuredNodes = new ArrayList<>();
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getTokens());
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getSpans());
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getStructures());

		// map
		this.mapSStructuredNode2Tiers(structuredNodes, speakerById);
		
		// re-order tiers if an order was given
		SFeature featTierOrder = getDocument().getDocumentGraph().getFeature(
				EXBNameIdentifier.EXB_NS, EXBNameIdentifier.EXB_TIER_ORDER);
		if(featTierOrder != null) {
			List<String> tierOrder = Splitter.on(",").trimResults().splitToList(featTierOrder.getValue().toString());
			Multimap<String, Tier> tiersByDisplayName = LinkedHashMultimap.create();
			for(Tier t : basicTranscription.getTiers()) {
				if(t.getDisplayName() == null) {
					tiersByDisplayName.put("", t);
				} else {
					tiersByDisplayName.put(t.getDisplayName(), t);
				}
			}
			
			// append in correct order
			List<Tier> newOrderedTierList = new LinkedList<>();
			for(String displayName : tierOrder) {
				Collection<Tier> tiers = tiersByDisplayName.removeAll(displayName);
				for(Tier t : tiers) {
					newOrderedTierList.add(t);
				}
			}
			// append all remaining tiers
			for(Tier t : tiersByDisplayName.values()) {
				newOrderedTierList.add(t);
			}
			// replace the list
			basicTranscription.getTiers().clear();
			basicTranscription.getTiers().addAll(newOrderedTierList);
		}

		saveToFile(basicTranscription);

		return (DOCUMENT_STATUS.COMPLETED);
	}

	private void saveToFile(BasicTranscription basicTranscription) {
		// create resource set and resource
		ResourceSet resourceSet = new ResourceSetImpl();
		// Register XML resource factory
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(EXMARaLDAExporter.FILE_EXTENION, new EXBResourceFactory());
		// load resource
		Resource resource = resourceSet.createResource(getResourceURI());
		if (resource == null) {
			throw new PepperModuleDataException(this, "Cannot save a resource to uri '" + getResourceURI() + "', because the given resource is null.");
		}
		resource.getContents().add(basicTranscription);
		try {
			resource.save(null);
		} catch (IOException e) {
			throw new PepperModuleDataException(this, "Cannot write exmaradla basic transcription to uri '" + getResourceURI() + "'.", e);
		}
	}
	
	
	private void mapMediaSourceToReferencedFile(SDocumentGraph sDocGraph, MetaInformation metaInfo) {
		if(sDocGraph.getMedialDSs() != null && !sDocGraph.getMedialDSs().isEmpty()) {
			SMedialDS medialDS = sDocGraph.getMedialDSs().get(0);
			File mediaFile;
			if(medialDS.getMediaReference().isFile()) {
				mediaFile = new File(medialDS.getMediaReference().toFileString());
			} else {
				mediaFile = new File(medialDS.getMediaReference().toString());
			}
			if(mediaFile.exists()) {
				// create a relative URL
				URI mediaLoc = URI.createFileURI(mediaFile.getAbsolutePath());
				URI baseLoc = this.getResourceURI();
				mediaLoc = mediaLoc.deresolve(baseLoc);
				metaInfo.setReferencedFile(mediaLoc.toFileString());
			}			
		}
	}

	/**
	 * Maps all SMetaAnnotations of document to MetaInformation or UDInformation
	 *
	 * @param sDoc
	 * @param metaInfo
	 */
	private void mapSDocument2MetaInfo(SDocument sDoc, MetaInformation metaInfo) {
		// map SMeatAnnotations2udInformation
		for (SMetaAnnotation sMetaAnno : sDoc.getMetaAnnotations()) {
			// map project name
			if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_PROJECT_NAME)) {
				metaInfo.setProjectName(sMetaAnno.getValue().toString());
			} else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME)) {
				// map transcription name
				metaInfo.setTranscriptionName(sMetaAnno.getValue().toString());
			} else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_REFERENCED_FILE)) {
				// map referenced file
				metaInfo.setReferencedFile(sMetaAnno.getValue().toString());
			} else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_COMMENT)) {
				metaInfo.setComment(sMetaAnno.getValue().toString());
			} else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION)) {
				// map transcription convention
				metaInfo.setTranscriptionConvention(sMetaAnno.getValue().toString());
			} else {
				UDInformation udInfo = ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
				this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
				metaInfo.getUdMetaInformations().add(udInfo);
			}
		}
	}
	
	/**
	 * Maps textual DS meta annotation as speaker meta data. Assumes each textual DS belongs to another speaker.
	 * @param doc
	 * @param speakerTable 
	 */
	private Map<String,Speaker> mapSDocument2SpeakerMeta(SDocument doc, List<Speaker> speakerTable) {
		final Splitter listSplitter = Splitter.on(',').trimResults();
		
		Map<String,Speaker> speakerById = new TreeMap<>();
		
		for(SMetaAnnotation sMetaAnno : doc.getMetaAnnotations()) {
			
			// get existing speaker or create new one based on the namespace
			String id = sMetaAnno.getNamespace();
			
			if(id != null) {
				
				Speaker speaker = speakerById.get(id);
				if(speaker == null) {
					speaker = ExmaraldaBasicFactory.eINSTANCE.createSpeaker();
					
					speaker.setId(id);					
					speakerById.put(id, speaker);
				}
				
				if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_SEX)) {
					SPEAKER_SEX v = SPEAKER_SEX.get(sMetaAnno.getValue().toString());
					if(v != null) {
						speaker.setSex(v);
					}
				} else if(sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_ABBR)) {
					speaker.setAbbreviation(sMetaAnno.getValue().toString());
				} else if(sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_COMMENT)) {
					speaker.setComment(sMetaAnno.getValue().toString());
				} else if(sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_L1)) {
					for(String l1 : listSplitter.split(sMetaAnno.getValue().toString())) {
						speaker.getL1().add(l1);
					}
				} else if(sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_L2)) {
					for(String l2 : listSplitter.split(sMetaAnno.getValue().toString())) {
						speaker.getL2().add(l2);
					}
				} else if(sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_LANGUAGES_USED)) {
					for(String l : listSplitter.split(sMetaAnno.getValue().toString())) {
						speaker.getLanguageUsed().add(l);
					}
				} else {
					// if unknown, use general UD information
					UDInformation udInfo = ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
					this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
					speaker.getUdSpeakerInformations().add(udInfo);
				}
			}
		} // end for each meta annotation
		
		// add all speaker entries to speaker table
		speakerTable.addAll(speakerById.values());
		
		return speakerById;
	}
	
	/**
	 * Creates content of a common timeline, and also creates all TLIs.
	 *
	 * @param sTimeline
	 * @param cTimeLine
	 */
	private void map2CommonTimeLine(STimeline sTimeline, CommonTimeLine cTimeLine) {
		if ((sTimeline == null) || (sTimeline.getEnd() == null) || (sTimeline.getEnd() == 0)) {
			this.getDocument().getDocumentGraph().createTimeline();
			sTimeline = this.getDocument().getDocumentGraph().getTimeline();
		}

		final boolean createFromMediaDS
				= this.getDocument().getDocumentGraph().getMedialDSs() != null
				&& !this.getDocument().getDocumentGraph().getMedialDSs().isEmpty()
				&& !this.getDocument().getDocumentGraph().getMedialRelations().isEmpty();

		if (createFromMediaDS) {
			String TLI_id = "T";
			
			// get the first media data source
			SMedialDS ds = this.getDocument().getDocumentGraph().getMedialDSs().get(0);
			
			// collect an ordered set of start/end times
			TreeSet<Double> timePoints = new TreeSet<>();
			for(SRelation rel : ds.getInRelations()) {
				if(rel instanceof SMedialRelation) {
					SMedialRelation mediaRel = (SMedialRelation) rel;
					
					// add both the start and end time to the set
					timePoints.add(mediaRel.getStart());
					timePoints.add(mediaRel.getEnd());

				}
			}
			
			int tIdx = 0;
			// iterate over the ordered times
			for(Double t : timePoints) {
				TLI tli = ExmaraldaBasicFactory.eINSTANCE.createTLI();
				cTimeLine.getTLIs().add(tli);
				tli.setTime("" + t);
				tli.setId(TLI_id + tIdx);
				
				this.tLI2PointOfTimeMap.put(tli.getTime(), tli);
				
				tIdx++;
			}
		} else {
			String TLI_id = "T";
			int i = 0;
			for (int j = 0; j <= sTimeline.getEnd(); j++) {
				TLI tli = ExmaraldaBasicFactory.eINSTANCE.createTLI();
				cTimeLine.getTLIs().add(tli);
				tli.setTime(j + "");
				tli.setId(TLI_id + i);
				i++;
				// put TLI to map
				this.tLI2PointOfTimeMap.put(j + "", tli);
			}
		}		
	}

	/**
	 * stores number of created tiers
	 */
	private Integer numOfTiers = 0;

	private Integer getNewNumOfTiers() {
		int num = numOfTiers;
		numOfTiers++;
		return (num);
	}
	
	private String createDisplayName(Tier tier, String annoName) {
		if(tier.getSpeaker() != null) {
			Speaker s = tier.getSpeaker();
			if(s.getAbbreviation() != null) {
				return s.getAbbreviation() + " [" + annoName + "]";
			} else {
				return s.getId() + " [" + annoName + "]";
			}
		} else {
			return "[" + annoName + "]";
		}
	}

	/**
	 * Stores the prefix for tier id
	 */
	public String TIER_ID_PREFIX = "TIE";
	/**
	 * Stores the name of the tier, which contains tokenization
	 */
	public String TIER_NAME_TOKEN = "tok";

	/**
	 * Maps a list of token to a tier. That means, that a textual tier will be
	 * created. It calls mapSToken2Event(). <br/>
	 * Please take care, that the mapping for SToken-annotations has to be
	 * treated seperatly
	 *
	 * @param sTokens
	 * @param tier
	 * @param textName 
	 */
	private void mapSToken2Tier(List<SToken> sTokens, Tier tier, String textName) {
		tier.setCategory(textName);
		tier.setDisplayName(createDisplayName(tier, textName));
		tier.setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
		tier.setType(TIER_TYPE.T);
		for (SToken sToken : sTokens) {
			Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
			tier.getEvents().add(event);
			this.mapSToken2Event(sToken, event);
		}
	}
	
	private DataSourceSequence<? extends Number> getTimeOverlappedSeq(SStructuredNode sNode) {
		
		List<SMedialDS> mediaDSs = getDocument().getDocumentGraph().getMedialDSs();
		if(mediaDSs != null && !mediaDSs.isEmpty()) {
			List<SToken> overlappedToken = getDocument().getDocumentGraph().getOverlappedTokens(sNode);
			
			double rangeStart = Double.MAX_VALUE;
			double rangeEnd = Double.MIN_VALUE;
			boolean foundMediaRel = false;
			
			for(SToken t : overlappedToken) {
				for(SRelation rel : t.getOutRelations()) {
					if(rel instanceof SMedialRelation) {
						
						foundMediaRel = true;
						
						SMedialRelation mediaRel = (SMedialRelation) rel;
						rangeStart = Math.min(mediaRel.getStart(), rangeStart);
						rangeEnd = Math.max(mediaRel.getEnd(), rangeEnd);
					}
				}
			}
			
			if(!foundMediaRel) {
				return null;
			}
			
			return new DataSourceSequence(mediaDSs.get(0), rangeStart, rangeEnd);
			
			
		} else {
			List<DataSourceSequence> sequences = getDocument().getDocumentGraph().getOverlappedDataSourceSequence(sNode, SALT_TYPE.STIME_OVERLAPPING_RELATION);
			if(!sequences.isEmpty()) {
				return sequences.get(0);
			}
		}
		
		return null;
	}

	/**
	 * Maps one token to one event.
	 *
	 * @param sToken
	 * @param event
	 */
	private void mapSToken2Event(SToken sToken, Event event) {
		
		DataSourceSequence<?> sequence = getTimeOverlappedSeq(sToken);
		if (sequence == null) {
			logger.error("Cannot map token to event, because there is no point of time for SToken: " + sToken.getId());
		}
		if (sequence.getStart() == null) {
			throw new PepperModuleDataException(this, "Cannot map token to event, because start of pot for following token is empty: " + sToken.getId());
		}
		if (sequence.getEnd() == null) {
			throw new PepperModuleDataException(this, "Cannot map token to event, because end of pot for following token is empty: " + sToken.getId());
		}
		event.setStart(this.tLI2PointOfTimeMap.get(sequence.getStart().toString()));
		event.setEnd(this.tLI2PointOfTimeMap.get(sequence.getEnd().toString()));
		event.setValue(stringXMLConformer(getDocument().getDocumentGraph().getText(sToken)));

	}
	
	/**
	 * Maps a a SStructuredNode-object to a tier. Therefore it takes all the
	 * annotations and creates one tier for each. <br/>
	 * Please take attention, that SToken-object shall be mapped by
	 * mapSToken2Tier() additionally to create a tier for text.
	 *
	 * @param sNodes
	 * @param tier
	 * @param speakerById
	 */
	private void mapSStructuredNode2Tiers(List<SStructuredNode> sNodes, Map<String, Speaker> speakerById) {
		// compute a table, which stores the names of tiers, and the
		// corresponding sAnnotationQName objects
		Map<String, Tier> annoName2Tier = new TreeMap<>();
		for (SStructuredNode sNode : sNodes) {// walk through the given list
			for (SAnnotation sAnno : sNode.getAnnotations()) {
				Tier currTier = null;
				if (annoName2Tier.containsKey(sAnno.getQName())) {
					// if annoName2Tier contains QName, than return
					currTier = annoName2Tier.get(sAnno.getQName());
				} else {// create new entry in annoName2Tier
					currTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
					currTier.setCategory(sAnno.getName());
					currTier.setType(TIER_TYPE.A);
					
					// check if this node is covering a textual DS with a speaker
					List<DataSourceSequence> seqList = this.getDocument().getDocumentGraph()
							.getOverlappedDataSourceSequence(sNode, SALT_TYPE.STEXT_OVERLAPPING_RELATION);
					if(seqList != null) {
						for(DataSourceSequence seq : seqList) {
							if(seq.getDataSource() instanceof STextualDS) {
								STextualDS ds = (STextualDS) seq.getDataSource();
								SFeature featSpeaker = 
										ds.getFeature(
												EXBNameIdentifier.EXB_NS, 
												EXBNameIdentifier.EXB_SPEAKER);
								if(featSpeaker != null && speakerById.containsKey(featSpeaker.getValue_STEXT())) {
									currTier.setSpeaker(speakerById.get(featSpeaker.getValue_STEXT()));
								}
							}
						}
					}
					currTier.setDisplayName(createDisplayName(currTier, sAnno.getName()));
					
					// add to map, so it is not recreated again
					annoName2Tier.put(sAnno.getQName(), currTier);
				}
				if ((!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM) && (!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_URL)))) {
					Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
					currTier.getEvents().add(event);
					SAnnotation sMediumAnno = sNode.getAnnotation(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM);
					SAnnotation sURLAnno = sNode.getAnnotation(EXBNameIdentifier.KW_EXB_EVENT_URL);
					if (sMediumAnno != null) {
						event.setMedium(EVENT_MEDIUM.get(sMediumAnno.getValue().toString()));
					}
					if (sURLAnno != null) {
						event.setUrl(sMediumAnno.getValue().toString());
					}
					this.mapSStructuredNode2Event(sNode, sAnno.getQName(), event);
				}
			}
		}
		// set the ID of the tier according to its position in the ordered map
		for (Map.Entry<String, Tier> e : annoName2Tier.entrySet()) {
			e.getValue().setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
			this.basicTranscription.getTiers().add(e.getValue());
		}
	}

	/**
	 * Maps a structuredNode to an event.
	 *
	 * @param sNode
	 * @param sAnnotationQName
	 * @param event
	 */
	private void mapSStructuredNode2Event(SStructuredNode sNode, String sAnnotationQName, Event event) {
		DataSourceSequence<?> sequence = getTimeOverlappedSeq(sNode);

		event.setStart(this.tLI2PointOfTimeMap.get(sequence.getStart().toString()));
		event.setEnd(this.tLI2PointOfTimeMap.get(sequence.getEnd().toString()));

		SAnnotation sAnno = sNode.getAnnotation(sAnnotationQName);
		if (sAnno != null) {
			event.setValue(this.stringXMLConformer(sAnno.getValue_STEXT()));
		}
		// map SMeatAnnotations2udInformation
		for (SMetaAnnotation sMetaAnno : sNode.getMetaAnnotations()) {
			UDInformation udInfo = ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
			this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
			event.getUdInformations().add(udInfo);
		}
	}

	/**
	 * Maps a meta annotation to a udInformation
	 *
	 * @param sMetaAnno
	 * @param udInfo
	 */
	private void mapSMetaAnnotation2UDInformation(SMetaAnnotation sMetaAnno, UDInformation udInfo) {
		if ((sMetaAnno.getName() != null) && (!sMetaAnno.getName().equals(""))) {
			udInfo.setAttributeName(stringXMLConformer(sMetaAnno.getName()));
			udInfo.setValue(stringXMLConformer(sMetaAnno.getValue_STEXT()));
		}
	}

	/**
	 * This method transforms a given string to a xml conform string and returns
	 * it.
	 *
	 * @param uncleanedString string which possibly is not conform to xml
	 * @return
	 */
	private String stringXMLConformer(String uncleanedString) {
		String retString = uncleanedString;
		if (retString != null) {
			retString = StringEscapeUtils.escapeXml(uncleanedString);

			retString = retString.replace("Ä", "&#196;");
			retString = retString.replace("Ö", "&#214;");
			retString = retString.replace("Ü", "&#220;");
			retString = retString.replace("ä", "&#228;");
			retString = retString.replace("ö", "&#246;");
			retString = retString.replace("ü", "&#252;");
			retString = retString.replace("ß", "&#223;");
		}
		return (retString);
	}
}
