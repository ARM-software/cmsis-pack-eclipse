package com.arm.cmsis.pack.ui.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class CpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public CpPreferencePage() {
	}


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CpPlugInUI.getDefault().getCorePreferenceStore());
	}


	@Override
	protected void createFieldEditors() {
		 addField(new DirectoryFieldEditor(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel,
			        getFieldEditorParent()));
	}


	@Override
	public Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
	}
}
