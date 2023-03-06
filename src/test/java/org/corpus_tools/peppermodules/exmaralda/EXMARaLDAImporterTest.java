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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import org.corpus_tools.pepper.common.CorpusDesc;
import org.corpus_tools.pepper.common.FormatDesc;
import org.corpus_tools.pepper.common.ModuleFitness;
import org.corpus_tools.pepper.common.ModuleFitness.FitnessFeature;
import org.corpus_tools.pepper.core.ModuleFitnessChecker;
import org.corpus_tools.pepper.testFramework.PepperImporterTest;
import org.corpus_tools.pepper.testFramework.PepperTestUtil;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class EXMARaLDAImporterTest extends PepperImporterTest {

  @Before
  public void setUp() throws Exception {
    super.setFixture(new EXMARaLDAImporter());
    super.getFixture().setSaltProject(SaltFactory.createSaltProject());

    // set formats to support
    FormatDesc formatDef = new FormatDesc();
    formatDef.setFormatName("EXMARaLDA");
    formatDef.setFormatVersion("1.0");
    addSupportedFormat(formatDef);
  }

  @Test
  public void testSetGetCorpusDefinition() {
    // TODO somethong to test???
    CorpusDesc corpDef = new CorpusDesc();
    FormatDesc formatDef = new FormatDesc();
    formatDef.setFormatName("EXMARaLDA");
    formatDef.setFormatVersion("1.0");
    corpDef.setFormatDesc(formatDef);
  }

  @Test
  public void whenSelfTestingModule_thenResultShouldBeTrue() {
    final ModuleFitness fitness =
        new ModuleFitnessChecker(PepperTestUtil.createDefaultPepper()).selfTest(fixture);
    assertThat(fitness.getFitness(FitnessFeature.HAS_SELFTEST)).isTrue();
    assertThat(fitness.getFitness(FitnessFeature.HAS_PASSED_SELFTEST)).isTrue();
    assertThat(fitness.getFitness(FitnessFeature.IS_IMPORTABLE_SEFTEST_DATA)).isTrue();
    assertThat(fitness.getFitness(FitnessFeature.IS_VALID_SELFTEST_DATA)).isTrue();
  }

  @Test
  public void testStrictFileEnding() throws IOException {

    File corpusPathFile = new File("./src/test/resources/testCorpora/specificFileEnding/");

    URI corpusPath = URI.createFileURI(corpusPathFile.getCanonicalPath());


    // creating and setting corpus definition
    CorpusDesc corpDef = new CorpusDesc();
    FormatDesc formatDef = new FormatDesc();
    formatDef.setFormatName("EXMARaLDA");
    formatDef.setFormatVersion("1.0");
    corpDef.setFormatDesc(formatDef);
    corpDef.setCorpusPath(corpusPath);
    getFixture().setCorpusDesc(corpDef);

    getFixture().getProperties().setPropertyValue(EXMARaLDAImporterProperties.PROP_STRICT_FILE_TYPE,
        true);


    // importing corpus
    start();


    SCorpusGraph cg = getFixture().getCorpusGraph();
    assertEquals(1, cg.getCorpora().size());
    assertEquals("specificFileEnding", cg.getCorpora().get(0).getName());
    assertEquals(1, cg.getDocuments().size());
    assertEquals("doc1", cg.getDocuments().get(0).getName());
  }

  @Test
  public void testNonStrictFileEnding() throws IOException {

    File corpusPathFile = new File("./src/test/resources/testCorpora/specificFileEnding/");

    URI corpusPath = URI.createFileURI(corpusPathFile.getCanonicalPath());


    // creating and setting corpus definition
    CorpusDesc corpDef = new CorpusDesc();
    FormatDesc formatDef = new FormatDesc();
    formatDef.setFormatName("EXMARaLDA");
    formatDef.setFormatVersion("1.0");
    corpDef.setFormatDesc(formatDef);
    corpDef.setCorpusPath(corpusPath);
    getFixture().setCorpusDesc(corpDef);

    getFixture().getProperties().setPropertyValue(EXMARaLDAImporterProperties.PROP_STRICT_FILE_TYPE,
        false);

    // importing corpus
    start();


    SCorpusGraph cg = getFixture().getCorpusGraph();
    assertEquals(1, cg.getCorpora().size());
    assertEquals("specificFileEnding", cg.getCorpora().get(0).getName());
    assertEquals(2, cg.getDocuments().size());
  }
}
