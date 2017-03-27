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

import com.arm.cmsis.pack.debugseq.debugSeq.And;
import com.arm.cmsis.pack.debugseq.debugSeq.Assignment;
import com.arm.cmsis.pack.debugseq.debugSeq.BitAnd;
import com.arm.cmsis.pack.debugseq.debugSeq.BitNot;
import com.arm.cmsis.pack.debugseq.debugSeq.BitOr;
import com.arm.cmsis.pack.debugseq.debugSeq.BitXor;
import com.arm.cmsis.pack.debugseq.debugSeq.Block;
import com.arm.cmsis.pack.debugseq.debugSeq.CodeBlock;
import com.arm.cmsis.pack.debugseq.debugSeq.Comparison;
import com.arm.cmsis.pack.debugseq.debugSeq.Control;
import com.arm.cmsis.pack.debugseq.debugSeq.DapDelay;
import com.arm.cmsis.pack.debugseq.debugSeq.DapJtagSequence;
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjClock;
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjPins;
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjSequence;
import com.arm.cmsis.pack.debugseq.debugSeq.DapWriteABORT;
import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel;
import com.arm.cmsis.pack.debugseq.debugSeq.DebugVars;
import com.arm.cmsis.pack.debugseq.debugSeq.Div;
import com.arm.cmsis.pack.debugseq.debugSeq.Equality;
import com.arm.cmsis.pack.debugseq.debugSeq.Expression;
import com.arm.cmsis.pack.debugseq.debugSeq.IntConstant;
import com.arm.cmsis.pack.debugseq.debugSeq.LoadDebugInfo;
import com.arm.cmsis.pack.debugseq.debugSeq.Message;
import com.arm.cmsis.pack.debugseq.debugSeq.Minus;
import com.arm.cmsis.pack.debugseq.debugSeq.Mul;
import com.arm.cmsis.pack.debugseq.debugSeq.Not;
import com.arm.cmsis.pack.debugseq.debugSeq.Or;
import com.arm.cmsis.pack.debugseq.debugSeq.Parameter;
import com.arm.cmsis.pack.debugseq.debugSeq.Plus;
import com.arm.cmsis.pack.debugseq.debugSeq.Query;
import com.arm.cmsis.pack.debugseq.debugSeq.QueryValue;
import com.arm.cmsis.pack.debugseq.debugSeq.Read16;
import com.arm.cmsis.pack.debugseq.debugSeq.Read32;
import com.arm.cmsis.pack.debugseq.debugSeq.Read64;
import com.arm.cmsis.pack.debugseq.debugSeq.Read8;
import com.arm.cmsis.pack.debugseq.debugSeq.ReadAP;
import com.arm.cmsis.pack.debugseq.debugSeq.ReadDP;
import com.arm.cmsis.pack.debugseq.debugSeq.Rem;
import com.arm.cmsis.pack.debugseq.debugSeq.Sequence;
import com.arm.cmsis.pack.debugseq.debugSeq.SequenceCall;
import com.arm.cmsis.pack.debugseq.debugSeq.Sequences;
import com.arm.cmsis.pack.debugseq.debugSeq.Shift;
import com.arm.cmsis.pack.debugseq.debugSeq.Statement;
import com.arm.cmsis.pack.debugseq.debugSeq.StringConstant;
import com.arm.cmsis.pack.debugseq.debugSeq.Ternary;
import com.arm.cmsis.pack.debugseq.debugSeq.VariableDeclaration;
import com.arm.cmsis.pack.debugseq.debugSeq.VariableRef;
import com.arm.cmsis.pack.debugseq.debugSeq.Write16;
import com.arm.cmsis.pack.debugseq.debugSeq.Write32;
import com.arm.cmsis.pack.debugseq.debugSeq.Write64;
import com.arm.cmsis.pack.debugseq.debugSeq.Write8;
import com.arm.cmsis.pack.debugseq.debugSeq.WriteAP;
import com.arm.cmsis.pack.debugseq.debugSeq.WriteDP;
import com.arm.cmsis.pack.debugseq.generator.IDsqScriptGenerator;
import com.arm.cmsis.pack.debugseq.util.DebugSeqUtil;
import com.arm.cmsis.pack.dsq.IDsqClient;
import com.arm.cmsis.pack.dsq.IDsqContext;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.generator.AbstractGenerator;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.generator.IGeneratorContext;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

/**
 * Generates Python script from Debug access sequence descriptions
 */
@SuppressWarnings("all")
public class DsqPhytonScriptGenerator extends AbstractGenerator implements IDsqScriptGenerator {
  private boolean generateFile = false;
  
  private final static String predefinedVars = new Function0<String>() {
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("(");
      _builder.append(IDsqContext.PROTOCOL, "");
      _builder.append(", ");
      _builder.append(IDsqContext.CONNECTION, "");
      _builder.append(", ");
      _builder.append(IDsqContext.DP, "");
      _builder.append(", ");
      _builder.append(IDsqContext.AP, "");
      _builder.append(", ");
      _builder.append(IDsqContext.TRACEOUT, "");
      _builder.append(", ");
      _builder.append(IDsqContext.ERRORCONTROL, "");
      _builder.append(")");
      return _builder.toString();
    }
  }.apply();
  
  @Override
  public String getDescription() {
    return "Generates Python script to run on Debug Server";
  }
  
  @Override
  public String getID() {
    return "com.arm.cmsis.pack.dsq.engine.generator.python";
  }
  
  @Override
  public String getName() {
    return "Python Generator";
  }
  
  @Override
  public void doGenerate(final Resource resource, final IFileSystemAccess2 fsa, final IGeneratorContext context) {
    TreeIterator<EObject> _allContents = resource.getAllContents();
    Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(_allContents);
    Iterable<DebugSeqModel> _filter = Iterables.<DebugSeqModel>filter(_iterable, DebugSeqModel.class);
    DebugSeqModel _get = ((DebugSeqModel[])Conversions.unwrapArray(_filter, DebugSeqModel.class))[0];
    final String script = this.generate(_get);
    if (this.generateFile) {
      fsa.generateFile("debug_sequences.py", script);
    }
  }
  
  public boolean setGenerateFile(final boolean genFile) {
    return this.generateFile = genFile;
  }
  
  @Override
  public String generate(final DebugSeqModel dsqModel, final String header) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(header, "");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    {
      Sequences _sequences = dsqModel.getSequences();
      EList<Sequence> _sequences_1 = _sequences.getSequences();
      for(final Sequence sequence : _sequences_1) {
        String _generate = this.generate(sequence);
        _builder.append(_generate, "");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
      }
    }
    return _builder.toString();
  }
  
  protected String _generate(final Sequence seq) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("def ");
    String _name = seq.getName();
    _builder.append(_name, "");
    _builder.append(DsqPhytonScriptGenerator.predefinedVars, "");
    _builder.append(":");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    DebugSeqModel _containingDebugSeqModel = DebugSeqUtil.containingDebugSeqModel(seq);
    DebugVars _debugvars = _containingDebugSeqModel.getDebugvars();
    String _generate = this.generate(_debugvars);
    _builder.append(_generate, "    ");
    _builder.newLineIfNotEmpty();
    {
      EList<CodeBlock> _codeblocks = seq.getCodeblocks();
      for(final CodeBlock codeblock : _codeblocks) {
        _builder.append("    ");
        String _generate_1 = this.generate(codeblock);
        _builder.append(_generate_1, "    ");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
  
  protected String _generate(final DebugVars gv) {
    StringConcatenation _builder = new StringConcatenation();
    {
      EList<Statement> _statements = gv.getStatements();
      Iterable<VariableDeclaration> _filter = Iterables.<VariableDeclaration>filter(_statements, VariableDeclaration.class);
      final Function1<VariableDeclaration, Boolean> _function = (VariableDeclaration it) -> {
        String _name = it.getName();
        boolean _isPredefinedVariable = DebugSeqUtil.isPredefinedVariable(_name);
        return Boolean.valueOf((!_isPredefinedVariable));
      };
      Iterable<VariableDeclaration> _filter_1 = IterableExtensions.<VariableDeclaration>filter(_filter, _function);
      for(final VariableDeclaration vardecl : _filter_1) {
        String _generate = this.generate(vardecl);
        _builder.append(_generate, "");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      EList<Statement> _statements_1 = gv.getStatements();
      Iterable<Expression> _filter_2 = Iterables.<Expression>filter(_statements_1, Expression.class);
      for(final Expression expr : _filter_2) {
        String _generate_1 = this.generate(expr);
        _builder.append(_generate_1, "");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
  
  protected String _generate(final Block block) {
    StringConcatenation _builder = new StringConcatenation();
    {
      EList<Statement> _statements = block.getStatements();
      int _size = _statements.size();
      boolean _tripleEquals = (_size == 0);
      if (_tripleEquals) {
        _builder.append("pass");
        _builder.newLine();
      }
    }
    {
      EList<Statement> _statements_1 = block.getStatements();
      for(final Statement stmt : _statements_1) {
        String _generate = this.generate(stmt);
        _builder.append(_generate, "");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
  
  protected String _generate(final Control control) {
    StringConcatenation _builder = new StringConcatenation();
    {
      Expression _if = control.getIf();
      boolean _tripleNotEquals = (_if != null);
      if (_tripleNotEquals) {
        _builder.append("if ");
        Expression _if_1 = control.getIf();
        String _generate = this.generate(_if_1);
        _builder.append(_generate, "");
        _builder.append(":");
        _builder.newLineIfNotEmpty();
        {
          Expression _while = control.getWhile();
          boolean _tripleNotEquals_1 = (_while != null);
          if (_tripleNotEquals_1) {
            _builder.append("    ");
            String _generateControlWhile = this.generateControlWhile(control);
            _builder.append(_generateControlWhile, "    ");
            _builder.newLineIfNotEmpty();
          } else {
            {
              EList<CodeBlock> _codeblocks = control.getCodeblocks();
              int _size = _codeblocks.size();
              boolean _tripleEquals = (_size == 0);
              if (_tripleEquals) {
                _builder.append("    ");
                _builder.append("pass");
                _builder.newLine();
              }
            }
            {
              EList<CodeBlock> _codeblocks_1 = control.getCodeblocks();
              for(final CodeBlock codeblock : _codeblocks_1) {
                _builder.append("    ");
                String _generate_1 = this.generate(codeblock);
                _builder.append(_generate_1, "    ");
                _builder.newLineIfNotEmpty();
              }
            }
          }
        }
      } else {
        Expression _while_1 = control.getWhile();
        boolean _tripleNotEquals_2 = (_while_1 != null);
        if (_tripleNotEquals_2) {
          String _generateControlWhile_1 = this.generateControlWhile(control);
          _builder.append(_generateControlWhile_1, "");
          _builder.newLineIfNotEmpty();
        } else {
          {
            EList<CodeBlock> _codeblocks_2 = control.getCodeblocks();
            for(final CodeBlock codeblock_1 : _codeblocks_2) {
              String _generate_2 = this.generate(codeblock_1);
              _builder.append(_generate_2, "");
              _builder.newLineIfNotEmpty();
            }
          }
        }
      }
    }
    return _builder.toString();
  }
  
  private String generateControlWhile(final Control control) {
    StringConcatenation _builder = new StringConcatenation();
    {
      long _timeout = control.getTimeout();
      boolean _tripleNotEquals = (_timeout != 0);
      if (_tripleNotEquals) {
        _builder.append("t = Timer()");
        _builder.newLine();
      }
    }
    _builder.append("while (");
    Expression _while = control.getWhile();
    String _generate = this.generate(_while);
    _builder.append(_generate, "");
    _builder.append(")");
    {
      long _timeout_1 = control.getTimeout();
      boolean _tripleNotEquals_1 = (_timeout_1 != 0);
      if (_tripleNotEquals_1) {
        _builder.append(" and t.getTime() < ");
        long _timeout_2 = control.getTimeout();
        _builder.append(_timeout_2, "");
      }
    }
    _builder.append(":");
    _builder.newLineIfNotEmpty();
    {
      EList<CodeBlock> _codeblocks = control.getCodeblocks();
      for(final CodeBlock codeblock : _codeblocks) {
        String _generate_1 = this.generate(codeblock);
        _builder.append(_generate_1, "");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("    ");
    _builder.append("continue");
    _builder.newLine();
    return _builder.toString();
  }
  
  protected String _generate(final VariableDeclaration vd) {
    StringConcatenation _builder = new StringConcatenation();
    String _name = vd.getName();
    _builder.append(_name, "");
    _builder.append(" = ");
    Expression _value = vd.getValue();
    String _generate = this.generate(_value);
    _builder.append(_generate, "");
    return _builder.toString();
  }
  
  protected String _generate(final Expression e) {
    String _switchResult = null;
    boolean _matched = false;
    if (e instanceof IntConstant) {
      _matched=true;
      StringConcatenation _builder = new StringConcatenation();
      long _value = ((IntConstant)e).getValue();
      _builder.append(_value, "");
      _switchResult = _builder.toString();
    }
    if (!_matched) {
      if (e instanceof StringConstant) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("\"");
        String _value = ((StringConstant)e).getValue();
        _builder.append(_value, "");
        _builder.append("\"");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof VariableRef) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        VariableDeclaration _variable = ((VariableRef)e).getVariable();
        String _name = _variable.getName();
        _builder.append(_name, "");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Not) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("not (");
        Expression _expression = ((Not)e).getExpression();
        String _generate = this.generate(_expression);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Assignment) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        Expression _left = ((Assignment)e).getLeft();
        VariableDeclaration _variable = ((VariableRef) _left).getVariable();
        String _name = _variable.getName();
        _builder.append(_name, "");
        _builder.append(" ");
        String _op = ((Assignment)e).getOp();
        _builder.append(_op, "");
        _builder.append(" ");
        Expression _right = ((Assignment)e).getRight();
        String _generate = this.generate(_right);
        _builder.append(_generate, "");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Ternary) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        Expression _exp1 = ((Ternary)e).getExp1();
        String _generate = this.generate(_exp1);
        _builder.append(_generate, "");
        _builder.append(" if (");
        Expression _left = ((Ternary)e).getLeft();
        String _generate_1 = this.generate(_left);
        _builder.append(_generate_1, "");
        _builder.append(") != 0 else ");
        Expression _exp2 = ((Ternary)e).getExp2();
        String _generate_2 = this.generate(_exp2);
        _builder.append(_generate_2, "");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Or) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Or)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") != 0 or (");
        Expression _right = ((Or)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(") != 0");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof And) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((And)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") != 0 and (");
        Expression _right = ((And)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(") != 0");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitOr) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((BitOr)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") | (");
        Expression _right = ((BitOr)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitXor) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((BitXor)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") ^ (");
        Expression _right = ((BitXor)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitAnd) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((BitAnd)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") & (");
        Expression _right = ((BitAnd)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitNot) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("~(");
        Expression _expression = ((BitNot)e).getExpression();
        String _generate = this.generate(_expression);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Equality) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Equality)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") ");
        String _op = ((Equality)e).getOp();
        _builder.append(_op, "");
        _builder.append(" (");
        Expression _right = ((Equality)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Comparison) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Comparison)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") ");
        {
          String _op = ((Comparison)e).getOp();
          boolean _equals = Objects.equal(_op, "&lt;");
          if (_equals) {
            _builder.append(" < ");
          } else {
            String _op_1 = ((Comparison)e).getOp();
            boolean _equals_1 = Objects.equal(_op_1, "&gt;");
            if (_equals_1) {
              _builder.append(" > ");
            } else {
              String _op_2 = ((Comparison)e).getOp();
              boolean _equals_2 = Objects.equal(_op_2, "&lt;=");
              if (_equals_2) {
                _builder.append(" <= ");
              } else {
                _builder.append(" >= ");
              }
            }
          }
        }
        _builder.append(" (");
        Expression _right = ((Comparison)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Shift) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Shift)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") ");
        {
          String _op = ((Shift)e).getOp();
          boolean _equals = Objects.equal(_op, "&lt;&lt;");
          if (_equals) {
            _builder.append(" << ");
          } else {
            _builder.append(" >> ");
          }
        }
        _builder.append(" (");
        Expression _right = ((Shift)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Plus) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Plus)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") + (");
        Expression _right = ((Plus)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Minus) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Minus)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") - (");
        Expression _right = ((Minus)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Mul) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Mul)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") * (");
        Expression _right = ((Mul)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Div) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Div)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") / (");
        Expression _right = ((Div)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Rem) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        Expression _left = ((Rem)e).getLeft();
        String _generate = this.generate(_left);
        _builder.append(_generate, "");
        _builder.append(") % (");
        Expression _right = ((Rem)e).getRight();
        String _generate_1 = this.generate(_right);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof SequenceCall) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        String _seqname = ((SequenceCall)e).getSeqname();
        _builder.append(_seqname, "");
        _builder.append(DsqPhytonScriptGenerator.predefinedVars, "");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Query) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DS.Query(");
        Expression _type = ((Query)e).getType();
        String _generate = this.generate(_type);
        _builder.append(_generate, "");
        _builder.append(", \"");
        String _message = ((Query)e).getMessage();
        _builder.append(_message, "");
        _builder.append("\", ");
        Expression _default = ((Query)e).getDefault();
        String _generate_1 = this.generate(_default);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof QueryValue) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DS.Query(");
        _builder.append(IDsqClient.QUERY_VALUE_TYPE, "");
        _builder.append(", \"");
        String _message = ((QueryValue)e).getMessage();
        _builder.append(_message, "");
        _builder.append("\", ");
        Expression _default = ((QueryValue)e).getDefault();
        String _generate = this.generate(_default);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof LoadDebugInfo) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("LoadDebugInfo(\"");
        String _path = ((LoadDebugInfo)e).getPath();
        _builder.append(_path, "");
        _builder.append("\")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Message) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Message(\"");
        String _format = ((Message)e).getFormat();
        _builder.append(_format, "");
        _builder.append("\"");
        {
          EList<Parameter> _parameters = ((Message)e).getParameters();
          for(final Parameter p : _parameters) {
            _builder.append(", ");
            String _generate = this.generate(p);
            _builder.append(_generate, "");
          }
        }
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read8) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read8(");
        Expression _addr = ((Read8)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read16) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read16(");
        Expression _addr = ((Read16)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read32) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read32(");
        Expression _addr = ((Read32)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read64) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read64(");
        Expression _addr = ((Read64)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof ReadAP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("ReadAP(");
        Expression _addr = ((ReadAP)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof ReadDP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("ReadDP(");
        Expression _addr = ((ReadDP)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write8) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write8(");
        Expression _addr = ((Write8)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((Write8)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write16) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write16(");
        Expression _addr = ((Write16)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((Write16)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write32) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write32(");
        Expression _addr = ((Write32)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((Write32)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write64) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write64(");
        Expression _addr = ((Write64)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((Write64)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof WriteAP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("WriteAP(");
        Expression _addr = ((WriteAP)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((WriteAP)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof WriteDP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("WriteDP(");
        Expression _addr = ((WriteDP)e).getAddr();
        String _generate = this.generate(_addr);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((WriteDP)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapDelay) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_Delay(");
        Expression _delay = ((DapDelay)e).getDelay();
        String _generate = this.generate(_delay);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapWriteABORT) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_WriteABORT(");
        Expression _value = ((DapWriteABORT)e).getValue();
        String _generate = this.generate(_value);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjPins) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Pins(");
        Expression _pinout = ((DapSwjPins)e).getPinout();
        String _generate = this.generate(_pinout);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _pinselect = ((DapSwjPins)e).getPinselect();
        String _generate_1 = this.generate(_pinselect);
        _builder.append(_generate_1, "");
        _builder.append(", ");
        Expression _pinwait = ((DapSwjPins)e).getPinwait();
        String _generate_2 = this.generate(_pinwait);
        _builder.append(_generate_2, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjClock) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Clock(");
        Expression _value = ((DapSwjClock)e).getValue();
        String _generate = this.generate(_value);
        _builder.append(_generate, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjSequence) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Sequence(");
        Expression _cnt = ((DapSwjSequence)e).getCnt();
        String _generate = this.generate(_cnt);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _val = ((DapSwjSequence)e).getVal();
        String _generate_1 = this.generate(_val);
        _builder.append(_generate_1, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapJtagSequence) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_JTAG_Sequence(");
        Expression _cnt = ((DapJtagSequence)e).getCnt();
        String _generate = this.generate(_cnt);
        _builder.append(_generate, "");
        _builder.append(", ");
        Expression _tms = ((DapJtagSequence)e).getTms();
        String _generate_1 = this.generate(_tms);
        _builder.append(_generate_1, "");
        _builder.append(", ");
        Expression _tdi = ((DapJtagSequence)e).getTdi();
        String _generate_2 = this.generate(_tdi);
        _builder.append(_generate_2, "");
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    return _switchResult;
  }
  
  public String generate(final EObject block) {
    if (block instanceof Block) {
      return _generate((Block)block);
    } else if (block instanceof Control) {
      return _generate((Control)block);
    } else if (block instanceof Expression) {
      return _generate((Expression)block);
    } else if (block instanceof VariableDeclaration) {
      return _generate((VariableDeclaration)block);
    } else if (block instanceof DebugVars) {
      return _generate((DebugVars)block);
    } else if (block instanceof Sequence) {
      return _generate((Sequence)block);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(block).toString());
    }
  }
}
