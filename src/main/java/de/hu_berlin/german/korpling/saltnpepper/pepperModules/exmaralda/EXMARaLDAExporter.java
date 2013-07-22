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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperFWException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="EXMARaLDAExporterJavaComponent", factory="PepperExporterComponentFactory")
public class EXMARaLDAExporter extends PepperExporterImpl implements PepperExporter
{
	/** ending of exmaralda files **/
	public static final String FILE_EXTENION="exb";
	
	public EXMARaLDAExporter()
	{
		super();
		//setting name of module
		this.name= "EXMARaLDAExporter";
		//set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId){
		Salt2EXMARaLDAMapper mapper= new Salt2EXMARaLDAMapper();
		mapper.setResourceURI(getSElementId2ResourceTable().get(sElementId));
		return(mapper);
	}
	
	@Override
	public void exportCorpusStructure(SCorpusGraph sCorpusGraph)
	{
		if (sCorpusGraph== null)
			throw new PepperFWException("No SCorpusGraph was passed for exportCorpusStructure(SCorpusGraph corpusGraph). This might be a bug of the pepper framework.");
		else 
		{
			for (SDocument sDocument: sCorpusGraph.getSDocuments())
			{
				this.createFolderStructure(sDocument.getSElementId());
				URI uri= URI.createFileURI(this.getCorpusDefinition().getCorpusPath().toFileString()+ "/" + sDocument.getSElementPath().toFileString()+ "." + FILE_EXTENION);
				this.getSElementId2ResourceTable().put(sDocument.getSElementId(), uri);
			}
		}
	}
}
