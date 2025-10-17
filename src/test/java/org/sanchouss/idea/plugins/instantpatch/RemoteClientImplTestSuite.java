package org.sanchouss.idea.plugins.instantpatch;

import org.junit.Test;
import org.sanchouss.idea.plugins.instantpatch.remote.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.fail;
import static org.sanchouss.idea.plugins.instantpatch.JschCredentials.*;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteClientImplTestSuite {
    private String homedir = "/home/instantpatch/work/";
    private String processdir = homedir + "tmp/process1/";
    private String toRemoteDirectory = "com/company/";
    private RemoteAuth remoteAuth = new RemoteAuth(privateKey, passphrase);
    public static final String reset_sh = "reset.sh";

    @Test
    public void testRunnerShell() {
        try {
            RemoteClient rc = new RemoteClientImpl(host, user, port, remoteAuth);

            Thread.sleep(100);
            {
                RemoteProcessRunnerShell createRunnerCont = rc.createRunnerShell(homedir, "");
                createRunnerCont.echo("I expect to get this msg back");
                createRunnerCont.rmdir();
            }

            {
                RemoteProcessRunnerShell createRunnerCont = rc.createRunnerShell(homedir, "");
                createRunnerCont.mkdir("com/test/proc/");
                for (int i = 0; i < 3; ++i) {
                    createRunnerCont.exec("pwd");
                    Thread.sleep(300);
                }
            }

            Thread.sleep(100);
            rc.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatcher() {
        try {
            RemoteClient rc = new RemoteClientImpl(host, user, port, remoteAuth);

            RemoteProcessSftpPatcher patcher = rc.createPatcher(processdir);
            patcher.mkdir();
            patcher.cd();
            patcher.mkdir(toRemoteDirectory);
            patcher.mkdir(toRemoteDirectory);

            List<String> files = new ArrayList<>();
            files.add(reset_sh);
            URL url = getClass().getClassLoader().getResource(reset_sh);
            File file = new File(url.toURI());
            String fromLocalDirectory = file.getParentFile().getAbsolutePath();
            patcher.uploadFiles(fromLocalDirectory, toRemoteDirectory, files);

            Thread.sleep(100);
            rc.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatcherAndExec() {
        try {
            RemoteClient rc = new RemoteClientImpl(host, user, port, remoteAuth);

            RemoteProcessSftpPatcher patcher = rc.createPatcher(processdir);
            patcher.mkdir();
            patcher.cd();
            patcher.mkdir(toRemoteDirectory);

            List<String> files = new ArrayList<>();
            files.add(reset_sh);
            URL url = getClass().getClassLoader().getResource(reset_sh);
            File file = new File(url.toURI());
            String fromLocalDirectory = file.getParentFile().getAbsolutePath();

            patcher.uploadFiles(fromLocalDirectory, toRemoteDirectory, files);
            Thread.sleep(100);
            patcher.chmod(0744, toRemoteDirectory + reset_sh);

            RemoteProcessRunnerExec runner = rc.createRunnerExec(processdir + toRemoteDirectory);
            runner.restart();

//            rc.channelShell.setInputStream(System.in);
//            rc.channelShell.setOutputStream(System.out);
//            rc.channelShellConnect();

            Thread.sleep(100);
            rc.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testSeeResources() {
        try {
            Enumeration<URL> urls = null;
            urls = getClass().getClassLoader().getResources(".");

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                System.out.println(url);
            }

            URL url = getClass().getClassLoader().getResource(reset_sh);
            File file = new File(url.toURI());
            String fullPath = file.getAbsolutePath();

            System.out.println("Location of " + reset_sh + " is: " + fullPath);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
