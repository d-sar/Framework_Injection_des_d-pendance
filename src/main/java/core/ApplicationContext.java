package core;

import annotation.Inject;
import annotation.SimplyAutoWired;
import annotation.SimplyComponent;
import configXML.BeanConfig;
import configXML.BeansConfig;
import exceptions.BeanNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String configFile) throws Exception {
        loadBeansFromXML(configFile);
        injectDependencies();
    }

    private void loadBeansFromXML(String configFile) throws JAXBException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        JAXBContext context = JAXBContext.newInstance(BeansConfig.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Charge le fichier en tant que ressource
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
        if (inputStream == null) {
            throw new IllegalArgumentException("Fichier non trouvé : " + configFile);
        }

        BeansConfig beansConfig = (BeansConfig) unmarshaller.unmarshal(inputStream);
        System.out.println("Beans chargés : " + beansConfig.getBeans());

        for (BeanConfig beanConfig : beansConfig.getBeans()) {
            // Vérifie que l'ID et le nom de la classe sont présents
            if (beanConfig.getId() == null || beanConfig.getId().isEmpty()) {
                throw new IllegalArgumentException("L'ID est manquant pour un bean.");
            }
            if (beanConfig.getClassName() == null || beanConfig.getClassName().isEmpty()) {
                throw new IllegalArgumentException("Le nom de la classe est manquant pour le bean : " + beanConfig.getId());
            }

            // Charge la classe
            Class<?> clazz;
            try {
                clazz = Class.forName(beanConfig.getClassName());
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Classe non trouvée : " + beanConfig.getClassName(), e);
            }

            // Crée une instance de la classe
            Object instance = clazz.newInstance();
            beans.put(beanConfig.getId(), instance);
        }
    }

    private void injectDependencies() throws Exception {
        for (Object bean : beans.values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(SimplyComponent.class)) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        Object dependency = beans.get(field.getName());
                        if (dependency == null) {
                            throw new BeanNotFoundException("Bean not found for field: " + field.getName());
                        }
                        field.setAccessible(true);
                        field.set(bean, dependency);
                    }
                }

                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Inject.class)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] dependencies = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            dependencies[i] = beans.get(parameterTypes[i].getSimpleName());
                            if (dependencies[i] == null) {
                                throw new BeanNotFoundException("Bean not found for method parameter: " + parameterTypes[i].getSimpleName());
                            }
                        }
                        method.setAccessible(true);
                        method.invoke(bean, dependencies);
                    }
                }

                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.isAnnotationPresent(SimplyAutoWired.class)) {
                        Class<?>[] parameterTypes = constructor.getParameterTypes();
                        Object[] dependencies = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            dependencies[i] = beans.get(parameterTypes[i].getSimpleName());
                            if (dependencies[i] == null) {
                                throw new BeanNotFoundException("Bean not found for constructor parameter: " + parameterTypes[i].getSimpleName());
                            }
                        }
                        constructor.setAccessible(true);
                        constructor.newInstance(dependencies);
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}