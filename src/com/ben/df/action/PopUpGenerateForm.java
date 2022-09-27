package com.ben.df.action;

import com.ben.df.ui.XmlGenerateUi;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author : wubenhao
 * @date : create in 2022/9/1
 */
public class PopUpGenerateForm extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile virtualDirectory = event.getData(CommonDataKeys.VIRTUAL_FILE);
        XmlGenerateUi xmlGenerateUi = new XmlGenerateUi(project,virtualDirectory);
        xmlGenerateUi.show();
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) return ;
        this.getTemplatePresentation().setVisible(virtualFile.isDirectory());
    }
}
