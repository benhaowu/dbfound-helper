package com.ben.df.provider;

import com.ben.df.common.Constant;
import com.ben.df.dom.tag.Model;
import com.ben.df.dom.tag.NameDomElement;
import com.ben.df.util.DomUtils;
import com.ben.df.util.IconUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author : wubenhao
 * @date : create in 2022/9/5
 */
public class ModelToModelNavProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        // 是否为DbFound文件
        if (element instanceof XmlTag && DomUtils.isDbFoundFile(element.getContainingFile())) {
            XmlTag xmlTag = (XmlTag) element;

            // 不是子标签则退出
            if (xmlTag.getParentTag() == null || xmlTag.getParentTag().getName().equals(Constant.DbFound.MODEL)) return;

            // 排查execute中是否包含
            if (xmlTag.getName().equals(Constant.DbFound.TAG_EXECUTE) || xmlTag.getName().equals(Constant.DbFound.TAG_QUERY)) {
                // 该表达式唯一标识
                XmlToken xmlToken = PsiTreeUtil.findChildOfType(xmlTag,XmlToken.class);

                resolveAttribute(element, result, xmlTag, xmlToken);
            }
        }
    }

    private void resolveAttribute(@NotNull PsiElement element,
                                  @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                  XmlTag xmlTag,
                                  XmlToken xmlToken) {
        if (xmlTag.getAttribute("modelName") != null) {
            String modelPath = xmlTag.getAttribute("modelName").getValue();
            String methodName = xmlTag.getAttribute("name") == null ? "" : xmlTag.getAttribute("name").getValue();

            // 构建虚拟路径查询对应xml
            Model model = DomUtils.findDomElements(xmlTag.getProject(), element.getContainingFile(), modelPath);

            CompareMethodName(result, methodName, model, xmlToken);
        } else {
            String methodName = xmlTag.getAttribute("name") == null ? "" : xmlTag.getAttribute("name").getValue();

            Model model = DomUtils.getDomFileElements(
                    xmlTag.getProject(),
                    xmlTag.getContainingFile().getVirtualFile()).get(0).getRootElement();

            CompareMethodName(result, methodName, model, xmlToken);
        }
    }

    // 寻找方法名称相同的Tag
    public static void CompareMethodName(@NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result, String methodName, Model model, XmlToken xmlToken) {
        if (model != null) {
            for (NameDomElement daoElement : model.getDaoElements()) {
                if (daoElement.getName().getRawText() == null) {
                    if ("".equals(methodName)) {
                        setNavigationRelation(xmlToken, daoElement, result);
                    }
                } else {
                    if (daoElement.getName().getRawText().equals(methodName)) {
                        setNavigationRelation(xmlToken, daoElement, result);
                    }
                }
            }
        }
    }

    // 生成导航关系
    public static void setNavigationRelation(XmlToken xmlToken, NameDomElement daoElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(IconUtils.MAPPER_LINE_MARKER_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTargets(daoElement.getXmlTag())
                        .setTooltipTitle(Constant.Common.NAV_TOOLTIP_TITLE);
        result.add(builder.createLineMarkerInfo(xmlToken));
    }
}
