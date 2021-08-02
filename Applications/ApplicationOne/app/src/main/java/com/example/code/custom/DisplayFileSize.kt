package com.example.code.custom

object DisplayFileSize {

    fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return when {
            bytes in 0 until kilobyte -> "$bytes B"
            bytes in kilobyte until megabyte -> (bytes / kilobyte).toString() + " KB"
            bytes in megabyte until gigabyte -> (bytes / megabyte).toString() + " MB"
            bytes in gigabyte until terabyte -> (bytes / gigabyte).toString() + " GB"
            bytes >= terabyte -> (bytes / terabyte).toString() + " TB"
            else -> "$bytes Bytes"
        }
    }

}