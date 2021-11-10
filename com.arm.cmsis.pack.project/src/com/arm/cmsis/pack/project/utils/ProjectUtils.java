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

package com.arm.cmsis.pack.project.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.configuration.RteConfiguration;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.CpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.parser.CpConfigParser;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectManager;
import com.arm.cmsis.pack.project.RteProjectNature;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 * Helper class with useful static methods
 */
public class ProjectUtils {

    /**
     * Returns ICpProject for given IProject if such exists
     * 
     * @param project IProject
     * @return
     */
    public static ICProject getCProject(IProject project) {
        if (project == null) {
            return null;
        }
        try {
            ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
            if (cProjects != null) {
                for (ICProject cProject : cProjects) {
                    if (project.equals(cProject.getProject())) {
                        return cProject;
                    }
                }
            }
        } catch (CModelException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return IProject of a project
     * 
     * @param projectName name of the project
     * @return instance of IProject
     */
    public static IProject getProject(String projectName) {
        if (projectName == null || projectName.isEmpty())
            return null;
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        return project;
    }

    /**
     * Return IProject of a project
     * 
     * @param projectName name of the project
     * @return instance of IProject
     */
    public static IProject getValidProject(String projectName) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (project == null || !project.exists()) {
            String msg = Messages.ProjectUtils_Project + projectName
                    + Messages.ProjectUtils_DoesNotExistsOrNotAccessible;
            Status status = new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, msg);
            throw new CoreException(status);
        }
        return project;
    }

    /**
     * Creates an empty file if it does not exist Folder is automatically created if
     * not existing.
     * 
     * @param project parent IProject
     * @param dstFile project file name, e.g. src/package1/file.c
     * @param monitor IProgressMonitor
     * @return created or existing IFile
     * @throws CoreException
     */
    public static IFile createFile(IProject project, String dstFile, IProgressMonitor monitor) throws CoreException {
        // create folder if not existing
        String folder = removeLastPathSegment(dstFile);
        createProjectFolder(project, folder, monitor);

        IFile file = project.getFile(dstFile);
        if (file.isLinked()) {
            file.delete(true, null);
            file = project.getFile(dstFile);
        }

        // Get existing file with same name but different case
        IResource existingFile = getExistingFile(dstFile, project, null);

        // Return existing file with same name but different case
        if (existingFile != null) {
            IPath dstFilePath = new Path(dstFile);
            dstFilePath = dstFilePath.removeLastSegments(1);
            dstFilePath = dstFilePath.append(existingFile.getName());
            file = project.getFile(dstFilePath);
        }

        if (file.exists()) {
            return file; // file already exists
        }

        // Create a new file, if it does not exists
        file.create(null, true, monitor);

        return file;
    }

    /**
     * Gets existing file with same name but different case
     * 
     * @param dstFile          project file name, e.g. src/package1/file.c
     * @param container(parent container e.g. project)
     * @return existing file with same name but different case
     * @throws CoreException
     */
    public static IResource getExistingFile(String dstFile, IContainer container, IResource existingFile)
            throws CoreException {
        IResource[] members = container.members();
        for (IResource member : members) {
            if (member instanceof IContainer)
                // Read content
                existingFile = getExistingFile(dstFile, (IContainer) member, existingFile);
            if (existingFile != null)
                return existingFile;
            if (member instanceof IFile) {
                IPath filePath = new Path(dstFile);
                IFile file = container.getFile(filePath);
                if (member.getName().equalsIgnoreCase(file.getName())) {
                    existingFile = member;
                    break;
                }
            }
        }
        return existingFile;
    }

    /**
     * Copy a local file to a local project folder. Destination file name can be
     * different than the source one. Folder is automatically created if not
     * existing.
     * 
     * @param projectName    name of the project
     * @param srcFile        source file name, e.g. C:/work/file.c
     * @param dstFile        project file name, e.g. src/package1/file.c
     * @param index          file index: >=0 for headers/sources of multi-instance
     *                       project components -1 for others
     * @param monitor        IProgressMonitor
     * @param forceOverwrite set to true when updating component file
     * @return 1 if the file has been copied, -1 if file already exists or 0 if
     *         there is an error
     * @throws CoreException
     */
    public static int copyFile(String projectName, String srcFile, String dstFile, int index, IProgressMonitor monitor,
            boolean forceOverwrite) throws Exception {

        IProject project = getValidProject(projectName);
        return copyFile(project, srcFile, dstFile, index, monitor, forceOverwrite);
    }

    /**
     * Copy a local file to a local project folder. Destination file name can be
     * different than the source one. Folder is automatically created if not
     * existing.
     * 
     * @param project        parent IProject
     * @param srcFile        source file name, e.g. C:/work/file.c
     * @param dstFile        destination file name, e.g. RTE/class/file.c
     * @param index          file index: >=0 for headers/sources of multi-instance
     *                       project components -1 for others
     * @param monitor        IProgressMonitor
     * @param forceOverwrite set to true when updating component file
     * @return 1 if the file has been copied, -1 if file already exists or 0 if
     *         there is an error
     * @throws CoreException
     */
    public static int copyFile(IProject project, String srcFile, String dstFile, int index, IProgressMonitor monitor,
            boolean forceOverwrite) throws CoreException {
        IFile file = createFile(project, dstFile, monitor);

        if (srcFile == null) {
            return 0; // only create resource, do not copy the content
        }

        IPath loc = file.getLocation();
        File f = loc.toFile();
        if (f != null && f.exists() && !forceOverwrite) {
            return -1; // destination file already exists
        }

        File inputfile = new File(srcFile);
        if (!inputfile.exists()) {
            String msg = Messages.ProjectUtils_TheFile + srcFile + Messages.ProjectUtils_DoesNotExistsOrNotAccessible;
            Status status = new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, msg);
            throw new CoreException(status);
        }

        try {
            if (index < 0) {
                FileInputStream fileStream = new FileInputStream(inputfile);
                if (file.exists()) {
                    file.delete(true, true, null);
                }
                file.create(fileStream, true, null);
                fileStream.close();
            } else {
                FileReader fr = new FileReader(inputfile);
                IPath p = file.getLocation();
                PrintWriter pw = new PrintWriter(p.toOSString());
                String instance = String.valueOf(index);
                BufferedReader br = new BufferedReader(fr);
                String s;
                while ((s = br.readLine()) != null) {
                    s = s.replaceAll(CmsisConstants.pINSTANCEp, instance);
                    pw.println(s);
                }
                fr.close();
                pw.close();
                file.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String msg = Messages.ProjectUtils_CannotCopyFile + srcFile + Messages.ProjectUtils_to + dstFile;
            Status status = new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, msg, e);
            throw new CoreException(status);
        }
        return 1;
    }

    /**
     * Create a link in a local (if folder exists) or virtual folder if folder has
     * to be created.
     * 
     * @param projectName name of project
     * @param srcFile     source file which is to be linked, may not be relative.
     * @param dstFile     destination file in a virtual project folder. Folder(s)
     *                    are created if not existing.
     * @param monitor     IProgressMonitor
     * @throws CoreException
     */
    public static void createLink(String projectName, String srcFile, String dstFile, IProgressMonitor monitor)
            throws CoreException {
        IProject project = getValidProject(projectName);
        createLink(project, srcFile, dstFile, monitor);
    }

    /**
     * Create a link in a local (if folder exists) or virtual folder if folder has
     * to be created.
     * 
     * @param srcFile source file which is to be linked, may not be relative.
     * @param project parent IProject
     * @param dstFile destination file in a virtual project folder. Folder(s) are
     *                created if not existing.
     * @param monitor IProgressMonitor
     * @throws CoreException
     */
    public static void createLink(IProject project, String srcFile, String dstFile, IProgressMonitor monitor)
            throws CoreException {

        // create folder if not existing
        String folder = removeLastPathSegment(dstFile); // retrieve only folder name
        createProjectFolder(project, folder, monitor);

        // create link
        IFile file = project.getFile(dstFile);

        IPath path = new Path(srcFile);
        file.createLink(path, IResource.REPLACE, monitor);
    }

    /**
     * Create folder (and sub-folders). Folder (and sub-folders) can be local or
     * virtual.
     * 
     * @param projectName   name of project
     * @param projectFolder project folder. E.g. src/package1/package2
     * @param monitor       IProgressMonitor
     * @throws CoreException
     */
    public static void createProjectFolder(String projectName, String projectFolder, IProgressMonitor monitor)
            throws CoreException {
        IProject project = getValidProject(projectName);
        createProjectFolder(project, projectFolder, monitor);
    }

    /**
     * Create folder (and sub-folders). Folder (and sub-folders) can be local or
     * virtual.
     * 
     * @param project       instance of IProject
     * @param projectFolder project folder. E.g. src/package1/package2
     * @param virtual       true if folder (and sub-folders) are virtual
     * @param monitor       IProgressMonitor
     * @throws CoreException
     */
    public static void createProjectFolder(IProject project, String projectFolder, IProgressMonitor monitor)
            throws CoreException {

        if (projectFolder.isEmpty()) {
            return;
        }

        IPath path = new Path(projectFolder);

        if (path.isAbsolute()) {
            String msg = Messages.ProjectUtils_ProjectfolderMustBeRelative;
            Status status = new Status(IStatus.ERROR, CpProjectPlugIn.PLUGIN_ID, msg);
            throw new CoreException(status);
        }

        // create non-existing folders
        for (int i = 1; i <= path.segmentCount(); i++) {
            IFolder subfolder = project.getFolder(path.uptoSegment(i));
            if (!subfolder.exists()) {
                subfolder.create(true, true, monitor);
            }
        }
    }

    /**
     * Remove the last segment of a path specification which can be a file or a
     * sub-folder.
     * 
     * @param filePath fully specified file/path name, e.g. src/package/file.c
     * @return path without last path segment, e.g. src/package
     */
    public static String removeLastPathSegment(String filePath) {
        IPath path = new Path(filePath);
        path = path.removeLastSegments(1); // remove file
        return path.toString();
    }

    /**
     * Set a configuration as active one.
     * 
     * @param projectName IProject owning configuration
     * @param configName  configuration name to set
     * @return true if active configuration has changed
     */
    public static boolean setDefaultConfiguration(String projectName, String configName) {
        IProject project = getProject(projectName);
        IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);

        if (buildInfo == null) {
            return false;
        }

        IConfiguration activeConfig = buildInfo.getDefaultConfiguration();
        if (activeConfig != null && configName.equals(activeConfig.getName())) {
            return true;
        }
        return buildInfo.setDefaultConfiguration(configName);
    }

    /**
     * Returns IConfiguration for given name
     * 
     * @param project owning IProject
     * @name configuration name to retrieve
     * @return IConfiguration
     */
    public static IConfiguration getConfiguration(IProject project, String name) {
        if (project == null) {
            return null;
        }
        try {
            IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
            IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
            for (IConfiguration c : configs) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Returns active configuration
     * 
     * @param project owning IProject
     * @return active IConfiguration
     */
    public static IConfiguration getDefaultConfiguration(IProject project) {
        if (project == null) {
            return null;
        }
        IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
        if (buildInfo != null) {
            return buildInfo.getDefaultConfiguration();
        }
        return null;
    }

    /**
     * Returns active configuration
     * 
     * @param projectName owning project name
     * @return active configuration
     */
    public static IConfiguration getDefaultConfiguration(String projectName) {
        IProject project = getProject(projectName);

        return getDefaultConfiguration(project);
    }

    /**
     * Returns active configuration name
     * 
     * @param projectName owning project name
     * @return active configuration name
     */
    public static String getDefaultConfigurationName(String projectName) {
        IConfiguration activeConfig = getDefaultConfiguration(projectName);
        if (activeConfig != null) {
            return activeConfig.getName();
        }
        return null;
    }

    /**
     * Removes all entries beginning with RTE or ${cmsis_pack_root} paths from
     * supplied list
     * 
     * @param paths list of paths/files to process
     * @return updated list
     */
    public static List<String> removeRtePathEntries(List<String> paths) {

        for (Iterator<String> iterator = paths.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            if (s.startsWith(CmsisConstants.PROJECT_RTE_PATH) || s.startsWith(CmsisConstants.CMSIS_PACK_ROOT_VAR)
                    || s.startsWith(CmsisConstants.CMSIS_RTE_VAR)) {
                iterator.remove();
            }
        }
        return paths;
    }

    /**
     * Returns IResource for the given object if any
     * 
     * @param obj an object that is derived from IResource or adapts IResource
     * @return IResource if can be resolved or null
     */
    public static IResource getResource(Object obj) {
        if (obj instanceof IResource) {
            return (IResource) obj;
        }
        if (obj instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) obj;
            Object o = a.getAdapter(IResource.class);
            if (o != null) {
                return (IResource) o;
            }
        }
        return null;
    }

    /**
     * Returns IReource object if it represents an RTE file or folder
     * 
     * @param obj an object that is derived from IResource or adapts IResource
     * @return IReource if it is an RTE resource or null
     */
    public static IResource getRteResource(Object obj) {
        IResource r = getResource(obj);
        if (r == null) {
            return null;
        }

        IProject project = r.getProject();
        if (!RteProjectNature.hasRteNature(project)) {
            return null;
        }
        if (r.getType() == IResource.PROJECT) {
            return r;
        }
        IPath path = r.getProjectRelativePath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (r.getType() == IResource.FILE && path.segmentCount() == 1) {
            if (CmsisConstants.RTECONFIG.equals(r.getFileExtension())) {
                return r;
            }
        }
        if (!path.segment(0).startsWith(CmsisConstants.RTE)) {
            return null;
        }

        return r;
    }

    /**
     * Returns IFile object if it represents an RTE file
     * 
     * @param obj an object that is derived from IResource or adapts IResource
     * @return IFile if it is an RTE file or null
     */
    public static IFile getRteFileResource(Object obj) {
        IResource r = getRteResource(obj);
        if (r instanceof IFile) {
            return (IFile) r;
        }
        return null;
    }

    public static ICpFileInfo getCpFileInfo(IResource resource) {
        if (resource == null || resource.getType() != IResource.FILE) {
            return null;
        }

        IProject project = resource.getProject();
        RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
        IRteProject rteProject = rteProjectManager.getRteProject(project);
        if (rteProject != null) {
            IPath path = resource.getProjectRelativePath();
            return rteProject.getProjectFileInfo(path.toString());
        }
        return null;
    }

    /**
     * Exclude the file with project relative path of "path" from build if bExclude
     * is set to true
     * 
     * @param project  the project
     * @param path     the resource's relative path to the project
     * @param bExclude set to true to exclude the resource from build
     * @throws CoreException
     */
    public static void setExcludeFromBuild(IProject project, String path, boolean bExclude) throws CoreException {
        IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
        if (buildInfo == null)
            return;
        IConfiguration activeConfig = buildInfo.getDefaultConfiguration();
        ICSourceEntry[] sourceEntries = activeConfig.getSourceEntries();
        sourceEntries = CDataUtil.setExcluded(new Path(path), false, bExclude, sourceEntries);
        activeConfig.setSourceEntries(sourceEntries);
    }

    /**
     * Check if a folder or file is excluded from build
     * 
     * @param project the project
     * @param path    the resource's relative path to the project
     * @return true if the folder or file is excluded from build
     */
    public static boolean isExcludedFromBuild(IProject project, String path) {
        IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
        if (buildInfo == null)
            return false;
        IConfiguration activeConfig = buildInfo.getDefaultConfiguration();
        if (activeConfig == null)
            return false;
        ICSourceEntry[] sourceEntries = activeConfig.getSourceEntries();
        return CDataUtil.isExcluded(new Path(path), sourceEntries);
    }

    /**
     * Returns a path equivalent to this path, but relative to the given base path
     * if possible.
     * 
     * @param path     path to make relative
     * @param basePath absolute base directory
     * @return A path relative to the base path, or this path if it could not be
     *         made relative to the given base
     */
    public static String makePathRelative(String path, String basePath) {
        if (path == null || basePath == null || basePath.isEmpty()) {
            return path;
        }
        IPath p = new Path(path);
        IPath base = new Path(basePath);
        p = p.makeRelativeTo(base);
        return p.toString();
    }

    /**
     * Creates Rte project without using defined methods in template.xml
     * 
     * @param project     project which will be contain Rte project
     * @param projectName project's name
     * @param deviceInfo  device's info
     * @param compiler    compiler's name
     * @param output      output's type e.g .exe
     * @param adapterId   adapter's ID
     * @return Rte project
     */
    public static IRteProject createRteProject(IProject project, String projectName, ICpDeviceInfo deviceInfo,
            String compiler, String output, String adapterId) {
        IRteProject rteProject = null;
        // Get toolchain adapter info to create Rte project
        RteToolChainAdapterInfo adapterInfo = createToolChainAdapter(adapterId);
        // Set Rte project's name to set Rte configuration to the Rte file
        String rteConfigName = projectName + CmsisConstants.DOT_RTECONFIG;
        // Create Rte project
        rteProject = createRteProject(project, adapterInfo);
        // Create Rte configuration
        IRteConfiguration rteConf = createRteConfiguration(compiler, output, deviceInfo);
        // Set Rte configuration
        rteProject.setRteConfiguration(rteConfigName, rteConf);

        return rteProject;
    }

    /**
     * Creates Rte file
     * 
     * @param project       project which will contain Rte file
     * @param rteConfigName Rte file's name
     * @param rteConf       configuration to be written into the Rte file
     * @param monitor       object to monitor the progress of Rte file creation
     * @return Rte file
     */
    public static IFile createRteFile(IProject project, String rteConfigName, IRteConfiguration rteConf,
            IProgressMonitor monitor) {
        IFile iFile = null;
        try {
            // Create RTE file
            iFile = ProjectUtils.createFile(project, rteConfigName, monitor);
            iFile.refreshLocal(IResource.DEPTH_ONE, null);
            project.refreshLocal(IResource.DEPTH_INFINITE, null);

            // Create a parser object to save RTE's configuration in XML format
            CpConfigParser confParser = new CpConfigParser();
            IPath location = iFile.getLocation();
            if (location != null) {
                File file = location.toFile();
                // Generates XML text out of RTE's configuration and saves it to an XML file
                confParser.writeToXmlFile(rteConf.getConfigurationInfo(), file.getAbsolutePath());
            }
        } catch (CoreException e) {
            e.printStackTrace();

        }
        return iFile;
    }

    /**
     * Opens rteconfig file for supplied project in RTE editor
     * 
     * @param project IProject
     * @return true if file exists and can be opened
     */
    public static boolean openRteConfigFile(IProject project) {
        if (project == null)
            return false;
        String rteConf = project.getName() + '.' + CmsisConstants.RTECONFIG;
        return openProjectRelativeFile(project, rteConf);
    }

    /**
     * Opens a file relative to the supplied project
     * 
     * @param project  parent IProject
     * @param fileName filename relative to parent project
     * @return true if file exists and queried to open
     */
    public static boolean openProjectRelativeFile(IProject project, String fileName) {
        if (project == null || fileName == null)
            return false;
        if (!PlatformUI.isWorkbenchRunning()) {
            return false; // nothing to do in headless mode
        }
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        Optional<String> perspectiveId = null;
        if (envProvider != null) {
            perspectiveId = envProvider.getCopyExamplePerspectiveSwitchId();
        }
        IResource r = project.findMember(fileName);
        if (r != null && r.exists() && r.getType() == IResource.FILE) {
            openEditorAsync(project.getFile(fileName), perspectiveId);
            return true;
        } else {
            switchToPerspectiveAsync(perspectiveId); // simply switch to an example perspective
        }
        return false;
    }

    /**
     * Opens file in an editor asynchronously
     * 
     * @param iFile file to be opened in dedicated editor
     */
    public static void openEditorAsync(IFile iFile) {
        openEditorAsync(iFile, null);
    }

    /**
     * Opens file in an editor asynchronously
     * 
     * @param iFile file to be opened in dedicated editor
     */
    public static void openEditorAsync(IFile iFile, Optional<String> perspectiveId) {
        if (iFile == null) {
            return; // nothing to open
        }

        if (!PlatformUI.isWorkbenchRunning()) {
            return; // nothing to do in headless mode
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbench wb = PlatformUI.getWorkbench();
                if (wb == null)
                    return;
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                IWorkbenchPage page = window.getActivePage();
                if (page == null)
                    return;
                switchToPerspective(wb, page, perspectiveId);
                try {
                    IDE.openEditor(page, iFile);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Switches active workbench page to a perspective specified by
     * {@link ICpEnvironmentProvider}
     */
    public static void switchToExamplePerspective() {
        ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
        if (envProvider != null) {
            switchToPerspectiveAsync(envProvider.getCopyExamplePerspectiveSwitchId());
        }
    }

    /**
     * Switches active workbench page to a specified perspective
     * 
     * @param perspectiveId perspective Id to switch to
     */
    public static void switchToPerspectiveAsync(Optional<String> perspectiveId) {
        if (perspectiveId == null || !perspectiveId.isPresent())
            return;
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbench wb = PlatformUI.getWorkbench();
                if (wb == null)
                    return;
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                IWorkbenchPage page = window.getActivePage();
                if (page == null)
                    return;
                switchToPerspective(wb, page, perspectiveId);
            }
        });
    }

    /**
     * Switches to specified perspective
     * 
     * @param wb            IWorkbench
     * @param page          IWorkbenchPage
     * @param perspectiveId perspective Id to switch to
     */
    public static void switchToPerspective(IWorkbench wb, IWorkbenchPage page, Optional<String> perspectiveId) {
        if (perspectiveId == null || !perspectiveId.isPresent())
            return;

        if (wb == null)
            return;

        if (page == null)
            return;

        IPerspectiveDescriptor persDescription = wb.getPerspectiveRegistry().findPerspectiveWithId(perspectiveId.get());
        if (persDescription == null)
            return;
        page.setPerspective(persDescription);
    }

    /**
     * Creates toolchain adapter
     * 
     * @param adapterId required adapterId to create a toolchain adapter
     * @return toolchain adapter info for given adapterId
     */
    public static RteToolChainAdapterInfo createToolChainAdapter(String adapterId) {
        // Create an adapterFactory object to get toolchain adapter info for given
        // adapterId
        RteToolChainAdapterFactory adapterFactory = RteToolChainAdapterFactory.getInstance();
        return adapterFactory.getAdapterInfo(adapterId);
    }

    /**
     * Creates Rte's configuration
     * 
     * @param compiler   compiler family
     * @param output     output type: " exe" or "lib"
     * @param deviceInfo device's info used in the configuration
     * @return Rte's configuration
     */
    public static IRteConfiguration createRteConfiguration(String compiler, String output, ICpDeviceInfo deviceInfo) {
        // Create toolchain info for given compiler and output type
        ICpItem toolchainInfo = createToolChainInfo(compiler, output);
        // Create configuration info for given device's info and toolchain's info
        ICpConfigurationInfo cpInfo = new CpConfigurationInfo(deviceInfo, toolchainInfo, true);
        // Create IRteConfiguration object to set configuration info
        IRteConfiguration rteConf = new RteConfiguration();
        // Sets ICpConfigurationInfo object, initializes model and collects settings
        rteConf.setConfigurationInfo(cpInfo);
        return rteConf;
    }

    /**
     * Creates Rte project
     * 
     * @param project     project which will contain a Rte project
     * @param adapterInfo Rte's toolchain adapter info
     * @return Rte project
     */
    public static IRteProject createRteProject(IProject project, RteToolChainAdapterInfo adapterInfo) {
        // Create a RteProjectManager to manage RTE project and its association with
        // IProject
        RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
        // Create a RTE project to be associated with the IProject
        IRteProject rteProject = rteProjectManager.createRteProject(project);
        // Sets RteToolChainAdapterInfo to be used by RTE project
        rteProject.setToolChainAdapterInfo(adapterInfo);
        return rteProject;
    }

    /**
     * Creates toolchain info for given compiler and output type
     * 
     * @param Tcompiler compiler family
     * @param Toutput   output type : " exe" or "lib"
     * @return toolchain info as ICpItem
     */
    public static ICpItem createToolChainInfo(String Tcompiler, String Toutput) {
        ICpItem toolchainInfo = new CpItem(null, CmsisConstants.TOOLCHAIN_TAG);
        toolchainInfo.attributes().setAttribute(CmsisConstants.TCOMPILER, Tcompiler);
        toolchainInfo.attributes().setAttribute(CmsisConstants.TOUTPUT, Toutput);
        return toolchainInfo;
    }

    /**
     * Gets toolchains corresponding to given ID prefix (usually reflects family)
     * 
     * @param prefix toolchain ID prefix
     * @return collection of IToolChain object satisfying thr prefix
     */
    public static Collection<IToolChain> getToolChainsByIdPrefix(String prefix) {
        List<IToolChain> toolchains = new LinkedList<>();
        IToolChain[] toolchainList = ManagedBuildManager.getRealToolChains();
        for (IToolChain t : toolchainList) {
            if (t == null || !t.isSupported())
                continue;
            // check if toolchain's id or one of its super classes start with the prefix
            for (IToolChain tc = t; tc != null; tc = tc.getSuperClass()) {
                String id = tc.getId();
                if (id.startsWith(prefix)) {
                    toolchains.add(t);
                }
            }
        }
        return toolchains;
    }

    /**
     * Get toolchain by its name
     * 
     * @param toolchainName localized toolchain name
     * @return IToolChain if found, null otherwise
     */
    public static IToolChain getToolChainByName(String toolchainName) {
        IToolChain[] toolchainList = ManagedBuildManager.getRealToolChains();
        for (IToolChain t : toolchainList) {
            if (t.getName().equals(toolchainName)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Get toolchain by name
     * 
     * @param toolchainName
     * @return IToolChain
     */
    public static IToolChain getToolChainById(String toolchainId) {
        IToolChain[] toolchainList = ManagedBuildManager.getRealToolChains();
        for (IToolChain t : toolchainList) {
            if (t.getId().equals(toolchainId)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets toolchains starting with given ID
     * 
     * @param id toolchain ID
     * @return Map of toolchanins (ID to name)
     */
    public static Map<String, String> getToolChains(String id) {
        Map<String, String> mToolchains = new TreeMap<>(new AlnumComparator());
        IToolChain[] toolchainList = ManagedBuildManager.getRealToolChains();
        for (IToolChain t : toolchainList) {
            if (t.getId().startsWith(id)) {
                mToolchains.put(t.getId(), t.getName());
            }
        }
        return mToolchains;
    }

    public static String getWorkspacePath() {
        String wsPath = CmsisConstants.EMPTY_STRING;

        // Get object which represents the workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Get location of workspace (java.io.File)
        File workspaceDirectory = workspace.getRoot().getLocation().toFile();

        wsPath = workspaceDirectory.toString();

        return wsPath;
    }

    /**
     * Loads project description
     * 
     * @param path projects path as IPath
     * @return IProjectDescription
     */
    public static IProjectDescription getProjectDescription(IPath path) {
        try {
            return ResourcesPlugin.getWorkspace().loadProjectDescription(path);
        } catch (CoreException e) {
            return null;
        }
    }

}
