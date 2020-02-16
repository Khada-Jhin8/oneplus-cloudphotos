package vip.zhguo.oneplus.Util;

import vip.zhguo.oneplus.pojo.Status;

public class Test {
    public static void begin(String cookie){
        Status status = new Status();
        status.setOverflag(0);
        Querystatus.setstatus(cookie, status);
        for(int i=0;i<10;i++){
            for(int j=0;j<100;j++){
                status.setStage(i);
                status.setCurrent(j);
                System.out.println("i="+i+"....j="+j);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        status.setOverflag(1);

    }

}
