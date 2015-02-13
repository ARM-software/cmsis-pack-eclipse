/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.ui;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpPlugInUI extends AbstractUIPlugin {

	private static HashMap<String, Image> images = new HashMap<String, Image>();
	
	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.ui"; //$NON-NLS-1$
	public static final String CMSIS_PACK_ROOT_PREFERENCE = "com.arm.cmsis.pack.root"; //$NON-NLS-1$ 
	
	// icons
	public static final String ICON_CHECKED = "checked.gif";
	public static final String ICON_UNCHECKED = "unchecked.gif";
	
	public static final String ICON_RESOLVED = "resolved.gif";
	public static final String ICON_DETAILS = "details.gif";
	
	public static final String ICON_INFO = "info.gif";
	public static final String ICON_RTEWARNING = "rteWarning.gif";
	public static final String ICON_RTEERROR = "rteError.gif";
	
	public static final String ICON_RTEFILTER = "rteFilter.gif";
	public static final String ICON_RTEMANAGER = "rteManager.gif";
	public static final String ICON_RTEMANAGER_WARNING = "rteManagerWarning.gif";
	public static final String ICON_RTEMANAGER_ERROR = "rteManagerError.gif";
	
	public static final String ICON_GROUP = "group.gif";
	public static final String ICON_COMPONENT = "component.gif";
	public static final String ICON_COMPONENT_WARNING = "componentWarning.gif";
	public static final String ICON_COMPONENT_ERROR = "componentError.gif";
	public static final String ICON_MULTICOMPONENT = "multiComponent.gif";
	public static final String ICON_MULTICOMPONENT_WARNING = "multiComponentWarning.gif";
	public static final String ICON_MULTICOMPONENT_ERROR = "multiComponentError.gif";
	
	public static final String ICON_PACKINSTALLER = "packInstaller.gif";
	public static final String ICON_CHECK4UPDATE = "check4Update.gif";
	
	public static final String ICON_DEVICE = "device.gif";
	public static final String ICON_DEPRDEVICE = "deprDevice.gif";

	public static final String ICON_RUN = "run.gif";
	
	public static final String ICON_PACKAGE = "package.png";
	public static final String ICON_PACKAGES = "packages.png";
	public static final String ICON_PACKAGE_GREY = "packageGrey.png";
	public static final String ICON_PACKAGE_EMPTY = "packageEmpty.png";
	
	public static final String ICON_EXPAND_ALL = "expandall.gif";
	
	// The shared instance
	private static CpPlugInUI plugin;
	
	/**
	 * The constructor
	 */
	public CpPlugInUI() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IPreferenceStore store = getPreferenceStore();
		String packRoot = store.getString(CMSIS_PACK_ROOT_PREFERENCE);  

		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty() == CMSIS_PACK_ROOT_PREFERENCE) {
					String newPackRoot = event.getNewValue().toString();
					ICpPackManager pm  = CpPlugIn.getDefault().getPackManager();
					if(pm != null)
						pm.setDefaultPackDirectory(newPackRoot);
				}
			}
		}); 

		ICpPackManager pm  = CpPlugIn.getDefault().getPackManager();
		if(pm != null)
			pm.setDefaultPackDirectory(packRoot);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Collection<Image> values = images.values();
		for (Image image : values) {
			image.dispose();
		}
		
		plugin = null;
		super.stop(context);
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
		ImageDescriptor imageDescr = imageDescriptorFromPlugin(PLUGIN_ID, file);
		if(imageDescr == null && !file.startsWith("icons/")) {
			imageDescr = imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + file);
		}
			
		return imageDescr;
	}
	
	

	/**
	 * Helper method to load an image
	 * @param file name of the image
	 * @return Image object
	 */
	private static Image createImage(String file) {
	  String path = "icons/" + file;
	  ImageDescriptor image = getImageDescriptor(path);
	  if(image != null)
		  return image.createImage();
	  return null;

	}
	
	/**
	 * @param imageName name of the image
	 * @return an Image object
	 */
	public static Image getImage(String imageName) {
		Image image = null;
		if (images.containsKey(imageName)) {
			image = images.get(imageName);
		}
		else {
			image = createImage(imageName);
			images.put(imageName, image);
		}
		
		return image;
	}
}
