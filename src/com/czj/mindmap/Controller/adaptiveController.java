/* ============================================================
 * 包: com.czj.mindmap.Controller
 * 职责: 本项目的主控制器，负责将所有菜单操作、鼠标交互、
 *       节点选择、布局切换等 UI 事件绑定到对应的业务逻辑。
 *       遵循 MVC 中的 Controller 角色，视图由 FXML 定义，
 *       模型由 MapNode / MapTab（树结构）和工具类提供。
 * ============================================================ */
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
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.hyk.mindmap.ui.MapNode;
import com.hyk.mindmap.ui.MapTab;
import com.hyk.mindmap.service.file.FileUtils;
import com.hyk.mindmap.service.layout.LayoutUtils;
import com.hyk.mindmap.service.tree.TreeUtils;

/**
 * adaptiveController —— 主控制器
 *
 * 实现 Initializable 接口，在 FXML 加载完成后（initialize 方法）
 * 完成所有交互逻辑的初始化。
 *
 * 核心职责:
 *   1. 初始化第一个导图页签（Tab）
 *   2. 为所有菜单项绑定事件回调
 *   3. 管理节点的选中/取消选中状态（高亮）
 *   4. 为 MapTab 注册缩放、取消选择等视口交互
 *   5. 提供工具类所需的回调接口（showStatus、updateMapName 等）
 */
public class adaptiveController implements Initializable {

    // ════════════════════════════════════════════════════════════════
    //  FXML 注入的 UI 组件（名称必须与 scene.fxml 中 fx:id 一一对应）
    // ════════════════════════════════════════════════════════════════

    @FXML private Pane pane;             // 根容器（AnchorPane）
    @FXML private MenuBar menu2;         // 主菜单栏
    @FXML private TabPane tabPane;       // 左侧的页签面板，每页一张导图
    @FXML private TreeView treeview;     // 右侧树形视图（导图的大纲）

    // — 菜单项 —
    @FXML private Menu newMap;           // "编辑" 菜单（含新建导图等）
    @FXML private Menu deleteNode;       // "删除节点" 菜单

    @FXML private MenuItem rightLayout;  // 右侧布局菜单项
    @FXML private MenuItem leftLayout;   // 左侧布局菜单项
    @FXML private MenuItem autoLayout;   // 自动布局菜单项
    @FXML private MenuItem create;       // "新建思维导图" 菜单项
    @FXML private MenuItem open;         // "打开" 菜单项
    @FXML private MenuItem save;         // "保存" 菜单项
    @FXML private MenuItem saveAs;       // "另存为" 菜单项
    @FXML private MenuItem newChild;     // "添加子节点" 菜单项
    @FXML private MenuItem newBro;       // "添加兄弟节点" 菜单项
    @FXML private MenuItem outputJPG;    // "导出为 JPG" 菜单项
    @FXML private MenuItem outputPNG;    // "导出为 PNG" 菜单项

    // — 状态标签 —
    @FXML private Label delete;          // 删除节点按钮的 Label（被放在 Menu 的 graphic 中）
    @FXML private Label mapNameLabel;    // 左上角显示 "当前导图：xxx"
    @FXML private Label statusLabel;     // 右上角显示状态信息

    // ════════════════════════════════════════════════════════════════
    //  内部状态
    // ════════════════════════════════════════════════════════════════

    private Stage stage;                // 主窗口引用（由 Main 注入）
    private MapNode selectedNode;       // 当前选中节点（高亮管理）

    // ════════════════════════════════════════════════════════════════
    //  初始化
    // ════════════════════════════════════════════════════════════════

    /**
     * FXML 加载完成后自动调用。在这里挂接所有交互逻辑。
     *
     * 初始化顺序:
     *   1. 将 TabPane 与 TreeView 绑定监听（切换 Tab 时同步更新 TreeView）
     *   2. 创建第一个默认导图页签
     *   3. 为该导图注册交互（点击选择、Ctrl+滚轮缩放等）
     *   4. 为该导图的中心节点创建对应的 TreeView 树
     *   5. 更新界面状态标签
     *   6. 为各个菜单项绑定事件处理器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ── 绑定 TabPane 切换监听，使右侧 TreeView 跟随当前 Tab ──
        LayoutUtils.ListenTab(tabPane, treeview);

        // ── 创建第一个导图页签 ────────────────────────────────────
        MapTab firstTab = LayoutUtils.creatMap(tabPane);
        registerMap(firstTab);                            // 注册交互事件
        LayoutUtils.creatNewTree(firstTab.getCenter(), treeview); // 创建 TreeView 树
        updateMapName(firstTab.getText());                // 显示导图名称
        showStatus("请选择节点后进行编辑");

        // ── 为各菜单项注册事件 ────────────────────────────────────

        // 「新建思维导图」: 创建一个新 Tab 并注册交互
        create.setOnAction(event -> {
            MapTab newTab = LayoutUtils.creatMap(tabPane);
            registerMap(newTab);
            LayoutUtils.creatNewTree(newTab.getCenter(), treeview);
            selectNode(null);                    // 清除选中状态
            updateMapName(newTab.getText());
            showStatus("已新建思维导图");
        });

        // 「添加子节点」: 在当前选中节点下新建一个子节点
        newChild.setOnAction(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            TreeUtils.newChildNode(node);        // 委托 TreeUtils 完成树操作
            showStatus("已添加子节点");
        });

        // 「添加兄弟节点」: 在当前选中节点的父节点下新建一个同级节点
        newBro.setOnAction(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            if (node.isCenter()) {
                // 中心节点没有「兄弟」概念，禁止操作
                showStatus("中心节点不能添加兄弟节点，只能添加子节点");
                return;
            }
            // 「兄弟」= 在父节点下添加一个新节点
            TreeUtils.newChildNode(node.getParentNode());
            showStatus("已添加兄弟节点");
        });

        // 「删除节点」: 移除选中的节点及其子树
        delete.setOnMouseClicked(event -> {
            MapNode node = getSelectedNode();
            if (node == null) {
                showStatus("请先选中一个节点");
                return;
            }
            if (node.isCenter()) {
                showStatus("中心节点不能删除");  // 中心节点是导图根，不可删
                return;
            }

            // 同步删除 TreeView 中的对应条目
            TreeItem parent = node.getTreeNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(node.getTreeNode());
            }

            TreeUtils.deleteNode(node);           // 删除树中的节点及连线
            selectNode(null);                     // 清空选中
            relayoutCurrentTab();                 // 删除后重新布局
            showStatus("节点已删除");
        });

        // 「右侧布局」: 所有子节点水平排列在中心节点右侧
        rightLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.rightLayout();
                showStatus("已切换为右侧布局");
            }
        });

        // 「左侧布局」: 所有子节点水平排列在中心节点左侧
        leftLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.leftLayout();
                showStatus("已切换为左侧布局");
            }
        });

        // 「自动布局」: 左右交替排列（类传统思维导图风格）
        autoLayout.setOnAction(event -> {
            if (selectedCenterForLayout()) {
                LayoutUtils.autoLayout();
                showStatus("已切换为自动布局");
            }
        });

        // 「保存」: 覆盖保存到原文件
        save.setOnAction(event -> {
            try {
                MapTab current = getCurrentTab();
                FileUtils.storeMap(current);       // 委托 FileUtils 序列化
                if (current != null) {
                    updateMapName(current.getText());
                    showStatus("保存成功");
                }
            } catch (IOException e) {
                showStatus("保存失败：" + e.getMessage());
                e.printStackTrace();
            }
        });

        // 「另存为」: 弹出文件对话框选择新路径保存
        saveAs.setOnAction(event -> {
            try {
                MapTab current = getCurrentTab();
                FileUtils.storeMapAt(current);     // 委托 FileUtils 另存
                if (current != null) {
                    updateMapName(current.getText());
                    showStatus("另存为成功");
                }
            } catch (IOException e) {
                showStatus("另存为失败：" + e.getMessage());
                e.printStackTrace();
            }
        });

        // 导出图片: 将当前画布内容截图输出
        outputJPG.setOnAction(event -> FileUtils.outputJPG(stage));
        outputPNG.setOnAction(event -> FileUtils.outputPNG(stage));
    }

    // ════════════════════════════════════════════════════════════════
    //  MapTab 交互注册
    // ════════════════════════════════════════════════════════════════

    /**
     * 为一个 MapTab 注册全局交互:
     *   - 递归为该 Tab 中所有节点绑定点击选中事件
     *   - 视口空白区域点击 → 取消选中
     *   - 按住 Ctrl + 滚轮 → 以鼠标位置为锚点缩放画布
     *
     * @param tab 需要注册交互的导图页签
     */
    public void registerMap(MapTab tab) {
        if (tab == null || tab.getCenter() == null) {
            return;
        }

        // 递归为树中所有节点绑定点击事件
        registerNodeTree(tab.getCenter());

        // 获取视口和画布引用
        AnchorPane viewportPane = tab.getViewportPane();
        AnchorPane canvasPane = tab.getCanvasPane();

        // 点击视口空白区域 → 取消当前选中
        viewportPane.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            // 只有点击在 viewportPane 或 canvasPane 本身（不是节点）时才取消选中
            if (target == viewportPane || target == canvasPane) {
                selectNode(null);
            }
        });

        // Ctrl + 滚轮缩放（使用事件过滤器，在事件冒泡前拦截）
        viewportPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown()) {
                return;  // 没有按住 Ctrl 则不处理
            }
            // 以鼠标当前位置为锚点缩放
            tab.zoomAt(event.getSceneX(), event.getSceneY(), event.getDeltaY());
            event.consume();  // 阻止事件继续传播
        });
    }

    /**
     * 递归遍历 MapNode 树，为每个节点绑定鼠标点击选中事件
     *
     * @param node 当前节点
     */
    private void registerNodeTree(MapNode node) {
        registerNode(node);
        for (MapNode child : node.getChildNodes()) {
            registerNodeTree(child);  // 递归处理子节点
        }
    }

    /**
     * 为单个 MapNode 绑定鼠标点击 → 选中该节点
     *
     * @param node 需要绑定的节点
     */
    public void registerNode(MapNode node) {
        node.setOnMouseClicked(event -> {
            selectNode(node);
            event.consume();  // 防止事件向上传播到视口
        });
    }

    // ════════════════════════════════════════════════════════════════
    //  节点选择管理
    // ════════════════════════════════════════════════════════════════

    /**
     * 选中一个节点（高亮），并取消之前节点的选中状态。
     * 使用 CSS 样式类 "MindNodeSelected" 实现视觉反馈。
     *
     * 设计要点:
     *   - 通过增删 styleClass 来实现高亮切换，
     *     避免直接操作节点样式，保持与 CSS 主题解耦。
     *   - selectedNode 为空表示当前没有选中任何节点。
     *
     * @param node 要选中的节点，传 null 表示取消所有选中
     */
    public void selectNode(MapNode node) {
        // 先移除旧节点的选中样式
        if (selectedNode != null) {
            selectedNode.getStyleClass().remove("MindNodeSelected");
        }
        selectedNode = node;

        // 为新节点添加选中样式（避免重复添加）
        if (selectedNode != null && !selectedNode.getStyleClass().contains("MindNodeSelected")) {
            selectedNode.getStyleClass().add("MindNodeSelected");
        }

        // 更新状态栏
        if (node == null) {
            showStatus("未选中节点");
        } else {
            showStatus("已选中节点：" + node.getText());
        }
    }

    /**
     * 获取当前选中的节点
     * @return 当前选中节点，可能是 null
     */
    public MapNode getSelectedNode() {
        return selectedNode;
    }

    // ════════════════════════════════════════════════════════════════
    //  UI 状态更新
    // ════════════════════════════════════════════════════════════════

    /**
     * 更新左上角的导图名称标签
     * @param name 新导图名称（如文件名或 "未命名"）
     */
    public void updateMapName(String name) {
        if (mapNameLabel != null) {
            mapNameLabel.setText("当前导图：" + name);
        }
    }

    /**
     * 更新右上角的状态信息栏
     * @param message 状态文本
     */
    public void showStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  布局辅助
    // ════════════════════════════════════════════════════════════════

    /**
     * 检查是否选中了中心节点（只有中心节点才能切换布局）
     * @return true 如果当前选中了中心节点
     */
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

    /**
     * 根据当前 Tab 记录的布局类型，调用对应的布局方法重新排列。
     * 常用于删除节点后自动刷新布局。
     */
    private void relayoutCurrentTab() {
        MapTab currentTab = getCurrentTab();
        if (currentTab == null) {
            return;
        }
        // MapTab 内部维护了布局标志位（isAuto / isLeft / isRight）
        if (currentTab.isAuto()) {
            LayoutUtils.autoLayout();
        } else if (currentTab.isLeft()) {
            LayoutUtils.leftLayout();
        } else {
            // 默认走右侧布局
            LayoutUtils.rightLayout();
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  工具方法
    // ════════════════════════════════════════════════════════════════

    /**
     * 获取当前选中的 Tab（当前可见的导图）
     * @return 当前 MapTab 对象
     */
    private MapTab getCurrentTab() {
        return (MapTab) tabPane.getSelectionModel().getSelectedItem();
    }

    // ════════════════════════════════════════════════════════════════
    //  Getter / Setter（供外部工具类或 FXML 使用）
    // ════════════════════════════════════════════════════════════════

    public TabPane getTabPane()   { return tabPane; }
    public MenuItem getOpen()     { return open; }
    public TreeView getTreeview() { return treeview; }
    public void setTreeview(TreeView treeview) { this.treeview = treeview; }

    public Stage getStage()       { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
}
