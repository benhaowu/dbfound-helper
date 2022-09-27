package com.ben.df.contributor;

import com.ben.df.common.Constant;
import com.ben.df.util.DomUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.sql.psi.patterns.SqlPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author : wubenhao
 * @date : create in 2022/9/13
 */
public class ModelCompletionContributor extends CompletionContributor {

    public ModelCompletionContributor() {
        extend(CompletionType.BASIC, psiElement().inside(SqlPatterns.sqlElement()), new CompletionProvider<CompletionParameters>() {
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext processingContext,
                                          @NotNull CompletionResultSet result) {
                final PsiElement position = parameters.getPosition();
                Editor editor = parameters.getEditor();
                Project project = editor.getProject();
                if (project == null) {
                    return;
                }
                InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
                PsiFile topLevelFile = injectedLanguageManager.getTopLevelFile(position);

                if (!DomUtils.isDbFoundFile(topLevelFile)) return;

                result.addElement(LookupElementBuilder.create(Constant.DbFound.WHERE_CLAUSE).withCaseSensitivity(true));
                result.addElement(LookupElementBuilder.create(Constant.DbFound.AND_CLAUSE).withCaseSensitivity(true));
                result.addElement(LookupElementBuilder.create(Constant.DbFound.BATCH_TEMPLATE_BEGIN).withCaseSensitivity(true));
                result.addElement(LookupElementBuilder.create(Constant.DbFound.BATCH_TEMPLATE_END).withCaseSensitivity(true));
                result.addElement(LookupElementBuilder.create("<![CDATA["+"\n\n"+"]]>").withLookupString("CD").withCaseSensitivity(true));
            }
        });
    }
}
