package org.sanchouss.idea.plugins.instantpatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sanchouss.idea.plugins.instantpatch.remote.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteClientImplMethTest {
    String user = "alex-pekin";
    String host = "braavos";
    String homedir = "/home/alex-pekin/";
    String processdir = homedir+"tmp/process1/";
    int port = 22;
    private RemoteAuth remoteAuth;

    @Before
    public void before() {
        remoteAuth = new RemoteAuth(IdentityResourceTest.SSH_PRIVATE_KEY, IdentityResourceTest.SSH_PASS_PHR);
    }

    @Test
    public void testHaveSshKeyInResources() {
        URL url = getClass().getClassLoader().getResource(IdentityResourceTest.SSH_PRIVATE_KEY);
        System.out.println(url);
        Assert.assertNotNull(url);
    }

    @Test
    public void testSeeResources() {
        Enumeration<URL> urls = null;
        try {
            urls = getClass().getClassLoader().getResources(".");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                System.out.println(url);
            }
    }

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
        }
    }

    @Test
    public void testPatcher() {
        try {
            String toRemoteDirectory = "com/company/";

            RemoteClient rc = new RemoteClientImpl(host, user, port, remoteAuth);

            RemoteProcessSftpPatcher patcher = rc.createPatcher(processdir);
            patcher.mkdir();
            patcher.cd();
            patcher.mkdir(toRemoteDirectory);
            patcher.mkdir(toRemoteDirectory);


            List<String> files = new ArrayList<>();
            files.add("test.resource");
            String fromLocalDirectory = "C:\\Users\\alex-pekin\\IdeaProjects\\InstantPatchIdeaPlugin\\resources";
            patcher.uploadFiles(fromLocalDirectory, toRemoteDirectory, files);

            Thread.sleep(100);
            rc.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPatcherAndExec() {
        try {
            String toRemoteDirectory = "com/company/";

            RemoteClient rc = new RemoteClientImpl(host, user, port, remoteAuth);

            RemoteProcessSftpPatcher patcher = rc.createPatcher(processdir);
            patcher.mkdir();
            patcher.cd();
            patcher.mkdir(toRemoteDirectory);

            List<String> files = new ArrayList<>();
            files.add("reset.sh");
            String fromLocalDirectory = "C:\\Users\\alex-pekin\\IdeaProjects\\InstantPatchIdeaPlugin\\resources";
            patcher.uploadFiles(fromLocalDirectory, toRemoteDirectory, files);
            patcher.chmod(0744, toRemoteDirectory + "reset.sh");

            RemoteProcessRunnerExec runner = rc.createRunnerExec(processdir + toRemoteDirectory);
            runner.restart();

//            rc.channelShell.setInputStream(System.in);
//            rc.channelShell.setOutputStream(System.out);
//            rc.channelShellConnect();

            Thread.sleep(100);
            rc.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
