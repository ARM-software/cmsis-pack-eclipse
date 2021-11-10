package com.arm.cmsis.pack.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.tree.CpItemColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

public class CpItemTreeDialog extends TitleAreaDialog {

    protected Label lblTreeLabel;
    protected TreeViewer fTreeViewer;
    protected ICpItem fItem = null;
    protected ICpItem fOriginalItem = null;
    protected IColumnAdvisor fColumnAdvisor = null;

    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public CpItemTreeDialog(Shell parentShell, ICpItem item) {
        super(parentShell);
        setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
        fOriginalItem = item;
        fItem = item.copyTo(null);
    }

    protected ITreeContentProvider createContentProvider() {
        return new TreeObjectContentProvider();
    }

    protected IColumnAdvisor createColumnAdvisor(TreeViewer viewer) {
        return new CpItemColumnAdvisor(viewer);
    }

    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        lblTreeLabel = new Label(container, SWT.NONE);
        lblTreeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblTreeLabel.setText(CpStringsUI.CpItemTreeDialog_Tree);

        fTreeViewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        fTreeViewer.setContentProvider(createContentProvider());

        fColumnAdvisor = createColumnAdvisor(fTreeViewer);

        createColumns();
        init();
        return area;
    }

    protected void createColumns() {
        TreeViewerColumn columnName = new TreeViewerColumn(fTreeViewer, SWT.LEFT);
        columnName.getColumn().setText(CpStringsUI.CpItemTreeDialog_Name);
        columnName.getColumn().setWidth(150);
        columnName.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return fColumnAdvisor.getString(element, 0);
            }
        });
    }

    protected void init() {
        fTreeViewer.setInput(fItem);
    }

    /**
     * Applies changes from working item to the original one
     * 
     * @return true if modified
     */
    public boolean apply() {
        return false;
    }

    public ICpItem getCpItem() {
        return fItem;
    }

    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 600);
    }
}
