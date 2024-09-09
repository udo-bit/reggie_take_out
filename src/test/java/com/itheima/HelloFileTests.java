package com.itheima;

import com.itheima.reggie.ReggieTakeOutApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest(classes = ReggieTakeOutApplication.class)
public class HelloFileTests {

    @Value("${reggie.path}")
    private String basePath;

    @Test
    void testFile() throws IOException {
        File file = new File(basePath);
        boolean exists = file.exists();
        System.out.println(exists);


        FileOutputStream fos = new FileOutputStream(new File(basePath + "test.txt"));
        fos.write("hello".getBytes());
        fos.flush();
        fos.close();


    }

    @Test
    void testFile2() throws IOException {
        File file = new File(basePath);
        System.out.println(file.getAbsoluteFile());
    }


}
