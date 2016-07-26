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

package com.arm.cmsis.pack.project;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.variableresolvers.PathVariableResolver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.configuration.IRteConfiguration;
import com.arm.cmsis.pack.project.utils.ProjectUtils;

/**
 * The class is responsible for resolving cmsis_pack_root variable.<br>
 * It is contributed to org.eclipse.core.variables.dynamicVariables and org.eclipse.core.resources.variableResolvers extension points
 */
public class CpVariableResolver extends PathVariableResolver implements IDynamicVariableResolver {

	public static final String[] supportedPathVariables = new String[] {CmsisConstants.CMSIS_PACK_ROOT_VAR, CmsisConstants.CMSIS_PACK_ROOT};
	
	@Override
	public String getValue(String variable, IResource resource) {
		if(variable.equals(CmsisConstants.CMSIS_PACK_ROOT_VAR) || variable.equals(CmsisConstants.CMSIS_PACK_ROOT)){
			URI packRootURI = getCmsisPackRootURI();
			if(packRootURI != null) {
				String packRoot = packRootURI.toString(); 
				return packRoot;  // a string like "file:/" + CMSIS_PACK_ROOT;
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
		if(argument == null || argument.isEmpty())
			return getCmsisPackRoot();
//		if(variable.equals(CmsisConstants.CMSIS_PACK_ROOT)) {
//			return getCmsisPackRoot() + argument;
//			
//		}
		if(variable.getName().equals(CmsisConstants.CMSIS_DFP)) {
			// the argument must represent project name  
			IProject proj = ProjectUtils.getProject(argument);  
			RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
			IRteProject rteProject = rteProjectManager.getRteProject(proj);
			if (rteProject == null)
				return null;
			IRteConfiguration rteConf = rteProject.getRteConfiguration();
			if(rteConf == null) 
				return null;
			return rteConf.getDfpPath();
		}
		return getCmsisPackRoot() + argument;
	}
	
	/**
	 * Returns CMSIS pack root folder (the value of <code>${cmsis_pack_root}</code> variable)
	 * @return CMSIS pack root folder as string
	 */
	static public String getCmsisPackRoot() {
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm != null) { 
			return pm.getCmsisPackRootDirectory();
		}
		return null;
	}
	
	/**
	 * Returns CMSIS pack root folder (the value of <code>${cmsis_pack_root}</code> variable) as URI
	 * @return CMSIS pack root folder as URI
	 */
	static public URI getCmsisPackRootURI() {
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm != null) { 
			return pm.getCmsisPackRootURI();
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
		if(root != null && !root.isEmpty()) {
			boolean ignoreCase = File.separatorChar == '\\';
			if(path.regionMatches(ignoreCase, 0, root, 0, root.length())){
				String result = CmsisConstants.CMSIS_PACK_ROOT_VAR + path.substring(root.length());
				return result; 
			}
			
		}
		return path;
	}
}
