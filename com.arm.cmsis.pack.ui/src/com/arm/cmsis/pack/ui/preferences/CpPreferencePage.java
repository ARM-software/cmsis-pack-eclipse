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

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.preferences.CpPreferenceInitializer;
import com.arm.cmsis.pack.repository.CpRepositoryList;
import com.arm.cmsis.pack.repository.ICpRepository;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.utils.Utils;

public class CpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private Composite fComposite;

	private Table fTable;
	private TableColumn fColumnType;
	private TableColumn fColumnName;
	private TableColumn fColumnUrl;

	private Composite fButtonsComposite;

	private String[] fButtonsNames = {
			CpStringsUI.CpRepoPreferencePage_Add,
			CpStringsUI.CpRepoPreferencePage_Edit,
			CpStringsUI.CpRepoPreferencePage_Delete };

	private Button[] fButtons; // right side buttons

	private List<ICpRepository> fContentList;

	private CpRepositoryList fRepos;

	public CpPreferencePage() {
		fContentList = null;
		fButtons = null;

		fRepos = CpPlugIn.getPackManager().getCpRepositoryList();
	}


	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(CpPlugInUI.getDefault().getCorePreferenceStore());
	}


	@Override
	protected void createFieldEditors() {

		Composite parent = getFieldEditorParent();
		StringFieldEditor cmsisRootEditor;
		if(CpPreferenceInitializer.hasCmsisRootProvider()) {
			cmsisRootEditor = new StringFieldEditor(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
			cmsisRootEditor.setEnabled(false, parent);
			cmsisRootEditor.getLabelControl(parent).setEnabled(true);
		} else {
			cmsisRootEditor = new DirectoryFieldEditor(CpPlugIn.CMSIS_PACK_ROOT_PREFERENCE, CpStringsUI.PreferencesPackRootLabel, parent);
		}
		addField(cmsisRootEditor);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    Label description = new Label(parent, SWT.NONE);
	    description.setText(CpStringsUI.CpRepoPreferencePage_AddLinksToSites);
		createCpRepoContents(parent);
		return control;
	}

	protected Control createCpRepoContents(Composite parent) {
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

		return fComposite;
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

		fButtonsComposite = new Composite(comp, SWT.NULL);

		if (names == null || names.length == 0) {
			return;
		}

		GridData layoutData = new GridData();
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.RIGHT;
		fButtonsComposite.setLayoutData(layoutData);

		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginLeft = 5;
		layout.marginRight = 5;
		layout.marginTop = 0;

		fButtonsComposite.setLayout(layout);

		fButtons = new Button[names.length];
		for (int i = 0; i < names.length; i++) {

			fButtons[i] = new Button(fButtonsComposite, SWT.PUSH);

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
			handleDelButton();
			break;
		}
		updateTableContent();
	}

	private void handleAddButton() {

		NewRepoDialog dlg = new NewRepoDialog(fComposite.getShell(), null);
		if (dlg.open() == Window.OK) {
			String[] data = dlg.getData();
			if (checkData(data)) {
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
			if (checkData(data)) {
				fContentList.set(index, fRepos.convertToCpRepository(data));
			}
		}
	}

	private void handleDelButton() {

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
	private boolean checkData(String[] data) {
		String url = data[data.length-1];
		if (!Utils.isValidURL(url)) {
			MessageDialog.openError(getShell(), CpStringsUI.CpPreferencePage_WrongUrlTitle,
					CpStringsUI.CpPreferencePage_WrongUrlMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void performDefaults() {

		super.performDefaults();

		fContentList = fRepos.getDefaultList();
		updateTableContent();
	}

	@Override
	public boolean performOk() {
		fRepos.putList(fContentList);
		return super.performOk();
	}

	@Override
	public Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
	}
}
