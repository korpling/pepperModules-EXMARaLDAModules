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

import junit.framework.TestCase;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Event;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.TLI;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.Tier;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDA2SaltMapper;

public class EXMARaLDA2SaltMapperTest extends TestCase 
{
	private EXMARaLDA2SaltMapper fixture= null;

	/**
	 * @param fixture the fixture to set
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
	
	public void setUp()
	{
		this.setFixture(new EXMARaLDA2SaltMapper());
	}
	
	/**
	 * Checks if completion in missing coverage of {@link TLI} objects by {@link Event} contained in token {@link Tier} 
	 * objects works correctly. 
	 * e= empty, T= real Token
	 *  e T e
	 * 1|2|3|4
	 */
	public void testCleanModel()
	{
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI tli1= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setId("1");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli1);
		TLI tli2= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setId("2");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli2);
		TLI tli3= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setId("3");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli3);
		TLI tli4= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli4.setId("4");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli4);
		
		Event event1= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setValue("word");
		event1.setStart(tli2);
		event1.setEnd(tli3);
		
		Tier tokenTier= ExmaraldaBasicFactory.eINSTANCE.createTier();
		tokenTier.getEvents().add(event1);
		
		EList<Tier> tokenTiers= new BasicEList<Tier>();
		tokenTiers.add(tokenTier);
		this.getFixture().cleanModel(basicTranscription, tokenTiers);
		
		assertEquals(tokenTier.getEvents().toString(), 3, tokenTier.getEvents().size());
		
		assertEquals(tli1, tokenTier.getEvents().get(0).getStart());
		assertEquals(tli2, tokenTier.getEvents().get(0).getEnd());
		
		assertEquals(tli2, event1.getStart());
		assertEquals(tli3, event1.getEnd());
		
		assertEquals(tli3, tokenTier.getEvents().get(2).getStart());
		assertEquals(tli4, tokenTier.getEvents().get(2).getEnd());
	}
	
	/**
	 * Checks if completion in missing coverage of {@link TLI} objects by {@link Event} contained in token {@link Tier} 
	 * objects works correctly. 
	 * e= empty, T= real Token
	 *  T e T
	 * 1|2|3|4
	 */
	public void testCleanModel2()
	{
		BasicTranscription basicTranscription = ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
		TLI tli1= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli1.setId("1");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli1);
		TLI tli2= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli2.setId("2");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli2);
		TLI tli3= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli3.setId("3");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli3);
		TLI tli4= ExmaraldaBasicFactory.eINSTANCE.createTLI();
		tli4.setId("4");
		basicTranscription.getCommonTimeLine().getTLIs().add(tli4);
		
		Tier tokenTier= ExmaraldaBasicFactory.eINSTANCE.createTier();
		
		Event event1= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event1.setValue("word");
		event1.setStart(tli1);
		event1.setEnd(tli2);
		tokenTier.getEvents().add(event1);
		
		Event event2= ExmaraldaBasicFactory.eINSTANCE.createEvent();
		event2.setValue("word");
		event2.setStart(tli3);
		event2.setEnd(tli4);
		tokenTier.getEvents().add(event2);
		
		EList<Tier> tokenTiers= new BasicEList<Tier>();
		tokenTiers.add(tokenTier);
		this.getFixture().cleanModel(basicTranscription, tokenTiers);
		
		assertEquals(tokenTier.getEvents().toString(), 3, tokenTier.getEvents().size());
		
		assertEquals(tli1, event1.getStart());
		assertEquals(tli2, event1.getEnd());
		
		assertEquals(tli2, tokenTier.getEvents().get(1).getStart());
		assertEquals(tli3, tokenTier.getEvents().get(1).getEnd());
		
		assertEquals(tli3, event2.getStart());
		assertEquals(tli4, event2.getEnd());
	}
}
