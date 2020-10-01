package net.virtela.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import net.virtela.constants.Constant;
import net.virtela.model.FileDir;
import net.virtela.model.exception.ServiceException;
import net.virtela.poi.export.FormCreator;
import net.virtela.util.CommonHelper;
import net.virtela.util.LogWriter;

/**
 * 
 * @author rreyles
 *
 */
public class Runner {

	private final static String[] fileSreachArray = CommonHelper.readConfig(Constant.KEY_SCAN_FILES).split(Constant.COMMA);
	private static final boolean FileDir = false;
	private static File rootScnDir = null;
	private static LogWriter logWritter = null;
	
	static ForkJoinPool parentPool = new ForkJoinPool(10);
	static ForkJoinPool childPool = new ForkJoinPool(20);

	public static void main(String[] args) throws ServiceException, CloneNotSupportedException {
		System.out.println("Files Locator has started.....");
		if (isConfigValid()) {
			System.out.println("Starting scan.....");
			logWritter = new LogWriter(CommonHelper.readConfig(Constant.KEY_SCN_STORE));
			rootScnDir = new File(CommonHelper.readConfig(Constant.KEY_SCAN_DIR));
			
			final Path parentPath = Paths.get(CommonHelper.readConfig(Constant.KEY_SCAN_DIR));
			
			
			
			if (parentPath != null && Files.isDirectory(parentPath)) {
				System.out.println("Scanning on direcotry: " + parentPath.getFileName());
				
				final List<Callable<List<FileDir>>> callFileDirList = new ArrayList<>();
				Arrays.asList(parentPath.toFile()
		                                .listFiles())
				                        .forEach(file -> {
				                        	callFileDirList.add(() -> {
				                				return locateFile(file);
				                			});
				                        });
				
				final List<FileDir> fileDirList = callFileDirList.parallelStream()
						                                         .map(parentPool::submit)
						                                         .map(ForkJoinTask::join)
						                                         .flatMap(List::stream)
						                                         .collect(Collectors.toList());
				System.out.println("Done scanning!");
				System.out.println("--------------------------------------------------------------------------------------");
				System.out.println("====================================Result============================================");
				System.out.println("--------------------------------------------------------------------------------------");
				
				fileDirList.forEach(rec -> {
					printDir(rec);
				});
				exportToExcel(fileDirList);
			} else {
				System.out.println("Main Directory is invalid... Exiting program");
			}
			
			parentPool.shutdown();
			childPool.shutdown();
			
			
		}
	}
	
	private static Integer rowIndex = 1;
	private static Integer colIndex = 0;
	
	private static void exportToExcel(List<FileDir> fileDirList) throws ServiceException, CloneNotSupportedException {
		
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("====================================Export============================================");
		System.out.println("--------------------------------------------------------------------------------------");
		
		final String tempalte = "/opt/templates/FileCrawler.xlsx";
		final String sheetName = "Data";
		
		final StringBuilder savePath = new StringBuilder();
		savePath.append(CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		savePath.append(Constant.SLASH);
		savePath.append(Instant.now().toEpochMilli());
		savePath.append(Constant.DOT);
		savePath.append(Constant.FILE_TYPE_XLSX);
		
		
		final Workbook workBook = FormCreator.getExistingWorkBook(tempalte);
		final Sheet formSheet = workBook.getSheet(sheetName);
		
		for (FileDir parentDir : fileDirList) {
			final List<FileDir> expandedList = new ArrayList<>();
			expandedList.addAll(expandList(parentDir));
			for (FileDir expnad : expandedList) {
			   System.out.println(getDirCrawl(expnad));
			}
			printToExcel(expandedList, formSheet);
		}
	
	
		FormCreator.saveWorkBook(workBook, savePath.toString());
	}
	
	private static String getDirCrawl(FileDir fileDir) {
		StringBuilder out = new StringBuilder();
		out.append(fileDir.getFileName());
		if (fileDir.getChild() != null) {
			out.append(" - ");
			out.append(getDirCrawl(fileDir.getChild()));
		}
		return out.toString();
	}
	
	private static List<FileDir> expandList(FileDir parentDir) throws CloneNotSupportedException {
		final List<FileDir> fileDirList = new ArrayList<>();
		if (parentDir.getChildList() != null) {
			for (FileDir childDir : parentDir.getChildList()) {
				 FileDir expand = new FileDir(parentDir.getFileName());
				 final List<FileDir> ccList = expandList(childDir);
				 for (FileDir ccDir : ccList) {
					 FileDir expandClone =  (FileDir) expand.clone();
					 expandClone.setChild(ccDir);
					 fileDirList.add(expandClone);
				 }
			}
		} else {
			fileDirList.add(new FileDir(parentDir.getFileName()));
		}
		return fileDirList;
	}
	
	
	private static void printToExcel(List<FileDir> fileDirList, Sheet formSheet) {
		for (FileDir fileDir : fileDirList ) {
			FormCreator.setCellValue(formSheet, fileDir.getFileName(), rowIndex, colIndex);
			colIndex++;
			if (fileDir.getChild() != null) {
				printChildDir(fileDir.getChild(), formSheet);
			} else {
				rowIndex = rowIndex + 1;
				colIndex = 0;
			}
		}
	}
	
	private static void printChildDir(FileDir childDir, Sheet formSheet) {
		FormCreator.setCellValue(formSheet, childDir.getFileName(), rowIndex, colIndex);
		colIndex++;
		if (childDir.getChild() != null) {
			printChildDir(childDir.getChild(), formSheet);
		} else {
			rowIndex = rowIndex + 1;
			colIndex = 0;
		}
	}
	
	private static void printDir(FileDir fileDir) {
		printDir(fileDir, 0);
	}

	private static void printDir(FileDir fileDir, int tabCount) {
		final StringBuilder printOut = new StringBuilder();
		for (int index = 1 ; index <= tabCount; index++) {
			printOut.append("-");
		}
		if(tabCount > 0) {
			printOut.append("►");
		}
		printOut.append(fileDir.getFileName());
		System.out.println(printOut.toString());
		if (fileDir.getChildList() != null) {
			fileDir.getChildList().forEach(rec -> {
				printDir(rec, tabCount + 1);
			});
		}
	}

	private static List<FileDir> locateFile(File inFile) {
		final List<FileDir> fileDirList = new ArrayList<>();
		if (inFile != null) {
			FileDir parentFileDir = new FileDir(inFile.getName());
			if (inFile.isFile()) {
//				checkFile(inFile);
				fileDirList.add(parentFileDir);
				return fileDirList;
			} 
			System.out.println("Scanning on direcotry: " + inFile.getName());
			final List<Callable<List<FileDir>>> callFileDirList = new ArrayList<>();
			final List<File> fileList =  Arrays.asList(inFile.listFiles());
			fileList.forEach(file -> {
			                           callFileDirList.add(() -> {
			                		      return locateFile(file);
			                		   });
			                         });

			final List<FileDir> childDirList = callFileDirList.parallelStream()
					                                          .map(childPool::submit)
					                                          .map(ForkJoinTask::join)
					                                          .flatMap(List::stream)
					                                          .collect(Collectors.toList());
			parentFileDir.setChildList(childDirList);
			fileDirList.add(parentFileDir);
		}
		return fileDirList;
	}
	
	private static FileDir checkFile(File file) {
		if (isFileAMatch(file)) {
			try {
				storeFile(file);
				logWritter.addLog(file.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
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
			System.out.println("Search Directory is invalid (Not existing, not a directory or has no read access)... Exiting program.");
			return false;
		}

		final File storeDir = new File(CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		if (storeDir.exists() && storeDir.isDirectory() && storeDir.canWrite()) {
			System.out.println("Directory to store located files: " + CommonHelper.readConfig(Constant.KEY_SCN_STORE));
		} else {
			System.out.println("Store Directory is invalid (Not existing, not a directory or has no write access)... Exiting program.");
			return false;
		}

		if (fileSreachArray.length < 0) {
			System.out.println("There is no file to search... Exiting program.");
			return false;
		}

		return true;
	}

}
