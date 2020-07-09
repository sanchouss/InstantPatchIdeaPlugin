package org.sanchouss.idea.plugins.instantpatch;

import org.sanchouss.idea.plugins.instantpatch.sshidentity.IdentityResource;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Alexander Perepelkin
 */
public class IdentityResourceTest {
    public static final String SSH_PRIVATE_KEY = "ssh-private-key.ppk";
    public static final String SSH_PASS_PHR = "gfccahtqp";

    @Test
    public void testSession() throws IOException, JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(IdentityResource.newInstance(SSH_PRIVATE_KEY, jsch), SSH_PASS_PHR.getBytes());

        String user="";
        String host="";
        int port=22;
        Session session = jsch.getSession(user, host, port);
        System.out.println("session created.");
    }
}
