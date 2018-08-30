package cn.jcyh.peephole.event;

/**
 * Created by jogger on 2018/7/16.
 */
public class DoorbellSystemAction extends BaseActionEvent {
    public static final String TYPE_DOORBELL_SYSTEM_RING = "type_doorbell_system_ring";//有人按门铃
    public static final String TYPE_DOORBELL_SYSTEM_ALARM = "type_doorbell_system_alarm";

    public DoorbellSystemAction(String type) {
        super(type);
    }
}
