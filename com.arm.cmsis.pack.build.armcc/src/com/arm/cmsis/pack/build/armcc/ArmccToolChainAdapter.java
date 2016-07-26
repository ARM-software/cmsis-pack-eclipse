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

package com.arm.cmsis.pack.build.armcc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.build.settings.ILinkerScriptGenerator;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Toolchain adapter for ARMCC 5.x compiler
 *
 */
public class ArmccToolChainAdapter extends RteToolChainAdapter {
	
	static public final int ARMCC5_ASMDEFINES_OPTION = IBuildSettings.RTE_USER_OPTION + 1;
	static public final int ARMCC_ENABLE_TOOL_SPECIFIC_OPTION = IBuildSettings.TOOLCHAIN_USER_OPTION + 1;
	static public final int ARMCC_USE_MICROLIB 		 = IBuildSettings.TOOLCHAIN_USER_OPTION + 2;
	static public final int CPU_FPU_OPTION 			 = IBuildSettings.TOOLCHAIN_USER_OPTION + 5;
	
	static public final String SPACE = " ";  //$NON-NLS-1$
	static public final String QUOTE = "\"";  //$NON-NLS-1$
	static public final String NoFPU = "NoFPU";  //$NON-NLS-1$
	static public final String SETA = " SETA ";  //$NON-NLS-1$

	static public final String AUTO = "auto"; //$NON-NLS-1$
	static public final String LITTLE = "little"; //$NON-NLS-1$
	static public final String BIG = "big"; //$NON-NLS-1$
	
	static protected final String ENDIAN_PREFICS_5 = "com.arm.tool.c.compiler.option.endian."; //$NON-NLS-1$
	static protected final String ENDIAN_PREFICS_6 = "com.arm.tool.c.compiler.v6.base.option.endian."; //$NON-NLS-1$
	
	protected int compilerVersion = 5; // major compiler version : 5 or 6

	public ArmccToolChainAdapter() {
	}

	@Override
	public void setToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {
		if(configuration == null)
			return;
		//String baseID =configuration.getBaseId();
		IToolChain toolChain = configuration.getToolChain();
		if(toolChain == null)
			return;
		String baseID = toolChain.getBaseId();
		if(baseID.startsWith("com.arm.toolchain.v6")) //$NON-NLS-1$
			compilerVersion = 6;
		
		super.setToolChainOptions(configuration, buildSettings);
	}


	@Override
	public ILinkerScriptGenerator getLinkerScriptGenerator() {
		return new ScatterFileGenerator();
	}


	@Override
	protected void updateRteOption(int oType, IConfiguration configuration, IHoldsOptions tool, IOption option, IBuildSettings buildSettings) throws BuildException {
		switch(oType) {
		// we add libraries with absolute paths => ignore lib paths
		case  IBuildSettings.RTE_LIBRARY_PATHS:
			return; // NO UPDATE OF THAT
		default :
			break;
		}
		super.updateRteOption(oType, configuration, tool, option, buildSettings);
	}

	@Override
	protected Collection<String> getStringListValue(IBuildSettings buildSettings, int oType) {
		if (oType == IBuildSettings.RTE_LIBRARY_PATHS) {
			return null; // we add libraries with absolute paths => ignore lib paths
		} else if (oType == ARMCC5_ASMDEFINES_OPTION) {
			List<String> asmDefines = new LinkedList<String>();
			Collection<String> defines = buildSettings.getStringListValue(IBuildSettings.RTE_DEFINES);
			if(defines != null) {
				for(String d : defines) {
					String value = getAsmDefString(d);
					asmDefines.add(value); 
				}
			}
			return asmDefines; // paths are not needed as libs are absolute
		}  		
		return super.getStringListValue(buildSettings, oType);
	}	
	
	@Override
	protected List<String> cleanStringList(List<String> value, int oType) {
		switch(oType){
		case ARMCC5_ASMDEFINES_OPTION:
		case IBuildSettings.RTE_DEFINES:
		case IBuildSettings.RTE_INCLUDE_PATH:
		case IBuildSettings.RTE_LIBRARIES:
		case IBuildSettings.RTE_CMISC:
		case IBuildSettings.RTE_ASMMISC:
		case IBuildSettings.RTE_LMISC:
			value.clear();
			break;
		case IBuildSettings.RTE_LIBRARY_PATHS:
		default:
			break;
		}
		return value; // do nothing for all other lists
	}

	
	
	protected String getAsmDefString(String d) {
		String val = "1"; //$NON-NLS-1$
		String s = CmsisConstants.EMPTY_STRING;
		int pos = d.indexOf('=');
		if(pos > 0) {
			val = d.substring(pos + 1);
			s +=  d.substring(0, pos);
		} else {
			s +=  d;
		}
		s += SETA;
		s += val;
		return s;
	}
	
	
	@Override
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings, IOption option) {
		switch(oType){
		case ARMCC_ENABLE_TOOL_SPECIFIC_OPTION:
			return CmsisConstants.ZERO; // returns "0" false to disable it 
		case CPU_FPU_OPTION:
			return getCpuFpuOptionValue(buildSettings, option);
		case IBuildSettings.ENDIAN_OPTION:
			return getEndianOptionValue( buildSettings);
		case IBuildSettings.ARCH_OPTION:
			return getTargetArchOptionValue( buildSettings);
		default:
			break;

		}
		return super.getRteOptionValue(oType, buildSettings, option);
	}

	protected String getTargetArchOptionValue(IBuildSettings buildSettings) {
		return null; // default does not have it
	}

	protected String getEndianOptionValue(IBuildSettings buildSettings) {
		String endian = getDeviceAttribute(IBuildSettings.ENDIAN_OPTION, buildSettings);
		String val = compilerVersion >= 6 ? ENDIAN_PREFICS_6 : ENDIAN_PREFICS_5;

		if (endian == null || endian.isEmpty()) {
			val = AUTO; 
		} else if(endian.equals(CmsisConstants.LITTLENDIAN)) {
			val = LITTLE;
		} else if(endian.equals(CmsisConstants.BIGENDIAN)) {
			val = BIG;
		} else {
			val = AUTO;
		}
		String prefix = compilerVersion >= 6 ? ENDIAN_PREFICS_6 : ENDIAN_PREFICS_5;
		return prefix + val;
	}

	
	@Override
	protected int getRteOptionType(String id) {
		switch(id){
		case "com.arm.tool.c.compiler.option.target.enableToolSpecificSettings"://$NON-NLS-1$
		case "com.arm.tool.assembler.option.target.enableToolSpecificSettings": //$NON-NLS-1$
		case "com.arm.tool.c.linker.option.target.enableToolSpecificSettings": 	//$NON-NLS-1$
		case "com.arm.tool.c.compiler.v6.base.options.target.enableToolSpecificSettings":  //$NON-NLS-1$
		case "com.arm.tool.assembler.v6.base.options.target.enableToolSpecificSettings": //$NON-NLS-1$
		case "com.arm.tool.linker.v6.base.options.target.enableToolSpecificSettings": //$NON-NLS-1$
			return ARMCC_ENABLE_TOOL_SPECIFIC_OPTION;
		
		case "com.arm.toolchain.ac5.options.libs.useMicroLib": 					//$NON-NLS-1$
		case "com.arm.toolchain.v6.base.options.libs.useMicroLib": 				//$NON-NLS-1$
			return ARMCC_USE_MICROLIB;

		case "com.arm.toolchain.ac5.option.target.cpu_fpu":						//$NON-NLS-1$
		case "com.arm.toolchain.v6.base.options.target.cpu_fpu":				//$NON-NLS-1$
			return CPU_FPU_OPTION;
		case "com.arm.toolchain.ac5.option.endian":								//$NON-NLS-1$
		case "com.arm.toolchain.v6.base.options.endian":						//$NON-NLS-1$
			return IBuildSettings.ENDIAN_OPTION;
		
		case "com.arm.tool.c.compiler.option.implicit.defmac":					//$NON-NLS-1$
		case "com.arm.tool.c.compiler.v6.base.option.implicit.defmac":			//$NON-NLS-1$
		case "com.arm.tool.assembler.v6.base.option.implicit.defmac":			//$NON-NLS-1$
			return IBuildSettings.RTE_DEFINES;
		case "com.arm.tool.assembler.option.implicit.predefine":				//$NON-NLS-1$ 
			return ARMCC5_ASMDEFINES_OPTION;
		case "com.arm.tool.c.compiler.option.implicit.incpath":					//$NON-NLS-1$
		case "com.arm.tool.assembler.option.implicit.incpath":					//$NON-NLS-1$
		case "com.arm.tool.c.compiler.v6.base.option.implicit.incpath":			//$NON-NLS-1$
		case "com.arm.tool.assembler.v6.base.option.implicit.incpath":			//$NON-NLS-1$
			return IBuildSettings.RTE_INCLUDE_PATH;

		case "com.arm.tool.c.linker.implicit.libs":								//$NON-NLS-1$
			return IBuildSettings.RTE_LIBRARIES;
			
		case "com.arm.tool.c.compiler.option.flags":							//$NON-NLS-1$
		case "com.arm.tool.c.compiler.v6.base.option.flags":					//$NON-NLS-1$
			return IBuildSettings.CMISC_OPTION;
		case "com.arm.tool.assembler.option.flags":								//$NON-NLS-1$
		case "com.arm.tool.assembler.v6.base.option.flags":						//$NON-NLS-1$
			return IBuildSettings.AMISC_OPTION;
		case "com.arm.tool.c.linker.option.flags":								//$NON-NLS-1$
			return IBuildSettings.LMISC_OPTION;
		case "com.arm.tool.librarion.options.misc":								//$NON-NLS-1$
			return IBuildSettings.ARMISC_OPTION;
		
		case "com.arm.tool.c.linker.option.scatter":							//$NON-NLS-1$
			return IBuildSettings.RTE_LINKER_SCRIPT;
			
		case "com.arm.tool.c.compiler.option.implicit.flags":					//$NON-NLS-1$
		case "com.arm.tool.c.compiler.v6.base.option.implicit.flags":			//$NON-NLS-1$
			return IBuildSettings.RTE_CMISC;
		case "com.arm.tool.assembler.option.implicit.flags":					//$NON-NLS-1$
		case "com.arm.tool.assembler.v6.base.option.implicit.flags":			//$NON-NLS-1$
			return IBuildSettings.RTE_ASMMISC;
		case "com.arm.tool.c.linker.option.implicit.flags":						//$NON-NLS-1$
			return IBuildSettings.RTE_LMISC;
			
		default:
			break;
		}
		return IBuildSettings.UNKNOWN_OPTION;
	}
	

	@Override
	protected int getOptionType(int valueType) {
		return IBuildSettings.UNKNOWN_OPTION; // do set unknown options 
	}


	/**
	 * Constructs value for CPU option
	 * @param buildSettings IBuildSettings to get infromation about device 
	 * @param option option to set new value 
	 * @return cpu option  string
	 */
	protected String getCpuFpuOptionValue(IBuildSettings buildSettings, IOption option) {
		String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
		
		// do we need to change the value for Cortex-A processors?
		if(cpu.startsWith("Cortex-") || cpu.startsWith("ARMV8")) {  //$NON-NLS-1$//$NON-NLS-2$
			String oldValue = getCurrentStringValue(option);
			if(oldValue != null && oldValue.startsWith(cpu))
				return null; // do not change option
		}
		
		String fpuSuffix = getFpuOptionValue(cpu, fpu);
		if(fpuSuffix != null && !fpuSuffix.isEmpty()){
			cpu += '.' + fpuSuffix;
		}
		return cpu;
	}

	/**
	 * Returns required FPU string depending on device info attributes 
	 * @param cpu device info's Dcore attribute
	 * @param fpu device info's Dfpu attribute 
	 * @param version compiler version (major)
	 * @return resulting FPU string
	 */
	public String getFpuOptionValue(String cpu, String fpu) {
		if(cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) {
			return NoFPU;
		}
		boolean dp = fpu.equals(CmsisConstants.DP_FPU);
		
		switch(cpu) {
		case "Cortex-M4": 	//$NON-NLS-1$
			return compilerVersion >= 6 ? "FPv4_SP_D16" : "FPv4_SP"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-M7": 	//$NON-NLS-1$
			if(compilerVersion >= 6)
				return dp? "FPv5_D16" : "FPv5_SP_D16"; //$NON-NLS-1$ //$NON-NLS-2$
			return dp? "FPv5_D16" : "FPv5_SP"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-R4": 	//$NON-NLS-1$
		case "Cortex-R5": 	//$NON-NLS-1$
			return "VFPv3_D16"; //$NON-NLS-1$
		case "Cortex-A5": 	//$NON-NLS-1$
		case "Cortex-A7": 	//$NON-NLS-1$
		case "Cortex-A53":	//$NON-NLS-1$
		case "Cortex-A57":	//$NON-NLS-1$
		case "Cortex-A72":	//$NON-NLS-1$
			if(compilerVersion >= 6 )
				return dp? "VFPv4" : "VFPv4.Neon"; //$NON-NLS-1$ //$NON-NLS-2$
			return dp? "VFPv4_D16" : "VFPv4"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-A8": 	//$NON-NLS-1$
			return (compilerVersion >= 6 ) ? "VFPv3.Neon": "VFPv3"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-A9": 	//$NON-NLS-1$
			if(compilerVersion >= 6 )
				return dp? "VFPv3_D16_FP16" : "VFPv3_FP16.Neon"; //$NON-NLS-1$ //$NON-NLS-2$
			return dp? "VFPv3_D16_FP16" : "VFPv3_FP16"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-A15":	//$NON-NLS-1$
			return dp? "VFPv4_D16" : "VFPv4"; //$NON-NLS-1$ //$NON-NLS-2$
		case "Cortex-A12":	//$NON-NLS-1$
		case "Cortex-A17":	//$NON-NLS-1$
			return "VFPv4"; //$NON-NLS-1$
		case "ARMV8MML": 	//$NON-NLS-1$	
		}
		return NoFPU;
	}
}
