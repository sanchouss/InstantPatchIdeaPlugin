package org.sanchouss.idea.plugins.instantpatch.actions;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.jcraft.jsch.SftpException;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginRegistration;
import org.sanchouss.idea.plugins.instantpatch.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.RemoteProcessPatcher;
import org.sanchouss.idea.plugins.instantpatch.RemoteProcessRunnerShell;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;


/**
 * Created by Alexander Perepelkin
 */
class CopyClassesToRemoteAction extends AnAction {
    private final RemoteProcessPatcher patcher;

    private final RemoteProcessRunnerShell runnerShell;
    private final HashSet<String> allowedResources = Sets.newHashSet(Arrays.asList(".xml", ".json", ".properties",".csv", ".sh"));
    private final String actionTitle = "Copying classes to remote";

    public CopyClassesToRemoteAction(RemoteClient remoteClient, Process process) {
        super("Copy Selected Classes to Remote Directory");
        this.patcher = remoteClient.createPatcher(process.getClassFilesDirectory());
        this.runnerShell = remoteClient.createRunnerShell(process.getClassFilesDirectory(), process.getProcessName());
    }

    public void actionPerformed(AnActionEvent e) {
        super.update(e);

        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);

            final RemoteJobCopy jobs = new RemoteJobCopy(patcher, runnerShell);
            final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

            final LinkedList<VirtualFile> filesToCopy = Lists.newLinkedList(Arrays.asList(files));
            System.out.println("Got " + filesToCopy.size() + " items to copy chosen initially");
            System.out.println("Allowed to copy resources are: " + allowedResources);
            final ProjectFileIndex index =  ProjectRootManager.getInstance(project).getFileIndex();
            final HashSet<String> filesArrangedForCopy = Sets.newHashSet();
            while (!filesToCopy.isEmpty()) {
                final VirtualFile file = filesToCopy.removeFirst();
                final Module module = index.getModuleForFile(file);
                if (module == null) {
                    throw new RuntimeException("Can not find module for file " + file);
                }
                final String moduleOutputPath = CompilerPaths.getModuleOutputPath(module, false);
                // construct proper class file location from *source folder *source file location *out folder
                final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

                if (file.isDirectory()) {
                    filesToCopy.addAll(Arrays.asList(file.getChildren()));
                } else {
                    final String packagePath = getPackagePath(sourceRoots, file);
                    try {
                        // look up for the compiled .class files matching passed .java files; prepare jobs to copy compiled files
//                        final List<String> pkg = getPackageDirectory(packagePath);
                        final String outputClassesRelativeDir = packagePath; //StringUtils.join(pkg, "/");
                        final String outputClassesLocalDir = moduleOutputPath + "/" + outputClassesRelativeDir;

                        final List<String> classes = getClassFilesForJava(outputClassesLocalDir, file);
                        if (classes.size() == 0) {
                            throw new IllegalArgumentException("File " + file + " does not have compiled class in " + outputClassesLocalDir);
                        }
                        // TODO: check that timestamp of classes is later that of sources
                        System.out.println("Src " + file.getPath() + "; module: " + module.getName() + "; bin loc dir: " + outputClassesLocalDir + "; out dir: " + outputClassesRelativeDir + "; classes: " + classes);

                        RemoteJobCopy.FileSet copyFilesJob = jobs.submit(outputClassesLocalDir, outputClassesRelativeDir);
                        for (String class_ : classes) {
                            if (filesArrangedForCopy.contains(class_))
                                continue;
                            copyFilesJob.files.add(class_);
                            filesArrangedForCopy.add(class_);
                        }
                    } catch (NotJavaResourceException njre) {
                        // todo: locate resource ouput directory and take files from output dir
//                        final String outputResourceRelativePath = findOutResourceRelativePath(packagePath);
//                        final String outputResourcesLocalDir = moduleOutputPath + "/" + outputResourceRelativePath;
                        final String outputResourceRelativePath = packagePath;
                        final String outputResourcesLocalDir = file.getParent().getPath();
                        // look for allowed resources files
                        final String fname = file.getName();
                        for (String ar: allowedResources) {
                            if (fname.endsWith(ar)) {
//                                if (outputResourceRelativePath == null) {
//                                    System.out.println("Can not find 'resources' subdirectory within the path " + packagePath);
//                                    break;
//                                }
                                System.out.println("Src " + file.getPath() + "; module: " + module.getName() + "; bin loc dir: " + outputResourcesLocalDir + "; out dir: " + outputResourceRelativePath + "; resource " + file.getName());
                                RemoteJobCopy.FileSet copyFilesJob = jobs.submit(outputResourcesLocalDir, outputResourceRelativePath);
                                copyFilesJob.files.add(file.getName());
                                break;
                            }
                        }
                    }
                }
            }

            int copiedFiles = 0;
            TreeMap<String, RemoteJobCopy.FileSet> jobsOrdered = jobs.getJobsOrderedByPath();
            for (Map.Entry<String, RemoteJobCopy.FileSet> jobEntry: jobsOrdered.entrySet()) {
                RemoteJobCopy.FileSet remoteJob = jobEntry.getValue();
                remoteJob.copySet();
                copiedFiles += remoteJob.files.size();
            }

            String msg = "Uploaded " + copiedFiles + " files to " + jobsOrdered.size() + " remote dirs";
            System.out.println(msg);

            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                    msg, NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER));

        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginRegistration.notificationGroupId, actionTitle,
                e1.toString(), NotificationType.ERROR, NotificationListener.URL_OPENING_LISTENER));
        }
    }

    private String findOutResourceRelativePath(String subModuleRelativePath) {
        final String resourcesMarker = "resources";
        final int resourcesMarkerPos = subModuleRelativePath.indexOf(resourcesMarker);
        if (resourcesMarkerPos == -1)
            return null;
        String res = subModuleRelativePath.substring(resourcesMarkerPos + resourcesMarker.length());
        return res.startsWith("/") ? res.substring(1) : res;
    }

    private static class RemoteJobCopy {
        private final RemoteProcessPatcher patcher;
        private final RemoteProcessRunnerShell runnerShell;
        private final HashMap<String, FileSet> jobs = Maps.newHashMap();
        private final HashSet<String> existAlreadyDirs = Sets.newHashSet();

        public RemoteJobCopy(RemoteProcessPatcher patcher, RemoteProcessRunnerShell runnerShell) {
            this.patcher = patcher;
            this.runnerShell = runnerShell;
        }

        FileSet submit(String outputClassesLocalDir, String toRemoteRelativeDir) {
            FileSet copyFilesJob = jobs.get(outputClassesLocalDir);
            if (copyFilesJob == null) {
                copyFilesJob = new FileSet(outputClassesLocalDir, toRemoteRelativeDir);
                jobs.put(outputClassesLocalDir, copyFilesJob);
            }
            return copyFilesJob;
        }

        TreeMap<String, RemoteJobCopy.FileSet> getJobsOrderedByPath() {
            TreeMap<String, RemoteJobCopy.FileSet> ordered = Maps.newTreeMap();
            ordered.putAll(jobs);
            return  ordered;
        }

        class FileSet {
            private final String fromLocalDir;
            private final String toRemoteRelativeDir;
            private final List<String> files = Lists.newArrayList();

            FileSet(String fromLocalDir, String toRemoteRelativeDir) {
                this.fromLocalDir = fromLocalDir;
                this.toRemoteRelativeDir = toRemoteRelativeDir;
            }

            public void copySet() throws SftpException, IOException {
                // one - by - one creation:
//            patcher.cd();
//            patcher.mkdir(toRemoteRelativeDir, existAlreadyDirs);
                // create with parents
                runnerShell.mkdir(toRemoteRelativeDir, existAlreadyDirs);
                patcher.uploadFiles(fromLocalDir, toRemoteRelativeDir, files);
            }
        }
    }

    private List<String> getClassFilesForJava(String outClassesDir, VirtualFile file) {
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

            @Override
            public boolean accept(File dir, String name) {
                return (name.equals(className) || name.startsWith(nestedClassPrefix));
            }
        });

        ArrayList<String> res = Lists.newArrayList();
        for (File clazz: relatedClasses) {
            res.add(clazz.getName());
        }

        return res;
    }

    private class NotJavaResourceException extends RuntimeException {
        public NotJavaResourceException(String message) {
            super(message);
        }
    }

    private String getPackagePath(VirtualFile[] sourceRoots, VirtualFile file) {
        VirtualFile sourceFolder = null;
        for (VirtualFile sourceRoot: sourceRoots) {
            VirtualFile parent = file.getParent();
            while (parent != null) {
                if (parent.equals(sourceRoot)) {
                    sourceFolder = parent;
                    break;
                }
                parent = parent.getParent();
            }
            if (sourceFolder != null) {
                break;
            }
        }

        if (sourceFolder == null) {
            throw new RuntimeException("Can not find source folder for the selected file " + file);
        }

        final String res = (file.getParent() == sourceFolder)
            // means no package hierarchy
            ? ""
            // strip leading slash
            : file.getParent().getPath().substring(sourceFolder.getPath().length()+1);

        return res;
    }

    private List<String> getPackageDirectory(String subdir) {
/*
        int firstComPackage = subdir.indexOf("/com/");
        if (firstComPackage == -1) {
            throw new NotJavaResourceException("Can not find /com/ part of the package in the " + subdir);
        }

        String packagePath =  subdir.substring(firstComPackage+1);
*/

        String[] pkg = subdir.split("/");

        return Lists.newArrayList(pkg);
    }

}