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
import java.io.File

fun Route.toJpg() {
    post("/toJpg") {
        val filesToDelete = mutableListOf<String>()
        call.receiveMultipart().forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val fileName = part.originalFileName ?: ""

                    FilesMethods.generatePath(fileName).also { path ->
                        FilesMethods.saveRequestFile(part.streamProvider().readBytes(), path)
                        filesToDelete.add(path.toString())
                    }

                    ToJpg.pdfToImages(fileName).also { list ->
                        if (list.isNotEmpty()) {
                            ToJpg.imagesToPdf(fileName, list).also { name ->
                                call.response.header(
                                    HttpHeaders.ContentDisposition,
                                    ContentDisposition.Attachment.withParameter(
                                        ContentDisposition.Parameters.FileName,
                                        name
                                    )
                                        .toString()
                                )
                                call.respondFile(File(name))
                                filesToDelete.add(name)
                            }
                            filesToDelete.addAll(list)
                        } else {
                            call.respondText(
                                text = "Błąd serwera",
                                status = HttpStatusCode.InternalServerError
                            )
                        }
                    }
                }

                else -> {}
            }
        }
        withContext(Dispatchers.IO) {
            filesToDelete.forEach {
                File(it).delete()
            }
        }
    }
}
