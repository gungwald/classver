package com.alteredmechanism.classversionextractor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassVersionExtractor {

	private final FileFilter javaFilter = new JavaFileFilter();
	private boolean displayMaxVersion = false;
	private boolean displaySummary = false;
	private ClassVersion maxVersion = null;
	private Map<ClassVersion, Integer> versionCounts = new TreeMap<ClassVersion, Integer>();

	/**
	 * Executions start here
	 * 
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

	public void usage() {
		System.out.println("usage: classver [options] [file]...");
		System.out.println();
		System.out.println("Display the major and minor class versions of compiled .class files");
		System.out.println();
		System.out.println("	--help, -h, -?		display this usage information");
		System.out.println("	-m, --max-version	display the maximum version found at the end");
		System.out.println("	-s, --summary		display a summary of versions found in each file");
		System.out.println();
	}

	public void printVersions(String[] args) throws IOException {
		if (args.length == 0) {
			printVersion(new File(System.getProperty("user.dir")));
		} else {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("--help") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-?")) {
					usage();
				} else if (arg.equalsIgnoreCase("-m") || arg.equalsIgnoreCase("--max-version")) {
					displayMaxVersion = true;
				} else if (arg.equalsIgnoreCase("-s") || arg.equalsIgnoreCase("--summary")) {
					displaySummary = true;
				} else {
					printVersion(new File(arg));
				}
			}
			if (displayMaxVersion) {
				printf("Max version found: %s%n", maxVersion.toString());
			} else if (displaySummary) {
				for (ClassVersion ver : versionCounts.keySet()) {
					printf("Number of %s classes: %d%n", ver.toString(), versionCounts.get(ver));
				}
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
					} else if (displaySummary) {
						updateCount(versionCounts, v);
					} else {
						printf("%s: %s: %s%n", jar.getName(), entry.getName(), v.toString());
					}
				}
			}
		}
	}

	void updateCount(Map<ClassVersion, Integer> versionCount, ClassVersion version) {
		Integer count = versionCounts.get(version);
		if (count == null) {
			versionCounts.put(version, 1);
		} else {
			count++;
			versionCounts.put(version, count);
		}
	}

	public void printVersion(File f) throws IOException {
		if (f.exists()) {
			if (f.isDirectory()) {
				for (File entry : f.listFiles(javaFilter)) {
					printVersion(entry);
				}
			} else if (f.getName().endsWith(".class")) {
				ClassVersion ver = readVersion(f);
				if (displayMaxVersion) {
					if (maxVersion == null || ver.compareTo(maxVersion) > 0) {
						maxVersion = ver;
					}
				} else if (displaySummary) {
					updateCount(versionCounts, ver);
				} else {
					printf("%s: %s%n", f.getName(), ver.toString());
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
