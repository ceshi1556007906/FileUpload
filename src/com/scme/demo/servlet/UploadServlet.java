package com.scme.demo.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UploadServlet extends javax.servlet.http.HttpServlet {

    private static final String uploadPath = "D://upload/";

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        // 创建diskFileItemFactory对象，配置缓存信息
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // 创建servletFileUpload
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        // 设置编码格式
        servletFileUpload.setHeaderEncoding("UTF-8");
        // 开始解析文件
        try {
            List<FileItem> fileItems = servletFileUpload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {// 数据
                    String fileName = fileItem.getFieldName();
                    if (fileName.equals("info")) {
                        // 获取文件信息
                        String info = fileItem.getString("utf-8");
                        System.out.println(info);
                    }
                } else {// 文件
                    InputStream inputStream = fileItem.getInputStream();
                    // 获取文件名字
                    String fileName = fileItem.getName();
                    FileUtils.copyInputStreamToFile(inputStream, new File(uploadPath + fileName));
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        this.doPost(request, response);
    }
}
