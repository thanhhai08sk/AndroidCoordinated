package org.de_studio.diary.base.architecture

import org.de_studio.diary.utils.extensionFunction.getAppContext
import org.de_studio.diary.utils.extensionFunction.getBackupFolder
import java.io.File

/**
 * Created by HaiNguyen on 11/19/17.
 */
object Environment {
    private val appContext = getAppContext()
    fun getString(id: Int): String = appContext.getString(id)
    fun getString(id: Int, vararg formatArgs: Any): String = appContext.getString(id, *formatArgs)
    fun getBackupXmlFiles(): Iterable<File> =
            getBackupXmlFolder().listFiles().asIterable()
    fun getBackupXmlFolder() : File = File(appContext.getBackupFolder(), "Xml")
}