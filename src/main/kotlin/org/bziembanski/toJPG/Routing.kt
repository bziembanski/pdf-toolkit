package org.bziembanski.toJPG

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
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun Route.toJpg(service: ToJpgService) {
    route("/toJpg") {
        post {
            lateinit var fileName: String
            lateinit var path: Path
            lateinit var fileBytes: ByteArray
            val filesToDelete = mutableListOf<String>()
            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: ""
                        fileBytes = part.streamProvider().readBytes()
                        path = Paths.get(ToJpgService.uploadsDir, fileName)
                    }

                    else -> {}
                }
            }
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
            filesToDelete.add(path.toString())
            val list = service.pdfToImages(fileName).also{
                filesToDelete.addAll(it)
            }
            if (list.isNotEmpty()) {
                val name = service.imagesToPdf(fileName, list)
                filesToDelete.add(name)
                val image = File(name)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        name
                    )
                        .toString()
                )
                call.respondFile(image)
                withContext(Dispatchers.IO){
                    filesToDelete.forEach {
                        File(it).delete()
                    }
                }
            } else {
                call.respondText(text = "Błąd serwera", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}
