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
package org.corpus_tools.peppermodules.exmaralda.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;

import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.peppermodules.exmaralda.EXMARaLDA2SaltMapper;
import org.corpus_tools.peppermodules.exmaralda.EXMARaLDAImporterProperties;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.CommonTimeLine;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.MetaInformation;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;

/**
 * Tests the mappers for a correct mapping of audio data.
 * 
 * @author Florian Zipser
 *
 */
public class AudioTest {
	File testFolder = new File("./src/test/resources/EXMARaLDAImporter/Audio1/");
	URI temproraryURI = URI.createFileURI("_TMP/de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.exmaralda");

	private EXMARaLDA2SaltMapper fixture = null;

	public EXMARaLDA2SaltMapper getFixture() {
		return fixture;
	}

	public void setFixture(EXMARaLDA2SaltMapper fixture) {
		this.fixture = fixture;
		getFixture().setProperties(new EXMARaLDAImporterProperties());
	}

	@Before
	public void setUp() throws Exception {
		this.setFixture(new EXMARaLDA2SaltMapper());
	}

	/**
	 * Checks one token referring to a start and end {@link TLI} object, both
	 * having a time value.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testImportAudio1() throws MalformedURLException {
		File testFile = new File(testFolder.getAbsolutePath() + "/WikinewsSpecialReportAyala.ogg");

		String txtTier = "txt";
		String token1 = "Word";
		Double start = 1.0;
		Double end = 2.5677097899;
		URI refFile = URI.createFileURI(testFile.getAbsolutePath());

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProperties().getProperty(EXMARaLDAImporterProperties.PROP_TOKEN_TIER);
		prop.setValue(txtTier);

		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		MetaInformation metaInformation = ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
		metaInformation.setReferencedFile(refFile.toString());
		basicTranscription.setMetaInformation(metaInformation);

		CommonTimeLine timeline = ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();

		TLI tli1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setTime(start.toString());
		timeline.getTLIs().add(tli1);

		TLI tli2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setTime(end.toString());
		timeline.getTLIs().add(tli2);

		basicTranscription.setCommonTimeLine(timeline);

		Tier tier1 = ExmaraldaBasicFactory.eINSTANCE.createTier();

		Event event1 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setStart(tli1);
		event1.setEnd(tli2);
		event1.setValue(token1);

		tier1.getEvents().add(event1);
		tier1.setCategory(txtTier);

		basicTranscription.getTiers().add(tier1);

		SDocument sDoc = SaltFactory.createSDocument();
		getFixture().setDocument(sDoc);
		getFixture().setBasicTranscription(basicTranscription);
		getFixture().mapSDocument();

		assertNotNull(sDoc.getDocumentGraph());
		assertNotNull(sDoc.getDocumentGraph().getTextualDSs());
		assertEquals(1, sDoc.getDocumentGraph().getTextualDSs().size());
		assertEquals(token1, sDoc.getDocumentGraph().getTextualDSs().get(0).getText());
		assertEquals(1, sDoc.getDocumentGraph().getTokens().size());
		assertEquals(1, sDoc.getDocumentGraph().getMedialDSs().size());
		assertEquals(refFile, sDoc.getDocumentGraph().getMedialDSs().get(0).getMediaReference());
		assertEquals(1, sDoc.getDocumentGraph().getMedialRelations().size());
		assertEquals(start, sDoc.getDocumentGraph().getMedialRelations().get(0).getStart());
		assertEquals(end, sDoc.getDocumentGraph().getMedialRelations().get(0).getEnd());
	}

	/**
	 * Checks two tokens, first referring to a time value in start {@link TLI}
	 * object, the second referring to a time value in end {@link TLI} object.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	public void testImportAudio2() throws MalformedURLException {
		File testFile = new File(testFolder.getAbsolutePath() + "/WikinewsSpecialReportAyala.ogg");

		String txtTier = "txt";
		String token1 = "Any";
		String token2 = "sample";
		Double start = 1.0;
		Double end = 2.5677097899;
		URI refFile = URI.createFileURI(testFile.getAbsolutePath());

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) getFixture().getProperties().getProperty(EXMARaLDAImporterProperties.PROP_TOKEN_TIER);
		prop.setValue(txtTier);

		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		MetaInformation metaInformation = ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
		metaInformation.setReferencedFile(refFile.toString());
		basicTranscription.setMetaInformation(metaInformation);

		CommonTimeLine timeline = ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();

		TLI tli1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setTime(start.toString());
		timeline.getTLIs().add(tli1);

		TLI tli2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		timeline.getTLIs().add(tli2);

		TLI tli3 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setTime(end.toString());
		timeline.getTLIs().add(tli3);

		basicTranscription.setCommonTimeLine(timeline);

		Tier tier1 = ExmaraldaBasicFactory.eINSTANCE.createTier();

		Event event1 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setStart(tli1);
		event1.setEnd(tli2);
		event1.setValue(token1);
		tier1.getEvents().add(event1);

		Event event2 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event2.setStart(tli2);
		event2.setEnd(tli3);
		event2.setValue(token2);
		tier1.getEvents().add(event2);

		tier1.setCategory(txtTier);

		basicTranscription.getTiers().add(tier1);

		SDocument sDoc = SaltFactory.createSDocument();
		getFixture().setDocument(sDoc);
		getFixture().setBasicTranscription(basicTranscription);
		getFixture().mapSDocument();

		assertNotNull(sDoc.getDocumentGraph());
		assertNotNull(sDoc.getDocumentGraph().getTextualDSs());
		assertEquals(1, sDoc.getDocumentGraph().getTextualDSs().size());
		assertTrue(sDoc.getDocumentGraph().getTextualDSs().get(0).getText().contains(token1));
		assertTrue(sDoc.getDocumentGraph().getTextualDSs().get(0).getText().contains(token2));

		assertEquals(2, sDoc.getDocumentGraph().getTokens().size());

		assertEquals(1, sDoc.getDocumentGraph().getMedialDSs().size());
		assertEquals(refFile, sDoc.getDocumentGraph().getMedialDSs().get(0).getMediaReference());

		// start: check audio relations
		assertEquals(2, sDoc.getDocumentGraph().getMedialRelations().size());
		assertEquals(start, sDoc.getDocumentGraph().getMedialRelations().get(0).getStart());
		assertNull(sDoc.getDocumentGraph().getMedialRelations().get(0).getEnd());

		assertNull(sDoc.getDocumentGraph().getMedialRelations().get(1).getStart());
		assertEquals(end, sDoc.getDocumentGraph().getMedialRelations().get(1).getEnd());
		// end: check audio relations
	}
}
