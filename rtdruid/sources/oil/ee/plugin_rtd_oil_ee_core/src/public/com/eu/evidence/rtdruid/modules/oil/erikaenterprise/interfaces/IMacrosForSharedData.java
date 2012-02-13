package com.eu.evidence.rtdruid.modules.oil.erikaenterprise.interfaces;

/**
 * This interface is used to protect shared variables using macros
 * 
 * @author Nicola Serreli
 *
 */
public interface IMacrosForSharedData {
	
	public interface IPragma extends IMacrosForSharedData {
		String getPragmaSections();
	}
	
	IPragma getPragma();
	
	String vectorRam(String vectorName, String array);

	String vectorRom(String vectorName, String array);

	String constVectorRam(String vectorName, String array);

	String constVectorRom(String vectorName, String array);

	String valueRam(String valueName);

	String valueRom(String valueName);

	String constValueRam(String valueName);

	String constValueRom(String valueName);
	
}
