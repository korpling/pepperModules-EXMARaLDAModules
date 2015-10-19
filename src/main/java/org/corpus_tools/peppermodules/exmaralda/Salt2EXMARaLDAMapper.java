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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.core.SAnnotation;
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
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TIER_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.UDInformation;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;

public class Salt2EXMARaLDAMapper extends PepperMapperImpl {
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
	private List<TLI2PointOfTime> tLI2PointOfTimeList = new ArrayList<TLI2PointOfTime>();

	private class TLI2PointOfTime {
		public TLI tli = null;

		public String pointOfTime = null;
	}

	private TLI getTLI(String sPointOfTime) {
		TLI retVal = null;
		for (TLI2PointOfTime tli2pot : tLI2PointOfTimeList) {
			if (tli2pot.pointOfTime.equalsIgnoreCase(sPointOfTime)) {
				retVal = tli2pot.tli;
				break;
			}
		}
		return (retVal);
	}

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
		this.mapSDocuent2MetaInfo(getDocument(), metaInformation);

		// creating timeline
		if (this.getDocument().getDocumentGraph().getTimeline() == null) {// if
																			// no
																			// timeline
																			// is
																			// included,
																			// create
																			// one
																			// SDocumentDataEnricher
																			// dataEnricher
																			// =
																			// new
																			// SDocumentDataEnricher();
			// dataEnricher.setDocumentGraph(this.getDocument().getDocumentGraph());
			// dataEnricher.createSTimeline();

			getDocument().getDocumentGraph().createTimeline();
		}
		CommonTimeLine cTimeLine = ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();
		basicTranscription.setCommonTimeLine(cTimeLine);
		this.map2CommonTimeLine(getDocument().getDocumentGraph().getTimeline(), cTimeLine);

		// creating token tier
		Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
		basicTranscription.getTiers().add(tokenTier);
		this.mapSToken2Tier(getDocument().getDocumentGraph().getTokens(), tokenTier);
		// map all SStructuredNodes to tiers

		List<SStructuredNode> structuredNodes = new ArrayList<>();
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getTokens());
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getSpans());
		// add all SToken to mapping list
		structuredNodes.addAll(getDocument().getDocumentGraph().getStructures());

		// map
		this.mapSStructuredNode2Tiers(structuredNodes);

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
		if (resource == null)
			throw new PepperModuleDataException(this, "Cannot save a resource to uri '" + getResourceURI() + "', because the given resource is null.");

		resource.getContents().add(basicTranscription);
		try {
			resource.save(null);
		} catch (IOException e) {
			throw new PepperModuleDataException(this, "Cannot write exmaradla basic transcription to uri '" + getResourceURI() + "'.", e);
		}
	}

	/**
	 * Maps all SMetaAnnotations of document to MetaInformation or UDInformation
	 * 
	 * @param sDoc
	 * @param metaInfo
	 */
	private void mapSDocuent2MetaInfo(SDocument sDoc, MetaInformation metaInfo) {
		// map SMeatAnnotations2udInformation
		for (SMetaAnnotation sMetaAnno : sDoc.getMetaAnnotations()) {
			// map project name
			if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_PROJECT_NAME)) {
				metaInfo.setProjectName(sMetaAnno.getValue().toString());
			}
			// map transcription name
			else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME))
				metaInfo.setTranscriptionName(sMetaAnno.getValue().toString());
			// map referenced file
			else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_REFERENCED_FILE)) {
				/* try { */
				// new URL(sMetaAnno.getValue().toString());
				metaInfo.setReferencedFile(sMetaAnno.getValue().toString());
				/*
				 * } catch (MalformedURLException e) { }
				 */
			} else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_COMMENT))
				metaInfo.setComment(sMetaAnno.getValue().toString());
			// map transcription convention
			else if (sMetaAnno.getName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION))
				metaInfo.setTranscriptionConvention(sMetaAnno.getValue().toString());
			else {
				UDInformation udInfo = ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
				this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
				metaInfo.getUdMetaInformations().add(udInfo);
			}
		}
	}

	/**
	 * Creates content of a common timeline, and also creates all TLI�s.
	 * 
	 * @param sTimeline
	 * @param cTimeLine
	 */
	private void map2CommonTimeLine(STimeline sTimeline, CommonTimeLine cTimeLine) {
		if ((sTimeline == null) || (sTimeline.getEnd() == null) || (sTimeline.getEnd() == 0))
		{
			this.getDocument().getDocumentGraph().createTimeline();
			sTimeline = this.getDocument().getDocumentGraph().getTimeline();
		}
		String TLI_id = "T";
		int i = 0;
		for (int j= 0; j <  sTimeline.getEnd(); j++) {
			TLI tli = ExmaraldaBasicFactory.eINSTANCE.createTLI();
			cTimeLine.getTLIs().add(tli);
			tli.setTime(j+"");
			tli.setId(TLI_id + i);
			i++;
			// put TLI to list
			TLI2PointOfTime tliPOT = new TLI2PointOfTime();
			tliPOT.pointOfTime = j+"";
			tliPOT.tli = tli;
			this.tLI2PointOfTimeList.add(tliPOT);
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
	 */
	private void mapSToken2Tier(List<SToken> sTokens, Tier tier) {
		tier.setCategory(TIER_NAME_TOKEN);
		tier.setDisplayName("[" + TIER_NAME_TOKEN + "]");
		tier.setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
		tier.setType(TIER_TYPE.T);
		for (SToken sToken : sTokens) {
			Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
			tier.getEvents().add(event);
			this.mapSToken2Event(sToken, event);
		}
	}

	/**
	 * Maps one token to one event.
	 * 
	 * @param sToken
	 * @param event
	 */
	private void mapSToken2Event(SToken sToken, Event event) {
//		SDocumentStructureAccessor acc = new SDocumentStructureAccessor();
//		acc.setDocumentGraph(this.getDocument().getDocumentGraph());
//		String text = acc.getSOverlappedText(sToken);
		
//		String text = getDocument().getDocumentGraph().getOverlappedText(sToken);
//		event.setValue(text);
//		POTPair potPair = acc.getPOT(sToken);
		
		List<SALT_TYPE> type= new ArrayList();
		type.add(SALT_TYPE.STIME_OVERLAPPING_RELATION);
		List<DataSourceSequence> sequences = getDocument().getDocumentGraph().getOverlappedDataSourceSequence(sToken, type);
		DataSourceSequence<Integer> sequence = (DataSourceSequence<Integer>)(DataSourceSequence<? extends Number>)sequences.get(0);
		if (sequence == null){
			throw new PepperModuleDataException(this, "Cannot map token to event, because there is no point of time for SToken: " + sToken.getId());
		}
		if (sequence.getStart() == null){
			throw new PepperModuleDataException(this, "Cannot map token to event, because start of pot for following token is empty: " + sToken.getId());
		}
		if (sequence.getEnd() == null){
			throw new PepperModuleDataException(this, "Cannot map token to event, because end of pot for following token is empty: " + sToken.getId());
		}
		event.setStart(this.getTLI(sequence.getStart().toString()));
		event.setEnd(this.getTLI(sequence.getEnd().toString()));
		event.setValue(getDocument().getDocumentGraph().getText(sToken));
		
	}

	/**
	 * Maps a a SStructuredNode-object to a tier. Therefore it takes all the
	 * annotations and creates one tier for each. <br/>
	 * Please take attention, that SToken-object shall be mapped by
	 * mapSToken2Tier() additionally to create a tier for text.
	 * 
	 * @param sNodes
	 * @param tier
	 */
	private void mapSStructuredNode2Tiers(List<SStructuredNode> sNodes) {
		// compute a table, which stores the names of tiers, and the
		// corresponding sAnnotationQName objects
		Map<String, Tier> annoName2Tier = new Hashtable<String, Tier>();
		for (SStructuredNode sNode : sNodes) {// walk through the given list
			for (SAnnotation sAnno : sNode.getAnnotations()) {
				Tier currTier = null;
				if (annoName2Tier.containsKey(sAnno.getQName())) {// if
																	// annoName2Tier
																	// contains
																	// QName,
																	// than
																	// return
					currTier = annoName2Tier.get(sAnno.getQName());
				} else {// create new entry in annoName2Tier
					currTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
					{// create everything for tier
						currTier.setCategory(sAnno.getName());
						currTier.setDisplayName("[" + sAnno.getName() + "]");
						currTier.setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
						currTier.setType(TIER_TYPE.T);
					}
					this.basicTranscription.getTiers().add(currTier);
					annoName2Tier.put(sAnno.getQName(), currTier);
				}
				if ((!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM) && (!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_URL)))) {
					Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
					currTier.getEvents().add(event);
					SAnnotation sMediumAnno = sNode.getAnnotation(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM);
					SAnnotation sURLAnno = sNode.getAnnotation(EXBNameIdentifier.KW_EXB_EVENT_URL);
					if (sMediumAnno != null)
						event.setMedium(EVENT_MEDIUM.get(sMediumAnno.getValue().toString()));
					if (sURLAnno != null) {
						/*
						 * try { // new URL(sMediumAnno.getValue().toString());
						 */
						event.setUrl(sMediumAnno.getValue().toString());/*
																		 * }
																		 * catch
																		 * (
																		 * MalformedURLException
																		 * e) {
																		 * }
																		 */
					}
					this.mapSStructuredNode2Event(sNode, sAnno.getQName(), event);
				}
			}
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
//		SDocumentStructureAccessor acc = new SDocumentStructureAccessor();
//		acc.setDocumentGraph(this.getDocument().getDocumentGraph());
//		POTPair potPair = acc.getPOT(sNode);
		
		List<SALT_TYPE> type= new ArrayList();
		type.add(SALT_TYPE.STIME_OVERLAPPING_RELATION);
		List<DataSourceSequence> sequences = getDocument().getDocumentGraph().getOverlappedDataSourceSequence(sNode, type);
		DataSourceSequence<Integer> sequence = (DataSourceSequence<Integer>)(DataSourceSequence<? extends Number>)sequences.get(0);
		
		event.setStart(this.getTLI(sequence.getStart().toString()));
		event.setEnd(this.getTLI(sequence.getEnd().toString()));

		SAnnotation sAnno = sNode.getAnnotation(sAnnotationQName);
		if (sAnno != null){
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
		if ((udInfo.getAttributeName() != null) && (!udInfo.getAttributeName().equals(""))) {
			sMetaAnno.setName(udInfo.getAttributeName());
			sMetaAnno.setValue(udInfo.getValue());
		}
	}

	/**
	 * This method transforms a given string to a xml conform string and returns
	 * it.
	 * 
	 * @param uncleanedString
	 *            string which possibly is not conform to xml
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
