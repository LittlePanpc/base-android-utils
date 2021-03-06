package me.pc.mobile.helper.v14.files;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Common File IO Utils */
public class FileUtils {

	private FileUtils() {}

	private static String TAG = FileUtils.class.getSimpleName();
	private static boolean WARN = false;

	// =========================================================================
	// Android IO methods wrappers
	// =========================================================================
	/**
	 * Given a string which corresponds to a directory path relative to the root
	 * of *internal* storage creates this directory path if it does not exists.
	 * Returns this path or throws IOException on failure
	 *
	 * @param directoryName
	 *            the path to the desired directory *relative* to the root of
	 *            *internal* storage
	 * @return the File that corresponds to the path if it was created
	 *         successfully or the path already existed and is a directory
	 * @throws IOException
	 *             if the directory can't be created
	 */
	public static File createDirInternal(Context ctx,
			String directoryName) throws IOException {
		File directory = new File(ctx.getFilesDir(), directoryName);
		if (createDir(directory)) return directory;
		throw new IOException("Cannot create directory " + directoryName
			+ " in internal storage");
	}

	// =========================================================================
	// IO FileUtils
	// =========================================================================
	/**
	 * Returns true if the directory exists and is empty *OR* if it does not
	 * exist. Will also return true if the directory is security restricted.
	 * Will throw IllegalArgumentException if the directory is not a directory.
	 *
	 * @param directory
	 *            must be a directory
	 * @return true if the directory does not exist or is empty or not
	 *         accessible, false otherwise
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the directory is not a directory
	 */
	public static boolean isEmptyOrAbsent(File directory) {
		if (!directory.exists()) {
			return true;
		}
		return sizeOfDirectory(directory) == 0;
	}

	/**
	 * List the files in the directory. Wrapper around {@link File#listFiles()}.
	 *
	 * @param directory
	 *            directory to inspect, must not be <code>null</code>
	 * @return a List<File> of the files contained in the directory
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the directory does not exist or is not a directory
	 */
	public static List<File> listFiles(File directory) {
		File[] listFiles = directory.listFiles();
		if (listFiles == null) {
			String message = directory
				+ ((directory.exists()) ? " is not a directory"
						: " does not exist");
			throw new IllegalArgumentException(message);
		}
		return Arrays.asList(listFiles); // empty array => empty List
	}

	// =========================================================================
	// Package helpers
	// =========================================================================
	/**
	 * Given a File which corresponds to a _directory_ path creates this path if
	 * it does not exists.
	 *
	 * @param directory
	 *            the File instance whose path must be created
	 * @return true if the path already exists and is a directory or was created
	 *         successfully as a directory, false otherwise
	 */
	static boolean createDir(File directory) {
		return directory.isDirectory() || directory.mkdirs();
	}

	// =========================================================================
	// Helpers
	// =========================================================================
	/**
	 * Counts the size of a directory recursively (sum of the length of all
	 * files). Modifies Apache commons <a href=
	 * "http://commons.apache.org/proper/commons-io/javadocs/api-release/org/apache/commons/io/FileUtils.html#sizeOfDirectory%28java.io.File%29"
	 * >FileUtils#sizeOfDirectory(java.io.File)</a> TODO : <a
	 * href="http://stackoverflow.com/a/3169970/281545">circular paths</a>
	 *
	 * @param directory
	 *            directory to inspect, must not be <code>null</code>
	 * @return size of directory in bytes, 0 if directory is security restricted
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the directory does not exist or is not a directory
	 */
	private static long sizeOfDirectory(File directory) {
		File[] files = directory.listFiles();
		if (files == null) { // null maybe if security restricted
			String message = null;
			if (!directory.exists()) {
				message = directory + " does not exist";
			} else if (!directory.isDirectory()) {
				message = directory + " is not a directory";
			}
			if (message != null) {
				throw new IllegalArgumentException(message);
			}
			return 0L;
		}
		long size = 0;
		for (int i = 0; i < files.length; ++i) {
			File file = files[i];
			if (file.isDirectory()) size += sizeOfDirectory(file);
			else size += file.length();
		}
		return size;
	}

	private static void w(String string, Throwable t) {
		if (WARN) {
			Log.w(TAG, string, t);
		}
	}
}
