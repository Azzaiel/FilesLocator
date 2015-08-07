package net.virtela.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.virtela.constants.Constant;
import net.virtela.util.CommonHelper;

/**
 * 
 * @author rreyles
 *
 */
public class Runner {

	private final static String[] fileSreachArray = CommonHelper.readConfig(Constant.KEY_SCAN_FILES)
			.split(Constant.COMMA);
	private static File rootScnDir = null;

	public static void main(String[] args) {
		System.out.println("Files Locator has started.....");
		if (isConfigValid()) {
			System.out.println("Starting scan.....");
			rootScnDir = new File(CommonHelper.readConfig(Constant.KEY_SCAN_DIR));
			int filesFound = searchAndStore(rootScnDir);
			if (filesFound > 0) {
				System.out.println("Total of " + filesFound + " File(s) was found and is stored in: "
						+ CommonHelper.readConfig(Constant.KEY_SCN_STORE));
			} else {
				System.out.println("No thing was found.");
			}

		}
	}

	private static int searchAndStore(File dir) {
		int filesFound = 0;
		if (dir != null) {
			System.out.println("Scanning on direcotry: " + dir.getName());
			if ( dir.listFiles() == null) {
				return 0;
			}
			for (File scanFile : dir.listFiles()) {
				if (scanFile != null) {
					if (scanFile.isDirectory()) {
						filesFound += searchAndStore(scanFile);
					} else if (isFileAMatch(scanFile)) {
						try {
							storeFile(scanFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						filesFound += 1;
					}
				}
			}
		}
		return filesFound;
	}

	private static boolean isFileAMatch(File file) {
		for (String fileSearch : fileSreachArray) {
			if (file.getName().equalsIgnoreCase(fileSearch)) {
				return true;
			}
		}
		return false;
	}

	private static void storeFile(File file) throws IOException {
		final StringBuffer savePath = new StringBuffer();
		savePath.append(CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		savePath.append(Constant.SLASH);
		savePath.append(generateFileName(file));

		InputStream input = null;
		OutputStream output = null;

		try {
			input = new FileInputStream(file);
			output = new FileOutputStream(savePath.toString());
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			input.close();
			output.close();
		}

	}

	private static String generateFileName(File file) {
		String filePath = file.getPath().replace(rootScnDir.getPath(), "");
		filePath = filePath.replace("\\", "_");
		filePath = filePath.replace(" ", "_");
		filePath = filePath.replace("/", "_");
		filePath = filePath.substring(1);
		return filePath;
	}

	private static boolean isConfigValid() {
		final File scnDir = new File(CommonHelper.readConfig(Constant.KEY_SCAN_DIR));
		if (scnDir.exists() && scnDir.isDirectory() && scnDir.canRead()) {
			System.out.println("Directory to search: " + CommonHelper.readConfig(Constant.KEY_SCAN_DIR));
		} else {
			System.out.println(
					"Search Directory is invalid (Not existing, not a directory or has no read access)... Exiting program.");
			return false;
		}

		final File storeDir = new File(CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		if (storeDir.exists() && storeDir.isDirectory() && storeDir.canWrite()) {
			System.out.println("Directory to store located files: " + CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		} else {
			System.out.println(
					"Store Directory is invalid (Not existing, not a directory or has no write access)... Exiting program.");
			return false;
		}

		if (fileSreachArray.length < 0) {
			System.out.println("There is no file to search... Exiting program.");
			return false;
		}

		return true;
	}

}
