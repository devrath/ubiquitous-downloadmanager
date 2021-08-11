package com.example.code.custom

import android.content.Context
import android.widget.Toast
import java.io.File

/**
 * Deleting a directory and its contents
 */
fun File.deleteDirectoryFiles(){
    this.listFiles().forEach {
        if(it.isDirectory){
            it.deleteDirectoryFiles()
        }else{
            it.delete()
        }
    }
    this.delete()
}

/**
 * Toast extension
 **/
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, message , duration).show()
}