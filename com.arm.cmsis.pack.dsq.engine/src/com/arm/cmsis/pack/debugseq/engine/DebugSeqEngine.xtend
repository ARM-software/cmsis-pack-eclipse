/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.debugseq.engine

import static extension com.arm.cmsis.pack.debugseq.util.DebugSeqUtil.*
import static extension org.eclipse.xtext.nodemodel.util.NodeModelUtils.*

import java.util.Stack
import java.util.Map
import java.util.HashMap
import com.arm.cmsis.pack.info.ICpDeviceInfo
import com.arm.cmsis.pack.debugseq.debugSeq.Expression
import com.arm.cmsis.pack.debugseq.debugSeq.IntConstant
import com.arm.cmsis.pack.debugseq.debugSeq.StringConstant
import com.arm.cmsis.pack.debugseq.debugSeq.Not
import com.arm.cmsis.pack.debugseq.debugSeq.Assignment
import com.arm.cmsis.pack.debugseq.debugSeq.VariableRef
import com.arm.cmsis.pack.debugseq.debugSeq.VariableDeclaration
import com.arm.cmsis.pack.debugseq.debugSeq.Ternary
import com.arm.cmsis.pack.debugseq.debugSeq.Or
import com.arm.cmsis.pack.debugseq.debugSeq.And
import com.arm.cmsis.pack.debugseq.debugSeq.BitOr
import com.arm.cmsis.pack.debugseq.debugSeq.BitXor
import com.arm.cmsis.pack.debugseq.debugSeq.BitAnd
import com.arm.cmsis.pack.debugseq.debugSeq.BitNot
import com.arm.cmsis.pack.debugseq.debugSeq.Equality
import com.arm.cmsis.pack.debugseq.debugSeq.Comparison
import com.arm.cmsis.pack.debugseq.debugSeq.Shift
import com.arm.cmsis.pack.debugseq.debugSeq.Plus
import com.arm.cmsis.pack.debugseq.debugSeq.Minus
import com.arm.cmsis.pack.debugseq.debugSeq.Mul
import com.arm.cmsis.pack.debugseq.debugSeq.Div
import com.arm.cmsis.pack.debugseq.debugSeq.Rem
import com.arm.cmsis.pack.debugseq.debugSeq.SequenceCall
import com.arm.cmsis.pack.debugseq.debugSeq.Query
import com.arm.cmsis.pack.debugseq.debugSeq.Read8
import com.arm.cmsis.pack.debugseq.debugSeq.Read16
import com.arm.cmsis.pack.debugseq.debugSeq.Read32
import com.arm.cmsis.pack.debugseq.debugSeq.Read64
import com.arm.cmsis.pack.debugseq.debugSeq.ReadAP
import com.arm.cmsis.pack.debugseq.debugSeq.ReadDP
import com.arm.cmsis.pack.debugseq.debugSeq.Write8
import com.arm.cmsis.pack.debugseq.debugSeq.Write16
import com.arm.cmsis.pack.debugseq.debugSeq.Write32
import com.arm.cmsis.pack.debugseq.debugSeq.Write64
import com.arm.cmsis.pack.debugseq.debugSeq.WriteAP
import com.arm.cmsis.pack.debugseq.debugSeq.WriteDP
import com.arm.cmsis.pack.debugseq.debugSeq.DapDelay
import com.arm.cmsis.pack.debugseq.debugSeq.DapWriteABORT
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjPins
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjClock
import com.arm.cmsis.pack.debugseq.debugSeq.DapSwjSequence
import com.arm.cmsis.pack.debugseq.debugSeq.DapJtagSequence
import com.arm.cmsis.pack.debugseq.debugSeq.QueryValue
import com.arm.cmsis.pack.debugseq.debugSeq.LoadDebugInfo
import com.arm.cmsis.pack.debugseq.debugSeq.Message
import com.arm.cmsis.pack.debugseq.debugSeq.DebugVars
import com.arm.cmsis.pack.debugseq.debugSeq.Sequence
import com.arm.cmsis.pack.debugseq.debugSeq.Block
import com.arm.cmsis.pack.debugseq.debugSeq.Control
import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel
import com.google.inject.Inject
import java.nio.file.Paths
import com.arm.cmsis.pack.parser.PdscParser
import java.util.List
import org.eclipse.emf.ecore.EObject
import com.arm.cmsis.pack.debugseq.DebugSeqStandaloneSetup
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.util.StringInputStream
import com.arm.cmsis.pack.debugseq.debugSeq.Statement
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.util.CancelIndicator
import java.util.Collection
import com.arm.cmsis.pack.dsq.IDsqCommand
import com.arm.cmsis.pack.dsq.IDsqLogger
import com.arm.cmsis.pack.dsq.DsqCommand
import com.arm.cmsis.pack.dsq.IDsqEngine
import com.arm.cmsis.pack.data.ICpSequence
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import com.arm.cmsis.pack.dsq.DsqException
import com.arm.cmsis.pack.dsq.IDsqContext
import com.arm.cmsis.pack.dsq.IDsqSequence
import com.arm.cmsis.pack.dsq.IDsqClient
import com.arm.cmsis.pack.utils.Utils
import org.eclipse.core.runtime.FileLocator
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import com.google.inject.Provider
import org.eclipse.xtext.validation.IResourceValidator
import com.arm.cmsis.pack.debugseq.generator.IDsqScriptGenerator
import com.arm.cmsis.pack.debugseq.generator.DsqScriptGeneratorFactory

class DebugSeqEngine implements IDsqEngine {
	
	@Inject Provider<XtextResourceSet> resourceSetProvider
	@Inject IResourceValidator validator;

	Resource resource
	IDsqScriptGenerator generator
	
	Stack<Map<String, Long>> contexts = new Stack
	ICpDeviceInfo deviceInfo
	IDsqClient debugSeqClient
	IDsqLogger logger
	
	DebugSeqModel dsqModel
	String debugVars
	
	boolean inAtomic = false
	boolean collectingCommands = false
	List<IDsqCommand> commands
	int commandIndex
	
	@Inject
	protected new(IDsqClient dsqClient) { // constructor for testing
		debugSeqClient = dsqClient
	}
	
	new(ICpDeviceInfo devInfo, IDsqClient dsqClient, IDsqLogger log) {
		deviceInfo = devInfo
		debugSeqClient = dsqClient
		logger = log
		debugVars = ''
		
		new DebugSeqStandaloneSetup().createInjectorAndDoEMFRegistration().injectMembers(this)
	}
	
	override Collection<String> getDefaultSequenceNames() {
		if (dsqModel === null) {
			dsqModel = parse
		}
		if (dsqModel !== null) {
			dsqModel.sequences.sequences.map[name].filter[defaultSqs.contains(it)].toList
		} else {
			newArrayList
		}
	}
	
	override boolean isSequenceDisabled(String sequenceName) {
		if (dsqModel === null) {
			dsqModel = parse
		}
		if (dsqModel === null) {
			return true
		}
		val seq = dsqModel.sequences.sequences.findFirst[name == sequenceName]
		return seq === null || seq.disable !== 0
	}
	
	override void execute(IDsqSequence seqContext) throws DsqException {
		val seq = seqContext.sequence
		
		if (seq !== null) {
			contexts.clear
			enterScope(false)
			dsqModel.debugvars.interpret
			// set the value of pre-defined variables
			setPredefinedVariableValues(seqContext)
			seq.interpret
			exitScope
		} else if (!seqContext.sequenceName.isEmptyDefaultSequence) {
			throw new DsqException("Sequence named '" + seqContext.sequenceName + "' is undefined")
		}
	}
	
	override String generateCode(String generatorID, String header) throws DsqException {
		if (generator === null) {
			generator = DsqScriptGeneratorFactory.instance.getExtender(generatorID)
			if (generator === null) {
				return null
			}
		}
		if (dsqModel === null) {
			dsqModel = parse
		}
		generator.generate(dsqModel, header)
	}
	
	def private Sequence getSequence(IDsqSequence seqContext) throws Exception {
		if (seqContext === null) {
			throw new DsqException("Predefined variables are not provided")
		}
		checkPredefinedVariables(seqContext)
		if (dsqModel === null) {
			dsqModel = parse
		}
		
		// firstly look for the sequence that matches both the sequence name and processor name
		val matchedSeq = dsqModel.sequences.sequences.findFirst[
			it.name == seqContext.sequenceName && it.pname == deviceInfo.processorName
		]
		
		// if matched Sequence is not found, use the sequence that matches the sequence name
		val seq = if (matchedSeq !== null) matchedSeq else
			dsqModel.sequences.sequences.findFirst[it.name == seqContext.sequenceName]
		
		return seq
	}
	
	def private void initDebugVariables() throws DsqException {
		val dv = if(deviceInfo.debugConfiguration === null) null else deviceInfo.debugConfiguration.debugVars
		val initialText = if (dv === null) '' else dv.text
		val sb = new StringBuilder(initialText + '\n')
		sb.append('''
			«IF dv === null»
			<debugvars>
			«ENDIF»
			__var «IDsqContext::AP» = 0;
			__var «IDsqContext::DP» = 0;
			__var «IDsqContext::PROTOCOL» = 0;
			__var «IDsqContext::CONNECTION» = 0;
			__var «IDsqContext::TRACEOUT» = 0;
			__var «IDsqContext::ERRORCONTROL» = 0;
			«IF dv === null»
			</debugvars>
			«ENDIF»
		''')
		if (dv !== null) {
			val text = dv.text
			if (dv.dgbConfFileName !== null && !dv.dgbConfFileName.empty) {
				sb.append(readFile(Paths.get(dv.dgbConfFileName).toUri().toURL()))
			}
			dv.text = sb.toString
			val xmlParser = new PdscParser
			debugVars = xmlParser.writeToXmlString(dv).postProcess
			dv.text = text
		} else {
			debugVars = sb.toString
		}
	}
	
	def private void checkPredefinedVariables(IDsqContext dsqContext) {
		val ap = dsqContext.getPredefinedVariableValue(IDsqContext::AP)
		if (ap === null) // exception 
			throw new DsqException("Variable " + IDsqContext::AP + " is not provided with a default value")
		
		val dp = dsqContext.getPredefinedVariableValue(IDsqContext::DP)
		if (dp === null) // exception 
			throw new DsqException("Variable " + IDsqContext::DP + " is not provided with a default value")
		
		val p = dsqContext.getPredefinedVariableValue(IDsqContext::PROTOCOL)
		if (p === null) // exception 
			throw new DsqException("Variable " + IDsqContext::PROTOCOL + " is not provided with a default value")
		
		val c = dsqContext.getPredefinedVariableValue(IDsqContext::CONNECTION)
		if (c === null) // exception 
			throw new DsqException("Variable " + IDsqContext::CONNECTION + " is not provided with a default value")
		
		val tc = dsqContext.getPredefinedVariableValue(IDsqContext::TRACEOUT)
		if (tc === null) // exception 
			throw new DsqException("Variable " + IDsqContext::TRACEOUT + " is not provided with a default value")
		
		val ec = dsqContext.getPredefinedVariableValue(IDsqContext::ERRORCONTROL)
		if (ec === null) // exception 
			throw new DsqException("Variable " + IDsqContext::ERRORCONTROL + " is not provided with a default value")
	}
	
	def private void setPredefinedVariableValues(IDsqContext dsqContext) {
		val ap = dsqContext.getPredefinedVariableValue(IDsqContext::AP)
		val dp = dsqContext.getPredefinedVariableValue(IDsqContext::DP)
		val p = dsqContext.getPredefinedVariableValue(IDsqContext::PROTOCOL)
		val c = dsqContext.getPredefinedVariableValue(IDsqContext::CONNECTION)
		val tc = dsqContext.getPredefinedVariableValue(IDsqContext::TRACEOUT)
		val ec = dsqContext.getPredefinedVariableValue(IDsqContext::ERRORCONTROL)
		if (ap !== null)
			contexts.peek.put(IDsqContext::AP, ap)
		if (dp !== null)
			contexts.peek.put(IDsqContext::DP, dp)
		if (p !== null)
			contexts.peek.put(IDsqContext::PROTOCOL, p)
		if (c !== null)
			contexts.peek.put(IDsqContext::CONNECTION, c)
		if (tc !== null)
			contexts.peek.put(IDsqContext::TRACEOUT, tc)
		if (ec !== null)
			contexts.peek.put(IDsqContext::ERRORCONTROL, ec)
	}
	
	/**
	 * Parses all sequences provided by device
	 * @return The root node of the parsed model
	 */
	def private DebugSeqModel parse() throws DsqException {
		// get the root node
		return getResource().getContents().get(0) as DebugSeqModel;
	}
	
	def private Resource getResource() {
		if (resource !== null) {
			return resource
		}
		
		initDebugVariables
		val xmlParser = new PdscParser
		val sequences = deviceInfo.debugConfiguration.sequences.values.map[xmlParser.writeToXmlString(it)].join('\n').postProcess
		val modelString = '''
			«debugVars»
			<sequences>
			«sequences»
			«addDefaultSeqs(deviceInfo.debugConfiguration.sequences)»
			</sequences>
		'''
		
		val resourceSet = resourceSetProvider.get()
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		
		// parse the model
		resource = resourceSet.createResource(URI.createURI("dummy:/dummy.dsq"))
    	resource.load(new StringInputStream(modelString), resourceSet.getLoadOptions())
    	
    	// validate the model
    	val issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl)
    	if (!issues.empty) {
    		dsqModel = null
    		
    		val errors = issues.map[
    			message + ":\n" + modelString.substring(it.offset, it.offset + it.length + 1)
    		].join("\n\n")
    		
    		throw new DsqException("Error while validating the debug sequences in pack file:\n"
    			+ deviceInfo.pack.fileName
    			+ "\n\nDevice: "
    			+ deviceInfo.deviceName
    			+ "\n\n"
    			+ errors
    		)
    	}
    	
    	return resource
	}
	
	def private String readFile(URL url) {
		var result = ""
		val inputStream = url.openConnection().getInputStream()
	    val in = new BufferedReader(new InputStreamReader(inputStream))
	    var inputLine = ""
	 
	    while ((inputLine = in.readLine()) !== null) {
	        result += inputLine + '\n'
	    }

	    in.close();
	    result
	}
	
	def private Collection<String> getDefaultSqs() {
		val url = FileLocator.toFileURL(new URL("platform:/plugin/com.arm.cmsis.pack.dsq.engine/default_sequences"))
		val defaultSeqsFolder = new File(url.file)
		defaultSeqsFolder.list.map[Utils.extractBaseFileName(it)]
	}
	
	def private addDefaultSeqs(Map<String, ICpSequence> sequences) {
		var seqs = ""
		for (defaultSeqName : defaultSqs) {
			if (!sequences.containsKey(defaultSeqName)) {
				seqs += readFile(new URL("platform:/plugin/com.arm.cmsis.pack.dsq.engine/default_sequences/" + defaultSeqName + ".dsq"))
			}
		}
		return seqs
	}
	
	def dispatch Long interpret(Void obj) throws DsqException {
		return 0L
	}
	
	def dispatch Long interpret(DebugVars debugvars) throws DsqException {
		debugvars.statements.forEach[logger.log(it, interpret.toLong)]
		0L
	}
	
	def dispatch Long interpret(Sequence seq) throws DsqException {
		if (logger !== null) 
			logger.logSeqStart(seq.name)
		enterScope(true)
		if (seq.codeblocks !== null)
			seq.codeblocks.forEach[interpret]
		exitScope
		if (logger !== null)
			logger.logSeqEnd(seq.name)
		0L
	}
	
	// a block returns the value of the last statement
	def dispatch Long interpret(Block block) throws DsqException {
		if (logger !== null)
			logger.logBlockStart(block.atomic !== 0, block.info)
		
		// if the block is atomic, we need to backup the symbol table
		val tempContexts = new Stack<Map<String, Long>>
		if (block.atomic !== 0) {
			inAtomic = true
			collectingCommands = true
			commands = newArrayList
			// backup the symbol table
			contexts.forEach[tempContexts.push(new HashMap<String, Long>(it))]
		}
		
		block.statements.interpretUntilLast
		var result = block.statements.last.interpret
		logger.log(block.statements.last, result.toLong)
		
		if (inAtomic) {
			// Execute the Debug Access Commands
			debugSeqClient.execute(commands, true)
			
			// restore the symbol table
			contexts = tempContexts
			
			// Get all the results
			collectingCommands = false
			commandIndex = 0
			block.statements.interpretUntilLast
			result = block.statements.last.interpret
			logger.log(block.statements.last, result.toLong)
			inAtomic = false
			commands.clear
		}
		if (logger !== null)
			logger.logBlockEnd()
		result.toLong
	}
	
	// a control returns the value of the last block
	def dispatch Long interpret(Control control) throws DsqException {
		var result = 0L
		if (logger !== null)
			logger.logContorlStart(control.info)
		enterScope(false)
		if (control.^if === null || logger.logIf(control.^if, control.^if.interpret.toLong) !== 0) {
			if (control.^while === null) {
				control.codeblocks.interpretUntilLast
				result = control.codeblocks.last.interpret.toLong
			} else {
				var timeout = control.timeout;
				if (timeout == 0) {
					timeout = Long::MAX_VALUE	// never time out
				}
				var runningTime = 0L
				val startTime = System.nanoTime
				while (logger.logWhile(control.^while, control.^while.interpret.toLong) !== 0 && runningTime < timeout) {
					control.codeblocks.interpretUntilLast
					result = control.codeblocks.last.interpret.toLong
					runningTime = (System.nanoTime - startTime) / 1000
				}
			}
		}
		exitScope
		if (logger !== null)
			logger.logControlEnd()
		result
	}
	
	def dispatch Object interpret(Expression e) throws DsqException {
		switch (e) {
			IntConstant: e.value
			StringConstant: e.value
			VariableRef: {
				val k = e.variable.name
				k.context.get(k)
			}
			Not: (e.expression.interpret.toLong == 0).toLong
			Assignment: {
				val k = (e.left as VariableRef).variable.name
				val i = k.context.get(k)
				val v = e.right.interpret.toLong
				switch (e.op) {
					case '=': { k.updateValue(v)}
					case '+=': { k.updateValue(i+v)}
					case '-=': { k.updateValue(i-v)}
					case '*=': { k.updateValue(i*v)}
					case '/=': {
						val value = Long.divideUnsigned(i, v)
						k.updateValue(value)
					}
					case '%=': {
						val value = Long.remainderUnsigned(i, v)
						k.updateValue(value)
					}
					case '&lt;&lt;=': { k.updateValue(i<<v.toInteger)}
					case '&gt;&gt;=': { k.updateValue(i>>v.toInteger)}
					case '&amp;=': { k.updateValue(i.bitwiseAnd(v))}
					case '^=': { k.updateValue(i.bitwiseXor(v))}
					case '|=': { k.updateValue(i.bitwiseOr(v))}
					default: 0L
				}
			}
			Ternary: {
				if (e.left.interpret !== 0)
					e.exp1.interpret
				else
					e.exp2.interpret
			}
			Or: ((e.left.interpret.toLong !== 0) || (e.right.interpret.toLong !== 0)).toLong
			And: ((e.left.interpret.toLong !== 0) && (e.right.interpret.toLong !== 0)).toLong
			BitOr: e.left.interpret.toLong.bitwiseOr(e.right.interpret.toLong)
			BitXor: e.left.interpret.toLong.bitwiseXor(e.right.interpret.toLong)
			BitAnd: e.left.interpret.toLong.bitwiseAnd(e.right.interpret.toLong)
			BitNot: e.expression.interpret.toLong.bitwiseNot
			Equality: {
				if (e.op == '==')
					(e.left.interpret == e.right.interpret).toLong
				else
					(e.left.interpret != e.right.interpret).toLong
			}
			Comparison: {
				val left = e.left.interpret.toLong
				val right = e.right.interpret.toLong
					
				switch (e.op) {
					case '&lt;': (Long.compareUnsigned(left, right) < 0).toLong
					case '&gt;': (Long.compareUnsigned(left, right) > 0).toLong
					case '&lt;=': (Long.compareUnsigned(left, right) <= 0).toLong
					case '&gt;=': (Long.compareUnsigned(left, right) >= 0).toLong
					default: 0L
				}
			}
			Shift: {
				val left = e.left.interpret.toLong
				val right = e.right.interpret.toLong.toInteger
				
				switch (e.op) {
					case '&lt;&lt;': left << right
					case '&gt;&gt;': left >> right
					default: 0L
				}
			}
			Plus: e.left.interpret.toLong + e.right.interpret.toLong
			Minus: e.left.interpret.toLong - e.right.interpret.toLong
			Mul: e.left.interpret.toLong * e.right.interpret.toLong
			Div: Long.divideUnsigned(e.left.interpret.toLong, e.right.interpret.toLong)
			Rem: Long.remainderUnsigned(e.left.interpret.toLong, e.right.interpret.toLong)
			SequenceCall: {
				val seq = e.containingSequences.sequences.findFirst[it.name == e.seqname]
				if (seq !== null) {
					seq.interpret
				} else if (!isEmptyDefaultSequence(e.seqname)) {
					throw new DsqException("Sequence with name '" + e.seqname + "' is undefined")
				}
			}
			Query: debugSeqClient.query(e.type.interpret.toLong, e.message, e.^default.interpret.toLong)
			QueryValue: debugSeqClient.query(IDsqClient.QUERY_VALUE_TYPE, e.message, e.^default.interpret.toLong)
			LoadDebugInfo: executeCommand(IDsqCommand.DSQ_LOAD_DEBUG_INFO, #[], #[deviceInfo.getAbsolutePath(e.path)])
			Message: {
				val parameters = e.parameters.map[interpret]
				try {
					val message = e.format.formatWithValues(parameters)
					executeCommand(IDsqCommand.DSQ_MESSAGE, #[e.type.interpret.toLong], #[message])
				} catch (Exception exp) {
					throw new DsqException(exp.message)
				}
			}
			Read8: executeCommand(IDsqCommand.DSQ_READ_8, #[e.addr.interpret.toLong])
			Read16: executeCommand(IDsqCommand.DSQ_READ_16, #[e.addr.interpret.toLong])
			Read32: executeCommand(IDsqCommand.DSQ_READ_32, #[e.addr.interpret.toLong])
			Read64: executeCommand(IDsqCommand.DSQ_READ_64, #[e.addr.interpret.toLong])
			ReadAP: executeCommand(IDsqCommand.DSQ_READ_AP, #[e.addr.interpret.toLong])
			ReadDP: executeCommand(IDsqCommand.DSQ_READ_DP, #[e.addr.interpret.toLong])
			Write8: executeCommand(IDsqCommand.DSQ_WRITE_8, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			Write16: executeCommand(IDsqCommand.DSQ_WRITE_16, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			Write32: executeCommand(IDsqCommand.DSQ_WRITE_32, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			Write64: executeCommand(IDsqCommand.DSQ_WRITE_64, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			WriteAP: executeCommand(IDsqCommand.DSQ_WRITE_AP, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			WriteDP: executeCommand(IDsqCommand.DSQ_WRITE_DP, #[e.addr.interpret.toLong, e.^val.interpret.toLong])
			DapDelay: executeCommand(IDsqCommand.DSQ_DAP_DELAY, #[e.delay.interpret.toLong])
			DapWriteABORT: executeCommand(IDsqCommand.DSQ_DAP_WRITE_ABORT, #[e.value.interpret.toLong])
			DapSwjPins: executeCommand(IDsqCommand.DSQ_DAP_SWJ_PINS, #[e.pinout.interpret.toLong, e.pinselect.interpret.toLong, e.pinwait.interpret.toLong])
			DapSwjClock: executeCommand(IDsqCommand.DSQ_DAP_SWJ_CLOCK, #[e.value.interpret.toLong])
			DapSwjSequence: executeCommand(IDsqCommand.DSQ_DAP_SWJ_SEQUENCE, #[e.cnt.interpret.toLong, e.^val.interpret.toLong])
			DapJtagSequence: executeCommand(IDsqCommand.DSQ_DAP_JTAG_SEQUENCE, #[e.cnt.interpret.toLong, e.tms.interpret.toLong, e.tdi.interpret.toLong])
		}
	}
	
	def private long executeCommand(String cmdName, List<Long> params) {
		executeCommand(cmdName, params, null)
	}
	
	def private long executeCommand(String cmdName, List<Long> params, List<String> strings) {
		if (!inAtomic) {
			val command = new DsqCommand(cmdName, params, strings)
			debugSeqClient.execute(#[command], false)
			command.output
		} else if (!collectingCommands) { // all the results are returned
			val command = findCommand(cmdName, params)
			command.output
		} else { // collecting commands to be sent
			addCommand(cmdName, params, strings)
			0L
		}
	}
	
	def private void addCommand(String cmdName, List<Long> params, List<String> strings) {
		val command = new DsqCommand(cmdName, params, strings)
		commands.add(command)
	}
	
	def private IDsqCommand findCommand(String cmdName, List<Long> params) {
		commands.get(commandIndex++) // all the commands are added in order
	}
	
	def dispatch Long interpret(VariableDeclaration vardecl) throws DsqException {
		if (contexts.isEmpty) {
			enterScope(false)
		}
		contexts.peek.put(vardecl.name, vardecl.value.interpret.toLong)
		0L
	}
	
	def private Map<String, Long> getContext(String k) {
		contexts.findLast[containsKey(k)]
	}
	
	def private long updateValue(String variableName, long newValue) {
		variableName.context.put(variableName, newValue)
		newValue
	}
	
	def private interpretUntilLast(List<? extends EObject> l) {
		if (l.size == 0)
			return 0L
		l.subList(0, l.size-1).forEach[
			val result = interpret
			if (it instanceof Statement) {
				logger.log(it, result.toLong)
			}
		]
	}
	
	def private void enterScope(boolean store) {
		if (contexts.isEmpty && store) {
			throw new DsqException("The symbol table is empty")
		}
		if (!store) {
			contexts.push(newHashMap)
		} else {
			val dp = contexts.peek.get(IDsqContext::DP)
			val ap = contexts.peek.get(IDsqContext::AP)
			val ec = contexts.peek.get(IDsqContext::ERRORCONTROL)
			contexts.push(newHashMap)
			contexts.peek.put(IDsqContext::DP, dp)
			contexts.peek.put(IDsqContext::AP, ap)
			contexts.peek.put(IDsqContext::ERRORCONTROL, ec)
		}
	}
	
	def private void exitScope() {
		contexts.pop
	}
	
	def private Long log(IDsqLogger logger, Statement stmt, Long result) {
		if (logger === null || collectingCommands) {
			result
		} else {
			logger.logStatement(stmt.node.tokenText, result, 0)
			result
		}
	}
	
	def private Long logIf(IDsqLogger logger, Statement stmt, Long result) {
		if (logger === null || collectingCommands) {
			result
		} else {
			logger.logIfStatement(stmt.node.tokenText, result, 0)
			result
		}
	}
	
	def private Long logWhile(IDsqLogger logger, Statement stmt, Long result) {
		if (logger === null || collectingCommands) {
			result
		} else {
			logger.logWhileStatement(stmt.node.tokenText, result, 0)
			result
		}
	}
	
	def private postProcess(String str) {
		var s = str.replace("&#13;", "")
		s = s.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
		s = s.replace("xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"", "")
		s = s.replace(">", ">\n")
		s = s.replace("<", "\n<")
	}
}