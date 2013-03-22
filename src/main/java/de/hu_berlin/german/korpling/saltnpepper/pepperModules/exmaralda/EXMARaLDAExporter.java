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

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="EXMARaLDAExporterJavaComponent", factory="PepperExporterComponentFactory")
public class EXMARaLDAExporter extends PepperExporterImpl implements PepperExporter
{
	public EXMARaLDAExporter()
	{
		super();
		//setting name of module
		this.name= "EXMARaLDAExporter";
		//set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);
	}

	public static final String FILE_EXTENION="exb";
	
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (sElementId.getSIdentifiableElement() instanceof SDocument)
		{
			SDocument sDoc= (SDocument) sElementId.getSIdentifiableElement();
			BasicTranscription basicTranscription= ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
			
			// start: mapping
				Salt2EXMARaLDAMapper mapper= new Salt2EXMARaLDAMapper();
				try
				{
					mapper.map2BasicTranscription(sDoc, basicTranscription);
				}catch (Exception e) {
					e.printStackTrace();
				}
			// start: mapping
			
			this.createFolderStructure(sElementId);
			//create uri to save
			URI uri= URI.createFileURI(this.getCorpusDefinition().getCorpusPath().toFileString()+ "/" + sElementId.getSElementPath().toFileString()+ "." + FILE_EXTENION);
			try {
				this.saveToFile(uri, basicTranscription);
			} catch (IOException e) {
				throw new EXMARaLDAExporterException("Cannot write document with id: '"+sElementId.getSElementPath().lastSegment()+"' into uri: '"+uri+"'.", e);
			}
		}
	}
	
	private void saveToFile(URI uri, BasicTranscription basicTranscription) throws IOException
	{
		// create resource set and resource 
		ResourceSet resourceSet = new ResourceSetImpl();
		// Register XML resource factory
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb",new EXBResourceFactory());
		//load resource 
		Resource resource = resourceSet.createResource(uri);
		if (resource== null)
			throw new EXMARaLDAExporterException("Cannot save a resource to uri '"+uri+"', because the given resource is null.");
		
		resource.getContents().add(basicTranscription);
		resource.save(null);
	}
}
