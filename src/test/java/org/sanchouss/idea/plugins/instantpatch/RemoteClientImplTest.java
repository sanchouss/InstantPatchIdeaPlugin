package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.junit.Test;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteAuth;
import org.sanchouss.idea.plugins.instantpatch.remote.RemoteClientImpl;

import java.io.IOException;

import static org.sanchouss.idea.plugins.instantpatch.JschCredentials.*;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteClientImplTest {

    @Test
    public void testConnectionOnly() throws InterruptedException, SftpException, JSchException, IOException {
        System.out.println("RemoteClientImpl client to be tested...");
        RemoteClientImpl impl = new RemoteClientImpl(host, user, 22,
            new RemoteAuth(privateKey, passphrase));
        impl.sendShellCommand("pwd", 1000);
        Thread.sleep(500);
        impl.disconnect();
        System.out.println("RemoteClientImpl is tested ok");
    }
}
