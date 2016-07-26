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

package com.arm.cmsis.pack.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import com.arm.cmsis.pack.generic.Attributes;


/**
 * Default implementation of IBuildSettings interface
 * @see IBuildSettings
 */
public class BuildSettings extends Attributes implements IBuildSettings {

	protected Map<Integer, Collection<String> > stringSets = new HashMap<Integer, Collection<String> >();

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
		Collection<String> value = stringSets.get(Integer.valueOf(type));
		return value;

	}

	@Override
	public String getDeviceAttribute(String key) {
		return null; // default implementation has no idea about device properties
	}


	@Override
	public void addStringListValue(int type, String value) {
		if(value == null || value.isEmpty()) {
			return;
		}
		Collection<String> set = ensureSet(type);
		set.add(value);
	}

	/**
	 * Ensures a collection to contain string values for given type exists
	 * @param type type returned by IRteToolchainAdapter.getOptionType()
	 * @return existing or newly created collection for string values
	 */
	protected Collection<String> ensureSet(int type) {
		Integer key = Integer.valueOf(type);
		Collection<String> set = stringSets.get(key);
		if(set == null){
			if(type == IBuildSettings.RTE_DEFINES)
				set = new LinkedHashSet<String>();
			else
				set = new TreeSet<String>();
			stringSets.put(key, set);
		}
		return set;
	}

	@Override
	public String getSingleLinkerScriptFile() {
		Collection<String> scripts = getStringListValue(IBuildSettings.RTE_LINKER_SCRIPT);
		if(scripts != null && scripts.size() >= 1) {
			for(String s : scripts) {
				return s;
			}
		}
		return null;
	}

}
