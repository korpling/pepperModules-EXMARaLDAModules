package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

public class EXMARaLDAImporterException extends PepperModuleException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1562813033026040200L;

	private static String prefixStr= "This Exception was throwed by EXMARaLDAImporter, an import module for pepper. The reason is: ";
	
	public EXMARaLDAImporterException()
	{ super(); }
	
    public EXMARaLDAImporterException(String s)
    { super(prefixStr + s); }
    
	public EXMARaLDAImporterException(String s, Throwable ex)
	{super(prefixStr + s, ex); }
}
