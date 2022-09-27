package com.ben.df.ui;

import com.ben.df.common.Constant;
import com.ben.df.util.ModelUtils;
import com.ben.df.util.StringUtils;
import com.ben.df.util.VirtualUtils;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.nfwork.dbfound.core.DBFoundConfig;
import com.nfwork.dbfound.db.JdbcConnectionProvide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author : wubenhao
 * @date : create in 2022/9/2
 */
public class XmlGenerateUi extends DialogWrapper {

    private JTextField TableNameField;
    private JTextField FileNameField;
    private JTextField PkNameField;
    private JPanel rootPanel;
    private JButton FileChooseButton;
    private JComboBox<String> dbListCombo;
    private JComboBox<String> dbDriverCombo;
    private JButton AutoChooseSourceButton;

    private final VirtualFile virtualDirectory;
    private final Project projectObj;
    private Map<String, HashMap<String, String>> dbConfigSourceMap = new HashMap<>();

    private static final String[] dbArgs = {"db0","db1","db2","db3","db4","db5","db6","db7","db8","db9","db10","db11","db12","db13","db14","db15","db16"};

    /**
     * GUI初始化
     * @param project 当前文件项目
     * @param virtualFile 选中的文件夹路径
     */
    public XmlGenerateUi(@Nullable Project project, VirtualFile virtualFile) {
        super(project);
        this.setTitle(Constant.Common.GUI_GENERATE_TITLE);

        setOKButtonText(Constant.Common.BUTTON_OK);
        setCancelButtonText(Constant.Common.BUTTON_Cancel);

        FileChooseButton.addActionListener(e -> {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(
                    true,           // 是否可以选择文件
                    false,       // 是否可以选择文件夹
                    false,         // 是否可以选择Jar包
                    false,   // 以读取普通文件的方式读取Jar包
                    false, // 是否可以选择.jar文件内容
                    false    // 是否可以多选
            );
            VirtualFile configFile = FileChooser.chooseFile(descriptor, project, null);
            if (configFile != null) {
                clearDataSource();
                resolveFile(project,configFile);
            }
        });

        AutoChooseSourceButton.addActionListener(e -> AutoChooseSourceButtonEvent(project, virtualFile));

        projectObj = project;
        virtualDirectory = virtualFile;
        super.init();
    }

    /**
     * 自动识别数据源事件
     */
    private void AutoChooseSourceButtonEvent(Project project, VirtualFile virtualFile) {
        try {
            File resourceDir = VirtualUtils.getResourceDir(project, virtualFile);

            if (resourceDir == null) return;

            if (!resourceDir.exists()) return;

            clearDataSource();

            List<YAMLFile> yamlFiles = VirtualUtils.findYamlFiles(project, resourceDir);

            for (YAMLFile yamlFile : yamlFiles) {
                parseYaml(yamlFile);
            }

            List<PropertiesFile> propertiesFiles = VirtualUtils.findPropertiesFiles(project, resourceDir);

            for (PropertiesFile propertiesFile : propertiesFiles) {
                parseProperties(propertiesFile);
            }

            PsiFile psiFile = VirtualUtils.findConfigXml(project, resourceDir);

            if (psiFile != null) parseXml((XmlFile) psiFile);

        } catch (Exception error) {
            Messages.showMessageDialog(error.getMessage(),  Constant.Common.DIALOG_TITLE, Messages.getInformationIcon());
        }
    }

    /**
     * 判断文件类型
     * @param configFile 选中的文件
     */
    private void resolveFile(Project project, VirtualFile configFile) {

        if (configFile.getExtension() == null) return ;

        if (configFile.getCanonicalPath() == null) return;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(configFile);

        // 如果为yaml文件
        if (psiFile instanceof YAMLFile) {
            parseYaml((YAMLFile) psiFile);
        }

        // 如果为properties文件
        if (psiFile instanceof PropertiesFile) {
            parseProperties((PropertiesFile) psiFile);
        }

        // 如果为DbFoundXml文件
        if (psiFile instanceof XmlFile) {
            parseXml((XmlFile) psiFile);
        }

    }

    /**
     * DbFoundConfigXml文件数据解析
     */
    private void parseXml(XmlFile xmlFile) {
        if (xmlFile.getRootTag() == null) return;
        if (xmlFile.getRootTag().getName().equals(Constant.DbFound.APPLICATION_ROOT)) {
            HashMap<String,HashMap<String,String>> dbConfigMap = new HashMap<>();
            XmlTag dataBaseTag;
            if ((dataBaseTag = xmlFile.getRootTag().findFirstSubTag("database")) != null) {
                XmlTag[] dataSourceTag;
                // dataSourceConnectionProvide 标签下遍历子标签属性
                if ((dataSourceTag = dataBaseTag.findSubTags("dataSourceConnectionProvide")).length != 0) {
                    for (XmlTag xmlTag : dataSourceTag) {
                        HashMap<String,String> dbConfigTemp = new HashMap<>();
                        XmlTag properties;
                        if ((properties = xmlTag.findFirstSubTag("properties")) != null) {
                            properties.accept(new XmlRecursiveElementVisitor() {
                                @Override
                                public void visitXmlTag(XmlTag tag) {
                                    super.visitXmlTag(tag);
                                    if (tag.getAttribute(Constant.Common.TAG_ATTRIBUTE_NAME) == null
                                            && tag.getAttribute(Constant.Common.TAG_ATTRIBUTE_VALUE) == null) return;
                                    dbConfigTemp.put(
                                            tag.getAttribute(Constant.Common.TAG_ATTRIBUTE_NAME).getValue(),
                                            tag.getAttribute(Constant.Common.TAG_ATTRIBUTE_VALUE).getValue());
                                }
                            });
                        }
                        dbSourceVerify(dbConfigMap, dbConfigTemp, dbArgs[dbConfigMap.size()], xmlFile.getName());
                    }
                }
                // jdbcConnectionProvide 标签下属性遍历
                XmlTag[] jdbcConnectionTag;
                if ((jdbcConnectionTag = dataBaseTag.findSubTags("jdbcConnectionProvide")).length != 0) {
                    for (XmlTag xmlTag : jdbcConnectionTag) {
                        HashMap<String,String> dbConfigTemp = new HashMap<>();
                        xmlTag.accept(new XmlRecursiveElementVisitor() {
                            @Override
                            public void visitXmlAttribute(XmlAttribute attribute) {
                                super.visitXmlAttribute(attribute);
                                if (attribute.getValue() == null) return;
                                dbConfigTemp.put(attribute.getName(),attribute.getValue());
                            }
                        });
                        dbSourceVerify(dbConfigMap, dbConfigTemp, dbArgs[dbConfigMap.size()], xmlFile.getName());
                    }
                }
            }
            dbConfigSourceMap.putAll(dbConfigMap);
        }
    }

    /**
     * 对Properties文件进行dbFound数据解析,
     */
    private void parseProperties(PropertiesFile propertiesFile) {
        HashMap<String,HashMap<String,String>> dbConfigMap = new HashMap<>();
        for (String dbArg : dbArgs) {
            HashMap<String,String> dbConfigTemp = new HashMap<>();
            if (Stream.of(".url", ".username", ".password").
                    anyMatch(s -> propertiesFile.findPropertyByKey("dbfound.datasource." + dbArg + s) == null)) {
                break;
            }

            dbConfigTemp.put(Constant.DbFound.APPLICATION_URL,
                    propertiesFile.findPropertyByKey("dbfound.datasource." + dbArg + ".url").getValue());
            dbConfigTemp.put(Constant.DbFound.APPLICATION_USERNAME,
                    propertiesFile.findPropertyByKey("dbfound.datasource." + dbArg + ".username").getValue());
            dbConfigTemp.put(Constant.DbFound.APPLICATION_PASSWORD,
                    propertiesFile.findPropertyByKey("dbfound.datasource." + dbArg + ".password").getValue());
            dbSourceVerify(dbConfigMap, dbConfigTemp, dbArg, propertiesFile.getName());
        }
        dbConfigSourceMap.putAll(dbConfigMap);
    }

    /**
     * 对Yaml文件进行dbFound数据解析,
     */
    private void parseYaml(YAMLFile yamlFile) {
        // 是否含有dbFound tag
        YAMLKeyValue dbFoundTag = YAMLUtil.getQualifiedKeyInFile(yamlFile,Constant.DbFound.APPLICATION_ROOT);
        if (dbFoundTag == null) return;
        // 是否含有datasource tag
        YAMLKeyValue datasourceTag = YAMLUtil.findKeyInProbablyMapping(dbFoundTag.getValue(),Constant.DbFound.APPLICATION_DATASOURCE);
        if (datasourceTag == null) return;
        YAMLKeyValue dbTemp;
        HashMap<String,HashMap<String,String>> dbConfigMap = new HashMap<>();
        // 遍历db Array
        for (String dbArg : dbArgs) {
            if(( dbTemp = YAMLUtil.findKeyInProbablyMapping(datasourceTag.getValue(), dbArg)) != null ) {
                HashMap<String,String> dbConfigTemp = new HashMap<>();
                dbTemp.accept(new YamlRecursivePsiElementVisitor() {
                    @Override
                    public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                        super.visitKeyValue(keyValue);
                        if (dbArg.equals(keyValue.getKeyText())) return;
                        // 存放临时的数据源
                        dbConfigTemp.put(keyValue.getKeyText(),keyValue.getValueText());

                    }
                });
                dbSourceVerify(dbConfigMap, dbConfigTemp, dbArg, yamlFile.getName());
            }
        }
        dbConfigSourceMap.putAll(dbConfigMap);
    }

    private void dbSourceVerify(HashMap<String, HashMap<String, String>> dbConfigMap, HashMap<String, String> dbConfigTemp, String dbArg, String fileName) {
        if (Stream.of(Constant.DbFound.APPLICATION_URL,
                Constant.DbFound.APPLICATION_USERNAME,
                Constant.DbFound.APPLICATION_PASSWORD).allMatch(dbConfigTemp::containsKey)) {
            String MapKey = "[" + fileName+ "]"+ dbArg;
            dbConfigMap.put(MapKey, dbConfigTemp);
            addComboData(MapKey, dbConfigTemp);
        }
    }

    private void addComboData(String mapKey, HashMap<String, String> dbConfigTemp) {
        if (dbConfigTemp.get(Constant.DbFound.APPLICATION_URL).contains("?")) {
            dbListCombo.addItem(mapKey + "#" + StringUtils.substringFront(dbConfigTemp.get(Constant.DbFound.APPLICATION_URL), "?"));
        } else {
            dbListCombo.addItem(mapKey + "#" + dbConfigTemp.get(Constant.DbFound.APPLICATION_URL));
        }
    }

    private void clearDataSource() {
        dbConfigSourceMap.clear();
        dbListCombo.removeAllItems();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected void doOKAction() {
        try {
        // 校验参数
        ValidationInfo validationInfo = doValidate();
        if (validationInfo != null) {
            Messages.showMessageDialog(validationInfo.message, Constant.Common.DIALOG_TITLE, Messages.getInformationIcon());
            return ;
        }

        if (dbListCombo.getSelectedItem() == null) {
            Messages.showMessageDialog(Constant.Common.DATASOURCE_PARSE_TIP, Constant.Common.DIALOG_TITLE, Messages.getInformationIcon());
            return ;
        }

        if (!virtualDirectory.isDirectory()) {
            Messages.showMessageDialog("Please select a folder to create", Constant.Common.DIALOG_TITLE, Messages.getInformationIcon());
            return ;
        }

        String ComBoData = StringUtils.substringFront((String) dbListCombo.getSelectedItem(),"#");
        String dataBase = StringUtils.subDataBaseByUrl(dbConfigSourceMap.get(ComBoData).get(Constant.DbFound.APPLICATION_URL));

        String NewModelFileName = virtualDirectory.getPath() + "/" + FileNameField.getText() + ".xml";

        GenerateJdbcCon(ComBoData);
        // idea 不允许主线程写操作
        ApplicationManager.getApplication().runWriteAction((ThrowableComputable<Object, Throwable>) () -> {
            ModelUtils.generateModel(Constant.DbFound.APPLICATION_PROVIDE_NAME,
                    dataBase,
                    TableNameField.getText(),
                    PkNameField.getText(),
                    NewModelFileName);
            File file = new File(NewModelFileName);
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            if (virtualFile != null) {
                (new OpenFileDescriptor(projectObj, virtualFile)).navigate(true);
            }
            return null;
        });
        super.doOKAction();
        } catch (Throwable e) {
            Messages.showMessageDialog(e.getMessage(),  Constant.Common.DIALOG_TITLE, Messages.getInformationIcon());
        }
    }

    private void GenerateJdbcCon(String comBoData) {
        JdbcConnectionProvide provide = new JdbcConnectionProvide();
        provide.setUrl(dbConfigSourceMap.get(comBoData).get(Constant.DbFound.APPLICATION_URL));
        provide.setDriverClass((String) dbDriverCombo.getSelectedItem());
        provide.setUsername(dbConfigSourceMap.get(comBoData).get(Constant.DbFound.APPLICATION_USERNAME));
        provide.setPassword(dbConfigSourceMap.get(comBoData).get(Constant.DbFound.APPLICATION_PASSWORD));
        provide.setDialect(Constant.DbFound.DIALECT_DEFAULT);
        provide.setProvideName(Constant.DbFound.APPLICATION_PROVIDE_NAME);
        provide.regist();
        DBFoundConfig.setInited(true);
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getCancelAction()};
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtils.isEmpty(TableNameField.getText()) ||
                StringUtils.isEmpty(FileNameField.getText()) ||
                StringUtils.isEmpty(PkNameField.getText())) {
            return new ValidationInfo(Constant.Common.EMPTY_ERROR_MESSAGE);
        }
        return super.doValidate();
    }
}
