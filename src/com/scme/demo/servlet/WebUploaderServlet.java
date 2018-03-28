package com.scme.demo.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet(name = "WebUploaderServlet")
public class WebUploaderServlet extends HttpServlet {

    private static final String uploadPath = "D://upload/";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
        servletFileUpload.setHeaderEncoding("UTF-8");
        String fileMd5 = "";
        String chunk = "";
        try {
            List<FileItem> fileItems = servletFileUpload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    String fieldName = fileItem.getFieldName();
                    if (fieldName.equals("fileMd5")) {
                        fileMd5 = fileItem.getString();
                    }
                    if (fieldName.equals("chunk")) {
                        chunk = fileItem.getString();
                    }
                } else {
                    // 创建一个唯一目录，保存分块文件
                    File file = new File(uploadPath + fileMd5);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    // 保存文件
                    File chunkFile = new File(uploadPath + fileMd5 + "/" + chunk);
                    FileUtils.copyInputStreamToFile(fileItem.getInputStream(), chunkFile);
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
