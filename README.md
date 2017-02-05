# InstantPatchIdeaPlugin
Instant Patch Remote Java process by copying fresh .class files and resource files directly to remote host via secure
channels.

Description
-------------------------------------------------------------------------------

Select Java classes and upload them onto remote server right from IntelliJ Idea. Restart java process from IDE afterwards.

Select *.java files which you just changed and compiled; appropriate .class files will be located
in the output directory of IntelliJ Idea IDE and copied to the remote server. Target directory for the .class files is
specified in the plugin's configuration file (you must have write permissions to that directory on remote server).
Non-Java (resource) files may be copied as well if selected.
After remote directory is patched with new .class files, you can restart remote process from the plugin's menu.
You may want to patch the process startup shell script as well in order to include the patch catalog with fresh .class files as the first entry
in CLASSPATH variable passed to JVM. If that shell file resides in the system catalog, it still can be overwritten:
first copied into remote temporary directory you have write access into, and then sudo-copied into the target system directory.
You will need to specify path to your private key file for connecting to the remote host securely. Passphrase is asked
separately and is not stored anywhere.

Installation
-------------------------------------------------------------------------------

This plugin is published on the
[JetBrains Plugin Repository](https://plugins.jetbrains.com/idea/plugin/9373)

Usage
-------------------------------------------------------------------------------

After plugin is installed, open Project View:

      View → Tool Windows → Project

Then right click on any file in Project view and choose

      Copy/Restart Remote... → Settings → Config file
      Copy/Restart Remote... → Settings → Private key file

to set up plugin configuration file and path to your private key file.

      Copy/Restart Remote... → Reload plugin config

will apply new configuration.

Sample plugin configuration file:

      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <configuration>
          <hostsList>
              <host>
                  <hostname>hosta</hostname>
                  <username>sanchouss</username>
                  <processesList>
                      <process>
                          <classFilesDirectory>/home/sanchouss/patches/</classFilesDirectory>
                          <processDirectory>/usr/bin/</processDirectory>
                          <processName>server-process1</processName>
                          <remoteDirectories>/usr/bin/</remoteDirectories>
                          <remoteDirectories>/var/lib/</remoteDirectories>
                          <temporaryDirectory>/home/sanchouss/tmp/</temporaryDirectory>
                      </process>
                      <process>
                          <classFilesDirectory>/home/sanchouss/patches/</classFilesDirectory>
                          <processDirectory>/usr/bin/</processDirectory>
                          <processName>server-process2</processName>
                          <remoteDirectories>/usr/bin/</remoteDirectories>
                          <remoteDirectories>/var/lib/</remoteDirectories>
                          <temporaryDirectory>/home/sanchouss/tmp/</temporaryDirectory>
                      </process>
                  </processesList>
              </host>
              <host>
                  <hostname>hostb</hostname>
                  <username>sanchouss</username>
                  <processesList>
                      <process>
                          <classFilesDirectory>/home/sanchouss/patches/</classFilesDirectory>
                          <processDirectory>/usr/bin/</processDirectory>
                          <processName>server-process1</processName>
                          <remoteDirectories>/usr/bin/</remoteDirectories>
                          <remoteDirectories>/var/lib/</remoteDirectories>
                          <temporaryDirectory>/home/sanchouss/tmp/</temporaryDirectory>
                      </process>
                      <process>
                          <classFilesDirectory>/home/sanchouss/patches/</classFilesDirectory>
                          <processDirectory>/usr/bin/</processDirectory>
                          <processName>server-process2</processName>
                          <remoteDirectories>/usr/bin/</remoteDirectories>
                          <remoteDirectories>/var/lib/</remoteDirectories>
                          <temporaryDirectory>/home/sanchouss/tmp/</temporaryDirectory>
                      </process>
                  </processesList>
              </host>
          </hostsList>
      </configuration>


Change log
-------------------------------------------------------------------------------

[CHANGELOG](CHANGELOG.md)


### Credits
Alexander Perepelkin

License
-------------------------------------------------------------------------------

[LICENSE](LICENSE)