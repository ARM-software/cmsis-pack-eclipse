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

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class HEXValueConverter extends AbstractLexerBasedConverter<Long> {
  @Override
  protected String toEscapedString(final Long value) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("0x");
    String _string = value.toString();
    _builder.append(_string);
    return _builder.toString();
  }
  
  @Override
  protected void assertValidValue(final Long value) {
    super.assertValidValue(value);
    if (((value).longValue() < 0)) {
      StringConcatenation _builder = new StringConcatenation();
      String _ruleName = this.getRuleName();
      _builder.append(_ruleName);
      _builder.append("-value may not be negative (value: ");
      _builder.append(value);
      _builder.append(").");
      throw new ValueConverterException(_builder.toString(), 
        null, null);
    }
  }
  
  @Override
  public Long toValue(final String string, final INode node) {
    boolean _isEmpty = Strings.isEmpty(string);
    if (_isEmpty) {
      throw new ValueConverterException("Couldn\'t convert empty string to an hex value.", node, null);
    }
    try {
      long longValue = Long.parseUnsignedLong(string.substring(2), 16);
      return Long.valueOf(longValue);
    } catch (final Throwable _t) {
      if (_t instanceof NumberFormatException) {
        final NumberFormatException e = (NumberFormatException)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Couldn\'t convert \'");
        _builder.append(string);
        _builder.append("\' to an hex value.");
        throw new ValueConverterException(_builder.toString(), node, e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
