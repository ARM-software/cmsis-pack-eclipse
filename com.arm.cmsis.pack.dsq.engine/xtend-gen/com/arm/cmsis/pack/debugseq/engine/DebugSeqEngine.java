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

import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDebugVars;
import com.arm.cmsis.pack.data.ICpPack;
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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.nodemodel.ICompositeNode;
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
    DebugSeqStandaloneSetup _debugSeqStandaloneSetup = new DebugSeqStandaloneSetup();
    Injector _createInjectorAndDoEMFRegistration = _debugSeqStandaloneSetup.createInjectorAndDoEMFRegistration();
    _createInjectorAndDoEMFRegistration.injectMembers(this);
  }
  
  @Override
  public Collection<String> getDefaultSequenceNames() {
    List<String> _xblockexpression = null;
    {
      if ((this.dsqModel == null)) {
        DebugSeqModel _parse = this.parse();
        this.dsqModel = _parse;
      }
      List<String> _xifexpression = null;
      if ((this.dsqModel != null)) {
        Sequences _sequences = this.dsqModel.getSequences();
        EList<Sequence> _sequences_1 = _sequences.getSequences();
        final Function1<Sequence, String> _function = (Sequence it) -> {
          return it.getName();
        };
        List<String> _map = ListExtensions.<Sequence, String>map(_sequences_1, _function);
        final Function1<String, Boolean> _function_1 = (String it) -> {
          Collection<String> _defaultSqs = this.getDefaultSqs();
          return Boolean.valueOf(_defaultSqs.contains(it));
        };
        Iterable<String> _filter = IterableExtensions.<String>filter(_map, _function_1);
        _xifexpression = IterableExtensions.<String>toList(_filter);
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
      DebugSeqModel _parse = this.parse();
      this.dsqModel = _parse;
    }
    if ((this.dsqModel == null)) {
      return true;
    }
    Sequences _sequences = this.dsqModel.getSequences();
    EList<Sequence> _sequences_1 = _sequences.getSequences();
    final Function1<Sequence, Boolean> _function = (Sequence it) -> {
      String _name = it.getName();
      return Boolean.valueOf(Objects.equal(_name, sequenceName));
    };
    final Sequence seq = IterableExtensions.<Sequence>findFirst(_sequences_1, _function);
    return ((seq == null) || (seq.getDisable() != 0));
  }
  
  @Override
  public void execute(final IDsqSequence seqContext) throws DsqException {
    try {
      final Sequence seq = this.getSequence(seqContext);
      if ((seq != null)) {
        this.contexts.clear();
        this.enterScope(false);
        DebugVars _debugvars = this.dsqModel.getDebugvars();
        this.interpret(_debugvars);
        this.setPredefinedVariableValues(seqContext);
        this.interpret(seq);
        this.exitScope();
      } else {
        String _sequenceName = seqContext.getSequenceName();
        boolean _isEmptyDefaultSequence = DebugSeqUtil.isEmptyDefaultSequence(_sequenceName);
        boolean _not = (!_isEmptyDefaultSequence);
        if (_not) {
          String _sequenceName_1 = seqContext.getSequenceName();
          String _plus = ("Sequence named \'" + _sequenceName_1);
          String _plus_1 = (_plus + "\' is undefined");
          throw new DsqException(_plus_1);
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Override
  public String generateCode(final String generatorID, final String header) throws DsqException {
    String _xblockexpression = null;
    {
      if ((this.generator == null)) {
        DsqScriptGeneratorFactory _instance = DsqScriptGeneratorFactory.getInstance();
        IDsqScriptGenerator _extender = _instance.getExtender(generatorID);
        this.generator = _extender;
        if ((this.generator == null)) {
          return null;
        }
      }
      if ((this.dsqModel == null)) {
        DebugSeqModel _parse = this.parse();
        this.dsqModel = _parse;
      }
      _xblockexpression = this.generator.generate(this.dsqModel, header);
    }
    return _xblockexpression;
  }
  
  private Sequence getSequence(final IDsqSequence seqContext) throws Exception {
    if ((seqContext == null)) {
      throw new DsqException("Predefined variables are not provided");
    }
    this.checkPredefinedVariables(seqContext);
    if ((this.dsqModel == null)) {
      DebugSeqModel _parse = this.parse();
      this.dsqModel = _parse;
    }
    Sequences _sequences = this.dsqModel.getSequences();
    EList<Sequence> _sequences_1 = _sequences.getSequences();
    final Function1<Sequence, Boolean> _function = (Sequence it) -> {
      return Boolean.valueOf((Objects.equal(it.getName(), seqContext.getSequenceName()) && Objects.equal(it.getPname(), this.deviceInfo.getProcessorName())));
    };
    final Sequence matchedSeq = IterableExtensions.<Sequence>findFirst(_sequences_1, _function);
    Sequence _xifexpression = null;
    if ((matchedSeq != null)) {
      _xifexpression = matchedSeq;
    } else {
      Sequences _sequences_2 = this.dsqModel.getSequences();
      EList<Sequence> _sequences_3 = _sequences_2.getSequences();
      final Function1<Sequence, Boolean> _function_1 = (Sequence it) -> {
        String _name = it.getName();
        String _sequenceName = seqContext.getSequenceName();
        return Boolean.valueOf(Objects.equal(_name, _sequenceName));
      };
      _xifexpression = IterableExtensions.<Sequence>findFirst(_sequences_3, _function_1);
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
        ICpDebugConfiguration _debugConfiguration_1 = this.deviceInfo.getDebugConfiguration();
        _xifexpression = _debugConfiguration_1.getDebugVars();
      }
      final ICpDebugVars dv = _xifexpression;
      String _xifexpression_1 = null;
      if ((dv == null)) {
        _xifexpression_1 = "";
      } else {
        _xifexpression_1 = dv.getText();
      }
      final String initialText = _xifexpression_1;
      final StringBuilder sb = new StringBuilder((initialText + "\n"));
      StringConcatenation _builder = new StringConcatenation();
      {
        if ((dv == null)) {
          _builder.append("<debugvars>");
          _builder.newLine();
        }
      }
      _builder.append("__var ");
      _builder.append(IDsqContext.AP, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.DP, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.PROTOCOL, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.CONNECTION, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.TRACEOUT, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      _builder.append("__var ");
      _builder.append(IDsqContext.ERRORCONTROL, "");
      _builder.append(" = 0;");
      _builder.newLineIfNotEmpty();
      {
        if ((dv == null)) {
          _builder.append("</debugvars>");
          _builder.newLine();
        }
      }
      sb.append(_builder);
      if ((dv != null)) {
        final String text = dv.getText();
        if (((dv.getDgbConfFileName() != null) && (!dv.getDgbConfFileName().isEmpty()))) {
          String _dgbConfFileName = dv.getDgbConfFileName();
          Path _get = Paths.get(_dgbConfFileName);
          URI _uri = _get.toUri();
          URL _uRL = _uri.toURL();
          String _readFile = this.readFile(_uRL);
          sb.append(_readFile);
        }
        String _string = sb.toString();
        dv.setText(_string);
        final PdscParser xmlParser = new PdscParser();
        String _writeToXmlString = xmlParser.writeToXmlString(dv);
        String _postProcess = this.postProcess(_writeToXmlString);
        this.debugVars = _postProcess;
        dv.setText(text);
      } else {
        String _string_1 = sb.toString();
        this.debugVars = _string_1;
      }
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
      Map<String, Long> _peek = this.contexts.peek();
      _peek.put(IDsqContext.AP, ap);
    }
    if ((dp != null)) {
      Map<String, Long> _peek_1 = this.contexts.peek();
      _peek_1.put(IDsqContext.DP, dp);
    }
    if ((p != null)) {
      Map<String, Long> _peek_2 = this.contexts.peek();
      _peek_2.put(IDsqContext.PROTOCOL, p);
    }
    if ((c != null)) {
      Map<String, Long> _peek_3 = this.contexts.peek();
      _peek_3.put(IDsqContext.CONNECTION, c);
    }
    if ((tc != null)) {
      Map<String, Long> _peek_4 = this.contexts.peek();
      _peek_4.put(IDsqContext.TRACEOUT, tc);
    }
    if ((ec != null)) {
      Map<String, Long> _peek_5 = this.contexts.peek();
      _peek_5.put(IDsqContext.ERRORCONTROL, ec);
    }
  }
  
  /**
   * Parses all sequences provided by device
   * @return The root node of the parsed model
   */
  private DebugSeqModel parse() throws DsqException {
    Resource _resource = this.getResource();
    EList<EObject> _contents = _resource.getContents();
    EObject _get = _contents.get(0);
    return ((DebugSeqModel) _get);
  }
  
  private Resource getResource() {
    try {
      if ((this.resource != null)) {
        return this.resource;
      }
      this.initDebugVariables();
      final PdscParser xmlParser = new PdscParser();
      ICpDebugConfiguration _debugConfiguration = this.deviceInfo.getDebugConfiguration();
      Map<String, ICpSequence> _sequences = _debugConfiguration.getSequences();
      Collection<ICpSequence> _values = _sequences.values();
      final Function1<ICpSequence, String> _function = (ICpSequence it) -> {
        return xmlParser.writeToXmlString(it);
      };
      Iterable<String> _map = IterableExtensions.<ICpSequence, String>map(_values, _function);
      String _join = IterableExtensions.join(_map, "\n");
      final String sequences = this.postProcess(_join);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(this.debugVars, "");
      _builder.newLineIfNotEmpty();
      _builder.append("<sequences>");
      _builder.newLine();
      _builder.append(sequences, "");
      _builder.newLineIfNotEmpty();
      ICpDebugConfiguration _debugConfiguration_1 = this.deviceInfo.getDebugConfiguration();
      Map<String, ICpSequence> _sequences_1 = _debugConfiguration_1.getSequences();
      String _addDefaultSeqs = this.addDefaultSeqs(_sequences_1);
      _builder.append(_addDefaultSeqs, "");
      _builder.newLineIfNotEmpty();
      _builder.append("</sequences>");
      _builder.newLine();
      final String modelString = _builder.toString();
      final XtextResourceSet resourceSet = this.resourceSetProvider.get();
      resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
      org.eclipse.emf.common.util.URI _createURI = org.eclipse.emf.common.util.URI.createURI("dummy:/dummy.dsq");
      Resource _createResource = resourceSet.createResource(_createURI);
      this.resource = _createResource;
      StringInputStream _stringInputStream = new StringInputStream(modelString);
      Map<Object, Object> _loadOptions = resourceSet.getLoadOptions();
      this.resource.load(_stringInputStream, _loadOptions);
      final List<Issue> issues = this.validator.validate(this.resource, CheckMode.ALL, CancelIndicator.NullImpl);
      boolean _isEmpty = issues.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        this.dsqModel = null;
        final Function1<Issue, String> _function_1 = (Issue it) -> {
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
        List<String> _map_1 = ListExtensions.<Issue, String>map(issues, _function_1);
        final String errors = IterableExtensions.join(_map_1, "\n\n");
        ICpPack _pack = this.deviceInfo.getPack();
        String _fileName = _pack.getFileName();
        String _plus = ("Error while validating the debug sequences in pack file:\n" + _fileName);
        String _plus_1 = (_plus + "\n\nDevice: ");
        String _deviceName = this.deviceInfo.getDeviceName();
        String _plus_2 = (_plus_1 + _deviceName);
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
        URLConnection _openConnection = url.openConnection();
        final InputStream inputStream = _openConnection.getInputStream();
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
        String[] _list = defaultSeqsFolder.list();
        final Function1<String, String> _function = (String it) -> {
          return Utils.extractBaseFileName(it);
        };
        _xblockexpression = ListExtensions.<String, String>map(((List<String>)Conversions.doWrapArray(_list)), _function);
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
        boolean _containsKey = sequences.containsKey(defaultSeqName);
        boolean _not = (!_containsKey);
        if (_not) {
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
      EList<Statement> _statements = debugvars.getStatements();
      final Consumer<Statement> _function = (Statement it) -> {
        Object _interpret = this.interpret(it);
        long _long = DebugSeqUtil.toLong(_interpret);
        this.log(this.logger, it, Long.valueOf(_long));
      };
      _statements.forEach(_function);
      _xblockexpression = 0L;
    }
    return Long.valueOf(_xblockexpression);
  }
  
  protected Long _interpret(final Sequence seq) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      if ((this.logger != null)) {
        String _name = seq.getName();
        this.logger.logSeqStart(_name);
      }
      this.enterScope(true);
      EList<CodeBlock> _codeblocks = seq.getCodeblocks();
      boolean _tripleNotEquals = (_codeblocks != null);
      if (_tripleNotEquals) {
        EList<CodeBlock> _codeblocks_1 = seq.getCodeblocks();
        final Consumer<CodeBlock> _function = (CodeBlock it) -> {
          this.interpret(it);
        };
        _codeblocks_1.forEach(_function);
      }
      this.exitScope();
      if ((this.logger != null)) {
        String _name_1 = seq.getName();
        this.logger.logSeqEnd(_name_1);
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
        String _info = block.getInfo();
        this.logger.logBlockStart(_tripleNotEquals, _info);
      }
      final Stack<Map<String, Long>> tempContexts = new Stack<Map<String, Long>>();
      long _atomic_1 = block.getAtomic();
      boolean _tripleNotEquals_1 = (_atomic_1 != 0);
      if (_tripleNotEquals_1) {
        this.inAtomic = true;
        this.collectingCommands = true;
        ArrayList<IDsqCommand> _newArrayList = CollectionLiterals.<IDsqCommand>newArrayList();
        this.commands = _newArrayList;
        final Consumer<Map<String, Long>> _function = (Map<String, Long> it) -> {
          HashMap<String, Long> _hashMap = new HashMap<String, Long>(it);
          tempContexts.push(_hashMap);
        };
        this.contexts.forEach(_function);
      }
      EList<Statement> _statements = block.getStatements();
      this.interpretUntilLast(_statements);
      EList<Statement> _statements_1 = block.getStatements();
      Statement _last = IterableExtensions.<Statement>last(_statements_1);
      Object result = this.interpret(_last);
      EList<Statement> _statements_2 = block.getStatements();
      Statement _last_1 = IterableExtensions.<Statement>last(_statements_2);
      long _long = DebugSeqUtil.toLong(result);
      this.log(this.logger, _last_1, Long.valueOf(_long));
      if (this.inAtomic) {
        this.debugSeqClient.execute(this.commands, true);
        this.contexts = tempContexts;
        this.collectingCommands = false;
        this.commandIndex = 0;
        EList<Statement> _statements_3 = block.getStatements();
        this.interpretUntilLast(_statements_3);
        EList<Statement> _statements_4 = block.getStatements();
        Statement _last_2 = IterableExtensions.<Statement>last(_statements_4);
        Object _interpret = this.interpret(_last_2);
        result = _interpret;
        EList<Statement> _statements_5 = block.getStatements();
        Statement _last_3 = IterableExtensions.<Statement>last(_statements_5);
        long _long_1 = DebugSeqUtil.toLong(result);
        this.log(this.logger, _last_3, Long.valueOf(_long_1));
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
        String _info = control.getInfo();
        this.logger.logContorlStart(_info);
      }
      this.enterScope(false);
      if (((control.getIf() == null) || ((this.logIf(this.logger, control.getIf(), Long.valueOf(DebugSeqUtil.toLong(this.interpret(control.getIf()))))).longValue() != 0))) {
        Expression _while = control.getWhile();
        boolean _tripleEquals = (_while == null);
        if (_tripleEquals) {
          EList<CodeBlock> _codeblocks = control.getCodeblocks();
          this.interpretUntilLast(_codeblocks);
          EList<CodeBlock> _codeblocks_1 = control.getCodeblocks();
          CodeBlock _last = IterableExtensions.<CodeBlock>last(_codeblocks_1);
          Object _interpret = this.interpret(_last);
          long _long = DebugSeqUtil.toLong(_interpret);
          result = _long;
        } else {
          long timeout = control.getTimeout();
          if ((timeout == 0)) {
            timeout = Long.MAX_VALUE;
          }
          long runningTime = 0L;
          final long startTime = System.nanoTime();
          while ((((this.logWhile(this.logger, control.getWhile(), Long.valueOf(DebugSeqUtil.toLong(this.interpret(control.getWhile()))))).longValue() != 0) && (runningTime < timeout))) {
            {
              EList<CodeBlock> _codeblocks_2 = control.getCodeblocks();
              this.interpretUntilLast(_codeblocks_2);
              EList<CodeBlock> _codeblocks_3 = control.getCodeblocks();
              CodeBlock _last_1 = IterableExtensions.<CodeBlock>last(_codeblocks_3);
              Object _interpret_1 = this.interpret(_last_1);
              long _long_1 = DebugSeqUtil.toLong(_interpret_1);
              result = _long_1;
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
          VariableDeclaration _variable = ((VariableRef)e).getVariable();
          final String k = _variable.getName();
          Map<String, Long> _context = this.getContext(k);
          _xblockexpression = _context.get(k);
        }
        _switchResult = _xblockexpression;
      }
    }
    if (!_matched) {
      if (e instanceof Not) {
        _matched=true;
        Expression _expression = ((Not)e).getExpression();
        Object _interpret = this.interpret(_expression);
        long _long = DebugSeqUtil.toLong(_interpret);
        boolean _equals = (_long == 0);
        _switchResult = Long.valueOf(DebugSeqUtil.toLong(Boolean.valueOf(_equals)));
      }
    }
    if (!_matched) {
      if (e instanceof Assignment) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          Expression _left = ((Assignment)e).getLeft();
          VariableDeclaration _variable = ((VariableRef) _left).getVariable();
          final String k = _variable.getName();
          Map<String, Long> _context = this.getContext(k);
          final Long i = _context.get(k);
          Expression _right = ((Assignment)e).getRight();
          Object _interpret = this.interpret(_right);
          final long v = DebugSeqUtil.toLong(_interpret);
          long _switchResult_1 = (long) 0;
          String _op = ((Assignment)e).getOp();
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
          _xblockexpression = _switchResult_1;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Ternary) {
        _matched=true;
        Object _xifexpression = null;
        Expression _left = ((Ternary)e).getLeft();
        Object _interpret = this.interpret(_left);
        boolean _tripleNotEquals = (_interpret != Integer.valueOf(0));
        if (_tripleNotEquals) {
          Expression _exp1 = ((Ternary)e).getExp1();
          _xifexpression = this.interpret(_exp1);
        } else {
          Expression _exp2 = ((Ternary)e).getExp2();
          _xifexpression = this.interpret(_exp2);
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
        Expression _left = ((BitOr)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((BitOr)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long | _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof BitXor) {
        _matched=true;
        Expression _left = ((BitXor)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((BitXor)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long ^ _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof BitAnd) {
        _matched=true;
        Expression _left = ((BitAnd)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((BitAnd)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long & _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof BitNot) {
        _matched=true;
        Expression _expression = ((BitNot)e).getExpression();
        Object _interpret = this.interpret(_expression);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf((~_long));
      }
    }
    if (!_matched) {
      if (e instanceof Equality) {
        _matched=true;
        long _xifexpression = (long) 0;
        String _op = ((Equality)e).getOp();
        boolean _equals = Objects.equal(_op, "==");
        if (_equals) {
          Expression _left = ((Equality)e).getLeft();
          Object _interpret = this.interpret(_left);
          Expression _right = ((Equality)e).getRight();
          Object _interpret_1 = this.interpret(_right);
          boolean _equals_1 = Objects.equal(_interpret, _interpret_1);
          _xifexpression = DebugSeqUtil.toLong(Boolean.valueOf(_equals_1));
        } else {
          Expression _left_1 = ((Equality)e).getLeft();
          Object _interpret_2 = this.interpret(_left_1);
          Expression _right_1 = ((Equality)e).getRight();
          Object _interpret_3 = this.interpret(_right_1);
          boolean _notEquals = (!Objects.equal(_interpret_2, _interpret_3));
          _xifexpression = DebugSeqUtil.toLong(Boolean.valueOf(_notEquals));
        }
        _switchResult = Long.valueOf(_xifexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Comparison) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          Expression _left = ((Comparison)e).getLeft();
          Object _interpret = this.interpret(_left);
          final long left = DebugSeqUtil.toLong(_interpret);
          Expression _right = ((Comparison)e).getRight();
          Object _interpret_1 = this.interpret(_right);
          final long right = DebugSeqUtil.toLong(_interpret_1);
          long _switchResult_1 = (long) 0;
          String _op = ((Comparison)e).getOp();
          switch (_op) {
            case "&lt;":
              int _compareUnsigned = Long.compareUnsigned(left, right);
              boolean _lessThan = (_compareUnsigned < 0);
              _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf(_lessThan));
              break;
            case "&gt;":
              int _compareUnsigned_1 = Long.compareUnsigned(left, right);
              boolean _greaterThan = (_compareUnsigned_1 > 0);
              _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf(_greaterThan));
              break;
            case "&lt;=":
              int _compareUnsigned_2 = Long.compareUnsigned(left, right);
              boolean _lessEqualsThan = (_compareUnsigned_2 <= 0);
              _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf(_lessEqualsThan));
              break;
            case "&gt;=":
              int _compareUnsigned_3 = Long.compareUnsigned(left, right);
              boolean _greaterEqualsThan = (_compareUnsigned_3 >= 0);
              _switchResult_1 = DebugSeqUtil.toLong(Boolean.valueOf(_greaterEqualsThan));
              break;
            default:
              _switchResult_1 = 0L;
              break;
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
          Expression _left = ((Shift)e).getLeft();
          Object _interpret = this.interpret(_left);
          final long left = DebugSeqUtil.toLong(_interpret);
          Expression _right = ((Shift)e).getRight();
          Object _interpret_1 = this.interpret(_right);
          long _long = DebugSeqUtil.toLong(_interpret_1);
          final int right = DebugSeqUtil.toInteger(Long.valueOf(_long));
          long _switchResult_1 = (long) 0;
          String _op = ((Shift)e).getOp();
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
          _xblockexpression = _switchResult_1;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Plus) {
        _matched=true;
        Expression _left = ((Plus)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((Plus)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long + _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Minus) {
        _matched=true;
        Expression _left = ((Minus)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((Minus)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long - _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Mul) {
        _matched=true;
        Expression _left = ((Mul)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((Mul)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf((_long * _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Div) {
        _matched=true;
        Expression _left = ((Div)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((Div)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(Long.divideUnsigned(_long, _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof Rem) {
        _matched=true;
        Expression _left = ((Rem)e).getLeft();
        Object _interpret = this.interpret(_left);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _right = ((Rem)e).getRight();
        Object _interpret_1 = this.interpret(_right);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(Long.remainderUnsigned(_long, _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof SequenceCall) {
        _matched=true;
        Object _xblockexpression = null;
        {
          Sequences _containingSequences = DebugSeqUtil.containingSequences(e);
          EList<Sequence> _sequences = _containingSequences.getSequences();
          final Function1<Sequence, Boolean> _function = (Sequence it) -> {
            String _name = it.getName();
            String _seqname = ((SequenceCall)e).getSeqname();
            return Boolean.valueOf(Objects.equal(_name, _seqname));
          };
          final Sequence seq = IterableExtensions.<Sequence>findFirst(_sequences, _function);
          Object _xifexpression = null;
          if ((seq != null)) {
            _xifexpression = this.interpret(seq);
          } else {
            Object _xifexpression_1 = null;
            String _seqname = ((SequenceCall)e).getSeqname();
            boolean _isEmptyDefaultSequence = DebugSeqUtil.isEmptyDefaultSequence(_seqname);
            boolean _not = (!_isEmptyDefaultSequence);
            if (_not) {
              String _seqname_1 = ((SequenceCall)e).getSeqname();
              String _plus = ("Sequence with name \'" + _seqname_1);
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
        Expression _type = ((Query)e).getType();
        Object _interpret = this.interpret(_type);
        long _long = DebugSeqUtil.toLong(_interpret);
        String _message = ((Query)e).getMessage();
        Expression _default = ((Query)e).getDefault();
        Object _interpret_1 = this.interpret(_default);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.debugSeqClient.query(_long, _message, _long_1));
      }
    }
    if (!_matched) {
      if (e instanceof QueryValue) {
        _matched=true;
        String _message = ((QueryValue)e).getMessage();
        Expression _default = ((QueryValue)e).getDefault();
        Object _interpret = this.interpret(_default);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.debugSeqClient.query(IDsqClient.QUERY_VALUE_TYPE, _message, _long));
      }
    }
    if (!_matched) {
      if (e instanceof LoadDebugInfo) {
        _matched=true;
        String _path = ((LoadDebugInfo)e).getPath();
        String _absolutePath = this.deviceInfo.getAbsolutePath(_path);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_LOAD_DEBUG_INFO, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList()), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_absolutePath))));
      }
    }
    if (!_matched) {
      if (e instanceof Message) {
        _matched=true;
        long _xblockexpression = (long) 0;
        {
          EList<Parameter> _parameters = ((Message)e).getParameters();
          final Function1<Parameter, Object> _function = (Parameter it) -> {
            return this.interpret(it);
          };
          final List<Object> parameters = ListExtensions.<Parameter, Object>map(_parameters, _function);
          long _xtrycatchfinallyexpression = (long) 0;
          try {
            long _xblockexpression_1 = (long) 0;
            {
              String _format = ((Message)e).getFormat();
              final String message = DebugSeqUtil.formatWithValues(_format, parameters);
              Expression _type = ((Message)e).getType();
              Object _interpret = this.interpret(_type);
              long _long = DebugSeqUtil.toLong(_interpret);
              _xblockexpression_1 = this.executeCommand(IDsqCommand.DSQ_MESSAGE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long))), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(message)));
            }
            _xtrycatchfinallyexpression = _xblockexpression_1;
          } catch (final Throwable _t) {
            if (_t instanceof Exception) {
              final Exception exp = (Exception)_t;
              String _message = exp.getMessage();
              throw new DsqException(_message);
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          _xblockexpression = _xtrycatchfinallyexpression;
        }
        _switchResult = Long.valueOf(_xblockexpression);
      }
    }
    if (!_matched) {
      if (e instanceof Read8) {
        _matched=true;
        Expression _addr = ((Read8)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_8, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read16) {
        _matched=true;
        Expression _addr = ((Read16)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_16, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read32) {
        _matched=true;
        Expression _addr = ((Read32)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_32, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Read64) {
        _matched=true;
        Expression _addr = ((Read64)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_64, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof ReadAP) {
        _matched=true;
        Expression _addr = ((ReadAP)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_AP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof ReadDP) {
        _matched=true;
        Expression _addr = ((ReadDP)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_READ_DP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write8) {
        _matched=true;
        Expression _addr = ((Write8)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((Write8)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_8, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write16) {
        _matched=true;
        Expression _addr = ((Write16)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((Write16)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_16, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write32) {
        _matched=true;
        Expression _addr = ((Write32)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((Write32)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_32, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof Write64) {
        _matched=true;
        Expression _addr = ((Write64)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((Write64)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_64, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof WriteAP) {
        _matched=true;
        Expression _addr = ((WriteAP)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((WriteAP)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_AP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof WriteDP) {
        _matched=true;
        Expression _addr = ((WriteDP)e).getAddr();
        Object _interpret = this.interpret(_addr);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((WriteDP)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_WRITE_DP, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapDelay) {
        _matched=true;
        Expression _delay = ((DapDelay)e).getDelay();
        Object _interpret = this.interpret(_delay);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_DELAY, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapWriteABORT) {
        _matched=true;
        Expression _value = ((DapWriteABORT)e).getValue();
        Object _interpret = this.interpret(_value);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_WRITE_ABORT, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjPins) {
        _matched=true;
        Expression _pinout = ((DapSwjPins)e).getPinout();
        Object _interpret = this.interpret(_pinout);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _pinselect = ((DapSwjPins)e).getPinselect();
        Object _interpret_1 = this.interpret(_pinselect);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        Expression _pinwait = ((DapSwjPins)e).getPinwait();
        Object _interpret_2 = this.interpret(_pinwait);
        long _long_2 = DebugSeqUtil.toLong(_interpret_2);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_PINS, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1), Long.valueOf(_long_2)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjClock) {
        _matched=true;
        Expression _value = ((DapSwjClock)e).getValue();
        Object _interpret = this.interpret(_value);
        long _long = DebugSeqUtil.toLong(_interpret);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_CLOCK, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapSwjSequence) {
        _matched=true;
        Expression _cnt = ((DapSwjSequence)e).getCnt();
        Object _interpret = this.interpret(_cnt);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _val = ((DapSwjSequence)e).getVal();
        Object _interpret_1 = this.interpret(_val);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_SWJ_SEQUENCE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1)))));
      }
    }
    if (!_matched) {
      if (e instanceof DapJtagSequence) {
        _matched=true;
        Expression _cnt = ((DapJtagSequence)e).getCnt();
        Object _interpret = this.interpret(_cnt);
        long _long = DebugSeqUtil.toLong(_interpret);
        Expression _tms = ((DapJtagSequence)e).getTms();
        Object _interpret_1 = this.interpret(_tms);
        long _long_1 = DebugSeqUtil.toLong(_interpret_1);
        Expression _tdi = ((DapJtagSequence)e).getTdi();
        Object _interpret_2 = this.interpret(_tdi);
        long _long_2 = DebugSeqUtil.toLong(_interpret_2);
        _switchResult = Long.valueOf(this.executeCommand(IDsqCommand.DSQ_DAP_JTAG_SEQUENCE, Collections.<Long>unmodifiableList(CollectionLiterals.<Long>newArrayList(Long.valueOf(_long), Long.valueOf(_long_1), Long.valueOf(_long_2)))));
      }
    }
    return _switchResult;
  }
  
  private long executeCommand(final String cmdName, final List<Long> params) {
    return this.executeCommand(cmdName, params, null);
  }
  
  private long executeCommand(final String cmdName, final List<Long> params, final List<String> strings) {
    long _xifexpression = (long) 0;
    if ((!this.inAtomic)) {
      long _xblockexpression = (long) 0;
      {
        final DsqCommand command = new DsqCommand(cmdName, params, strings);
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
    final DsqCommand command = new DsqCommand(cmdName, params, strings);
    this.commands.add(command);
  }
  
  private IDsqCommand findCommand(final String cmdName, final List<Long> params) {
    int _plusPlus = this.commandIndex++;
    return this.commands.get(_plusPlus);
  }
  
  protected Long _interpret(final VariableDeclaration vardecl) throws DsqException {
    long _xblockexpression = (long) 0;
    {
      boolean _isEmpty = this.contexts.isEmpty();
      if (_isEmpty) {
        this.enterScope(false);
      }
      Map<String, Long> _peek = this.contexts.peek();
      String _name = vardecl.getName();
      Expression _value = vardecl.getValue();
      Object _interpret = this.interpret(_value);
      long _long = DebugSeqUtil.toLong(_interpret);
      _peek.put(_name, Long.valueOf(_long));
      _xblockexpression = 0L;
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
      Map<String, Long> _context = this.getContext(variableName);
      _context.put(variableName, Long.valueOf(newValue));
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
    List<? extends EObject> _subList = l.subList(0, _minus);
    final Consumer<EObject> _function = (EObject it) -> {
      final Object result = this.interpret(it);
      if ((it instanceof Statement)) {
        long _long = DebugSeqUtil.toLong(result);
        this.log(this.logger, ((Statement)it), Long.valueOf(_long));
      }
    };
    _subList.forEach(_function);
    return null;
  }
  
  private void enterScope(final boolean store) {
    if ((this.contexts.isEmpty() && store)) {
      throw new DsqException("The symbol table is empty");
    }
    if ((!store)) {
      HashMap<String, Long> _newHashMap = CollectionLiterals.<String, Long>newHashMap();
      this.contexts.push(_newHashMap);
    } else {
      Map<String, Long> _peek = this.contexts.peek();
      final Long dp = _peek.get(IDsqContext.DP);
      Map<String, Long> _peek_1 = this.contexts.peek();
      final Long ap = _peek_1.get(IDsqContext.AP);
      Map<String, Long> _peek_2 = this.contexts.peek();
      final Long ec = _peek_2.get(IDsqContext.ERRORCONTROL);
      HashMap<String, Long> _newHashMap_1 = CollectionLiterals.<String, Long>newHashMap();
      this.contexts.push(_newHashMap_1);
      Map<String, Long> _peek_3 = this.contexts.peek();
      _peek_3.put(IDsqContext.DP, dp);
      Map<String, Long> _peek_4 = this.contexts.peek();
      _peek_4.put(IDsqContext.AP, ap);
      Map<String, Long> _peek_5 = this.contexts.peek();
      _peek_5.put(IDsqContext.ERRORCONTROL, ec);
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
        ICompositeNode _node = NodeModelUtils.getNode(stmt);
        String _tokenText = NodeModelUtils.getTokenText(_node);
        logger.logStatement(_tokenText, result, 0);
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
        ICompositeNode _node = NodeModelUtils.getNode(stmt);
        String _tokenText = NodeModelUtils.getTokenText(_node);
        logger.logIfStatement(_tokenText, result, 0);
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
        ICompositeNode _node = NodeModelUtils.getNode(stmt);
        String _tokenText = NodeModelUtils.getTokenText(_node);
        logger.logWhileStatement(_tokenText, result, 0);
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
      String _replace = s.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
      s = _replace;
      String _replace_1 = s.replace("xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
      s = _replace_1;
      String _replace_2 = s.replace(">", ">\n");
      s = _replace_2;
      String _replace_3 = s.replace("<", "\n<");
      _xblockexpression = s = _replace_3;
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
