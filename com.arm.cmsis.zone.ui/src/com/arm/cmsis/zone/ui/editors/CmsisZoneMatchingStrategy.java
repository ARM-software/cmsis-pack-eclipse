package com.arm.cmsis.zone.ui.editors;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.utils.Utils;

public class CmsisZoneMatchingStrategy implements IEditorMatchingStrategy {

    /**
     * Returns whether the editor represented by the given editor reference matches
     * the given editor input.
     * 
     * @param editorRef the editor reference to match against
     * @param input     the editor input to match
     */
    @Override
    public boolean matches(IEditorReference editorRef, IEditorInput input) {
        if (editorRef == null) {
            return false;
        }

        // Get input's name
        String inputFileName = input.getName();
        // Get input file's extension
        String inputFileExt = Utils.extractFileExtension(inputFileName);

        // Validate if input is either azone or rzone file
        if (inputFileExt.equals(CmsisConstants.AZONE) || inputFileExt.contentEquals(CmsisConstants.RZONE)) {
            // Get editor file's name
            String editorFileName = editorRef.getName();

            if (editorFileName == null || editorFileName.isEmpty())
                return false;

            // Remove extension from editor's file
            editorFileName = Utils.removeFileExtension(editorRef.getName());
            // Remove extension from input file
            inputFileName = Utils.removeFileExtension(inputFileName);

            if (!editorFileName.equals(inputFileName)) {
                return false; // open in new editor
            }

            // We can still have differences in file paths, compare them
            IFile inputFile = ResourceUtil.getFile(input);
            if (inputFile == null)
                return false;
            File inputFileLocation = inputFile.getLocation().toFile();
            String inputFilePath = Utils.removeFileExtension(inputFileLocation.getAbsolutePath());

            String editorFilePath = CmsisConstants.EMPTY_STRING;
            try {
                // Get editor file's full path
                IFile editorRefFile = ResourceUtil.getFile(editorRef.getEditorInput());
                if (editorRefFile == null)
                    return false;
                File editorRefFileLocation = editorRefFile.getLocation().toFile();
                editorFilePath = Utils.removeFileExtension(editorRefFileLocation.getAbsolutePath());
            } catch (PartInitException e) { // thrown by editorRef.getEditorInput()
                e.printStackTrace();
                return false;
            }

            if (inputFilePath.equals(editorFilePath)) {
                return true;
            }

        }
        return false; // open file in new editor;
    }

}
