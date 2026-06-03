/* ============================================================
 * 包: com.czj.mindmap
 * 职责: 本包属于 CZJ 组（UI/控制层），负责程序入口、界面布局
 *       及用户交互控制。业务逻辑（树操作、布局算法、文件IO）
 *       委托给 com.hyk.mindmap.service 下的工具类处理。
 * ============================================================ */
package com.czj.mindmap;

import com.czj.mindmap.Controller.adaptiveController;
import com.hyk.mindmap.service.file.FileUtils;
import com.hyk.mindmap.service.layout.LayoutUtils;
import com.hyk.mindmap.service.tree.TreeUtils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main —— 程序入口类
 *
 * 继承 javafx.application.Application，是整个思维导图编辑器的启动点。
 * JavaFX 应用的固定生命周期: main() → launch() → init() → start() → 用户交互 → stop()。
 *
 * 职责:
 *   1. 加载 FXML 布局文件 scene.fxml
 *   2. 加载 CSS 主题样式
 *   3. 将 Stage（窗口）引用分发给各个工具类，使工具类能操作文件对话框等
 *   4. 将 Controller 引用注入 TreeUtils / LayoutUtils / FileUtils，
 *      使这些工具类能回调 controller 的方法（如 selectNode、showStatus）
 *   5. 配置「打开文件」菜单项的逻辑
 */
public class Main extends Application {

    /**
     * 程序入口，由 JVM 调用
     * @param args 命令行参数（本项目未使用）
     */
    public static void main(String[] args) {
        /*
         * 主界面布局的简要结构（对应 scene.fxml）:
         *   AnchorPane (root)
         *     ├── Label "当前导图：xxx"          (左上角)
         *     ├── Label "请选择节点后进行编辑"   (右上角)
         *     ├── MenuBar (菜单栏)
         *     │     ├── 文件 (保存/另存为/打开)
         *     │     ├── 导出 (JPG/PNG)
         *     │     ├── 编辑 (新建导图/添加子节点/添加兄弟节点)
         *     │     ├── 布局方式 (左/自动/右)
         *     │     └── 删除节点
         *     ├── TabPane (左侧大区域) — 每个 Tab 一张思维导图
         *     └── TreeView (右侧 160px 侧边栏) — 树形轮廓视图
         */
        launch(args);  // JavaFX 启动方法，自动创建 Stage 并调用 start()
    }

    /**
     * JavaFX 应用启动后的回调方法
     *
     * @param stage 主窗口对象，由 JavaFX 框架自动创建
     * @throws Exception FXML 加载或资源读取异常
     */
    @Override
    public void start(Stage stage) throws Exception {
        // ── 1. 加载 FXML 布局 ─────────────────────────────────
        FXMLLoader fxmlLoader = new FXMLLoader();
        // scene.fxml 与 Main.class 在同一个包目录下（com/czj/mindmap/）
        fxmlLoader.setLocation(getClass().getResource("scene.fxml"));
        Pane root = fxmlLoader.load();      // 解析 FXML 得到根容器 AnchorPane
        Scene scene = new Scene(root);      // 将根容器挂到 Scene 上

        // ── 2. 加载自定义 CSS 样式表 ──────────────────────────
        // 使用绝对路径（以 / 开头表示从 classpath 根开始查找）
        String cssPath = getClass().getResource("/com/czj/mindmap/Css/MindMap.css").toString();
        scene.getStylesheets().add(cssPath);

        // ── 3. 设置窗口图标与标题 ─────────────────────────────
        Image image = new Image(getClass().getResourceAsStream("/com/czj/mindmap/image/image.png"));
        stage.setTitle("轻量思维导图绘制工具");
        stage.getIcons().add(image);

        // ── 4. 依赖注入: 向外层工具类分发 Stage 和 Controller ──
        // FileUtils 需要 Stage 引用来弹出文件保存对话框
        FileUtils.stage = stage;

        // 从 FXML 加载器中取出 Controller 实例
        adaptiveController controller = fxmlLoader.getController();
        controller.setStage(stage);         // 同时也让 controller 持有 stage

        // 将 fxmlLoader 和 controller 注入三个工具类，
        // 以便工具类能:
        //   - 通过 fxmlLoader 加载新的 FXML 片段（如 MapTab、MapNode）
        //   - 通过 controller 回调 UI 更新方法（showStatus、selectNode 等）
        TreeUtils.setFxmlLoader(fxmlLoader);
        TreeUtils.setController(controller);
        LayoutUtils.setFxmlLoader(fxmlLoader);
        LayoutUtils.setController(controller);
        FileUtils.setFxmlLoader(fxmlLoader);
        FileUtils.setController(controller);

        // ── 5. 配置「打开文件」菜单项 ─────────────────────────
        // 从 controller 获取 open MenuItem 的引用，注册事件
        controller.getOpen().setOnAction(event -> {
            // 默认存储目录: 项目根下的 storage/ 文件夹
            File storage = new File("storage");
            if (!storage.exists()) {
                storage.mkdirs();  // 自动创建目录
            }

            // 创建系统文件选择对话框
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(storage);
            // 设置文件类型过滤器，仅显示 .mindmap 文件
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("思维导图文件", "*.mindmap"),
                    new FileChooser.ExtensionFilter("所有文件", "*.*"));

            // 弹出对话框让用户选择文件
            File targetFile = fileChooser.showOpenDialog(stage);
            // 委托 FileUtils 完成反序列化和加载
            FileUtils.openMap(targetFile);
        });

        // ── 6. 设置窗口属性并显示 ─────────────────────────────
        stage.setScene(scene);
        stage.setMinWidth(900);     // 最小宽度，防止界面被压缩
        stage.setMinHeight(650);    // 最小高度
        stage.setMaximized(true);   // 启动时默认最大化
        stage.show();
    }
}
