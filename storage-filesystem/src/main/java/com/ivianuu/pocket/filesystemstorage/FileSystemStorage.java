/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.pocket.filesystemstorage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ivianuu.pocket.Storage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ivianuu.preconditions.Preconditions.checkNotNull;

/**
 * Storage implementation
 */
public final class FileSystemStorage implements Storage {

    private static final String DEFAULT_FILES_DIR = "pocket";
    private static final String FILE_EXT = ".pt";
    private static final String BAK_EXT = ".bak";

    private final File filesDir;
    private boolean pocketDirCreated;

    private FileSystemStorage(File filesDir) {
        this.filesDir = filesDir;
        pocketDirCreated = filesDir.exists();
    }

    /**
     * Returns a new file system storage
     * This will use the default files name
     */
    @NonNull
    public static Storage create(@NonNull Context context) {
        checkNotNull(context, "context == null");
        File filesDir = new File(
                context.getApplicationContext().getFilesDir().getPath() + File.separator + DEFAULT_FILES_DIR);
        return create(filesDir);
    }

    /**
     * Returns a new file system storage
     */
    @NonNull
    public static Storage create(@NonNull Context context, @NonNull String name) {
        checkNotNull(context, "context == null");
        checkNotNull(name, "name == null");
        File filesDir = new File(
                context.getApplicationContext().getFilesDir().getPath() + File.separator + name);
        return create(filesDir);
    }

    /**
     * Returns a new file system storage
     */
    @NonNull
    public static Storage create(@NonNull File filesDir) {
        checkNotNull(filesDir, "filesDir == null");
        return new FileSystemStorage(filesDir);
    }

    @Override
    public void put(@NonNull String key, @NonNull String value) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        final File backupFile = makeBackupFile(originalFile);
        // Rename the current file so it may be used as a backup during the next read
        if (originalFile.exists()) {
            //Rename original to backup
            if (!backupFile.exists()) {
                if (!originalFile.renameTo(backupFile)) {
                    throw new IllegalStateException("Couldn't rename file " + originalFile
                            + " to backup file " + backupFile);
                }
            } else {
                //Backup exist -> original file is broken and must be deleted
                //noinspection ResultOfMethodCallIgnored
                originalFile.delete();
            }
        }

        writeTableFile(key, value, originalFile, backupFile);
    }

    @Nullable
    @Override
    public String get(@NonNull String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        final File backupFile = makeBackupFile(originalFile);
        if (backupFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            originalFile.delete();
            //noinspection ResultOfMethodCallIgnored
            backupFile.renameTo(originalFile);
        }

        if (!contains(key)) {
            return null;
        }

        return readTableFile(key, originalFile);
    }

    @Override
    public void delete(@NonNull String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        if (!originalFile.exists()) {
            return;
        }

        boolean deleted = originalFile.delete();
        if (!deleted) {
            throw new IllegalArgumentException("Couldn't delete file " + originalFile
                    + " for table " + key);
        }
    }

    @Override
    public void deleteAll() {
        assertInit();
        deleteDirectory(filesDir);
        pocketDirCreated = false;
    }

    @Override
    public boolean contains(@NonNull String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists();
    }

    @NonNull
    @Override
    public List<String> getAllKeys() {
        assertInit();

        String[] files = filesDir.list();
        if (files != null) {
            //remove extensions
            for (int i = 0; i < files.length; i++) {
                files[i] = files[i].replace(FILE_EXT, "");
            }
            return Arrays.asList(files);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        String[] files = filesDir.list();
        return files != null ? files.length : 0;
    }

    private File getOriginalFile(String key) {
        final String tablePath = filesDir.getPath() + File.separator + key + FILE_EXT;
        return new File(tablePath);
    }

    private void writeTableFile(String key, String value, File originalFile, File backupFile) {
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(originalFile));
            outputStream.write(value.getBytes());
            outputStream.flush();
            outputStream.close(); //also close file stream

            // Writing was successful, delete the backup file if there is one.
            //noinspection ResultOfMethodCallIgnored
            backupFile.delete();
        } catch (IOException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IllegalArgumentException("Couldn't clean up partially-written file "
                            + originalFile, e);
                }
            }
            throw new IllegalArgumentException("Couldn't save table: " + key + ". " +
                    "Backed up table will be used on next read attempt", e);
        }
    }

    private String readTableFile(String key, File originalFile) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)));
            StringBuilder value = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                value.append(line).append('\n');
            }

            reader.close();
            return value.toString();
        } catch (FileNotFoundException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IllegalArgumentException("Couldn't clean up broken file "
                            + originalFile, e);
                }
            }
            throw new IllegalStateException("Couldn't read file " + originalFile
                    + " for table " + key, e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void assertInit() {
        if (!pocketDirCreated) {
            createPocketDir();
            pocketDirCreated = true;
        }
    }

    private void createPocketDir() {
        if (!filesDir.exists()) {
            boolean isReady = filesDir.mkdirs();
            if (!isReady) {
                throw new RuntimeException("Couldn't create Pocket dir: " + filesDir.getPath());
            }
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        directory.delete();
    }

    private File makeBackupFile(File originalFile) {
        return new File(originalFile.getPath() + BAK_EXT);
    }
}