<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2018/2/12
  Time: 16:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>WebUploader实现文件上传</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webuploader/webuploader.css" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/webuploader/jquery-1.10.2.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/webuploader/webuploader.js"></script>
    <style type="text/css">
        #dndArea {
            width: 200px;
            height: 100px;
            border: 1px dashed red;
        }
    </style>

</head>
<body>
    <!-- 设计页面元素 -->
    <div id="fileUploader">
        <!-- 文件拖拽区 -->
        <div id="dndArea"></div>
        <div id="filePicker">点击选择文件</div>
        <!-- 显示文件列表信息 -->
        <div id="fileList"></div>
    </div>

    <script type="text/javascript">
        // 获取到了文件标记
        var fileMd5;
        // 监听分块上传文件过程的三个时间点
        WebUploader.Uploader.register({
            "before-send-file" : "beforeSendFile",
            "before-send" : "beforeSend",
            "after-send-file" : "afterSendFile"
        }, {
            // 时间点1：所有文件分块上传之前调用
            beforeSendFile : function (file) {
                var deferred = WebUploader.Deferred();
                // 1.计算文件的唯一标记，用于断点续传和秒转
                (new WebUploader.Uploader()).md5File(file, 0, 5*1024*1024)
                  .progress(function (percentage) {
                    $("#" + file.id).find("span.state").text("正在获取文件信息...");
                }).then(function (val) {
                    fileMd5 = val;
                    $("#" + file.id).find("span.state").text("");
                    deferred.resolve();
                });
                // 请求后台是否保存过该文件，如果存在，则跳过该文件，实现秒传功能
                return deferred.promise();
            },
            // 时间点2：如果有分块上传，则每个分块上传之前调用此函数
            beforeSend : function (fileBlock) {
                var deferred = WebUploader.Deferred();

                $.ajax({
                    type : "POST",
                    url : "${pageContext.request.contextPath}/uploadActionServlet?action=mergeChunks",
                    data : {
                        fileMd5 : fileMd5,
                        // 当前分块下标
                        chunk : fileBlock.chunk,
                        // 当前分块大小
                        chunkSize : fileBlock.end-fileBlock.start
                    },
                    dataType : "json",
                    success : function (response) {
                        if (response.ifExist) {
                            // 分块存在，调过该分块
                            deferred.reject();
                        } else {
                            // 分块不完整或不存在，重新发送分块内容
                            deferred.resolve();
                        }
                    }
                });

                this.owner.options.formData.fileMd5 = fileMd5;
                deferred.resolve();
                return deferred.promise();
                // 请求后台是否保存过该文件，如果存在，则跳过该文件，实现断点续传
            },
            // 时间点3：分块上传完之后调用
            afterSendFile : function (file) {
                $.ajax({
                    type : "POST",
                    url : "${pageContext.request.contextPath}/uploadActionServlet?action=mergeChunks",
                    data : {"fileMd5" : fileMd5, "fileName" : file.name},
                    success : function (response) {

                    }
                })
            }
        });
        // 初始化全局参数
        var uploader = WebUploader.create({
            // flash的地址
            swf : "${pageContext.request.contextPath}/webuploader/Uploader.swf",
            // 设置提交的服务器地址
            server : "${pageContext.request.contextPath}/webUploaderServlet",
            // 渲染文件上传元素
            pick : "#filePicker",
            // 自动上传
            auto : true,
            // 开启拖拽功能
            dnd : "#dndArea",
            // 屏蔽拖拽区域外的功能
            disableGlobalDnd : true,
            // 开启黏贴功能
            paste : "#fileUploader",
            // 分块上传
            chunked : true,
            // 每块文件大小(默认5M)
            chunkSize : 5*1024*1024
        });

        var i = 0;
        var length;

        // 选择文件后，文件消息队列展示
        // 当文件加入队列后触发
        // file代表当前选择的文件
        uploader.on("fileQueued", function (file) {
            // 追加文件信息
            $("#fileList").append("<div id='" + file.id + "' class='fileInfo'><img/>&nbsp;<span>" + file.name + " - </span><span class='percentage'></span><span class='state'></span></div>");
            // 制作缩略图
            // error：不是图片
            // src：代表生成后的缩略图地址
            uploader.makeThumb(file, function (error, src) {
                // 判断是否已经生成缩略图
                if (error) {
                    $("#" + file.id).find("img").replaceWith("无法预览");
                }
                // 成功
                $("#" + file.id).find("img").attr("src", src);
            });
        });

        // 在文件上传的过程中实现文件上传监控
        // percentage:代表文件上传百分比
        uploader.on("uploadProgress", function (file, percentage) {
            if (percentage && percentage == 1) {
                $("#" + file.id).find("span.percentage").text("已完成");
            } else {
                $("#" + file.id).find("span.percentage").text(Math.round(percentage * 100) + "%");
            }
        });
    </script>
</body>
</html>
