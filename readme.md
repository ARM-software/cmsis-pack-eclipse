ARM CMSIS-Pack for Eclipse
==========================

ARM CMSIS-Pack for Eclipse provides a reference implementation for support
of the [CMSIS-Pack] standard in Eclipse.

The intent is to provide a reference implementation of CMSIS-Pack support in Eclipse environments
that can be used by ARM Development Tool vendors to add support for CMSIS-Pack to their products.

This project contains three plug-in projects:
* com.arm.cmsis.pack 	- core responsible for loading pack data and managing RTE (run-time environment) configuration 
* com.arm.cmsis.pack.ui - a set of reusable GUI elements that can be used to manipulate pack and RTE data
* com.arm.cmsis.pack.refclient - a sample plug-in demonstrating how to access Pack and RTE data
 
An index of existing CMSIS-Packs is available on the [Keil Pack page].

ARM will continue development in collaboration with interested parties to
provide a full reference implementation of CMSIS-Pack in Eclipse
to the benefit of all developers working with software components for ARM-based microcontrollers.


License
-------

The software is provided under the Apache [License], Version 2.0 [license]. 


This Release
------------
This initial release is a limited functionality implementation of CMSIS-Pack for Eclipse.
It provides a suitable starting point for productization. Future versions will
contain new features, optimizations and quality improvements.
Note that current release does not provide any integration to Eclipse CDT.  


### Platforms
This release of the CMSIS-Pack for Eclipse has been tested on Eclipse 4.4.0 (Luna)

Getting Started
---------------

### Install CMSIS packs

Download and install packs from [Keil Pack page]. 
Alternatively on Windows you can install [Keil-MDK-ARM Version 5] and run its PackInstaller to install required packs.

The packs can be installed using MDK-Lite license, included in Keil-MDK-ARM installation.

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
[Keil Pack page]:		     http://www.keil.com/dd2/Pack/
[Keil-MDK-ARM Version 5]: http://www2.keil.com/mdk5/install
