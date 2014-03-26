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

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepper.common.CorpusDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.common.FormatDesc;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testFramework.PepperImporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDAImporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;

public class EXMARaLDAImporterTest extends PepperImporterTest
{	
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	
	@Before
	public void setUp() throws Exception 
	{
		super.setFixture(new EXMARaLDAImporter());
		super.getFixture().setSaltProject(SaltCommonFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		
		//setting resources
		this.getFixture().setResources(resourceURI);
		
		//set formats to support
		FormatDesc formatDef= new FormatDesc();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
	}
	
	@Test
	public void testSetGetCorpusDefinition()
	{
		//TODO somethong to test???
		CorpusDesc corpDef= new CorpusDesc();
		FormatDesc formatDef= new FormatDesc();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDesc(formatDef);
	}
}
