/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.pack.build.settings;

import java.util.Collection;

/**
 * This interface contains build settings obtained from RTE configuration<br>
 * It provides methods with common types to avoid dependencies on IRteConfiguration internal details,  
 */
public interface IBuildSettings {

	/**
	 * Retrieves setting value as collection of strings (defines, include paths, etc) 
	 * @param type a type of setting to retrieve, corresponds to a value returned by IOption.getValueType()   
	 * @return the settings value as collection of strings or <code>null</code> if there is no string list for that type 
	 * @see IOption
	 */
	Collection<String> getStringListValue(int type);
	
	/**
	 * Adds a value string list entry to corresponding collection
	 * @param type a type of setting to add, corresponds to a value returned by IRteToolchainAdapter.getOptionType()
	 * @param value value to add
	 */
	public void addStringListValue(int type, String value);
	
	/**
	 * Retrieves attribute of selected device<br>
	 * See: <a href="http://www.keil.com/pack/doc/CMSIS/Pack/html/pdsc_family_pg.html#element_processor">"http://www.keil.com/pack/doc/CMSIS/Pack/html/pdsc_family_pg.html#element_processor"</a><br>
	 * @param key processor attribute name or one of: "Dname", "Dfamily", "DsubFamily", "Dvariant"       
	 * @return device attribute 
	 * @note the returned value in most cases cannot be set to an {@link IOption} directly<br>
	 *  it should be converted to toolchain-specific value(s) first   
	 */
	String getDeviceAttribute(String key);
	
	
	/**
	 * Returns single linker script file (or scatter file)
	 * @return linker script file if it is one and only one 
	 */
	String getSingleLinkerScriptFile();
	
}
