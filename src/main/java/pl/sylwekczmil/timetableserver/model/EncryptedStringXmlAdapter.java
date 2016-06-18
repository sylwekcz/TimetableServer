package pl.sylwekczmil.timetableserver.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class EncryptedStringXmlAdapter extends XmlAdapter<String, String> {

    public String unmarshal(String value) throws Exception {
        return value;
    }

    public String marshal(String value) throws Exception {
        return "";
    }

}
