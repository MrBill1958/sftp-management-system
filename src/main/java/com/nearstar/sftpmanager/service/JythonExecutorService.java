package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.entity.ScheduledTask;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JythonExecutorService {

    private final EncryptionUtil encryptionUtil;

    public ExecutionResult executeScript(ScheduledTask task) {
        ExecutionResult result = new ExecutionResult();
        StringWriter outputWriter = new StringWriter();

        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            // Set output to capture script output
            interpreter.setOut(outputWriter);
            interpreter.setErr(outputWriter);

            // Set up script context
            Site site = task.getSite();
            interpreter.set("site_name", site.getSiteName());
            interpreter.set("site_host", site.getIpAddress());
            interpreter.set("site_port", site.getPort());
            interpreter.set("site_username", site.getUsername());
            interpreter.set("site_password", encryptionUtil.decrypt(site.getEncryptedPassword()));
            interpreter.set("site_path", site.getTargetPath());

            // Parse command line parameters
            if (task.getCommandLineParams() != null) {
                Map<String, String> params = parseCommandLineParams(task.getCommandLineParams());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    interpreter.set(entry.getKey(), entry.getValue());
                }
            }

            // Execute the script
            interpreter.exec(task.getJythonScript());

            // Get execution result
            PyObject pyResult = interpreter.get("result");
            if (pyResult != null) {
                result.setResult(pyResult.toString());
            }

            result.setSuccess(true);
            result.setOutput(outputWriter.toString());

        } catch (Exception e) {
            log.error("Error executing Jython script for task: " + task.getTaskName(), e);
            result.setSuccess(false);
            result.setError(e.getMessage());
            result.setOutput(outputWriter.toString());
        }

        return result;
    }

    private Map<String, String> parseCommandLineParams(String params) {
        Map<String, String> paramMap = new HashMap<>();

        if (params != null && !params.trim().isEmpty()) {
            String[] pairs = params.split("\\s+");
            for (String pair : pairs) {
                if (pair.contains("=")) {
                    String[] keyValue = pair.split("=", 2);
                    paramMap.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
                }
            }
        }

        return paramMap;
    }

    @lombok.Data
    public static class ExecutionResult {
        private boolean success;
        private String output;
        private String error;
        private String result;
    }
}