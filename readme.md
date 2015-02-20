#CMSIS-Pack Management for Eclipse

The **CMSIS-Pack Management for Eclipse** is created by ARM to provide a open-source reference implementation for the support of the CMSIS-Pack specification in the [Eclipse] environment. It implements the fundamentals to access the information and resources contained in Software Packs and can be re-used by the ARM eco-system in any type of tools, for example configuration utilities or development environments.

ARM is commited to maintain this **CMSIS-Pack Management for Eclipse** to ensure consistency with future extensions and modifications to the [CMSIS-Pack Specification].  This beta release  has some limitations but is a good starting point for review and experiments. It stores configuration information into a file and does not yet integrate with the Eclipse CDT.
We are now looking for feedback and the final version will cover more features, improvements and optimizations.

**License:** The software is provided under the [Apache License] Version 2.0. 

**Platform:** This release of the **CMSIS-Pack Management for Eclipse** has been tested on Eclipse 4.4.0 (Luna).

##Source Code 

This repository contains the source code of the **CMSIS-Pack Management for Eclipse** that consists of three plug-ins:
* **com.arm.cmsis.pack** 	- core responsible for loading pack data and managing the RTE (Run-Time Environment) configurations
* **com.arm.cmsis.pack.ui** - set of reusable GUI elements that can be used to manipulate pack and RTE data
* **com.arm.cmsis.pack.refclient** - a sample plug-in demonstrating the access to the pack content and data filtered by RTE selections

![Eclipse Plug-In Overview] 

##Usage and Pre-Built Plug-In

Refer to [Hands-on section] of the pre-built plug-in of the CMSIS-Pack Management for detailed usage information.

- - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2014-2015, ARM Limited and Contributors. All rights reserved._


[Apache License]:           ./license.md "Apache License for CMSIS-Pack Management for Eclipse"

[CMSIS-Pack Management for Eclipse]: https://www.github.com/ARM-software/cmsis-pack-eclipse 
[Hands-on section]:         https://github.com/ARM-software/cmsis-pack-eclipse-prebuilt#hands-on
[CMSIS Pack Repository]:	  http://www.keil.com/pack/
[Keil-MDK-ARM Version 5]:   http://www2.keil.com/mdk5/install
[Eclipse]:                  http://www.eclipse.org
[CMSIS-Pack specification]: http://www.keil.com/pack/doc/CMSIS/Pack/html/index.html

[Eclipse Plug-In Overview]: ./images/EclipseOverview.png
[Select the Device]:        ./images/Eclipse2.png
[Component Selection]:      ./images/Eclipse4.png
