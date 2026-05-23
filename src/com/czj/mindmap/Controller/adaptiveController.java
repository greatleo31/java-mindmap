package com.czj.mindmap.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.hyk.mindmap.ui.MapNode;
import com.hyk.mindmap.ui.MapTab;
import com.hyk.mindmap.service.file.FileUtils;
import com.hyk.mindmap.service.layout.LayoutUtils;
import com.hyk.mindmap.service.tree.TreeUtils;

public class adaptiveController implements Initializable {

    @FXML
    private Pane pane;
    @FXML
    private MenuBar menu2;
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeView treeview;
    @FXML
    private Menu newMap;
    @FXML
    private Menu deleteNode;
    @FXML
    private MenuItem rightLayout;
    @FXML
    private MenuItem leftLayout;
    @FXML
    private MenuItem autoLayout;
    @FXML
    private MenuItem create;
    @FXML
    private Label delete;
    @FXML
    private MenuItem open;
    @FXML
    private MenuItem save;
    @FXML
    private MenuItem saveAs;
    @FXML
    private MenuItem newChild;
    @FXML
    private MenuItem newBro;
    @FXML
    private MenuItem outputJPG;
    @FXML
    private MenuItem outputPNG;
    @FXML
    private Label mapNameLabel;
    @FXML
    private Label statusLabel;

    private Stage stage;
    private MapNode selectedNode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LayoutUtils.ListenTab(tabPane, treeview);
        MapTab firstTab = LayoutUtils.creatMap(tabPane);
        registerMap(firstTab);
        LayoutUtils.creatNewTree(firstTab.getCenter(), treeview);
        updateMapName(firstTab.getText());
        showStatus("请选择节点后进行编辑");

        create.setOnAction(event -> {
            MapTab newTab = LayoutUtils.creatMap(tabPane);
            registerMap(newTab);
            LayoutUtils.creatNewTree(newTab.getCenter(), treeview);
            selectNode(null);
            updateMapName(newTab.getText());
            showStatus("已新建思维导图");
        });

        newChild.setOnAction(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            TreeUtils.newChildNode(node);
            showStatus("已添加子节点");
        });

        newBro.setOnAction(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            if (node.isCenter()) {
                showStatus("中心节点不能添加兄弟节点，只能添加子节点");
                return;
            }
            TreeUtils.newChildNode(node.getParentNode());
            showStatus("已添加兄弟节点");
        });

        delete.setOnMouseClicked(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            if (node.isCenter()) {
                showStatus("中心节点不能删除");
                return;
            }
            TreeItem parent = node.getTreeNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(node.getTreeNode());
            }
            TreeUtils.deleteNode(node);
            selectNode(null);
            relayoutCurrentTab();
            showStatus("节点已删除");
        });

        rightLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.rightLayout();
                showStatus("已切换为右侧布局");
            }
        });

        leftLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.leftLayout();
                showStatus("已切换为左侧布局");
            }
        });

        autoLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.autoLayout();
                showStatus("已切换为自动布局");
            }
        });

        save.setOnAction(event -> {
            try {
                MapTab current = getCurrentTab();
                FileUtils.storeMap(current);
                if (current != null) {
                    updateMapName(current.getText());
                    showStatus("保存成功");
                }
            } catch (IOException e) {
                showStatus("保存失败：" + e.getMessage());
                e.printStackTrace();
            }
        });

        saveAs.setOnAction(event -> {
            try {
                MapTab current = getCurrentTab();
                FileUtils.storeMapAt(current);
                if (current != null) {
                    updateMapName(current.getText());
                    showStatus("另存为成功");
                }
            } catch (IOException e) {
                showStatus("另存为失败：" + e.getMessage());
                e.printStackTrace();
            }
        });

        outputJPG.setOnAction(event -> FileUtils.outputJPG(stage));
        outputPNG.setOnAction(event -> FileUtils.outputPNG(stage));
    }

    public void registerMap(MapTab tab) {
        if (tab == null || tab.getCenter() == null) {
            return;
        }
        registerNodeTree(tab.getCenter());
        if (tab.getContent() instanceof AnchorPane) {
            AnchorPane content = (AnchorPane) tab.getContent();
            content.setOnMouseClicked(event -> {
                if (event.getTarget() == content) {
                    selectNode(null);
                }
            });
        }
    }

    private void registerNodeTree(MapNode node) {
        registerNode(node);
        for (MapNode child : node.getChildNodes()) {
            registerNodeTree(child);
        }
    }

    public void registerNode(MapNode node) {
        node.setOnMouseClicked(event -> {
            selectNode(node);
            event.consume();
        });
    }

    public void selectNode(MapNode node) {
        if (selectedNode != null) {
            selectedNode.getStyleClass().remove("MindNodeSelected");
        }
        selectedNode = node;
        if (selectedNode != null && !selectedNode.getStyleClass().contains("MindNodeSelected")) {
            selectedNode.getStyleClass().add("MindNodeSelected");
        }
        if (node == null) {
            showStatus("未选中节点");
        } else {
            showStatus("已选中节点：" + node.getText());
        }
    }

    public MapNode getSelectedNode() {
        return selectedNode;
    }

    public void updateMapName(String name) {
        if (mapNameLabel != null) {
            mapNameLabel.setText("当前导图：" + name);
        }
    }

    public void showStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private boolean selectedCenterForLayout() {
        if (selectedNode == null) {
            showStatus("请先选中中心节点再设置布局");
            return false;
        }
        if (!selectedNode.isCenter()) {
            showStatus("布局设置只对中心节点开放");
            return false;
        }
        return true;
    }

    private void relayoutCurrentTab() {
        MapTab currentTab = getCurrentTab();
        if (currentTab == null) {
            return;
        }
        if (currentTab.isAuto()) {
            LayoutUtils.autoLayout();
        } else if (currentTab.isLeft()) {
            LayoutUtils.leftLayout();
        } else {
            LayoutUtils.rightLayout();
        }
    }

    private MapTab getCurrentTab() {
        return (MapTab) tabPane.getSelectionModel().getSelectedItem();
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public MenuItem getOpen() {
        return open;
    }

    public TreeView getTreeview() {
        return treeview;
    }

    public void setTreeview(TreeView treeview) {
        this.treeview = treeview;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
