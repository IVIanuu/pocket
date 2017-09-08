package com.ivianuu.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

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

/**
 * Store implementation
 */
final class RealStorage<T> implements Storage<T> {

    private final Context context;
    private final String dbName;
    private final Gson gson;
    private final Class<T> clazz;
    
    private String filesDir;
    private boolean pocketDirCreated;
    
    RealStorage(@NonNull Context context,
                @NonNull String dbName,
                @NonNull Gson gson,
                @NonNull Class<T> clazz) {
        this.context = context;
        this.dbName = dbName;
        this.gson = gson;
        this.clazz = clazz;
    }

    @Override
    public void put(@NonNull String key, @NonNull T value) {
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
    public T get(@NonNull String key) {
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

        final String dbPath = getDbPath(context, dbName);
        deleteDirectory(dbPath);
        pocketDirCreated = false;
    }

    @Override
    public boolean contains(@NonNull String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists();
    }

    @Override
    public synchronized long lastModified(@NonNull String key) {
        assertInit();

        final File originalFile = getOriginalFile(key);
        return originalFile.exists() ? originalFile.lastModified() : -1;
    }

    @NonNull
    @Override
    public List<String> getAllKeys() {
        assertInit();

        File bookFolder = new File(filesDir);
        String[] names = bookFolder.list();
        if (names != null) {
            //remove extensions
            for (int i = 0; i < names.length; i++) {
                names[i] = names[i].replace(".pt", "");
            }
            return Arrays.asList(names);
        } else {
            return new ArrayList<>();
        }
    }

    private File getOriginalFile(String key) {
        final String tablePath = filesDir + File.separator + key + ".pt";
        return new File(tablePath);
    }
    
    private void writeTableFile(String key, T value, File originalFile, File backupFile) {
        try {
            FileOutputStream fileStream = new FileOutputStream(originalFile);
            fileStream.write(gson.toJson(value, clazz).getBytes());
            fileStream.flush();
            sync(fileStream);
            fileStream.close(); //also close file stream

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

    private T readTableFile(String key, File originalFile) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                json.append(line).append('\n');
            }
            T value = gson.fromJson(json.toString(), clazz);

            r.close();
            return value;
        } catch (FileNotFoundException e) {
            // Clean up an unsuccessfully written file
            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IllegalArgumentException("Couldn't clean up broken/unserializable file "
                            + originalFile, e);
                }
            }
            String errorMessage = "Couldn't read/deserialize file "
                    + originalFile + " for table " + key;
            throw new IllegalStateException(errorMessage, e);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private String getDbPath(Context context, String dbName) {
        return context.getFilesDir() + File.separator + dbName;
    }

    private void assertInit() {
        if (!pocketDirCreated) {
            createPaperDir();
            pocketDirCreated = true;
        }
    }

    private void createPaperDir() {
        filesDir = getDbPath(context, dbName);
        if (!new File(filesDir).exists()) {
            boolean isReady = new File(filesDir).mkdirs();
            if (!isReady) {
                throw new RuntimeException("Couldn't create Pocket dir: " + filesDir);
            }
        }
    }

    private static void deleteDirectory(String dirPath) {
        File directory = new File(dirPath);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.toString());
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
        return new File(originalFile.getPath() + ".bak");
    }
    
    private static void sync(FileOutputStream stream) {
        //noinspection EmptyCatchBlock
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
        } catch (IOException e) {
        }
    }
}

