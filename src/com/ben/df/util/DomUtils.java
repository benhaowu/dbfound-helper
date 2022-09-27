package com.ben.df.util;

import com.ben.df.common.Constant;
import com.ben.df.dom.tag.Model;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : wubenhao
 * @date : create in 2022/8/30
 */
public class DomUtils {

    public static boolean isDbFoundFile(@Nullable PsiFile file) {

        if (file == null || !isXmlFile(file)) return false;

        XmlTag rootTag = ((XmlFile) file).getRootTag();

        if (rootTag == null || !rootTag.getName().equals(Constant.DbFound.MODEL)) return false;

        return rootTag.getNamespace().equals(Constant.DbFound.MODEL_NAMESPACE);
    }

    private static boolean isXmlFile(@NotNull PsiFile file) {
        return file instanceof XmlFile;
    }

    public static Model findDomElements(Project project, PsiFile psiFile, String args) {
        Module module = ModuleUtil.findModuleForFile(psiFile);

        if (module == null) return null;

        String targetPath = project.getBasePath();
        String modelFilePath = Constant.Common.RESOURCE_MODEL_PATH + args + Constant.Common.XML_SUFFIX;

        // 判断多模块还是单模块 拼接文件路径
        targetPath += (ModuleManager.getInstance(project).getModules().length > 1) ? "/" + module.getName() + modelFilePath : modelFilePath;

        File file = new File(targetPath);

        if (!file.exists()) return null;

        // 获取虚拟路径
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);

        List<DomFileElement<Model>> elements = getDomFileElements(project, virtualFile);

        return elements.get(0).getRootElement();
    }

    public static List<DomFileElement<Model>> getDomFileElements(Project project, VirtualFile virtualFile) {
        return DomService.getInstance().getFileElements(
                Model.class,
                project,
                GlobalSearchScope.fileScope(project, virtualFile));
    }

    @NotNull
    @NonNls
    public static <T extends DomElement> Collection<T> findDomElements(@NotNull Project project, Class<T> clazz) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        List<DomFileElement<T>> elements = DomService.getInstance().getFileElements(clazz, project, scope);
        return elements.stream().map(DomFileElement::getRootElement).collect(Collectors.toList());
    }

    @NotNull
    @NonNls
    public static <T extends DomElement> Collection<VirtualFile> findDomVirtualFile(@NotNull Project project, Class<T> clazz) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        return DomService.getInstance().getDomFileCandidates(clazz, project, scope);
    }
}