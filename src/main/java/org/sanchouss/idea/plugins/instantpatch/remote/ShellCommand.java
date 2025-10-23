package org.sanchouss.idea.plugins.instantpatch.remote;

/**
 *
 * Created by Alexander Perepelkin
 */
@FunctionalInterface
public interface ShellCommand<T> {
    void accept(T t);
}
