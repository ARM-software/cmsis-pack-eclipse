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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 * Widget to be placed on the top of editor's page<br>
 * Contains label with image and text as well as three toolbars : left, center
 * and right
 *
 */
public class RteEditorPageHeader extends Composite {

	public static final String STAR = "*"; //$NON-NLS-1$
	private Label label;
	private Label imageLabel;
	private Map<Integer, ManagedToolBar> toolBars = new HashMap<Integer, ManagedToolBar>();
	Composite focusWidget;

	class ManagedToolBar {
		ToolBar toolBar;
		ToolBarManager toolBarManager;

		public ManagedToolBar(final Composite parent, int style) {
			toolBarManager = new ToolBarManager(style);
			toolBar = toolBarManager.createControl(parent);
			toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			toolBar.addListener(SWT.FOCUSED, new Listener() {
				@Override
				public void handleEvent(Event event) {
					parent.setFocus();
				}
			});
		}

		public void addAction(IAction action, boolean showText) {
			if (showText) {
				ActionContributionItem aci = new ActionContributionItem(action);
				aci.setMode(ActionContributionItem.MODE_FORCE_TEXT);
				toolBarManager.add(aci);
			} else {
				toolBarManager.add(action);
			}
			toolBarManager.update(true);
		}

		public void dispose() {
			toolBarManager.removeAll();
			toolBarManager.dispose();
			toolBarManager = null;
			toolBar = null;
		}
	}

	public RteEditorPageHeader(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 1;
		gridLayout.marginTop = 1;
		setLayout(gridLayout);

		imageLabel = new Label(this, SWT.LEFT);

		label = new Label(this, SWT.LEFT);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD).increaseHeight(1);
		Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;

		// add toolbars
		ManagedToolBar toolbar = new ManagedToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBars.put(SWT.LEFT, toolbar);
		toolbar = new ManagedToolBar(this, SWT.FLAT | SWT.RIGHT_TO_LEFT);
		toolBars.put(SWT.RIGHT, toolbar);

	}

	@Override
	public void dispose() {
		for (ManagedToolBar t : toolBars.values()) {
			t.dispose();
		}
		super.dispose();
	}

	public void setLabel(String text, Image image) {
		label.setText(text);
		imageLabel.setImage(image);
	}
	public void setLabelText(String newText) {
		String text = label.getText();
		if(text == null && newText == null)
			return;
		if(newText != null && newText.equals(text))
			return;

		boolean bModified = text != null && text.endsWith(STAR);
		text = newText;
		if(bModified)
			text += STAR;	
		label.setText(text);
		layout();
	}
	
	public void setModified(boolean bModified) {
		if(isDisposed())
			return;
		String text = label.getText();
		if (text == null) {
			return;
		}
		if (text.endsWith(STAR) == bModified) {
			return;
		}
		if (bModified) {
			text += STAR;
		} else {
			text = text.substring(0, text.length() - 1);
		}
		label.setText(text);
		layout();
	}

	/**
	 * Returns ManagedToolBar for given position
	 * 
	 * @param position
	 *            toolbar position: SWT.LEFT or SWT.RIGHT
	 * @return ManagedToolBar for given position
	 */
	ManagedToolBar getToolBar(int position) {
		return toolBars.get(position);
	}

	/**
	 * Adds an action to specified toolbar
	 * 
	 * @param action
	 *            IAction to add
	 * @param position
	 *            toolbar position to add action : SWT.LEFT or SWT.RIGHT
	 */
	public void addAction(IAction action, int position) {
		addAction(action, position, false);
	}

	/**
	 * Adds an action to specified toolbar
	 * 
	 * @param action
	 *            IAction to add
	 * @param position
	 *            toolbar position to add action : SWT.LEFT or SWT.RIGHT
	 * @param showText
	 *            flag to show text and image
	 */
	public void addAction(IAction action, int position, boolean showText) {
		if (action == null) {
			return;
		}
		ManagedToolBar toolBar = getToolBar(position);
		if (toolBar != null) {
			toolBar.addAction(action, showText);
		}
	}

	/**
	 * Creates save action and adds it to the right toolbar
	 * 
	 * @return created IAction
	 */
	public IAction addSaveAction() {
		Action saveAction = new Action("Save", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				ICommandService commandService = CpPlugInUI.getCommandService();
				if (commandService == null) {
					return;
				}
				try {
					// Lookup save command with its ID
					Command command = commandService.getCommand("org.eclipse.ui.file.save"); //$NON-NLS-1$
					if (command != null && command.isEnabled()) {
						command.executeWithChecks(new ExecutionEvent());
					}
				} catch (Exception e) {
					// Replace with real-world exception handling
					e.printStackTrace();
				}
			}
		};
		saveAction.setToolTipText(CpStringsUI.RteManagerWidget_ApplyAndSave);
		saveAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		saveAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
		addAction(saveAction, SWT.RIGHT);
		return saveAction;
	}
	
	/**
	 * Creates help action and adds it to the right toolbar
	 * @param parent 
	 * 
	 * @return created IAction
	 */
	public IAction addHelpAction() {
		Action helpAction = new Action("Help", IAction.AS_PUSH_BUTTON) { //$NON-NLS-1$
			@Override
			public void run() {
				focusWidget.notifyListeners(SWT.Help, new Event());
			}
		};
		helpAction.setToolTipText(CpStringsUI.RteEditorPageHeader_ShowHelp); 
		helpAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));
		addAction(helpAction, SWT.RIGHT);
		return helpAction;
	}

	/**
	 * Sets widget which receives focus when a toolbar is clicked
	 * 
	 * @param focusWidget
	 *            widget to set focus
	 */
	public void setFocusWidget(Composite focusWidget) {
		this.focusWidget = focusWidget;
	}

	@Override
	public boolean setFocus() {
		if (focusWidget != null) {
			return focusWidget.setFocus();
		}
		return super.setFocus();
	}

}
