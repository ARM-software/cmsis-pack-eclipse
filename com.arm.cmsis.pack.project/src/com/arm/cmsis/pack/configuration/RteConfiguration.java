/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.PlatformObject;

import com.arm.cmsis.pack.build.BuildSettings;
import com.arm.cmsis.pack.build.IBuildSettings;
import com.arm.cmsis.pack.build.IMemorySettings;
import com.arm.cmsis.pack.build.MemorySettings;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpCodeTemplate;
import com.arm.cmsis.pack.data.ICpCodeTemplate;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDebugVars;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EFileCategory;
import com.arm.cmsis.pack.enums.EFileRole;
import com.arm.cmsis.pack.info.CpDebugVarsInfo;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpFileInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.project.CpVariableResolver;
import com.arm.cmsis.pack.project.Messages;
import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.pack.project.utils.RtePathComparator;
import com.arm.cmsis.pack.rte.IRteModel;
import com.arm.cmsis.pack.rte.RteModel;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of IRteConfiguration interface
 */
public class RteConfiguration extends PlatformObject implements IRteConfiguration {

    protected IRteModel fModel = null; // underlying model that is source of information
    protected ICpConfigurationInfo fConfigInfo = null; // meta-information that is stored .rteconfig file and used to
                                                       // transfer information to/from model

    protected IBuildSettings rteBuildSettings = new BuildSettings();

    protected IMemorySettings fMemorySettings = null;

    // source files included in project: project relative path -> absPath
    protected Map<String, ICpFileInfo> fProjectFiles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    // root of the code template hierarchy
    protected ICpCodeTemplate fCodeTemplateRoot = new CpCodeTemplate(null);

    // scvd files for component viewer: project relative path -> absPath
    protected Map<String, ICpFileInfo> fScvdFiles = new HashMap<>();

    // paths to library sources (for debugger)
    protected Set<String> libSourcePaths = new TreeSet<>(new RtePathComparator());

    // pieces of code put into RTE_Components.h file
    protected Set<String> rteComponentsH = new TreeSet<>();

    // pieces of code put into RTE_Components.h file
    protected Set<String> globalPreIncludeStrings = new TreeSet<>();

    // pieces of code put into local pre-includes (include name to text )
    protected Map<String, String> localPreIncludeStrings = new TreeMap<>(new AlnumComparator(false));

    // header -> comment (for editor)
    protected Map<String, String> headers = new TreeMap<>(new AlnumComparator(false));

    // documentation files relevant to configuration
    protected Map<String, String> docs = new TreeMap<>(new AlnumComparator(false));

    protected String svdFile = null;

    protected ICpComponentInfo deviceStartupComponent = null;
    protected ICpComponentInfo cmsisCoreComponent = null;
    protected ICpComponentInfo cmsisRtosComponent = null;
    // device header name without path
    protected String deviceHeader = null;

    protected Collection<ICpPackInfo> fMissingPacks = new HashSet<ICpPackInfo>();

    boolean valid = true; // flag that indicates that device and all required components are resolved

    public RteConfiguration() {
        // does nothing
    }

    protected void clear() {
        rteBuildSettings.clear();
        fProjectFiles.clear();
        libSourcePaths.clear();

        rteComponentsH.clear();
        globalPreIncludeStrings.clear();
        localPreIncludeStrings.clear();

        headers.clear();
        docs.clear();
        deviceHeader = null;
        deviceStartupComponent = null;
        cmsisCoreComponent = null;
        cmsisRtosComponent = null;
        valid = true;

        fMissingPacks.clear();
        fScvdFiles.clear();
    }

    @Override
    public ICpConfigurationInfo getConfigurationInfo() {
        return fConfigInfo;
    }

    @Override
    public IRteModel getRteModel() {
        return fModel;
    }

    @Override
    public ICpDeviceInfo getDeviceInfo() {
        return fConfigInfo != null ? fConfigInfo.getDeviceInfo() : null;
    }

    @Override
    public ICpDebugConfiguration getDebugConfiguration() {
        ICpDeviceInfo di = getDeviceInfo();
        if (di != null) {
            return di.getDebugConfiguration();
        }
        return null;
    }

    @Override
    public ICpPack getDfp() {
        if (fConfigInfo == null) {
            return null;
        }
        return fConfigInfo.getPack();
    }

    @Override
    public String getDfpPath() {
        if (fConfigInfo == null) {
            return null;
        }
        return fConfigInfo.getDfpPath();
    }

    @Override
    public IBuildSettings getBuildSettings() {
        return rteBuildSettings;
    }

    @Override
    public Map<String, ICpFileInfo> getProjectFiles() {
        return fProjectFiles;
    }

    @Override
    public ICpFileInfo getProjectFileInfo(String fileName) {
        return fProjectFiles.get(fileName);
    }

    @Override
    public ICpFileInfo[] getProjectFileInfos(String fileName) {
        Collection<ICpFileInfo> fileInfos = new LinkedList<>();
        for (Entry<String, ICpFileInfo> e : fProjectFiles.entrySet()) {
            if (e.getKey().matches(fileName)) {
                fileInfos.add(e.getValue());
            }
        }
        ICpFileInfo[] infos = new ICpFileInfo[fileInfos.size()];
        return fileInfos.toArray(infos);
    }

    @Override
    public Collection<String> getLibSourcePaths() {
        return libSourcePaths;
    }

    @Override
    public Collection<String> getRteComponentsHCode() {
        return rteComponentsH;
    }

    @Override
    public Collection<String> getGlobalPreIncludeStrings() {
        return globalPreIncludeStrings;
    }

    @Override
    public Map<String, String> getLocalPreIncludeStrings() {
        return localPreIncludeStrings;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, String> getDocs() {
        return docs;
    }

    @Override
    public String getDeviceHeader() {
        return deviceHeader;
    }

    @Override
    public String getSvdFile() {
        return svdFile;
    }

    @Override
    public ICpComponentInfo getDeviceStartupComponent() {
        return deviceStartupComponent;
    }

    @Override
    public ICpComponentInfo getCmsisCoreComponent() {
        return cmsisCoreComponent;
    }

    @Override
    public ICpComponentInfo getCmsisRtosComponent() {
        return cmsisRtosComponent;
    }

    @Override
    public ICpCodeTemplate getCmsisCodeTemplate() {
        return fCodeTemplateRoot;
    }

    @Override
    public Map<String, ICpFileInfo> getScvdFiles() {
        return fScvdFiles;
    }

    @Override
    public void setConfigurationInfo(ICpConfigurationInfo info) {
        if (fConfigInfo == info) {
            return;
        }
        fConfigInfo = info;
        if (fModel == null && fConfigInfo != null) {
            fModel = new RteModel();
        }
        if (fModel != null) {
            fModel.setConfigurationInfo(info);
            fConfigInfo = fModel.getConfigurationInfo();
        }
        collectSettings();
    }

    protected void collectSettings() {
        clear();
        if (getDeviceInfo() == null) {
            return;
        }
        // insert default settings
        rteBuildSettings.addStringListValue(IBuildSettings.RTE_INCLUDE_PATH, CmsisConstants.PROJECT_RTE_PATH);
        rteBuildSettings.addStringListValue(IBuildSettings.RTE_DEFINES, CmsisConstants._RTE_);
        headers.put(CmsisConstants.RTE_RTE_Components_h, Messages.RteConfiguration_ComponentSelection);

        ICpItem apisItem = fConfigInfo.getFirstChild(CmsisConstants.APIS_TAG);
        collectComponentSettings(apisItem);
        ICpItem componentsItem = fConfigInfo.getFirstChild(CmsisConstants.COMPONENTS_TAG);
        collectComponentSettings(componentsItem);

        collectDeviceSettings(getDeviceInfo());
    }

    protected void collectDeviceSettings(ICpDeviceInfo di) {
        ICpDeviceItem d = di.getDevice();
        if (d == null) {
            return;
        }

        rteBuildSettings.addAttributes(di.attributes()); // add device attributes
        ICpItem props = di.getEffectiveProperties();
        if (props == null) {
            return;
        }

        Collection<? extends ICpItem> children = props.getChildren();
        for (ICpItem p : children) {
            String tag = p.getTag();

            if (p instanceof ICpDebugVars) {
                ICpDebugVars dv = (ICpDebugVars) p;
                CpDebugVarsInfo dvi = new CpDebugVarsInfo(di, dv);
                dv.replaceChild(dvi);
                collectFile(dvi, null, -1);
                continue;
            }
            if (tag.equals(CmsisConstants.COMPILE_TAG)) {
                String define = p.getAttribute(CmsisConstants.DEFINE);
                rteBuildSettings.addStringListValue(IBuildSettings.RTE_DEFINES, define);
                String pdefine = p.getAttribute(CmsisConstants.PDEFINE);
                rteBuildSettings.addStringListValue(IBuildSettings.RTE_DEFINES, pdefine);

                String header = p.getAttribute(CmsisConstants.HEADER);
                if (header != null && !header.isEmpty()) {
                    deviceHeader = Utils.extractFileName(header);
                    // check if header is already defined via device startup component
                    if (!headers.containsKey(deviceHeader)) {
                        addFile(deviceHeader, EFileCategory.HEADER, Messages.RteConfiguration_DeviceHeader, null);
                        header = ProjectUtils.removeLastPathSegment(p.getAbsolutePath(header));
                        header = CpVariableResolver.insertCmsisRootVariable(header);
                        addFile(header, EFileCategory.INCLUDE, Messages.RteConfiguration_DeviceHeader, null);
                    }
                }
            }
        }
    }

    protected void collectComponentSettings(ICpItem componentsParent) {
        if (componentsParent == null) {
            return;
        }
        Collection<? extends ICpItem> components = componentsParent.getChildren();
        if (components == null || components.isEmpty()) {
            return;
        }
        for (ICpItem child : components) {
            if (child instanceof ICpComponentInfo) {
                collectComponentSettings((ICpComponentInfo) child);
            }
        }
    }

    protected void collectComponentSettings(ICpComponentInfo ci) {
        // collect specific components
        if (ci.isDeviceStartupComponent()) {
            deviceStartupComponent = ci;
            rteBuildSettings.setAttribute(CmsisConstants.Device_Startup, true);
        } else if (ci.isCmsisCoreComponent()) {
            cmsisCoreComponent = ci;
            rteBuildSettings.setAttribute(CmsisConstants.CMSIS_Core, true);
        } else if (ci.isCmsisRtosComponent()) {
            cmsisRtosComponent = ci;
            rteBuildSettings.setAttribute(CmsisConstants.CMSIS_RTOS, true);
        }
        ICpComponent c = ci.getComponent();
        int count = ci.getInstanceCount();
        if (c != null) {
            collectIncludeStrings(c, count);
        }
        collectFiles(ci);
    }

    protected void collectFiles(ICpComponentInfo ci) {
        Collection<? extends ICpItem> children = ci.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        boolean bMultiInstance = ci.isMultiInstance();
        int count = ci.getInstanceCount();
        // first collect local pre-includes if any
        for (ICpItem child : children) {
            if (!(child instanceof ICpFileInfo)) {
                continue;
            }
            ICpFileInfo fi = (ICpFileInfo) child;
            if (fi.getCategory() == EFileCategory.PRE_INCLUDE_LOCAL) {
                collectFile(fi, ci, -1);
            }
        }

        // collect other files
        for (ICpItem child : children) {
            if (!(child instanceof ICpFileInfo)) {
                continue;
            }
            ICpFileInfo fi = (ICpFileInfo) child;
            if (fi.getCategory() == EFileCategory.PRE_INCLUDE_LOCAL) {
                continue;
            }
            if (bMultiInstance && fi.getRole() == EFileRole.CONFIG) {
                for (int i = 0; i < count; i++) {
                    collectFile(fi, ci, i);
                }
            } else {
                collectFile(fi, ci, -1);
            }
        }
    }

    /**
     * Collects file to configuration
     *
     * @param fi    ICpFileInfo
     * @param ci    parent ICpComponentInfo
     * @param index for multi-instance components : instance index, for others -1
     */
    protected void collectFile(ICpFileInfo fi, ICpComponentInfo ci, int index) {
        String name = Utils.extractFileName(fi.getName());
        EFileCategory cat = fi.getCategory();
        ICpFile f = fi.getFile();
        String absPath = null;
        String effectivePath = null;
        if (f != null) {
            if (cat.isHeader()) {
                absPath = f.getFilePath(); // we only need path portion
                name = f.getFileName();
            } else {
                absPath = f.getAbsolutePath(fi.getName());
            }
        }

        EFileRole role = fi.getRole();
        if (isAddToProject(fi)) {
            String className = ci != null ? ci.getAttribute(CmsisConstants.CCLASS) : CmsisConstants.EMPTY_STRING;
            String deviceName = fConfigInfo.getDeviceInfo().getFullDeviceName();
            effectivePath = getPathRelativeToProject(fi, absPath, name, className, deviceName, index);
            if (effectivePath == null) {
                return;
            }
            fProjectFiles.put(effectivePath, fi);
            if (cat.isSource() && ci != null) {
                IBuildSettings componentSettings = rteBuildSettings.getBuildSettings(ci.getName());
                if (componentSettings != null) {
                    // simply create empty settings: it will inherit parent group
                    componentSettings.createBuildSettings(effectivePath, IBuildSettings.Level.FILE);
                }
            }
            if (fi.isGenerated() || (role != EFileRole.CONFIG && role != EFileRole.COPY)) {
                effectivePath = CpVariableResolver.insertCmsisRootVariable(absPath);
            } else if (cat == EFileCategory.HEADER) {
                int nSegmentsToRemove = Utils.getSegmentCount(name);
                effectivePath = Utils.removeTrailingPathSegments(effectivePath, nSegmentsToRemove);
            }
        } else {
            effectivePath = CpVariableResolver.insertCmsisRootVariable(absPath);
        }

        if (ci == null)
            return;

        if (cat == EFileCategory.LINKER_SCRIPT && !ci.isDeviceStartupComponent()) {
            return;
        }

        String componentName = ci.getName();

        if (cat == EFileCategory.HEADER) {
            addFile(name, cat, componentName, ci); // only adds header filename
            cat = EFileCategory.INCLUDE;
        }

        addFile(effectivePath, cat, componentName, ci);
        if (cat == EFileCategory.LIBRARY) {
            addLibrarySourcePaths(f);
        }

        collectCodeTemplates(fi, ci);
        collectScvdFile(fi);
    }

    /**
     * Collects file of code templates
     *
     * @param fi ICpFileInfo
     * @param ci parent ICpComponentInfo
     */
    protected void collectCodeTemplates(ICpFileInfo fi, ICpComponentInfo ci) {
        ICpPack pack = ci.getPack();
        if (fi.getRole() == EFileRole.TEMPLATE && pack != null && (pack.getPackState().isInstalledOrLocal())) {
            String className = ci.getAttribute(CmsisConstants.CCLASS);
            ICpCodeTemplate component = (ICpCodeTemplate) fCodeTemplateRoot.getFirstChild(className);
            if (component == null) {
                component = new CpCodeTemplate(fCodeTemplateRoot, className, ci);
                fCodeTemplateRoot.addChild(component);
            }
            String selectName = fi.getAttribute(CmsisConstants.SELECT);
            ICpCodeTemplate codeTemplate = (ICpCodeTemplate) component.getFirstChild(selectName);
            if (codeTemplate == null) {
                codeTemplate = new CpCodeTemplate(component, selectName, fi);
                component.addChild(codeTemplate);
            }
            codeTemplate.addCodeTemplate(fi.getAttribute(CmsisConstants.NAME));
        }

    }

    /**
     * Collects scvd files for component viewer
     *
     * @param fi ICpFileInfo
     */
    protected void collectScvdFile(ICpFileInfo fi) {
        ICpPack pack = fi.getPack();
        if (fi.getCategory() == EFileCategory.OTHER && fi.getName().endsWith(CmsisConstants.EXT_SCVD) && pack != null
                && pack.getPackState().isInstalledOrLocal()) {
            fScvdFiles.put(pack.getAbsolutePath(fi.getName()), fi);
        }
    }

    /**
     * Adds {@link CmsisConstants#PROJECT_LOCAL_PATH} prefix if path is relative
     *
     * @param path path to adjust
     * @return
     */
    protected String adjustRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (path.startsWith(CmsisConstants.RTE)) {
            return CmsisConstants.PROJECT_LOCAL_PATH + path;
        }
        if (path.startsWith(CmsisConstants.CMSIS_PACK_ROOT_VAR)) {
            return path;
        }
        if (path.startsWith(CmsisConstants.CMSIS_RTE_VAR)) {
            return path;
        }
        return CmsisConstants.CMSIS_RTE_VAR + path;
    }

    protected void addFile(String effectivePath, EFileCategory cat, String comment, ICpComponentInfo ci) {
        if (effectivePath == null || effectivePath.isEmpty()) {
            return;
        }
        switch (cat) {
        case DOC:
            docs.put(effectivePath, comment);
            break;
        case HEADER:
            headers.put(effectivePath, comment);
            break; // we no longer add include paths here due to "path" attribute
        case SOURCE: {
            String ext = Utils.extractFileExtension(effectivePath);
            if (ext == null || !ext.equals("s")) //$NON-NLS-1$
                break;
            // else fall through and treat the file as an assembler source
        }
        case SOURCE_ASM:
            effectivePath = ProjectUtils.removeLastPathSegment(effectivePath);
        case INCLUDE:
            if (!effectivePath.isEmpty()) {
                effectivePath = adjustRelativePath(effectivePath);
                rteBuildSettings.addStringListValue(IBuildSettings.RTE_INCLUDE_PATH,
                        Utils.removeTrailingSlash(effectivePath));
            }
            break;
        case IMAGE:
            break;
        case LIBRARY:
            effectivePath = adjustRelativePath(effectivePath);
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_LIBRARIES, effectivePath);
            effectivePath = ProjectUtils.removeLastPathSegment(effectivePath);
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_LIBRARY_PATHS,
                    Utils.removeTrailingSlash(effectivePath));
            break;
        case LINKER_SCRIPT:
            effectivePath = adjustRelativePath(effectivePath);
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_LINKER_SCRIPT, effectivePath);
            break;
        case OBJECT:
            effectivePath = adjustRelativePath(effectivePath);
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_OBJECTS, effectivePath);
            break;
        case OTHER:
            break;
        case PRE_INCLUDE_GLOBAL:
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_PRE_INCLUDES, adjustRelativePath(effectivePath));
            break;
        case PRE_INCLUDE_LOCAL:
            addComponentPreincludeOption(ci.getName(), adjustRelativePath(effectivePath));
            break;
        case SOURCE_C:
            break;
        case SOURCE_CPP:
            break;
        case UTILITY:
            break;
        case SVD:
            svdFile = effectivePath;
        default:
            break;
        }
    }

    protected void addComponentPreincludeOption(String componentName, String fileName) {
        IBuildSettings componentSettings = rteBuildSettings.createBuildSettings(componentName,
                IBuildSettings.Level.VIRTUAL_GROUP);
        componentSettings.addStringListValue(IBuildSettings.RTE_PRE_INCLUDES, fileName);
    }

    protected void addLibrarySourcePaths(ICpFile f) {
        if (f == null) {
            return;
        }
        String src = f.getAttribute(CmsisConstants.SRC);

        if (src == null || src.isEmpty()) {
            return;
        }

        String[] paths = src.split(";"); //$NON-NLS-1$
        if (paths == null || paths.length == 0) {
            return;
        }

        for (String p : paths) {
            if (p == null || p.isEmpty()) {
                continue;
            }
            String absPath = f.getAbsolutePath(p);
            String path = CpVariableResolver.insertCmsisRootVariable(absPath);
            libSourcePaths.add(path);
        }
    }

    /**
     * Adds piece of RteComponents.h and pre-include code for the component
     *
     * @param c     ICpComponent
     * @param count number of component instances
     * @return code string
     */
    protected void collectIncludeStrings(ICpComponent c, int count) {
        if (c == null || count <= 0) {
            return;
        }
        String componentName = c.getName();
        String componentComment = "/* " + componentName + " */\n"; //$NON-NLS-1$//$NON-NLS-2$
        String s = collectIncludeString(CmsisConstants.RTE_COMPONENTS_H, c, count);
        if (s != null && !s.isEmpty()) {
            rteComponentsH.add(componentComment + s);
        }
        s = collectIncludeString(CmsisConstants.PRE_INCLUDE_GLOBAL_H, c, count);
        if (s != null && !s.isEmpty()) {
            globalPreIncludeStrings.add(componentComment + s);
            rteBuildSettings.addStringListValue(IBuildSettings.RTE_PRE_INCLUDES,
                    CmsisConstants.PROJECT_RTE_Pre_Include_Global_h);
        }
        s = collectIncludeString(CmsisConstants.PRE_INCLUDE_LOCAL_COMPONENT_H, c, count);
        if (s != null && !s.isEmpty()) {
            String fileName = CmsisConstants.Pre_Include_ + Utils.nonAlnumToUndersore(c.getName()) + ".h"; //$NON-NLS-1$
            localPreIncludeStrings.put(fileName, componentComment + s);
            addComponentPreincludeOption(c.getName(), CmsisConstants.PROJECT_RTE_PATH + '/' + fileName);
        }
    }

    /**
     * Collects specific include string
     *
     * @param tag   child's tag
     * @param c     ICpComponent
     * @param count number of component instances
     * @return code string
     */
    protected String collectIncludeString(String tag, ICpComponent c, int count) {
        if (c == null || count <= 0) {
            return null;
        }
        ICpItem child = c.getFirstChild(tag);
        if (child == null) {
            return null;
        }
        String s = c.getFirstChildText(tag);
        return expandInstancePlaceHolders(s, count);

    }

    /**
     * Expands given string by replacing %INSTANSE% Strings with index
     *
     * @param s     String to expand
     * @param count number of component instances
     * @return expanded
     */
    protected static String expandInstancePlaceHolders(String s, int count) {
        if (s == null || s.isEmpty() || count <= 0) {
            return s;
        }
        // convert all line endings to unix format
        s = s.replaceAll("\\\\r\\\\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
        int index = s.indexOf(CmsisConstants.pINSTANCEp);
        if (index < 0)
            return s;
        String expanded = CmsisConstants.EMPTY_STRING;
        for (int i = 0; i < count; i++) {
            String instance = String.valueOf(i);
            String tmp = s.replaceAll(CmsisConstants.pINSTANCEp, instance);
            expanded += tmp + '\n';
        }
        return expanded;
    }

    @Override
    public boolean isAddToProject(ICpFileInfo fi) {
        if (fi == null) {
            return false;
        }
        if (isGeneratedAndRelativeToProject(fi)) {
            return true;
        }

        EFileRole role = fi.getRole();
        boolean includeInProject = false;
        switch (role) {
        case TEMPLATE:
            return false;
        case CONFIG:
        case COPY:
            includeInProject = true;
        case INTERFACE:
        case NONE:
        default:
            break;
        }
        EFileCategory cat = fi.getCategory();
        switch (cat) {
        case SOURCE:
        case SOURCE_ASM:
        case SOURCE_C:
        case SOURCE_CPP:
        case LINKER_SCRIPT:
        case LIBRARY:
        case OBJECT:
            return true;
        case INCLUDE:
            return false;
        case HEADER:
        case PRE_INCLUDE_GLOBAL:
        case PRE_INCLUDE_LOCAL:
        default:
            break;
        }
        return includeInProject;
    }

    /**
     * Check if file is generated and relative to project (=> to config file
     * directory)
     *
     * @param fi {@link ICpFileInfo} to check
     * @return true if file is resolved to a generated file that is relative to
     *         project directory
     */
    protected boolean isGeneratedAndRelativeToProject(ICpFileInfo fi) {
        ICpFile f = fi.getFile();
        if (f != null && f.isGenerated()) {
            String abs = f.getAbsolutePath(f.getName());
            String base = fConfigInfo.getDir(true);
            if (abs.startsWith(base)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns path relative to project (for component files)
     *
     * @param fi         {@link ICpFileInfo} represents file
     * @param absPath    source absolute path
     * @param fileName   filename to use, may also contain path segments
     * @param className  class name of the file's component
     * @param deviceName device name used in project
     * @param index      component instance index
     * @return path relative to the project
     */
    protected String getPathRelativeToProject(ICpFileInfo fi, String absPath, String fileName, String className,
            String deviceName, int index) {
        if (fi == null) {
            return null;
        }

        if (fi.isGenerated()) {
            String baseDir = fConfigInfo.getDir(false);
            ICpFile f = fi.getFile();
            if (f != null) {
                absPath = f.getAbsolutePath(f.getName());
            }
            if (absPath.startsWith(baseDir)) {
                // the file is within project
                return ProjectUtils.makePathRelative(absPath, baseDir);
            }
        }

        String path = CmsisConstants.RTE;
        path += '/';
        if (className != null && !className.isEmpty()) {
            path += Utils.wildCardsToX(className) + '/'; // escape spaces with underscores
        }
        if (deviceName != null && !deviceName.isEmpty() && CmsisConstants.Device.contentEquals(className)) {
            path += Utils.wildCardsToX(deviceName) + '/';
        }

        String effectivePath = insertFileSuffix(path, fileName, index);

        for (int uniqueIndex = 1; uniqueIndex < 128; uniqueIndex++) {
            ICpFileInfo addedFi = fProjectFiles.get(effectivePath);
            if (addedFi == null) {
                break;
            }
            ICpFile f = addedFi.getFile();
            if (f != null && f.getAbsolutePath(addedFi.getName()).equals(absPath)) {
                return null; // already inserted
            }

            String uniquePath = path + uniqueIndex + '/';
            effectivePath = insertFileSuffix(uniquePath, fileName, index);
        }
        return effectivePath;
    }

    protected static String insertFileSuffix(String path, String fileName, int index) {
        if (index >= 0) {
            int nSegments = Utils.getSegmentCount(fileName);
            String filePath = Utils.extractPath(fileName, false);
            if (nSegments > 1 && !filePath.isEmpty()) {
                path += filePath + '/';
            }
            String ext = Utils.extractFileExtension(fileName);
            fileName = Utils.extractBaseFileName(fileName);
            fileName += "_" + index; //$NON-NLS-1$
            if (ext != null) {
                fileName += "." + ext; //$NON-NLS-1$
            }
        }
        return path + fileName;
    }

    /**
     * Creates memory settings from device information
     *
     * @param deviceInfo ICpDeviceInfo object
     * @return IMemorySettings
     */
    public static IMemorySettings createMemorySettings(ICpDeviceInfo di) {

        ICpDeviceItem d = di.getDevice();
        if (d == null) {
            return null;
        }
        ICpItem props = di.getEffectiveProperties();
        if (props == null) {
            return null;
        }
        Map<String, ICpMemory> memoryItems = null;
        ICpDebugConfiguration dc = di.getDebugConfiguration();
        if (dc != null) {
            memoryItems = dc.getMemoryItems();
        }
        return new MemorySettings(memoryItems);
    }

    /**
     * Returns memory settings (previously set or created from device information
     *
     * @return IMemorySettings
     */
    @Override
    public IMemorySettings getMemorySettings() {
        if (fMemorySettings == null) {
            fMemorySettings = createMemorySettings(getDeviceInfo());
        }
        return fMemorySettings;
    }

    /**
     * Sets explicit memory settings, usually from an import operation
     *
     * @param memorySettings IMemorySettings to set
     */
    @Override
    public void setMemorySettings(IMemorySettings memorySettings) {
        fMemorySettings = memorySettings;
    }

    @Override
    public Collection<? extends IRteDependencyItem> validate() {
        EEvaluationResult res = fModel.getEvaluationResult();
        if (res.ordinal() >= EEvaluationResult.INSTALLED.ordinal()) {
            valid = true;
            return null;
        }
        return fModel.getDependencyItems();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public EEvaluationResult getEvaluationResult() {
        if (fModel != null) {
            return fModel.getEvaluationResult();
        }
        return EEvaluationResult.UNDEFINED;
    }

    @Override
    public void setEvaluationResult(EEvaluationResult result) {
        if (fModel != null) {
            fModel.setEvaluationResult(result);
        }
    }

    @Override
    public boolean isGeneratedPackUsed(String gpdsc) {
        if (fModel != null) {
            return fModel.isGeneratedPackUsed(gpdsc);
        }
        return false;
    }

}
