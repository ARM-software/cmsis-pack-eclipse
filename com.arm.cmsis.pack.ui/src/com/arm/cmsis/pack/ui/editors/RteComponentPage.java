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

package com.arm.cmsis.pack.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IHelpContextIds;
import com.arm.cmsis.pack.ui.widgets.RteComponentManagerWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RteComponentPage extends RteEditorPage {

	protected RteComponentManagerWidget rteManagerWidget;
	IAction resolveAction = null;

	public RteComponentPage() {
		rteManagerWidget = new RteComponentManagerWidget();
	}

	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		rteManagerWidget.setModelController(model);
		update();
	}

	@Override
	public Composite getFocusWidget() {
		return rteManagerWidget.getFocusWidget();
	}

	@Override
	public void createPageContent(Composite parent) {
		rteManagerWidget.createControl(parent);
		headerWidget.setFocusWidget(getFocusWidget());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getFocusWidget(), IHelpContextIds.COMPONENT_PAGE);
	}

	@Override
	protected void setupHeader() {
		headerWidget.setLabel(CpStringsUI.RteManagerWidget_Components, CpPlugInUI.getImage(CpPlugInUI.ICON_RTE));

		resolveAction = new Action(CpStringsUI.RteComponentTreeWidget_Resolve, IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				IRteModelController model = getModelController();
				if (model != null) {
					model.resolveComponentDependencies();
				}
			}
		};
		resolveAction.setToolTipText(CpStringsUI.RteComponentTreeWidget_ResolveComponentDependencies);
		resolveAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RESOLVE_CHECK_WARN));
		resolveAction.setDisabledImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RESOLVE_CHECK_GREY));
		headerWidget.addAction(resolveAction, SWT.LEFT, true);

		super.setupHeader();
	}

	@Override
	public void handle(RteEvent event) {
		switch (event.getTopic()) {
		case RteEvent.COMPONENT_SELECTION_MODIFIED:
			update();
			return;
		default:
			super.handle(event);
		}
	}

	@Override
	public void update() {
		if (headerWidget != null && getModelController() != null) {
			bModified = getModelController().isComponentSelectionModified();
			headerWidget.setModified(bModified);
			EEvaluationResult res = getModelController().getEvaluationResult();
			resolveAction.setEnabled(res == EEvaluationResult.SELECTABLE);
		}
		super.update();
	}

	@Override
	public void refresh() {
	}
}
