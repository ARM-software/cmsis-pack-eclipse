package com.arm.cmsis.pack.installer.ui;

import org.eclipse.core.expressions.PropertyTester;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackInstaller;

public class CpPackInstallerUiPropertyTester extends PropertyTester {
    public static final String MY_PROPERTY = "canUpdate"; //$NON-NLS-1$
	public CpPackInstallerUiPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (CpPlugIn.getPackManager() != null && CpPlugIn.getPackManager().getPackInstaller() != null) {
			ICpPackInstaller packInstaller = CpPlugIn.getPackManager().getPackInstaller();
			return !packInstaller.isUpdatingPacks();
		}
		return true;
	}

}
