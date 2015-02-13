/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.arm.cmsis.pack.tree;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import com.arm.cmsis.pack.tree.IColumnAdvisor.CellControlType;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * This is a generic class to support owner-draw cell label displayed in a
 * TreeViewer
 */
public class AdvisedCellLabelProvider extends StyledCellLabelProvider {

	private static final int TEXTOFFSET = 4;
	private static final Cursor CURSOR_HAND = Display.getCurrent()
			.getSystemCursor(SWT.CURSOR_HAND); // hand cursor for URL text
	private IColumnAdvisor columnAdvisor = null; // column advisor necessary
													// processing cell
													// attributes e.g. image,
													// check state
	private int columnIndex; // column index of this cell
	private Control control; // primary control of the tree viewer which is a
								// Tree

	public AdvisedCellLabelProvider(IColumnAdvisor columnAdviser, int colIndex) {
		this.columnAdvisor = columnAdviser;
		this.columnIndex = colIndex;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void paint(Event event, Object element) {

		switch (columnAdvisor.getCellControlType(element, columnIndex)) {
		case URL:
			super.paint(event, element); 	// draw only URL text
			drawUrl(event, element); 		// underline text
			break;
		case CHECK:
			drawCheckbox(event, element);
			break;
		default:
			super.paint(event, element); 	// draw only URL text
			break;
		}
	}

	@Override
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		super.initialize(viewer, column);

		this.control = viewer.getControl();
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
	}

	/**
	 * Handling mouse over event to display hand cursor in case cell contains an
	 * URL
	 * 
	 * @param e mouse event
	 */
	private void handleMouseOver(MouseEvent e) {
		Point pt = new Point(e.x, e.y);
		ViewerCell cell = getViewer().getCell(pt);
		boolean cursorSet = false;

		if (cell != null) {
			int colIndex = cell.getColumnIndex();
			Object element = cell.getElement();
			if (columnAdvisor.getCellControlType(element, colIndex) == CellControlType.URL) {
				if (columnAdvisor.getString(element, colIndex) != null) {
					this.control.setCursor(CURSOR_HAND);
					cursorSet = true;
				}
			}
		}

		if (!cursorSet) {
			handleMouseExit(e);
		}
	}

	/**
	 * reset cursor 'hand'
	 * 
	 * @param e
	 *            mouse event
	 */
	private void handleMouseExit(MouseEvent e) {
		if (this.control.getCursor() == CURSOR_HAND) {
			this.control.setCursor(null);
		}
	}

	/**
	 * Draw a check box
	 * 
	 * @param event
	 * @param element
	 *            tree item in the column zero
	 */
	private void drawCheckbox(Event event, Object element) {
		Rectangle cellBounds = getCellBounds(event);
		boolean check = columnAdvisor.getCheck(element, columnIndex);
		Image image = check ? CpPlugInUI.getImage(CpPlugInUI.ICON_CHECKED) : 
			CpPlugInUI.getImage(CpPlugInUI.ICON_UNCHECKED);
		Rectangle imageBounds = image.getBounds();
		int dstX = (cellBounds.x + cellBounds.width / 2) - imageBounds.width / 2;
		int dstY = (cellBounds.y + cellBounds.height / 2) - imageBounds.height / 2 + cellBounds.height % 2;

		event.gc.drawImage(image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height,
				dstX, dstY, imageBounds.width, imageBounds.height);
	}

	/**
	 * Draw an URL
	 * 
	 * @param event
	 * @param element
	 *            tree item in the column zero
	 */
	private void drawUrl(Event event, Object element) {
		Rectangle cellBounds = getCellBounds(event);
		String text = columnAdvisor.getString(element, columnIndex);
		if (text != null) {
			int x = cellBounds.x + TEXTOFFSET;
			int y = cellBounds.y;
			Point extent = event.gc.textExtent(text);
			event.gc.drawLine(x - 1, y + extent.y, x + extent.x - 1, y
					+ extent.y);
		}
	}

	/**
	 * Return the cell bounds
	 * 
	 * @param event
	 * @return The cell bounds
	 */
	private Rectangle getCellBounds(Event event) {
		TreeItem treeItem = (TreeItem) event.item;
		Rectangle cellBounds = new Rectangle(0, 0, 0, 0);
		if (treeItem != null) {
			cellBounds = treeItem.getBounds(event.index);
		}
		return cellBounds;
	}

	/**
	 * @return the Display object
	 */
	public static Display getDisplay() {
		Display display = Display.getCurrent();
		// may be null if outside the UI thread
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	
	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		int index = cell.getColumnIndex();
		Color clr = columnAdvisor.getBgColor(element, index);
		if(clr != null)
			cell.setBackground(clr);
		else 
			cell.setBackground(null);
		
		switch (columnAdvisor.getCellControlType(element, index)) {
		case TEXT:
			cell.setText(columnAdvisor.getString(element, index));
			cell.setImage(columnAdvisor.getImage(element, index));
			break;
		case COMBO:
			cell.setText(columnAdvisor.getString(element, index));
			break;
		case URL:
			cell.setText(columnAdvisor.getString(element, index));
			cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
			break;
		default:
			break;
		}
		super.update(cell);
	}

	@Override
	public String getToolTipText(Object element) {
		return columnAdvisor.getTooltipText(element, columnIndex);
	}
}
