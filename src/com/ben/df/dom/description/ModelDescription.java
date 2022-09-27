package com.ben.df.dom.description;

import com.ben.df.common.Constant;
import com.intellij.openapi.module.Module;
import com.ben.df.dom.tag.Model;
import com.ben.df.util.DomUtils;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author : wubenhao
 * @date : create in 2022/8/30
 */
public class ModelDescription extends DomFileDescription<Model> {

    public ModelDescription() {
        super(Model.class, Constant.DbFound.MODEL);
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
        return DomUtils.isDbFoundFile(file);
    }
}
