ARM CMSIS-Pack for Eclipse
==========================

ARM CMSIS-Pack for Eclipse provides a reference implementation for support
of the [CMSIS-Pack] specification in the [Eclipse] development environment.

The intent is to provide a reference implementation of CMSIS-Pack support in [Eclipse] environments
that can be reused by the ARM eco-system to make use of information and resources contained in CMSIS-Pack
without the need to reimplement the fundamentals.

This project currently contains three plug-in projects:
* com.arm.cmsis.pack 	- core responsible for loading pack data and managing RTE (run-time environment) configurations
* com.arm.cmsis.pack.ui - a set of reusable GUI elements that can be used to manipulate pack and RTE data
* com.arm.cmsis.pack.refclient - a sample plug-in demonstrating the access to the Pack content and RTE configuration data

ARM is going to develop and maintain the basic support in sync with the future extensions and modifications to the specification of CMSIS-Pack.

CMSIS compliant Packs are available for download from the [CMSIS Pack Repository].

License
-------

The software is provided under the Apache [License], Version 2.0 [license]. 


This Release
------------
This is an early version of the eclipse plug-in with limitations.
It provides a good starting point for review and experiments. Future versions will
cover more features of the CMSIS-Pack specification, improvements and optimizations.
E.g. this release does not yet provide an integration to Eclipse CDT but only 
stores configuration information into a file. No support for examples and boards.


### Platforms
This release of the CMSIS-Pack for Eclipse has been tested on Eclipse 4.4.0 (Luna)

Getting Started
---------------

### Install CMSIS packs

Download and install packs from [CMSIS Pack Repository] by extracting each pack into an individual subfolder using 
any zip archive utility.
Alternatively on Windows you can install [Keil-MDK-ARM Version 5] and run its PackInstaller to install a selection of packs.

### Get plug-in sources 
Pull the ARM CMSIS-Pack for Eclipse source code from [GitHub].
Add projects to an Eclipse workspace and build them.

### Run reference client application
* Launch RefClient project as an Eclipse application.

* Specify CMSIS-Packs location: Window->Preferences->CMSIS Packs->CMSIS Pack root folder

* To watch installed packs open PackView: Window->Show View->Other...->CMSIS->CMSIS-Packs

* Create an empty project.

* Create an RTE configuration using File->New->Other...->CMSIS-RTE Configuration wizard.
 
* The created configuration will open in an RTE Configuration editor.
  You can select components and save the configuration.

* Open Window->Show View->Other...->CMSIS->RTE Configuration view to see the configuration data.


- - - - - - - - - - - - - - - - - - - - - - - - - -

_Copyright (c) 2014-2015, ARM Limited and Contributors. All rights reserved._


[License]:              ./license.md "Apache License for CMSIS-Pack for Eclipse"
[GitHub]:				           https://www.github.com/ARM-software/cmsis-pack-eclipse

[CMSIS-Pack]:		         http://www.keil.com/pack/doc/CMSIS/Pack/html/index.html
[CMSIS Pack Repository]:		http://www.keil.com/dd2/Pack/
[Keil-MDK-ARM Version 5]: http://www2.keil.com/mdk5/install
[Eclipse]:                http://www.eclipse.org
