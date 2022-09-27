package com.ben.df.dom.tag;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.SubTagList;
import com.intellij.util.xml.SubTagsList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author : wubenhao
 * @date : create in 2022/8/30
 */
public interface Model extends DomElement {

    @NotNull
    @SubTagsList({"query", "execute", "param"})
    List<NameDomElement> getDaoElements();

    @NotNull
    @SubTagList("query")
    List<Query> getQuery();

    @NotNull
    @SubTagList("execute")
    List<Execute> getExecute();

    @NotNull
    @SubTagList("param")
    List<Param> getParam();

    @SubTagList("param")
    Param addParam();

    @SubTagList("query")
    Query addQuery();

    @SubTagList("execute")
    Execute addExecute();
}
