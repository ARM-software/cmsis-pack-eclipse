/**
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.util;

import com.arm.cmsis.pack.debugseq.debugSeq.Block;
import com.arm.cmsis.pack.debugseq.debugSeq.Control;
import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel;
import com.arm.cmsis.pack.debugseq.debugSeq.Sequence;
import com.arm.cmsis.pack.debugseq.debugSeq.Sequences;
import com.arm.cmsis.pack.dsq.IDsqContext;
import com.google.common.base.Objects;
import java.math.BigInteger;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class DebugSeqUtil {
  public static DebugSeqModel containingDebugSeqModel(final EObject e) {
    return EcoreUtil2.<DebugSeqModel>getContainerOfType(e, DebugSeqModel.class);
  }
  
  public static Sequences containingSequences(final EObject e) {
    return EcoreUtil2.<Sequences>getContainerOfType(e, Sequences.class);
  }
  
  public static Sequence containingSequence(final EObject e) {
    return EcoreUtil2.<Sequence>getContainerOfType(e, Sequence.class);
  }
  
  public static Control containingControl(final EObject e) {
    return EcoreUtil2.<Control>getContainerOfType(e, Control.class);
  }
  
  public static Block containingBlock(final EObject e) {
    return EcoreUtil2.<Block>getContainerOfType(e, Block.class);
  }
  
  public static boolean isEmptyDefaultSequence(final String seqName) {
    return ((((((((Objects.equal(seqName, "DebugDeviceUnlock") || Objects.equal(seqName, "TraceStart")) || Objects.equal(seqName, "DebugCodeMemRemap")) || Objects.equal(seqName, "TraceStop")) || Objects.equal(seqName, "FlashEraseDone")) || Objects.equal(seqName, "FlashProgramDone")) || Objects.equal(seqName, "RecoverySupportStart")) || Objects.equal(seqName, "RecoverySupportStop")) || Objects.equal(seqName, "RecoveryAcknowledge"));
  }
  
  public static boolean isDefaultSequence(final String seqName) {
    return ((((((((((((((((((((Objects.equal(seqName, "DebugPortSetup") || 
      Objects.equal(seqName, "DebugPortStart")) || 
      Objects.equal(seqName, "DebugPortStop")) || 
      Objects.equal(seqName, "DebugDeviceUnlock")) || 
      Objects.equal(seqName, "DebugCoreStart")) || 
      Objects.equal(seqName, "DebugCoreStop")) || 
      Objects.equal(seqName, "DebugCodeMemRemap")) || 
      Objects.equal(seqName, "ResetSystem")) || 
      Objects.equal(seqName, "ResetProcessor")) || 
      Objects.equal(seqName, "ResetHardware")) || 
      Objects.equal(seqName, "ResetHardwareAssert")) || 
      Objects.equal(seqName, "ResetHardwareDeassert")) || 
      Objects.equal(seqName, "ResetCatchSet")) || 
      Objects.equal(seqName, "ResetCatchClear")) || 
      Objects.equal(seqName, "FlashEraseDone")) || 
      Objects.equal(seqName, "FlashProgramDone")) || 
      Objects.equal(seqName, "TraceStart")) || 
      Objects.equal(seqName, "TraceStop")) || 
      Objects.equal(seqName, "RecoverySupportStart")) || 
      Objects.equal(seqName, "RecoverySupportStop")) || 
      Objects.equal(seqName, "RecoveryAcknowledge"));
  }
  
  public static boolean isPredefinedVariable(final String varName) {
    return (((((Objects.equal(varName, IDsqContext.AP) || 
      Objects.equal(varName, IDsqContext.DP)) || 
      Objects.equal(varName, IDsqContext.PROTOCOL)) || 
      Objects.equal(varName, IDsqContext.CONNECTION)) || 
      Objects.equal(varName, IDsqContext.TRACEOUT)) || 
      Objects.equal(varName, IDsqContext.ERRORCONTROL));
  }
  
  public static long toLong(final Object o) {
    long _xifexpression = (long) 0;
    if ((o instanceof Boolean)) {
      long _xifexpression_1 = (long) 0;
      if (((Boolean) o).booleanValue()) {
        _xifexpression_1 = 1L;
      } else {
        _xifexpression_1 = 0L;
      }
      _xifexpression = _xifexpression_1;
    } else {
      long _xifexpression_2 = (long) 0;
      if ((o instanceof Integer)) {
        _xifexpression_2 = ((Integer)o).intValue();
      } else {
        Long _xifexpression_3 = null;
        if ((o instanceof Long)) {
          _xifexpression_3 = Long.valueOf(((Long)o).longValue());
        } else {
          Long _xifexpression_4 = null;
          if ((o instanceof String)) {
            Long _xtrycatchfinallyexpression = null;
            try {
              _xtrycatchfinallyexpression = Long.valueOf(((String)o));
            } catch (final Throwable _t) {
              if (_t instanceof Exception) {
                _xtrycatchfinallyexpression = Long.valueOf(0L);
              } else {
                throw Exceptions.sneakyThrow(_t);
              }
            }
            _xifexpression_4 = _xtrycatchfinallyexpression;
          } else {
            _xifexpression_4 = Long.valueOf(0L);
          }
          _xifexpression_3 = _xifexpression_4;
        }
        _xifexpression_2 = (_xifexpression_3).longValue();
      }
      _xifexpression = _xifexpression_2;
    }
    return _xifexpression;
  }
  
  public static int toInteger(final Long l) {
    return Integer.parseInt(l.toString());
  }
  
  /**
   * Format the string with parameters
   * @param string string to format
   * @param values values for specifiers
   * @return formatted string
   */
  public static String formatWithValues(final String string, final List<Object> values) throws IllegalFormatException {
    String _xblockexpression = null;
    {
      if (((string == null) || string.isEmpty())) {
        return string;
      } else {
        if (((values == null) || values.isEmpty())) {
          return String.format(string);
        }
      }
      final Pattern pattern = Pattern.compile("%0?(\\d+)?(\\.\\d+)?((ll)?[uxXob]|L?f|s)");
      final Matcher matcher = pattern.matcher(string);
      StringBuilder result = new StringBuilder(string);
      final LinkedList<Integer> removeIndices = CollectionLiterals.<Integer>newLinkedList();
      int idx = 0;
      final LinkedList<Object> parms = CollectionLiterals.<Object>newLinkedList();
      for (final Object value : values) {
        parms.add(value);
      }
      while (matcher.find()) {
        {
          int _end = matcher.end();
          final int lastIdx = (_end - 1);
          char _charAt = string.charAt(lastIdx);
          boolean _matched = false;
          char _charAt_1 = "u".charAt(0);
          if (Objects.equal(_charAt, _charAt_1)) {
            _matched=true;
            result.setCharAt(lastIdx, 'd');
          }
          if (!_matched) {
            char _charAt_2 = "b".charAt(0);
            if (Objects.equal(_charAt, _charAt_2)) {
              _matched=true;
              result.setCharAt(lastIdx, 'd');
              final Object v = parms.get(idx);
              String _binaryString = Long.toBinaryString(DebugSeqUtil.toLong(v));
              BigInteger _bigInteger = new BigInteger(_binaryString);
              parms.set(idx, _bigInteger);
            }
          }
          if (!_matched) {
            char _charAt_3 = "f".charAt(0);
            if (Objects.equal(_charAt, _charAt_3)) {
              _matched=true;
              final Object v_1 = parms.get(idx);
              char _charAt_4 = string.charAt((lastIdx - 1));
              char _charAt_5 = "L".charAt(0);
              boolean _tripleEquals = (_charAt_4 == _charAt_5);
              if (_tripleEquals) {
                removeIndices.add(Integer.valueOf((lastIdx - 1)));
                parms.set(idx, Double.valueOf(Double.longBitsToDouble(DebugSeqUtil.toLong(v_1))));
              } else {
                parms.set(idx, Float.valueOf(Float.intBitsToFloat(DebugSeqUtil.toInteger(Long.valueOf(DebugSeqUtil.toLong(v_1))))));
              }
            }
          }
          char _charAt_6 = string.charAt((lastIdx - 1));
          char _charAt_7 = "l".charAt(0);
          boolean _tripleEquals_1 = (_charAt_6 == _charAt_7);
          if (_tripleEquals_1) {
            removeIndices.add(Integer.valueOf((lastIdx - 2)));
            removeIndices.add(Integer.valueOf((lastIdx - 1)));
          }
          idx++;
        }
      }
      int modifier = 0;
      for (final int removeIdx : removeIndices) {
        {
          result.deleteCharAt((removeIdx - modifier));
          modifier++;
        }
      }
      _xblockexpression = String.format(result.toString(), parms.toArray());
    }
    return _xblockexpression;
  }
}
