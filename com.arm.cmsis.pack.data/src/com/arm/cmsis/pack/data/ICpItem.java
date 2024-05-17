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

package com.arm.cmsis.pack.data;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.generic.IAttributedItem;
import com.arm.cmsis.pack.item.ICmsisTreeItem;
import com.arm.cmsis.pack.utils.FullDeviceName;

/**
 * Base for all items in CMSIS-Packs
 *
 */
public interface ICpItem extends IAttributedItem, ICpItemFactory, ICmsisTreeItem<ICpItem> {

    public static final ICpItem NULL_CPITEM = null; // to resolve ambiguity in constructors
    public static final ICpItem[] EMPTY_CPITEM_ARRAY = new ICpItem[0];
    public static final List<ICpItem> EMPTY_CPITEM_LIST = new ArrayList<ICpItem>();

    /**
     * Returns root item containing this item as ICpRootItem
     *
     * @return pack item owning the item tree
     */
    ICpRootItem getRootItem();

    /**
     * Returns root item containing this item as ICpPack
     *
     * @return pack item owning the item tree
     */
    ICpPack getPack();

    /**
     * Returns ID of pack containing this item
     *
     * @return pack id
     */
    String getPackId();

    /**
     * Returns family ID of pack containing this item (pack ID without version in
     * form Vendor.Name)
     *
     * @return pack family ID
     */
    String getPackFamilyId();

    /**
     * Returns absolute directory path of the root item
     *
     * @param keepSlash flag if to keep or remove trailing slash
     * @return absolute directory path of the root item
     */
    String getRootDir(boolean keepSlash);

    /**
     * Returns absolute file name of the root item
     *
     * @return absolute file name of the root item
     */
    String getRootFileName();

    /**
     * Returns parent item in the hierarchy that has corresponding tag
     *
     * @param tag parent's tag to search
     * @return parent item in the hierarchy with corresponding tag or null
     */
    ICpItem getParent(final String tag);

    /**
     * Returns parent component or API item
     *
     * @return parent component item in the hierarchy if any
     */
    ICpComponent getParentComponent();

    /**
     * Returns grand children of a child with specified tag
     *
     * @param tag child's tag
     * @return collection of child's children or null if no child is found
     */
    Collection<? extends ICpItem> getGrandChildren(final String tag);

    /**
     * Returns collection of children that have specified tag
     *
     * @param tag child's tag
     * @return collection of children having specified tag
     */
    Collection<ICpItem> getChildren(final String tag);

    /**
     * Returns collected attributes from this item and parent items. Search is
     * performed until parent is null or parent implementation stops search
     *
     * @param m attribute map to fill. If null, the collection is allocated.
     * @return filled attribute collection
     */
    Map<String, String> getEffectiveAttributes(Map<String, String> m);

    /**
     * Checks if item has "condition" attribute
     *
     * @return true if item has condition
     */
    boolean hasCondition();

    /**
     * Returns item's condition ID
     *
     * @return condition ID if exists, otherwise null
     */
    String getConditionId();

    /**
     * Items like components and files can have condition
     *
     * @return condition object for this item or null if item has no condition
     */
    ICpItem getCondition();

    /**
     * Evaluates underlying condition for given context and returns its result
     *
     * @param context condition evaluation context
     * @return evaluation result for underlying condition or IGNORED if item has no
     *         condition
     */
    EEvaluationResult evaluate(ICpConditionContext context);

    /**
     * Searches child collection for the first item corresponding to the given ID
     * string
     *
     * @param id    implementation-dependent ID to search for
     * @param child item if found, null otherwise
     */
    ICpItem getProperty(final String id);

    /**
     * Checks if availability of this item depends on selected device
     *
     * @return true if item is device dependent
     */
    boolean isDeviceDependent();

    /**
     * Checks if availability of this item depends on selected board
     *
     * @return true if item is board dependent
     */
    boolean isBoardDependent();

    /**
     * Merges property to the child list: adds if the property with same ID does not
     * yet exist
     *
     * @param property      item to merge
     * @param processorName only merge properties that have the same processor name
     */
    void mergeProperty(ICpItem property, String processorName);

    /**
     * Merges content of supplied property to the child item whose ID equals to
     * supplied property one
     *
     * @param property      property which content to merge
     * @param processorName only merge properties that have the same processor name
     */
    void mergeEffectiveContent(ICpItem property, String processorName);

    /**
     * Returns true if the property provides effective content and must collect it
     *
     * @param true if the property provides effective content and must collect it
     */
    boolean providesEffectiveContent();

    /**
     * Device properties: returns if item is unique => appears only once in
     * effective properties
     *
     * @return true if property is unique
     */
    boolean isUnique();

    /**
     * Gets the item containing collection of effective sub-properties merged with
     * corresponding properties in higher levels in device description hierarchy
     *
     * @return collection of effective sub-properties
     */
    ICpItem getEffectiveContent();

    /**
     * Returns vendor of the element
     *
     * @return vendor name of this element
     */
    String getVendor();

    /**
     * Returns version of the element
     *
     * @return version of this element
     */
    String getVersion();

    /**
     * Returns revisionn of the element
     *
     * @return revision of this element
     */
    String getRevision();

    /**
     * Returns "Punit" attribute as integer
     *
     * @return processor unit index (0 is default)
     */
    int getPunitIndex();

    /**
     * Returns "Punits" attribute as integer
     *
     * @return processor unit count (1 is default)
     */
    int getPunitsCount();

    /**
     * Returns "Dname" or "Dvariant" attribute of the element representing device
     * property
     *
     * @return device name or null if neither "Dname" nor "Dvariant" attribute" is
     *         found
     */
    default String getDeviceName() {
        return FullDeviceName.getDeviceName(attributes());
    }

    /**
     * Returns "Pname" attribute of the element representing device property
     *
     * @return processor name or empty string if "pname" attribute not found
     */
    default String getProcessorName() {
        return FullDeviceName.getProcessorName(attributes());
    }

    /**
     * Returns full device name in form "Dname:Pname"
     *
     * @return full device name or an empty string if this element does not
     *         represent device
     */
    default String getFullDeviceName() {
        return FullDeviceName.getFullDeviceName(getDeviceName(), getProcessorName());
    }

    /**
     * Returns name of component's bundle
     *
     * @return bundle name or empty string if component has no bundle
     */
    String getBundleName();

    /**
     * Returns version of component's bundle
     *
     * @return bundle version or empty string if component has no bundle
     */
    default String getBundleVersion() {
        return getAttribute(CmsisConstants.CBUNDLEVERSION);
    }

    /**
     * Returns version match mode to be used when resolving the item (component, api
     * or pack)
     *
     * @return version match mode as EVersionMatchMode value
     */
    EVersionMatchMode getVersionMatchMode();

    /**
     * Sets version match mode that should be use when resolving the item
     *
     * @param mode version match mode to set
     */
    void setVersionMatchMode(EVersionMatchMode mode);

    /**
     * Checks if fixed version match mode to be used when resolving the item
     *
     * @return true if fixed version match mode is to be used
     */
    boolean isVersionFixed();

    /**
     * Checks if this item represents a default variant or default bundle
     * (effectively has "isDefaultVariant" attribute set to "1")
     *
     * @return true if default variant
     */
    boolean isDefaultVariant();

    /**
     * Checks if an item is generated (comes from gpdsc file)
     *
     * @return true if item is generated
     */
    boolean isGenerated();

    /**
     * Checks if the item comes from deprecated pack
     *
     * @return true if deprecated
     */
    boolean isDeprecated();

    /**
     * Returns absolute path of supplied relative one, if supplied path is an URL or
     * absolute, returns it
     *
     * @param relPath path to convert to absolute
     * @return absolute path
     */
    String getAbsolutePath(String relPath);

    /**
     * Return collection of documents associated with the device or board (items
     * with "book" tag)
     *
     * @return collection of ICpItem objects representing books
     */
    Collection<ICpItem> getBooks();

    /**
     * Checks if this item matches running host.
     *
     * @return true if matches
     */
    boolean matchesHost();

    /**
     * Casts a supplied object to a ICpItem
     *
     * @param obj object to cast
     * @return casted object if possible, null otherwise
     */
    static ICpItem cast(Object obj) {
        if (obj instanceof ICpItem) {
            return (ICpItem) obj;
        }
        return null;
    }

    /**
     * Clones this item and adds the clone to new parent, makes deep copy
     *
     * @param newParent new parent of the clone
     * @return new ICpItem that is a clone of this one
     */
    default ICpItem copyTo(ICpItem newParent) {
        return copyTo(newParent, true);
    }

    /**
     * Clones this item and adds the clone to new parent, makes deep copy
     *
     * @param newParent    new parent of the clone
     * @param copyChildren boolean flag to copy children as well (hierarchically)
     * @return new ICpItem that is a clone of this one
     */
    ICpItem copyTo(ICpItem newParent, boolean copyChildren);

    /**
     * Clones child blocks to specified parent
     *
     * @param newParent new parent of the clone
     */
    void copyChildrenTo(ICpItem newParent);

    /**
     * Updates this item according to the supplied one by updating attributes and
     * children. Does not change parent.
     *
     * @param other ICpItem to get information from
     * @return true if this item is modified
     */
    boolean updateItem(ICpItem other);

    /**
     * Creates a simple ICpItem item with all attributes expanded as separate
     * ICpItems with tag as name and text as value.
     *
     * @param parent parent ICpItem for the created item
     * @return created ICpItem
     */
    ICpItem toSimpleTree(ICpItem parent);

    /**
     * Creates a simple ICpItem out of attribute values
     *
     * @param parent parent ICpItem for the created item
     * @param key    attribute key (cannot be null or empty))
     * @param value  attribute value
     * @return created ICpItem
     */
    ICpItem toSimpleItem(ICpItem parent, String key, String value);

    /**
     * Returns "info" attribute value if any
     *
     * @return value of "info" attribute or empty string
     */
    default String getInfo() {
        return getAttribute(CmsisConstants.INFO);
    }

    /**
     * Initializes this item
     */
    default void initItem() {
        // default does nothing
    }
}
