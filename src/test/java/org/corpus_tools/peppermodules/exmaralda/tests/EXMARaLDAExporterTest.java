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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.testFramework.PepperExporterTest;
import org.corpus_tools.peppermodules.exmaralda.EXMARaLDAExporter;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusDocumentRelation;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class EXMARaLDAExporterTest extends PepperExporterTest {
	URI resourceURI = URI.createFileURI(new File(".").getAbsolutePath());

	@Before
	public void setUp() {
		super.setFixture(new EXMARaLDAExporter());
		super.getFixture().setSaltProject(SaltFactory.createSaltProject());
		super.setResourcesURI(resourceURI);

		// set formats to support
		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		this.addSupportedFormat(formatDef);

		// set corpus definition
		CorpusDesc corpDef = new CorpusDesc();
		corpDef.setFormatDesc(formatDef);
	}

	@Test
	public void testSetGetCorpusDefinition() {
		// TODO something to test???
		CorpusDesc corpDef = new CorpusDesc();
		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDesc(formatDef);
	}

	private void removeDirRec(File dir) {
		if (dir != null) {
			if (dir.listFiles() != null && dir.listFiles().length != 0) {
				for (File subDir : dir.listFiles()) {
					this.removeDirRec(subDir);
				}
			}
			dir.delete();
		}
	}

	/**
	 * Tests exporting a corpus structure. corp1 | doc1 Tests importing a corpus
	 * with one document and without a timeline. The timeline has to be
	 * computed. This test checks the sample of {@link SampleGenerator} created
	 * by the methods {@link SampleGenerator#createPrimaryData(SDocument)},
	 * {@link SampleGenerator#createTokens(SDocument)},
	 * {@link SampleGenerator#createMorphologyAnnotations(SDocument)},
	 * {@link SampleGenerator#createInformationStructureSpan(SDocument)} and
	 * {@link SampleGenerator#createInformationStructureAnnotations(SDocument)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testStart1() throws IOException {
		File corpusPathFile = new File(getTempPath("exmaraldaExporterTest").getAbsolutePath() + "/current");
		File currentFile = new File(getTempPath("exmaraldaExporterTest").getAbsolutePath() + "/current/corp1/doc1.exb");
		File expectedFile = new File("./src/test/resources/EXMARaLDAExporter/expected/sample1/corp1/doc1.exb");

		URI corpusPath = URI.createFileURI(corpusPathFile.getCanonicalPath());
		URI currentURI = URI.createFileURI(currentFile.getCanonicalPath());
		URI expectedURI = URI.createFileURI(expectedFile.getCanonicalPath());

		this.removeDirRec(new File(corpusPath.toFileString()));

		// creating and setting corpus definition
		CorpusDesc corpDef = new CorpusDesc();
		FormatDesc formatDef = new FormatDesc();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		corpDef.setFormatDesc(formatDef);
		corpDef.setCorpusPath(corpusPath);
		getFixture().setCorpusDesc(corpDef);

		// start: create sample
		// start:create corpus structure
		SDocument sDoc = this.createCorpusStructure();
		// end:create corpus structure
		SampleGenerator.createPrimaryData(sDoc);
		SampleGenerator.createTokens(sDoc);
		SampleGenerator.createMorphologyAnnotations(sDoc);
		SampleGenerator.createInformationStructureSpan(sDoc);
		SampleGenerator.createInformationStructureAnnotations(sDoc);
		// end: create sample

		// start: exporting document
		start();
		// end: exporting document

		// checking if export was correct
		assertTrue("The files '" + expectedURI + "' and '" + currentURI + "' aren't identical. ", this.compareFiles(expectedURI, currentURI));
	}

	/**
	 * Creates a corpus structure with one corpus and one document. It returns
	 * the created document. corp1 | doc1
	 * 
	 * @return
	 */
	private SDocument createCorpusStructure() {
		{// creating corpus structure
			SCorpusGraph corpGraph = SaltFactory.createSCorpusGraph();
			getFixture().getSaltProject().addCorpusGraph(corpGraph);
			// corp1
			// |
			// doc1

			// corp1
			SCorpus corp1 = SaltFactory.createSCorpus();
			corp1.setName("corp1");
			SaltFactory.createIdentifier(corp1, "corp1");
			corpGraph.addNode(corp1);

			// doc1
			SDocument doc1 = SaltFactory.createSDocument();
			SaltFactory.createIdentifier(doc1, "corp1/doc1");
			doc1.setName("doc1");
			corpGraph.addNode(doc1);
			doc1.setDocumentGraph(SaltFactory.createSDocumentGraph());

			// CorpDocRel
			SCorpusDocumentRelation corpDocRel1 = SaltFactory.createSCorpusDocumentRelation();
			SaltFactory.createIdentifier(corpDocRel1, "rel1");
			corpDocRel1.setName("rel1");
			corpDocRel1.setSource(corp1);
			corpDocRel1.setTarget(doc1);
			corpGraph.addRelation(corpDocRel1);
			return (doc1);
		}
	}
}
