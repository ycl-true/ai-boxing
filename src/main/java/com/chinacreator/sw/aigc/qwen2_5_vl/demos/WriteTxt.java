package com.chinacreator.sw.aigc.qwen2_5_vl.demos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>Description: 描述</p>
 * <p>Copyright: Copyright (c) 2025</p>
 * <p>Company: ChinaCreator（湖南科创）</p>
 *
 * @author ycl
 * @version 1.0
 * @date 2025/2/17
 */
public class WriteTxt {
    public void write(String content, String path) {
        // 创建文件对象
        File file = new File(path);
        if(!file.exists()){
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try {
            // 创建FileWriter对象
            fileWriter = new FileWriter(file);
            // 创建BufferedWriter对象
            bufferedWriter = new BufferedWriter(fileWriter);

            // 写入内容
            bufferedWriter.write(content);
            System.out.println("坐标写入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
