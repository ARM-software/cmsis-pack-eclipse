/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/
package com.arm.cmsis.pack.build;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * This interface contains build settings obtained from RTE configuration<br>
 * It provides methods with common types to avoid dependencies on
 * IRteConfiguration internal details,
 */
public interface IBuildSettings extends IAttributes {

    /**
     * Retrieves setting value as collection of strings (defines, include paths,
     * etc)
     *
     * @param type a type of setting to retrieve, see most commonly used type
     *             descriptions below
     * @return the settings value as collection of strings or <code>null</code> if
     *         there is no string list for that type
     * @see IOption
     */
    Collection<String> getStringListValue(int type);

    /**
     * Adds a value string list entry to corresponding collection
     *
     * @param type  a type of setting to add, see most commonly used type
     *              descriptions below
     * @param value value to add
     */
    void addStringListValue(int type, String value);

    /**
     * Retrieves setting value as a string, returning the first (usually single)
     * element from the list
     *
     * @param type type of setting to retrieve, see most commonly used type
     *             descriptions below
     * @return the settings value string or <code>null</code> if there is no string
     *         list for that type or that list is empty
     */
    default String getSingleStringValue(int type) {
        Collection<String> strings = getStringListValue(type);
        if (strings == null || strings.isEmpty())
            return null;
        return strings.iterator().next();
    }

// individual build settings for
    enum Level {
        PROJECT, // project level settings(default)
        FOLDER, // folder level for a physical folder
        VIRTUAL_GROUP, // a group combining files and folders from different hierarchy levels, e.g. a
                       // software component
        FILE // file level
    }

    /**
     * Returns level of this build settings apply to
     *
     * @return IBuildSettings.Lebvel enum, default is PROJECT
     */
    default Level getLevel() {
        return Level.PROJECT;
    }

    /**
     * Returns individual build settings for resources (folders and files) and
     * virtual groups
     *
     * @return Map<String, IBuildSettings> where key is a resource path relative to
     *         the project
     */
    Map<String, IBuildSettings> getChildren();

    /**
     * Returns individual build settings for given resource or virtual group
     *
     * @param resourcePath file or folder path relative to the project or virtual
     *                     group name
     * @return IBuildSettings or null if no individual settings are available for
     *         given resource
     */
    IBuildSettings getBuildSettings(String resourcePath);

    /**
     * Ensures individual IBuildSettings object is allocated for given resource or
     * virtual group
     *
     * @param resourcePath file or folder path relative to the project or virtual
     *                     group name
     * @return created/existing IBuildSettings object
     */
    IBuildSettings createBuildSettings(String name, Level level);

    /**
     * Returns parent settings of individual one
     *
     * @return parent IBuildSettings if available, null otherwise
     */
    default IBuildSettings getParentSettings() {
        return null;
    }

    /**
     * Retrieves attribute of selected device<br>
     * See: <a href=
     * "https://arm-software.github.io/CMSIS_5/Pack/html/pdsc_family_pg.html#element_processor">"https://arm-software.github.io/CMSIS_5/Pack/html/pdsc_family_pg.html#element_processor"</a><br>
     *
     * @param key processor attribute name or one of: "Dname", "Dfamily",
     *            "DsubFamily", "Dvariant"
     * @return device attribute
     * @note the returned value in most cases cannot be set to an {@link IOption}
     *       directly<br>
     *       it should be converted to toolchain-specific value(s) first
     */
    String getDeviceAttribute(String key);

    /**
     * Returns single linker script file (or scatter file)
     *
     * @return linker script the very first occurrence of the file if any
     */
    String getSingleLinkerScriptFile();

    /**
     * Checks if project uses Device Startup component
     *
     * @return true if component is used
     */
    default boolean usesDeviceStartup() {
        return getAttributeAsBoolean(CmsisConstants.Device_Startup, false);
    }

    /**
     * Checks if project uses CMSIS Core component
     *
     * @return true if component is used
     */
    default boolean usesCmsisCore() {
        return getAttributeAsBoolean(CmsisConstants.CMSIS_Core, false);
    }

    /**
     * Checks if project uses a CMSIS RTOS component
     *
     * @return true if component is used
     */
    default boolean usesCmsisRtos() {
        return getAttributeAsBoolean(CmsisConstants.CMSIS_RTOS, false);
    }

    // several build-specific string constants could appear in pdsc file:
    public static final String AMISC_TAG = "AMisc"; //$NON-NLS-1$
    public static final String CMISC_TAG = "CMisc"; //$NON-NLS-1$
    public static final String CPPMISC_TAG = "CPPMisc"; //$NON-NLS-1$
    public static final String LMISC_TAG = "LMisc"; //$NON-NLS-1$
    public static final String ARMISC_TAG = "ARMisc"; //$NON-NLS-1$
    public static final String PRE_BUILD_TAG = "preBuild"; //$NON-NLS-1$
    public static final String POST_BUILD_TAG = "postBuild"; //$NON-NLS-1$

    // RTE option types most commonly used by getStringListValue(),
    // addStringListValue() and RteToolChainAdapter.getRteOptionType()
    // not implemented as enum to be easily extensible
    public static final int UNKNOWN_OPTION = -1;
    // all other options start with 100
    public static final int RTE_OPTION = 100;
    // options provided by RTE configuration, updated every time project loaded or
    // component added/removed
    public static final int RTE_DEFINES = RTE_OPTION + 1; // option to hold RTE preprocessor definitions
    public static final int RTE_INCLUDE_PATH = RTE_OPTION + 2; // option to hold RTE includes
    public static final int RTE_LIBRARIES = RTE_OPTION + 3; // option to hold RTE libs
    public static final int RTE_LIBRARY_PATHS = RTE_OPTION + 4; // option to hold RTE lib paths
    public static final int RTE_OBJECTS = RTE_OPTION + 5; // option to hold RTE additional object files
    public static final int RTE_LINKER_SCRIPT = RTE_OPTION + 6; // linker script/scatter file
    public static final int RTE_PRE_INCLUDES = RTE_OPTION + 7; // option to hold RTE pre-include files

    // user-defined options (defined in derived RteConfiguration or
    // IEnvironnmentProvider) should start here
    public static final int RTE_USER_OPTION = RTE_OPTION + 10;

    // options that could be set during project creation, device change or forced
    // option restore.
    public static final int RTE_INITIAL_OPTION = RTE_OPTION + 100;

    public static final int RTE_CMISC = RTE_INITIAL_OPTION + 2; // RTE C compiler miscellaneous
    public static final int RTE_CPPMISC = RTE_INITIAL_OPTION + 3; // RTE CPP compiler miscellaneous
    public static final int RTE_ASMMISC = RTE_INITIAL_OPTION + 4; // RTE assembler miscellaneous
    public static final int RTE_ARMISC = RTE_INITIAL_OPTION + 5; // RTE archiver (librarian) miscellaneous
    public static final int RTE_LMISC = RTE_INITIAL_OPTION + 6; // RTE linker miscellaneous

    // options that are specific to toolchain integration
    // most commonly used options are listed here (it is not mandatory to use
    // all/any of them)
    public static final int TOOL_CHAIN_OPTION = RTE_INITIAL_OPTION + 100;

    public static final int CDEFINES_OPTION = TOOL_CHAIN_OPTION + 3; // C compiler preprocessor definitions (editable)
    public static final int CPPDEFINES_OPTION = TOOL_CHAIN_OPTION + 4; // CPP compiler preprocessor definitions
                                                                       // (editable)
    public static final int ADEFINES_OPTION = TOOL_CHAIN_OPTION + 5; // assembler compiler preprocessor definitions
                                                                     // (editable)

    public static final int CINCPATHS_OPTION = TOOL_CHAIN_OPTION + 6; // C compiler include paths (editable)
    public static final int CPPINCPATHS_OPTION = TOOL_CHAIN_OPTION + 7; // CPP compiler include paths (editable)
    public static final int ASMINCPATHS_OPTION = TOOL_CHAIN_OPTION + 8; // assembler include paths (editable)

    public static final int LIBS_OPTION = TOOL_CHAIN_OPTION + 9; // libraries for linker
    public static final int LIBPATH_OPTION = TOOL_CHAIN_OPTION + 10; // library paths for linker
    public static final int OBJECTS_OPTION = TOOL_CHAIN_OPTION + 11; // objects for linker

    public static final int CMISC_OPTION = TOOL_CHAIN_OPTION + 12; // C compiler miscellaneous
    public static final int CPPMISC_OPTION = TOOL_CHAIN_OPTION + 13; // CPP compiler miscellaneous
    public static final int AMISC_OPTION = TOOL_CHAIN_OPTION + 14; // assembler miscellaneous
    public static final int ARMISC_OPTION = TOOL_CHAIN_OPTION + 15; // archiver (librarian) miscellaneous
    public static final int LMISC_OPTION = TOOL_CHAIN_OPTION + 16; // linker miscellaneous

    public static final int CUNDEFINES_OPTION = TOOL_CHAIN_OPTION + 17; // C compiler preprocessor definitions:
                                                                        // Undefined option
    public static final int AUNDEFINES_OPTION = TOOL_CHAIN_OPTION + 18; // assembler preprocessor definitions: Undefined
                                                                        // option

    public static final int C5_ASMINCPATHS_OPTION = TOOL_CHAIN_OPTION + 19; // assembler include paths (editable)
    public static final int C6_ASMINCPATHS_OPTION = TOOL_CHAIN_OPTION + 20; // assembler include paths (editable)
// pre/post build steps
    public static final int PRE_BUILD_STEPS = TOOL_CHAIN_OPTION + 1; // pre-build steps
    public static final int POST_BUILD_STEPS = TOOL_CHAIN_OPTION + 2; // post-build steps
    public static final int BUILD_USER = 32; // user pre/post post-build steps
    public static final int PRE_BUILD_STEPS_USER = PRE_BUILD_STEPS + BUILD_USER; // user pre-build steps
    public static final int POST_BUILD_STEPS_USER = POST_BUILD_STEPS + BUILD_USER; // user post-build steps

    // options specific to target device
    public static final int TOOLCHAIN_DEVICE_OPTION = TOOL_CHAIN_OPTION + 40;
    public static final int CPU_OPTION = TOOLCHAIN_DEVICE_OPTION + 1;
    public static final int ARCH_OPTION = TOOLCHAIN_DEVICE_OPTION + 2; // architecture
    public static final int INSTR_SET_OPTION = TOOLCHAIN_DEVICE_OPTION + 3; // instruction set
    public static final int THUMB_OPTION = TOOLCHAIN_DEVICE_OPTION + 4; // thumb option if separate from instruction set
    public static final int ENDIAN_OPTION = TOOLCHAIN_DEVICE_OPTION + 5;
    public static final int FPU_OPTION = TOOLCHAIN_DEVICE_OPTION + 6;
    public static final int FLOAT_ABI_OPTION = TOOLCHAIN_DEVICE_OPTION + 7;
    public static final int DSP_OPTION = TOOLCHAIN_DEVICE_OPTION + 8;
    public static final int MVE_OPTION = TOOLCHAIN_DEVICE_OPTION + 9;

    // initial toolchain-specific options (defined in derived toolchain adapters)
    // should start here
    public static final int TOOLCHAIN_USER_OPTION = TOOL_CHAIN_OPTION + 100;

    /**
     * Converts option tag to option type used by build Settings
     *
     * @param tag String option type
     * @return integer build type
     */
    static int getMiscOptionType(String tag) {
        switch (tag) {
        case AMISC_TAG:
            return RTE_ASMMISC;
        case CMISC_TAG:
            return RTE_CMISC;
        case CPPMISC_TAG:
            return RTE_CPPMISC;
        case LMISC_TAG:
            return RTE_LMISC;
        case IBuildSettings.ARMISC_TAG:
            return RTE_ARMISC;
        default:
            break;
        }
        // pre and post build steps in packs are often given with a suffix like
        // preBuild1, preBuild2, etc.
        if (tag.startsWith(PRE_BUILD_TAG))
            return PRE_BUILD_STEPS;
        else if (tag.startsWith(IBuildSettings.POST_BUILD_TAG))
            return POST_BUILD_STEPS;

        return UNKNOWN_OPTION;
    }

}
