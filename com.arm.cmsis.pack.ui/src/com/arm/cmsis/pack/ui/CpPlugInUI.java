/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Eclipse Project - generation from template   
* ARM Ltd and ARM Germany GmbH - application-specific implementation

* Snippet to obtain resource from selection is taken from here:
* https://wiki.eclipse.org/FAQ_How_do_I_access_the_active_project%3F
*******************************************************************************/

package com.arm.cmsis.pack.ui;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpPlugInUI extends AbstractUIPlugin {

	private static HashMap<String, Image> images = new HashMap<String, Image>();
	private static HashMap<String, ImageDescriptor> imageDescriptors = new HashMap<String, ImageDescriptor>();
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.ui"; //$NON-NLS-1$
		
	public static final String ICONS_PATH  		= "icons/"; 		//$NON-NLS-1$
	// icons

	public static final String ICON_BOOK  		= "book.png"; 		//$NON-NLS-1$

	public static final String ICON_CHECKED 	= "checked.gif"; 	//$NON-NLS-1$
	public static final String ICON_UNCHECKED 	= "unchecked.gif"; 	//$NON-NLS-1$

	public static final String ICON_CHECKED_GREY 	= "checkedGrey.gif"; 	//$NON-NLS-1$
	public static final String ICON_UNCHECKED_GREY 	= "uncheckedGrey.gif"; 	//$NON-NLS-1$
	
	
	public static final String ICON_RESOLVE 		= "resolve.gif"; 	//$NON-NLS-1$
	public static final String ICON_RESOLVE_DISABLED= "resolveDisabled.gif"; 	//$NON-NLS-1$
	public static final String ICON_DETAILS 		= "details.gif"; 	//$NON-NLS-1$
	
	public static final String ICON_INFO 			= "info.gif"; 		//$NON-NLS-1$
	public static final String ICON_WARNING 		= "warning.gif"; 	//$NON-NLS-1$
	public static final String ICON_ERROR 			= "error.gif"; 		//$NON-NLS-1$
	
	public static final String ICON_RTEFILTER 		= "rteFilter.gif"; 	//$NON-NLS-1$
	public static final String ICON_RTE 	  		= "rte.gif"; 		//$NON-NLS-1$
	public static final String ICON_RTE_WARNING 	= "rteWarning.gif"; //$NON-NLS-1$
	public static final String ICON_RTE_ERROR   	= "rteError.gif"; 	//$NON-NLS-1$

	public static final String ICON_RTE_OVR 	  	= "rte_ovr.gif"; 		//$NON-NLS-1$
	public static final String ICON_RTE_WARNING_OVR = "rteWarning_ovr.gif"; //$NON-NLS-1$
	public static final String ICON_RTE_ERROR_OVR   = "rteError_ovr.gif"; 	//$NON-NLS-1$

	public static final String ICON_RTE_CONSOLE   	= "rteConsole.gif"; 	//$NON-NLS-1$

	
	public static final String ICON_COMPONENT_CLASS			= "componentClass.gif"; 		//$NON-NLS-1$
	public static final String ICON_COMPONENT 				= "component.gif"; 				//$NON-NLS-1$
	public static final String ICON_COMPONENT_WARNING 		= "componentWarning.gif"; 		//$NON-NLS-1$
	public static final String ICON_COMPONENT_ERROR 		= "componentError.gif"; 		//$NON-NLS-1$
	public static final String ICON_MULTICOMPONENT 			= "multiComponent.gif"; 		//$NON-NLS-1$
	public static final String ICON_MULTICOMPONENT_WARNING 	= "multiComponentWarning.gif"; 	//$NON-NLS-1$
	public static final String ICON_MULTICOMPONENT_ERROR   	= "multiComponentError.gif"; 	//$NON-NLS-1$
	
	public static final String ICON_PACKINSTALLER 	= "packInstaller.gif"; 	//$NON-NLS-1$
	public static final String ICON_CHECK4UPDATE 	= "check4Update.gif"; 	//$NON-NLS-1$
	
	public static final String ICON_DEVICE 			= "device.gif"; 		//$NON-NLS-1$
	public static final String ICON_DEPRDEVICE 		= "deprDevice.gif"; 	//$NON-NLS-1$
	public static final String ICON_DEVICE_32 		= "device32.png"; 		//$NON-NLS-1$
	public static final String ICON_DEVICE_48 		= "device48.png"; 		//$NON-NLS-1$
	
	public static final String ICON_BOARD 			= "board.png";	 		//$NON-NLS-1$
	public static final String ICON_BOARD_GREY		= "boardGrey.png";		//$NON-NLS-1$
	
	public static final String ICON_RUN 			= "run.gif"; 			//$NON-NLS-1$
	
	public static final String ICON_PACKAGE 		= "package.png"; 		//$NON-NLS-1$
	public static final String ICON_PACKAGE_EMPTY 	= "packageEmpty.png"; 	//$NON-NLS-1$
	public static final String ICON_PACKAGE_GREY 	= "packageGrey.png"; 	//$NON-NLS-1$
	public static final String ICON_PACKAGE_RED		= "packageRed.png"; 	//$NON-NLS-1$
	
	public static final String ICON_PACKAGES 		= "packages.png"; 		//$NON-NLS-1$
	public static final String ICON_PACKAGES_EMPTY 	= "packagesEmpty.png"; 	//$NON-NLS-1$
	public static final String ICON_PACKAGES_GREY	= "packagesGrey.png"; 	//$NON-NLS-1$
	public static final String ICON_PACKAGES_RED	= "packagesRed.png"; 	//$NON-NLS-1$

	public static final String ICON_PACKAGES_FILTER	= "packagesFilter.png";	//$NON-NLS-1$
	
	public static final String ICON_EXPAND_ALL 		= "expandall.gif"; 		//$NON-NLS-1$
	
	public static final String ICON_PIN = "pin.png"; 						//$NON-NLS-1$
	public static final String ERROR_OVR = "error_ovr.gif"; 				//$NON-NLS-1$
	public static final String WARN_OVR = "warn_ovr.gif"; 					//$NON-NLS-1$
	public static final String ASSUME_VALID_OVR = "assume_valid_ovr.gif"; 	//$NON-NLS-1$
	public static final String CHECKEDOUT_OVR = "checkedout_ovr.gif"; 		//$NON-NLS-1$
	public static final String MODIFIED_OVR = "modified_ovr.gif"; 			//$NON-NLS-1$
	
	public static final RGB GREEN = new RGB(189,249,181);
	public static final RGB YELLOW = new RGB(252,200, 46);
	
	// The shared instance
	private static CpPlugInUI plugin;

	private IPreferenceStore fCorePreferenceStore = null;
	
	/**
	 * The constructor
	 */
	public CpPlugInUI() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IPreferenceStore store = getCorePreferenceStore();

		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE) {
					String newPackRoot = event.getNewValue().toString();
					ICpPackManager pm  = CpPlugIn.getPackManager();
					if(pm != null && !pm.getCmsisPackRootDirectory().equals(newPackRoot) ) {
						pm.setCmsisPackRootDirectory(newPackRoot);
						pm.reload();
					}
				}
			}
		}); 

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Collection<Image> values = images.values();
		for (Image image : values) {
			image.dispose();
		}
		images.clear(); 
		imageDescriptors.clear();
		
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns preference store of CpPlugIn, since that does not have GUI
	 * @return IPreferenceStore
	 */
	public IPreferenceStore getCorePreferenceStore() {
		if (fCorePreferenceStore == null) {
			fCorePreferenceStore= new ScopedPreferenceStore(InstanceScope.INSTANCE, CpPlugIn.PLUGIN_ID);
		}
		return fCorePreferenceStore;
	}

	
	static public void addPreferenceStoreListener(IPropertyChangeListener listener) {
		if(plugin == null)
			return;
		IPreferenceStore store = plugin.getPreferenceStore();
		if(store == null)
			return;
		store.addPropertyChangeListener(listener);
	}

	static public void removePreferenceStoreListener(IPropertyChangeListener listener) {
		if(plugin == null)
			return;
		IPreferenceStore store = plugin.getPreferenceStore();
		if(store == null)
			return;
		store.removePropertyChangeListener(listener);
	}

	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CpPlugInUI getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String file) {
		String icons_file = null;
		if(!file.startsWith(ICONS_PATH))
			icons_file = ICONS_PATH + file;
		else 
			icons_file = file;
		
		if(imageDescriptors.containsKey(icons_file))
			return imageDescriptors.get(icons_file);
		
		ImageDescriptor imageDescr = imageDescriptorFromPlugin(PLUGIN_ID, icons_file);
		if(imageDescr != null) { 
			imageDescriptors.put(icons_file, imageDescr);
		}
			
		return imageDescr;
	}
	
	

	/**
	 * Helper method to load an image
	 * @param file name of the image
	 * @return Image object
	 */
	private static Image createImage(String file) {
	  ImageDescriptor image = getImageDescriptor(file);
	  if(image != null)
		  return image.createImage();
	  return null;

	}
	
	/**
	 * @param file name of the image
	 * @return an Image object
	 */
	public static Image getImage(String file) {
		String icons_file = null;
		if(!file.startsWith(ICONS_PATH))
			icons_file = ICONS_PATH + file;
		else 
			icons_file = file;
		
		Image image = null;
		if (images.containsKey(icons_file)) {
			image = images.get(icons_file);
		}
		else {
			image = createImage(icons_file);
			images.put(icons_file, image);
		}
		
		return image;
	}
	
	/**
	 * Returns first selected resource  
	 * @param selection ISelection selection  
	 * @return IResource or null if not selected
	 */
	public static IResource getResourceFromSelection(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return null;
		IStructuredSelection structSel = (IStructuredSelection) selection;
		Object element = structSel.getFirstElement();
		if (element instanceof IResource)
			return (IResource) element;
		else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable)element;
			Object adapter = adaptable.getAdapter(IResource.class);
			return (IResource) adapter;
		}
		return null;
	}
	
	
	/**
	 * Returns first selected project  
	 * @param selection ISelection selection  
	 * @return IProject or null if not selected
	 */
	public static IProject getProjectFromSelection(ISelection selection) {
		IResource res = getResourceFromSelection(selection);
		if(res != null)
			return res.getProject();
		return null;
	}
}
