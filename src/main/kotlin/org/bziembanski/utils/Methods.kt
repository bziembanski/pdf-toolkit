package org.bziembanski.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

interface Utility {
    interface FilesMethods {
        companion object {
            suspend fun saveRequestFile(fileBytes: ByteArray, path: Path) {
                withContext(Dispatchers.IO) {
                    Files.createDirectories(path.parent)
                    DataOutputStream(
                        Files.newOutputStream(
                            path,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE
                        )
                    ).apply {
                        write(fileBytes)
                        flush()
                        close()
                    }
                }
            }

            fun generatePath(fileName: String): Path = Paths.get(
                Constants.FileNames.uploadsDir,
                fileName
            )
        }
    }

    interface PdfMethods {
        companion object {
            fun Int.toPDSize(): Float = (this.toFloat() / Constants.Dimensions.dpi) * Constants.Dimensions.ppi


        }
    }


}