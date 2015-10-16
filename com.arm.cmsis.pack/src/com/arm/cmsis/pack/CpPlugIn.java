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
*******************************************************************************/

package com.arm.cmsis.pack;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.BundleContext;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpPlugIn extends Plugin implements IRteEventProxy {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack"; //$NON-NLS-1$
	public static final String CMSIS_PACK_ROOT_PREFERENCE = "com.arm.cmsis.pack.root"; //$NON-NLS-1$ 
	private static BundleContext context;
	private static CpPlugIn plugin;
	
	IRteEventProxy rteEventProxy = new RteEventProxy();   
	ICpPackManager packManager = null;
	
	public CpPlugIn() {
		super();
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		plugin = this;
		CpPlugIn.context = bundleContext;
		IPreferencesService prefs = Platform.getPreferencesService();
		String packRoot = prefs.getString(PLUGIN_ID, CMSIS_PACK_ROOT_PREFERENCE, CmsisConstants.EMPTY_STRING, null);
		DeviceVendor.fillMaps();
		
		// The shared instance
		packManager = new CpPackManager();
		packManager.setRteEventProxy(this);
		packManager.initParser(null);
		packManager.setCmsisPackRootDirectory(packRoot);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		CpPlugIn.context = null;
		DeviceVendor.clear();
		plugin = null;
		packManager = null;
		rteEventProxy.removeAllListeners();
		rteEventProxy = null;
		super.stop(bundleContext);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CpPlugIn getDefault() {
		return plugin;
	}

	static BundleContext getContext() {
		return context;
	}


	/**
	 * Return the pack manager 
	 * @return ICpPackManager
	 */
	static public ICpPackManager getPackManager() {
		return plugin != null ? plugin.packManager : null;
	}
	
	public void setPackManager(ICpPackManager pm) {
		packManager = pm;
	}

	static public void addRteListener(IRteEventListener listener) {
		if(plugin != null) 
			plugin.addListener(listener);
	}

	static public void removeRteListener(IRteEventListener listener) {
		if(plugin != null) 
			plugin.removeListener(listener);
	}

	
	@Override
	public void addListener(IRteEventListener listener) {
		rteEventProxy.addListener(listener);
	}

	@Override
	public void removeListener(IRteEventListener listener) {
		rteEventProxy.removeListener(listener);
	}

	@Override
	public void removeAllListeners() {
		rteEventProxy.removeAllListeners();
	}

	@Override
	public void notifyListeners(RteEvent event) {
		rteEventProxy.notifyListeners(event);
	}

	@Override
	public void emitRteEvent(String topic, Object data) {
		rteEventProxy.emitRteEvent(topic, data);
		
	}

}
