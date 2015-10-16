/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.preferences;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * The class is responsible for resolving cmsis_pack_root variable.<br>
 * It is contributed to org.eclipse.core.variables.dynamicVariables and org.eclipse.core.resources.variableResolvers extension points
 */
public class CpVariableResolver extends PathVariableResolver implements IDynamicVariableResolver {

	public static final String[] supportedPathVariables = new String[] {CmsisConstants.CMSIS_PACK_ROOT};
	
	@Override
	public String getValue(String variable, IResource resource) {
		if(variable.equals(CmsisConstants.CMSIS_PACK_ROOT)){
			String packRoot =getCmsisPackRoot();
			if(packRoot != null && !packRoot.isEmpty()) {
				packRoot = packRoot.replace('\\', '/'); // convert all backslashes to slashes for consistency
				return  "file:/" + packRoot; //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public String[] getVariableNames(String variable, IResource resource) {
		return supportedPathVariables;
	}

	@Override
	public String resolveValue(IDynamicVariable variable, String argument) 	throws CoreException {
		return getCmsisPackRoot();
	}
	
	/**
	 * Returns CMSIS pack root folder (the value of <code>${cmsis_pack_root}</code> variable)
	 * @return CMSIS pack root folder
	 */
	static public String getCmsisPackRoot() {
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm != null) { 
			return pm.getCmsisPackRootDirectory();
		}
		return null;
	}
	
	
	/**
	 * Replaces <code>${cmsis_pack_root}</code> prefix with actual value
	 * @param path source path to substitute
	 * @return the resulting string
	 */
	static public String expandCmsisRootVariable(String path) {
		if(path == null || path.isEmpty())
			return path;

		if(path.startsWith(CmsisConstants.CMSIS_PACK_ROOT_VAR)) {
			String root = getCmsisPackRoot();
			if(root != null && !root.isEmpty()) { 
				String result = root + path.substring(CmsisConstants.CMSIS_PACK_ROOT_VAR.length());
				return result; 
			}
		}
		return path;
	}

	/**
	 * Replaces path prefix with <code>${cmsis_pack_root}</code> if it equals to the prefix with actual value
	 * @param path source path to substitute
	 * @return the resulting string
	 */
	static public String insertCmsisRootVariable(String path) {
		if(path == null || path.isEmpty())
			return path;
		String root = getCmsisPackRoot();
		if(root != null && !root.isEmpty() && path.startsWith(root)) { 
			String result = CmsisConstants.CMSIS_PACK_ROOT_VAR + path.substring(root.length());
			return result; 
		}
		return path;
	}
}
