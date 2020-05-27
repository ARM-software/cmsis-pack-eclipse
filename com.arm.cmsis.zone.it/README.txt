-------------------------------------------------------------------------------------------------------------------------------------------------
CMSIS-Zone plug-in test (com.arm.cmsis.zone.it)

This plug-in is designed to run automated tests for the CMSIS-Zone feature (com.arm.cmsis.pack.feature)
-------------------------------------------------------------------------------------------------------------------------------------------------

EXISTING TEST CASES:

TC1: LPC55S69     -> Fixed LPC55 example resource description
TC2: M2351	  	  -> Fixed M2351 MDK project
TC3: MCBSTM32F400 -> MDK TCP/IP Example using MPU on MCBSTM32F400 evaluation board (Not working properly in debug way (15.10.2019), FreeMarker templates should be updated)
TC4: Musca-A1	  -> Updated Musca-A1 rzone, azone file (Not working properly in debug way (15.10.2019), FreeMarker templates should be updated)
TC5: SAML11		  -> Added MDK project for SAML11 (Not working properly in debug way (15.10.2019), FreeMarker templates should be updated)
TC6: STM32L5	  -> Fixed STM32L5 blinky_ns flash download script



RUN TESTS SUITE:
	
	1. Click on 'CmsisZoneTestSuite.launch' file (com.arm.cmsis.zone.it/launch_config/CmsisZoneTestSuite.launch) and select 'Run as' CmsisZoneTestSuite
	
	Expected result: automated test will run and results will be printed in console and in a log file inside created project (CmsisZoneTest) in CMSIS-Zone application workspace		 
		


HOW TO SEE TEST RESULTS:

	1. Click on 'Eclipse Application.launch' file (com.arm.cmsis.zone.it/launch_config/Eclipse Application.launch) and select 'Run as' Eclipse Application  

	Expected result: CmsisZoneTest project in CMSIS-Zone application workspace
	

CREATE ADDITIONAL TEST CASES (if needed). Note: This plug-in already has 3 test cases to be tested (See 'EXISTING TEST CASES' section)

	1. Copy desired new test case in TestInputData folder (com.arm.cmsis.zone.it/TestInputData)
	
	2. Change name of new test case in TestInputData folder to the missing or following test case number TC# e.g.:'TC7'

	3. Import desired new test case into the CMSIS-Zone GUI (https://www.keil.com/pack/doc/CMSIS/Zone/html/zTUI.html)

	4. Generate azone files

	5. Copy whole imported project into test plugin 'com.arm.cmsis.zone.it/GoldenResults' and change name of new test case in 'GoldenResults' folder to the missing or following test case number TC# e.g.:'TC7'

	6. Run tests (See RUN TESTS SUITE section)


