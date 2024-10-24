package com.coop;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ExcelReaderGUI extends Application {

    private TableView<RowData> tableView;
    private ComboBox<String> userComboBox;
    private ComboBox<String> deviceComboBox;
    private DatePicker fromDateEntradaPicker;
    private DatePicker toDateEntradaPicker;
    private DatePicker fromDateSalidaPicker;
    private DatePicker toDateSalidaPicker;
    private FilteredList<RowData> filteredData;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        launch(args);  // Iniciar la aplicación JavaFX
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Excel Reader");

        // Crear el TableView donde se mostrarán las columnas
        tableView = new TableView<>();

        // Definir las columnas de la tabla y ajustar su tamaño, incluyendo el tamaño de la fuente
        TableColumn<RowData, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreCol.setMinWidth(150);
        nombreCol.setPrefWidth(200);
        nombreCol.setStyle("-fx-font-size: 16px;");  // Ajustar el tamaño de la fuente

        TableColumn<RowData, String> apellidoCol = new TableColumn<>("Apellido");
        apellidoCol.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        apellidoCol.setMinWidth(150);
        apellidoCol.setPrefWidth(200);
        apellidoCol.setStyle("-fx-font-size: 16px;");

        TableColumn<RowData, String> dispositivoCol = new TableColumn<>("Dispositivo");
        dispositivoCol.setCellValueFactory(new PropertyValueFactory<>("dispositivo"));
        dispositivoCol.setMinWidth(150);
        dispositivoCol.setPrefWidth(200);
        dispositivoCol.setStyle("-fx-font-size: 16px;");

        TableColumn<RowData, String> horaEntradaCol = new TableColumn<>("Hora de Entrada");
        horaEntradaCol.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        horaEntradaCol.setMinWidth(200);
        horaEntradaCol.setPrefWidth(250);
        horaEntradaCol.setStyle("-fx-font-size: 16px;");
        horaEntradaCol.setComparator(Comparator.comparing(this::parseDate));  // Ordenar por fecha

        TableColumn<RowData, String> horaSalidaCol = new TableColumn<>("Hora de Salida");
        horaSalidaCol.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        horaSalidaCol.setMinWidth(200);
        horaSalidaCol.setPrefWidth(250);
        horaSalidaCol.setStyle("-fx-font-size: 16px;");
        horaSalidaCol.setComparator(Comparator.comparing(this::parseDate));  // Ordenar por fecha

        TableColumn<RowData, String> tiempoTranscurridoCol = new TableColumn<>("Tiempo Transcurrido");
        tiempoTranscurridoCol.setCellValueFactory(new PropertyValueFactory<>("tiempoTranscurrido"));
        tiempoTranscurridoCol.setMinWidth(150);
        tiempoTranscurridoCol.setPrefWidth(200);
        tiempoTranscurridoCol.setStyle("-fx-font-size: 16px;");
        tiempoTranscurridoCol.setComparator((time1, time2) -> {
            long horas1 = getHoursFromTimeString(time1);
            long horas2 = getHoursFromTimeString(time2);
            return Long.compare(horas1, horas2);
        });

        // Agregar las columnas al TableView
        tableView.getColumns().addAll(nombreCol, apellidoCol, dispositivoCol, horaEntradaCol, horaSalidaCol, tiempoTranscurridoCol);

        // Asignar los colores según el tiempo transcurrido
        tableView.setRowFactory(tv -> new TableRow<RowData>() {
            @Override
            protected void updateItem(RowData rowData, boolean empty) {
                super.updateItem(rowData, empty);
                if (rowData != null && !empty) {
                    String[] tiempoParts = rowData.getTiempoTranscurrido().split(" ");
                    if (tiempoParts.length > 0) {
                        long horas = Long.parseLong(tiempoParts[0]);  // Obtener las horas transcurridas

                        // Establecer colores según el rango de horas
                        if (horas < 4) {
                            setStyle("-fx-background-color: lightgreen;");
                        } else if (horas >= 4 && horas < 8) {
                            setStyle("-fx-background-color: yellow;");
                        } else {
                            setStyle("-fx-background-color: lightcoral;");  // Rojo claro para evitar un rojo fuerte
                        }
                    } else {
                        setStyle("");  // Restablecer el estilo cuando no haya datos
                    }
                } else {
                    setStyle("");  // Limpiar el estilo para filas vacías
                }
            }
        });

        // Crear los ComboBox para los usuarios y dispositivos
        userComboBox = new ComboBox<>();
        userComboBox.setPromptText("Selecciona un usuario");
        userComboBox.getItems().add("Todos");
        userComboBox.setDisable(true);  // Deshabilitar hasta que se carguen los datos

        deviceComboBox = new ComboBox<>();
        deviceComboBox.setPromptText("Selecciona un dispositivo");
        deviceComboBox.getItems().add("Todos");
        deviceComboBox.setDisable(true);  // Deshabilitar hasta que se carguen los datos

        // Crear los DatePickers para las fechas de entrada y salida
        fromDateEntradaPicker = new DatePicker();
        fromDateEntradaPicker.setPromptText("Fecha de entrada desde");
        toDateEntradaPicker = new DatePicker();
        toDateEntradaPicker.setPromptText("Fecha de entrada hasta");

        fromDateSalidaPicker = new DatePicker();
        fromDateSalidaPicker.setPromptText("Fecha de salida desde");
        toDateSalidaPicker = new DatePicker();
        toDateSalidaPicker.setPromptText("Fecha de salida hasta");

        // Acciones cuando se selecciona un usuario, dispositivo o rango de fechas
        userComboBox.setOnAction(e -> applyFilters());
        deviceComboBox.setOnAction(e -> applyFilters());
        fromDateEntradaPicker.setOnAction(e -> applyFilters());
        toDateEntradaPicker.setOnAction(e -> applyFilters());
        fromDateSalidaPicker.setOnAction(e -> applyFilters());
        toDateSalidaPicker.setOnAction(e -> applyFilters());

        // Botón para seleccionar y cargar un archivo Excel
        Button loadButton = new Button("Cargar archivo Excel");
        loadButton.setOnAction(e -> loadExcelFile(primaryStage));

        // Layout para contener los elementos
        VBox layout = new VBox(10);
        layout.getChildren().addAll(loadButton, userComboBox, deviceComboBox, fromDateEntradaPicker, toDateEntradaPicker, fromDateSalidaPicker, toDateSalidaPicker, tableView);

        // Configurar y mostrar la escena
        Scene scene = new Scene(layout, 1000, 700);  // Ajustar el tamaño de la ventana
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Método para cargar el archivo Excel
    private void loadExcelFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xls"));

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            // Si se selecciona un archivo, cargar y mostrar su contenido
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                HSSFWorkbook workbook = new HSSFWorkbook(fis);
                Sheet sheet = workbook.getSheetAt(0);

                Map<String, List<RowData>> userEvents = new HashMap<>();
                ObservableList<RowData> rowDataList = FXCollections.observableArrayList();

                Set<String> usuarios = new HashSet<>();
                Set<String> dispositivos = new HashSet<>();

                System.out.println("Cargando archivo: " + selectedFile.getAbsolutePath());

                // Guardar los eventos en una lista primero
                List<Row> rows = new ArrayList<>();
                for (Row row : sheet) {
                    rows.add(row);
                }

                // Recorrer los eventos en orden inverso
                Collections.reverse(rows);

                for (Row row : rows) {
                    // Obtener los datos necesarios
                    org.apache.poi.ss.usermodel.Cell tiempoCell = row.getCell(0);  // Columna "Tiempo"
                    org.apache.poi.ss.usermodel.Cell nombreCell = row.getCell(2);  // Columna "Nombre"
                    org.apache.poi.ss.usermodel.Cell apellidoCell = row.getCell(3);  // Columna "Apellido"
                    org.apache.poi.ss.usermodel.Cell dispositivoCell = row.getCell(5);  // Columna "Dispositivo"
                    org.apache.poi.ss.usermodel.Cell estadoCell = row.getCell(8);  // Columna "Estado"

                    String estado = getCellValue(estadoCell);
                    System.out.println("Estado: " + estado);

                    if (estado.equals("Check-In") || estado.equals("Check-Out")) {
                        String usuario = getCellValue(nombreCell) + " " + getCellValue(apellidoCell);
                        String dispositivo = getCellValue(dispositivoCell);
                        String tiempo = getCellValue(tiempoCell);

                        System.out.println("Usuario: " + usuario + " | Dispositivo: " + dispositivo + " | Tiempo: " + tiempo);

                        usuarios.add(usuario);  // Agregar usuario a la lista de usuarios únicos
                        dispositivos.add(dispositivo);  // Agregar dispositivo a la lista de dispositivos únicos

                        RowData evento = new RowData(
                                getCellValue(nombreCell),
                                getCellValue(apellidoCell),
                                getCellValue(dispositivoCell),
                                tiempo,
                                "",
                                ""
                        );

                        if (!userEvents.containsKey(usuario)) {
                            userEvents.put(usuario, new ArrayList<>());
                        }
                        userEvents.get(usuario).add(evento);

                        // Revisar si ya hay un par de eventos consecutivos para el usuario
                        if (userEvents.get(usuario).size() >= 2) {
                            RowData evento1 = userEvents.get(usuario).get(0);
                            RowData evento2 = userEvents.get(usuario).get(1);

                            // Calcular tiempo transcurrido
                            Date horaEntrada = dateFormat.parse(evento1.getHoraEntrada());
                            Date horaSalida = dateFormat.parse(evento2.getHoraEntrada());
                            long diffMillis = horaSalida.getTime() - horaEntrada.getTime();
                            long diffHours = diffMillis / (1000 * 60 * 60);
                            long diffMinutes = (diffMillis / (1000 * 60)) % 60;

                            System.out.println("Tiempo transcurrido entre eventos para " + usuario + ": " + diffHours + " horas, " + diffMinutes + " minutos");

                            RowData nuevaFila = new RowData(
                                    evento1.getNombre(),
                                    evento1.getApellido(),
                                    evento1.getDispositivo(),
                                    evento1.getHoraEntrada(),
                                    evento2.getHoraEntrada(),
                                    diffHours + " horas, " + diffMinutes + " minutos"
                            );

                            rowDataList.add(nuevaFila);

                            userEvents.get(usuario).remove(evento1);
                            userEvents.get(usuario).remove(evento2);
                        }
                    }
                }

                // Agregar los usuarios y dispositivos al ComboBox
                userComboBox.getItems().clear();
                userComboBox.getItems().add("Todos");
                userComboBox.getItems().addAll(usuarios);
                userComboBox.setValue("Todos");
                userComboBox.setDisable(false);

                deviceComboBox.getItems().clear();
                deviceComboBox.getItems().add("Todos");
                deviceComboBox.getItems().addAll(dispositivos);
                deviceComboBox.setValue("Todos");
                deviceComboBox.setDisable(false);

                filteredData = new FilteredList<>(rowDataList, p -> true);
                tableView.setItems(filteredData);

                System.out.println("Carga completada. Filas cargadas: " + rowDataList.size());

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para aplicar todos los filtros (usuario, dispositivo, fechas)
    private void applyFilters() {
        if (filteredData == null) return;

        String selectedUser = userComboBox.getValue();
        String selectedDevice = deviceComboBox.getValue();
        LocalDate fromEntrada = fromDateEntradaPicker.getValue();
        LocalDate toEntrada = toDateEntradaPicker.getValue();
        LocalDate fromSalida = fromDateSalidaPicker.getValue();
        LocalDate toSalida = toDateSalidaPicker.getValue();

        filteredData.setPredicate(rowData -> {
            boolean matchesUser = selectedUser.equals("Todos") || (rowData.getNombre() + " " + rowData.getApellido()).equals(selectedUser);
            boolean matchesDevice = selectedDevice.equals("Todos") || rowData.getDispositivo().equals(selectedDevice);

            boolean matchesEntrada = true;
            boolean matchesSalida = true;

            try {
                Date horaEntrada = dateFormat.parse(rowData.getHoraEntrada());
                Date horaSalida = dateFormat.parse(rowData.getHoraSalida());

                if (fromEntrada != null) {
                    matchesEntrada = !horaEntrada.before(Date.from(fromEntrada.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                if (toEntrada != null) {
                    matchesEntrada = matchesEntrada && !horaEntrada.after(Date.from(toEntrada.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }

                if (fromSalida != null) {
                    matchesSalida = !horaSalida.before(Date.from(fromSalida.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                if (toSalida != null) {
                    matchesSalida = matchesSalida && !horaSalida.after(Date.from(toSalida.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return matchesUser && matchesDevice && matchesEntrada && matchesSalida;
        });
    }

    // Método auxiliar para convertir cadenas a objetos Date
    private Date parseDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método auxiliar para obtener el valor de una celda, manejando los diferentes tipos de datos
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    // Método auxiliar para extraer horas desde la cadena "Tiempo Transcurrido"
    private long getHoursFromTimeString(String timeString) {
        String[] parts = timeString.split(" ");
        return Long.parseLong(parts[0]);
    }

    // Clase para almacenar los datos de cada fila
    public static class RowData {
        private final SimpleStringProperty nombre;
        private final SimpleStringProperty apellido;
        private final SimpleStringProperty dispositivo;
        private final SimpleStringProperty horaEntrada;
        private final SimpleStringProperty horaSalida;
        private final SimpleStringProperty tiempoTranscurrido;

        public RowData(String nombre, String apellido, String dispositivo, String horaEntrada, String horaSalida, String tiempoTranscurrido) {
            this.nombre = new SimpleStringProperty(nombre);
            this.apellido = new SimpleStringProperty(apellido);
            this.dispositivo = new SimpleStringProperty(dispositivo);
            this.horaEntrada = new SimpleStringProperty(horaEntrada);
            this.horaSalida = new SimpleStringProperty(horaSalida);
            this.tiempoTranscurrido = new SimpleStringProperty(tiempoTranscurrido);
        }

        public String getNombre() {
            return nombre.get();
        }

        public String getApellido() {
            return apellido.get();
        }

        public String getDispositivo() {
            return dispositivo.get();
        }

        public String getHoraEntrada() {
            return horaEntrada.get();
        }

        public String getHoraSalida() {
            return horaSalida.get();
        }

        public String getTiempoTranscurrido() {
            return tiempoTranscurrido.get();
        }
    }
}

