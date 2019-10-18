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
package com.arm.cmsis.pack.debugseq.engine;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDebugVars;
import com.arm.cmsis.pack.data.ICpSequence;
import com.arm.cmsis.pack.debugseq.DebugSeqStandaloneSetup;
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
import com.arm.cmsis.pack.debugseq.generator.DsqScriptGeneratorFactory;
import com.arm.cmsis.pack.debugseq.generator.IDsqScriptGenerator;
import com.arm.cmsis.pack.debugseq.util.DebugSeqUtil;
import com.arm.cmsis.pack.dsq.DsqCommand;
import com.arm.cmsis.pack.dsq.DsqException;
import com.arm.cmsis.pack.dsq.IDsqClient;
import com.arm.cmsis.pack.dsq.IDsqCommand;
import com.arm.cmsis.pack.dsq.IDsqContext;
import com.arm.cmsis.pack.dsq.IDsqEngine;
import com.arm.cmsis.pack.dsq.IDsqLogger;
import com.arm.cmsis.pack.dsq.IDsqSequence;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.parser.PdscParser;
import com.arm.cmsis.pack.utils.Utils;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class DebugSeqEngine implements IDsqEngine {
  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;
  
  @Inject
  private IResourceValidator validator;
  
  private Resource resource;
  
  private IDsqScriptGenerator generator;
  
  private Stack<Map<String, Long>> contexts = new Stack<Map<String, Long>>();
  
  private ICpDeviceInfo deviceInfo;
  
  private IDsqClient debugSeqClient;
  
  private IDsqLogger logger;
  
  private DebugSeqModel dsqModel;
  
  private String debugVars;
  
  private boolean inAtomic = false;
  
  private boolean collectingCommands = false;
  
  private List<IDsqCommand> commands;
  
  private int commandIndex;
  
  @Inject
  protected DebugSeqEngine(final IDsqClient dsqClient) {
    this.debugSeqClient = dsqClient;
  }
  
  public DebugSeqEngine(final ICpDeviceInfo devInfo, final IDsqClient dsqClient, final IDsqLogger log) {
    this.deviceInfo = devInfo;
    this.debugSeqClient = dsqClient;
    this.logger = log;
    this.debugVars = "";
    new DebugSeqStandaloneSetup().createInjectorAndDoEMFRegistration().injectMembers(this);
  }
  
  @Override
  public Collection<String> getDefaultSequenceNames() {
    List<String> _xblockexpression = null;
    {
      if ((this.dsqModel == null)) {
        this.dsqModel = this.parse();
      }
      List<String> _xifexpression = null;
      if ((this.dsqModel != null)) {
        final Function1<Sequence, String> _function = (Sequence it) -> {
          return it.getName();
        };
        final Function1<String, Boolean> _function_1 = (String it) -> {
          return Boolean.valueOf(this.getDefaultSqs().contains(it));
        };
        _xifexpression = IterableExtensions.<String>toList(IterableExtensions.<String>filter(ListExtensions.<Sequence, String>map(this.dsqModel.getSequences().getSequences(), _function), _function_1));
      } else {
        _xifexpression = CollectionLiterals.<String>newArrayList();
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  @Override
  public boolean isSequenceDisabled(final String sequenceName) {
    if ((this.dsqModel == null)) {
      this.dsqModel = this.parse();
    }
    if ((this.dsqModel == null)) {
      return true;
    }
    final Function1<Sequence, Boolean> _function = (Sequence it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, sequenceName));
    };
    final Sequence seq = IterableExtensions.<Sequence>findFirst(this.dsqModel.getSequences().getSequences(), _function);
    return ((seq == null) || (seq.getDisable() != 0));
  }
  
  @Override
  public void execute(final IDsqSequence seqContext) throws DsqException {
    final Sequence seq = this.getSequence(seqContext);
    if ((seq != null)) {
      this.contexts.clear();
      this.enterScope(false);
      this.setPredefinedVariableValues(seqContext);
      this.interpret(this.dsqModel.getDebugvars());
      this.interpret(seq);
      this.exitScope();
    } else {
      boolean _isEmptyDefaultSequence = DebugSeqUtil.isEmptyDefaultSequence(seqContext.getSequenceName());
      boolean _not = (!_isEmptyDefaultSequence);
      if (_not) {
        String _sequenceName = seqContext.getSequenceName();
        String _plus = ("Sequence named \'" + _sequenceName);
        String _plus_1 = (_plus + "\' is undefined");
        throw new DsqException(_plus_1);
      }
    }
  }
  
  @Override
  public String generateCode(final String generatorID, final String header) throws DsqException {
    String _xblockexpression = null;
    {
      if ((this.generator == null)) {
        this.generator = DsqScriptGeneratorFactory.getInstance().getExtender(generatorID);
        if ((this.generator == null)) {
          return null;
        }
      }
      if ((this.dsqModel == null)) {
        this.dsqModel = this.parse();
      }
      _xblockexpression = this.generator.generate(this.dsqModel, header);
    }
    return _xblockexpression;
  }
  
  private Sequence getSequence(final IDsqSequence seqContext) throws DsqException {
    if ((seqContext == null)) {
      throw new DsqException("Predefined variables are not provided");
    }
    this.checkPredefinedVariables(seqContext);
    if ((this.dsqModel == null)) {
      this.dsqModel = this.parse();
    }
    final Function1<Sequence, Boolean> _function = (Sequence it) -> {
      return Boolean.valueOf((Objects.equal(it.getName(), seqContext.getSequenceName()) && Objects.equal(it.getPname(), this.deviceInfo.getProcessorName())));
    };
    final Sequence matchedSeq = IterableExtensions.<Sequence>findFirst(this.dsqModel.getSequences().getSequences(), _function);
    Sequence _xifexpression = null;
    if ((matchedSeq != null)) {
      _xifexpression = matchedSeq;
    } else {
      final Function1<Sequence, Boolean> _function_1 = (Sequence it) -> {
        String _name = it.getName();
        String _sequenceName = seqContext.getSequenceName();
        return Boolean.valueOf(Objects.equal(_name, _sequenceName));
      };
      _xifexpression = IterableExtensions.<Sequence>findFirst(this.dsqModel.getSequences().getSequences(), _function_1);
    }
    final Sequence seq = _xifexpression;
    return seq;
  }
  
  private void initDebugVariables() throws DsqException {
    try {
      ICpDebugVars _xifexpression = null;
      ICpDebugConfiguration _debugConfiguration = this.deviceInfo.getDebugConfiguration();
      boolean _tripleEquals = (_debugConfiguration == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.deviceInfo.getDebugConfiguration().getDebugVars();
      }
      final ICpDebugVars dv = _xifexpression;
      String _xifexpression_1 = null;
      if ((dv == null)) {
        _xifexpression_1 = "";
      } else {
        String _xmlString = dv.attributes().toXmlString();
        _xifexpression_1 = (" " + _xmlString);
      }
      final String attributes = _xifexpression_1;
      String _xifexpression_2 = null;
      if ((dv == null)) {
        _xifexpression_2 = "";
      } else {
        _xifexpression_2 = dv.getText();
      }
      final String initialBody = _xifexpression_2;
      final StringBuilder sb = new StringBuilder(((("<debugvars" + attributes) + ">") + "\n"));
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("\t\t    ");
      _builder.append(initialBody, "\t\t    ");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.AP);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.DP);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.PROTOCOL);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.CONNECTION);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.TRACEOUT);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.ERRORCONTROL);
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      sb.append(_builder);
      if ((dv != null)) {
        final String dgbConfFileName = this.deviceInfo.getDgbConfFileName();
        if (((dgbConfFileName != null) && (!dgbConfFileName.isEmpty()))) {
          final Path path = Paths.get(dgbConfFileName);
          boolean _exists = Files.exists(path);
          if (_exists) {
            final String text = this.readFile(path.toUri().toURL());
            sb.append(text);
          }
        }
      }
      sb.append("</debugvars>");
      this.debugVars = sb.toString();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void checkPredefinedVariables(final IDsqContext dsqContext) {
    final Long ap = dsqContext.getPredefinedVariableValue(IDsqContext.AP);
    if ((ap == null)) {
      throw new DsqException((("Variable " + IDsqContext.AP) + " is not provided with a default value"));
    }
    final Long dp = dsqContext.getPredefinedVariableValue(IDsqContext.DP);
    if ((dp == null)) {
      throw new DsqException((("Variable " + IDsqContext.DP) + " is not provided with a default value"));
    }
    final Long p = dsqContext.getPredefinedVariableValue(IDsqContext.PROTOCOL);
    if ((p == null)) {
      throw new DsqException((("Variable " + IDsqContext.PROTOCOL) + " is not provided with a default value"));
    }
    final Long c = dsqContext.getPredefinedVariableValue(IDsqContext.CONNECTION);
    if ((c == null)) {
      throw new DsqException((("Variable " + IDsqContext.CONNECTION) + " is not provided with a default value"));
    }
    final Long tc = dsqContext.getPredefinedVariableValue(IDsqContext.TRACEOUT);
    if ((tc == null)) {
      throw new DsqException((("Variable " + IDsqContext.TRACEOUT) + " is not provided with a default value"));
    }
    final Long ec = dsqContext.getPredefinedVariableValue(IDsqContext.ERRORCONTROL);
    if ((ec == null)) {
      throw new DsqException((("Variable " + IDsqContext.ERRORCONTROL) + " is not provided with a default value"));
    }
  }
  
  private void setPredefinedVariableValues(final IDsqContext dsqContext) {
    final Long ap = dsqContext.getPredefinedVariableValue(IDsqContext.AP);
    final Long dp = dsqContext.getPredefinedVariableValue(IDsqContext.DP);
    final Long p = dsqContext.getPredefinedVariableValue(IDsqContext.PROTOCOL);
    final Long c = dsqContext.getPredefinedVariableValue(IDsqContext.CONNECTION);
    final Long tc = dsqContext.getPredefinedVariableValue(IDsqContext.TRACEOUT);
    final Long ec = dsqContext.getPredefinedVariableValue(IDsqContext.ERRORCONTROL);
    if ((ap != null)) {
      this.contexts.peek().put(IDsqContext.AP, ap);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.AP) + " = ") + ap), Long.valueOf(0L), 0);
      }
    }
    if ((dp != null)) {
      this.contexts.peek().put(IDsqContext.DP, dp);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.DP) + " = ") + dp), Long.valueOf(0L), 0);
      }
    }
    if ((p != null)) {
      this.contexts.peek().put(IDsqContext.PROTOCOL, p);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.PROTOCOL) + " = ") + p), Long.valueOf(0L), 0);
      }
    }
    if ((c != null)) {
      this.contexts.peek().put(IDsqContext.CONNECTION, c);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.CONNECTION) + " = ") + c), Long.valueOf(0L), 0);
      }
    }
    if ((tc != null)) {
      this.contexts.peek().put(IDsqContext.TRACEOUT, tc);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.TRACEOUT) + " = ") + tc), Long.valueOf(0L), 0);
      }
    }
    if ((ec != null)) {
      this.contexts.peek().put(IDsqContext.ERRORCONTROL, ec);
      if ((this.logger != null)) {
        this.logger.logStatement(((("__var " + IDsqContext.ERRORCONTROL) + " = ") + ec), Long.valueOf(0L), 0);
      }
    }
  }
  
  /**
   * Parses all sequences provided by device
   * @return The root node of the parsed model
   */
  private DebugSeqModel parse() throws DsqException {
    EObject _get = this.getResource().getContents().get(0);
    return ((DebugSeqModel) _get);
  }
  
  private String getSequencesAsString(final Map<String, ICpSequence> sequences) {
    if (((sequences == null) || sequences.isEmpty())) {
      return CmsisConstants.EMPTY_STRING;
    }
    final PdscParser xmlParser = new PdscParser();
    final Function1<ICpSequence, String> _function = (ICpSequence it) -> {
      return xmlParser.writeToXmlString(it);
    };
    return this.postProcess(IterableExtensions.join(IterableExtensions.<ICpSequence, String>map(sequences.values(), _function), "\n"));
  }
  
  private Resource getResource() {
    try {
      if ((this.resource != null)) {
        return this.resource;
      }
      this.initDebugVariables();
      Map<String, ICpSequence> _xifexpression = null;
      ICpDebugConfiguration _debugConfiguration = this.deviceInfo.getDebugConfiguration();
      boolean _tripleEquals = (_debugConfiguration == null);
      if (_tripleEquals) {
        _xifexpression = null;
      } else {
        _xifexpression = this.deviceInfo.getDebugConfiguration().getSequences();
      }
      final Map<String, ICpSequence> sequences = _xifexpression;
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(this.debugVars);
      _builder.newLineIfNotEmpty();
      _builder.append("<sequences>");
      _builder.newLine();
      String _sequencesAsString = this.getSequencesAsString(sequences);
      _builder.append(_sequencesAsString);
      _builder.newLineIfNotEmpty();
      String _addDefaultSeqs = this.addDefaultSeqs(sequences);
      _builder.append(_addDefaultSeqs);
      _builder.newLineIfNotEmpty();
      _builder.append("</sequences>");
      _builder.newLine();
      final String modelString = _builder.toString();
      final XtextResourceSet resourceSet = this.resourceSetProvider.get();
      resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      this.resource = resourceSet.createResource(URI.createURI("dummy:/dummy.dsq"));
      StringInputStream _stringInputStream = new StringInputStream(modelString);
      this.resource.load(_stringInputStream, resourceSet.getLoadOptions());
      final List<Issue> issues = this.validator.validate(this.resource, CheckMode.ALL, CancelIndicator.NullImpl);
      boolean _isEmpty = issues.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        this.dsqModel = null;
        final Function1<Issue, String> _function = (Issue it) -> {
          String _message = it.getMessage();
          String _plus = (_message + ":\n");
          Integer _offset = it.getOffset();
          Integer _offset_1 = it.getOffset();
          Integer _length = it.getLength();
          int _plus_1 = ((_offset_1).intValue() + (_length).intValue());
          int _plus_2 = (_plus_1 + 1);
          String _substring = modelString.substring((_offset).intValue(), _plus_2);
          return (_plus + _substring);
        };
        final String errors = IterableExtensions.join(ListExtensions.<Issue, String>map(issues, _function), "\n\n");
        String _fileName = this.deviceInfo.getPack().getFileName();
        String _plus = ("Error while validating the debug sequences in pack file:\n" + _fileName);
        String _plus_1 = (_plus + "\n\nDevice: ");
        String _fullDeviceName = this.deviceInfo.getFullDeviceName();
        String _plus_2 = (_plus_1 + _fullDeviceName);
        String _plus_3 = (_plus_2 + "\n\n");
        String _plus_4 = (_plus_3 + errors);
        throw new DsqException(_plus_4);
      }
      return this.resource;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private String readFile(final URL url) {
    try {
      String _xblockexpression = null;
      {
        String result = "";
        final InputStream inputStream = url.openConnection().getInputStream();
        InputStreamReader _inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader in = new BufferedReader(_inputStreamReader);
        String inputLine = "";
        while (((inputLine = in.readLine()) != null)) {
          String _result = result;
          result = (_result + (inputLine + "\n"));
        }
        in.close();
        _xblockexpression = result;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private Collection<String> getDefaultSqs() {
    try {
      List<String> _xblockexpression = null;
      {
        URL _uRL = new URL("platform:/plugin/com.arm.cmsis.pack.dsq.engine/default_sequences");
        final URL url = FileLocator.toFileURL(_uRL);
        String _file = url.getFile();
        final File defaultSeqsFolder = new File(_file);
        final Function1<String, String> _function = (String it) -> {
          return Utils.extractBaseFileName(it);
        };
        _xblockexpression = ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(defaultSeqsFolder.list())), _function);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private String addDefaultSeqs(final Map<String, ICpSequence> sequences) {
    try {
      String seqs = "";
      Collection<String> _defaultSqs = this.getDefaultSqs();
      for (final String defaultSeqName : _defaultSqs) {
        if (((sequences == null) || (!sequences.containsKey(defaultSeqName)))) {
          String _seqs = seqs;
          URL _uRL = new URL((("platform:/plugin/com.arm.cmsis.pack.dsq.engine/default_sequences/" + defaultSeqName) + ".dsq"));
          String _readFile = this.readFile(_uRL);
          seqs = (_seqs + _readFile);
        }
      }
      return seqs;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected Long _interpret(final Void obj) throws DsqException {
    return Long.valueOf(0L);
  }
  
  protected Long _interpret(final DebugVars debugvars) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      final Function1<VariableDeclaration, Boolean> _function = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.AP)));
      };
      final Function1<VariableDeclaration, Boolean> _function_1 = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.DP)));
      };
      final Function1<VariableDeclaration, Boolean> _function_2 = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.CONNECTION)));
      };
      final Function1<VariableDeclaration, Boolean> _function_3 = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.PROTOCOL)));
      };
      final Function1<VariableDeclaration, Boolean> _function_4 = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.TRACEOUT)));
      };
      final Function1<VariableDeclaration, Boolean> _function_5 = (VariableDeclaration it) -> {
        String _name = it.getName();
        return Boolean.valueOf((!Objects.equal(_name, IDsqContext.ERRORCONTROL)));
      };
      final Iterable<VariableDeclaration> stats = IterableExtensions.<VariableDeclaration>filter(IterableExtensions.<VariableDeclaration>filter(IterableExtensions.<VariableDeclaration>filter(IterableExtensions.<VariableDeclaration>filter(IterableExtensions.<VariableDeclaration>filter(IterableExtensions.<VariableDeclaration>filter(Iterables.<VariableDeclaration>filter(debugvars.getStatements(), VariableDeclaration.class), _function), _function_1), _function_2), _function_3), _function_4), _function_5);
      final Consumer<VariableDeclaration> _function_6 = (VariableDeclaration it) -> {
        this.log(this.logger, it, Long.valueOf(DebugSeqUtil.toLong(this.interpret(it))));
      };
      stats.forEach(_function_6);
      _xblockexpression = 0L;
    }
    return Long.valueOf(_xblockexpression);
  }
  
  protected Long _interpret(final Sequence seq) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      if ((this.logger != null)) {
        this.logger.logSeqStart(seq.getName());
      }
      this.enterScope(true);
      EList<CodeBlock> _codeblocks = seq.getCodeblocks();
      boolean _tripleNotEquals = (_codeblocks != null);
      if (_tripleNotEquals) {
        final Consumer<CodeBlock> _function = (CodeBlock it) -> {
          this.interpret(it);
        };
        seq.getCodeblocks().forEach(_function);
      }
      this.exitScope();
      if ((this.logger != null)) {
        this.logger.logSeqEnd(seq.getName());
      }
      _xblockexpression = 0L;
    }
    return Long.valueOf(_xblockexpression);
  }
  
  protected Long _interpret(final Block block) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      if ((this.logger != null)) {
        long _atomic = block.getAtomic();
        boolean _tripleNotEquals = (_atomic != 0);
        this.logger.logBlockStart(_tripleNotEquals, block.getInfo());
      }
      final Stack<Map<String, Long>> tempContexts = new Stack<Map<String, Long>>();
      long _atomic_1 = block.getAtomic();
      boolean _tripleNotEquals_1 = (_atomic_1 != 0);
      if (_tripleNotEquals_1) {
        this.inAtomic = true;
        this.collectingCommands = true;
        this.commands = CollectionLiterals.<IDsqCommand>newArrayList();
        final Consumer<Map<String, Long>> _function = (Map<String, Long> it) -> {
          HashMap<String, Long> _hashMap = new HashMap<String, Long>(it);
          tempContexts.push(_hashMap);
        };
        this.contexts.forEach(_function);
      }
      this.interpretUntilLast(block.getStatements());
      Object result = this.interpret(IterableExtensions.<Statement>last(block.getStatements()));
      this.log(this.logger, IterableExtensions.<Statement>last(block.getStatements()), Long.valueOf(DebugSeqUtil.toLong(result)));
      if (this.inAtomic) {
        this.debugSeqClient.execute(this.commands, true);
        this.contexts = tempContexts;
        this.collectingCommands = false;
        this.commandIndex = 0;
        this.interpretUntilLast(block.getStatements());
        result = this.interpret(IterableExtensions.<Statement>last(block.getStatements()));
        this.log(this.logger, IterableExtensions.<Statement>last(block.getStatements()), Long.valueOf(DebugSeqUtil.toLong(result)));
        this.inAtomic = false;
        this.commands.clear();
      }
      if ((this.logger != null)) {
        this.logger.logBlockEnd();
      }
      _xblockexpression = DebugSeqUtil.toLong(result);
    }
    return Long.valueOf(_xblockexpression);
  }
  
  protected Long _interpret(final Control control) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      long result = 0L;
      if ((this.logger != null)) {
        this.logger.logContorlStart(control.getInfo());
      }
      this.enterScope(false);
      if (((control.getIf() == null) || ((this.logIf(this.logger, control.getIf(), Long.valueOf(DebugSeqUtil.toLong(this.interpret(control.getIf()))))).longValue() != 0))) {
        Expression _while = control.getWhile();
        boolean _tripleEquals = (_while == null);
        if (_tripleEquals) {
          this.interpretUntilLast(control.getCodeblocks());
          result = DebugSeqUtil.toLong(this.interpret(IterableExtensions.<CodeBlock>last(control.getCodeblocks())));
        } else {
          long timeout = control.getTimeout();
          if ((timeout == 0)) {
            timeout = Long.MAX_VALUE;
          }
          long runningTime = 0L;
          final long startTime = System.nanoTime();
          while ((((this.logWhile(this.logger, control.getWhile(), Long.valueOf(DebugSeqUtil.toLong(this.interpret(control.getWhile()))))).longValue() != 0) && (runningTime < timeout))) {
            {
              this.interpretUntilLast(control.getCodeblocks());
              result = DebugSeqUtil.toLong(this.interpret(IterableExtensions.<CodeBlock>last(control.getCodeblocks())));
              long _nanoTime = System.nanoTime();
              long _minus = (_nanoTime - startTime);
              long _divide = (_minus / 1000);
              runningTime = _divide;
            }
          }
        }
      }
      this.exitScope();
      if ((this.logger != null)) {
        this.logger.logControlEnd();
      }
      _xblockexpression = result;
    }
    return Long.valueOf(_xblockexpression);
  }
  
  protected Object _interpret(final Expression e) throws DsqException {
    Object _switchResult = null;
    boolean _matched = false;
    if (e instanceof IntConstant) {
      _matched=true;
      _switchResult = Long.valueOf(((IntConstant)e).getValue());
    }
    if (!_matched) {
      if (e instanceof StringConstant) {
        _matched=true;
        _switchResult = ((StringConstant)e).getValue();
      }
    }
    if (!_matched) {
      if (e instanceof VariableRef) {
        _matched=true;
        Long _xblockexpression = null;
        {
          final String k = ((VariableRef)e).getVariable().getName();
          _xblockexpression = this.getContext(k).get(k);
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (e instanceof Not) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Not)e).getExpression()));
        _switchResult = Long.valueOf(DebugSeqUtil.toLong(Boolean.valueOf((_long == 0))));
      }
    }
    if (!_matched) {
      if (e instanceof Assignment) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          Expression _left = ((Assignment)e).getLeft();
          final String k = ((VariableRef) _left).getVariable().getName();
          final Long i = this.getContext(k).get(k);
          final long v = DebugSeqUtil.toLong(this.interpret(((Assignment)e).getRight()));
          long _switchResult_1 = (long) 0;
          String _op = ((Assignment)e).getOp();
          if (_op != null) {
            switch (_op) {
              case "=":
                _switchResult_1 = this.updateValue(k, v);
                break;
              case "+=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() + v));
                break;
              case "-=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() - v));
                break;
              case "*=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() * v));
                break;
              case "/=":
                long _xblockexpression_1 = (long) 0;
                {
                  final long value = Long.divideUnsigned((i).longValue(), v);
                  _xblockexpression_1 = this.updateValue(k, value);
                }
                _switchResult_1 = _xblockexpression_1;
                break;
              case "%=":
                long _xblockexpression_2 = (long) 0;
                {
                  final long value = Long.remainderUnsigned((i).longValue(), v);
                  _xblockexpression_2 = this.updateValue(k, value);
                }
                _switchResult_1 = _xblockexpression_2;
                break;
              case "&lt;&lt;=":
                int _integer = DebugSeqUtil.toInteger(Long.valueOf(v));
                long _doubleLessThan = ((i).longValue() << _integer);
                _switchResult_1 = this.updateValue(k, _doubleLessThan);
                break;
              case "&gt;&gt;=":
                int _integer_1 = DebugSeqUtil.toInteger(Long.valueOf(v));
                long _doubleGreaterThan = ((i).longValue() >> _integer_1);
                _switchResult_1 = this.updateValue(k, _doubleGreaterThan);
                break;
              case "&amp;=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() & v));
                break;
              case "^=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() ^ v));
                break;
              case "|=":
                _switchResult_1 = this.updateValue(k, ((i).longValue() | v));
                break;
              default:
                _switchResult_1 = 0L;
                break;
            }
          } else {
            _switchResult_1 = 0L;
          }
          _xblockexpression = _switchResult_1;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Ternary) {
        _matched=true;
        Object _xifexpression = null;
        Object _interpret = this.interpret(((Ternary)e).getLeft());
        boolean _tripleNotEquals = (_interpret != Integer.valueOf(0));
        if (_tripleNotEquals) {
          _xifexpression = this.interpret(((Ternary)e).getExp1());
        } else {
          _xifexpression = this.interpret(((Ternary)e).getExp2());
        }
        _switchResult = _xifexpression;
      }
    }
    if (!_matched) {
      if (e instanceof Or) {
        _matched=true;
        _switchResult = Long.valueOf(DebugSeqUtil.toLong(Boolean.valueOf(((DebugSeqUtil.toLong(this.interpret(((Or)e).getLeft())) != 0) || (DebugSeqUtil.toLong(this.interpret(((Or)e).getRight())) != 0)))));
      }
    }
    if (!_matched) {
      if (e instanceof And) {
        _matched=true;
        _switchResult = Long.valueOf(DebugSeqUtil.toLong(Boolean.valueOf(((DebugSeqUtil.toLong(this.interpret(((And)e).getLeft())) != 0) && (DebugSeqUtil.toLong(this.interpret(((And)e).getRight())) != 0)))));
      }
    }
    if (!_matched) {
      if (e instanceof BitOr) {
        _matched=true;
        _switchResult = Long.valueOf((DebugSeqUtil.toLong(this.interpret(((BitOr)e).getLeft())) | DebugSeqUtil.toLong(this.interpret(((BitOr)e).getRight()))));
      }
    }
    if (!_matched) {
      if (e instanceof BitXor) {
        _matched=true;
        _switchResult = Long.valueOf((DebugSeqUtil.toLong(this.interpret(((BitXor)e).getLeft())) ^ DebugSeqUtil.toLong(this.interpret(((BitXor)e).getRight()))));
      }
    }
    if (!_matched) {
      if (e instanceof BitAnd) {
        _matched=true;
        _switchResult = Long.valueOf((DebugSeqUtil.toLong(this.interpret(((BitAnd)e).getLeft())) & DebugSeqUtil.toLong(this.interpret(((BitAnd)e).getRight()))));
      }
    }
    if (!_matched) {
      if (e instanceof BitNot) {
        _matched=true;
        _switchResult = Long.valueOf((~DebugSeqUtil.toLong(this.interpret(((BitNot)e).getExpression()))));
      }
    }
    if (!_matched) {
      if (e instanceof Equality) {
        _matched=true;
        long _xifexpression = (long) 0;
        String _op = ((Equality)e).getOp();
        boolean _equals = Objects.equal(_op, "==");
        if (_equals) {
          Object _interpret = this.interpret(((Equality)e).getLeft());
          Object _interpret_1 = this.interpret(((Equality)e).getRight());
          _xifexpression = DebugSeqUtil.toLong(Boolean.valueOf(Objects.equal(_interpret, _interpret_1)));
        } else {
          Object _interpret_2 = this.interpret(((Equality)e).getLeft());
          Object _interpret_3 = this.interpret(((Equality)e).getRight());
          _xifexpression = DebugSeqUtil.toLong(Boolean.valueOf((!Objects.equal(_interpret_2, _interpret_3))));
        }
        _switchResult = Long.valueOf(_xifexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Comparison) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          final long left = DebugSeqUtil.toLong(this.interpret(((Comparison)e).getLeft()));
          final long right = DebugSeqUtil.toLong(this.interpret(((Comparison)e).getRight()));
          long _switchResult_1 = (long) 0;
          String _op = ((Comparison)e).getOp();
          if (_op != null) {
            switch (_op) {
              case "&lt;":
                int _compareUnsigned = Long.compareUnsigned(left, right);
                _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf((_compareUnsigned < 0)));
                break;
              case "&gt;":
                int _compareUnsigned_1 = Long.compareUnsigned(left, right);
                _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf((_compareUnsigned_1 > 0)));
                break;
              case "&lt;=":
                int _compareUnsigned_2 = Long.compareUnsigned(left, right);
                _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf((_compareUnsigned_2 <= 0)));
                break;
              case "&gt;=":
                int _compareUnsigned_3 = Long.compareUnsigned(left, right);
                _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf((_compareUnsigned_3 >= 0)));
                break;
              default:
                _switchResult_1 = 0L;
                break;
            }
          } else {
            _switchResult_1 = 0L;
          }
          _xblockexpression = _switchResult_1;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Shift) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          final long left = DebugSeqUtil.toLong(this.interpret(((Shift)e).getLeft()));
          final int right = DebugSeqUtil.toInteger(Long.valueOf(DebugSeqUtil.toLong(this.interpret(((Shift)e).getRight()))));
          long _switchResult_1 = (long) 0;
          String _op = ((Shift)e).getOp();
          if (_op != null) {
            switch (_op) {
              case "&lt;&lt;":
                _switchResult_1 = (left << right);
                break;
              case "&gt;&gt;":
                _switchResult_1 = (left >> right);
                break;
              default:
                _switchResult_1 = 0L;
                break;
            }
          } else {
            _switchResult_1 = 0L;
          }
          _xblockexpression = _switchResult_1;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Plus) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Plus)e).getLeft()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Plus)e).getRight()));
        _switchResult = Long.valueOf((_long + _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Minus) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Minus)e).getLeft()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Minus)e).getRight()));
        _switchResult = Long.valueOf((_long - _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Mul) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Mul)e).getLeft()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Mul)e).getRight()));
        _switchResult = Long.valueOf((_long * _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Div) {
        _matched=true;
        _switchResult = Long.valueOf(Long.divideUnsigned(DebugSeqUtil.toLong(this.interpret(((Div)e).getLeft())), DebugSeqUtil.toLong(this.interpret(((Div)e).getRight()))));
      }
    }
    if (!_matched) {
      if (e instanceof Rem) {
        _matched=true;
        _switchResult = Long.valueOf(Long.remainderUnsigned(DebugSeqUtil.toLong(this.interpret(((Rem)e).getLeft())), DebugSeqUtil.toLong(this.interpret(((Rem)e).getRight()))));
      }
    }
    if (!_matched) {
      if (e instanceof SequenceCall) {
        _matched=true;
        Object _xblockexpression = null;
        {
          final Function1<Sequence, Boolean> _function = (Sequence it) -> {
            String _name = it.getName();
            String _seqname = ((SequenceCall)e).getSeqname();
            return Boolean.valueOf(Objects.equal(_name, _seqname));
          };
          final Sequence seq = IterableExtensions.<Sequence>findFirst(DebugSeqUtil.containingSequences(e).getSequences(), _function);
          Object _xifexpression = null;
          if ((seq != null)) {
            _xifexpression = this.interpret(seq);
          } else {
            Object _xifexpression_1 = null;
            boolean _isEmptyDefaultSequence = DebugSeqUtil.isEmptyDefaultSequence(((SequenceCall)e).getSeqname());
            boolean _not = (!_isEmptyDefaultSequence);
            if (_not) {
              String _seqname = ((SequenceCall)e).getSeqname();
              String _plus = ("Sequence with name \'" + _seqname);
              String _plus_1 = (_plus + "\' is undefined");
              throw new DsqException(_plus_1);
            }
            _xifexpression = _xifexpression_1;
          }
          _xblockexpression = _xifexpression;
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (e instanceof Query) {
        _matched=true;
        _switchResult = Long.valueOf(this.debugSeqClient.query(DebugSeqUtil.toLong(this.interpret(((Query)e).getType())), ((Query)e).getMessage(), DebugSeqUtil.toLong(this.interpret(((Query)e).getDefault()))));
      }
    }
    if (!_matched) {
      if (e instanceof QueryValue) {
        _matched=true;
        _switchResult = Long.valueOf(this.debugSeqClient.query(IDsqClient.QUERY_VALUE_TYPE, ((QueryValue)e).getMessage(), DebugSeqUtil.toLong(this.interpret(((QueryValue)e).getDefault()))));
      }
    }
    if (!_matched) {
      if (e instanceof LoadDebugInfo) {
        _matched=true;
        String _absolutePath = this.deviceInfo.getAbsolutePath(((LoadDebugInfo)e).getPath());
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_LOAD_DEBUG_INFO, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList()), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_absolutePath))));
      }
    }
    if (!_matched) {
      if (e instanceof Message) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          final Function1<Parameter, Object> _function = (Parameter it) -> {
            return this.interpret(it);
          };
          final List<Object> parameters = ListExtensions.<Parameter, Object>map(((Message)e).getParameters(), _function);
          final String message = DebugSeqUtil.formatWithValues(((Message)e).getFormat(), parameters);
          long _long = DebugSeqUtil.toLong(this.interpret(((Message)e).getType()));
          _xblockexpression = this.executeCommand(IDsqCommand.DSQ_MESSAGE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long))), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(message)));
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Read8) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Read8)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_8, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read16) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Read16)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_16, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read32) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Read32)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_32, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read64) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Read64)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_64, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof ReadAP) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((ReadAP)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_AP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof ReadDP) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((ReadDP)e).getAddr()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_DP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write8) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Write8)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Write8)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_8, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write16) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Write16)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Write16)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_16, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write32) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Write32)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Write32)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_32, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write64) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((Write64)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((Write64)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_64, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof WriteAP) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((WriteAP)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((WriteAP)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_AP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof WriteDP) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((WriteDP)e).getAddr()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((WriteDP)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_DP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapDelay) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapDelay)e).getDelay()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_DELAY, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapWriteABORT) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapWriteABORT)e).getValue()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_WRITE_ABORT, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjPins) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapSwjPins)e).getPinout()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((DapSwjPins)e).getPinselect()));
        long _long_2 = DebugSeqUtil.toLong(this.interpret(((DapSwjPins)e).getPinwait()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_PINS, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1), Long.valueOf(_long_2)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjClock) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapSwjClock)e).getValue()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_CLOCK, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjSequence) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapSwjSequence)e).getCnt()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((DapSwjSequence)e).getVal()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_SEQUENCE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapJtagSequence) {
        _matched=true;
        long _long = DebugSeqUtil.toLong(this.interpret(((DapJtagSequence)e).getCnt()));
        long _long_1 = DebugSeqUtil.toLong(this.interpret(((DapJtagSequence)e).getTms()));
        long _long_2 = DebugSeqUtil.toLong(this.interpret(((DapJtagSequence)e).getTdi()));
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_JTAG_SEQUENCE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1), Long.valueOf(_long_2)))));
      }
    }
    return _switchResult;
  }
  
  private long executeCommand(final String cmdName, final List<Long> params) throws DsqException {
    return this.executeCommand(cmdName, params, null);
  }
  
  private long executeCommand(final String cmdName, final List<Long> params, final List<String> strings) throws DsqException {
    long _xifexpression = (long) 0;
    if ((!this.inAtomic)) {
      long _xblockexpression = (long) 0;
      {
        final IDsqCommand command = this.createCommand(cmdName, params, strings);
        this.debugSeqClient.execute(Collections.<IDsqCommand>unmodifiableList(CollectionLiterals.<IDsqCommand>newArrayList(command)), false);
        _xblockexpression = command.getOutput();
      }
      _xifexpression = _xblockexpression;
    } else {
      long _xifexpression_1 = (long) 0;
      if ((!this.collectingCommands)) {
        long _xblockexpression_1 = (long) 0;
        {
          final IDsqCommand command = this.findCommand(cmdName, params);
          _xblockexpression_1 = command.getOutput();
        }
        _xifexpression_1 = _xblockexpression_1;
      } else {
        long _xblockexpression_2 = (long) 0;
        {
          this.addCommand(cmdName, params, strings);
          _xblockexpression_2 = 0L;
        }
        _xifexpression_1 = _xblockexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }
  
  private void addCommand(final String cmdName, final List<Long> params, final List<String> strings) {
    final IDsqCommand command = this.createCommand(cmdName, params, strings);
    this.commands.add(command);
  }
  
  private IDsqCommand findCommand(final String cmdName, final List<Long> params) {
    int _plusPlus = this.commandIndex++;
    return this.commands.get(_plusPlus);
  }
  
  private IDsqCommand createCommand(final String cmdName, final List<Long> params, final List<String> strings) {
    DsqCommand _xblockexpression = null;
    {
      final HashMap<String, Long> predefinedVars = CollectionLiterals.<String, Long>newHashMap();
      Map<String, Long> _context = this.getContext(IDsqContext.AP);
      Long _get = null;
      if (_context!=null) {
        _get=_context.get(IDsqContext.AP);
      }
      predefinedVars.put(IDsqContext.AP, _get);
      Map<String, Long> _context_1 = this.getContext(IDsqContext.DP);
      Long _get_1 = null;
      if (_context_1!=null) {
        _get_1=_context_1.get(IDsqContext.DP);
      }
      predefinedVars.put(IDsqContext.DP, _get_1);
      Map<String, Long> _context_2 = this.getContext(IDsqContext.PROTOCOL);
      Long _get_2 = null;
      if (_context_2!=null) {
        _get_2=_context_2.get(IDsqContext.PROTOCOL);
      }
      predefinedVars.put(IDsqContext.PROTOCOL, _get_2);
      Map<String, Long> _context_3 = this.getContext(IDsqContext.CONNECTION);
      Long _get_3 = null;
      if (_context_3!=null) {
        _get_3=_context_3.get(IDsqContext.CONNECTION);
      }
      predefinedVars.put(IDsqContext.CONNECTION, _get_3);
      Map<String, Long> _context_4 = this.getContext(IDsqContext.TRACEOUT);
      Long _get_4 = null;
      if (_context_4!=null) {
        _get_4=_context_4.get(IDsqContext.TRACEOUT);
      }
      predefinedVars.put(IDsqContext.TRACEOUT, _get_4);
      Map<String, Long> _context_5 = this.getContext(IDsqContext.ERRORCONTROL);
      Long _get_5 = null;
      if (_context_5!=null) {
        _get_5=_context_5.get(IDsqContext.ERRORCONTROL);
      }
      predefinedVars.put(IDsqContext.ERRORCONTROL, _get_5);
      _xblockexpression = new DsqCommand(cmdName, params, strings, predefinedVars);
    }
    return _xblockexpression;
  }
  
  protected Long _interpret(final VariableDeclaration vardecl) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      boolean _isEmpty = this.contexts.isEmpty();
      if (_isEmpty) {
        this.enterScope(false);
      }
      final long value = DebugSeqUtil.toLong(this.interpret(vardecl.getValue()));
      this.contexts.peek().put(vardecl.getName(), Long.valueOf(value));
      _xblockexpression = value;
    }
    return Long.valueOf(_xblockexpression);
  }
  
  private Map<String, Long> getContext(final String k) {
    final Function1<Map<String, Long>, Boolean> _function = (Map<String, Long> it) -> {
      return Boolean.valueOf(it.containsKey(k));
    };
    return IterableExtensions.<Map<String, Long>>findLast(this.contexts, _function);
  }
  
  private long updateValue(final String variableName, final long newValue) {
    long _xblockexpression = (long) 0;
    {
      this.getContext(variableName).put(variableName, Long.valueOf(newValue));
      _xblockexpression = newValue;
    }
    return _xblockexpression;
  }
  
  private Long interpretUntilLast(final List<? extends EObject> l) {
    int _size = l.size();
    boolean _equals = (_size == 0);
    if (_equals) {
      return Long.valueOf(0L);
    }
    int _size_1 = l.size();
    int _minus = (_size_1 - 1);
    final Consumer<EObject> _function = (EObject it) -> {
      final Object result = this.interpret(it);
      if ((it instanceof Statement)) {
        this.log(this.logger, ((Statement)it), Long.valueOf(DebugSeqUtil.toLong(result)));
      }
    };
    l.subList(0, _minus).forEach(_function);
    return null;
  }
  
  private void enterScope(final boolean store) {
    if ((this.contexts.isEmpty() && store)) {
      throw new DsqException("The symbol table is empty");
    }
    if ((!store)) {
      this.contexts.push(CollectionLiterals.<String, Long>newHashMap());
    } else {
      final Long dp = this.getContext(IDsqContext.DP).get(IDsqContext.DP);
      final Long ap = this.getContext(IDsqContext.AP).get(IDsqContext.AP);
      final Long ec = this.getContext(IDsqContext.ERRORCONTROL).get(IDsqContext.ERRORCONTROL);
      this.contexts.push(CollectionLiterals.<String, Long>newHashMap());
      this.contexts.peek().put(IDsqContext.DP, dp);
      this.contexts.peek().put(IDsqContext.AP, ap);
      this.contexts.peek().put(IDsqContext.ERRORCONTROL, ec);
    }
  }
  
  private void exitScope() {
    this.contexts.pop();
  }
  
  private Long log(final IDsqLogger logger, final Statement stmt, final Long result) {
    Long _xifexpression = null;
    if (((logger == null) || this.collectingCommands)) {
      _xifexpression = result;
    } else {
      Long _xblockexpression = null;
      {
        logger.logStatement(NodeModelUtils.getTokenText(NodeModelUtils.getNode(stmt)), result, 0);
        _xblockexpression = result;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  private Long logIf(final IDsqLogger logger, final Statement stmt, final Long result) {
    Long _xifexpression = null;
    if (((logger == null) || this.collectingCommands)) {
      _xifexpression = result;
    } else {
      Long _xblockexpression = null;
      {
        logger.logIfStatement(NodeModelUtils.getTokenText(NodeModelUtils.getNode(stmt)), result, 0);
        _xblockexpression = result;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  private Long logWhile(final IDsqLogger logger, final Statement stmt, final Long result) {
    Long _xifexpression = null;
    if (((logger == null) || this.collectingCommands)) {
      _xifexpression = result;
    } else {
      Long _xblockexpression = null;
      {
        logger.logWhileStatement(NodeModelUtils.getTokenText(NodeModelUtils.getNode(stmt)), result, 0);
        _xblockexpression = result;
      }
      _xifexpression = _xblockexpression;
    }
    return _xifexpression;
  }
  
  private String postProcess(final String str) {
    String _xblockexpression = null;
    {
      String s = str.replace("&#13;", "");
      s = s.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
      s = s.replace("xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
      s = s.replace(">", ">\n");
      _xblockexpression = s = s.replace("<", "\n<");
    }
    return _xblockexpression;
  }
  
  public Object interpret(final EObject block) throws DsqException {
    if (block instanceof Block) {
      return _interpret((Block)block);
    } else if (block instanceof Control) {
      return _interpret((Control)block);
    } else if (block instanceof Expression) {
      return _interpret((Expression)block);
    } else if (block instanceof VariableDeclaration) {
      return _interpret((VariableDeclaration)block);
    } else if (block instanceof DebugVars) {
      return _interpret((DebugVars)block);
    } else if (block instanceof Sequence) {
      return _interpret((Sequence)block);
    } else if (block == null) {
      return _interpret((Void)null);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(block).toString());
    }
  }
}
