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

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.ui.widgets.RteWidget;

/**
 * Base abstract class for RTE configuration editor pages
 */
public abstract class RteEditorPage extends RteWidget implements IRteEventListener{

	protected RteEditorPageHeader headerWidget = null;
    protected boolean bModified = false;
	private IAction saveAction = null;

	/**
	 * Creates page content
	 * @param parent parent composite for content
	 */
	public abstract void createPageContent(Composite parent);
	
	/**
	 * Check if the page has been modified from last saved state
	 * @return true if modified
	 */
	public boolean isModified() {
		return bModified;
	}
		
	/**
	 *  Creates action elements in the header and sets the header label 
	 */
	protected void setupHeader() {
		saveAction = headerWidget.addSaveAction();
	}
	
	@Override
	public Composite createControl(Composite parent) {
		Composite pageComposite = new Composite(parent, SWT.NONE);
    	GridLayout gridLayout = new GridLayout();
    	gridLayout.numColumns = 1;
    	gridLayout.marginHeight = 0;
    	gridLayout.marginTop = 0;
    	pageComposite.setLayout(gridLayout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    	pageComposite.setLayoutData(gd);

    	headerWidget = new RteEditorPageHeader(pageComposite, SWT.NONE);
    	headerWidget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    	createPageContent(pageComposite);
    	setupHeader();
    	update();
    	return pageComposite;
	}

	
	
	@Override
	public void handle(RteEvent event) {
		updateSaveAction(); // update on every event
	}

	@Override
	public void update() {
		updateSaveAction();
	}
	
	private void updateSaveAction(){
		if(saveAction != null && getModelController() != null){
			saveAction.setEnabled(getModelController().isModified());
		}
	}
}
