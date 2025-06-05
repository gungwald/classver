package com.alteredmechanism.classver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

// This is a user-facing app, not a service that needs to write to a log.
// So, it should call printStackTrace.
@SuppressWarnings("CallToPrintStackTrace")
public class ClassVersionExtractor {

	private final FileFilter javaFilter = new JavaFileFilter();
	private boolean displayManifest = false;
	private boolean displayMaxVersion = false;
	private boolean displaySummary = false;
	private boolean displayJarEntriesTooSmall = false;
	private ClassVersion maxVersion = null;
	private final Map<ClassVersion, Integer> versionCounts = new TreeMap<ClassVersion, Integer>();

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
	public void println() {
		System.out.println();
	}
	public void println(String line) {
		System.out.println(line);
	}

	public void usage() {
		println();
		println("NAME");
		println("   classver.bat - Windows batch file wrapper");
		println("   classver     - Linux/UN*X shell script wrapper");
		println("   classver.jar - Runnable Java ARchive program");
		println();
		println("DESCRIPTION");
		println("   Displays the major class versions of compiled .class files");
		println("   recursively through subdirectories and JAR files.");
		println();
		println("INVOCATION");
		println("   To run from a batch file or shell script:");
		println("      classver [options] [file]...");
		println();
		println("   To run directly from Java:");
		println("      java -jar classver.jar [options] [file]...");
		println();
		println("   options: ");
		println("      -h, -?, --help    Displays this usage information");
		println("      -f, --manifest    Displays the manifest of a single jar");
		println("      -m, --max-version Displays the maximum version found at the end");
		println("      -s, --summary     Displays a summary of versions found in each file");
		println("      -t, --too-small   Displays jar entries that are too small");
		println();
	}

	public void printVersions(String[] args) throws IOException {
		if (args.length == 0) {
			printVersion(new File(System.getProperty("user.dir")));
		} else {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("--help") || arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("-?")) {
					usage();
				} else if (arg.equalsIgnoreCase("-f") || arg.equalsIgnoreCase("--manifest")) {
					displayManifest = true;
				} else if (arg.equalsIgnoreCase("-m") || arg.equalsIgnoreCase("--max-version")) {
					displayMaxVersion = true;
				} else if (arg.equalsIgnoreCase("-s") || arg.equalsIgnoreCase("--summary")) {
					displaySummary = true;
				} else if (arg.equalsIgnoreCase("-t") || arg.equalsIgnoreCase("--too-small")) {
					displayJarEntriesTooSmall = true;
				} else {
					printVersion(new File(arg));
				}
			}
			if (displayMaxVersion) {
				printf("Max version found: %s%n", maxVersion.toString());
			} else if (displaySummary) {
				for (ClassVersion ver : versionCounts.keySet()) {
					printf("Number of %s classes (having major version %d): %d%n", ver.getProductName(), ver.getMajorVersion(), versionCounts.get(ver));
				}
			}
		}
	}

	@SuppressWarnings("unused")
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
		ClassVersion v;
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

	public void printApplicationVersion(JarFile jar) throws IOException {
		Manifest manifest = jar.getManifest();
		if (manifest == null) {
			printf("%s: No manifest in which to find application version%n", jar.getName());
		} else {
			Attributes mainAttrs = manifest.getMainAttributes();
			Object specVersion = mainAttrs.get("Specification-Version");
			if (specVersion != null) {
				printf("%s: Specification-Version=%s%n", jar.getName(), specVersion);
			}
			Object implVersion = mainAttrs.get("Implementation-Version");
			if (implVersion != null) {
				printf("%s: Implementation-Version=%s%n", jar.getName(), implVersion);
			}
		}
	}

	public void printManifest(JarFile jar) throws IOException {
		Manifest manifest = jar.getManifest();
		if (manifest == null) {
			printf("%s: No manifest%n", jar.getName());
		} else {
			Attributes mainAttrs = manifest.getMainAttributes();
			for (Map.Entry<Object,Object> attr : mainAttrs.entrySet()) {
				printf("%s: Main Attribute: %s=%s%n", jar.getName(), attr.getKey(), attr.getValue());
			}
			for (Map.Entry<String,Attributes> section : manifest.getEntries().entrySet()) {
				printf("%s: Manifest Section: %s%n", jar.getName(), section.getKey());
				for (Map.Entry<Object,Object> attr : section.getValue().entrySet()) {
					printf("%s: %s=%s%n", jar.getName(), attr.getKey(), attr.getValue());
				}
			}
		}
	}

	public void printVersions(JarFile jar) throws IOException {
		printApplicationVersion(jar);
		Enumeration<? extends JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
				if (entry.getName().endsWith(".jar")) {
					printf("%s: %s: Can't process jar-in-jar yet%n", jar.getName(), entry.getName());
				}
                if (entry.getSize() < 10 && displayJarEntriesTooSmall) {
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
                            updateCount(v);
                        } else {
                            printf("%s: %s: %s%n", jar.getName(), entry.getName(), v.toString());
                        }
                    }
                }
            }
        }
	}

	void updateCount(ClassVersion version) {
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
				File[] files = f.listFiles(javaFilter);
				if (files != null) {
					for (File entry : files) {
						printVersion(entry);
					}
				}
			} else if (f.getName().endsWith(".class")) {
				ClassVersion ver = readVersion(f);
				if (displayMaxVersion) {
					if (maxVersion == null || ver.compareTo(maxVersion) > 0) {
						maxVersion = ver;
					}
				} else if (displaySummary) {
					updateCount(ver);
				} else {
					printf("%s: %s%n", f.getName(), ver.toString());
				}
			} else if (f.getName().endsWith(".jar")) {
				JarFile jar = new JarFile(f);
				if (displayManifest) {
					printManifest(jar);
				} else {
					printVersions(jar);
				}
			} else {
				printf("%s: file type has no Java version%n", f.getCanonicalPath());
			}
		} else {
			printf("%s: file not found%n", f.getCanonicalFile());
		}
	}
}
