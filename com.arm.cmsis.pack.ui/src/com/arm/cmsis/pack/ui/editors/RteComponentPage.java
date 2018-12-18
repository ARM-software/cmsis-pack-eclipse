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
import org.eclipse.swt.graphics.Image;

import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.IHelpContextIds;
import com.arm.cmsis.pack.ui.widgets.RteComponentManagerWidget;
import com.arm.cmsis.pack.ui.widgets.RteWidget;

/**
 * Editor page that wraps RteManagerWidget
 *
 */
public class RteComponentPage extends RteModelEditorPage {

	IAction resolveAction = null;

	public RteComponentPage() {
	}


	@Override
	protected void createActions() {
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

		super.createActions();
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
	public void updateActions() {
		if(getModelController() != null) {
			EEvaluationResult res = getModelController().getEvaluationResult();
			resolveAction.setEnabled(res == EEvaluationResult.SELECTABLE);
		}
		super.updateActions();
	}

	@Override
	protected RteWidget<IRteModelController> createContentWidget() {
		return  new RteComponentManagerWidget();
	}

	@Override
	protected String getHelpID() {
		return IHelpContextIds.COMPONENT_PAGE;
	}

	@Override
	protected Image getImage() {
		return CpPlugInUI.getImage(CpPlugInUI.ICON_RTE);
	}

	@Override
	protected String getLabel() {
		return CpStringsUI.RteManagerWidget_Components;
	}
	
	@Override
	public boolean isModified() {
		if(getModelController() != null)
			return getModelController().isComponentSelectionModified();
		return false;
	}

}
