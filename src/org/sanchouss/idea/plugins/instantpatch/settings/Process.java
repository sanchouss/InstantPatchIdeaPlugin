package org.sanchouss.idea.plugins.instantpatch.settings;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;

/**
 * Created by Alexander Perepelkin
 */
public class Process {
    /**
     * Name of the command file for the remote target process
     */
    private String processName;
    /**
     * Remote directory containing target process command file
     */
    private String processDirectory;
    /**
     * Remote directory which is in the CLASSPATH of the remote target process
     */
    private String classFilesDirectory;
    /**
     * Some specific remote directories which are targets for the copied files
     */
    private ArrayList<String> remoteDirectories;
    /**
     * Remote directory which is writable via SFTP
     */
    private String temporaryDirectory;

    public String getProcessName() {
        return processName;
    }

    @XmlElement
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessDirectory() {
        return processDirectory;
    }

    @XmlElement
    public void setProcessDirectory(String processDirectory) {
        this.processDirectory = processDirectory;
    }

    public String getClassFilesDirectory() {
        return classFilesDirectory;
    }

    @XmlElement
    public void setClassFilesDirectory(String classFilesDirectory) {
        this.classFilesDirectory = classFilesDirectory;
    }

    public ArrayList<String> getRemoteDirectories() {
        return remoteDirectories;
    }

    @XmlElement
    public void setRemoteDirectories(ArrayList<String> remoteDirectories) {
        this.remoteDirectories = remoteDirectories;
    }


    @Override
    public String toString() {
        return "Process{" +
            "processName='" + processName + '\'' +
            ", processDirectory='" + processDirectory + '\'' +
            ", classFilesDirectory='" + classFilesDirectory + '\'' +
            ", remoteDirectories='" + remoteDirectories + '\'' +
            '}';
    }

    public String getTemporaryDirectory() {
        return temporaryDirectory;
    }

    public void setTemporaryDirectory(String temporaryDirectory) {
        this.temporaryDirectory = temporaryDirectory;
    }
}

