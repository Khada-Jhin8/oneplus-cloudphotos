package vip.zhguo.oneplus.controller;
import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;
import vip.zhguo.oneplus.Util.Downimg;
import vip.zhguo.oneplus.Util.Querystatus;
import vip.zhguo.oneplus.Util.Test;
import vip.zhguo.oneplus.pojo.Status;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class ToDown {
    @Autowired
    Status status;
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
    @GetMapping("/test")
    public void test(String cookie) {
        Test.begin(cookie);
    }

    @PostMapping("/down")
    public void down(String cookie, HttpServletRequest res) throws Exception {
        status.setOverflag(0);
        status.setDowbUrl("");
        Querystatus.setstatus(cookie, status);
        System.out.println(cookie);
        String postServer=null;
        //用于计算百分比，int类型计算
        NumberFormat numberFormat =NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        int sum = 0;
//		int count=0;
        ArrayList<String> arr = new ArrayList<String>();
        Map<String, String> hashmap = new HashMap<String, String>();
        //文件保存路径
        String saveImgPath = res.getServletPath() + "/" + UUID.randomUUID();
        //ce970714-2607-4e42-9413-26aef45c9e28
//        String saveImgPath = res.getServletPath() + "/ce970714-2607-4e42-9413-26aef45c9e28";

        //获取相册列表
        //cursor=20190822&photoIndex=1
        //lastMatchedMoment   realPhotoIndex
        //初始化数据
        String data = "size=100&state=active&smallPhotoScaleParams=image/resize,m_mfit,h_250,w_250&originalPhotoScaleParams=image/resize,m_mfit,h_1300,w_1300";
        String urlpath = "https://cloud.h2os.com/gallery/pc/listNormalPhotos";
        //String cookie="acw_tc=784c10e815665472969636674eaf2fc0aad2480c2a19c73d5583ea4b245cca; _uab_collina=156654729729478340591497; opcloud_sid=f4d6a4d85b72477d98cb609938bf6dd3; opcloud_token=ONEPLUS_cdJMSApHGEmsJYxjYfUcXyWk24LTaAqphN; NEARME_ACCOUNTNAME_COOKIE=M1563165562682; opcloud_vcode=fdc8e702c3774c48acf3d0546d41229b; accountName=M1563165562682";
        while (true) {
            //post请求得到json数据
            postServer = Downimg.PostServer(data, urlpath, cookie);
            if("posterror".equals(postServer)){
                status.setOverflag(2);
//                res.setAttribute("status", status);
//                session.setAttribute("status", status);
                break;
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
            }
            /*此处可以添加多线程A*/
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
            for (String key : hashmap.keySet()) {
                String realPathJson = Downimg.getRealPath(hashmap.get(key), cookie);
                JSONObject j = JSONObject.parseObject(realPathJson);
                String realPath = j.getString(hashmap.get(key));
                Downimg.downloadPicture(realPath, saveImgPath, key);
                sum++;
                status.setStage(sum);
                //计算百分比保留2位小数
                String format = numberFormat.format((float) sum / (float) totalCount * 100);
                double bfb=Double.parseDouble(format);
                status.setCurrent(bfb);
//        	count++;
//        	System.out.println("count......"+count);
            }
            //        清空map
            map1.clear();
            hashmap.clear();
            arr.clear();
            /*此处可以添加多线程A*/
            //判断是否结束
            if (lastMatchedMoment.equals("EOF")) {
                System.out.println("全部下载完成");
                status.setOverflag(1);
                status.setDowbUrl("http://www.baidu.com");
                break;
            }
        }

    }
}
