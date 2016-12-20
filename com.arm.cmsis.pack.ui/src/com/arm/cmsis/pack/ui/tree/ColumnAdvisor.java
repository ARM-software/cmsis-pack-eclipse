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

import org.eclipse.jface.util.Geometry;
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

import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.OpenURL;


/**
 *Default implementation of IColumnAdvisor interface
 */
public abstract class ColumnAdvisor implements IColumnAdvisor {

	protected ColumnViewer columnViewer;
	protected Control control;
	protected static final Cursor CURSOR_HAND = Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND); 	// hand cursor for URL text

	protected Object selectedItem = null;
	protected Object selectedUpSpinner = null;
	protected Object selectedDownSpinner = null;
	protected Object selectedRightAlignedButton = null;
	public static final Rectangle EMPTY_RECTANGLE = new Rectangle(0,0,0,0);

	/**
	 * Constructs advisor for a viewer
	 * @param columnViewer ColumnViewer on which the advisor is installed
	 */
	public ColumnAdvisor(ColumnViewer columnViewer) {
		if (columnViewer == null) {
			return;
		}
		this.columnViewer = columnViewer;
		this.control = columnViewer.getControl();
		this.control.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				handleMouseOver(e);
			}
			@Override
			public void mouseExit(MouseEvent e) {
				handleMouseExit(e);
			}
		});

		this.control.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				handleMouseOver(e);
			}
		});
		this.control.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				handleMouseDown(e);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				handleMouseUp(e);
			}
		});

	}



	/**
	 * Handling mouse over event to display hand cursor when necessary.
	 * Default usage is when cell contains an URL
	 *
	 * @param e mouse event
	 */
	protected void handleMouseOver(MouseEvent e) {

		Cursor cursorToSet = null;

		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);

		if (cell != null) {
			int colIndex = cell.getColumnIndex();
			Object element = cell.getElement();
			if (getCellControlType(element, colIndex) == CellControlType.BUTTON && isEnabled(element, colIndex)) {
				Rectangle cellBounds = cell.getBounds();
				Image img = getImage(element, colIndex);
				if (img != null) {
					cellBounds.x += img.getBounds().width;
				}
				if (cellBounds.contains(pt)) {
					cursorToSet = CURSOR_HAND;
				}
			} else if (getCellControlType(element, colIndex) == CellControlType.URL) {
				String url = getUrl(e.x, e.y);
				if (url != null && !url.isEmpty()) {
					cursorToSet = CURSOR_HAND;
				}
			}
		}

		if (cursorToSet == CURSOR_HAND) {
			if (this.control.getCursor() != CURSOR_HAND) {
				this.control.setCursor(CURSOR_HAND);
			}
		} else if (this.control.getCursor() == CURSOR_HAND) {
			this.control.setCursor(null);
		}

	}
	/**
	 * Resets cursor 'hand'
	 * @param e mouse event
	 */
	protected void handleMouseExit(MouseEvent e) {
		String url = getUrl(e.x, e.y);
		if (url == null || url.isEmpty()) {
			if(this.control.getCursor() == CURSOR_HAND){
				this.control.setCursor(null);
			}
		}
	}

	/**
	 * Handling mouse down event
	 * @param e
	 */
	protected void handleMouseDown(MouseEvent e) {
		if (e.button != 1) { // must be left key
			return;
		}
		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);
		if (cell == null) {
			return;
		}

		int colIndex = cell.getColumnIndex();
		Object element = cell.getElement();
		if (!isEnabled(element, colIndex)) {
			return;
		}

		Rectangle cellBounds = cell.getBounds();
		Rectangle buttonBounds = EMPTY_RECTANGLE;
		if (hasSuffixButton(element, colIndex)) {
			buttonBounds = getSuffixButtonBounds(cellBounds, element, colIndex);
			cellBounds.width -= buttonBounds.width;
		}
		switch (getCellControlType(element, colIndex)) {
		case BUTTON:
			if (cellBounds.contains(pt)) {
				setButtonPressed(element, colIndex, element);
			}
			break;
		case INPLACE_SPIN:
			inplaceSpinPressed(cellBounds, pt, element, colIndex);
			break;
		case INPLACE_CHECK:
			if(!suffixButtonPressed(buttonBounds, pt, element, colIndex)) {
				inplaceCheckboxPressed(cellBounds, pt, element, colIndex);
			}
			break;
		default:
			break;
		}
		this.control.redraw();
	}

	/**
	 * Handling mouse up event to display hand cursor when necessary.
	 * Default usage is to opens URL for an URL control
	 * @param e mouse event
	 */
	protected void handleMouseUp(MouseEvent e) {
		if (e.button != 1) {
			return;
		}
		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);
		if (cell == null) {
			return;
		}

		int colIndex = cell.getColumnIndex();
		Object element = cell.getElement();
		if (!isEnabled(element, colIndex)) {
			return;
		}

		Rectangle cellBounds = cell.getBounds();
		Rectangle buttonBounds = EMPTY_RECTANGLE;
		if (hasSuffixButton(element, colIndex)) {
			buttonBounds = getSuffixButtonBounds(cellBounds, element, colIndex);
			cellBounds.width -= buttonBounds.width;
		}
		switch (getCellControlType(element, colIndex)) {
		case INPLACE_SPIN:
			inplaceSpinClicked(cellBounds, pt, element, colIndex);
			break;
		case INPLACE_CHECK:
			suffixButtonClicked(buttonBounds, pt, element, colIndex);
			break;
		default: // default is to open the url
			String url = getUrl(e.x, e.y);
			if (url != null && !url.isEmpty()) {
				openUrl(url);
			}
			break;
		}
	}

	/**
	 * Action to take when in-place spinner is pressed (after mouse down event)
	 * @param cellBounds cell's bounds excluding suffix button if it exists
	 * @param pt the mouse's point
	 * @param element cell's element
	 * @param colIndex cell's column index
	 */
	protected void inplaceSpinPressed(Rectangle cellBounds, Point pt, Object element, int colIndex) {
		Rectangle upSpinnerBounds = getUpSpinnerBounds(cellBounds, element, colIndex);
		if (upSpinnerBounds.contains(pt)) {
			setUpSpinnerPressed(element, colIndex, element);
		} else {
			Rectangle downSpinnerBounds = getDownSpinnerBounds(cellBounds, element, colIndex);
			if (downSpinnerBounds.contains(pt)) {
				setDownSpinnerPressed(element, colIndex, element);
			}
		}
	}

	/**
	 * Action to take when in-place spinner is clicked (after mouse up event)
	 * @param cellBounds cell's bounds excluding suffix button if it exists
	 * @param pt the mouse's point
	 * @param element cell's element
	 * @param colIndex cell's column index
	 */
	protected void inplaceSpinClicked(Rectangle cellBounds, Point pt, Object element, int colIndex) {
		Rectangle upBound = getUpSpinnerBounds(cellBounds, element, colIndex);
		if (isUpSpinnerPressed(element, colIndex) && upBound.contains(pt)) {
			long newVal = getCurrentSelectedIndex(element, colIndex) + getSpinStep(element, colIndex);
			if (newVal <= getMaxCount(element, colIndex)) {
				setCurrentSelectedIndex(element, colIndex, newVal);
			}
		} else if (isDownSpinnerPressed(element, colIndex)) {
			Rectangle downBound = getDownSpinnerBounds(cellBounds, element, colIndex);
			if (downBound.contains(pt)) {
				long newVal = getCurrentSelectedIndex(element, colIndex) - getSpinStep(element, colIndex);
				if (newVal >= getMinCount(element, colIndex)) {
					setCurrentSelectedIndex(element, colIndex, newVal);
				}
			}
		}
		setUpSpinnerPressed(element, colIndex, null);
		setDownSpinnerPressed(element, colIndex, null);
		this.control.redraw();
	}

	/**
	 * Return true if the checkbox in the cell is pressed
	 * @param cellBounds cell's bounds excluding suffix button if it exists
	 * @param pt the mouse's point
	 * @param element cell's element
	 * @param colIndex cell's column index
	 * @return true if the checkbox in the cell is pressed
	 */
	protected boolean inplaceCheckboxPressed(Rectangle cellBounds, Point pt, Object element, int colIndex) {
		Image image = getCheckboxImage(element, colIndex);
		if (image == null) {
			return false;
		}
		int x = cellBounds.x;
		int y = cellBounds.y;
		int width = image.getBounds().width;
		int height = image.getBounds().height;
		Rectangle checkboxBound = new Rectangle(x, y, width, height);
		if (checkboxBound.contains(pt)) {
			boolean isChecked = getCheck(element, colIndex);
			setCheck(element, colIndex, !isChecked);
			return true;
		}
		return false;
	}

	/**
	 * Return true if the suffix button in the cell is pressed
	 * @param buttonBounds the suffix button's bounds that the click happens
	 * @param pt the mouse's point
	 * @param element cell's element
	 * @param colIndex cell's column index
	 * @return true if the suffix button in the cell is pressed
	 */
	protected boolean suffixButtonPressed(Rectangle buttonBounds, Point pt, Object element, int colIndex) {
		if (isSuffixButtonEnabled(element, colIndex) && buttonBounds.contains(pt)) {
			setSuffixButtonPressed(element, colIndex, element);
			return true;
		}
		return false;
	}

	/**
	 * Action to take when the suffix button is clicked (after mouse up event)
	 * @param buttonBounds the suffix button's bounds that the click happens
	 * @param pt the mouse's point
	 * @param element cell's element
	 * @param colIndex cell's column index
	 */
	protected void suffixButtonClicked(Rectangle buttonBounds, Point pt, Object element, int colIndex) {
		if (isSuffixButtonPressed(element, colIndex) && buttonBounds.contains(pt)) {
			Rectangle rect = Geometry.toDisplay(control, buttonBounds);	
			executeSuffixButtonAction(element, colIndex, new Point(rect.x, rect.y));
		}
		setSuffixButtonPressed(element, colIndex, null);
		this.control.redraw();
	}

	/**
	 * Executes an action associated with the suffix button
	 * @param element cell's element
	 * @param colIndex cell's column index
	 * @param pt point to show menu if needed (in display coordinates)  
	 */
	protected void executeSuffixButtonAction(Object element, int colIndex,  Point pt) {
		// default does nothing
	}

	public String getUrl(int x, int y){
		Point pt = new Point(x, y);
		ViewerCell cell = getViewer().getCell(pt);

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
					return getUrl(element, colIndex);
				}
			}
		}
		return null;
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


	@Override
	public String getUrl(Object obj, int columnIndex) {
		return null;
	}

	@Override
	public void openUrl(String url) {
		OpenURL.open(url, this.control != null ? this.control.getShell() : null);
	}

	@Override
	public boolean isEnabled(Object obj, int columnIndex) {
		return true;
	}

	@Override
	public void setString(Object obj, int columnIndex, String newVal) {
	}

	@Override
	public void setCurrentSelectedIndex(Object obj, int columnIndex, long newVal) {

	}

	@Override
	public long getCurrentSelectedIndex(Object element, int columnIndex) {
		return -1;
	}

	@Override
	public long getMaxCount(Object obj, int columnIndex) {
		return 0;
	}

	@Override
	public long getMinCount(Object obj, int columnIndex) {
		return 0;
	}

	@Override
	public long getSpinStep(Object obj, int columnIndex) {
		return 1;
	}

	@Override
	public int getItemBase(Object obj, int columnIndex) {
		return 10;
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
	public Image getSuffixButtonImage(Object obj, int columnIndex) {
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
	public void setButtonPressed(Object obj, int columnIndex, Object newVal) {
		if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
			selectedItem = newVal;
		}
	}

	@Override
	public boolean isButtonPressed(Object obj, int columnIndex) {
		if (getCellControlType(obj, columnIndex) == CellControlType.BUTTON) {
			return selectedItem == obj;
		}
		return false;
	}

	@Override
	public void setUpSpinnerPressed(Object obj, int columnIndex, Object newVal) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_SPIN) {
			selectedUpSpinner = newVal;
		}
	}

	@Override
	public boolean isUpSpinnerPressed(Object obj, int columnIndex) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_SPIN) {
			return selectedUpSpinner == obj;
		}
		return false;
	}

	@Override
	public void setDownSpinnerPressed(Object obj, int columnIndex, Object newVal) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_SPIN) {
			selectedDownSpinner = newVal;
		}
	}

	@Override
	public boolean isDownSpinnerPressed(Object obj, int columnIndex) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_SPIN) {
			return selectedDownSpinner == obj;
		}
		return false;
	}

	@Override
	public int getSpinnerWidth(Rectangle cellBounds, Object obj, int columnIndex) {
		int itWidth = getImageTextWidth(obj, columnIndex);
		return Math.max(0, Math.min(SPINNER_WIDTH, cellBounds.width - itWidth));
	}

	@Override
	public boolean hasSuffixButton(Object obj, int columnIndex) {
		return false;
	}

	@Override
	public void setSuffixButtonPressed(Object obj, int columnIndex, Object newVal) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_CHECK) {
			selectedRightAlignedButton = newVal;
		}
	}

	@Override
	public boolean isSuffixButtonPressed(Object obj, int columnIndex) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_CHECK) {
			return selectedRightAlignedButton == obj;
		}
		return false;
	}

	@Override
	public Rectangle getSuffixButtonBounds(Rectangle cellBounds, Object obj, int columnIndex) {
		if(!hasSuffixButton(obj, columnIndex)) {
			return EMPTY_RECTANGLE;
		}
		int width = Math.min(SUFFIX_BUTTON_WIDTH, cellBounds.width);
		int height = cellBounds.height;
		int x = cellBounds.x + cellBounds.width - width;
		int y = cellBounds.y;
		return new Rectangle(x, y, width, height);
	}

	@Override
	public boolean isSuffixButtonEnabled(Object obj, int columnIndex) {
		return isEnabled(obj, columnIndex) && getCheck(obj, columnIndex);
	}

	@Override
	public Menu getMenu(Object obj, int columnIndex) {
		String[] strings = getStringArray(obj, columnIndex);
		String selectedString = getString(obj, columnIndex);
		String defaultString = getDefaultString(obj, columnIndex);
		boolean bDefault = defaultString !=null && isDefault(obj, columnIndex);

		return createMenu(strings, selectedString, defaultString, bDefault);
	}

	@Override
	public boolean isEmpty(Object obj, int columnIndex) {
		return false;
	}

	@Override
	public Image getCheckboxImage(Object obj, int columnIndex) {
		if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_CHECK ||
				getCellControlType(obj, columnIndex) == CellControlType.CHECK) {
			boolean check = getCheck(obj, columnIndex);
			boolean enabled = canEdit(obj, columnIndex);
			Image image;
			if (enabled) {
				image = check ? CpPlugInUI.getImage(CpPlugInUI.ICON_CHECKED)
						: CpPlugInUI.getImage(CpPlugInUI.ICON_UNCHECKED);
			} else {
				image = check ? CpPlugInUI.getImage(CpPlugInUI.ICON_CHECKED_GREY)
						: CpPlugInUI.getImage(CpPlugInUI.ICON_UNCHECKED_GREY);
			}
			return image;
		}
		return null;
	}

	/**
	 * Creates menu
	 * @param strings collection of strings
	 * @return Menu
	 */
	protected Menu createMenu(String[] strings) {
		if(strings == null || strings.length == 0) {
			return null;
		}

		Menu menu = new Menu(this.control);
		for (String s : strings) {
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(s);
		}
		return menu;
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
		if(strings == null || strings.length == 0) {
			return null;
		}

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

	/**
	 * Get the bounds of the up spinner button in this cell
	 * @param cellBounds cell's bounds excluding suffix button if it exists
	 * @return The bounds of the up spinner button in this cell
	 */
	protected Rectangle getUpSpinnerBounds(Rectangle cellBounds, Object obj, int columnIndex) {
		int width = getSpinnerWidth(cellBounds, obj, columnIndex);
		int height = cellBounds.height / 2;
		int x = cellBounds.x + cellBounds.width - width;
		int y = cellBounds.y + 1;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Get the bounds of the down spinner button in this cell
	 * @param cellBounds cell's bounds excluding suffix button if it exists
	 * @return The bounds of the down spinner button in this cell
	 */
	protected Rectangle getDownSpinnerBounds(Rectangle cellBounds, Object obj, int columnIndex) {
		int width = getSpinnerWidth(cellBounds, obj, columnIndex);
		int height = cellBounds.height / 2 - 1;
		int x = cellBounds.x + cellBounds.width - width;
		int y = cellBounds.y + height + 1;
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Get the added width of image and text together
	 * @param obj cell's object
	 * @param columnIndex cell's columnIndex
	 * @return Get the added width of image and text together
	 */
	protected int getImageTextWidth(Object obj, int columnIndex) {
		Image image = getImage(obj, columnIndex);
		int imageWidth;
		if (image == null) {
			imageWidth = 0;
		} else {
			imageWidth = image.getBounds().width;
		}
		String text = getString(obj, columnIndex);
		int textWidth = 0;
		if (text != null) {
			textWidth = text.length() * CHAR_WIDTH + 5;
		}
		return imageWidth + textWidth;
	}

}
