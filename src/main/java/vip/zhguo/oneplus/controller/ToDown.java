package vip.zhguo.oneplus.controller;
import java.text.NumberFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vip.zhguo.oneplus.Util.Downimg;
import vip.zhguo.oneplus.Util.NewDowning;
import vip.zhguo.oneplus.Util.Querystatus;
import vip.zhguo.oneplus.pojo.Status;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ToDown {
    @Autowired
    Status status;
    int sum = 1;
    String lastCookie=null;
    String nowcookie;
    @GetMapping("/")
    public String home(Model model) {
//        Status status = new Status(2, 10.5);
////        model.addAttribute("status", status);
        return "home";
    }

    @ResponseBody
    @PostMapping("/query")
    public Status query(Model model, String cookie) {
//        System.out.println(cookie);
        Status getquery = Querystatus.getstatus(cookie);
        return getquery;
    }
    @ResponseBody
    @PostMapping("/updatecookie")
    public String updatecookie(String cookie) {
        this.nowcookie=cookie;
//        System.out.println(cookie);
        Status temp = Querystatus.getstatus(lastCookie);
        temp.setOverflag(0);
        Querystatus.setstatus(nowcookie,temp);
        return "success";
    }
//    @ResponseBody
//    @GetMapping("/test")
//    public void test(String cookie,HttpServletRequest res) {
//        System.out.println(res.getServerName());
//    }
//@GetMapping("/downzip")
//    public void downzip(String path,HttpServletResponse response){
//    Downimg.download(path, response);
//}
    @PostMapping("/down")
    public void down(String cookie, HttpServletRequest res) throws Exception {
        Map<String,String> realPathMap = new HashMap();
        this.nowcookie=cookie;
        status.setOverflag(0);
        status.setDownUrl("");
        status.setCurrent(0.0);
        Querystatus.setstatus(nowcookie, status);
         lastCookie=nowcookie;  //如果cookie失效，记录上一条失cookie，拿到上一条下载状态。
        System.out.println(nowcookie);
        String postServer=null;
        //用于计算百分比，int类型计算
        NumberFormat numberFormat =NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

//		int count=0;
        ArrayList<String> arr = new ArrayList<String>();
        Map<String, String> hashmap = new HashMap<String, String>();
        //文件保存路径
//        String saveImgPath = res.getServletPath() + "/" + UUID.randomUUID();
        String saveImgPath = "D:/"+res.getServletPath();
        //获取相册列表
        //cursor=20190822&photoIndex=1
        //lastMatchedMoment   realPhotoIndex
        //初始化数据
        String data = "size=100&state=active&smallPhotoScaleParams=image/resize,m_mfit,h_250,w_250&originalPhotoScaleParams=image/resize,m_mfit,h_1300,w_1300";
        String urlpath = "https://cloud.h2os.com/gallery/pc/listNormalPhotos";
        while( Querystatus.getstatus(nowcookie).getOverflag()==0 ||Querystatus.getstatus(nowcookie).getOverflag()==2) {
            if (Querystatus.getstatus(nowcookie).getOverflag() == 0) {
                while (true) {
                    //post请求得到json数据
                    if (sum==1) { //sum==1 说明第一次提交
                        postServer = NewDowning.postServer(data, urlpath, nowcookie);
                    }else{
                        postServer = NewDowning.postServer( Querystatus.getstatus(nowcookie).getDataUrl(), urlpath, nowcookie);
                    }
                    if ("posterror".equals(postServer)) {
                        Querystatus.getstatus(nowcookie).setOverflag(2);
                        break ;
                    }
                    //重置data基础数据
                    data = "size=100&state=active&smallPhotoScaleParams=image/resize,m_mfit,h_250,w_250&originalPhotoScaleParams=image/resize,m_mfit,h_1300,w_1300";
                    System.out.println("postServer" + "======" + postServer);
                    JSONObject jobj = JSON.parseObject(postServer);
                    System.out.println("下一个时间段" + jobj.getString("lastMatchedMoment") + "....." + "下一个index" + jobj.getString("realPhotoIndex"));
                    //获取下一页游标
                    String lastMatchedMoment = (jobj.getString("lastMatchedMoment")) == null ? null : jobj.getString("lastMatchedMoment");
                    String photoIndex = (jobj.getString("realPhotoIndex")) == null ? null : jobj.getString("realPhotoIndex");
                    //判断下一页是否有值
                    if (lastMatchedMoment != null && !"".equals(lastMatchedMoment) && photoIndex != null && !"".equals(photoIndex)) {
                        data = data + "&cursor=" + lastMatchedMoment + "&photoIndex=" + photoIndex;
                        Querystatus.getstatus(nowcookie).setDataUrl(data);
                    }
                    //拿到photos节点
                    JSONObject photosjson = jobj.getJSONObject("photos");
                    System.out.println(photosjson);
                    //拿到照片总数
                    Integer totalCount = Integer.parseInt(jobj.getString("totalCount"));
                    status.setTotal(totalCount);
                    //将photosjson赋值给map1，便于后面遍历时间节点。由于时间节点不固定，无法准确拿到json数据。
                    Map map1 = photosjson;
                    //遍历出key值存入arr

                    for (Object o : map1.entrySet()) {
                        Map.Entry<String, JSONArray> entry = (Map.Entry<String, JSONArray>) o;
                        arr.add(entry.getKey());
                    }
                    //遍历每个时间节点的值
                    for (int i = 0; i < arr.size(); i++) {
                        JSONArray photos = photosjson.getJSONArray(arr.get(i));
                        //遍历每个时间节点下的多个照片
                        for (int j = 0; j < photos.size(); j++) {
                            JSONObject phototimeJson = (JSONObject) photos.get(j);
                            String title = phototimeJson.getString("title");
                            //得到图片id,存入hashmap<"title","id">
                            String img_id = phototimeJson.getString("id");
                            hashmap.put(title, img_id);
                        }

                    }
                    System.out.println("hashmap>>>>>>"+hashmap.toString());
                    for (String key : hashmap.keySet()) {
                        String realPathJson = NewDowning.getRealPath(hashmap.get(key), nowcookie);
                        System.out.println("realPathJson>>>>>>"+realPathJson.toString());
                        JSONObject j = JSONObject.parseObject(realPathJson);
                        String realPath = j.getString(hashmap.get(key));
                        realPathMap.put(key,realPath);
//                        下载
//                        Downimg.downloadPicture(realPath, saveImgPath, key);
                        sum++;
                        status.setStage(sum);
                        //计算百分比保留2位小数
                        String format = numberFormat.format((float) sum / (float) totalCount * 100);
                        double bfb = Double.parseDouble(format);
                        System.out.println(sum + "......" + totalCount);
                        status.setCurrent(bfb);
//                    if(bfb==100.00){
//                        //        清空map
//                        map1.clear();
//                        hashmap.clear();
//                        arr.clear();
//                        System.out.println("全部下载完成,已下载：百分比"+bfb);
//                        String downUrl=saveImgPath+".tar.gz";
//                        status.setOverflag(1);
//                        Zip. compress(saveImgPath,downUrl);
//                        status.setOverflag(3);
//                        status.setDownUrl(downUrl);
//                        break;
//                    }
                    }
                    //        清空map
                    map1.clear();
                    hashmap.clear();
                    arr.clear();
                    if (lastMatchedMoment.equals("EOF")) {
                        System.out.println("全部下载完成EOF");
                        System.out.println(realPathMap.toString());
                        status.setOverflag(1);
                        return;
                    }
                }
            }
            System.out.println("等待更新cookie");
            Thread.sleep(1000);
        }
    }
}
