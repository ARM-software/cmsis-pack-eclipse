package com.arm.cmsis.pack.project.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;

/**
 * The handler responsible for deleting excluded RTE config files
 */
public class CleanupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection sel = (IStructuredSelection) selection;
		for (Object obj : sel.toArray()) {
			IRteProject project = CpProjectPlugIn.getRteProjectManager().getRteProject((IProject) obj);
			if (project == null) {
				continue;
			}
			project.cleanup();
		}

		return null;
	}

}
