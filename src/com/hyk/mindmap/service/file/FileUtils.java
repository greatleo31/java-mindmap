package com.hyk.mindmap.service.file;

import com.czj.mindmap.Controller.adaptiveController;
import com.hyk.mindmap.ui.MapLine;
import com.hyk.mindmap.ui.MapNode;
import com.hyk.mindmap.ui.MapTab;
import com.hyk.mindmap.service.layout.LayoutUtils;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class FileUtils {
    private static FXMLLoader fxmlLoader;
    private static adaptiveController controller;
    public static Stage stage;

    /**
     * 保存当前思维导图
     * 
     * @param mapTab 当前页签
     * @throws IOException 保存异常
     */
    public static void storeMap(MapTab mapTab) throws IOException {
        // 1.无页签时直接返回
        if (mapTab == null) {
            return;
        }
        // 2.优先获取已有保存路径
        File target = mapTab.getSaveFile();
        if (target == null) {
            // 3.首次保存时弹出保存对话框
            target = chooseMindMapSaveFile(mapTab);
            if (target == null) {
                return;
            }
            target = ensureExtension(target, ".mindmap");
            mapTab.setSaveFile(target);
            mapTab.textProperty().set(stripMindMapExtension(target.getName()));
        }
        // 4.写入思维导图文件
        writeMap(mapTab, target);
    }

    /**
     * 另存为思维导图文件
     * 
     * @param mapTab 当前页签
     * @throws IOException 保存异常
     */
    public static void storeMapAt(MapTab mapTab) throws IOException {
        // 1.无页签时直接返回
        if (mapTab == null) {
            return;
        }
        // 2.弹出保存对话框选择新路径
        File target = chooseMindMapSaveFile(mapTab);
        if (target == null) {
            return;
        }
        // 3.补全扩展名并更新页签保存信息
        target = ensureExtension(target, ".mindmap");
        mapTab.setSaveFile(target);
        mapTab.textProperty().set(stripMindMapExtension(target.getName()));
        // 4.写入思维导图文件
        writeMap(mapTab, target);
    }

    /**
     * 选择思维导图保存路径
     * 
     * @param mapTab 当前页签
     * @return 保存文件
     */
    private static File chooseMindMapSaveFile(MapTab mapTab) {
        // 1.准备默认存储目录和文件选择器
        File storageDirectory = ensureStorageDirectory();
        FileChooser fc = new FileChooser();
        // 2.设置默认标题、文件名和目录
        fc.setTitle("保存思维导图");
        fc.setInitialFileName(mapTab.textProperty().getValue() + ".mindmap");
        fc.setInitialDirectory(storageDirectory);
        // 3.限制文件类型并弹出保存窗口
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("思维导图文件", "*.mindmap"),
                new FileChooser.ExtensionFilter("所有文件", "*.*"));
        return fc.showSaveDialog(stage);
    }

    /**
     * 将思维导图对象写入文件
     * 
     * @param mapTab 当前页签
     * @param file   目标文件
     * @throws IOException 写入异常
     */
    private static void writeMap(MapTab mapTab, File file) throws IOException {
        // 1.先保存节点和连线的瞬时属性
        saveProperties(mapTab);
        // 2.序列化写入目标文件
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))) {
            output.writeObject(mapTab);
        }
    }

    /**
     * 保存页签内所有节点和连线属性
     * 
     * @param mapTab 当前页签
     */
    public static void saveProperties(MapTab mapTab) {
        // 1.保存中心节点及其子树位置
        MapNode center = mapTab.getCenter();
        saveNodePosition(center);
        // 2.保存所有连线位置
        ArrayList<MapLine> mapLines = mapTab.getMapLines();
        for (MapLine mapLine : mapLines) {
            saveLinePosition(mapLine);
        }
    }

    /**
     * 递归保存节点位置和文本
     * 
     * @param node 当前节点
     */
    public static void saveNodePosition(MapNode node) {
        // 1.记录当前节点坐标和文本
        node.setSaveX(node.getXProperty().get());
        node.setSaveY(node.getYProperty().get());
        node.setSaveText(node.getTextProperty().getValue());
        // 2.递归保存所有子节点
        for (MapNode child : node.getChildNodes()) {
            saveNodePosition(child);
        }
    }

    /**
     * 保存连线端点位置
     * 
     * @param mapLine 当前连线
     */
    public static void saveLinePosition(MapLine mapLine) {
        // 1.记录连线起点和终点坐标
        mapLine.setSaveBeginX(mapLine.getBeginXProperty().get());
        mapLine.setSaveBeginY(mapLine.getBeginYProperty().get());
        mapLine.setSaveEndX(mapLine.getEndXProperty().get());
        mapLine.setSaveEndY(mapLine.getEndYProperty().get());
    }

    /**
     * 打开思维导图文件
     * 
     * @param file 目标文件
     */
    public static void openMap(File file) {
        // 1.无文件时直接返回
        if (file == null) {
            return;
        }
        String fileName = file.getName();
        // 2.校验文件扩展名
        if (!fileName.toLowerCase().endsWith(".mindmap")) {
            showWarning("文件类型错误", "请选择 .mindmap 思维导图文件。");
            return;
        }

        // 3.反序列化读取思维导图对象
        MapTab target;
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
            target = (MapTab) input.readObject();
        } catch (Exception e) {
            showWarning("打开失败", "文件无法打开或格式不正确。");
            e.printStackTrace();
            return;
        }

        // 4.更新保存文件信息并移除同名tab
        target.setSaveFile(file);
        target.textProperty().set(stripMindMapExtension(fileName));
        TabPane tabPane = controller.getTabPane();
        for (Tab tab : new ArrayList<Tab>(tabPane.getTabs())) {
            if (tab.getText().equals(stripMindMapExtension(fileName))) {
                tabPane.getTabs().remove(tab);
                break;
            }
        }
        // 5.显示导图并更新界面状态
        showMap(target);
        controller.updateMapName(target.getText());
        controller.showStatus("打开成功：" + fileName);
    }

    /**
     * 将思维导图显示到界面
     * 
     * @param mapTab 目标页签
     */
    public static void showMap(MapTab mapTab) {
        // 1.准备tab容器和画布
        TabPane tabPane = controller.getTabPane();
        AnchorPane anchorPane = new AnchorPane();
        MapNode center = mapTab.getCenter();
        ArrayList<MapLine> mapLines = mapTab.getMapLines();
        // 2.先将所有连线加入画布
        for (MapLine mapLine : mapLines) {
            anchorPane.getChildren().add(mapLine);
        }
        // 3.递归加载所有节点
        loadNodes(center, anchorPane);
        // 4.恢复连线属性和端点位置
        for (MapLine mapLine : mapLines) {
            mapLine.init();
            mapLine.setStartX(mapLine.getSaveBeginX() + mapLine.getBeginNode().prefWidthProperty().get() / 2);
            mapLine.setStartY(mapLine.getSaveBeginY() + mapLine.getBeginNode().prefHeightProperty().get() / 2);
            mapLine.setEndX(mapLine.getSaveEndX() + mapLine.getEndNode().prefWidthProperty().get() / 2);
            mapLine.setEndY(mapLine.getSaveEndY() + mapLine.getEndNode().prefHeightProperty().get() / 2);
        }

        // 5.将画布装入tab并加入界面
        mapTab.attachToPane(anchorPane);
        mapTab.init();
        tabPane.getTabs().add(mapTab);
        tabPane.getSelectionModel().select(mapTab);
        controller.registerMap(mapTab);
        // 6.按原布局状态重新排版
        if (mapTab.isAuto()) {
            LayoutUtils.autoLayout();
        } else if (mapTab.isLeft()) {
            LayoutUtils.leftLayout();
        } else {
            LayoutUtils.rightLayout();
        }
    }

    /**
     * 递归加载节点到画布
     * 
     * @param node       当前节点
     * @param anchorPane 目标画布
     */
    public static void loadNodes(MapNode node, AnchorPane anchorPane) {
        // 1.初始化节点并恢复坐标
        node.init();
        node.setXProperty(node.getSaveX());
        node.setYProperty(node.getSaveY());
        // 2.恢复右侧树节点绑定关系
        node.setTreeNode(new TreeItem(node.getSaveText()));
        node.getTreeNode().valueProperty().bind(node.textProperty());
        // 3.将当前节点加入画布
        anchorPane.getChildren().add(node);
        // 4.递归加载子节点并挂接树结构
        for (MapNode child : node.getChildNodes()) {
            child.setParentNode(node);
            loadNodes(child, anchorPane);
            child.getParentNode().getTreeNode().getChildren().add(child.getTreeNode());
        }
        // 5.恢复节点文本
        node.setTextProperty(node.getSaveText());
    }

    /**
     * 导出JPG图片
     * 
     * @param stage 当前窗口
     */
    public static void outputJPG(Stage stage) {
        // 1.按JPG格式导出图片
        outputImage(stage, "jpg", ".jpg", "导出为 JPG");
    }

    /**
     * 导出PNG图片
     * 
     * @param stage 当前窗口
     */
    public static void outputPNG(Stage stage) {
        // 1.按PNG格式导出图片
        outputImage(stage, "png", ".png", "导出为 PNG");
    }

    /**
     * 导出当前导图图片
     * 
     * @param stage     当前窗口
     * @param format    图片格式
     * @param extension 文件扩展名
     * @param title     对话框标题
     */
    private static void outputImage(Stage stage, String format, String extension, String title) {
        // 1.获取当前页签
        MapTab curTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        if (curTab == null) {
            return;
        }
        // 2.弹出文件选择器获取保存路径
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.setInitialFileName(curTab.getText() + extension);
        fc.setInitialDirectory(ensureStorageDirectory());
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(title, "*" + extension),
                new FileChooser.ExtensionFilter("所有文件", "*.*"));
        File path = fc.showSaveDialog(stage);
        if (path == null) {
            return;
        }
        // 3.截取当前画布快照
        File file = ensureExtension(path, extension);
        WritableImage image = curTab.getContent().snapshot(new SnapshotParameters(), null);
        // 4.按指定格式输出图片
        try {
            if ("jpg".equals(format)) {
                writeJpg(image, file);
            } else {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);
            }
            controller.showStatus("导出成功：" + file.getName());
        } catch (IOException e) {
            controller.showStatus("导出失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 将快照写出为JPG文件
     * 
     * @param image 图片快照
     * @param file  目标文件
     * @throws IOException 写入异常
     */
    private static void writeJpg(WritableImage image, File file) throws IOException {
        // 1.将FX图片转换为BufferedImage
        BufferedImage source = SwingFXUtils.fromFXImage(image, null);
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        // 2.填充白色背景并绘制原图
        Graphics2D graphics = rgb.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        // 3.写出JPG文件
        ImageIO.write(rgb, "jpg", file);
    }

    /**
     * 确保存储目录存在
     * 
     * @return 存储目录
     */
    private static File ensureStorageDirectory() {
        // 1.定位storage目录
        File storageDirectory = new File("storage");
        // 2.目录不存在时自动创建
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }
        // 3.返回存储目录
        return storageDirectory;
    }

    /**
     * 确保文件名带有指定扩展名
     * 
     * @param file      原文件
     * @param extension 扩展名
     * @return 处理后的文件
     */
    private static File ensureExtension(File file, String extension) {
        // 1.已有扩展名时直接返回
        if (file.getName().toLowerCase().endsWith(extension)) {
            return file;
        }
        // 2.补全扩展名后返回新文件对象
        return new File(file.getParentFile(), file.getName() + extension);
    }

    /**
     * 去掉思维导图扩展名
     * 
     * @param fileName 文件名
     * @return 去掉扩展名后的文件名
     */
    private static String stripMindMapExtension(String fileName) {
        // 1.是mindmap文件时去掉扩展名
        if (fileName.toLowerCase().endsWith(".mindmap")) {
            return fileName.substring(0, fileName.length() - ".mindmap".length());
        }
        // 2.其他文件名原样返回
        return fileName;
    }

    /**
     * 弹出警告提示框
     * 
     * @param title   标题
     * @param content 内容
     */
    private static void showWarning(String title, String content) {
        // 1.创建警告对话框
        Alert alert = new Alert(Alert.AlertType.WARNING);
        // 2.设置标题和内容
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        // 3.显示提示框
        alert.showAndWait();
    }

    public static void setFxmlLoader(FXMLLoader fxmlLoader) {
        FileUtils.fxmlLoader = fxmlLoader;
    }

    public static void setController(adaptiveController controller) {
        FileUtils.controller = controller;
    }
}
