package org.python.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.file.ClassDefItem;

import dalvik.system.DexFile;

/**
 * That's the main Codes that fix the difference between the dalvik and startard
 * jvm
 * 
 * @author classfoo
 */
public class FixMe {
	//this is useful now because of the getDeclaringClass fix, 
	//when android fix this bug, it will not need this path anymore.
	//TODO check whether this bug still exists
	public static String apppath = null;
	public static final String tmpdirpath = "/data/jythonroid/";
	public static boolean isinitialized = false;

	public static final String ps1= ">>>";
	public static final String ps2 = "+++";

	public static boolean initialize() {
		apppath=System.getProperty("java.class.path");  
		// create the tmp dir
		File tdp = new File(tmpdirpath);
		if (!tdp.exists()) {
			tdp.mkdir();
		} else {
			if (!tdp.isDirectory()) {
				throw new RuntimeException("make sure the "+tmpdirpath+"is a fold");
			}
		}
		isinitialized = true;
		return true;
	}

	/**
	 * get a class by apk file name and class name need recurse as the Class
	 * instance can not get when the super class's not infered; Exmaple:<code>
	 * Class<c>=getClassByName("/tmp/fuck.apk","org/freehust/pystring");
	 * </code>
	 * 
	 * @param String
	 *            filename,String classname
	 * @return Class
	 */
	private static Class<?> getClassByName(String filename, String classname) {
		try {
			//System.out.println("DEBUG:the file name in getClassByName"
			//		+ filename);
			DexFile f = new DexFile(new File(filename));
			Class<?> s=f.loadClass(classname, ClassLoader.getSystemClassLoader());
			
			return s;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	private static String fixPath(String path) {
		if (File.separatorChar == '\\')
			path = path.replace('\\', '/');
		int index = path.lastIndexOf("/./");
		if (index != -1)
			return path.substring(index + 3);
		if (path.startsWith("./"))
			return path.substring(2);
		else
			return path;
	}

	/**
	 * return the dalvik Class object from the java bytecode stream
	 * 
	 * @param String
	 *            name
	 * @param byte[] data
	 * @return Class<?> cls
	 * @throws IOException
	 */
	public static Class<?> getDexClass(String name, byte[] data)
			throws IOException {
		// translate the java bytecode to dalvik bytecode
		com.android.dx.dex.file.DexFile outputDex = new com.android.dx.dex.file.DexFile();
		CfOptions cf = new CfOptions();
		ClassDefItem clazz = CfTranslator.translate(fixPath(name.replace('.',
				'/')
				+ ".class"), data, cf);
		outputDex.add(clazz);
		// create the specific fold in tmpdir
		File tmpdir = new File(tmpdirpath + name);
		if (!tmpdir.exists()) {
			tmpdir.mkdir();
		} else {
			if (!tmpdir.isDirectory()) {
				throw new RuntimeException("the tmp dir conflicts");
			}
		}
		// create the zip file name.apk
		File apk = new File(tmpdirpath + name + "/" + name + ".apk");
		if (!apk.exists()) {
			apk.createNewFile();
		}
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(apk));
		ZipEntry classeszip = new ZipEntry("classes.dex");
		zos.putNextEntry(classeszip);
		outputDex.writeTo(zos, null, false);
		zos.closeEntry();
		zos.close();
		// load the name.apk file
		Class<?> c = getClassByName(tmpdirpath + name + "/" + name + ".apk",
				name.replace('.', '/'));
		return c;
	}
	static class JRClassLoader extends ClassLoader{
		public JRClassLoader() {
			super();
		}
	}
}
