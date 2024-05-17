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
package com.arm.cmsis.config.editors;

import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.arm.cmsis.config.IHelpContextIds;
import com.arm.cmsis.config.Messages;
import com.arm.cmsis.config.model.IConfigWizardItem;
import com.arm.cmsis.config.model.IConfigWizardItem.EItemErrorType;
import com.arm.cmsis.config.model.IConfigWizardItem.EItemType;
import com.arm.cmsis.pack.ui.ColorConstants;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.AdvisedEditingSupport;
import com.arm.cmsis.pack.ui.tree.ColumnAdvisor;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;
import com.arm.cmsis.parser.ConfigWizardParser;
import com.arm.cmsis.parser.ConfigWizardScanner;
import com.arm.cmsis.utils.Utils;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to use configuration wizard
 * </ul>
 */
@SuppressWarnings("restriction")
public class ConfigEditor extends MultiPageEditorPart implements IResourceChangeListener, ITextEditor {

    /** The text editor used in page 0. */
    TextEditor editor;

    /** The document in the editor. */
    private IDocument fDocument;

    /** The resource file in the workspace */
    IFile fFile;

    /** parser job for the config wizard. */
    Job fParserJob;

    /** parser for the config wizard. */
    ConfigWizardParser fParser;

    /** True if user has changed the text before switch pages */
    boolean fNeedReparse;

    /** The tree viewer */
    TreeViewer fViewer;

    /** The second column's advisor */
    private IColumnAdvisor fColumnAdvisor;

    /** The tooltip text */
    private StyledText fText;

    IConfigWizardItem getConfigWizardItem(Object obj) {
        if (obj instanceof IConfigWizardItem) {
            return (IConfigWizardItem) obj;
        }
        return null;
    }

    /**
     * Creates a multi-page editor example.
     */
    public ConfigEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        fParserJob = new Job(Messages.ConfigEditor_ParsingConfigWizard) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(Messages.ConfigEditor_ParsingConfigWizard, 100);
                monitor.worked(98);
                fParser.parse();
                fNeedReparse = false;
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        fParserJob.setUser(true);
        fParserJob.setPriority(Job.INTERACTIVE);
        fParserJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        IConfigWizardItem root = fParser.getConfigWizardRoot();
                        if (root == null) { // parsing error
                            editor.selectAndReveal(fParser.getParsingErrorOffset(), 0);
                        } else if (fViewer.getControl() != null && !fViewer.getControl().isDisposed()) {
                            fViewer.setInput(root);
                        }
                    }
                });
            }
        });
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter.isInstance(this)) {
            return adapter.cast(this);
        }
        if (editor != null && adapter.isInstance(editor)) {
            return adapter.cast(editor);
        }
        return super.getAdapter(adapter);
    }

    /**
     * Creates page 0 of the multi-page editor, which contains a text editor.
     */
    void createPage0() {
        try {
            IEditorInput editorInput = getEditorInput();
            if (editorInput.getName().toLowerCase().endsWith(".s")) { //$NON-NLS-1$
                editor = new AsmTextEditor();
            } else {
                editor = new CEditor();
            }
            int index = addPage(editor, getEditorInput());
            setPageText(index, Messages.ConfigEditor_FirstPageText);
            setPartName(editor.getTitle());
            setTitleImage(CpPlugInUI.getImage(CpPlugInUI.ICON_DETAILS));

            fDocument = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        } catch (PartInitException e) {
            ErrorDialog.openError(getSite().getShell(), Messages.ConfigEditor_ErrorInNestedTextEditor, null,
                    e.getStatus());
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which shows the configuration wizard
     */
    void createPage1() {

        Composite composite = new Composite(getContainer(), SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        buildToolBar(composite);

        buildTreeViewer(composite);

        buildText(composite);

        int index = addPage(composite);
        setPageText(index, Messages.ConfigEditor_SecondPageText);

    }

    /**
     * Creates the pages of the multi-page editor.
     */
    @Override
    protected void createPages() {
        createPage0();
        fParser = new ConfigWizardParser(new ConfigWizardScanner(editor instanceof AsmTextEditor), fDocument);
        if (fParser.containWizard()) {
            fParserJob.schedule();
            createPage1();
            hookDocumentChangeListener();
            setActivePage(1);
        }
    }

    protected void hookDocumentChangeListener() {
        fDocument.addDocumentListener(new IDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                fNeedReparse = true;
            }

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // do nothing
            }
        });
    }

    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors. Subclasses
     * may extend.
     */
    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    /**
     * Saves the multi-page editor's document.
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        getEditor(0).doSave(monitor);
    }

    /**
     * Saves the multi-page editor's document as another file. Also updates the text
     * for page 0's tab, and updates this multi-page editor's input to correspond to
     * the nested editor's.
     */
    @Override
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPartName(editor.getTitle());
        setInput(editor.getEditorInput());
    }

    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        fFile = ResourceUtil.getFile(input);
        String title = input.getName();
        setPartName(title);
    }

    /**
     * The <code>MultiPageEditorExample</code> implementation of this method checks
     * that the input is an instance of <code>IFileEditorInput</code>.
     */
    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        if (!(editorInput instanceof IFileEditorInput)) {
            throw new PartInitException(Messages.ConfigEditor_InvalidEditorInput);
        }
        super.init(site, editorInput);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     *
     * Calculates the contents of page 2 when the it is activated.
     */
    @Override
    protected void pageChange(int newPageIndex) {
        if (newPageIndex == 1 && fNeedReparse) {
            if (fParser.findConfigurationWizard()) {
                fParserJob.schedule();
                fNeedReparse = false;
                super.pageChange(newPageIndex);
            } else {
                removePage(1);
            }
            return;
        } else if (newPageIndex == 0) {
            fNeedReparse = false;
        }
        super.pageChange(newPageIndex);
    }

    /**
     * Closes all project files on project close.
     */
    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        // consider only POST_CHANGE events
        if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
            return;
        }
        IResourceDelta resourseDelta = event.getDelta();
        IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
            @Override
            public boolean visit(IResourceDelta delta) {
                IResource resource = delta.getResource();
                int type = resource.getType();
                if (type == IResource.ROOT) {
                    return true; // workspace => visit children
                }

                int kind = delta.getKind();
                int flags = delta.getFlags();

                if (type == IResource.FILE && kind == IResourceDelta.REMOVED && resource.equals(fFile)) {

                    if ((flags & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
                        // renamed
                        IPath newPath = delta.getMovedToPath();
                        IFile r = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(newPath);
                        final FileEditorInput fileEditorInput = new FileEditorInput(r);
                        Display.getDefault().asyncExec(() -> setInput(fileEditorInput));
                        return false;
                    } else if (flags == 0 // project deleted
                            || (flags & IResourceDelta.MARKERS) != 0) { // markers have changed
                        Display.getDefault().asyncExec(() -> {
                            ConfigEditor.this.getEditorSite().getPage().closeEditor(ConfigEditor.this, true);
                        });
                        return false;
                    }
                }
                return true;
            }
        };

        try {
            resourseDelta.accept(deltaVisitor);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Control getControl(int pageIndex) {
        if (pageIndex == 1) {
            return fViewer.getControl();
        }
        return super.getControl(pageIndex);
    }

    @Override
    public void setFocus() {
        if (fNeedReparse && getActivePage() == 1) {
            if (fParser.findConfigurationWizard()) {
                fParserJob.schedule();
                fNeedReparse = false;
            } else {
                removePage(1);
            }
        }
        super.setFocus();
    }

    /*************************
     * Here begins the Configuration Wizard GUI part
     *************************/

    class ManagedToolBar {
        ToolBar toolBar;
        ToolBarManager toolBarManager;

        public ManagedToolBar(final Composite parent, int style) {
            toolBarManager = new ToolBarManager(style);
            toolBar = toolBarManager.createControl(parent);
            toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
            toolBar.addListener(SWT.FOCUSED, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    parent.setFocus();
                }
            });
        }

        public void addAction(IAction action, boolean showText) {
            if (showText) {
                ActionContributionItem aci = new ActionContributionItem(action);
                aci.setMode(ActionContributionItem.MODE_FORCE_TEXT);
                toolBarManager.add(aci);
            } else {
                toolBarManager.add(action);
            }
            toolBarManager.update(true);
        }

        public void dispose() {
            toolBarManager.removeAll();
            toolBarManager.dispose();
            toolBarManager = null;
            toolBar = null;
        }
    }

    private void buildToolBar(Composite parent) {

        Label imageLabel = new Label(parent, SWT.LEFT);
        imageLabel.setImage(CpPlugInUI.getImage(CpPlugInUI.ICON_DETAILS));

        Label title = new Label(parent, SWT.NONE);
        title.setFont(JFaceResources.getHeaderFont());
        title.setText(Messages.ConfigEditor_SecondPageText);

        ManagedToolBar toolbar = new ManagedToolBar(parent, SWT.FLAT | SWT.RIGHT_TO_LEFT);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IHelpContextIds.CONFIG_WIZARD);

        Action help = new Action(Messages.ConfigEditor_Help, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                parent.notifyListeners(SWT.Help, new Event());
            }
        };
        help.setToolTipText(Messages.ConfigEditor_HelpForConfigWizard);
        help.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_HELP));
        toolbar.addAction(help, false);

        Action collapseAll = new Action(Messages.ConfigEditor_CollapseAll, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                fViewer.collapseAll();
            }
        };
        collapseAll.setToolTipText(Messages.ConfigEditor_CollapseAllItems);
        collapseAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_COLLAPSE_ALL));
        toolbar.addAction(collapseAll, false);

        Action expandAll = new Action(Messages.ConfigEditor_ExpandAll, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                fViewer.expandAll();
            }
        };
        expandAll.setToolTipText(Messages.ConfigEditor_ExpandAllItems);
        expandAll.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_EXPAND_ALL));
        toolbar.addAction(expandAll, false);
    }

    private void buildText(Composite parent) {
        fText = new StyledText(parent, SWT.MULTI | SWT.BORDER);
        fText.setEnabled(false);
        fText.setEditable(false);
        GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        gd_text.heightHint = 80;
        fText.setLayoutData(gd_text);
    }

    class ContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            IConfigWizardItem item = getConfigWizardItem(parentElement);
            if (item != null) {
                return item.getChildren().toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            IConfigWizardItem item = getConfigWizardItem(element);
            if (item != null) {
                return item.getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element) != null && getChildren(element).length > 0;
        }

    }

    class FirstColumnLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object obj) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            if (item != null) {
                return item.getName();
            }
            return null;
        }

        @Override
        public Color getForeground(Object element) {
            IConfigWizardItem item = getConfigWizardItem(element);
            if (item.isInconsistent()) {
                return ColorConstants.RED;
            }
            return super.getForeground(element);
        }

        @Override
        public String getToolTipText(Object obj) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            if (item != null && !item.getTooltip().isEmpty()) {
                return item.getTooltip();
            }
            return null;
        }
    }

    private static final int COLEDIT = 1;

    private class ConfigColumnAdvisor extends ColumnAdvisor {

        public ConfigColumnAdvisor(ColumnViewer columnViewer) {
            super(columnViewer);
        }

        @Override
        public CellControlType getCellControlType(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            EItemType type = item.getItemType();
            switch (type) {
            case HEADING_ENABLE:
            case OPTION_CHECK:
            case CODE_ENABLE:
            case CODE_DISABLE:
                return CellControlType.CHECK;
            case OPTION_SELECT:
                return CellControlType.MENU;
            case OPTION:
                if (item.getSpinStep() != 0) {
                    return CellControlType.SPIN;
                }
                break;
            default:
                break;
            }

            return CellControlType.TEXT;
        }

        @Override
        public boolean isEnabled(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            return item.canModify();
        }

        @Override
        public boolean canEdit(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            EItemType type = item.getItemType();
            switch (type) {
            case HEADING:
                return false;
            case HEADING_ENABLE:
            case CODE_ENABLE:
            case CODE_DISABLE:
                return isEnabled(item.getParent(), columnIndex);
            case OPTION:
            case OPTION_CHECK:
            case OPTION_SELECT:
            case OPTION_STRING:
                return isEnabled(obj, columnIndex);

            default:
                break;
            }
            return false;
        }

        @Override
        public boolean getCheck(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            return item.getValue() > 0;
        }

        @Override
        public void setCheck(Object element, int columnIndex, boolean newVal) {
            IConfigWizardItem item = getConfigWizardItem(element);
            fParser.updateModel(item, newVal);
            fViewer.update(item, null);
            fViewer.refresh();
        }

        @Override
        public Image getImage(Object obj, int columnIndex) {
            if (getCellControlType(obj, columnIndex) == CellControlType.INPLACE_CHECK
                    || getCellControlType(obj, columnIndex) == CellControlType.CHECK) {
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
            return super.getImage(obj, columnIndex);
        }

        @Override
        public String getString(Object obj, int columnIndex) {
            CellControlType type = getCellControlType(obj, columnIndex);
            if (type == CellControlType.CHECK) {
                return null;
            }
            IConfigWizardItem item = getConfigWizardItem(obj);
            EItemErrorType errorType = item.getItemErrorType();
            if (errorType == EItemErrorType.NUMBER_PARSE_ERROR) {
                return null;
            } else if (errorType == EItemErrorType.LOCATE_POSITION_ERROR) {
                return "<<< Unable to locate value position >>>"; //$NON-NLS-1$
            }

            if (item.getItemType() == EItemType.OPTION_STRING) {
                return item.getString().replace("\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            // for heading item and notification item, there is no value to show
            if (item.getItemType() == EItemType.HEADING || item.getItemType() == EItemType.ROOT
                    || item.getItemType() == EItemType.NOTIFICATION) {
                return null;
            }

            long value = item.getValue(); // current selected value in the combo box
            long modifier = item.getModifier(); // number modifier after the keyword o
            char op = item.getModification();
            long realValue = Utils.modifyValueR(value, op, modifier, item.getMaxValue(), item.getMinValue());

            if (item.isStringOption() && type == CellControlType.MENU) { // string options
                Map<String, String> items = item.getStrItems();
                if (item.getSelStr() != null && items.containsKey(item.getSelStr())) {
                    return items.get(item.getSelStr());
                }
                return null;
            }
            if (type == CellControlType.MENU) {
                Map<Long, String> items = item.getItems();
                if (items.containsKey(realValue)) {
                    return items.get(realValue);
                }
                return null;
            }
            if (item.getBase() == 10) {
                return String.valueOf(realValue);
            } else if (item.getBase() == 2) {
                String str = Long.toBinaryString(realValue);
                StringBuilder sb = new StringBuilder(str);
                int idx = sb.length() - 4;

                while (idx > 0) {
                    sb.insert(idx, " "); //$NON-NLS-1$
                    idx = idx - 4;
                }
                return "0b" + sb.toString().toUpperCase(); //$NON-NLS-1$
            } else if (item.getBase() == 8) {
                String str = Long.toOctalString(realValue);
                return "0" + str; //$NON-NLS-1$
            } else {
                String str = Long.toHexString(realValue);
                if (!str.startsWith("0") && str.length() % 2 != 0) { //$NON-NLS-1$
                    str = "0" + str; //$NON-NLS-1$
                }
                StringBuilder sb = new StringBuilder(str);
                int idx = sb.length() - 4;

                while (idx > 0) {
                    sb.insert(idx, " "); //$NON-NLS-1$
                    idx = idx - 4;
                }
                return "0x" + sb.toString().toUpperCase(); //$NON-NLS-1$
            }
        }

        @Override
        public long getCurrentSelectedIndex(Object element, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(element);
            // This is selection
            if (item.getItemType() == EItemType.OPTION_SELECT) {
                long key = item.getValue();
                int i = 0;
                for (Long k : item.getItems().keySet()) {
                    if (k == key) {
                        return i;
                    }
                    i++;
                }
                return 0;
            }
            long modifier = item.getModifier();
            char op = item.getModification();
            long realValue = Utils.modifyValueR(item.getValue(), op, modifier, item.getMaxValue(), item.getMinValue());
            return realValue;
        }

        @Override
        public void setCurrentSelectedIndex(Object obj, int columnIndex, long newVal) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            // This is selection
            fParser.updateModel(item, newVal);
            fViewer.update(item, null);
        }

        @Override
        public String[] getStringArray(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            Assert.isTrue(item.getItemType() == EItemType.OPTION_SELECT);
            if (item.isStringOption()) {
                Collection<String> str = item.getStrItems().values();
                return str.toArray(new String[str.size()]);
            }
            Collection<String> str = item.getItems().values();
            return str.toArray(new String[str.size()]);
        }

        @Override
        public long getMaxCount(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            long max = item.getMaxValue();
            return max;
        }

        @Override
        public long getMinCount(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            long min = item.getMinValue();
            if (min < 0) {
                min = 0;
            }
            return min;
        }

        @Override
        public long getSpinStep(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            long spinStep = item.getSpinStep();
            return spinStep;
        }

        @Override
        public int getItemBase(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            return item.getBase();
        }

        @Override
        public void setString(Object obj, int columnIndex, String newVal) {
            if (getCellControlType(obj, columnIndex) == CellControlType.CHECK) {
                return;
            }
            IConfigWizardItem item = getConfigWizardItem(obj);
            fParser.updateModel(item, newVal);
            fViewer.update(item, null);
        }

        @Override
        public String getTooltipText(Object obj, int columnIndex) {
            IConfigWizardItem item = getConfigWizardItem(obj);
            if (item.getTooltip().isEmpty()) {
                return null;
            }
            return item.getTooltip();
        }

    }

    private void buildTreeViewer(Composite parent) {
        fViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        Tree tree = fViewer.getTree();
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                if (item != null) {
                    IConfigWizardItem configItem = getConfigWizardItem(item.getData());
                    if (configItem != null) {
                        setCursorAndRevealAt(configItem.getStartLine());
                        showTooltipText(configItem);
                    }
                }
            }
        });
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);
        fColumnAdvisor = new ConfigColumnAdvisor(fViewer);

        TreeViewerColumn column0 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column0.getColumn().setText(Messages.ConfigEditor_Option);
        column0.getColumn().setWidth(320);
        column0.setLabelProvider(new FirstColumnLabelProvider());

        TreeViewerColumn column1 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column1.getColumn().setText(Messages.ConfigEditor_Value);
        column1.getColumn().setWidth(100);
        column1.setEditingSupport(new AdvisedEditingSupport(fViewer, fColumnAdvisor, COLEDIT));
        column1.setLabelProvider(new AdvisedCellLabelProvider(fColumnAdvisor, COLEDIT));

        // Add an empty column and adjust its width automatically for better looking
        TreeViewerColumn column2 = new TreeViewerColumn(fViewer, SWT.LEFT);
        column2.getColumn().setWidth(1);
        column2.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ""; //$NON-NLS-1$
            }
        });

        if (System.getProperty("os.name").startsWith("Windows")) { //$NON-NLS-1$ //$NON-NLS-2$
            parent.addControlListener(new ControlAdapter() {
                @Override
                public void controlResized(ControlEvent e) {
                    Tree tree = (Tree) fViewer.getControl();
                    Rectangle area = parent.getClientArea();
                    Point preferredSize = tree.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                    int width = area.width - 2 * tree.getBorderWidth();
                    if (preferredSize.y > tree.getBounds().height) {
                        // Subtract the scrollbar width from the total column width
                        // if a vertical scrollbar will be required
                        Point vBarSize = tree.getVerticalBar().getSize();
                        width -= vBarSize.x;
                    }
                    Point oldSize = tree.getSize();
                    if (oldSize.x > area.width) {
                        // tree is getting smaller so make the columns
                        // smaller first and then resize the tree to
                        // match the client area width
                        column2.getColumn().setWidth(Math.max(0,
                                width - column0.getColumn().getWidth() - column1.getColumn().getWidth() - 10));
                        tree.setSize(area.width, area.height);
                    } else {
                        // tree is getting bigger so make the tree
                        // bigger first and then make the columns wider
                        // to match the client area width
                        tree.setSize(area.width, area.height);
                        column2.getColumn().setWidth(Math.max(0,
                                width - column0.getColumn().getWidth() - column1.getColumn().getWidth() - 10));
                    }
                }
            });
        }

        fViewer.setContentProvider(new ContentProvider());
        fViewer.setInput(fParser.getConfigWizardRoot());

        ColumnViewerToolTipSupport.enableFor(fViewer);
    }

    void setCursorAndRevealAt(int line) {
        try {
            int offset = fDocument.getLineOffset(line);
            editor.selectAndReveal(offset, 0);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void showTooltipText(IConfigWizardItem configItem) {
        if (configItem.getTooltip().isEmpty()) {
            fText.setText(""); //$NON-NLS-1$
            return;
        }
        String name = configItem.getName() + System.lineSeparator();
        String tooltip = configItem.getTooltip();
        fText.setText(name + tooltip);

        StyleRange styleRange = new StyleRange();
        styleRange.start = 0;
        styleRange.length = name.length();
        styleRange.fontStyle = SWT.BOLD;
        styleRange.foreground = ColorConstants.BLACK;
        fText.setStyleRange(styleRange);
    }

    @Override
    public IDocumentProvider getDocumentProvider() {
        return editor == null ? null : editor.getDocumentProvider();
    }

    @Override
    public void close(boolean save) {
        editor.close(save);
    }

    @Override
    public boolean isEditable() {
        return editor.isEditable();
    }

    @Override
    public void doRevertToSaved() {
        editor.doRevertToSaved();
    }

    @Override
    public void setAction(String actionID, IAction action) {
        editor.setAction(actionID, action);
    }

    @Override
    public IAction getAction(String actionId) {
        return editor.getAction(actionId);
    }

    @Override
    public void setActionActivationCode(String actionId, char activationCharacter, int activationKeyCode,
            int activationStateMask) {
        editor.setActionActivationCode(actionId, activationCharacter, activationKeyCode, activationStateMask);
    }

    @Override
    public void removeActionActivationCode(String actionId) {
        editor.removeActionActivationCode(actionId);
    }

    @Override
    public boolean showsHighlightRangeOnly() {
        return editor.showsHighlightRangeOnly();
    }

    @Override
    public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
        editor.showHighlightRangeOnly(showHighlightRangeOnly);
    }

    @Override
    public void setHighlightRange(int offset, int length, boolean moveCursor) {
        editor.setHighlightRange(offset, length, moveCursor);
    }

    @Override
    public IRegion getHighlightRange() {
        return editor.getHighlightRange();
    }

    @Override
    public void resetHighlightRange() {
        editor.resetHighlightRange();
    }

    @Override
    public ISelectionProvider getSelectionProvider() {
        return editor.getSelectionProvider();
    }

    @Override
    public void selectAndReveal(int offset, int length) {
        setActivePage(0);
        editor.selectAndReveal(offset, length);
    }

}
