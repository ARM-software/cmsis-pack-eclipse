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

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Toolchain adapter for GNU ARM C/C++ Cross Compiler<br>
 * See: <a href="https://gnu-mcu-eclipse.github.io/plugins/install/">"https://gnu-mcu-eclipse.github.io/plugins/install/"</a><br>
 */
public class GnuarmeclipseToolChainAdapter extends RteToolChainAdapter {

	public static final String GNUARMECLIPSE_TOOLCHAIN_PREFIX = "ilg.gnuarmeclipse.managedbuild.cross"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_TOOLCHAIN_ID = GNUARMECLIPSE_TOOLCHAIN_PREFIX + ".toolchain.base"; //$NON-NLS-1$

	public static final String GNUARMECLIPSE_OPTION = GNUARMECLIPSE_TOOLCHAIN_PREFIX 	+ ".option"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_ARM_TARGET = GNUARMECLIPSE_OPTION 			+ ".arm.target"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_CPU_OPTION = GNUARMECLIPSE_ARM_TARGET		+ ".family"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_FPU_OPTION = GNUARMECLIPSE_ARM_TARGET		+ ".fpu.unit"; //$NON-NLS-1$

	public static final String GNUARMECLIPSE_FPU_ABI_OPTION 		= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.abi"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_ENDIAN_OPTION 			= GNUARMECLIPSE_ARM_TARGET	+ ".endianness"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_INSTR_SET_OPTION 		= GNUARMECLIPSE_ARM_TARGET	+ ".instructionset"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_LINKER_SCRIPT_OPTION 	= GNUARMECLIPSE_OPTION 		+ ".base.linker.scriptfile"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_LINKER_SCRIPT_OPTION_C = GNUARMECLIPSE_OPTION 		+ ".c.linker.scriptfile"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_LINKER_SCRIPT_OPTION_CPP = GNUARMECLIPSE_OPTION 		+ ".cpp.linker.scriptfile"; //$NON-NLS-1$
			// ilg.gnuarmeclipse.managedbuild.cross.option.c.linker.scriptfile
	public static final String GNUARMECLIPSE_CPU_VALUE_PREFIX 		= GNUARMECLIPSE_ARM_TARGET	+ ".mcpu."; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_FPU_VALUE_PREFIX 		= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.unit."; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_FPU_ABI_VALUE_PREFIX 	= GNUARMECLIPSE_ARM_TARGET	+ ".fpu.abi."; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_ENDIAN_VALUE_PREFIX 	= GNUARMECLIPSE_ARM_TARGET	+ ".endianness."; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_INSTR_SET_VALUE_PREFIX = GNUARMECLIPSE_ARM_TARGET	+ ".instructionset."; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_CMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".base.compiler.other"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_AMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".assembler.other"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_LMISC_OPTION			= GNUARMECLIPSE_OPTION		+ ".base.linker.other"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_C_PRE_INLUDES          = GNUARMECLIPSE_OPTION      + ".c.compiler.include.files"; //$NON-NLS-1$
	public static final String GNUARMECLIPSE_CPP_PRE_INLUDES        = GNUARMECLIPSE_OPTION      + ".cpp.compiler.include.files"; //$NON-NLS-1$

	@Override
	protected int getRteOptionType(String id) {
		switch (id) {
		case GNUARMECLIPSE_CPU_OPTION:
			return IBuildSettings.CPU_OPTION;
		case GNUARMECLIPSE_FPU_OPTION:
			return IBuildSettings.FPU_OPTION;
		case GNUARMECLIPSE_FPU_ABI_OPTION:
			return IBuildSettings.FLOAT_ABI_OPTION;
		case GNUARMECLIPSE_ENDIAN_OPTION:
			return IBuildSettings.ENDIAN_OPTION;
		case GNUARMECLIPSE_INSTR_SET_OPTION:
			return IBuildSettings.INSTR_SET_OPTION;
		case GNUARMECLIPSE_LINKER_SCRIPT_OPTION:
		case GNUARMECLIPSE_LINKER_SCRIPT_OPTION_C:
		case GNUARMECLIPSE_LINKER_SCRIPT_OPTION_CPP:
			return IBuildSettings.RTE_LINKER_SCRIPT;
			// misc options are here for completeness
		case GNUARMECLIPSE_CMISC_OPTION:
			return IBuildSettings.CMISC_OPTION;
		case GNUARMECLIPSE_AMISC_OPTION:
			return IBuildSettings.AMISC_OPTION;
		case GNUARMECLIPSE_LMISC_OPTION:
			return IBuildSettings.LMISC_OPTION;
		case GNUARMECLIPSE_C_PRE_INLUDES:
		case GNUARMECLIPSE_CPP_PRE_INLUDES:
			return IBuildSettings.RTE_PRE_INCLUDES;
		default:
			break;
		}
		return IBuildSettings.UNKNOWN_OPTION;
	}

	@Override
	protected String getRteOptionValue(int oType, IBuildSettings buildSettings, IOption option) {
		switch (oType) {
		case IBuildSettings.CPU_OPTION:
			return getCpuOptionValue(buildSettings);
		case IBuildSettings.INSTR_SET_OPTION:
			return GNUARMECLIPSE_INSTR_SET_VALUE_PREFIX + "thumb"; //$NON-NLS-1$
		case IBuildSettings.ENDIAN_OPTION:
			return getEndianOptionValue(buildSettings);
		case IBuildSettings.FPU_OPTION:
			return getFpuOptionValue(buildSettings);
		case IBuildSettings.FLOAT_ABI_OPTION:
			return getFloatAbiOptionValue(buildSettings);
		case IBuildSettings.RTE_LINKER_SCRIPT:
			return null; // reported via getStringListValue()
		default:
			break;

		}
		return super.getRteOptionValue(oType, buildSettings, option);
	}

	@Override
	protected Collection<String> getStringListValue(IBuildSettings buildSettings, int type) {
		if (type == IBuildSettings.RTE_LIBRARIES || type == IBuildSettings.RTE_LIBRARY_PATHS) {
			return null; // we add libraries as objects => ignore libs and lib
							// paths
		} else if (type == IBuildSettings.RTE_OBJECTS) {
			Collection<String> objs = buildSettings.getStringListValue(IBuildSettings.RTE_OBJECTS);
			List<String> value = new LinkedList<String>();
			if (objs != null && !objs.isEmpty())
				value.addAll(objs);
			// add libraries as objects (gcc does not allow to specify libs with
			// absolute paths)
			Collection<String> libs = buildSettings.getStringListValue(IBuildSettings.RTE_LIBRARIES);
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
		String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
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
		String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
		String val = "default"; //$NON-NLS-1$
		if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) {
			// default
		} else {
			if (cpu.equals("Cortex-M7")) { //$NON-NLS-1$
				if (fpu.equals(CmsisConstants.SP_FPU))
					val = "fpv5spd16"; //$NON-NLS-1$
				if (fpu.equals(CmsisConstants.DP_FPU))
					val = "fpv5d16"; //$NON-NLS-1$
			} else if (fpu.equals(CmsisConstants.SP_FPU)) {
				val = "fpv4spd16"; //$NON-NLS-1$
			}
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
	public String getFloatAbiOptionValue(IBuildSettings buildSettings) {
		String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
		String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
		String val;
		if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU)
				|| !coreHasFpu(cpu))
			val = "default"; //$NON-NLS-1$
		else
			val = "hard"; //$NON-NLS-1$
		return GNUARMECLIPSE_FPU_ABI_VALUE_PREFIX + val;
	}

	protected String getEndianOptionValue(IBuildSettings buildSettings) {
		String endian = getDeviceAttribute(IBuildSettings.ENDIAN_OPTION, buildSettings);
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
