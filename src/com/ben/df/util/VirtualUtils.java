package com.ben.df.util;

import com.ben.df.common.Constant;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.yaml.psi.YAMLFile;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class VirtualUtils {

    public static File getResourceDir(Project project, VirtualFile virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);

        if (module == null) return null;

        String targetPath = project.getBasePath() + ((ModuleManager.getInstance(project).getModules().length > 1) ?
                "/" + module.getName() + Constant.Common.RESOURCE_PATH :
                Constant.Common.RESOURCE_PATH);

        return new File(targetPath);
    }

    public static List<YAMLFile> findYamlFiles(Project project, File resourceDir) {
        List<File> files =  FileUtil.findFilesByMask(Pattern.compile("^[\\s\\S]*\\.(yaml|yml)$"), resourceDir);
        List<YAMLFile> yamlFiles = new LinkedList<>();
        for (File e : files) {
            if (LocalFileSystem.getInstance().findFileByIoFile(e) == null) continue;
            yamlFiles.add((YAMLFile) PsiManager.getInstance(project).findFile(getVirtualFileByIoFile(e)));
        }
        return yamlFiles;
    }

    public static List<PropertiesFile> findPropertiesFiles(Project project, File resourceDir) {
        List<File> files =  FileUtil.findFilesByMask(Pattern.compile("^[\\s\\S]*\\.(properties)$"), resourceDir);
        List<PropertiesFile> propertiesFiles = new LinkedList<>();
        for (File e : files) {
            if (LocalFileSystem.getInstance().findFileByIoFile(e) == null) continue;
            propertiesFiles.add((PropertiesFile) PsiManager.getInstance(project).findFile(getVirtualFileByIoFile(e)));
        }
        return propertiesFiles;
    }

    public static PsiFile findConfigXml(Project project, File resourceDir) {
        List<File> files = FileUtil.findFilesByMask(Pattern.compile("^(dbfound-conf.xml)$"),resourceDir);
        if (files.size() == 0 || files.get(0) == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(getVirtualFileByIoFile(files.get(0)));
    }

    private static VirtualFile getVirtualFileByIoFile(File file) {
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }
}
