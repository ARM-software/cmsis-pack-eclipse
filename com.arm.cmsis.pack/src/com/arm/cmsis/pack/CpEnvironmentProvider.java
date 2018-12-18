package com.arm.cmsis.pack;

import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceProperty;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteEventProxy;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.utils.Utils;

/**
 *  Default implementation of ICpEnvironmentProvider interface as "null object"
 */
public class CpEnvironmentProvider extends PlatformObject implements ICpEnvironmentProvider {

	protected IRteEventProxy fRteEventProxy = null;
	protected ICpExampleImporter fExampleImporter = null; 
	
	@Override
	public String getName() {
		return CmsisConstants.EMPTY_STRING;
	}

	
	@Override
	public void init() {
		// default does nothing
	}

	@Override
	public void release() {
		// default does nothing
	}
	
	@Override
	public void handle(RteEvent event) {
		// default ignores RTE events
	}

	@Override
	public void setRteEventProxy(IRteEventProxy rteEventProxy) {
		fRteEventProxy = rteEventProxy;
		if(fRteEventProxy != null) {
			fRteEventProxy.addListener(this);
		}
	}
	
	@Override
	public IRteEventProxy getRteEventProxy() {
		return fRteEventProxy;
	}
	
	@Override
	public boolean isEnvironmentSupported(String envName, EnvironmentContext context) {
		if(envName == null)
			return false;
		
		// default checks only if supplied name is listed in the supported array independently from context
		String[] names = getSupportedNames();
		if(names == null || names.length == 0)
			return false;
		
		for(String name : names) {
			if(name == null)
				continue;
			if(name.equals(envName))
				return true;
		}
		return false;
	}

	@Override
	public boolean isSupported(ICpItem item) {
		if(item == null) 
			return false;
		if(item instanceof ICpExample)
			return isExampleSupported((ICpExample)item); 
		// checks if the item is an environment element  			
		if(CmsisConstants.ENVIRONMENT_TAG.equals(item.getTag())) {
			// check if environment name is supported
			String envName = item.getName(); 
			if(item instanceof ICpDeviceProperty)
				return isEnvironmentSupported(envName, EnvironmentContext.DEVICE);
			return isEnvironmentSupported(envName, EnvironmentContext.EXAMPLE);
		}

		return true;
	}

	@Override
	public boolean isExampleSupported(ICpExample example) {
		// default checks if the example has an environment section that is supported
		if(example == null)
			return false;

		Collection<? extends ICpItem> envs = example.getGrandChildren(CmsisConstants.PROJECT_TAG);
		if (envs == null || envs.isEmpty()) {
			return false;
		}
		for (ICpItem item : envs) {
			if(!item.getTag().equals(CmsisConstants.ENVIRONMENT_TAG))
				continue;
			if(isLoadPathSupported(example, item))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Checks if example's load path is supported  
	 * @param example ICpExample to check 
	 * @param environment ICpItem describing particular environment 
	 * @return true if supported
	 */
	protected boolean isLoadPathSupported(ICpExample example, ICpItem environment) {
		if(example == null || environment == null)
			return false;
		// base implementation only checks environment
		String envName = environment.getName();
		return isEnvironmentSupported(envName, EnvironmentContext.EXAMPLE);
	}


	@Override
	public IAdaptable copyExample(ICpExample example) {
		// default checks if the example is supported and sends import event to the project manager
		if(example == null)
			return null;
		if(!isExampleSupported(example))
			return null;
		
		ICpExampleImporter importer = getImporter(example);
		if(importer == null) 
			return null;
		
		return importer.importExample(example);
	}
	
	@Override
	public String getAbsoluteLoadPath(ICpExample example) {
		if(example == null)
			return null;
		String[] names = getSupportedNames();
		if(names == null || names.length == 0)
			return null;

		// evaluate in the order of priority
		for(String name : names) {
			String loadPath = example.getAbsoluteLoadPath(name);
			if(loadPath != null)
				return loadPath;
		}
		return null;
	}
	

	@Override
	public String getEnvironment(ICpExample example) {
		if(example == null)
			return null;
		String[] names = getSupportedNames();
		if(names == null || names.length == 0)
			return null;

		// evaluate in the order of priority
		for(String name : names) {
			String loadPath = example.getAbsoluteLoadPath(name);
			if(loadPath != null)
				return name;
		}
		return null;	
	}

	
	@Override
	public void adjustBuildSettings(IBuildSettings buildSettings, ICpConfigurationInfo configInfo) {
		if(buildSettings == null || configInfo == null)
			return;
		adjustDeviceBuildSettings(buildSettings, configInfo); 
	}

	@Override
	public void adjustInitialBuildSettings(IBuildSettings buildSettings, ICpConfigurationInfo configInfo) {
		adjustBuildSettings(buildSettings, configInfo);
	}
	
	/**
	 * Adjusts build settings according to device info
	 * @param buildSettings IBuildSettings to adjust
	 * @param deviceInfo ICpDeviceInfo as information source
	 */
	public void adjustDeviceBuildSettings(IBuildSettings buildSettings, ICpConfigurationInfo configInfo) {
		// default adds misc options and pre/postbuild steps if specified 
		
		ICpDeviceInfo deviceInfo = configInfo.getDeviceInfo();
		
		if(deviceInfo == null || deviceInfo.getDevice() == null)
			return; // only update settings for available device
		ICpItem effectiveProperties = deviceInfo.getEffectiveProperties();
		if(effectiveProperties == null)
			return;
		Collection<ICpItem> envs = effectiveProperties.getChildren(CmsisConstants.ENVIRONMENT_TAG);
		if(envs == null)
			return;
		
		for(ICpItem e : envs) {
			if(!isSupported(e))
				continue;
			Collection<? extends ICpItem> options = e.getChildren();
			if(options == null)
				continue;
			for(ICpItem o : options){
				int optionType = getOptionType(o.getTag());
				if(optionType == IBuildSettings.UNKNOWN_OPTION)
					continue;
				
				String value = o.getText();
				if(optionType == IBuildSettings.PRE_BUILD_STEPS || optionType == IBuildSettings.POST_BUILD_STEPS)
					value = expandString(value, configInfo, false);
				buildSettings.addStringListValue(optionType, value); 
			}
		}
	}

	/**
	 * Converts option tag to option type used by build Settings    
	 * @param tag String option type 
	 * @return integer build type
	 */
	protected int getOptionType(String tag) {
		// default solely uses IBuildSettings implementation  
		return IBuildSettings.getMiscOptionType(tag);
	}

	@Override
	public String expandString(String input, ICpConfigurationInfo configInfo, boolean bAsolute) {
		// default converts key sequences (http://www.keil.com/pack/doc/CMSIS/Pack/html/pdsc_generators_pg.html)
		// to Eclipse variables
		if(input == null || input.isEmpty() || configInfo == null)
			return input;
		
		int len = input.length();
		int lastIndex = len - 1;
		StringBuilder output = new StringBuilder(len);
		for(int i = 0 ; i < len; i++) {
			char ch = input.charAt(i);
			if(i < lastIndex && ( ch == '$' || ch == '#' || ch == '@' || ch == '%')) {
				String key = input.substring(i, i + 2);
				String s = bAsolute ? expandToAbsolute(key, configInfo) : expandToVariable(key, configInfo);
				if(s == null || s.equals(key)) {
					if (key.equals("$K")){ //$NON-NLS-1$
						// skip commonly used $K\ARM\BIN\ to empty string
						s = input.substring(i+2).toUpperCase().replace('\\', '/');
						String prefix = CmsisConstants.EMPTY_STRING;
						if(s.startsWith("/")) { //$NON-NLS-1$
							prefix = "/"; //$NON-NLS-1$
						}
						prefix += "ARM/BIN/"; //$NON-NLS-1$
						if(s.startsWith(prefix)) {
							i++; // skip key char
							i += prefix.length(); // skip prefix 
						}
						continue;
					}
				}
				if(s != null) {
					output.append(s);
					i++; // skip key char
					continue;
				} 
			} 
			output.append(ch);
		}
		return output.toString();
	}
	
	
	public String expandToVariable(String key, ICpConfigurationInfo configInfo) {
		switch(key) {
		case "$D":  //$NON-NLS-1$
		case "#D": //$NON-NLS-1$
		case "@D": //$NON-NLS-1$
			return expandToAbsolute(key, configInfo); 
		case "$P": //$NON-NLS-1$
			return CmsisConstants.PROJECT_ABS_PATH;
		case "#P": //$NON-NLS-1$
			return CmsisConstants.PROJECT_ABS_PATH + CmsisConstants.DOT_PROJECT;
		case "@P": //$NON-NLS-1$
		case "%P": //$NON-NLS-1$
			return CmsisConstants.PROJECT_NAME;
		case "$S":  //$NON-NLS-1$
			return CmsisConstants.CMSIS_DFP_VAR_PRJ;
		case "%L": //$NON-NLS-1$
			return CmsisConstants.OUTPUT_FILE;
		case "@L": //$NON-NLS-1$
			return CmsisConstants.OUTPUT_FILE_BASE;
		case "$L": //$NON-NLS-1$
			return CmsisConstants.OUTPUT_PATH;
		case "#L": //$NON-NLS-1$
			return CmsisConstants.OUTPUT_ABS_FILE;
		}
		return key; // do not expand, can be another variable
	}

	public String expandToAbsolute(String key, ICpConfigurationInfo configInfo) {
		if(key == null || key.length() <2)
			return key;
		char ch0 = key.charAt(0);
		char ch1 = key.charAt(1);
		
		if(ch1 == 'D') {
			ICpDeviceInfo di = configInfo.getDeviceInfo();
			String fullDeviceName = di.getFullDeviceName();
			if(ch0 == '$')
				return Utils.wildCardsToX(fullDeviceName);
			
			String dName = fullDeviceName;
			String pName = CmsisConstants.EMPTY_STRING;
			int i = fullDeviceName.indexOf(':');
			if (i >= 0) {
				dName = fullDeviceName.substring(0, i);
				pName = fullDeviceName.substring(i + 1);
			}
			if(ch0 == '#')
				return dName;
			else if(ch0 == '@')
				return pName;
			return key;
		}

		if(ch1 == 'P') {
			if(ch0 == '@' || ch0 == '%')
				return Utils.extractBaseFileName(configInfo.getFileName()); // config base name always corresponds to project name
			String pathName = configInfo.getDir(true);
			if(ch0 == '#')
				pathName += CmsisConstants.DOT_PROJECT;
			return pathName;
		}
		
		if(ch1 == 'S' && ch0 == '$') {
			return configInfo.getDfpPath();
		}
		return key;
	}


	@Override
	public ICpExampleImporter getDefaultImporter() {
		return fExampleImporter;
	}


	@Override
	public void setDefaultImporter(ICpExampleImporter exampleImporter) {
		fExampleImporter = exampleImporter;
	}
	
}
