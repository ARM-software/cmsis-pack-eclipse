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

package com.arm.cmsis.pack.build.armcc5;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import com.arm.cmsis.pack.build.settings.IBuildSettings;
import com.arm.cmsis.pack.build.settings.ILinkerScriptGenerator;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Toolchain adapter for ARMCC 5.x compiler
 *
 */
public class Armcc5ToolChainAdapter extends RteToolChainAdapter {


	static public final String SPACE = " ";  //$NON-NLS-1$
	static public final String QUOTE = "\"";  //$NON-NLS-1$
	static public final String SETA = " SETA ";  //$NON-NLS-1$
	static public final String PD =" --pd \""; //$NON-NLS-1$
	
	static public final String ASM_DEF_RTE = "--pd \"_RTE_ SETA 1\""; //$NON-NLS-1$
	
	
	public Armcc5ToolChainAdapter() {
	}
	
	
	@Override
	public ILinkerScriptGenerator getLinkerScriptGenerator() {
		return new ScatterFileGenerator();
	}

	

	@Override
	protected void updateRteOption(int oType, IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException {

		switch(oType) {	
		case  AMISC_OPTION:
			updateAsmMiscOption(configuration, tool, option, buildSettings);
			return;
		default :
			break;
		}
		super.updateRteOption(oType, configuration, tool, option, buildSettings);
	}


	@Override
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings) {
		switch(oType){
		case CPU_OPTION:
			return getCpuOptionValue(buildSettings);
		case FPU_OPTION:
			return getFpuOptionValue(buildSettings);
		case ENDIAN_OPTION:
			return getEndianOptionValue( buildSettings);
		default:
			break;
		
		}
		return super.getRteOptionValue(oType, buildSettings);
	}


	
	@Override
	protected Collection<String> getStringListValue(IBuildSettings buildSettings, int oType) {
		if(oType == IOption.LIBRARY_PATHS)
			return null; //paths are not needed as libs are absolute  
		return super.getStringListValue(buildSettings, oType);
	}


	protected String getEndianOptionValue(IBuildSettings buildSettings) {

		String endian = getDeviceAttribute(ENDIAN_OPTION, buildSettings);
		String val = null;
		if(endian == null || endian.isEmpty()) { 
			val = "com.arm.tool.c.compiler.option.endian.auto"; //$NON-NLS-1$
		} else if(endian.equals(CmsisConstants.LITTLENDIAN)) {
			val = "com.arm.tool.c.compiler.option.endian.little"; //$NON-NLS-1$
		}else if(endian.equals(CmsisConstants.BIGENDIAN)) {
			val = "com.arm.tool.c.compiler.option.endian.big"; //$NON-NLS-1$
		} else {
			val = "com.arm.tool.c.compiler.option.endian.auto"; //$NON-NLS-1$
		}
		return val;
	}


	@Override
	protected int getRteOptionType(String id) {
		switch(id){
		case "com.arm.tool.c.compiler.option.targetcpu": //$NON-NLS-1$
		case "com.arm.tool.assembler.option.cpu": //$NON-NLS-1$
		case "com.arm.tool.c.linker.option.cpu": //$NON-NLS-1$
			return CPU_OPTION; 
		case "com.arm.tool.c.compiler.option.targetfpu": //$NON-NLS-1$
		case "com.arm.tool.assembler.option.fpu": //$NON-NLS-1$
		case "com.arm.tool.c.linker.option.fpu": //$NON-NLS-1$
			return FPU_OPTION; 
		case "com.arm.tool.c.compiler.option.endian": //$NON-NLS-1$
		case "com.arm.tool.assembler.option.endian": //$NON-NLS-1$
			return ENDIAN_OPTION;
		case "com.arm.tool.c.compiler.option.flags": //$NON-NLS-1$
			return CMISC_OPTION;
		case "com.arm.tool.assembler.option.flags": //$NON-NLS-1$
			return AMISC_OPTION;
		case "com.arm.tool.c.linker.option.flags": //$NON-NLS-1$
			return LMISC_OPTION;
		case "com.arm.tool.librarion.options.misc": //$NON-NLS-1$
			return ARMISC_OPTION;
		case "com.arm.tool.c.linker.option.scatter":  //$NON-NLS-1$
			return LINKER_SCRIPT_OPTION;	
		default:
			break;
		}
		return UNKNOWN_OPTION;
	}

	/**
	 * Updates assembler other flags option to set macro defines 
	 * @param configuration
	 * @param tool
	 * @param option
	 * @param buildSettings
	 * @throws BuildException
	 */
	protected void updateAsmMiscOption(IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException{
		String  value = option.getStringValue();
		
		int pos = value.indexOf(ASM_DEF_RTE);
		if(pos >=0 )
			value = value.substring(0,  pos);
		if(!value.isEmpty() && !value.endsWith(SPACE))
			value += SPACE; 
		value += ASM_DEF_RTE;

		Collection<String> defines = buildSettings.getStringListValue(IOption.PREPROCESSOR_SYMBOLS);
		if(defines != null) {
			for(String d : defines) {
				value += getAsmDefString(d);
			}
		}		

		ManagedBuildManager.setOption(configuration, tool, option, value);
	}
	
	
	protected String getAsmDefString(String d) {
		String val = "1"; //$NON-NLS-1$
		String s = PD;
		int pos = d.indexOf('=');
		if(pos > 0) {
			val = d.substring(pos + 1);
			s +=  d.substring(0, pos); 
		} else {
			s +=  d;
		}
		s += SETA;
		s += val;
		s += QUOTE;
		return s;
	}

	/**
	 * Constructs value for --cpu option
	 * @param buildSettings
	 * @return
	 */
	protected String getCpuOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(FPU_OPTION, buildSettings);
		String fpuSuffix = getFpuSuffix(cpu, fpu);
		
		if(fpuSuffix != null && !fpuSuffix.isEmpty()){
			cpu += '.' + fpuSuffix;
		}
		return cpu;
	}

	public String getFpuSuffix(String cpu, String fpu) {
		if(cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) 
			return null;
		String suffix = "fp"; //$NON-NLS-1$
		
		if(cpu.equals("Cortex-M7")) { //$NON-NLS-1$
		 if(fpu.equals(CmsisConstants.SP_FPU))
			 suffix += ".sp"; //$NON-NLS-1$
		 else if(fpu.equals(CmsisConstants.DP_FPU))
			 suffix += ".dp"; //$NON-NLS-1$
		} 
		return suffix;
	}

	public String getFpuOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(FPU_OPTION, buildSettings);
		if(cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) 
			return CmsisConstants.EMPTY_STRING;
		if(cpu.equals("Cortex-M7") && fpu.equals(CmsisConstants.SP_FPU)) { //$NON-NLS-1$
			return "FPv4-SP"; //$NON-NLS-1$
		} 
		return null;
	}
	
}
