package com.alteredmechanism.classversionextractor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassVersionExtractor {

    private FileFilter javaFilter = new JavaFileFilter();

    /**
     * @param args
     */
    public static void main(String[] args) {
        ClassVersionExtractor cve = new ClassVersionExtractor();
        try {
            cve.printVersions(args);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printVersions(String[] fileNames) throws IOException {
        for (String name : fileNames) {
            printVersion(new File(name));
        }
    }

    public void printVersions(File[] files) throws IOException {
        for (File file : files) {
            printVersion(file);
        }
    }

    public ClassVersion readVersion(InputStream in) throws IOException {
        DataInputStream din = null;
        ClassVersion ver = null;
        try {
            din = new DataInputStream(in);
            int magic = din.readInt();
            if (magic == 0xcafebabe) {
                din.readUnsignedShort();
                int major = din.readUnsignedShort();
                ver = ClassVersion.lookupByMajorVersion(major);
                //System.out.println(f.getName() + ": " + ver);
            }
        }
        finally {
            close(din);
        }
        return ver;
    }

    public void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void printVersions(JarFile jar) throws IOException {
        Enumeration<? extends JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            else if (entry.getSize() < 10) {
                System.out.println(jar.getName() + ": " + entry.getName() + ": Entry is to small to read the version");
            }
            else if (entry.getName().endsWith(".class")) {
                InputStream in = jar.getInputStream(entry);
                ClassVersion v = readVersion(in);
                if (v == null) {
                    System.out.println(jar.getName() + ": " + entry.getName() + ": Unable to read version");
                }
                else {
                    System.out.println(jar.getName() + ": " + entry.getName() + ": " + v.toString());
                }
            }
        }
    }

    public void printVersion(File f) throws IOException {
        if (f.isDirectory()) {
            for (File entry : f.listFiles(javaFilter)) {
                printVersion(entry);
            }
        }
        else if (f.getName().endsWith(".class")) {
            InputStream in = new FileInputStream(f);
            try {
                ClassVersion v = readVersion(in);
                System.out.println(f.getName() + ": " + v.toString());
            }
            finally {
                close(in);
            }
        }
        else if (f.getName().endsWith(".jar")) {
            printVersions(new JarFile(f));
        }
        else {
            // Fail
        }
    }
}
