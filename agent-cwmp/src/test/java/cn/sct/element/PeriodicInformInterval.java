package cn.sct.element;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.annotation.CWMPItem;

@CWMPItem(name = "InternetGatewayDevice.ManagementServer.PeriodicInformInterval")
public class PeriodicInformInterval implements Item<Long> {
    @Override
    public Long getValue() {
        return 86400L;
    }
    @Override
    public Long setValue(Long value) {
        return Item.super.setValue(value);
    }

}
