/*package com.logitech.integration.test

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import java.util.*

class Tester {
    var appContext = InstrumentationRegistry.getInstrumentation().targetContext


    @Test
    fun runTests() {
        //content://com.logitech.test.support
        runAllTheTests()
    }

    private fun runAllTheTests() {
        var CONTENT_URI_START = Uri.parse("content://com.logitech.test.support/start")

        val values = ContentValues()
        values.put("package", "com.logitech.integration.test.test")
        values.put("class", "com.logitech.integration.test.audio.AudioTest")
        values.put("coverage", "false")
        values.put("test.runner", "androidx.test.runner.AndroidJUnitRunner")

        val uri = appContext.contentResolver.insert(CONTENT_URI_START, values)
        val runId = uri!!.getQueryParameter("runId")
        Log.d("KONGINTEGRATION", "start runId: $runId")

        var CONTENT_URI_REPORT = Uri.parse("content://com.logitech.test.support/report?runId=$runId")

        val builder = Uri.Builder()
        builder

            .authority("content://com.logitech.test.support")
            .appendPath("report")
            .appendQueryParameter("runId", runId)
        Log.d("KONGINTEGRATION", "sleeping 10")
        Thread.sleep(10000)
        Log.d("KONGINTEGRATION", "sleeping 10 over")

        Log.d("KONGINTEGRATION", "passing uri $CONTENT_URI_REPORT")

        val cursor =
            appContext.contentResolver.query(CONTENT_URI_REPORT, null, null, null, null)
        cursor?.let {
            Log.d("KONGINTEGRATION", "cursor is not empty $cursor")
            Log.d("KONGINTEGRATION", "cursor count is: ${cursor.count} " +
                    " , column count: ${cursor.columnCount} , column names: ${
                        Arrays.toString(cursor.columnNames)} ")


            if (cursor.moveToFirst()) {
                val strBuild = StringBuilder()
                while (!cursor.isAfterLast) {
                    strBuild.append(
                        """ 
      
    ${cursor.getString(cursor.getColumnIndex("testResult"))}
-${cursor.getString(cursor.getColumnIndex("className"))} 
-${cursor.getString(cursor.getColumnIndex("id"))} 
-${cursor.getString(cursor.getColumnIndex("numTest"))} 
-${cursor.getString(cursor.getColumnIndex("testName"))} 
    """.trimIndent()
                    )
                    cursor.moveToNext()
                }
                Log.d("KONGINTEGRATION", "returned query: $strBuild")
            }
            else {
                Log.d("KONGINTEGRATION", "No Records Found")
            }
        }
        Log.d("KONGINTEGRATION", "testing over")

    }

}*/
