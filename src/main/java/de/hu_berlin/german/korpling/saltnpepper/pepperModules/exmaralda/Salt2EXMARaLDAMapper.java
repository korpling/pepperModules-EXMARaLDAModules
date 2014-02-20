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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
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
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.DOCUMENT_STATUS;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleDataException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.modules.SDocumentDataEnricher;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.modules.SDocumentStructureAccessor;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.modules.SDocumentStructureAccessor.POTPair;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructuredNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STimeline;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SMetaAnnotation;

public class Salt2EXMARaLDAMapper extends PepperMapperImpl
{
// -------------------- basic transcription	
	public void setBasicTranscription(BasicTranscription basicTranscription) {
		this.basicTranscription = basicTranscription;
	}

	public BasicTranscription getBasicTranscription() {
		return basicTranscription;
	}

	

	private BasicTranscription basicTranscription= null;
// -------------------- basic transcription	
// -------------------- start: helping structures
	private EList<TLI2PointOfTime> tLI2PointOfTimeList = new BasicEList<TLI2PointOfTime>();
	private class TLI2PointOfTime
	{
		public TLI tli= null;
		
		public String pointOfTime= null;
	}
	
	private TLI getTLI(String sPointOfTime)
	{
		TLI retVal= null;
		for (TLI2PointOfTime tli2pot: tLI2PointOfTimeList)
		{
			if (tli2pot.pointOfTime.equalsIgnoreCase(sPointOfTime))
			{
				retVal= tli2pot.tli;
				break;
			}
		}
		return(retVal);
	}
// -------------------- end: helping structures
	
	/**
	 * {@inheritDoc PepperMapper#setSDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getSDocument().getSDocumentGraph()== null)
			getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
		
		this.setBasicTranscription(ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription());
		
		// mapping for MetaInformation
			MetaInformation metaInformation= ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
			basicTranscription.setMetaInformation(metaInformation);
			this.mapSDocuent2MetaInfo(getSDocument(), metaInformation);
		
		//creating timeline
			if (this.getSDocument().getSDocumentGraph().getSTimeline()== null)
			{//if no timeline is included, create one
				SDocumentDataEnricher dataEnricher= new SDocumentDataEnricher();
				dataEnricher.setSDocumentGraph(this.getSDocument().getSDocumentGraph());
				dataEnricher.createSTimeline();
			}
			CommonTimeLine cTimeLine= ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();
			basicTranscription.setCommonTimeLine(cTimeLine);
			this.map2CommonTimeLine(getSDocument().getSDocumentGraph().getSTimeline(), cTimeLine);
		
		//creating token tier
			Tier tokenTier= ExmaraldaBasicFactory.eINSTANCE.createTier();
			basicTranscription.getTiers().add(tokenTier);
			this.mapSToken2Tier(getSDocument().getSDocumentGraph().getSTokens(), tokenTier);
		//map all SStructuredNodes to tiers
			
			EList<SStructuredNode> structuredNodes= new BasicEList<SStructuredNode>();
			//add all SToken to mapping list 
			structuredNodes.addAll(getSDocument().getSDocumentGraph().getSTokens());	
			//add all SToken to mapping list 
			structuredNodes.addAll(getSDocument().getSDocumentGraph().getSSpans());
			//add all SToken to mapping list 
			structuredNodes.addAll(getSDocument().getSDocumentGraph().getSStructures());
			
			//map
			this.mapSStructuredNode2Tiers(structuredNodes);
			
			saveToFile(basicTranscription);
			
		return(DOCUMENT_STATUS.COMPLETED);
	}
	
	private void saveToFile(BasicTranscription basicTranscription)
	{
		// create resource set and resource 
		ResourceSet resourceSet = new ResourceSetImpl();
		// Register XML resource factory
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(EXMARaLDAExporter.FILE_EXTENION,new EXBResourceFactory());
		//load resource 
		Resource resource = resourceSet.createResource(getResourceURI());
		if (resource== null)
			throw new PepperModuleDataException(this, "Cannot save a resource to uri '"+getResourceURI()+"', because the given resource is null.");
		
		resource.getContents().add(basicTranscription);
		try{
			resource.save(null);
		} catch (IOException e)
		{ throw new PepperModuleDataException(this, "Cannot write exmaradla basic transcription to uri '"+getResourceURI()+"'.", e);}
	}
	
	
	/**
	 * Maps all SMetaAnnotations of document to MetaInformation or UDInformation
	 * @param sDoc
	 * @param metaInfo
	 */
	private void mapSDocuent2MetaInfo(SDocument sDoc, MetaInformation metaInfo)
	{
		//map SMeatAnnotations2udInformation
		for (SMetaAnnotation sMetaAnno: sDoc.getSMetaAnnotations())
		{
			//map project name
			if (sMetaAnno.getSName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_PROJECT_NAME))
			{
				metaInfo.setProjectName(sMetaAnno.getValueString());
			}
			//map transcription name
			else if (sMetaAnno.getSName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME))
				metaInfo.setTranscriptionName(sMetaAnno.getValueString());
			//map referenced file
			else if (sMetaAnno.getSName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_REFERENCED_FILE))
			{	try {
					metaInfo.setReferencedFile(new URL(sMetaAnno.getValueString()));
				} catch (MalformedURLException e) {
				}
			}
			else if (sMetaAnno.getSName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_COMMENT))
				metaInfo.setComment(sMetaAnno.getValueString());
			//map transcription convention
			else if (sMetaAnno.getSName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION))
				metaInfo.setTranscriptionConvention(sMetaAnno.getValueString());
			else 
			{
				UDInformation udInfo= ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
				this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
				metaInfo.getUdMetaInformations().add(udInfo);
			}
		}
	}
	
	/**
	 * Creates content of a common timeline, and also creates all TLI�s.
	 * @param sTimeline
	 * @param cTimeLine
	 */
	private void map2CommonTimeLine(STimeline sTimeline, CommonTimeLine cTimeLine)
	{
		if (	(sTimeline== null)||
				(sTimeline.getSPointsOfTime()== null)||
				(sTimeline.getSPointsOfTime().size()==0));
		{
			this.getSDocument().getSDocumentGraph().createSTimeline();
			sTimeline= this.getSDocument().getSDocumentGraph().getSTimeline();
		}
		String TLI_id= "T";
		int i= 0;
		for (String pointOfTime: sTimeline.getSPointsOfTime())
		{
			TLI tli= ExmaraldaBasicFactory.eINSTANCE.createTLI();
			cTimeLine.getTLIs().add(tli);
			tli.setTime(pointOfTime);
			tli.setId(TLI_id+i);
			i++;
			//put TLI to list
			TLI2PointOfTime tliPOT= new TLI2PointOfTime();
			tliPOT.pointOfTime= pointOfTime;
			tliPOT.tli= tli;
			this.tLI2PointOfTimeList.add(tliPOT);
		}
	}
	/**
	 * stores number of created tiers
	 */
	private Integer numOfTiers= 0;
	private Integer getNewNumOfTiers()
	{
		int num= numOfTiers;
		numOfTiers++;
		return(num);
	}
	
	/**
	 * Stores the prefix for tier id
	 */
	public String TIER_ID_PREFIX= "TIE";
	/**
	 * Stores the name of the tier, which contains tokenization
	 */
	public String TIER_NAME_TOKEN= "tok"; 
	
	/**
	 * Maps a list of token to a tier. That means, that a textual tier will be created.
	 * It calls mapSToken2Event().
	 * <br/>
	 * Please take care, that the mapping for SToken-annotations has to be treated seperatly
	 * @param sTokens
	 * @param tier
	 */
	private void mapSToken2Tier(EList<SToken> sTokens, Tier tier)
	{
		tier.setCategory(TIER_NAME_TOKEN);
		tier.setDisplayName("["+TIER_NAME_TOKEN+"]");
		tier.setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
		tier.setType(TIER_TYPE.T);
		for (SToken sToken: sTokens)
		{
			Event event= ExmaraldaBasicFactory.eINSTANCE.createEvent();
			tier.getEvents().add(event);
			this.mapSToken2Event(sToken, event);
		}
	}
	
	/**
	 * Maps one token to one event.
	 * @param sToken
	 * @param event
	 */
	private void mapSToken2Event(SToken sToken, Event event)
	{
		SDocumentStructureAccessor acc= new SDocumentStructureAccessor();
		acc.setSDocumentGraph(this.getSDocument().getSDocumentGraph());
		String text= acc.getSOverlappedText(sToken);
		event.setValue(text);
		POTPair potPair= acc.getPOT(sToken);
		
		if (potPair== null)
			throw new PepperModuleDataException(this, "Cannot map token to event, because there is no point of time for SToken: "+ sToken.getSId());
		if (potPair.getStartPot()== null)
			throw new PepperModuleDataException(this, "Cannot map token to event, because start of pot for following token is empty: "+ sToken.getSId());
		if (potPair.getEndPot()== null)
			throw new PepperModuleDataException(this, "Cannot map token to event, because end of pot for following token is empty: "+ sToken.getSId());
		event.setStart(this.getTLI(potPair.getStartPot().toString()));
		event.setEnd(this.getTLI(potPair.getEndPot().toString()));
	}
	
	/**
	 * Maps a a SStructuredNode-object to a tier. Therefore it takes all the annotations and 
	 * creates one tier for each.
	 * <br/>
	 * Please take attention, that SToken-object shall be mapped by mapSToken2Tier() additionally to
	 * create a tier for text.
	 * @param sNodes
	 * @param tier
	 */
	private void mapSStructuredNode2Tiers(EList<SStructuredNode> sNodes)
	{
		//compute a table, which stores the names of tiers, and the corresponding sAnnotationQName objects
		Map<String, Tier> annoName2Tier= new Hashtable<String, Tier>();
		for (SStructuredNode sNode: sNodes)
		{//walk through the given list
			for (SAnnotation sAnno : sNode.getSAnnotations())
			{
				Tier currTier= null;
				if (annoName2Tier.containsKey(sAnno.getQName()))
				{//if annoName2Tier contains QName, than return 
					currTier= annoName2Tier.get(sAnno.getQName());
				}
				else 
				{// create new entry in annoName2Tier
					currTier= ExmaraldaBasicFactory.eINSTANCE.createTier();
					{//create everything for tier
						currTier.setCategory(sAnno.getSName());
						currTier.setDisplayName("["+sAnno.getSName()+"]");
						currTier.setId(TIER_ID_PREFIX + this.getNewNumOfTiers());
						currTier.setType(TIER_TYPE.T);
					}
					this.basicTranscription.getTiers().add(currTier);
					annoName2Tier.put(sAnno.getQName(), currTier);
				}
				if (	(!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM) &&
						(!sAnno.getQName().equalsIgnoreCase(EXBNameIdentifier.KW_EXB_EVENT_URL))))
				{		
					Event event= ExmaraldaBasicFactory.eINSTANCE.createEvent();
					currTier.getEvents().add(event);
					SAnnotation sMediumAnno= sNode.getSAnnotation(EXBNameIdentifier.KW_EXB_EVENT_MEDIUM);
					SAnnotation sURLAnno= sNode.getSAnnotation(EXBNameIdentifier.KW_EXB_EVENT_URL);
					if (sMediumAnno!= null)
						event.setMedium(EVENT_MEDIUM.get(sMediumAnno.getSValue().toString()));
					if (sURLAnno!= null)
					{	
						try {
							event.setUrl(new URL(sMediumAnno.getSValue().toString()));
						} catch (MalformedURLException e) 
						{}
					}
					this.mapSStructuredNode2Event(sNode, sAnno.getQName(), event);
				}
			}
		}
	}
	
	/**
	 * Maps a structuredNode to an event.
	 * @param sNode
	 * @param sAnnotationQName
	 * @param event
	 */
	private void mapSStructuredNode2Event(SStructuredNode sNode, String sAnnotationQName, Event event)
	{
		SDocumentStructureAccessor acc= new SDocumentStructureAccessor();
		acc.setSDocumentGraph(this.getSDocument().getSDocumentGraph());
		POTPair potPair= acc.getPOT(sNode);
		event.setStart(this.getTLI(potPair.getStartPot().toString()));
		event.setEnd(this.getTLI(potPair.getEndPot().toString()));
		
		SAnnotation sAnno= sNode.getSAnnotation(sAnnotationQName);
		if (sAnno!= null)
			event.setValue(this.stringXMLConformer(sAnno.getSValueSTEXT()));
		//map SMeatAnnotations2udInformation
		for (SMetaAnnotation sMetaAnno: sNode.getSMetaAnnotations())
		{
			UDInformation udInfo= ExmaraldaBasicFactory.eINSTANCE.createUDInformation();
			this.mapSMetaAnnotation2UDInformation(sMetaAnno, udInfo);
			event.getUdInformations().add(udInfo);
		}
	}
	
	/**
	 * Maps a meta annotation to a udInformation
	 * @param sMetaAnno
	 * @param udInfo
	 */
	private void mapSMetaAnnotation2UDInformation(SMetaAnnotation sMetaAnno, UDInformation udInfo)
	{
		if (	(udInfo.getAttributeName()!= null) &&
				(!udInfo.getAttributeName().equals("")))
		{
			sMetaAnno.setSName(udInfo.getAttributeName());
			sMetaAnno.setSValue(udInfo.getValue());
		}
	}
	
	/**
	 * This method transforms a given string to a xml conform string and returns it.
	 * 
	 * @param uncleanedString string which possibly is not conform to xml 
	 * @return
	 */
	private String stringXMLConformer(String uncleanedString)
	{
		String retString= uncleanedString;
		if (retString!= null)
		{	
			retString = StringEscapeUtils.escapeXml(uncleanedString);
			
			retString= retString.replace("Ä", "&#196;");
			retString= retString.replace("Ö", "&#214;");
			retString= retString.replace("Ü", "&#220;");
			retString= retString.replace("ä", "&#228;");
			retString= retString.replace("ö", "&#246;");
			retString= retString.replace("ü", "&#252;");
			retString= retString.replace("ß", "&#223;");
		}			
		return(retString);
	}
}
