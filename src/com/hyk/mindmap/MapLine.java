package com.hyk.mindmap;

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

    /**
     * 无参构造，供反序列化使用
     */
    public MapLine() {
        // 1.保留空构造，供序列化恢复对象使用
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
        // 2.记录起始节点和终止节点
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

    /**
     * 获取起点X属性
     * 
     * @return 起点X属性
     */
    public SimpleDoubleProperty getBeginXProperty() {
        // 1.返回起点X属性
        return beginXProperty;
    }

    /**
     * 获取起点Y属性
     * 
     * @return 起点Y属性
     */
    public SimpleDoubleProperty getBeginYProperty() {
        // 1.返回起点Y属性
        return beginYProperty;
    }

    /**
     * 获取终点X属性
     * 
     * @return 终点X属性
     */
    public SimpleDoubleProperty getEndXProperty() {
        // 1.返回终点X属性
        return endXProperty;
    }

    /**
     * 获取终点Y属性
     * 
     * @return 终点Y属性
     */
    public SimpleDoubleProperty getEndYProperty() {
        // 1.返回终点Y属性
        return endYProperty;
    }

    /**
     * 获取保存用起点X坐标
     * 
     * @return 保存用起点X坐标
     */
    public double getSaveBeginX() {
        // 1.返回保存用起点X坐标
        return saveBeginX;
    }

    /**
     * 设置保存用起点X坐标
     * 
     * @param saveBeginX 保存用起点X坐标
     */
    public void setSaveBeginX(double saveBeginX) {
        // 1.记录保存用起点X坐标
        this.saveBeginX = saveBeginX;
    }

    /**
     * 获取保存用起点Y坐标
     * 
     * @return 保存用起点Y坐标
     */
    public double getSaveBeginY() {
        // 1.返回保存用起点Y坐标
        return saveBeginY;
    }

    /**
     * 设置保存用起点Y坐标
     * 
     * @param saveBeginY 保存用起点Y坐标
     */
    public void setSaveBeginY(double saveBeginY) {
        // 1.记录保存用起点Y坐标
        this.saveBeginY = saveBeginY;
    }

    /**
     * 获取保存用终点X坐标
     * 
     * @return 保存用终点X坐标
     */
    public double getSaveEndX() {
        // 1.返回保存用终点X坐标
        return saveEndX;
    }

    /**
     * 设置保存用终点X坐标
     * 
     * @param saveEndX 保存用终点X坐标
     */
    public void setSaveEndX(double saveEndX) {
        // 1.记录保存用终点X坐标
        this.saveEndX = saveEndX;
    }

    /**
     * 获取保存用终点Y坐标
     * 
     * @return 保存用终点Y坐标
     */
    public double getSaveEndY() {
        // 1.返回保存用终点Y坐标
        return saveEndY;
    }

    /**
     * 设置保存用终点Y坐标
     * 
     * @param saveEndY 保存用终点Y坐标
     */
    public void setSaveEndY(double saveEndY) {
        // 1.记录保存用终点Y坐标
        this.saveEndY = saveEndY;
    }

    /**
     * 获取起始节点
     * 
     * @return 起始节点
     */
    public MapNode getBeginNode() {
        // 1.返回起始节点
        return beginNode;
    }

    /**
     * 获取结束节点
     * 
     * @return 结束节点
     */
    public MapNode getEndNode() {
        // 1.返回结束节点
        return endNode;
    }
}
