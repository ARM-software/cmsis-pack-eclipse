# Debug Sequence Framework

This document describes implementation of Debug Sequence Framework for Eclipse and provides instructions for engineers how to integrate the framework into their development environments. 
The framework provides support for [CMSIS Debug Access Sequences].
The framework is split into two parts to separate interfaces from implementation:
* **com.arm.cmsis.pack** plug-in defines interfaces and datatypes
* **com.arm.cmsis.pack.dsq.engine** plug-in provides default Debug Sequence Engine implementation that consists of parser, executer and script generator.  

The parser is implemented using [Xtext](https://eclipse.org/Xtext/) framework and therefore requires Xtext Runtime plug-ins for execution.  Full Xtext SDK is only needed to regenerate sources in a rare case of changes in Debug access sequence grammar.  Therefore all generated sources are also checked-in into this Git repository. 

The Debug Sequence Engine provides two integration options:
* execution of debug sequence commands via Java API
* generation an environment-specific debug script (for instance Python) that is then executed by a debugger or its Debug Server

Debug sequence parsing is common for both cases.

## Debug Sequence Engine with an Executor
The **com.arm.cmsis.pack** plug-in defines **IDsqClient** interface that must be implemented by a Debugger before running the engine.
It must provide the implementations of executing commands for the engine such as read/write value from memory/register,
setup application and debug ports.

![Dsq Engine Architecture]

The work flow of using the Debug Sequence Engine with an Executor is like follows:
1.	The Debugger creates a DsqClient that implements IDsqClient.
2.	The Debugger passes an ICpDeviceInfo (obtained from CSMS project) and the created IDsqClient to DsqEngineFactory to let it create an engine.
3.	The Debug Sequence Engine creates debug sequence model by parsing sequences from the device description provided ICpDeviceInfo. Default implementation of a standard sequence is used if it is not overridden by device description. In the end the Engine will have a collection of Debug Sequences as virtual codes.
4.	When the Debugger needs to execute a debug sequence, it creates a DsqSequence object which contains all the necessary information for the Executor such as the Sequence name and the values of pre-defined variables.
5.	The Debugger sends the DsqSequence to the Executor to let it start executing.
6.	The Executor tries to find the Sequence in its collection (terminates immediately is sequence not found).
7.	The Executor executes the next statements in the Sequence.
8.	If a statement needs target access such as reading values from the target, it constructs a DsqCommand with all the necessary arguments. 
9.	The Executor sends the DsqCommand to the Debugger to get the result of the command. In case of an atomic block, all commands in the block are sent in one call as a list.
10.	The Debugger makes target access and set the DsqCommand’s output.
11.	The Debugger sends the DsqCommand with output back to the Executor.
12.	The Executor returns the output of the command as the result of the statement. Back to step 7 until all the statements have been executed.

## Debug Sequence Engine with a Script Generator
The Debug Sequence Engine can generate a script that a client Debugger or its Debug Server can execute.  

![Dsq Engine Script Generator]

At the Debugger side, it needs to have a sequence builder which defines the Sequence name and provides values for predefined variables.
The work flow is in this case looks like that: 
1.	The Debugger creates a Debug Sequence Engine by calling the DsqEngineFactory passing ICpDeviceInfo obtained from CSMS C/C++project
2.	The Debug Sequence Engine creates debug sequence model by parsing sequences from the device description provided ICpDeviceInfo. Default implementation of a standard sequence is used if it is not overridden by device description. In the end the Engine will have a collection of Debug Sequences as virtual codes.
3.	The Debugger calls IDsqEngine.generateCode (String generatorID, String header). The ID defines the generator to be used. Header string provides required information:  predefined variable values and imports, in particular target read/write access methods. 
4.	The Debugger or its Debug Server executes the sequences from script when needed. 

The **com.arm.cmsis.pack.dsq.engine** plug-in contains built-in Python script generator (ID=**com.arm.cmsis.pack.dsq.engine.generator.python**). 
You can provide your own script generator for other languages (JavaScript, TCL, etc.) by extending the **ScriptGenerator** extension point.

## DsqClient example

An example of using Debug Sequnce Engine in Executor mode is provided by **com.arm.cmsis.pack.refclient** plug-in. 
It uses a dummy **DsqClient** which sets the output of a command by rotating a long value to left by one. See **SequenceView** class for implementation.

To see the Sequence View, you need to create a CMSIS C/C++ project, then open the Device Sequences View. 
Select the project and you’ll see the default sequences listed in the view:

![RefClient Dsq View] 

When you click the Execute button of any Sequence, the output will be shown in the CMSIS Sequence console:

![RefClient Dsq Console] 

The left side of ‘->’ in a line corresponds to a statement in the Debug Sequence. The right side is the result of this statement execution (in this case it's a dummy long value).

- - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2014-2021, ARM Limited and Contributors. All rights reserved._


[CMSIS Debug Access Sequences]: https://arm-software.github.io/CMSIS_5/Pack/html/pdsc_SequenceNameEnum_pg.html

[Dsq Engine Architecture]:    ./images/DsqEngineArchitecture.png
[Dsq Engine Script Generator]:./images/DsqScriptGenerator.png
[RefClient Dsq View]:  			  ./images/DsqRefClientView.png
[RefClient Dsq Console]:  		./images/DsqRefClientConsole.png

