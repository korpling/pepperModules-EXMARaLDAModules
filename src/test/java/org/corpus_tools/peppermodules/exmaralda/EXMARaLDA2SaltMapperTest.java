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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.peppermodules.exmaralda.EXMARaLDA2SaltMapper;
import org.corpus_tools.peppermodules.exmaralda.EXMARaLDAImporterProperties;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Speaker;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TIER_TYPE;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;

public class EXMARaLDA2SaltMapperTest {
	private EXMARaLDA2SaltMapper fixture = null;

	/**
	 * @param fixture
	 *            the fixture to set
	 */
	public void setFixture(EXMARaLDA2SaltMapper fixture) {
		this.fixture = fixture;
	}

	/**
	 * @return the fixture
	 */
	public EXMARaLDA2SaltMapper getFixture() {
		return fixture;
	}

	@Before
	public void setUp() {
		this.setFixture(new EXMARaLDA2SaltMapper());
	}

	/**
	 * Tests the mapping of two transcription tiers having parallel events to
	 * two {@link STextualDS} objects.
	 */
	public void test() {
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI t1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		basicTranscription.getCommonTimeLine().getTLIs().add(t1);
		TLI t2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		basicTranscription.getCommonTimeLine().getTLIs().add(t2);
		TLI t3 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		basicTranscription.getCommonTimeLine().getTLIs().add(t3);
		TLI t4 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		basicTranscription.getCommonTimeLine().getTLIs().add(t4);
		TLI t5 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		basicTranscription.getCommonTimeLine().getTLIs().add(t5);

		Tier text1 = ExmaraldaBasicFactory.eINSTANCE.createTier();
		text1.setType(TIER_TYPE.T);
		basicTranscription.getTiers().add(text1);

		Event a = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		a.setValue("A");
		a.setStart(t1);
		a.setEnd(t2);
		text1.getEvents().add(a);

		Event sample = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		sample.setValue("Sample");
		sample.setStart(t2);
		sample.setEnd(t3);
		text1.getEvents().add(sample);

		Event text = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		text.setValue("Text");
		text.setStart(t3);
		text.setEnd(t4);
		text1.getEvents().add(text);

		Tier text2 = ExmaraldaBasicFactory.eINSTANCE.createTier();
		text2.setType(TIER_TYPE.T);
		basicTranscription.getTiers().add(text2);

		Event oh = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		oh.setValue("Oh");
		oh.setStart(t2);
		oh.setEnd(t3);
		text1.getEvents().add(oh);

		Event yes = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		yes.setValue("yes");
		yes.setStart(t3);
		yes.setEnd(t4);
		text1.getEvents().add(yes);

		getFixture().setBasicTranscription(basicTranscription);
		getFixture().mapSDocument();

		SDocumentGraph sDocGraph = getFixture().getDocument().getDocumentGraph();
		assertNotNull(sDocGraph);
		assertEquals(2, sDocGraph.getTextualDSs().size());
		assertEquals("A sample text", sDocGraph.getTextualDSs().get(0).getText());
		assertEquals("oh yes", sDocGraph.getTextualDSs().get(1).getText());

		assertEquals(5, sDocGraph.getTokens().size());
		assertEquals("A", sDocGraph.getText(sDocGraph.getTokens().get(0)));
		assertEquals("sample", sDocGraph.getText(sDocGraph.getTokens().get(0)));
		assertEquals("text", sDocGraph.getText(sDocGraph.getTokens().get(0)));
		assertEquals("Oh", sDocGraph.getText(sDocGraph.getTokens().get(0)));
		assertEquals("yes", sDocGraph.getText(sDocGraph.getTokens().get(0)));
	}

	/**
	 * Tests the helper method, for the following case:
	 * <table border= "1">
	 * <tr>
	 * <td>This</td>
	 * <td>is</td>
	 * <td>a</td>
	 * <td>sample</td>
	 * <td>text</td>
	 * </tr>
	 * </table>
	 * with the sequence from 6 to 12
	 */
	@Test
	public void testGetAdjacentSTokens() {
		getFixture().setDocument(SaltFactory.createSDocument());
		getFixture().getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
		STextualDS text = getFixture().getDocument().getDocumentGraph().createTextualDS("This is a sample text");
		getFixture().getDocument().getDocumentGraph().tokenize();

		DataSourceSequence sequence = new DataSourceSequence();
		sequence.setDataSource(text);
		sequence.setStart(6);
		sequence.setEnd(12);

		List<SToken> tokens = getFixture().getAdjacentSTokens(sequence);
		assertEquals(3, tokens.size());
	}

	/**
	 * Checks if completion in missing coverage of {@link TLI} objects by
	 * {@link Event} contained in token {@link Tier} objects works correctly. e=
	 * empty, T= real Token e T e 1|2|3|4
	 */
	@Test
	public void testCleanModel() {
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI tli1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setId("1");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli1);
		TLI tli2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setId("2");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli2);
		TLI tli3 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setId("3");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli3);
		TLI tli4 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli4.setId("4");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli4);

		Event event1 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setValue("word");
		event1.setStart(tli2);
		event1.setEnd(tli3);

		Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
		tokenTier.getEvents().add(event1);

		List<Tier> tokenTiers = new ArrayList<>();
		tokenTiers.add(tokenTier);
		getFixture().cleanModel(basicTranscription, tokenTiers);

		assertEquals(tokenTier.getEvents().toString(), 3, tokenTier.getEvents().size());

		assertEquals(tli1, tokenTier.getEvents().get(0).getStart());
		assertEquals(tli2, tokenTier.getEvents().get(0).getEnd());

		assertEquals(tli2, event1.getStart());
		assertEquals(tli3, event1.getEnd());

		assertEquals(tli3, tokenTier.getEvents().get(2).getStart());
		assertEquals(tli4, tokenTier.getEvents().get(2).getEnd());
	}

	/**
	 * Checks if completion in missing coverage of {@link TLI} objects by
	 * {@link Event} contained in token {@link Tier} objects works correctly. e=
	 * empty, T= real Token T e T 1|2|3|4
	 */
	@Test
	public void testCleanModel2() {
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI tli1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setId("1");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli1);
		TLI tli2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setId("2");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli2);
		TLI tli3 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setId("3");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli3);
		TLI tli4 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli4.setId("4");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli4);

		Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();

		Event event1 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setValue("word");
		event1.setStart(tli1);
		event1.setEnd(tli2);
		tokenTier.getEvents().add(event1);

		Event event2 = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event2.setValue("word");
		event2.setStart(tli3);
		event2.setEnd(tli4);
		tokenTier.getEvents().add(event2);

		List<Tier> tokenTiers = new ArrayList<Tier>();
		tokenTiers.add(tokenTier);
		getFixture().cleanModel(basicTranscription, tokenTiers);

		assertEquals(tokenTier.getEvents().toString(), 3, tokenTier.getEvents().size());

		assertEquals(tli1, event1.getStart());
		assertEquals(tli2, event1.getEnd());

		assertEquals(tli2, tokenTier.getEvents().get(1).getStart());
		assertEquals(tli3, tokenTier.getEvents().get(1).getEnd());

		assertEquals(tli3, event2.getStart());
		assertEquals(tli4, event2.getEnd());
	}

	@Test
	public void test_Speaker2SLayer() {
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI tli1 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setId("1");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli1);
		TLI tli2 = ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setId("2");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli2);

		Speaker speaker = ExmaraldaBasicFactory.eINSTANCE.createSpeaker();
		speaker.setId("spk0");
		speaker.setAbbreviation("Bart");
		basicTranscription.getSpeakertable().add(speaker);
		
		Tier tokenTier = ExmaraldaBasicFactory.eINSTANCE.createTier();
		tokenTier.setCategory("tok");
		tokenTier.setSpeaker(speaker);
		basicTranscription.getTiers().add(tokenTier);

		Event tok = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		tok.setValue("sample");
		tok.setStart(tli1);
		tok.setEnd(tli2);
		tokenTier.getEvents().add(tok);


		Tier tier = ExmaraldaBasicFactory.eINSTANCE.createTier();
		tier.setType(TIER_TYPE.A);
		tier.setCategory("anno");
		tier.setSpeaker(speaker);
		basicTranscription.getTiers().add(tier);

		Event event = ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event.setStart(tli1);
		event.setEnd(tli2);
		event.setValue("value");
		tier.getEvents().add(event);

		getFixture().setBasicTranscription(basicTranscription);
		getFixture().setDocument(SaltFactory.createSDocument());
		getFixture().setProperties(new EXMARaLDAImporterProperties());
		getFixture().getProperties().setPropertyValue(EXMARaLDAImporterProperties.PROP_TOKEN_TIER, "tok");
		getFixture().mapSDocument();

		assertNotNull(getFixture().getDocument().getDocumentGraph());
		assertEquals(1, getFixture().getDocument().getDocumentGraph().getSpans().get(0).getAnnotations().size());

		SAnnotation sAnno = getFixture().getDocument().getDocumentGraph().getSpans().get(0).getAnnotations().iterator().next();
		assertEquals("anno", sAnno.getName());
		assertEquals("value", sAnno.getValue());
		assertEquals("Bart", sAnno.getNamespace());
	}
}
