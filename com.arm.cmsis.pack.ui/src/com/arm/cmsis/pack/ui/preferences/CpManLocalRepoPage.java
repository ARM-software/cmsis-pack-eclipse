package com.arm.cmsis.pack.ui.preferences;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.parser.CpPidxParser;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class CpManLocalRepoPage extends PreferencePage implements IWorkbenchPreferencePage {

    static final String[] FILTER_NAME = { CpStringsUI.CpManLocalRepoPage_PdscFiles };
    static final String[] FILTER_EXT = { "*.pdsc" }; //$NON-NLS-1$
    private static final String FILTER_PATH = "C:/"; //$NON-NLS-1$

    protected Table table;
    Map<String, String> itemFromFile = new HashMap<>();

    /**
     * Create the preference page.
     */
    public CpManLocalRepoPage() {
        // no initialization
    }

    /**
     * Create contents of the preference page.
     * 
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));

        TableViewer tableViewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
        gd_table.widthHint = 369;
        table.setLayoutData(gd_table);

        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnPack = tableViewerColumn.getColumn();
        tblclmnPack.setWidth(150);
        tblclmnPack.setText(CpStringsUI.CpManLocalRepoPage_Pack);

        tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {

                return ((String[]) element)[0];
            }
        });

        TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnRepository = tableViewerColumn_1.getColumn();
        tblclmnRepository.setWidth(220);
        tblclmnRepository.setText(CpStringsUI.CpManLocalRepoPage_Repository);
        tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                String[] str = (String[]) element;
                return str[1]; // repository column
            }
        });

        Button btnAdd = new Button(container, SWT.NONE);
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                FileDialog dialog = new FileDialog(shell, SWT.SINGLE);
                dialog.setText(CpStringsUI.CpManLocalRepoPage_AddPackFromLocalRepo);
                dialog.setFilterNames(FILTER_NAME);
                dialog.setFilterExtensions(FILTER_EXT);
                dialog.setFilterPath(FILTER_PATH);
                if (dialog.open() != null) {
                    String pdscFile = dialog.getFilterPath() + '/' + dialog.getFileName();
                    String packId = CpPidxParser.parsePdsc(pdscFile);
                    if (packId == null) {
                        String msg = CpStringsUI.CpManLocalRepoPage_ErrorInPdsc;
                        msg += ":\n\n"; //$NON-NLS-1$
                        msg += pdscFile;
                        MessageDialog.openError(getShell(), CpStringsUI.CpManLocalRepoPage_ErrorInPdsc, msg);
                        return;
                    }
                    String dir = dialog.getFilterPath() + '/';
                    dir = dir.replace('\\', '/');
                    int cnt = table.getItemCount();
                    boolean found = false;
                    for (int i = 0; i < cnt; ++i) {
                        TableItem ti = table.getItem(i);
                        if (packId.equals(ti.getText())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        String[] str = new String[2];
                        TableItem item = new TableItem(table, SWT.NONE);
                        str[0] = packId;
                        str[1] = dir;
                        item.setText(str);
                    }
                }
            }
        });

        GridData gd_btnAdd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnAdd.widthHint = 56;
        btnAdd.setLayoutData(gd_btnAdd);
        btnAdd.setText(CpStringsUI.CpManLocalRepoPage_Add);

        Button btnRemove = new Button(container, SWT.NONE);
        btnRemove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                table.remove(table.getSelectionIndices());
            }
        });
        btnRemove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        btnRemove.setText(CpStringsUI.CpManLocalRepoPage_Remove);

        List<String[]> list = new LinkedList<>();
        for (Map.Entry<String, String> item : itemFromFile.entrySet()) {
            String[] str = new String[2];
            str[0] = item.getKey();
            str[1] = item.getValue();
            list.add(str);
        }
        tableViewer.setInput(list);

        return container;
    }

    @Override
    public boolean performOk() {
        URI rteRootPath = CpPlugIn.getPackManager().getCmsisPackRootURI();
        String localRepoFile = CpPlugIn.getPackManager().getCmsisPackLocalDir() + '/'
                + CmsisConstants.LOCAL_REPOSITORY_PIDX;
        TableItem[] items = table.getItems();
        Map<String, String> newItems = new HashMap<>();

        if (items != null) {
            for (TableItem item : items) {
                newItems.put(item.getText(0), item.getText(1));
            }
        }

        if (!newItems.equals(itemFromFile)) {
            CpPidxParser.createPidxFile(localRepoFile, newItems, rteRootPath.toString());
            CpPlugIn.getPackManager().reload();
            itemFromFile = newItems;

        }
        return super.performOk();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        if (table != null) {
            readPidx();
            table.removeAll();
            if (itemFromFile == null) {
                return;
            }
            for (Map.Entry<String, String> item : itemFromFile.entrySet()) {
                String[] str = new String[2];
                str[0] = item.getKey();
                str[1] = item.getValue();
                TableItem row = new TableItem(table, SWT.NONE);
                row.setText(str);
            }
        }
    }

    /**
     * Initialize the preference page.
     */
    @Override
    public void init(IWorkbench workbench) {
        readPidx();
    }

    protected void readPidx() {
        String localRepoFile = CpPlugIn.getPackManager().getCmsisPackLocalDir() + '/'
                + CmsisConstants.LOCAL_REPOSITORY_PIDX;
        Map<String, String> map = CpPidxParser.parsePidx(localRepoFile);

        if (itemFromFile == null || map == null) {
            return;
        }

        for (Map.Entry<String, String> item : map.entrySet()) {
            String packId = item.getKey();
            if (packId != null) {
                itemFromFile.put(packId, item.getValue().replace('\\', '/'));
            }
        }
    }
}
