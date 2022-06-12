package ru.gb.filemanager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    @FXML
    private TextField pathField;
    @FXML
    private ListView<FileInfo> fileList;
//root - текущая директория
    Path root;

    Path selectedCopyFile;
    Path selectedMoveFile;
    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*
        Initializable - интерфейс. Могу настроить поведение при старте программы.
         */
        Path root = Paths.get("1");
        fileList.getItems().addAll(scanFiles(root));

        //Генерирую ячейки для ListView
        fileList.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> fileInfoListView) {
                return new ListCell<FileInfo>() {
                    //updateItem() - Отвечает за то, как выглядит ячейка (что написано, какой стиль, какие картинки нарисованы)
                    @Override
                    protected void updateItem(FileInfo item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFilename = String.format("%-30s", item.getFileName()); // %s - строка. - выравнивание по левому краю. 30 - 30 символов.
                            String formattedFileLength = String.format("%,d bytes", item.getLength()); // %d - число. , разделитель разрядов. 20 - 20 символов.
                            if(item.getLength() == -1){
                                formattedFileLength = String.format("%s", "[DIR]");
                            }
                            if(item.getLength() == -2){
                                formattedFileLength = String.format("");
                            }
                            String text = String.format("%s %-20s", formattedFilename, formattedFileLength);
                            //item - информация об одном файле
                            setText(text);
                        }
                    }
                };
            }
        });
        goToPath(Paths.get("1"));
    }

    public void goToPath(Path path){
        root = path;
        pathField.setText(root.toAbsolutePath().toString());
        fileList.getItems().clear();
        fileList.getItems().add(new FileInfo(FileInfo.getUpToken(), -2L));
        fileList.getItems().addAll(scanFiles(path));
        fileList.getItems().sort((o1, o2) -> {
            if(o1.getFileName().equals(FileInfo.getUpToken())){
                return -1;
            }
            if((int)Math.signum(o1.getLength()) == (int)Math.signum(o2.getLength())){
                return o1.getFileName().compareTo(o2.getFileName());
            }
            return new Long(o1.getLength() - o2.getLength()).intValue();
        });
    }
    public List<FileInfo> scanFiles(Path root) {
        try {
//            Получение списка путей из корневой папки без стримов
//            List<FileInfo> out = new ArrayList<>();
//            List<Path> pathInRoot = null;
//            pathInRoot = Files.list(root).collect(Collectors.toList());
//            for (Path path : pathInRoot) {
//                out.add(new FileInfo(path));
//            }
//            return out;

//            Запрашиваю список файлов в директории. Возвращается стрим путей. Затем стрим путей преобразуется
//            в FileInfo. Все собираю в лист. Возвращаю лист.
            return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Files scan exception: " + root);
        }
    }

    public void filesListClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem(); //получили ссылку на выбранный элемент
            if(fileInfo != null){
                if(fileInfo.isDirectory()){
                    Path pathTo = root.resolve(fileInfo.getFileName()); //склеиваем 2 пути. Если кликнули на папку, значит вы хотите туда войти
                    goToPath(pathTo);
                }
            }
            if(fileInfo.isUpElement()){
                Path pathTo = root.toAbsolutePath().getParent();
                goToPath(pathTo);
            }
        }
    }
    //Обновляет список файлов в текущем каталоге
    public void refresh(){
        goToPath(root);
    }
    public void copyAction(ActionEvent actionEvent) {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if(selectedCopyFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())){
            return;
        }
        if(selectedCopyFile == null){
            selectedCopyFile = root.resolve(fileInfo.getFileName());
            ((Button)actionEvent.getSource()).setText("Копируется: " + fileInfo.getFileName());
            return;
        }
        if(selectedCopyFile != null){
            try {
                Files.copy(selectedCopyFile,root.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selectedCopyFile = null;
                ((Button) actionEvent.getSource()).setText("Копирование");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно скопировать файл");
                alert.showAndWait();
            }
        }
    }

    public void deleteAction(ActionEvent actionEvent) {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if(fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement()){
            return;
        }
        try {
            Files.delete(root.resolve(fileInfo.getFileName()));
            refresh();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить выбранный файл");
            alert.showAndWait();
        }
    }

    public void moveAction(ActionEvent actionEvent) {
        FileInfo fileInfo = fileList.getSelectionModel().getSelectedItem();
        if(selectedMoveFile == null && (fileInfo == null || fileInfo.isDirectory() || fileInfo.isUpElement())){
            return;
        }
        if(selectedMoveFile == null){
            selectedMoveFile = root.resolve(fileInfo.getFileName());
            ((Button)actionEvent.getSource()).setText("Перемещается: " + fileInfo.getFileName());
            return;
        }
        if(selectedMoveFile != null){
            try {
                Files.move(selectedMoveFile,root.resolve(selectedMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selectedMoveFile = null;
                ((Button) actionEvent.getSource()).setText("Перемещение");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно скопировать файл");
                alert.showAndWait();
            }
        }
    }
}