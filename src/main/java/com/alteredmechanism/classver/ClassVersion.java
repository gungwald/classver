package com.alteredmechanism.classver;

import java.util.HashMap;
import java.util.Map;

/**
 * Major Version Numbers:
 * <pre>
 * J2SE 8 = 52 (0x34 hex),
 * J2SE 7 = 51 (0x33 hex),
 * J2SE 6.0 = 50 (0x32 hex),
 * J2SE 5.0 = 49 (0x31 hex),
 * JDK 1.4 = 48 (0x30 hex),
 * JDK 1.3 = 47 (0x2F hex),
 * JDK 1.2 = 46 (0x2E hex),
 * JDK 1.1 = 45 (0x2D hex).
 * </pre>
 * 
 * @author bill
 */
public class ClassVersion implements Comparable<ClassVersion> {

	public static final int MAJOR_VER_TO_PRODUCT_VER_DIFFERENCE = 44;

	private final int majorVersion;
	private final float[] productVersionNumbers;
	
	private static final Map<Integer,ClassVersion> majorVersionMap = new HashMap<Integer, ClassVersion>();
	private static final Map<Float,ClassVersion> productVersionMap = new HashMap<Float, ClassVersion>();
	
	static {
		int[] specialMajorVersions = new int[]{45,46,47,48};
		float[] specialProductVersions = new float[]{1.1f,1.2f,1.3f,1.4f};
		for (int i = 0; i < specialMajorVersions.length; i++) {
			ClassVersion cv = new ClassVersion(specialMajorVersions[i],specialProductVersions[i]);
			majorVersionMap.put(specialMajorVersions[i], cv);
			productVersionMap.put(specialProductVersions[i], cv);
		}
		/* Calculate product version from the fixed relationship with major versions */
		for (int majorVersion = 5; majorVersion < 40; majorVersion++) {
			float productVersion = majorVersion - MAJOR_VER_TO_PRODUCT_VER_DIFFERENCE;
			ClassVersion classVersion = new ClassVersion(majorVersion, productVersion);
			majorVersionMap.put(majorVersion, classVersion);
			productVersionMap.put(productVersion, classVersion);
		}
	}

	/**
	 * This class controls all instances
	 *
	 * @param majorVersion Java class byte-code version number
	 * @param productVersionNumbers Java product version numbers
	 */
	private ClassVersion(int majorVersion, float... productVersionNumbers) {
		this.majorVersion = majorVersion;
		this.productVersionNumbers = productVersionNumbers;
	}

	@SuppressWarnings("unused")
	public int getMajorVersion() {
		return majorVersion;
	}

	@SuppressWarnings("unused")
	public static boolean isInteger(float f) {
		return f - Math.floor(f) == 0;
	}

	@SuppressWarnings("unused")
	public String getFormattedProductVersion() {
		float version = getProductVersion();
		int versionFloor = (int) Math.floor(version);
		String fmtd;
		if (version - versionFloor == 0) {
			fmtd = String.valueOf(versionFloor);
		} else {
			fmtd = String.valueOf(version);
		}
		return fmtd;
	}

	public float getProductVersion() {
		return productVersionNumbers[0];
	}

	@SuppressWarnings("unused")
	public String getProductName() {
		return "Java " + getFormattedProductVersion();
	}

	@SuppressWarnings("unused")
	public float[] getProductVersions() {
		return productVersionNumbers;
	}
	
	public static ClassVersion lookupByMajorVersion(int majorVersion) {
		ClassVersion cv;
		cv = majorVersionMap.get(majorVersion);
		if (cv == null) {
			cv = new ClassVersion(majorVersion, majorVersion - MAJOR_VER_TO_PRODUCT_VER_DIFFERENCE);
			majorVersionMap.put(majorVersion, cv);
			productVersionMap.put(cv.productVersionNumbers[0], cv);
		}
		return cv;
	}

	@SuppressWarnings("unused")
	public static ClassVersion lookupByProductVersion(float productVersion) {
		return productVersionMap.get(productVersion);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ClassVersion)) return false;
		ClassVersion that = (ClassVersion) o;
		return majorVersion == that.majorVersion;
	}

	@Override
	public int hashCode() {
		return majorVersion;
	}

	@Override
	public String toString() {
		return String.format("%s (major version: %d)", getProductName(), majorVersion);
	}

	/**
	 * Implements method required by Comparable interface
	 * @param other the object to be compared.
	 * @return Normal compareTo stuff
	 */
	public int compareTo(ClassVersion other) {
		return this.majorVersion - other.majorVersion;
	}

}
