package com.ben.df.filter;

import com.ben.df.common.Constant;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.sql.psi.SqlFile;
import com.intellij.sql.psi.impl.SqlParameterImpl;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class SqlRainbowVisitor implements HighlightVisitor {

    private static final HighlightSeverity INFO = new HighlightSeverity("INFO", 200);
    private static final TextAttributesKey DF_KEYWORD = TextAttributesKey.createTextAttributesKey("DF_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    private static final HighlightInfoType DF_KEY_INFO = new HighlightInfoType.HighlightInfoTypeImpl(INFO, DF_KEYWORD);

    private HighlightInfoHolder myHolder;

    @Override
    public boolean suitableForFile(@NotNull PsiFile psiFile) {
        return psiFile instanceof SqlFile;
    }

    @Override
    public void visit(@NotNull PsiElement psiElement) {
        if (psiElement instanceof SqlParameterImpl) {
            if (Stream.of(Constant.DbFound.WHERE_CLAUSE,
                    Constant.DbFound.AND_CLAUSE,
                    Constant.DbFound.BATCH_TEMPLATE_BEGIN,
                    Constant.DbFound.BATCH_TEMPLATE_END).anyMatch(e -> psiElement.getText().equals(e))) {
                addInfo(psiElement);
            }
        }
    }

    @Override
    public boolean analyze(@NotNull PsiFile file,
                           boolean updateWholeFile,
                           @NotNull HighlightInfoHolder holder,
                           @NotNull Runnable action) {
        myHolder = holder;
        try {
            action.run();
        }
        finally {
            myHolder = null;
        }
        return true;
    }

    @NotNull
    @Override
    public HighlightVisitor clone() {
        return new SqlRainbowVisitor();
    }

    private void addInfo(PsiElement psiElement) {
        myHolder.add(HighlightInfo.newHighlightInfo(DF_KEY_INFO).range(psiElement).create());
    }

}
