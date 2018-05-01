package org.sanchouss.idea.plugins.instantpatch.util;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander Perepelkin
 */
public class ClassFinder {
    final String outClassesDir;
    final VirtualFile file;

    public ClassFinder(String outClassesDir, VirtualFile file) {
        this.outClassesDir = outClassesDir;
        this.file = file;
    }

    public List<String> getClassFilesForJava() {
        String javaFileName = file.getName();
        if (!javaFileName.endsWith(".java")) {
            throw new NotJavaResourceException(javaFileName + " filename must end with .java");
        }
        final String classPrefix = javaFileName.substring(0, javaFileName.indexOf(".java"));
        final String nestedClassPrefix = classPrefix; // not only $...
        final String className = classPrefix + ".class";

        File outClassesDirFile = new File(outClassesDir);
        if (!outClassesDirFile.exists()) {
            throw new IllegalArgumentException(outClassesDir + " filename must exist");
        }
        if (!outClassesDirFile.isDirectory()) {
            throw new IllegalArgumentException(outClassesDir + " filename must be a directory");
        }
        File relatedClasses[] = outClassesDirFile.listFiles(new FilenameFilter() {
            //todo: there are more cases: .java may produce other classes than public and its inners
            @Override
            public boolean accept(File dir, String name) {
                return (name.equals(className) || name.startsWith(nestedClassPrefix));
            }
        });

        ArrayList<String> res = new ArrayList<>();
        for (File clazz : relatedClasses) {
            res.add(clazz.getName());
        }

        return res;
    }

    void f() {
        /*
        CompilerManager compilerManager = CompilerManager.getInstance();
        compilerManager.addCompilationStatusListener(new CompilationStatusListener() {
            @Override
            public void compilationFinished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                CompileContext context = compileContext;
//                final SourceToOutputMapping sourceToOutputMap = context.getProject().
//                final SourceToOutputMapping sourceToOutputMap = context.getProjectDescriptor().dataManager.getSourceToOutputMap(chunk.representativeTarget());
            }

            @Override
            public void automakeCompilationFinished(int errors, int warnings, CompileContext compileContext) {

            }

            @Override
            public void fileGenerated(String outputRoot, String relativePath) {

            }
        });
//        CompilerTopics.COMPILATION_STATUS.
//        SourceToOutputMapping
    }
*/
    }
}


