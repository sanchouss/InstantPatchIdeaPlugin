package org.sanchouss.idea.plugins.instantpatch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import java.io.IOException;


/**
 * Created by Alexander Perepelkin
 */
public class RemoteAuth {
    private final String privateKeyFile;
    private final String passphrase;

    public RemoteAuth(String privateKeyFile, String passphrase) {
        this.privateKeyFile = privateKeyFile;
        this.passphrase = passphrase;
    }

    public JSch createJSch() throws IOException, JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyFile, passphrase);

        return jsch;
    }
}
