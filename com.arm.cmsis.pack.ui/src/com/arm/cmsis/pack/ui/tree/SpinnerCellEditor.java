package com.arm.cmsis.pack.ui.tree;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A cell editor that presents a spinner. The cell editor's value is the
 * selection of the spinner. The bounds of the spinner are set by first getting
 * the spinner and then applying its setter.
 */
public class SpinnerCellEditor extends CellEditor {
    private NumberSpinner spinner;
    private Text text;

    public SpinnerCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
        spinner = new NumberSpinner(parent, SWT.NONE);
        text = spinner.getText();
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                handleDefaultSelection(e);
            }
        });
        return spinner;
    }

    @Override
    protected Object doGetValue() {
        if (spinner != null) {
            return spinner.getContent();
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
            if (value instanceof Long) {
                spinner.setSelection((long) value);
            }
        }
    }

    @Override
    public void performCut() {
        text.cut();
    }

    @Override
    public void performCopy() {
        text.copy();
    }

    @Override
    public void performPaste() {
        text.paste();
    }

    protected void handleDefaultSelection(SelectionEvent event) {
        // same with enter-key handling code in keyReleaseOccured(e);
        fireApplyEditorValue();
        deactivate();
    }

    /**
     *
     * @return the embedded spinner control
     */
    public NumberSpinner getSpinner() {
        return spinner;
    }

}
