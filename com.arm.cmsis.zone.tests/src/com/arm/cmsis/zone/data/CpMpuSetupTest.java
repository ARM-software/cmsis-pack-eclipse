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

package com.arm.cmsis.zone.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.parser.CpXmlParser;

/**
 * JUnit class to test MPU allocation algorithms
 */
public class CpMpuSetupTest {

    /**
     * Testing lazy creation of memory region collection
     */
    @Test
    public void nullTest() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);
        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(null);
        assertNotNull(regions);
        assertTrue(regions.isEmpty());
    }

    /**
     * Testing allocation of two blocks for Arm V8 MPU expected: two regions
     */
    @Test
    public void testTwoBlocks8() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        Collection<ICpMpuRegion> regions = construct2Regions(mpuSetup);
        assertNotNull(regions);
        assertTrue(regions.size() == 2);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftl2BlocksMpu8.xml"); //$NON-NLS-1$
        assertEquals("MPU8 2 Blocks FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of two blocks for Arm V7 MPU expected: one region
     */
    @Test
    public void testTwoBlocks7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);
        Collection<ICpMpuRegion> regions = construct2Regions(mpuSetup);
        assertTrue(regions.size() == 1);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftl2BlocksMpu7.xml"); //$NON-NLS-1$
        assertEquals("MPU7 2 Blocks FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of 96 Byte block for Arm V7 MPU expected: one region
     */
    @Test
    public void test96ByteBlock7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        memoryBlocks.add(createMemoryBlock("Mem96", 0x00280000, 96, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$

        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(memoryBlocks);
        assertTrue(regions.size() == 1);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftl96ByteBlockMpu7.xml"); //$NON-NLS-1$
        assertEquals("MPU7 96 Byte Block FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of 32 + 96 Byte blocks for Arm V7 MPU expected: one region
     */
    @Test
    public void testTwoSmallBlocks7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        memoryBlocks.add(createMemoryBlock("Mem32", 0x00280060, 32, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(createMemoryBlock("Mem96", 0x00280080, 96, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$

        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(memoryBlocks);
        assertTrue(regions.size() == 1);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftlTwoSmallBlocksMpu7.xml"); //$NON-NLS-1$
        assertEquals("MPU7 2 Small Blocks FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of 512 K + 32 + 96 Byte blocks for Arm V7 MPU expected:
     * two regions
     */
    @Test
    public void testDifferentSizeBlocks7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        memoryBlocks.add(createMemoryBlock("Mem521K", 0x00200000, 0x80000, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(createMemoryBlock("Mem96", 0x00280000, 96, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(createMemoryBlock("Mem32", 0x00280060, 32, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$

        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(memoryBlocks);
        assertTrue(regions.size() == 2);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftlDifferentSizeBlocksMpu7.xml"); //$NON-NLS-1$
        assertEquals("MPU7 Different Blocks FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of (512 K -32) + 32 Byte blocks for Arm V7 MPU expected:
     * one region
     */
    @Test
    public void testAdjustedSizeBlocks7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        memoryBlocks.add(createMemoryBlock("Mem521K-32", 0x00200000, 0x7FFE0, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(createMemoryBlock("Mem32", 0x0027FFE0, 32, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$

        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(memoryBlocks);
        assertTrue(regions.size() == 1);

        String xml = toFtlXmlString(mpuSetup);
        assertNotNull("Generated FTL model string", xml); //$NON-NLS-1$

        String goldenXml = readXmlResourceString("ftlAdjustedSizeBlocksMpu7.xml"); //$NON-NLS-1$
        assertEquals("MPU7 Adjusted Blocks FTL", goldenXml, xml); //$NON-NLS-1$
    }

    /**
     * Testing allocation of (512 K -32) + 32 Byte blocks for Arm V7 MPU expected:
     * one region
     */
    @Test
    public void testUnalignedSizeBlock7() {
        ICpMpuSetup mpuSetup = new CpMpuSetup(null);
        mpuSetup.setAttribute(CmsisConstants.TYPE, CmsisConstants.V7M);

        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        ICpMemoryBlock mb = createMemoryBlock("Mem521K-32", 0x00200000, 0x7FFE0, "rwx"); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(mb);

        Collection<ICpMpuRegion> regions = mpuSetup.constructMpuRegions(memoryBlocks);
        assertTrue(regions.size() == 1);

        ICpMpuRegion r = regions.iterator().next();
        assertTrue(r != null);
        assertTrue(r.getSize() > mb.getSize());
        assertEquals(r.getSize(), ICpMpuRegion.alignToMpu7(mb.getSize()));
    }

    /**
     * Creates memory block with given properties
     *
     * @param name   block name
     * @param start  start address (logical)
     * @param size   block size
     * @param access access string (rwx)
     * @return newly created CpMemoryBlock
     */
    private ICpMemoryBlock createMemoryBlock(String name, long start, long size, String access) {
        ICpMemoryBlock block = new CpMemoryBlock();
        block.setAttribute(CmsisConstants.NAME, name);
        block.attributes().setAttributeHex(CmsisConstants.START, start);
        block.attributes().setAttributeHex(CmsisConstants.SIZE, size);
        block.setAttribute(CmsisConstants.ACCESS, access);
        return block;
    }

    /**
     * Creates two memory blocks of 4 Kbytes with a gap of 8 Kbytes
     *
     * @return collection of
     */
    private Collection<ICpMemoryBlock> createMemoryBlocks2() {
        Collection<ICpMemoryBlock> memoryBlocks = new LinkedList<>();
        memoryBlocks.add(createMemoryBlock("Mem2", 8192, 4096, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        memoryBlocks.add(createMemoryBlock("Mem1", 0x0L, 4096, "rwx")); //$NON-NLS-1$ //$NON-NLS-2$
        return memoryBlocks;
    }

    /**
     * Constructs two memory regions
     *
     * @param mpuSetup ICpMpuSetup
     * @return collection of ICpMpuRegion
     */
    private Collection<ICpMpuRegion> construct2Regions(ICpMpuSetup mpuSetup) {
        Collection<ICpMemoryBlock> memoryBlocks = createMemoryBlocks2();
        return mpuSetup.constructMpuRegions(memoryBlocks);
    }

    /**
     * Creates FTL model and prints it to string
     *
     * @param mpuSetup ICpMpuSetup
     * @return XML String
     */
    private String toFtlXmlString(ICpMpuSetup mpuSetup) {
        ICpItem root = new CpItem(null, CmsisConstants.FZONE);
        // check if ftlModel is Generated properly
        ICpItem ftlModel = mpuSetup.toFtlModel(root);
        assertNotNull("Generated FTL model", ftlModel); //$NON-NLS-1$
        root.addChild(ftlModel);
        return toXmlString(root);
    }

    /**
     * Prints ICpItem to XML string
     *
     * @param root root ICpItem
     * @return XML string
     */
    private String toXmlString(ICpItem root) {
        CpXmlParser parser = new CpXmlParser();
        parser.setXsdFile("fzone.xsd"); //$NON-NLS-1$
        String xml = parser.writeToXmlString(root);
        assertTrue("XML string from ICpItem", parser.getSevereErrorCount() == 0); //$NON-NLS-1$
        return xml;
    }

    /**
     * Reads resource local to this class and reads it to XML string
     *
     * @param resource resource
     * @return XML string
     */
    private String readXmlResourceString(String resource) {
        String xml = CmsisConstants.EMPTY_STRING;
        try (InputStream inputStream = this.getClass().getResourceAsStream(resource);
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            xml = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return xml;
        }
        // make sure to ignore any line endings and spaces by parsing xml and
        // re-generating the string
        CpXmlParser parser = new CpXmlParser();
        ICpItem root = parser.parseXmlString(xml);
        assertNotNull(resource, root);
        return toXmlString(root);
    }
}
