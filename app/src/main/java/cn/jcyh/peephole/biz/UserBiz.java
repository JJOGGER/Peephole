package cn.jcyh.peephole.biz;


import cn.jcyh.peephole.http.HttpAction;

/**
 * Created by jogger on 2018/1/10.
 */

public class UserBiz {
    private HttpAction mHttpAction;

    public UserBiz() {
        mHttpAction = HttpAction.getHttpAction();
    }
}
