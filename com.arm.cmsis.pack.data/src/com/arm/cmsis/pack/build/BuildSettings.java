/*******************************************************************************
* Copyright (c) 2015-2018 ARM Ltd. and others
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
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;


/**
 * Default implementation of IBuildSettings interface
 * @see IBuildSettings
 */
public class BuildSettings extends Attributes implements IBuildSettings {

	protected Map<Integer, Collection<String> > stringLists = new HashMap<>();
	protected Map<String, IBuildSettings> individualSettings = null;
	protected IBuildSettings parentSettings = null;
	protected Level level = Level.PROJECT;
	/**
	 *  Default constructor
	 */
	public BuildSettings() {
	}

	public BuildSettings(IBuildSettings parent, Level level) {
		parentSettings = parent;
		this.level = level;
	}
	
	@Override
	synchronized public void clear(){
		stringLists.clear();
		individualSettings = null;
		super.clear();
	}


	@Override
	public IBuildSettings getParentSettings() {
		return parentSettings;
	}

	
	@Override
	public Level getLevel() {
		return level ;
	}
	
	@Override
	public Collection<String> getStringListValue(int type) {
		Collection<String> value = stringLists.get(type);
		if( getParentSettings() == null)
			return value;
		Collection<String> parentValue = getParentSettings().getStringListValue(type);
		if(parentValue == null || parentValue.isEmpty())
			return value;
		Collection<String> removeValue = stringLists.get(-type); 
		if((value == null || value.isEmpty()) && (removeValue == null || removeValue.isEmpty()) )
			return parentValue;
		
		Collection<String> merged = createList(type);
		if(value != null) {
			merged.addAll(value);
		}
		merged.addAll(parentValue);
		if(removeValue != null) {
			merged.removeAll(removeValue);
		}
		return merged;
	}
	
	@Override
	public String getDeviceAttribute(String key) {
		return getAttribute(key); // since device attribue keys do not collide with others, we can reuse them
	}

	@Override
	public void addStringListValue(int type, String value) {
		if(value == null || value.isEmpty()) {
			return;
		}
		Collection<String> list = ensureList(type);
		list.add(value);
	}

	@Override
	synchronized public String getAttribute(String key) {
		String value = super.getAttribute(key);
		if(value == null && getParentSettings() != null && getParentSettings().getLevel() == Level.VIRTUAL_GROUP) {
			return getParentSettings().getAttribute(key);
		}
		return value;
	}

	@Override
	synchronized public boolean hasAttribute(String key) {
		if(super.hasAttribute(key))
			return true;
		if(getParentSettings() != null && getParentSettings().getLevel() == Level.VIRTUAL_GROUP) {
			return getParentSettings().hasAttribute(key);
		}
		return false;
	}

	
	/**
	 * Ensures a collection to contain string values for given type exists
	 * @param type type returned by IRteToolchainAdapter.getOptionType()
	 * @return existing or newly created collection for string values
	 */
	protected Collection<String> ensureList(int type) {
		Integer key = Integer.valueOf(type);
		Collection<String> list = stringLists.get(key);
		if(list == null){
			list = createList(type);
			stringLists.put(key, list);
		}
		return list;
	}
	
	/**
	 * Creates a string collection for given type
	 * @param type type returned by IRteToolchainAdapter.getOptionType()
	 * @return new collection for string values 
	 */
	protected Collection<String> createList(int type) {
		switch(type) {
		case RTE_INCLUDE_PATH   :
		case RTE_PRE_INCLUDES	:			
		case CINCPATHS_OPTION 	:
		case CPPINCPATHS_OPTION :
		case ASMINCPATHS_OPTION :
			return new TreeSet<>(new AlnumComparator(false, true));
		default:
			return new LinkedList<>();
		}
	}
	

	@Override
	public String getSingleLinkerScriptFile() {
		return getSingleStringValue(IBuildSettings.RTE_LINKER_SCRIPT);
	}

	@Override
	public IBuildSettings getBuildSettings(String resourcePath) {
		if(individualSettings == null) 
			return null;
		
		return individualSettings.get(toCanonicalResourcePath(resourcePath));
	}

	@Override
	public Map<String, IBuildSettings> getChildren() {
		return individualSettings;
	}

	@Override
	public IBuildSettings createBuildSettings(String resourcePath, IBuildSettings.Level level) {
		String canonicalPath = toCanonicalResourcePath(resourcePath); 
		IBuildSettings buildSettings = getBuildSettings(canonicalPath);
		if(buildSettings == null) {
			if(individualSettings == null) {
				individualSettings = new TreeMap<>();
			}
			buildSettings = new BuildSettings(this, level);
			individualSettings.put(canonicalPath, buildSettings);
		}
		return buildSettings;
	}

	/**
	 * Converts all backslashes to slashes, removes leading "./" and trailing '/'
	 * @param resourcePath resource path to convert
	 * @return converted resource path
	 */
	public String toCanonicalResourcePath(String resourcePath) {
		if(resourcePath == null)
			return null;
		String canonicalPath = resourcePath.replace('\\',  '/');
		if(canonicalPath.startsWith("./")) { //$NON-NLS-1$
			return canonicalPath.substring(2);
		}
		return Utils.removeTrailingSlash(canonicalPath);
	}
	
}
