package com.arm.cmsis.pack.refclient.wizards;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.parser.ConfigParser;
import com.arm.cmsis.pack.refclient.RefClient;
import com.arm.cmsis.pack.rte.IRteConfiguration;
import com.arm.cmsis.pack.rte.RteConfiguration;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.wizards.RteDeviceSelectorPage;

public class NewRteConfigurationWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage filePage = null;
	private RteDeviceSelectorPage devicePage = null;
	private ToolchainSelectorPage toolchainPage = null;

	private IStructuredSelection fSelection;
	private IWorkbench fWorkbench;


	public NewRteConfigurationWizard() {
		setWindowTitle("New Rte Configuration");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.fWorkbench = workbench;
		this.fSelection = selection;
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		filePage = new WizardNewFileCreationPage("Rte Configuration File", fSelection);
		filePage.setTitle("Configuration file");
		filePage.setDescription("Select filename to store RTE configuration information");
		filePage.setFileName("default");
	 	filePage.setFileExtension("rteconfig");
	 	filePage.setAllowExistingResources(true);
	    addPage(filePage);
	    
	    
	    IRteDeviceItem devices = null;
	    ICpPackManager packManager = CpPlugIn.getDefault().getPackManager();
		devices = packManager.getDevices();
	    devicePage = new RteDeviceSelectorPage("Select device", devices);
	    devicePage.setTitle("Device");
	    devicePage.setDescription("Select device");
	    addPage(devicePage);
	    
	    toolchainPage = new ToolchainSelectorPage();
	    addPage(toolchainPage);

	    IRteConfiguration config = RefClient.getDefault().getActiveRteConfiguration();
	    if(config != null) {
	    	devicePage.setInitialSelection(config.getDeviceInfo());
	        toolchainPage.setInitialSelection(config.getToolchainInfo());
	    }
	    

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		if(filePage == null)
			return false;
		if(devicePage == null)
			return false;
		return super.canFinish();	
	}

	@Override
	public boolean performFinish() {
		IFile iFile = filePage.createNewFile();
        if (iFile == null)
            return false;

        ICpDeviceInfo deviceInfo = null;
		if(devicePage != null){
	    	IRteDeviceItem device = devicePage.getDevice();
			if(device == null)
				return false;
			deviceInfo = devicePage.getDeviceInfo(); 
		}

		String compiler = "GCC";
		String output = "exe";
		if(toolchainPage != null) {
			compiler = toolchainPage.getCompilerName();
			output = toolchainPage.getBuildOutput();
		}
		
		ICpItem toolchainInfo = new CpItem(null, "toolchain");
		toolchainInfo.attributes().setAttribute("Tcompiler", compiler);
		toolchainInfo.attributes().setAttribute("Toutput", output);

		IRteConfiguration conf = new RteConfiguration();
		conf.setFilterAttributes(deviceInfo, toolchainInfo);
		conf.apply();
		ConfigParser confParser = new ConfigParser(); 
		
		IPath location = iFile.getLocation();
		if(location!= null) {
			File file =  location.toFile();
			confParser.writeToXmlFile(conf.getConfigurationInfo(), file.getAbsolutePath());
		}
		
		IWorkbenchWindow window = fWorkbench.getActiveWorkbenchWindow();
		if(window != null) {
			IWorkbenchPage page = window.getActivePage();
			if(page != null) {
				try {
					IDE.openEditor(page, iFile);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}

}
