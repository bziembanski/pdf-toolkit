package org.bziembanski.utils

interface Constants {
    interface FileExtensions {
        companion object {
            const val imageExtension = "jpg"
            const val pdfExtension = "pdf"
        }
    }

    interface FileNames {
        companion object {
            const val uploadsDir = "uploads"
            const val newFileName = "-readOnly"
        }
    }

    interface Dimensions {
        companion object {
            const val dpi = 300f //resolution of JPGs
            const val ppi = 72f //standard PDF PPI
        }
    }
}