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

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.core.SelfTestDesc;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

@Component(name = "EXMARaLDAImporterJavaComponent", factory = "PepperImporterComponentFactory")
public class EXMARaLDAImporter extends PepperImporterImpl implements PepperImporter {
  public static final String[] EXMARALDA_FILE_ENDINGS = {"exb", "xml", "xmi", "exmaralda"};

  public EXMARaLDAImporter() {
    super();
    // setting name of module
    setName("EXMARaLDAImporter");
    setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
    setSupplierHomepage(
        URI.createURI("https://github.com/korpling/pepperModules-EXMARaLDAModules"));
    setDesc("This importer transforms data in the exb format of EXMARaLDA to a Salt model. ");
    setProperties(new EXMARaLDAImporterProperties());
    // set list of formats supported by this module
    addSupportedFormat("EXMARaLDA", "1.0", null);

    // adding all file endings to list of endings for documents (necessary
    // for importCorpusStructure)
    for (String ending : EXMARALDA_FILE_ENDINGS) {
      getDocumentEndings().add(ending);
    }
  }

  /** emf resource loader **/
  private ResourceSet resourceSet = null;

  private ResourceSet getResourceSet() {
    if (resourceSet == null) {
      synchronized (this) {
        if (resourceSet == null) {
          resourceSet = new ResourceSetImpl();
          // Register XML resource factory
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exmaralda",
              new XMIResourceFactoryImpl());
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
              new XMIResourceFactoryImpl());
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb",
              new EXBResourceFactory());
          resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",
              new EXBResourceFactory());
        }

      }
    }
    return (resourceSet);
  }

  @Override
  public SelfTestDesc getSelfTestDesc() {
    return new SelfTestDesc(
        getResources().appendSegment("selfTests").appendSegment("exmaraldaImporter")
            .appendSegment("in"),
        getResources().appendSegment("selfTests").appendSegment("exmaraldaImporter")
            .appendSegment("expected"));
  }

  @Override
  public Double isImportable(URI corpusPath) {
    Double retValue = 0.0;
    for (String content : sampleFileContent(corpusPath, EXMARALDA_FILE_ENDINGS)) {
      if ((content.contains("<?xml version=\"1.0\""))
          && (content.contains("<basic-transcription>"))) {
        retValue = 1.0;
        break;
      }
    }
    return retValue;
  }


  @Override
  public SALT_TYPE setTypeOfResource(URI resource) {
    SALT_TYPE result = super.setTypeOfResource(resource);
    if (result == SALT_TYPE.SDOCUMENT && getProps().getStrictFileType()) {
      // Only allow "exb" file ending
      if ("exb".equalsIgnoreCase(resource.fileExtension())) {
        return SALT_TYPE.SDOCUMENT;

      } else {
        return null;
      }
    } else {
      return result;
    }
  }

  /**
   * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
   * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
   */
  @Override
  public PepperMapper createPepperMapper(Identifier sElementId) {
    EXMARaLDA2SaltMapper mapper = new EXMARaLDA2SaltMapper();
    URI resourcePath = getIdentifier2ResourceTable().get(sElementId);
    if (sElementId.getIdentifiableElement() instanceof SDocument) {
      mapper.setResourceURI(resourcePath);
      mapper.setResourceSet(getResourceSet());
    }
    return (mapper);
  }

  /**
   * casts {@link PepperModulePropertiesImpl} to {@link EXMARaLDAImporterProperties}
   **/
  public EXMARaLDAImporterProperties getProps() {
    return ((EXMARaLDAImporterProperties) this.getProperties());
  }
}
