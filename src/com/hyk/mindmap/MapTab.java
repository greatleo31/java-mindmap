package com.hyk.mindmap;

import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import com.hyk.mindmap.utils.NodeUtils;

public class MapTab extends Tab implements Serializable {
    private MapNode center;
    private ArrayList<MapLine> mapLines;
    private boolean left;
    private boolean auto;
    private File saveFile;

    /**
     * 无参构造，供反序列化使用
     */
    public MapTab() {
        // 1.保留空构造，供序列化恢复对象使用
    }

    /**
     * 新建一个思维导图页签
     * 
     * @param text 中心节点文本
     */
    public MapTab(String text) {
        // 1.新建中心节点并设置页签标题
        center = new MapNode(text);
        super.textProperty().set(text);
        // 2.监听中心节点文本变化并同步页签标题
        center.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                super.textProperty().set(newValue.trim());
            }
        });
        // 3.初始化中心节点位置和状态
        center.setCenter(true);
        center.setXProperty(100);
        center.setYProperty(250);
        center.getStyleClass().add("rootNode");
        this.setLeft(false);
        // 4.初始化页签事件
        init();
    }

    /**
     * 初始化页签中的节点和拖拽事件
     */
    public void init() {
        // 1.空连线集合时先初始化
        if (mapLines == null) {
            mapLines = new ArrayList<MapLine>();
        }
        // 2.监听中心节点文本变化并同步页签标题
        center.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                super.textProperty().set(newValue.trim());
            }
        });
        // 3.按下鼠标时记录拖拽起点
        center.setOnMousePressed(event -> {
            center.setMoveX(event.getSceneX());
            center.setMoveY(event.getSceneY());
        });
        // 4.拖拽时同步移动整棵树
        center.setOnMouseDragged(event -> {
            NodeUtils.changeX(center, event.getSceneX() - center.getMoveX());
            NodeUtils.addY(center, event.getSceneY() - center.getMoveY());
            center.setMoveX(event.getSceneX());
            center.setMoveY(event.getSceneY());
        });
    }

    /**
     * 获取中心节点
     * 
     * @return 中心节点
     */
    public MapNode getCenter() {
        // 1.返回中心节点
        return center;
    }

    /**
     * 获取连线集合
     * 
     * @return 连线集合
     */
    public ArrayList<MapLine> getMapLines() {
        // 1.返回连线集合
        return mapLines;
    }

    /**
     * 设置中心节点
     * 
     * @param center 中心节点
     */
    public void setCenter(MapNode center) {
        // 1.记录中心节点
        this.center = center;
    }

    /**
     * 设置连线集合
     * 
     * @param mapLines 连线集合
     */
    public void setMapLines(ArrayList<MapLine> mapLines) {
        // 1.记录连线集合
        this.mapLines = mapLines;
    }

    /**
     * 判断是否为左布局
     * 
     * @return 是否为左布局
     */
    public boolean isLeft() {
        // 1.返回左布局标记
        return left;
    }

    /**
     * 设置左布局标记
     * 
     * @param left 左布局标记
     */
    public void setLeft(boolean left) {
        // 1.记录左布局标记
        this.left = left;
    }

    /**
     * 判断是否为自动布局
     * 
     * @return 是否为自动布局
     */
    public boolean isAuto() {
        // 1.返回自动布局标记
        return auto;
    }

    /**
     * 设置自动布局标记
     * 
     * @param auto 自动布局标记
     */
    public void setAuto(boolean auto) {
        // 1.记录自动布局标记
        this.auto = auto;
    }

    /**
     * 获取保存文件
     * 
     * @return 保存文件
     */
    public File getSaveFile() {
        // 1.返回保存文件
        return saveFile;
    }

    /**
     * 设置保存文件
     * 
     * @param saveFile 保存文件
     */
    public void setSaveFile(File saveFile) {
        // 1.记录保存文件
        this.saveFile = saveFile;
    }

    /**
     * 将页签绑定到画布
     * 
     * @param anchorPane 画布
     */
    public void attachToPane(AnchorPane anchorPane) {
        // 1.将画布设置为当前页签内容
        setContent(anchorPane);
    }
}
