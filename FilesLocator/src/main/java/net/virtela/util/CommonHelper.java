package net.virtela.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static String readConfig(String configKey) {
		final Properties configProp = getConfigPropInstance();
		return configProp.getProperty(configKey);
	}

	/**
	 * 
	 * @param obj
	 *            any object to test if the value is valid
	 * @return boolean true if value is valude and false if not
	 */
	public static boolean hasValidValue(final Object obj) {

		if (obj != null) {
			if (obj.getClass() == String.class) {
				final String strValue = obj.toString();
				if (strValue.trim().length() != 0) {
					return true;
				}
			} else if (isNumber(obj.toString())) {
				double number = toDecimalNumber(obj.toString());
				if (number > 0) {
					return true;
				}
			} else if (obj.getClass() == Date.class) {
				return true;
			} else if (obj.getClass() == ArrayList.class) {
				return true;
			} else if (obj.getClass() == Timestamp.class) {
				return true;
			} else if (obj.getClass() == Boolean.class) {
				return true;
			} else if (obj.getClass() == Double.class) {
				return obj.toString().length() > 0;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param strNum
	 *            - String to convert to a decimal number
	 * @return
	 */

	public static Double toDecimalNumber(Object strNum) {
		if (hasValidValue(strNum) && isNumber(strNum.toString())) {
			return Double.parseDouble(strNum.toString());
		} else {
			return new Double("0");
		}
	}

	/**
	 * 
	 * @param value
	 * @return return true if enterd String is a valid Number
	 */

	public static boolean isNumber(String value) {

		String regex = "^(\\-)?[0-9]+(\\.[0-9][0-9]?)?$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		return matcher.find();

	}

	public static void deleteFile(String filePath) {
		if (hasValidValue(filePath)) {
			final File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
		}
	}

}
