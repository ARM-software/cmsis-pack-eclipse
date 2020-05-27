/**
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.converter;

import com.arm.cmsis.pack.debugseq.converter.DECValueConverter;
import com.arm.cmsis.pack.debugseq.converter.HEXValueConverter;
import com.google.inject.Inject;
import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;

@SuppressWarnings("all")
public class DebugSeqTerminalConverters extends DefaultTerminalConverters {
  /**
   * ------------------ HEX ------------------
   */
  @Inject
  private HEXValueConverter hexValueConverter;
  
  @ValueConverter(rule = "HEX")
  public IValueConverter<Long> HEX() {
    return this.hexValueConverter;
  }
  
  @Inject
  private HEXValueConverter terminalsHexValueConverter;
  
  /**
   * @since 2.9
   */
  @ValueConverter(rule = "org.example.expressions.Expressions.HEX")
  public IValueConverter<Long> TerminalsHEX() {
    return this.terminalsHexValueConverter;
  }
  
  /**
   * ------------------ DEC ------------------
   */
  @Inject
  private DECValueConverter decValueConverter;
  
  @ValueConverter(rule = "DEC")
  public IValueConverter<Long> DEC() {
    return this.decValueConverter;
  }
  
  @Inject
  private DECValueConverter terminalsDecValueConverter;
  
  /**
   * @since 2.9
   */
  @ValueConverter(rule = "org.example.expressions.Expressions.DEC")
  public IValueConverter<Long> TerminalsDEC() {
    return this.terminalsDecValueConverter;
  }
}
