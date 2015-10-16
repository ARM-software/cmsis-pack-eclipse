package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;

/**
 * A cell editor that presents a drop down menu. The cell editor's
 * value is the selected menu item.
 */
public class MenuCellEditor extends CellEditor {

	Menu menu = null; 
	Button button;
	String selectedItem;
	ViewerCell viewerCell;
	Composite parent;
	
	public MenuCellEditor(Composite parent) {
		super(parent);
		this.parent = parent;
	}

	@Override
	protected Control createControl(Composite parent) {
		
		button = new Button(parent, SWT.PUSH|SWT.LEFT);
		return button;
	}

	@Override
	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		super.activate(activationEvent);
		viewerCell = (ViewerCell)activationEvent.getSource();
		button.setText(viewerCell.getText());
		button.setImage(viewerCell.getImage());
		showMenu();
	}

	@Override
	protected Object doGetValue() {
		return selectedItem;
	}

	/**
	 * This is the menu item selection listener used to apply the selected menu item.
	 * 
	 */
	class MenuItemSelListener implements SelectionListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			selectedItem = ((MenuItem)e.getSource()).getText();
			boolean selected = ((MenuItem)e.getSource()).getSelection();
			if (!selected) {
				for (int i = 0; i < menu.getItemCount(); ++i) {
					if (menu.getItem(i).getSelection()) {
						selectedItem = menu.getItem(i).getText();
						break;
					}
				}
			}
			applyEditorValueAndDeactivate();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		
	}
	
	@Override
	protected void doSetValue(Object value) {
		if (!(value instanceof Menu)) 
			return;
		
		this.menu = (Menu)value; 
 
		// Set menu item selection listener
		for (int i = 0; i < menu.getItemCount(); ++i) {
			menu.getItem(i).addSelectionListener(new MenuItemSelListener());
		}
		
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
			}
			@Override
			public void menuHidden(MenuEvent e) {
				deactivate();
			}
		});
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		menu.dispose();
		button.dispose();
		fireApplyEditorValue();
		deactivate();
	}
	
	/**
	 * Show the drop down menu just under the selected cell.
	 */
	private void showMenu() {
		Tree tree = (Tree)parent;;
		if (viewerCell != null) {
			viewerCell.getBounds();
			Rectangle rc = viewerCell.getBounds();
			Rectangle rc2 = Geometry.toDisplay(tree, rc);		// convert to screen coordinates
			menu.setLocation(rc2.x, rc2.y + rc2.height);		// set menu position
			menu.setVisible(true);
			button.setVisible(false);
		}
	}

	@Override
	protected void doSetFocus() {
		// TODO Auto-generated method stub
	}
}
