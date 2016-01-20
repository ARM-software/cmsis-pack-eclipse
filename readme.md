#CMSIS-Pack Management for Eclipse

The **CMSIS-Pack Management for Eclipse** is created by ARM to provide a open-source reference implementation for the support of the CMSIS-Pack specification in the [Eclipse] environment. It implements the fundamentals to access the information and resources contained in Software Packs and can be re-used by the ARM eco-system in any type of tools, for example configuration utilities or development environments.

ARM is commited to maintain this **CMSIS-Pack Management for Eclipse** to ensure consistency with future extensions and modifications to the [CMSIS-Pack Specification].  

This release 1.1 allows creation and management of an CDT-based C/C++ project. We are now looking for feedback and the future versions will cover more features, improvements and optimizations.

For evaluation you need CMSIS-Pack compliant Software Packs that are available for download from the [CMSIS Pack Repository].

**License:** The software is provided under the [Eclipse Public License] Version 1.0. 

**Platform:** This release of the **CMSIS-Pack Management for Eclipse** has been tested on Eclipse 4.4.0 (Luna).

##Source Code 
This repository contains the source code of the **CMSIS-Pack Management for Eclipse** that consists of eight plug-ins:
* **com.arm.cmsis.pack** 	- core responsible for loading pack data and managing the RTE (Run-Time Environment) configurations
* **com.arm.cmsis.pack.common** 	- generic base code, constants and utilities
* **com.arm.cmsis.pack.ui** - set of reusable GUI elements that can be used to manipulate pack and RTE data
* **com.arm.cmsis.pack.project** - CDT project management
* **com.arm.cmsis.pack.build.settings** - Toolchain adapter extension point and generic adapter
* **com.arm.cmsis.pack.build.setting.armcc5** - adapter for  ARMCC 5.x toolchain (DS-5 built-in)
* **com.arm.cmsis.pack.build.setting.armgcc** - adapter for  ARM GCC toolchain (DS-5 built-in)
* **com.arm.cmsis.pack.build.setting.gnuarmeclipse** - adapter for Cross ARM GNU toolchain: http://gnuarmeclipse.github.io/

###CMSIS-Pack Management Plug-In has been presented at the Eclipse Conference Europe 2015

![EclipseConEurope2015] 

The presentation is available on [YouTube], slides to download:
[Enhanced Project Management for Embedded C/C++ Programming using Software Components]

##Hands-On  

![Eclipse Plug-In Overview] 

###Installing the CMSIS-Pack Management Plug-In

1. **Install Software Packs**: Download and install Software Packs from [CMSIS Pack Repository] by extracting each pack into an individual sub-directory using any zip archive utility. Alternatively on Windows you can install [Keil-MDK-ARM Version 5] and run the **Pack Installer** to install a selection of packs or use the [GNU ARM Eclipse Packs Manager] available here: http://gnuarmeclipse.sourceforge.net/updates-test.

2. **Install the Plug-ins** for the CMSIS-Pack Management. Download the **CMSISPackPlugin_x.x.zip** and use the Eclipse menu command: **Help - Install New Software...**. Click **Add...** and the **Archive...** and specify the location of the **CMSISPackPlugin_x.x.zip** file.

###Using the CMSIS-Pack Management Plug-In:  
1. Start Eclipse with installed **CMSIS-Pack Management Plug-In**.

2. Specify the location of the installed Software Packs with the Eclipse menu command **Window - Preferences - CMSIS Packs**. The **CMSIS Pack root folder** is the base directory, for example *C:\Keil_v5\ARM\Pack*.

3. Verify that the Software Packs are loaded using the Eclipse menu command **Window - Show View - Other... - CMSIS - CMSIS-Packs**. This window allows you to explore the raw data in all Software Packs.

## Creating RTE project 
1. Open New C/C++ Project wizard using Elipse menu command **File - New - Project... - C/C++ - C Project CMSIS - RTE Configuration**.
2. In the first page specify Project name, select desired Toolchain and "CMSIS RTE C/C++ Project" type, press Next. ![NewProjectWizardTemplatePage]
3. In the second page select a Toolchain Adapter and press next.
![Select Adapter]

 Note: CMSIS-Pack Eclipse plug-in contains adapters for the following toolchains:
 * ARM Compiler 5 (ARM DS-5 built-in)
 * Cross ARM GCC (http://gnuarmeclipse.github.io/)
 
 If one of those toolchains is selected in the first page, the matching adapter is selected automatically.

 For any other toolchain select Generic Toolchain Adapter and a toolchain family (defines component filtering).
 That does not set all required compiler options, but allows to experiment with RTE Project management.
4. In the third page select a device from the installed Software Packs and press Finish or continue to the next pages.
![Select Device]

5. New C/C++ will be created and the RTE configuration editor will open. The editor allows you to select the Software Components that are available based on your device and compiler selection. 
You may change the component selection and use the **Resolve** toolbar button. 
![Component Selection]

6. Save the configuration by pressing **Save** toolbar button. This will trigger project update. You can now investigate project content, compiler settings and build the project.

###Information Displays

The Eclipse menu command **Window - Show View - Other... - CMSIS** allows you to open the following windows:

* **CMSIS-Packs** displays the content of all installed Software Packs.
* **CMSIS-Devices** displays the devices available in the installed Software Packs.


- - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2014-2016, ARM Limited and Contributors. All rights reserved._


[Eclipse Public License]:   ./license.md "Eclipse Public License for CMSIS-Pack Management for Eclipse"

[CMSIS-Pack Management for Eclipse]: https://www.github.com/ARM-software/cmsis-pack-eclipse 
[CMSIS Pack Repository]:	  http://www.keil.com/pack/
[Keil-MDK-ARM Version 5]:   http://www2.keil.com/mdk5/install
[Eclipse]:                  http://www.eclipse.org
[CMSIS-Pack specification]: http://www.keil.com/pack/doc/CMSIS/Pack/html/index.html

[Eclipse Plug-In Overview]:      ./images/EclipseOverview.png
[NewProjectWizardTemplatePage]:  ./images/NewProjectWizardTemplatePage.png
[Select Adapter]:             	./images/NewProjectWizardAdapterPage.png
[Select Device]:             ./images/NewProjectWizardDevicePage.png
[Component Selection]:           ./images/RteConfigEditor.png
[GNU ARM Eclipse Packs Manager]: http://gnuarmeclipse.livius.net/blog/packs-manager/
[http://gnuarmeclipse.github.io/]: http://gnuarmeclipse.github.io/ 

[EclipseConEurope2015]:     ./images/EclipseConEurope2015.png
[YouTube]: https://www.youtube.com/watch?v=z8n2I1s6zgg&list=PLy7t4z5SYNaR0yp9EQ9txQhO-JgCLJAga&index=29
[Enhanced Project Management for Embedded C/C++ Programming using Software Components]: https://www.eclipsecon.org/europe2015/session/enhanced-project-management-embedded-cc-programming-using-software-components

