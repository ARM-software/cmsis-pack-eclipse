/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
* 
* Resource change listener snippet is taken from: 
* https://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
* *******************************************************************************/

package com.arm.cmsis.pack.project;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.events.IRteEventListener;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;

/**
 * Class that manages RTE projects and their associations to ICproject and IProject 
 */
public class RteProjectManager extends RteEventProxy implements IRteEventListener, IResourceChangeListener{

	private RteSetupParticipant rteSetupParticipant = null;
	private Map<String, IRteProject> rteProjects = Collections.synchronizedMap(new HashMap<String, IRteProject>());
	
	/**
	 *  Default constructor
	 */
	public RteProjectManager() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		CpPlugIn.addRteListener(this);
	}

	/**
	 *  Clears internal collection of the projects 
	 */
	public void destroy() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
		CpPlugIn.removeRteListener(this);
		synchronized (rteProjects) { // do it as atomic operation
			for(IRteProject rteProject : rteProjects.values()) {
				rteProject.destroy();
			}
			rteProjects.clear();
		}
	}
	
	/**
	 *  Initializes RteSetupParticipant does nothing if already initialized  
	 */
	public void initRteSetupParticipant() {
		if(rteSetupParticipant == null)
			rteSetupParticipant = new RteSetupParticipant();
	}
	
	/**
	 * Triggers project index update and notifies that project is updated
	 * @param project IProject associated with an RTE project
	 */
	public void updateIndex(IProject project) {
		if(rteSetupParticipant != null) {
			rteSetupParticipant.updateIndex(project);
		}
		emitRteEvent(RteEvent.PROJECT_UPDATED, getRteProject(project));
	}
	
	/**
	 * Returns IRteProject associated for given name  
	 * @param project IProject object associated with IRteProject 
	 * @return IRteProject
	 */
	synchronized public IRteProject getRteProject(String name) {
		return rteProjects.get(name);
	}
	
	
	/**
	 * Returns IRteProject associated with given IRteProject if any  
	 * @param project IProject object associated with IRteProject 
	 * @return IRteProject
	 */
	public IRteProject getRteProject(IProject project) {
		if(project != null)
			return getRteProject(project.getName());
		return null;
	}
	
	/**
	 * Creates or returns existing IRteProject associated with given IRteProject
	 * @param project IProject object to be associated with IRteProject
	 * @return existing IRteProject if exists or new one
	 */
	synchronized public IRteProject createRteProject(IProject project) {
	IRteProject rteProject = getRteProject(project);
		if(rteProject == null) {
			rteProject = new RteProject(project);
			addRteProject(rteProject);
		}
		return rteProject;
	}
	
	
	/**
	 * Adds RTE project to the internal collection 
	 * @param rteProject IRteProject to add  
	 */
	synchronized public void addRteProject(IRteProject rteProject) {
		if(rteProject != null) {
			rteProjects.put(rteProject.getName(), rteProject);
			emitRteEvent(RteEvent.PROJECT_ADDED, rteProject);
		}
	}
	
	/**
	 * Removes RTE project from internal collection
	 * @param rteProject IRteProject to remove 
	 */
	synchronized public void deleteRteProject(IRteProject rteProject) {
		if(rteProject != null) {
			rteProjects.remove(rteProject.getName());
			rteProject.destroy();
			emitRteEvent(RteEvent.PROJECT_REMOVED, rteProject);
		}
	}
	
	
	/**
	 * Renames RTE project and updates collection
	 * @param rteProject IRteProject to remove 
	 */
	public void renameRteProject(String oldName, String newName) {
		IRteProject rteProject = getRteProject(oldName);
		if(rteProject != null) {
			rteProject.setName(newName);
			synchronized(rteProjects) { // do it as atomic operation
				rteProjects.remove(oldName);
				rteProjects.put(newName, rteProject);
				emitRteEvent(RteEvent.PROJECT_UPDATED, rteProject);
			}
		}
	}

	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().equals(RteEvent.PACKS_RELOADED)){
			synchronized(rteProjects) {
				for(IRteProject rteProject : rteProjects.values())
					rteProject.reload();
			}
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		 // consider only POST_CHANGE events
        if (event.getType() != IResourceChangeEvent.POST_CHANGE)
           return;
        IResourceDelta resourseDelta = event.getDelta();
        IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {
           public boolean visit(IResourceDelta delta) {
              IResource resource = delta.getResource();
              int type = resource.getType();
              if(type == IResource.ROOT)
            	  return true; // workspace => visit children  

              IProject project = resource.getProject();

              int kind = delta.getKind();
        	  int flags = delta.getFlags();

        	  if(type == IResource.PROJECT && kind == IResourceDelta.REMOVED) {
    			  IRteProject rteProject = getRteProject(project);
    			  if(rteProject == null)
    				  return false; // not an RTE project or not loaded => ignore
        		  if((flags & IResourceDelta.MOVED_TO) == IResourceDelta.MOVED_TO) {
        			  // renamed
        			  IPath newPath = delta.getMovedToPath();
        			  String newName = newPath.lastSegment();
        			  renameRteProject(project.getName(), newName);
        			  return false;
        		  } 
        		  // removed
        		  deleteRteProject(rteProject);
        		  return false;
        	  }
              
              // only consider RTE projects 
              if(!RteProjectNature.hasRteNature(project))
            	  return false; // skip children
              
              if(type == IResource.PROJECT) {
            	  // is project renamed?
            	  if (kind == IResourceDelta.REMOVED) {
            		  if((flags & IResourceDelta.CHANGED )== 1) {
            			  return false;
            		  } else if((flags & IResourceDelta.MOVED_TO) == 1) {
            			  return false;
            		  }
            		  return true;
            	  }
            	  
              } else if (type == IResource.FILE) {
                  // is resource changed? 
                  if (kind != IResourceDelta.CHANGED)
                     return true; 
                  
                  // is content changed?
            	  if ((flags & IResourceDelta.CONTENT) == 0)
            		  return true;
            	  // check only RTE configuration files with ".rteconfig" extension  
            	  if (CmsisConstants.RTECONFIG.equalsIgnoreCase(resource.getFileExtension())) { 
            		  IRteProject rteProject = getRteProject(project);
            		  if(rteProject != null){
            			  String relName = resource.getProjectRelativePath().toString();
            			  if(relName.equals(rteProject.getRteConfigurationName()))
            				  rteProject.reload();
            		  }
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

}
