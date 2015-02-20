#CMSIS-Pack Management for Eclipse

The **CMSIS-Pack Management for Eclipse** is created by ARM to provide a open-source reference implementation for the support of the CMSIS-Pack specification in the [Eclipse] environment. It implements the fundamentals to access the information and resources contained in Software Packs and can be re-used by the ARM eco-system in any type of tools, for example configuration utilities or development environments.

ARM is commited to maintain this The **CMSIS-Pack Management for Eclipse** to ensure consistency with future extensions and modifications to the [CMSIS-Pack Specification].  This beta release  has some limitations but is a good starting point for review and experiments. It stores configuration information into a file and does not yet integrate with the Eclipse CDT.
We are now looking for feedback and the final version will cover more features, improvements and optimizations.

For evaluation you need CMSIS-Pack compliant Software Packs that are available for download from the [CMSIS Pack Repository].

**License:** The software is provided under the [Apache License] Version 2.0. 

**Platform:** This release of the **CMSIS-Pack Management for Eclipse** has been tested on Eclipse 4.4.0 (Luna).

##Pre-Build Plug-Ins

This repository contains the pre-built **CMSIS-Pack Management for Eclipse** that can be directly installed.
The plug-ins are build from the [CMSIS-Pack Management for Eclipse] source code repository that can be pulled form GitHub.

This **CMSIS-Pack Management for Eclipse** consists of three plug-ins:
* **com.arm.cmsis.pack** 	- core responsible for loading pack data and managing the RTE (Run-Time Environment) configurations
* **com.arm.cmsis.pack.ui** - set of reusable GUI elements that can be used to manipulate pack and RTE data
* **com.arm.cmsis.pack.refclient** - a sample plug-in demonstrating the access to the pack content and data filtered by RTE selections

![Eclipse Plug-In Overview] 

##Hands-On

###Installing the CMSIS-Pack Management Plug-In

1. **Install Software Packs**: Download and install Software Packs from [CMSIS Pack Repository] by extracting each pack into an individual sub-directory using any zip archive utility. Alternatively on Windows you can install [Keil-MDK-ARM Version 5] and run the **Pack Installer** to install a selection of packs.

2. **Install the Plug-ins** for the CMSIS-Pack Management. Download the **CMSISPackPlugin_x.x.zip** and use the Eclipse menu command: **Help - Install New Software...**. Click **Add...** and the **Archive...** and specify the location of the **CMSISPackPlugin_x.x.zip** file.

###Using the CMSIS-Pack Management Plug-In
1. Start Eclipse with installed **CMSIS-Pack Management Plug-In**.

2. Specify the location of the installed Software Packs with the Eclipse menu command **Window - Preferences - CMSIS Packs**. The **CMSIS Pack root folder** is the base directory, for example *C:\Keil_v5\ARM\Pack*.

3. Verify that the Software Packs are loaded using the Eclipse menu command **Window - Show View - Other... - CMSIS - CMSIS-Packs**. This window allows you to explore the raw data in all Software Packs.

###Setup a RTE configuration
1. Create an RTE configuration using the Eclipse menu command **File - New - Other... - CMSIS - RTE Configuration**. Specify a file name that stores the RTE configuaration information, for example *default.rteconfig*.
 
2. This opens the **New RTE Configuration** wizard were you first select the device from the installed Software Packs. The next step selects the Compiler along with the output (execuable or library). 
![Select the Device]

3. Opening the RTE configuration file (*default.rteconfig*) allows you to select the Software Components that are available based on your device and compiler selection. You may change the component selection and use the **Resolve** toolbar button.
![Component Selection]

###Information Displays

The Eclipse menu command **Window - Show View - Other... - CMSIS** allows you to open the following windows:

* **CMSIS-Packs** displays the content of all installed Software Packs.
* **CMSIS-Devices** displays the devices available in the installed Software Packs.
* **RTE-Configuration** displays the result of the device and component selection of the RTE configuration file that is currently active in the editor.

- - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2014-2015, ARM Limited and Contributors. All rights reserved._


[Apache License]:           ./license.md "Apache License for CMSIS-Pack Management for Eclipse"

[CMSIS-Pack Management for Eclipse]: https://www.github.com/ARM-software/cmsis-pack-eclipse 
[CMSIS Pack Repository]:	  	http://www.keil.com/pack/
[Keil-MDK-ARM Version 5]:   http://www2.keil.com/mdk5/install
[Eclipse]:                  http://www.eclipse.org
[CMSIS-Pack specification]: http://www.keil.com/pack/doc/CMSIS/Pack/html/index.html

[Eclipse Plug-In Overview]: ./images/EclipseOverview.png
[Select the Device]:        ./images/Eclipse2.png
[Component Selection]:      ./images/Eclipse4.png
