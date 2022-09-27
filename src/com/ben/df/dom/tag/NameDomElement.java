package com.ben.df.dom.tag;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * @author : wubenhao
 * @date : create in 2022/8/30
 */
public interface NameDomElement extends DomElement {

    @Attribute("name")
    GenericAttributeValue<String> getName();

    void setValue(String content);
}
