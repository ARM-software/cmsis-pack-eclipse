/*******************************************************************************
 * Copyright (c) 2015-2018 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.project.template;

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.build.settings.RteToolChainAdapterFactory;
import com.arm.cmsis.pack.build.settings.RteToolChainAdapterInfo;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.project.IHelpContextIds;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.CDTUtils;
import com.arm.cmsis.pack.ui.IStatusMessageListener;

/**
 * Page to calculate and select required toolchain adapter 
 */
public class RteTemplateCmsisProjectPage extends AbstractWizardDataPage implements IStatusMessageListener {

	private Label lblProjectType;
	private Label lblOutput;
	private Button chkMain;

	private RteToolChainAdapterSelector adapterSelector = null;

	protected boolean updatingControls;
	protected boolean initialized = false;
	protected boolean hideCreateMain = false;

	protected String Tcompiler = CmsisConstants.EMPTY_STRING;
	protected String Toutput = CmsisConstants.EMPTY_STRING;

	protected String adapterId = CmsisConstants.EMPTY_STRING;

	/**
	 * @param pageName
	 */
	public RteTemplateCmsisProjectPage(String pageName) {
		super(pageName);
	}

	/**
	 * @param pageName
	 * @param title
	 * @param imageDescriptor
	 */
	public RteTemplateCmsisProjectPage(String pageName, String title, ImageDescriptor imageDescriptor, boolean hideCreateMain) {
		super(pageName, title, imageDescriptor);
		this.hideCreateMain = hideCreateMain;
		initData();
	}

	private void initData() {
		IToolChain tc = RteProjectTemplate.getSelectedToolChain();
		if(tc == null)
			return;
		IConfiguration cfg = tc.getParent();
		if (cfg != null) {
			IProjectType pt = cfg.getProjectType();
			if (pt != null) {
				IBuildPropertyValue artefact = pt.getBuildArtefactType();
				if (artefact != null) {
					String aId = artefact.getId();
					if (CDTUtils.BUILD_ARTEFACT_TYPE_LIB.equals(aId)) {
						Toutput = CmsisConstants.TOUTPUT_LIB;
					} else if (CDTUtils.BUILD_ARTEFACT_TYPE_EXE.equals(aId)) {
						Toutput = CmsisConstants.TOUTPUT_EXE;
					}
				}
			}
		}
		
		RteToolChainAdapterFactory factory = RteToolChainAdapterFactory.getInstance();
		Collection<RteToolChainAdapterInfo> adapters = factory.getAdapterInfos(tc);
		if(adapters.size() != 1 )
			return; // needs user interaction

		RteToolChainAdapterInfo info = adapters.iterator().next();
		Tcompiler = info.getFamily();
		adapterId = info.getId();
	}

	
	@Override
	public Map<String, String> getPageData() {

		Attributes a = new Attributes();
		a.setAttribute(CmsisConstants.TCOMPILER, Tcompiler);
		a.setAttribute(CmsisConstants.TOUTPUT, Toutput);
		a.setAttribute("Tadapter", adapterId); //$NON-NLS-1$

		String ext = getArtifactExtension();
		a.setAttribute("Textension", ext); //$NON-NLS-1$

		boolean createMain = false;
		if (initialized && chkMain != null) {
			createMain = chkMain.getSelection();
		}
		a.setAttribute("Tmain", createMain); //$NON-NLS-1$

		a.setAttribute("Tmain", createMain); //$NON-NLS-1$
		// FIXME: move this to another place
		a.setAttribute("Tlast", !(getWizard() instanceof IImportWizard)); //$NON-NLS-1$
		return a.getAttributesAsMap();
	}

	protected String getArtifactExtension() {
		String ext = CmsisConstants.EMPTY_STRING;
		if (Toutput.equals(CmsisConstants.TOUTPUT_EXE)) {
			if (CmsisConstants.ARMCC.equals(Tcompiler)) {
				ext = "axf"; //$NON-NLS-1$
			} else {
				ext = "elf"; //$NON-NLS-1$
			}
		} else {
			if (CmsisConstants.ARMCC.equals(Tcompiler)) {
				ext = "lib"; //$NON-NLS-1$
			} else {
				ext = "a"; //$NON-NLS-1$
			}
		}
		return ext;
	}
	
	@Override
	public void createControl(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 8;
		mainComposite.setLayout(gridLayout);

		Label lblProjectTypeLabel = new Label(mainComposite, SWT.NONE);
		lblProjectTypeLabel.setText(Messages.RteTemplateCmsisProjectPage_SelectedProjectType);

		lblProjectType = new Label(mainComposite, SWT.NONE);
		lblProjectType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblOutputLabel = new Label(mainComposite, SWT.NONE);
		lblOutputLabel.setText(Messages.RteTemplateCmsisProjectPage_Output);

		lblOutput = new Label(mainComposite, SWT.NONE);
		lblOutput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		adapterSelector = new RteToolChainAdapterSelector(mainComposite, SWT.NONE);
		adapterSelector.addListener(this);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		adapterSelector.setLayoutData(gd);

		if (!hideCreateMain) {
			chkMain = new Button(mainComposite, SWT.CHECK);
			chkMain.setText(Messages.RteTemplateCmsisProjectPage_CreateDefaultMain);

			chkMain.setSelection(false);
		}

		setControl(mainComposite);
		initialized = true;
		update();

		// add context-sensitive help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.CMSIS_PROJECT_WIZARD);

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			update();
		}
	}

	protected void update() {
		if (!initialized) {
			return;
		}
		IToolChain tc = RteProjectTemplate.getSelectedToolChain();
		adapterSelector.setToolChain(tc);
		String projectType = CmsisConstants.EMPTY_STRING;
		if (tc != null) {
			IConfiguration cfg = tc.getParent();
			if (cfg != null) {
				IProjectType pt = cfg.getProjectType();
				if (pt != null) {
					IBuildPropertyValue artefact = pt.getBuildArtefactType();
					if (artefact != null) {
						projectType = artefact.getName();
					}
				}
			}
		} else {
			setErrorMessage(Messages.RteTemplateCmsisProjectPage_NoTollchainAvailable);
		}
		lblProjectType.setText(projectType);
		lblOutput.setText(getArtifactExtension());
	}

	@Override
	public void handle(String message) {
		updateStatus(message);
	}

	
	public void updateStatus(String message) {
		setErrorMessage(message);
		if (adapterSelector != null) {
			Tcompiler = adapterSelector.getTcompiler();
			adapterId = adapterSelector.getAdapterId();
		}

		boolean complete = Tcompiler != null && !Tcompiler.isEmpty() && adapterId != null && !adapterId.isEmpty();
		setPageComplete(complete); // TODO : implement
	}

	@Override
	public boolean isPageComplete() {
		boolean complete = Tcompiler != null && !Tcompiler.isEmpty() && adapterId != null && !adapterId.isEmpty();
		return complete;
	}
	
	

}
