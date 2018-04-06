/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author humberto.leone
 */
public class TarefaValidacao implements Runnable {

    @FXML
    private final TextArea textValid;
    @FXML
    private final Button btnValidar;

    Workbook workbook;
    String tipoTemplate;

    public TarefaValidacao(TextArea textValid, Workbook workbook, String tipoTemplate, Button btnValidar) {
        this.textValid = textValid;
        this.workbook = workbook;
        this.tipoTemplate = tipoTemplate;
        this.btnValidar = btnValidar;
    }

    public int numSheets() {
        return workbook.getNumberOfSheets();
    }

    @Override
    public void run() {
        Platform.runLater(()->textValid.appendText("\n\n\t\t-----VALIDAÇÃO INICIADA-----\n"));
        btnValidar.setDisable(true);

        switch (tipoTemplate) {
            case "VENDAS":

                if (numSheets() == 2) {
                    validAlteracaoOS(2);
                } else if (numSheets() == 4) {
                    validAlteracaoOS(4);
                } else {
                    Platform.runLater(()->textValid.appendText("\n\t\t----TEMPLATE DE VENDAS INVÁLIDO----"));
                }

                break;
            case "IT":
                if (numSheets() == 2) {
                    validAlteracaoOS(2);
                } else {
                    Platform.runLater(()->textValid.appendText("\n\t\t----TEMPLATE DE INSTALAÇÃO INVÁLIDO----"));
                }
                break;
            case "AT":
                if (numSheets() == 2) {
                    validAlteracaoOS(2);
                } else if (numSheets() == 4) {
                    validAlteracaoOS(4);
                } else {
                    Platform.runLater(()->textValid.appendText("\n\t\t----TEMPLATE DE AT INVÁLIDO----"));
                }

                break;
        }
        Platform.runLater(()->textValid.appendText("\n\n\t\t-----VALIDAÇÃO CONCLUIDA-----"));
    }
    
     public void validAlteracaoOS(int abasVerificar) {
        //System.out.println("VALIDANDO DADOS");

        if (abasVerificar == 2) {

            Sheet sheet = workbook.getSheetAt(1);//olho só na planilha transaction
            tamanhoOS(sheet); //verifico o tamanho da OS
            pipePipe(sheet);
            osDuplicadas(sheet);
            camposObrigatoriosAdicionaisPorEvento(sheet);//verifico os campos obrigatório por evento

        } else {

            Sheet sheetTransaction = workbook.getSheetAt(2); //olho na aba transaction                      
            Sheet sheetORDER = workbook.getSheetAt(1); //aba ORDER pode vir com OS's duplicadas
            Sheet sheetParticipantPreAss = workbook.getSheetAt(3); //pego a planilha Participant

            tamanhoOS(sheetTransaction); //verifico o tamanho da OS
            
            pipePipe(sheetTransaction);
            pipePipe(sheetORDER);
            pipePipe(sheetParticipantPreAss);
            
            osDuplicadas(sheetORDER);
            camposObrigatoriosAdicionaisPorEvento(sheetTransaction); //verifico os campos obrigatórios por evento
            // Create a DataFormatter to format and get each cell's value as String            
            camposObrigatoriosParticipantPreAss(sheetParticipantPreAss); //verifico os campos obrigatórios
            

        }

    }

    //verifico o tamanho da OS
    public void tamanhoOS(Sheet sheet) {
        DataFormatter dataFormatter = new DataFormatter();
        for (Row row : sheet) {
            Cell c = row.getCell(0);
            String cellValue = dataFormatter.formatCellValue(c);

            if (cellValue.length() > 40) {
                Platform.runLater(()->textValid.appendText("Erro na linha: " + row.getRowNum() + ":\n"
                        + "\t\tOS não pode ter tamanho superior a 40 caracteres."));
            }

        }

    }

    private void osDuplicadas(Sheet sheet) {
        DataFormatter dataFormatter = new DataFormatter();
        List<String> duplicados = new ArrayList<>();
        for (Row row : sheet) {
            Cell c = row.getCell(0);
            String cellValue = dataFormatter.formatCellValue(c);

            if (duplicados.contains(cellValue)) {
                Platform.runLater(()->textValid.appendText("\nErro na linha: " + row.getRowNum() + ":\n"
                        + "\t\tOS OS duplicada."));
            } else {
                //System.out.println("Não duplicado " + cellValue);
                duplicados.add(cellValue);
            }
        }
    }

    private void pipePipe(Sheet sheet) {
        DataFormatter dataFormatter = new DataFormatter();
        for (Row row : sheet) {
            for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
                Cell c = row.getCell(i);
                String cellValue = dataFormatter.formatCellValue(c);

                if (cellValue.contains("||")) {
                    Platform.runLater(()->textValid.appendText("\nErro na linha: " + row.getRowNum() + ":\n"
                            + "\t\t || encontrado."));
                } else {
                    //System.out.println("Sem pipe pipe ||");
                   
                }
            }

        }
    }

    //Recarga, habilitação, instalação, base ativa, etc
    public void camposObrigatoriosAdicionaisPorEvento(final Sheet sheet) {
        DataFormatter dataFormatter = new DataFormatter();
        //String colunasCell;

        Cell tipo = sheet.getRow(1).getCell(3); //Olho o event_type da planilha
        String eventType = dataFormatter.formatCellValue(tipo);

        switch (eventType) {
            case "RECARGA":
                for (Row row : sheet) {
                    camposObrigatoriosTransaction(row);
                    recarga(row);
                }
                break;
            case "HABILITACAO":
                for (Row row : sheet) {
                    camposObrigatoriosTransaction(row);
                    habilitacao(row);
                }
                break;
            case "INSTALACAO":
                for (Row row : sheet) {
                    camposObrigatoriosTransaction(row);
                    instalacao(row);
                }
                break;
            case "BASE_ATIVA":
                Cell ga7Cell = sheet.getRow(1).getCell(78); //para saber se é base ativa ou estorno de base ativa
                String ga7 = dataFormatter.formatCellValue(ga7Cell);

                if (ga7 == null || ga7.isEmpty()) {
                    for (Row row : sheet) {
                        camposObrigatoriosTransaction(row);
                        baseAtiva(row);
                    }
                } else {
                    for (Row row : sheet) {
                        camposObrigatoriosTransaction(row);
                        estornoBaseAtiva(row);
                    }
                }

                break;
            case "AJUSTE":
                for (Row row : sheet) {
                    camposObrigatoriosTransaction(row);
                }
                break;
            case "INFORMATIVO":
                for (Row row : sheet) {
                    camposObrigatoriosTransaction(row);
                    informativo(row);
                }
        }

    }

    public void camposObrigatoriosTransaction(Row row) {
        Sheet sheet = row.getSheet();
        DataFormatter dataFormatter = new DataFormatter();
        String colunasCell;
        for (int c = 0; c <= 4; c++) {
            Cell campos = row.getCell(c);
            String cellValue = dataFormatter.formatCellValue(campos);
            colunasCell = sheet.getRow(0).getCell(c).getStringCellValue();
            if (cellValue == null || cellValue.equals("")) {
                textValid.appendText("\nErro na linha: " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasCell + " é obrigatória na aba Transaction.");

            }
        }
        for (int c = 12; c <= 15; c++) {
            Cell campos = row.getCell(c);
            String cellValue = dataFormatter.formatCellValue(campos);
            colunasCell = sheet.getRow(0).getCell(c).getStringCellValue();
            if (cellValue == null || cellValue.equals("")) {
                textValid.appendText("\nErro na linha: " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasCell + " é obrigatório na aba Transaction.");

            }
        }
    }

    //----------------CAMPOS OBRIGATÓRIOS DO TRANSACTION PARTICIPANT PRE ASS-------------------
    public void camposObrigatoriosParticipantPreAss(Sheet sheet) {
        DataFormatter dataFormatter = new DataFormatter();
        for (Row row : sheet) {
            for (int c = 0; c <= 5; c++) {
                Cell campos = row.getCell(c);
                String cellValue = dataFormatter.formatCellValue(campos);
                String colunasCell = sheet.getRow(0).getCell(c).getStringCellValue();
                if (cellValue == null || cellValue.equals("")) {
                    Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + " da aba Participant Pre-Ass:\n"
                            + "\t\tColuna " + colunasCell + " é obrigatória."));

                }
            }

            Cell setNumber = row.getCell(7);
            String setNum = dataFormatter.formatCellValue(setNumber);
            String colunasCell = sheet.getRow(0).getCell(7).getStringCellValue();
            if (setNum == null || setNum.equals("")) {
                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + " da aba Participant Pre-Ass\n"
                        + "\t\tColuna " + colunasCell + " é obrigatória."));
            }

        }

    }

    public void recarga(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        int numColuna;
        Sheet sheet = row.getSheet();

        Map<Integer, Cell> obrigatoriosRecarga = new HashMap<>();
        //agora as colunas secundárias
        Cell prodID = row.getCell(6);
        Cell prodName = row.getCell(7);
        Cell ga12 = row.getCell(83);
        Cell ga24 = row.getCell(95);
        Cell gd1 = row.getCell(156);
        Cell gb1 = row.getCell(162);

        obrigatoriosRecarga.put(6, prodID);
        obrigatoriosRecarga.put(7, prodName);
        obrigatoriosRecarga.put(83, ga12);
        obrigatoriosRecarga.put(95, ga24);
        obrigatoriosRecarga.put(156, gd1);
        obrigatoriosRecarga.put(162, gb1);

        for (Map.Entry<Integer, Cell> cellREC : obrigatoriosRecarga.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(cellREC.getValue());
            numColuna = cellREC.getKey();
            String colunasHAB = sheet.getRow(0).getCell(numColuna).getStringCellValue();//nome da coluna GB5

            if (cellValue == null || cellValue.isEmpty()) {

                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasHAB + " obrigatória na aba Transaction"));
            }
        }
    }

    public void habilitacao(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        int numColuna;
        Sheet sheet = row.getSheet();

        Map<Integer, Cell> obrigatoriosHAB = new HashMap<>();
        Cell prodID = row.getCell(6);
        Cell prodName = row.getCell(7);
        Cell ga12 = row.getCell(83);
        Cell gd1 = row.getCell(156);
        int colProdID = 6, colProdName = 7, colGa12 = 83, colGd1 = 156;

        obrigatoriosHAB.put(colProdID, prodID);
        obrigatoriosHAB.put(colProdName, prodName);
        obrigatoriosHAB.put(colGa12, ga12);
        obrigatoriosHAB.put(colGd1, gd1);

        //usando um hashmap para associar uma coluna à célula e pegar o nome da coluna
        for (Map.Entry<Integer, Cell> cellHAB : obrigatoriosHAB.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(cellHAB.getValue());
            if (cellValue == null || cellValue.isEmpty()) {
                numColuna = cellHAB.getKey();
                String colunasHAB = sheet.getRow(0).getCell(numColuna).getStringCellValue();//nome da coluna GB5
                //System.out.println(cellValue+" key: "+numColuna);

                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasHAB + " obrigatória na aba Transaction"));
            }

        }

    }

    public void instalacao(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        //Sheet sheet = row.getSheet();
        //String colunasIT = sheet.getRow(0).getCell(166).getStringCellValue();//nome da coluna GB5

        Cell gb5 = row.getCell(166);
        String cellValue = dataFormatter.formatCellValue(gb5);//Valor da célula em String

        if (cellValue == null || cellValue.equals("")) {
            Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                    + "\t\tColuna  Generic Boolean 5 obrigatória na aba Transaction"));
        }
    }

    public void baseAtiva(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = row.getSheet();
        int numColuna;
        Map<Integer, Cell> obrigatoriosBA = new HashMap<>();

        Cell ga1 = row.getCell(72);
        Cell gn1Value = row.getCell(144);
        Cell gn1Uni = row.getCell(145);

        int colGa1 = 72, colGn1Value = 144, colGn1Uni = 145;

        obrigatoriosBA.put(colGa1, ga1);
        obrigatoriosBA.put(colGn1Value, gn1Value);
        obrigatoriosBA.put(colGn1Uni, gn1Uni);
        //usando um hashmap para associar uma coluna à célula e pegar o nome da coluna
        for (Map.Entry<Integer, Cell> cellBA : obrigatoriosBA.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(cellBA.getValue());
            numColuna = cellBA.getKey();
            String colunasBA = sheet.getRow(0).getCell(numColuna).getStringCellValue();//nome da coluna GB5

            if (cellValue == null || cellValue.isEmpty()) {
                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasBA + " obrigatória na aba Transaction"));
            }

        }

    }

    public void estornoBaseAtiva(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = row.getSheet();
        int numColuna;
        Map<Integer, Cell> obrigatoriosEstornoBA = new HashMap<>();

        Cell ga7 = row.getCell(78);
        Cell ga8 = row.getCell(79);
        Cell ga15 = row.getCell(86);
        Cell ga30 = row.getCell(101);
        Cell ga31 = row.getCell(102);
        Cell gn1Value = row.getCell(144);
        Cell gn1Unit = row.getCell(145);
        Cell gn2Value = row.getCell(146);
        Cell gn2Unit = row.getCell(147);

        obrigatoriosEstornoBA.put(78, ga7);
        obrigatoriosEstornoBA.put(79, ga8);
        obrigatoriosEstornoBA.put(86, ga15);
        obrigatoriosEstornoBA.put(101, ga30);
        obrigatoriosEstornoBA.put(102, ga31);
        obrigatoriosEstornoBA.put(144, gn1Value);
        obrigatoriosEstornoBA.put(145, gn1Unit);
        obrigatoriosEstornoBA.put(146, gn2Value);
        obrigatoriosEstornoBA.put(147, gn2Unit);

        //usando um hashmap para associar uma coluna à célula e pegar o nome da coluna
        for (Map.Entry<Integer, Cell> cellBA : obrigatoriosEstornoBA.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(cellBA.getValue());

            if (cellValue == null || cellValue.isEmpty()) {
                numColuna = cellBA.getKey();
                String colunasBA = sheet.getRow(0).getCell(numColuna).getStringCellValue();//nome da coluna GB5
                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasBA + " obrigatória na aba Transaction"));
            }

        }
    }

    public void ajuste(Row row) {

    }

    private void informativo(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = row.getSheet();
        int numColuna;
        Map<Integer, Cell> obrigatoriosINFORMATIVO = new HashMap<>();

        Cell ga32 = row.getCell(103);
        Cell gd6 = row.getCell(161);

        obrigatoriosINFORMATIVO.put(103, ga32);
        obrigatoriosINFORMATIVO.put(161, gd6);
        //usando um hashmap para associar uma coluna à célula e pegar o nome da coluna
        for (Map.Entry<Integer, Cell> cellBA : obrigatoriosINFORMATIVO.entrySet()) {
            String cellValue = dataFormatter.formatCellValue(cellBA.getValue());

            if (cellValue == null || cellValue.isEmpty()) {
                numColuna = cellBA.getKey();
                String colunasINFO = sheet.getRow(0).getCell(numColuna).getStringCellValue();
                Platform.runLater(()->textValid.appendText("\nErro na linha " + row.getRowNum() + ":\n"
                        + "\t\tColuna " + colunasINFO + " obrigatória na aba Transaction"));
            }

        }
    }

}
