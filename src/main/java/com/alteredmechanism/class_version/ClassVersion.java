package com.alteredmechanism.class_version;

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
public class ClassVersion {

	public static final int MAJOR_PROD_DIFF = 44;

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
		for (int mv = 5; mv < 40; mv++) {
			float pv = mv - MAJOR_PROD_DIFF;
			ClassVersion cv = new ClassVersion(mv, pv);
			majorVersionMap.put(mv, cv);
			productVersionMap.put(pv, cv);
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
	public float getProductVersion() {
		return productVersionNumbers[0];
	}

	@SuppressWarnings("unused")
	public String getProductName() {
		return "Java " + productVersionNumbers[0];
	}

	@SuppressWarnings("unused")
	public float[] getProductVersions() {
		return productVersionNumbers;
	}
	
	public static ClassVersion lookupByMajorVersion(int majorVersion) {
		ClassVersion cv;
		cv = majorVersionMap.get(majorVersion);
		if (cv == null) {
			cv = new ClassVersion(majorVersion, majorVersion - MAJOR_PROD_DIFF);
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
	public String toString() {
		return String.format("%s (major version: %d)", getProductName(), majorVersion);
	}

	public int compareTo(ClassVersion maxVersion) {
		return this.majorVersion - maxVersion.majorVersion;
	}
}
