package com.hyk.mindmap.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;

import java.io.Serializable;
import java.util.ArrayList;

import com.hyk.mindmap.service.tree.TreeUtils;

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

    public MapNode() {
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
                TreeUtils.newChildNode(this.getParentNode());
            } else if (event.getCode().equals(ENTER)) {
                TreeUtils.newChildNode(this);
            }
        });
        // 4.监听文本长度并动态更新宽度
        changeTextLen();
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
            double textLen = this.getTextLen();
            // 实际宽度变化量=新增文本宽度-当前宽度（最低不低于默认宽度80）
            double change = textLen - Math.max(this.prefWidthProperty().get(), TextWidth);
            this.prefWidthProperty().set(textLen);
            if (this.left) {
                // 左侧布局左右颠倒x的位置即可
                change = -1 * change;
                this.setXProperty(this.getXProperty().get() + change);
            }
            // 同步更新子节点位置
            ArrayList<MapNode> childNodes = this.getChildNodes();
            for (MapNode childNode : childNodes) {
                if (this.isLeft() == childNode.isLeft()) {
                    TreeUtils.changeX(childNode, change);
                }
                // 强制重绘
                this.setXProperty(this.getXProperty().get() + 0.1);
                this.setXProperty(this.getXProperty().get() - 0.1);
            }
        });
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

    public ObservableValue<? extends String> getTextProperty() {
        return textProperty;
    }

    public SimpleDoubleProperty getXProperty() {
        return XProperty;
    }

    public SimpleDoubleProperty getYProperty() {
        return YProperty;
    }

    public ArrayList<MapNode> getChildNodes() {
        return childNodes;
    }

    public MapNode getParentNode() {
        return parentNode;
    }

    public int getDeep() {
        return deep;
    }

    public double getHigh() {
        return high;
    }

    public MapLine getEndWithLine() {
        return endWithLine;
    }

    public boolean isCenter() {
        return center;
    }

    public boolean isLeft() {
        return left;
    }

    public void setTextProperty(String textProperty) {
        this.textProperty.set(textProperty);
    }

    public void setXProperty(double XProperty) {
        this.XProperty.set(XProperty);
    }

    public void setYProperty(double YProperty) {
        this.YProperty.set(YProperty);
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setChildNodes(ArrayList<MapNode> childNodes) {

        this.childNodes = childNodes;
    }

    public void setParentNode(MapNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setCenter(boolean center) {
        this.center = center;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setEndWithLine(MapLine endWithLine) {
        this.endWithLine = endWithLine;
    }

    public TreeItem getTreeNode() {
        return treeNode;
    }

    public double getOffset() {
        return offset;
    }

    public void setTreeNode(TreeItem treeNode) {
        this.treeNode = treeNode;
    }

    public double getRightHigh() {
        return rightHigh;
    }

    public void setRightHigh(double rightHigh) {
        this.rightHigh = rightHigh;
    }

    public double getMoveX() {
        return moveX;
    }

    public void setMoveX(double moveX) {
        this.moveX = moveX;
    }

    public double getMoveY() {
        return moveY;
    }

    public void setMoveY(double moveY) {
        this.moveY = moveY;
    }

    public double getSaveX() {
        return saveX;
    }

    public void setSaveX(double saveX) {
        this.saveX = saveX;
    }

    public double getSaveY() {
        return saveY;
    }

    public void setSaveY(double saveY) {
        this.saveY = saveY;
    }

    public String getSaveText() {
        return saveText;
    }

    public void setSaveText(String saveText) {
        this.saveText = saveText;
    }
}
