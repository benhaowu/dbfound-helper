package com.ben.df.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

/**
 * @author : wubenhao
 * @date : create in 2022/9/13
 */
public class CreateDbFoundAction extends CreateFileFromTemplateAction {

    public CreateDbFoundAction() {
        super("Dbfound Model File", "Creates dbFound model file", AllIcons.FileTypes.Xml);
    }

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle("New DbFound Model File")
                .addKind("Xml", AllIcons.FileTypes.Xml, "DbFound Model");
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, @NotNull String newName, String templateName) {
        return "Create DbFound Model File: " + newName;
    }
}
