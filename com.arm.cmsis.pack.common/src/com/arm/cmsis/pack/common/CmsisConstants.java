/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.common;

/**
 * Utility class providing CMSIS constants: tags, attribute names, delimiters,
 * etc.
 */
public class CmsisConstants {

    /**
     * Empty string used as "null-object" to avoid using null strings
     */
    public static final String EMPTY_STRING = ""; //$NON-NLS-1$
    public static final String ONE = "1"; //$NON-NLS-1$
    public static final String ZERO = "0"; //$NON-NLS-1$
    public static final String ZERO8 = "00000000"; // 8 zeros //$NON-NLS-1$
    public static final String ZERO16 = ZERO8 + ZERO8;
    public static final String ZEROX = "0x"; //$NON-NLS-1$
    public static final String ZEROX8 = ZEROX + ZERO8;
    public static final String ZEROX16 = ZEROX + ZERO16;

    public static final String SLASH = "/"; //$NON-NLS-1$
    public static final String BACKSLASH = "\\"; //$NON-NLS-1$

    public static final String SPACE = " "; //$NON-NLS-1$
    public static final String SPACES4 = "    "; // 4 spaces //$NON-NLS-1$
    public static final String SPACES8 = SPACES4 + SPACES4;
    public static final String SPACES16 = SPACES8 + SPACES8;
    public static final String SPACES32 = SPACES16 + SPACES16;

    public static final String COLON = ":"; //$NON-NLS-1$
    public static final String DOUBLE_COLON = "::"; //$NON-NLS-1$
    public static final String COMMA = ","; //$NON-NLS-1$
    public static final String POINT = "."; //$NON-NLS-1$
    public static final String QUOTE = "\""; //$NON-NLS-1$
    public static final String EQUAL = "="; //$NON-NLS-1$
    public static final String DOT = "."; //$NON-NLS-1$
    public static final String ASTERISK = "*"; //$NON-NLS-1$
    public static final String QUESTION = "?"; //$NON-NLS-1$
    public static final String UNDERSCORE = "_"; //$NON-NLS-1$
    public static final String MINUS = "-"; //$NON-NLS-1$

    public static final String ARM = "ARM"; //$NON-NLS-1$
    public static final String CORTEX = "Cortex"; //$NON-NLS-1$
    public static final String CMSIS = "CMSIS"; //$NON-NLS-1$
    public static final String PACK = "pack"; //$NON-NLS-1$

    public static final String PACK_ID = "packId"; //$NON-NLS-1$

    // commonly used values for component classes and groups
    public static final String RTOS = "RTOS"; //$NON-NLS-1$
    public static final String Core = "Core"; //$NON-NLS-1$
    public static final String Device = "Device"; //$NON-NLS-1$
    public static final String Startup = "Startup";//$NON-NLS-1$
    public static final String Device_Startup = Device + '_' + Startup;
    public static final String CMSIS_Core = CMSIS + '_' + Core;
    public static final String CMSIS_RTOS = CMSIS + '_' + RTOS;

    // commonly used tags and attributes
    public static final String CONDITION = "condition"; //$NON-NLS-1$
    public static final String VENDOR = "vendor"; //$NON-NLS-1$
    public static final String VERSION = "version"; //$NON-NLS-1$
    public static final String URL = "url"; //$NON-NLS-1$
    public static final String NAME = "name"; //$NON-NLS-1$
    public static final String ID = "id"; //$NON-NLS-1$
    public static final String DOC = "doc"; //$NON-NLS-1$
    public static final String DESCRIPTION = "description"; //$NON-NLS-1$
    public static final String FILE_NAME = "filename"; //$NON-NLS-1$

    // condition expressions
    public static final String ACCEPT = "accept"; //$NON-NLS-1$
    public static final String REQUIRE = "require"; //$NON-NLS-1$
    public static final String DENY = "deny"; //$NON-NLS-1$

    // attribute keys
    public static final String C_ATTRIBUTE_PREFIX = "C"; //$NON-NLS-1$
    public static final String D_ATTRIBUTE_PREFIX = "D"; //$NON-NLS-1$
    public static final String T_ATTRIBUTE_PREFIX = "T"; //$NON-NLS-1$

    public static final String CBUNDLE = "Cbundle"; //$NON-NLS-1$
    public static final String CBUNDLEVERSION = "Cbundleversion"; //$NON-NLS-1$

    public static final String CGROUP = "Cgroup"; //$NON-NLS-1$
    public static final String CCLASS = "Cclass"; //$NON-NLS-1$
    public static final String CSUB = "Csub"; //$NON-NLS-1$
    public static final String CVARIANT = "Cvariant"; //$NON-NLS-1$
    public static final String CVENDOR = "Cvendor"; //$NON-NLS-1$
    public static final String CVERSION = "Cversion"; //$NON-NLS-1$
    public static final String CAPIVERSION = "Capiversion"; //$NON-NLS-1$
    public static final String EXCLUSIVE = "exclusive"; //$NON-NLS-1$
    public static final String CUSTOM = "custom"; //$NON-NLS-1$
    public static final String MAX_INSTANCES = "maxInstances"; //$NON-NLS-1$
    public static final String IS_DEFAULT_VARIANT = "isDefaultVariant"; //$NON-NLS-1$

    public static final String BNAME = "Bname"; //$NON-NLS-1$
    public static final String BVENDOR = "Bvendor"; //$NON-NLS-1$
    public static final String BREVISION = "Brevision"; //$NON-NLS-1$

    public static final String DNAME = "Dname"; //$NON-NLS-1$
    public static final String DFAMILY = "Dfamily"; //$NON-NLS-1$
    public static final String DSUBFAMILY = "DsubFamily"; //$NON-NLS-1$
    public static final String DVARIANT = "Dvariant"; //$NON-NLS-1$
    public static final String DVENDOR = "Dvendor"; //$NON-NLS-1$
    public static final String DVERSION = "Dversion"; //$NON-NLS-1$
    public static final String DCORE = "Dcore"; //$NON-NLS-1$
    public static final String DFPU = "Dfpu"; //$NON-NLS-1$
    public static final String DMPU = "Dmpu"; //$NON-NLS-1$
    public static final String DDSP = "Ddsp"; //$NON-NLS-1$
    public static final String DTZ = "Dtz"; //$NON-NLS-1$
    public static final String DSECURE = "Dsecure"; //$NON-NLS-1$
    public static final String DENDIAN = "Dendian"; //$NON-NLS-1$
    public static final String DCLOCK = "Dclock"; //$NON-NLS-1$
    public static final String DMVE = "Dmve"; //$NON-NLS-1$
    public static final String DCDECP = "Dcdecp"; //$NON-NLS-1$

    public static final String NO_VENDOR = "NO_VENDOR:0"; //$NON-NLS-1$
    public static final String NO_MCU = "NO_MCU"; //$NON-NLS-1$

    public static final String MVE = "MVE"; //$NON-NLS-1$
    public static final String NO_MVE = "NO_MVE"; //$NON-NLS-1$
    public static final String FP_MVE = "FP_MVE"; //$NON-NLS-1$
    public static final String DnumMpuRegions = "DnumMpuRegions";//$NON-NLS-1$
    public static final String DnumInterrupts = "DnumInterrupts"; //$NON-NLS-1$
    public static final String DnumSauRegions = "DnumSauRegions"; //$NON-NLS-1$

    public static final String CATEGORY = "category"; //$NON-NLS-1$
    public static final String ATTR = "attr"; //$NON-NLS-1$
    public static final String PATH = "path"; //$NON-NLS-1$

    public static final String HEADER = "header"; //$NON-NLS-1$
    public static final String INCLUDE = "include"; //$NON-NLS-1$
    public static final String SOURCE = "source"; //$NON-NLS-1$
    public static final String SOURCE_ASM = "sourceAsm"; //$NON-NLS-1$
    public static final String SOURCE_C = "sourceC"; //$NON-NLS-1$
    public static final String SOURCE_CPP = "sourceCpp"; //$NON-NLS-1$
    public static final String LIBRARY = "library"; //$NON-NLS-1$
    public static final String OBJECT = "object"; //$NON-NLS-1$
    public static final String SRC = "src"; //$NON-NLS-1$
    public static final String SVD = "svd"; //$NON-NLS-1$
    public static final String SDF = "sdf"; //$NON-NLS-1$
    public static final String IMAGE = "image"; //$NON-NLS-1$
    public static final String UTILITY = "utility"; //$NON-NLS-1$
    public static final String LINKER_SCRIPT = "linkerScript"; //$NON-NLS-1$
    public static final String PRE_INCLUDE_GLOBAL = "preIncludeGlobal"; //$NON-NLS-1$
    public static final String PRE_INCLUDE_LOCAL = "preIncludeLocal"; //$NON-NLS-1$

    public static final String DEFINE = "define"; //$NON-NLS-1$
    public static final String PDEFINE = "Pdefine"; //$NON-NLS-1$
    public static final String SELECT = "select"; //$NON-NLS-1$
    public static final String SHOW = "show"; //$NON-NLS-1$
    public static final String ACCESS = "access"; //$NON-NLS-1$
    public static final String SECURITY = "security"; //$NON-NLS-1$
    public static final String PRIVILEGE = "privilege"; //$NON-NLS-1$
    public static final String PRIVILEGED = "privileged"; //$NON-NLS-1$

    public static final String PNAME = "Pname"; //$NON-NLS-1$
    public static final String PREF = "Pref"; //$NON-NLS-1$
    public static final String PUNIT = "Punit"; //$NON-NLS-1$
    public static final String PUNITS = "Punits"; //$NON-NLS-1$

    public static final String TCOMPILER = "Tcompiler"; //$NON-NLS-1$
    public static final String TOPTIONS = "Toptions"; //$NON-NLS-1$
    public static final String TOUTPUT = "Toutput"; //$NON-NLS-1$

    public static final String TOUTPUT_EXE = "exe"; //$NON-NLS-1$
    public static final String TOUTPUT_LIB = "lib"; //$NON-NLS-1$

    public static final String THOST = "Thost"; //$NON-NLS-1$
    public static final String TENVIRONMENT = "Tenvironment"; //$NON-NLS-1$
    public static final String TECLIPSE = "Teclipse"; //$NON-NLS-1$

    public static final String HOST = "host"; //$NON-NLS-1$
    public static final String ALL = "all"; //$NON-NLS-1$
    public static final String WIN = "win"; //$NON-NLS-1$
    public static final String LINUX = "linux"; //$NON-NLS-1$
    public static final String MAC = "mac"; //$NON-NLS-1$
    public static final String NIX = "nix"; //$NON-NLS-1$
    public static final String NUX = "nux"; //$NON-NLS-1$

    public static final String EXE = "exe"; //$NON-NLS-1$
    public static final String ECLIPSE = "eclipse"; //$NON-NLS-1$
    public static final String WEB = "web"; //$NON-NLS-1$

    public static final String PLUGIN = "plugin"; //$NON-NLS-1$
    public static final String CLASS = "class"; //$NON-NLS-1$
    public static final String METHOD = "method"; //$NON-NLS-1$

    public static final String VERSION_MODE = "versionMatchMode";//$NON-NLS-1$

    public static final String FOLDER = "folder"; //$NON-NLS-1$
    public static final String LOAD = "load"; //$NON-NLS-1$

    public static final String DEPRECATED = "deprecated"; //$NON-NLS-1$
    public static final String REPLACEMENT = "replacement"; //$NON-NLS-1$

    // attribute values
    public static final String DATE = "date"; //$NON-NLS-1$

    public static final String BIGENDIAN = "Big-endian"; //$NON-NLS-1$
    public static final String LITTLENDIAN = "Little-endian"; //$NON-NLS-1$
    public static final String CONFIGENDIAN = "Configurable"; //$NON-NLS-1$

    public static final String LATEST = "latest"; //$NON-NLS-1$
    public static final String FIXED = "fixed"; //$NON-NLS-1$
    public static final String EXCLUDED = "excluded"; //$NON-NLS-1$

    public static final String FPU = "FPU"; //$NON-NLS-1$
    public static final String NO_FPU = "NO_FPU"; //$NON-NLS-1$
    public static final String SP_FPU = "SP_FPU"; //$NON-NLS-1$
    public static final String DP_FPU = "DP_FPU"; //$NON-NLS-1$
    public static final String MPU = "MPU"; //$NON-NLS-1$
    public static final String NO_MPU = "NO_MPU"; //$NON-NLS-1$

    public static final String TZ = "TZ"; //$NON-NLS-1$
    public static final String NO_TZ = "NO_TZ"; //$NON-NLS-1$

    public static final String DSP = "DSP"; //$NON-NLS-1$
    public static final String NO_DSP = "NO_DSP"; //$NON-NLS-1$

    public static final String secure = "secure"; //$NON-NLS-1$
    public static final String SECURE = "Secure"; //$NON-NLS-1$
    public static final String NON_SECURE = "Non-secure"; //$NON-NLS-1$
    public static final String TZ_DISABLED = "TZ-disabled"; //$NON-NLS-1$

    public static final String COPY = "copy"; //$NON-NLS-1$
    public static final String CONFIG = "config"; //$NON-NLS-1$
    public static final String TEMPLATE = "template"; //$NON-NLS-1$
    public static final String INTERFACE = "interface"; //$NON-NLS-1$
    public static final String pINSTANCEp = "%Instance%"; //$NON-NLS-1$

    // tags
    public static final String API_TAG = "api"; //$NON-NLS-1$
    public static final String APIS_TAG = "apis"; //$NON-NLS-1$
    public static final String BUNDLE_TAG = "bundle"; //$NON-NLS-1$
    public static final String COMPONENT_TAG = "component"; //$NON-NLS-1$
    public static final String COMPONENTS_TAG = "components"; //$NON-NLS-1$
    public static final String CONDITIONS_TAG = "conditions"; //$NON-NLS-1$
    public static final String RELEASE_TAG = "release"; //$NON-NLS-1$
    public static final String RELEASES_TAG = "releases"; //$NON-NLS-1$
    public static final String DEVICE_TAG = "device"; //$NON-NLS-1$
    public static final String DEVICES_TAG = "devices"; //$NON-NLS-1$
    public static final String DEVICES = "Devices"; //$NON-NLS-1$
    public static final String FAMILY_TAG = "family"; //$NON-NLS-1$
    public static final String SUBFAMILY_TAG = "subFamily"; //$NON-NLS-1$
    public static final String VARIANT_TAG = "variant"; //$NON-NLS-1$
    public static final String FILE_TAG = "file"; //$NON-NLS-1$
    public static final String FILES_TAG = "files"; //$NON-NLS-1$
    public static final String PACKAGE_TAG = "package"; //$NON-NLS-1$
    public static final String PACKAGES_TAG = "packages"; //$NON-NLS-1$
    public static final String PROCESSOR_TAG = "processor"; //$NON-NLS-1$
    public static final String TAXONOMY_TAG = "taxonomy"; //$NON-NLS-1$
    public static final String EXAMPLE_TAG = "example"; //$NON-NLS-1$
    public static final String EXAMPLES_TAG = "examples"; //$NON-NLS-1$
    public static final String REQUIREMENTS_TAG = "requirements"; //$NON-NLS-1$

    public static final String TEXT = "text"; //$NON-NLS-1$

    public static final String RTE_COMPONENTS_H = "RTE_Components_h"; //$NON-NLS-1$
    public static final String PRE_INCLUDE_GLOBAL_H = "Pre_Include_Global_h"; //$NON-NLS-1$
    public static final String PRE_INCLUDE_LOCAL_COMPONENT_H = "Pre_Include_Local_Component_h"; //$NON-NLS-1$

    public static final String RTE = "RTE"; //$NON-NLS-1$
    public static final String RTEDIR = "RTE/"; //$NON-NLS-1$
    public static final String _RTE_ = "_RTE_"; //$NON-NLS-1$
    public static final String RTECONFIG = "rteconfig"; //$NON-NLS-1$
    public static final String DOT_RTECONFIG = ".rteconfig"; //$NON-NLS-1$
    public static final String DOT_LAUNCH = ".launch"; //$NON-NLS-1$
    public static final String PROJECT_TAG = "project"; //$NON-NLS-1$
    public static final String LICENSE_TAG = "license"; //$NON-NLS-1$
    public static final String DEVICE_SPECIFIC = "Device Specific"; //$NON-NLS-1$
    public static final String GENERIC = "Generic"; //$NON-NLS-1$
    public static final String PREVIOUS = "Previous"; //$NON-NLS-1$
    public static final String Reset_Handler = "Reset_Handler"; //$NON-NLS-1$
    public static final String Vectors = "Vectors"; //$NON-NLS-1$

    public static final String DOT_XSD = ".xsd"; //$NON-NLS-1$

    public static final String DOT_SZONE = ".szone"; //$NON-NLS-1$
    public static final String DOT_PZONE = ".pzone"; //$NON-NLS-1$
    public static final String DOT_AZONE = ".azone"; //$NON-NLS-1$
    public static final String DOT_RZONE = ".rzone"; //$NON-NLS-1$
    public static final String DOT_FZONE = ".fzone"; //$NON-NLS-1$
    public static final String DOT_FTL = ".ftl"; //$NON-NLS-1$
    public static final String FTL = "ftl"; //$NON-NLS-1$
    public static final String FTL_GEN = "ftl_gen"; //$NON-NLS-1$

    // gpdsc-related property tags and attributes
    public static final String GENERATOR = "generator"; //$NON-NLS-1$
    public static final String GENERATOR_TAG = GENERATOR;
    public static final String GENERATORS_TAG = "generators"; //$NON-NLS-1$
    public static final String GENERATED = "generated"; //$NON-NLS-1$
    public static final String COMMAND = "command"; //$NON-NLS-1$
    public static final String COMMAND_TAG = COMMAND;
    public static final String ARGUMENT_TAG = "argument"; //$NON-NLS-1$
    public static final String ARGUMENTS_TAG = "arguments"; //$NON-NLS-1$
    public static final String SWITCH = "switch"; //$NON-NLS-1$
    public static final String GPDSC_TAG = "gpdsc"; //$NON-NLS-1$
    public static final String GPDSCS_TAG = "gpdscs"; //$NON-NLS-1$
    public static final String WORKING_DIR_TAG = "workingDir"; //$NON-NLS-1$
    public static final String PROJECT_FILES_TAG = "project_files"; //$NON-NLS-1$

    // device property tags and attributes
    public static final String DEBUG_TAG = "debug"; //$NON-NLS-1$
    public static final String DEBUGCONFIG_TAG = "debugconfig";//$NON-NLS-1$
    public static final String DEBUGVARS_TAG = "debugvars"; //$NON-NLS-1$
    public static final String ALGORITHM_TAG = "algorithm"; //$NON-NLS-1$
    public static final String ENVIRONMENT_TAG = "environment";//$NON-NLS-1$
    public static final String TRACE_TAG = "trace"; //$NON-NLS-1$
    public static final String COMPILE_TAG = "compile"; //$NON-NLS-1$
    public static final String MEMORY_TAG = "memory"; //$NON-NLS-1$
    public static final String FEATURE_TAG = "feature"; //$NON-NLS-1$
    public static final String BOOK_TAG = "book"; //$NON-NLS-1$
    public static final String BLOCK_TAG = "block"; //$NON-NLS-1$
    public static final String CONTROL_TAG = "control"; //$NON-NLS-1$
    public static final String SEQUENCES_TAG = "sequences"; //$NON-NLS-1$
    public static final String SEQUENCE_TAG = "sequence"; //$NON-NLS-1$
    public static final String DATAPATCH_TAG = "datapatch"; //$NON-NLS-1$
    public static final String DEBUGPORT_TAG = "debugport"; //$NON-NLS-1$

    public static final String JTAG = "jtag"; //$NON-NLS-1$
    public static final String CJTAG = "cjtag"; //$NON-NLS-1$
    public static final String SWD = "swd"; //$NON-NLS-1$

    public static final String IDCODE = "idcode"; //$NON-NLS-1$
    public static final String TARGETSEL = "targetsel"; //$NON-NLS-1$
    public static final String TAPINDEX = "tapindex"; //$NON-NLS-1$
    public static final String IRLEN = "irlen"; //$NON-NLS-1$

    public static final String SWJ = "swj"; //$NON-NLS-1$
    public static final String DORMANT = "dormant"; //$NON-NLS-1$

    public static final String __DP = "__dp"; //$NON-NLS-1$
    public static final String __AP = "__ap"; //$NON-NLS-1$
    public static final String DP = "AP"; //$NON-NLS-1$
    public static final String AP = "DP"; //$NON-NLS-1$
    public static final String MEM = "Mem"; //$NON-NLS-1$

    public static final String CONFIGFILE = "configfile"; //$NON-NLS-1$

    public static final String ATOMIC = "atomic"; //$NON-NLS-1$
    public static final String IF = "if"; //$NON-NLS-1$
    public static final String WHILE = "while"; //$NON-NLS-1$
    public static final String DISABLE = "disable"; //$NON-NLS-1$
    public static final String TIMEOUT = "timeout"; //$NON-NLS-1$

    public static final String START = "start"; //$NON-NLS-1$
    public static final String END = "end"; //$NON-NLS-1$
    public static final String START_S = "start_s"; //$NON-NLS-1$
    public static final String LOGICAL = "logical"; //$NON-NLS-1$
    public static final String PHYSICAL = "physical"; //$NON-NLS-1$

    public static final String OFFSET = "offset"; //$NON-NLS-1$
    public static final String SIZE = "size"; //$NON-NLS-1$
    public static final String STARTUP = "startup"; //$NON-NLS-1$
    public static final String DEFAULT = "default"; //$NON-NLS-1$
    public static final String INIT = "init"; //$NON-NLS-1$ // deprecated, use UNINIT
    public static final String UNINIT = "uninit"; //$NON-NLS-1$
    public static final String ADDRESS = "address"; //$NON-NLS-1$
    public static final String VALUE = "value"; //$NON-NLS-1$
    public static final String MASK = "mask"; //$NON-NLS-1$
    public static final String INFO = "info"; //$NON-NLS-1$
    public static final String LINKER_CONTROL = "linker_control"; //$NON-NLS-1$
    public static final String CLOCK = "clock"; //$NON-NLS-1$
    public static final String TYPE = "type"; //$NON-NLS-1$
    public static final String KEY = "key"; //$NON-NLS-1$
    public static final String ALIAS = "alias"; //$NON-NLS-1$
    public static final String INDEX = "index"; //$NON-NLS-1$
    public static final String DMA = "dma"; //$NON-NLS-1$
    public static final String EXTERNAL = "external"; //$NON-NLS-1$
    public static final String SHARED = "shared"; //$NON-NLS-1$

    public static final String RAMSTART = "RAMstart"; //$NON-NLS-1$
    public static final String RAMSIZE = "RAMsize"; //$NON-NLS-1$

    public static final String RAM = "RAM"; //$NON-NLS-1$
    public static final String ROM = "ROM"; //$NON-NLS-1$
    public static final String ROM_TAG = "rom"; //$NON-NLS-1$

    public static final String IRAM = "IRAM"; //$NON-NLS-1$
    public static final String IRAM1 = "IRAM1"; //$NON-NLS-1$
    public static final String IRAM2 = "IRAM2"; //$NON-NLS-1$
    public static final String IRAM3 = "IRAM3"; //$NON-NLS-1$
    public static final String IRAM4 = "IRAM4"; //$NON-NLS-1$
    public static final String IRAM5 = "IRAM5"; //$NON-NLS-1$
    public static final String IRAM6 = "IRAM6"; //$NON-NLS-1$
    public static final String IRAM7 = "IRAM7"; //$NON-NLS-1$
    public static final String IRAM8 = "IRAM8"; //$NON-NLS-1$

    public static final String IROM = "IROM"; //$NON-NLS-1$
    public static final String IROM1 = "IROM1"; //$NON-NLS-1$
    public static final String IROM2 = "IROM2"; //$NON-NLS-1$
    public static final String IROM3 = "IROM3"; //$NON-NLS-1$
    public static final String IROM4 = "IROM4"; //$NON-NLS-1$
    public static final String IROM5 = "IROM5"; //$NON-NLS-1$
    public static final String IROM6 = "IROM6"; //$NON-NLS-1$
    public static final String IROM7 = "IROM7"; //$NON-NLS-1$
    public static final String IROM8 = "IROM8"; //$NON-NLS-1$

    public static final String ARMCC = "ARMCC"; //$NON-NLS-1$

    public static final String TITLE = "title"; //$NON-NLS-1$
    public static final String ERRORS = "ERRORS"; //$NON-NLS-1$
    // board tags and attributes
    public static final String ALL_BOARDS = "All Boards"; //$NON-NLS-1$
    public static final String ALL_DEVICES = "All Devices"; //$NON-NLS-1$
    public static final String NO_BOARD = "- No Board -"; //$NON-NLS-1$

    public static final String BOARD_TAG = "board"; //$NON-NLS-1$
    public static final String BOARDS_TAG = "boards"; //$NON-NLS-1$
    public static final String BOARDS = "Boards"; //$NON-NLS-1$
    public static final String BOARD_ID = "boardId"; //$NON-NLS-1$
    public static final String REVISION = "revision"; //$NON-NLS-1$
    public static final String MOUNTED_DEVICE_TAG = "mountedDevice"; //$NON-NLS-1$
    public static final String COMPATIBLE_DEVICE_TAG = "compatibleDevice"; //$NON-NLS-1$
    public static final String MOUNTED_DEVICES = "Mounted Devices"; //$NON-NLS-1$
    public static final String COMPATIBLE_DEVICES = "Compatible Devices"; //$NON-NLS-1$

    // configuration tags and attributes
    public static final String INSTANCES = "instances"; //$NON-NLS-1$
    public static final String DEVICE_DEPENDENT = "deviceDependent";//$NON-NLS-1$
    public static final String BOARD_DEPENDENT = "boardeDependent";//$NON-NLS-1$

    public static final String TOOLCHAIN_TAG = "toolchain"; //$NON-NLS-1$
    public static final String CONFIGURATION_TAG = "configuration"; //$NON-NLS-1$

    public static final String USE_ALL_LATEST_PACKS = "useAllLatestPacks"; //$NON-NLS-1$

    // view titles
    public static final String PACK_TITLE = "Pack"; //$NON-NLS-1$
    public static final String EXAMPLE_TITLE = "Example"; //$NON-NLS-1$
    public static final String ACTION_TITLE = "Action"; //$NON-NLS-1$
    public static final String DESCRIPTION_TITLE = "Description"; //$NON-NLS-1$
    public static final String DEVICE_TITLE = "Device"; //$NON-NLS-1$
    public static final String BOARD_TITLE = "Board"; //$NON-NLS-1$
    public static final String SUMMARY_TITLE = "Summary"; //$NON-NLS-1$

    public static final long DEFAULT_DEBUG_CLOCK = 10000000L;
    public static final long DEFAULT_DATAPATCH_MASK = 0xFFFFFFFFFFFFFFFFL;

    // file extensions
    public static final String EXT_PDSC = ".pdsc"; //$NON-NLS-1$
    public static final String EXT_GPDSC = ".gpdsc"; //$NON-NLS-1$
    public static final String EXT_CPDSC = ".cpdsc"; //$NON-NLS-1$
    public static final String EXT_PACK = ".pack"; //$NON-NLS-1$
    public static final String EXT_CPROJECT = ".cproject"; //$NON-NLS-1$
    public static final String EXT_SCT = ".sct"; //$NON-NLS-1$
    public static final String EXT_SCVD = ".scvd"; //$NON-NLS-1$

    // repository constants
    public static final String REPO_PACK_TYPE = "CMSIS-Pack"; //$NON-NLS-1$
    public static final String REPO_TYPE = "type"; //$NON-NLS-1$
    public static final String REPO_NAME = "name"; //$NON-NLS-1$
    public static final String REPO_LIST = "list"; //$NON-NLS-1$
    public static final String REPO_URL = "url"; //$NON-NLS-1$
    public static final String REPO_LOCATION = "Location"; //$NON-NLS-1$
    public static final String REPO_KEIL = "Keil"; //$NON-NLS-1$
    public static final String REPO_KEIL_SERVER = "www.keil.com"; //$NON-NLS-1$
    public static final String REPO_KEIL_PACK_SERVER = "https://www.keil.com/pack/"; //$NON-NLS-1$
    public static final String REPO_KEIL_INDEX_FILE = "index.idx"; //$NON-NLS-1$
    public static final String REPO_KEIL_PINDEX_FILE = "index.pidx"; //$NON-NLS-1$
    public static final String REPO_KEIL_INDEX_URL = REPO_KEIL_PACK_SERVER + REPO_KEIL_PINDEX_FILE;

    public static final String LOCAL_REPOSITORY_PIDX = "local_repository.pidx"; //$NON-NLS-1$
    public static final String LOCAL_FILE_URL = "file://localhost/"; //$NON-NLS-1$
    public static final String TAG = "tag"; //$NON-NLS-1$
    public static final String REPOSITORY = "repository"; //$NON-NLS-1$
    public static final String TIMESTAMP = "timestamp"; //$NON-NLS-1$
    public static final String PDSC = "pdsc"; //$NON-NLS-1$
    public static final String PINDEX = "pindex"; //$NON-NLS-1$

    // paths and variables

    public static final String CMSIS_PACK_ROOT = "cmsis_pack_root"; //$NON-NLS-1$
    public static final String CMSIS_PACK_ROOT_VAR = "${cmsis_pack_root}"; //$NON-NLS-1$
    public static final String CMSIS_DFP = "cmsis_dfp"; //$NON-NLS-1$
    public static final String CMSIS_DFP_VAR = "${cmsis_dfp}"; //$NON-NLS-1$
    public static final String CMSIS_DFP_VAR_PRJ = "${cmsis_dfp:${ProjName}}"; //$NON-NLS-1$

    public static final String DOT_DOWNLOAD = ".Download"; //$NON-NLS-1$
    public static final String DOT_WEB = ".Web"; //$NON-NLS-1$
    public static final String DOT_LOCAL = ".Local"; //$NON-NLS-1$
    public static final String DOT_PROJECT = ".project"; //$NON-NLS-1$
    public static final String DOT_BACK = ".back"; //$NON-NLS-1$

    public static final String KEIL = "Keil"; //$NON-NLS-1$

    public static final String MAP = "map"; //$NON-NLS-1$
    public static final String MEMORY = "memory"; //$NON-NLS-1$
    public static final String Memory = "Memory"; //$NON-NLS-1$
    public static final String MEMORIES = "memories"; //$NON-NLS-1$
    public static final String PARTITION = "partition"; //$NON-NLS-1$

    public static final String GROUP = "group"; //$NON-NLS-1$
    public static final String RESOURCES = "resources"; //$NON-NLS-1$
    public static final String PERIPHERAL = "peripheral"; //$NON-NLS-1$
    public static final String PERIPHERALS = "peripherals"; //$NON-NLS-1$
    public static final String Peripherals = "Peripherals"; //$NON-NLS-1$
    public static final String INTERRUPT = "interrupt"; //$NON-NLS-1$
    public static final String SLOT = "slot"; //$NON-NLS-1$
    public static final String SLOT_NAME = "slot_name"; //$NON-NLS-1$
    public static final String SLOT_TYPE = "slot_type"; //$NON-NLS-1$

    public static final String PARENT = "parent"; //$NON-NLS-1$

    public static final String SYSTEM = "system"; //$NON-NLS-1$
    public static final String ZONES = "zones"; //$NON-NLS-1$
    public static final String ZONE = "zone"; //$NON-NLS-1$
    public static final String RZONE = "rzone"; //$NON-NLS-1$
    public static final String AZONE = "azone"; //$NON-NLS-1$
    public static final String FZONE = "fzone"; //$NON-NLS-1$

    public static final String CONFIGURE = "configure"; //$NON-NLS-1$
    public static final String MODE = "mode"; //$NON-NLS-1$
    public static final String PROJECT = "project"; //$NON-NLS-1$

    public static final String SAU = "sau"; //$NON-NLS-1$
    public static final String SAU_INIT = "sau_init"; //$NON-NLS-1$
    public static final String REGION = "region"; //$NON-NLS-1$
    public static final String SETUP = "setup"; //$NON-NLS-1$
    public static final String TZ_SETUP = "tz_setup"; //$NON-NLS-1$
    public static final String REG_SETUP = "reg_setup"; //$NON-NLS-1$
    public static final String MPU_SETUP = "mpu_setup"; //$NON-NLS-1$
    public static final String MPC = "mpc"; //$NON-NLS-1$
    public static final String MPC_SETUP = "mpc_setup"; //$NON-NLS-1$
    public static final String BLK_MAX = "blk_max"; //$NON-NLS-1$
    public static final String BLK_CFG = "blk_cfg"; //$NON-NLS-1$
    public static final String BLK_LUT = "blk_lut"; //$NON-NLS-1$
    public static final String BLK_SIZE = "blk_size"; //$NON-NLS-1$
    public static final String S_bit = "S_bit"; //$NON-NLS-1$
    public static final String P_bit = "P_bit"; //$NON-NLS-1$
    public static final String bit_comment = "bit_comment"; //$NON-NLS-1$

    public static final String CREATOR = "creator"; //$NON-NLS-1$
    public static final String TOOL = "tool"; //$NON-NLS-1$

    public static final String ASSIGN = "assign"; //$NON-NLS-1$
    public static final String IRQ = "irq"; //$NON-NLS-1$
    public static final String IRQN = "irqn"; //$NON-NLS-1$
    public static final String NSC = "nsc"; //$NON-NLS-1$
    public static final String N = "n"; //$NON-NLS-1$
    public static final String S = "s"; //$NON-NLS-1$
    public static final String C = "c"; //$NON-NLS-1$

    public static final String LIST = "list"; //$NON-NLS-1$
    public static final String TREE = "tree"; //$NON-NLS-1$

    public static final String REMOVED = "removed"; //$NON-NLS-1$
    public static final String VALID = "valid"; //$NON-NLS-1$

    public static final String Info = "Info"; //$NON-NLS-1$
    public static final String Warning = "Warning"; //$NON-NLS-1$
    public static final String Error = "Error"; //$NON-NLS-1$
    public static final String FatalError = "Fatal Error"; //$NON-NLS-1$

    // variable that is used as markers, all are are expanded to empty string
    public static final String CMSIS_RTE = "cmsis_rte"; //$NON-NLS-1$
    public static final String CMSIS_RTE_VAR = "${cmsis_rte}"; //$NON-NLS-1$
    public static final String CMSIS_RTE_BEGIN_VAR = "${cmsis_rte:begin}"; //$NON-NLS-1$
    public static final String CMSIS_RTE_END_VAR = "${cmsis_rte:end}"; //$NON-NLS-1$

    // launch file variables
    public static final String CMSIS_EXE_FILE = "CMSIS_EXE_FILE"; //$NON-NLS-1$
    public static final String CMSIS_PREVIOUS_PRIMARY_FILE = "CMSIS_PREVIOUS_PRIMARY_FILE"; //$NON-NLS-1$
    public static final String CMSIS_PRIMARY_FILE = "CMSIS_PRIMARY_FILE"; //$NON-NLS-1$
    public static final String CMSIS_PROJ = "CMSIS_PROJ"; //$NON-NLS-1$
    public static final String CMSIS_PROJECT_EXECUTABLE = "CMSIS_PROJECT_EXECUTABLE"; //$NON-NLS-1$
    public static final String FILES_ICE_DEBUG_RESOURCES_0_VALUE = "FILES.ICE_DEBUG.RESOURCES.0.VALUE"; //$NON-NLS-1$
    public static final String KEY_COMMANDS_AS_CONNECT_TEXT = "KEY_COMMANDS_AS_CONNECT_TEXT"; //$NON-NLS-1$

    public static final String PACK_IDX = "pack.idx"; //$NON-NLS-1$

    public static final String WORKSPACE_LOC = "${workspace_loc}/"; //$NON-NLS-1$
    public static final String PROJECT_LOCAL_PATH = "${workspace_loc:/${ProjName}}/"; //$NON-NLS-1$
    public static final String PROJECT_LOCAL_PATH_CMSIS_RTE = "${workspace_loc:/${ProjName}}/" + CMSIS_RTE; //$NON-NLS-1$
    public static final String PROJECT_NAME = "${ProjName}/"; //$NON-NLS-1$
    public static final String PROJECT_ABS_PATH = "${ProjDirPath}/"; //$NON-NLS-1$
    public static final String PROJECT_RTE_PATH = PROJECT_LOCAL_PATH + RTE;
    public static final String OUTPUT_FILE_BASE = "${BuildArtifactFileBaseName}"; //$NON-NLS-1$
    public static final String OUTPUT_FILE = "${BuildArtifactFileName}"; //$NON-NLS-1$
    public static final String OUTPUT_PATH = "${ProjDirPath}/${ConfigName}/"; //$NON-NLS-1$
    public static final String OUTPUT_ABS_FILE = OUTPUT_PATH + OUTPUT_FILE;

    public static final String RTE_Components_h = "RTE_Components.h"; //$NON-NLS-1$
    public static final String RTE_RTE_Components_h = RTEDIR + RTE_Components_h;
    public static final String Pre_Include_Global_h = "Pre_Include_Global.h"; //$NON-NLS-1$
    public static final String RTE_Pre_Include_Global_h = RTEDIR + Pre_Include_Global_h;
    public static final String PROJECT_RTE_Pre_Include_Global_h = PROJECT_RTE_PATH + '/' + Pre_Include_Global_h;
    public static final String Pre_Include_ = "Pre_Include_"; //$NON-NLS-1$
    public static final String RTE_Pre_Include_ = RTEDIR + Pre_Include_;
    public static final String PROJECT_RTE_Pre_Include_ = PROJECT_RTE_PATH + '/' + Pre_Include_;
    public static final String CMSIS_device_header = "CMSIS_device_header"; //$NON-NLS-1$
    public static final String IMPORT_REPORT_TXT = "ImportReport.txt"; //$NON-NLS-1$

    public static final String UV = "uv"; //$NON-NLS-1$

    public static final String V7M = "v7M"; //$NON-NLS-1$
    public static final String V8M = "v8M"; //$NON-NLS-1$
    public static final String V81M = "v81M"; //$NON-NLS-1$
    public static final String MIXED = "mixed"; //$NON-NLS-1$
    public static final String UNKNOWN = "unknown"; //$NON-NLS-1$

    public static final String SIZE_V7M = "size_v7M"; //$NON-NLS-1$
    public static final String ADDR_V7M = "addr_v7M"; //$NON-NLS-1$
    public static final String SRD_V7M = "srd_v7M"; //$NON-NLS-1$

    // standard key sequences
    // (http://www.keil.com/pack/doc/CMSIS/Pack/html/pdsc_generators_pg.html)
    public static final String[] STANDARD_KEY_SEQENCES = new String[] { "$D", // Directory name //$NON-NLS-1$
                                                                              // corresponding to project's device,
                                                                              // equals to #D_@D if @D is not empty, to
                                                                              // #D otherwise
            "#D", // Device device item without processor name //$NON-NLS-1$
            "@D", // Processor name if not empty //$NON-NLS-1$
            "$P", // PATH to current project //$NON-NLS-1$
            "#P", // PATH and filename name of the current project //$NON-NLS-1$
            "@P", // name of the current project //$NON-NLS-1$
            "$S", // PATH to PACK folder containing the Device description used by //$NON-NLS-1$
                  // the current project
            "$K", // PATH to environment-specific "kernel" directory //$NON-NLS-1$
            "$L", // PATH to output directory //$NON-NLS-1$
            "#L", // Absolute linker output file //$NON-NLS-1$
            "@L", // Linker output file base name without extension //$NON-NLS-1$
            "%L" // Linker output file name with extension //$NON-NLS-1$
    };

    public static final String[] LAUNCH_TYPES = new String[] { ECLIPSE, EXE, WEB };

    /**
     * Private constructor to prevent instantiating the utility class
     */
    private CmsisConstants() {
        throw new IllegalStateException("CmsisConstants is a utility class"); //$NON-NLS-1$
    }

}
