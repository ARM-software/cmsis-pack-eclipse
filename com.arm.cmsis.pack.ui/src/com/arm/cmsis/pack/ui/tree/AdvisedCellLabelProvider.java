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
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * This is a generic class to support owner-draw cell label displayed in a
 * TreeViewer
 */
public class AdvisedCellLabelProvider extends StyledCellLabelProvider {

	private static final int ALPHA = 160;			// transparency value
	private static final int TEXTOFFSET = 4;
	private IColumnAdvisor columnAdvisor = null; 	// column advisor necessary
													// processing cell
													// attributes e.g. image,
													// check state
	private int columnIndex; 						// column index of this cell

	public AdvisedCellLabelProvider(IColumnAdvisor columnAdviser, int colIndex) {
		this.columnAdvisor = columnAdviser;
		this.columnIndex = colIndex;
	}

	@Override
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		super.initialize(viewer, column);
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void paint(Event event, Object element) {
		drawBackground(event, element);
		switch (columnAdvisor.getCellControlType(element, columnIndex)) {
		case URL:
			super.paint(event, element); 	// draw only URL text
			drawUrl(event, element); 		// underline text
			break;
		case CHECK:
			drawCheckbox(event, element);
			break;
		case SPIN:
		case MENU:
		case COMBO:
			drawImage(event, element);
			super.paint(event, element);
			drawMark(event, element);
			break;
		default:
			super.paint(event, element);
			break;
		}
	}


	private void drawBackground(Event event, Object element) {
		Rectangle cellBounds = getCellBounds(event);
		Color clr = columnAdvisor.getBgColor(element, columnIndex);
		if (clr != null) {
			event.gc.setAlpha(ALPHA);
			event.gc.setBackground(clr);
			event.gc.fillRectangle(cellBounds);
			event.gc.setAlpha(255);
		}
	}
	
	private void drawImage(Event event, Object element) {
		Image image = columnAdvisor.getImage(element, columnIndex);
		if (image != null) {
			Rectangle cellBounds = getCellBounds(event);
			Rectangle imageBounds = image.getBounds();
			int dstX = (cellBounds.x + cellBounds.width) - imageBounds.width;
			int yOff = (cellBounds.height - imageBounds.height)/2;
			yOff = Math.max(2, yOff);
			int dstY = cellBounds.y + yOff;

			event.gc.setAlpha(ALPHA);
			event.gc.drawImage(image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height,
					dstX, dstY, imageBounds.width, imageBounds.height);
			event.gc.setAlpha(255);
		}
	}
	
	private void drawMark(Event event, Object element) {
		Rectangle cellBounds = getCellBounds(event);
		int []ptArr = new int[6];
		
		int offs = cellBounds.height/3;
		ptArr[0] = cellBounds.x + cellBounds.width;
		ptArr[1] = cellBounds.y + cellBounds.height - offs;
		ptArr[2] = ptArr[0];
		ptArr[3] = cellBounds.y + cellBounds.height;
		ptArr[4] = ptArr[0] - offs;
		ptArr[5] = ptArr[3];
		
		event.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		event.gc.fillPolygon(ptArr);
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
		boolean enabled = columnAdvisor.canEdit(element, columnIndex);
		Image image;
		if (enabled)
			image = check ? CpPlugInUI.getImage(CpPlugInUI.ICON_CHECKED)
						  : CpPlugInUI.getImage(CpPlugInUI.ICON_UNCHECKED);
		else
			image = check ? CpPlugInUI.getImage(CpPlugInUI.ICON_CHECKED_GREY)
						  : CpPlugInUI.getImage(CpPlugInUI.ICON_UNCHECKED_GREY);
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

			Image img = columnAdvisor.getImage(element, columnIndex);
			if(img != null){
				x += img.getBounds().width;
				y++;
			}
			
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
		Widget item = event.item;
		Rectangle cellBounds = new Rectangle(0, 0, 0, 0);
		if (item instanceof TreeItem)  {
			TreeItem treeItem = (TreeItem) item;
			cellBounds = treeItem.getBounds(event.index);
		} else if (item instanceof TableItem)  {
			TableItem tableItem = (TableItem) item;
			cellBounds = tableItem.getBounds(event.index);
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
	public Image getToolTipImage(Object object) {
		// TODO Auto-generated method stub
		return super.getToolTipImage(object);
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
		
		boolean enabled = columnAdvisor.isEnabled(element, index);
		
		switch (columnAdvisor.getCellControlType(element, index)) {
		case TEXT:
			cell.setImage(columnAdvisor.getImage(element, index));
		case SPIN:
		case MENU:
		case COMBO:
			cell.setText(columnAdvisor.getString(element, index));
			if(!enabled) {
				cell.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			} else {
				cell.setForeground(null);
			}
			break;
		case URL:
			cell.setImage(columnAdvisor.getImage(element, index));
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
