package org.bziembanski.toJPG

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO


class ToJpgService {
    fun pdfToImages(fileName: String): List<String> {
        val bytes = Files.readAllBytes(Paths.get(uploadsDir, fileName))
        val document: PDDocument = Loader.loadPDF(bytes)
        val pdfRenderer = PDFRenderer(document)
        val imagesList = mutableListOf<String>()
        for (i in 0 until document.numberOfPages) {
            val bim = pdfRenderer.renderImageWithDPI(i, DPI, ImageType.RGB)
            val path = Paths.get(uploadsDir, "$fileName$i.$imageExtension")
            ImageIOUtil.writeImage(bim, path.toString(), DPI.toInt())
            imagesList.add(path.toString())
        }
        document.close()
        return imagesList
    }

    fun imagesToPdf(fileName: String, list: List<String>): String {
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

        val path = Paths.get(uploadsDir, "$fileName$newFileName.pdf")
        document.save(path.toString())
        document.close()
        return path.toString()
    }

    private fun Int.getPDSize(): Float {
        return (this.toFloat() / DPI) * PPI
    }

    companion object {
        const val DPI = 300f //resolution of JPGs
        const val PPI = 72f //standard PDF PPI
        const val imageExtension = "jpg"
        const val uploadsDir = "uploads"
        const val newFileName = "-readOnly"
    }
}