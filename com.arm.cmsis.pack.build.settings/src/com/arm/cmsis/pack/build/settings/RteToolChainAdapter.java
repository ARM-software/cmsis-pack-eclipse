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

package com.arm.cmsis.pack.build.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.Utils;


/**
 * Generic toolchain adapter implementation
 */
public class RteToolChainAdapter implements IRteToolChainAdapter {
	
	protected boolean bInitialUpdate = false; 

	@Override
	public ILinkerScriptGenerator getLinkerScriptGenerator() {
		// generic adapter has no linker script generator
		return null;
	}

	
	@Override
	public void setToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {

		if(configuration == null || buildSettings == null)
			return;
		IToolChain toolchain = configuration.getToolChain();
		if(toolchain == null)
			return;
		// iterate over toolchain options
		updateOptions(configuration, toolchain, buildSettings);
		
		// iterate over tools
		ITool[] tools = toolchain.getTools();
		for(ITool t : tools) {
			if(t == null || !t.isEnabled())
				continue;
			updateOptions(configuration, t, buildSettings);
		}
	}
	
	@Override
	public void setInitialToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {
		// default updates all options
		bInitialUpdate = true;
		setToolChainOptions(configuration, buildSettings);
		bInitialUpdate = false;
	}
	
	
	/**
	 * Updates tollchain/tool options for given configuration
	 * @param configuration option's parent IConfiguration 
	 * @param tool IHoldsOptions representing ITool or IToolChain 
	 * @param buildSettings IBuildSettings containing source RTE information
	 */
	protected void updateOptions(IConfiguration configuration, IHoldsOptions tool, IBuildSettings buildSettings) {
		IOption[] options = tool.getOptions();
		for(IOption o : options) {
			try {
				if(o != null)
					updateOption(configuration, tool, o, buildSettings);
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates an option that contains preprocessor defines, libraries, include paths or library paths .<br>
	 * Removes all options after <code>_RTE_</code> and adds new defines.  
	 * @param configuration option's parent IConfiguration 
	 * @param tool option's parent IHoldsOptions (ITool or IToolChain )
	 * @param option IOption to update 
	 * @param buildSettings IBuildSettings containing source RTE information 
	 * @throws BuildException
	 */
	protected void updateOption(IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException {
		int oType = getOptionType(option);
		if(!bInitialUpdate && isInitialOption(oType))
			return; // initial update only 
		
		if(oType > RTE_OPTION ) {
			updateRteOption(oType, configuration, tool, option, buildSettings);
		} else {
			// the rest updates include paths, defines and libraries
			setStringListOptionValue(oType, configuration, tool, option, buildSettings);
		}
	}

	/**
	 * Updates device-related option value : CPU, FPU and or Endian 
	 * @param oType option's device type : CPU, FPU and or ENDIAN 
	 * @param configuration option's parent IConfiguration 
	 * @param tool option's parent IHoldsOptions
	 * @param option IOption to update
	 * @param buildSettings IBuildSettings containing source RTE information 
	 * @throws BuildException
	 */
	protected void updateRteOption(int oType, IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException {
		int type = option.getBasicValueType();
		
		if(type == IOption.STRING_LIST) {
			setStringListOptionValue(oType, configuration, tool, option, buildSettings);
			return;
		}
		
		String value = getRteOptionValue(oType, buildSettings);
		if(value != null) {
			if(type == IOption.BOOLEAN) {
				boolean bVal = value.equals("1") || value.equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$
				ManagedBuildManager.setOption(configuration, tool, option, bVal);
			} else {
				ManagedBuildManager.setOption(configuration, tool, option, value);
			}
		}
	}
	

	
	/**
	 * Returns RTE values for  
	 * @param buildSettings IBuildSettings to get value from
	 * @param type option type returned by getOptionType(IOption option)
	 * @return collection of strings for the option
	 */
	protected Collection<String> getStringListValue(IBuildSettings buildSettings, int type) {
		return buildSettings.getStringListValue(type);
	}
	
	
	/**
	 * Updates device-related option value : CPU, FPU and or Endian 
	 * @param oType option's device type : CPU, FPU and or ENDIAN 
	 * @param configuration option's parent IConfiguration 
	 * @param tool option's parent IHoldsOptions
	 * @param option IOption to update 
	 * @throws BuildException
	 */
	protected void setStringListOptionValue(int oType, IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException{
		
		if(option.getBasicValueType() != IOption.STRING_LIST)
			return;
		
		List<String> value = getStringList(option);
		if(value == null){
			return;
		}
		switch(oType){
		case IOption.PREPROCESSOR_SYMBOLS: 
			value = truncateStringList(value, CmsisConstants._RTE_);
			value.add(CmsisConstants._RTE_);
			break;
		case IOption.INCLUDE_PATH: 
		case IOption.LIBRARY_PATHS: 
		case IOption.LIBRARIES:
		case IOption.OBJECTS:
		case IRteToolChainAdapter.LINKER_SCRIPT_OPTION:
			value = removeRtePathEntries(value);
			break;
		default:
			return;
		}

		Collection<String> newValue = getStringListValue(buildSettings, oType);
		if(newValue != null)
			value.addAll(newValue);	
		// copy to array and add quotes if needed 
		String[] arrayValue = new String[value.size()];
		int i = 0;
		for(String s : value) {
			if(oType == IOption.PREPROCESSOR_SYMBOLS)
				arrayValue[i] = s;
			else
				arrayValue[i] = Utils.addQuotes(s);
			i++;
		}
		
		ManagedBuildManager.setOption(configuration, tool, option, arrayValue);
	}

	
	/**
	 * Checks if specified option type is for initial setting only 
	 * @param oType option type
	 * @return true if option is an initial option
	 */
	protected boolean isInitialOption(int oType) {
		return oType > RTE_DEVICE_OPTION;
	}


	/**
	 * Retrieves string list value form IOption
	 * @param option option from which to retrieve string list 
	 * @return string list or null if there is no value for this option   
	 * @throws BuildException
	 */
	protected List<String> getStringList(IOption option) throws BuildException{
		String[] array = null;
		int type = option.getValueType();
		switch(type){
		case IOption.PREPROCESSOR_SYMBOLS: 
			array = option.getDefinedSymbols();
			break;
		case IOption.INCLUDE_PATH: 
			array = option.getIncludePaths();
			break;
		case IOption.LIBRARY_PATHS: 
			array = option.getLibraryPaths();
			break;
		case IOption.LIBRARIES: 
			array = option.getLibraries();
			break;
		case IOption.OBJECTS: 
			array = option.getUserObjects();
			break;
		case IOption.STRING_LIST:			
			array = option.getStringListValue();
			break;
		default:
			break;
		}
		if(array == null)
			return null;
		return new ArrayList<String>(Arrays.asList(array)); 
	}

	
	/**
	 * Removes all entries in the list after truncateFrom string (inclusive truncateFrom entry) 
	 * @param strings list of strings to truncate
	 * @param truncateFrom
	 * @return updated list
	 */
	static public List<String> truncateStringList(List<String> strings, String truncateFrom) {
		if(strings == null)
			return null;
		int index = strings.indexOf(truncateFrom);
		if(index >=0)
			return strings.subList(0, index);
		return strings;
	}

	/**
	 * Removes all entries beginning with RTE or ${cmsis_pack_root} paths from supplied list  
	 * @param paths list of paths/files to process  
	 * @return updated list
	 */
	static public List<String> removeRtePathEntries(List<String> paths) {

		for (Iterator<String> iterator = paths.iterator(); iterator.hasNext();) {
			String s = iterator.next();
			if(s.startsWith(CmsisConstants.PROJECT_RTE_PATH, 1) || 
			   s.startsWith(CmsisConstants.CMSIS_PACK_ROOT_VAR, 1)) {
					iterator.remove();
			}
		}	
		return paths;
	}
	
	/**
	 * Returns if CPU can have FPU
	 * @param cpu core name
	 * @return true
	 */
	public boolean coreHasFpu(String cpu) {
		if(cpu == null)
			return false;
		switch(cpu) {
		  default:
		  case "SC000": 		//$NON-NLS-1$
	      case "SC300": 		//$NON-NLS-1$
	      case "Cortex-M0": 	//$NON-NLS-1$
	      case "Cortex-M0+": 	//$NON-NLS-1$
	      case "Cortex-M1": 	//$NON-NLS-1$
	      case "Cortex-M3": 	//$NON-NLS-1$
	    	  return false;
	      case "Cortex-M4": 	//$NON-NLS-1$
	      case "Cortex-M7": 	//$NON-NLS-1$
	      case "Cortex-R4": 	//$NON-NLS-1$
	      case "Cortex-R5": 	//$NON-NLS-1$
	      case "Cortex-A5": 	//$NON-NLS-1$
	      case "Cortex-A8": 	//$NON-NLS-1$
	      case "Cortex-A9": 	//$NON-NLS-1$
	      case "Cortex-A15":	//$NON-NLS-1$ 
	    	  return true; 
		}
	}

	/**
	 * Return option type: CPU, FPU, ENDIAN, etc.  
	 * @param option IOption to get type
	 * @return positive integer  if it is a device-related option, 0 otherwise 
	 */
	public int getOptionType(IOption option) {
		if(option == null)
			return UNKNOWN_OPTION;
		
		for(IOption o = option; o != null; o = o.getSuperClass()) {
			String id = o.getId();
			int rteType = getRteOptionType(id);
			if( rteType > RTE_OPTION )
				return rteType;
			id = o.getBaseId();
			rteType = getRteOptionType(id);
			if( rteType > RTE_OPTION )
				return rteType;
		}
		try {
			return option.getValueType();
		} catch (BuildException e) {
			e.printStackTrace();
		}
		return UNKNOWN_OPTION;
	}

	/**
	 * Returns device option type for specified option ID
	 * @param id ID or base ID to check
	 * @return positive integer  if it is a device-related option, 0 otherwise 
	 */
	protected int getRteOptionType(String id) {
		return UNKNOWN_OPTION; // default does not know 
	}

	
	/**
	 * Returns option value for given option type 
	 * @return option value string  
	 */
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings) {
		if(oType > RTE_OPTION) {
			switch(oType) {
			case LINKER_SCRIPT_OPTION:
				return getLinkerSrciptOptionValue(buildSettings);
			default:
				// default simply returns device attribute value
				return getDeviceAttribute(oType, buildSettings);
			}
		}
		return null;
	}

	/**
	 * Returns device attribute for given option type
	 * @return CPU option string 
	 */
	protected String getDeviceAttribute(int oType, IBuildSettings buildSettings) {
		switch(oType) {
		case CPU_OPTION:
			return buildSettings.getDeviceAttribute("Dcore"); //$NON-NLS-1$
		case FPU_OPTION:
			return buildSettings.getDeviceAttribute("Dfpu"); //$NON-NLS-1$
		case ENDIAN_OPTION:
			return buildSettings.getDeviceAttribute("Dendian"); //$NON-NLS-1$
		default:
			break;
		}
		return null;
	}

	/**
	 * Returns single linker script file if it is only one 
	 * @param buildSettings IBuildSettings to get value from
	 * @return single linker script file or null 
	 */
	protected String getLinkerSrciptOptionValue(IBuildSettings buildSettings) {
		return Utils.addQuotes(buildSettings.getSingleLinkerScriptFile());
	}
	
}
