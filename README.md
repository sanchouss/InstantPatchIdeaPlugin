# InstantPatchIdeaPlugin
Instant Patch Remote Java process by copying freshly compiled .class files and resource files directly to remote host via secure
channels.

Description
-------------------------------------------------------------------------------

### Main goal
This plugin allows you to **quickly upload compiled `.class` files** to a remote server directly from **IntelliJ IDEA**, and then **restart the remote Java process** — all without leaving the IDE.

### How It Works
* **Select Java files** `(.java)` that you’ve modified and compiled in the IntelliJ project tree.
* Choose the option to **copy them to the remote server**.
* The plugin automatically finds the corresponding `.class` files in IntelliJ’s **output directory** and uploads them. 
* The **remote destination path** for .class files is configured via the plugin’s XML settings. 
* You must have **write access** to the target directory on remote server.
* You can also upload **non-Java (resource) files**, as long as they're selected.

After remote patch directory is filled with new .class files, you can restart remote process from the plugin's menu.
You may want to modify the process startup shell script as well. The patch catalog with fresh .class files is better to be the very first entry
in CLASSPATH variable passed to JVM. If that shell file resides in the system catalog, it still can be overwritten:
first copied into remote temporary directory you have write access into, and then sudo-copied into the target system directory.

You will need to specify path to your private key file for connecting to the remote host securely. Passphrase is asked
separately and is not stored anywhere.

---

### Restarting the Remote Java Process

Once the updated `.class` files are uploaded:

- You can **restart the remote Java process** from the plugin menu.
- Modify the remote **startup shell script** so that the patch directory is the **first entry in the `CLASSPATH`**.
- If the shell script for remote process is located in a **system directory**:
    - Copy it first to a **temporary directory** where you have write access.
    - Then use `sudo` to move it into the system location, replacing the original.

---

### Remote Access & Security

- Configure the **path to your private SSH key** for secure access to the remote server.
- If your key has a **passphrase**, you’ll be prompted to enter it when connecting.
- **Note:** The passphrase is **never stored** by the plugin.


Installation
-------------------------------------------------------------------------------

This plugin is published on the
[JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/9373-instant-patch-remote-java-process)

Usage
-------------------------------------------------------------------------------

After plugin is installed, open Project View:

      View → Tool Windows → Project

Then right click on any file in Project view and choose

      Copy/Restart Remote... → Settings → Config file
      Copy/Restart Remote... → Settings → Private key file

to set up plugin configuration file and path to your private key file.

If you changed config file (added entries, etc.), then choose

      Copy/Restart Remote... → Reload plugin config

to apply new configuration.

### Sample plugin configuration file
Save on disk and choose it from plugin menu.
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration>
  <hostsList>
    <host>
      <hostname>host.a</hostname>
      <username>instantpatch</username>
      <processesList>
        <process>
          <classFilesDirectory>/home/instantpatch/classes/</classFilesDirectory>
          <processDirectory>/home/instantpatch/myservice/</processDirectory>
          <processName>a.myservice</processName>
          <processStartCommand>systemctl --user start myservice.service</processStartCommand>
          <processStopCommand>systemctl --user stop myservice.service</processStopCommand>
          <remoteDirectories>/usr/bin/</remoteDirectories>
          <remoteDirectories>/var/lib/</remoteDirectories>
          <remoteDirectories>/home/instantpatch/resources/</remoteDirectories>
          <temporaryDirectory>/home/instantpatch/tmp_ftp/</temporaryDirectory>
        </process>
        <process>
          <classFilesDirectory>/home/instantpatch/classes/</classFilesDirectory>
          <processDirectory>/usr/bin/</processDirectory>
          <processName>a.server-process</processName>
          <processStartCommand>server-process start</processStartCommand>
          <processStopCommand>server-process stop</processStopCommand>
          <remoteDirectories>/usr/bin/</remoteDirectories>
          <remoteDirectories>/var/lib/</remoteDirectories>
          <remoteDirectories>/home/instantpatch/resources/</remoteDirectories>
          <temporaryDirectory>/home/instantpatch/tmp_ftp/</temporaryDirectory>
        </process>
      </processesList>
    </host>
    <host>
      <hostname>host.b</hostname>
      <username>instantpatch</username>
      <processesList>
        <process>
          <classFilesDirectory>/home/instantpatch/classes/</classFilesDirectory>
          <processDirectory>/home/instantpatch/myservice/</processDirectory>
          <processName>b.myservice</processName>
          <processStartCommand>systemctl --user start myservice.service</processStartCommand>
          <processStopCommand>systemctl --user stop myservice.service</processStopCommand>
          <remoteDirectories>/usr/bin/</remoteDirectories>
          <remoteDirectories>/var/lib/</remoteDirectories>
          <remoteDirectories>/home/instantpatch/resources/</remoteDirectories>
          <temporaryDirectory>/home/instantpatch/tmp_ftp/</temporaryDirectory>
        </process>
        <process>
          <classFilesDirectory>/home/instantpatch/classes/</classFilesDirectory>
          <processDirectory>/usr/bin/</processDirectory>
          <processName>b.server-process</processName>
          <processStartCommand>server-process start</processStartCommand>
          <processStopCommand>server-process stop</processStopCommand>
          <remoteDirectories>/usr/bin/</remoteDirectories>
          <remoteDirectories>/var/lib/</remoteDirectories>
          <remoteDirectories>/home/instantpatch/resources/</remoteDirectories>
          <temporaryDirectory>/home/instantpatch/tmp_ftp/</temporaryDirectory>
        </process>
      </processesList>
    </host>
  </hostsList>
</configuration>

```

FAQ
-------------------------------------------------------------------------------
1. Could not connect to server using this plugin?

If you are connecting via IPv6 network, try to add following VM Options into Idea:
(Help → Edit Custom VM Options)

      -Djava.net.preferIPv6Addresses=true
      -Djava.net.preferIPv4Stack=false

Change log
-------------------------------------------------------------------------------

[CHANGELOG](CHANGELOG.md)


### Credits
Alexander Perepelkin

License
-------------------------------------------------------------------------------

[LICENSE](LICENSE)