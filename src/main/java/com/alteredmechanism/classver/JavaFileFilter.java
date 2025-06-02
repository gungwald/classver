package com.alteredmechanism.classver;

import java.io.File;
import java.io.FileFilter;

public class JavaFileFilter implements FileFilter {

	public boolean accept(File f) {
        return f.getName().endsWith(".jar") || f.getName().endsWith(".class") || f.isDirectory();
	}

}
