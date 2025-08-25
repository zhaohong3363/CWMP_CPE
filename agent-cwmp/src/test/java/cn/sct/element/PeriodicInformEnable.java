package cn.sct.element;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.annotation.CWMPItem;
import cn.sct.networkmanager.agent.domain.CWMPElement;
import reactor.util.Logger;
import reactor.util.Loggers;

@CWMPItem(name = "InternetGatewayDevice.ManagementServer.PeriodicInformEnable")
public class PeriodicInformEnable extends CWMPElement<Boolean> {
    private static final Logger log = Loggers.getLogger(PeriodicInformEnable.class);
    @Override
    public Boolean setValue(Boolean value) {
        log.info("我被调用设置值");
        this.notice();
        return super.setValue(value);
    }
    @Override
    public Boolean getValue() {

        return true;
    }


    @Override
    public String getPath() {
        return "InternetGatewayDevice.ManagementServer.PeriodicInformEnable";
    }
}
