package com.ben.df.util;

import com.ben.df.dom.tag.Model;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.nfwork.dbfound.core.Context;
import com.nfwork.dbfound.core.DBFoundConfig;
import com.nfwork.dbfound.model.tools.Column;
import com.nfwork.dbfound.model.tools.FreemarkFactory;
import com.nfwork.dbfound.model.tools.Table;
import com.nfwork.dbfound.util.DataUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author : wubenhao
 * @date : create in 2022/8/31
 */
public class ModelUtils {

    @SuppressWarnings("unchecked")
    @NotNull
    @NonNls
    public static Model getModel(@NotNull DomElement element) {
        Optional<Model> optional = Optional.ofNullable(DomUtil.getParentOfType(element, Model.class, true));
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new IllegalArgumentException("Unknown element");
        }
    }
    public static void generateModel(String connectionProvide, String databse,String tablename, String pk, String fileName) throws Exception {
        if (DataUtil.isNull(connectionProvide)) {
            connectionProvide = "_default";
        }
        File file = new File(fileName);
        Table table = getTable(connectionProvide, databse,tablename);

        boolean pkFlag = true;

        for (Column column : table.getColumnlist()) {
            column.setType(getDataType(column.getDataType()));
            column.setParamName(column.getColumnName());
            if (column.getColumnName().equals(pk)) pkFlag = false;
        }

        if (pkFlag) throw new IllegalAccessException("Create model failure : pk argument error");

        generateModel(table, pk, new OutputStreamWriter(new FileOutputStream(file)));
    }

    private static Table getTable(String connectionProvide, String database ,String tablename) throws Exception {
        Context context = new Context();
        try {
            Connection connection = context.getConn(connectionProvide);

            DatabaseMetaData dbmetadata = connection.getMetaData();
            ResultSet rs = dbmetadata.getTables(database, null, tablename, new String[]{"TABLE"});

            List<Table> tablelist = new ArrayList<>();
            while (rs.next()) {
                Table table = new Table();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setRemarks(rs.getString("REMARKS"));
                tablelist.add(table);
            }
            rs.close();

            Table table = tablelist.get(0);
            rs = dbmetadata.getColumns(database, null, table.getTableName(), null);

            List<Column> columnlist = new ArrayList<>();
            while (rs.next()) {
                Column column = new Column();
                column.setColumnName(rs.getString("COLUMN_NAME"));
                column.setDataType(rs.getInt("DATA_TYPE"));
                column.setRemarks(rs.getString("REMARKS"));
                column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                column.setNullAble(rs.getString("IS_NULLABLE"));
                column.setDefaultValue(rs.getString("COLUMN_DEF"));
                column.setAutoIncrement(rs.getString("IS_AUTOINCREMENT"));
                columnlist.add(column);
            }
            table.setColumnlist(columnlist);
            return table;
        }catch (IndexOutOfBoundsException e){
            throw new IndexOutOfBoundsException("Create model failure : table name argument error");
        } finally {
            context.closeConns();
            DBFoundConfig.destory();
        }
    }

    private static String getDataType(int key) {
        switch (key) {
            case Types.INTEGER:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.VARBINARY:
            case Types.BIGINT:
            case Types.REAL:
                return "number";
            default:
                return "varchar";
        }
    }

    private static void generateModel(Table table, String pk, Writer writer) throws IOException, TemplateException {
        Configuration cfg = FreemarkFactory.getConfig();
        Template template = cfg.getTemplate("model.ftl");
        Map<String, Object> root = new HashMap<>();
        root.put("table", table);
        Column pkcColumn = null;
        for (Column column : table.getColumnlist()) {
            if (column.getColumnName().equals(pk)) {
                pkcColumn = column;
                break;
            }
        }
        table.getColumnlist().remove(pkcColumn);
        root.put("pk", pkcColumn);
        template.process(root, writer);
    }
}
