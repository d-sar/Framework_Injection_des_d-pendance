package core;

import annotation.Inject;
import annotation.SimplyAutoWired;
import annotation.SimplyComponent;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationContextAnnotation {


        private final Map<Class<?>, Object> objectRegistryMap = new HashMap<>();

        public ApplicationContextAnnotation(Class<?> configClass, String... additionalPackages) {
            initializeContext(configClass, additionalPackages);
        }

    private void initializeContext(Class<?> configClass, String... additionalPackages) {
        if (!configClass.isAnnotationPresent(SimplyComponent.class)) {
            throw new RuntimeException("Please provide a valid configuration file!");
        }

        // Scan the package of the configClass
        String mainPackage = configClass.getPackageName();
        System.out.println("Scanning package: " + mainPackage);
        Set<Class<?>> classes = scanPackage(mainPackage);

        // Scan additional packages
        for (String packageName : additionalPackages) {
            System.out.println("Scanning package: " + packageName);
            classes.addAll(scanPackage(packageName));
        }

        // Sort classes alphabetically to ensure consistent bean creation order
        List<Class<?>> sortedClasses = new ArrayList<>(classes);
        sortedClasses.sort(Comparator.comparing(Class::getName));

        // First pass: create beans for classes annotated with @SimplyComponent
        for (Class<?> clazz : sortedClasses) {
            if (clazz.isAnnotationPresent(SimplyComponent.class)) {
                System.out.println("Creating bean for class: " + clazz.getName());
                createBean(clazz);
            }
        }

        // Second pass: inject dependencies
        for (Object bean : objectRegistryMap.values()) {
            injectDependencies(bean);
        }
    }

        private Set<Class<?>> scanPackage(String packageName) {
            try {
                String path = packageName.replace(".", "/");
                URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(path);
                if (packageUrl == null) {
                    throw new RuntimeException("Package not found: " + packageName);
                }

                return Files.walk(Paths.get(packageUrl.toURI()))
                        .filter(Files::isRegularFile)
                        .map(file -> file.getFileName().toString())
                        .filter(name -> name.endsWith(".class"))
                        .map(name -> packageName + "." + name.replace(".class", ""))
                        .map(this::loadClass)
                        .peek(clazz -> System.out.println("Scanned class: " + clazz.getName())) // Debug log
                        .collect(Collectors.toSet());

            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Error scanning package: " + packageName, e);
            }
        }

        private Class<?> loadClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found: " + className, e);
            }
        }

    private void createBean(Class<?> clazz) {
        try {
            System.out.println("Creating bean for class: " + clazz.getName()); // Debug log
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();

            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(SimplyAutoWired.class)) {
                    // Injection par constructeur
                    Object instance = createInstanceWithConstructor(constructor);
                    registerBean(clazz, instance); // Register the bean
                    return;
                }
            }

            // Si aucun constructeur annoté @SimplyAutoWired, on utilise le constructeur par défaut
            Object instance = clazz.getDeclaredConstructor().newInstance();
            registerBean(clazz, instance); // Register the bean

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate class: " + clazz.getName(), e);
        }
    }

        private void registerBean(Class<?> clazz, Object instance) {
            // Register the bean by its concrete class
            objectRegistryMap.put(clazz, instance);
            System.out.println("Bean created and registered: " + clazz.getName());

            // Register the bean by all its interfaces
            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                objectRegistryMap.put(interfaceClass, instance);
                System.out.println("Bean registered by interface: " + interfaceClass.getName());
            }
        }

        private Object createInstanceWithConstructor(Constructor<?> constructor) throws IllegalAccessException, InvocationTargetException, InstantiationException {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = getBean(paramTypes[i]);
                if (params[i] == null) {
                    throw new RuntimeException("No bean found for dependency: " + paramTypes[i].getName());
                }
            }

            return constructor.newInstance(params);
        }

        private void injectDependencies(Object bean) {
            Class<?> clazz = bean.getClass();

            // Injection par champ
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getBean(field.getType());
                    if (dependency == null) {
                        throw new RuntimeException("No bean found for dependency: " + field.getType().getName());
                    }
                    field.setAccessible(true);
                    try {
                        field.set(bean, dependency);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                    }
                }
            }

            // Injection par méthode
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Inject.class)) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];

                    for (int i = 0; i < paramTypes.length; i++) {
                        params[i] = getBean(paramTypes[i]);
                        if (params[i] == null) {
                            throw new RuntimeException("No bean found for dependency: " + paramTypes[i].getName());
                        }
                    }

                    method.setAccessible(true);
                    try {
                        method.invoke(bean, params);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Failed to inject method: " + method.getName(), e);
                    }
                }
            }
        }

        public <T> T getBean(Class<T> clazz) {
            return clazz.cast(objectRegistryMap.get(clazz));
        }
    }
