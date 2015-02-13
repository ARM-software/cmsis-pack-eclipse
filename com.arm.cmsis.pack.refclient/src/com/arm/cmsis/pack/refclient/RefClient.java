package com.arm.cmsis.pack.refclient;


import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.CpPackManager;
import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * The activator class controls the plug-in life cycle
 */
public class RefClient extends AbstractUIPlugin implements IRteEventProxy {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack.refclient"; //$NON-NLS-1$

	// The shared instance
	private static RefClient plugin;
	private IRteConfigurationProxy activeConfiguration = null; 
	private ListenerList rteConfigChangeListeners = new ListenerList();
	ICpPackManager packManager = null;
	
	/**
	 * The constructor
	 */
	public RefClient() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		IPreferenceStore store = CpPlugInUI.getDefault().getPreferenceStore();
		String packRoot = store.getString(CpPlugInUI.CMSIS_PACK_ROOT_PREFERENCE);  

		packManager = new CpPackManager();
		packManager.setRteEventProxy(this);
		packManager.initParser(null);
		packManager.setDefaultPackDirectory(packRoot);
		CpPlugIn.getDefault().setPackManager(packManager);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		activeConfiguration = null;
		if(packManager != null) {
			packManager.destroy();
			packManager = null;
		}
		super.stop(context);
	}

	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RefClient getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @return the active configuration
	 */
	public IRteConfigurationProxy getActiveRteConfiguration() {
		return activeConfiguration;
	}
	
	/**
	 * @param activeConfiguration the 
	 */
	public void setActiveRteConfiguration(IRteConfigurationProxy configuration) {
		if(activeConfiguration != configuration) {
			activeConfiguration = configuration;
			fireRteEvent(new RteEvent(RteEvent.CONFIGURATION_ACTIVATED, activeConfiguration));
		}
	}
	
	public void addRteConfigListener(IRteEventListener listener) {
		rteConfigChangeListeners.add(listener);
	}
	public void removeRteConfigListener(IRteEventListener listener) {
		rteConfigChangeListeners.remove(listener);
	}

	public void removeRteConfigListeners() {
		rteConfigChangeListeners.clear();
	}

	
	public void fireRteEvent(RteEvent event) {
		for (Object obj : rteConfigChangeListeners.getListeners()) {
			IRteEventListener listener = (IRteEventListener) obj;
			try {
				listener.handleRteEvent(event);
			} catch (Exception ex) {
				removeRteConfigListener(listener);
			}
		} 
	}
		

	@Override
	public void processRteEvent(RteEvent event) {
		fireRteEvent(event);
	}
	
}
