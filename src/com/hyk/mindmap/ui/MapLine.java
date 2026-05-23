package com.hyk.mindmap.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.Line;

import java.io.Serializable;

public class MapLine extends Line implements Serializable {
    private transient SimpleDoubleProperty beginXProperty = new SimpleDoubleProperty();
    private transient SimpleDoubleProperty beginYProperty = new SimpleDoubleProperty();
    private transient SimpleDoubleProperty endXProperty = new SimpleDoubleProperty();
    private transient SimpleDoubleProperty endYProperty = new SimpleDoubleProperty();
    private double saveBeginX;
    private double saveBeginY;
    private double saveEndX;
    private double saveEndY;
    private MapNode beginNode;
    private MapNode endNode;

    public MapLine() {
    }

    /**
     * 新建一条节点连线并初始化
     * 
     * @param beginNode 起始节点
     * @param endNode   结束节点
     * @param beginX    起点X坐标
     * @param beginY    起点Y坐标
     * @param endX      终点X坐标
     * @param endY      终点Y坐标
     */
    public MapLine(MapNode beginNode, MapNode endNode, double beginX, double beginY, double endX, double endY) {
        // 1.设置连线起止坐标
        super.setStartX(beginX);
        super.setStartY(beginY);
        super.setEndX(endX);
        super.setEndY(endY);
        // 2.记录起始节点 and 终止节点
        this.beginNode = beginNode;
        this.endNode = endNode;
        // 3.初始化属性绑定和样式
        init();
    }

    /**
     * 初始化连线样式和节点坐标绑定
     */
    public void init() {
        // 1.设置连线样式
        this.getStyleClass().add("MindNodeLine");
        // 2.反序列化后为空时重新初始化属性对象
        if (this.beginXProperty == null) {
            this.beginXProperty = new SimpleDoubleProperty();
        }
        if (this.beginYProperty == null) {
            this.beginYProperty = new SimpleDoubleProperty();
        }
        if (this.endXProperty == null) {
            this.endXProperty = new SimpleDoubleProperty();
        }
        if (this.endYProperty == null) {
            this.endYProperty = new SimpleDoubleProperty();
        }
        // 3.绑定节点坐标属性
        this.beginXProperty.bind(beginNode.getXProperty());
        this.beginYProperty.bind(beginNode.getYProperty());
        this.endXProperty.bind(endNode.getXProperty());
        this.endYProperty.bind(endNode.getYProperty());
        // 4.监听属性变化并同步线段端点位置
        this.beginXProperty.addListener((observable, oldValue, newValue) -> MapLine.super.setStartX(
                newValue.doubleValue() + MapLine.this.beginNode.getPrefWidth() / 2));
        this.beginYProperty.addListener((observable, oldValue, newValue) -> MapLine.super.setStartY(
                newValue.doubleValue() + MapLine.this.beginNode.getPrefHeight() / 2));
        this.endXProperty.addListener((observable, oldValue,
                newValue) -> MapLine.super.setEndX(newValue.doubleValue() + MapLine.this.endNode.getPrefWidth() / 2));
        this.endYProperty.addListener((observable, oldValue,
                newValue) -> MapLine.super.setEndY(newValue.doubleValue() + MapLine.this.endNode.getPrefHeight() / 2));
    }

    public SimpleDoubleProperty getBeginXProperty() {
        return beginXProperty;
    }

    public SimpleDoubleProperty getBeginYProperty() {
        return beginYProperty;
    }

    public SimpleDoubleProperty getEndXProperty() {
        return endXProperty;
    }

    public SimpleDoubleProperty getEndYProperty() {
        return endYProperty;
    }

    public double getSaveBeginX() {
        return saveBeginX;
    }

    public void setSaveBeginX(double saveBeginX) {
        this.saveBeginX = saveBeginX;
    }

    public double getSaveBeginY() {
        return saveBeginY;
    }

    public void setSaveBeginY(double saveBeginY) {
        this.saveBeginY = saveBeginY;
    }

    public double getSaveEndX() {
        return saveEndX;
    }

    public void setSaveEndX(double saveEndX) {
        this.saveEndX = saveEndX;
    }

    public double getSaveEndY() {
        return saveEndY;
    }

    public void setSaveEndY(double saveEndY) {
        this.saveEndY = saveEndY;
    }

    public MapNode getBeginNode() {
        return beginNode;
    }

    public MapNode getEndNode() {
        return endNode;
    }
}
