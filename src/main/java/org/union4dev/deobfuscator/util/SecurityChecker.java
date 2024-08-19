package org.union4dev.deobfuscator.util;

import java.io.FileDescriptor;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.security.Permission;

public class SecurityChecker extends SecurityManager {
    private final boolean blockPropertiesAccess;
    private final boolean blockPropertyAccess;
    private final boolean blockConnection;
    private final boolean blockPermission;
    private final boolean blockMulticast;
    private final boolean blockFileWrite;
    private final boolean blockFileRead;
    private final boolean blockAccept;
    private final boolean blockFileDel;
    private final boolean blockListen;
    private final boolean blockExec;
    private final boolean blockLink;
    private final boolean blockExit;

    public SecurityChecker(boolean blockPropertiesAccess, boolean blockPropertyAccess, boolean blockConnection, boolean blockPermission, boolean blockMulticast, boolean blockFileWrite, boolean blockFileRead, boolean blockAccept, boolean blockFileDel, boolean blockListen, boolean blockExec, boolean blockLink, boolean blockExit) {
        this.blockPropertiesAccess = blockPropertiesAccess;
        this.blockPropertyAccess = blockPropertyAccess;
        this.blockConnection = blockConnection;
        this.blockPermission = blockPermission;
        this.blockMulticast = blockMulticast;
        this.blockFileWrite = blockFileWrite;
        this.blockFileRead = blockFileRead;
        this.blockAccept = blockAccept;
        this.blockFileDel = blockFileDel;
        this.blockListen = blockListen;
        this.blockExec = blockExec;
        this.blockLink = blockLink;
        this.blockExit = blockExit;
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (pkg.startsWith("org.union4dev.deobfuscator"))
            throw new SecurityException("Package access was blocked.");
    }

    @Override
    public void checkSecurityAccess(String target) {
        throw new SecurityException("Security access was blocked.");
    }

    @Override
    public void checkPropertiesAccess() {
        if (blockPropertiesAccess)
            throw new SecurityException("Access properties operation was blocked.");
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (blockPropertyAccess)
            throw new SecurityException("Access property \"" + key + "\" operation was blocked.");
    }

    @Override
    public void checkPermission(Permission perm) {
        if (perm instanceof ReflectPermission || perm instanceof RuntimePermission) return;
        if (blockPermission)
            throw new SecurityException("Permission \"" + perm.getName() + "/" + perm.getActions() + "\" was blocked.");
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }

    @Override
    public void checkAccept(String host, int port) {
        if (blockAccept)
            throw new SecurityException("Accept \"" + host + ":" + port + "\" was blocked.");
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (blockMulticast)
            throw new SecurityException("Multicast \"" + maddr.getCanonicalHostName() + "\" was blocked.");
    }

    @Override
    public void checkListen(int port) {
        if (blockListen)
            throw new SecurityException("Listen \"" + port + "\" was blocked.");
    }

    @Override
    public void checkLink(String lib) {
        if (blockLink)
            throw new SecurityException("Link with \"" + lib + "\" was blocked.");
    }

    @Override
    public void checkExit(int status) {
        if (blockExit)
            throw new SecurityException("Exit with status \"" + status + "\" was blocked.");
    }

    @Override
    public void checkExec(String cmd) {
        if (blockExec)
            throw new SecurityException("Execute command \"" + cmd + "\" was blocked.");
    }

    @Override
    public void checkDelete(String file) {
        if (blockFileDel)
            throw new SecurityException("Delete \"" + file + "\" was blocked.");
    }

    @Override
    public void checkWrite(String file) {
        if (blockFileWrite)
            throw new SecurityException("Write in \"" + file + "\" was blocked.");
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (blockFileWrite)
            throw new SecurityException("Write operation was blocked.");
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (blockFileRead)
            throw new SecurityException("Read operation was blocked.");
    }

    @Override
    public void checkRead(String file) {
        if (blockFileRead)
            throw new SecurityException("Read in \"" + file + "\" was blocked.");
    }

    @Override
    public void checkRead(String file, Object context) {
        checkRead(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        if (blockConnection)
            throw new SecurityException("Connection to \"" + host + ":" + port + "\" was blocked.");
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        checkConnect(host, port);
    }
}
