 /** 
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
 
package com.arm.cmsis.pack.debugseq.generator

import static extension com.arm.cmsis.pack.debugseq.util.DebugSeqUtil.*

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import com.arm.cmsis.pack.dsq.IDsqContext
import com.arm.cmsis.pack.dsq.IDsqClient
import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel
import com.arm.cmsis.pack.debugseq.debugSeq.DebugVars
import com.arm.cmsis.pack.debugseq.debugSeq.Sequence
import com.arm.cmsis.pack.debugseq.debugSeq.Block
import com.arm.cmsis.pack.debugseq.debugSeq.Control
import com.arm.cmsis.pack.debugseq.debugSeq.VariableDeclaration
import com.arm.cmsis.pack.debugseq.debugSeq.IntConstant
import com.arm.cmsis.pack.debugseq.debugSeq.StringConstant
import com.arm.cmsis.pack.debugseq.debugSeq.VariableRef
import com.arm.cmsis.pack.debugseq.debugSeq.Not
import com.arm.cmsis.pack.debugseq.debugSeq.Assignment
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
import com.arm.cmsis.pack.debugseq.debugSeq.QueryValue
import com.arm.cmsis.pack.debugseq.debugSeq.LoadDebugInfo
import com.arm.cmsis.pack.debugseq.debugSeq.Message
import com.arm.cmsis.pack.debugseq.debugSeq.Expression
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

/**
 * Generates Python script from Debug access sequence descriptions 
 * 
 */
class DsqPythonScriptGenerator extends AbstractGenerator implements IDsqScriptGenerator {
	
	var generateFile = false
	static val predefinedVars =
		'''(«IDsqContext::PROTOCOL», «IDsqContext::CONNECTION», «IDsqContext::DP», «IDsqContext::AP», «IDsqContext::TRACEOUT», «IDsqContext::ERRORCONTROL»)'''

	override getDescription() {
		return "Generates Python script to run on Debug Server"
	}
	
	override getID() {
		return "com.arm.cmsis.pack.dsq.engine.generator.python"
	}
	
	override getName() {
		return "Python Generator"
	}
	
	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val script = resource.allContents.toIterable.filter(typeof(DebugSeqModel)).get(0).generate;
		if (generateFile) {
			fsa.generateFile('debug_sequences.py', script)
		}
	}
	
	def setGenerateFile(boolean genFile) {
		generateFile = genFile
	}
	
	override generate(DebugSeqModel dsqModel, String header) {
		'''
		«header»
		
		«FOR sequence : dsqModel.sequences.sequences»
			«sequence.generate»
			
		«ENDFOR»
		'''
	}
	
	def dispatch String generate(Sequence seq) {
		'''
		def «seq.name»«predefinedVars»:
		    «seq.containingDebugSeqModel.debugvars.generate»
		    «FOR codeblock : seq.codeblocks»
		    	«codeblock.generate»
		    «ENDFOR»
		'''
	}
	
	def dispatch String generate(DebugVars gv) { // global variables
		'''
		«FOR vardecl : gv.statements.filter(typeof(VariableDeclaration)).filter[!name.isPredefinedVariable]»
			«vardecl.generate»
		«ENDFOR»
		«FOR expr : gv.statements.filter(typeof(Expression))»
			«expr.generate»
		«ENDFOR»
		'''
	}
	
	def dispatch String generate(Block block) {
		'''
		«IF block.statements.size === 0»
			pass
		«ENDIF»
		«FOR stmt : block.statements»
			«stmt.generate»
		«ENDFOR»
		'''
	}
	
	def dispatch String generate(Control control) {
		'''
		«IF control.^if !== null»
		if «control.^if.generate»:
		    «IF control.^while !== null»
		    	«control.generateControlWhile»
		    «ELSE»
		    «IF control.codeblocks.size === 0»
		        pass
		    «ENDIF»
		    «FOR codeblock : control.codeblocks»
		    	«codeblock.generate»
		    «ENDFOR»
		    «ENDIF»
		«ELSEIF control.^while !== null»
			«control.generateControlWhile»
		«ELSE»
		«FOR codeblock: control.codeblocks»
			«codeblock.generate»
		«ENDFOR»
		«ENDIF»
		'''
	}
	
	def private String generateControlWhile(Control control) {
		'''
		«IF control.timeout !== 0»
			t = Timer()
		«ENDIF»
		while («control.^while.generate»)«IF control.timeout !== 0» and t.getTime() < «control.timeout»«ENDIF»:
		    «FOR codeblock: control.codeblocks»
				«codeblock.generate»
		    «ENDFOR»
		    continue
		'''
	}
	
	def dispatch String generate(VariableDeclaration vd) {
		'''«vd.name» = «vd.value.generate»'''
	}
	
	def dispatch String generate(Expression e) {
		switch (e) {
			IntConstant: '''«e.value»'''
			StringConstant: '''"«e.value»"'''
			VariableRef: '''«e.variable.name»'''
			Not: '''not («e.expression.generate»)'''
			Assignment: '''«(e.left as VariableRef).variable.name» «e.op» «e.right.generate»'''
			Ternary: '''«e.exp1.generate» if («e.left.generate») != 0 else «e.exp2.generate»'''
			Or: '''(«e.left.generate») != 0 or («e.right.generate») != 0'''
			And: '''(«e.left.generate») != 0 and («e.right.generate») != 0'''
			BitOr: '''(«e.left.generate») | («e.right.generate»)'''
			BitXor: '''(«e.left.generate») ^ («e.right.generate»)'''
			BitAnd: '''(«e.left.generate») & («e.right.generate»)'''
			BitNot: '''~(«e.expression.generate»)'''
			Equality: '''(«e.left.generate») «e.op» («e.right.generate»)'''
			Comparison: '''(«e.left.generate») «IF e.op == "&lt;"» < «ELSEIF e.op == "&gt;"» > «ELSEIF e.op == "&lt;="» <= «ELSE» >= «ENDIF» («e.right.generate»)'''
			Shift: '''(«e.left.generate») «IF e.op == "&lt;&lt;"» << «ELSE» >> «ENDIF» («e.right.generate»)'''
			Plus: '''(«e.left.generate») + («e.right.generate»)'''
			Minus: '''(«e.left.generate») - («e.right.generate»)'''
			Mul: '''(«e.left.generate») * («e.right.generate»)'''
			Div: '''(«e.left.generate») / («e.right.generate»)'''
			Rem: '''(«e.left.generate») % («e.right.generate»)'''
			SequenceCall: '''«e.seqname»«predefinedVars»'''
			Query: '''DS.Query(«e.type.generate», "«e.message»", «e.^default.generate»)'''
			QueryValue: '''DS.Query(«IDsqClient::QUERY_VALUE_TYPE», "«e.message»", «e.^default.generate»)'''
			LoadDebugInfo: '''LoadDebugInfo("«e.path»")'''
			Message: '''Message("«e.format»"«FOR p : e.parameters», «p.generate»«ENDFOR»)'''
			Read8: '''Read8(«e.addr.generate»)'''
			Read16: '''Read16(«e.addr.generate»)'''
			Read32: '''Read32(«e.addr.generate»)'''
			Read64: '''Read64(«e.addr.generate»)'''
			ReadAP: '''ReadAP(«e.addr.generate»)'''
			ReadDP: '''ReadDP(«e.addr.generate»)'''
			Write8: '''Write8(«e.addr.generate», «e.^val.generate»)'''
			Write16: '''Write16(«e.addr.generate», «e.^val.generate»)'''
			Write32: '''Write32(«e.addr.generate», «e.^val.generate»)'''
			Write64: '''Write64(«e.addr.generate», «e.^val.generate»)'''
			WriteAP: '''WriteAP(«e.addr.generate», «e.^val.generate»)'''
			WriteDP: '''WriteDP(«e.addr.generate», «e.^val.generate»)'''
			DapDelay: '''DAP_Delay(«e.delay.generate»)'''
			DapWriteABORT: '''DAP_WriteABORT(«e.value.generate»)'''
			DapSwjPins: '''DAP_SWJ_Pins(«e.pinout.generate», «e.pinselect.generate», «e.pinwait.generate»)'''
			DapSwjClock: '''DAP_SWJ_Clock(«e.value.generate»)'''
			DapSwjSequence: '''DAP_SWJ_Sequence(«e.cnt.generate», «e.^val.generate»)'''
			DapJtagSequence: '''DAP_JTAG_Sequence(«e.cnt.generate», «e.tms.generate», «e.tdi.generate»)'''
		}
	}
	
}
