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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor.CellControlType;

/**
 * This is a generic class to support owner-draw cell label displayed in a
 * TreeViewer
 */
public class AdvisedCellLabelProvider extends StyledCellLabelProvider implements ILabelProvider {

	protected static final int ALPHA = 160;			// transparency value
	protected static final int TEXTOFFSET = 4;
	protected final IColumnAdvisor columnAdvisor; 	// column advisor necessary processing cell attributes e.g. image, check state
	protected final int columnIndex; 					// column index of this cell

	public AdvisedCellLabelProvider(IColumnAdvisor columnAdvisor, int colIndex) {
		this.columnAdvisor = columnAdvisor;
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
		case BUTTON:
			super.paint(event, element);
			drawButton(event, element);
			break;
		case INPLACE_SPIN:
			super.paint(event, element);
			drawSpinner(event, element);
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
		if (columnAdvisor.hasSuffixButton(element, columnIndex)) {
			Rectangle buttonBounds = columnAdvisor.getSuffixButtonBounds(
					getCellBounds(event), element, columnIndex);
			drawSuffixButton(event, element, buttonBounds);
		}
	}

	protected void drawBackground(Event event, Object element) {
		Color clr = columnAdvisor.getBgColor(element, columnIndex);
		if (clr != null) {
			Rectangle cellBounds = getCellControlBounds(event);
			event.gc.setAlpha(ALPHA);
			event.gc.setBackground(clr);
			event.gc.fillRectangle(cellBounds);
			event.gc.setAlpha(255);
		}
	}

	protected void drawImage(Event event, Object element) {
		Image image = columnAdvisor.getImage(element, columnIndex);
		if (image != null) {
			Rectangle cellBounds = getCellControlBounds(event);
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

	protected void drawMark(Event event, Object element) {
		Rectangle cellBounds = getCellControlBounds(event);
		int []ptArr = new int[6];

		int offs = cellBounds.height/3;
		ptArr[0] = cellBounds.x + cellBounds.width;
		ptArr[1] = cellBounds.y + cellBounds.height - offs;
		ptArr[2] = ptArr[0];
		ptArr[3] = cellBounds.y + cellBounds.height;
		ptArr[4] = ptArr[0] - offs;
		ptArr[5] = ptArr[3];

		event.gc.setBackground(ColorConstants.DARK_GRAY);
		event.gc.fillPolygon(ptArr);
	}

	/**
	 * Draw a suffix button
	 *
	 * @param event
	 * @param element tree item in the column zero
	 */
	protected void drawSuffixButton(Event event, Object element, Rectangle buttonBounds) {
		event.gc.setAntialias(SWT.ON);

		int height = buttonBounds.height;
		int width = buttonBounds.width - 2;
		int x = buttonBounds.x + 1;
		int y = buttonBounds.y;

		if (!columnAdvisor.isSuffixButtonPressed(element, columnIndex)) {
			event.gc.setForeground(ColorConstants.COLOR_SUFFICS_BUTTON_TOP);
			event.gc.setBackground(ColorConstants.COLOR_SUFFICS_BUTTON_BOTTOM);
		} else {
			event.gc.setForeground(ColorConstants.COLOR_SUFFICS_BUTTON_BOTTOM);
			event.gc.setBackground(ColorConstants.COLOR_SUFFICS_BUTTON_TOP);
		}
		event.gc.fillGradientRectangle(x, y, width, height, true);
		drawButtonImage(columnAdvisor.getSuffixButtonImage(element, columnIndex), event.gc, buttonBounds);
		event.gc.setForeground(ColorConstants.COLOR_BORDER);
		event.gc.drawRectangle(x, y, width, height);
	}

	/**
	 * Get the cell bounds excluding the suffix button's bound if drawn
	 * @param event the drawing event
	 * @return cell bounds excluding the suffix button's bound if drawn
	 */
	protected Rectangle getCellControlBounds(Event event) {
		Rectangle cellBounds = getCellBounds(event);
		Object element = event.item.getData();
		if (columnAdvisor.hasSuffixButton(element, columnIndex)) {
			Rectangle buttonBounds = columnAdvisor.getSuffixButtonBounds(cellBounds, element, columnIndex);
			if (buttonBounds.width == 0) {
				return cellBounds;
			}
			cellBounds.width -= buttonBounds.width;
		}
		return cellBounds;
	}

	/**
	 * Draw a button
	 * @param event
	 * @param element
	 */
	protected void drawButton(Event event, Object element) {
		if (columnAdvisor.isEmpty(element, columnIndex)) {
			return;
		}
		Rectangle cellBounds = getCellControlBounds(event);
		event.gc.setAntialias(SWT.ON);

		if (!columnAdvisor.isButtonPressed(element, columnIndex)) {
			event.gc.setForeground(ColorConstants.COLOR_BUTTON_TOP);
			event.gc.setBackground(ColorConstants.COLOR_BUTTON_BOTTOM);
		} else {
			event.gc.setForeground(ColorConstants.COLOR_BUTTON_BOTTOM);
			event.gc.setBackground(ColorConstants.COLOR_BUTTON_TOP);
		}

		event.gc.fillGradientRectangle(cellBounds.x + 1, cellBounds.y + 1,
				cellBounds.width-1, cellBounds.height, true);
		event.gc.setForeground(ColorConstants.COLOR_BORDER);
		event.gc.drawRoundRectangle(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height,
				ColorConstants.ARC_WIDTH_HEIGHT, ColorConstants.ARC_WIDTH_HEIGHT);

		int dstX = drawButtonImage(columnAdvisor.getImage(element, columnIndex), event.gc, cellBounds) + 8;

		String text = columnAdvisor.getString(element, columnIndex);
		Point extent = event.gc.textExtent(text);

		//int dstX = (cellBounds.x + cellBounds.width / 2) - extent.x / 2;
		int dstY = (cellBounds.y + cellBounds.height / 2) - extent.y / 2 + cellBounds.height % 2;

		if (columnAdvisor.isEnabled(element, columnIndex)) {
			event.gc.setForeground(ColorConstants.BLACK);
		} else {
			event.gc.setForeground(ColorConstants.COLOR_WIDGET_BORDER);
		}
		event.gc.drawString(text, dstX, dstY, true);
	}

	
	/**
	 * Draw the button image
	 * @param image {@link Image} to draw 
	 * @param gc {@GC} to draw in 
	 * @param bounds rectangle defining destination image position 
	 * @return the right point of the image in the cell, can be used to
	 * calculate the starting position of the cell string
	 */
	protected int drawButtonImage(Image image, GC gc, Rectangle bounds) {
		int dstX = bounds.x;
		int dstY = bounds.y;
		if (image != null) {
			Rectangle imageBounds = image.getBounds();

			//gc.setAlpha(ALPHA);
			gc.drawImage(image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height,
					dstX, dstY, imageBounds.width, imageBounds.height);
			//gc.setAlpha(255);
			return dstX + imageBounds.width;
		}
		return dstX;
	}

	
	
	/**
	 * Draw inplace spinner
	 * @param event the event
	 * @param element the cell's element
	 */
	protected void drawSpinner(Event event, Object element) {
		Rectangle cellBounds = getCellControlBounds(event);
		event.gc.setAntialias(SWT.ON);

		int width = columnAdvisor.getSpinnerWidth(cellBounds, element, columnIndex);
		int x = cellBounds.x + cellBounds.width - width;
		int height = cellBounds.height / 2;

		// draw the up spinner button
		{
			if (!columnAdvisor.isUpSpinnerPressed(element, columnIndex)) {
				event.gc.setForeground(ColorConstants.COLOR_SPINNER_TOP);
				event.gc.setBackground(ColorConstants.COLOR_SPINNER_BUTTON);
			} else {
				event.gc.setForeground(ColorConstants.COLOR_SPINNER_BUTTON);
				event.gc.setBackground(ColorConstants.COLOR_SPINNER_TOP);
			}
			int y = cellBounds.y;
			event.gc.fillGradientRectangle(x, y, width, height, true);
			event.gc.setForeground(ColorConstants.COLOR_BORDER);
			event.gc.drawRectangle(x, y, width, height);

			event.gc.setBackground(ColorConstants.DARK_GRAY);
			int[] ptArr = new int[6];
			ptArr[0] = x + width / 3 - 1;
			ptArr[1] = y + 2 * height / 3;
			ptArr[2] = x + width / 2;
			ptArr[3] = y + height / 3;
			ptArr[4] = x + 2 * width / 3 + 1;
			ptArr[5] = ptArr[1];
			event.gc.fillPolygon(ptArr);
		}

		// draw the down spinner button
		{
			if (!columnAdvisor.isDownSpinnerPressed(element, columnIndex)) {
				event.gc.setForeground(ColorConstants.COLOR_SPINNER_TOP);
				event.gc.setBackground(ColorConstants.COLOR_SPINNER_BUTTON);
			} else {
				event.gc.setForeground(ColorConstants.COLOR_SPINNER_BUTTON);
				event.gc.setBackground(ColorConstants.COLOR_SPINNER_TOP);
			}
			int y = cellBounds.y + height;
			event.gc.fillGradientRectangle(x, y, width, height, true);
			event.gc.setForeground(ColorConstants.COLOR_BORDER);
			event.gc.drawRectangle(x, y, width, height);

			event.gc.setBackground(ColorConstants.DARK_GRAY);
			int[] ptArr = new int[6];
			ptArr[0] = x + width / 3;
			ptArr[1] = y + height / 3 + 1;
			ptArr[2] = x + width / 2;
			ptArr[3] = y + 2 * height / 3 + 1;
			ptArr[4] = x + 2 * width / 3 + 1;
			ptArr[5] = ptArr[1];
			event.gc.fillPolygon(ptArr);
		}
	}

	/**
	 * Draw an URL
	 * @param event the event
	 * @param element tree item in the column zero
	 */
	protected void drawUrl(Event event, Object element) {
		Rectangle cellBounds = getCellControlBounds(event);
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

	/** Return the cell bounds
	 * @param event
	 * @return the cell bounds
	 */
	protected Rectangle getCellBounds(Event event) {
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


	@Override
	public Image getToolTipImage(Object object) {
		return super.getToolTipImage(object);
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		int index = cell.getColumnIndex();
		Color clr = columnAdvisor.getBgColor(element, index);
		if(clr != null) {
			cell.setBackground(clr);
		} else {
			cell.setBackground(null);
		}

		CellControlType cellControlType = columnAdvisor.getCellControlType(element, index);
		switch (cellControlType) {
		case TEXT:
			cell.setImage(columnAdvisor.getImage(element, index));
			break;
		case INPLACE_CHECK:
		case CHECK:			
			cell.setImage(columnAdvisor.getCheckboxImage(element, index));
			break;
		default:
			break;
		}
		
		switch (cellControlType) {
		case TEXT:
		case CHECK:			
		case SPIN:
		case INPLACE_CHECK:
		case INPLACE_SPIN:
		case MENU:
		case COMBO:
			String s = columnAdvisor.getString(element, index);
			if(s != null && !s.isEmpty()) {
			// workaround: append space to the text to avoid last character truncation the cells
				s += ' ';
			}
			cell.setText(s);
			cell.setForeground(columnAdvisor.getFgColor(element, index));
			Font font = columnAdvisor.getFont(element, index);
			if(font != null) {
				Font oldFont = cell.getFont();
				if(oldFont == null || !oldFont.equals(font))
					cell.setFont(font);
			} else {
				Font oldFont = cell.getFont();
				if(oldFont != null)
					cell.setFont(null);
			}
			
			break;
		case URL:
			cell.setImage(columnAdvisor.getImage(element, index));
			cell.setText(columnAdvisor.getString(element, index));
			cell.setForeground(columnAdvisor.getFgColor(element, index));
			break;
		case BUTTON:
			cell.setImage(columnAdvisor.getImage(element, index));
			cell.setText(columnAdvisor.getString(element, index));
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

	@Override
	public Image getImage(Object element) {
		return columnAdvisor.getImage(element, columnIndex);
	}

	@Override
	public String getText(Object element) {
		return columnAdvisor.getString(element, columnIndex);
	}

}
