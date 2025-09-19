package com.roc.service.impl;

import com.roc.config.UploadConfig;
import com.roc.entity.filemanage.FileInfo;
import com.roc.entity.Result;
import com.roc.entity.filemanage.UploadFileDTO;
import com.roc.entity.filemanage.UserQuota;
import com.roc.mapper.FileMapper;
import com.roc.mapper.UserQuotaMapper;
import com.roc.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private UserQuotaMapper userQuotaMapper;
    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public Result uploadFile(UploadFileDTO uploadFileDTO) {
        MultipartFile file = uploadFileDTO.getFile();
        Integer userId = uploadFileDTO.getUserId();
        String remark = uploadFileDTO.getRemark();

        try {
            // 1. 检查用户空间是否足够
            UserQuota userQuota = userQuotaMapper.selectByUserId(userId);
            if (userQuota == null) {
                return Result.error("用户空间不存在");
            } else if (userQuota.getUsedQuota() + file.getSize() > userQuota.getTotalQuota()) {
                return Result.error("用户空间不足");
            }

            // 2.创建存储目录(如果不存在)
            // String path = uploadConfig.getPath();
            File dir = new File(uploadConfig.getPath());
            if (!dir.exists()) {
                boolean isCreated = dir.mkdirs();
                if (!isCreated) {
                    log.error("无法创建头像存储目录: {}", dir.getAbsolutePath());
                    throw new RuntimeException("文件存储路径创建失败");
                }
            }

            // 3.获取原始文件名并验证
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.error("文件名无效");
            }
            // 检查是否包含有效的文件扩展名
            if (!originalFilename.contains(".")) {
                return Result.error("文件格式无效");
            }

            // 4. 生成唯一文件名
            // String fileName = System.currentTimeMillis() + "_" + originalFilename;
            String fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));// 从原始文件名中提取文件扩展名
            String newFilename = UUID.randomUUID().toString() + fileExt; // 使用UUID生成新的文件名
            try {
                // 5. 保存文件到本地
                File destFile = new File(dir, newFilename); // 创建目标文件
                /*
                  transferTo() 方法将上传的文件保存到目标文件
                  将内存中的文件数据写入磁盘
                  自动处理文件流的打开和关闭
                  完成实际的文件存储操作
                 */
                file.transferTo(destFile); // 执行文件保存
                log.info("文件保存成功: {}", destFile.getAbsolutePath());

            } catch (Exception e) {
                log.error("文件保存失败: {}", e.getMessage());
            }

            // 6. 保存文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setOriginalFilename(originalFilename);
            fileInfo.setNewFilename(newFilename);
            fileInfo.setPath(uploadConfig.getPath());
            fileInfo.setFileSize(file.getSize());
            fileInfo.setUserId(userId);
            fileInfo.setRemark(remark);
            // 保存到数据库
            /*
            在 MyBatis 中，当传入的参数是一个对象（比如 fileInfo）时，
            SQL 语句中的 #{xxx}实际上是访问该对象的属性（getter 方法），而不是直接访问局部变量。
             */
            fileMapper.insertFile(fileInfo);

            // 7. 更新用户空间使用量
            userQuota.setUsedQuota(userQuota.getUsedQuota() + file.getSize());
            userQuotaMapper.updateUserQuota(userQuota);

            return Result.success("文件上传成功！");

        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
