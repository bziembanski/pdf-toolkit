package org.bziembanski.toJPG

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import org.bziembanski.utils.Constants
import org.bziembanski.utils.Utility.FilesMethods
import org.bziembanski.utils.Utility.PdfMethods.Companion.toPDSize
import java.nio.file.Files
import java.nio.file.Path

interface ToJpg {
    companion object {
        suspend fun pdfToImages(fileName: String): List<String> {
            return withContext(Dispatchers.IO) {
                val imagesList = mutableListOf<String>()
                var document: PDDocument? = null
                try {
                    Files.newInputStream(
                        FilesMethods.generatePath(fileName)
                    ).apply {
                        document = Loader.loadPDF(
                            this,
                            MemoryUsageSetting.setupTempFileOnly()
                        )
                        val pdfRenderer = PDFRenderer(document).apply {
                            isSubsamplingAllowed = true
                        }

                        document?.pages?.forEachIndexed { index, _ ->
                            val bim = pdfRenderer.renderImageWithDPI(index, Constants.Dimensions.dpi, ImageType.RGB)
                            val path = FilesMethods.generatePath(
                                "$fileName$index.${Constants.FileExtensions.imageExtension}"
                            )

                            val wasWritten = ImageIOUtil.writeImage(
                                bim,
                                path.toString(),
                                Constants.Dimensions.dpi.toInt()
                            )
                            if (wasWritten) {
                                imagesList.add(path.toString())
                            }
                        }
                        close()
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
                    val pdImageXObject: PDImageXObject
                    Files.newInputStream(Path.of(path)).apply {
                        pdImageXObject = JPEGFactory.createFromStream(document, this)
                        close()
                    }

                    val rectangle = PDRectangle(
                        pdImageXObject.image.width.toPDSize(),
                        pdImageXObject.image.height.toPDSize()
                    )
                    val page = PDPage(rectangle)

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
                val path = FilesMethods.generatePath(
                    "$fileName${Constants.FileNames.newFileName}.${Constants.FileExtensions.pdfExtension}"
                )
                document.save(path.toString())
                document.close()
                path.toString()
            }
        }
    }
}