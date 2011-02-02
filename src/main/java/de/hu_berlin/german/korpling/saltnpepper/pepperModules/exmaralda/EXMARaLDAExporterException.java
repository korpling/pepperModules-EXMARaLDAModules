package de.hu_berlin.german.korpling.saltnpepper.pepperModules.exmaralda;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;

public class EXMARaLDAExporterException extends PepperModuleException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1562813033026040200L;

	private static String prefixStr= "This Exception was throwed by EXMARaLDAExporter, an export module for pepper. The reason is: ";
	
	public EXMARaLDAExporterException()
	{ super(); }
	
    public EXMARaLDAExporterException(String s)
    { super(prefixStr + s); }
    
	public EXMARaLDAExporterException(String s, Throwable ex)
	{super(prefixStr + s, ex); }
}
