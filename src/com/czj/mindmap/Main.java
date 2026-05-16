package com.czj.mindmap;

import com.czj.mindmap.Controller.adaptiveController;
import com.hyk.mindmap.utils.FileUtils;
import com.hyk.mindmap.utils.Menu2Utils;
import com.hyk.mindmap.utils.NodeUtils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    public static void main(String[] args) {
        /*
         * 在 scene.fxml 里，主界面大概是这样：
         * 最外层是一个 AnchorPane
         * 里面有一个 MenuBar
         * TabPane，在左边大区域
         * 一个 TreeView，在右边大区域
         * 每一个 Tab 代表一张思维导图
         * 每个 Tab 里面又放了一个 AnchorPane，这个 pane 才是真正的“导图画布
         */
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("scene.fxml"));
        Pane root = fxmlLoader.load();
        Scene scene = new Scene(root);

        String cssPath = getClass().getResource("/com/lsy/mindmap/Css/MindMap.css").toString();
        scene.getStylesheets().add(cssPath);

        Image image = new Image(getClass().getResourceAsStream("/com/lsy/mindmap/image/icon.png"));
        stage.setTitle("轻量思维导图绘制工具");
        stage.getIcons().add(image);

        FileUtils.stage = stage;
        adaptiveController controller = fxmlLoader.getController();
        controller.setStage(stage);
        NodeUtils.setFxmlLoader(fxmlLoader);
        NodeUtils.setController(controller);
        Menu2Utils.setFxmlLoader(fxmlLoader);
        Menu2Utils.setController(controller);
        FileUtils.setFxmlLoader(fxmlLoader);
        FileUtils.setController(controller);

        controller.getOpen().setOnAction(event -> {
            File storage = new File("storage");
            if (!storage.exists()) {
                storage.mkdirs();
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(storage);
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("思维导图文件", "*.mindmap"),
                    new FileChooser.ExtensionFilter("所有文件", "*.*"));
            File targetFile = fileChooser.showOpenDialog(stage);
            FileUtils.openMap(targetFile);
        });

        stage.setScene(scene);
        stage.show();
    }
}
