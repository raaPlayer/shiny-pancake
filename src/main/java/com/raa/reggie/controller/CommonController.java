package com.raa.reggie.controller;

import com.raa.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    //读取yaml文件配置
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
//        MultipartFile对象是在%Temp%下的一个临时文件，需要转存，在本次请求完成后会自动删除

        //原始文件名
        String originalFilename = file.getOriginalFilename();
        //TODO 原文件名可能不带. 用UUID确保文件不重名
        String suffix = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //确保目录存在
        File dir = new File(basePath);
        if(!dir.exists()){
            //警告,要处理返回结果
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(basePath + suffix));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(suffix);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            FileInputStream fileInputStream = new FileInputStream(basePath + name);

            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
