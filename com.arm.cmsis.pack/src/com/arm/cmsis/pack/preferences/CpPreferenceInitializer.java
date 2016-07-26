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

package com.arm.cmsis.pack.preferences;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackRootProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 *  Initializes CMSIS pack and RTE preferences
 */
public class CpPreferenceInitializer extends AbstractPreferenceInitializer {

	private static ICpPackRootProvider packRootProvider = null;
	
	/**
	 *  Default constructor
	 */
	public CpPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		if (defaultPreferences.get(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CmsisConstants.EMPTY_STRING).isEmpty()) {
			String defaultValue = getDefaultPackRoot();
			defaultPreferences.put(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, defaultValue);
		}
		String defaultRepoKey = CpPlugIn.CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + 0;
		if (defaultPreferences.get(defaultRepoKey, CmsisConstants.EMPTY_STRING).isEmpty()) {
			String defaultValue = getDefaultCpRepository();
			defaultPreferences.put(defaultRepoKey, defaultValue);
		}
	}

	/**
	 * Returns environment-specific provider of CMSIS Pack root directory 
	 * @return
	 */
	public static ICpPackRootProvider getCmsisRootProvider() {
		if(packRootProvider == null) {
			ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
			if(envProvider != null)
				packRootProvider = envProvider.getCmsisRootProvider();
		}
		return packRootProvider;
	}

	public static String getDefaultPackRoot() {
		String defaultValue = CmsisConstants.EMPTY_STRING;
		ICpPackRootProvider provider = getCmsisRootProvider();
		if(provider != null) {
			String root = provider.getPackRoot();
			if(root != null) {
				defaultValue = root;
			}
		}
		return defaultValue;
	}

	public static String getPackRoot() {
		IPreferencesService prefs = Platform.getPreferencesService();
		String defaultRoot = getDefaultPackRoot();
		if(!defaultRoot.isEmpty()) {
			setPackRoot(defaultRoot);  // synchronize preferences with external provider
			return defaultRoot;
		}
		String packRoot = prefs.getString(CpPlugIn.PLUGIN_ID, CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, defaultRoot, null);
		return packRoot;
	}

	public static void setPackRoot(String newPackRoot) {
		if(newPackRoot == null) {
			newPackRoot = CmsisConstants.EMPTY_STRING;
		}
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		String oldPackRoot = instancePreferences.get(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CmsisConstants.EMPTY_STRING);
		if(!newPackRoot.equals(oldPackRoot)) {
			instancePreferences.put(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, newPackRoot);
		}
	}

	public static List<String> getCpRepositories() {
		List<String> repos = new LinkedList<String>();
		IPreferencesService prefs = Platform.getPreferencesService();
		int i = 0;
		String key = CpPlugIn.CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
		String repo = prefs.getString(CpPlugIn.PLUGIN_ID, key, CmsisConstants.EMPTY_STRING, null);
		while (!repo.isEmpty()) {
			repos.add(repo);
			i++;
			key = CpPlugIn.CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
			repo = prefs.getString(CpPlugIn.PLUGIN_ID, key, CmsisConstants.EMPTY_STRING, null);
		}
		return repos;
	}

	public static String getDefaultCpRepository() {
		IAttributes attr = new Attributes();
		attr.setAttribute(CmsisConstants.REPO_TYPE, CmsisConstants.REPO_PACK_TYPE);
		attr.setAttribute(CmsisConstants.REPO_NAME, CmsisConstants.REPO_KEIL);
		attr.setAttribute(CmsisConstants.REPO_URL, CmsisConstants.REPO_KEILINDEX);
		return attr.toString();
	}

	public static boolean hasCmsisRootProvider() {
		return getCmsisRootProvider() != null;
	}

	public static void destroy() {
		packRootProvider = null;
	}


}
