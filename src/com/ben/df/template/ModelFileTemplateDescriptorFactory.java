package com.ben.df.template;

import com.ben.df.util.IconUtils;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

/**
 * @author : wubenhao
 * @date : create in 2022/9/13
 */
public class ModelFileTemplateDescriptorFactory implements FileTemplateGroupDescriptorFactory {

    public static final String MODEL_XML_TEMPLATE = "DbFound Model.xml";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("DbFound", IconUtils.MAPPER_LINE_MARKER_ICON);
        group.addTemplate(new FileTemplateDescriptor(MODEL_XML_TEMPLATE,  IconUtils.MAPPER_LINE_MARKER_ICON));
        return group;
    }
}
