package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {
    
    // 定义文件存储路径
    private static final String FILE_STORAGE_PATH = System.getProperty("user.dir") + File.separator + "file";
    
    /**
     * 上传头像文件
     * @param file 上传的文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ApiResponse uploadFile(@RequestParam("file") MultipartFile file,@RequestParam("user") String user) {
        User user1=JSONObject.parseObject(user,User.class);
//        System.out.println("jsonObject = " + jsonObject);

        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.failure("文件不能为空");
        }
        
        try {
            // 创建文件存储目录（如果不存在）
            File storageDir = new File(FILE_STORAGE_PATH);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (!StringUtils.hasText(originalFilename)) {
                return ApiResponse.failure("文件名不能为空");
            }
            
            // 生成唯一文件名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 构建文件存储路径
            Path filePath = Paths.get(FILE_STORAGE_PATH, uniqueFilename);
            
            // 保存文件
            file.transferTo(filePath);
//            User username = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("username").is(user1.getUsername())), User.class);
            user1.setAvatarUrl("/file/"+uniqueFilename);
            mongoTemplate.save(user1);

            // 返回成功响应，包含文件名
            return ApiResponse.success(uniqueFilename, "文件上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.failure("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 通用文件上传（不修改用户头像）
     * @param file 上传的文件
     * @return 上传结果，包含文件路径
     */
    @PostMapping("/upload-file")
    public ApiResponse uploadGeneralFile(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ApiResponse.failure("文件不能为空");
        }
        
        try {
            // 创建文件存储目录（如果不存在）
            File storageDir = new File(FILE_STORAGE_PATH);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (!StringUtils.hasText(originalFilename)) {
                return ApiResponse.failure("文件名不能为空");
            }
            
            // 生成唯一文件名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // 构建文件存储路径
            Path filePath = Paths.get(FILE_STORAGE_PATH,uniqueFilename);
            
            // 保存文件
            file.transferTo(filePath);
            
            // 返回成功响应，包含文件路径
            String fileUrl = "/file/" + uniqueFilename;
            String name = file.getName();
            JSONObject object=new JSONObject();
            object.put("name", uniqueFilename);
            object.put("originalFilename",originalFilename);
            object.put("fileUrl", fileUrl);
            object.put("id",uniqueFilename);

            return ApiResponse.success(object, "文件上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.failure("文件上传失败: " + e.getMessage());
        }
    }
}