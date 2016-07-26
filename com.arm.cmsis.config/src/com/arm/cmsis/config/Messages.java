/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.config;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.arm.cmsis.config.messages"; //$NON-NLS-1$
	public static String ConfigEditor_CollapseAll;
	public static String ConfigEditor_CollapseAllItems;
	public static String ConfigEditor_ErrorInNestedTextEditor;
	public static String ConfigEditor_ExpandAll;
	public static String ConfigEditor_ExpandAllItems;
	public static String ConfigEditor_FirstPageText;
	public static String ConfigEditor_Help;
	public static String ConfigEditor_HelpForConfigWizard;
	public static String ConfigEditor_InvalidEditorInput;
	public static String ConfigEditor_Option;
	public static String ConfigEditor_ParsingConfigWizard;
	public static String ConfigEditor_SecondPageText;
	public static String ConfigEditor_Value;
	public static String ConfigWizardParser_BlockComment;
	public static String ConfigWizardParser_Comment;
	public static String ConfigWizardParser_CorrectTokenFormat;
	public static String ConfigWizardParser_EndOfConfiguration;
	public static String ConfigWizardParser_ErrorInConfigWizard;
	public static String ConfigWizardParser_FailToParseModifierNumber;
	public static String ConfigWizardParser_Number;
	public static String ConfigWizardParser_Range;
	public static String ConfigWizardParser_RealIs;
	public static String ConfigWizardParser_Selection;
	public static String ConfigWizardParser_SkipTooManyLines;
	public static String ConfigWizardParser_String;
	public static String ConfigWizardParser_SyntaxErrorAtLine;
	public static String ConfigWizardParser_TooltipForUnknownConfigItem;
	public static String ConfigWizardParser_Unknown;
	public static String ConfigWizardParser_UnknownTokenType;
	public static String ConfigWizardParser_WrongEndingToken;
	public static String ConfigWizardParser_WrongHeadTokenFormat;
	public static String ConfigWizardParser_WrongModificationFormat;
	public static String ConfigWizardParser_WrongModifierFormat;
	public static String ConfigWizardParser_WrongRangeSelectionFormat;
	public static String ConfigWizardParser_WrongSelectionToken;
	public static String ConfigWizardParser_WrongTokenFormat;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
