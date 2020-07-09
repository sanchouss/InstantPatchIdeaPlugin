package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.util.text.StringUtil;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Ignore;
import org.junit.Test;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientImpl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteClientImplTest {

    private String privateKey = "/usr/tmp/ppk";
    private String passphrase = "";
    private String host = "remhost";
    private String user = "sanchouss";
    private int port = 22;

    @Test
    @Ignore
    public void testJschConnection() throws InterruptedException, SftpException, JSchException, IOException, URISyntaxException {
        byte[] pvkKey = Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("ssh-private-key.ppk").toURI()));

        JSch jsch = new JSch();
        String passphrase = "";
        jsch.addIdentity("testIdentity", pvkKey, new byte[0],
            StringUtil.isEmpty(passphrase) ?
            null : passphrase.getBytes());

        Session session = jsch.getSession(user, host, port);
        System.out.println("session created.");

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications",
            "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.connect();
        Thread.sleep(500);
        session.disconnect();
    }

    @Test
    @Ignore
    public void testConnection() throws InterruptedException, SftpException, JSchException, IOException {
        RemoteClientImpl impl = new RemoteClientImpl(host, user, 22,
            new RemoteAuth(privateKey, passphrase));
        impl.sendShellCommand("pwd");
        Thread.sleep(500);
        impl.disconnect();
    }
}
