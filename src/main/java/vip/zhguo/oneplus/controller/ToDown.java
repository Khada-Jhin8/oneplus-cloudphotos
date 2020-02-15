package vip.zhguo.oneplus.controller;
import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;
import vip.zhguo.oneplus.Util.Downimg;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ToDown {
    @GetMapping("/")
    public  String home(){
        return "home";
    }
    @PostMapping("/down")
    @ResponseBody
    public void down(String cookie, HttpServletRequest res) throws  Exception{
//		int sum=0;
//		int count=0;
//		int initialCapacity=256;
        ArrayList<String> arr= new ArrayList<String>();
        Map<String, String> hashmap=new HashMap<String, String>();
        System.out.println("cookie："+cookie);
        System.out.println(res.getServletPath()+"/");
        //文件保存路径
        String saveImgPath=res.getServletPath()+"/"+UUID.randomUUID();
        //String saveImgPath="C://Users//zhg//Desktop//img//";
        //获取相册列表
        //cursor=20190822&photoIndex=1
        //lastMatchedMoment   realPhotoIndex
        //初始化数据
        String data="size=100&state=active&smallPhotoScaleParams=image/resize,m_mfit,h_250,w_250&originalPhotoScaleParams=image/resize,m_mfit,h_1300,w_1300";
        String urlpath="https://cloud.h2os.com/gallery/pc/listNormalPhotos";
        //String cookie="acw_tc=784c10e815665472969636674eaf2fc0aad2480c2a19c73d5583ea4b245cca; _uab_collina=156654729729478340591497; opcloud_sid=f4d6a4d85b72477d98cb609938bf6dd3; opcloud_token=ONEPLUS_cdJMSApHGEmsJYxjYfUcXyWk24LTaAqphN; NEARME_ACCOUNTNAME_COOKIE=M1563165562682; opcloud_vcode=fdc8e702c3774c48acf3d0546d41229b; accountName=M1563165562682";
        while(true){
            //post请求得到json数据
            String postServer = Downimg.PostServer(data, urlpath, cookie);
            //重置data基础数据
            data="size=100&state=active&smallPhotoScaleParams=image/resize,m_mfit,h_250,w_250&originalPhotoScaleParams=image/resize,m_mfit,h_1300,w_1300";
            System.out.println("postServer"+"======"+postServer);
            JSONObject jobj = JSON.parseObject(postServer);
            System.out.println("下一个时间段"+jobj.getString("lastMatchedMoment")+"....."+"下一个index"+jobj.getString("realPhotoIndex"));
            //获取下一页游标
            String lastMatchedMoment=(jobj.getString("lastMatchedMoment"))== null?null:jobj.getString("lastMatchedMoment");
            String photoIndex=(jobj.getString("realPhotoIndex"))== null?null:jobj.getString("realPhotoIndex");
            //判断下一页是否有值
            if(lastMatchedMoment!=null&&!"".equals(lastMatchedMoment)&&photoIndex!=null&&!"".equals(photoIndex)){
                data=data+"&cursor="+lastMatchedMoment+"&photoIndex="+photoIndex;
            }
            //System.out.println("name"+jobj.get("photos"));
            //拿到photos节点
            JSONObject photosjson = jobj.getJSONObject("photos");
            //将photosjson赋值给map1，便于后面遍历时间节点。由于时间节点不固定，无法准确拿到json数据。
            Map map1 = photosjson;
            //遍历出key值存入arr

            for (Object o : map1.entrySet()) {
                Map.Entry<String,JSONArray> entry = (Map.Entry<String,JSONArray>)o;
                arr.add(entry.getKey());
            }
            //遍历每个时间节点的值
            for (int i = 0; i < arr.size(); i++) {
                JSONArray photos = photosjson.getJSONArray(arr.get(i));
                //遍历每个时间节点下的多个照片
                for (int j = 0; j < photos.size(); j++) {
                    JSONObject phototimeJson = (JSONObject) photos.get(j);
                    String title=phototimeJson.getString("title");
                    //得到图片id,存入hashmap<"title","id">
                    String img_id=phototimeJson.getString("id");
                    hashmap.put(title, img_id);
                }

            }
            for(String key : hashmap.keySet()){
                String realPathJson = Downimg.getRealPath(hashmap.get(key), cookie);
                JSONObject j=JSONObject.parseObject(realPathJson);
                String  realPath = j.getString(hashmap.get(key));
                Downimg.downloadPicture(realPath, saveImgPath,key);
//        	count++;
//        	System.out.println("count......"+count);
            }
            if(lastMatchedMoment.equals("EOF")){
                System.out.println("下载完成");
                return ;
            }
//        sum+=count;
//        count=0;
//        System.out.println("........"+"all。。。。。。。。。。。"+sum);
            //清空数据
            map1.clear();
            hashmap.clear();
            arr.clear();
            System.out.println("清空完成");
           // return "服务器下载完成";
        }
    }

}
