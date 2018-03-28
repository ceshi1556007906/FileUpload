package com.scme.demo.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

@WebServlet(name = "UploadActionServlet")
public class UploadActionServlet extends HttpServlet {

    private static final String uploadPath = "D://upload/";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action.equals("mergeChunks")) {
            // 合并文件
            String fileMd5 = request.getParameter("fileMd5");
            String fileName = request.getParameter("fileName");
            File f = new File(uploadPath + fileMd5);
            // 排除目录，只要文件
            File[] fileArray = f.listFiles(pathname -> {
                if (pathname.isDirectory()) {
                    return false;
                }
                return true;
            });
            List<File> fileList = new ArrayList<File>(Arrays.asList(fileArray));
            Collections.sort(fileList, (o1, o2) -> {
                if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                    return -1;
                }
                return 1;
            });
            File outputFile = new File(uploadPath + fileName);
            // 创建文件
            outputFile.createNewFile();
            FileChannel outChannel = new FileOutputStream(outputFile).getChannel();
            // 合并
            FileChannel inChannel;
            for (File file : fileList) {
                inChannel = new FileInputStream(file).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                inChannel.close();

                // 删除分块
                file.delete();
            }
            // 清楚文件夹
            File tempFile = new File(uploadPath + fileMd5);
            if (tempFile.isDirectory() && tempFile.exists()) {
                tempFile.delete();
            }
            System.out.println("合并成功");
        }
    }
}
