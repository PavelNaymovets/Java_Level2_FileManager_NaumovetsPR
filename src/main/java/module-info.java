module ru.gb.filemanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.gb.filemanager to javafx.fxml;
    exports ru.gb.filemanager;
}