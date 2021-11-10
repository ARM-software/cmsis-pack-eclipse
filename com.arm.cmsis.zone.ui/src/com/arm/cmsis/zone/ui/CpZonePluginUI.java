package com.arm.cmsis.zone.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.error.ICmsisErrorCollection;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpZonePluginUI extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.arm.cmsis.zone.ui"; //$NON-NLS-1$

    public static final String CMSIS_ZONE_PROBLEM_MARKER = "com.arm.cmsis.zone.problemmarker"; //$NON-NLS-1$
    public static final String CMSIS_ZONE_PROBLEM_MARKER_ITEM = CMSIS_ZONE_PROBLEM_MARKER + ".item"; //$NON-NLS-1$

    public static final String toolName = Messages.CpZonePluginUI_ToolName;
    public static String toolId = null;

    // The shared instance
    private static CpZonePluginUI plugin;

    /**
     * The constructor
     */
    public CpZonePluginUI() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        Bundle bundle = getBundle();
        String version = bundle.getVersion().toString();

        toolId = toolName + CmsisConstants.SPACE + Utils.removeFileExtension(version); // removes .qualifier

        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static CpZonePluginUI getDefault() {
        return plugin;
    }

    public static String getToolId() {
        return toolId;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative
     * path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Sets CMSIS-Zone markers to the resources specified by CMSIS-Zone errors
     * errors
     * 
     * @param errors ICmsisErrorCollection
     */
    public static void setCmsisZoneMarkers(ICmsisErrorCollection errors) {
        if (!PlatformUI.isWorkbenchRunning())
            return; // no markers in non-UI
        if (errors == null)
            return;

        try {
            for (CmsisError err : errors.getErrors()) {
                int severity = toMarkerSeverity(err.getSeverity());
                if (severity < 0)
                    continue; // not a problem
                IFile file = CpPlugInUI.getFileForLocation(err.getFile());
                if (file == null)
                    continue; // no associated file

                IMarker marker = file.createMarker(CpZonePluginUI.CMSIS_ZONE_PROBLEM_MARKER);
                marker.setAttribute(IMarker.SEVERITY, severity);
                marker.setAttribute(IMarker.MESSAGE, err.getFormattedMessage());

                // construct location
                ICmsisItem item = err.getItem();
                int line = err.getLine();
                if (line >= 0) {
                    String location = file.getName() + err.getLineColumnString();
                    marker.setAttribute(IMarker.LOCATION, location);
                    marker.setAttribute(IMarker.LINE_NUMBER, line);
                } else if (item != null) {
                    marker.setAttribute(IMarker.LOCATION, item.getId());
                    marker.setAttribute(CpZonePluginUI.CMSIS_ZONE_PROBLEM_MARKER_ITEM, item);
                }
            }

        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Removes CMSIS-Zone markers for the supplied resource
     * 
     * @param resource FILE, FOLDER or PROJECT to update
     */
    public static void removeCmsisZoneMarkers(IResource resource) {
        if (!PlatformUI.isWorkbenchRunning())
            return; // no markers in non-UI
        if (resource == null)
            return;

        try {
            resource.deleteMarkers(CpZonePluginUI.CMSIS_ZONE_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Converts ESeverity enum values to IMarker constants
     * 
     * @param severity ESeverity
     * @return integer constant or -1 if severity is None
     */
    public static int toMarkerSeverity(ESeverity severity) {
        switch (severity) {
        case Error:
        case FatalError:
            return IMarker.SEVERITY_ERROR;
        case Info:
            return IMarker.SEVERITY_INFO;
        case Warning:
            return IMarker.SEVERITY_WARNING;
        case None:
        default:
            break;

        }
        return -1;
    }

}
