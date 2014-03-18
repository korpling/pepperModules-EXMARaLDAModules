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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperModuleProperty;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDA2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDAImporterProperties;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;

/**
 * Tests the mappers for a correct mapping of audio data.
 * @author Florian Zipser
 *
 */
public class AudioTest
{	
	File testFolder= new File("./src/test/resources/EXMARaLDAImporter/Audio1/");
	URI temproraryURI= URI.createFileURI("_TMP/de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.exmaralda");
	
	private EXMARaLDA2SaltMapper fixture= null;
	
	public EXMARaLDA2SaltMapper getFixture() {
		return fixture;
	}

	public void setFixture(EXMARaLDA2SaltMapper fixture) {
		this.fixture = fixture;
		this.getFixture().setProperties(new EXMARaLDAImporterProperties());
	}

	@Before
	public void setUp() throws Exception 
	{
		this.setFixture(new EXMARaLDA2SaltMapper());
	}

	/**
	 * Checks one token referring to a start and end {@link TLI} object, both having a time value.
	 * @throws MalformedURLException
	 */
	@Test
	public void testImportAudio1() throws MalformedURLException
	{
		File testFile= new File(testFolder.getAbsolutePath()+ "/WikinewsSpecialReportAyala.ogg");
		
		String txtTier= "txt";
		String token1= "Word";
		Double start= 1.0;
		Double end= 2.5677097899;
		URI refFile= URI.createFileURI(testFile.getAbsolutePath());
		
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProperties().getProperty(EXMARaLDAImporterProperties.PROP_TOKEN_TIER);
		prop.setValue(txtTier);
		
		URL referencedFile= new URL(refFile.toString());
		BasicTranscription basicTranscription= ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		MetaInformation metaInformation= ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
		metaInformation.setReferencedFile(referencedFile);
		basicTranscription.setMetaInformation(metaInformation);
		
		CommonTimeLine timeline= ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();
		
		TLI tli1= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setTime(start.toString());
		timeline.getTLIs().add(tli1);
		
		TLI tli2= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setTime(end.toString());
		timeline.getTLIs().add(tli2);
		
		basicTranscription.setCommonTimeLine(timeline);
		
		Tier tier1= ExmaraldaBasicFactory.eINSTANCE.createTier();
		
		Event event1= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setStart(tli1);
		event1.setEnd(tli2);
		event1.setValue(token1);
		
		tier1.getEvents().add(event1);
		tier1.setCategory(txtTier);
		
		basicTranscription.getTiers().add(tier1);
		
		SDocument sDoc= SaltFactory.eINSTANCE.createSDocument();
		this.getFixture().mapDocument(sDoc, basicTranscription);
		
		assertNotNull(sDoc.getSDocumentGraph());
		assertNotNull(sDoc.getSDocumentGraph().getSTextualDSs());
		assertEquals(1, sDoc.getSDocumentGraph().getSTextualDSs().size());
		assertEquals(token1, sDoc.getSDocumentGraph().getSTextualDSs().get(0).getSText());
		assertEquals(1, sDoc.getSDocumentGraph().getSTokens().size());
		assertEquals(1, sDoc.getSDocumentGraph().getSAudioDataSources().size());
		assertEquals(refFile, sDoc.getSDocumentGraph().getSAudioDataSources().get(0).getSAudioReference());
		assertEquals(1, sDoc.getSDocumentGraph().getSAudioDSRelations().size());
		assertEquals(start, sDoc.getSDocumentGraph().getSAudioDSRelations().get(0).getSStart());
		assertEquals(end, sDoc.getSDocumentGraph().getSAudioDSRelations().get(0).getSEnd());
	}
	
	/**
	 * Checks two tokens, first referring to a time value in start {@link TLI} object, the second referring to a time value
	 * in end {@link TLI} object.
	 * @throws MalformedURLException
	 */
	@Test
	public void testImportAudio2() throws MalformedURLException
	{
		File testFile= new File(testFolder.getAbsolutePath()+ "/WikinewsSpecialReportAyala.ogg");
		
		String txtTier= "txt";
		String token1= "Any";
		String token2= "sample";
		Double start= 1.0;
		Double end= 2.5677097899;
		URI refFile= URI.createFileURI(testFile.getAbsolutePath());
		
		PepperModuleProperty<String> prop= (PepperModuleProperty<String>)this.getFixture().getProperties().getProperty(EXMARaLDAImporterProperties.PROP_TOKEN_TIER);
		prop.setValue(txtTier);
		
		URL referencedFile= new URL(refFile.toString()); 
		BasicTranscription basicTranscription= ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		MetaInformation metaInformation= ExmaraldaBasicFactory.eINSTANCE.createMetaInformation();
		metaInformation.setReferencedFile(referencedFile);
		basicTranscription.setMetaInformation(metaInformation);
		
		CommonTimeLine timeline= ExmaraldaBasicFactory.eINSTANCE.createCommonTimeLine();
		
		TLI tli1= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setTime(start.toString());
		timeline.getTLIs().add(tli1);
		
		TLI tli2= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		timeline.getTLIs().add(tli2);
		
		TLI tli3= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setTime(end.toString());
		timeline.getTLIs().add(tli3);
		
		basicTranscription.setCommonTimeLine(timeline);
		
		Tier tier1= ExmaraldaBasicFactory.eINSTANCE.createTier();
		
		Event event1= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setStart(tli1);
		event1.setEnd(tli2);
		event1.setValue(token1);
		tier1.getEvents().add(event1);
		
		Event event2= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event2.setStart(tli2);
		event2.setEnd(tli3);
		event2.setValue(token2);
		tier1.getEvents().add(event2);
		
		tier1.setCategory(txtTier);
		
		basicTranscription.getTiers().add(tier1);
		
		SDocument sDoc= SaltFactory.eINSTANCE.createSDocument();
		this.getFixture().mapDocument(sDoc, basicTranscription);
		
		assertNotNull(sDoc.getSDocumentGraph());
		assertNotNull(sDoc.getSDocumentGraph().getSTextualDSs());
		assertEquals(1, sDoc.getSDocumentGraph().getSTextualDSs().size());
		assertTrue(sDoc.getSDocumentGraph().getSTextualDSs().get(0).getSText().contains(token1));
		assertTrue(sDoc.getSDocumentGraph().getSTextualDSs().get(0).getSText().contains(token2));
		
		assertEquals(2, sDoc.getSDocumentGraph().getSTokens().size());
		
		assertEquals(1, sDoc.getSDocumentGraph().getSAudioDataSources().size());
		assertEquals(refFile, sDoc.getSDocumentGraph().getSAudioDataSources().get(0).getSAudioReference());
		
		//start: check audio relations 
			assertEquals(2, sDoc.getSDocumentGraph().getSAudioDSRelations().size());
			assertEquals(start, sDoc.getSDocumentGraph().getSAudioDSRelations().get(0).getSStart());
			assertNull(sDoc.getSDocumentGraph().getSAudioDSRelations().get(0).getSEnd());
			
			assertNull(sDoc.getSDocumentGraph().getSAudioDSRelations().get(1).getSStart());
			assertEquals(end, sDoc.getSDocumentGraph().getSAudioDSRelations().get(1).getSEnd());
		//end: check audio relations
	}
}
