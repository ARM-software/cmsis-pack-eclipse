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

package com.arm.cmsis.pack.project;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IndexerSetupParticipant;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.arm.cmsis.pack.project.utils.ProjectUtils;

/**
 * This class handles project creation event in order to apply RTE configuration
 * data to loaded project.<br>
 * It also postpones project indexing until RTE data applied to the project.
 */
public class RteSetupParticipant extends IndexerSetupParticipant implements ICProjectDescriptionListener {

    /**
     * Default constructor that registers this as IndexerSetupParticipant and
     * ICProjectDescriptionListener
     */
    public RteSetupParticipant() {
        ICProjectDescriptionManager descManager = CCorePlugin.getDefault().getProjectDescriptionManager();
        if (descManager != null) {
            descManager.addCProjectDescriptionListener(this, CProjectDescriptionEvent.ALL);
        }

        IIndexManager indexManager = CCorePlugin.getIndexManager();
        if (indexManager != null) {
            indexManager.addIndexerSetupParticipant(this);
        }
    }

    @Override
    public void handleEvent(CProjectDescriptionEvent event) {
        if (event.getEventType() == CProjectDescriptionEvent.LOADED) {
            IProject project = event.getProject();
            if (!RteProjectNature.hasRteNature(project))
                return;

            RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
            IRteProject rteProject = rteProjectManager.createRteProject(project);
            try {
                rteProject.load();
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    // IndexerSetupParticipant overridden methods
    @Override
    public boolean postponeIndexerSetup(ICProject cproject) {
        IProject project = cproject.getProject();
        if (!RteProjectNature.hasRteNature(project))
            return false;

        RteProjectManager rteProjectManager = CpProjectPlugIn.getRteProjectManager();
        if (rteProjectManager.isPostponeRefresh())
            return true;
        IRteProject rteProject = rteProjectManager.getRteProject(project);
        if (rteProject == null || !rteProject.isUpdateCompleted()) {
            return true; // postpone indexer until RTE data is loaded and updated
        }
        return false;
    }

    public void updateIndex(IProject project) {
        ICProject cproject = ProjectUtils.getCProject(project);
        if (cproject != null) {
            notifyIndexerSetup(cproject);
        }
    }

}
