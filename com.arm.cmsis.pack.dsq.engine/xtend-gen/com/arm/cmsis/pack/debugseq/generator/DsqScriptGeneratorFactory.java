/**
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.generator;

import com.arm.cmsis.pack.CpAbstractExtensionFactory;
import com.arm.cmsis.pack.debugseq.generator.IDsqScriptGenerator;
import java.util.Collection;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class DsqScriptGeneratorFactory extends CpAbstractExtensionFactory<IDsqScriptGenerator> {
  public static final String SCRIPT_GENERATOR = "ScriptGenerator";
  
  private static DsqScriptGeneratorFactory theFactory = null;
  
  private Map<String, IDsqScriptGenerator> fDsqScriptGenerators = CollectionLiterals.<String, IDsqScriptGenerator>newHashMap();
  
  protected DsqScriptGeneratorFactory(final String extensionPointId) {
    super(extensionPointId);
  }
  
  @Override
  public String getExtensionPointId(final String elementId) {
    return ("com.arm.cmsis.pack.dsq.engine." + elementId);
  }
  
  public static DsqScriptGeneratorFactory getInstance() {
    if ((DsqScriptGeneratorFactory.theFactory == null)) {
      DsqScriptGeneratorFactory _dsqScriptGeneratorFactory = new DsqScriptGeneratorFactory(DsqScriptGeneratorFactory.SCRIPT_GENERATOR);
      DsqScriptGeneratorFactory.theFactory = _dsqScriptGeneratorFactory;
    }
    return DsqScriptGeneratorFactory.theFactory;
  }
  
  public synchronized IDsqScriptGenerator getExtender(final String generatorID) {
    boolean _containsKey = this.fDsqScriptGenerators.containsKey(generatorID);
    if (_containsKey) {
      return this.fDsqScriptGenerators.get(generatorID);
    }
    boolean _containsKey_1 = this.elements.containsKey(generatorID);
    if (_containsKey_1) {
      try {
        IDsqScriptGenerator generator = this.createExtender(this.elements.get(generatorID));
        if ((generator != null)) {
          this.fDsqScriptGenerators.put(generatorID, generator);
          return generator;
        }
      } catch (final Throwable _t) {
        if (_t instanceof CoreException) {
          final CoreException e = (CoreException)_t;
          e.printStackTrace();
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    }
    return null;
  }
  
  public Collection<IDsqScriptGenerator> getDsqScriptGenerators() {
    return this.fDsqScriptGenerators.values();
  }
  
  @Override
  protected IDsqScriptGenerator castToExtenderClass(final Object extender) {
    if ((extender instanceof IDsqScriptGenerator)) {
      return ((IDsqScriptGenerator)extender);
    }
    return null;
  }
}
