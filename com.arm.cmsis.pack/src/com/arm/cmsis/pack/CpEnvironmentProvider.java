package com.arm.cmsis.pack;

import java.util.Collection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceProperty;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.utils.Utils;

/**
 *  Default implementation of ICpEnvironmentProvider interface as "null object"
 */
public class CpEnvironmentProvider extends PlatformObject implements ICpEnvironmentProvider {

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
	public ICpPackRootProvider getCmsisRootProvider() {
		return null; // default has no root provider
	}

	@Override
	public void handle(RteEvent event) {
		// default ignores RTE events
	}

	
	@Override
	public boolean isEnvironmentSupported(String name, EnvironmentContext context) {
		if(name == null)
			return false;
		
		// default checks only for this environment independently from context
		String thisName = getName();
		if(thisName == null || thisName.isEmpty())
			return false;
		return thisName.equals(name);   
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

	/**
	 * Checks if given example is supported by this provider and can be instantiated.    
	 * @param example ICpExample to check
	 * @return true if supported
	 */
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
			if(isSupported(item))
				return true;
		}
		return false;
	}
	
	
	@Override
	public IAdaptable copyExample(ICpExample example) {
		// default calls ICpPackInstaller to copy the example
		ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
		if(packInstaller == null)
			return null;
		return packInstaller.copyExample(example);
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
		switch(tag){
		case IBuildSettings.AMISC_TAG:
			return IBuildSettings.RTE_ASMMISC;
		case IBuildSettings.CMISC_TAG: 
			return IBuildSettings.RTE_CMISC;
		case IBuildSettings.CPPMISC_TAG:
			return IBuildSettings.RTE_CPPMISC;
		case IBuildSettings.LMISC_TAG:
			return IBuildSettings.RTE_LMISC;
		case IBuildSettings.ARMISC_TAG:
			return IBuildSettings.RTE_ARMISC;
		default:
			break;
		}
		// pre and post build steps in packs are often given with a suffix like preBuild1, prebuild2, etc.
		if(tag.startsWith(IBuildSettings.PRE_BUILD_TAG))
			return IBuildSettings.PRE_BUILD_STEPS;
		else if(tag.startsWith(IBuildSettings.POST_BUILD_TAG))
			return IBuildSettings.POST_BUILD_STEPS;
		
		return IBuildSettings.UNKNOWN_OPTION;
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
			if(i < lastIndex && ( ch == '$' || ch == '#' || ch == '@')) {
				String key = input.substring(i, i + 2);
				String s = bAsolute ? expandToAbsolute(key, configInfo) : expandToVariable(key, configInfo);
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
			return CmsisConstants.PROJECT_ABS_PATH + ".project"; //$NON-NLS-1$
		case "$S":  //$NON-NLS-1$
			return CmsisConstants.CMSIS_DFP_VAR;
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
			String fullDeviceName = di.getDeviceName();
			if(ch1 == '$')
				return fullDeviceName;
			
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
			String fileName = configInfo.getFileName();
			String pathName = Utils.extractPath(fileName, true);
			if(ch0 == '#')
				pathName += ".project"; //$NON-NLS-1$
			return pathName;
		}
		
		if(ch1 == 'S' && ch0 == '$') {
			return configInfo.getDfpPath();
		}
		return key;
	}
}
