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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Default implementation of IBuildSettings interface
 * @see IBuildSettings 
 */
public class BuildSettings implements IBuildSettings {

	protected Map<Integer, Set<String> > stringSets = new HashMap<Integer, Set<String> >();

	/**
	 *  Default constructor
	 */
	public BuildSettings() {
	}

	/**
	 *  Clears all collections
	 */
	public void clear(){
		stringSets.clear();
	}
	
	
	@Override
	public Collection<String> getStringListValue(int type) {
		Set<String> value = stringSets.get(Integer.valueOf(type));
		return value;
		
	}

	@Override
	public String getDeviceAttribute(String key) {
		return null; // default implementation has no idea about device properties
	}

		
	@Override
	public void addStringListValue(int type, String value) {
		if(value == null || value.isEmpty())
			return;
		Set<String> set = ensureSet(type); 
		set.add(value);
	}

	/**
	 * Ensures a collection to contain string values for given type exists 
	 * @param type type returned by IRteToolchainAdapter.getOptionType() 
	 * @return existing or newly created collection for string values 
	 */
	protected Set<String> ensureSet(int type) {
		Integer key = Integer.valueOf(type);
		Set<String> set = stringSets.get(key);
		if(set == null){
			 set = new TreeSet<String>();
			 stringSets.put(key, set);
		}
		return set;
	}

	@Override
	public String getSingleLinkerScriptFile() {
		Collection<String> scripts = getStringListValue(IRteToolChainAdapter.LINKER_SCRIPT_OPTION);
		if(scripts != null && scripts.size() >= 1) {
			for(String s : scripts) 
				return s;
		} 
		return null;
	}

}
