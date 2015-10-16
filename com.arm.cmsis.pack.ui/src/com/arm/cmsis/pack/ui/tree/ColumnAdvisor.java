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

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.arm.cmsis.pack.ui.OpenURL;


/**
 *Default implementation of IColumnAdvisor interface   
 */
public abstract class ColumnAdvisor implements IColumnAdvisor {

	protected ColumnViewer columnViewer;
	private Control control;
	private static final Cursor CURSOR_HAND = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND); 	// hand cursor for URL text

	/**
	 * Constructs advisor for a viewer
	 * @param columnViewer ColumnViewer on which the advisor is installed
	 */
	public ColumnAdvisor(ColumnViewer columnViewer) {
		this.columnViewer = columnViewer;
		this.control = columnViewer.getControl();
		this.control.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				handleMouseOver(e);
			}
			public void mouseExit(MouseEvent e) {
				handleMouseExit(e);
			}
		});

		this.control.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				handleMouseOver(e);
			}
		});
		this.control.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				handleMouseUp(e);
			}
		});
		
	}

	/**
	 * Handling mouse over event to display hand cursor in case cell contains an
	 * URL
	 * 
	 * @param e mouse event
	 */
	void handleMouseOver(MouseEvent e) {
		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);
		boolean cursorSet = false;

		if (cell != null) {
			int colIndex = cell.getColumnIndex();
			Object element = cell.getElement();
			if (getCellControlType(element, colIndex) == CellControlType.URL) {
				Rectangle cellBounds = cell.getBounds();
				Image img = getImage(element, colIndex);
				if(img != null) {
					cellBounds.x+=img.getBounds().width;
				}
				if(cellBounds.contains(pt)) {
					if (getString(element, colIndex) != null) {
						this.control.setCursor(CURSOR_HAND);
						cursorSet = true;
					}
				}
			}
		}

		if (!cursorSet) {
			handleMouseExit(e);
		}
	}
	/**
	 * Resets cursor 'hand'
	 * @param e mouse event
	 */
	void handleMouseExit(MouseEvent e) {
		if (this.control.getCursor() == CURSOR_HAND) {
			this.control.setCursor(null);
		}
	}
	
	/**
	 * Opens URL for an URL control
	 * @param e mouse event
	 */
	void handleMouseUp(MouseEvent e) {
		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);
		if (cell != null && this.control.getCursor() == CURSOR_HAND) {
			int colIndex = cell.getColumnIndex();
			Object element = cell.getElement();
			if (getCellControlType(element, colIndex) == CellControlType.URL) {
				String url = getUrl(element, colIndex);
				if (url != null && !url.isEmpty()) {
					OpenURL.open(url, this.control.getShell());
				}
			}
		}
	}
	
	@Override
	public ColumnViewer getViewer() {
		return columnViewer;
	}

	@Override
	public CellControlType getCellControlType(Object obj, int columnIndex) {
		return CellControlType.TEXT;
	}

	@Override
	public boolean getCheck(Object obj, int columnIndex) {
		return false;
	}

//	@Override
//	public String getString(Object obj, int columnIndex) {
//		return null;
//	}

	
	
	@Override
	public String getUrl(Object obj, int columnIndex) {
		return null;
	}
	
	@Override
	public boolean isEnabled(Object obj, int columnIndex) {
		return true;
	}

	@Override
	public void setString(Object obj, int columnIndex, String newVal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentSelectedIndex(Object obj, int columnIndex, int newVal) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCurrentSelectedIndex(Object element, int columnIndex) {
		return -1;
	}

	@Override
	public int getMaxCount(Object obj, int columnIndex) {
		return 0;
	}

	@Override
	public String[] getStringArray(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public String getDefaultString(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public boolean isDefault(Object obj, int columnIndex) {
		return false;
	}

	@Override
	public boolean canEdit(Object obj, int columnIndex) {
		return false;
	}

	@Override
	public Image getImage(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public Color getBgColor(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public String getTooltipText(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public void setCheck(Object element, int columnIndex, boolean newVal) {
	}

	@Override
	public Menu getMenu( Object obj, int columnIndex) {
		String[] strings = getStringArray(obj, columnIndex);  
		String selectedString = getString(obj, columnIndex);
		String defaultString = getDefaultString(obj, columnIndex);
		boolean bDefault = defaultString !=null && isDefault(obj, columnIndex);
		
		return createMenu(strings, selectedString, defaultString, bDefault);
	}

	/**
	 * Creates menu 
	 * @param parent parent control
	 * @param strings collection of strings 
	 * @param selectedString selected item
	 * @param defaultString default item if any or null
	 * @return Menu
	 */
	protected Menu createMenu(String[] strings, String selectedString, String defaultString, boolean bDefault) {
		if(strings == null || strings.length == 0)
			return null;

		Menu menu = new Menu(this.control);
		// insert default value first
		if(defaultString != null){
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(defaultString);
			menuItem.setSelection(bDefault);
			menuItem = new MenuItem(menu, SWT.SEPARATOR);
		}
		
		for (String s : strings) {
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(s);
			menuItem.setSelection(!bDefault && s.equals(selectedString));
		}
		return menu;
	}
	
}
