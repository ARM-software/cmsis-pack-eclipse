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
public class DsqPythonScriptGenerator extends AbstractGenerator implements IDsqScriptGenerator {
  private boolean generateFile = false;
  
  private static final String predefinedVars = new Function0<String>() {
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("(");
      _builder.append(IDsqContext.PROTOCOL);
      _builder.append(", ");
      _builder.append(IDsqContext.CONNECTION);
      _builder.append(", ");
      _builder.append(IDsqContext.DP);
      _builder.append(", ");
      _builder.append(IDsqContext.AP);
      _builder.append(", ");
      _builder.append(IDsqContext.TRACEOUT);
      _builder.append(", ");
      _builder.append(IDsqContext.ERRORCONTROL);
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
    final String script = this.generate(((DebugSeqModel[])Conversions.unwrapArray((Iterables.<DebugSeqModel>filter(IteratorExtensions.<EObject>toIterable(resource.getAllContents()), DebugSeqModel.class)), DebugSeqModel.class))[0]);
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
    _builder.append(header);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    {
      EList<Sequence> _sequences = dsqModel.getSequences().getSequences();
      for(final Sequence sequence : _sequences) {
        String _generate = this.generate(sequence);
        _builder.append(_generate);
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
    _builder.append(_name);
    _builder.append(DsqPythonScriptGenerator.predefinedVars);
    _builder.append(":");
    _builder.newLineIfNotEmpty();
    _builder.append("    ");
    String _generate = this.generate(DebugSeqUtil.containingDebugSeqModel(seq).getDebugvars());
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
      final Function1<VariableDeclaration, Boolean> _function = (VariableDeclaration it) -> {
        boolean _isPredefinedVariable = DebugSeqUtil.isPredefinedVariable(it.getName());
        return Boolean.valueOf((!_isPredefinedVariable));
      };
      Iterable<VariableDeclaration> _filter = IterableExtensions.<VariableDeclaration>filter(Iterables.<VariableDeclaration>filter(gv.getStatements(), VariableDeclaration.class), _function);
      for(final VariableDeclaration vardecl : _filter) {
        String _generate = this.generate(vardecl);
        _builder.append(_generate);
        _builder.newLineIfNotEmpty();
      }
    }
    {
      Iterable<Expression> _filter_1 = Iterables.<Expression>filter(gv.getStatements(), Expression.class);
      for(final Expression expr : _filter_1) {
        String _generate_1 = this.generate(expr);
        _builder.append(_generate_1);
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
  
  protected String _generate(final Block block) {
    StringConcatenation _builder = new StringConcatenation();
    {
      int _size = block.getStatements().size();
      boolean _tripleEquals = (_size == 0);
      if (_tripleEquals) {
        _builder.append("pass");
        _builder.newLine();
      }
    }
    {
      EList<Statement> _statements = block.getStatements();
      for(final Statement stmt : _statements) {
        String _generate = this.generate(stmt);
        _builder.append(_generate);
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
        String _generate = this.generate(control.getIf());
        _builder.append(_generate);
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
              int _size = control.getCodeblocks().size();
              boolean _tripleEquals = (_size == 0);
              if (_tripleEquals) {
                _builder.append("    ");
                _builder.append("pass");
                _builder.newLine();
              }
            }
            {
              EList<CodeBlock> _codeblocks = control.getCodeblocks();
              for(final CodeBlock codeblock : _codeblocks) {
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
          _builder.append(_generateControlWhile_1);
          _builder.newLineIfNotEmpty();
        } else {
          {
            EList<CodeBlock> _codeblocks_1 = control.getCodeblocks();
            for(final CodeBlock codeblock_1 : _codeblocks_1) {
              String _generate_2 = this.generate(codeblock_1);
              _builder.append(_generate_2);
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
    String _generate = this.generate(control.getWhile());
    _builder.append(_generate);
    _builder.append(")");
    {
      long _timeout_1 = control.getTimeout();
      boolean _tripleNotEquals_1 = (_timeout_1 != 0);
      if (_tripleNotEquals_1) {
        _builder.append(" and t.getTime() < ");
        long _timeout_2 = control.getTimeout();
        _builder.append(_timeout_2);
      }
    }
    _builder.append(":");
    _builder.newLineIfNotEmpty();
    {
      EList<CodeBlock> _codeblocks = control.getCodeblocks();
      for(final CodeBlock codeblock : _codeblocks) {
        String _generate_1 = this.generate(codeblock);
        _builder.append(_generate_1);
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
    _builder.append(_name);
    _builder.append(" = ");
    String _generate = this.generate(vd.getValue());
    _builder.append(_generate);
    return _builder.toString();
  }
  
  protected String _generate(final Expression e) {
    String _switchResult = null;
    boolean _matched = false;
    if (e instanceof IntConstant) {
      _matched=true;
      StringConcatenation _builder = new StringConcatenation();
      long _value = ((IntConstant)e).getValue();
      _builder.append(_value);
      _switchResult = _builder.toString();
    }
    if (!_matched) {
      if (e instanceof StringConstant) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("\"");
        String _value = ((StringConstant)e).getValue();
        _builder.append(_value);
        _builder.append("\"");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof VariableRef) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        String _name = ((VariableRef)e).getVariable().getName();
        _builder.append(_name);
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Not) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("not (");
        String _generate = this.generate(((Not)e).getExpression());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Assignment) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        Expression _left = ((Assignment)e).getLeft();
        String _name = ((VariableRef) _left).getVariable().getName();
        _builder.append(_name);
        _builder.append(" ");
        String _op = ((Assignment)e).getOp();
        _builder.append(_op);
        _builder.append(" ");
        String _generate = this.generate(((Assignment)e).getRight());
        _builder.append(_generate);
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Ternary) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        String _generate = this.generate(((Ternary)e).getExp1());
        _builder.append(_generate);
        _builder.append(" if (");
        String _generate_1 = this.generate(((Ternary)e).getLeft());
        _builder.append(_generate_1);
        _builder.append(") != 0 else ");
        String _generate_2 = this.generate(((Ternary)e).getExp2());
        _builder.append(_generate_2);
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Or) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Or)e).getLeft());
        _builder.append(_generate);
        _builder.append(") != 0 or (");
        String _generate_1 = this.generate(((Or)e).getRight());
        _builder.append(_generate_1);
        _builder.append(") != 0");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof And) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((And)e).getLeft());
        _builder.append(_generate);
        _builder.append(") != 0 and (");
        String _generate_1 = this.generate(((And)e).getRight());
        _builder.append(_generate_1);
        _builder.append(") != 0");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitOr) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((BitOr)e).getLeft());
        _builder.append(_generate);
        _builder.append(") | (");
        String _generate_1 = this.generate(((BitOr)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitXor) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((BitXor)e).getLeft());
        _builder.append(_generate);
        _builder.append(") ^ (");
        String _generate_1 = this.generate(((BitXor)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitAnd) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((BitAnd)e).getLeft());
        _builder.append(_generate);
        _builder.append(") & (");
        String _generate_1 = this.generate(((BitAnd)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof BitNot) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("~(");
        String _generate = this.generate(((BitNot)e).getExpression());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Equality) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Equality)e).getLeft());
        _builder.append(_generate);
        _builder.append(") ");
        String _op = ((Equality)e).getOp();
        _builder.append(_op);
        _builder.append(" (");
        String _generate_1 = this.generate(((Equality)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Comparison) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Comparison)e).getLeft());
        _builder.append(_generate);
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
        String _generate_1 = this.generate(((Comparison)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Shift) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Shift)e).getLeft());
        _builder.append(_generate);
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
        String _generate_1 = this.generate(((Shift)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Plus) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Plus)e).getLeft());
        _builder.append(_generate);
        _builder.append(") + (");
        String _generate_1 = this.generate(((Plus)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Minus) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Minus)e).getLeft());
        _builder.append(_generate);
        _builder.append(") - (");
        String _generate_1 = this.generate(((Minus)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Mul) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Mul)e).getLeft());
        _builder.append(_generate);
        _builder.append(") * (");
        String _generate_1 = this.generate(((Mul)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Div) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Div)e).getLeft());
        _builder.append(_generate);
        _builder.append(") / (");
        String _generate_1 = this.generate(((Div)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Rem) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("(");
        String _generate = this.generate(((Rem)e).getLeft());
        _builder.append(_generate);
        _builder.append(") % (");
        String _generate_1 = this.generate(((Rem)e).getRight());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof SequenceCall) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        String _seqname = ((SequenceCall)e).getSeqname();
        _builder.append(_seqname);
        _builder.append(DsqPythonScriptGenerator.predefinedVars);
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Query) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DS.Query(");
        String _generate = this.generate(((Query)e).getType());
        _builder.append(_generate);
        _builder.append(", \"");
        String _message = ((Query)e).getMessage();
        _builder.append(_message);
        _builder.append("\", ");
        String _generate_1 = this.generate(((Query)e).getDefault());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof QueryValue) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DS.Query(");
        _builder.append(IDsqClient.QUERY_VALUE_TYPE);
        _builder.append(", \"");
        String _message = ((QueryValue)e).getMessage();
        _builder.append(_message);
        _builder.append("\", ");
        String _generate = this.generate(((QueryValue)e).getDefault());
        _builder.append(_generate);
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
        _builder.append(_path);
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
        _builder.append(_format);
        _builder.append("\"");
        {
          EList<Parameter> _parameters = ((Message)e).getParameters();
          for(final Parameter p : _parameters) {
            _builder.append(", ");
            String _generate = this.generate(p);
            _builder.append(_generate);
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
        String _generate = this.generate(((Read8)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read16) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read16(");
        String _generate = this.generate(((Read16)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read32) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read32(");
        String _generate = this.generate(((Read32)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Read64) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Read64(");
        String _generate = this.generate(((Read64)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof ReadAP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("ReadAP(");
        String _generate = this.generate(((ReadAP)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof ReadDP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("ReadDP(");
        String _generate = this.generate(((ReadDP)e).getAddr());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write8) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write8(");
        String _generate = this.generate(((Write8)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((Write8)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write16) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write16(");
        String _generate = this.generate(((Write16)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((Write16)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write32) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write32(");
        String _generate = this.generate(((Write32)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((Write32)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof Write64) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Write64(");
        String _generate = this.generate(((Write64)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((Write64)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof WriteAP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("WriteAP(");
        String _generate = this.generate(((WriteAP)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((WriteAP)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof WriteDP) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("WriteDP(");
        String _generate = this.generate(((WriteDP)e).getAddr());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((WriteDP)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapDelay) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_Delay(");
        String _generate = this.generate(((DapDelay)e).getDelay());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapWriteABORT) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_WriteABORT(");
        String _generate = this.generate(((DapWriteABORT)e).getValue());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjPins) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Pins(");
        String _generate = this.generate(((DapSwjPins)e).getPinout());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((DapSwjPins)e).getPinselect());
        _builder.append(_generate_1);
        _builder.append(", ");
        String _generate_2 = this.generate(((DapSwjPins)e).getPinwait());
        _builder.append(_generate_2);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjClock) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Clock(");
        String _generate = this.generate(((DapSwjClock)e).getValue());
        _builder.append(_generate);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjSequence) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_SWJ_Sequence(");
        String _generate = this.generate(((DapSwjSequence)e).getCnt());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((DapSwjSequence)e).getVal());
        _builder.append(_generate_1);
        _builder.append(")");
        _switchResult = _builder.toString();
      }
    }
    if (!_matched) {
      if (e instanceof DapJtagSequence) {
        _matched=true;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DAP_JTAG_Sequence(");
        String _generate = this.generate(((DapJtagSequence)e).getCnt());
        _builder.append(_generate);
        _builder.append(", ");
        String _generate_1 = this.generate(((DapJtagSequence)e).getTms());
        _builder.append(_generate_1);
        _builder.append(", ");
        String _generate_2 = this.generate(((DapJtagSequence)e).getTdi());
        _builder.append(_generate_2);
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
