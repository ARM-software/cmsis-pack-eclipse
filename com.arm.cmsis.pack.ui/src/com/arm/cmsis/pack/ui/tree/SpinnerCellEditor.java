package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;

/**
 * A cell editor that presents a spinner. The cell editor's
 * value is the selection of the spinner. The bounds of the spinner
 * are set by first getting the spinner and then applying its setter.
 */
public class SpinnerCellEditor extends CellEditor {
	private Spinner spinner;
	public SpinnerCellEditor(Composite parent) {
		super(parent);
	}
	
	@Override
	protected Control createControl(Composite parent) {
		spinner = new Spinner(parent, SWT.NONE);
		return spinner;
	}

	@Override
	protected Object doGetValue() {
		if (spinner != null) {
			return spinner.getText();
		}
		return null;
	}

	@Override
	protected void doSetFocus() {
		spinner.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		if (value != null && spinner != null) {
			if (value instanceof Integer) {
				spinner.setSelection((int) value);
			}
		}
	}
	
	/**
	 * 
	 * @return the embedded spinner control
	 */
	public Spinner getSpinner() {
		return spinner;
	}

}
