package vip.zhguo.oneplus.Util;

import java.io.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import sun.misc.BASE64Encoder;

/**
 * <p>Title: Downimg.java</p>
 *
 * <p>Description:下载工具 </p>
 *
 * <p>Copyright: Copyright (c) 2017</p>
 *
 * <p>Company: www.zhguo.vip</p>
 *
 * @author zhenghuiguo
 * @date 2019-8-23
 * @version 1.0
 */

/**
 * <p>Title: Downimg</p>
 * <p>Description: </p>
 *
 * @author Zhenghuiguo
 * @date 2019-8-23
 */
public class Downimg extends HttpServlet {
    //Post请求
    public static String PostServer(String data, String urlpath, String cookie) {
        PrintWriter out = null;
        BufferedReader is = null;
        String result = "";
        try {
            URL url = new URL(urlpath);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//	            PrintWriter out = null;

            /**设置URLConnection的参数和普通的请求属性****start***/
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0");
            conn.setRequestProperty("Host", "cloud.h2os.com");
            conn.setRequestProperty("referer", "https://cloud.h2os.com/");

            /**设置URLConnection的参数和普通的请求属性****end***/
            conn.setRequestProperty("Cookie", cookie);
            conn.setRequestMethod("POST");//GET和POST必须全大写
            //设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            //最常用的Http请求无非是get和post，get请求可以获取静态页面，也可以把参数放在URL字串后面，传递给servlet，
            //post与get的 不同之处在于post的参数不是放在URL字串里面，而是放在http请求的正文内。
            conn.setDoOutput(true);
            conn.setDoInput(true);
            /***POST方法请求****start*/
            out = new PrintWriter(conn.getOutputStream());//获取URLConnection对象对应的输出流
            out.print(data);//发送请求参数即数据
            out.flush();//缓冲数据
            /***POST方法请求****end*/

            // 定义BufferedReader输入流来读取URL的响应
            is = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = is.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
            return "posterror";
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }
        }
        return result;
    }


    //根据id得到无压缩图片地址
    public static String getRealPath(String Imgid, String cookie) {
        String urlpath = "https://cloud.h2os.com/gallery/pc/getRealPhotoUrls";
        String data = "ids=[\"" + Imgid + "\"]";
//			 System.out.println("imgId"+"====="+data);
        String realImgPath = PostServer(data, urlpath, cookie);
        return realImgPath;
    }

    //链接url下载图片
    public static void downloadPicture(String urlPath, String savePath, String key) {
        URL url = null;
        try {
            url = new URL(urlPath);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            File filedir = new File(savePath);
            if (!filedir.exists()) {
                filedir.mkdirs();
            }
            File file = new File(filedir + "/" + key);
            if (!file.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int length;

                while ((length = dataInputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
                BASE64Encoder encoder = new BASE64Encoder();
                String encode = encoder.encode(buffer);//返回Base64编码过的字节数组字符串
//	            System.out.println(encode);
                fileOutputStream.write(output.toByteArray());
                dataInputStream.close();
                fileOutputStream.close();
            } else {
                System.out.println("文件已经存在");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //下载压缩文件
    public static String download(String path, HttpServletResponse response) {
//        try {
//            // path是指欲下载的文件的路径。
            File file = new File(path);
//            // 取得文件名。
            String filename = file.getName();
//            // 取得文件的后缀名。
//            String ext = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();
//
//            // 以流的形式下载文件。
//            InputStream fis = new BufferedInputStream(new FileInputStream(path));
//            byte[] buffer = new byte[fis.available()];
//            fis.read(buffer);
//            fis.close();
//            // 清空response
//            response.reset();
//            // 设置response的Header
//            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
//            response.addHeader("Content-Length", "" + file.length());
//            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
//            response.setContentType("application/octet-stream");
//            toClient.write(buffer);
//            toClient.flush();
//            toClient.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return response;
        if(file.exists()){ //判断文件父目录是否存在
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            // response.setContentType("application/force-download");
            try {
                response.setHeader("Content-Disposition", "attachment;fileName=" +   java.net.URLEncoder.encode(filename,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte[] buffer = new byte[1024];
            FileInputStream fis = null; //文件输入流
            BufferedInputStream bis = null;

            OutputStream os = null; //输出流
            try {
                os = response.getOutputStream();
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                int i = bis.read(buffer);
                while(i != -1){
                    os.write(buffer);
                    i = bis.read(buffer);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("----------file download---" + filename);
            try {
                bis.close();
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;

    }
}
