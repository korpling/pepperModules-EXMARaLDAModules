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

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.core.SelfTestDesc;
import org.corpus_tools.pepper.impl.PepperExporterImpl;
import org.corpus_tools.pepper.modules.PepperExporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;


@Component(name = "EXMARaLDAExporterJavaComponent", factory = "PepperExporterComponentFactory")
public class EXMARaLDAExporter extends PepperExporterImpl implements PepperExporter {
	/** ending of exmaralda files **/
	public static final String FILE_EXTENION = "exb";

	public EXMARaLDAExporter() {
		super();
		// setting name of module
		setName("EXMARaLDAExporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-EXMARaLDAModules"));
		setDesc("This exporter transforms a Salt model into the exb format for EXMARaLDA.  ");
		// set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);
		setDocumentEnding(FILE_EXTENION);
		setExportMode(EXPORT_MODE.DOCUMENTS_IN_FILES);
		setProperties(new EXMARaLDAExporterProperties());
	}

	@Override
	public SelfTestDesc getSelfTestDesc() {
		return new SelfTestDesc(
				getResources().appendSegment("selfTests").appendSegment("exmaraldaExporter").appendSegment("in"),
				getResources().appendSegment("selfTests").appendSegment("exmaraldaExporter").appendSegment("expected"));
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}. {@inheritDoc
	 * PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		Salt2EXMARaLDAMapper mapper = new Salt2EXMARaLDAMapper();
		mapper.setResourceURI(getIdentifier2ResourceTable().get(sElementId));
		return (mapper);
	}
}
