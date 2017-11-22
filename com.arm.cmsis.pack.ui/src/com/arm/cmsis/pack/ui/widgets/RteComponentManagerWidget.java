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
package com.arm.cmsis.pack.ui.widgets;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.rte.IRteModelController;

/**
 * This class implements functionality of component selector page  
 */
public class RteComponentManagerWidget extends RteModelWidget {
	private SashForm sashForm = null;

	RteComponentSelectorWidget 	rteComponentTreeWidget = null;
	RteValidateWidget 		rteValidateWidget = null;

	public RteComponentManagerWidget() {
		super();
		rteComponentTreeWidget = new RteComponentSelectorWidget();
		rteValidateWidget = new RteValidateWidget();
	}

	public SashForm getSashForm() {
		return sashForm;
	}

	@Override
	public Composite getFocusWidget() {
		TreeViewer viewer = rteComponentTreeWidget.getViewer();
		return viewer.getTree();
	}

	@Override
	public void setModelController(IRteModelController model) {
		super.setModelController(model);
		rteComponentTreeWidget.setModelController(model);
		rteValidateWidget.setModelController(model);
		update();
	}

	@Override
	public Composite createControl(Composite parent) {
		sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashForm.setSashWidth(3);
		rteComponentTreeWidget.createControl(sashForm);
		rteValidateWidget.createControl(sashForm);
		sashForm.setWeights(new int[] {3,1});
		getFocusWidget().setFocus();
		
		return sashForm;
	}

	@Override
	public void handle(RteEvent event) {
	}

	@Override
	public void refresh() {
	}

	@Override
	public void update() {
		if (sashForm != null) {
			rteComponentTreeWidget.update();
			rteValidateWidget.update();
		}
	}
}
