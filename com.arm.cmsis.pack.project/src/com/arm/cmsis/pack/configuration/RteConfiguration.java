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

package com.arm.cmsis.pack.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arm.cmsis.pack.build.settings.IBuildSettings;
import com.arm.cmsis.pack.build.settings.IMemorySettings;
import com.arm.cmsis.pack.build.settings.IRteToolChainAdapter;
import com.arm.cmsis.pack.build.settings.MemorySettings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.CpVariableResolver;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.project.utils.RtePathComparator;
import com.arm.cmsis.pack.rte.IRteModel;
import com.arm.cmsis.pack.rte.RteModel;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of IRteConfiguration interface
 */
public class RteConfiguration implements IRteConfiguration {
	
	protected IRteModel fModel = null;     // underlying model that is source of information
	protected ICpConfigurationInfo fConfigInfo = null;   // meta-information that is stored .rteconfig file and used to transfer information to/from model

	RteBuildSettings rteBuildSettings = new RteBuildSettings(); 

	 // source files included in project: project relative path -> absPath
	protected Map<String, ICpFileInfo> projectFiles = new HashMap<String, ICpFileInfo>();
	// paths to library sources (for debugger)
	protected Set<String> libSourcePaths = new TreeSet<String>(new RtePathComparator());  	
	// pieces of code put into RTE_Components.h file 
	protected List<String> rteComponentsH = new LinkedList<String>(); 
	// header -> comment (for editor)
	protected Map<String, String> headers = new TreeMap<String, String>(new AlnumComparator(false)); 
	// documentation files relevant to configuration
	protected Map<String, String>  docs = new TreeMap<String, String>(new AlnumComparator(false)); 
	protected String svdFile = null;      
	
	protected ICpComponentInfo deviceStartupComponent = null;
	protected ICpComponentInfo cmsisCoreComponent = null;
	protected ICpComponentInfo cmsisRtosComponent = null;
	// device header name without path
	protected String 		 deviceHeader = null;  // 
	
	boolean valid = true; // flag that indicates that device and all required components are resolved   
	
	public RteConfiguration() {
	}

	protected void clear() {
		rteBuildSettings.clear();
		projectFiles.clear();
		libSourcePaths.clear();  	
		
		rteComponentsH.clear(); 
		
		headers.clear(); 
		docs.clear();     
		deviceHeader = null;
		deviceStartupComponent = null;
		cmsisCoreComponent = null;
		cmsisRtosComponent = null;
		valid = true;
	}


	@Override
	public ICpConfigurationInfo getConfigurationInfo() {
		return fConfigInfo;
	}

	
	@Override
	public ICpDeviceInfo getDeviceInfo() {
		return fConfigInfo != null ? fConfigInfo.getDeviceInfo() : null;
	}

	@Override
	public ICpDebugConfiguration getDebugConfiguration()
	{
		ICpDeviceInfo di = getDeviceInfo();
		if(di != null)
			return di.getDebugConfiguration();
		return null;
	}
	
	@Override
	public IBuildSettings getBuildSettings() {
		return rteBuildSettings;
	}

	@Override
	public Map<String, ICpFileInfo> getProjectFiles() {
		return projectFiles;
	}
	

	@Override
	public ICpFileInfo getProjectFileInfo(String fileName) {
		return projectFiles.get(fileName);
	}

	@Override
	public Collection<String> getLibSourcePaths() {
		return libSourcePaths;
	}

	@Override
	public Collection<String> getRteComponentsHCode() {
		return rteComponentsH;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public Map<String, String> getDocs() {
		return docs;
	}

	@Override
	public String getDeviceHeader() {
		return deviceHeader;
	}


	@Override
	public String getSvdFile() {
		return svdFile;
	}

	@Override
	public ICpComponentInfo getDeviceStartupComponent() {
		return deviceStartupComponent;
	}

	@Override
	public ICpComponentInfo getCmsisCoreComponent() {
		return cmsisCoreComponent;
	}

	@Override
	public ICpComponentInfo getCmsisRtosComponent() {
		return cmsisRtosComponent;
	}

	@Override
	public void setConfigurationInfo(ICpConfigurationInfo info) {
		if(fConfigInfo == info)
			return;
		fConfigInfo = info;
		if(fModel == null && fConfigInfo != null) {
			fModel = new RteModel();
		}
		if(fModel != null) {
			fModel.setConfigurationInfo(info);
			fConfigInfo = fModel.getConfigurationInfo();
		}
		collectSettings();
	}

	protected void collectSettings() {
		clear();
		if(getDeviceInfo() == null)
			return;
		ICpItem apisItem =  fConfigInfo.getFirstChild(CmsisConstants.APIS_TAG);
		collectComponentSettings(apisItem);
		ICpItem componentsItem =  fConfigInfo.getFirstChild(CmsisConstants.COMPONENTS_TAG);
		collectComponentSettings(componentsItem);
		
		collectDeviceSettings(getDeviceInfo());
		
		// insert default settings
		rteBuildSettings.addStringListValue(IOption.INCLUDE_PATH, CmsisConstants.PROJECT_RTE_PATH);
		headers.put(CmsisConstants.RTE_RTE_Components_h, Messages.RteConfiguration_ComponentSelection);
		
	}

	protected void collectDeviceSettings(ICpDeviceInfo di) {
		ICpDeviceItem d = di.getDevice();
		if(d == null)
			return;

		rteBuildSettings.setDeviceAttributes(di.attributes());
		String processorName = di.getProcessorName();
		ICpItem props = d.getEffectiveProperties(processorName);
		if(props == null)
			return;
		
		Collection<? extends ICpItem> children = props.getChildren();
		for(ICpItem p : children) {
			String tag = p.getTag();
			
			if(tag.equals(CmsisConstants.COMPILE_TAG)) {
				String define = p.getAttribute(CmsisConstants.DEFINE);
				rteBuildSettings.addStringListValue(IOption.PREPROCESSOR_SYMBOLS, define);
				
				String header = p.getAttribute(CmsisConstants.HEADER);
				if(header != null && !header.isEmpty()) {
					deviceHeader = Utils.extractFileName(header);
					// check if header is already defined via device startup component
					boolean inserted = false;
					for(String h: headers.keySet()) {
						if(Utils.extractFileName(h).equals(deviceHeader)) {
							inserted = true;
							break;
						}
					}
					if(!inserted) {
						header = p.getAbsolutePath(header);
						header = CpVariableResolver.insertCmsisRootVariable(header);
						addFile(header, EFileCategory.HEADER, Messages.RteConfiguration_DeviceHeader);
					}
				}
			}
		}
	}


	protected void collectComponentSettings(ICpItem componentsParent) {
		if(componentsParent == null)
			return;
		Collection<? extends ICpItem> components = componentsParent.getChildren();
		if(components == null || components.isEmpty())
			return;
		for(ICpItem child : components) {
			if(child instanceof ICpComponentInfo) {
				collectComponentSettings((ICpComponentInfo)child);	
			}
		}
	}

	protected void collectComponentSettings(ICpComponentInfo ci) {
		// collect specific components
		if(ci.isDeviceStartupComponent()) {
			deviceStartupComponent = ci;
		} else if(ci.isCmsisCoreComponent()) {
			cmsisCoreComponent = ci;
		} else if(ci.isCmsisRtosComponent()) {
			cmsisRtosComponent = ci;
		}
		ICpComponent c = ci.getComponent();
		int count = ci.getInstanceCount();
		if(c != null) {
			addRteComponentsHCode(c, count);
		}
		collectFiles(ci);
	}

	private void collectFiles(ICpComponentInfo ci) {
		Collection<? extends ICpItem> children = ci.getChildren();
		if( children == null || children.isEmpty())
			return;
		boolean bMultiInstance = ci.isMultiInstance();
		int count = ci.getInstanceCount();

		for(ICpItem child :  children) {
			if(!(child instanceof ICpFileInfo))
				continue;
			ICpFileInfo fi = (ICpFileInfo)child;
			if(bMultiInstance && fi.getRole() == EFileRole.CONFIG) {
				for(int i = 0; i < count; i++) {
					collectFile(fi, ci, i);	
				}
			} else {
				collectFile(fi, ci, -1);
			}
		}
	}

	/**
	 * Collects file to configuration
	 * @param fi ICpFileInfo
	 * @param ci parent ICpComponentInfo
	 * @param index for multi-instance components : instance index, for others -1
	 */
	protected void collectFile(ICpFileInfo fi, ICpComponentInfo ci, int index) {
		String name = fi.getName();
		
		ICpFile f = fi.getFile();
		String absPath  = null;
		String effectivePath = null;
		if(f != null)
			absPath = f.getAbsolutePath(name);
		
		EFileRole role = fi.getRole();
		if(isAddToProject(fi)){
			String className = ci.getAttribute(CmsisConstants.CCLASS);
			String deviceName = fConfigInfo.getDeviceInfo().getDeviceName();
			effectivePath = getProjectRelativePath(fi, className, deviceName, index);
			projectFiles.put(effectivePath, fi);
			if(role != EFileRole.CONFIG  && role != EFileRole.COPY) {
				effectivePath = CpVariableResolver.insertCmsisRootVariable(absPath);
			}
		} else {
			effectivePath = CpVariableResolver.insertCmsisRootVariable(absPath);
		}
		
		EFileCategory cat = fi.getCategory();
		if(cat == EFileCategory.LINKER_SCRIPT  && !ci.isDeviceStartupComponent())
			return;

		String componentName = ci.getName();
		addFile(effectivePath, cat, componentName);
		if(cat == EFileCategory.LIBRARY) {
			addLibrarySourcePaths(f);
		}
	}
	
	/**
	 * Adds CmsisConstants.PROJECT_LOCAL_PATH  prefix if path is relative 
	 * @param path path to adjust
	 * @return
	 */
	protected String adjustRelativePath(String path){
		if(path != null && path.startsWith(CmsisConstants.RTE))  // local case 
			return CmsisConstants.PROJECT_LOCAL_PATH + path;
		return path;
	}
	
	protected void addFile(String effectivePath, EFileCategory cat, String comment) {
		if(effectivePath == null || effectivePath.isEmpty())
			return;
		switch(cat){
		case DOC:
			docs.put(effectivePath, comment);
			break;
		case HEADER:
			headers.put(effectivePath, comment);
			effectivePath = ProjectUtils.removeLastPathSegment(effectivePath);
		case INCLUDE:
			if(!effectivePath.isEmpty()) {
				effectivePath = adjustRelativePath(effectivePath); 
				rteBuildSettings.addStringListValue(IOption.INCLUDE_PATH, Utils.removeTrailingSlash(effectivePath));
			}
			break;
		case IMAGE:
			break;
		case LIBRARY:
			effectivePath = adjustRelativePath(effectivePath);
			rteBuildSettings.addStringListValue(IOption.LIBRARIES, effectivePath);
			effectivePath = ProjectUtils.removeLastPathSegment(effectivePath);
			rteBuildSettings.addStringListValue(IOption.LIBRARY_PATHS, Utils.removeTrailingSlash(effectivePath));
			break;
		case LINKER_SCRIPT:
			effectivePath = adjustRelativePath(effectivePath);
			rteBuildSettings.addStringListValue(IRteToolChainAdapter.LINKER_SCRIPT_OPTION, effectivePath);
			break;
		case OBJECT:
			effectivePath = adjustRelativePath(effectivePath);
			rteBuildSettings.addStringListValue(IOption.OBJECTS, effectivePath);
			break;
		case OTHER:
			break;
		case SOURCE:
			break;
		case SOURCE_ASM:
			break;
		case SOURCE_C:
			break;
		case SOURCE_CPP:
			break;
		case UTILITY:
			break;
		case SVD:
			svdFile  = effectivePath;
		default:
			break;
		}
	}

	private void addLibrarySourcePaths(ICpFile f) {
		if(f == null)
			return;
		String src =  f.getAttribute(CmsisConstants.SRC);
		
		if(src == null || src.isEmpty())
			return; 
		 
		String[] paths = src.split(";"); //$NON-NLS-1$
		if(paths == null || paths.length == 0)
			return;
		
		for(String p: paths){
			if(p == null || p.isEmpty())
				continue;
			String absPath = f.getAbsolutePath(p);
			String path = CpVariableResolver.insertCmsisRootVariable(absPath);
			libSourcePaths.add(path);
		}
	}

	
	/**
	 * Adds piece of RteComponents.h code for the component
	 * @param c ICpComponent 
	 * @param count number of component instances
	 * @return code string
	 */
	protected void addRteComponentsHCode(ICpComponent c, int count) {
		String code = c.getRteComponentsHCode();
		if(code == null || code.isEmpty()) 
			return ;
		// convert all line endings to unix format
		code = code.replaceAll("\\\\r\\\\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
		int index = code.indexOf(CmsisConstants.pINSTANCEp);
		if(index >= 0) {
			for(int i = 0; i < count; i++) {
				String instance = String.valueOf(i);
				String tmp = code.replaceAll(CmsisConstants.pINSTANCEp, instance);
				rteComponentsH.add(tmp);
			}
		} else {
			rteComponentsH.add(code);
		}
	}

	@Override
	public boolean isAddToProject(ICpFileInfo fi) {
		if(fi == null )
			return false;
		EFileRole role = fi.getRole();
		boolean includeInProject = false; 
		switch(role) {
		case INTERFACE:
		case TEMPLATE:
			return false;
		case CONFIG:
		case COPY:
			includeInProject = true;
		case NONE:
		default:
			break;
		}
		EFileCategory cat = fi.getCategory();
		switch(cat){
		case SOURCE:
		case SOURCE_ASM:
		case SOURCE_C:
		case SOURCE_CPP:
		case LINKER_SCRIPT:
		case LIBRARY: 
		case OBJECT:  
			return true;
		case INCLUDE:
			return false;
		case HEADER:
		default:
			break;
		}
		return includeInProject;
	}
	
	protected String getProjectRelativePath(ICpFile f, String className, String deviceName, int index){
		if(f == null)
			return null;
		String path = CmsisConstants.RTE;
		path += '/';
		if(className != null && !className.isEmpty())
			path += className + '/';
		if(f.isDeviceDependent() && deviceName != null && !deviceName.isEmpty())
			path += Utils.wildCardsToX(deviceName) + '/';
		
		String fileName = Utils.extractFileName(f.getName());
		if(index >= 0) {
			String ext =  Utils.extractFileExtension(fileName);
			fileName = Utils.extractBaseFileName(fileName);
			fileName += "_" + String.valueOf(index);  //$NON-NLS-1$
			if(ext != null) {
				fileName += "." + ext; //$NON-NLS-1$
			}
		}
		return path + fileName;
	}

	/**
	 * Creates memory settings from device information
	 * @param deviceInfo ICpDeviceInfo object
	 */
	public static IMemorySettings createMemorySettings(ICpDeviceInfo deviceInfo) {

		ICpDeviceItem d = deviceInfo.getDevice();
		if(d == null) {
			return null;
		}
		String processorName = deviceInfo.getProcessorName();
		ICpItem props = d.getEffectiveProperties(processorName);
		if(props == null)
			return null;

		Map<String, IAttributes> entries = new TreeMap<String, IAttributes>(); 		

		
		ICpDebugConfiguration dc = deviceInfo.getDebugConfiguration();
		Map<String, ICpMemory> memoryItems = dc.getMemoryItems();
		
		for(Entry<String, ICpMemory> e: memoryItems.entrySet()){
			String id = e.getKey();
			ICpMemory m = e.getValue();
			IAttributes a = new Attributes(m.attributes());
			entries.put(id, a);
			
		}
		
		return new MemorySettings(entries);
	}

	@Override
	public Collection<String> validate() {
		EEvaluationResult res = fModel.getEvaluationResult();
		if(res != EEvaluationResult.FAILED) {
			valid = true;
			return null;
		}
		List<String> errors = new LinkedList<String>();
		Collection<? extends IRteDependencyItem> results = fModel.getDependencyItems();
		for(IRteDependencyItem item : results ){
			String msg = item.getName() + " - " + item.getDescription(); //$NON-NLS-1$
			errors.add(msg);
		}
		valid = false;
		return errors;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	
}
