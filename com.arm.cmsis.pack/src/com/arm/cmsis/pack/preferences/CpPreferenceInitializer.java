/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackRootProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.utils.Encryptor;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Initializes CMSIS-Pack and RTE preferences
 */
public class CpPreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String CMSIS_PACK_ROOT_PREFERENCE = "com.arm.cmsis.pack.root"; //$NON-NLS-1$
    public static final String CMSIS_PACK_INSTALL_MISSING_PACKS_PREFERENCE = "com.arm.cmsis.pack.install.missing"; //$NON-NLS-1$
    public static final String CMSIS_PACK_REPOSITORY_PREFERENCE = "com.arm.cmsis.pack.repository"; //$NON-NLS-1$
    private static final String PROXY_PREFIX = "com.arm.cmsis.proxy."; //$NON-NLS-1$
    public static final String PROXY_MODE = PROXY_PREFIX + "mode"; //$NON-NLS-1$
    public static final String PROXY_ADDRESS = PROXY_PREFIX + "address"; //$NON-NLS-1$
    public static final String PROXY_PORT = PROXY_PREFIX + "port"; //$NON-NLS-1$
    public static final String PROXY_USER = PROXY_PREFIX + "username"; //$NON-NLS-1$
    public static final String PROXY_PASSWORD = PROXY_PREFIX + "password"; //$NON-NLS-1$

    protected static ICpPackRootProvider packRootProvider = null;

    private static final String UPDATE_CFG = "update.cfg"; //$NON-NLS-1$
    private static String lastUpdateTime = CmsisConstants.EMPTY_STRING;
    private static Boolean autoUpdateFlag = null;

    /**
     * Default constructor
     */
    public CpPreferenceInitializer() {
        // does nothing
    }

    @Override
    public void initializeDefaultPreferences() {
        // Get default pack root
        String defaultPackRoot = getDefaultPackRoot();
        // and store it in the default preferences overriding the existing value
        IEclipsePreferences defaultPreferences = DefaultScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        defaultPreferences.put(CMSIS_PACK_ROOT_PREFERENCE, defaultPackRoot);

        String defaultRepoKey = CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + 0;
        String defaultValue = getDefaultCpRepository();
        defaultPreferences.put(defaultRepoKey, defaultValue);

        defaultPreferences.putBoolean(CMSIS_PACK_INSTALL_MISSING_PACKS_PREFERENCE, true);
    }

    /**
     * Returns environment-specific provider of CMSIS-Pack root directory
     *
     * @return ICpPackRootProvider, never null
     */
    public static ICpPackRootProvider getCmsisRootProvider() {
        if (packRootProvider == null) {
            // Returns the environment provider
            ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
            if (envProvider != null) {
                // Returns environment-specific provider of CMSIS-Pack root directory
                packRootProvider = envProvider.getCmsisRootProvider();
            }
            if (packRootProvider == null) {
                packRootProvider = new ICpPackRootProvider() {
                    /* default provider */};
            }
        }
        return packRootProvider;
    }

    public static String getDefaultPackRoot() {
        String defaultValue = CmsisConstants.EMPTY_STRING;
        String root = getCmsisRootProvider().getPackRoot();
        if (root == null || root.isEmpty()) {
            root = getDefaultCMSISPackDir().getAbsolutePath();
        }
        if (root == null || root.isEmpty()) {
            return defaultValue;
        }
        // normalize and convert to OS format
        IPath p = new org.eclipse.core.runtime.Path(Utils.removeTrailingSlash(root));
        defaultValue = p.toOSString();
        return defaultValue;
    }

    /**
     * Returns default CMSIS-Pack root directory as a File
     *
     * @return default CMSIS-Pack root directory as a File
     */
    public static File getDefaultCMSISPackDir() {
        File rootPackDir;

        if (Utils.getHostType().equals(CmsisConstants.WIN)) {
            String appDataPath = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
            rootPackDir = (appDataPath != null) ? new File(appDataPath) : new File(System.getProperty("user.home")); //$NON-NLS-1$
            rootPackDir = new File(rootPackDir, "Arm\\Packs"); //$NON-NLS-1$
        } else {
            String rootPath = System.getenv("XDG_CACHE_HOME"); //$NON-NLS-1$
            rootPackDir = (rootPath != null) ? new File(rootPath) : new File(System.getProperty("user.home"), ".cache"); //$NON-NLS-1$ //$NON-NLS-2$
            rootPackDir = new File(rootPackDir, "arm/packs"); //$NON-NLS-1$
        }

        // Create the directory if it doesn't exist
        ensureRootDirExists(rootPackDir);
        return rootPackDir;
    }

    private static void ensureRootDirExists(File rootPackDir) {
        if (rootPackDir.exists())
            return;
        if (!rootPackDir.mkdirs() && !rootPackDir.exists()) {
            // This method can get called with no logging configured
            System.err.printf("Unable to create CMSIS-Packs root directory: %s%n", rootPackDir.getAbsolutePath()); //$NON-NLS-1$
        }
    }

    public static String getPackRoot() {
        String packRoot;

        // Get default pack root
        String defaultPackRoot = getDefaultPackRoot();

        // Save default pack root into 'Default preference scope'
        IEclipsePreferences defaultEclipsePreferences = DefaultScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        try {
            defaultEclipsePreferences.put(CMSIS_PACK_ROOT_PREFERENCE, defaultPackRoot);
            // Forces any changes in the contents of this node and its descendants to the
            // persistent store.
            defaultEclipsePreferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        // Check if the user can edit CMSIS-Pack root preference supplied by the
        // provider
        boolean isCMSISRootPreferenceEditable = isCmsisRootEditable();

        // Set packs' directory if 'defaultRoot' is not empty and if the user can NOT
        // edit CMSIS-Pack root preference supplied by the provider
        if (!defaultPackRoot.isEmpty() && !isCMSISRootPreferenceEditable) {
            packRoot = defaultPackRoot;
            return packRoot;
        }

        // Get preferences pack root
        IEclipsePreferences eclipsePreferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        String preferencesPackRoot = eclipsePreferences.get(CMSIS_PACK_ROOT_PREFERENCE, CmsisConstants.EMPTY_STRING);

        if (defaultPackRoot.equals(preferencesPackRoot)) {
            try {
                eclipsePreferences.remove(CMSIS_PACK_ROOT_PREFERENCE);
                // Forces any changes in the contents of this node and its descendants to the
                // persistent store.
                eclipsePreferences.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
            packRoot = defaultPackRoot;
        } else {
            if (preferencesPackRoot.isEmpty()) {
                packRoot = defaultPackRoot;
            } else {
                packRoot = preferencesPackRoot;
            }
        }
        return packRoot;
    }

    public static void setPackRoot(String newPackRoot) {
        if (newPackRoot == null) {
            newPackRoot = CmsisConstants.EMPTY_STRING;
        }

        // Get default pack root
        String defaultPackRoot = getDefaultPackRoot();

        // normalize and convert to OS format
        IPath p = new org.eclipse.core.runtime.Path(Utils.removeTrailingSlash(newPackRoot));
        String newOSPackRoot = p.toOSString();

        IEclipsePreferences eclipsePreferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        String oldPackRoot = eclipsePreferences.get(CMSIS_PACK_ROOT_PREFERENCE, CmsisConstants.EMPTY_STRING);

        if (defaultPackRoot.equals(newOSPackRoot)) {
            if (!oldPackRoot.isEmpty()) {
                try {
                    eclipsePreferences.remove(CMSIS_PACK_ROOT_PREFERENCE);
                    // Forces any changes in the contents of this node and its descendants to the
                    // persistent store.
                    eclipsePreferences.flush();
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                }
            }
            return; // Nothing new to set
        }

        if (!newOSPackRoot.equals(oldPackRoot)) {
            // Associates the specified value with the specified key in this node.
            eclipsePreferences.put(CMSIS_PACK_ROOT_PREFERENCE, newOSPackRoot);
            try {
                // Forces any changes in the contents of this node and its descendants to the
                // persistent store.
                eclipsePreferences.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }

    }

    public static List<String> getCpRepositories() {
        List<String> repos = new ArrayList<>();
        IEclipsePreferences prefs = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        int i = 0;
        String key = CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
        String repo = prefs.get(key, CmsisConstants.EMPTY_STRING);
        while (!repo.isEmpty()) {
            repos.add(repo);
            i++;
            key = CMSIS_PACK_REPOSITORY_PREFERENCE + '.' + i;
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
        if (autoUpdateFlag == null) { // not defined yet
            readUpdateFile();
        }
        return autoUpdateFlag;
    }

    public static void setAutoUpdateFlag(boolean flag) {
        autoUpdateFlag = flag;
        writeUpdateFile();
    }

    /**
     * Checks if to perform automatic installation of missing packs on project load
     *
     * @return true if auto-install is enabled
     */
    public static boolean isAutoInstallMissingPacks() {
        IEclipsePreferences eclipsePreferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        return eclipsePreferences.getBoolean(CMSIS_PACK_INSTALL_MISSING_PACKS_PREFERENCE, true);
    }

    /**
     * Read the update.cfg file in .Web folder. If the file does not exist, set
     * autoUpdateFlag to true and the lastUpdateTime to current time.
     */
    private static void readUpdateFile() {
        try (Stream<String> stream = Files
                .lines(Paths.get(CpPlugIn.getPackManager().getCmsisPackWebDir(), UPDATE_CFG))) {
            stream.forEach(line -> {
                if (line.startsWith("Date=")) { //$NON-NLS-1$
                    lastUpdateTime = line.substring(5);
                } else if (line.startsWith("Auto=")) { //$NON-NLS-1$
                    autoUpdateFlag = Boolean.parseBoolean(line.substring(5));
                }
            });
        } catch (NoSuchFileException e) {
            autoUpdateFlag = true;
            updateLastUpdateTime(true);
        } catch (IOException e) {
            // do nothing
        }
    }

    private static void writeUpdateFile() {
        if (autoUpdateFlag == null) { // not defined yet
            readUpdateFile();
        }

        List<String> lines = Arrays.asList("Date=" + lastUpdateTime, "Auto=" + autoUpdateFlag.toString()); //$NON-NLS-1$ //$NON-NLS-2$
        Path file = Paths.get(CpPlugIn.getPackManager().getCmsisPackWebDir(), UPDATE_CFG);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // do nothing
        }
    }

    public static int getProxyMode() {
        IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        return preferences.getInt(PROXY_MODE, 0);
    }

    public static String getProxyAddress() {
        IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        return preferences.get(PROXY_ADDRESS, CmsisConstants.EMPTY_STRING);
    }

    public static int getProxyPort() {
        IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        String portString = preferences.get(PROXY_PORT, CmsisConstants.EMPTY_STRING);
        return Integer.parseInt(portString);
    }

    public static String getProxyUsername() {
        IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        return preferences.get(PROXY_USER, CmsisConstants.EMPTY_STRING);
    }

    public static String getProxyPassword() {
        IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode(CpPlugIn.PLUGIN_ID);
        String password = preferences.get(PROXY_PASSWORD, CmsisConstants.EMPTY_STRING);
        Encryptor encryptor = Encryptor.getEncryptor(Encryptor.DEFAULT_KEY);
        return encryptor.decrypt(password);
    }

    public static boolean isCmsisRootEditable() {
        return getCmsisRootProvider().isUserEditable();
    }

    public static void destroy() {
        packRootProvider = null;
    }

}
