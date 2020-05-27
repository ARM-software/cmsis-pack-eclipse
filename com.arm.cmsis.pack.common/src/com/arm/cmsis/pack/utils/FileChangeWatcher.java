/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;


/**
 *  Monitors file system for creation, change and deletion of given file(s)
 */
public abstract class FileChangeWatcher  {

	// flags for watch events (to isolate extender classes from StandardWatchEventKinds)
	public static final int CREATE = 1;
	public static final int MODIFY = 2;
	public static final int DELETE = 4;
	public static final int ALL = CREATE|MODIFY|DELETE;

	protected WatchService watcher;
	protected Map<WatchKey, Path> dirKeys; // we actually monitor directories
	protected Map<String, Long> filesToWatch; // with their modification times
    protected int watchFlags;

    protected WatchingThread thread;
    protected WatchEvent.Kind<?>[] watchEventKinds;

    protected Map<String, ActionTask> fScheduledTasks; 
    protected boolean containsWildcards = false; // flag indicating that filesToWatch contains entries with wildcards 
    

    class WatchingThread extends Thread  { 	// FileChageWatcher cannot extend Thread because it must be able to restart
		@Override
		public void run() {
			processWatchEvents();
		}
    }


    class ActionTask extends TimerTask  { 	// Class to make a delayed (1 sec) processing of change event to allow file system to complete 
    	String fFile;
    	int fKind;
    	ActionTask(String file, int kind) {
    		fFile = file;
    		fKind = kind;
    	}
    	
    	@Override
		public void run() {
    		removeScheduledTask(fFile); // first remove task from the collection allowing to reschedule new change
    		action(fFile, fKind); // then proceed with action 
		}
    }

    
	/**
	 * Default constructor, registers for all events
	 * @throws IOException
	 */
	protected FileChangeWatcher() throws IOException {
		this(ALL);
	}

	/**
	 * Main constructor, registers for specified events
	 * @param flags a combination of CREATE, MODIFY and DELETE flags. if 0 the behavior is undefined
	 * @throws IOException
	 */
	protected FileChangeWatcher(int flags)  {
		dirKeys = new HashMap<>();
		filesToWatch = new HashMap<>();
		watchFlags = flags;
		watchEventKinds = getEventKinds();
		fScheduledTasks = Collections.synchronizedMap(new HashMap<>());
		thread = null;

		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
			watcher = null;
		}
	}

	/**
	 *  Stops watch and clears all keys
	 */
	public void clearWatch() {
		stopWatch();
		filesToWatch.clear();
		containsWildcards = false;
		for(WatchKey key : dirKeys.keySet()) {
			key.cancel();
		}
		dirKeys.clear();
	}

	public synchronized void startWatch() {
		if(watcher == null) {
			return;
		}

		if(dirKeys.isEmpty()) {
			return;
		}

		if(thread != null && thread.isAlive()) {
			return;
		}
		thread = new WatchingThread();
		thread.start();
	}

	public synchronized void stopWatch() {
		if(thread != null && thread.isAlive()) {
			thread.interrupt();
		}
		thread = null;
	}

	/**
	 * Registers file for watching, the file does not need to exists if watcher monitors creation,
	 * @param file absolute filename to register for watching, wildcards are allowed in the name and extension
	 * @throws IOException
	 */
	public synchronized void registerFile(String file) {
		if(file == null || file.isEmpty()) {
			return;
		}

		if(filesToWatch.containsKey(file)) {
			return;
		}
		if(file.indexOf('*') >= 0 || file.indexOf('&') >= 0 )
			containsWildcards = true;
		
		File f = new File(file);
		long modified = 0;
		if(f.exists()) {
			modified = f.lastModified();
		}
		
		filesToWatch.put(file, modified);
		File parentDir = f.getParentFile();
		if(!parentDir.exists()) {
			parentDir.mkdirs();
		}
		registerDir(Paths.get(parentDir.getAbsolutePath()));
	}

	/**
	 * Removes file from watching.
	 * @param file absolute filename to stop watching, must be the same as passed to regiterFile()
	 * @throws IOException
	 */
	public synchronized void removeFile(String file) {
		if(!filesToWatch.containsKey(file)) {
			return;
		}
		filesToWatch.remove(file);
		containsWildcards = false;
		for(String f : filesToWatch.keySet()) {
			if(f.indexOf('*') >= 0 || f.indexOf('&') >= 0 ) {
				containsWildcards = true;
				break;
			}
		}
		
		String dir = Utils.extractPath(file, false);
		if(isDirToWatch(dir)) {
			return;
		}

		WatchKey key = getKey(dir);
		if(key != null) {
			dirKeys.remove(key);
			key.cancel();
		}
		if(dirKeys.isEmpty())
			clearWatch();
	}

	protected WatchKey getKey(String dir) {
		Path path = Paths.get(dir);
		if(path == null) {
			return null;
		}
		WatchKey key = null;
		for(Entry<WatchKey, Path> e : dirKeys.entrySet()) {
			Path p = e.getValue();
			if(path.equals(p)) {
				return e.getKey();
			}
		}
		return key;
	}

	protected synchronized void registerDir(Path path){
		if(dirKeys.containsValue(path)) {
			return;
		}
		if(watcher == null) {
			return;
		}

		WatchKey key;
		try {
			key = path.register(watcher, watchEventKinds);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		dirKeys.put(key, path);
	}

	protected WatchEvent.Kind<?>[] getEventKinds() {
		List<WatchEvent.Kind<Path> > eventList = new LinkedList<>();
		if((watchFlags & CREATE) == CREATE) {
			eventList.add(StandardWatchEventKinds.ENTRY_CREATE);
		}
		if((watchFlags & MODIFY) == MODIFY) {
			eventList.add(StandardWatchEventKinds.ENTRY_MODIFY);
		}
		if((watchFlags & DELETE) == DELETE) {
			eventList.add(StandardWatchEventKinds.ENTRY_DELETE);
		}

		return eventList.toArray(new WatchEvent.Kind<?>[0]);
	}

	public static int eventKindToInt(Kind<?> kind) {
		if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
			return CREATE;
		} else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
			return MODIFY;
		} else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
			return DELETE;
		}
		return 0;
	}


	/**
	 * Checks if directory is registered for watching
	 * @param dir absolute directory pathname
	 * @return true if to watch
	 */
	public boolean isDirToWatch(String dir) {
		for(String file : filesToWatch.keySet()) {
			if(file.startsWith(dir)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a file is registered for watching
	 * @param file absolute filename, wildcards are allowed in the name segment
	 * @return 0 if file not watched, 1 if watched explicitly, -1 if implicitly via wildcards
	 */
	public int isFileToWatch(String file) {
		if(filesToWatch.containsKey(file)) {
			return 1;
		}

		for(String f: filesToWatch.keySet()) {
			if(WildCards.match(file, f)) {
				return -1;
			}
		}
		return 0;
	}


	protected void processWatchEvents() {

		while(true) {
	        // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
            	Thread.currentThread().interrupt();
                return;
            }

            Path dir = dirKeys.get(key);
            if (dir == null) {
                continue; // not our directory
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                Kind<?> eventKind = event.kind();

                int kind = eventKindToInt(eventKind);
                if((kind & watchFlags) == 0) {
					continue; // not interested, ignore it
				}

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>)event;

                Path name = ev.context();

                String file = dir.toString().replace('\\', '/') + '/'+ name;
                int watched = isFileToWatch(file); 
                if(watched == 0 ) {
					continue;
				}
                long diff = 0;
                if(watched == 1) {
                    long lastModified = 0;
                	File f = new File(file);
                	if(kind != DELETE) {
                		lastModified = f.lastModified();
                	}
                	if(lastModified > 0) {
                    	// check if modification time is changed, only diff > 100 usec is processed
                		diff = lastModified - filesToWatch.get(file); 
                		filesToWatch.put(file, lastModified); // anyway update the entry
                	}
                }
                boolean changed = kind != MODIFY || diff > 100L || watched < 0; 

                if(changed && key.isValid()) {
                	scheduleTask(file, kind);
				}
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                dirKeys.remove(key);
                // all directories are inaccessible
                if (dirKeys.isEmpty()) {
                	break;
                }
            }
        }
	}

	protected void scheduleTask(String file, int kind) {
    	ActionTask task = getScheduledTask(file);
    	if(task != null) {
    		task.cancel();
    	} 
    	task = new ActionTask(file, kind);
		synchronized(fScheduledTasks) {
			fScheduledTasks.put(file, task);
		}
		Timer timer = new Timer();
		timer.schedule(task, 1000);  // schedule update with 1 second delay to allow file system to complete write
	}

	protected ActionTask getScheduledTask(String file) {
		ActionTask t = null;
		synchronized(fScheduledTasks) {
			t = fScheduledTasks.get(file);
		}
		return t;
	}

	protected void removeScheduledTask(String file) {
		synchronized(fScheduledTasks) {
			fScheduledTasks.remove(file);
		}
	}

	
	/**
	 * Executes an action on file change
	 * @param file absolute filename
	 * @param kind one of CREATE, MODIFIED or DELETE values
	 */
	protected abstract void action(String file, int kind);

	/**
	 * Sets file' last modified time to the current system tyme
	 * @param file absolute filename
	 */
	public static void touchFile(String file) {
		if(file == null || file.isEmpty()) {
			return;
		}
		Path path = Paths.get(file);
		try {
			Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));
		} catch (IOException e) {
			// do nothing
		}
	}
	
	public static void createDirectories(String dir) throws IOException {
		if(dir == null || dir.isEmpty()) {
			return;
		}
		Path path = Paths.get(dir);
		Files.createDirectories(path);
	}
	
}
