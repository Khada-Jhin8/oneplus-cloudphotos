package vip.zhguo.oneplus.pojo;

public class Status {
    private Integer stage;//第几张
    private Integer total;//总张数
    private double current;//当前百分比
    private Integer overflag;//结束标志0；未结束，1：结束 2:异常终止
    private String dowbUrl;//下载链接

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getDowbUrl() {
        return dowbUrl;
    }

    public void setDowbUrl(String dowbUrl) {
        this.dowbUrl = dowbUrl;
    }

    public Integer getOverflag() {
        return overflag;
    }

    public void setOverflag(Integer overflag) {
        this.overflag = overflag;
    }

    public Status() {
    }

    public Status(Integer stage, double current) {
        this.stage = stage;
        this.current = current;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }
}
