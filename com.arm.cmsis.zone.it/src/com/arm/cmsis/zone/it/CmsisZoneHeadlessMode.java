package com.arm.cmsis.zone.it;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.equinox.app.IApplicationContext;

import com.arm.cmsis.zone.gen.HeadlessGen;

public class CmsisZoneHeadlessMode extends HeadlessGen {

    protected HeadlessGen fHeadlessModeCmsisZone = new HeadlessGen();
    private RunTests fRunTests;
    List<String> testDataArgs = new ArrayList<>();
    boolean isTestResultsDirCreated = false;
    protected Integer totalResult = EXIT_OK;
    Set<String> fInputFiles = new HashSet<String>();

    /**
     * Default constructor
     */
    public CmsisZoneHeadlessMode() {
        fRunTests = new RunTests();
        RunTests.setHeadlessModeCmsisZone(this);
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        /*** Prepare test data ***/

        CmsisZoneTestData testData = new CmsisZoneTestData();

        // 1. create CmsisZoneTest project ( just Eclipse project)
        IProject project = testData.createProject();

        if (project != null) {

            // 2. extract structure of 'TestInputData' and 'GoldenData' directories from the
            // 'com.arm.cmsis.zone.it' plug-in to CmsisZoneTest project
            testData.createTestFolderStructure(project);

        }

        // 3. Create 'TestResults' directory in the 'CmsisZoneTest' project
        if (project != null) {
            testData.createTestResultsFolder(project);
        }

        // 4. Creates command line with all '.azone' files found in 'TestInputData'
        // directory
        testData.createInputCommandLine(project);

        // 5. Get command line to be added into the getArguments() of the CMSIS-Zone
        // headless mode
        testDataArgs = testData.geInputCommandLine();

        try {

            /*** Run CMSIS-Zone headless mode ***/

            // Note: CMSIS-Zone headless mode only generates parent 'fzone' file and
            // 'ftl_gen' directory inside CMSIS-Zone project
            totalResult = (Integer) super.start(context);
        } finally {

            /*** Run after azone files generation ***/

            // 1. Compare list of created *.txt files in the TestResults directory to the
            // *txt files in GoldenData directory
            fRunTests.runTests();

        }
        return totalResult;
    }

    @Override
    public Integer getArguments(String[] args) {
        return super.getArguments(testDataArgs.toArray(new String[testDataArgs.size()]));
    }

}
