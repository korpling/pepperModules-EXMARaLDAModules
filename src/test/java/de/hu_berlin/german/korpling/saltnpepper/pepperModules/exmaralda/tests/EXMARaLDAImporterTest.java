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
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperConvertException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.CorpusDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.testSuite.moduleTests.PepperImporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDAImporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;

public class EXMARaLDAImporterTest extends PepperImporterTest
{	
//	URI resourceURI= URI.createFileURI("e:/workspace/SaltNPepper/de.hub.corpling.pepper.modules.EXMARaLDAModules/");
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	URI temproraryURI= URI.createFileURI("./_TMP/de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.exmaralda");
	URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/specialParams/specialParams1.prop");
	
	protected void setUp() throws Exception 
	{
		super.setFixture(new EXMARaLDAImporter());
		super.getFixture().setSaltProject(SaltCommonFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		super.setTemprorariesURI(temproraryURI);
		
		//setting temproraries and resources
		this.getFixture().setTemproraries(temproraryURI);
		this.getFixture().setResources(resourceURI);
		
		//set formats to support
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
		
		//set specialParams
		this.getFixture().setSpecialParams(specialParamsURI);
	}
	
	public void SetGetCorpusDefinition()
	{
		//TODO somethong to test???
		CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDefinition(formatDef);
	}
	
//	/**
//	 * Tests importing corpus structure by just one document. It shall work, corpus has to
//	 * be set artificial with name of document. 
//	 * @throws IOException 
//	 */
//	public void testImportCorpusStructure1() throws IOException
//	{
//		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/corpus1.saltCommon");
//		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case4/corpus1/corpus1.saltCommon");
//		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/corpus1");
//		
//		{//creating and setting corpus definition
//			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
//			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
//			formatDef.setFormatName("EXMARaLDA");
//			formatDef.setFormatVersion("1.0");
//			corpDef.setFormatDefinition(formatDef);
////			URI corpusPath= URI.createFileURI("./src/test/resources/testCorpora/sampleCorpus1/");
//			corpDef.setCorpusPath(corpusPath);
//			this.getFixture().setCorpusDefinition(corpDef);
//		}
//		
//		{//setting corpus graph and importing
//			SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
//			this.getFixture().getSaltProject().getSCorpusGraphs().add(corpGraph);
//			this.getFixture().importCorpusStructure(corpGraph);
//		}
//		
//		{//print saltGraph (just for testing)
//			// create resource set and resource 
//			ResourceSet resourceSet = new ResourceSetImpl();
//			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
//			
//			//load resource 
//			Resource resource2 = resourceSet.createResource(exportCorpusURI);
//			if (resource2== null)
//				throw new PepperConvertException("The resource is null.");
//			resource2.getContents().add(this.getFixture().getSaltProject());
//			resource2.save(null);
//		}
//				
//		assertTrue(this.compareFiles(expectedCorpusURI, exportCorpusURI));
//	}
	
	
	
	/**
	 * Tests if all token and texts and annotations are there.
	 * Terminals and non terminals with edges and secedges between them. 
	 * @throws IOException 
	 */
	public void testStart1() throws IOException
	{
		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case1/corpus1/corpus1.saltCommon");
		//System.out.println("Expected Corpus URI: "+ expectedCorpusURI.toFileString());
		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case1/corpus1/sample1.saltCommon");
		//System.out.println("Expected URI: "+ expectedURI.toFileString());
		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case1/corpus1/corpus1.saltCommon");
		//System.out.println("Export corpus URI: "+ exportCorpusURI.toFileString());
		URI exportURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case1/corpus1/sample1.saltCommon");
		//System.out.println("Export URI: "+ exportURI.toFileString());
		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case1/corpus1");
		//System.out.println("Corpus Path: "+ corpusPath.toFileString());
		URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case1/specialParams1.prop");
		//System.out.println("Special Params URI: "+ specialParamsURI.toFileString());
		
		this.testStart(expectedCorpusURI, expectedURI, exportCorpusURI, exportURI, corpusPath, specialParamsURI);
	}
	
	/**
	 * Tests if all token and texts and annotations are there.
	 * Terminals and non terminals with edges and secedgesbetween them. 
	 * @throws IOException 
	 */
	public void testStart2() throws IOException
	{
		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case2/corpus1/corpus1.saltCommon");
		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case2/corpus1/sample2.saltCommon");
		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case2/corpus1/corpus1.saltCommon");
		URI exportURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case2/corpus1/sample2.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case2/corpus1");
		URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case2/specialParams1.prop");
		
		
		this.testStart(expectedCorpusURI, expectedURI, exportCorpusURI, exportURI, corpusPath, specialParamsURI);
	}
	
	/**
	 * Tests if all token and texts and annotations are there.
	 * Terminals and non terminals with edges and secedgesbetween them. 
	 * @throws IOException 
	 */
	public void testStart3() throws IOException
	{
		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case3/corpus1/corpus1.saltCommon");
		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case3/corpus1/sample3.saltCommon");
		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case3/corpus1/corpus1.saltCommon");
		URI exportURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case3/corpus1/sample3.saltCommon");
		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case3/corpus1");
		URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case3/specialParams1.prop");
		
		
		this.testStart(expectedCorpusURI, expectedURI, exportCorpusURI, exportURI, corpusPath, specialParamsURI);
	}
	
//	/**
//	 * Tests if all token and texts and annotations are there.
//	 * Terminals and non terminals with edges and secedgesbetween them. 
//	 * @throws IOException 
//	 */
//	public void testStart4() throws IOException
//	{
//		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/corpus1/corpus1.saltCommon");
//		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/corpus1/sample1.saltCommon");
//		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case4/corpus1/corpus1.saltCommon");
//		URI exportURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case4/corpus1/sample1.saltCommon");
//		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/corpus1");
//		URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case4/specialParams1.prop");
//		
//		
//		this.testStart(expectedCorpusURI, expectedURI, exportCorpusURI, exportURI, corpusPath, specialParamsURI);
//	}
	
//	/**
//	 * Tests if all token and texts and annotations are there.
//	 * Terminals and non terminals with edges and secedgesbetween them. 
//	 * @throws IOException 
//	 */
//	public void testStart5() throws IOException
//	{
//		URI expectedCorpusURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case5/corpus1/corpus1.saltCommon");
//		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case5/corpus1/sample3.saltCommon");
//		URI exportCorpusURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case5/corpus1/corpus1.saltCommon");
//		URI exportURI= URI.createFileURI(this.temproraryURI+"/EXMARaLDAImporter/Case5/corpus1/sample3.saltCommon");
//		URI corpusPath= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case5/corpus1");
//		URI specialParamsURI= URI.createFileURI("./src/test/resources/EXMARaLDAImporter/Case5/specialParams1.prop");
//		
//		
//		this.testStart(expectedCorpusURI, expectedURI, exportCorpusURI, exportURI, corpusPath, specialParamsURI);
//	}
	
	/**
	 * Tests if all token and texts and annotations are there. 
	 * @throws IOException 
	 */
	private void testStart(URI expectedCorpusURI, URI expectedURI, URI exportCorpusURI, URI exportURI, URI corpusPath, URI specialParamsURI) throws IOException
	{
		this.getFixture().setSpecialParams(specialParamsURI);
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("EXMARaLDA");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
			/*
			System.out.println("Corpus Path: "+ this.getFixture().getCorpusDefinition().getCorpusPath());
			System.out.println("Format Definition: Name: "+ this.getFixture().getCorpusDefinition().getFormatDefinition().getFormatName());
			System.out.println("Format Definition: Version: "+ this.getFixture().getCorpusDefinition().getFormatDefinition().getFormatVersion());
			*/
		}
		
		{//setting corpus graph and importing corpus structure
			SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
			this.getFixture().getSaltProject().getSCorpusGraphs().add(corpGraph);
			this.getFixture().importCorpusStructure(corpGraph);
		}
		
		this.start();
		
		assertNotNull(this.getFixture().getSaltProject().getSCorpusGraphs());
		assertNotNull(this.getFixture().getSaltProject().getSCorpusGraphs().get(0));
		assertNotNull(this.getFixture().getSaltProject().getSCorpusGraphs().get(0).getSDocuments());
		assertTrue(this.getFixture().getSaltProject().getSCorpusGraphs().get(0).getSDocuments().size()> 0);
		SDocument sDocument= this.getFixture().getSaltProject().getSCorpusGraphs().get(0).getSDocuments().get(0);
//		{//print saltGraph to dot (just for testing)
//			Salt2DOT salt2Dot= new Salt2DOT();
//			salt2Dot.salt2Dot(sDocument.getSElementId(), URI.createFileURI(this.getFixture().getTemproraries() + "/doc1.dot"));
//		}//print saltGraph (just for testing)
		
		{
			SDocumentGraph sDocGraph= sDocument.getSDocumentGraph();
			sDocGraph.setSDocument(null);
			// create resource set and resource 
			ResourceSet resourceSet = new ResourceSetImpl();

			// Register XML resource factory
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
			//load resource 
			Resource resource = resourceSet.createResource(exportURI);
			if (resource== null)
				throw new PepperConvertException("The resource is null.");
			resource.getContents().add(sDocGraph);
			resource.save(null);
			sDocGraph.setSDocument(sDocument);
			
			//load resource 
			Resource resource2 = resourceSet.createResource(exportCorpusURI);
			if (resource2== null)
				throw new PepperConvertException("The resource is null.");
			resource2.getContents().add(this.getFixture().getSaltProject());
			resource2.save(null);
//			sDocGraph.setSDocument(sDocument);
		}
		
		assertTrue("The files '"+expectedCorpusURI+"' and '"+exportCorpusURI+"' aren't identical. ", this.compareFiles(expectedCorpusURI, exportCorpusURI));
		assertTrue("The files '"+expectedURI+"' and '"+exportURI+"' aren't identical. ", this.compareFiles(expectedURI, exportURI));
	}
	//TODO much more tests for example getSupportedFormats, getName
}
