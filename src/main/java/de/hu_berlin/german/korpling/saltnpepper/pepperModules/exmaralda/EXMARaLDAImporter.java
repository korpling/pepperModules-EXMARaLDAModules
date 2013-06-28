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
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="EXMARaLDAImporterJavaComponent", factory="PepperImporterComponentFactory")
public class EXMARaLDAImporter extends PepperImporterImpl implements PepperImporter
{	
	public static final String PROP_TOKEN="salt.token";
	public static final String PROP_TOKENSEP="salt.tokenSeperator";
	public static final String PROP_TIERMERGE="salt.tierMerge";
	public static final String PROP_LAYERS_SMALL="salt.layers";
	public static final String PROP_LAYERS_BIG="salt.Layers";
	public static final String PROP_URI_ANNOTATION="salt.URIAnnotation";
	public static final String PROP_SALT_SEMANTICS_POS="saltSemantics.POS";
	public static final String PROP_SALT_SEMANTICS_LEMMA="saltSemantics.LEMMA";
	public static final String PROP_SALT_SEMANTICS_WORD="saltSemantics.WORD";
	
	public static final String[] EXMARALDA_FILE_ENDINGS={"exb", "xml", "xmi", "exmaralda"};
	
	public EXMARaLDAImporter()
	{
		super();
		//setting name of module
		this.name= "EXMARaLDAImporter";

		this.setProperties(new EXMARaLDAImporterProperties());
		//set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);
		
		//adding all file endings to list of endings for documents (necessary for importCorpusStructure)
		for (String ending: EXMARALDA_FILE_ENDINGS)
			this.getSDocumentEndings().add(ending);
	}

	/** emf resource loader **/
	private ResourceSet resourceSet = null;

	private ResourceSet getResourceSet()
	{
		if (resourceSet== null)
		{
			synchronized (this)
			{
				if (resourceSet== null)
				{
					resourceSet = new ResourceSetImpl();
					// Register XML resource factory
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exmaralda",new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb",new EXBResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",new EXBResourceFactory());
				}
				
			}
		}
		return(resourceSet);
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId)
	{
		EXMARaLDA2SaltMapper mapper= new EXMARaLDA2SaltMapper();
		System.out.println("this.getSElementId2ResourceTable(): "+ this.getSElementId2ResourceTable());
		URI resourcePath= this.getSElementId2ResourceTable().get(sElementId);
		if (sElementId.getSIdentifiableElement() instanceof SDocument)
		{
			//load resource 
			Resource resource = getResourceSet().createResource(resourcePath);
			if (resource== null)
				throw new EXMARaLDAImporterException("Cannot load the exmaralda file: "+ resourcePath+", becuase the resource is null.");
			try {
				resource.load(null);
			} catch (IOException e) 
			{
				throw new EXMARaLDAImporterException("Cannot load the exmaralda file: "+ resourcePath+".", e);
			}
			
			BasicTranscription basicTranscription=null;
			basicTranscription= (BasicTranscription) resource.getContents().get(0);	
			
			mapper.setBasicTranscription(basicTranscription);
		}
		return(mapper);
	}
	
//	@Override
//	public void start(SElementId sElementId) throws EXMARaLDAImporterException 
//	{
//		Long timeImportSDocumentStructure= System.nanoTime();
//		{//checking special parameter
//			if (this.getSpecialParams()== null)
//				throw new EXMARaLDAImporterException("Cannot start converting, because no special parameters are set.");
//			File specialParamFile= new File(this.getSpecialParams().toFileString());
//			if (!specialParamFile.exists())
//				throw new EXMARaLDAImporterException("Cannot start converting, because the file for special parameters does not exist: "+ specialParamFile);
//			if (!specialParamFile.isFile())
//				throw new EXMARaLDAImporterException("Cannot start converting, because the file for special parameters is not a file: "+ specialParamFile);
//		}
//		if(!	(	(sElementId.getSIdentifiableElement() instanceof SDocument) ||
//					(sElementId.getSIdentifiableElement() instanceof SCorpus)))
//			throw new EXMARaLDAImporterException("Cannot import data to given sElementID "+sElementId.getSId()+", because the corresponding element is not of kind SDocument. It is of kind: "+ sElementId.getSIdentifiableElement().getClass().getName());
//		//getting uri of elementID
//		if (sElementId.getSIdentifiableElement() instanceof SCorpus)
//		{
//			;
//		}
//		else if (sElementId.getSIdentifiableElement() instanceof SDocument)
//		{//sElementId belongs to SDOcument-object
//			URI documentPath= this.getSElementId2ResourceTable().get(sElementId);
//			if (documentPath!= null)
//			{
//				BasicTranscription basicTranscription=null;
//				{//loading exmaralda model
//					Long timeToLoadSDocumentStructure= System.nanoTime();
//					// create resource set and resource 
//					ResourceSet resourceSet = new ResourceSetImpl();
//	
//					// Register XML resource factory
//					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exmaralda",new XMIResourceFactoryImpl());
//					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",new XMIResourceFactoryImpl());
//					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb",new EXBResourceFactory());
//					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",new EXBResourceFactory());
//					//load resource 
//					Resource resource = resourceSet.createResource(documentPath);
//					if (resource== null)
//						throw new EXMARaLDAImporterException("Cannot load the exmaralda file: "+ documentPath+", becuase the resource is null.");
//					try {
//						resource.load(null);
//					} catch (IOException e) 
//					{
//						throw new EXMARaLDAImporterException("Cannot load the exmaralda file: "+ documentPath+".", e);
//					}
//					basicTranscription= (BasicTranscription) resource.getContents().get(0);	
//				}//loading exmaralda model
//				
//				{//creating and starting the mapping between exmaralda and salt
//					Long timeToMapSDocumentStructure= System.nanoTime();
//					EXMARaLDA2SaltMapper mapper= new EXMARaLDA2SaltMapper();
//					Properties props= new Properties();
//					try {
//						props.load(new InputStreamReader(new FileInputStream(this.getSpecialParams().toFileString())));
//					} catch (FileNotFoundException e) {
//						
//					} catch (IOException e) {
//						throw new EXMARaLDAImporterException("Cannot start converting, because can not read the given file for special parameters: "+ this.getSpecialParams());
//					}
//					mapper.setProps(props);
//					mapper.setsDocument((SDocument)sElementId.getIdentifiableElement());
//					mapper.setBasicTranscription(basicTranscription);
//					//setting the current file-path to current document
//					mapper.setDocumentFilePath(documentPath);
//					mapper.setLogService(this.getLogService());
//					mapper.startMapping();
//					this.totalTimeToMapDocument= this.totalTimeToMapDocument + (System.nanoTime()- timeToMapSDocumentStructure);
//				}//creating and starting the mapping between exmaralda and salt
//			}
//		}
//		this.totalTimeImportSDocumentStructure= this.totalTimeImportSDocumentStructure + (System.nanoTime()- timeImportSDocumentStructure);
//	}
}
