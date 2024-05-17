/*******************************************************************************
* Copyright (c) 2024 ARM Ltd. and others
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

package com.arm.cmsis.pack.project.csolution;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPack;
import com.arm.cmsis.pack.data.CpRootItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.CpComponentInfo;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.CpPackInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.parser.yml.CpYmlParser;
import com.arm.cmsis.pack.project.CpVariableResolver;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.importer.IRteProjectImporter;
import com.arm.cmsis.pack.project.importer.RteProjectImporter;
import com.arm.cmsis.pack.project.utils.CDTUtils;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.utils.Utils;

/**
 *
 */
public class CsolutionProjectImporter extends RteProjectImporter implements IRteProjectImporter {

    private CpYmlParser parser;
    private HashMap<ICpItem, ArrayList<String>> cbuildFileRootToPacks;

    /**
     * Imports Csolution Project i.e. *.cbuild-idx.yml file
     */
    public CsolutionProjectImporter() {
        super();
        parser = new CpYmlParser();
        cbuildFileRootToPacks = new HashMap<>();
    }

    public void readAndSaveFiles() throws OperationCanceledException {
        validateProjectFile();

        ICpItem fileRoot = parser.parseFile(getSourceProjectFile());
        Collection<? extends ICpItem> cbuilds;
        boolean isCbuildIdxFile = fileRoot.getFirstChild(CmsisConstants.BUILD_IDX_TAG) != null;
        if (!isCbuildIdxFile) {
            // cbuild-set file
            cbuilds = fileRoot.getFirstChild(CmsisConstants.CBUILD_SET_TAG).getGrandChildren(CmsisConstants.CONTEXTS);
        } else {
            cbuilds = fileRoot.getFirstChild(CmsisConstants.BUILD_IDX_TAG).getGrandChildren(CmsisConstants.CBUILDS);
        }

        String rootPath = fileRoot.getRootDir(true);
        cbuildFileRootToPacks.clear();
        for (ICpItem cbuild : cbuilds) {
            String fileName;
            if (isCbuildIdxFile) {
                fileName = cbuild.getAttribute(CmsisConstants.CBUILD);
            } else {
                fileName = cbuild.getAttribute(CmsisConstants.CONTEXT) + CmsisConstants.EXT_CBUILD_YML;
            }

            String cbuildFilePath = rootPath + fileName;
            ICpItem cbuildFileRoot = parser.parseFile(cbuildFilePath);
            if (cbuildFileRoot == null) {
                cbuildFileRoot = new CpRootItem(CmsisConstants.EMPTY_STRING, fileName);
            }
            cbuildFileRoot.setAttribute(CmsisConstants.NAME, Utils.getPrefix(fileName, CmsisConstants.EXT_CBUILD_YML));
            cbuildFileRootToPacks.put(cbuildFileRoot, new ArrayList<>());
        }
        validate();
    }

    @Override
    protected void doImportProject(IProgressMonitor monitor) throws OperationCanceledException, CoreException {
        validateProjectFile();

        Collection<ICpItem> selectedCbuildFileRoots = cbuildFileRootToPacks.keySet().stream()
                .filter(item -> item.getAttributeAsBoolean(CmsisConstants.IS_SELECTED, false))
                .collect(Collectors.toList());

        for (ICpItem cbuildFileRoot : selectedCbuildFileRoots) {
            try {
                doImportProject(cbuildFileRoot, monitor);
            } catch (OperationCanceledException | CoreException e) {
                e.printStackTrace();
                deleteProject();
                String errorMsg = e.getMessage();
                getCmsisConsole().outputError(errorMsg);
                throw e;
            }
        }
    }

    /**
     * Parse *.cbuild.yml file, download required packs and create the project
     * appropriately
     *
     * @param cbuild  ICpItem node containing the
     * @param monitor
     * @throws OperationCanceledException
     * @throws CoreException
     */
    protected void doImportProject(ICpItem cbuildFileRoot, IProgressMonitor monitor)
            throws OperationCanceledException, CoreException {
        ICpItem buildItem = cbuildFileRoot.getFirstChild(CmsisConstants.BUILD_TAG);
        progress(1, monitor);

        String contextName = buildItem.getAttribute(CmsisConstants.CONTEXT);
        fProject = CDTUtils.createCProject(contextName, monitor);
        fCreatedProjects.add(contextName);
        progress(1, monitor);

        // Search devices and get DeviceInfo
        IRteDeviceItem device = searchDevices(buildItem);
        String deviceAttributeValue = buildItem.getAttribute(CmsisConstants.DEVICE_TAG);

        ICpDeviceInfo deviceInfo = new CpDeviceInfo(null);
        if (device != null) {
            deviceInfo.setDevice(device.getDevice(),
                    Utils.stripPrefix(deviceAttributeValue, CmsisConstants.DOUBLE_COLON));
            // Defaults
            if (deviceInfo.getAttribute(CmsisConstants.DENDIAN).equals(CmsisConstants.CONFIGENDIAN)) {
                deviceInfo.setAttribute(CmsisConstants.DENDIAN, CmsisConstants.LITTLENDIAN);
            }
            if (deviceInfo.getAttribute(CmsisConstants.DSECURE).isEmpty()) {
                deviceInfo.setAttribute(CmsisConstants.DSECURE, CmsisConstants.TZ_DISABLED);
            }
        } else {
            throw createErrorException(Messages.CsolutionProjectImport_ErrorDeviceNotFound);
        }

        createRteProject(cbuildFileRoot, deviceInfo, monitor);
        progress(1, monitor);

        createComponentLinks(buildItem, monitor);
        createOutputLinks(buildItem, monitor);
        createGroupLinks(buildItem.getFirstChild(CmsisConstants.GROUPS_TAG), CmsisConstants.EMPTY_STRING, monitor);
        createFileLinks(buildItem.getFirstChild(CmsisConstants.CONSTRUCTED_FILES), CmsisConstants.CONSTRUCTED_FILES,
                monitor);
        progress(1, monitor);
    }

    /**
     * Searches through stored devices and returns device item
     *
     * @param buildItem item that represents the child with tag "build" inside the
     *                  *.cbuild.yml file
     * @return found item
     */
    private IRteDeviceItem searchDevices(ICpItem buildItem) {
        IRteDeviceItem device = null;
        String deviceAttributeValue = buildItem.getAttribute(CmsisConstants.DEVICE_TAG);
        IRteDeviceItem devices = CpPlugIn.getPackManager().getDevices();

        if (devices != null) {
            String vendor = Utils.getPrefix(deviceAttributeValue, CmsisConstants.DOUBLE_COLON);
            if (vendor != null && !vendor.isEmpty()) {
                device = devices.findItem(Utils.getSuffix(deviceAttributeValue, CmsisConstants.DOUBLE_COLON), vendor,
                        true);
            } else {
                device = devices.findItem(deviceAttributeValue, null, true);
            }
        }
        return device;
    }

    /**
     * Create virtual links to files in the components
     *
     * @param buildItem "build" tag node of the root file item
     * @param monitor   progress monitor
     */
    private void createComponentLinks(ICpItem buildItem, IProgressMonitor monitor) throws CoreException {
        for (ICpItem component : buildItem.getGrandChildren(CmsisConstants.COMPONENTS_TAG)) {
            String directoryName = CmsisConstants.RTE + File.separatorChar
                    + component.getAttribute(CmsisConstants.COMPONENT_TAG).replaceAll(":+", //$NON-NLS-1$
                            CmsisConstants.DOT); // ;
            for (ICpItem file : component.getGrandChildren(CmsisConstants.FILES_TAG)) {
                if (CmsisConstants.TEMPLATE.equals(file.getAttribute(CmsisConstants.ATTR))
                        || CmsisConstants.INCLUDE.equals(file.getAttribute(CmsisConstants.CATEGORY))) {
                    continue; // skip
                }
                File path = new File(
                        CpVariableResolver.expandCmsisRootVariable(file.getAttribute(CmsisConstants.FILE_TAG)));
                String virtualDestinationPath = directoryName + File.separatorChar
                        + Utils.extractFileName(path.toString());

                String existingLinkPath;
                if (!path.isAbsolute()) {
                    existingLinkPath = buildItem.getParent().getAbsolutePath(path.toString());
                } else {
                    existingLinkPath = path.toString();
                }
                ProjectUtils.createLink(fProject, existingLinkPath, virtualDestinationPath, false, monitor);
            }
        }
    }

    /**
     * Create virtual links to output directory
     *
     * @param buildItem "build" tag node of the root file item
     * @param monitor   progress monitor
     */
    private void createOutputLinks(ICpItem buildItem, IProgressMonitor monitor) throws CoreException {
        ICpItem outputDirs = buildItem.getFirstChild(CmsisConstants.OUTPUT_DIRS_TAG);
        ICpItem output = buildItem.getFirstChild(CmsisConstants.OUTPUT_TAG);
        if (outputDirs == null || output == null) {
            return;
        }
        String outputDir = outputDirs.getAttribute(CmsisConstants.OUTDIR_TAG);
        String outputFile = null;
        for (ICpItem child : output.getChildren()) {
            if (CmsisConstants.ELF.equals(child.getAttribute(CmsisConstants.TYPE))) {
                outputFile = child.getAttribute(CmsisConstants.FILE_TAG);
                break;
            }
        }
        if (outputFile == null) {
            return;
        }
        String srcFile = buildItem.getAbsolutePath(outputDir) + File.separatorChar + outputFile;
        String dstFile = outputDir + File.separatorChar + outputFile;
        ProjectUtils.createLink(fProject, srcFile, dstFile, false, monitor);
        // create additional link to the executable at the project root level
        IFile file = fProject.getFile(outputFile);
        IPath path = new Path(srcFile);
        file.createLink(path, IResource.REPLACE, monitor);
    }

    /**
     * Create project hierarchy based on "groups" tag via virtual links
     *
     * @param groupsItem      group container item
     * @param parentGroupName parent hierarchical group name
     * @param monitor         progress monitor
     */
    private void createGroupLinks(ICpItem groupsItem, String parentGroupName, IProgressMonitor monitor)
            throws CoreException {
        if (groupsItem == null) {
            return;
        }

        for (ICpItem group : groupsItem.getChildren()) {
            String groupName = parentGroupName;
            if (!groupName.isEmpty()) {
                groupName += File.separatorChar;
            }
            groupName += Utils.wildCardsToX(group.getAttribute(CmsisConstants.GROUP));

            // add files
            createFileLinks(group.getFirstChild(CmsisConstants.FILES_TAG), groupName, monitor);
            // add subgroups
            for (ICpItem subGroup : group.getGrandChildren(CmsisConstants.GROUPS_TAG)) {
                createGroupLinks(subGroup, groupName, monitor);
            }
        }
    }

    /**
     * Create project hierarchy based on "groups" tag via virtual links
     *
     * @param container file container : "files" or "constructed-files"
     * @param group     name
     * @param monitor   progress monitor
     */
    private void createFileLinks(ICpItem container, String groupName, IProgressMonitor monitor) throws CoreException {
        if (container == null) {
            return;
        }

        // add files
        for (ICpItem file : container.getChildren()) {
            String filePath = file.getAttribute(CmsisConstants.FILE_TAG);
            String fileName = Utils.extractFileName(filePath);
            String absPath = file.getAbsolutePath(filePath);
            ProjectUtils.createLink(fProject, absPath, groupName + File.separatorChar + fileName, false, monitor);
        }
    }

    /**
     * Creates RteProject, .rteconfig and saves .rteconfig persistently
     *
     * @param cbuildFileRoot
     * @param deviceInfo
     * @param monitor
     * @throws CoreException
     */

    private void createRteProject(ICpItem cbuildFileRoot, ICpDeviceInfo deviceInfo, IProgressMonitor monitor)
            throws CoreException {
        ICpItem buildItem = cbuildFileRoot.getFirstChild(CmsisConstants.BUILD_TAG);

        String compiler = decodeCompiler(buildItem.getAttribute(CmsisConstants.COMPILER));
        IRteProject rteProject = ProjectUtils.createRteProject(getProject(),
                buildItem.getAttribute(CmsisConstants.CONTEXT), deviceInfo, compiler, CmsisConstants.EXE,
                "com.arm.cmsis.pack.GenericToolChainAdapter"); //$NON-NLS-1$
        if (rteProject == null) {
            throw createErrorException("Error creating RTE Project"); //$NON-NLS-1$
        }

        updateConfigurationInfo(cbuildFileRoot, rteProject.getRteConfiguration().getConfigurationInfo());

        IFile createdFile = ProjectUtils.createRteFile(getProject(), rteProject.getRteConfigurationName(),
                rteProject.getRteConfiguration(), monitor);
        createdFile.refreshLocal(IResource.DEPTH_ONE, null);

        // Make .rteconfig read-only
        ResourceAttributes readOnlyAttributes = new ResourceAttributes();
        readOnlyAttributes.setReadOnly(true);
        createdFile.setResourceAttributes(readOnlyAttributes);

        rteProject.save();
    }

    private void updateConfigurationInfo(ICpItem cbuildFileRoot, ICpConfigurationInfo configurationInfo) {
        ICpPackFilterInfo packFilterInfo = configurationInfo.getPackFilterInfo();
        ICpPackCollection installedPacks = CpPlugIn.getPackManager().getInstalledPacks();

        // Add packs to configuration info
        for (String packId : cbuildFileRootToPacks.get(cbuildFileRoot)) {
            IAttributes packAttributes = CpPack.attributesFromId(packId);

            ICpPackInfo packInfo = new CpPackInfo(null, packAttributes);
            packInfo.setPack(installedPacks.getPack(packId));
            packFilterInfo.addChild(packInfo);
            if (packInfo.getVersion() == null) {
                packInfo.setVersionMatchMode(EVersionMatchMode.LATEST);
            } else {
                packInfo.setVersionMatchMode(EVersionMatchMode.FIXED);
                if (packFilterInfo.isUseAllLatestPacks()) {
                    packFilterInfo.setUseAllLatestPacks(false);
                }
            }
        }
        String cbuildYmlAttributeKey = CmsisConstants.CBUILD + CmsisConstants.UNDERSCORE + CmsisConstants.YML;
        configurationInfo.setAttribute(cbuildYmlAttributeKey, cbuildFileRoot.getRootFileName());

        // Add components to configuration info
        ICpItem componentsItem = configurationInfo.getComponentsItem();
        ICpItem buildItem = cbuildFileRoot.getFirstChild();
        for (ICpItem entry : buildItem.getGrandChildren(CmsisConstants.COMPONENTS_TAG)) {
            CpComponentInfo component = new CpComponentInfo(componentsItem, CmsisConstants.COMPONENT_TAG);
            String tag = entry.getAttribute(CmsisConstants.COMPONENT_TAG);
            component.setAttributesFromComponentId(tag);

            String packId = getValidPackname(entry.getAttribute(CmsisConstants.FROM_PACK_TAG));
            IAttributes packAttributes = CpPack.attributesFromId(packId);

            CpPackInfo packInfo = new CpPackInfo(component, packAttributes);
            packInfo.setPack(installedPacks.getPack(packId));
            component.addChild(packInfo);
            componentsItem.addChild(component);
        }
    }

    @Override
    public void installRequiredPacks(IProgressMonitor monitor) throws OperationCanceledException, InterruptedException {
        super.installRequiredPacks(monitor);
        CpPlugIn.getPackManager().getInstalledPacks(); // trigger loading of packs
    }

    @Override
    public Collection<String> getRequiredPackIDs() {
        HashSet<String> requiredPacks = new HashSet<>();
        for (ICpItem rootItem : cbuildFileRootToPacks.keySet()) {
            ICpItem buildItem = rootItem.getFirstChild(CmsisConstants.BUILD_TAG);
            if (buildItem == null) {
                continue;
            }
            Collection<? extends ICpItem> allPacks = buildItem.getFirstChild(CmsisConstants.PACKS_TAG).getChildren();
            for (ICpItem packNode : allPacks) {
                String packId = getValidPackname(packNode.getAttribute(CmsisConstants.PACK));
                requiredPacks.add(packId);
                cbuildFileRootToPacks.get(rootItem).add(packId);
            }
        }
        return requiredPacks;
    }

    public String validateProjectFile() {
        String projectFile = getSourceProjectFile();
        File workspacePath = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
        String messageStatus = null;
        if (projectFile == null || projectFile.isEmpty()) {
            messageStatus = Messages.CsolutionProjectImport_ErrorFileFieldEmpty;
        } else if (!projectFile.endsWith(CmsisConstants.EXT_CBUILD_IDX)
                && !projectFile.endsWith(CmsisConstants.EXT_CBUILD_SET)
                && !projectFile.endsWith(CmsisConstants.EXT_CSOLUTION_YML)) {
            messageStatus = Messages.CsolutionProjectImport_ErrorFileTypeIncorrect;
        }
        clear();

        File f = new File(projectFile);
        if (!f.exists()) {
            messageStatus = projectFile + CmsisConstants.SPACE + Messages.RteProjectUpdater_ErrorConfigFileNotExist;
        } else if (f.toPath().startsWith(workspacePath.getPath())) {
            messageStatus = Messages.CsolutionProjectImport_ErrorWorkspacePathNotAllowed;
        }
        return messageStatus;
    }

    @Override
    public String validate() {
        String errorProjectFile = validateProjectFile();
        if (errorProjectFile == null) {
            String errorCbuilds = validateCbuilds();
            return errorCbuilds;
        }
        return errorProjectFile;
    }

    /**
     * Format packName correctly so that the Eclipse Pack Installer can identify the
     * pack e.g. ARM::V2M_MPS3_SSE_300_BSP@1.2.0 ---> ARM.V2M_MPS3_SSE_300_BSP.1.2.0
     *
     * @param packName
     * @return formatted packName
     */
    private static String getValidPackname(String packName) {
        return packName.replaceAll("@|::", CmsisConstants.DOT); //$NON-NLS-1$
    }

    private String decodeCompiler(String compiler) {
        if (compiler.equals("AC6") || compiler.equals("AC5")) { //$NON-NLS-1$ //$NON-NLS-2$
            return CmsisConstants.ARMCC;
        }
        return compiler;
    }

    public HashMap<ICpItem, ArrayList<String>> getCbuildFileRootToPacks() {
        return cbuildFileRootToPacks;
    }

    public String setSourceFileFromCsolutionYmlFile(String filePath) {
        String folderPath = Utils.extractPath(filePath, true);
        String projectName = Utils.getPrefix(Utils.extractFileName(filePath), CmsisConstants.EXT_CSOLUTION_YML);
        File cbuildSet = new File(folderPath + projectName + CmsisConstants.EXT_CBUILD_SET);
        File cbuildIdx = new File(folderPath + projectName + CmsisConstants.EXT_CBUILD_IDX);
        if (cbuildSet.exists()) {
            setSourceProjectFile(cbuildSet.getPath());
        } else if (cbuildIdx.exists()) {
            setSourceProjectFile(cbuildIdx.getPath());
        } else {
            return Messages.CsolutionProjectImport_ErrorNoCbuildSetAndCbuildIndex;
        }
        return null;
    }

    private String validateCbuilds() {
        Collection<ICpItem> cbuildFileRoots = getCbuildFileRootToPacks().keySet();
        String messageStatus = null;
        for (ICpItem cbuildFile : cbuildFileRoots) {
            ICpItem buildItem = cbuildFile.getFirstChild(CmsisConstants.BUILD_TAG);
            if (buildItem == null) {
                cbuildFile.setValid(false);
                cbuildFile.setAttribute(CmsisConstants.INFO, Messages.CsolutionProjectImport_ErrorCbuildNotBuilt);
                messageStatus = Messages.CsolutionProjectImport_ErrorCbuildNotBuilt;
            } else if (buildItem.getFirstChild(CmsisConstants.OUTPUT_TAG) == null) {
                cbuildFile.setValid(false);
                cbuildFile.setAttribute(CmsisConstants.INFO, Messages.CsolutionProjectImport_ErrorOutputFileNotPresent);
                messageStatus = Messages.CsolutionProjectImport_ErrorOutputFileNotPresent;
            } else {
                cbuildFile.setValid(true);
                cbuildFile.setAttribute(CmsisConstants.IS_SELECTED, true);
            }
        }
        return messageStatus;
    }
}
