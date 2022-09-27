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
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.ig.psiutils.ExpressionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author : wubenhao
 * @date : create in 2022/8/26
 */
public class ClassToModelNavProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (element instanceof PsiMethodCallExpression && element.getContainingFile() instanceof PsiJavaFile) {

            PsiMethodCallExpression expression = (PsiMethodCallExpression) element;

            PsiExpression MethodField = expression.getMethodExpression().getQualifierExpression();

            if (MethodField == null) return;

            if (MethodField.getType() == null) {
                if (!MethodField.getText().equals(Constant.DbFound.MODEL_ENGINE)) return;
            } else {
                if (!MethodField.getType().equalsToText(Constant.DbFound.DF_PACKAGE_NAME)) return;
            }

            List<String> temp = new ArrayList<>();

            for (PsiExpression psiExpression : expression.getArgumentList().getExpressions()) {

                if (psiExpression.getType() == null) return;

                if (temp.size() == 2) break;

                if (psiExpression.getType().getPresentableText().equals("String")) {

                    if (ExpressionUtils.getLiteral(psiExpression) != null) {
                        temp.add(psiExpression.getText().replaceAll("\"", ""));
                    } else {
                        if (psiExpression.getReference() == null) return;
                        PsiElement psiElement = psiExpression.getReference().resolve();
                        // 常量
                        if (psiElement instanceof PsiField) {
                            getConstantOrLocalVariable(temp, psiElement);
                        }
                        // 局部变量
                        if (psiElement instanceof PsiLocalVariable) {
                            getConstantOrLocalVariable(temp, psiElement);
                        }
                    }
                }

                if (psiExpression.getType().getPresentableText().equals("null")) {
                    temp.add("");
                }
            }
            if (temp.size() < 1) return;

            if (temp.size() == 1) temp.add("");
            // 构建虚拟路径查询对应xml
            Model model = DomUtils.findDomElements(expression.getProject(), element.getContainingFile(), temp.get(0));
            // 该表达式唯一标识
            PsiIdentifier psiId = PsiTreeUtil.findChildOfType(expression.getMethodExpression(),PsiIdentifier.class);
            // 寻找方法名称相同的Tag
            CompareMethodName(temp, model, psiId, result);
        }
    }

    // 获取常量或本地变量值
    private void getConstantOrLocalVariable(List<String> temp, PsiElement psiElement) {
        PsiLiteralExpression psiLiteralExpression = PsiTreeUtil.getChildOfType(psiElement, PsiLiteralExpression.class);
        if (psiLiteralExpression != null) {
            temp.add((String) psiLiteralExpression.getValue());
        }
    }

    // 寻找方法名称相同的Tag
    private void CompareMethodName(List<String> temp, Model element, PsiIdentifier psiId, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element != null && psiId != null) {
            for (NameDomElement daoElement : element.getDaoElements()) {
                if (daoElement.getName().getRawText() == null) {
                    if ("".equals(temp.get(1))) {
                        setNavigationRelation(psiId, daoElement, result);
                    }
                } else {
                    if (daoElement.getName().getRawText().equals(temp.get(1))) {
                        setNavigationRelation(psiId, daoElement, result);
                    }
                }
            }
        }
    }

    // 生成导航关系
    private void setNavigationRelation(PsiIdentifier psiId, NameDomElement daoElement, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        NavigationGutterIconBuilder<PsiElement> builder =
                NavigationGutterIconBuilder.create(IconUtils.MAPPER_LINE_MARKER_ICON)
                        .setAlignment(GutterIconRenderer.Alignment.CENTER)
                        .setTargets(daoElement.getXmlTag())
                        .setTooltipTitle(Constant.Common.NAV_TOOLTIP_TITLE);
        result.add(builder.createLineMarkerInfo(psiId));
    }
}
