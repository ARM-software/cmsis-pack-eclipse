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

package com.arm.cmsis.zone.project;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.error.ICmsisConsole;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.parser.CpXmlParser;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.project.CmsisProjectNature;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.CpResourceZone;
import com.arm.cmsis.zone.data.CpRootZone;
import com.arm.cmsis.zone.data.CpZoneItem;
import com.arm.cmsis.zone.data.ICpResourceZone;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.parser.CpZoneParser;
import com.arm.cmsis.zone.ui.CpZonePluginUI;
import com.arm.cmsis.zone.ui.Messages;

/**
 * This class manages data and methods to perform creation of CMSIS-Zone
 * projects
 */
public class CmsisZoneProjectCreator {
    private String fRzoneFileName = CmsisConstants.EMPTY_STRING; // .rzone file's name
    private IProject fProject = null;
    private IPath fProjectLocation = null;
    private ICpDeviceInfo fDeviceInfo = null;
    private IProjectDescription fDescription = null;
    private boolean fbDeviceSelected = true;

    /**
     * Default constructor
     */
    public CmsisZoneProjectCreator() {

    }

    /**
     * Creates a CMSIS-Zone project in workspace
     *
     * @param monitor progress monitor
     * @return IFile for created file
     * @throws CoreException
     */
    public IFile createProject(IProgressMonitor monitor) throws CoreException {
        // Get workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Creates and returns a new project description for a project with the given
        // name
        fDescription = workspace.newProjectDescription(fProject.getName());
        // Sets the local file system location for the described project
        if ((fProjectLocation != null) && (!fProjectLocation.equals(Platform.getLocation()))) {
            fDescription.setLocation(fProjectLocation);
        }

        // Create project
        fProject.create(fDescription, monitor);
        fProject.open(monitor);
        CmsisProjectNature.addNature(fProject, CmsisZoneProjectNature.CMSIS_ZONE_MASTER_NATURE_ID, monitor);

        String projectName = fProject.getName();
        // Create .rzone file's name
        String rFileName = projectName + CmsisConstants.DOT_RZONE;

        if (fDeviceInfo != null) {
            ICpRootZone rzone = new CpResourceZone(null, fDeviceInfo, CpZonePluginUI.getToolId());
            saveFile(rzone, rFileName, fProject, monitor);
        } else {
            // Copy and rename the file
            String rzoneFileSource = getRzoneFile();
            if (!rzoneFileSource.isEmpty()) {
                // Copy file to the project folder created in eclipse workspace.
                ProjectUtils.copyFile(fProject, rzoneFileSource, rFileName, -1, monitor, true);
            }
        }

        // Create .azone file
        String aFileName = projectName + CmsisConstants.DOT_AZONE;
        ICpRootZone azone = new CpRootZone(null, CmsisConstants.AZONE);
        ICpItem rZoneRef = new CpZoneItem(azone, CmsisConstants.RZONE);
        rZoneRef.setAttribute(CmsisConstants.NAME, rFileName);
        azone.addChild(rZoneRef);
        IFile aZoneFile = saveFile(azone, aFileName, fProject, monitor);

        // create ftl folder
        ProjectUtils.createProjectFolder(fProject, CmsisConstants.FTL, monitor);

        fProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        return aZoneFile;

    }

    /**
     * Saves a zone file
     *
     * @param root     root ICpItem to save
     * @param fileName absolute filename to save to
     * @param project  IProject
     * @param monitor  prIProgressMonitoro
     * @return IFile for saved file
     * @throws CoreException
     */
    public static IFile saveFile(ICpItem root, String fileName, IProject project, IProgressMonitor monitor)
            throws CoreException {
        IFile iFile = ProjectUtils.createFile(project, fileName, monitor);
        CpZoneParser confParser = new CpZoneParser();
        IPath location = iFile.getLocation();
        if (location != null) {
            File file = location.toFile();
            confParser.writeToXmlFile(root, file.getAbsolutePath());
        }
        return iFile;
    }

    /**
     * Manages required validations to allow the copy of a rzone file into a
     * CMSIS-zone project
     *
     * @return String with validation message (if exists) otherwise returns an empty
     *         string
     */
    public String validate() {
        String result = CmsisConstants.EMPTY_STRING;

        // Validate source file
        String validateSourceFile = validateRzoneFile();
        if (!validateSourceFile.isEmpty())
            return validateSourceFile;

        return result;
    }

    /**
     * Creates a new azone file if it does not exists and opens it in editor
     *
     * @param rzoneFileName rzone file for which to create an azone file
     * @param azoneFileName azone filename
     * @return
     */
    public static boolean createAzoneFile(String rzoneFileName, String azoneFileName) {
        if (rzoneFileName == null) {
            return false;
        }
        boolean result = false;

        // Validate if input is a .rzone file
        String ext = Utils.extractFileExtension(rzoneFileName);

        // Build azone file path
        IPath rzoneFilePath = new Path(rzoneFileName);
        IPath azoneFilePath = rzoneFilePath.removeLastSegments(1);
        String aFileName = azoneFilePath.toPortableString() + CmsisConstants.SLASH + azoneFileName;

        if (ext.equals(CmsisConstants.RZONE)) {
            // Check if file exists
            File file = new File(aFileName);
            if (!file.exists()) { // Create azone file
                try {
                    IProgressMonitor monitor = new NullProgressMonitor();
                    ICpZone zone = null;
                    saveAZoneFile(aFileName, zone, rzoneFileName, monitor);
                    CpPlugInUI.refreshProject(azoneFileName);
                    result = true;
                    openFileInWorkspace(aFileName);

                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * Save azone file
     *
     * @param azoneFileName azone filename
     * @param zone          ICpZone to save
     * @param rzoneFileName rzone filename
     * @param monitor       IProgressMonitor
     * @return true if file has been created or changed
     * @throws CoreException
     */
    public static boolean saveAZoneFile(String azoneFileName, ICpZone zone, String rzoneFileName,
            IProgressMonitor monitor) throws CoreException {
        // create zone
        ICpXmlParser parser = new CpZoneParser();

        File file = new File(azoneFileName);
        ICpItem root = parser.parseFile(file.getAbsolutePath());
        if (root instanceof ICpRootZone) { // checks if the file with right contenet already exists
            return false;
        }
        ICpRootZone azone = new CpRootZone(null, CmsisConstants.AZONE);
        ICpItem rZoneRef = new CpZoneItem(azone, CmsisConstants.RZONE);

        rZoneRef.setAttribute(CmsisConstants.NAME, Utils.extractFileName(rzoneFileName));
        azone.addChild(rZoneRef);

        // Saves XML string to file
        String xml = parser.writeToXmlString(azone);
        CpXmlParser.saveXmlToFile(xml, azoneFileName);
        CpPlugInUI.refreshFile(azoneFileName);
        return true;
    }

    /**
     * @param absFileName rzone filename
     * @param zone        IcpZone to create/write rzone file for
     * @param monitor     IProgressMonitor
     * @return true if file has been created or changed
     * @throws CoreException
     */
    public static boolean saveRZoneFile(String absFileName, ICpZone zone, IProgressMonitor monitor)
            throws CoreException {
        // create zone
        ICpXmlParser parser = new CpZoneParser();

        File file = new File(absFileName);
        ICpItem root = parser.parseFile(file.getAbsolutePath());
        String curXml = CmsisConstants.EMPTY_STRING;

        ICpResourceZone rzone = null;
        if (root instanceof ICpResourceZone) { // also checks for null
            rzone = (ICpResourceZone) root;
            curXml = parser.writeToXmlString(root);
        } else {
            rzone = new CpResourceZone(null, CmsisConstants.RZONE);
        }
        String toolId = CpZonePluginUI.getToolId();
        if (!rzone.updateResources(zone, toolId)) {
            return false; // no change
        }

        String xml = parser.writeToXmlString(rzone);
        if (curXml.equals(xml))
            return false;
        CpXmlParser.saveXmlToFile(xml, absFileName);
        return true;
    }

    /**
     * Create sub-zone files for given root zone
     *
     * @param rootZone ICpRootZone
     * @param console  ICmsisConsole or null
     * @param monitor  IProgressMonitor
     * @throws CoreException
     */
    public static void createZoneFiles(ICpRootZone rootZone, ICmsisConsole console, IProgressMonitor monitor)
            throws CoreException {
        // Disable the generation of sub-zone files (*.rzone) in Execution Zone mode

        if (!rootZone.isZoneModeProject()) {
            return;
        }

        // Save individual project zone files
        Collection<ICpZone> zones = rootZone.getZones();
        if (zones == null || zones.isEmpty())
            return;
        for (ICpZone zone : zones) {
            CmsisZoneProjectCreator.createZoneFiles(zone, console, monitor);
        }
        CpPlugInUI.refreshProject(rootZone.getRootFileName());
    }

    /**
     * Create rzone and azone file for supplied zone
     *
     * @param zone    ICpZone
     * @param console ICmsisConsole or null
     * @param monitor IProgressMonitor
     * @throws CoreException
     */
    public static void createZoneFiles(ICpZone zone, ICmsisConsole console, IProgressMonitor monitor)
            throws CoreException {
        if (zone == null)
            return;

        // Get zone's name
        String folderName = zone.getName();

        // Convert absFileName to Path
        IPath filePath = new Path(zone.getRootDir(false));

        // Build folder's path
        folderName = filePath + CmsisConstants.SLASH + folderName;

        // Create folder
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Create rzone file
        String rFileName = zone.getName() + CmsisConstants.DOT_RZONE;
        String rFile = folderName + CmsisConstants.SLASH + rFileName;
        boolean modified = saveRZoneFile(rFile, zone, monitor);
        if (modified && console != null) {
            console.output(Messages.CmsisZoneProjectCreator_GenSubZoneResourceFile + rFile);
        }

        // Create azone file
        String aFile = folderName + CmsisConstants.SLASH + zone.getName() + CmsisConstants.DOT_AZONE;
        modified = saveAZoneFile(aFile, zone, rFileName, monitor);
        if (modified && console != null) {
            console.output(Messages.CmsisZoneProjectCreator_GenSubZoneAssignFile + aFile);
        }

    }

    /**
     * Opens file in editor
     *
     * @param absFileName absolute filename
     */
    public static void openFileInWorkspace(String absFileName) {
        if (!PlatformUI.isWorkbenchRunning()) {
            return; // nothing to do in headless mode
        }
        IFile aZoneFile = CpPlugInUI.getFileForLocation(absFileName);
        ProjectUtils.openEditorAsync(aZoneFile);
    }

    /**
     * Validates rzone file
     *
     * @return String with an error message or empty string
     */
    public String validateRzoneFile() {
        // Validate rzone file's name
        if (getRzoneFile().isEmpty())
            return Messages.CmsisZoneManager_ValidateRzoneFile;

        // Validate if rzone file exists
        File f = new File(getRzoneFile());
        if (!f.exists())
            return Messages.CmsisZoneManager_ValidateRzoneFileExistence;

        return CmsisConstants.EMPTY_STRING;
    }

    /**
     * Creates a new exception
     *
     * @param message error message
     * @return exception
     */
    public static CoreException createErrorException(String message) {
        return new CoreException(new Status(IStatus.ERROR, getPlugInId(), message));
    }

    /**
     * Returns plug-in ID implementing the importer
     *
     * @return plug-in ID
     */
    public static String getPlugInId() {
        return CpPlugInUI.PLUGIN_ID;
    }

    /*** getters ***/

    public String getRzoneFile() {
        return fRzoneFileName;
    }

    public IProject getProject() {
        return fProject;
    }

    public IPath getProjectLocation() {
        return fProjectLocation;
    }

    public ICpDeviceInfo getDeviceInfo() {
        return fDeviceInfo;
    }

    public boolean isDeviceSelected() {
        return fbDeviceSelected;
    }

    /*** setters ***/

    public void setRzoneFile(String fileName) {
        fRzoneFileName = fileName;
    }

    public void setProject(IProject project) {
        fProject = project;
    }

    public void setProjectLocation(IPath projectLocation) {
        fProjectLocation = projectLocation;
    }

    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        fDeviceInfo = deviceInfo;
    }

    public void setDeviceSelected(boolean bDeviceSelected) {
        fbDeviceSelected = bDeviceSelected;
    }

}
