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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="EXMARaLDAImporterJavaComponent", factory="PepperImporterComponentFactory")
@Service(value=PepperImporter.class)
public class EXMARaLDAImporter extends PepperImporterImpl implements PepperImporter
{	
	public EXMARaLDAImporter()
	{
		super();
		//setting name of module
		this.name= "EXMARaLDAImporter";
		//set list of formats supported by this module
		this.addSupportedFormat("EXMARaLDA", "1.0", null);
	}

	/**
	 * Measured time which is needed to import the corpus structure. 
	 */
	private Long timeImportSCorpusStructure= 0l;
	/**
	 * Measured total time which is needed to import the document corpus structure. 
	 */
	private Long totalTimeImportSDocumentStructure= 0l;
	/**
	 * Measured time which is needed to load all documents into exmaralda model.. 
	 */
	private Long totalTimeToLoadDocument= 0l;
	/**
	 * Measured time which is needed to map all documents to salt. 
	 */
	private Long totalTimeToMapDocument= 0l;
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	
	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws EXMARaLDAImporterException
	{
		this.timeImportSCorpusStructure= System.nanoTime();
		this.setSCorpusGraph(corpusGraph);
		if (this.getSCorpusGraph()== null)
			throw new EXMARaLDAImporterException(this.name+": Cannot start with importing corpus, because salt project isn�t set.");
		
		if (this.getCorpusDefinition()== null)
			throw new EXMARaLDAImporterException(this.name+": Cannot start with importing corpus, because no corpus definition to import is given.");
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new EXMARaLDAImporterException(this.name+": Cannot start with importing corpus, because the path of given corpus definition is null.");
		
		if (this.getCorpusDefinition().getCorpusPath().isFile())
		{
			this.documentResourceTable= new Hashtable<SElementId, URI>();
			//clean uri in corpus path (if it is a folder and ends with/, / has to be removed)
			if (	(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("/")) || 
					(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("\\")))
			{
				this.getCorpusDefinition().setCorpusPath(this.getCorpusDefinition().getCorpusPath().trimSegments(1));
			}
			try {
				EList<String> endings= new BasicEList<String>();
				endings.add("exb");
				endings.add("xml");
				endings.add("exmaralda");
				this.documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, endings);
			} catch (IOException e) {
				throw new EXMARaLDAImporterException(this.name+": Cannot start with importing corpus, because saome exception occurs: ",e);
			}
			finally
			{
				timeImportSCorpusStructure= System.nanoTime()- timeImportSCorpusStructure;
			}
		}	
	}
	
	
	
	@Override
	public void start(SElementId sElementId) throws EXMARaLDAImporterException 
	{
		Long timeImportSDocumentStructure= System.nanoTime();
		{//checking special parameter
			if (this.getSpecialParams()== null)
				throw new EXMARaLDAImporterException("Cannot start converting, because no special parameters are set.");
			File specialParamFile= new File(this.getSpecialParams().toFileString());
			if (!specialParamFile.exists())
				throw new EXMARaLDAImporterException("Cannot start converting, because the file for special parameters does not exists: "+ specialParamFile);
			if (!specialParamFile.isFile())
				throw new EXMARaLDAImporterException("Cannot start converting, because the file for special parameters is not a file: "+ specialParamFile);
		}
		if(!	(	(sElementId.getSIdentifiableElement() instanceof SDocument) ||
					(sElementId.getSIdentifiableElement() instanceof SCorpus)))
			throw new EXMARaLDAImporterException("Cannot import data to given sElementID "+sElementId.getSId()+", because the corresponding element is not of kind SDocument. It is of kind: "+ sElementId.getSIdentifiableElement().getClass().getName());
		//getting uri of elementID
		if (sElementId.getSIdentifiableElement() instanceof SCorpus)
		{
			;
		}
		else if (sElementId.getSIdentifiableElement() instanceof SDocument)
		{//sElementId belongs to SDOcument-object
			URI documentPath= this.documentResourceTable.get(sElementId);
			if (documentPath!= null)
			{
	//			SDocument sDoc= (SDocument) sElementId.getSIdentifiableElement();
				BasicTranscription basicTranscription=null;
				{//loading exmaralda model
					Long timeToLoadSDocumentStructure= System.nanoTime();
					// create resource set and resource 
					ResourceSet resourceSet = new ResourceSetImpl();
	
					// Register XML resource factory
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exmaralda",new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",new XMIResourceFactoryImpl());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("exb",new EXBResourceFactory());
					resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xml",new EXBResourceFactory());
					//load resource 
					Resource resource = resourceSet.createResource(documentPath);
					if (resource== null)
						throw new NullPointerException("Cannot load the exmaralda file: "+ documentPath+", becuase the resource is null.");
					try {
						resource.load(null);
					} catch (IOException e) 
					{
						throw new EXMARaLDAImporterException("Cannot load the exmaralda file: "+ documentPath+".", e);
					}
					basicTranscription= (BasicTranscription) resource.getContents().get(0);	
					this.totalTimeToLoadDocument= this.totalTimeToLoadDocument + (System.nanoTime()- timeToLoadSDocumentStructure);
				}//loading exmaralda model
				
				{//creating and starting the mapping between exmaralda and salt
					Long timeToMapSDocumentStructure= System.nanoTime();
					EXMARaLDA2SaltMapper mapper= new EXMARaLDA2SaltMapper();
					Properties props= new Properties();
					try {
						props.load(new InputStreamReader(new FileInputStream(this.getSpecialParams().toFileString())));
					} catch (FileNotFoundException e) {
						
					} catch (IOException e) {
						throw new EXMARaLDAImporterException("Cannot start converting, because can not read the given file for special parameters: "+ this.getSpecialParams());
					}
					mapper.setProps(props);
					mapper.setsDocument((SDocument)sElementId.getIdentifiableElement());
					mapper.setBasicTranscription(basicTranscription);
					//setting the current file-path to current document
					mapper.setDocumentFilePath(documentPath);
					mapper.setLogService(this.getLogService());
					mapper.startMapping();
					this.totalTimeToMapDocument= this.totalTimeToMapDocument + (System.nanoTime()- timeToMapSDocumentStructure);
				}//creating and starting the mapping between exmaralda and salt
			}
		}
		this.totalTimeImportSDocumentStructure= this.totalTimeImportSDocumentStructure + (System.nanoTime()- timeImportSDocumentStructure);
	}
	
	@Override
	public void end()
	{
		super.end();
		if (this.getLogService()!= null)
		{	
			StringBuffer msg= new StringBuffer();
			msg.append("needed time of "+this.getName()+":\n");
			msg.append("\t time to import whole corpus-structure:\t\t\t\t"+ timeImportSCorpusStructure / 1000000+"\n");
			msg.append("\t total time to import whole document-structure:\t\t"+ totalTimeImportSDocumentStructure / 1000000+"\n");
			msg.append("\t total time to load whole document-structure:\t\t\t"+ totalTimeToLoadDocument / 1000000+"\n");
			msg.append("\t total time to map whole document-structure to salt:\t"+ totalTimeToMapDocument / 1000000+"\n");
			this.getLogService().log(LogService.LOG_DEBUG, msg.toString());
		}
	}
}
