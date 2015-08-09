package net.virtela.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import net.virtela.constants.Constant;

public class LogWriter {
	
	private String logPath;
	
	public LogWriter(String desPath) {
		super();
		this.logPath = desPath + Constant.SLASH + Constant.LOG_NAME;
		CommonHelper.deleteFile(this.logPath);
		
	}
	
	public void addLog(String message) {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(logPath, true));
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally { // always close the file
			if (bufferedWriter != null)
				try {
					bufferedWriter.close();
				} catch (IOException ioe2) {
				}
		}
	}
	

}
