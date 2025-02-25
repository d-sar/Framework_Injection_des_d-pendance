package core;

import annotation.SimplyComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    Map<Class<?>,Object> objectRegistryMap= new HashMap<>();
    ApplicationContext(Class<?> clazz){
       // initializeContext(clazz);
    }
//    private void initializeContext(Class<?> clazz){
//        if (!clazz.isAnnotationPresent(SimplyComponent.class)){
//            throw new RuntimeException("Please provide a valid configuration file!");
//        }else {
//            String packageValue = clazz.getPackageName();
//            Set<Class<?>> classes =
//        }
//    }
}
