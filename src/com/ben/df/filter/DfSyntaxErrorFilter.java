package com.ben.df.filter;

import com.ben.df.common.Constant;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.sql.dialects.mysql.MysqlDialect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DfSyntaxErrorFilter extends HighlightErrorFilter {

    @Override
    public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement errorElement) {

        final PsiFile sqlFile = errorElement.getContainingFile();

        if (sqlFile == null || sqlFile.getViewProvider().getBaseLanguage() != MysqlDialect.INSTANCE
                && MysqlDialect.INSTANCE != errorElement.getLanguage()) return true;

        if (!(sqlFile.getOriginalFile().getVirtualFile() instanceof VirtualFileWindow)) return true;

        PsiFile psiFile =
                PsiManager
                        .getInstance(sqlFile.getProject())
                        .findFile((
                                (VirtualFileWindow) sqlFile
                                        .getOriginalFile()
                                        .getVirtualFile()).getDelegate());

        if (!(psiFile instanceof XmlFile) || ((XmlFile) psiFile).getRootTag() == null) return true;

        if ((!Objects.requireNonNull(((XmlFile) psiFile).getRootTag()).getNamespace().equals(Constant.DbFound.MODEL_NAMESPACE))) return true;

        return !skip(errorElement, sqlFile);
    }

    private static boolean skip(@NotNull PsiErrorElement element, PsiFile sqlFile) {

        if (element.getErrorDescription().contains("expected, got")) return true;

        PsiElement psiElement = sqlFile.findElementAt(element.getTextOffset());

        if (psiElement!=null) {

            if (psiElement instanceof PsiWhiteSpace) return true;

            return psiElement.getText().matches("（=|#|>|<）");

        }
        return false;
    }
}
