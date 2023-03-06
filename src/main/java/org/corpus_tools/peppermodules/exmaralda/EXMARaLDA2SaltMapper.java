/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 */
package org.corpus_tools.peppermodules.exmaralda;

import com.google.common.base.Joiner;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.CommonTimeLine;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Speaker;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TIER_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.UDInformation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SMedialDS;
import org.corpus_tools.salt.common.SMedialRelation;
import org.corpus_tools.salt.common.SSequentialRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SMetaAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.semantics.SWordAnnotation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.corpus_tools.salt.util.SaltUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps data coming from the EXMARaLDA (EXB) model to a Salt model. There are some
 * properties to influence the mapping: salt.Layers= {layerName1{tierName1, tierName2,...}},
 * {layerName2{tierName3, tierName4,...}} With this property you can map some tiers of EXMARaLDA to
 * one SLAyer object. As you can see, the value of this is a list of pairs consisting of the one
 * layerName (name of the SLayer-object) and a list of tiers (Tier-objects). All the events of the
 * tier objects, mapped to an SNode will be added the SLayer-object.
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
   * casts {@link PepperModulePropertiesImpl} to {@link EXMARaLDAImporterProperties}
   **/
  public EXMARaLDAImporterProperties getProps() {
    return ((EXMARaLDAImporterProperties) this.getProperties());
  }

  /**
   * Relates the name of the tiers to the layers, to which they shall be append.
   **/
  private Map<String, SLayer> tierNames2SLayers = null;

  /**
   * Maps a token to the speaker it was created for.
   */
  private final Map<SToken, Speaker> token2Speaker = new Hashtable<>();

  /**
   * {@inheritDoc PepperMapper#setDocument(SDocument)}
   * 
   * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
   */
  @Override
  public DOCUMENT_STATUS mapSDocument() {
    if (this.getDocument().getDocumentGraph() == null)
      this.getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());

    if (getBasicTranscription() == null) {
      // load resource
      Resource resource = getResourceSet().createResource(getResourceURI());
      if (resource == null)
        throw new PepperModuleDataException(this, "Cannot load the exmaralda file: "
            + getResourceURI() + ", becuase the resource is null.");
      try {
        resource.load(null);
      } catch (IOException e) {
        throw new PepperModuleDataException(this,
            "Cannot load the exmaralda file: " + getResourceURI() + ".", e);
      }

      BasicTranscription basicTranscription = null;
      basicTranscription = (BasicTranscription) resource.getContents().get(0);
      setBasicTranscription(basicTranscription);
    }

    addProgress(0.5);
    // TODO: creates some useful indexes to speed up computing
    // createIndexes();
    this.getDocument().getDocumentGraph().setId(this.getDocument().getId());
    tierNames2SLayers = getProps().getTier2SLayers();
    if (tierNames2SLayers.size() > 0) {
      for (SLayer sLayer : tierNames2SLayers.values()) {
        getDocument().getDocumentGraph().addLayer(sLayer);
      }
    }
    this.setBasicTranscription(basicTranscription);
    // compute collection of tiers which belong together
    this.computeTierCollection();
    // mapping MetaInformation
    if (basicTranscription.getMetaInformation() != null) {
      this.mapMetaInformation2SDocument(basicTranscription, getDocument());
    }
    // mapping the speakers object
    if (getProps().isMapSpeakerMetadata()) {
      for (Speaker speaker : basicTranscription.getSpeakertable()) {
        // map all speaker objects
        this.mapSpeaker2SMetaAnnotation(speaker, getDocument());
      }
    }
    // remember the original order of the tiers
    List<String> tierDisplayNames = new LinkedList<>();
    for (Tier t : basicTranscription.getTiers()) {
      if (t.getDisplayName() != null) {
        tierDisplayNames.add(t.getDisplayName());
      }
    }
    getDocument().getDocumentGraph().createFeature(EXBNameIdentifier.EXB_NS,
        EXBNameIdentifier.EXB_TIER_ORDER, Joiner.on(',').join(tierDisplayNames));

    // mapping the timeline
    STimeline sTimeline = SaltFactory.createSTimeline();
    getDocument().getDocumentGraph().setTimeline(sTimeline);
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
      throw new PepperModuleDataException(this, "Cannot convert given exmaralda file '"
          + this.getResourceURI() + "', because no textual source layer was found.");
    }
    if ("true".equalsIgnoreCase(getProps().getCleanModel().toString())) {
      // run clean model
      cleanModel(basicTranscription, allTextSlots.keySet());
    }

    // map each text slot
    for (Map.Entry<Tier, List<Tier>> entry : allTextSlots.entrySet()) {
      Tier eTextTier = entry.getKey();
      List<Tier> textSlot = entry.getValue();

      STextualDS sTextDS = SaltFactory.createSTextualDS();
      String textName = null;
      if (getProps().qualifyTextNames()) {
        textName = eTextTier.getSpeaker().getAbbreviation() + "::" + eTextTier.getCategory();
      } else {
        textName = eTextTier.getCategory();
      }
      logger.debug("[EXMARaLDAImporter] create primary data for tier '{}'.", textName);
      sTextDS.setName(textName);

      Speaker speaker = eTextTier.getSpeaker();
      if (speaker != null) {
        // add speaker information to this specific textual DS as feature, so the
        // exporter
        // can assign the correct document meta-data (which has the speaker ID as
        // namespace)
        // to this textual DS.
        sTextDS.createFeature(EXBNameIdentifier.EXB_NS, EXBNameIdentifier.EXB_SPEAKER,
            speaker.getAbbreviation() == null ? speaker.getId() : speaker.getAbbreviation());
      }

      getDocument().getDocumentGraph().addNode(sTextDS);
      this.mapTier2STextualDS(eTextTier, sTextDS, textSlot);
    }

    // remove all text-slots as processed
    this.tierCollection.removeAll(allTextSlots.values());
    // map other tiers
    for (List<Tier> slot : this.tierCollection) {
      this.mapTiers2SNodes(slot);
    }

    if (!getProps().isMapTimeline() && sTimeline != null) {
      // remove existing timeline if it should not be included
      getDocument().getDocumentGraph().removeNode(sTimeline);
      getDocument().getDocumentGraph().setTimeline(null);
    }

    setProgress(1.0);
    return (DOCUMENT_STATUS.COMPLETED);
  }

  /**
   * Checks a given EXMARalDA model given by the {@link BasicTranscription} object if it is valid to
   * be mapped to a Salt model. Some problems occurring while mapping will be solved in this step.
   * Here is a list of problems and solutions if exist:
   * <ul>
   * <li>Given two or more {@link TLI} objects having no corresponding {@link Event} object on token
   * tier: Create artificial {@link Event} objects, a space character as value.</li>
   * </ul>
   * 
   * @param basicTranscription the model to be checked
   * @param tokenTiers a list of tiers representing the token layer
   */
  public void cleanModel(BasicTranscription basicTranscription, Collection<Tier> tokenTiers) {
    for (Tier tokenTier : tokenTiers) {
      if (tokenTier.getEvents().size() < basicTranscription.getCommonTimeLine().getTLIs().size()
          - 1) {
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
              event.setValue(" ");
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
   * Maps all metaInformation objects to SMetaAnnotation objects and adds them to the given
   * SDocument object. Also UDInformation-objects will be mapped. If a {@link BasicTranscription}
   * object contains the not empty attribute referencedFile, a {@link SMedialDS} will be created
   * containing the given {@link URI}.
   * 
   * @param basicTranscription
   * @param sDoc
   */
  private void mapMetaInformation2SDocument(BasicTranscription basicTranscription, SDocument sDoc) {
    if ((basicTranscription.getMetaInformation().getProjectName() != null)
        && (!basicTranscription.getMetaInformation().getProjectName().isEmpty())) {
      // project name
      sDoc.setName(basicTranscription.getMetaInformation().getProjectName());
    }
    if ((basicTranscription.getMetaInformation().getTranscriptionName() != null)
        && (!basicTranscription.getMetaInformation().getTranscriptionName().isEmpty())) {
      // transcription name
      SMetaAnnotation sMetaAnno = SaltFactory.createSMetaAnnotation();
      sMetaAnno.setName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_NAME);
      sMetaAnno.setValue(basicTranscription.getMetaInformation().getTranscriptionName());
      sDoc.addMetaAnnotation(sMetaAnno);
    }

    if (basicTranscription.getMetaInformation().getReferencedFile() != null) {
      // map referencedFile to SMedialDS
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
        logger.warn("[EXMARaLDAImporter] The file refered in exmaralda model '" + audioURI
            + "' does not exist and cannot be mapped to a salt model. It will be ignored.");
      } else {
        SMedialDS sAudioDS = SaltFactory.createSMedialDS();
        sAudioDS.setMediaReference(URI.createFileURI(audioFile.getPath()));
        sDoc.getDocumentGraph().addNode(sAudioDS);
      }
    }
    if ((basicTranscription.getMetaInformation().getComment() != null)
        && (!basicTranscription.getMetaInformation().getComment().isEmpty())) {
      // comment
      SMetaAnnotation sMetaAnno = SaltFactory.createSMetaAnnotation();
      sMetaAnno.setName(EXBNameIdentifier.KW_EXB_COMMENT);
      sMetaAnno.setValue(basicTranscription.getMetaInformation().getComment());
      sDoc.addMetaAnnotation(sMetaAnno);
    }
    if ((basicTranscription.getMetaInformation().getTranscriptionConvention() != null)
        && (!basicTranscription.getMetaInformation().getTranscriptionConvention().isEmpty())) {
      // project transcription convention
      SMetaAnnotation sMetaAnno = SaltFactory.createSMetaAnnotation();
      sMetaAnno.setName(EXBNameIdentifier.KW_EXB_TRANSCRIPTION_CONVENTION);
      sMetaAnno.setValue(basicTranscription.getMetaInformation().getTranscriptionConvention());
      sDoc.addMetaAnnotation(sMetaAnno);
    }
    if (basicTranscription.getMetaInformation().getUdMetaInformations() != null) {
      // map ud-informations
      this.mapUDInformations2SAnnotationContainer(
          basicTranscription.getMetaInformation().getUdMetaInformations(), sDoc);
    }
  }

  /**
   * Maps informatios of Speaker to SMetaAnnotations and adds them to the given SDocument object.
   * 
   * @param speaker speaker object to map
   * @param sDocument object to add SMetaAnnotation objects
   */
  private void mapSpeaker2SMetaAnnotation(Speaker speaker, SDocument sDocument) {
    if (sDocument == null)
      throw new PepperModuleDataException(this,
          "Exception in method 'mapSpeaker2SMetaAnnotation()'. The given SDocument-object is null. Exception occurs in file '"
              + this.getResourceURI() + "'.");
    if ((speaker != null) && (speaker.getUdSpeakerInformations() != null)) {
      // map abbreviation
      if ((speaker.getAbbreviation() != null) && (!speaker.getAbbreviation().isEmpty())) {
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_ABBR,
            speaker.getAbbreviation().toString());
      }
      // map sex
      if (speaker.getSex() != null) {
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_SEX,
            speaker.getSex().toString());
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
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_LANGUAGES_USED,
            langUsedStr.toString());
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
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_L1, l1Str.toString());
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

        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_L2, l2Str.toString());
      }
      // map comment
      if ((speaker.getComment() != null) && (!speaker.getComment().isEmpty())) {
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sDocument.createMetaAnnotation(namespace, EXBNameIdentifier.KW_EXB_COMMENT,
            speaker.getComment());
      }
      // map ud-informations
      for (UDInformation udInfo : speaker.getUdSpeakerInformations()) {
        SMetaAnnotation sMetaAnno = null;
        sMetaAnno = SaltFactory.createSMetaAnnotation();
        String namespace =
            (speaker.getAbbreviation() != null) ? speaker.getAbbreviation() : speaker.getId();
        sMetaAnno.setNamespace(namespace);
        sMetaAnno.setName(udInfo.getAttributeName());
        sMetaAnno.setValue(udInfo.getValue());
        if (sDocument.getMetaAnnotation(sMetaAnno.getQName()) == null) {
          // only create the meta-annotation, if it does not still
          // exists
          sDocument.addMetaAnnotation(sMetaAnno);
        }
      }
    }
  }

  /**
   * Mapps a list of UDInformation objects to a list of SMetaAnnotation objects and adds them to the
   * given sOwner.
   * 
   * @param udInformations
   * @param sOwner
   */
  private void mapUDInformations2SAnnotationContainer(List<UDInformation> udInformations,
      SAnnotationContainer sOwner) {
    SMetaAnnotation sMetaAnno = null;
    for (UDInformation udInfo : udInformations) {
      if (udInfo.getAttributeName() != null && !udInfo.getAttributeName().trim().isEmpty()) {
        sMetaAnno = SaltFactory.createSMetaAnnotation();
        sMetaAnno.setName(udInfo.getAttributeName());
        sMetaAnno.setValue(udInfo.getValue());
        sOwner.addMetaAnnotation(sMetaAnno);
      }
    }
  }

  /**
   * Stores tiers, which belongs together if mode is merged.
   */
  private List<List<Tier>> tierCollection = null;

  /**
   * Compute which tiers belong together and stores them in tierCollection. Therefore the property
   * {@link EXMARaLDAImporterProperties#getTierMerge()} is used.
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
          List<Tier> slot = new ArrayList<>();
          this.tierCollection.add(slot);
          for (String tierCat : tierCategories) {
            tierCat = tierCat.trim();
            // searching for tier
            for (Tier tier : this.getBasicTranscription().getTiers()) {
              if (tier.getCategory() == null)
                throw new PepperModuleDataException(this,
                    "Cannot convert given exmaralda file '" + this.getResourceURI()
                        + "', because there is a <tier> element ('id=\"" + tier.getId()
                        + "\"') without a @category attribute.");;
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

      if (tier.getType() == TIER_TYPE.D && !getProps().isMapDescriptions()) {
        continue;
      }

      logger.debug("[EXMARaLDAImporter] mapping tier '{}'. ", tier.getCategory());
      SLayer sLayer = null;
      if ((this.tierNames2SLayers != null)) {
        // if current tier shall be added to a layer
        sLayer = this.tierNames2SLayers.get(tier.getCategory());
      }

      for (Event eEvent : tier.getEvents()) {
        SSpan sSpan = SaltFactory.createSSpan();
        getDocument().getDocumentGraph().addNode(sSpan);
        this.mapEvent2SNode(tier, eEvent, sSpan);

        if (sLayer != null) {
          // if current tier shall be added to a layer, than add sSpan
          // to SLayer
          sSpan.addLayer(sLayer);
        }

        // creating semanticalAnnotation for token
        mapSStructuredNode2SemanticAnnotation(tier, sSpan);

        this.mapUDInformations2SAnnotationContainer(eEvent.getUdInformations(), sSpan);

        Integer startPos =
            getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getStart());
        if (startPos < 0) {
          if (getBasicTranscription().getCommonTimeLine().getTLIs().contains(eEvent.getStart())) {
            logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue()
                + "' of tier '" + tier.getCategory()
                + "' because its start value reffering to timeline is less than 0.");
          } else {
            logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue()
                + "' of tier '" + tier.getCategory()
                + "' because this event is not connected to the timeline.");
          }
          break;
        }
        Integer endPos =
            getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(eEvent.getEnd());
        if (endPos < 0) {
          if (getBasicTranscription().getCommonTimeLine().getTLIs().contains(eEvent.getEnd())) {
            logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue()
                + "' of tier " + tier.getCategory()
                + "' because its end value reffering to timeline is less than 0.");
          } else {
            logger.warn("[EXMARaLDAImporter] Can not map an event '" + eEvent.getValue()
                + "' of tier " + tier.getCategory()
                + "' because this event is not connected to the timeline.");
          }
          break;
        }
        DataSourceSequence sequence = new DataSourceSequence();
        sequence.setStart(startPos);
        sequence.setEnd(endPos);
        sequence.setDataSource(getDocument().getDocumentGraph().getTimeline());

        // TODO: use an index to get overlapped token
        List<SToken> sTokens = getAdjacentSTokens(sequence);

        // filter out tokens that do not belong to the right STextualDS
        ListIterator<SToken> itTokens = sTokens.listIterator();
        while (itTokens.hasNext()) {
          SToken tok = itTokens.next();
          Speaker tokSpeaker = token2Speaker.get(tok);
          if (tokSpeaker != eEvent.getTier().getSpeaker()) {
            itTokens.remove();
          }
        }

        if (sTokens.isEmpty()) {
          String tierCaption = tier.getCategory();
          if (tier.getSpeaker() != null && tier.getSpeaker().getAbbreviation() != null) {
            tierCaption = tierCaption + " (" + tier.getSpeaker().getAbbreviation() + ")";
          }
          String start = eEvent.getStart().getTime() == null ? eEvent.getStart().getId()
              : eEvent.getStart().getTime();
          String end = eEvent.getEnd().getTime() == null ? eEvent.getEnd().getId()
              : eEvent.getEnd().getTime();

          if (!getProps().getCleanModel()) {
            logger.warn("There are no matching tokens found on token-tier " + "for current tier: '"
                + tierCaption + "' in event starting at '" + start + "' and ending at '" + end
                + "' having the value '" + eEvent.getValue()
                + "'. The problem was detected in file '" + this.getResourceURI()
                + "'. You can try to set the property \"cleanModel\" to \"true\".");
          } else {
            logger.warn("There are no matching tokens found on token-tier " + "for current tier: '"
                + tierCaption + "' in event starting at '" + start + "' and ending at '" + end
                + "' having the value '" + eEvent.getValue()
                + "'. The problem was detected in file '" + this.getResourceURI()
                + "'. Unfortunatly property '" + EXMARaLDAImporterProperties.PROP_CLEAN_MODEL
                + "' did not helped here. ");
          }
        } else {

          for (SToken sToken : sTokens) {
            SSpanningRelation spanRel = SaltFactory.createSSpanningRelation();
            spanRel.setSource(sSpan);
            spanRel.setTarget(sToken);
            this.getDocument().getDocumentGraph().addRelation(spanRel);
          }
        }
        // medium and url to SAnnotation
        this.mapMediumURL2SSNode(eEvent, sSpan);
      }
    }
  }

  /**
   * Returns a list of tokens, which are not exactly in the passed sequence. That means, each token
   * starting or ending inside the range is returned. This method should help in case of a token
   * tier was not minimal. For instance imagine a tier containing A, B and C.
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
  public List<SToken> getAdjacentSTokens(DataSourceSequence<Number> sequence) {
    List<SToken> sTokens = new ArrayList<>();
    List<? extends SSequentialRelation> sSeqRels = null;
    if (sequence.getDataSource() instanceof STextualDS) {
      sSeqRels = getDocument().getDocumentGraph().getTextualRelations();
    } else if (sequence.getDataSource() instanceof STimeline) {
      sSeqRels = getDocument().getDocumentGraph().getTimelineRelations();
    } else {
      throw new PepperModuleException(this,
          "Cannot compute overlaped nodes, because the given dataSource is not supported by this method.");
    }
    for (SSequentialRelation rel : sSeqRels) {
      // walk through all textual relations
      if (sequence.getDataSource().equals(rel.getTarget())) {
        if (rel.getSource() instanceof SToken) {
          if (rel instanceof STextualRelation || rel instanceof STimelineRelation) {
            DataSourceSequence<Integer> intSeq =
                (DataSourceSequence<Integer>) (DataSourceSequence<? extends Number>) sequence;
            if (((Integer) rel.getStart() <= intSeq.getStart())
                && ((Integer) rel.getEnd() > intSeq.getStart())) {
              sTokens.add((SToken) rel.getSource());
            } else if (((Integer) rel.getStart() >= intSeq.getStart())
                && ((Integer) rel.getStart() < intSeq.getEnd())) {
              sTokens.add((SToken) rel.getSource());
            }
          }
        }
      }
    }
    return (sTokens);
  }

  /**
   * Maps the medium and url of an event and maps it to SAnnotations of an SNode.
   * 
   * @param event
   * @param sNode
   */
  private void mapMediumURL2SSNode(Event event, SNode sNode) {
    // mapMedium to SAnnotation
    if (event.getMedium() != null) {
      sNode.createAnnotation(null, EXBNameIdentifier.KW_EXB_EVENT_MEDIUM,
          event.getMedium().toString());
    }
    // mapURL to SAnnotation
    if (event.getUrl() != null) {
      sNode.createAnnotation(null, EXBNameIdentifier.KW_EXB_EVENT_URL, event.getUrl().toString());
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
      sTimeLine.increasePointOfTime();
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
  public void mapEvent2SToken(Event eEvent, CommonTimeLine eCTimeline, SToken sToken,
      STimeline sTime) {
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
    STimelineRelation sTimeRel = SaltFactory.createSTimelineRelation();
    sTimeRel.setTarget(sTime);
    sTimeRel.setSource(sToken);
    sTimeRel.setStart(startPos);
    sTimeRel.setEnd(endPos);
    this.getDocument().getDocumentGraph().addRelation(sTimeRel);
  }

  /**
   * Maps a tier to STextualDS, creates SToken objects and relates them by STextualRelation to
   * STextualDS.
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
        if (getProps().isTrimEvents()) {
          text.append(eventValue.trim());
        } else {
          text.append(eventValue);
        }
      }
      end = text.length();
      String sep = this.getTokenSepearator();
      if ((eventValue != null) && (sep != null)) {
        text.append(sep);
      }
      // creating and adding token
      SToken sToken = SaltFactory.createSToken();
      getDocument().getDocumentGraph().addNode(sToken);
      if (this.tierNames2SLayers != null) {
        // add sToken to layer if required
        SLayer sLayer = this.tierNames2SLayers.get(eTextTier.getCategory());
        if (sLayer != null) {
          sToken.addLayer(sLayer);
        }
      }
      if (event.getTier().getSpeaker() != null) {
        token2Speaker.put(sToken, event.getTier().getSpeaker());
      }
      // creating annotation for token
      this.mapUDInformations2SAnnotationContainer(event.getUdInformations(), sToken);
      // creating semanticalAnnotation for token
      mapSStructuredNode2SemanticAnnotation(eTextTier, sToken);
      // medium and url to SAnnotation
      this.mapMediumURL2SSNode(event, sToken);

      // creating textual relation
      STextualRelation sTextRel = SaltFactory.createSTextualRelation();
      sTextRel.setTarget(sText);
      sTextRel.setSource(sToken);
      sTextRel.setStart(start);
      sTextRel.setEnd(end);
      getDocument().getDocumentGraph().addRelation(sTextRel);

      if ((getDocument().getDocumentGraph().getMedialDSs() != null)
          && (getDocument().getDocumentGraph().getMedialDSs().size() > 0)
          && ((event.getStart().getTime() != null) || (event.getEnd().getTime() != null))) {
        // start: creating SMedialRelation
        try {
          Double audioStart = null;
          if (event.getStart().getTime() != null)
            audioStart = Double.valueOf(event.getStart().getTime());
          Double audioEnd = null;
          if (event.getEnd().getTime() != null)
            audioEnd = Double.valueOf(event.getEnd().getTime());

          SMedialRelation sAudioDSRelation = SaltFactory.createSMedialRelation();
          sAudioDSRelation.setSource(sToken);
          sAudioDSRelation.setTarget(getDocument().getDocumentGraph().getMedialDSs().get(0));
          sAudioDSRelation.setStart(audioStart);
          sAudioDSRelation.setEnd(audioEnd);
          getDocument().getDocumentGraph().addRelation(sAudioDSRelation);
        } catch (NumberFormatException e) {
          logger.warn(
              "[EXMARaLDAImporter] Cannot map time attribute of timeline to SStart or SEnd, because value '"
                  + event.getStart().getTime() + "' is not mappable to a double value.");
        }
      }

      // creating timelineRel
      this.mapEvent2SToken(event, this.getBasicTranscription().getCommonTimeLine(), sToken,
          getDocument().getDocumentGraph().getTimeline());
    }
    sText.setText(text.toString());

    for (Tier tier : textSlot) {
      if (!tier.equals(eTextTier)) {
        // if tier is annotation tier
        for (Event event : tier.getEvents()) {
          Integer startPos =
              getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getStart());
          Integer endPos =
              getBasicTranscription().getCommonTimeLine().getTLIs().indexOf(event.getEnd());

          DataSourceSequence sequence = new DataSourceSequence();
          sequence.setStart(startPos);
          sequence.setEnd(endPos);
          sequence.setDataSource(getDocument().getDocumentGraph().getTimeline());

          List<SToken> sTokens = getDocument().getDocumentGraph().getTokensBySequence(sequence);

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
      SAnnotation sAnno = SaltFactory.createSPOSAnnotation();
      if (getProps().isTrimEvents()) {
        sAnno.setValue(eEvent.getValue().trim());
      } else {
        sAnno.setValue(eEvent.getValue());
      }
      sNode.addAnnotation(sAnno);
    } else if (tier.getCategory().equalsIgnoreCase(lemmaTier)) {
      // this tier has special annotations: lemma annotations
      SAnnotation sAnno = SaltFactory.createSLemmaAnnotation();
      if (getProps().isTrimEvents()) {
        sAnno.setValue(eEvent.getValue().trim());
      } else {
        sAnno.setValue(eEvent.getValue());
      }
      sNode.addAnnotation(sAnno);
    } else if ((uriTiers != null) && (uriTiers.contains(tier.getCategory()))) {
      String pathName = this.getResourceURI().toFileString()
          .replace(this.getResourceURI().lastSegment(), eEvent.getValue());
      File file = new File(pathName);
      if (!file.exists()) {
        logger.warn("[EXMARaLDAImporter] Cannot add the uri-annotation '" + eEvent.getValue()
            + "' of tier '" + tier.getCategory() + "', because the file '" + pathName
            + "' does not exist.");
      } else {
        URI corpusFilePath = URI.createFileURI(file.getAbsolutePath());
        sNode.createAnnotation(null, tier.getCategory(), corpusFilePath.toFileString());
      }
    } else {
      String namespace = null;
      String name = tier.getCategory();

      if (getProps().isParseNamespace()) {
        Pair<String, String> splitted = SaltUtil.splitQName(tier.getCategory());
        namespace = splitted.getLeft();
        name = splitted.getRight();
      } else if (eEvent.getTier().getSpeaker() != null) {
        namespace = eEvent.getTier().getSpeaker().getAbbreviation();
      }

      if (getProps().isTrimEvents()) {
        String value = eEvent.getValue() == null ? null : eEvent.getValue().trim();
        sNode.createAnnotation(namespace, name, value);
      } else {
        sNode.createAnnotation(namespace, name, eEvent.getValue());
      }
    }
    if ((eEvent.getUdInformations() != null) && (eEvent.getUdInformations().size() > 0)) {
      this.mapUDInformations2SAnnotationContainer(eEvent.getUdInformations(), sNode);
    }

  }

  /**
   * Creates additional semantic annotations to given node if necessary. E.g. marking the given node
   * as Word.
   * 
   * @param tier
   * @param sStructuredNode
   */
  private void mapSStructuredNode2SemanticAnnotation(Tier tier, SStructuredNode sStructuredNode) {
    // check if tier is word tier
    if (tier.getCategory().equalsIgnoreCase(this.getSWordTier())) {
      SWordAnnotation sWordAnno = SaltFactory.createSWordAnnotation();
      sStructuredNode.addAnnotation(sWordAnno);
    }
  }

  private String getTokenSepearator() {
    String retVal = null;
    if ((this.getProps().getTokenSeparator() != null)
        && (!this.getProps().getTokenSeparator().isEmpty())) {
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
   * Returns the category name of tier, which has to be annoted additional as Word.
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
