package com.ourexists.mesedge.message.core;

import java.util.List;

public interface NotifyPusher {

    void push(List<NotifyMsg> notifyMsgs);
}
