package com.chinacreator.sw.aigc.qwen2_5_vl.demos;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class DrawImage {
    /**
     * 拷贝文件到另一个文件夹。
     *
     * @param sourceFilePath 源文件的路径
     * @param targetDirectoryPath 目标文件夹的路径
     * @throws IOException 如果发生 I/O 错误
     */
    public static void copyFileToDirectory(String sourceFilePath, String targetDirectoryPath) throws IOException {
        // 将字符串路径转换为 Path 对象
        Path sourcePath = Paths.get(sourceFilePath);
        Path targetDirectory = Paths.get(targetDirectoryPath);

        // 确保目标目录存在，如果不存在则创建它
        if (!Files.exists(targetDirectory)) {
            Files.createDirectories(targetDirectory);
        }

        // 获取目标文件的完整路径（包括文件名）
        Path targetPath = targetDirectory.resolve(sourcePath.getFileName());

        // 执行文件拷贝操作
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) throws IOException {
        copyFileToDirectory("C:\\Users\\asus\\Desktop\\sleep_368.jpg", "C:\\Users\\asus\\Desktop\\test");
    }

    /**
     * 在指定的图片上根据多个对角点坐标对绘制矩形框。
     *
     * @param imagePath 图片路径
     * @param rectangles 一个List，每个元素都是一个包含四个整数的数组，分别表示矩形的左上角X, Y 和 右下角X, Y 坐标
     * @param outputImagePath 输出图片的路径
     */
    public void drawMultipleRectanglesOnImage(String imagePath, List<int[]> rectangles, String outputImagePath) {
        Graphics2D g2d = null;
        try {
            // 读取图片
            BufferedImage image = ImageIO.read(new File(imagePath));

            // 获取Graphics2D对象以便绘图
            g2d = image.createGraphics();

            // 设置颜色和线条宽度
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3)); // 线条宽度为3像素

            // 遍历所有矩形并绘制
            for (int[] rect : rectangles) {
                int topLeftX = rect[0];
                int topLeftY = rect[1];
                int bottomRightX = rect[2];
                int bottomRightY = rect[3];

                // 绘制矩形框
                g2d.drawRect(topLeftX, topLeftY, bottomRightX - topLeftX, bottomRightY - topLeftY);
            }

            // 释放资源
            g2d.dispose();

            File file = new File(outputImagePath);
            if(!file.exists()){
                file.mkdirs();
            }
            // 保存修改后的图片
            ImageIO.write(image, "png", file);

            System.out.println("矩形框已成功绘制并保存到: " + outputImagePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if(g2d != null){
                g2d.dispose();
            }
        }
    }

//    public static void main(String[] args) {
//        DrawImage drawImage = new DrawImage();
//
//        // 示例调用：准备多个矩形的坐标
//        List<int[]> rectangles = new ArrayList<>();
//        // VL-MAX-202501025=VL-72B  1366*768  3(1366_768).jpg   以矩形框形式定位图中人物
////        rectangles.add(new int[]{418, 59, 603, 542}); // 第一个矩形
////        rectangles.add(new int[]{667, 115, 836, 668}); // 第二个矩形
//
//        // VL-3B  1280*720    3(1280_720).jpg    以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{670, 115, 834, 669}); // 第二个矩形
////        rectangles.add(new int[]{390, 58, 603, 542}); // 第二个矩形
//
//        // VL-3B  1394*720    3(1280_720).jpg    以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{670, 115, 834, 669}); // 第二个矩形
////        rectangles.add(new int[]{390, 58, 603, 542}); // 第二个矩形
//
//        // VL-3B  高分辨率    3.jpg    以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{980, 174, 1226, 1003}); // 第二个矩形
////        rectangles.add(new int[]{615, 86, 886, 813}); // 第二个矩形
//
//        // VL-72B  1280*720  3(1280_720).jpg   以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{418, 58, 604, 544}); // 第一个矩形
////        rectangles.add(new int[]{670, 114, 835, 669}); // 第二个矩形
//
//        //豆包 1280*720   不行 幻觉
////        rectangles.add(new int[]{154, 224, 360, 611}); // 第二个矩形
////        rectangles.add(new int[]{442, 236, 610, 613}); // 第二个矩形
//
//        //秘塔ai  3(1280*720).jpg     以矩形框形式定位图中人物
////        rectangles.add(new int[]{445, 62, 642, 584}); // 第二个矩形
////        rectangles.add(new int[]{712, 126, 888, 720}); // 第二个矩形
//
//        // QVQ-72B-Preview 擅长推理
////        rectangles.add(new int[]{270, 70, 400, 730}); // 第二个矩形
////        rectangles.add(new int[]{520, 150, 610, 910}); // 第二个矩形
//
//        // kimi  3.jpg
////        rectangles.add(new int[]{(int)(0.31*1920), (int)(0.08*1080), (int)(0.45*1920), (int)(0.74*1080)}); // 第二个矩形
////        rectangles.add(new int[]{(int)(0.49*1920), (int)(0.17*1080), (int)(0.61*1920), (int)(0.93*1080)}); // 第二个矩形
//
//        // kimi  4.jpg
////        rectangles.add(new int[]{(int)(0.31*1920), (int)(0.30*1080), (int)(0.43*1920), (int)(0.84*1080)}); // 第二个矩形
//
//        // VL-72B 4.jpg
////        rectangles.add(new int[]{408, 197, 613, 615}); // 第二个矩形
//
//
//        // 智谱Ai glm-4v-flash  3(1366_768).jpg     以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{289, 344, 489, 544});
////        rectangles.add(new int[]{607, 345, 808, 546});
//
//        // 百川智能  3.jpg 以矩形框形式定位图中人物
////        rectangles.add(new int[]{(int)(0.38*1920), (int)(0.24*1080), (int)(0.52*1920), (int)(0.59*1080)}); // 第二个矩形
////        rectangles.add(new int[]{(int)(0.56*1920), (int)(0.27*1080), (int)(0.67*1920), (int)(0.73*1080)}); // 第二个矩形
//
//        // 文言一心  3.jpg 以矩形框形式定位图中人物,并且输出格式为(x1,y1,x2,y2)的坐标
////        rectangles.add(new int[]{(int)(0.2*1920), (int)(0.3*1080), (int)(0.4*1920), (int)(0.6*1080)}); // 第二个矩形
////        rectangles.add(new int[]{(int)(0.5*1920), (int)(0.3*1080), (int)(0.7*1920), (int)(0.6*1080)}); // 第二个矩形
//
//        // 吴恩达 在线api   4.jpg
////        rectangles.add(new int[]{572, 256, 913, 730});
//
//        // 小尺寸图片
////        rectangles.add(new int[]{76, 180, 109, 190});
//
//        //  香烟 3b 这个坐标偏离的需要调整！！！！！！
////        rectangles.add(new int[]{450, 189, 463, 207});
////        rectangles.add(new int[]{964, 430, 971, 438});
//
//
//        //  香烟 kimi
////        rectangles.add(new int[]{(int)(0.33*2560), (int)(0.36*1440), (int)(0.35*2560), (int)(0.38*1440)});
//
//
//
//        // 调用drawMultipleRectanglesOnImage方法进行绘制
//        drawImage.drawMultipleRectanglesOnImage(
//                "C://Users/asus/Desktop/xiyan.jpg", // 输入图片路径
//                rectangles,                     // 包含多个矩形坐标的列表
//                "C://Users/asus/Desktop/3_kimi_xiyan.jpg" // 输出图片路径
//        );
//    }
}