package com.example.toJPG

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun Route.toJpg(service: ToJpgService) {
    route("/toJpg") {
        post {
            var fileName: String
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: ""
                        val fileBytes = part.streamProvider().readBytes()
                        val path = Paths.get("uploads", fileName)
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
                            val list = service.pdfToImages(fileName)
                            val name = service.imagesToPdf(fileName, list)
                            val file = File(name)
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    name
                                )
                                    .toString()
                            )
                            call.respondFile(file)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}