package com.ben.df.contributor;

import com.ben.df.common.Constant;
import com.ben.df.dom.tag.Model;
import com.ben.df.util.DomUtils;
import com.ben.df.util.StringUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class ModelAttributeContributor extends CompletionContributor {

    public ModelAttributeContributor() {
        extend(CompletionType.BASIC, psiElement().inside(XmlPatterns.xmlAttributeValue(Constant.DbFound.TAG_ATTRIBUTE_MODELNAME)), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext processingContext,
                                          @NotNull CompletionResultSet result) {
                final PsiElement position = parameters.getPosition();

                Editor editor = parameters.getEditor();
                Project project = editor.getProject();
                if (project == null) {
                    return;
                }

                final XmlTag xmlTag = PsiTreeUtil.getParentOfType(position, XmlTag.class);

                if (xmlTag == null) return;

                if (!xmlTag.getNamespace().equals(Constant.DbFound.MODEL_NAMESPACE)) return;

                if (xmlTag.getName().equals(Constant.DbFound.TAG_EXECUTE) || xmlTag.getName().equals(Constant.DbFound.TAG_QUERY)) {

                    Collection<VirtualFile> modelFile = DomUtils.findDomVirtualFile(project, Model.class);

                    modelFile.stream()
                            .filter(model -> model.getUrl().contains(Constant.Common.RESOURCE_MODEL_PATH))
                            .map( m -> StringUtils.substring(m.getUrl(),Constant.Common.RESOURCE_MODEL_PATH,Constant.Common.XML_SUFFIX))
                            .forEach(e -> result.addElement(LookupElementBuilder.create(e).withCaseSensitivity(false)));
                }
            }
        });
        extend(CompletionType.BASIC, psiElement().inside(XmlPatterns.xmlAttributeValue(Constant.Common.TAG_ATTRIBUTE_NAME)), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters,
                                          @NotNull ProcessingContext processingContext,
                                          @NotNull CompletionResultSet result) {
                final PsiElement position = parameters.getPosition();

                Editor editor = parameters.getEditor();
                Project project = editor.getProject();
                if (project == null) {
                    return;
                }

                final XmlTag xmlTag = PsiTreeUtil.getParentOfType(position, XmlTag.class);

                if (xmlTag == null) return;

                if (!xmlTag.getNamespace().equals(Constant.DbFound.MODEL_NAMESPACE)) return;

                if (xmlTag.getName().equals(Constant.DbFound.TAG_EXECUTE) || xmlTag.getName().equals(Constant.DbFound.TAG_QUERY)) {

                    XmlAttribute xmlAttribute = xmlTag.getAttribute(Constant.DbFound.TAG_ATTRIBUTE_MODELNAME);

                    if (xmlAttribute == null){

                        Model model = DomUtils.getDomFileElements(
                                project,
                                position.getContainingFile()
                                        .getOriginalFile()
                                        .getVirtualFile()).get(0).getRootElement();

                        AddLookUpElement(result, model, xmlTag.getName());

                    } else if (!Objects.equals(xmlAttribute.getValue(), "")){

                        // 构建虚拟路径查询对应xml
                        Model model = DomUtils.findDomElements(
                                project,
                                position.getContainingFile().getOriginalFile(),
                                xmlAttribute.getValue());

                        if(model == null) return;

                        AddLookUpElement(result, model, xmlTag.getName());
                    }
                }
            }
        });
    }

    private void AddLookUpElement(CompletionResultSet result, Model model, String relationTag) {
        model.getDaoElements().stream()
                .filter(p -> p.getXmlTag() != null && p.getXmlTag().getName().equals(relationTag))
                .filter(m -> m.getName().getRawText() != null)
                .forEach(e -> result.addElement(LookupElementBuilder.create(e.getName().getRawText()).withCaseSensitivity(false)));
    }
}
