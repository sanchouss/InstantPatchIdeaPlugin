package org.sanchouss.idea.plugins.instantpatch.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.sanchouss.idea.plugins.instantpatch.InstantPatchRemotePluginService;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClient;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessRunnerShell;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteProcessSftpPatcher;
import org.sanchouss.idea.plugins.instantpatch.settings.Process;
import org.sanchouss.idea.plugins.instantpatch.util.ClassFinder;
import org.sanchouss.idea.plugins.instantpatch.util.ExceptionUtils;
import org.sanchouss.idea.plugins.instantpatch.util.NotJavaResourceException;

import java.util.*;
import java.util.stream.Collectors;

import static org.sanchouss.idea.plugins.instantpatch.Checks.SLASH_LINUX_STYLE;


/**
 * Created by Alexander Perepelkin
 * <p>
 * //todo: work on notifications: connections start/finish/fail, other actions - finish/fail
 */
class CopyClassesToRemoteAction extends AnAction {
    private final RemoteProcessSftpPatcher patcher;

    private final RemoteProcessRunnerShell runnerShell;
    private final HashSet<String> allowedResources = new HashSet<>(
            Arrays.asList(".xml", ".json", ".properties", ".csv", ".sh", ".sql"));
    private final String actionTitle = "Copying classes to remote";
    private final RemoteClient remoteClient;

    public CopyClassesToRemoteAction(RemoteClient remoteClient, Process process) {
        super("Copy Selected Classes to Remote Directory");
        this.remoteClient = remoteClient;
        this.patcher = remoteClient.createPatcher(process.getClassFilesDirectory());
        this.runnerShell = remoteClient.createRunnerShell(process.getClassFilesDirectory(), process.getProcessName());
    }

    public void actionPerformed(AnActionEvent e) {
        super.update(e);

        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);

            final RemoteJobCopy jobs = new RemoteJobCopy();
            final VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

            final LinkedList<VirtualFile> filesToCopy = new LinkedList<>(Arrays.asList(files));
            System.out.println("Got " + filesToCopy.size() + " classes to copy chosen initially");
            System.out.println("Allowed to copy resources are: " + allowedResources);
            final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
            final HashSet<String> filesArrangedForCopy = new HashSet<>();

            while (!filesToCopy.isEmpty()) {
                final VirtualFile file = filesToCopy.removeFirst();
                final Module module = index.getModuleForFile(file);
                if (module == null) {
                    throw new RuntimeException("Can not find module for file " + file);
                }
                VirtualFile compilerOutputPath = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
                if (compilerOutputPath == null) {
                    throw new RuntimeException("Module " + module.getName() + " has no compiler output path!");
                }
                final String moduleOutputPath = compilerOutputPath.getPath();

                // construct proper class file location from *source folder *source file location *out folder
                final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

                if (file.isDirectory()) {
                    filesToCopy.addAll(Arrays.asList(file.getChildren()));
                } else {
                    final String packagePath = getPackagePath(sourceRoots, file);
                    try {
                        // look up for the compiled .class files matching passed .java files; prepare jobs to copy compiled files
//                        final List<String> pkg = getPackageDirectory(packagePath);
                        final String outputClassesRelativeDir = packagePath; //StringUtils.join(pkg, SLASH_LINUX_STYLE);
                        final String outputClassesLocalDir = moduleOutputPath + SLASH_LINUX_STYLE + outputClassesRelativeDir;

                        final ClassFinder classFinder = new ClassFinder(outputClassesLocalDir, file);
                        final List<String> classes = classFinder.getClassFilesForJava();
                        if (classes.size() == 0) {
                            throw new IllegalArgumentException("Source file " + file + " does not have compiled class in "
                                    + outputClassesLocalDir);
                        }
                        // TODO: check that timestamp of classes is later that of sources
                        System.out.println("Src " + file.getPath() + "; module: " + module.getName() + "; bin loc dir: "
                                + outputClassesLocalDir + "; out dir: " + outputClassesRelativeDir + "; classes: " + classes);

                        RemoteJobCopy.FileSet copyFilesJob = jobs.submit(outputClassesLocalDir, outputClassesRelativeDir);
                        for (String class_ : classes) {
                            if (filesArrangedForCopy.contains(class_))
                                continue;
                            copyFilesJob.files.add(class_);
                            filesArrangedForCopy.add(class_);
                        }
                    } catch (NotJavaResourceException njre) {
                        // todo: locate resource output directory and take files from output dir
//                        final String outputResourceRelativePath = findOutResourceRelativePath(packagePath);
//                        final String outputResourcesLocalDir = moduleOutputPath + SLASH_LINUX_STYLE + outputResourceRelativePath;
                        final String outputResourceRelativePath = packagePath;
                        final String outputResourcesLocalDir = file.getParent().getPath();
                        // look for allowed resources files
                        final String fname = file.getName();
                        for (String ar : allowedResources) {
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

            if (jobs.jobs.size() == 0) {
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                        "No files are to be copied. Allowed resources are .java and the following files: " +
                                allowedResources.stream().map(s -> "*" + s).collect(Collectors.joining(", ")),
                        NotificationType.WARNING));
            } else {
                remoteClient.enqueue(new CopyClassesToRemoteCommand(jobs));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                    ExceptionUtils.getStructuredErrorString(e1), NotificationType.ERROR));
        }
    }

    private String findOutResourceRelativePath(String subModuleRelativePath) {
        final String resourcesMarker = "resources";
        final int resourcesMarkerPos = subModuleRelativePath.indexOf(resourcesMarker);
        if (resourcesMarkerPos == -1)
            return null;
        String res = subModuleRelativePath.substring(resourcesMarkerPos + resourcesMarker.length());
        return res.startsWith(SLASH_LINUX_STYLE) ? res.substring(1) : res;
    }

    private static class RemoteJobCopy {
        private final HashMap<String, FileSet> jobs = new HashMap<>();
        private final HashSet<String> existAlreadyDirs = new HashSet<>();

        FileSet submit(String outputClassesLocalDir, String toRemoteRelativeDir) {
            FileSet copyFilesJob = jobs.get(outputClassesLocalDir);
            if (copyFilesJob == null) {
                copyFilesJob = new FileSet(outputClassesLocalDir, toRemoteRelativeDir);
                jobs.put(outputClassesLocalDir, copyFilesJob);
            }
            return copyFilesJob;
        }

        TreeMap<String, RemoteJobCopy.FileSet> getJobsOrderedByPath() {
            TreeMap<String, RemoteJobCopy.FileSet> ordered = new TreeMap<>();
            ordered.putAll(jobs);
            return ordered;
        }

        class FileSet {
            private final String fromLocalDir;
            private final String toRemoteRelativeDir;
            private final List<String> files = new ArrayList<>();

            FileSet(String fromLocalDir, String toRemoteRelativeDir) {
                this.fromLocalDir = fromLocalDir;
                this.toRemoteRelativeDir = toRemoteRelativeDir;
            }
        }
    }

    private String getPackagePath(VirtualFile[] sourceRoots, VirtualFile file) {
        VirtualFile sourceFolder = null;
        for (VirtualFile sourceRoot : sourceRoots) {
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
                : file.getParent().getPath().substring(sourceFolder.getPath().length() + 1);

        return res;
    }

    private class CopyClassesToRemoteCommand implements Runnable {
        private final RemoteJobCopy jobs;

        public CopyClassesToRemoteCommand(RemoteJobCopy jobs) {
            this.jobs = jobs;
        }

        @Override
        public void run() {
            try {

                int copiedFiles = 0;
                TreeMap<String, RemoteJobCopy.FileSet> jobsOrdered = jobs.getJobsOrderedByPath();
                for (Map.Entry<String, RemoteJobCopy.FileSet> jobEntry : jobsOrdered.entrySet()) {
                    RemoteJobCopy.FileSet fileSet = jobEntry.getValue();

                    runnerShell.mkdir(fileSet.toRemoteRelativeDir, jobs.existAlreadyDirs);
                    // todo: if directory was absent, then newly created might not be seen immediately
//                    patcher.cd(fileSet.toRemoteRelativeDir);    // prevent No such file
                    patcher.uploadFiles(fileSet.fromLocalDir, fileSet.toRemoteRelativeDir, fileSet.files);

                    copiedFiles += fileSet.files.size();
                }

                String msg = "Uploaded " + copiedFiles + " files to " + jobsOrdered.size() + " remote dirs";
                System.out.println(msg);

                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                        msg, NotificationType.INFORMATION));

            } catch (Exception e1) {
                e1.printStackTrace();
                Notifications.Bus.notify(new Notification(InstantPatchRemotePluginService.notificationGroupId, actionTitle,
                        ExceptionUtils.getStructuredErrorString(e1), NotificationType.ERROR));
            }
        }
    }
}
