package net.virtela.poi.export;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.virtela.constants.Constant;
import net.virtela.constants.ErrorConstant;
import net.virtela.model.exception.ServiceException;
import net.virtela.model.ErrorMessage;
import net.virtela.model.ErrorMessages;
import net.virtela.util.CommonHelper;

public class ExcelExport {

	private Workbook workbook;
	private Sheet sheet;

	private CellStyle cellStyleRequired;
	private CellStyle cellStyleNormal;
	private CellStyle cellStyleWarning;

	public ExcelExport(String tempaltePath, String sheetName) {
		try {
			this.workbook = this.getExistingWorkBook(tempaltePath);
			this.sheet = workbook.getSheet(sheetName);
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	public void initalizeCellStyle(String sheetName, String requiredRef, String normarlRef) {
		final Sheet styleSheet = this.getWorkBook().getSheet(sheetName);
		this.cellStyleRequired = this.getCellStyleInstance(styleSheet, requiredRef);
		this.cellStyleNormal = this.getCellStyleInstance(styleSheet, normarlRef);
	}

	public void initializeRemarkStyle(String sheetName, List<Integer> remarkIndexList, String ref) {
		final Sheet styleSheet = this.getWorkBook().getSheet(sheetName);
		this.cellStyleWarning = this.getCellStyleInstance(styleSheet, ref);
		for (int index : remarkIndexList) {
			this.getSheet().setColumnHidden(index, false);
		}
	}

	public CellStyle getCellStyleRequired() {
		return cellStyleRequired;
	}

	public CellStyle getCellStyleNormal() {
		return cellStyleNormal;
	}

	public CellStyle getCellStyleWarning() {
		return cellStyleWarning;
	}

	public Workbook getWorkBook() {
		return this.workbook;
	}

	public Sheet getSheet() {
		return this.sheet;
	}

	protected Workbook getExistingWorkBook(String templatePath) throws ServiceException {
		if (new File(templatePath).exists()) {
			try {
				final String fileExtension = CommonHelper.getFileExtension(templatePath);

				if (fileExtension.equals(Constant.FILE_TYPE_XLSX) || fileExtension.equals(Constant.FILE_TYPE_XLSM)) {
					return new XSSFWorkbook(new FileInputStream(templatePath));
				} else if (fileExtension.equals(Constant.FILE_TYPE_XLS)) {
					return new HSSFWorkbook(new FileInputStream(templatePath));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new ServiceException(generateTemplateDoesNotExistError());
	}

	protected void setCellValue(final Object value, final int rowIndex, final int colIndex) {
		this.setCellValue(value, this.getRowInstance(rowIndex), colIndex, null);
	}

	protected void setCellValue(final Object value, final Row row, final int colIndex, final CellStyle style) {
		final Cell cell = this.getCellInstance(row, colIndex);
		if (value != null) {
			this.identfyAndSetCellValue(cell, value);
		} else {
			this.identfyAndSetCellValue(cell, Constant.EMPTY);
		}
		if (style != null) {
			cell.setCellStyle(style);
		}
	}

	protected Row getRowInstance(final int rowIndex) {
		return getRowInstance(this.getSheet(), rowIndex);
	}

	protected Row getRowInstance(Sheet sheet, final int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row != null) {
			return row;
		} else {
			return sheet.createRow(rowIndex);
		}
	}

	protected Cell getCellInstance(final Row row, final int celIndex) {
		if (row.getCell(celIndex) != null) {
			return row.getCell(celIndex);
		} else {
			return row.createCell(celIndex);
		}
	}

	public void identfyAndSetCellValue(final Cell cell, final Object value) {
		if (value == null) {
			// DO nothing for now
		} else if (value.getClass() == Integer.class) {
			cell.setCellValue(Integer.parseInt(value.toString()));
		} else if (value.getClass() == Double.class) {
			cell.setCellValue(Double.parseDouble(value.toString()));
		} else if (value.getClass() == Long.class) {
			cell.setCellValue(Long.parseLong(value.toString()));
		} else if (value.getClass() == Timestamp.class) {
			cell.setCellValue((Timestamp) value);
		} else {
			if (CommonHelper.hasLineBreaks(value.toString())) {
				final CellStyle cellStyle = cell.getCellStyle();
				cellStyle.setWrapText(true);
				cell.setCellStyle(cellStyle);
			}
			cell.setCellValue(value.toString());
		}
	}

	protected CellStyle getCellStyleInstance(Sheet sheet, String colRef) {
		final CellReference cellRef = new CellReference(colRef);
		final Cell cell = sheet.getRow(cellRef.getRow()).getCell(cellRef.getCol());
		if (cell != null) {
			return cell.getCellStyle();
		}
		return null;
	}

	private static ErrorMessages generateTemplateDoesNotExistError() {
		final ErrorMessages errorMessages = new ErrorMessages();
		errorMessages.setCode(500);

		final List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
		final ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.setCode(ErrorConstant.ERROR_CODE_MISSING_TEMPLATE);
		errorMessage.setMessage(ErrorConstant.ERROR_MSG_MISSING_TEMPLATE);
		errors.add(errorMessage);

		errorMessages.setErrors(errors);
		return errorMessages;
	}

	public void autoSizeCol(Sheet sheet, final int startCol, final int endCol) {
		for (int colIndex = startCol; colIndex < endCol; colIndex++) {
			sheet.autoSizeColumn(colIndex, true);
		}
	}

}
