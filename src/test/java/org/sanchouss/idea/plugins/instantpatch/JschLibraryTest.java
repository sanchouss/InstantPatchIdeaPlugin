package org.sanchouss.idea.plugins.instantpatch;

import com.intellij.openapi.util.text.StringUtil;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.sanchouss.idea.plugins.instantpatch.JschCredentials.*;

/**
 * Created by Alexander Perepelkin
 */
public class JschLibraryTest {

    @Test
    public void testJschLibConnection() throws InterruptedException, SftpException, JSchException, IOException, URISyntaxException {

        System.out.println("jsch client to be tested...");
        JSch jsch = new JSch();
        jsch.addIdentity(privateKey, StringUtil.isEmpty(passphrase) ? null : passphrase);

//        byte[] pvkKey = Files.readAllBytes(Paths.get(privateKey));
//        jsch.addIdentity(user, pvkKey, new byte[0],
//            StringUtil.isEmpty(passphrase) ? null : passphrase.getBytes());

        Session session = jsch.getSession(user, host, port);
        System.out.println("jsch session created.");
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications",
                "publickey,keyboard-interactive,password");
        session.setConfig(config);
        session.connect();
        System.out.println("jsch session connected.");
        Thread.sleep(500);
        session.disconnect();
        System.out.println("jsch session disconnected.");
    }

}
