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

import de.hu_berlin.german.korpling.saltnpepper.devTools.generalModuleTests.GeneralPepperExporterTest;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.CorpusDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda.EXMARaLDAExporter;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SaltSemanticsPackage;


public class EXMARaLDAExporterTest extends GeneralPepperExporterTest
{
	URI resourceURI= URI.createFileURI(new File(".").getAbsolutePath());
	
	protected void setUp() throws Exception 
	{
		super.setFixture(new EXMARaLDAExporter());
		super.getFixture().setSaltProject(SaltCommonFactory.eINSTANCE.createSaltProject());
		super.setResourcesURI(resourceURI);
		URI temproraryURI= URI.createFileURI("_TMP/de.hub.corpling.pepper.modules.exmaralda");
		super.setTemprorariesURI(temproraryURI);
		
		//set formats to support
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		this.supportedFormatsCheck.add(formatDef);
	}
	
	public void testSetGetCorpusDefinition()
	{
		//TODO something to test???
		CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDefinition(formatDef);
	}
	
	private void removeDirRec(File dir)
	{
		if (dir != null)
		{
			if (dir.listFiles()!= null && dir.listFiles().length!= 0)
			{	
				for (File subDir: dir.listFiles())
				{
					this.removeDirRec(subDir);
				}
			}
			dir.delete();
		}
	}
	
	/**
	 * Tests exporting a corpus structure. 
	 * 		corp1
	 *		|
	 *		doc1
	 * @throws IOException 
	 */
	public void testStart1() throws IOException
	{
		URI corpusPath= URI.createFileURI("./_TMP/exportTest/actual/");
		URI inputURI= URI.createFileURI("./src/test/resources/EXMARaLDAExporter/expected/sample1/sampleCorpus1.saltCommon");
		URI actualURI= URI.createFileURI("./_TMP/exportTest/actual/corp1/doc1.exb");
		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAExporter/expected/sample1/corp1/doc1.exb");
//		URI dotOutputPath= URI.createFileURI("./src/test/resources/dotOutput/");
		
		this.removeDirRec(new File(corpusPath.toFileString()));
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("EXMARaLDA");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
		}
		//create corpus structure
		SDocument sDoc= this.createCorpusStructure();
		{//load document structure
			// create resource set and resource 
			ResourceSet resourceSet = new ResourceSetImpl();

			//register packages 
			resourceSet.getPackageRegistry().put(SaltSemanticsPackage.eINSTANCE.getNsURI(), SaltSemanticsPackage.eINSTANCE);
			
			// Register XML resource factory
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("salt",new XMIResourceFactoryImpl());
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",new XMIResourceFactoryImpl());
			
			//load resource 
			Resource resource = resourceSet.createResource(inputURI);
			
			if (resource== null)
				throw new NullPointerException("Cannot load the exmaralda file: "+ inputURI+", becuase the resource is null.");
			try {
				resource.load(null);
			} catch (IOException e) 
			{
				throw new PepperModuleException("Cannot load the exmaralda file: "+ inputURI+".", e);
			}
			
			SDocumentGraph sDocGraph= (SDocumentGraph) resource.getContents().get(0);
			sDoc.setSDocumentGraph(sDocGraph);
		}
		
//		{//print saltGraph to dot (just for testing)
//			Salt2DOT salt2Dot= new Salt2DOT();
//			salt2Dot.salt2Dot(sDoc.getSElementId(), URI.createFileURI(dotOutputPath.toFileString() + "/doc1.dot"));
//		}
		
		{//exporting document
			this.start();
		}
		
		
		{//checking if export was correct
			assertTrue("The files '"+expectedURI+"' and '"+actualURI+"' aren't identical. ", this.compareFiles(expectedURI, actualURI));
		}
	}
	
	/**
	 * Tests importing a corpus with one document and without a timeline. The timeline has to be computed. 
	 * @throws IOException 
	 */
	public void testStart2() throws IOException
	{
		URI corpusPath= URI.createFileURI("./_TMP/exportTest/actual/");
		URI inputURI= URI.createFileURI("./src/test/resources/EXMARaLDAExporter/expected/sample2/sampleCorpus1.saltCommon");
		URI actualURI= URI.createFileURI("./_TMP/exportTest/actual/corp1/doc1.exb");
		URI expectedURI= URI.createFileURI("./src/test/resources/EXMARaLDAExporter/expected/sample2/corp1/doc1.exb");
//		URI dotOutputPath= URI.createFileURI("./src/test/resources/dotOutput/");
		
		this.removeDirRec(new File(corpusPath.toFileString()));
		{//creating and setting corpus definition
			CorpusDefinition corpDef= PepperInterfaceFactory.eINSTANCE.createCorpusDefinition();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			formatDef.setFormatName("EXMARaLDA");
			formatDef.setFormatVersion("1.0");
			corpDef.setFormatDefinition(formatDef);
			corpDef.setCorpusPath(corpusPath);
			this.getFixture().setCorpusDefinition(corpDef);
		}
		//create corpus structure
		SDocument sDoc= this.createCorpusStructure();
		{//load document structure
			// create resource set and resource 
			ResourceSet resourceSet = new ResourceSetImpl();

			//register packages 
			resourceSet.getPackageRegistry().put(SaltSemanticsPackage.eINSTANCE.getNsURI(), SaltSemanticsPackage.eINSTANCE);
			
			// Register XML resource factory
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("saltCommon",new XMIResourceFactoryImpl());
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",new XMIResourceFactoryImpl());
			
			//load resource 
			Resource resource = resourceSet.createResource(inputURI);
			
			if (resource== null)
				throw new NullPointerException("Cannot load the exmaralda file: "+ inputURI+", becuase the resource is null.");
			try {
				resource.load(null);
			} catch (IOException e) 
			{
				throw new PepperModuleException("Cannot load the exmaralda file: "+ inputURI+".", e);
			}
			
			SDocumentGraph sDocGraph= (SDocumentGraph) resource.getContents().get(0);
			sDoc.setSDocumentGraph(sDocGraph);
		}
		
//		{//print saltGraph to dot (just for testing)
//			Salt2DOT salt2Dot= new Salt2DOT();
//			salt2Dot.salt2Dot(sDoc.getSElementId(), URI.createFileURI(dotOutputPath.toFileString() + "/doc1.dot"));
//		}
		
		{//exporting document
			this.start();
		}
		
		
		{//checking if export was correct
			assertTrue("The files '"+expectedURI+"' and '"+actualURI+"' aren't identical. ", this.compareFiles(expectedURI, actualURI));
		}
	}
	
	/**
	 * Creates a corpus structure with one corpus and one document. It returns the created document.
	 * 		corp1
	 *		|
	 *		doc1
	 * @return
	 */
	private SDocument createCorpusStructure()
	{
		{//creating corpus structure
			SCorpusGraph corpGraph= SaltCommonFactory.eINSTANCE.createSCorpusGraph();
			this.getFixture().getSaltProject().getSCorpusGraphs().add(corpGraph);
			//		corp1
			//		|
			//		doc1
			
			//corp1
			SElementId sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("corp1");
			SCorpus corp1= SaltCommonFactory.eINSTANCE.createSCorpus();
			corp1.setSName("corp1");
			corp1.setSElementId(sElementId);
			corpGraph.addSNode(corp1);
			
			//doc1
			SDocument doc1= SaltCommonFactory.eINSTANCE.createSDocument();
			sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("corp1/doc1");
			doc1.setSElementId(sElementId);
			doc1.setSName("doc1");
			corpGraph.addSNode(doc1);
			
			//CorpDocRel
			SCorpusDocumentRelation corpDocRel1= SaltCommonFactory.eINSTANCE.createSCorpusDocumentRelation();
			sElementId= SaltCommonFactory.eINSTANCE.createSElementId();
			sElementId.setSId("rel1");
			corpDocRel1.setSElementId(sElementId);
			corpDocRel1.setSName("rel1");
			corpDocRel1.setSCorpus(corp1);
			corpDocRel1.setSDocument(doc1);
			corpGraph.addSRelation(corpDocRel1);
			return(doc1);
		}
	}
}
