package com.chinacreator.sw.aigc.qwen2_5_vl.demos;

import com.alibaba.dashscope.exception.UploadFileException;
import lombok.Data;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>Description: 描述</p>
 * <p>Copyright: Copyright (c) 2025</p>
 * <p>Company: ChinaCreator（湖南科创）</p>
 *
 * @author ycl
 * @version 1.0
 * @date 2025/2/18
 */
@Data
public class ModelParam {
    // 百炼API-KEY
    String apiKey;
    // 百炼模型名称
    String apiModel;
    // 待测试的文件夹路径 windows格式 比如 C://Users/asus/Desktop
    String apiTestFolder;
    // 输出图片的路径 文件夹不存在会自动创建
    String apiOutputFolder;
    // 提示词
    String apiPrompt;
}
