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
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.repository.ICpRepository;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.utils.Encryptor;
import com.arm.cmsis.pack.utils.Utils;

public class CpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Composite fComposite;
	private StringFieldEditor fCmsisRootEditor;

	private Table fTable;
	private TableColumn fColumnType;
	private TableColumn fColumnName;
	private TableColumn fColumnUrl;

	private String[] fButtonsNames = {
			CpStringsUI.CpRepoPreferencePage_Add,
			CpStringsUI.CpRepoPreferencePage_Edit,
			CpStringsUI.CpRepoPreferencePage_Delete };

	private Button[] fButtons; // right side buttons

	private List<ICpRepository> fContentList;

	private CpRepositoryList fRepos;

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
		super();
		fContentList = null;
		fButtons = null;

		fRepos = CpPlugIn.getPackManager().getCpRepositoryList();

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
			fCmsisRootEditor = new StringFieldEditor(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
			fCmsisRootEditor.setEnabled(false, parent);
			fCmsisRootEditor.getLabelControl(parent).setEnabled(true);
		} else {
			fCmsisRootEditor = new CpDirectoryFieldEditor(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
		}
		addField(fCmsisRootEditor);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);

		// Repository Settings
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    Label description = new Label(parent, SWT.NONE);
	    description.setText(CpStringsUI.CpRepoPreferencePage_AddLinksToSites);
		createCpRepoContents(parent);

		// Proxy Settings
	    createProxyContents(parent);

		return control;
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

	protected void createCpRepoContents(Composite parent) {
		fComposite = new Composite(parent, SWT.NULL);
		fComposite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 5;
		fComposite.setLayout(layout);

		GridData layoutData;

		layoutData = new GridData();
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.heightHint = 400;
		fComposite.setLayoutData(layoutData);

		// Column 1: table
		{
			initTable(fComposite);

			fContentList = fRepos.getList();

			updateTableContent();
		}

		// Column 2: buttons
		{
			initButtons(fComposite, fButtonsNames);
		}

		// Row 2: check box of "Check for Update"
		{
			Button checkbox = new Button(fComposite, SWT.CHECK);
			checkbox.setText(CpStringsUI.CpPreferencePage_CheckForUpdatesEveryday);
			checkbox.setSelection(fAutoUpdate);
			checkbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fAutoUpdate = !fAutoUpdate;
				}
			});
		}
	}

	protected void initTable(Composite comp) {

		fTable = new Table(comp, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		GridData layoutData = new GridData();
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.grabExcessVerticalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		fTable.setLayoutData(layoutData);

		fColumnType = new TableColumn(fTable, SWT.NULL);
		fColumnType.setText(CpStringsUI.CpRepoPreferencePage_TypeColumnText);
		fColumnType.setWidth(100);
		fColumnType.setResizable(true);

		fColumnName = new TableColumn(fTable, SWT.NULL);
		fColumnName.setText(CpStringsUI.CpRepoPreferencePage_NameColumnText);
		fColumnName.setWidth(100);
		fColumnName.setResizable(true);

		fColumnUrl = new TableColumn(fTable, SWT.NULL);
		fColumnUrl.setText(CpStringsUI.CpRepoPreferencePage_UrlColumnText);
		fColumnUrl.setWidth(350);
		fColumnUrl.setResizable(true);
	}

	protected void updateTableContent() {

		fTable.removeAll();

		if (fContentList != null) {
			TableItem item;
			for (ICpRepository repo : fContentList) {
				item = new TableItem(fTable, SWT.NULL);
				item.setText(fRepos.convertToArray(repo));
			}
		}
	}

	protected void initButtons(Composite comp, String[] names) {

		Composite buttonsComposite = new Composite(comp, SWT.NULL);

		if (names == null || names.length == 0) {
			return;
		}

		GridData layoutData = new GridData();
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.RIGHT;
		buttonsComposite.setLayoutData(layoutData);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.marginTop = 0;

		buttonsComposite.setLayout(layout);

		fButtons = new Button[names.length];
		for (int i = 0; i < names.length; i++) {

			fButtons[i] = new Button(buttonsComposite, SWT.PUSH);

			layoutData = new GridData(GridData.FILL_HORIZONTAL);
			layoutData.verticalAlignment = SWT.CENTER;
			layoutData.grabExcessHorizontalSpace = false;
			layoutData.horizontalAlignment = SWT.FILL;

			if (names[i] != null) {
				fButtons[i].setText(names[i]);
			} else { // no button, but placeholder !
				fButtons[i].setVisible(false);
				fButtons[i].setEnabled(false);
				layoutData.heightHint = 10;
			}

			fButtons[i].setLayoutData(layoutData);

			fButtons[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					buttonPressed(event);
				}
			});
		}
	}

	void buttonPressed(SelectionEvent e) {

		for (int i = 0; i < fButtons.length; i++) {
			if (fButtons[i].equals(e.widget)) {
				buttonPressed(i);
				return;
			}
		}
	}

	public void buttonPressed(int index) {

		switch (index) {
		case 0:
			handleAddButton();
			break;
		case 1:
			handleEditButton();
			break;
		case 2:
			handleDeleteButton();
			break;
		}
		updateTableContent();
	}

	private void handleAddButton() {

		NewRepoDialog dlg = new NewRepoDialog(fComposite.getShell(), null);
		if (dlg.open() == Window.OK) {
			String[] data = dlg.getData();
			if (checkRepositoryData(data)) {
				fContentList.add(fRepos.convertToCpRepository(data));
			}
		}
	}

	private void handleEditButton() {

		int index = fTable.getSelectionIndex();
		if (index == -1) {
			return; // nothing selected
		}

		NewRepoDialog dlg = new NewRepoDialog(fComposite.getShell(),
				fRepos.convertToArray(fContentList.get(index)));
		if (dlg.open() == Window.OK) {
			String[] data = dlg.getData();
			if (checkRepositoryData(data)) {
				fContentList.set(index, fRepos.convertToCpRepository(data));
			}
		}
	}

	private void handleDeleteButton() {

		int index = fTable.getSelectionIndex();
		if (index == -1) {
			return; // nothing selected
		}

		fContentList.remove(index);
	}

	/**
	 * Check the data entered by the user
	 * @param data the data
	 * @return true if the data is valid, otherwise false
	 */
	private boolean checkRepositoryData(String[] data) {
		String url = data[data.length-1];
		if (!Utils.isValidURL(url)) {
			MessageDialog.openError(getShell(), CpStringsUI.CpPreferencePage_WrongUrlTitle,
					CpStringsUI.CpPreferencePage_WrongUrlMessage);
			return false;
		}
		return true;
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

		
		fContentList = fRepos.getDefaultList();
		updateTableContent();

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
		
		fRepos.putList(fContentList);
		
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
		fProxyMode = store.getInt(CpPlugIn.PROXY_MODE);
		fAddress = store.getString(CpPlugIn.PROXY_ADDRESS);
		fPort = store.getString(CpPlugIn.PROXY_PORT);
		fUser = store.getString(CpPlugIn.PROXY_USER);
		fPass = decrypt(store.getString(CpPlugIn.PROXY_PASSWORD));
	}

	protected void saveProxyData() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(CpPlugIn.PROXY_MODE, fProxyMode);
		store.setValue(CpPlugIn.PROXY_ADDRESS, fAddressText.getText());
		store.setValue(CpPlugIn.PROXY_PORT, fPortText.getText());
		store.setValue(CpPlugIn.PROXY_USER, fUserText.getText());
		store.setValue(CpPlugIn.PROXY_PASSWORD, encrypt(fPassText.getText()));
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
