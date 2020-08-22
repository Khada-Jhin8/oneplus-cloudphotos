package vip.zhguo.oneplus.Util;

import vip.zhguo.oneplus.pojo.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * 动态设置状态
 */
public class Querystatus {
    static Map<String,Status> map=new HashMap<String,Status>();
    public static Status getstatus(String cookie){

        return map.get(cookie);
    }
    public static void setstatus(String cookie,Status status){
        map.put(cookie, status);
    }
    public static int getMapsize(){
        return map.size();
    }
}
