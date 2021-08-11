package com.example.code.custom

import java.io.File

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