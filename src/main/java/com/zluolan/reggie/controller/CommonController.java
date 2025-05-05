package com.zluolan.reggie.controller;

import com.zluolan.reggie.common.R;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * MultipartFile 是SpringMVC提供的文件上传对象，用于接收前端上传的文件
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) { //参数: file必须和前端的file的name属性值相同
        // file是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        log.info(file.toString());

        // 原始文件名
        String originalFilename = file.getOriginalFilename();// xxx.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));// 获取文件后缀

        String fileName = UUID.randomUUID().toString() + suffix; // 文件名，唯一

        // 创建一个目录对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            // 目录不存在，需要创建
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("文件上传失败");
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     *
     * @param httpServletResponse
     * @param name
     * @return
     */
    @RequestMapping("/download")
    public void download(HttpServletResponse httpServletResponse, String name) {

        try {
            // 输入流，通过输入流读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            httpServletResponse.setContentType("image/jpeg");
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) != -1) {
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
