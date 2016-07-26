package com.arm.cmsis.pack.project.ui.handlers;

import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.project.CpProjectPlugIn;
import com.arm.cmsis.pack.project.IRteProject;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.RteProjectStorage;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.utils.Utils;

public class UpdateConfigFileHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection sel = (IStructuredSelection) selection;
		for (Object obj : sel.toArray()) {

			IFile file = ProjectUtils.getRteFileResource(obj);
			String dstFile = file.getProjectRelativePath().toString();

			ICpFileInfo fi = ProjectUtils.getCpFileInfo(file);
			ICpFile f = fi.getFile();
			String srcFile = fi.getAbsolutePath(f.getName());

			EFileRole role = fi.getRole();
			if (role == EFileRole.CONFIG || role == EFileRole.COPY) {
				int index = -1;
				EFileCategory cat = fi.getCategory();
				if (cat.isHeader() || cat.isSource()) {
					String baseSrc = Utils.extractBaseFileName(srcFile);
					String baseDst = Utils.extractBaseFileName(dstFile);
					int len = baseSrc.length() + 1;
					if (baseDst.length() > len) {
						String instance = baseDst.substring(len);
						try {
							index = Integer.decode(instance);
						} catch (NumberFormatException e) {
							// do nothing, use -1
						}
					}
				}
				try {
					int bCopied = ProjectUtils.copyFile(file.getProject(), srcFile, dstFile, index, null, true);
					if (bCopied == 1) {
						// do the version update and save it in the .cproject file
						fi.setVersion(f.getVersion());
						IRteProject rteProject = CpProjectPlugIn.getRteProjectManager()
								.getRteProject(file.getProject());
						RteProjectStorage projectStorage = rteProject.getProjectStorage();
						projectStorage.setConfigFileVersion(dstFile, f.getVersion());
						projectStorage.save(CoreModel.getDefault().getProjectDescription(file.getProject()));
						rteProject.save();
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		ISelection selection = selectionService.getSelection("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.size() == 1) {
				Object obj = sel.getFirstElement();
				IFile file = ProjectUtils.getRteFileResource(obj);
				ICpFileInfo fi = ProjectUtils.getCpFileInfo(file);
				if (fi == null || fi.getFile() == null) {
					return;
				}
				int versionDiff = fi.getVersionDiff();
				String versionText = " (" + fi.getVersion() + " -> " + fi.getFile().getVersion() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (versionDiff < 0) {
					element.setText(Messages.UpdateConfigFileHandler_Upgrade + file.getName() + versionText);
				} else if (versionDiff > 2) {
					element.setText(Messages.UpdateConfigFileHandler_Downgrade + file.getName() + versionText);
				}
			} else if (sel.size() > 1) {
				element.setText(Messages.UpdateConfigFileHandler_UpdateSelectedFiles);
			}
		}
	}

}
