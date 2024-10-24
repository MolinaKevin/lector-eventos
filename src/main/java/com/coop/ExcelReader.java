package com.coop;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

public class ExcelReader {
    public static void main(String[] args) {
        // Verificar si se pasó la ruta del archivo como parámetro
        if (args.length < 1) {
            System.out.println("Por favor, proporciona la ruta del archivo .xls como argumento.");
            System.exit(1);
        }

        String excelFilePath = args[0];  // Ruta del archivo .xls pasado por parámetro

        try (FileInputStream fis = new FileInputStream(new File(excelFilePath))) {
            // Crear instancia de HSSFWorkbook para archivos .xls
            HSSFWorkbook workbook = new HSSFWorkbook(fis);

            // Obtener la primera hoja del archivo
            Sheet sheet = workbook.getSheetAt(0);

            // Iterar sobre las filas de la hoja
            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t");
                            break;
                        case BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        default:
                            System.out.print("Tipo desconocido\t");
                            break;
                    }
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

