package net.virtela.poi.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import net.virtela.constants.Constant;
import net.virtela.constants.ErrorConstant;
import net.virtela.model.exception.ServiceException;
import net.virtela.model.ErrorMessage;
import net.virtela.model.ErrorMessages;
import net.virtela.util.CommonHelper;

/**
 * @author rreyles@nttglobal.net
 */

public class FormCreator {

	public static final String FILE_TYPE_XLSX = "xlsx";
	public static final String FILE_TYPE_XLS = "xls";
	public static final String FILE_TYPE_XLSM = "xlsm";
	public static final String FILE_TYPE_PDF = "pdf";

	private static CellStyle dateCellStyle;
	private static Sheet sheet;
	
	private FormCreator() {
		//Not to be initiated
	}

	/**
	 * Will return XSSFWorkbook if template is XLSX Will return HSSFWorkbook if template is XLS
	 * 
	 * @param templatePath
	 *            path to the template file
	 * @return Poi Workbook or null of template does not exist
	 */
	public static Workbook getExistingWorkBook(String templatePath) throws ServiceException {
		if (new File(templatePath).exists()) {
			try {
				final String fileExtension = CommonHelper.getFileExtension(templatePath);

				if (fileExtension.equals(FILE_TYPE_XLSX) || fileExtension.equals(FILE_TYPE_XLSM)) {
					return new XSSFWorkbook(new FileInputStream(templatePath));
				} else if (fileExtension.equals(FILE_TYPE_XLS)) {
					return new HSSFWorkbook(new FileInputStream(templatePath));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new ServiceException(generateTemplateDoesNotExistError());
	}

	public static Workbook getExistingWorkBook(java.io.InputStream fileIo, String fileType) throws ServiceException {
		if (fileIo != null && fileType != null) {
			try {
				if (fileType.equals(FILE_TYPE_XLSX) || fileType.equals(FILE_TYPE_XLSM)) {
					return new XSSFWorkbook(fileIo);
				} else if (fileType.equals(FILE_TYPE_XLS)) {
					return new HSSFWorkbook(fileIo);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		throw new ServiceException(generateTemplateDoesNotExistError());
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

	public static void saveWorkBook(Workbook wb, String savePath) {
		try {
			final FileOutputStream fileOut = new FileOutputStream(savePath);
			wb.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public static void autoSizeCol(Sheet sheet, final int startCol, final int endCol) {
        for (int colIndex = startCol; colIndex < endCol; colIndex++) {
        	sheet.autoSizeColumn(colIndex, true);
        }
    }
    
    public static void deleteColumn(Sheet sheet, int columnToDelete){        
        int maxColumn = 0;
        for ( int r=0; r < sheet.getLastRowNum()+1; r++ ){
            Row row     = sheet.getRow(r);
 
            // if no row exists here; then nothing to do; next!
            if ( row == null )
                continue;
 
            // if the row doesn't have this many columns then we are good; next!
            int lastColumn = row.getLastCellNum();
            if ( lastColumn > maxColumn )
                maxColumn = lastColumn;
 
            if ( lastColumn < columnToDelete )
                continue;
 
            for ( int x=columnToDelete+1; x < lastColumn + 1; x++ ){
                Cell oldCell    = row.getCell(x-1);
                if ( oldCell != null )
                    row.removeCell( oldCell );
 
                Cell nextCell   = row.getCell( x );
                if ( nextCell != null ){
                    Cell newCell    = row.createCell( x-1, nextCell.getCellType() );
                    cloneCell(newCell, nextCell);
                }
            }
        }
    }
 
    private static void cloneCell( Cell cNew, Cell cOld ){
        cNew.setCellComment( cOld.getCellComment() );
        cNew.setCellStyle( cOld.getCellStyle() );
 
        switch ( cNew.getCellType() ){
            case Cell.CELL_TYPE_BOOLEAN:{
                cNew.setCellValue( cOld.getBooleanCellValue() );
                break;
            }
            case Cell.CELL_TYPE_NUMERIC:{
                cNew.setCellValue( cOld.getNumericCellValue() );
                break;
            }
            case Cell.CELL_TYPE_STRING:{
                cNew.setCellValue( cOld.getStringCellValue() );
                break;
            }
            case Cell.CELL_TYPE_ERROR:{
                cNew.setCellValue( cOld.getErrorCellValue() );
                break;
            }
            case Cell.CELL_TYPE_FORMULA:{
                cNew.setCellFormula( cOld.getCellFormula() );
                break;
            }
        }
 
    }

	public static void setCellValues(Sheet sheet, final List<Object> values, final int rowIndex, final int colIndex) {
		final Row row = getRowInstance(sheet, rowIndex);
		int tempCollIndex = colIndex;
		for (Object value : values) {
			final Cell cell = getCellInstance(row, tempCollIndex);
			identfyAndSetCellValue(cell, value);
			tempCollIndex += 1;
		}
	}

	public static void setCellValue(Sheet sheet, final Object value, final int rowIndex, final int colIndex) {
		setCellValue(sheet, value,  getRowInstance(sheet, rowIndex), colIndex, null);
	}
	
	public static void setCellValue(Sheet sheet, final Object value, Row row, final int colIndex, CellStyle style) {
		setCellValue(sheet, value,  row, colIndex, null, null);
	}
	
	public static void setCellValue(Sheet sheet, final Object value, Row row, final int colIndex, CellStyle style, String comment) {
		final Cell cell = getCellInstance(row, colIndex);
		if (value != null) {
			identfyAndSetCellValue(cell, value);
		} else {
			identfyAndSetCellValue(cell, Constant.EMPTY_STRING);
		}
		if (style != null) {
			cell.setCellStyle(style);
		}
		if (comment != null) {
			
		}
	}
	
	public static void setCellValue(Sheet sheet, final Object value, String cellRef) {
		final CellReference cr = new CellReference(cellRef);
		setCellValue(sheet, value, cr.getRow(), cr.getCol());
	}

	public static Row getRowInstance(Sheet sheet, final int rowIndex) {
		Row row = sheet.getRow(rowIndex);
		if (row != null) {
			return row;
		} else {
			return sheet.createRow(rowIndex);
		}
	}

	public static Cell getCellInstance(final Row row, final int celIndex) {
		if (row.getCell(celIndex) != null) {
			return row.getCell(celIndex);
		} else {
			return row.createCell(celIndex);
		}
	}

	public void unhideColumn(int colIndex) {
		sheet.setColumnHidden(colIndex, false);
	}

	public void hideColumns(int starttColIndex, int endColIndex) {
		for (int index = starttColIndex; index < endColIndex; index++) {
			sheet.setColumnHidden(index, true);
		}
	}

	public static void hideRows(Sheet sheet, int currentRowIndex, int endRowIndex) {
		for (int index = currentRowIndex; index < endRowIndex; index++) {
			Row row = sheet.getRow(currentRowIndex);
			row.setZeroHeight(true);
			currentRowIndex += 1;
		}
	}

	private static void identfyAndSetCellValue(final Cell cell, final Object value) {

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
			cell.setCellStyle(dateCellStyle);
		} else {
			if (CommonHelper.hasLineBreaks(value.toString())) {
				final CellStyle cellStyle = cell.getCellStyle();
				cellStyle.setWrapText(true);
				cell.setCellStyle(cellStyle);
			}
			cell.setCellValue(value.toString());
		}
	}

	public static void copyRowFormat(Workbook workbook, Sheet worksheet, int sourceRowIndex, int destinationRowIndex) {
		final Row sourceRow = worksheet.getRow(sourceRowIndex);
		worksheet.shiftRows(destinationRowIndex, worksheet.getLastRowNum(), 1);
		final Row newRow = worksheet.createRow(destinationRowIndex);

		for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
			Cell oldCell = sourceRow.getCell(i);
			Cell newCell = newRow.createCell(i);

			if (oldCell == null) {
				newCell = null;
				continue;
			}

			newCell.setCellStyle(oldCell.getCellStyle());

			if (oldCell.getCellComment() != null) {
				newCell.setCellComment(oldCell.getCellComment());
			}

			if (oldCell.getHyperlink() != null) {
				newCell.setHyperlink(oldCell.getHyperlink());
			}

			newCell.setCellType(oldCell.getCellType());

		}

		/** Copy Merged cell **/
		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			final CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
			if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
				final CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
				        (newRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress.getFirstColumn(),
				        cellRangeAddress.getLastColumn());
				worksheet.addMergedRegion(newCellRangeAddress);
			}
		}
	}

	public static void lockedCell(Sheet sheet, String password, List<RowColumnIndex> rowColumnIndexes) {
		for (Row row : sheet) {
			for (Cell cell : row) {
				boolean setCellToLocked = false;
				final int rowIndex = row.getRowNum();
				final int colIndex = cell.getColumnIndex();

				for (RowColumnIndex rowColumnIndex : rowColumnIndexes) {
					if (CommonHelper.isEqual(rowIndex, rowColumnIndex.getRowIndex())
					        && CommonHelper.isEqual(colIndex, rowColumnIndex.getColumnIndex())) {
						setCellToLocked = true;
						break;
					}
				}

				if (setCellToLocked) {
					final CellStyle lockedStyle = sheet.getWorkbook().createCellStyle();
					lockedStyle.cloneStyleFrom(cell.getCellStyle());
					lockedStyle.setLocked(true);

					cell.setCellStyle(lockedStyle);
				} else {
					cell.getCellStyle().setLocked(false);
				}
			}
		}
		sheet.protectSheet(password);
	}

	public static class RowColumnIndex {
		private int rowIndex;
		private int colIndex;

		public RowColumnIndex(int rowIndex, int colIndex) {
			super();
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
		}

		public int getRowIndex() {
			return rowIndex;
		}

		public void setRowIndex(int rowIndex) {
			this.rowIndex = rowIndex;
		}

		public int getColumnIndex() {
			return colIndex;
		}

		public void setColumnIndex(int colIndex) {
			this.colIndex = colIndex;
		}

	}
	
	public static void evalutateSheetArea(XSSFWorkbook wb, String sheet, int startRow, int startCol, int endRow, int endCol) {

		for (int row = startRow; row <= endRow; row++) {
			final Row cellRow = getRowInstance(wb.getSheet(sheet), row);
			for (int col = startCol; col <= endCol; col++) {
				final Cell cell = getCellInstance(cellRow, col);
				new XSSFFormulaEvaluator(wb).evaluateFormulaCell(cell);
			}
		}
	}

	public static void evalutateCell(XSSFWorkbook wb, String sheet, int row, int col) {
		final Row cellRow = getRowInstance(wb.getSheet(sheet), row);
		final Cell cell = getCellInstance(cellRow, col);
		new XSSFFormulaEvaluator(wb).evaluateFormulaCell(cell);		
	}

	public static void evalutateColumn(XSSFWorkbook wb, String sheet, int col, int startRow, int endRow) {
		evalutateSheetArea(wb, sheet, startRow, col, endRow, col);
	}

}
