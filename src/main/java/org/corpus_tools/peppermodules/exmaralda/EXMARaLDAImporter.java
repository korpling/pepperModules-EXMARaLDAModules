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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name = "EXMARaLDAImporterJavaComponent", factory = "PepperImporterComponentFactory")
public class EXMARaLDAImporter extends PepperImporterImpl implements PepperImporter {
	public static final String[] EXMARALDA_FILE_ENDINGS = { "exb", "xml", "xmi", "exmaralda" };

	public EXMARaLDAImporter() {
		super();
		// setting name of module
		setName("EXMARaLDAImporter");
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-EXMARaLDAModules"));
		setDesc("This importer transforms data in the exb format of EXMARaLDA to a Salt model. ");
		this.setProperties(new EXMARaLDAImporterProperties());
		// set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);

		// adding all file endings to list of endings for documents (necessary
		// for importCorpusStructure)
		for (String ending : EXMARALDA_FILE_ENDINGS)
			this.getSDocumentEndings().add(ending);
	}

	/** emf resource loader **/
	private ResourceSet resourceSet = null;

	private ResourceSet getResourceSet() {
		if (resourceSet == null) {
			synchronized (this) {
				if (resourceSet == null) {
					resourceSet = new ResourceSetImpl();
					// Register XML resource factory
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exmaralda", new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb", new EXBResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml", new EXBResourceFactory());
				}

			}
		}
		return (resourceSet);
	}

	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId) {
		EXMARaLDA2SaltMapper mapper = new EXMARaLDA2SaltMapper();
		URI resourcePath = this.getSElementId2ResourceTable().get(sElementId);
		if (sElementId.getSIdentifiableElement() instanceof SDocument) {
			mapper.setResourceURI(resourcePath);
			mapper.setResourceSet(getResourceSet());
		}
		return (mapper);
	}
}
