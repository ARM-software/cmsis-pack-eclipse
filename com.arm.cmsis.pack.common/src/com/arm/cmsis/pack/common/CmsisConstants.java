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


package com.arm.cmsis.pack.common;

/**
 * Utility class providing CMSIS constants: tags, attribute names as well as helper functions
 */
public class CmsisConstants {

	/**
	 * Empty string used as "null-object" to avoid using null strings
	 */
	static public final String EMPTY_STRING = "";		//$NON-NLS-1$

	static public final String COLON		= ":";		//$NON-NLS-1$
	static public final String DOUBLE_COLON	= "::";		//$NON-NLS-1$

	static public final String ARM			= "ARM";	//$NON-NLS-1$
	static public final String CORTEX		= "Cortex";	//$NON-NLS-1$
	static public final String CMSIS		= "CMSIS";	//$NON-NLS-1$
	static public final String PACK			= "pack";	//$NON-NLS-1$

	static public final String PACK_ID 		= "packId";		//$NON-NLS-1$
	
	// commonly used values for component classes and groups
	static public final String RTOS			= "RTOS";	//$NON-NLS-1$
	static public final String Core			= "Core";	//$NON-NLS-1$
	static public final String Device		= "Device";	//$NON-NLS-1$
	static public final String Startup		= "Startup";//$NON-NLS-1$
	static public final String Device_Startup= Device + '_' + Startup;
	static public final String CMSIS_Core	= CMSIS + '_' + Core;
	static public final String CMSIS_RTOS	= CMSIS + '_' + RTOS;
	
	// commonly used tags and attributes
	static public final String CONDITION 	= "condition";		//$NON-NLS-1$
	static public final String VENDOR		= "vendor";			//$NON-NLS-1$
	static public final String VERSION		= "version";		//$NON-NLS-1$
	static public final String URL			= "url";			//$NON-NLS-1$
	static public final String NAME			= "name";			//$NON-NLS-1$
	static public final String ID			= "id";				//$NON-NLS-1$
	static public final String DOC			= "doc";			//$NON-NLS-1$
	static public final String DESCRIPTION	= "description";	//$NON-NLS-1$
	public static final String FILE_NAME 	= "filename";		//$NON-NLS-1$


	// condition expressions
	static public final String ACCEPT		= "accept";			//$NON-NLS-1$
	static public final String REQUIRE		= "require";		//$NON-NLS-1$
	static public final String DENY			= "deny";			//$NON-NLS-1$

	// attribute keys
	static public final String C_ATTRIBUTE_PREFIX = "C";		//$NON-NLS-1$
	static public final String D_ATTRIBUTE_PREFIX = "D";		//$NON-NLS-1$
	static public final String T_ATTRIBUTE_PREFIX = "T";		//$NON-NLS-1$

	static public final String CBUNDLE 		= "Cbundle";		//$NON-NLS-1$
	static public final String CBUNDLEVERSION = "Cbundleversion";		//$NON-NLS-1$

	static public final String CGROUP 		= "Cgroup";			//$NON-NLS-1$
	static public final String CCLASS 		= "Cclass";			//$NON-NLS-1$
	static public final String CSUB 		= "Csub";			//$NON-NLS-1$
	static public final String CVARIANT		= "Cvariant";		//$NON-NLS-1$
	static public final String CVENDOR 		= "Cvendor";		//$NON-NLS-1$
	static public final String CVERSION 	= "Cversion";		//$NON-NLS-1$
	static public final String CAPIVERSION 	= "Capiversion";	//$NON-NLS-1$
	static public final String EXCLUSIVE	= "exclusive";   	//$NON-NLS-1$
	static public final String MAX_INSTANCES = "maxInstances";	//$NON-NLS-1$
	static public final String IS_DEFAULT_VARIANT = "isDefaultVariant";   //$NON-NLS-1$

	static public final String DNAME 		= "Dname";			//$NON-NLS-1$
	static public final String DFAMILY 		= "Dfamily";		//$NON-NLS-1$
	static public final String DSUBFAMILY 	= "DsubFamily";		//$NON-NLS-1$
	static public final String DVARIANT 	= "Dvariant";		//$NON-NLS-1$
	static public final String DVENDOR 		= "Dvendor";		//$NON-NLS-1$
	static public final String DVERSION		= "Dversion";		//$NON-NLS-1$
	static public final String DCORE		= "Dcore";			//$NON-NLS-1$
	static public final String DFPU			= "Dfpu";			//$NON-NLS-1$
	static public final String DMPU			= "Dmpu";			//$NON-NLS-1$
	static public final String DDSP			= "Ddsp";			//$NON-NLS-1$
	static public final String DTZ			= "Dtz";			//$NON-NLS-1$
	static public final String DSECURE		= "Dsecure";		//$NON-NLS-1$
	static public final String DENDIAN		= "Dendian";		//$NON-NLS-1$
	static public final String DCLOCK		= "Dclock";			//$NON-NLS-1$

	static public final String CATEGORY 	= "category";		//$NON-NLS-1$
	static public final String ATTR 		= "attr";			//$NON-NLS-1$
	static public final String PATH 		= "path";			//$NON-NLS-1$
	
	static public final String HEADER		= "header";			//$NON-NLS-1$
	static public final String INCLUDE		= "include";		//$NON-NLS-1$
	static public final String SOURCE		= "source";			//$NON-NLS-1$
	static public final String SOURCE_ASM	= "sourceAsm";		//$NON-NLS-1$
	static public final String SOURCE_C		= "sourceC";		//$NON-NLS-1$
	static public final String SOURCE_CPP	= "sourceCpp";		//$NON-NLS-1$
	static public final String LIBRARY		= "library";		//$NON-NLS-1$
	static public final String OBJECT		= "object";			//$NON-NLS-1$
	static public final String SRC			= "src";			//$NON-NLS-1$
	static public final String SVD 			= "svd";			//$NON-NLS-1$
	static public final String SDF 			= "sdf";			//$NON-NLS-1$
	static public final String IMAGE 		= "image";			//$NON-NLS-1$
	static public final String UTILITY 		= "utility";		//$NON-NLS-1$
	static public final String LINKER_SCRIPT = "linkerScript";	//$NON-NLS-1$	
	static public final String PRE_INCLUDE_GLOBAL= "preIncludeGlobal"; //$NON-NLS-1$
	static public final String PRE_INCLUDE_LOCAL = "preIncludeLocal"; //$NON-NLS-1$

	static public final String DEFINE		= "define";			//$NON-NLS-1$
	static public final String PDEFINE		= "Pdefine";		//$NON-NLS-1$
	static public final String SELECT		= "select";			//$NON-NLS-1$
	static public final String ACCESS		= "access";			//$NON-NLS-1$

	static public final String PNAME 		= "Pname";			//$NON-NLS-1$
	static public final String PUNIT 		= "Punit";			//$NON-NLS-1$
	static public final String PUNITS 		= "Punits";			//$NON-NLS-1$

	static public final String TCOMPILER	= "Tcompiler";		//$NON-NLS-1$
	static public final String TOPTIONS		= "Toptions";		//$NON-NLS-1$
	static public final String TOUTPUT		= "Toutput";		//$NON-NLS-1$

	static public final String TOUTPUT_EXE	= "exe";			//$NON-NLS-1$
	static public final String TOUTPUT_LIB	= "lib";			//$NON-NLS-1$

	static public final String THOST		= "Thost";			//$NON-NLS-1$
	static public final String TENVIRONMENT	= "Tenvironment";	//$NON-NLS-1$
	static public final String TECLIPSE		= "Teclipse";		//$NON-NLS-1$

	static public final String HOST			= "host";			//$NON-NLS-1$
	public static final String ALL 			= "all";			//$NON-NLS-1$
	static public final String WIN			= "win";			//$NON-NLS-1$
	static public final String LINUX		= "linux";			//$NON-NLS-1$
	static public final String MAC			= "mac";			//$NON-NLS-1$
	static public final String NIX			= "nix";			//$NON-NLS-1$
	static public final String NUX			= "nux";			//$NON-NLS-1$

	static public final String EXE			= "exe";			//$NON-NLS-1$
	static public final String ECLIPSE		= "eclipse";		//$NON-NLS-1$
	static public final String WEB			= "web";			//$NON-NLS-1$

	static public final String PLUGIN		= "plugin";			//$NON-NLS-1$
	static public final String CLASS		= "class";			//$NON-NLS-1$
	static public final String METHOD		= "method";			//$NON-NLS-1$


	static public final String VERSION_MODE	= "versionMatchMode";//$NON-NLS-1$

	static public final String FOLDER		= "folder";			//$NON-NLS-1$
	static public final String LOAD			= "load";			//$NON-NLS-1$

	static public final String DEPRECATED	= "deprecated";		//$NON-NLS-1$
	static public final String REPLACEMENT	= "replacement";	//$NON-NLS-1$

	// attribute values
	static public final String DATE			= "date";		//$NON-NLS-1$

	static public final String BIGENDIAN	= "Big-endian";		//$NON-NLS-1$
	static public final String LITTLENDIAN	= "Little-endian";	//$NON-NLS-1$
	static public final String CONFIGENDIAN	= "Configurable";	//$NON-NLS-1$

	static public final String LATEST		= "latest";			//$NON-NLS-1$
	static public final String FIXED		= "fixed";			//$NON-NLS-1$
	static public final String EXCLUDED		= "excluded";		//$NON-NLS-1$

	static public final String FPU			= "FPU";			//$NON-NLS-1$
	static public final String NO_FPU		= "NO_FPU";			//$NON-NLS-1$
	static public final String SP_FPU		= "SP_FPU";			//$NON-NLS-1$
	static public final String DP_FPU		= "DP_FPU";			//$NON-NLS-1$
	static public final String MPU			= "MPU";			//$NON-NLS-1$
	static public final String NO_MPU		= "NO_MPU";			//$NON-NLS-1$

	static public final String TZ			= "TZ";				//$NON-NLS-1$
	static public final String NO_TZ		= "NO_TZ";			//$NON-NLS-1$

	static public final String DSP			= "DSP";				//$NON-NLS-1$
	static public final String NO_DSP		= "NO_DSP";			//$NON-NLS-1$

	static public final String SECURE		= "Secure";				//$NON-NLS-1$
	static public final String NON_SECURE	= "Non-secure";			//$NON-NLS-1$


	static public final String COPY			= "copy";			//$NON-NLS-1$
	static public final String CONFIG		= "config";			//$NON-NLS-1$
	static public final String TEMPLATE		= "template";		//$NON-NLS-1$
	static public final String INTERFACE	= "interface";		//$NON-NLS-1$
	static public final String pINSTANCEp	= "%Instance%";		//$NON-NLS-1$


	// tags
	static public final String API_TAG			= "api";		//$NON-NLS-1$
	static public final String APIS_TAG			= "apis";		//$NON-NLS-1$
	static public final String BUNDLE_TAG		= "bundle";		//$NON-NLS-1$
	static public final String COMPONENT_TAG	= "component";	//$NON-NLS-1$
	static public final String COMPONENTS_TAG	= "components";	//$NON-NLS-1$
	static public final String CONDITIONS_TAG	= "conditions";	//$NON-NLS-1$
	static public final String RELEASE_TAG		= "release";	//$NON-NLS-1$
	static public final String RELEASES_TAG		= "releases";	//$NON-NLS-1$
	static public final String DEVICE_TAG		= "device";		//$NON-NLS-1$
	static public final String DEVICES_TAG		= "devices";	//$NON-NLS-1$
	static public final String FAMILY_TAG		= "family";		//$NON-NLS-1$
	static public final String SUBFAMILY_TAG 	= "subFamily";	//$NON-NLS-1$
	static public final String VARIANT_TAG   	= "variant";	//$NON-NLS-1$
	static public final String FILE_TAG      	= "file";		//$NON-NLS-1$
	static public final String FILES_TAG      	= "files";		//$NON-NLS-1$
	static public final String PACKAGE_TAG     	= "package";	//$NON-NLS-1$
	static public final String PACKAGES_TAG    	= "packages";	//$NON-NLS-1$
	static public final String PROCESSOR_TAG 	= "processor";	//$NON-NLS-1$
	static public final String TAXONOMY_TAG		= "taxonomy";	//$NON-NLS-1$
	static public final String EXAMPLE_TAG		= "example";	//$NON-NLS-1$
	static public final String EXAMPLES_TAG		= "examples";	 //$NON-NLS-1$
	static public final String REQUIREMENTS_TAG	= "requirements"; //$NON-NLS-1$	


	static public final String RTE_COMPONENTS_H = "RTE_Components_h"; //$NON-NLS-1$
	static public final String PRE_INCLUDE_GLOBAL_H = "Pre_Include_Global_h"; //$NON-NLS-1$
	static public final String PRE_INCLUDE_LOCAL_COMPONENT_H = "Pre_Include_Local_Component_h"; //$NON-NLS-1$

	
	static public final String RTE				= "RTE"; 		//$NON-NLS-1$
	static public final String RTEDIR			= "RTE/"; 		//$NON-NLS-1$
	static public final String _RTE_			= "_RTE_"; 		//$NON-NLS-1$
	static public final String RTECONFIG		= "rteconfig";	//$NON-NLS-1$
	static public final String DOT_RTECONFIG	= ".rteconfig";	//$NON-NLS-1$
	static public final String PROJECT_TAG		= "project";	//$NON-NLS-1$
	static public final String LICENSE_TAG		= "license";	//$NON-NLS-1$
	static public final String DEVICE_SPECIFIC	= "Device Specific";	//$NON-NLS-1$
	static public final String GENERIC			= "Generic";	//$NON-NLS-1$
	static public final String PREVIOUS 		= "Previous";	//$NON-NLS-1$
	static public final String Reset_Handler 	= "Reset_Handler";	//$NON-NLS-1$
	static public final String Vectors 			= "Vectors";	//$NON-NLS-1$

	static public final String DOT_SZONE		= ".szone";		//$NON-NLS-1$
	static public final String DOT_PZONE		= ".pzone";		//$NON-NLS-1$

	// gpdsc-related property tags and attributes
	static public final String GENERATOR		= "generator";	//$NON-NLS-1$
	static public final String GENERATOR_TAG	=  GENERATOR;
	static public final String GENERATORS_TAG	= "generators";	//$NON-NLS-1$
	static public final String GENERATED		= "generated";	//$NON-NLS-1$
	static public final String COMMAND		    = "command";	//$NON-NLS-1$
	static public final String COMMAND_TAG		=  COMMAND;
	static public final String ARGUMENT_TAG     = "argument";	//$NON-NLS-1$
	static public final String ARGUMENTS_TAG    = "arguments";	//$NON-NLS-1$
	static public final String SWITCH		    = "switch";	//$NON-NLS-1$
	static public final String GPDSC_TAG		= "gpdsc";		//$NON-NLS-1$
	static public final String GPDSCS_TAG		= "gpdscs";		//$NON-NLS-1$
	static public final String WORKING_DIR_TAG  = "workingDir";	//$NON-NLS-1$
	static public final String PROJECT_FILES_TAG = "project_files";	//$NON-NLS-1$

	// device property tags and attributes
	static public final String DEBUG_TAG		= "debug";		//$NON-NLS-1$
	static public final String DEBUGCONFIG_TAG	= "debugconfig";//$NON-NLS-1$
	static public final String DEBUGVARS_TAG	= "debugvars";	//$NON-NLS-1$
	static public final String ALGORITHM_TAG	= "algorithm";	//$NON-NLS-1$
	static public final String ENVIRONMENT_TAG	= "environment";//$NON-NLS-1$
	static public final String TRACE_TAG		= "trace";		//$NON-NLS-1$
	static public final String COMPILE_TAG		= "compile";	//$NON-NLS-1$
	static public final String MEMORY_TAG		= "memory";		//$NON-NLS-1$
	static public final String FEATURE_TAG		= "feature";	//$NON-NLS-1$
	static public final String BOOK_TAG			= "book";		//$NON-NLS-1$
	static public final String BLOCK_TAG		= "block";		//$NON-NLS-1$
	static public final String CONTROL_TAG		= "control";	//$NON-NLS-1$
	static public final String SEQUENCES_TAG	= "sequences";	//$NON-NLS-1$
	static public final String SEQUENCE_TAG		= "sequence";	//$NON-NLS-1$
	static public final String DATAPATCH_TAG	= "datapatch";	//$NON-NLS-1$
	static public final String DEBUGPORT_TAG	= "debugport";	//$NON-NLS-1$

	static public final String JTAG				= "jtag";		//$NON-NLS-1$
	static public final String CJTAG			= "cjtag";		//$NON-NLS-1$
	static public final String SWD				= "swd";		//$NON-NLS-1$

	static public final String IDCODE			= "idcode";		//$NON-NLS-1$
	static public final String TARGETSEL		= "targetsel";	//$NON-NLS-1$
	static public final String TAPINDEX			= "tapindex";	//$NON-NLS-1$
	static public final String IRLEN			= "irlen";		//$NON-NLS-1$

	static public final String SWJ				= "swj";		//$NON-NLS-1$

	static public final String __DP				= "__dp";		//$NON-NLS-1$
	static public final String __AP				= "__ap";		//$NON-NLS-1$
	static public final String DP				= "AP";			//$NON-NLS-1$
	static public final String AP				= "DP";			//$NON-NLS-1$
	static public final String MEM				= "Mem";		//$NON-NLS-1$

	static public final String CONFIGFILE		= "configfile";	//$NON-NLS-1$

	static public final String ATOMIC			= "atomic";		//$NON-NLS-1$
	static public final String IF				= "if";			//$NON-NLS-1$
	static public final String WHILE			= "while";		//$NON-NLS-1$
	static public final String DISABLE			= "disable";	//$NON-NLS-1$
	static public final String TIMEOUT			= "timeout";	//$NON-NLS-1$

	static public final String START			= "start";		//$NON-NLS-1$
	static public final String OFFSET			= "offset";		//$NON-NLS-1$
	static public final String SIZE				= "size";		//$NON-NLS-1$
	static public final String STARTUP			= "startup";	//$NON-NLS-1$
	static public final String DEFAULT			= "default";	//$NON-NLS-1$
	static public final String INIT				= "init";		//$NON-NLS-1$
	static public final String ADDRESS			= "address";	//$NON-NLS-1$
	static public final String VALUE			= "value";		//$NON-NLS-1$
	static public final String MASK				= "mask";		//$NON-NLS-1$
	static public final String INFO				= "info";		//$NON-NLS-1$
	static public final String CLOCK			= "clock";		//$NON-NLS-1$
	static public final String TYPE				= "type";		//$NON-NLS-1$
	static public final String ALIAS			= "alias";		//$NON-NLS-1$
	static public final String AS				= "as";			//$NON-NLS-1$

	static public final String RAMSTART			= "RAMstart";	//$NON-NLS-1$
	static public final String RAMSIZE			= "RAMsize";	//$NON-NLS-1$

	static public final String RAM				= "RAM";		//$NON-NLS-1$
	static public final String ROM				= "ROM";		//$NON-NLS-1$

	static public final String IRAM				= "IRAM";		//$NON-NLS-1$
	static public final String IRAM1			= "IRAM1";		//$NON-NLS-1$
	static public final String IRAM2			= "IRAM2";		//$NON-NLS-1$
	static public final String IRAM3			= "IRAM3";		//$NON-NLS-1$
	static public final String IRAM4			= "IRAM4";		//$NON-NLS-1$
	static public final String IRAM5			= "IRAM5";		//$NON-NLS-1$
	static public final String IRAM6			= "IRAM6";		//$NON-NLS-1$
	static public final String IRAM7			= "IRAM7";		//$NON-NLS-1$
	static public final String IRAM8			= "IRAM8";		//$NON-NLS-1$

	static public final String IROM				= "IROM";		//$NON-NLS-1$
	static public final String IROM1			= "IROM1";		//$NON-NLS-1$
	static public final String IROM2			= "IROM2";		//$NON-NLS-1$
	static public final String IROM3			= "IROM3";		//$NON-NLS-1$
	static public final String IROM4			= "IROM4";		//$NON-NLS-1$
	static public final String IROM5			= "IROM5";		//$NON-NLS-1$
	static public final String IROM6			= "IROM6";		//$NON-NLS-1$
	static public final String IROM7			= "IROM7";		//$NON-NLS-1$
	static public final String IROM8			= "IROM8";		//$NON-NLS-1$

	static public final String ARMCC			= "ARMCC";		//$NON-NLS-1$

	static public final String TITLE			= "title";		//$NON-NLS-1$
	static public final String ERRORS			= "ERRORS";		//$NON-NLS-1$
	// 	board tags and attributes
	static public final String ALL_BOARDS		= "All Boards"; //$NON-NLS-1$
	static public final String ALL_DEVICES		= "All Devices"; //$NON-NLS-1$

	static public final String BOARD_TAG		= "board";		//$NON-NLS-1$
	static public final String BOARDS_TAG		= "boards";		//$NON-NLS-1$
	static public final String REVISION			= "revision";	//$NON-NLS-1$
	static public final String MOUNTED_DEVICE_TAG 	= "mountedDevice";		//$NON-NLS-1$
	static public final String COMPATIBLE_DEVICE_TAG= "compatibleDevice";	//$NON-NLS-1$
	static public final String MOUNTED_DEVICES 	= "Mounted Devices";		//$NON-NLS-1$
	static public final String COMPATIBLE_DEVICES = "Compatible Devices";	//$NON-NLS-1$

	// configuration tags and attributes
	static public final String INSTANCES		= "instances";		//$NON-NLS-1$
	static public final String DEVICE_DEPENDENT	= "deviceDependent";//$NON-NLS-1$

	static public final String TOOLCHAIN_TAG	= "toolchain";		//$NON-NLS-1$
	static public final String CONFIGURATION_TAG= "configuration";	//$NON-NLS-1$

	static public final String USE_ALL_LATEST_PACKS ="useAllLatestPacks";	//$NON-NLS-1$

	// view titles
	static public final String PACK_TITLE		= "Pack"; //$NON-NLS-1$
	static public final String EXAMPLE_TITLE	= "Example"; //$NON-NLS-1$
	static public final String ACTION_TITLE		= "Action"; //$NON-NLS-1$
	static public final String DESCRIPTION_TITLE= "Description"; //$NON-NLS-1$
	static public final String DEVICE_TITLE		= "Device"; //$NON-NLS-1$
	static public final String BOARD_TITLE		= "Board"; //$NON-NLS-1$
	static public final String SUMMARY_TITLE= "Summary"; //$NON-NLS-1$

	static public final long DEFAULT_DEBUG_CLOCK = 10000000L;
	static public final long DEFAULT_DATAPATCH_MASK = 0xFFFFFFFFFFFFFFFFL;
	
	// file extensions
	static public final String EXT_PDSC 		= ".pdsc"; //$NON-NLS-1$
	static public final String EXT_GPDSC 		= ".gpdsc"; //$NON-NLS-1$
	static public final String EXT_CPDSC 		= ".cpdsc"; //$NON-NLS-1$
	static public final String EXT_PACK 		= ".pack"; //$NON-NLS-1$
	static public final String EXT_CPROJECT		= ".cproject"; //$NON-NLS-1$
	static public final String EXT_SCT			= ".sct"; //$NON-NLS-1$
	static public final String EXT_SCVD			= ".scvd"; //$NON-NLS-1$

	// repository constants
	public static final String REPO_PACK_TYPE	= "CMSIS Pack"; //$NON-NLS-1$
	static public final String REPO_TYPE		= "type"; //$NON-NLS-1$
	static public final String REPO_NAME		= "name"; //$NON-NLS-1$
	static public final String REPO_LIST		= "list"; //$NON-NLS-1$
	static public final String REPO_URL			= "url"; //$NON-NLS-1$
	static public final String REPO_LOCATION	= "Location"; //$NON-NLS-1$
	static public final String REPO_KEIL		= "Keil"; //$NON-NLS-1$
	static public final String REPO_KEIL_SERVER	= "www.keil.com"; //$NON-NLS-1$
	static public final String REPO_KEIL_PACK_SERVER = "http://www.keil.com/pack/"; //$NON-NLS-1$
	static public final String REPO_KEIL_INDEX_FILE = "index.idx"; //$NON-NLS-1$
	static public final String REPO_KEIL_PINDEX_FILE = "index.pidx"; //$NON-NLS-1$
	static public final String REPO_KEIL_INDEX_URL = REPO_KEIL_PACK_SERVER + REPO_KEIL_PINDEX_FILE;

	public static final String LOCAL_REPOSITORY_PIDX = "local_repository.pidx"; //$NON-NLS-1$
	public static final String LOCAL_FILE_URL = "file://localhost/"; //$NON-NLS-1$
	public static final String TAG = "tag"; //$NON-NLS-1$
	public static final String REPOSITORY = "repository"; //$NON-NLS-1$
	public static final String TIMESTAMP = "timestamp"; //$NON-NLS-1$
	public static final String INDEX = "index"; //$NON-NLS-1$
	public static final String SCHEMAVERSION = "schemaVersion"; //$NON-NLS-1$
	public static final String SCHEMAINSTANCE = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$
	public static final String SCHEMALOCATION = "xs:noNamespaceSchemaLocation"; //$NON-NLS-1$
	public static final String DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss"; //$NON-NLS-1$
	public static final String PDSC = "pdsc"; //$NON-NLS-1$
	public static final String PINDEX = "pindex"; //$NON-NLS-1$

	static public final String ZERO	= "0"; //$NON-NLS-1$
	static public final String ONE	= "1"; //$NON-NLS-1$
	// paths and variables

	static public final String CMSIS_PACK_ROOT = "cmsis_pack_root";   			//$NON-NLS-1$
	static public final String CMSIS_PACK_ROOT_VAR = "${cmsis_pack_root}" ; 	//$NON-NLS-1$
	static public final String CMSIS_DFP = "cmsis_dfp";   						//$NON-NLS-1$
	static public final String CMSIS_DFP_VAR = "${cmsis_dfp}";   				//$NON-NLS-1$
	static public final String CMSIS_DFP_VAR_PRJ = "${cmsis_dfp:${ProjName}}"; 	//$NON-NLS-1$

	static public final String DOT_DOWNLOAD = ".Download"; 						//$NON-NLS-1$
	static public final String DOT_WEB = ".Web";		 						//$NON-NLS-1$
	static public final String DOT_LOCAL = ".Local";	 						//$NON-NLS-1$
	static public final String DOT_PROJECT = ".project"; 						//$NON-NLS-1$

	static public final String KEIL =  "Keil";									//$NON-NLS-1$

	static public final String SYSTEM_TAG = "system";		 					//$NON-NLS-1$
	static public final String MEMORY_MAP_TAG = "memory_map"; 					//$NON-NLS-1$
	static public final String MAP_TAG = 		"map";		 					//$NON-NLS-1$
	static public final String REGION_TAG = "region";							//$NON-NLS-1$
	static public final String GROUP_TAG = "group";								//$NON-NLS-1$
	static public final String RESOURCES_TAG = "resources";						//$NON-NLS-1$
	static public final String PERIPHERAL_TAG = "peripheral";					//$NON-NLS-1$
	static public final String PERIPHERALS_TAG = "peripherals";					//$NON-NLS-1$
	public static final String INTERRUPT_TAG = "interrupt";						//$NON-NLS-1$


	public static final String ZONES_TAG 	 = "zones";							//$NON-NLS-1$
	public static final String PZONE_TAG 	 = "pzone";							//$NON-NLS-1$
	public static final String XZONE_TAG 	 = "xzone";							//$NON-NLS-1$
	public static final String ASSIGN_TAG 	 = "assign";						//$NON-NLS-1$

	public static final String LIST		 	 = "list";						//$NON-NLS-1$
	public static final String TREE		 	 = "tree";						//$NON-NLS-1$

	public static final String REMOVED		 = "removed";					//$NON-NLS-1$
	public static final String VALID		 = "valid";						//$NON-NLS-1$


	// variable that is used as markers, all are are expanded to empty string
	static public final String CMSIS_RTE = "cmsis_rte";		 						//$NON-NLS-1$
	static public final String CMSIS_RTE_VAR = "${cmsis_rte}";		 				//$NON-NLS-1$
	static public final String CMSIS_RTE_BEGIN_VAR = "${cmsis_rte:begin}"; 			//$NON-NLS-1$
	static public final String CMSIS_RTE_END_VAR   = "${cmsis_rte:end}";			//$NON-NLS-1$

	static public final String PACK_IDX = "pack.idx"; //$NON-NLS-1$

	static public final String PROJECT_LOCAL_PATH 	= "${workspace_loc:/${ProjName}}/"; //$NON-NLS-1$
	static public final String PROJECT_NAME			= "${ProjName}/"; //$NON-NLS-1$
	static public final String PROJECT_ABS_PATH		= "${ProjDirPath}/"; //$NON-NLS-1$
	static public final String PROJECT_RTE_PATH    	= PROJECT_LOCAL_PATH + RTE;
	static public final String OUTPUT_FILE_BASE		= "${BuildArtifactFileBaseName}"; //$NON-NLS-1$
	static public final String OUTPUT_FILE  		= "${BuildArtifactFileName}"; //$NON-NLS-1$
	static public final String OUTPUT_PATH  		= "${ProjDirPath}/${ConfigName}/"; //$NON-NLS-1$
	static public final String OUTPUT_ABS_FILE  	= OUTPUT_PATH + OUTPUT_FILE;
	

	static public final String RTE_Components_h = "RTE_Components.h";  //$NON-NLS-1$
	static public final String RTE_RTE_Components_h = RTEDIR + RTE_Components_h;
	static public final String Pre_Include_Global_h = "Pre_Include_Global.h";  //$NON-NLS-1$
	static public final String RTE_Pre_Include_Global_h = RTEDIR + Pre_Include_Global_h;
	static public final String PROJECT_RTE_Pre_Include_Global_h = PROJECT_RTE_PATH + '/' + Pre_Include_Global_h;
	static public final String Pre_Include_ 		= "Pre_Include_";  //$NON-NLS-1$
	static public final String RTE_Pre_Include_ 	= RTEDIR + Pre_Include_;
	static public final String PROJECT_RTE_Pre_Include_ = PROJECT_RTE_PATH + '/' + Pre_Include_;
	static public final String CMSIS_device_header = "CMSIS_device_header";  		//$NON-NLS-1$
    public static final String IMPORT_REPORT_TXT = "ImportReport.txt"; 				//$NON-NLS-1$
	

	static public final String UV = "uv";  											//$NON-NLS-1$

	// standard key sequences (http://www.keil.com/pack/doc/CMSIS/Pack/html/pdsc_generators_pg.html)
	static public final String[] STANDARD_KEY_SEQENCES =
			new String[]{
					"$D",   // Directory name corresponding to project's device, equals to #D_@D if @D is not empty, to #D otherwise   //$NON-NLS-1$
					"#D", 	// Device device item without processor name   //$NON-NLS-1$
					"@D",   // Processor name if not empty  //$NON-NLS-1$
					"$P", 	// PATH to current project //$NON-NLS-1$
					"#P", 	// PATH and filename name of the current project  //$NON-NLS-1$
					"@P", 	// name of the current project  //$NON-NLS-1$
					"$S",	// PATH to PACK folder containing the Device description used by the current project  //$NON-NLS-1$
					"$K",	// PATH to environment-specific "kernel" directory  //$NON-NLS-1$
					"$L",   // PATH to output directory //$NON-NLS-1$
					"#L",   // Absolute linker output file //$NON-NLS-1$
					"@L",   // Linker output file base name without extension //$NON-NLS-1$
					"%L"    // Linker output file name with extension //$NON-NLS-1$
	};

	static public final String[] LAUNCH_TYPES = new String[]{ECLIPSE, EXE, WEB};
}
