/*******************************************************************************
 * Copyright (c) 2015 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 * Liviu Ionescu - review, testing and enhancements   
 *******************************************************************************/

package com.arm.cmsis.pack.build.gnuarmeclipse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arm.cmsis.pack.build.settings.IBuildSettings;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Toolchain adapter for GNU ARM C/C++ Cross Compiler<br>
 * See: <a
 * href="http://gnuarmeclipse.livius.net">"http://gnuarmeclipse.livius.net"</a><br>
 */
public class GnuarmeclipseToolChainAdapter extends RteToolChainAdapter {

	static public final String GNUARMECLIPSE_OPTION = "ilg.gnuarmeclipse.managedbuild.cross.option"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_ARM_TARGET = GNUARMECLIPSE_OPTION 			+ ".arm.target"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_CPU_OPTION = GNUARMECLIPSE_ARM_TARGET		+ ".family"; //$NON-NLS-1$  
	static public final String GNUARMECLIPSE_FPU_OPTION = GNUARMECLIPSE_ARM_TARGET		+ ".fpu.unit"; //$NON-NLS-1$  
	
	static public final String GNUARMECLIPSE_FPU_ABI_OPTION 		= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.abi"; //$NON-NLS-1$  
	static public final String GNUARMECLIPSE_ENDIAN_OPTION 			= GNUARMECLIPSE_ARM_TARGET	+ ".endianness"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_INSTR_SET_OPTION 		= GNUARMECLIPSE_ARM_TARGET	+ ".instructionset"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_LINKER_SCRIPT_OPTION 	= GNUARMECLIPSE_OPTION 		+ ".base.linker.scriptfile"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_CPU_VALUE_PREFIX 		= GNUARMECLIPSE_ARM_TARGET	+ ".mcpu."; //$NON-NLS-1$  
	static public final String GNUARMECLIPSE_FPU_VALUE_PREFIX 		= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.unit."; //$NON-NLS-1$  
	static public final String GNUARMECLIPSE_FPU_ABI_VALUE_PREFIX 	= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.abi."; //$NON-NLS-1$  
	static public final String GNUARMECLIPSE_ENDIAN_VALUE_PREFIX 	= GNUARMECLIPSE_ARM_TARGET	+ ".endianness."; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_INSTR_SET_VALUE_PREFIX = GNUARMECLIPSE_ARM_TARGET	+ ".instructionset."; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_CMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".base.compiler.other"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_AMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".assembler.other"; //$NON-NLS-1$
	static public final String GNUARMECLIPSE_LMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".base.linker.other"; //$NON-NLS-1$

	
	@Override
	protected int getRteOptionType(String id) {
		switch (id) {
		case GNUARMECLIPSE_CPU_OPTION:
			return CPU_OPTION;
		case GNUARMECLIPSE_FPU_OPTION:
			return FPU_OPTION;
		case GNUARMECLIPSE_FPU_ABI_OPTION:
			return FLOAT_ABI_OPTION;
		case GNUARMECLIPSE_ENDIAN_OPTION:
			return ENDIAN_OPTION;
		case GNUARMECLIPSE_INSTR_SET_OPTION:
			return INSTR_SET_OPTION;
		case GNUARMECLIPSE_LINKER_SCRIPT_OPTION:
			return LINKER_SCRIPT_OPTION;
			// misc options are here for completeness
		case GNUARMECLIPSE_CMISC_OPTION: 
			return CMISC_OPTION;
		case GNUARMECLIPSE_AMISC_OPTION:
			return AMISC_OPTION;
		case GNUARMECLIPSE_LMISC_OPTION:
			return LMISC_OPTION;

		default:
			break;
		}
		return UNKNOWN_OPTION;
	}

	@Override
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings) {
		switch (oType) {
		case CPU_OPTION:
			return getCpuOptionValue(buildSettings);
		case INSTR_SET_OPTION:
			return GNUARMECLIPSE_INSTR_SET_VALUE_PREFIX + "thumb"; //$NON-NLS-1$
		case ENDIAN_OPTION:
			return getEndianOptionValue(buildSettings); // bug in DS-5 armgcc
														// toolchain
		case FPU_OPTION:
			return getFpuOptionValue(buildSettings);
		case FLOAT_ABI_OPTION:
			return getFloatAbiOptionValue(buildSettings);
		case LINKER_SCRIPT_OPTION:
			return null; // reported via getStringListValue()
		default:
			break;

		}
		return super.getRteOptionValue(oType, buildSettings);
	}

	@Override
	protected Collection<String> getStringListValue(
			IBuildSettings buildSettings, int type) {
		if (type == IOption.LIBRARIES || type == IOption.LIBRARY_PATHS) {
			return null; // we add libraries as objects => ignore libs and lib
							// paths
		} else if (type == IOption.OBJECTS) {
			Collection<String> objs = buildSettings.getStringListValue(type);
			List<String> value = new LinkedList<String>();
			if (objs != null && !objs.isEmpty())
				value.addAll(objs);
			// add libraries as objects (gcc does not allow to specify libs with
			// absolute paths)
			Collection<String> libs = buildSettings
					.getStringListValue(IOption.LIBRARIES);
			if (libs != null && !libs.isEmpty())
				value.addAll(libs);
			return value;
		}
		return super.getStringListValue(buildSettings, type);
	}

	/**
	 * Returns value for CPU_OPTION
	 * 
	 * @param buildSettings
	 *            settings to get required information from
	 * @return CPU_OPTION value string
	 */
	protected String getCpuOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(CPU_OPTION, buildSettings);
		int pos = cpu.indexOf('+');
		if (pos > 0) {
			// Cortex-M0+ -> Cortex-M0plus
			cpu = cpu.substring(0, pos);
			cpu += "plus"; //$NON-NLS-1$
		}
		return GNUARMECLIPSE_CPU_VALUE_PREFIX + cpu.toLowerCase();
	}

	/**
	 * Returns enum value for FPU_OPTION
	 * 
	 * @param buildSettings
	 *            settings to get required information from
	 * @return FPU_OPTION value string
	 */
	public String getFpuOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(FPU_OPTION, buildSettings);
		String val = "default"; //$NON-NLS-1$
		if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU)
				|| !coreHasFpu(cpu)) {
			// default
		}
		if (cpu.equals("Cortex-M7")) { //$NON-NLS-1$
			if (fpu.equals(CmsisConstants.SP_FPU))
				val = "fpv5spd16"; //$NON-NLS-1$
			if (fpu.equals(CmsisConstants.DP_FPU))
				val = "fpv5d16"; //$NON-NLS-1$ 
		} else if (fpu.equals(CmsisConstants.SP_FPU)) {
			val = "fpv4spd16"; //$NON-NLS-1$
		}
		return GNUARMECLIPSE_FPU_VALUE_PREFIX + val;
	}

	/**
	 * Returns enum value for FLOAT_ABI_OPTION
	 * 
	 * @param buildSettings
	 *            settings to get required information from
	 * @return FPU_OPTION value string
	 */
	private String getFloatAbiOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(FPU_OPTION, buildSettings);
		String val;
		if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU)
				|| !coreHasFpu(cpu))
			val = "default"; //$NON-NLS-1$
		else
			val = "hard"; //$NON-NLS-1$
		return GNUARMECLIPSE_FPU_ABI_VALUE_PREFIX + val;
	}

	protected String getEndianOptionValue(IBuildSettings buildSettings) {
		String endian = getDeviceAttribute(ENDIAN_OPTION, buildSettings);
		String val;
		if (endian == null || endian.isEmpty()) {
			val = "default"; //$NON-NLS-1$
		} else if (endian.equals(CmsisConstants.LITTLENDIAN)) {
			val = "little"; //$NON-NLS-1$
		} else if (endian.equals(CmsisConstants.BIGENDIAN)) {
			val = "big"; //$NON-NLS-1$
		} else {
			val = "default"; //$NON-NLS-1$
		}
		return GNUARMECLIPSE_ENDIAN_VALUE_PREFIX + val;
	}
}
