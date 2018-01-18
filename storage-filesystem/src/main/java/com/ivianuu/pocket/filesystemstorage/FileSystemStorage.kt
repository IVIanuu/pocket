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

package com.ivianuu.pocket.filesystemstorage

import com.ivianuu.pocket.Storage
import java.io.*

/**
 * A [Storage] which uses the filesystem internally
 */
class FileSystemStorage private constructor(private val rootDir: File) : Storage {

    init {
        rootDir.createParentDirs()
    }

    @Synchronized override fun put(key: String, value: String) {
        write(value, getFile(key))
    }

    @Synchronized override fun get(key: String): String? {
        val file = getFile(key)
        return if (file.exists()) {
            return read(file)
        } else {
            null
        }
    }

    @Synchronized override fun delete(key: String) {
        val file = getFile(key)
        if (!file.exists()) {
            return
        }

        if (!file.delete()) {
            throw IOException("Couldn't delete file for key " + key)
        }
    }

    @Synchronized override fun deleteAll() {
        rootDir.clean()
    }

    @Synchronized override fun contains(key: String): Boolean = getFile(key).exists()

    @Synchronized override fun getAllKeys(): List<String> =
        rootDir.list()?.toList() ?: emptyList()

    private fun read(file: File): String {
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            val value = StringBuilder()
            var line = reader.readLine()
            while (line != null) {
                value.append(line).append('\n')
                line = reader.readLine()
            }

            reader.close()
            return value.toString()
        } catch (e: Exception) {
            throw IOException("failed to read file ${file.path}")
        }
    }

    private fun write(value: String, file: File) {
        val tmpFile = File.createTempFile(
            "new", "tmp", file.parentFile)
        try {
            BufferedOutputStream(FileOutputStream(tmpFile)).run {
                write(value.toByteArray())
                flush()
                close()
            }
            if (!tmpFile.renameTo(file)) {
                throw IOException("unable to move tmp file to " + file.path)
            }
        } catch (e: Exception) {
            throw IOException("unable to write to file", e)
        }
    }

    private fun getFile(key: String): File {
        val file = File(rootDir.path + File.separator + key)
        file.createParentDirs()
        return file
    }

    private fun File.clean() {
        listFiles()?.let { files ->
            files.forEach { file ->
                if (file.isDirectory) {
                    file.clean()
                } else {
                    file.delete()
                }
            }
        }
    }

    private fun File.createParentDirs() {
        val parent = canonicalFile.parentFile ?: return
        parent.mkdirs()
        if (!parent.isDirectory) {
            throw IOException("Unable to create parent directories of $this")
        }
    }

    companion object {
        /**
         * Returns a new [Storage]
         */
        @JvmStatic
        fun create(rootDir: File): Storage {
            return FileSystemStorage(rootDir)
        }
    }
}