package com.example.appdevelopmentprojectfinal.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class JsonUtil {
    String internalFileName = "_internal.json";

    public void copyFileToInternalStorageIfNeeded(Context context, String assetFileName) {
        File file = context.getFileStreamPath(this.internalFileName);
        if (!file.exists()) {
            copyFileFromAssetsToInternalStorage(context, assetFileName);
        } else {
            // File already exists, handle accordingly (e.g., log a message)
            Log.e("MainActivity", "File already exists in internal storage: " + this.internalFileName);
        }
    }
    public void copyFileFromAssetsToInternalStorage(Context context, String assetFileName) {
        InputStream in = null;
        OutputStream out = null;
        try {
            // Open the input stream from assets folder
            in = context.getAssets().open(assetFileName);

            // Open the output stream to the internal storage directory
            out = new FileOutputStream(context.getFileStreamPath(this.internalFileName));

            // Copy the content of the file
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public String readFileFromInternalStorage(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(this.internalFileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            char[] buffer = new char[1024];
            int read;
            while ((read = inputStreamReader.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, read);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public void writeFileToInternalStorage(Context context, String content) {
        try {
            FileOutputStream fos = context.openFileOutput(this.internalFileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Update the "show" value for a specific module and save the updated file
    public void updateShowStatusAndSave(Context context, String moduleCode, boolean newShowValue) {
        // Step 1: Read the current content of the timetable.json file
        String timetableContent = readFileFromInternalStorage(context);

        try {
            // Step 2: Parse the JSON content
            JSONObject timetable = new JSONObject(timetableContent);

            // Step 3: Get the modules array from the JSON object
            JSONArray modules = timetable.getJSONArray("modules");

            // Step 4: Loop through the modules and find the module with the matching code
            for (int i = 0; i < modules.length(); i++) {
                JSONObject module = modules.getJSONObject(i);

                // Check if the module code matches the provided code
                if (module.getString("code").equals(moduleCode)) {
                    System.out.println("Current: " + module.getString("show") + ", New: " + newShowValue);
                    // Step 5: Update the "show" field for the found module
                    module.put("show", newShowValue);

                    // Step 6: Convert the updated JSON object back to a string
                    String updatedContent = timetable.toString();

                    // Step 7: Write the updated content back to internal storage
                    writeFileToInternalStorage(context, updatedContent);
                    break;  // Stop after finding and updating the first match
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getShowStatusForModule(Context context, String moduleCode) {
        String content = readFileFromInternalStorage(context);
        try {
            JSONObject timetable = new JSONObject(content);
            JSONArray modules = timetable.getJSONArray("modules");

            for (int i = 0; i < modules.length(); i++) {
                JSONObject module = modules.getJSONObject(i);
                if (module.getString("code").equals(moduleCode)) {
                    return module.getBoolean("show");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if not found or error occurs
    }

}
