/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import validator.TarefaValidacao;

/**
 * FXML Controller class
 *
 * @author humberto.leone
 * @version 1.1
 * @email humberto.leone@ciscorporate.com, leoneazevedo@outlook.com
 */
public class LayoutController implements Initializable {

    @FXML
    private TextArea textValid;
    @FXML
    private Button btnUpload;
    @FXML
    private Button btnValidar;
    @FXML
    private ComboBox selectTemplate;
    @FXML
    private ProgressBar barID;

    private Stage stage;
    Workbook workbook;
    String tipoTemplate;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        selectTemplate.getItems().addAll(
                "",
                "VENDAS",
                "AT",
                "IT"
        );
        textValid.setWrapText(true);
        btnValidar.setDisable(true);
        btnUpload.setDisable(true);
        barID.setProgress(0);

    }

    @FXML
    public void selectComboBox() {
        String evento = (String) selectTemplate.getSelectionModel().getSelectedItem();
        eventoSelect(evento);
    }

    public void init(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void actionUpload(ActionEvent event) throws IOException, InvalidFormatException {

        btnValidar.setDisable(true);
        textValid.setText("--------CARREGANDO TEMPLATE DE " + tipoTemplate + "--------\n");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione o template");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                textValid.appendText("\nValidando arquivo " + file);

                workbook = WorkbookFactory.create(file);//aqui eu pego o arquivo depois do upload

                infoInicialTemplate();

            } catch (IOException | InvalidFormatException | EncryptedDocumentException ex) {
                System.out.println("Erro ao carregar arquivo");
            }

        } else {
            btnValidar.setDisable(true);
        }
    }
    public int numSheets() {
        return workbook.getNumberOfSheets();
    }
    //--------------DADOS INICIAIS DO TEMPLATE -----------------
    
    public void infoInicialTemplate() {

        DataFormatter dataFormatter = new DataFormatter();
        int numeroSheets = numSheets();
        String tipoVendas = null;

        textValid.appendText("\nTemplate com " + numeroSheets + " abas : ");

        if (numeroSheets == 4) {
            for (Sheet sheet : workbook) {
                textValid.appendText("\n=> " + sheet.getSheetName());
                Cell tipo = sheet.getRow(1).getCell(3);
                tipoVendas = dataFormatter.formatCellValue(tipo);
            }

            textValid.appendText("\nNovas OS's de " + tipoVendas + "- Clique em Validar para "
                    + "iniciar verificação");
            btnValidar.setDisable(false);

        } else if (numeroSheets == 2) {
            for (Sheet sheet : workbook) {
                textValid.appendText("\n=> " + sheet.getSheetName());
                Cell tipo = sheet.getRow(1).getCell(3);
                tipoVendas = dataFormatter.formatCellValue(tipo);
            }

            textValid.appendText("\nAlteração de OS's já existentes de " + tipoVendas + "- Clique em Validar "
                    + "para iniciar verificação");
            btnValidar.setDisable(false);

        } else {
            textValid.appendText("\n\nTemplate inválido. \n\t\tO template deve ter a aba Transaction para alteração."
                    + "\n\t\tOu deve conter as abas Order, Transaction e Participant, para novas OS's.");
            btnValidar.setDisable(true);
        }

    }

    public void eventoSelect(String tevento) {
        switch (tevento) {
            case "":
                btnValidar.setDisable(true);
                btnUpload.setDisable(true);
                break;
            case "VENDAS":
                btnUpload.setDisable(false);
                tipoTemplate = "VENDAS";
                break;
            case "AT":
                btnUpload.setDisable(false);
                tipoTemplate = "AT";
                break;
            case "IT":
                btnUpload.setDisable(false);
                tipoTemplate = "IT";
                break;
        }
    }

    @FXML
    private void actionValidar(ActionEvent event) {

        Runnable tarefa = new TarefaValidacao(textValid, workbook, tipoTemplate, btnValidar);
        Thread tValida = new Thread(tarefa);
        
        tValida.start();

    }


}
