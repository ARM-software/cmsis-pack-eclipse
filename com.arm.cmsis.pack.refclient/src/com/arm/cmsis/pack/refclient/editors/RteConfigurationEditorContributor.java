package com.arm.cmsis.pack.refclient.editors;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.refclient.RefClient;
import com.arm.cmsis.pack.ui.CpPlugInUI;

/**
 * Manages the installation/deinstallation of global actions for RTE Configuration editor
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
public class RteConfigurationEditorContributor extends MultiPageEditorActionBarContributor {
	private IEditorPart activeEditorPart;
	private Action resolveAction;
	IRteConfigurationProxy activeConfig = null;
	/**
	 * Creates a multi-page contributor.
	 */
	public RteConfigurationEditorContributor() {
		super();
		createActions();
	}
	/**
	 * Returns the action registered with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}
	
	
	@Override
	public void setActiveEditor(IEditorPart part) {
		if(part instanceof RteConfigurationEditor)  {
			activeConfig = ((RteConfigurationEditor)part).getConfiguration();
		} else {
			activeConfig = null;			
		}
		RefClient.getDefault().setActiveRteConfiguration(activeConfig);
		
		super.setActiveEditor(part);
	}

	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {

			ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part : null;

			actionBars.setGlobalActionHandler(
				ActionFactory.DELETE.getId(),
				getAction(editor, ITextEditorActionConstants.DELETE));
			actionBars.setGlobalActionHandler(
				ActionFactory.UNDO.getId(),
				getAction(editor, ITextEditorActionConstants.UNDO));
			actionBars.setGlobalActionHandler(
				ActionFactory.REDO.getId(),
				getAction(editor, ITextEditorActionConstants.REDO));
			actionBars.setGlobalActionHandler(
				ActionFactory.CUT.getId(),
				getAction(editor, ITextEditorActionConstants.CUT));
			actionBars.setGlobalActionHandler(
				ActionFactory.COPY.getId(),
				getAction(editor, ITextEditorActionConstants.COPY));
			actionBars.setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				getAction(editor, ITextEditorActionConstants.PASTE));
			actionBars.setGlobalActionHandler(
				ActionFactory.SELECT_ALL.getId(),
				getAction(editor, ITextEditorActionConstants.SELECT_ALL));
			actionBars.setGlobalActionHandler(
				ActionFactory.FIND.getId(),
				getAction(editor, ITextEditorActionConstants.FIND));
			actionBars.setGlobalActionHandler(
				IDEActionFactory.BOOKMARK.getId(),
				getAction(editor, IDEActionFactory.BOOKMARK.getId()));
			actionBars.updateActionBars();
		}
	}
	private void createActions() {
		resolveAction = new Action() {
			public void run() {
				if(activeConfig != null){
					activeConfig.resolveDependencies();
				}
			}
		};
		resolveAction.setText("Resolve Component Dependencies");
		resolveAction.setToolTipText("Resolve component dependencies");
		resolveAction.setImageDescriptor(CpPlugInUI.getImageDescriptor(CpPlugInUI.ICON_RESOLVED));
	}
	public void contributeToMenu(IMenuManager manager) {
		IMenuManager menu = new MenuManager("RTE");
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
		menu.add(resolveAction);
	}
	public void contributeToToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		manager.add(resolveAction);
	}
}
