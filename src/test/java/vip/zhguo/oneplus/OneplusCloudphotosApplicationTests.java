package vip.zhguo.oneplus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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


}
