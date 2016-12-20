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
import org.eclipse.core.runtime.PlatformObject;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.Utils;


/**
 * Generic toolchain adapter implementation, uses PlatformObject's implementation of IAdaptable interface 
 */
public class RteToolChainAdapter extends PlatformObject implements IRteToolChainAdapter {

	protected boolean bInitialUpdate = false;

	@Override
	public ILinkerScriptGenerator getLinkerScriptGenerator() {
		// generic adapter has no linker script generator
		return null;
	}


	@Override
	public void setToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {

		if(configuration == null || buildSettings == null) {
			return;
		}
		
		if(bInitialUpdate) {
			updateBuildSteps(configuration, buildSettings, IBuildSettings.PRE_BUILD_STEPS);
			updateBuildSteps(configuration, buildSettings, IBuildSettings.POST_BUILD_STEPS);
		}
		
		IToolChain toolchain = configuration.getToolChain();
		if(toolchain == null) {
			return;
		}
		// iterate over toolchain options
		updateOptions(configuration, toolchain, buildSettings);

		// iterate over tools
		ITool[] tools = toolchain.getTools();
		for(ITool t : tools) {
			if(t == null || !t.isEnabled()) {
				continue;
			}
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
				if(o != null) {
					updateOption(configuration, tool, o, buildSettings);
				}
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
		if(!bInitialUpdate && isInitialOption(oType)) {
			return; // initial update only
		}

		if(oType > IBuildSettings.RTE_OPTION ) {
			updateRteOption(oType, configuration, tool, option, buildSettings);
		}
	}

	/**
	 * Updates RTE option value
	 * @param oType option's type : see getRteOptionType()
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

		String value = getRteOptionValue(oType, buildSettings, option);
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
	 * Updates string list option values
	 * @param oType option's extended type: see getOptionType()
	 * @param configuration option's parent IConfiguration
	 * @param tool option's parent IHoldsOptions
	 * @param option IOption to update
	 * @throws BuildException
	 */
	protected void setStringListOptionValue(int oType, IConfiguration configuration, IHoldsOptions tool, IOption option,
			IBuildSettings buildSettings) throws BuildException {

		if (option.getBasicValueType() != IOption.STRING_LIST) {
			return;
		}

		List<String> value = getCurrentStringListValue(option);
		if (value == null) {
			return;
		}
		value = cleanStringList(value, oType);
		Collection<String> newValue = getStringListValue(buildSettings, oType);
		if (newValue != null) {
			value.addAll(newValue);
		}
		// copy to array and add quotes if needed
		String[] arrayValue = new String[value.size()];
		int i = 0;
		for (String s : value) {
			if (isToQuoteOption(oType, option)) {
				arrayValue[i] = Utils.addQuotes(s);
			} else {
				arrayValue[i] = s;
			}
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
		return oType > IBuildSettings.RTE_INITIAL_OPTION; 
	}

	/**
	 * Checks if option value shout be quoted
	 * @param oType option's extended type: see getOptionType()
	 * @param option IOption to check 
	 * @return true if surround with quotes
	 */
	protected boolean isToQuoteOption(int oType, IOption option) {
		if(oType == IBuildSettings.RTE_LINKER_SCRIPT)
			return true;
		int valueType;
		try {
			valueType = option.getValueType();
		} catch (BuildException e) {
			e.printStackTrace();
			return false;
		}
		switch(valueType){
		case IOption.INCLUDE_PATH:
		case IOption.LIBRARY_PATHS:
		case IOption.LIBRARIES:
		case IOption.OBJECTS:
			return true;
		case IOption.PREPROCESSOR_SYMBOLS:
		default:
			return false;
		}
	}

	/**
	 * Retrieves current string list value form IOption
	 * @param option option from which to retrieve string list
	 * @return string list or null if there is no value for this option
	 * @throws BuildException
	 */
	protected List<String> getCurrentStringListValue(IOption option) throws BuildException {
		String[] array = null;
		int type = option.getValueType();
		switch (type) {
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
		if(array == null) {
			return null;
		}
		return new ArrayList<String>(Arrays.asList(array));
	}

	/**
	 * Returns current string value stored by option of STRING base type
	 * @param option option to get value from
	 * @return option value as String, null if option base type is not STRING 
	 */
	public String getCurrentStringValue(IOption option) {
			try {
				int type = option.getBasicValueType();
				if(type == IOption.STRING)
					return option.getStringValue();
			} catch (BuildException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	/**
	 * Removes entries from a string list for given option
	 * @param value list of strings to clean
	 * @param oType option's extended type: see getOptionType()
	 */
	protected List<String> cleanStringList(List<String> value, int oType) {
		switch(oType){
		case IBuildSettings.RTE_DEFINES:
			value = truncateStringList(value, CmsisConstants._RTE_);
			break;
		case IBuildSettings.RTE_INCLUDE_PATH:
		case IBuildSettings.RTE_LIBRARY_PATHS:
		case IBuildSettings.RTE_LIBRARIES:
		case IBuildSettings.RTE_OBJECTS:
		case IBuildSettings.RTE_LINKER_SCRIPT:
			value = removeRtePathEntries(value);
			break;
		default:
			break;
		}
		return value;
	}

	/**
	 * Removes all entries in the list after truncateFrom string (inclusive truncateFrom entry)
	 * @param strings list of strings to truncate
	 * @param truncateFrom
	 * @return updated list
	 */
	static public List<String> truncateStringList(List<String> strings, String truncateFrom) {
		if(strings == null) {
			return null;
		}
		int index = strings.indexOf(truncateFrom);
		if(index >=0) {
			return strings.subList(0, index);
		}
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
					s.startsWith(CmsisConstants.CMSIS_PACK_ROOT_VAR, 1) ||
					s.startsWith(CmsisConstants.CMSIS_RTE_VAR, 1)	) {
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
		if(cpu == null) {
			return false;
		}
		switch(cpu) {
		case "SC000": 		//$NON-NLS-1$
		case "SC300": 		//$NON-NLS-1$
		case "Cortex-M0": 	//$NON-NLS-1$
		case "Cortex-M0+": 	//$NON-NLS-1$
		case "Cortex-M1": 	//$NON-NLS-1$
		case "Cortex-M3": 	//$NON-NLS-1$
		case "ARMV8MBL": 	//$NON-NLS-1$
			return false;
		default:
		case "Cortex-M4": 	//$NON-NLS-1$
		case "Cortex-M7": 	//$NON-NLS-1$
		case "Cortex-R4": 	//$NON-NLS-1$
		case "Cortex-R5": 	//$NON-NLS-1$
		case "Cortex-A5": 	//$NON-NLS-1$
		case "Cortex-A7": 	//$NON-NLS-1$
		case "Cortex-A8": 	//$NON-NLS-1$
		case "Cortex-A9": 	//$NON-NLS-1$
		case "Cortex-A15":	//$NON-NLS-1$
		case "Cortex-A17":	//$NON-NLS-1$
		case "Cortex-A53":	//$NON-NLS-1$
		case "Cortex-A57":	//$NON-NLS-1$
		case "Cortex-A72":	//$NON-NLS-1$
		case "ARMV8MML": 	//$NON-NLS-1$			
			return true;
		}
	}

	/**
	 * Return option type: base or RTE one
	 * @param option IOption to get type
	 * @return positive integer if it is a known option, -1 otherwise
	 */
	public int getOptionType(IOption option) {
		if(option == null) {
			return IBuildSettings.UNKNOWN_OPTION;
		}

		for(IOption o = option; o != null; o = o.getSuperClass()) {
			String id = o.getId();
			int rteType = getRteOptionType(id);
			if( rteType > IBuildSettings.RTE_OPTION ) {
				return rteType;
			}
			id = o.getBaseId();
			rteType = getRteOptionType(id);
			if( rteType > IBuildSettings.RTE_OPTION ) {
				return rteType;
			}
		}
		try {
			return getOptionType(option.getValueType());
		} catch (BuildException e) {
			e.printStackTrace();
		}
		return IBuildSettings.UNKNOWN_OPTION;
	}

	/**
	 * Returns device option type for specified option ID
	 * @param id ID or base ID to check
	 * @return positive integer  if it is a device-related option, 0 otherwise
	 */
	protected int getRteOptionType(String id) {
		// default has no idea about specific option IDs
		return IBuildSettings.UNKNOWN_OPTION; 
	}

	protected int getOptionType(int valueType) {
		// default converts some base types to RTE ones
		switch(valueType){
		case IOption.PREPROCESSOR_SYMBOLS:
			return IBuildSettings.RTE_DEFINES;
		case IOption.INCLUDE_PATH:
			return IBuildSettings.RTE_INCLUDE_PATH;
		case IOption.LIBRARIES:
			return IBuildSettings.RTE_LIBRARIES;
		case IOption.LIBRARY_PATHS:
			return IBuildSettings.RTE_LIBRARY_PATHS;
		case IOption.OBJECTS:
			return IBuildSettings.RTE_OBJECTS;
		default:
			break;
		}
		return valueType; 
	}

	/**
	 * Returns option value for given option type
	 * @param oType RTE option type
	 * @param buildSettings IBuildSettings containing source RTE information 
	 * @param option IOption for which to get new value 
	 * @return new option value as a string
	 */
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings, IOption option) {
		if(oType > IBuildSettings.RTE_OPTION) {
			switch(oType) {
			case IBuildSettings.RTE_LINKER_SCRIPT:
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
		case IBuildSettings.CPU_OPTION:
			return buildSettings.getDeviceAttribute("Dcore"); //$NON-NLS-1$
		case IBuildSettings.FPU_OPTION:
			return buildSettings.getDeviceAttribute("Dfpu"); //$NON-NLS-1$
		case IBuildSettings.ENDIAN_OPTION:
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
		return buildSettings.getSingleLinkerScriptFile();
	}

	/**
	 * Updates  pre- or post-build command for given configuration  
	 * @param configuration destination IConfiguration to set steps  to
	 * @param buildSettings source IBuildSettings
	 * @param oType option type: PRE_BUILD_STEPS or POST_BUILD_STEPS
	 */
	protected void updateBuildSteps(IConfiguration configuration, IBuildSettings buildSettings, int oType) {
		
		String step;
		if(oType == IBuildSettings.PRE_BUILD_STEPS)
			step = configuration.getPrebuildStep();
		else if(oType == IBuildSettings.POST_BUILD_STEPS)
			step = configuration.getPostbuildStep();
		else 
			return;

		if(step == null) // actually should not happen, but do not rely on CDT 
			step = CmsisConstants.EMPTY_STRING; 
		
		// find begin and end markers
		int beginPos = step.indexOf(CmsisConstants.CMSIS_RTE_BEGIN_VAR);
		if(beginPos < 0)
			beginPos = step.length();
		int endPos = step.indexOf(CmsisConstants.CMSIS_RTE_END_VAR);
		if(endPos < 0)
			endPos = step.length();
		else { 
			if(beginPos > endPos)
				beginPos = endPos; // a marker has been removed by user
			endPos += CmsisConstants.CMSIS_RTE_END_VAR.length();
		}		

		String prefix = (beginPos > 0) ? step.substring(0, beginPos) : CmsisConstants.EMPTY_STRING;
		String suffix = (endPos >= 0) ? step.substring(endPos) : CmsisConstants.EMPTY_STRING;

		String rteCommand = getPrePostCommand(buildSettings, oType);

		String newStep = CmsisConstants.EMPTY_STRING;
		if(!prefix.isEmpty()) {
			if(prefix.endsWith(";")) //$NON-NLS-1$
				prefix = prefix.substring(0,  prefix.length() - 1);
			newStep += prefix;
		}
		
		if(rteCommand != null && !rteCommand.isEmpty()) {
			if(!newStep.isEmpty() && !newStep.endsWith(";")) //$NON-NLS-1$
				newStep += ';'; 
			newStep += rteCommand;
		}

		if(!suffix.isEmpty()) {
			if(!newStep.isEmpty() && !newStep.endsWith(";") && !suffix.startsWith(";")) //$NON-NLS-1$ //$NON-NLS-2$
				newStep += ';'; 
			newStep += suffix;
		}
		
		if(step.equals(newStep))
			return; // nothing to do

		if(oType == IBuildSettings.PRE_BUILD_STEPS)
			configuration.setPrebuildStep(newStep);
		else if(oType == IBuildSettings.POST_BUILD_STEPS)
			configuration.setPostbuildStep(newStep);
	}

	/**
	 * Returns assembled command of build steps
	 * @param buildSettings source IBuildSettings
	 * @param oType option type: PRE_BUILD_STEPS or POST_BUILD_STEPS
	 * @return String containing assembled command of build steps 
	 */
	protected String getPrePostCommand(IBuildSettings buildSettings, int oType) {
		Collection<String> steps = buildSettings.getStringListValue(oType);
		if(steps == null || steps.isEmpty())
			return CmsisConstants.EMPTY_STRING;
		String cmd = CmsisConstants.CMSIS_RTE_BEGIN_VAR;
		for(String s : steps) {
			if(cmd.length() > CmsisConstants.CMSIS_RTE_BEGIN_VAR.length())
				cmd += ';';
			cmd += s; 
		}
		cmd += CmsisConstants.CMSIS_RTE_END_VAR;
		return cmd;
	}

}
