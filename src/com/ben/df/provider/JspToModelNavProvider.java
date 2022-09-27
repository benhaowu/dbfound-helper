package com.ben.df.provider;

import com.ben.df.dom.tag.Model;
import com.ben.df.util.DomUtils;
import com.ben.df.util.StringUtils;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JspToModelNavProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (element instanceof XmlTag
                && element.getContainingFile() instanceof JspFile
                && ((JspFile) element.getContainingFile()).getRootTag().findFirstSubTag("taglib") != null
                && ((JspFile) element.getContainingFile()).getRootTag().findFirstSubTag("taglib").getAttribute("uri") != null
                && ((JspFile) element.getContainingFile()).getRootTag().findFirstSubTag("taglib").getAttribute("uri").getValue() != null
                && ((JspFile) element.getContainingFile()).getRootTag().findFirstSubTag("taglib").getAttribute("uri").getValue().equals("dbfound-tags")) {

            XmlTag xmlTag = (XmlTag) element;

            // 该表达式唯一标识
            XmlToken xmlToken = PsiTreeUtil.findChildOfType(xmlTag,XmlToken.class);

            if (xmlTag.getName().equals("d:dataSet") || xmlTag.getName().equals("d:query")) {
                resolveAttribute(element, result, xmlTag, xmlToken,"modelName","queryName");
                return;
            }

            if (xmlTag.getName().equals("d:grid")) {
                resolveGridTag(element, result, xmlTag, xmlToken);
                return;
            }

            if (xmlTag.getName().equals("d:gridButton") || xmlTag.getName().equals("d:formButton")) {
                if (xmlTag.getAttribute("action") != null) {
                    String url = xmlTag.getAttribute("action").getValue();
                    resolveAction(element, result, xmlTag, xmlToken, url);
                    return;
                }
                return;
            }

            if (xmlTag.getName().equals("d:batchExecute")) {
                resolveAttribute(element, result, xmlTag, xmlToken,"modelName","name");
                return;
            }

            if (xmlTag.getName().equals("d:execute")) {
                resolveAttribute(element, result, xmlTag, xmlToken,"modelName","executeName");
            }
        }
    }

    private void resolveGridTag(@NotNull PsiElement element,
                                @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                XmlTag xmlTag,
                                XmlToken xmlToken) {
        if (xmlTag.getAttribute("queryUrl") != null) {
            String url = xmlTag.getAttribute("queryUrl").getValue();
            resolveAction(element, result, xmlTag, xmlToken, url);
            return;
        }

        if (xmlTag.getAttribute("model") != null) {
            String modelPath = xmlTag.getAttribute("model").getValue();

            // 构建虚拟路径查询对应xml
            Model model = DomUtils.findDomElements(xmlTag.getProject(), element.getContainingFile(), modelPath);

            ModelToModelNavProvider.CompareMethodName(result, "", model, xmlToken);
        }
    }

    private void resolveAction(@NotNull PsiElement element,
                               @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                               XmlTag xmlTag, XmlToken xmlToken,
                               String url) {
        if (url != null) {
            String modelPath = url.contains(".") ? StringUtils.substringFront(url,".") : url;
            String methodName = !url.contains("!") ?
                    "" : (url.contains("?") ?
                    StringUtils.substring(url,"!","?") :
                    StringUtils.substring(url,"!",""));

            // 构建虚拟路径查询对应xml
            Model model = DomUtils.findDomElements(xmlTag.getProject(), element.getContainingFile(), modelPath);

            ModelToModelNavProvider.CompareMethodName(result, methodName, model, xmlToken);
        }
    }

    private void resolveAttribute(@NotNull PsiElement element,
                                  @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result,
                                  XmlTag xmlTag,
                                  XmlToken xmlToken,
                                  String modelName,
                                  String queryName) {
        if (xmlTag.getAttribute(modelName) != null) {
            String modelPath = xmlTag.getAttribute(modelName).getValue();
            String methodName = xmlTag.getAttribute(queryName) == null ? "" : xmlTag.getAttribute(queryName).getValue();

            // 构建虚拟路径查询对应xml
            Model model = DomUtils.findDomElements(xmlTag.getProject(), element.getContainingFile(), modelPath);

            ModelToModelNavProvider.CompareMethodName(result, methodName, model, xmlToken);
        }
    }
}
