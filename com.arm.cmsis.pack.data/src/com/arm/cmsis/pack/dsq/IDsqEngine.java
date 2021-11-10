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

package com.arm.cmsis.pack.dsq;

import java.util.Collection;

/**
 * Interface of Debug Sequence Engine that parses and executes debug sequences
 */
public interface IDsqEngine extends IDsqApiVersion {

    /**
     * Get the sequence name list
     *
     * @return A list of sequence names provided by the engine, or an empty array
     * @throws DsqException throws exception if there is problem while parsing the
     *                      model
     */
    Collection<String> getDefaultSequenceNames() throws DsqException;

    /**
     * Check if a sequence is disabled
     *
     * @param seqName Name of the sequence
     * @return True if the sequence is disabled
     * @throws DsqException throws exception if there is problem while parsing the
     *                      model
     */
    boolean isSequenceDisabled(String seqName) throws DsqException;

    /**
     * Executes sequence
     *
     * @param seqContext sequence to execute
     * @throws DsqException throws exception if there is problem while parsing the
     *                      model
     */
    void execute(IDsqSequence seqContext) throws DsqException;

    /**
     * Generate the script code to run on Debug Server
     *
     * @param generatorID ID of the script generator
     * @param header      script code of includes and imports
     * @return The script code to run on Debug Server
     * @throws DsqException throws exception if there is problem while parsing the
     *                      model
     */
    String generateCode(String generatorID, String header) throws DsqException;

}
