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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 * Convenience interface to access debug/trace device configuration
 */
public interface ICpDebugConfiguration extends ICpItem {

    /**
     * Returns default device debug protocol (usually SWD )
     *
     * @return EDebugProtocolType: SWD, JTAG or CJTAG
     */
    EDebugProtocolType getDefaultProtocolType();

    /**
     * Returns if Debug Port is a CoreSight SWJ-DP (default is true)
     *
     * @return true if SWJ-DP
     */
    boolean isSWJ();

    /**
     * Returns if Debug Port supports dormant state (default is false)
     *
     * @return true if dormant state supported
     */
    default boolean hasDormantState() {
        return false;
    }

    /**
     * Returns default debug clock (10 MHz if not specified)
     *
     * @return debug clock value
     */
    long getDefaultClock();

    /**
     * Returns absolute SDF filename: System Description File, which contains
     * information about available system components, types and versions
     *
     * @return absolute SDF filename
     */
    String getSdfFile();

    /**
     * Returns absolute SVD filename
     *
     * @return absolute SVD filename
     */
    String getSvdFile();

    /**
     * Returns "debug" item for processor unit 0 (default)
     *
     * @return ICpDebug for default processor unit
     */
    ICpDebug getDebugItem();

    /**
     * Returns "debug" item for given processor unit
     *
     * @param punitIndex processor unit index
     * @return ICpDebug for processor unit
     */
    ICpDebug getDebugItem(int punitIndex);

    /**
     * Returns "trace" item
     *
     * @return ICpTrace
     */
    ICpTrace getTraceItem();

    /**
     * Check if trace capabilities are available
     *
     * @return true if supported
     */
    boolean isTraceSupported();

    /**
     * Returns "debugvars" item
     *
     * @return ICpDebugVars
     */
    ICpDebugVars getDebugVars();

    /**
     * Return collection of available debug ports
     *
     * @return map of index to ICpDebugPort
     */
    Map<Long, ICpDebugPort> getDebugPorts();

    /**
     * Returns debug port for given index
     *
     * @param index debug port index
     * @return ICpDebugPort or null if no port is defined for given index
     */
    ICpDebugPort getDebugPort(long index);

    /**
     * Returns collection of debug sequences
     *
     * @return map of name to ICpSequence
     */
    Map<String, ICpSequence> getSequences();

    /**
     * Returns sequence for given name
     *
     * @return ICpSequence or null if not found
     */
    ICpSequence getSequence(String name);

    /**
     * Returns collection of memory items
     *
     * @return map of id to IcpMemory
     */
    Map<String, ICpMemory> getMemoryItems();

    /**
     * Returns memory corresponding to specified id
     *
     * @param id memory ID
     * @return ICpMemory or null if not defined
     */
    ICpMemory getMemory(String id);

    /**
     * Default memory that shall be used by linker
     *
     * @return ICpMemory
     */
    ICpMemory getDefaulMemory();

    /**
     * Memory that shall be used for the startup by linker
     *
     * @return ICpMemory
     */
    ICpMemory getStartupMemory();

    /**
     * Returns collection of flash algorithms
     *
     * @return collection of ICpAlgorithm items
     */
    Collection<ICpAlgorithm> getAlgorithms();

    /**
     * Returns default flash algorithms (those that have attribute "default="1")
     *
     * @return collection of default ICpAlgorithm items
     */
    Collection<ICpAlgorithm> getDefaultAlgorithms();

}
