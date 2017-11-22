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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

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
import com.arm.cmsis.pack.utils.Encryptor;
import com.arm.cmsis.pack.utils.Utils;

/**
 *  Initializes CMSIS pack and RTE preferences
 */
public class CpPreferenceInitializer extends AbstractPreferenceInitializer {

	protected static ICpPackRootProvider packRootProvider = null;

	private static final String UPDATE_CFG = "update.cfg"; //$NON-NLS-1$
	private static String lastUpdateTime = CmsisConstants.EMPTY_STRING;
	private static String autoUpdateFlag = CmsisConstants.EMPTY_STRING;

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
			if(envProvider != null) {
				packRootProvider = envProvider.getCmsisRootProvider();
			}
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
		if(!defaultRoot.isEmpty() && !isCmsisRootEditable()) {
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
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		int i = 0;
		String key = CpPlugIn.CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
		String repo = prefs.get(key, CmsisConstants.EMPTY_STRING);
		while (!repo.isEmpty()) {
			repos.add(repo);
			i++;
			key = CpPlugIn.CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
			repo = prefs.get(key, CmsisConstants.EMPTY_STRING);
		}
		return repos;
	}

	public static String getDefaultCpRepository() {
		IAttributes attr = new Attributes();
		attr.setAttribute(CmsisConstants.REPO_TYPE, CmsisConstants.REPO_PACK_TYPE);
		attr.setAttribute(CmsisConstants.REPO_NAME, CmsisConstants.REPO_KEIL);
		attr.setAttribute(CmsisConstants.REPO_URL, CmsisConstants.REPO_KEIL_INDEX_URL);
		return attr.toString();
	}

	public static String getLastUpdateTime() {
		if (!lastUpdateTime.isEmpty()) { // not undefined
			return lastUpdateTime;
		}
		readUpdateFile();
		return lastUpdateTime;
	}

	public static void updateLastUpdateTime(boolean useCurrentDate) {
		if (useCurrentDate) {
			lastUpdateTime = Utils.getCurrentDate();
		} else {
			lastUpdateTime = CmsisConstants.EMPTY_STRING;
		}
		writeUpdateFile();
	}

	public static boolean getAutoUpdateFlag() {
		if (!autoUpdateFlag.isEmpty()) { // not undefined
			return Boolean.parseBoolean(autoUpdateFlag);
		}
		readUpdateFile();
		return Boolean.parseBoolean(autoUpdateFlag);
	}

	public static void setAutoUpdateFlag(boolean flag) {
		autoUpdateFlag = Boolean.toString(flag);
		writeUpdateFile();
	}

	/**
	 * Read the update.cfg file in .Web folder.
	 * If the file does not exist, set autoUpdateFlag to false
	 * and the lastUpdateTime to current time.
	 */
	private static void readUpdateFile() {
		try (Stream<String> stream = Files.lines(Paths.get(CpPlugIn.getPackManager().getCmsisPackWebDir(), UPDATE_CFG))) {
			stream.forEach(line -> {
				if (line.startsWith("Date=")) { //$NON-NLS-1$
					lastUpdateTime = line.substring(5);
				} else if (line.startsWith("Auto=")) { //$NON-NLS-1$
					autoUpdateFlag = line.substring(5);
				}
			});
		} catch (NoSuchFileException e) {
			autoUpdateFlag = Boolean.toString(true);
			updateLastUpdateTime(true);
		} catch (IOException e) {
			// do nothing
		}
	}

	private static void writeUpdateFile() {
		List<String> lines = Arrays.asList("Date=" + lastUpdateTime, "Auto=" + autoUpdateFlag); //$NON-NLS-1$ //$NON-NLS-2$
		Path file = Paths.get(CpPlugIn.getPackManager().getCmsisPackWebDir(), UPDATE_CFG);
		try {
			Files.write(file, lines, Charset.forName("UTF-8")); //$NON-NLS-1$
		} catch (IOException e) {
			// do nothing
		}
	}

	public static int getProxyMode() {
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		return instancePreferences.getInt(CpPlugIn.PROXY_MODE, 0);
	}

	public static String getProxyAddress() {
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		return instancePreferences.get(CpPlugIn.PROXY_ADDRESS, CmsisConstants.EMPTY_STRING);
	}

	public static int getProxyPort() {
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		String portString = instancePreferences.get(CpPlugIn.PROXY_PORT, CmsisConstants.EMPTY_STRING);
		return Integer.parseInt(portString);
	}

	public static String getProxyUsername() {
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		return instancePreferences.get(CpPlugIn.PROXY_USER, CmsisConstants.EMPTY_STRING);
	}

	public static String getProxyPassword() {
		IEclipsePreferences instancePreferences = InstanceScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
		String password = instancePreferences.get(CpPlugIn.PROXY_PASSWORD, CmsisConstants.EMPTY_STRING);
		Encryptor encryptor = Encryptor.getEncryptor(Encryptor.DEFAULT_KEY);
		return encryptor.decrypt(password);
	}

	public static boolean hasCmsisRootProvider() {
		return getCmsisRootProvider() != null;
	}

	public static boolean isCmsisRootEditable() {
		ICpPackRootProvider rootProvider = getCmsisRootProvider();

		return rootProvider == null || rootProvider.isUserEditable();
	}

	public static void destroy() {
		packRootProvider = null;
	}


}
