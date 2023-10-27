/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.MalformedParametersException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpGenerator;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;

/**
 *
 */
public class LaunchGenerator {

    protected ICpGenerator fGenerator;
    protected ICpConfigurationInfo fConfigInfo;
    protected String fType;

    /**
     * Constructor for a launch generator
     *
     * @param generator
     * @param configuration
     */
    public LaunchGenerator(ICpGenerator generator, ICpConfigurationInfo configInfo) {
        fGenerator = generator;
        fConfigInfo = configInfo;
    }

    protected static LaunchGenerator getInstance(ICpGenerator generator, ICpConfigurationInfo configInfo) {
        LaunchGenerator launchGenerator = null;
        ICpEnvironmentProvider ep = CpPlugIn.getEnvironmentProvider();
        if (ep != null) {
            launchGenerator = ep.getAdapter(LaunchGenerator.class);
        }
        if (launchGenerator == null) {
            // use default implementation
            launchGenerator = new LaunchGenerator(generator, configInfo);
        }
        return launchGenerator;
    }

    /**
     * Launches first implemented generator type: eclipse, exe or web
     *
     * @param generator  {@link ICpGenerator} to launch
     * @param configInfo {@link ICpConfigurationInfo} to expand argument strings
     */
    public static void launch(ICpGenerator generator, ICpConfigurationInfo configInfo) {
        launch(generator, configInfo, null);
    }

    /**
     * Launches generator of given type: eclipse, exe or web
     *
     * @param generator  {@link ICpGenerator} to launch
     * @param type       one of eclipse, exe or web
     * @param configInfo {@link ICpConfigurationInfo} to expand argument strings
     */
    public static void launch(ICpGenerator generator, ICpConfigurationInfo configInfo, String type) {
        LaunchGenerator launchGenerator = getInstance(generator, configInfo);
        launchGenerator.launch(type);
    }

    /**
     * Launches generator of given type
     *
     * @param type generator type to launch, if null tries to launch first
     *             successful
     */
    protected void launch(String type) {
        for (String launchType : CmsisConstants.LAUNCH_TYPES) {
            if (type != null && !launchType.equals(type))
                continue;
            ICpItem command = fGenerator.getCommand(launchType);
            if (command == null)
                continue;
            try {
                switch (launchType) {
                case CmsisConstants.ECLIPSE:
                    launchJava();
                    return;
                case CmsisConstants.EXE:
                    launchExe();
                    return;
                case CmsisConstants.WEB:
                    launchWeb();
                    return;
                }
            } catch (ClassNotFoundException | MalformedParametersException | SecurityException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                CpPlugIn.getDefault().emitRteEvent(RteEvent.GPDSC_LAUNCH_ERROR, e.getMessage());
                continue;
            } catch (NoSuchMethodException e) {
                CpPlugIn.getDefault().emitRteEvent(RteEvent.GPDSC_LAUNCH_ERROR,
                        NLS.bind(CpStringsUI.LaunchGenerator_MethodNotFound, e.getMessage()));
            } catch (IOException e) {
                CpPlugIn.getDefault().emitRteEvent(RteEvent.GPDSC_LAUNCH_ERROR, e.getMessage());
            }
        }
    }

    protected void launchExe() throws IOException {
        ICpItem commandItem = fGenerator.getCommand(CmsisConstants.EXE);
        Collection<ICpItem> argItems = fGenerator.getArguments(CmsisConstants.EXE);
        ICpEnvironmentProvider ep = CpPlugIn.getEnvironmentProvider();
        String command = ep.expandString(commandItem.getText(), fConfigInfo, true);
        File file = new File(command);
        if (!file.isAbsolute()) {
            command = commandItem.getAbsolutePath(command);
        }

        List<String> args = new ArrayList<>();
        args.add(command);

        for (ICpItem argItem : argItems) {
            String arg = argItem.getAttribute(CmsisConstants.SWITCH)
                    + ep.expandString(argItem.getText(), fConfigInfo, true);
            args.add(arg);
        }
        ProcessBuilder pb = new ProcessBuilder(args);
        String wkDir = ep.expandString(fGenerator.getWorkingDir(), fConfigInfo, true);
        if (wkDir != null) {
            // ensure it working directory exists
            File workDir = new File(wkDir);
            workDir.mkdirs();
            pb.directory(workDir);
        }
        pb.start();
    }

    protected void launchWeb() throws MalformedParametersException {
        ICpItem commandItem = fGenerator.getCommand(CmsisConstants.WEB);
        Collection<ICpItem> argItems = fGenerator.getArguments(CmsisConstants.WEB);

        String command = commandItem.getUrl();
        StringBuilder url = new StringBuilder(command);

        if (argItems != null && !argItems.isEmpty()) {
            ICpEnvironmentProvider ep = CpPlugIn.getEnvironmentProvider();
            char delimiter = '?'; // first delimiter after URL base
            for (ICpItem argItem : argItems) {
                String key = argItem.getAttribute(CmsisConstants.SWITCH);
                if (key.isEmpty()) {
                    throw new MalformedParametersException(CpStringsUI.LaunchGenerator_UrlMustHaveSwitch);
                }
                url.append(delimiter);
                url.append(key);
                url.append('=');
                String arg = ep.expandString(argItem.getText(), fConfigInfo, true);
                url.append(arg);
                delimiter = '&'; // delimiter between arguments
            }
        }
        OpenURL.open(url.toString());
    }

    protected void launchJava() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedParametersException {
        ICpItem commandItem = fGenerator.getCommand(CmsisConstants.ECLIPSE);
        Collection<ICpItem> argItems = fGenerator.getArguments(CmsisConstants.ECLIPSE);

        String pluginId = commandItem.getAttribute(CmsisConstants.PLUGIN);
        Bundle bundle = pluginId.isEmpty() ? null : Platform.getBundle(pluginId);
        if (bundle == null) {
            throw new ClassNotFoundException(NLS.bind(CpStringsUI.LaunchGenerator_PluginNotFound, pluginId));
        }

        String className = commandItem.getAttribute(CmsisConstants.CLASS);
        Class<?> clazz = className.isEmpty() ? null : bundle.loadClass(className);
        if (clazz == null) {
            throw new ClassNotFoundException(NLS.bind(CpStringsUI.LaunchGenerator_ClassNotFound, className));
        }

        Collection<Class<String>> paraTypes = new ArrayList<Class<String>>();
        for (int i = 0; i < argItems.size(); i++) {
            paraTypes.add(String.class);
        }

        String methodName = commandItem.getAttribute(CmsisConstants.METHOD);
        Method method = methodName.isEmpty() ? null
                : clazz.getMethod(methodName, paraTypes.toArray(new Class<?>[paraTypes.size()]));
        if (method == null) {
            throw new ClassNotFoundException(
                    NLS.bind(CpStringsUI.LaunchGenerator_MethodNotFound, methodName, className));
        }

        // check modifiers of the method
        if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())
                || !method.getReturnType().equals(Void.TYPE)) {
            String message = method.toGenericString();
            throw new NoSuchMethodException(message.substring(message.lastIndexOf(' ') + 1));
        }

        ICpEnvironmentProvider ep = CpPlugIn.getEnvironmentProvider();
        Collection<String> params = new ArrayList<String>();
        for (ICpItem argItem : argItems) {
            params.add(ep.expandString(argItem.getText(), fConfigInfo, true));
        }
        method.invoke(clazz, params.toArray());
    }

}
