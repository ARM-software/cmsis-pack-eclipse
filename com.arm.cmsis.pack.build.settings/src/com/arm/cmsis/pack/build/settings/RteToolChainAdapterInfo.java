/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.build.settings;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.arm.cmsis.pack.build.IBuildSettings;

/**
 * The class describes an entry defined by
 * com.arm.cmsis.pack.project.ToolchainAdapter extension point
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class RteToolChainAdapterInfo {

    private static final String ID = "id"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String DESCRIPTION = "Description"; //$NON-NLS-1$
    private static final String CLASS = "class"; //$NON-NLS-1$
    private static final String TOOLCHAIN_FAMILY = "toolChainFamily"; //$NON-NLS-1$

    private Set<String> fAssociations = new HashSet<String>();

    private IConfigurationElement fConfigElement;

    private String fId;
    private String fName;
    private String fTcompiler;
    private String fDescription;
    private String fPlugInId; // originating plug-in ID

    private static class NullAdapter implements IRteToolChainAdapter {
        static final NullAdapter INSTANCE = new NullAdapter();

        @Override
        public void setToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {
            // does nothing
        }

        @Override
        public void setInitialToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {
            // does nothing
        }

        @Override
        public ILinkerScriptGenerator getLinkerScriptGenerator() {
            return null;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Object getAdapter(Class adapter) {
            return null;
        }
    }

    /**
     * Constructs info out of IConfigurationElement
     *
     * @param element IConfigurationElement
     */
    public RteToolChainAdapterInfo(IConfigurationElement element) {
        fConfigElement = element;
        fId = element.getAttribute(ID);
        fName = element.getAttribute(NAME);
        fTcompiler = element.getAttribute(TOOLCHAIN_FAMILY);
        fDescription = element.getAttribute(DESCRIPTION);
        fPlugInId = element.getNamespaceIdentifier();
    }

    /**
     * Returns IRteToolChainAdapter, instantiates it if needed
     *
     * @return IRteToolChainAdapter
     */
    public synchronized IRteToolChainAdapter getToolChainAdapter() {
        IRteToolChainAdapter adapter = null;
        try {
            adapter = createAdapter();
        } catch (CoreException e) {
            CCorePlugin.log(e);
        }
        if (adapter == null) {
            adapter = NullAdapter.INSTANCE;
        }
        return adapter;
    }

    private IRteToolChainAdapter createAdapter() throws CoreException {
        Object obj = fConfigElement.createExecutableExtension(CLASS);
        if (obj instanceof IRteToolChainAdapter) {
            return (IRteToolChainAdapter) obj;
        }
        Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Invalid toolchain adapter class specified"); //$NON-NLS-1$
        throw new CoreException(status);
    }

    /**
     * Returns unique toolchain adapter ID
     *
     * @return ID string
     */
    public String getId() {
        return fId;
    }

    /**
     * Returns toolchain adapter name
     *
     * @return toolchain adapter name
     */
    public String getName() {
        return fName;
    }

    /**
     * Returns toolchain family : corresponds to Tcompiler filter attribute in RTE
     * model
     *
     * @return toolchain family
     */
    public String getFamily() {
        return fTcompiler;
    }

    /**
     * Returns toolchain adapter short description
     *
     * @return description string
     */
    public String getDescription() {
        return fDescription;
    }

    /**
     * Addst toolchain association to this adapter
     *
     * @param toolChainId toolchain id this adapter
     */
    public void addToolChainAssociation(String toolChainId) {
        int pos = toolChainId.indexOf('*');
        if (pos >= 0) {
            if (toolChainId.equals("*")) // actually the same as no limit //$NON-NLS-1$
                return;
            // translate wildcard to regexp
            String regex = ("\\Q" + toolChainId + "\\E").replace("*", "\\E.*\\Q"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            fAssociations.add(regex);
        } else {
            fAssociations.add(toolChainId);
        }
    }

    /**
     *
     * @param toolChain IToolChain to match
     * @return one of the following (best match is positive and has lower weight)
     *         <dl>
     *         <dt>0
     *         <dd>adapter does not match the toolchain
     *         <dt>99
     *         <dd>adapter has no associations => generic
     *         <dt>10...39
     *         <dd>exact match of toolchain ID or its super classes ID
     *         <dt>40...
     *         <dd>wildcard match of toolchain ID or its super classes ID
     *         </dl>
     */
    public int matchToolChain(IToolChain toolChain) {
        if (toolChain == null)
            return 0;
        if (fAssociations.isEmpty())
            return 99;

        int result = 10;
        // first try exact match
        IToolChain tc;
        for (tc = toolChain; tc != null; tc = tc.getSuperClass(), result++) {
            String id = tc.getId();
            if (fAssociations.contains(id))
                return result;
        }

        // perform wildcard match
        result = 40;
        for (tc = toolChain; tc != null; tc = tc.getSuperClass(), result++) {
            String id = tc.getId();
            if (matchesToolChainId(id))
                return result;
        }

        return 0;
    }

    /**
     * Performs wildcard match
     *
     * @param id toolchain id to match
     * @return true if matches
     */
    private boolean matchesToolChainId(String id) {
        for (String associatedId : fAssociations) {
            boolean matches = false;
            if (associatedId.indexOf('*') < 0)
                matches = id.equals(associatedId);
            else
                matches = id.matches(associatedId);
            if (matches)
                return true;
        }
        return false;
    }

    /**
     * Returns originating plug-in ID
     *
     * @return the plug-in ID string
     */
    public String getPlugInId() {
        return fPlugInId;
    }

}
