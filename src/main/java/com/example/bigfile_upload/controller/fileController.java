package com.example.bigfile_upload.controller;/*
 *项目名: bigFile_upload
 *文件名: fileController
 *创建者: KLH
 *创建时间:2024/3/20 20:59
 *描述: TODO

 */

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.Logger;

@RestController

@RequestMapping("/file")

public class fileController {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(fileController.class);

    @Value("${slice.saveDir}")
    private String saveDir;

    private static final Map<String,Integer> fileCountMap = new ConcurrentHashMap<>();

    @PostMapping("/uploadBySlice")
    public float upload(MultipartFile file,String fileName,String fileMd5,Integer chunkCount,Integer currentIndex) throws FileNotFoundException {
        String tempFilePath = saveDir + File.separator + fileMd5 + ".tmp";
        checkFilePath();
        try(RandomAccessFile accessFile = new RandomAccessFile(tempFilePath,"rw")){
            accessFile.seek(currentIndex);
            accessFile.write(file.getBytes());
            fileCountMap.put(fileMd5,fileCountMap.getOrDefault(fileMd5,0)+1);
            if(Objects.equals(fileCountMap.get(fileMd5),chunkCount)){
                File tempFile = new File(tempFilePath);
                File finallyFile = new File(saveDir + File.separator + fileName);
                tempFile.renameTo(finallyFile);
                tempFile.delete();
                logger.info("上传完成：{}",saveDir+File.separator + fileName);
                fileCountMap.remove(fileMd5);
                return 1;
            }else{
                logger.info("{}-{}件正在上传",fileMd5,currentIndex);
                return (float)fileCountMap.get(fileMd5)/chunkCount;
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void checkFilePath(){
        File file = new File(saveDir);
        if(!file.exists()){
            file.mkdirs();
        }
    }
}
