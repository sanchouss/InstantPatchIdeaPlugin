package org.sanchouss.idea.plugins.instantpatch.remote;

import com.jcraft.jsch.SftpException;

/**
 *
 * Created by Alexander Perepelkin
 */
@FunctionalInterface
public interface SftpCommand<T> {
    void accept(T t) throws SftpException;
}
