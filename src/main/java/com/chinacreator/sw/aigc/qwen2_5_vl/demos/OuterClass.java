package com.chinacreator.sw.aigc.qwen2_5_vl.demos;

/**
 * <p>Description: 描述</p>
 * <p>Copyright: Copyright (c) 2025</p>
 * <p>Company: ChinaCreator（湖南科创）</p>
 *
 * @author ycl
 * @version 1.0
 * @date 2025/2/24
 */

// 外部类：
public class OuterClass {
    private int x;
    public void outerMethod(){
        System.out.println("This is outer method");
    }

    //内部类
    public class InnerClass {
        public void innerMethod(){
            System.out.println("This is inner method");
            System.out.println("The value of outer class variable x is "+x);
        }
    }
}

