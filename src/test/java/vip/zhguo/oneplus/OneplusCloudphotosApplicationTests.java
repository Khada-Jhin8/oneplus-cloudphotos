package vip.zhguo.oneplus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vip.zhguo.oneplus.Util.Zip;

import java.text.NumberFormat;

@SpringBootTest
class OneplusCloudphotosApplicationTests {


    @Test
    void contextLoads() {
        int a=40;
        int b=401;
        double c =((float) a/(float) b*100);
        NumberFormat numberFormat =NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        String format = numberFormat.format(c);
        System.out.println(format);
        System.out.println((float) a/(float) b);
        System.out.println(c);
    }
    @Test
    void test(){
        String targetFolderPath = "D:\\down\\ce970714-2607-4e42-9413-26aef45c9e28";
        String newZipFilePath = "D:\\down\\new.zip";

    }
@Test
    void test1(){
        Integer a = 1;
        Integer b = 1;
        Integer c = 500;
        Integer d = 500;
        int f=2091;
        Integer e=2091;
        System.out.println(a == b);
        System.out.println(c == d);
        System.out.println(f == e);
    }


}
