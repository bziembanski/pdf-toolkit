package org.bziembanski.toJPG

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.io.DataOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.imageio.ImageIO

fun Route.toJpg() {
    val dpi = 300f //resolution of JPGs
    val ppi = 72f //standard PDF PPI

    val imageExtension = "jpg"
    val pdfExtension = "pdf"

    val uploadsDir = "uploads"

    val newFileName = "-readOnly"

    fun Int.getPDSize(): Float {
        return (this.toFloat() / dpi) * ppi
    }

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

    suspend fun pdfToImages(fileName: String): List<String> {
        return withContext(Dispatchers.IO) {
            val imagesList = mutableListOf<String>()
            var document: PDDocument? = null
            try {
                document = Loader.loadPDF(
                    Files.newInputStream(
                        Paths.get(
                            uploadsDir,
                            fileName
                        )
                    ),
                    MemoryUsageSetting.setupTempFileOnly()
                )
                val pdfRenderer = PDFRenderer(document).apply {
                    isSubsamplingAllowed = true
                }

                document.pages.forEachIndexed { index, _ ->
                    val bim = pdfRenderer.renderImageWithDPI(index, dpi, ImageType.RGB)
                    val path = Paths.get(uploadsDir, "$fileName$index.$imageExtension")
                    ImageIOUtil.writeImage(bim, path.toString(), dpi.toInt())
                    imagesList.add(path.toString())
                }
            } catch (e: Error) {
                e.printStackTrace()
            } finally {
                document?.close()
            }
            imagesList
        }
    }

    suspend fun imagesToPdf(fileName: String, list: List<String>): String {
        return withContext(Dispatchers.IO) {
            val document = PDDocument()
            list.forEach { path ->
                val image = ImageIO.read(File(path))

                val rectangle = PDRectangle(
                    image.width.getPDSize(),
                    image.height.getPDSize()
                )
                val page = PDPage(rectangle)

                val pdImageXObject = JPEGFactory.createFromImage(document, image)
                PDRectangle()
                PDPageContentStream(document, page).apply {
                    drawImage(
                        pdImageXObject,
                        0f,
                        0f,
                        rectangle.width,
                        rectangle.height,
                    )
                    close()
                }
                document.addPage(page)
            }
            val path = Paths.get(uploadsDir, "$fileName$newFileName.$pdfExtension")
            document.save(path.toString())
            document.close()
            path.toString()
        }

    }

    route("/toJpg") {
        post {
            val filesToDelete = mutableListOf<String>()
            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName ?: ""
                        val fileBytes = part.streamProvider().readBytes()
                        val path = Paths.get(uploadsDir, fileName)

                        saveRequestFile(fileBytes, path)
                        filesToDelete.add(path.toString())

                        pdfToImages(fileName).also { list ->
                            filesToDelete.addAll(list)

                            if (list.isNotEmpty()) {
                                val name = imagesToPdf(fileName, list)
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
}
