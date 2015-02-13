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

package com.arm.cmsis.pack;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CpPlugIn extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.arm.cmsis.pack"; //$NON-NLS-1$
	private static BundleContext context;
	private static CpPlugIn plugin;
	
	ICpPackManager packManager = null;
	

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		CpPlugIn.context = bundleContext;
		// The shared instance
		plugin = this;
		
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		CpPlugIn.context = null;
		plugin = null;
		packManager = null;
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

	/**
	 * @return the packManager
	 */
	public ICpPackManager getPackManager() {
		return packManager;
	}
	
	public void setPackManager(ICpPackManager pm) {
		packManager = pm;
	}

	
}
