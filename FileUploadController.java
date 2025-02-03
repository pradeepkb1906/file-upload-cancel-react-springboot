package com.example.upload;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ConcurrentHashMap<String, Future<?>> uploadTasks = new ConcurrentHashMap<>();

    @PostMapping("/start")
    public String startUpload(@RequestParam("uploadId") String uploadId) {
        Future<?> future = executorService.submit(() -> {
            try {
                for (int i = 0; i < 10; i++) { // Simulating multiple file uploads
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Upload cancelled: " + uploadId);
                        return;
                    }
                    System.out.println("Uploading chunk " + (i + 1));
                    Thread.sleep(1000); // Simulate file upload delay
                }
                System.out.println("Upload completed: " + uploadId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupted status
                System.out.println("Upload interrupted: " + uploadId);
            }
        });

        uploadTasks.put(uploadId, future);
        return "Upload started: " + uploadId;
    }

    @PostMapping("/cancel/{uploadId}")
    public String cancelUpload(@PathVariable String uploadId) {
        Future<?> future = uploadTasks.remove(uploadId);
        if (future != null) {
            future.cancel(true); // Attempt to cancel
            return "Upload cancelled: " + uploadId;
        }
        return "Upload ID not found or already completed.";
    }
}
