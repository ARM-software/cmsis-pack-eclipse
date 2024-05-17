/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TypedListener;

/**
 * A spinner that can represent hex, binary, octal numbers in the box.
 */
public class NumberSpinner extends Composite {

    static final int BUTTON_WIDTH = 16;
    Text text;
    Button up, down;
    long minimum, maximum;
    int base;
    Composite parent;
    long spinStep = 1;

    public NumberSpinner(Composite parent, int style) {

        super(parent, style);

        this.parent = parent;

        text = new Text(this, style | SWT.SINGLE | SWT.BORDER);
        up = new Button(this, style | SWT.ARROW | SWT.UP);
        down = new Button(this, style | SWT.ARROW | SWT.DOWN);

        text.addListener(SWT.Verify, new Listener() {
            @Override
            public void handleEvent(Event e) {
                verify(e);
            }
        });

        text.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event e) {
                mytraverse(e);
            }
        });

        up.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                up();
            }
        });

        down.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event e) {
                down();
            }
        });

        addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                resize();
            }
        });

        addListener(SWT.FocusIn, new Listener() {
            @Override
            public void handleEvent(Event e) {
                focusIn();
            }
        });

        text.setFont(getFont());
        setSelection(0);
    }

    void verify(Event e) {
    }

    void mytraverse(Event e) {

        switch (e.detail) {
        case SWT.TRAVERSE_ARROW_PREVIOUS:
            if (e.keyCode == SWT.ARROW_UP) {
                e.doit = true;
                e.detail = SWT.NULL;
                up();
            }
            break;

        case SWT.TRAVERSE_ARROW_NEXT:
            if (e.keyCode == SWT.ARROW_DOWN) {
                e.doit = true;
                e.detail = SWT.NULL;
                down();
            }
            break;
        }

    }

    void up() {
        setSelection(getSelection() + spinStep);
        notifyListeners(SWT.Selection, new Event());
    }

    void down() {
        setSelection(getSelection() - spinStep);
        notifyListeners(SWT.Selection, new Event());
    }

    void focusIn() {
        text.setFocus();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        text.setFont(font);
    }

    public void setBase(int base) {
        this.base = base;
    }

    public void setSelection(long selection) {
        if (selection < minimum) {
            selection = minimum;
        } else if (selection > maximum) {
            selection = maximum;
        }

        switch (base) {
        case 10:
            text.setText(Long.toString(selection));
            break;
        case 16:
            text.setText("0x" + Long.toHexString(selection).toUpperCase()); //$NON-NLS-1$
            break;
        case 8:
            text.setText("0" + Long.toOctalString(selection)); //$NON-NLS-1$
            break;
        case 2:
            text.setText("0b" + Long.toBinaryString(selection)); //$NON-NLS-1$
        default:
            break;
        }

        text.selectAll();
        text.setFocus();
    }

    public long getSelection() {
        try {
            String num = getContent();
            switch (base) {
            case 16:
            case 2:
                num = num.substring(2);
                break;
            case 8:
                num = num.substring(1);
                break;
            default:
                break;
            }
            return Long.parseLong(num, base);
        } catch (NumberFormatException e) {
            return minimum;
        }
    }

    public void setMaximum(long maximum) {
        checkWidget();
        this.maximum = maximum;
    }

    public long getMaximum() {
        return maximum;
    }

    public void setMinimum(long minimum) {
        this.minimum = minimum;
    }

    public long getMinimum() {
        return minimum;
    }

    void resize() {
        Point pt = computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int textWidth = pt.x - BUTTON_WIDTH;
        int buttonHeight = pt.y / 2;
        text.setBounds(0, 0, textWidth, pt.y);
        up.setBounds(textWidth, 0, BUTTON_WIDTH, buttonHeight);
        down.setBounds(textWidth, pt.y - buttonHeight - 2, BUTTON_WIDTH, buttonHeight);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point pt = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int width = pt.x;
        try {
            // FIXME: better way to calculate the width?
            width = ((Tree) parent).getColumn(1).getWidth();
        } catch (Exception e) {
        }
        int height = pt.y;

        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }

        return new Point(width, height);
    }

    public void addSelectionListener(SelectionListener listener) {
        if (listener == null) {
            throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
        }

        addListener(SWT.Selection, new TypedListener(listener));
    }

    @Override
    public void dispose() {
        text.dispose();
        up.dispose();
        down.dispose();
    }

    public Text getText() {
        return text;
    }

    /**
     * @return the text in the text control
     */
    public String getContent() {
        return text.getText();
    }

    /**
     * @param spinStep
     */
    public void setIncrement(long spinStep) {
        this.spinStep = spinStep;
    };

}
