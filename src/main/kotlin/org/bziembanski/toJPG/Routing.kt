package org.bziembanski.toJPG

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bziembanski.utils.Utility.FilesMethods
import java.nio.file.Path
import kotlin.io.path.name

fun Route.toJpg() {
    post("/toJpg") {
        val filesToDelete = mutableListOf<Path>()
        call.receiveMultipart().forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val fileName = part.originalFileName ?: ""

                    FilesMethods.generatePath(fileName).also { path ->
                        part.streamProvider().apply {
                            FilesMethods.saveRequestFile(readBytes(), path)
                            close()
                        }
                        filesToDelete.add(path)

                        val toJpg = ToJpg(path)

                        toJpg.pdfToImages().apply {
                            filesToDelete.addAll(this)
                        }

                        val pdf = toJpg.imagesToPdf()
                        filesToDelete.add(pdf)

                        call.response.header(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.Attachment.withParameter(
                                ContentDisposition.Parameters.FileName,
                                pdf.name
                            )
                                .toString()
                        )
                        call.respondFile(pdf.toFile())
                    }
                }

                else -> {}
            }
        }

        withContext(Dispatchers.IO) {
            filesToDelete.forEach {
                it.toFile().delete()
            }
        }
    }
}
