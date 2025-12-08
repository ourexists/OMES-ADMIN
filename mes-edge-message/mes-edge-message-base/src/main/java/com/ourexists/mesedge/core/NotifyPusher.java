package com.ourexists.mesedge.core;

import java.util.List;

public interface NotifyPusher {

    void push(List<NotifyMsg> notifyMsgs);
}
