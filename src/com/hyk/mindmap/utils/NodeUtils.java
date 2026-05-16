package com.hyk.mindmap.utils;

import com.czj.mindmap.Controller.adaptiveController;
import com.hyk.mindmap.MapLine;
import com.hyk.mindmap.MapNode;
import com.hyk.mindmap.MapTab;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class NodeUtils {
    private static FXMLLoader fxmlLoader;
    private static adaptiveController controller;

    /**
     * 新建子节点
     * 
     * @param node 父节点
     */
    public static void newChildNode(MapNode node) {
        // 1.获取当前tab和当前画布
        MapTab currentTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        AnchorPane currentPane = (AnchorPane) currentTab.getContent();
        // 2.新建子节点并且初始化子节点
        MapNode newNode = new MapNode("新节点");
        newNode.setParentNode(node);
        newNode.setLeft(node.isLeft());
        controller.registerNode(newNode);
        // 3.同步树TreeView
        Menu2Utils.addTreeNode(newNode);

        // 4.计算位置
        int len = node.getChildNodes().size();
        double high;
        if (node.isLeft()) {
            high = node.getHigh() - node.getRightHigh();
        } else {
            high = node.getRightHigh();
        }
        if (len >= 1) {
            newNode.setYProperty(node.getYProperty().get() + high / 2);
        } else {
            newNode.setYProperty(node.getYProperty().get());
        }

        if (node.isLeft()) {
            newNode.setXProperty(node.getXProperty().get() - newNode.prefWidthProperty().get() - 80);
        } else {
            newNode.setXProperty(node.getXProperty().get() + node.prefWidthProperty().get() + 80);
        }
        node.getChildNodes().add(newNode);

        if (len > 0) {
            changeHigh(node, node.prefHeightProperty().get() * 1.5);
        }
        changeDeep(node, 1);

        // 5.建连线并且加入tab
        MapLine mapLine = new MapLine(
                node,
                newNode,
                node.getXProperty().get() + node.prefWidthProperty().get() / 2,
                node.getYProperty().get() + node.prefHeightProperty().get() / 2,
                newNode.getXProperty().get() + newNode.prefWidthProperty().get() / 2,
                newNode.getYProperty().get() + newNode.prefHeightProperty().get() / 2);
        newNode.setEndWithLine(mapLine);
        currentPane.getChildren().add(mapLine);
        currentPane.getChildren().add(newNode);
        currentPane.getChildren().remove(node);
        currentPane.getChildren().add(node);
        currentTab.getMapLines().add(mapLine);
        // 6.按照布局模式重排更新tab
        if (currentTab.isAuto()) {
            Menu2Utils.autoLayout();
        } else if (currentTab.isLeft()) {
            Menu2Utils.leftLayout();
        } else {
            Menu2Utils.rightLayout();
        }
        controller.selectNode(newNode);
    }

    /**
     * 删除指定节点及其子树
     * 
     * @param node 待删除节点
     */
    public static void deleteNode(MapNode node) {
        // add:防止删中心节点
        if (node.isCenter()) {
            return;
        }
        // 1.获取当前tab和当前画布
        MapTab currentTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        AnchorPane currentPane = (AnchorPane) currentTab.getContent();
        // 2.从节点属性中获取父节点和关联线
        MapNode parent = node.getParentNode();
        ArrayList<MapNode> childNodes = (ArrayList<MapNode>) node.getChildNodes().clone();
        MapLine endWithLine = node.getEndWithLine();
        // 3.从tab和画布中移除连线与节点
        currentTab.getMapLines().remove(endWithLine);
        currentPane.getChildren().remove(endWithLine);
        parent.getChildNodes().remove(node);
        currentPane.getChildren().remove(node);
        // 4.更新树深度和递归删除子孙节点
        changeDeep(parent, -1 * node.getDeep());
        for (MapNode child : childNodes) {
            deleteNode(child);
        }
        // 5.调整父节点高度信息
        if (parent.getChildNodes().size() != 0) {
            changeHigh(parent, -1 * node.getHigh());
        }
        node.setParentNode(null);
    }

    /**
     * 递归更新节点深度统计
     * 
     * @param node 当前节点
     * @param d    深度变化量
     */
    public static void changeDeep(MapNode node, int d) {
        // add:递归由深及浅更新树深度统计
        node.setDeep(node.getDeep() + d);
        if (node.getParentNode() == null) {
            return;
        }
        changeDeep(node.getParentNode(), d);
    }

    /**
     * 递归增加节点及其子树Y坐标
     * 
     * @param node   当前节点
     * @param change 变化量
     */
    public static void addY(MapNode node, double change) {
        // add:递归增加所有子节点Y坐标
        node.getYProperty().set(node.getYProperty().get() + change);
        for (MapNode child : node.getChildNodes()) {
            addY(child, change);
        }
    }

    /**
     * 递归减少节点及其子树Y坐标
     * 
     * @param node   当前节点
     * @param change 变化量
     */
    public static void subY(MapNode node, double change) {
        // add:递归减少所有子节点Y坐标
        node.getYProperty().set(node.getYProperty().get() - change);
        for (MapNode child : node.getChildNodes()) {
            subY(child, change);
        }
    }

    /**
     * 递归增加节点及其子树X坐标
     * 
     * @param node   当前节点
     * @param change 变化量
     */
    public static void changeX(MapNode node, double change) {
        // add:递归增加所有子节点X坐标
        node.getXProperty().set(node.getXProperty().get() + change);
        for (MapNode child : node.getChildNodes()) {
            changeX(child, change);
        }
    }

    /**
     * 递归更新节点高度统计
     * 
     * @param node   当前节点
     * @param change 变化量
     */
    public static void changeHigh(MapNode node, double change) {
        // add:递归由深及浅增加高度信息
        node.setHigh(node.getHigh() + change);
        if (!node.isLeft()) {
            node.setRightHigh(node.getRightHigh() + change);
        }
        if (node.getParentNode() == null) {
            return;
        }
        changeHigh(node.getParentNode(), change);
    }

    /**
     * 递归调整兄弟节点Y坐标
     * 
     * @param node      当前节点
     * @param subChange 上移变化量
     * @param addChange 下移变化量
     * @param end       兄弟节点遍历结束下标
     */
    public static void adaptNodes(MapNode node, double subChange, double addChange, int end) {
        // add:有深及浅递归调整所有兄弟节点Y坐标
        if (node.getParentNode() == null) {
            return;
        }
        MapNode parent = node.getParentNode();
        ArrayList<MapNode> bros = parent.getChildNodes();
        for (int i = 0; i < end; i++) {
            MapNode bro = bros.get(i);
            if (bro.isLeft() != node.isLeft()) {
                continue;
            }
            if (bro.getYProperty().get() <= node.getYProperty().get() && !bro.equals(node)) {
                subY(bro, subChange);
            } else if (bro.getYProperty().get() > node.getYProperty().get()) {
                addY(bro, addChange);
            }
        }
        if (parent.isCenter()) {
            return;
        }
        adaptNodes(parent, subChange, addChange, parent.getParentNode().getChildNodes().size());
    }

    public static void setFxmlLoader(FXMLLoader fxmlLoader) {
        NodeUtils.fxmlLoader = fxmlLoader;
    }

    public static void setController(adaptiveController controller) {
        NodeUtils.controller = controller;
    }
}
