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

package com.arm.cmsis.pack.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * Console preferences : colors, activation
 *
 */
public class CpConsolePreferencePage extends FieldEditorPreferencePage implements
IWorkbenchPreferencePage {

	public CpConsolePreferencePage() {
		super(GRID);
	}


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CpPlugInUI.getDefault().getPreferenceStore());
	}

	@Override
	public Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE_CONSOLE);
	}


	@Override
	protected void createFieldEditors() {

		addField(new BooleanFieldEditor(CpUIPreferenceConstants.CONSOLE_PRINT_IN_CDT, CpStringsUI.CpConsolePreferencePage_PrintInCdtConsole, SWT.NONE, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CpUIPreferenceConstants.CONSOLE_OPEN_ON_OUT, CpStringsUI.CpConsolePreferencePage_AutoOpen, SWT.NONE, getFieldEditorParent()));

		addField(new ColorFieldEditor(CpUIPreferenceConstants.CONSOLE_OUT_COLOR, CpStringsUI.CpConsolePreferencePage_OutputColor, getFieldEditorParent()));
		addField(new ColorFieldEditor(CpUIPreferenceConstants.CONSOLE_INFO_COLOR, CpStringsUI.CpConsolePreferencePage_InfoColor, getFieldEditorParent()));
		addField(new ColorFieldEditor(CpUIPreferenceConstants.CONSOLE_WARNING_COLOR, CpStringsUI.CpConsolePreferencePage_WarnColor, getFieldEditorParent()));
		addField(new ColorFieldEditor(CpUIPreferenceConstants.CONSOLE_ERROR_COLOR, CpStringsUI.CpConsolePreferencePage_ErrorColor, getFieldEditorParent()));
		addField(new ColorFieldEditor(CpUIPreferenceConstants.CONSOLE_BG_COLOR, CpStringsUI.CpConsolePreferencePage_BgColor, getFieldEditorParent()));

	}




}
