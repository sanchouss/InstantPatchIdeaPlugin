package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by
 */
public class ChooseFileTest {
    @Ignore
    @Test
    public void testChoose() {
        final FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false,false, false);
        final AtomicReference<String> chosenFile = new AtomicReference<>();
        final File start = new File("C:\\usr\\");
        Assert.assertTrue(start.exists());
        final VirtualFile toSelect =
            LocalFileSystem.getInstance().findFileByIoFile(start);
        FileChooser.chooseFile(descriptor, null, toSelect, virtualFile -> chosenFile.set(virtualFile.getCanonicalPath()));
    }

}
