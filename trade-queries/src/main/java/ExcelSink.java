import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hazelcast.jet.datamodel.Tuple3;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.SinkBuilder;

// https://www.baeldung.com/java-microsoft-excel
public class ExcelSink {

	public static Sink<? super Entry<String, Tuple3<Long, Long, Integer>>> buildExcelSink(long timestamp) {
		
		Instant instant = Instant.ofEpochMilli(timestamp);
		LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

		// ":" Not allowed in Excel
		final String sheetName = "Export " + localDateTime.getHour() + "_" + localDateTime.getMinute(); 
		
		return SinkBuilder.sinkBuilder(
				"excelSink", 
				__ -> {
					Workbook workbook = new XSSFWorkbook();
					System.out.println("__ WORKBOOK + " + System.identityHashCode(workbook));//XXX
					
					Sheet sheet = workbook.createSheet(sheetName);
					sheet.setColumnWidth(0, 10_000);
					sheet.setColumnWidth(1, 10_000);
					sheet.setColumnWidth(2, 10_000);
					sheet.setColumnWidth(3, 10_000);
					 
					Row header = sheet.createRow(0);
					
					CellStyle headerStyle = workbook.createCellStyle();
					headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
					headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					 
					XSSFFont font = ((XSSFWorkbook) workbook).createFont();
					font.setFontName("Arial");
					font.setFontHeightInPoints((short) 16);
					font.setBold(true);
					headerStyle.setFont(font);
					 
					Cell headerCell0 = header.createCell(0);
					Cell headerCell1 = header.createCell(1);
					Cell headerCell2 = header.createCell(2);
					Cell headerCell3 = header.createCell(3);
					headerCell0.setCellValue("Stock");
					headerCell1.setCellValue("Count");
					headerCell2.setCellValue("Sum");
					headerCell3.setCellValue("Latest");
					headerCell0.setCellStyle(headerStyle);
					headerCell1.setCellStyle(headerStyle);
					headerCell2.setCellStyle(headerStyle);
					headerCell3.setCellStyle(headerStyle);
					
					return workbook;
				})
				.receiveFn((Workbook workbook, Entry<String, Tuple3<Long, Long, Integer>> entry) -> {
					 
					Sheet sheet = workbook.getSheetAt(0);

					int rowCount = 0;
					Iterator<Row> iterator = sheet.rowIterator();
					while (iterator.hasNext()) {
						rowCount++;
						iterator.next();
					}
					
					Row row = sheet.getRow(rowCount);
					if (row == null) {
						row = sheet.createRow(rowCount);
					}
					CellStyle headerStyle = workbook.createCellStyle();
					headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
					headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					 
					XSSFFont font = ((XSSFWorkbook) workbook).createFont();
					font.setFontName("Courier");
					font.setFontHeightInPoints((short) 16);
					font.setBold(true);
					headerStyle.setFont(font);
					 
					Cell headerCell0 = row.createCell(0);
					Cell headerCell1 = row.createCell(1);
					Cell headerCell2 = row.createCell(2);
					Cell headerCell3 = row.createCell(3);
					headerCell0.setCellValue(entry.getKey());
					headerCell1.setCellValue("'" + entry.getValue().f0());
					headerCell2.setCellValue("'" + entry.getValue().f1());
					headerCell3.setCellValue("'" + entry.getValue().f2());
					headerCell0.setCellStyle(headerStyle);
					headerCell1.setCellStyle(headerStyle);
					headerCell2.setCellStyle(headerStyle);
					headerCell3.setCellStyle(headerStyle);
				})
				.preferredLocalParallelism(1)
				.destroyFn(workbook -> {
					String base = "/Users/" + System.getProperty("user.name") + "/Desktop/";
					String filename = base + "excel-" + timestamp + ".xlsx";
					
					FileOutputStream fileOutputStream = new FileOutputStream(new File(filename));
					workbook.write(fileOutputStream);
					
					workbook.close();
				})
				.build();
	}
}
