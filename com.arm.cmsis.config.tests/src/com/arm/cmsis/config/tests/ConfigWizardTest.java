/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
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

package com.arm.cmsis.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;

import com.arm.cmsis.config.model.IConfigWizardItem;
import com.arm.cmsis.config.model.IConfigWizardItem.EItemType;
import com.arm.cmsis.parser.ConfigWizardParser;
import com.arm.cmsis.parser.ConfigWizardScanner;

/**
 * Test cases for CMSIS Configuration Wizard
 */
public class ConfigWizardTest {

    @Test
    public void testWrongConfigWizardStart() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//-------- <<< Us Configuration Wizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//-------- <<< Use ConfigurationWizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//-------- << Use Configuration Wizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//-------- <<< Use Configuration Wizard in Context Menu >> -----------------\n" //$NON-NLS-1$
                + "/*-------- <<< Use Configuration Wizard in Context Menu >>> -----------------*/\n" //$NON-NLS-1$
                + "//     <o>IP1: Address byte 1 <0-255>\n" //$NON-NLS-1$
                + "//     <i> Default: 192\n" //$NON-NLS-1$
                + "#define IP1            192\n" //$NON-NLS-1$
                + "//------------- <<< end of configuration section >>> -----------------------"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();
        assertFalse("This should be a wrong configuration wizard start", parser.containWizard()); //$NON-NLS-1$
    }

    @Test
    public void testCorrectConfigWizardStart1() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//-------- <<< Use Configuration Wizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//-------- <<<Use Configuration Wizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//-------- <<< Use Configuration Wizard in Context Menu>>> -----------------\n" //$NON-NLS-1$
                + "//     <o>IP1: Address byte 1 <0-255>\n" //$NON-NLS-1$
                + "//     <i> Default: 192\n" //$NON-NLS-1$
                + "//------------- <<< end of configuration section >>> -----------------------"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();
        assertTrue("This is a valid configuration wizard start", parser.containWizard()); //$NON-NLS-1$
    }

    @Test
    public void testCorrectConfigWizardStart2() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//-------- <<<   Use   Configuration  Wizard   in Context    Menu >>> -----------------\n" //$NON-NLS-1$
                + "//     <o>IP1: Address byte 1 <0-255>\n" //$NON-NLS-1$
                + "//     <i> Default: 192\n" //$NON-NLS-1$
                + "//------------- <<< end of configuration section >>> -----------------------"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();
        assertTrue("This is a valid configuration wizard start", parser.containWizard()); //$NON-NLS-1$
    }

    @Test
    public void testCorrectConfigWizardStart3() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "/*//-------- <<< Use Configuration Wizard in Context Menu >>> -----------------*/\n" //$NON-NLS-1$
                + "//     <o>IP1: Address byte 1 <0-255>\n" //$NON-NLS-1$
                + "//     <i> Default: 192\n" //$NON-NLS-1$
                + "//------------- <<< end of configuration section >>> -----------------------"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();
        assertTrue("This is a valid configuration wizard start", parser.containWizard()); //$NON-NLS-1$
    }

    @Test
    public void testCorrectConfigWizardStart4() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(true);
        String documentContent = "; *------- <<< Use Configuration Wizard in Context Menu >>> ------------------" //$NON-NLS-1$
                + "; <h> Stack Configuration\n" //$NON-NLS-1$
                + ";   <o> Stack Size (in Bytes) <0x0-0xFFFFFFFF:8>\n" //$NON-NLS-1$
                + "; </h>"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();
        assertTrue("This is a valid configuration wizard start", parser.containWizard()); //$NON-NLS-1$
    }

    @Test
    public void testTokenTypes() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <h> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "//   <i> This is a tooltip\n" //$NON-NLS-1$
                + "//   <e1.13> Enable Chip Select 0 (CSR0)\n" //$NON-NLS-1$
                + "//     <o1.20..31> BA: Base Address <0x0-0xFFF00000:0x100000><#/0x100000>\n" //$NON-NLS-1$
                + "//     <o1.7..8>   PAGES: Page Size      <0=> 1M Byte    <1=> 4M Bytes\n" //$NON-NLS-1$
                + "//                                       <2=> 16M Bytes  <3=> 64M Bytes\n" //$NON-NLS-1$
                + "//     <o1.17>   Check Bit\n" //$NON-NLS-1$
                + "//   </e>\n" //$NON-NLS-1$
                + "//   <q1.4>        DRP: Data Read Protocol\n" //$NON-NLS-1$
                + "//                      <0=> Standard Read\n" //$NON-NLS-1$
                + "//                      <1=> Early Read\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0A10242A);   // EBI_CSR0: Flash\n" //$NON-NLS-1$
                + "//   <s> Change ID\n" //$NON-NLS-1$
                + "#define ID \"123456789\"\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$
                + "//*** <<< end of configuration section >>>    ***"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();
        IConfigWizardItem head = root.getLastChild();

        assertEquals("Wrong token type", IConfigWizardItem.EItemType.HEADING, head.getItemType()); //$NON-NLS-1$
        assertEquals("Wrong tooltip", "This is a tooltip", head.getTooltip()); //$NON-NLS-1$ //$NON-NLS-2$

        int i = 0;
        for (IConfigWizardItem item : head.getChildren()) {
            switch (i) {
            case 0:
                assertEquals("Wrong token type", IConfigWizardItem.EItemType.HEADING_ENABLE, item.getItemType()); //$NON-NLS-1$
                int j = 0;
                for (IConfigWizardItem child : item.getChildren()) {
                    if (j == 0) {
                        assertEquals("Wrong token type", IConfigWizardItem.EItemType.OPTION, child.getItemType()); //$NON-NLS-1$
                        assertEquals("Wrong min bit", 20, child.getMinBit()); //$NON-NLS-1$
                        assertEquals("Wrong max bit", 31, child.getMaxBit()); //$NON-NLS-1$
                        assertEquals("The spinner step is wrong", 0x100000L, child.getSpinStep()); //$NON-NLS-1$
                        assertEquals("The minimum range is wrong", 0x0L, child.getMinValue()); //$NON-NLS-1$
                        assertEquals("The maximum range is wrong", 0xFFF00000L, child.getMaxValue()); //$NON-NLS-1$
                    } else if (j == 1) {
                        assertEquals("Wrong token type", IConfigWizardItem.EItemType.OPTION_SELECT, //$NON-NLS-1$
                                child.getItemType());
                    } else if (j == 2) {
                        assertEquals("Wrong token type", IConfigWizardItem.EItemType.OPTION_CHECK, child.getItemType()); //$NON-NLS-1$
                    }
                    j++;
                }
                break;
            case 1:
                assertEquals("Wrong token type", IConfigWizardItem.EItemType.OPTION_SELECT, item.getItemType()); //$NON-NLS-1$
                break;
            case 2:
                assertEquals("Wrong token type", IConfigWizardItem.EItemType.OPTION_STRING, item.getItemType()); //$NON-NLS-1$
            default:
                break;
            }
            i++;
        }
    }

    @Test
    public void testInvalidIndex() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "//   <o> PAGES: Page Size\n" //$NON-NLS-1$
                + "//     <1=> 1M Byte    <4=> 4M Bytes\n" //$NON-NLS-1$
                + "#define SEL 3;   // Selection index\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem option = parser.parse().getLastChild();
        assertTrue("The index 1 is valid", option.getItems().containsKey(1L)); //$NON-NLS-1$
        assertTrue("The index 4 is valid", option.getItems().containsKey(4L)); //$NON-NLS-1$
        assertFalse("The index 3 is not valid", option.getItems().containsKey(3L)); //$NON-NLS-1$
    }

    @Test
    public void testNumbers() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <h> Numbers Octal\n" //$NON-NLS-1$
                + "//   <o0> 177O       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o1> 177o       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o2> 0177       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o3> 177Q       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o4> 177q       <0-0xffff>\n" //$NON-NLS-1$
                + "#define NUM_OCT0     177O\n" //$NON-NLS-1$
                + "#define NUM_OCT1     177o\n" //$NON-NLS-1$
                + "#define NUM_OCT2     0177\n" //$NON-NLS-1$
                + "#define NUM_OCT3     177Q\n" //$NON-NLS-1$
                + "#define NUM_OCT4     177q\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$

                + "// <h> Numbers Decimal\n" //$NON-NLS-1$
                + "//   <o0> 127\n" //$NON-NLS-1$
                + "//   <o1> 127D\n" //$NON-NLS-1$
                + "//   <o2> 127d\n" //$NON-NLS-1$
                + "#define NUM_DEC0     127\n" //$NON-NLS-1$
                + "#define NUM_DEC1     127D\n" //$NON-NLS-1$
                + "#define NUM_DEC2     127d\n" //$NON-NLS-1$
                + "// </h>" //$NON-NLS-1$

                + "// <h> Numbers Hexadecimal\n" //$NON-NLS-1$
                + "//   <o0> 0x7f       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o1> 0x7F       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o2> 7FH        <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o3> 7Fh        <0-0xffff>\n" //$NON-NLS-1$
                + "#define NUM_HEX0     0x7f\n" //$NON-NLS-1$
                + "#define NUM_HEX1     0x7F\n" //$NON-NLS-1$
                + "#define NUM_HEX2     7FH\n" //$NON-NLS-1$
                + "#define NUM_HEX3     7Fh\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$

                + "// <h> Numbers Binary\n" //$NON-NLS-1$
                + "//   <o0> 1111111B\n" //$NON-NLS-1$
                + "//   <o1> 01111111b\n" //$NON-NLS-1$
                + "//   <o2> 0b01111111\n" //$NON-NLS-1$
                + "//   <o3> 0B1111111\n" //$NON-NLS-1$
                + "#define NUM_BIN0      1111111B\n" //$NON-NLS-1$
                + "#define NUM_BIN1     01111111b\n" //$NON-NLS-1$
                + "#define NUM_BIN2     0b01111111\n" //$NON-NLS-1$
                + "#define NUM_BIN3     0B1111111\n" //$NON-NLS-1$
                + "// </h>"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();

        assertNotNull(root);
        assertTrue(parser.containWizard());

        for (IConfigWizardItem header : root.getChildren()) {
            assertEquals("Wrong token type", IConfigWizardItem.EItemType.HEADING, header.getItemType()); //$NON-NLS-1$
            for (IConfigWizardItem child : header.getChildren()) {
                long number = child.getValue();
                assertEquals("Wrong option number", 127, number); //$NON-NLS-1$
            }
        }
    }

    @Test
    public void testNegativeNumbers() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <h> Numbers Decimal\n" //$NON-NLS-1$
                + "//   <o0> 127\n" //$NON-NLS-1$
                + "//   <o1> 127D\n" //$NON-NLS-1$
                + "//   <o2> 127d\n" //$NON-NLS-1$
                + "#define NUM_DEC0     -127\n" //$NON-NLS-1$
                + "#define NUM_DEC1     -127D\n" //$NON-NLS-1$
                + "#define NUM_DEC2     -127d\n" //$NON-NLS-1$
                + "// </h>"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();

        assertNotNull(root);
        assertTrue(parser.containWizard());

        for (IConfigWizardItem header : root.getChildren()) {
            assertEquals("Wrong token type", IConfigWizardItem.EItemType.HEADING, header.getItemType()); //$NON-NLS-1$
            for (IConfigWizardItem child : header.getChildren()) {
                long number = child.getValue();
                assertEquals("Wrong option number", -127, number); //$NON-NLS-1$
                parser.updateModel(child, "127"); //$NON-NLS-1$
            }
        }

        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "// <h> Numbers Decimal\n" //$NON-NLS-1$
                        + "//   <o0> 127\n" //$NON-NLS-1$
                        + "//   <o1> 127D\n" //$NON-NLS-1$
                        + "//   <o2> 127d\n" //$NON-NLS-1$
                        + "#define NUM_DEC0     127\n" //$NON-NLS-1$
                        + "#define NUM_DEC1     127\n" //$NON-NLS-1$
                        + "#define NUM_DEC2     127\n" //$NON-NLS-1$
                        + "// </h>", //$NON-NLS-1$
                document.get());

        for (IConfigWizardItem header : root.getChildren()) {
            assertEquals("Wrong token type", IConfigWizardItem.EItemType.HEADING, header.getItemType()); //$NON-NLS-1$
            for (IConfigWizardItem child : header.getChildren()) {
                parser.updateModel(child, "-1"); //$NON-NLS-1$
                assertEquals("Wrong option number", -1, child.getValue()); //$NON-NLS-1$
            }
        }

        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "// <h> Numbers Decimal\n" //$NON-NLS-1$
                        + "//   <o0> 127\n" //$NON-NLS-1$
                        + "//   <o1> 127D\n" //$NON-NLS-1$
                        + "//   <o2> 127d\n" //$NON-NLS-1$
                        + "#define NUM_DEC0     -1\n" //$NON-NLS-1$
                        + "#define NUM_DEC1     -1\n" //$NON-NLS-1$
                        + "#define NUM_DEC2     -1\n" //$NON-NLS-1$
                        + "// </h>", //$NON-NLS-1$
                document.get());
    }

    @Test
    public void testWrongNumbers() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <h> Invalid Numbers\n" //$NON-NLS-1$
                + "//   <o0> 0b999      <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o1> 999b       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o2> 999o       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o3> 0999       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o4> 999Q       <0-0xffff>\n" //$NON-NLS-1$
                + "//   <o5> 123abc     <0-0xffff>\n" //$NON-NLS-1$
                + "#define NUM_OCT0     0b999\n" //$NON-NLS-1$
                + "#define NUM_OCT1     999b\n" //$NON-NLS-1$
                + "#define NUM_OCT2     999o\n" //$NON-NLS-1$
                + "#define NUM_OCT3     0999\n" //$NON-NLS-1$
                + "#define NUM_OCT4     999Q\n" //$NON-NLS-1$
                + "#define NUM_OCT5     123abc\n" //$NON-NLS-1$
                + "// </h>\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();

        IConfigWizardItem header = root.getLastChild();
        for (IConfigWizardItem child : header.getChildren()) {
            assertEquals("The item's error type is wrong.", //$NON-NLS-1$
                    IConfigWizardItem.EItemErrorType.NUMBER_PARSE_ERROR, child.getItemErrorType());
        }
    }

    @Test
    public void testWrongRangeBits() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <h> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "//   <o1.20.31> BA: Base Address <0x0-0xFFF00000:0x100000><#/0x100000>\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0A10242A);   // EBI_CSR0: Flash\n" //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();

        assertTrue("Invalid item modifier: <o1.20.31>", parser.getParsingErrorOffset() >= 0); //$NON-NLS-1$
        long expectedOffset = ("//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <h> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "//   ").length(); //$NON-NLS-1$
        assertEquals("Parsing error offset is wrong", expectedOffset, parser.getParsingErrorOffset()); //$NON-NLS-1$
    }

    @Test
    public void testUnkownToken() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <t> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "// </t>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0A10242A);   // EBI_CSR0: Flash\n" //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();

        assertTrue("Unknown token: <t>", parser.getParsingErrorOffset() >= 0); //$NON-NLS-1$
        long expectedOffset = ("//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// ").length(); //$NON-NLS-1$
        assertEquals("Parsing error offset is wrong", expectedOffset, parser.getParsingErrorOffset()); //$NON-NLS-1$
    }

    @Test
    public void testUnknownSelectionToken() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <h> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "//   <e1.13> Enable Chip Select 0 (CSR0)\n" //$NON-NLS-1$
                + "//     <o1.7..8>   PAGES: Page Size      <0=> 1M Byte    <k=> 4M Bytes\n" //$NON-NLS-1$
                + "//                                       <2=> 16M Bytes  <3=> 64M Bytes\n" //$NON-NLS-1$
                + "//                 <i> Selects Active Bits in Base Address\n" //$NON-NLS-1$
                + "//   </e>\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0A10242A);   // EBI_CSR0: Flash\n" //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();

        assertTrue("Invalid selection token: <k=> 4M Byte", parser.getParsingErrorOffset() >= 0); //$NON-NLS-1$
        long expectedOffset = ("//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <h> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "//   <e1.13> Enable Chip Select 0 (CSR0)\n" //$NON-NLS-1$
                + "//     <o1.7..8>   PAGES: Page Size      <0=> 1M Byte    ").length(); //$NON-NLS-1$
        assertEquals("Parsing error offset is wrong", expectedOffset, parser.getParsingErrorOffset()); //$NON-NLS-1$
    }

    @Test
    public void testWrongEndingToken() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <e> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "// </h>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0A10242A);   // EBI_CSR0: Flash\n" //$NON-NLS-1$
                + "}"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        parser.parse();

        assertTrue("Wrong ending token: expected: </e>, actual: </h>", parser.getParsingErrorOffset() >= 0); //$NON-NLS-1$
        long expectedOffset = ("//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "FUNC void Setup (void) {\n" //$NON-NLS-1$
                + "// <e> External Bus Interface (EBI)\n" //$NON-NLS-1$
                + "// ").length(); //$NON-NLS-1$
        assertEquals("Parsing error offset is wrong", expectedOffset, parser.getParsingErrorOffset()); //$NON-NLS-1$
    }

    @Test
    public void testNumberStringInComments() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//-------- <<< Use Configuration Wizard in Context Menu >>> -----------------\n" //$NON-NLS-1$
                + "//   <e>Test Number/String in Comments\n" //$NON-NLS-1$
                + "//     <o>Test number in comments 1\n" //$NON-NLS-1$
                + "// 123\n" //$NON-NLS-1$
                + "//     <i> Correct: 192\n" //$NON-NLS-1$
                + "/* 456 */\n" //$NON-NLS-1$
                + "// 789\n" //$NON-NLS-1$
                + "#define IP1    /* 489 */    123\n" //$NON-NLS-1$
                + "/*\n" //$NON-NLS-1$
                + "//     <o>Test number in comments 2\n" //$NON-NLS-1$
                + "//     <i> Correct: 168\n" //$NON-NLS-1$
                + " 123, 456\n" //$NON-NLS-1$
                + " */\n" //$NON-NLS-1$
                + "#define IP2            123\n" //$NON-NLS-1$
                + "/*\n" //$NON-NLS-1$
                + "//     <o>Test number in comments 3\n" //$NON-NLS-1$
                + "//     <i> Correct: 0\n" //$NON-NLS-1$
                + "//     <o1>Test number in comments 4\n" //$NON-NLS-1$
                + "//     <i> Correct: 1\n" //$NON-NLS-1$
                + " 123, 456\n" //$NON-NLS-1$
                + " */\n" //$NON-NLS-1$
                + "#define IP3            123\n" //$NON-NLS-1$
                + "/* 423 */\n" //$NON-NLS-1$
                + "// 789\n" //$NON-NLS-1$
                + "#define IP4            123\n" //$NON-NLS-1$
                + "/*\n" //$NON-NLS-1$
                + "//     <s>Test string in comments\n" //$NON-NLS-1$
                + "//     <i> Correct: Test \"1\"\n" //$NON-NLS-1$
                + "// \"Wrong 0\"\n" //$NON-NLS-1$
                + " \"Wrong 1\"\n" //$NON-NLS-1$
                + " */\n" //$NON-NLS-1$
                + "// \"Wrong 2\"\n" //$NON-NLS-1$
                + "#define IP5  /* \"Wrong 3\" */  \"Test \\\"1\\\"\"\n" //$NON-NLS-1$
                + "//   </e>\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();

        assertNotNull(root);
        assertTrue(parser.containWizard());
        assertTrue(parser.getParsingErrorOffset() == -1);

        IConfigWizardItem header = root.getLastChild();
        assertEquals(IConfigWizardItem.EItemType.HEADING_ENABLE, header.getItemType());

        int i = 0;
        for (IConfigWizardItem item : header.getChildren()) {
            if (i < 4) {
                assertEquals("Wrong option value", 123, item.getValue()); //$NON-NLS-1$
            } else {
                assertEquals("Wrong string value", "Test \\\"1\\\"", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            i++;
        }
    }

    @Test
    public void testComments() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "/*  Example for enabling code              */\n" //$NON-NLS-1$
                + "// <c1> Use MY_CPU_VARIANT\n" //$NON-NLS-1$
                + "// <i> Use MY_CPU_VARIANT, and set the include file\n" //$NON-NLS-1$
                + "#define MY_CPU_VARIANT\n" //$NON-NLS-1$
                + "#include \"MyCpuVariant.h\"\n" //$NON-NLS-1$
                + "// </c>\n" //$NON-NLS-1$
                + "//*** <<< end of configuration section >>>    ***\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem comment = parser.parse().getLastChild();

        assertEquals("This is a comment item", IConfigWizardItem.EItemType.CODE_ENABLE, comment.getItemType()); //$NON-NLS-1$
        assertTrue("Comment is enabled", comment.getValue() == 1 && !comment.invertValue()); //$NON-NLS-1$

        parser.updateModel(comment, false);

        assertTrue("Comment is disabled", comment.getValue() == 0); //$NON-NLS-1$
        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "/*  Example for enabling code              */\n" //$NON-NLS-1$
                        + "// <c1> Use MY_CPU_VARIANT\n" //$NON-NLS-1$
                        + "// <i> Use MY_CPU_VARIANT, and set the include file\n" //$NON-NLS-1$
                        + "//#define MY_CPU_VARIANT\n" //$NON-NLS-1$
                        + "//#include \"MyCpuVariant.h\"\n" //$NON-NLS-1$
                        + "// </c>\n" //$NON-NLS-1$
                        + "//*** <<< end of configuration section >>>    ***\n", //$NON-NLS-1$
                document.get());

        parser.updateModel(comment, true);

        assertTrue("Comment is enabled again", comment.getValue() == 1); //$NON-NLS-1$
        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "/*  Example for enabling code              */\n" //$NON-NLS-1$
                        + "// <c1> Use MY_CPU_VARIANT\n" //$NON-NLS-1$
                        + "// <i> Use MY_CPU_VARIANT, and set the include file\n" //$NON-NLS-1$
                        + "#define MY_CPU_VARIANT\n" //$NON-NLS-1$
                        + "#include \"MyCpuVariant.h\"\n" //$NON-NLS-1$
                        + "// </c>\n" //$NON-NLS-1$
                        + "//*** <<< end of configuration section >>>    ***\n", //$NON-NLS-1$
                document.get());
    }

    @Test
    public void testUncomment() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "/*  Example for disabling code              */\n" //$NON-NLS-1$
                + "// <!c1> Disable log\n" //$NON-NLS-1$
                + "// <i> Disable log file generation\n" //$NON-NLS-1$
                + "#define _USE_LOG\n" //$NON-NLS-1$
                + "// </c>\n" //$NON-NLS-1$
                + "//*** <<< end of configuration section >>>    ***\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem comment = parser.parse().getLastChild();

        assertEquals("This is an uncomment item", IConfigWizardItem.EItemType.CODE_DISABLE, comment.getItemType()); //$NON-NLS-1$
        assertTrue("Comment is not disabled", comment.getValue() == 0 && comment.invertValue()); //$NON-NLS-1$

        parser.updateModel(comment, true);

        assertEquals("Comment is disabled", 1, comment.getValue()); //$NON-NLS-1$
        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "/*  Example for disabling code              */\n" //$NON-NLS-1$
                        + "// <!c1> Disable log\n" //$NON-NLS-1$
                        + "// <i> Disable log file generation\n" //$NON-NLS-1$
                        + "//#define _USE_LOG\n" //$NON-NLS-1$
                        + "// </c>\n" //$NON-NLS-1$
                        + "//*** <<< end of configuration section >>>    ***\n", //$NON-NLS-1$
                document.get());

        parser.updateModel(comment, false);

        assertTrue("Comment is not disabled again", comment.getValue() == 0); //$NON-NLS-1$
        assertEquals("Document is not correctly updated", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "/*  Example for disabling code              */\n" //$NON-NLS-1$
                        + "// <!c1> Disable log\n" //$NON-NLS-1$
                        + "// <i> Disable log file generation\n" //$NON-NLS-1$
                        + "#define _USE_LOG\n" //$NON-NLS-1$
                        + "// </c>\n" //$NON-NLS-1$
                        + "//*** <<< end of configuration section >>>    ***\n", //$NON-NLS-1$
                document.get());
    }

    @Test
    public void testInconsistentComment() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "/*  Example for inconsistent comment              */\n" //$NON-NLS-1$
                + "// <c1>  Example of inconsistent comment\n" //$NON-NLS-1$
                + "// <i> a mix of commented and uncommented lines in the block create an inconsistency\n" //$NON-NLS-1$
                + "// This type of comment, mixed with uncommented lines, creates the inconsistency\n" //$NON-NLS-1$
                + "/* You can use this type of comment without creating an inconsistency */\n" //$NON-NLS-1$
                + "   do_whatever;              // adding this type of comment here is allowed\n" //$NON-NLS-1$
                + "// </c>\n" //$NON-NLS-1$
                + "//*** <<< end of configuration section >>>    ***\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem comment = parser.parse().getLastChild();

        assertTrue("Comment is not inconsistent", comment.isInconsistent()); //$NON-NLS-1$
        assertEquals("Inconsistent comment is not always checked", 1, comment.getValue()); //$NON-NLS-1$
    }

    @Test
    public void testString() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <s> Change ID\n" //$NON-NLS-1$
                + "// <s1.30> Change Password String\n" //$NON-NLS-1$
                + "// <s2.30> Confirm Password String\n" //$NON-NLS-1$
                + "#define ID  \"My User ID\"\n" //$NON-NLS-1$
                + "char pw[] = \"My Password\"\n" //$NON-NLS-1$
                + "char cpw[] = \"My Confirmed Password\"\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);

        IConfigWizardItem root = parser.parse();
        int i = 0;
        for (IConfigWizardItem item : root.getChildren()) {
            assertEquals("Wrong item type", EItemType.OPTION_STRING, item.getItemType()); //$NON-NLS-1$
            if (i == 0) {
                assertEquals("Wrong string content", "My User ID", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (i == 1) {
                assertEquals("Wrong string content", "My Password", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (i == 2) {
                assertEquals("Wrong string content", "My Confirmed Password", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            i++;
        }

        i = 0;
        // change the strings
        for (IConfigWizardItem item : root.getChildren()) {
            if (i == 0) {
                parser.updateModel(item, "User ID"); //$NON-NLS-1$
            } else if (i == 1) {
                parser.updateModel(item, "This is \"My\" Password"); //$NON-NLS-1$
            } else if (i == 2) {
                parser.updateModel(item, "This is \"NOT My\" Confirmed Password"); //$NON-NLS-1$
            }
            i++;
        }

        i = 0;
        for (IConfigWizardItem item : root.getChildren()) {
            if (i == 0) {
                assertEquals("Wrong string content", "User ID", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (i == 1) {
                assertEquals("Wrong string content", "This is \\\"My\\\" Password", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (i == 2) {
                assertEquals("Wrong string content", "This is \\\"NOT My\\\" Confirmed Password", item.getString()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            i++;
        }

        assertEquals("Document content is wrong", //$NON-NLS-1$
                "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                        + "// <s> Change ID\n" //$NON-NLS-1$
                        + "// <s1.30> Change Password String\n" //$NON-NLS-1$
                        + "// <s2.30> Confirm Password String\n" //$NON-NLS-1$
                        + "#define ID  \"User ID\"\n" //$NON-NLS-1$
                        + "char pw[] = \"This is \\\"My\\\" Password\"\n" //$NON-NLS-1$
                        + "char cpw[] = \"This is \\\"NOT My\\\" Confirmed Password\"\n", //$NON-NLS-1$
                document.get());
    }

    @Test
    public void testBits() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "//   <o0.0 > Bit 0  <o0.1 > Bit 1   <o0.2 > Bit 2   <o0.3 > Bit 3   <o0.4 > Bit 4   <o0.5 > Bit 5   <o0.6 > Bit 6   <o0.7 > Bit 7\n" //$NON-NLS-1$
                + "//   <o0.8 > Bit 8  <o0.9 > Bit 9   <o0.10> Bit 10  <o0.11> Bit 11  <o0.12> Bit 12  <o0.13> Bit 13  <o0.14> Bit 14  <o0.15> Bit 15\n" //$NON-NLS-1$
                + "//   <o0.16> Bit 16 <o0.17> Bit 17  <o0.18> Bit 18  <o0.19> Bit 19  <o0.20> Bit 20  <o0.21> Bit 21  <o0.22> Bit 22  <o0.23> Bit 23\n" //$NON-NLS-1$
                + "//   <o0.24> Bit 24 <o0.25> Bit 25  <o0.26> Bit 26  <o0.27> Bit 27  <o0.28> Bit 28  <o0.29> Bit 29  <o0.30> Bit 30  <o0.31> Bit 31\n" //$NON-NLS-1$
                + "#define BIT_VAL         (0x0000ffff)\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem root = parser.parse();
        int i = 0;
        for (IConfigWizardItem child : root.getChildren()) {
            assertEquals("Wrong itme type", IConfigWizardItem.EItemType.OPTION_CHECK, child.getItemType()); //$NON-NLS-1$
            if (i < 16) {
                assertEquals("Wrong parsed bit value", 1, child.getValue()); //$NON-NLS-1$
            } else {
                assertEquals("Wrong parsed bit value", 0, child.getValue()); //$NON-NLS-1$
            }
            i++;
        }
        assertEquals("Wrong number of bits parsed", 32, i); //$NON-NLS-1$

        for (IConfigWizardItem child : root.getChildren()) {
            parser.updateModel(child, (1L - child.getValue()) == 1);
        }

        i = 0;
        for (IConfigWizardItem child : root.getChildren()) {
            assertEquals("Wrong itme type", IConfigWizardItem.EItemType.OPTION_CHECK, child.getItemType()); //$NON-NLS-1$
            if (i < 16) {
                assertEquals("Wrong parsed bit value", 0, child.getValue()); //$NON-NLS-1$
            } else {
                assertEquals("Wrong parsed bit value", 1, child.getValue()); //$NON-NLS-1$
            }
            i++;
        }

        String lastLine = getLastLine(document.get());
        assertEquals("Document is modified incorrectly", "#define BIT_VAL         (0xFFFF0000)", lastLine); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testEnable() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "//   <e1.13> Enable Chip Select 0 (CSR0)\n" //$NON-NLS-1$
                + "//     <o1.20..31> BA: Base Address <0x0-0xFFF00000:0x100000><#/0x100000>\n" //$NON-NLS-1$
                + "//                 <i> Start Address for Chip Select Signal\n" //$NON-NLS-1$
                + "//     <o1.7..8>   PAGES: Page Size      <0=> 1M Byte    <1=> 4M Bytes\n" //$NON-NLS-1$
                + "//                                       <2=> 16M Bytes  <3=> 64M Bytes\n" //$NON-NLS-1$
                + "//     <o1.9..11>  TDF: Data Float Output Time <0-7>\n" //$NON-NLS-1$
                + "//                 <i> Number of Cycles Added after the Transfer\n" //$NON-NLS-1$
                + "//     <e1.5>      WSE: Enable Wait State Generation\n" //$NON-NLS-1$
                + "//       <o1.19>     CSSON: Clock Security System enable\n" //$NON-NLS-1$
                + "//       <o1.2..4>   NWS: Number of Standard Wait States <1-8><#-1>\n" //$NON-NLS-1$
                + "//     </e>\n" //$NON-NLS-1$
                + "//   </e>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x010004A9);   // EBI_CSR0: Flash\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);

        String lastLine;
        IConfigWizardItem enable = parser.parse().getLastChild();

        assertEquals("Wrong root item type", IConfigWizardItem.EItemType.HEADING_ENABLE, enable.getItemType()); //$NON-NLS-1$

        for (IConfigWizardItem item : enable.getChildren()) {
            assertFalse("Item should be disabled", item.canModify()); //$NON-NLS-1$
        }

        // update <e1.13> Enable Chip Select 0 (CSR0)
        parser.updateModel(enable, true);
        for (IConfigWizardItem item : enable.getChildren()) {
            assertTrue("Item should be enabled", item.canModify()); //$NON-NLS-1$
        }
        lastLine = getLastLine(document.get());
        assertEquals("Document content is not correctly updated", //$NON-NLS-1$
                "  _WDWORD(0xFFE00000, 0x010024A9);   // EBI_CSR0: Flash", //$NON-NLS-1$
                lastLine);

        // update <e1.5> WSE: Enable Wait State Generation
        IConfigWizardItem subEnable = enable.getLastChild();
        parser.updateModel(subEnable, false);
        for (IConfigWizardItem item : subEnable.getChildren()) {
            assertFalse("Item should be disabled", item.canModify()); //$NON-NLS-1$
        }
        lastLine = getLastLine(document.get());
        assertEquals("Document content is not correctly updated", //$NON-NLS-1$
                "  _WDWORD(0xFFE00000, 0x01002489);   // EBI_CSR0: Flash", //$NON-NLS-1$
                lastLine);
    }

    @Test
    public void testStringCombox() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "//   <o TIMESTAMP_SOURCE>string options without number and keyword character\n" //$NON-NLS-1$
                + "//      <dwt=> DWT Cycle Counter\n" //$NON-NLS-1$
                + "//      <systick=> SysTick\n" //$NON-NLS-1$
                + "//      <user=> User Timer\n" //$NON-NLS-1$
                + "//   <i>Selects source for 32-bit time stamp\n" //$NON-NLS-1$
                + "#define TIMESTAMP_SOURCE  dwt\n"; //$NON-NLS-1$

        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem item = parser.parse().getLastChild();

        assertEquals("The modified value is incorrect", "dwt", item.getSelStr()); //$NON-NLS-1$

        parser.updateModel(item, "User Timer"); //$NON-NLS-1$
        assertEquals("The value is incorrect", "user", item.getSelStr()); //$NON-NLS-1$
        String lastLine = getLastLine(document.get());
        assertEquals("The document is incorrectly updated", //$NON-NLS-1$
                "#define TIMESTAMP_SOURCE  user", //$NON-NLS-1$
                lastLine);
    }

    @Test
    public void testStringCombox2() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <o define> string options with numbers and keyword character\n" //$NON-NLS-1$
                + "//<str111=> string with number\n" //$NON-NLS-1$
                + "//<0x123FF=> number\n" //$NON-NLS-1$
                + "//<h_Keyword=> string containing keyword h\n" //$NON-NLS-1$
                + "//<i_Keyword=> string containing keyword i\n" //$NON-NLS-1$
                + "//<o_Keyword=> string containing keyword o\n" //$NON-NLS-1$
                + "#define 0x123FF \"123\"\n"; //$NON-NLS-1$

        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem item = parser.parse().getLastChild();

        assertEquals("The modified value is incorrect", "0x123FF", item.getSelStr()); //$NON-NLS-1$

        parser.updateModel(item, "string with number"); //$NON-NLS-1$
        assertEquals("The value is incorrect", "str111", item.getSelStr()); //$NON-NLS-1$
        parser.updateModel(item, "number"); //$NON-NLS-1$
        assertEquals("The value is incorrect", "0x123FF", item.getSelStr()); //$NON-NLS-1$
        parser.updateModel(item, "string containing keyword o"); //$NON-NLS-1$
        assertEquals("The value is incorrect", "o_Keyword", item.getSelStr()); //$NON-NLS-1$

        String lastLine = getLastLine(document.get());
        assertEquals("The document is incorrectly updated", //$NON-NLS-1$
                "#define o_Keyword \"123\"", //$NON-NLS-1$
                lastLine);
    }

    @Test
    public void testCombox() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <o1.20..31> BA: Base Address <0x0-0xFFF00000:0x100000><#/0x100000>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x0AB0242A);   // EBI_CSR0: Flash\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);
        IConfigWizardItem item = parser.parse().getLastChild();

        assertEquals("The modified value is incorrect", 0x0AB, item.getValue()); //$NON-NLS-1$
        assertEquals("The spinner step is incorrect", 0x100000, item.getSpinStep()); //$NON-NLS-1$

        // the user has entered "0x0ABFFFFF" in the combo box
        parser.updateModel(item, 0x0ABFFFFFL);
        assertEquals("The value is incorrect", 0x0AB, item.getValue()); //$NON-NLS-1$
        String lastLine = getLastLine(document.get());
        assertEquals("The document is incorrectly updated", //$NON-NLS-1$
                "  _WDWORD(0xFFE00000, 0x0AB0242A);   // EBI_CSR0: Flash", //$NON-NLS-1$
                lastLine);
    }

    @Test
    public void testConsecutiveChanges() throws Exception {
        ConfigWizardScanner scanner = new ConfigWizardScanner(false);
        String documentContent = "//*** <<< Use Configuration Wizard in Context Menu >>> ***\n" //$NON-NLS-1$
                + "// <s> Change ID\n" //$NON-NLS-1$
                + "// <s1.30> Change Password String\n" //$NON-NLS-1$
                + "#define ID  \"My User ID\"\n" //$NON-NLS-1$
                + "char pw[] = \"My Password\"\n" //$NON-NLS-1$
                + "// <e1.13> Enable Chip Select 0 (CSR0)\n" //$NON-NLS-1$
                + "//   <o1.20..31> BA: Base Address <0x0-0xFFF00000:0x100000><#/0x100000>\n" //$NON-NLS-1$
                + "//               <i> Start Address for Chip Select Signal\n" //$NON-NLS-1$
                + "//   <o1.7..8>   PAGES: Page Size      <0=> 1M Byte    <1=> 4M Bytes\n" //$NON-NLS-1$
                + "//                                     <2=> 16M Bytes  <3=> 64M Bytes\n" //$NON-NLS-1$
                + "//   <o1.9..11>  TDF: Data Float Output Time <0-7>\n" //$NON-NLS-1$
                + "//               <i> Number of Cycles Added after the Transfer\n" //$NON-NLS-1$
                + "//   <e1.5>      WSE: Enable Wait State Generation\n" //$NON-NLS-1$
                + "//     <o1.2..4>   NWS: Number of Standard Wait States <1-8><#-1>\n" //$NON-NLS-1$
                + "//   </e>\n" //$NON-NLS-1$
                + "// </e>\n" //$NON-NLS-1$
                + "  _WDWORD(0xFFE00000, 0x010004A9);   // EBI_CSR0: Flash\n"; //$NON-NLS-1$
        IDocument document = new Document(documentContent);
        ConfigWizardParser parser = new ConfigWizardParser(scanner, document);

        IConfigWizardItem root = parser.parse();
        int i = 0;
        for (IConfigWizardItem item : root.getChildren()) {
            // change the strings
            if (i == 0) {
                parser.updateModel(item, "This is My User ID"); //$NON-NLS-1$
            } else if (i == 1) {
                parser.updateModel(item, "This is \"My\" Password"); //$NON-NLS-1$
            } else { // <e1.13>
                int j = 0;
                for (IConfigWizardItem child : item.getChildren()) {
                    if (j == 0) { // <o1.20..31>
                        parser.updateModel(child, 0x0ABFFFFFL);
                    } else if (j == 1) { // <o1.7..8>
                        parser.updateModel(child, 3L);
                    } else if (j == 2) { // <o1.9..11>
                        parser.updateModel(child, 7L);
                    } else if (j == 3) { // <e1.5>
                        IConfigWizardItem gc = child.getLastChild(); // <o1.2..4>
                        parser.updateModel(gc, "10"); // exceeds the range //$NON-NLS-1$
                        parser.updateModel(child, false);
                    }
                    j++;
                }
            }
            i++;
        }

        String lastLine = getLastLine(document.get());
        assertEquals("Document is not correctly udpated", //$NON-NLS-1$
                "  _WDWORD(0xFFE00000, 0x0AB00F9D);   // EBI_CSR0: Flash", //$NON-NLS-1$
                lastLine);
    }

    private String getLastLine(String document) {
        String[] lines = document.split("\\\n"); //$NON-NLS-1$
        return lines[lines.length - 1];
    }

}
