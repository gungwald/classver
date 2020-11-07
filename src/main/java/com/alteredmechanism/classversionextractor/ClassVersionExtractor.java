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

	private final FileFilter javaFilter = new JavaFileFilter();
	private boolean displayMaxVersion = false;
	private ClassVersion maxVersion = null;

	/**
	 * Executions start here
	 * @param args Command line arguments provided to program
	 */
	public static void main(String[] args) {
		ClassVersionExtractor cve = new ClassVersionExtractor();
		try {
			cve.printVersions(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void printf(String format, Object... args) {
		System.out.printf(format, args);
	}

	public void printVersions(String[] args) throws IOException {
		if (args.length == 0) {
			printVersion(new File(System.getProperty("user.dir")));
		} else {
			for (String arg : args) {
				if (arg.equals("-m")) {
					displayMaxVersion = true;
				}
				else {
					printVersion(new File(arg));
				}
			}
			if (displayMaxVersion) {
				printf("Max version found: %s%n", maxVersion.toString());
			}
		}
	}

	public void printVersions(File[] files) throws IOException {
		for (File file : files)
			printVersion(file);
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
				// System.out.println(f.getName() + ": " + ver);
			}
		} finally {
			close(din);
		}
		return ver;
	}

	public ClassVersion readVersion(File f) throws IOException {
		ClassVersion v = null;
		InputStream in = new FileInputStream(f);
		try {
			v = readVersion(in);
		} finally {
			close(in);
		}
		return v;
	}

	public void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {
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
			} else if (entry.getSize() < 10) {
				printf("%s: %s: Entry to small%n", jar.getName(), entry.getName());
			} else if (entry.getName().endsWith(".class")) {
				InputStream in = jar.getInputStream(entry);
				ClassVersion v = readVersion(in);
				if (v == null) {
					printf("%s: %s: Can't read version%n", jar.getName(), entry.getName());
				} else {
					if (displayMaxVersion) {
						if (maxVersion == null || v.compareTo(maxVersion) > 0) {
							maxVersion = v;
						}
					} else {
						printf("%s: %s: %s%n", jar.getName(), entry.getName(), v.toString());
					}
				}
			}
		}
	}

	public void printVersion(File f) throws IOException {
		if (f.exists()) {
			if (f.isDirectory()) {
				for (File entry : f.listFiles(javaFilter)) {
					printVersion(entry);
				}
			} else if (f.getName().endsWith(".class")) {
				if (displayMaxVersion) {
					ClassVersion ver = readVersion(f);
					if (maxVersion == null || ver.compareTo(maxVersion) > 0) {
						maxVersion = ver;
					} else {
						printf("%s: %s%n", f.getName(), ver.toString());
					}
				}
			} else if (f.getName().endsWith(".jar")) {
				printVersions(new JarFile(f));
			} else {
				printf("%s: file type has no Java version%n", f.getCanonicalPath());
			}
		} else {
			printf("%s: file not found%n", f.getCanonicalFile());
		}
	}
}
