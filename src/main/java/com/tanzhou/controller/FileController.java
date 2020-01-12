package com.tanzhou.controller;

import com.tanzhou.dto.FileDTO;
import com.tanzhou.provider.UCloudProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class FileController {

    @Autowired
    private UCloudProvider uCloudProvider;

    @RequestMapping("/file/upload")
    @ResponseBody
    public FileDTO upload(HttpServletRequest request){
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartHttpServletRequest.getFile("editormd-image-file");
        try {
            String fileUrl = uCloudProvider.upload(file.getInputStream(),file.getContentType(),file.getOriginalFilename());
            FileDTO fileDTO = new FileDTO();
            fileDTO.setMessage("上传成功");
            fileDTO.setSuccess(1);
            fileDTO.setUrl(fileUrl);
            return fileDTO;
        } catch (IOException e) {
            e.printStackTrace();
            FileDTO fileDTO = new FileDTO();
            fileDTO.setMessage("上传失败");
            fileDTO.setSuccess(0);
            return  fileDTO;
        }
    }
}
