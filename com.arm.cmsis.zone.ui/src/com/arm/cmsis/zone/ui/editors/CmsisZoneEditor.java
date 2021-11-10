/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eclipse Project - generation from template
 * ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/
package com.arm.cmsis.zone.ui.editors;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.parser.ICpXmlParser;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.ui.editors.RteEditor;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.data.ICpZoneItem;
import com.arm.cmsis.zone.parser.CpZoneParser;
import com.arm.cmsis.zone.project.CmsisZoneProjectCreator;
import com.arm.cmsis.zone.ui.CpZonePluginUI;
import com.arm.cmsis.zone.ui.Messages;

/**
 * Base class for CMSIS-Zone editors
 */
public class CmsisZoneEditor extends RteEditor<CmsisZoneController> {

    protected CmsisZoneResourceMapPage fResourceMapPage;
    protected CmsisZoneMapPage fZoneMapPage;
    protected CmsisZoneSetupPage fZoneSetupPage;

    public CmsisZoneEditor() {
        super();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected ICpXmlParser createParser() {
        return new CpZoneParser();
    }

    @Override
    protected CmsisZoneController createController() {
        return new CmsisZoneController();
    }

    @Override
    protected void createPages() {
        createResourceMapPage();
        createZoneMapPage();
        createZoneSetupPage();
    }

    protected void createResourceMapPage() {
        fResourceMapPage = new CmsisZoneResourceMapPage();
        Composite composite = fResourceMapPage.createControl(getContainer());
        int index = addPage(composite);
        setPageText(index, Messages.CmsisZoneEditor_Resources);
        fResourceMapPage.setModelController(fModelController);
    }

    protected void createZoneMapPage() {
        fZoneMapPage = new CmsisZoneMapPage();
        Composite composite = fZoneMapPage.createControl(getContainer());
        int index = addPage(composite);
        setPageText(index, Messages.CmsisZoneEditor_Zones);
        fZoneMapPage.setModelController(fModelController);
    }

    protected void createZoneSetupPage() {
        fZoneSetupPage = new CmsisZoneSetupPage();
        Composite composite = fZoneSetupPage.createControl(getContainer());
        int index = addPage(composite);
        setPageText(index, Messages.CmsisZoneEditor_Setup);
        fZoneSetupPage.setModelController(fModelController);
    }

    @Override
    public void gotoMarker(IMarker marker) {
        if (getModelController() == null)
            return;
        try {
            if (!marker.getType().equals(CpZonePluginUI.CMSIS_ZONE_PROBLEM_MARKER))
                return;
            ICpZoneItem item = ITreeObject.castTo(marker.getAttribute(CpZonePluginUI.CMSIS_ZONE_PROBLEM_MARKER_ITEM),
                    ICpZoneItem.class);
            if (item == null)
                return;
            String id = item.getId();
            ICpMemoryBlock block = getModelController().getResources().getMemoryBlock(id);
            getModelController().emitRteEvent(CmsisZoneController.ZONE_ITEM_SHOW, block);

        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRelevantFile(String absFileName) {
        if (absFileName == null) {
            return false;
        }
        if (super.isRelevantFile(absFileName))
            return true;

        CmsisZoneController controller = getModelController();
        if (controller == null)
            return false;
        ICpRootZone rootZone = controller.getRootZone();
        if (rootZone == null)
            return false;

        // Convert absFileName to Path
        Path filePath = new Path(absFileName);

        // Get absolute rzone file's path
        String rzoneFile = rootZone.getAbsolutePath(rootZone.getResourceFileName());
        // Convert to File or Path
        Path rzoneFilePath = new Path(rzoneFile);

        // Check if both files are the same
        if (filePath.equals(rzoneFilePath))
            return true;

        return false;
    }

    @Override
    public void handle(RteEvent event) {
        if (event.getData() instanceof ICpZone) {
            switch (event.getTopic()) {
            case CmsisZoneController.ZONE_MODIFIED:
                break;
            case CmsisZoneController.ZONE_ADDED:
                break;
            case CmsisZoneController.ZONE_DELETED:
                break;
            }
            firePropertyChange(IEditorPart.PROP_DIRTY);
            return;
        }
        super.handle(event);
    }

    @Override
    protected boolean checkInputChanged(ICpItem root) {
        if (super.checkInputChanged(root)) {
            return true;
        }
        if (getModelController() == null)
            return true;
        // it could be that rzone file has changed, perform full check
        ICpItem curRoot = fModelController.getDataInfo();
        if (curRoot == null)
            return true;
        CpZoneParser zoneParser = new CpZoneParser();
        String curXmlString = zoneParser.writeToFullXmlString(curRoot);
        String xmlString = zoneParser.writeToFullXmlString(root);
        if (curXmlString == null || xmlString == null)
            return true;

        return !curXmlString.equals(xmlString);
    }

    @Override
    protected void saveXml(IProgressMonitor monitor) throws CoreException {
        super.saveXml(monitor);

        IProject project = null;
        IFile iFile = CpPlugInUI.getFileForLocation(fAbsFileName);
        if (iFile != null) {
            project = iFile.getProject();
        }

        ICpRootZone rootZone = fModelController.getRootZone();
        CmsisZoneProjectCreator.createZoneFiles(rootZone, RteConsole.openConsole(project), monitor);
    }

    @Override
    protected void loadData(String absFileName) {

        if (absFileName == null) {
            return;
        }
        // Validate if input is a .rzone file
        String ext = Utils.extractFileExtension(absFileName);

        // Create azone file name
        String azoneFileName = Utils.removeFileExtension(absFileName) + CmsisConstants.DOT_AZONE;

        if (ext.equals(CmsisConstants.RZONE)) {
            // Check if file exists
            File file = new File(azoneFileName);
            if (!file.exists()) { // Create azone file
                try {
                    IProgressMonitor monitor = new NullProgressMonitor();
                    ICpZone zone = null;
                    CmsisZoneProjectCreator.saveAZoneFile(azoneFileName, zone, absFileName, monitor);
                    // Refresh project's content
                    CpPlugInUI.refreshFile(azoneFileName);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }

        // Load azone file's data
        super.loadData(azoneFileName);
    }

}
