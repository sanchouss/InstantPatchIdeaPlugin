package org.sanchouss.idea.plugins.instantpatch.remote;

/**
 * Created by Alexander Perepelkin
 */
public class RemoteAuth {
    public final String privateKeyFile;
    public final String passphrase;

    public RemoteAuth(String privateKeyFile, String passphrase) {
        this.privateKeyFile = privateKeyFile;
        this.passphrase = passphrase;
    }
}
