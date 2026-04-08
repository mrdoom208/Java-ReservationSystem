package com.mycompany.reservationsystem.util;

import com.mycompany.reservationsystem.controller.main.AdministratorUIController;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundViewLoader {

    private static final Duration TRANSITION_DURATION = Duration.millis(200);
    private static final int MAX_CACHE_SIZE = 10;
    
    private final Map<String, Object> controllerCache;
    private final Map<String, Node> viewCache;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private volatile Task<ViewLoadResult> activeTask;
    private AdministratorUIController adminUIController;

    public BackgroundViewLoader() {
        controllerCache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
        viewCache = Collections.synchronizedMap(new LinkedHashMap<String, Node>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Node> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    }

    public void setAdminUIController(AdministratorUIController adminUIController) {
        this.adminUIController = adminUIController;
    }

    public void loadViewAsync(String fxmlFile, StackPane content, Runnable onComplete) {

        if (activeTask != null && activeTask.isRunning()) {
            activeTask.cancel();
        }

        if (adminUIController != null) {
            adminUIController.setNavigationLoading(true);
            adminUIController.disableNavButtons(true);
        }

        Node cachedView = viewCache.get(fxmlFile);
        if (cachedView != null) {
            if (adminUIController != null) {
                adminUIController.setNavigationLoading(false);
                adminUIController.disableNavButtons(false);
            }
            animateViewSwitch(content, cachedView, onComplete);
            return;
        }

        activeTask = new Task<>() {
            @Override
            protected ViewLoadResult call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                configureControllerFactory(loader);
                Node view = loader.load();
                Object controller = loader.getController();
                return new ViewLoadResult(view, controller);
            }
        };

        activeTask.setOnSucceeded(e -> {
            ViewLoadResult result = activeTask.getValue();
            viewCache.put(fxmlFile, result.view);
            controllerCache.put(fxmlFile, result.controller);
            if (adminUIController != null) {
                adminUIController.setNavigationLoading(false);
                adminUIController.disableNavButtons(false);
            }
            // Use PauseTransition to defer animation slightly (non-blocking)
            PauseTransition delay = new PauseTransition(Duration.millis(30));
            delay.setOnFinished(ev -> animateViewSwitch(content, result.view, onComplete));
            delay.play();
        });

        activeTask.setOnFailed(e -> {
            if (adminUIController != null) {
                adminUIController.setNavigationLoading(false);
                adminUIController.disableNavButtons(false);
            }
            activeTask.getException().printStackTrace();
            content.getChildren().clear();
        });

        executor.submit(activeTask);
    }

    private void configureControllerFactory(FXMLLoader loader) {
        loader.setControllerFactory(param -> {
            try {
                Constructor<?> constructor = param.getConstructor();
                Object controller = constructor.newInstance();
                if (controller instanceof AdministratorUIController) {
                    return controller;
                }
                java.lang.reflect.Method setMethod = param.getMethod("setAdminUIController", AdministratorUIController.class);
                setMethod.invoke(controller, adminUIController);
                return controller;
            } catch (Exception e) {
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot create controller: " + param.getName(), ex);
                }
            }
        });
    }

    private void animateViewSwitch(StackPane content, Node newView, Runnable onComplete) {
        Node oldView = content.getChildren().isEmpty() ? null : content.getChildren().get(0);

        if (oldView == null) {
            newView.setOpacity(0);
            newView.setScaleX(0.95);
            newView.setScaleY(0.95);
            content.getChildren().setAll(newView);
            animateFadeIn(newView, onComplete);
            return;
        }

        ParallelTransition fadeOut = createFadeTransition(oldView, 1, 0, TRANSITION_DURATION);
        
        fadeOut.setOnFinished(e -> {
            newView.setOpacity(0);
            newView.setScaleX(0.95);
            newView.setScaleY(0.95);
            content.getChildren().setAll(newView);
            animateFadeIn(newView, onComplete);
        });

        fadeOut.play();
    }

    private void animateFadeIn(Node node, Runnable onComplete) {
        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION, node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(TRANSITION_DURATION, node);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        ParallelTransition transition = new ParallelTransition(fadeIn, scaleIn);
        transition.setOnFinished(e -> {
            node.setScaleX(1);
            node.setScaleY(1);
            if (onComplete != null) onComplete.run();
        });
        transition.play();
    }

    private ParallelTransition createFadeTransition(Node node, double from, double to, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(from);
        fade.setToValue(to);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        
        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        
        return new ParallelTransition(fade, scale);
    }

    public void preloadView(String fxmlFile) {
        if (viewCache.containsKey(fxmlFile)) return;

        Task<ViewLoadResult> preloadTask = new Task<>() {
            @Override
            protected ViewLoadResult call() throws Exception {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                configureControllerFactory(loader);
                Node view = loader.load();
                Object controller = loader.getController();
                return new ViewLoadResult(view, controller);
            }
        };

        preloadTask.setOnSucceeded(e -> {
            ViewLoadResult result = preloadTask.getValue();
            viewCache.put(fxmlFile, result.view);
            controllerCache.put(fxmlFile, result.controller);
        });

        preloadTask.setOnFailed(e -> preloadTask.getException().printStackTrace());

        executor.submit(preloadTask);
    }

    public void preloadViews(String... fxmlFiles) {
        for (String fxml : fxmlFiles) preloadView(fxml);
    }

    public Object getCachedController(String fxmlFile) {
        return controllerCache.get(fxmlFile);
    }

    public void clearCache() {
        viewCache.clear();
        controllerCache.clear();
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static class ViewLoadResult {
        final Node view;
        final Object controller;
        ViewLoadResult(Node view, Object controller) {
            this.view = view;
            this.controller = controller;
        }
    }
}
