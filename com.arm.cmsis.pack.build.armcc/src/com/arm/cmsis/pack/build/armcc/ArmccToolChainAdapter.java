/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.build.settings.ILinkerScriptGenerator;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Toolchain adapter for ARMCC 5.x compiler
 *
 */
public class ArmccToolChainAdapter extends RteToolChainAdapter {

    public static final int ARMCC5_ASMDEFINES_OPTION = IBuildSettings.RTE_USER_OPTION + 1;
    public static final int ARMCC_ENABLE_TOOL_SPECIFIC_OPTION = IBuildSettings.TOOLCHAIN_USER_OPTION + 1;
    public static final int ARMCC_USE_MICROLIB = IBuildSettings.TOOLCHAIN_USER_OPTION + 2;
    public static final int CPU_FPU_OPTION = IBuildSettings.TOOLCHAIN_USER_OPTION + 5;
    public static final int C5_LANGUAGE_MODE = IBuildSettings.TOOLCHAIN_USER_OPTION + 6;
    public static final int CPP5_LANGUAGE_MODE = IBuildSettings.TOOLCHAIN_USER_OPTION + 7;
    public static final int C6_LANGUAGE_MODE = IBuildSettings.TOOLCHAIN_USER_OPTION + 8;
    public static final int CPP6_LANGUAGE_MODE = IBuildSettings.TOOLCHAIN_USER_OPTION + 9;

    public static final int ARMCC5_OPTLEVEL = IBuildSettings.TOOLCHAIN_USER_OPTION + 10;
    public static final int ARMCC_LINKER_ENTRY = IBuildSettings.TOOLCHAIN_USER_OPTION + 11;
    public static final int A6_FORCE_PREPROC = IBuildSettings.TOOLCHAIN_USER_OPTION + 12;
    public static final int A6_MASM = IBuildSettings.TOOLCHAIN_USER_OPTION + 13;

    public static final int C6_FLOAT_ABI = IBuildSettings.TOOLCHAIN_USER_OPTION + 14;

    public static final int C6_SHORT_ENUMS = IBuildSettings.TOOLCHAIN_USER_OPTION + 20;

    public static final String CORTEX = "Cortex"; //$NON-NLS-1$
    public static final String CORTEX_A = "Cortex-A"; //$NON-NLS-1$
    public static final String CORTEX_M = "Cortex-M"; //$NON-NLS-1$
    public static final String GENERIC = "Generic"; //$NON-NLS-1$
    public static final String GENERIC_ARMV8M_BASE = GENERIC + ".ARMv8-M.Base"; //$NON-NLS-1$
    public static final String GENERIC_ARMV8M_MAIN = GENERIC + ".ARMv8-M.Main"; //$NON-NLS-1$
    public static final String GENERIC_ARMV81M_MAIN = GENERIC + ".ARMv8.1-M.Main"; //$NON-NLS-1$

    public static final String NoFPU = "NoFPU"; //$NON-NLS-1$
    public static final String NoDSP_NoFPU = "NoDSP.NoFPU"; //$NON-NLS-1$

    public static final String SETA = " SETA "; //$NON-NLS-1$

    public static final String AUTO = "auto"; //$NON-NLS-1$
    public static final String LITTLE = "little"; //$NON-NLS-1$
    public static final String BIG = "big"; //$NON-NLS-1$

    public static final String TOOLCHAIN_PREFIX_5 = "com.arm.toolchain.ac5.option."; //$NON-NLS-1$

    public static final String COPT_PREFIX_5 = "com.arm.tool.c.compiler.option."; //$NON-NLS-1$
    public static final String COPT_PREFIX_6 = "com.arm.tool.c.compiler.v6.base.option."; //$NON-NLS-1$

    public static final String CPPOPT_PREFIX_5 = "com.arm.tool.cpp.compiler.option."; //$NON-NLS-1$
    public static final String CPPOPT_PREFIX_6 = "com.arm.tool.cpp.compiler.v6.base.option."; //$NON-NLS-1$

    public static final String ENDIAN_PREFICS_5 = COPT_PREFIX_5 + "endian."; //$NON-NLS-1$
    public static final String ENDIAN_PREFICS_6 = COPT_PREFIX_6 + "endian."; //$NON-NLS-1$

    public static final String C5_C_MODE = "com.arm.tool.c.compile.option.lang"; //$NON-NLS-1$
    public static final String C5_AUTO = "com.arm.tool.c.compile.option.lang.auto"; //$NON-NLS-1$
    public static final String C5_C90 = "com.arm.tool.c.compile.option.lang.c90"; //$NON-NLS-1$
    public static final String C5_C99 = "com.arm.tool.c.compile.option.lang.c99"; //$NON-NLS-1$
    public static final String C5_CPP = "com.arm.tool.c.compile.option.lang.cpp"; //$NON-NLS-1$
    public static final String C5_CPP11 = "com.arm.tool.c.compile.option.lang.cpp11"; //$NON-NLS-1$

    public static final String C5_CPP_MODE = "com.arm.tool.cpp.compiler.option.lang"; //$NON-NLS-1$
    public static final String CPP5_AUTO = "com.arm.tool.cpp.compiler.option.lang.auto"; //$NON-NLS-1$
    public static final String CPP5_C90 = "com.arm.tool.cpp.compiler.option.lang.c90"; //$NON-NLS-1$
    public static final String CPP5_C99 = "com.arm.tool.cpp.compiler.option.lang.c99"; //$NON-NLS-1$
    public static final String CPP5_CPP = "com.arm.tool.cpp.compiler.option.lang.cpp"; //$NON-NLS-1$
    public static final String CPP5_CPP11 = "com.arm.tool.cpp.compiler.option.lang.cpp11"; //$NON-NLS-1$

    public static final String C6_C_MODE = "com.arm.tool.c.compiler.v6.base.option.lang"; //$NON-NLS-1$
    public static final String C6_GNU11 = "com.arm.tool.c.compiler.v6.base.option.lang.gnu11"; //$NON-NLS-1$
    public static final String C6_C90 = "com.arm.tool.c.compiler.v6.baseoption.lang.c90"; //$NON-NLS-1$
    public static final String C6_GNU90 = "com.arm.tool.c.compiler.v6.baseoption.lang.gnu90"; //$NON-NLS-1$
    public static final String C6_C99 = "com.arm.tool.c.compiler.v6.base.option.lang.c99"; //$NON-NLS-1$
    public static final String C6_GNU99 = "com.arm.tool.c.compiler.v6.base.option.lang.gnu99"; //$NON-NLS-1$
    public static final String C6_C11 = "com.arm.tool.c.compiler.v6.base.option.lang.c11"; //$NON-NLS-1$

    public static final String C6_CPP_LANGUAGE_MODE = "com.arm.tool.cpp.compiler.v6.base.option.lang"; //$NON-NLS-1$
    public static final String CPP6_C99 = "com.arm.tool.cpp.compiler.v6.base.option.lang.c99"; //$NON-NLS-1$
    public static final String CPP6_CPP11 = "com.arm.tool.cpp.compiler.v6.base.option.lang.c11"; //$NON-NLS-1$

    public static final String C99 = "--C99"; //$NON-NLS-1$
    public static final String c99 = "--c99"; //$NON-NLS-1$

    public static final String AC5 = "AC5"; //$NON-NLS-1$
    public static final String AC6 = "AC6"; //$NON-NLS-1$
    public static final String AC6LTO = "AC6LTO"; //$NON-NLS-1$

    public static final String STRICT = COPT_PREFIX_5 + "strict"; //$NON-NLS-1$
    public static final String STRICT_AUTO = COPT_PREFIX_5 + "strict.auto"; //$NON-NLS-1$
    public static final String STRICT_NONE = COPT_PREFIX_5 + "strict.none"; //$NON-NLS-1$
    public static final String STRICT_ERROR = COPT_PREFIX_5 + "strict.error"; //$NON-NLS-1$
    public static final String STRICT_WARN = COPT_PREFIX_5 + "strict.warn"; //$NON-NLS-1$
    public static final String OPT_FOR = COPT_PREFIX_5 + "optfor"; //$NON-NLS-1$

    public static final String OPT_FOR_AUTO = OPT_FOR + ".auto"; //$NON-NLS-1$
    public static final String OPT_FOR_SIZE = OPT_FOR + ".size"; //$NON-NLS-1$
    public static final String OPT_FOR_TIME = OPT_FOR + ".time"; //$NON-NLS-1$

    // compiler and toolchain options for enum
    public static final String OPT_ENUM = "enum"; //$NON-NLS-1$
    public static final String C5_ENUM_INT = COPT_PREFIX_5 + OPT_ENUM;
    public static final String T5_ENUM_INT = TOOLCHAIN_PREFIX_5 + OPT_ENUM;

    public static final String WARNINGS_INTO_ERRORS = COPT_PREFIX_6 + "warnaserr"; //$NON-NLS-1$

    public static final String A_NO_WARNINGS = "com.arm.tool.assembler.v6.base.option.suppresswarn"; //$NON-NLS-1$

    public static final String C5_L_SCATTER_FILE = "com.arm.tool.c.linker.option.scatter"; //$NON-NLS-1$
    public static final String C5_L_RO_BASE = "com.arm.tool.c.linker.option.robase"; //$NON-NLS-1$
    public static final String C5_L_RW_BASERW = "com.arm.tool.c.linker.option.rwbase"; //$NON-NLS-1$
    public static final String C5_L_ENTRY = "com.arm.tool.c.linker.option.entry"; //$NON-NLS-1$
    public static final String C5_L_DISABLE_WARNINGS = "com.arm.tool.c.link.option.suppress"; //$NON-NLS-1$

    public static final String C6_L_DISABLE_WARNINGS = "com.arm.tool.linker.v6.option.suppress"; //$NON-NLS-1$

    public static final String C5_ARMCC_USE_MICROLIB = "com.arm.toolchain.ac5.options.libs.useMicroLib"; //$NON-NLS-1$
    public static final String C6_ARMCC_USE_MICROLIB = "com.arm.toolchain.v6.base.options.libs.useMicroLib"; //$NON-NLS-1$

    public static final String A6_MASM_DEFAULT = "masm.val.default"; //$NON-NLS-1$
    public static final String A6_MASM_AUTO = "masm.val.auto"; //$NON-NLS-1$
    public static final String A6_MASM_GNU = "masm.val.gnu"; //$NON-NLS-1$
    public static final String A6_MASM_ARMASM = "masm.val.armasm"; //$NON-NLS-1$

    public static final String A5_CPREPROC = "--cpreproc"; //$NON-NLS-1$
    public static final String A5_CPREPROC_OPTS = "--cpreproc_opts"; //$NON-NLS-1$

    public static final String C6_FLOAT_ABI_ID = "com.arm.toolchain.v6.base.options.floatabi"; //$NON-NLS-1$
    public static final String C6_FLOAT_ABI_HARD = "com.arm.tool.c.compiler.v6.base.option.floatabi.hard"; //$NON-NLS-1$

    protected int majorCompilerVersion = 5; // major compiler version : 5 or 6
    protected String toolchainVersion; // full toolchain version
    protected Attributes rteOptions = null;

    public ArmccToolChainAdapter() {
        rteOptions = new Attributes();
    }

    public int getMajorCompilerVersion() {
        return majorCompilerVersion;
    }

    public boolean isVersion6() {
        return majorCompilerVersion >= 6;
    }

    public String getToolchainVersion() {
        return toolchainVersion;
    }

    public boolean isAssempler5() {
        if (currentTool == null)
            return !isVersion6();
        return currentTool.getId().startsWith("com.arm.tool.assembler.base"); //$NON-NLS-1$
    }

    @Override
    public IAttributes getRteOptions(IConfiguration configuration) {
        if (configuration == null) {
            return rteOptions;
        }
        checkToolchainVersion(configuration);
        collectRteAttributes(configuration);
        return rteOptions;
    }

    protected void collectRteAttributes(IConfiguration configuration) {
        rteOptions = new Attributes();
        if (!isVersion6()) {
            rteOptions.setAttribute(CmsisConstants.TOPTIONS, AC5);
        } else {
            rteOptions.setAttribute(CmsisConstants.TOPTIONS, AC6);
        }
    }

    protected void checkToolchainVersion(IConfiguration configuration) {
        IToolChain toolChain = configuration.getToolChain();
        if (toolChain == null)
            return;
        String baseID = toolChain.getBaseId();
        if (baseID.startsWith("com.arm.toolchain.v6")) //$NON-NLS-1$
            majorCompilerVersion = 6;
        toolchainVersion = extractToolchainVersion(baseID);
    }

    /**
     * Extracts toolchain version from toolchain ID
     *
     * @param toolChainId ARM toolchain ID
     * @return version string
     */
    public static String extractToolchainVersion(String toolChainId) {
        String version = CmsisConstants.EMPTY_STRING;
        if (toolChainId == null || toolChainId.isEmpty()) {
            return version;
        }

        String[] sections = toolChainId.split("-"); //$NON-NLS-1$
        if (sections.length >= 2)
            version = sections[1];

        return version;
    }

    @Override
    public void setToolChainOptions(IConfiguration configuration, IBuildSettings buildSettings) {
        if (configuration == null)
            return;

        adjustBuildSettings(configuration, buildSettings);
        super.setToolChainOptions(configuration, buildSettings);
    }

// mak
    @Override
    protected void adjustBuildSettings(IConfiguration configuration, IBuildSettings buildSettings) {
        checkToolchainVersion(configuration);
        // remove --C99 from RTE C-Misc build settings
        Collection<String> values = buildSettings.getStringListValue(IBuildSettings.RTE_CMISC);
        if (values != null) {
            boolean bC99 = false;
            if (values.contains(C99)) {
                bC99 = true;
                values.remove(C99);
            }
            if (values.contains(c99)) {
                bC99 = true;
                values.remove(c99);
            }
            if (bC99) {
                if (!isVersion6()) {
                    buildSettings.setAttribute("com.arm.tool.c.compile.option.lang", C5_C99); //$NON-NLS-1$
                }
            }
        }

        if (!isVersion6())
            return;
        adjustA6miscOptions(IBuildSettings.RTE_ASMMISC, buildSettings);
        adjustA6miscOptions(IBuildSettings.AMISC_OPTION, buildSettings);
    }

    protected void adjustA6miscOptions(int type, IBuildSettings buildSettings) {
        // adjust assembler settings to compiler 6
        Collection<String> values = buildSettings.getStringListValue(type);
        if (values == null || values.isEmpty())
            return;
        for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
            String s = iterator.next();
            if (!s.contains(A5_CPREPROC))
                continue;
            extractAsmPreprocDefines(s, buildSettings);
            iterator.remove();
        }
    }

    protected void extractAsmPreprocDefines(String s, IBuildSettings buildSettings) {
        int indexOpts = s.indexOf(A5_CPREPROC_OPTS);
        if (indexOpts < 0)
            return;
        indexOpts = s.indexOf('=', indexOpts + A5_CPREPROC_OPTS.length());
        if (indexOpts < 0)
            return;
        s = s.substring(indexOpts + 1).trim();
        if (s.isEmpty())
            return;
        String[] opts = s.split(","); //$NON-NLS-1$
        if (opts == null || opts.length == 0)
            return;
        for (String sOpt : opts) {
            sOpt = sOpt.trim();
            if (sOpt.length() < 3)
                continue;
            String val = sOpt.substring(2);
            if (sOpt.startsWith("-D")) { //$NON-NLS-1$
                buildSettings.addStringListValue(IBuildSettings.ADEFINES_OPTION, CmsisConstants.CMSIS_RTE_VAR + val);
            } else if (sOpt.startsWith("-U")) { //$NON-NLS-1$
                buildSettings.addStringListValue(IBuildSettings.AUNDEFINES_OPTION, CmsisConstants.CMSIS_RTE_VAR + val);
            }
        }
    }

    @Override
    public ILinkerScriptGenerator getLinkerScriptGenerator() {
        return new ScatterFileGenerator();
    }

    @Override
    protected void updateRteOption(int oType, IBuildObject configuration, IHoldsOptions tool, IOption option,
            IBuildSettings buildSettings) throws BuildException {
        switch (oType) {
        // we add libraries with absolute paths => ignore lib paths
        case IBuildSettings.RTE_LIBRARY_PATHS:
            return; // NO UPDATE OF THAT
        default:
            break;
        }
        super.updateRteOption(oType, configuration, tool, option, buildSettings);
    }

    @Override
    protected Collection<String> getStringListValue(IBuildSettings buildSettings, int oType) {
        Collection<String> rteValue = null;
        switch (oType) {
        case IBuildSettings.RTE_LIBRARY_PATHS: // we add libraries with absolute paths => ignore lib paths
        case IBuildSettings.RTE_CMISC: // Appended to regular CMISC
        case IBuildSettings.RTE_ASMMISC: // Appended to regular AMISC
        case IBuildSettings.RTE_LMISC: // Appended to regular LMISC
            return null;
        case ARMCC5_ASMDEFINES_OPTION:
        case IBuildSettings.ADEFINES_OPTION: {
            List<String> asmDefines = new LinkedList<String>();
            if (oType == ARMCC5_ASMDEFINES_OPTION)
                oType = IBuildSettings.RTE_DEFINES; // change to key used in buildSettings
            Collection<String> defines = buildSettings.getStringListValue(oType);
            if (defines != null) {
                for (String d : defines) {
                    String value = getAsmDefString(d);
                    asmDefines.add(value);
                }
            }
            return asmDefines;
        }
        case IBuildSettings.CMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_CMISC);
            break;
        case IBuildSettings.AMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_ASMMISC);
            break;
        case IBuildSettings.ARMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_ARMISC);
            break;
        case IBuildSettings.LMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_LMISC);
            break;
        default:
            break;
        }
        Collection<String> value = super.getStringListValue(buildSettings, oType);
        if (rteValue != null && !rteValue.isEmpty()) {
            if (value == null) {
                value = new LinkedList<>();
            }
            for (String s : rteValue) {
                value.add(CmsisConstants.CMSIS_RTE_VAR + s);
            }
        }
        return value;
    }

    protected Collection<String> getRteStringListValue(IBuildSettings buildSettings, int oType) {
        Collection<String> rteValue = null;
        switch (oType) {
        case IBuildSettings.ADEFINES_OPTION:
        case ARMCC5_ASMDEFINES_OPTION: {
            List<String> asmDefines = new LinkedList<String>();
            if (oType == ARMCC5_ASMDEFINES_OPTION)
                oType = IBuildSettings.RTE_DEFINES; // change to key used in buildSettings
            Collection<String> defines = buildSettings.getStringListValue(oType);
            if (defines != null) {
                for (String d : defines) {
                    String value = getAsmDefString(d);
                    asmDefines.add(value);
                }
            }
            return asmDefines;
        }
        case IBuildSettings.CMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_CMISC);
            break;
        case IBuildSettings.AMISC_OPTION:
            rteValue = buildSettings.getStringListValue(IBuildSettings.RTE_ASMMISC);
            break;
        case IBuildSettings.ARMISC_OPTION:
            rteValue = super.getStringListValue(buildSettings, IBuildSettings.RTE_ARMISC);
            break;
        case IBuildSettings.LMISC_OPTION:
            rteValue = super.getStringListValue(buildSettings, IBuildSettings.RTE_LMISC);
            break;
        default:
            break;
        }
        Collection<String> value = super.getStringListValue(buildSettings, oType);
        if (value == null) {
            return rteValue;
        }

        return value;
    }

    @Override
    protected List<String> cleanStringList(List<String> value, int oType) {
        switch (oType) {
        case ARMCC5_ASMDEFINES_OPTION:
        case IBuildSettings.RTE_DEFINES:
        case IBuildSettings.RTE_INCLUDE_PATH:
        case IBuildSettings.RTE_LIBRARIES:
        case IBuildSettings.RTE_CMISC:
        case IBuildSettings.RTE_ASMMISC:
        case IBuildSettings.RTE_LMISC:
            value.clear();
            break;

        case IBuildSettings.CMISC_OPTION:
        case IBuildSettings.AMISC_OPTION:
        case IBuildSettings.ARMISC_OPTION:
        case IBuildSettings.LMISC_OPTION:
        case IBuildSettings.ADEFINES_OPTION:
        case IBuildSettings.AUNDEFINES_OPTION:
        case IBuildSettings.RTE_PRE_INCLUDES:
            value = removeRtePathEntries(value);
            break;

        case IBuildSettings.RTE_LIBRARY_PATHS:
        default:
            break;
        }
        return value; // do nothing for all other lists
    }

    protected String getAsmDefString(String d) {
        if (!isAssempler5())
            return d;
        String val = "1"; //$NON-NLS-1$
        String s = CmsisConstants.EMPTY_STRING;
        int pos = d.indexOf('=');
        if (pos > 0) {
            val = d.substring(pos + 1);
            s += d.substring(0, pos);
        } else {
            s += d;
        }
        s += SETA;
        s += val;
        return s;
    }

    @Override
    protected String getRteOptionValue(int oType, IBuildSettings buildSettings, IOption option) {
        switch (oType) {
        case ARMCC_ENABLE_TOOL_SPECIFIC_OPTION:
            return CmsisConstants.ZERO; // returns "0" false to disable it
        case A6_FORCE_PREPROC:
            return CmsisConstants.ONE; // returns "1" true to enable it
        case A6_MASM:
            return A6_MASM_AUTO;
        case CPU_FPU_OPTION:
            return getCpuFpuOptionValue(buildSettings, option);
        case C5_LANGUAGE_MODE:
            return C5_C99;
        case C6_LANGUAGE_MODE:
            return C6_C99;
        case C6_SHORT_ENUMS: {
            String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
            if (cpu != null && cpu.startsWith(CORTEX_M)) {
                return CmsisConstants.ONE; // returns "1" true to enable it
            }
            return null;
        }

        case ARMCC_LINKER_ENTRY: {
            String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
            if (cpu.startsWith(CORTEX_A)) {
                return CmsisConstants.Vectors;
            } else if (cpu.startsWith(CORTEX_M) || cpu.startsWith("ARMV8M") || cpu.startsWith("ARMV81M")) {
                return CmsisConstants.Reset_Handler;
            } else if (buildSettings.usesDeviceStartup())
                return CmsisConstants.Reset_Handler;
        }
            return null;

        // Updated
        case ARMCC_USE_MICROLIB: {
            Collection<String> values = buildSettings.getStringListValue(ARMCC_USE_MICROLIB);
            if (values != null && !values.isEmpty()) {
                for (String s : values) {
                    return s;
                }
            }
            return "0"; //$NON-NLS-1$
            // return buildSettings.getAttribute("USE_MICROLIB","0");
        }
        // Updated
        case IBuildSettings.ENDIAN_OPTION:
            return getEndianOptionValue(buildSettings);
        case IBuildSettings.ARCH_OPTION:
            return getTargetArchOptionValue(buildSettings);

        case C6_FLOAT_ABI:
            String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
            String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
            if (fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) {
                return null;
            }
            return C6_FLOAT_ABI_HARD;

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
        String val = AUTO;
        ;

        if (endian != null) {
            if (endian.equals(CmsisConstants.LITTLENDIAN)) {
                val = LITTLE;
            } else if (endian.equals(CmsisConstants.BIGENDIAN)) {
                val = BIG;
            }
        }
        String prefix = isVersion6() ? ENDIAN_PREFICS_6 : ENDIAN_PREFICS_5;
        return prefix + val;
    }

    @Override
    protected int getRteOptionType(String id) {
        switch (id) {
        case "com.arm.tool.c.compiler.option.target.enableToolSpecificSettings"://$NON-NLS-1$
        case "com.arm.tool.assembler.option.target.enableToolSpecificSettings": //$NON-NLS-1$
        case "com.arm.tool.c.linker.option.target.enableToolSpecificSettings": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.options.target.enableToolSpecificSettings": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.options.target.enableToolSpecificSettings": //$NON-NLS-1$
        case "com.arm.tool.linker.v6.base.options.target.enableToolSpecificSettings": //$NON-NLS-1$
            return ARMCC_ENABLE_TOOL_SPECIFIC_OPTION;

        case "com.arm.toolchain.ac5.options.libs.useMicroLib": //$NON-NLS-1$
        case "com.arm.toolchain.v6.base.options.libs.useMicroLib": //$NON-NLS-1$
            return ARMCC_USE_MICROLIB;

        case "com.arm.toolchain.ac5.option.target.cpu_fpu": //$NON-NLS-1$
        case "com.arm.toolchain.v6.base.options.target.cpu_fpu": //$NON-NLS-1$
            return CPU_FPU_OPTION;

        case "com.arm.toolchain.ac5.option.endian": //$NON-NLS-1$
        case "com.arm.toolchain.v6.base.options.endian": //$NON-NLS-1$
            return IBuildSettings.ENDIAN_OPTION;

        case "com.arm.tool.c.compile.option.lang": //$NON-NLS-1$
            return C5_LANGUAGE_MODE;

        case "com.arm.tool.cpp.compiler.option.lang": //$NON-NLS-1$
            return CPP5_LANGUAGE_MODE;

        case "com.arm.tool.c.compiler.v6.base.option.lang": //$NON-NLS-1$
            return C6_LANGUAGE_MODE;

        case "com.arm.tool.cpp.compiler.v6.base.option.lang": //$NON-NLS-1$
            return CPP6_LANGUAGE_MODE;

        case "com.arm.tool.c.compiler.v6.base.option.shortEnumsWchar": //$NON-NLS-1$
            return C6_SHORT_ENUMS;

        case "com.arm.tool.c.compiler.option.defmac": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.defmac": //$NON-NLS-1$
            return IBuildSettings.CDEFINES_OPTION;

        case "com.arm.tool.c.compiler.option.undefmac": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.undefmac": //$NON-NLS-1$
            return IBuildSettings.CUNDEFINES_OPTION;

        case "com.arm.tool.c.compiler.option.incpath": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.incpath": //$NON-NLS-1$
            return IBuildSettings.CINCPATHS_OPTION;

        case "com.arm.tool.c.compiler.option.implicit.defmac": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.implicit.defmac": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.implicit.defmac": //$NON-NLS-1$
            return IBuildSettings.RTE_DEFINES;

        case "com.arm.tool.assembler.option.implicit.predefine": //$NON-NLS-1$
            return ARMCC5_ASMDEFINES_OPTION;

        case "com.arm.tool.assembler.option.predefine": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.defmac": //$NON-NLS-1$
            return IBuildSettings.ADEFINES_OPTION;
        case "com.arm.tool.assembler.v6.base.option.undefmac": //$NON-NLS-1$
            return IBuildSettings.AUNDEFINES_OPTION;

        case "com.arm.tool.c.compiler.option.implicit.incpath": //$NON-NLS-1$
        case "com.arm.tool.assembler.option.implicit.incpath": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.implicit.incpath": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.implicit.incpath": //$NON-NLS-1$
            return IBuildSettings.RTE_INCLUDE_PATH;

        case "com.arm.tool.c.compiler.option.preinc": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.preinc": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.preinc": //$NON-NLS-1$
            return IBuildSettings.RTE_PRE_INCLUDES;

        case "com.arm.tool.c.linker.implicit.libs": //$NON-NLS-1$
            return IBuildSettings.RTE_LIBRARIES;

        case "com.arm.tool.c.compiler.option.flags": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.flags": //$NON-NLS-1$
            return IBuildSettings.CMISC_OPTION;

        case "com.arm.tool.assembler.option.flags": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.flags": //$NON-NLS-1$
            return IBuildSettings.AMISC_OPTION;

        case "com.arm.tool.c.linker.option.flags": //$NON-NLS-1$
            return IBuildSettings.LMISC_OPTION;

        case "com.arm.tool.librarion.options.misc": //$NON-NLS-1$
            return IBuildSettings.ARMISC_OPTION;

        case "com.arm.tool.c.linker.option.scatter": //$NON-NLS-1$
            return IBuildSettings.RTE_LINKER_SCRIPT;

        case "com.arm.tool.c.compiler.option.implicit.flags": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.v6.base.option.implicit.flags": //$NON-NLS-1$
            return IBuildSettings.RTE_CMISC;

        case "com.arm.tool.assembler.option.implicit.flags": //$NON-NLS-1$
        case "com.arm.tool.assembler.v6.base.option.implicit.flags": //$NON-NLS-1$
            return IBuildSettings.RTE_ASMMISC;

        case "com.arm.tool.c.linker.option.implicit.flags": //$NON-NLS-1$
            return IBuildSettings.RTE_LMISC;

        case "com.arm.tool.c.compiler.option.optlevel": //$NON-NLS-1$
            return ARMCC5_OPTLEVEL;

        case "com.arm.tool.assembler.option.incpath": //$NON-NLS-1$
            return IBuildSettings.C5_ASMINCPATHS_OPTION;

        case "com.arm.tool.assembler.v6.base.option.incpath": //$NON-NLS-1$
            return IBuildSettings.C6_ASMINCPATHS_OPTION;

        case "com.arm.tool.c.linker.libs": //$NON-NLS-1$
            return IBuildSettings.LIBS_OPTION;

        case "com.arm.tool.assembler.v6.base.option.force.preproc": //$NON-NLS-1$
            return A6_FORCE_PREPROC;

        case "com.arm.tool.assembler.v6.base.option.masm": //$NON-NLS-1$
            return A6_MASM;

        case C5_L_ENTRY:
            return ARMCC_LINKER_ENTRY;

        case C6_FLOAT_ABI_ID:
            return C6_FLOAT_ABI;

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
     *
     * @param buildSettings IBuildSettings to get infromation about device
     * @param option        option to set new value
     * @return cpu option string
     */
    protected String getCpuFpuOptionValue(IBuildSettings buildSettings, IOption option) {
        String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
        if (cpu == null || cpu.isEmpty())
            return null;

        String mve = getDeviceAttribute(IBuildSettings.MVE_OPTION, buildSettings);
        String cpuFpu = getCpuPrefix(cpu, mve);
        // do we need to change the value for Cortex-A processors or V8?
        if (cpuFpu.startsWith(CORTEX_A) || cpuFpu.startsWith(GENERIC)) {
            String oldValue = getCurrentStringValue(option);
            if (oldValue != null && oldValue.startsWith(cpuFpu))
                return null; // do not change option
        }
        String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
        String dsp = getDeviceAttribute(IBuildSettings.DSP_OPTION, buildSettings);
        String fpuSuffix = getFpuSuffix(cpu, fpu, dsp, mve);
        if (fpuSuffix != null && !fpuSuffix.isEmpty()) {
            cpuFpu += '.' + fpuSuffix;
        }
        return cpuFpu;
    }

    /**
     * Constructs CPU prefix for CPU-FPU option
     *
     * @param cpu device info's Dcore attribute
     * @param mve device info's Dmve attribute
     * @return CPU prefix string
     */
    protected String getCpuPrefix(String cpu, String mve) {
        String prefix = cpu;
        boolean bMve = false;
        switch (cpu) {
        case "Cortex-M0+": //$NON-NLS-1$
            return "Cortex-M0.Plus"; //$NON-NLS-1$
        case "ARMV8MBL": //$NON-NLS-1$
            return GENERIC_ARMV8M_BASE; // $NON-NLS-1$
        case "ARMV8MML": //$NON-NLS-1$
            return GENERIC_ARMV8M_MAIN; // $NON-NLS-1$
        case "ARMV81MML": //$NON-NLS-1$
            prefix = GENERIC_ARMV81M_MAIN; // $NON-NLS-1$
            bMve = true;
            break;
        case "Cortex-M55": //$NON-NLS-1$
            bMve = true;
            break;
        default:
            break;
        }
        if (bMve && mve != null) {
            switch (mve) {
            case CmsisConstants.MVE:
                prefix += ".MVEI"; //$NON-NLS-1$
                break;
            case CmsisConstants.FP_MVE:
                prefix += ".MVEF"; //$NON-NLS-1$
                break;
            case CmsisConstants.NO_MVE:
            default:
                break;
            }
        }
        return prefix;
    }

    /**
     * Returns required FPU string depending on device info attributes
     *
     * @param cpu device info's Dcore attribute
     * @param fpu device info's Dfpu attribute
     * @param mve device info's Ddsp attribute
     * @param mve device info's Dmve attribute
     * @return resulting FPU string
     */
    public String getFpuSuffix(String cpu, String fpu, String dsp, String mve) {
        if (fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu)) {
            if (CmsisConstants.FP_MVE.equals(mve)) {
                return "FP16.FP32"; //$NON-NLS-1$
            }
            if ("Cortex-M33".equals(cpu)) { //$NON-NLS-1$
                if (!CmsisConstants.DSP.equals(dsp)) {
                    return NoDSP_NoFPU;
                }
            }
            return NoFPU;
        }
        boolean dp = fpu.equals(CmsisConstants.DP_FPU);

        switch (cpu) {
        case "Cortex-M4": //$NON-NLS-1$
            return isVersion6() ? "FPv4_SP_D16" : "FPv4_SP"; //$NON-NLS-1$ //$NON-NLS-2$

        case "Cortex-M7": //$NON-NLS-1$
            if (isVersion6())
                return dp ? "FPv5_D16" : "FPv5_SP_D16"; //$NON-NLS-1$ //$NON-NLS-2$
            return dp ? "FPv5_D16" : "FPv5_SP"; //$NON-NLS-1$ //$NON-NLS-2$

        case "Cortex-R4": //$NON-NLS-1$
        case "Cortex-R5": //$NON-NLS-1$
            return "VFPv3_D16"; //$NON-NLS-1$

        case "Cortex-R7": //$NON-NLS-1$
        case "Cortex-R8": //$NON-NLS-1$
            return "VFPv3_D16_FP16"; //$NON-NLS-1$

        case "Cortex-A7": //$NON-NLS-1$
        case "Cortex-A53": //$NON-NLS-1$
        case "Cortex-A57": //$NON-NLS-1$
        case "Cortex-A72": //$NON-NLS-1$
            if (isVersion6())
                return dp ? "VFPv4" : "VFPv4.Neon"; //$NON-NLS-1$ //$NON-NLS-2$
            return dp ? "VFPv4_D16" : "VFPv4.Neon"; //$NON-NLS-1$ //$NON-NLS-2$

        case "Cortex-A5": //$NON-NLS-1$
            return dp ? "VFPv4_D16" : "VFPv4.Neon"; //$NON-NLS-1$ //$NON-NLS-2$

        case "Cortex-A8": //$NON-NLS-1$
            if (isVersion6())
                return "VFPv3.Neon"; //$NON-NLS-1$
            return "VFPv3"; //$NON-NLS-1$

        case "Cortex-A9": //$NON-NLS-1$
            if (isVersion6())
                return dp ? "VFPv3_D16_FP16" : "VFPv3_FP16.Neon"; //$NON-NLS-1$ //$NON-NLS-2$
            return dp ? "VFPv3_D16_FP16" : "VFPv3_FP16.Neon"; //$NON-NLS-1$ //$NON-NLS-2$

        case "Cortex-A15": //$NON-NLS-1$
            return dp ? "VFPv4_D16" : "VFPv4.Neon"; //$NON-NLS-1$ //$NON-NLS-2$
        case "Cortex-A12": //$NON-NLS-1$
        case "Cortex-A17": //$NON-NLS-1$
            return "VFPv4.Neon"; //$NON-NLS-1$

        case "ARMV8MML": //$NON-NLS-1$
            return "FPv5_D16"; //$NON-NLS-1$
        case "ARMV81MML": //$NON-NLS-1$
            return dp ? "FP16.FP32.FP64" : "FP16.FP32"; //$NON-NLS-1$ //$NON-NLS-2$
        case "Cortex-M55": //$NON-NLS-1$
            return "FP16.FP32.FP64"; // $NON-NLS-2$

        case "Cortex-M35P": //$NON-NLS-1$
        case "Cortex-M33": //$NON-NLS-1$
            return "FPv5_SP_D16"; //$NON-NLS-1$

        }
        return NoFPU;
    }
}
