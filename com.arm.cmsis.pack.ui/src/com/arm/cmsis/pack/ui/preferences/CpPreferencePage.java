/*******************************************************************************
 *  Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Liviu Ionescu - initial implementation
 *     ARM Ltd and ARM Germany GmbH - application-specific implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui.preferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.utils.Encryptor;
import com.arm.cmsis.pack.utils.Utils;

public class CpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	Map<String, String> itemFromFile = new HashMap<>();
	static final String[] FILTER_NAME = { CpStringsUI.CpPreferencePage_PdscFiles };
	static final String[] FILTER_EXT = { "*.pdsc" }; //$NON-NLS-1$
	private StringFieldEditor fCmsisRootEditor;

	boolean fAutoUpdate; // flag of 'check for update once a day'

	Button fNoProxyButton;
	Button fHttpProxyButton;
	Button fSockProxyButton;
	private int fProxyMode;
	private Text fAddressText;
	private String fAddress;
	private Text fPortText;
	private String fPort;
	private Text fUserText;
	private String fUser;
	private Text fPassText;
	private String fPass;

	protected static Encryptor encryptor;

	public CpPreferencePage() {
		super(GRID);

		if (encryptor == null) {
	        encryptor = Encryptor.getEncryptor(Encryptor.DEFAULT_KEY);
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CpPlugInUI.getDefault().getCorePreferenceStore());
		fAutoUpdate = CpPreferenceInitializer.getAutoUpdateFlag();
		loadProxyData();
	}


	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		if(!CpPreferenceInitializer.isCmsisRootEditable()) {
			fCmsisRootEditor = new StringFieldEditor(CpPreferenceInitializer.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
			fCmsisRootEditor.setEnabled(false, parent);
			fCmsisRootEditor.getLabelControl(parent).setEnabled(true);
		} else {
			fCmsisRootEditor = new CpDirectoryFieldEditor(CpPreferenceInitializer.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
		}
		addField(fCmsisRootEditor);
		addField(new BooleanFieldEditor(CpPreferenceInitializer.CMSIS_PACK_INSTALL_MISSING_PACKS_PREFERENCE, CpStringsUI.PreferencesInstallMissingPacks, SWT.NONE, parent));
	}

	@Override
	protected Control createContents(Composite parent) {
		// Row 1: field editor for 'CMSIS-Pack root folder'
		Control control = createCmsisPackRootFolder(parent);

		// Row 2: check box of "Check for Update"
		createCheckForUpdate(parent);

		// separator and label
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Row 3: Proxy Settings
	    createProxyContents(parent);

	    // Row 4: dummy to align the buttons 'Restore Defaults' and 'Apply'
	    Label description = new Label(parent, SWT.NONE);
	    description.setText(CmsisConstants.EMPTY_STRING);
		GridData dummy = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		description.setLayoutData(dummy);

		return control;
	}

	protected void createCheckForUpdate(Composite parent) {
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(CpStringsUI.CpPreferencePage_CheckForUpdatesEveryday);
		checkbox.setSelection(fAutoUpdate);
		checkbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fAutoUpdate = !fAutoUpdate;
			}
		});
	}

	protected Control createCmsisPackRootFolder(Composite parent) {

		Composite feComp = new Composite(parent, SWT.NULL);
		GridLayout feLayout = new GridLayout();		// grid layout for field editor
		feLayout.numColumns = 1;
		feLayout.marginHeight = 0;
		feLayout.marginWidth = 0;
		feLayout.marginHeight = 0;
		feComp.setLayout(feLayout);

		GridData feLayoutData;

		feLayoutData = new GridData();				// grid layout data for field data
		feLayoutData.verticalAlignment = SWT.TOP;
		feLayoutData.grabExcessVerticalSpace = false;
		feLayoutData.horizontalAlignment = SWT.FILL;
		feLayoutData.grabExcessHorizontalSpace = true;
		feComp.setLayoutData(feLayoutData);

		return super.createContents(feComp);
	}

	protected void createProxyContents(Composite parent) {
		Group proxyComposite = new Group(parent, SWT.LEFT);
        GridLayout layout = new GridLayout(5, false);
        proxyComposite.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        proxyComposite.setLayoutData(data);
        proxyComposite.setText(CpStringsUI.CpPreferencePage_ProxySettings);

        SelectionListener selectionListener = new SelectionAdapter() {

            @Override
			public void widgetSelected(SelectionEvent e) {
                selectProxyMode(fNoProxyButton.getSelection(),
                		fHttpProxyButton.getSelection(),
                		fSockProxyButton.getSelection());
            }
        };

        fNoProxyButton = createRadioButton(proxyComposite, CpStringsUI.CpPreferencePage_NoProxy);
        fNoProxyButton.addSelectionListener(selectionListener);
        fNoProxyButton.setSelection(fProxyMode == 0);

        fHttpProxyButton = createRadioButton(proxyComposite, CpStringsUI.CpPreferencePage_HttpProxy);
        fHttpProxyButton.addSelectionListener(selectionListener);
        fHttpProxyButton.setSelection(fProxyMode == 1);

        fSockProxyButton = createRadioButton(proxyComposite, CpStringsUI.CpPreferencePage_SocksProxy);
        fSockProxyButton.addSelectionListener(selectionListener);
        fSockProxyButton.setSelection(fProxyMode == 2);

        Label label = new Label(proxyComposite, SWT.NULL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        label = new Label(proxyComposite, SWT.LEFT);
        label.setText(CpStringsUI.CpPreferencePage_ProxyAddress);
        fAddressText = new Text(proxyComposite, SWT.SINGLE | SWT.BORDER);
        fAddressText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        fAddressText.setText(fAddress);
        fAddressText.setEnabled(fProxyMode != 0);

        label = new Label(proxyComposite, SWT.LEFT);
        label.setText(CpStringsUI.CpPreferencePage_ProxyPort);
        fPortText = new Text(proxyComposite, SWT.SINGLE | SWT.BORDER);
        fPortText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        fPortText.setText(fPort);
        fPortText.setEnabled(fProxyMode != 0);

        label = new Label(proxyComposite, SWT.LEFT);
        label.setText(CpStringsUI.CpPreferencePage_ProxyUsername);
        fUserText = new Text(proxyComposite, SWT.SINGLE | SWT.BORDER);
        fUserText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        fUserText.setText(fUser);
        fUserText.setEnabled(fProxyMode != 0);

        label = new Label(proxyComposite, SWT.LEFT);
        label.setText(CpStringsUI.CpPreferencePage_ProxyPassword);
        fPassText = new Text(proxyComposite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        fPassText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        fPassText.setText(fPass);
        fPassText.setEnabled(fProxyMode != 0);
	}

	void selectProxyMode(boolean noProxy, boolean httpProxy, boolean sockProxy) {
        fProxyMode = noProxy ? 0 : httpProxy ? 1 : sockProxy ? 2 : 0;
        fAddressText.setEnabled(!noProxy);
        fPortText.setEnabled(!noProxy);
        fUserText.setEnabled(!noProxy);
        fPassText.setEnabled(!noProxy);
    }

	protected static Button createRadioButton(Composite parent, String label) {
        Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
        button.setText(label);
        return button;
    }

	private boolean checkProxyData() {
		if(fProxyMode == 0) {
			return true;
		}
		if (!Utils.isValidURL(fAddressText.getText())) {
			MessageDialog.openError(getShell(), CpStringsUI.CpPreferencePage_WrongProxyUrlTitle,
					CpStringsUI.CpPreferencePage_WrongProxyUrlMessage);
			return false;
		}
		try {
			int port = Integer.parseInt(fPortText.getText());
			if (port < 0 || port > 65535) {
				MessageDialog.openError(getShell(), CpStringsUI.CpPreferencePage_WrongPortTitle,
						CpStringsUI.CpPreferencePage_WrongPortMessage);
				return false;
			}
		} catch (NumberFormatException e) {
			MessageDialog.openError(getShell(), CpStringsUI.CpPreferencePage_WrongPortTitle,
					CpStringsUI.CpPreferencePage_WrongPortMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void performDefaults() {

		super.performDefaults();

		fAutoUpdate = CpPreferenceInitializer.getAutoUpdateFlag();

		updateProxySettings();
	}

	protected void updateProxySettings() {
		loadProxyData();

		fNoProxyButton.setSelection(fProxyMode == 0);
		fHttpProxyButton.setSelection(fProxyMode == 1);
		fSockProxyButton.setSelection(fProxyMode == 2);

		fAddressText.setText(fAddress);
		fAddressText.setEnabled(fProxyMode != 0);

		fPortText.setText(fPort);
		fPortText.setEnabled(fProxyMode != 0);

		fUserText.setText(fUser);
		fUserText.setEnabled(fProxyMode != 0);

		fPassText.setText(fPass);
		fPassText.setEnabled(fProxyMode != 0);
	}

	@Override
	public boolean performOk() {
		if(!checkCmsisRoot()) {
			return false;
		}

		CpPreferenceInitializer.setAutoUpdateFlag(fAutoUpdate);

		if (!checkProxyData()) {
			return false;
		}
		saveProxyData();

		return super.performOk();
	}

	protected boolean checkCmsisRoot() {
		if(!CpPreferenceInitializer.isCmsisRootEditable())
			return true; // we can do nothing about it

		String cmsisRootDir = fCmsisRootEditor.getStringValue().trim();
		if(cmsisRootDir.isEmpty())
			return true; // valid value (though it is empty)
		File file = new File(cmsisRootDir);
		if(file.exists())
			return true;
		// try to create directory
		if(!file.mkdirs()) {
			String msg = CpStringsUI.PathErrorCreatingCmsisPackRootDirectory;
			msg += ":\n\n"; //$NON-NLS-1$
			msg += file.getAbsolutePath();
			MessageDialog.openError(getShell(), CpStringsUI.PathErrorCreatingCmsisPackRootDirectory,	msg);
			return false;
		}
		return true;
	}

	protected void loadProxyData() {
		IPreferenceStore store = getPreferenceStore();
		fProxyMode = store.getInt(CpPreferenceInitializer.PROXY_MODE);
		fAddress = store.getString(CpPreferenceInitializer.PROXY_ADDRESS);
		fPort = store.getString(CpPreferenceInitializer.PROXY_PORT);
		fUser = store.getString(CpPreferenceInitializer.PROXY_USER);
		fPass = decrypt(store.getString(CpPreferenceInitializer.PROXY_PASSWORD));
	}

	protected void saveProxyData() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(CpPreferenceInitializer.PROXY_MODE, fProxyMode);
		store.setValue(CpPreferenceInitializer.PROXY_ADDRESS, fAddressText.getText());
		store.setValue(CpPreferenceInitializer.PROXY_PORT, fPortText.getText());
		store.setValue(CpPreferenceInitializer.PROXY_USER, fUserText.getText());
		store.setValue(CpPreferenceInitializer.PROXY_PASSWORD, encrypt(fPassText.getText()));
	}

	protected String encrypt(String input) {
		return encryptor.encrypt(input);
	}

	protected String decrypt(String input) {
		return encryptor.decrypt(input);
	}

	@Override
	public Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
	}

}
