package com.arm.cmsis.pack.build.armcc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapter;
import com.arm.cmsis.pack.common.CmsisConstants;

public class ArmToolchainForEmbeddedAdapter extends RteToolChainAdapter {

    public static final String SPACE = " "; //$NON-NLS-1$
    public static final String EQUAL = "="; //$NON-NLS-1$
    public static final String MTHUMB = "-mthumb"; //$NON-NLS-1$
    public static final String MCPU = "-mcpu"; //$NON-NLS-1$
    public static final String MFPU = "-mfpu"; //$NON-NLS-1$
    public static final String MFABI = "-mfloat-abi"; //$NON-NLS-1$
    public static final String SECURE = "secure";

    public static final String ENABLED = "1"; //$NON-NLS-1$
    public static final String DISABLED = "0"; //$NON-NLS-1$

    public static final String HARD_FLOAT_ABI = "hard"; //$NON-NLS-1$
    public static final String FPV5_SP_D16 = "fpv5-sp-d16"; //$NON-NLS-1$
    public static final String FPV5_D16 = "fpv5-d16"; //$NON-NLS-1$
    public static final String FPV4_SP_D16 = "fpv4-sp-d16"; //$NON-NLS-1$
    public static final String CORTEX_M7 = "Cortex-M7"; //$NON-NLS-1$

    public static final String ENDIAN_OPT = "com.arm.tool.c.assembler.atfe.base.option.endian"; //$NON-NLS-1$
    public static final String AUTO_ENDIAN = ENDIAN_OPT + ".auto"; //$NON-NLS-1$
    public static final String LITTLE_ENDIAN = ENDIAN_OPT + ".little"; //$NON-NLS-1$
    public static final String BIG_ENDIAN = ENDIAN_OPT + ".big"; //$NON-NLS-1$

    public static final int TARGET_OPTION = IBuildSettings.TOOLCHAIN_USER_OPTION + 21;
    public static final int SECURITY_OPTION = IBuildSettings.TOOLCHAIN_USER_OPTION + 22;

    public static final String ARM_NONE_EABI = "arm-none-eabi";

    // The C++ specific tools inherit options from the C variants, so we only need
    // to switch on the C tools which will also be set in the C++ tools.
    @Override
    protected int getRteOptionType(String id) {
        switch (id) {

        case "com.arm.tool.c.compiler.atfe.base.option.cpu": //$NON-NLS-1$
        case "com.arm.tool.c.linker.atfe.base.option.cpu": //$NON-NLS-1$
        case "com.arm.tool.c.assembler.atfe.base.option.cpu": //$NON-NLS-1$
            return IBuildSettings.CPU_OPTION;

        case "com.arm.tool.c.assembler.atfe.base.option.fpu": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.atfe.base.option.fpu": //$NON-NLS-1$ O
            return IBuildSettings.FPU_OPTION;

        case "com.arm.tool.c.assembler.atfe.base.option.fabi": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.atfe.base.option.fabi": //$NON-NLS-1$
            return IBuildSettings.FLOAT_ABI_OPTION;

        case "com.arm.tool.c.assembler.atfe.base.option.endian": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.atfe.base.option.endian": //$NON-NLS-1$
            return IBuildSettings.ENDIAN_OPTION;

        case "com.arm.tool.c.assembler.atfe.base.option.thumb": //$NON-NLS-1$
        case "com.arm.tool.c.compiler.atfe.base.option.thumb": //$NON-NLS-1$
            return IBuildSettings.THUMB_OPTION;

        case "com.arm.tool.c.compiler.atfe.base.option.other": //$NON-NLS-1$
            return IBuildSettings.CMISC_OPTION;

        case "com.arm.tool.c.assembler.atfe.base.option.other": //$NON-NLS-1$
            return IBuildSettings.AMISC_OPTION;

        case "com.arm.tool.c.linker.atfe.base.option.flags": //$NON-NLS-1$
            return IBuildSettings.LMISC_OPTION;

        case "com.arm.tool.c.linker.atfe.base.option.script": //$NON-NLS-1$
            return IBuildSettings.RTE_LINKER_SCRIPT;

        // Compiler/linker/assembler target architecture
        case "com.arm.tool.c.compiler.atfe.base.option.target":
        case "com.arm.tool.c.assembler.atfe.base.option.target":
        case "com.arm.tool.c.linker.atfe.base.option.target":
            return TARGET_OPTION;

        // Cortex-M Security Extensions (-mcmse)
        case "com.arm.tool.c.compiler.atfe.base.option.secure.mode":
            return SECURITY_OPTION;

        default:
            break;
        }
        return IBuildSettings.UNKNOWN_OPTION;
    }

    @Override
    protected void updateRteOption(int oType, IBuildObject configuration, IHoldsOptions tool, IOption option,
            IBuildSettings buildSettings) throws BuildException {
        switch (oType) {
        case IBuildSettings.LMISC_OPTION:
            updateLinkerMiscOption(configuration, tool, option, buildSettings);
            return;
        case TARGET_OPTION:
            updateBuildOption(configuration, tool, option, buildSettings, ARM_NONE_EABI);
            return;
        case SECURITY_OPTION:
            updateSecurityOption(configuration, tool, option, buildSettings);
            return;
        default:
            break;
        }
        super.updateRteOption(oType, configuration, tool, option, buildSettings);
    }

    // Cortex-M Security Extensions
    private void updateSecurityOption(IBuildObject configuration, IHoldsOptions tool, IOption option,
            IBuildSettings buildSettings) throws BuildException {
        String security = getDeviceAttribute(SECURITY_OPTION, buildSettings);
        String value = DISABLED;
        if (security != null && SECURE.equalsIgnoreCase(security.trim())) {
            value = ENABLED;
        }
        updateBuildOption(configuration, tool, option, buildSettings, value);
    }

    private void updateBuildOption(IBuildObject configuration, IHoldsOptions tool, IOption option,
            IBuildSettings buildSettings, String value) throws BuildException {
        if (configuration instanceof IConfiguration) {
            ManagedBuildManager.setOption((IConfiguration) configuration, tool, option, value);
        } else if (configuration instanceof IResourceInfo) {
            ManagedBuildManager.setOption((IResourceInfo) configuration, tool, option, value);
        }
    }

    private void updateLinkerMiscOption(IBuildObject configuration, IHoldsOptions tool, IOption option,
            IBuildSettings buildSettings) throws BuildException {

        String value = option.getStringValue();

        int pos = value.indexOf(MTHUMB);
        if (pos >= 0)
            value = value.substring(0, pos);
        if (!value.isEmpty() && !value.endsWith(SPACE))
            value += SPACE;
        value += MTHUMB;

        String cpu = getCpuOptionValue(buildSettings);
        value += SPACE + MCPU + EQUAL + cpu;

        String fpu = getFpuOptionValue(buildSettings);
        if (fpu != null && !fpu.isEmpty()) {
            value += SPACE + MFPU + EQUAL + fpu;
        }
        String floatAbi = getFloatAbiOptionValue(buildSettings);
        if (floatAbi != null && !floatAbi.isEmpty()) {
            value += SPACE + MFABI + EQUAL + floatAbi;
        }
        updateBuildOption(configuration, tool, option, buildSettings, value);
    }

    @Override
    protected String getRteOptionValue(int oType, IBuildSettings buildSettings, IOption option) {
        switch (oType) {
        case IBuildSettings.CPU_OPTION:
            return getCpuOptionValue(buildSettings);
        case IBuildSettings.THUMB_OPTION:
            // Only set when -mthumb is supplied
            return ENABLED;
        case IBuildSettings.ENDIAN_OPTION:
            return getEndianOptionValue(buildSettings);
        case IBuildSettings.FPU_OPTION:
            return getFpuOptionValue(buildSettings);
        case IBuildSettings.FLOAT_ABI_OPTION:
            return getFloatAbiOptionValue(buildSettings);
        default:
            break;
        }
        return super.getRteOptionValue(oType, buildSettings, option);
    }

    private String getCpuOptionValue(IBuildSettings buildSettings) {
        String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
        int pos = cpu.indexOf('+');
        if (pos > 0) {
            // Cortex-M0+ -> Cortex-M0plus
            cpu = cpu.substring(0, pos);
            cpu += "plus"; //$NON-NLS-1$
        }
        return cpu.toLowerCase();
    }

    @Override
    protected Collection<String> getStringListValue(IBuildSettings buildSettings, int type) {
        if (type == IBuildSettings.RTE_LIBRARIES || type == IBuildSettings.RTE_LIBRARY_PATHS) {
            return null; // we add libraries as objects => ignore libs and lib paths
        } else if (type == IBuildSettings.RTE_OBJECTS) {
            Collection<String> objs = buildSettings.getStringListValue(IBuildSettings.RTE_OBJECTS);
            List<String> value = new ArrayList<String>();
            if (objs != null && !objs.isEmpty())
                value.addAll(objs);
            Collection<String> libs = buildSettings.getStringListValue(IBuildSettings.RTE_LIBRARIES);
            if (libs != null && !libs.isEmpty())
                value.addAll(libs);
            return value;
        }
        return super.getStringListValue(buildSettings, type);
    }

    private String getFpuOptionValue(IBuildSettings buildSettings) {
        String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
        String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
        if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu))
            return CmsisConstants.EMPTY_STRING;
        if (cpu.equals(CORTEX_M7)) {
            if (fpu.equals(CmsisConstants.SP_FPU))
                return FPV5_SP_D16;
            if (fpu.equals(CmsisConstants.DP_FPU))
                return FPV5_D16;
        } else if (fpu.equals(CmsisConstants.SP_FPU)) {
            return FPV4_SP_D16;
        }
        return null;
    }

    private String getFloatAbiOptionValue(IBuildSettings buildSettings) {
        String cpu = getDeviceAttribute(IBuildSettings.CPU_OPTION, buildSettings);
        String fpu = getDeviceAttribute(IBuildSettings.FPU_OPTION, buildSettings);
        if (cpu == null || fpu == null || fpu.equals(CmsisConstants.NO_FPU) || !coreHasFpu(cpu))
            return CmsisConstants.EMPTY_STRING;
        return HARD_FLOAT_ABI;
    }

    private String getEndianOptionValue(IBuildSettings buildSettings) {
        String endian = getDeviceAttribute(IBuildSettings.ENDIAN_OPTION, buildSettings);
        if (endian == null) {
            return AUTO_ENDIAN;
        }
        if (endian.equalsIgnoreCase(CmsisConstants.LITTLENDIAN)) {
            return LITTLE_ENDIAN;
        } else if (endian.equalsIgnoreCase(CmsisConstants.BIGENDIAN)) {
            return BIG_ENDIAN;
        }
        return AUTO_ENDIAN;
    }
}
