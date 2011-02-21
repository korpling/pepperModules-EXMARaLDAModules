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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.BasicTranscription;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.ExmaraldaBasicFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.exmaralda.resources.EXBResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="EXMARaLDAExporterJavaComponent", factory="PepperExporterComponentFactory")
@Service(value=PepperExporter.class)
public class EXMARaLDAExporter extends PepperExporterImpl implements PepperExporter
{
	public EXMARaLDAExporter()
	{
		super();
		this.name= "EXMARaLDAExporter";
		//for testing the symbolic name has to be set without osgi
		if (	(this.getSymbolicName()==  null) ||
				(this.getSymbolicName().equalsIgnoreCase("")))
			this.setSymbolicName("de.hu_berlin.german.korpling.saltnpepper.pepperModules.EXMARaLDAModules");
		this.init();
		if (this.getLogService()!= null)
			this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
	}

	protected void init()
	{
		this.supportedFormats= new BasicEList<FormatDefinition>();
		FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
		formatDef.setFormatName("EXMARaLDA");
		formatDef.setFormatVersion("1.0");
		this.supportedFormats.add(formatDef);
	}

	protected void activate(ComponentContext componentContext) 
	{
		this.setSymbolicName(componentContext.getBundleContext().getBundle().getSymbolicName());
		if (this.getLogService()!= null)
			this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is activated...");
	}

	/**
	 * Wird von der Service Component Runtime vor der Deaktivierung der Komponente
	 * aufgerufen und gibt noch eine Abschiedsbotschaft aus
	 * 
	 * @param componentContext
	 *          Der Kontext der Komponente
	 */
	protected void deactivate(ComponentContext componentContext) {
		if (this.getLogService()!= null)
			this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is deactivated...");

	}
	
	String FILE_EXTENION="exb";
	
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (sElementId.getSIdentifiableElement() instanceof SDocument)
		{
			SDocument sDoc= (SDocument) sElementId.getSIdentifiableElement();
			BasicTranscription basicTranscription= ExmaraldaBasicFactory.eINSTANCE.createBasicTranscription();
			{// mapping
				Salt2EXMARaLDAMapper mapper= new Salt2EXMARaLDAMapper();
				mapper.map2BasicTranscription(sDoc, basicTranscription);
			}
			
			this.createFolderStructure(sElementId);
			//create uri to save
			URI uri= URI.createFileURI(this.getCorpusDefinition().getCorpusPath().toFileString()+ "/" + sElementId.getSElementPath()+ "." + FILE_EXTENION);
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
		resource.getContents().add(basicTranscription);
		if (resource== null)
			throw new EXMARaLDAExporterException("The resource is null.");
		resource.save(null);
	}
}
