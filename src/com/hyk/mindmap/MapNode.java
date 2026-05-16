package com.hyk.mindmap;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

import java.io.Serializable;
import java.util.ArrayList;

import com.hyk.mindmap.utils.NodeUtils;

import static javafx.scene.input.KeyCode.ENTER;

public class MapNode extends TextField implements Serializable {
    private transient SimpleStringProperty textProperty = new SimpleStringProperty();
    private transient SimpleDoubleProperty XProperty = new SimpleDoubleProperty();
    private transient SimpleDoubleProperty YProperty = new SimpleDoubleProperty();
    private double saveX;
    private double saveY;
    private String saveText;
    private double TextWidth = 80;
    private double offset = 180;
    private double TextHeight = 60;
    private int deep;
    private double high;
    private double rightHigh;
    private ArrayList<MapNode> childNodes = new ArrayList<MapNode>();
    private MapNode parentNode;
    private boolean center;
    private boolean left;
    private MapLine endWithLine;
    private transient TreeItem treeNode;
    private double moveX;
    private double moveY;

    /**
     * 无参构造，供反序列化使用
     */
    public MapNode() {
        // 1.保留空构造，供序列化恢复对象使用
    }

    /**
     * 新建一个思维导图节点
     * 
     * @param text 节点文本
     */
    public MapNode(String text) {
        // 1.初始化节点基础属性
        init();
        // 2.设置节点文本和初始状态
        super.setText(text);
        this.textProperty.set(text);
        this.center = false;
        this.left = false;
        this.high = 1.5 * super.prefHeightProperty().get();
        this.rightHigh = this.high;
    }

    /**
     * 初始化节点样式、属性和快捷键
     */
    public void init() {
        // 1.初始化节点尺寸和样式
        super.prefHeightProperty().set(TextHeight);
        super.prefWidthProperty().set(TextWidth);
        if (!this.getStyleClass().contains("MindNode")) {
            this.getStyleClass().add("MindNode");
        }
        super.setAlignment(Pos.CENTER);
        // 2.初始化文本和坐标属性
        textProperty = new SimpleStringProperty();
        XProperty = new SimpleDoubleProperty();
        YProperty = new SimpleDoubleProperty();
        super.textProperty().bindBidirectional(this.textProperty);
        super.layoutXProperty().bindBidirectional(this.XProperty);
        super.layoutYProperty().bindBidirectional(this.YProperty);
        this.textProperty.set("");
        // 3.绑定回车快捷键新增节点
        super.setOnKeyPressed(event -> {
            if (event.isShiftDown() && event.getCode().equals(ENTER) && this.getParentNode() != null) {
                NodeUtils.newChildNode(this.getParentNode());
            } else if (event.getCode().equals(ENTER)) {
                NodeUtils.newChildNode(this);
            }
        });
        // 4.监听文本长度并动态更新宽度
        changeTextLen();
    }

    /**
     * 计算当前文本应占宽度
     * 
     * @return 节点宽度
     */
    public double getTextLen() {
        // 1.先设置节点最小尺寸
        this.setMinSize(TextWidth, TextHeight);
        int len = this.textProperty.getValue().length();
        double width = 0;
        // 2.按字符类型累计文本宽度
        for (int i = 0; i < len; i++) {
            int code = this.textProperty.getValue().charAt(i);
            if (code <= 256 && code >= 0) {
                width += 15;
            } else {
                width += 20;
            }
        }
        // 3.返回不小于最小宽度的节点宽度
        if (width > 60) {
            width = super.getMinWidth() + (width - 60);
        } else {
            width = super.getMinWidth();
        }
        return width;
    }

    /**
     * 监听文本变化并动态调整节点宽度
     */
    public void changeTextLen() {
        // 1.文本为空时先置为空串
        if (this.textProperty.getValue() == null) {
            this.textProperty.set("");
        }
        // 2.监听文本变化并同步节点宽度和子节点位置
        this.textProperty.addListener(e -> {
            double width = this.getTextLen();
            double change = width - Math.max(this.prefWidthProperty().get(), TextWidth);
            this.prefWidthProperty().set(width);
            if (this.left) {
                change = -1 * change;
                this.setXProperty(this.getXProperty().get() + change);
            }
            ArrayList<MapNode> childNodes = this.getChildNodes();
            for (MapNode childNode : childNodes) {
                if (this.isLeft() == childNode.isLeft()) {
                    NodeUtils.changeX(childNode, change);
                }
                this.setXProperty(this.getXProperty().get() + 0.1);
                this.setXProperty(this.getXProperty().get() - 0.1);
            }
        });
    }

    /**
     * 获取文本属性
     * 
     * @return 文本属性
     */
    public ObservableValue<? extends String> getTextProperty() {
        // 1.返回文本属性
        return textProperty;
    }

    /**
     * 获取X坐标属性
     * 
     * @return X坐标属性
     */
    public SimpleDoubleProperty getXProperty() {
        // 1.返回X坐标属性
        return XProperty;
    }

    /**
     * 获取Y坐标属性
     * 
     * @return Y坐标属性
     */
    public SimpleDoubleProperty getYProperty() {
        // 1.返回Y坐标属性
        return YProperty;
    }

    /**
     * 获取子节点集合
     * 
     * @return 子节点集合
     */
    public ArrayList<MapNode> getChildNodes() {
        // 1.返回子节点集合
        return childNodes;
    }

    /**
     * 获取父节点
     * 
     * @return 父节点
     */
    public MapNode getParentNode() {
        // 1.返回父节点
        return parentNode;
    }

    /**
     * 获取节点深度统计
     * 
     * @return 节点深度统计
     */
    public int getDeep() {
        // 1.返回节点深度统计
        return deep;
    }

    /**
     * 获取节点高度统计
     * 
     * @return 节点高度统计
     */
    public double getHigh() {
        // 1.返回节点高度统计
        return high;
    }

    /**
     * 获取连接到父节点的连线
     * 
     * @return 连接到父节点的连线
     */
    public MapLine getEndWithLine() {
        // 1.返回连接到父节点的连线
        return endWithLine;
    }

    /**
     * 判断是否为中心节点
     * 
     * @return 是否为中心节点
     */
    public boolean isCenter() {
        // 1.返回中心节点标记
        return center;
    }

    /**
     * 判断是否为左侧节点
     * 
     * @return 是否为左侧节点
     */
    public boolean isLeft() {
        // 1.返回左侧节点标记
        return left;
    }

    /**
     * 设置节点文本
     * 
     * @param textProperty 节点文本
     */
    public void setTextProperty(String textProperty) {
        // 1.记录节点文本
        this.textProperty.set(textProperty);
    }

    /**
     * 设置X坐标
     * 
     * @param XProperty X坐标
     */
    public void setXProperty(double XProperty) {
        // 1.记录X坐标
        this.XProperty.set(XProperty);
    }

    /**
     * 设置Y坐标
     * 
     * @param YProperty Y坐标
     */
    public void setYProperty(double YProperty) {
        // 1.记录Y坐标
        this.YProperty.set(YProperty);
    }

    /**
     * 设置节点深度统计
     * 
     * @param deep 节点深度统计
     */
    public void setDeep(int deep) {
        // 1.记录节点深度统计
        this.deep = deep;
    }

    /**
     * 设置节点高度统计
     * 
     * @param high 节点高度统计
     */
    public void setHigh(double high) {
        // 1.记录节点高度统计
        this.high = high;
    }

    /**
     * 设置子节点集合
     * 
     * @param childNodes 子节点集合
     */
    public void setChildNodes(ArrayList<MapNode> childNodes) {
        // 1.记录子节点集合
        this.childNodes = childNodes;
    }

    /**
     * 设置父节点
     * 
     * @param parentNode 父节点
     */
    public void setParentNode(MapNode parentNode) {
        // 1.记录父节点
        this.parentNode = parentNode;
    }

    /**
     * 设置中心节点标记
     * 
     * @param center 中心节点标记
     */
    public void setCenter(boolean center) {
        // 1.记录中心节点标记
        this.center = center;
    }

    /**
     * 设置左侧节点标记
     * 
     * @param left 左侧节点标记
     */
    public void setLeft(boolean left) {
        // 1.记录左侧节点标记
        this.left = left;
    }

    /**
     * 设置连接到父节点的连线
     * 
     * @param endWithLine 连接到父节点的连线
     */
    public void setEndWithLine(MapLine endWithLine) {
        // 1.记录连接到父节点的连线
        this.endWithLine = endWithLine;
    }

    /**
     * 获取树节点对象
     * 
     * @return 树节点对象
     */
    public TreeItem getTreeNode() {
        // 1.返回树节点对象
        return treeNode;
    }

    /**
     * 获取节点横向偏移量
     * 
     * @return 节点横向偏移量
     */
    public double getOffset() {
        // 1.返回节点横向偏移量
        return offset;
    }

    /**
     * 设置树节点对象
     * 
     * @param treeNode 树节点对象
     */
    public void setTreeNode(TreeItem treeNode) {
        // 1.记录树节点对象
        this.treeNode = treeNode;
    }

    /**
     * 获取右侧高度统计
     * 
     * @return 右侧高度统计
     */
    public double getRightHigh() {
        // 1.返回右侧高度统计
        return rightHigh;
    }

    /**
     * 设置右侧高度统计
     * 
     * @param rightHigh 右侧高度统计
     */
    public void setRightHigh(double rightHigh) {
        // 1.记录右侧高度统计
        this.rightHigh = rightHigh;
    }

    /**
     * 获取拖拽起点X坐标
     * 
     * @return 拖拽起点X坐标
     */
    public double getMoveX() {
        // 1.返回拖拽起点X坐标
        return moveX;
    }

    /**
     * 设置拖拽起点X坐标
     * 
     * @param moveX 拖拽起点X坐标
     */
    public void setMoveX(double moveX) {
        // 1.记录拖拽起点X坐标
        this.moveX = moveX;
    }

    /**
     * 获取拖拽起点Y坐标
     * 
     * @return 拖拽起点Y坐标
     */
    public double getMoveY() {
        // 1.返回拖拽起点Y坐标
        return moveY;
    }

    /**
     * 设置拖拽起点Y坐标
     * 
     * @param moveY 拖拽起点Y坐标
     */
    public void setMoveY(double moveY) {
        // 1.记录拖拽起点Y坐标
        this.moveY = moveY;
    }

    /**
     * 获取保存用X坐标
     * 
     * @return 保存用X坐标
     */
    public double getSaveX() {
        // 1.返回保存用X坐标
        return saveX;
    }

    /**
     * 设置保存用X坐标
     * 
     * @param saveX 保存用X坐标
     */
    public void setSaveX(double saveX) {
        // 1.记录保存用X坐标
        this.saveX = saveX;
    }

    /**
     * 获取保存用Y坐标
     * 
     * @return 保存用Y坐标
     */
    public double getSaveY() {
        // 1.返回保存用Y坐标
        return saveY;
    }

    /**
     * 设置保存用Y坐标
     * 
     * @param saveY 保存用Y坐标
     */
    public void setSaveY(double saveY) {
        // 1.记录保存用Y坐标
        this.saveY = saveY;
    }

    /**
     * 获取保存用文本
     * 
     * @return 保存用文本
     */
    public String getSaveText() {
        // 1.返回保存用文本
        return saveText;
    }

    /**
     * 设置保存用文本
     * 
     * @param saveText 保存用文本
     */
    public void setSaveText(String saveText) {
        // 1.记录保存用文本
        this.saveText = saveText;
    }
}
