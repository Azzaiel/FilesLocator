package net.virtela.util;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.virtela.constants.Constant;

/**
 * 
 * @author rreyles
 *
 */
public class CommonHelper {
	
	private static Properties configProp;
	
	private static synchronized Properties getConfigPropInstance() {
		if (configProp == null) {
			
			final StringBuffer configPath = new StringBuffer();
			
			configPath.append(new File("").getAbsolutePath());
			configPath.append(Constant.SLASH);
			configPath.append(Constant.CONFIG_NAME);
		
			try {
				configProp = new Properties();
				final FileInputStream configFile = new FileInputStream(configPath.toString());
				configProp.load(configFile);
				configFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return configProp;
	}

	private static Object File(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String readConfig(String configKey) {
		final Properties configProp = getConfigPropInstance();
		return configProp.getProperty(configKey);
	}


}
