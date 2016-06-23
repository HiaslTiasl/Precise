package it.unibz.precise.main;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class Long2StringAdapter extends XmlAdapter<String, Long>{

	@Override
	public Long unmarshal(String v) throws Exception {
		return Long.valueOf(v);
	}

	@Override
	public String marshal(Long v) throws Exception {
		return Long.toString(v);
	}

}
