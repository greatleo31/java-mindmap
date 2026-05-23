package com.hyk.mindmap.service.layout;

import com.czj.mindmap.Controller.adaptiveController;
import com.hyk.mindmap.ui.MapNode;
import com.hyk.mindmap.ui.MapTab;
import com.hyk.mindmap.service.tree.TreeUtils;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class LayoutUtils {
    private static int id = 1;
    private static FXMLLoader fxmlLoader;
    private static adaptiveController controller;

    /**
     * 创建新的思维导图页签
     * 
     * @param tabPane 页签容器
     * @return 新建的页签
     */
    public static MapTab creatMap(TabPane tabPane) {
        // 1.新建一个MapTab
        MapTab newMap = new MapTab("思维导图" + (id++));
        // 2.新建一个画布
        AnchorPane anchorPane = new AnchorPane();
        // 3.将tab的中心节点放进画布
        anchorPane.getChildren().add(newMap.getCenter());
        // 4.将画布放进tab
        newMap.attachToPane(anchorPane);
        // 5.将tab放进tabPane并且选中当前tab
        tabPane.getTabs().add(newMap);
        tabPane.getSelectionModel().select(newMap);
        return newMap;
    }

    /**
     * 初始化右侧树的根节点
     * 
     * @param mindNode 中心节点
     * @param treeview 树视图
     */
    public static void creatNewTree(MapNode mindNode, TreeView treeview) {
        // 1.新建一个根TreeItem
        TreeItem root = new TreeItem(mindNode.getText());
        // 2.使TreeItem的值绑定mindNode的textProperty
        root.valueProperty().bind(mindNode.textProperty());
        // 3.将TreeItem设置给treeview的root
        treeview.setRoot(root);
        // 4.将TreeItem存进mindNode的treeNode
        mindNode.setTreeNode(root);
    }

    /**
     * 新增子节点时同步右侧树
     * 
     * @param mindNode 新增节点
     */
    public static void addTreeNode(MapNode mindNode) {
        // 1.新建一个TreeItem
        TreeItem child = new TreeItem(mindNode.getText());
        // 2.使TreeItem的值绑定mindNode的textProperty
        child.valueProperty().bind(mindNode.textProperty());
        // 3.将TreeItem添加到父节点的children中
        mindNode.getParentNode().getTreeNode().getChildren().add(child);
        // 4.将TreeItem存进mindNode的treeNode
        mindNode.setTreeNode(child);
    }

    /**
     * 监听tab切换并更新右侧树
     * 
     * @param tabpane  页签容器
     * @param treeView 树视图
     */
    public static void ListenTab(TabPane tabpane, TreeView treeView) {
        // 1.监听当前选中tab变化
        tabpane.getSelectionModel().selectedItemProperty().addListener(e -> {
            Tab tab = tabpane.getSelectionModel().getSelectedItem();
            if (tab != null) {
                // 2.遍历当前画布节点并找到中心节点同步树
                AnchorPane pane = (AnchorPane) tab.getContent();
                ObservableList<Node> nodes = pane.getChildren();
                for (Node node : nodes) {
                    if (node instanceof MapNode) {
                        MapNode mindNode = (MapNode) node;
                        if (mindNode.isCenter()) {
                            treeView.setRoot(mindNode.getTreeNode());
                            if (controller != null) {
                                controller.updateMapName(tab.getText());
                                controller.selectNode(null);
                            }
                        }
                    }
                }
            } else {
                // 3.无选中tab时清空树并重置状态
                treeView.setRoot(null);
                if (controller != null) {
                    controller.updateMapName("未命名");
                    controller.selectNode(null);
                }
            }
        });
    }

    /**
     * 按右侧方向重新布局
     */
    public static void rightLayout() {
        // 1.获取当前tab and 中心节点
        MapTab currentTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        MapNode center = currentTab.getCenter();
        center.setRightHigh(center.getHigh());
        ArrayList<MapNode> childNodes = center.getChildNodes();
        // 2.根据当前状态计算中心节点X坐标
        double x;
        if (currentTab.isAuto()) {
            x = center.getXProperty().get() - center.getOffset();
        } else if (center.isLeft()) {
            x = center.getXProperty().get() - 2 * center.getOffset();
        } else {
            x = center.getXProperty().get();
        }
        // 3.更新tab和中心节点的布局状态
        currentTab.setAuto(false);
        if (currentTab.isLeft()) {
            currentTab.setLeft(false);
        }
        center.setLeft(false);
        center.setXProperty(x);
        // 4.递归更新所有子节点布局
        for (int i = 0; i < childNodes.size(); i++) {
            showChildTree(center, childNodes.get(i), 0, i, false);
        }
    }

    /**
     * 按左侧方向重新布局
     */
    public static void leftLayout() {
        // 1.先按右布局完成基础排布
        rightLayout();
        MapTab currentTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        MapNode center = currentTab.getCenter();
        // 2.更新tab和中心节点的左布局状态
        currentTab.setLeft(true);
        currentTab.setAuto(false);
        center.setLeft(true);
        // 3.将所有子节点转换到左侧
        ArrayList<MapNode> childNodes = center.getChildNodes();
        for (MapNode childNode : childNodes) {
            right2left(childNode, center);
        }
        // 4.整体平移中心节点位置
        TreeUtils.changeX(center, 2 * center.getOffset());
    }

    /**
     * 自动分配左右布局
     */
    public static void autoLayout() {
        // 1.获取当前tab and 中心节点
        MapTab currentTab = (MapTab) controller.getTabPane().getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        MapNode center = currentTab.getCenter();
        // 2.根据当前状态计算中心节点X坐标
        double x;
        if (currentTab.isAuto()) {
            x = center.getXProperty().get() - center.getOffset();
        } else if (center.isLeft()) {
            x = center.getXProperty().get() - 2 * center.getOffset();
        } else {
            x = center.getXProperty().get();
        }
        // 3.更新自动布局状态
        currentTab.setAuto(true);
        currentTab.setLeft(false);
        center.setLeft(false);
        center.setXProperty(x);
        // 4.按高度将子节点分配到左右两侧
        ArrayList<MapNode> childNodes = center.getChildNodes();
        double leftHigh = 0;
        int i = 0;
        int rightBegin;
        for (i = 0; i < childNodes.size(); i++) {
            showChildTree(center, childNodes.get(i), 0, i, true);
            leftHigh += childNodes.get(i).getHigh();
            if (leftHigh >= center.getHigh() / 2) {
                break;
            }
        }
        center.setRightHigh(center.getHigh() - leftHigh);
        rightBegin = i + 1;
        for (i = i + 1; i < childNodes.size(); i++) {
            showChildTree(center, childNodes.get(i), rightBegin, i, false);
        }
        // 5.整体调整中心节点位置
        TreeUtils.changeX(center, center.getOffset());
    }

    /**
     * 递归展示并布局子树
     * 
     * @param node    父节点
     * @param newNode 当前子节点
     * @param begin   兄弟节点起始下标
     * @param end     当前节点下标
     * @param left    是否放在左侧
     */
    public static void showChildTree(MapNode node, MapNode newNode, int begin, int end, boolean left) {
        // 1.设置当前节点左右状态
        newNode.setLeft(left);
        if (left) {
            newNode.setRightHigh(0);
        }
        // 2.计算当前节点纵向位置
        ArrayList<MapNode> bros = node.getChildNodes();
        double curHigh = 0;
        for (int i = begin; i < end; i++) {
            curHigh += bros.get(i).getHigh();
        }
        newNode.setYProperty(node.getYProperty().get() + curHigh / 2);

        // 3.调整同侧兄弟节点的纵向位置
        for (int i = 0; i < end; i++) {
            MapNode bro = bros.get(i);
            if (bro.isLeft() != left) {
                continue;
            }
            if (bro.getYProperty().get() <= newNode.getYProperty().get()) {
                TreeUtils.subY(bro, newNode.prefHeightProperty().get() * 0.75);
            } else {
                TreeUtils.addY(bro, newNode.prefHeightProperty().get() * 0.75);
            }
        }
        // 4.计算当前节点横向位置
        if (left) {
            newNode.setXProperty(node.getXProperty().get() - newNode.prefWidthProperty().get() - 80);
        } else {
            newNode.setXProperty(node.getXProperty().get() + node.prefWidthProperty().get() + 80);
        }
        // 5.递归调整祖先层兄弟节点位置
        if (end >= 1) {
            int index;
            if (node.getParentNode() != null) {
                for (index = 0; index < node.getParentNode().getChildNodes().size(); index++) {
                    if (node.getParentNode().getChildNodes().get(index).equals(node)) {
                        break;
                    }
                }
            } else {
                index = 0;
            }
            TreeUtils.adaptNodes(node, newNode.prefHeightProperty().get() * 0.75,
                    newNode.prefHeightProperty().get() * 0.75, index);
        }

        // 6.递归布局当前节点的所有子节点
        ArrayList<MapNode> grandchildren = newNode.getChildNodes();
        for (int i = 0; i < grandchildren.size(); i++) {
            showChildTree(newNode, grandchildren.get(i), 0, i, left);
        }
    }

    /**
     * 将右侧节点转换为左侧布局
     * 
     * @param node   当前节点
     * @param center 中心节点
     */
    public static void right2left(MapNode node, MapNode center) {
        // 1.设置节点为左侧布局
        node.setLeft(true);
        // 2.按中心节点镜像计算横向位置
        node.setXProperty(center.getXProperty().get()
                - (node.getXProperty().get() - center.getXProperty().get() - center.prefWidthProperty().get())
                - node.prefWidthProperty().get());
        // 3.递归转换所有子节点
        for (MapNode childNode : node.getChildNodes()) {
            right2left(childNode, center);
        }
    }

    public static void setFxmlLoader(FXMLLoader fxmlLoader) {
        LayoutUtils.fxmlLoader = fxmlLoader;
    }

    public static void setController(adaptiveController controller) {
        LayoutUtils.controller = controller;
    }
}
