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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;


/**
 * Interface that sets IConfiguration build options according to IBuildSettings 
 */
public interface IRteToolChainAdapter {

	// RTE option types, extend IOption.getValueType() values 
	// not implemented as enum to be easily extensible
	public static final int UNKNOWN_OPTION 	= -1;
    // all other options start with 100
	public static final int RTE_OPTION		= 100;
	public static final int CMISC_OPTION 		 = RTE_OPTION + 1; // C compiler miscellaneous
	public static final int CPPMISC_OPTION 		 = RTE_OPTION + 2; // CPP compiler miscellaneous
	public static final int AMISC_OPTION 		 = RTE_OPTION + 3; // assembler miscellaneous
	public static final int ARMISC_OPTION 		 = RTE_OPTION + 4; // archiver (librarian) miscellaneous
	public static final int LMISC_OPTION 		 = RTE_OPTION + 5; // linker miscellaneous
	public static final int LINKER_SCRIPT_OPTION = RTE_OPTION + 6; // linker script/scatter file

	
	public static final int RTE_DEVICE_OPTION	= RTE_OPTION  + 10;
	public static final int CPU_OPTION 			= RTE_DEVICE_OPTION + 1;
	public static final int ARCH_OPTION 		= RTE_DEVICE_OPTION + 2;
	public static final int INSTR_SET_OPTION 	= RTE_DEVICE_OPTION + 3; // instruction set
	public static final int THUMB_OPTION 		= RTE_DEVICE_OPTION + 4; // thumb option if separate from instruction set
	public static final int ENDIAN_OPTION 		= RTE_DEVICE_OPTION + 5;
	public static final int FPU_OPTION 			= RTE_DEVICE_OPTION + 6;
	public static final int FLOAT_ABI_OPTION 	= RTE_DEVICE_OPTION + 7;
	
	/**
	 * Sets toolchain build options for given IConfiguration according to supplied IRteBuildSettings
	 * @param configuration destination IConfiguration to set options to 
	 * @param buildSettings source IBuildSettings
	 */
	void setToolChainOptions(IConfiguration configuration,  IBuildSettings buildSettings);
	
	
	/**
	 * Sets initial toolchain build options for given IConfiguration according to supplied IRteBuildSettings.<br>
	 * This function is called when device settings are changed (e.g. when new project is created)
	 * @param configuration destination IConfiguration to set options to 
	 * @param buildSettings source IBuildSettings
	 */
	void setInitialToolChainOptions(IConfiguration configuration,  IBuildSettings buildSettings);
	
	
	/**
	 * Returns Linker script generator associated with the adapter 
	 * @return ILinkerScriptGenerator or null if no generator is available
	 */
	ILinkerScriptGenerator getLinkerScriptGenerator();
	
}
