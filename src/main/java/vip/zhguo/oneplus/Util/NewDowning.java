package vip.zhguo.oneplus.Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NewDowning {
    public static String postServer(String data, String urlpath, String cookie) throws Exception {
        String result = HttpClientUtil.requestPostString(urlpath, data, cookie);
        return result;
    }
    //根据id得到无压缩图片地址
    public static String getRealPath(String Imgid, String cookie) throws Exception {
        String urlpath = "https://cloud.h2os.com/gallery/pc/getRealPhotoUrls";
        String data = "ids=[\"" + Imgid + "\"]";
        System.out.println("imgId"+"====="+data);

        String realImgPath = postServer(data, urlpath, cookie);
        return realImgPath;



    }
}
