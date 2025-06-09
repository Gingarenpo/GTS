package com.gfactory.core.mqo;

import java.io.IOException;

public class Tmp {

    public static void main(String[] args) throws IOException, MQOException {
        MQO mqo = MQOLoader.load(ClassLoader.getSystemResourceAsStream("assets/gts/dummy/trafficcontroller.mqo"), true);
        System.out.println(mqo);
    }
}
