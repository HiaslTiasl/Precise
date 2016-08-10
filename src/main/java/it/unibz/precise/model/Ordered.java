package it.unibz.precise.model;

import java.util.List;

public interface Ordered {

	int getPosition();
	void setPosition(int position);
	
	static <T extends Ordered> List<T> adjustPositions(List<T> list) {
		int len = list.size();
		for (int i = 0; i < len; i++)
			list.get(i).setPosition(i+1);
		return list;
	}
	
}
