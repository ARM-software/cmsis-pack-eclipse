/*******************************************************************************
* Copyright (c) 2023 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.internal.core.HeadlessBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpPackCollection;

/**
 * Headless builder for CMSIS projects
 */
@SuppressWarnings("restriction")
public class CmsisHeadlessBuilder extends HeadlessBuilder {

    public enum Stage {
        INIT, IMPORT, UPDATE, BUILD, CLEANUP
    };

    private static final String cmsisAppId = "com.arm.cmsis.pack.project.headlessbuild"; //$NON-NLS-1$
    private static final String importArgDefault = "{[uri:/]/path/to/project/}"; //$NON-NLS-1$

    /** Error return status */
    public static final Integer EXIT_ERROR = 1; // important work is not done, processing is aborted
    public static final Integer EXIT_WARNING = 2; // work is only partially done, some of the tasks did not succeed

    public static final String cmsisRootArg = "-cmsisRoot"; //$NON-NLS-1$
    public static final String helpArg = "-help"; //$NON-NLS-1$ ;
    protected Stage processingStage = Stage.INIT; // processing stage
    protected String cmsisPackRoot = null; // CMSIS-Pack root directory supplied via command line
    protected Object totalResult = EXIT_OK;
    protected boolean bSavedAutoBuildFlag = false; // preserve workspace flag in init(), restore it in cleanup

    protected String[] fArgs = null;
    protected static final String[] helpArgs = new String[] { helpArg };
    protected static final String[] emptyArgs = new String[0];
    protected boolean fbShowUsage = false;
    protected boolean fbPacksLoaded = false;

    /**
     * Default constructor
     */
    public CmsisHeadlessBuilder() {
    }

    /**
     * Returns processing stage
     *
     * @return the processingStage
     */
    public Stage getProcessingStage() {
        return processingStage;
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {

        try {
            for (Stage stage : Stage.values()) {
                Object result = runStage(stage, context);
                if (result == EXIT_OK)
                    continue;
                totalResult = result;
                if (result == EXIT_ERROR) {
                    break;
                }
            }
        } finally {
            if (totalResult == EXIT_ERROR && getProcessingStage() != Stage.CLEANUP) {
                runStage(Stage.CLEANUP, context); // ensure cleanup stage is run
            }
        }
        return fbShowUsage ? EXIT_OK : totalResult;
    }

    /**
     * Runs processing stage
     *
     * @param stage   Stage to perform : IMPORT, UPDATE, BUILD
     * @param context the application context passed to the application
     * @return the return value of the stage
     * @throws Exception
     */
    protected Object runStage(Stage stage, IApplicationContext context) throws Exception {
        processingStage = stage;
        Location instanceLoc = Platform.getInstanceLocation();
        instanceLoc.release();
        switch (stage) {
        case INIT:
            return init(context);
        case IMPORT:
            return importProjects(context);
        case UPDATE:
            return updateProjects();
        case BUILD:
            return buildProjects(context);
        case CLEANUP:
            return cleanup(context);
        default:
            break;
        }
        return EXIT_OK;
    }

    /**
     * Initializes the application, provided for possible extensions, default does
     * nothing
     *
     * @param context the application context passed to the application
     * @return OK if successful, EXIT_ERROR or WARNING otherwise
     * @throws Exception
     */
    protected Object init(IApplicationContext context) throws Exception {
        // Turn off workspace auto-build
        bSavedAutoBuildFlag = setAutoBuild(false);
        String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        if (!getArguments(args)) {
            if (fbShowUsage) {
                printUsage(context);
                return EXIT_ERROR;
            }
        }

        // disable indexer before all operations
        if (super.start(context) != OK) {
            return EXIT_ERROR;
        }

        fbPacksLoaded = loadPacks();
        return EXIT_OK;

    }

    /**
     * Sets auto-build project flag to the workspace
     *
     * @param bAutoBuild flag to set
     * @return previous value of auto-build flag
     */
    public static boolean setAutoBuild(boolean bAutoBuild) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        boolean isAutoBuilding = root.getWorkspace().isAutoBuilding();
        if (isAutoBuilding != bAutoBuild) {
            try {
                IWorkspaceDescription desc = root.getWorkspace().getDescription();
                desc.setAutoBuilding(bAutoBuild);
                root.getWorkspace().setDescription(desc);
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return isAutoBuilding;
    }

    /**
     * Cleanups the application (still in start)
     *
     * @param context the application context passed to the application
     * @return OK if successful, EXIT_ERROR otherwise
     */
    protected Object cleanup(IApplicationContext context) {
        if (bSavedAutoBuildFlag) {
            setAutoBuild(true);
        }
        refreshWorkspace(); // make full workspace refresh
        return EXIT_OK;
    }

    /**
     * Disables indexer and imports projects specified at the command line
     *
     * @param context the application context passed to the application
     * @return the return value of import stage
     * @throws Exception
     */
    protected Object importProjects(IApplicationContext context) throws Exception {
        Object result = super.start(context);
        return result;
    }

    /**
     * Loads packs and updates CMSIS RTE projects
     *
     * @param context the application context passed to the application
     * @return OK if successful, EXIT_ERROR otherwise
     * @throws Exception
     */
    protected Object updateProjects() throws Exception {
        if (!refreshRteProjects())
            return EXIT_ERROR;
        return OK;
    }

    /**
     * Builds projects
     *
     * @param context the application context passed to the application
     * @return the return value of build stage
     * @throws Exception
     */
    protected Object buildProjects(IApplicationContext context) throws Exception {
        Object result = super.start(context);
        Location instanceLoc = Platform.getInstanceLocation();
        instanceLoc.release();
        return result;
    }

    @Override
    public boolean getArguments(String[] args) {

        boolean result = true;
        try {
            // that is the only public function we can override from the superclass
            // extract and process our arguments
            List<String> remainingArgs = new ArrayList<>();
            List<String> importArgs = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if (helpArg.equals(a)) {
                    fbShowUsage = true;
                    return false;
                }
                if (cmsisRootArg.equals(a)) {
                    cmsisPackRoot = args[++i];
                    if (cmsisPackRoot.endsWith(CmsisConstants.SLASH)
                            || cmsisPackRoot.endsWith(CmsisConstants.BACKSLASH)) {
                        cmsisPackRoot = cmsisPackRoot.substring(0, cmsisPackRoot.length() - 1);
                    }
                    continue;
                } else if ("-import".equals(a) || "-importAll".equals(a)) { //$NON-NLS-1$ //$NON-NLS-2$
                    importArgs.add(a);
                    importArgs.add(args[++i]);
                    continue; // exclude import args from remaining
                }
                remainingArgs.add(a);
            }

            if (getProcessingStage() == Stage.IMPORT) {
                // ensure indexes is not running when importing projects
                importArgs.add("-no-indexer"); //$NON-NLS-1$
                // let first super implementation run just to import projects
                return super.getArguments(importArgs.toArray(new String[importArgs.size()]));
            } else if (getProcessingStage() == Stage.INIT) {
                remainingArgs.clear(); // clear args for init stage, just disable indexer
            }
            // ensure indexes is not running when importing projects
            remainingArgs.add("-no-indexer"); //$NON-NLS-1$
            result = super.getArguments(remainingArgs.toArray(new String[remainingArgs.size()]));
            if (!result) {
                fbShowUsage = true;
            }
        } catch (Exception e) {
            fbShowUsage = true;
            return false;
        }
        return result;
    }

    /**
     * Prints command line
     *
     * @param args arguments passed to the application
     */
    protected void printUsage(IApplicationContext context) {
        String binaryName = System.getProperty("eclipse.launcher", "PROGRAM"); //$NON-NLS-1$ //$NON-NLS-2$
        String[] bindings = new String[] { binaryName, getAppId(), getImportArgumentHelpString() };

        System.out.println(NLS.bind(Messages.CommandLineUsage, bindings));
        printAdditionalArguments();
    }

    /**
     * Prints additional arguments
     */
    protected void printAdditionalArguments() {
        // default does nothing
    }

    protected String getImportArgumentHelpString() {
        return importArgDefault;
    }

    protected String getAppId() {
        return cmsisAppId;
    }

    /**
     * Initializes pack manager and loads installed packs
     *
     * @return true if successful, false otherwise
     */
    protected boolean loadPacks() {
        ICpPackManager packManager = CpPlugIn.getPackManager();
        if (packManager == null) {
            System.err.println(Messages.CmsisHeadlessBuilder_CmsisPackManagerNotAvailable);
            return false;
        }
        if (cmsisPackRoot == null || cmsisPackRoot.isEmpty()) {
            cmsisPackRoot = packManager.getCmsisPackRootDirectory();
            if (cmsisPackRoot == null || cmsisPackRoot.isEmpty()) {
                System.err.println(Messages.CmsisHeadlessBuilder_NoCmsisPackRoot);
                return false;
            }
        } else {
            packManager.setCmsisPackRootDirectory(cmsisPackRoot);
        }
        // ensure packs are loaded
        ICpPackCollection installedPacks = packManager.getInstalledPacks();
        if (installedPacks == null) {
            System.err.println(Messages.CmsisHeadlessBuilder_NoInstalledPacks);
            return false;
        }
        return true;
    }

    /**
     * Updates all opened RTE projects in the workspace
     *
     * @return true if successful, false otherwise
     */
    protected boolean refreshRteProjects() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        if (projects == null)
            return true; // nothing to do

        RteProjectManager mgr = CpProjectPlugIn.getRteProjectManager();

        for (IProject project : projects) {
            if (!project.isOpen()) {
                continue;
            }
            if (!RteProjectNature.hasRteNature(project)) {
                continue;
            }
            ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project, false);
            if (desc == null) {
                continue;
            }
            IRteProject rteProject = mgr.createRteProject(project);
            try {
                rteProject.load();
            } catch (CoreException e) {
                e.printStackTrace();
                return false;
            }
        }
        refreshWorkspace();
        return true;
    }

    /**
     * Refreshes the entire workspace
     */
    public static void refreshWorkspace() {
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            root.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

}
