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

package com.arm.cmsis.pack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Base class to handle creation of objects contributed via com.arm.cmsis.pack.*
 * extension points
 */
public abstract class CpAbstractExtensionFactory<T> {

    public static final String ID = "id"; //$NON-NLS-1$
    public static final String CLASS = "class"; //$NON-NLS-1$
    protected Map<String, IConfigurationElement> elements = new HashMap<String, IConfigurationElement>();
    protected T fExtender = null;

    protected CpAbstractExtensionFactory(String extensionPointId) {
        initialize(extensionPointId);
    }

    protected String getExtensionPointId(String elementId) {
        return CpPlugIn.PLUGIN_ID + '.' + elementId;
    }

    /**
     * Loads extension point data
     */
    private void initialize(String elementId) {
        String extensionPointId = getExtensionPointId(elementId);
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(extensionPointId).getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement config : configElements) {
                String configName = config.getName();
                if (configName.equals(elementId)) {
                    String id = config.getAttribute(ID);
                    elements.put(id, config);
                }
            }
        }
    }

    /**
     * Returns extender, instantiates it if needed
     *
     * @return T
     */
    public synchronized T getExtender() {
        if (fExtender == null) {
            fExtender = createExtender();
        }
        return fExtender;
    }

    protected T createExtender() {
        IConfigurationElement element = null;
        T extender = null;
        for (Entry<String, IConfigurationElement> e : elements.entrySet()) {
            String id = e.getKey();
            IConfigurationElement elem = e.getValue();
            // Prioritize non-ARM extenders
            if (element == null || !id.startsWith(CpPlugIn.PLUGIN_ID)) {
                element = elem;
            }
        }
        if (element != null) {
            try {
                extender = createExtender(element);
            } catch (CoreException e1) {
                e1.printStackTrace();
            }
        }
        return extender;
    }

    protected T createExtender(IConfigurationElement element) throws CoreException {
        if (element == null) {
            return null;
        }
        Object obj = element.createExecutableExtension(CLASS);
        T extender = castToExtenderClass(obj);
        if (extender != null) {
            return castToExtenderClass(obj);
        }
        Status status = new Status(IStatus.ERROR, CpPlugIn.PLUGIN_ID, "Invalid extension class specified"); //$NON-NLS-1$
        throw new CoreException(status);
    }

    protected abstract T castToExtenderClass(Object extender);

}
