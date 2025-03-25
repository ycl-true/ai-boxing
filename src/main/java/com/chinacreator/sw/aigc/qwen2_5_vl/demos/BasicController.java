/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chinacreator.sw.aigc.qwen2_5_vl.demos;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

@Controller
public class BasicController {
    @Value("${api.key}")
    String apiKey;
    @Value("${api.model}")
    String apiModel;
    @Value("${api.test.folder}")
    String apiTestFolder;
    @Value("${api.output.folder}")
    String apiOutputFolder;
    @Value("${api.prompt}")
    String apiPrompt;


    // 返回归一化坐标
    // http://127.0.0.1:8080/run?modelName=qwen2.5-vl-3b-instruct
    @RequestMapping(value = "/run", method = RequestMethod.POST)
    @ResponseBody
    public String run(ModelParam modelParam) throws NoApiKeyException, UploadFileException {
        // 读取apiTestFolder文件夹下的所有图片
        File folder = new File(modelParam.getApiTestFolder());
        File[] files = folder.listFiles();

        long totalToken = 0L;
        int index = 0;

        long totalTime = 0L;
        StringBuilder errorSb = new StringBuilder();
        for (File file : files) {
            // 判断图片像素大小
            Graphics2D g2d = null;
            String absolutePath = file.getAbsolutePath();
            int originalWidth = 0;
            int originalHeight = 0;
            try {
                BufferedImage originalImage = ImageIO.read(new File(absolutePath));
                originalWidth = originalImage.getWidth();
                originalHeight = originalImage.getHeight();

                if (!(originalWidth <= 1280 && originalHeight <= 784)) {
//                    double widthRatio = (double) 1280 / originalWidth;
//                    double heightRatio = (double) 784 / originalHeight;
//                    double ratio = Math.min(widthRatio, heightRatio);
//
//                    int newWidth = (int) (originalWidth * ratio);
//                    int newHeight = (int) (originalHeight * ratio);

//                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
                    BufferedImage resizedImage = new BufferedImage(1280, 784, originalImage.getType());
                    originalWidth = 1280;
                    originalHeight = 784;
                    g2d = resizedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g2d.drawImage(originalImage, 0, 0, 1280, 784, null);
                    g2d.dispose();
                    //获取图片后缀
                    String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    //将resizedImage生成新图并覆盖旧图
                    ImageIO.write(resizedImage, fileExtension, new File(absolutePath));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if(g2d != null){
                    g2d.dispose();
                }
            }

            String origin = absolutePath;
            // 获取图片绝对路径
            // C:\Users\asus\Desktop\test\11825996402262016-7221402-1722002129(1).jpg
            absolutePath = "file:///" + absolutePath.replace("\\", "/");
            System.out.println("图片路径:" + absolutePath);
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("text", "You are a helpful Object recognition assistant."))).build();
            String finalAbsolutePath = absolutePath;
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            new HashMap<String, Object>(){{put("image", finalAbsolutePath);}},
                            Collections.singletonMap("text", modelParam.getApiPrompt()))).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                    .apiKey(modelParam.getApiKey())
                    .model(modelParam.getApiModel())
//                .parameter("vl_high_resolution_images", true)
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .build();
            //记录开始时间
            long startTime = System.currentTimeMillis();
            ++index;
            MultiModalConversationResult result = null;
            try {
                result = conv.call(param);
            } catch (Exception e) {
                e.printStackTrace();
                errorSb.append("第" + index + "张图片出错：路径为" + origin);
                continue;
            }
            long costTime = System.currentTimeMillis() - startTime;
            System.out.println("第" + index + "张图片耗时：" + costTime + "ms");
            totalTime += costTime;
            if(result.getUsage() != null){
                totalToken += result.getUsage().getTotalTokens();
            }
            String text = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            System.out.println("结果："+text);
            final String substringFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
            if("0".equals(text) || text.length() <= 13){
                System.out.println("第" + index + "张图片没有目标：路径为" + origin);
                // 挪照片并生成空的txt
                try {
                    DrawImage.copyFileToDirectory(origin,modelParam.apiOutputFolder);
                    WriteTxt writeTxt = new WriteTxt();
                    writeTxt.write("", modelParam.apiOutputFolder + "/location/" + substringFileName + ".txt" );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            // 去除text前8位和后4位
            text = text.substring(8, text.length() - 4);
            //text转json
            JSONArray jsonArray = JSONArray.parseArray(text);
            List<int[]> rectangles = new ArrayList<>();
            StringBuilder write = new StringBuilder();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray bbox_2d = jsonObject.getJSONArray("bbox_2d");
                int[] intArray = new int[bbox_2d.size()];
                write.append("0 ");
                for (int j = 0; j < bbox_2d.size(); j++) {
                    intArray[j] = bbox_2d.getInteger(j);
                }
                convertToYoloFormat(intArray[0],intArray[1],intArray[2],intArray[3], originalWidth, originalHeight, write);
                //多个框
                if(i != jsonArray.size() - 1){
                    write.append("\n");
                }
                rectangles.add(intArray);
            }
            write.append("\n");
            // 绘制矩形框
            DrawImage drawImage = new DrawImage();
            drawImage.drawMultipleRectanglesOnImage(
                    origin, // 输入图片路径
                    rectangles,                     // 包含多个矩形坐标的列表
                    modelParam.apiOutputFolder + "/" + file.getName() // 输出图片路径
            );
            WriteTxt writeTxt = new WriteTxt();
            writeTxt.write(write.toString(), modelParam.apiOutputFolder + "/location/" + substringFileName + ".txt" );
        }

        return "成功，共" + index + "张图片，共消耗" + totalToken + "个token，接口调用耗时" + totalTime + "ms\n"
                + (errorSb.length() > 0 ? "错误：" + errorSb.toString() : "");
    }

    public void convertToYoloFormat(int x1, int y1, int x2, int y2, int originalWidth, int originalHeight, StringBuilder sb) {
        //计算边界框的宽度和高度
        double width = x2 - x1;
        double height = y2 - y1;

        //计算边界框中心点的坐标
        double cx = (double) (x1 + x2) / 2.0;
        double cy = (double) (y1 + y2) / 2.0;

        //将这些值归一化到[0, 1]范围内
        double normalized_cx = cx / originalWidth;
        double normalized_cy = cy / originalHeight;
        double normalized_w = width / originalWidth;
        double normalized_h = height / originalHeight;
        sb.append(String.format("%.6f", normalized_cx)).append(" ")
                .append(String.format("%.6f", normalized_cy)).append(" ")
                .append(String.format("%.6f", normalized_w)).append(" ")
                .append(String.format("%.6f", normalized_h));
    }


    @RequestMapping("/run2")
    @ResponseBody
    public String run2(ModelParam modelParam) throws NoApiKeyException, UploadFileException {
        // 读取apiTestFolder文件夹下的所有图片
        File folder = new File(modelParam.getApiTestFolder());
        File[] files = folder.listFiles();

        long totalToken = 0L;
        int index = 0;
        StringBuilder write = new StringBuilder();
        long totalTime = 0L;
        for (File file : files) {
            // 判断图片像素大小
            Graphics2D g2d = null;
            String absolutePath = file.getAbsolutePath();
            try {
                BufferedImage originalImage = ImageIO.read(new File(absolutePath));
                int originalWidth = originalImage.getWidth();
                int originalHeight = originalImage.getHeight();

                if (!(originalWidth <= 1280 && originalHeight <= 784)) {
//                    double widthRatio = (double) 1280 / originalWidth;
//                    double heightRatio = (double) 784 / originalHeight;
//                    double ratio = Math.min(widthRatio, heightRatio);
//
//                    int newWidth = (int) (originalWidth * ratio);
//                    int newHeight = (int) (originalHeight * ratio);

//                    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
                    BufferedImage resizedImage = new BufferedImage(1280, 784, originalImage.getType());
                    g2d = resizedImage.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//                    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                    g2d.drawImage(originalImage, 0, 0, 1280, 784, null);
                    g2d.dispose();
                    //获取图片后缀
                    String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    //将resizedImage生成新图并覆盖旧图
                    ImageIO.write(resizedImage, fileExtension, new File(absolutePath));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if(g2d != null){
                    g2d.dispose();
                }
            }

            String origin = absolutePath;
            // 获取图片绝对路径
            // C:\Users\asus\Desktop\test\11825996402262016-7221402-1722002129(1).jpg
            absolutePath = "file:///" + absolutePath.replace("\\", "/");
            System.out.println("图片路径:" + absolutePath);
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage systemMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("text", "You are a helpful Object recognition assistant."))).build();
            String finalAbsolutePath = absolutePath;
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(Arrays.asList(
                            new HashMap<String, Object>(){{put("image", finalAbsolutePath);}},
                            Collections.singletonMap("text", modelParam.getApiPrompt()))).build();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                    .apiKey(modelParam.getApiKey())
                    .model(modelParam.getApiModel())
//                .parameter("vl_high_resolution_images", true)
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .build();
            //记录开始时间
            long startTime = System.currentTimeMillis();
            MultiModalConversationResult result = conv.call(param);
            ++index;
            long costTime = System.currentTimeMillis() - startTime;
            System.out.println("第" + index + "张图片耗时：" + costTime + "ms");
            totalTime += costTime;
            if(result.getUsage() == null){
                totalToken += result.getUsage().getTotalTokens();
            }
            String text = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            System.out.println("结果："+text);
            if("0".equals(text) || text.length() <= 13){
                System.out.println("第" + index + "张图片没有目标：路径为" + origin);
                continue;
            }
            // 去除text前8位和后4位
            text = text.substring(8, text.length() - 4);
            //text转json
            JSONArray jsonArray = JSONArray.parseArray(text);
            List<int[]> rectangles = new ArrayList<>();
            write.append(file.getName()).append(" ");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONArray bbox_2d = jsonObject.getJSONArray("bbox_2d");
                int[] intArray = new int[bbox_2d.size()];
                write.append("[");
                for (int j = 0; j < bbox_2d.size(); j++) {
                    intArray[j] = bbox_2d.getInteger(j);
                    write.append(bbox_2d.getInteger(j));
                    if(j != bbox_2d.size() - 1){
                        write.append(",");
                    }
                }
                write.append("]");
                if(i != jsonArray.size() - 1){
                    write.append(",");
                }
                rectangles.add(intArray);
            }
            write.append("\n");
            // 绘制矩形框
            DrawImage drawImage = new DrawImage();
            drawImage.drawMultipleRectanglesOnImage(
                    origin, // 输入图片路径
                    rectangles,                     // 包含多个矩形坐标的列表
                    modelParam.apiOutputFolder + "/" + file.getName() // 输出图片路径
            );
        }
        WriteTxt writeTxt = new WriteTxt();
        writeTxt.write(write.toString(), modelParam.apiOutputFolder + "/location.txt" );

        return "成功，共" + index + "张图片，共消耗" + totalToken + "个token，接口调用耗时" + totalTime + "ms";
    }


    public static BufferedImage resizeImage(BufferedImage originalImage) {
        int maxWidth = 1280;
        int maxHeight = 784;
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return originalImage;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    // 示例方法，用于测试resizeImage方法
    public static void testResizeImage() {
        try {
            BufferedImage originalImage = ImageIO.read(new File("C://Users/asus/Desktop/3.jpg"));
            BufferedImage resizedImage = resizeImage(originalImage);
            ImageIO.write(resizedImage, "jpg", new File("C://Users/asus/Desktop/resized_image.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testResizeImage();
    }

}
