package org.sanchouss.idea.plugins.instantpatch.sshidentity;

import com.jcraft.jsch.*;

import java.io.*;
import java.net.URL;

/**
 * Created by Alexander Perepelkin
 */
public class IdentityResource implements Identity {
    final private JSch jsch;
    final private KeyPair kpair;
    final private String keyClasspath;

    public static IdentityResource newInstance(String keyClasspath, JSch jsch) throws JSchException, IOException {

        final URL ppk = IdentityResource.class.getClassLoader().getResource(keyClasspath);
        final Object ctn = ppk.getContent();
        final InputStream is = (InputStream) ctn;
        final BufferedInputStream bis = new BufferedInputStream(is);
        final ByteArrayOutputStream ba = new ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        int read = 0;
        while ((read = bis.read(buf)) > 0) {
            ba.write(buf, 0, read);
        }
        byte[] prvfile = ba.toByteArray();

        KeyPair kpair = KeyPair.load(jsch, prvfile, null);
        return new IdentityResource(jsch, keyClasspath, kpair);
    }

        private IdentityResource(JSch jsch, String keyClasspath, KeyPair kpair) throws JSchException {
            this.jsch = jsch;
            this.keyClasspath = keyClasspath;
            this.kpair = kpair;
        }

        public boolean setPassphrase(byte[] passphrase) throws JSchException {
            return this.kpair.decrypt(passphrase);
        }

        public byte[] getPublicKeyBlob() {
            return this.kpair.getPublicKeyBlob();
        }

        public byte[] getSignature(byte[] data) {
            return this.kpair.getSignature(data);
        }

        /** @deprecated */
        public boolean decrypt() {
            throw new RuntimeException("not implemented");
        }

        // hard-coded to value from KeyPairRSA
        private static final byte[] sshrsa;
        static {
            byte[] name;
            try {
                name = "ssh-rsa".getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                name  = new byte[0];
            }
            sshrsa = name;
        }

        public String getAlgName() {
            return new String(sshrsa);
        }

        public String getName() {
            return this.keyClasspath;
        }

        public boolean isEncrypted() {
            return this.kpair.isEncrypted();
        }

        public void clear() {
            this.kpair.dispose();
//            this.kpair = null;
        }

        public KeyPair getKeyPair() {
            return this.kpair;
        }

}
