package cn.david.android.client;

import java.util.ArrayList;
import java.util.List;

import cn.david.domain.ReceiveMsg;

public class MsgAndFilter implements MsgFilter<ReceiveMsg> {

	private List<MsgFilter<ReceiveMsg>> filters = new ArrayList<MsgFilter<ReceiveMsg>>();
	
	public MsgAndFilter(MsgFilter<ReceiveMsg>... filters ) {
		if(filters == null) {
			throw new IllegalArgumentException("Parameters can not be null.");
		}
		for(MsgFilter<ReceiveMsg> filter : filters) {
			if(filter == null) {
				throw new IllegalArgumentException("Parameters can not be null.");
			}
			this.filters.add(filter);
		}
	}
	
	public void addFilter(MsgFilter<ReceiveMsg> filter) {
		filters.add(filter);
	}
	@Override
	public boolean accpet(ReceiveMsg msg) {
		for(MsgFilter<ReceiveMsg> filter : filters) {
			if(!filter.accpet(msg)) {
				return false;
			}
		}
		return true;
	}
	
}
