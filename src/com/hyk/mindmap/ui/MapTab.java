package com.hyk.mindmap.ui;

import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Point2D;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import com.hyk.mindmap.service.tree.TreeUtils;

public class MapTab extends Tab implements Serializable {
    private static final double MIN_SCALE = 0.4;
    private static final double MAX_SCALE = 2.5;
    private static final double SCALE_FACTOR = 1.1;

    private MapNode center;
    private ArrayList<MapLine> mapLines;
    private boolean left;
    private boolean auto;
    private File saveFile;
    private transient AnchorPane viewportPane;
    private transient AnchorPane canvasPane;
    private transient Rectangle viewportClip;
    private transient double viewScale = 1.0;

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
            double deltaX = (event.getSceneX() - center.getMoveX()) / viewScale;
            double deltaY = (event.getSceneY() - center.getMoveY()) / viewScale;
            TreeUtils.changeX(center, deltaX);
            TreeUtils.addY(center, deltaY);
            // 必需实时更新当前起始位置实现平滑移动
            center.setMoveX(event.getSceneX());
            center.setMoveY(event.getSceneY());
        });
    }

    private void ensureViewportPane() {
        if (viewportPane == null) {
            viewportPane = new AnchorPane();
            viewportPane.setPickOnBounds(true);
        }
        if (viewportClip == null) {
            viewportClip = new Rectangle();
            viewportClip.widthProperty().bind(viewportPane.widthProperty());
            viewportClip.heightProperty().bind(viewportPane.heightProperty());
            viewportPane.setClip(viewportClip);
        }
        if (canvasPane != null && !viewportPane.getChildren().contains(canvasPane)) {
            viewportPane.getChildren().setAll(canvasPane);
        }
    }

    public AnchorPane getViewportPane() {
        ensureViewportPane();
        return viewportPane;
    }

    public AnchorPane getCanvasPane() {
        return canvasPane;
    }

    public double getViewScale() {
        return viewScale;
    }

    public void resetViewState() {
        viewScale = 1.0;
        if (canvasPane == null) {
            return;
        }
        canvasPane.setScaleX(viewScale);
        canvasPane.setScaleY(viewScale);
        canvasPane.setTranslateX(0);
        canvasPane.setTranslateY(0);
    }

    public void zoomAt(double sceneX, double sceneY, double deltaY) {
        if (canvasPane == null || deltaY == 0) {
            return;
        }
        Point2D pivotInCanvas = canvasPane.sceneToLocal(sceneX, sceneY);
        double scaleChange = deltaY > 0 ? SCALE_FACTOR : 1 / SCALE_FACTOR;
        double newScale = clamp(viewScale * scaleChange, MIN_SCALE, MAX_SCALE);
        if (newScale == viewScale) {
            return;
        }
        viewScale = newScale;
        canvasPane.setScaleX(viewScale);
        canvasPane.setScaleY(viewScale);
        Point2D pivotAfterScale = canvasPane.localToScene(pivotInCanvas);
        canvasPane.setTranslateX(canvasPane.getTranslateX() + sceneX - pivotAfterScale.getX());
        canvasPane.setTranslateY(canvasPane.getTranslateY() + sceneY - pivotAfterScale.getY());
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public MapNode getCenter() {
        return center;
    }

    public ArrayList<MapLine> getMapLines() {
        return mapLines;
    }

    public void setCenter(MapNode center) {
        this.center = center;
    }

    public void setMapLines(ArrayList<MapLine> mapLines) {
        this.mapLines = mapLines;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(File saveFile) {
        this.saveFile = saveFile;
    }

    public void attachToPane(AnchorPane anchorPane) {
        canvasPane = anchorPane;
        canvasPane.setPickOnBounds(false);
        ensureViewportPane();
        AnchorPane.setTopAnchor(canvasPane, 0.0);
        AnchorPane.setRightAnchor(canvasPane, 0.0);
        AnchorPane.setBottomAnchor(canvasPane, 0.0);
        AnchorPane.setLeftAnchor(canvasPane, 0.0);
        resetViewState();
        setContent(viewportPane);
    }
}
