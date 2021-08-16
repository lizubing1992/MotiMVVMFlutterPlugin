package com.moti;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MvvmFlutterPlugin extends AnAction {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:ss");

    public MvvmFlutterPlugin() {
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = (Editor) e.getData(PlatformDataKeys.EDITOR);
        if (editor != null) {
            Project project = (Project) e.getData(PlatformDataKeys.PROJECT);
            PsiFile psiFile = (PsiFile) e.getData(PlatformDataKeys.PSI_FILE);
            String fileName = psiFile.getName();
            String className = this.getClassName(fileName);
            if (className == null) {
                Messages.showMessageDialog("Create failed ,Can't found '_contract' in your file name," +
                                "your file name must contain '_contract'",
                        "Error", Messages.getErrorIcon());
            } else {
                String currentPath = this.getCurrentPath(e);
                String basePath = currentPath.replace(fileName, "");
                String basePackage = this.getPackageName(basePath, project.getName());
                String modelName = className.substring(0, className.indexOf("Contract"));
                String modelFileName = fileName.substring(0, fileName.indexOf("_contract"));
                String currentUser = System.getProperty("user.name");
                String contractContent = String.format("import 'package:flutter_mvvm/flutter_mvvm.dart';" +
                        "\n/// @desc TODO" +
                        "\n /// @time %s" +
                        "\n /// @author %s" +
                        "\nabstract class ViewModel implements ICommonViewModel {" +
                        "\n" +
                        "}" +
                        "\n" +
                        "\n" +
                        "abstract class Model implements ICommonModel {" +
                        "\n" +
                        "}" +
                        "\n" +
                        "", this.sdf.format(new Date()), currentUser);
                WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                    editor.getDocument().setText(contractContent);
                });

                try {
                    this.createPresenterClass(basePackage, basePath, modelName, modelFileName);
                    this.createModelClass(basePackage, basePath, modelName, modelFileName);
                    this.createWidgetClass(basePackage, basePath, modelName, modelFileName);
                } catch (IOException var15) {
                    Messages.showMessageDialog("create file failed", "Error", Messages.getErrorIcon());
                    return;
                }

                Messages.showMessageDialog("created success! please wait a moment", "Success", Messages.getInformationIcon());
                this.refreshProject(e);
            }
        }
    }

    private String getClassName(String content) {
        if (!content.contains("_contract")) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            String[] words = content.split("_");
            String[] var4 = words;
            int var5 = words.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String word = var4[var6];
                sb.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            }

            return sb.toString();
        }
    }

    private void refreshProject(AnActionEvent e) {
        e.getProject().getBaseDir().refresh(false, true);
    }

    private void createModelClass(String basePackage, String path, String modelName, String fileName) throws IOException {
        String filePath = path + fileName + "_model.dart";
        File dirs = new File(path);
        File file = new File(filePath);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        String currentUser = System.getProperty("user.name");
        String content = String.format("import 'package:flutter_mvvm/flutter_mvvm.dart';" +
                "\nimport 'package:moti_shop_app/common/http/http_manager.dart';" +
                "\nimport 'package:%s/%s_contract.dart';" +
                "\n\n\n/// @desc TODO" +
                "\n /// @time %s" +
                "\n /// @author %s" +
                "\n class %sModel extends CommonModel implements Model {" +
                "\n   @override" +
                "\n   void dispose(){" +
                "\n        HttpManager().cancel(tag);" +
                "\n         " +
                "}\n}", basePackage, fileName, this.sdf.format(new Date()), currentUser, modelName);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private void createPresenterClass(String basePackage, String path, String modelName, String fileName) throws IOException {
        String filePath = path + fileName + "_viewmodel.dart";
        File dirs = new File(path);
        File file = new File(filePath);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        String currentUser = System.getProperty("user.name");
        String content = String.format("import 'package:flutter_mvvm/flutter_mvvm.dart';" +
                "\nimport 'package:%s/%s_contract.dart';" +
                "\nimport 'package:%s/%s_model.dart';" +
                "\n\n\n/// @desc TODO" +
                "\n /// @time %s" +
                "\n /// @author %s" +
                "\n class %sViewModel extends CommonViewModel<Model> implements ViewModel {" +
                "\n\n   @override" +
                "\n   Model createModel(){" +
                "\n       return %sModel();" +
                "\n   }" +
                "\n}", basePackage, fileName, basePackage, fileName, this.sdf.format(new Date()), currentUser, modelName, modelName);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private void createWidgetClass(String basePackage, String path, String modelName, String fileName) throws IOException {
        String filePath = path + fileName + ".dart";
        File dirs = new File(path);
        File file = new File(filePath);
        if (!dirs.exists()) {
            dirs.mkdirs();
        }

        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        String currentUser = System.getProperty("user.name");
        String content = String.format("import 'package:flutter/material.dart';" +
                        "\nimport 'package:moti_shop_app/common/base/dh_widget.dart';" +
                        "\nimport 'package:moti_shop_app/common/router/mt_router.dart';" +
                        "\nimport 'package:%s/%s_viewmodel.dart';" +
                        "\n\n\n/// @desc TODO" +
                        "\n /// @time %s" +
                        "\n /// @author %s" +
                        "\n class %s extends DHWidget{" +
                        "\n static const String router = MTRouter.scheme + \"%s\";" +
                        "\n\n   %s({Object? arguments}) : super(arguments: arguments,routerName: router);" +
                        "\n\n   @override\n   DHWidgetState getState(){" +
                        "\n       return  _%sState();" +
                        "\n   }" +
                        "\n}" +
                        "\nclass _%sState extends DHWidgetState<%s,%sViewModel> {" +
                        "\n @override\n" +
                        "  void initState() {\n" +
                        "    super.initState();\n" +
                        "    setAppbar(\n" +
                        "        title: \"\");\n" +
                        "  }" +
                        "\n\n   @override" +
                        "\n   Widget buildWidget(BuildContext context,%sViewModel viewModel){" +
                        "\n       //TODO: implement buildWidget " +
                        "\n       return  Container();" +
                        "\n   }" +
                        "\n\n @override\n" +
                        "  createViewModel() {\n" +
                        "    return %sViewModel();\n" +
                        "  }" +
                        "\n\n   @override" +
                        "\n   void queryData(%sViewModel viewModel){" +
                        "\n       //TODO: implement queryData " +
                        "\n   }" +
                        "\n}", basePackage, fileName, this.sdf.format(new Date()), currentUser, modelName,
                modelName, modelName, modelName, modelName, modelName, modelName, modelName, modelName, modelName);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    private String getPackageName(String path, String projectName) {
        String[] strings = path.split("/");
        StringBuilder packageName = new StringBuilder(projectName);
        packageName.append("/");
        boolean packageBegin = false;

        for (int i = 0; i < strings.length; ++i) {
            String string = strings[i];
            if (string.equals("lib")) {
                packageBegin = true;
            } else if (packageBegin) {
                packageName.append(string);
                packageName.append("/");
            }
        }

        if (packageName.toString().endsWith("/")) {
            packageName.deleteCharAt(packageName.length() - 1);
        }

        return packageName.toString().replace("-", "_");
    }

    private String getCurrentPath(AnActionEvent e) {
        VirtualFile currentFile = (VirtualFile) PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        return currentFile != null ? currentFile.getPath() : null;
    }
}
